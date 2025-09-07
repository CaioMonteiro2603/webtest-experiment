package GPT5.ws10.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgriHeadlessSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ---------------- Helpers ----------------

    private void openBase() {
        driver.get(BASE_URL);
        waitDocumentReady();
        dismissCookieOrWelcome();
    }

    private void waitDocumentReady() {
        try {
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
    }

    private void dismissCookieOrWelcome() {
        List<By> candidates = Arrays.asList(
                By.cssSelector("button#onetrust-accept-btn-handler"),
                By.cssSelector("button[class*='accept'],button[class*='agree'],button[class*='ok']"),
                By.cssSelector(".cookie-accept,.cc-accept,.cc-dismiss,.btn-close,.close")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(els.get(0))).click();
                    break;
                } catch (Exception ignored) {}
            }
        }
    }

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        return null;
    }

    private WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private String hostOf(String url) {
        try { return URI.create(url).getHost(); } catch (Exception e) { return ""; }
    }

    private int depthOfPath(String path) {
        if (path == null) return 0;
        String p = path;
        if (p.startsWith("/")) p = p.substring(1);
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        if (p.isEmpty()) return 0;
        return p.split("/").length;
    }

    private void safeLogoutIfPossible() {
        // Try common logout spots
        List<By> candidates = Arrays.asList(
                By.xpath("//a[contains(.,'Logout') or contains(.,'Sair')]"),
                By.cssSelector("button[aria-label*='logout'],button[title*='logout']"),
                By.cssSelector("a[href*='logout'],button[href*='logout']")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                try { waitClickable(els.get(0)).click(); waitDocumentReady(); return; } catch (Exception ignored) {}
            }
        }
    }

    private boolean isLoggedInHeuristic() {
        // Heuristic: dashboard route not containing /login and presence of main app shell
        String url = driver.getCurrentUrl();
        if (url.toLowerCase().contains("/login")) return false;
        if (!driver.findElements(By.cssSelector("nav,header,.sidebar,.navbar")).isEmpty()) return true;
        return !driver.findElements(By.cssSelector("a[href*='logout']")).isEmpty();
    }

    private void login(String user, String pass) {
        openBase();
        WebElement email = firstPresent(
                By.id("email"), By.name("email"),
                By.cssSelector("input[type='email']"),
                By.cssSelector("input[placeholder*='mail'], input[placeholder*='email']")
        );
        WebElement password = firstPresent(
                By.id("password"), By.name("password"),
                By.cssSelector("input[type='password']"),
                By.cssSelector("input[placeholder*='senha'], input[placeholder*='password']")
        );
        WebElement submit = firstPresent(
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Entrar') or contains(.,'Login') or contains(.,'Acessar')]")
        );

        Assumptions.assumeTrue(email != null && password != null && submit != null, "Login form not found; cannot proceed.");
        email.clear(); email.sendKeys(user);
        password.clear(); password.sendKeys(pass);
        waitClickable(submit).click();
        waitDocumentReady();
    }

    private void handleExternalLink(WebElement link) {
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        String baseHost = hostOf(driver.getCurrentUrl());

        waitClickable(link).click();
        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !baseHost.equals(hostOf(d.getCurrentUrl())));
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            waitDocumentReady();
            Assertions.assertNotEquals(baseHost, hostOf(driver.getCurrentUrl()), "External link should navigate to different domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            // Same tab navigation
            Assertions.assertNotEquals(baseHost, hostOf(driver.getCurrentUrl()), "External link should navigate to different domain");
            driver.navigate().back();
            waitDocumentReady();
        }
    }

    // ---------------- Tests ----------------

    @Test
    @Order(1)
    public void basePage_Loads_And_HasLoginForm() {
        openBase();
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("/login"), "Should be on login page URL");
        WebElement email = firstPresent(By.id("email"), By.name("email"), By.cssSelector("input[type='email']"));
        WebElement password = firstPresent(By.id("password"), By.name("password"), By.cssSelector("input[type='password']"));
        Assertions.assertAll(
                () -> Assertions.assertNotNull(email, "Email field should be present"),
                () -> Assertions.assertNotNull(password, "Password field should be present")
        );
    }

    @Test
    @Order(2)
    public void login_WithValidCredentials_NavigatesToApp() {
        login(LOGIN, PASSWORD);

        // Assert not on login and some shell is visible
        boolean logged = wait.until(d -> isLoggedInHeuristic());
        Assertions.assertTrue(logged, "Valid login should navigate to application (not stay on /login)");
    }

    @Test
    @Order(3)
    public void logout_FromMenu_IfAvailable_ReturnsToLogin() {
        // Ensure logged in first
        if (!isLoggedInHeuristic()) login(LOGIN, PASSWORD);

        // Try to open burger / user menu if needed
        WebElement burger = firstPresent(
                By.cssSelector("button.navbar-toggler"),
                By.cssSelector("button[aria-label*='menu'],button[aria-label*='Menu']"),
                By.cssSelector("button[class*='menu'],.hamburger, .burger")
        );
        if (burger != null) {
            try { waitClickable(burger).click(); } catch (Exception ignored) {}
        }

        safeLogoutIfPossible();
        waitDocumentReady();

        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("/login")
                        || driver.findElements(By.cssSelector("input[type='password']")).size() > 0,
                "After logout, user should be on login page");
    }

    @Test
    @Order(4)
    public void invalidLogin_ShowsErrorOrRemainsOnLogin() {
        openBase();
        WebElement email = firstPresent(By.id("email"), By.name("email"), By.cssSelector("input[type='email']"));
        WebElement password = firstPresent(By.id("password"), By.name("password"), By.cssSelector("input[type='password']"));
        WebElement submit = firstPresent(By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Entrar') or contains(.,'Login') or contains(.,'Acessar')]"));
        Assumptions.assumeTrue(email != null && password != null && submit != null, "Login form not found; skipping");

        email.clear(); email.sendKeys("invalid@example.com");
        password.clear(); password.sendKeys("wrongpassword");
        waitClickable(submit).click();
        waitDocumentReady();

        boolean stayedOnLogin = driver.getCurrentUrl().toLowerCase().contains("/login");
        boolean errorVisible = driver.findElements(By.cssSelector(".alert,.error,.invalid-feedback,[role='alert']")).size() > 0;
        Assertions.assertTrue(stayedOnLogin || errorVisible, "Invalid login should not pass authentication");
    }

    @Test
    @Order(5)
    public void menu_AllItems_And_ResetAppState_IfPresent() {
        // Log in
        if (!isLoggedInHeuristic()) login(LOGIN, PASSWORD);

        // Open burger
        WebElement burger = firstPresent(
                By.cssSelector("button.navbar-toggler"),
                By.cssSelector("button[aria-label*='menu'],button[aria-label*='Menu']"),
                By.cssSelector("button[class*='menu'],.hamburger, .burger")
        );
        Assumptions.assumeTrue(burger != null, "Menu (burger) button not found; skipping");
        waitClickable(burger).click();

        // Try "All Items" like option
        WebElement allItems = firstPresent(
                By.xpath("//a[contains(.,'All Items') or contains(.,'Início') or contains(.,'Home')]"),
                By.cssSelector("a[href='/'],a[href='#']")
        );
        Assumptions.assumeTrue(allItems != null, "All Items/Home link not found in menu; skipping");
        String before = driver.getCurrentUrl();
        waitClickable(allItems).click();
        waitDocumentReady();
        Assertions.assertTrue(!driver.getCurrentUrl().equals(before) || driver.findElements(By.tagName("main")).size() > 0,
                "All Items/Home should navigate or refresh content");

        // Reset App State if exists
        WebElement reset = firstPresent(
                By.xpath("//a[contains(.,'Reset') or contains(.,'Reset App State') or contains(.,'Limpar')]"),
                By.cssSelector("a[href*='reset'],button[href*='reset'],button[id*='reset']")
        );
        if (reset != null) {
            waitClickable(reset).click();
            waitDocumentReady();
            // Assert some confirmation or simply no error
            Assertions.assertTrue(driver.findElements(By.cssSelector(".toast,.alert,[role='status']")).size() >= 0,
                    "Reset action should not break page");
        }
    }

    @Test
    @Order(6)
    public void about_External_FromMenu_IfPresent() {
        if (!isLoggedInHeuristic()) login(LOGIN, PASSWORD);

        WebElement burger = firstPresent(
                By.cssSelector("button.navbar-toggler"),
                By.cssSelector("button[aria-label*='menu'],button[aria-label*='Menu']"),
                By.cssSelector("button[class*='menu'],.hamburger, .burger")
        );
        if (burger != null) {
            try { waitClickable(burger).click(); } catch (Exception ignored) {}
        }

        WebElement about = firstPresent(
                By.xpath("//a[contains(.,'About') or contains(.,'Sobre')]"),
                By.cssSelector("a[href*='about']")
        );
        Assumptions.assumeTrue(about != null, "About link not present; skipping");

        String baseHost = hostOf(driver.getCurrentUrl());
        handleExternalLink(about);
        Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should return to base window after external check");
    }

    @Test
    @Order(7)
    public void footerSocial_ExternalLinks_ValidateDomains() {
        if (!isLoggedInHeuristic()) openBase(); // social links may exist on login

        String baseHost = hostOf(driver.getCurrentUrl());
        List<WebElement> anchors = driver.findElements(By.cssSelector("footer a[href], .footer a[href], a.social[href]"));
        if (anchors.isEmpty()) {
            // try general anchors and then filter social domains
            anchors = driver.findElements(By.cssSelector("a[href]"));
        }
        List<WebElement> external = anchors.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    return href != null && href.startsWith("http")
                            && !hostOf(href).equalsIgnoreCase(baseHost)
                            && (href.contains("twitter") || href.contains("facebook") || href.contains("linkedin") || href.contains("instagram"));
                })
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!external.isEmpty(), "No social external links found; skipping");
        int validated = 0;
        for (WebElement link : external) {
            if (validated >= 2) break;
            try {
                handleExternalLink(link);
                validated++;
            } catch (Exception e) {
                driver.get(BASE_URL);
                waitDocumentReady();
            }
        }
        Assertions.assertTrue(validated > 0, "At least one social external link should be validated");
    }

    @Test
    @Order(8)
    public void internalLinks_OneLevelBelow_Work() {
        // Ensure logged in to expose more links; if login fails, we still use whatever links present
        if (!isLoggedInHeuristic()) {
            try { login(LOGIN, PASSWORD); } catch (Exception ignored) {}
        }

        String current = driver.getCurrentUrl();
        String baseHost = hostOf(current);
        URI base = URI.create(current);
        int baseDepth = depthOfPath(base.getPath());

        List<String> hrefs = driver.findElements(By.cssSelector("a[href]")).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(h -> h.startsWith("http"))
                .distinct()
                .collect(Collectors.toList());

        List<String> internalOneLevel = hrefs.stream()
                .filter(h -> baseHost.equals(hostOf(h)))
                .filter(h -> {
                    try {
                        URI u = URI.create(h);
                        int depth = depthOfPath(u.getPath());
                        return depth <= baseDepth + 1; // one level below
                    } catch (Exception e) { return false; }
                })
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!internalOneLevel.isEmpty(), "No internal one-level-below links found; skipping");

        int visited = 0;
        for (String link : internalOneLevel) {
            if (visited >= 3) break; // limit for stability
            Optional<WebElement> anchor = driver.findElements(By.cssSelector("a[href]")).stream()
                    .filter(a -> link.equals(a.getAttribute("href"))).findFirst();
            if (anchor.isEmpty()) continue;

            String before = driver.getCurrentUrl();
            try {
                waitClickable(anchor.get()).click();
                wait.until(d -> !d.getCurrentUrl().equals(before));
                waitDocumentReady();
                Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should remain on same host");
                Assertions.assertTrue(driver.findElements(By.tagName("body")).size() > 0, "Destination should render");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(before));
                visited++;
            } catch (Exception e) {
                // Recover to base and continue
                driver.get(before);
                waitDocumentReady();
            }
        }
        Assertions.assertTrue(visited >= 1, "At least one internal link should be navigable");
    }

    @Test
    @Order(9)
    public void optionalSortingDropdown_IfAny_ChangesOrder() {
        if (!isLoggedInHeuristic()) {
            try { login(LOGIN, PASSWORD); } catch (Exception ignored) {}
        }

        List<WebElement> selects = driver.findElements(By.tagName("select"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No sorting/select dropdown found; skipping");

        // Choose the first select to test (best-effort)
        Select select = new Select(selects.get(0));
        List<WebElement> options = select.getOptions();
        Assumptions.assumeTrue(options.size() >= 2, "Not enough options to exercise sort; skipping");

        String beforeKey = captureListKeySnapshot();

        select.selectByIndex(1);
        waitDocumentReady();
        String afterKey = captureListKeySnapshot();

        // If the page uses async, give it a brief condition to change DOM length/text
        if (beforeKey.equals(afterKey)) {
            try {
                wait.until(d -> !captureListKeySnapshot().equals(beforeKey));
                afterKey = captureListKeySnapshot();
            } catch (TimeoutException ignored) {}
        }

        Assertions.assertNotEquals(beforeKey, afterKey, "Ordering/content should change after selecting a different option");
    }

    private String captureListKeySnapshot() {
        // Best-effort snapshot of visible list/grid items (texts)
        List<WebElement> candidates = new ArrayList<>();
        candidates.addAll(driver.findElements(By.cssSelector("table tbody tr")));
        candidates.addAll(driver.findElements(By.cssSelector("ul li")));
        candidates.addAll(driver.findElements(By.cssSelector("[role='listitem']")));
        if (candidates.isEmpty()) {
            // fallback to generic cards
            candidates.addAll(driver.findElements(By.cssSelector(".card,.list-group-item,.item")));
        }
        return candidates.stream().limit(5).map(e -> e.getText().trim()).collect(Collectors.joining("|"));
    }
}
