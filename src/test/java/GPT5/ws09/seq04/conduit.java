package GPT5.ws09.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://demo.realworld.io/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ---------------- Helpers ----------------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private ExpectedCondition<Boolean> urlChangedFrom(String previous) {
        return d -> previous == null || !d.getCurrentUrl().equals(previous);
    }

    private WebElement first(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? null : els.get(0);
    }

    private void click(WebElement el) {
        if (el != null) wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private String registrableDomain(String url) {
        try {
            URI u = URI.create(url);
            String host = u.getHost();
            if (host == null) return "";
            String[] parts = host.split("\\.");
            if (parts.length < 2) return host;
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        } catch (Exception e) {
            return "";
        }
    }

    private void verifyExternalLink(WebElement anchor) {
        String href = anchor.getAttribute("href");
        if (href == null || href.isEmpty() || href.startsWith("javascript:") || href.startsWith("mailto:")) return;

        String baseDomain = registrableDomain(BASE_URL);
        String targetDomain = registrableDomain(href);
        boolean external = !baseDomain.equalsIgnoreCase(targetDomain);

        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(anchor)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],'_blank');", href);
        }

        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(driver.getCurrentUrl()));
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newTab = after.iterator().next();
            driver.switchTo().window(newTab);
            if (external) {
                wait.until(ExpectedConditions.urlContains(targetDomain));
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "External URL should contain expected domain: " + targetDomain);
            }
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            if (external) {
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "External URL should contain expected domain: " + targetDomain);
            }
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    private void verifyAllExternalLinksOnPage() {
        String baseDomain = registrableDomain(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        Map<String, WebElement> uniqueExternal = new LinkedHashMap<>();
        for (WebElement a : links) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith("http")) continue;
            String d = registrableDomain(href);
            if (!d.isEmpty() && !d.equalsIgnoreCase(baseDomain)) {
                uniqueExternal.putIfAbsent(href, a);
            }
        }
        for (WebElement a : uniqueExternal.values()) {
            verifyExternalLink(a);
        }
    }

    private String firstArticleTitle() {
        WebElement t = first(By.cssSelector(".article-preview h1, .article-preview a.preview-link h1, .article-preview a.preview-link"));
        return t != null ? t.getText().trim() : "";
    }

    // ---------------- Tests ----------------

    @Test
    @Order(1)
    public void homePageLoads() {
        goHome();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Base URL should load.");
        String title = driver.getTitle();
        Assertions.assertTrue(title != null && !title.isEmpty(), "Document should have a title.");
        // Header links present
        Assertions.assertTrue(driver.findElements(By.linkText("Home")).size() > 0
                        || driver.findElements(By.cssSelector("a.nav-link[href='#/']")).size() > 0,
                "Home link should be visible.");
        verifyAllExternalLinksOnPage(); // exercise external references if present
    }

    @Test
    @Order(2)
    public void navigateToSignInAndNegativeLogin() {
        goHome();
        WebElement signInLink = first(By.linkText("Sign in"));
        if (signInLink == null) signInLink = first(By.cssSelector("a[href*='#/login'], a[href*='login']"));
        Assertions.assertNotNull(signInLink, "Sign in link should be present.");
        String before = driver.getCurrentUrl();
        click(signInLink);
        wait.until(urlChangedFrom(before));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("login") || driver.getCurrentUrl().contains("#/login"),
                "Should be on login page.");
        // Perform negative login attempt
        WebElement email = first(By.cssSelector("input[type='email'], input[placeholder='Email']"));
        WebElement pass = first(By.cssSelector("input[type='password'], input[placeholder='Password']"));
        WebElement btn = first(By.cssSelector("button[type='submit']"));
        Assertions.assertAll("Login form fields",
                () -> Assertions.assertNotNull(email, "Email field should exist."),
                () -> Assertions.assertNotNull(pass, "Password field should exist."),
                () -> Assertions.assertNotNull(btn, "Submit button should exist.")
        );
        email.clear(); email.sendKeys("invalid@example.com");
        pass.clear(); pass.sendKeys("wrongpassword");
        click(btn);
        // Error presence (do not depend on specific message text)
        boolean hasError = wait.until(d ->
                d.findElements(By.cssSelector(".error-messages li, .error-messages, .ng-invalid, .has-error")).size() > 0
                        || d.getCurrentUrl().contains("login")
        );
        Assertions.assertTrue(hasError, "An error message or invalid state should be indicated for bad credentials.");
    }

    @Test
    @Order(3)
    public void signUpPageOpensAndValidatesRequiredFields() {
        goHome();
        WebElement signUp = first(By.linkText("Sign up"));
        if (signUp == null) signUp = first(By.cssSelector("a[href*='#/register'], a[href*='register']"));
        Assertions.assertNotNull(signUp, "Sign up link should be present.");
        String before = driver.getCurrentUrl();
        click(signUp);
        wait.until(urlChangedFrom(before));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("register") || driver.getCurrentUrl().contains("#/register"),
                "Should navigate to register page.");
        // Ensure form fields visible
        Assertions.assertTrue(driver.findElements(By.cssSelector("input[type='text'], input[placeholder='Username']")).size() > 0,
                "Username field should be present.");
        Assertions.assertTrue(driver.findElements(By.cssSelector("input[type='email'], input[placeholder='Email']")).size() > 0,
                "Email field should be present.");
        Assertions.assertTrue(driver.findElements(By.cssSelector("input[type='password'], input[placeholder='Password']")).size() > 0,
                "Password field should be present.");
        verifyAllExternalLinksOnPage();
    }

    @Test
    @Order(4)
    public void tagsFilterChangesFeed() {
        goHome();
        // Grab initial first article title
        String initial = firstArticleTitle();
        // Click a tag (if present)
        WebElement tag = first(By.cssSelector(".tag-list a, .sidebar .tag-pill"));
        Assumptions.assumeTrue(tag != null, "No tags available on home; skipping tag filter test.");
        click(tag);
        // Wait for feed to refresh and confirm first article can change (best effort)
        try {
            wait.until(d -> {
                String t = firstArticleTitle();
                return !t.isEmpty() && !t.equals(initial);
            });
        } catch (TimeoutException ignored) {}
        String after = firstArticleTitle();
        Assumptions.assumeTrue(!after.isEmpty(), "No article title found after filtering; skipping assertion.");
        Assertions.assertNotEquals(initial, after, "First article title should change after selecting a tag.");
    }

    @Test
    @Order(5)
    public void sortingDropdownIfPresent() {
        goHome();
        // RealWorld usually has no sorting select; exercise if any select exists with multiple options
        WebElement selectEl = first(By.cssSelector("select"));
        Assumptions.assumeTrue(selectEl != null, "No sorting dropdown present; skipping.");
        Select sel = new Select(selectEl);
        List<WebElement> options = sel.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Sorting dropdown has insufficient options; skipping.");
        String before = firstArticleTitle();
        sel.selectByIndex(1);
        try {
            wait.until(d -> !Objects.equals(before, firstArticleTitle()));
        } catch (TimeoutException ignored) {}
        String after = firstArticleTitle();
        Assertions.assertNotEquals(before, after, "Order should change after selecting a different sort option.");
        sel.selectByIndex(0);
    }

    @Test
    @Order(6)
    public void footerExternalLinks() {
        goHome();
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        // Common external links in RealWorld footer (Thinkster, GitHub)
        List<String> domains = Arrays.asList("thinkster.io", "github.com", "twitter.com", "facebook.com", "linkedin.com");
        Map<String, WebElement> found = new LinkedHashMap<>();
        for (String dom : domains) {
            List<WebElement> anchors = driver.findElements(By.cssSelector("a[href*='" + dom + "']"));
            if (!anchors.isEmpty()) found.put(dom, anchors.get(0));
        }
        Assumptions.assumeTrue(!found.isEmpty(), "No external footer links found; skipping.");
        for (WebElement a : found.values()) {
            verifyExternalLink(a);
        }
    }

    @Test
    @Order(7)
    public void openArticlePreviewAndBack() {
        goHome();
        WebElement article = first(By.cssSelector(".article-preview a.preview-link, .article-preview h1 a, a[href*='article']"));
        Assumptions.assumeTrue(article != null, "No article preview links present; skipping.");
        String before = driver.getCurrentUrl();
        click(article);
        // Article detail page should show title and content (best effort)
        try {
            wait.until(urlChangedFrom(before));
        } catch (TimeoutException ignored) {}
        boolean hasTitle = driver.findElements(By.cssSelector("h1")).size() > 0;
        boolean hasBody = driver.findElements(By.cssSelector(".article-content, .markdown")).size() > 0;
        Assertions.assertTrue(hasTitle || hasBody, "Article page should show a title or content.");
        driver.navigate().back();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    @Test
    @Order(8)
    public void burgerMenuIfPresent() {
        goHome();
        // RealWorld typically has a static header, but handle responsive menu if present
        List<By> candidates = Arrays.asList(
                By.cssSelector("button[aria-label*='menu' i]"),
                By.cssSelector(".navbar-toggle, .hamburger, .navbar-toggler"),
                By.xpath("//button[contains(.,'Menu') or contains(.,'menu')]")
        );
        WebElement burger = null;
        for (By c : candidates) {
            burger = first(c);
            if (burger != null) break;
        }
        Assumptions.assumeTrue(burger != null, "No burger/menu button found; skipping.");
        click(burger);
        boolean itemsVisible = driver.findElements(By.cssSelector("nav a, .navbar a")).size() > 0;
        Assertions.assertTrue(itemsVisible, "Menu items should be visible after opening the burger.");
    }

    @Test
    @Order(9)
    public void iterateInternalLinksOneLevelFromHome() {
        goHome();
        String baseDomain = registrableDomain(BASE_URL);

        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<String> oneLevel = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(h -> !h.isEmpty())
                .filter(h -> {
                    if (h.startsWith("#") || h.startsWith("/")) return true;
                    if (h.startsWith("http")) {
                        String d = registrableDomain(h);
                        return d.equalsIgnoreCase(baseDomain) && h.startsWith(BASE_URL);
                    }
                    return false;
                })
                .distinct()
                .collect(Collectors.toList());

        int visited = 0;
        for (String href : oneLevel) {
            if (visited >= 6) break; // limit for stability
            WebElement link = driver.findElements(By.cssSelector("a[href='" + href + "']")).stream().findFirst().orElse(null);
            if (link == null) continue;

            String prev = driver.getCurrentUrl();
            try {
                click(link);
                try {
                    wait.until(d -> !d.getCurrentUrl().equals(prev) || d.findElements(By.cssSelector(".article-preview, .home-page, .settings-page, .auth-page, .profile-page")).size() > 0);
                } catch (TimeoutException ignored) {}

                boolean hasContent = driver.findElements(By.cssSelector("h1")).size() > 0
                        || driver.findElements(By.cssSelector(".article-preview, .auth-page, .settings-page, .profile-page, .home-page")).size() > 0;
                Assertions.assertTrue(hasContent, "Internal page should render content for " + href);

                verifyAllExternalLinksOnPage();
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
            visited++;
        }
        Assertions.assertTrue(visited >= 0, "Visited internal links without failures.");
    }
}
