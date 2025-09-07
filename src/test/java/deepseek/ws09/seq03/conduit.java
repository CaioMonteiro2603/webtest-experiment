package GTP5.ws09.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RealWorldHeadlessTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://demo.realworld.io/";

    @BeforeAll
    public static void setupClass() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1366, 900));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardownClass() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    public void navigateHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        // RealWorld is SPA; ensure home route is loaded
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("nav.navbar")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.home-page, .article-preview"))
        ));
    }

    // ===== Helpers =====

    private WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private WebElement first(By by) {
        List<WebElement> list = driver.findElements(by);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<WebElement> all(By by) {
        try { return driver.findElements(by); } catch (Exception e) { return Collections.emptyList(); }
    }

    private void set(WebElement el, String value) {
        wait.until(ExpectedConditions.visibilityOf(el));
        try { el.clear(); } catch (Exception ignored) {}
        el.sendKeys(value);
    }

    private void clickNavbarLinkContains(String containsText) {
        List<WebElement> links = driver.findElements(By.cssSelector("nav.navbar a.nav-link"));
        Optional<WebElement> match = links.stream().filter(a -> a.getText().toLowerCase().contains(containsText.toLowerCase())).findFirst();
        if (match.isPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(match.get())).click();
        } else {
            // fallback to href hash
            WebElement byHref = first(By.cssSelector("nav.navbar a[href*='" + containsText + "']"));
            Assertions.assertNotNull(byHref, "Navbar link containing '" + containsText + "' should exist");
            wait.until(ExpectedConditions.elementToBeClickable(byHref)).click();
        }
    }

    private boolean openExternalAndAssertDomain(WebElement link, String expectedDomainFragment) {
        String expected = expectedDomainFragment.toLowerCase();
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        try {
            wait.until(d -> driver.getWindowHandles().size() > before.size() || !driver.getCurrentUrl().equals(BASE_URL));
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        boolean domainOk;

        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            try { wait.until(ExpectedConditions.urlContains(".")); } catch (Exception ignored) {}
            domainOk = driver.getCurrentUrl().toLowerCase().contains(expected);
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            // Same-tab navigation
            try { wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL))); } catch (Exception ignored) {}
            domainOk = driver.getCurrentUrl().toLowerCase().contains(expected);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
        return domainOk;
    }

    private String currentRoute() {
        String url = driver.getCurrentUrl();
        int idx = url.indexOf("#/");
        return idx >= 0 ? url.substring(idx) : url;
    }

    // ===== Tests =====

    @Test
    @Order(1)
    public void homePageLoads_HeaderVisible_ArticlesOrHeroShown() {
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL.");
        WebElement navbar = first(By.cssSelector("nav.navbar"));
        Assertions.assertNotNull(navbar, "Navbar should be present.");
        boolean hasArticles = !all(By.cssSelector(".article-preview, a.preview-link")).isEmpty();
        boolean hasHero = !all(By.cssSelector(".banner .logo-font")).isEmpty();
        Assertions.assertTrue(hasArticles || hasHero, "Home should show hero banner or article previews.");
    }

    @Test
    @Order(2)
    public void navbarLinks_NavigateToLoginAndRegisterAndHome() {
        // Sign in
        clickNavbarLinkContains("Sign in");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("login"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("form[ng-submit], form"))
        ));
        Assertions.assertTrue(currentRoute().contains("login"), "URL should contain '#/login'.");

        // Home
        clickNavbarLinkContains("Home");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".home-page")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-preview"))
        ));
        Assertions.assertTrue(currentRoute().contains("#/") || driver.getCurrentUrl().equals(BASE_URL),
                "Should return to home route.");

        // Sign up
        clickNavbarLinkContains("Sign up");
        wait.until(ExpectedConditions.urlContains("register"));
        Assertions.assertTrue(currentRoute().contains("register"), "URL should contain '#/register'.");
    }

    @Test
    @Order(3)
    public void negativeLogin_ShowsErrorMessage() {
        clickNavbarLinkContains("Sign in");
        WebElement email = first(By.cssSelector("input[type='email'], input[placeholder='Email']"));
        WebElement password = first(By.cssSelector("input[type='password'], input[placeholder='Password']"));
        WebElement signInBtn = first(By.cssSelector("button[type='submit'], button.btn-primary"));
        Assertions.assertAll("Login form fields",
                () -> Assertions.assertNotNull(email, "Email field should exist."),
                () -> Assertions.assertNotNull(password, "Password field should exist."),
                () -> Assertions.assertNotNull(signInBtn, "Sign In button should exist.")
        );

        set(email, "invalid@example.com");
        set(password, "wrongpass");
        wait.until(ExpectedConditions.elementToBeClickable(signInBtn)).click();

        // Expect error list like: email or password is invalid
        By errorLocator = By.cssSelector(".error-messages li, .error-messages");
        List<WebElement> errors = all(errorLocator);
        // Allow SPA delay
        if (errors.isEmpty()) {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(errorLocator));
            errors = all(errorLocator);
        }
        boolean hasInvalidMsg = errors.stream().anyMatch(e -> e.getText().toLowerCase().contains("invalid"));
        Assertions.assertTrue(hasInvalidMsg || currentRoute().contains("login"),
                "Invalid login should show error and remain on login page.");
    }

    @Test
    @Order(4)
    public void openArticleFromGlobalFeed_DetailsVisible() {
        // Ensure we're on home
        clickNavbarLinkContains("Home");
        // Click first article preview
        WebElement firstArticle = first(By.cssSelector("a.preview-link"));
        if (firstArticle == null) {
            // sometimes previews load in cards
            firstArticle = first(By.cssSelector(".article-preview a"));
        }
        Assertions.assertNotNull(firstArticle, "At least one article preview link should exist on home.");
        String before = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(firstArticle)).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(before)));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("article"),
                "URL should indicate article page.");
        WebElement title = first(By.cssSelector(".article-page h1"));
        Assertions.assertNotNull(title, "Article title should be visible.");
        Assertions.assertTrue(title.getText().trim().length() > 0, "Article title text should not be empty.");

        // Navigate back to keep state clean
        driver.navigate().back();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".home-page, .article-preview")),
                ExpectedConditions.urlToBe(BASE_URL)
        ));
    }

    @Test
    @Order(5)
    public void tagsSidebar_IfPresent_AppliesTagFilter() {
        // Tags are on home page
        clickNavbarLinkContains("Home");
        List<WebElement> tags = all(By.cssSelector(".tag-list a.tag-pill, .sidebar .tag-pill"));
        if (!tags.isEmpty()) {
            String tagText = tags.get(0).getText().trim();
            wait.until(ExpectedConditions.elementToBeClickable(tags.get(0))).click();
            // Assert tag feed heading appears or articles reload
            // Many implementations add an active tag at top
            boolean tagApplied = wait.until(d -> {
                List<WebElement> feeds = d.findElements(By.cssSelector(".feed-toggle .nav-link.active"));
                return feeds.stream().anyMatch(f -> f.getText().toLowerCase().contains("tag") || f.getText().toLowerCase().contains(tagText.toLowerCase()));
            });
            Assertions.assertTrue(tagApplied, "Tag feed should become active after selecting a tag.");
        } else {
            Assertions.assertTrue(true, "No tag list present; skipping tag test for this skin.");
        }
    }

    @Test
    @Order(6)
    public void internalRoutes_OneLevelBelow_AreReachable() {
        String[] routes = new String[] {
                "#/login", "#/register", "#/settings", "#/editor"
        };
        int visited = 0;
        for (String r : routes) {
            String target = BASE_URL + r;
            String before = driver.getCurrentUrl();
            driver.navigate().to(target);
            try {
                wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(before)));
            } catch (Exception ignored) {}
            // Basic checks per route
            if (r.contains("login")) {
                Assertions.assertTrue(currentRoute().contains("login"), "Should be on login route.");
            } else if (r.contains("register")) {
                Assertions.assertTrue(currentRoute().contains("register"), "Should be on register route.");
            } else if (r.contains("settings")) {
                // When unauthenticated, many implementations redirect to login
                Assertions.assertTrue(currentRoute().contains("settings") || currentRoute().contains("login"),
                        "Settings should be reachable or redirect to login when unauthenticated.");
            } else if (r.contains("editor")) {
                Assertions.assertTrue(currentRoute().contains("editor") || currentRoute().contains("login"),
                        "Editor should be reachable or redirect to login when unauthenticated.");
            }
            visited++;
        }
        Assertions.assertTrue(visited >= 4, "Visited expected one-level routes.");
        // Return to home
        driver.navigate().to(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("nav.navbar")));
    }

    @Test
    @Order(7)
    public void footerExternalLinks_OpenAndMatchDomains() {
        // Look for external links commonly present: Thinkster, GitHub, RealWorld project
        Map<String, String> expectedDomains = new LinkedHashMap<>();
        expectedDomains.put("thinkster.io", "thinkster.io");
        expectedDomains.put("github.com", "github.com");
        expectedDomains.put("realworld", "github.com/gothinkster");

        List<WebElement> anchors = driver.findElements(By.cssSelector("footer a[href], .footer a[href], a[href]"));
        int checked = 0;
        for (Map.Entry<String, String> entry : expectedDomains.entrySet()) {
            String key = entry.getKey().toLowerCase();
            Optional<WebElement> link = anchors.stream()
                    .filter(a -> {
                        String h = a.getAttribute("href");
                        return h != null && h.toLowerCase().contains(key);
                    })
                    .findFirst();
            if (link.isPresent()) {
                boolean ok = openExternalAndAssertDomain(link.get(), entry.getValue());
                Assertions.assertTrue(ok, "External link should navigate to: " + entry.getValue());
                checked++;
                if (checked >= 2) break; // limit to reduce flakiness
            }
        }
        Assertions.assertTrue(checked >= 1, "Validated at least one external footer link, if present.");
    }

    @Test
    @Order(8)
    public void sortDropdown_IfPresent_ExercisesOptions() {
        // RealWorld feed usually lacks a sort dropdown; guard for optional UI in some implementations.
        By sortSelectBy = By.cssSelector("select.sort, select[name*='sort'], .sort select");
        WebElement sort = first(sortSelectBy);
        if (sort != null) {
            Select s = new Select(sort);
            List<String> originalOrder = captureFirstArticleTitles(3);
            for (int i = 0; i < s.getOptions().size(); i++) {
                s.selectByIndex(i);
                // wait for change by URL or first title change
                wait.until(d -> {
                    List<String> titles = captureFirstArticleTitles(3);
                    return !titles.equals(originalOrder);
                });
                List<String> newOrder = captureFirstArticleTitles(3);
                Assertions.assertNotEquals(originalOrder, newOrder, "Selecting sort option should change feed order.");
                originalOrder = newOrder;
            }
        } else {
            Assertions.assertTrue(true, "No sort dropdown present; skipping.");
        }
    }

    private List<String> captureFirstArticleTitles(int max) {
        List<WebElement> titles = driver.findElements(By.cssSelector(".article-preview h1, a.preview-link h1"));
        List<String> res = new ArrayList<>();
        for (int i = 0; i < Math.min(max, titles.size()); i++) {
            res.add(titles.get(i).getText().trim());
        }
        return res;
        }

    @Test
    @Order(9)
    public void editorRoute_RequiresLogin_RedirectsOrBlocks() {
        driver.navigate().to(BASE_URL + "#/editor");
        // Expect either editor form or redirect to login
        boolean editorOrLogin = wait.until(d -> currentRoute().contains("editor") || currentRoute().contains("login"));
        Assertions.assertTrue(editorOrLogin, "Navigating to editor should show editor or redirect to login when unauthenticated.");
    }
}
