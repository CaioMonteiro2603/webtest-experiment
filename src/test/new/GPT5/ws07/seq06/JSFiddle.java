package GPT5.ws07.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setupClass() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    }

    @AfterAll
    public static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ============================
    // Helpers / Utilities
    // ============================

    private static void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("jsfiddle.net"));
        // Dismiss cookie banner if present
        List<String> cookieSelectors = Arrays.asList(
            "button#CybotCookiebotDialogBodyLevelButtonLevelAccept",
            "button#CybotCookiebotDialogBodyButtonAccept",
            "button[aria-label*='accept' i]",
            "button[aria-label*='agree' i]",
            "button:contains('Accept')",
            "button:contains('Allow')",
            "button[class*='accept' i]"
        );
        
        for (String selector : cookieSelectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                for (WebElement element : elements) {
                    if (element.isDisplayed()) {
                        element.click();
                        Thread.sleep(500);
                        break;
                    }
                }
            } catch (Exception e) {
                // Continue trying other selectors
            }
        }
        
        // Also try XPath for cases where CSS :contains is not supported
        String[] xpathSelectors = {
            "//button[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept')]",
            "//button[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'agree')]"
        };
        
        for (String xpath : xpathSelectors) {
            try {
                WebElement element = driver.findElement(By.xpath(xpath));
                if (element.isDisplayed()) {
                    element.click();
                    break;
                }
            } catch (Exception e) {
                // Continue trying other selectors
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Base page did not load.");
    }

    private static Optional<WebElement> first(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? Optional.empty() : Optional.of(els.get(0));
    }

    private static Optional<WebElement> waitClickable(By by) {
        try {
            return Optional.of(wait.until(ExpectedConditions.elementToBeClickable(by)));
        } catch (TimeoutException e) {
            return Optional.empty();
        }
    }

    private static boolean clickIfPresent(By by) {
        Optional<WebElement> el = waitClickable(by);
        el.ifPresent(WebElement::click);
        return el.isPresent();
    }

    private static boolean elementExists(By by) {
        return driver.findElements(by).size() > 0;
    }

    private static void clearAndType(By locator, String value) {
        Optional<WebElement> el = first(locator);
        el.ifPresent(e -> {
            wait.until(ExpectedConditions.visibilityOf(e));
            e.clear();
            e.sendKeys(value);
        });
    }

    private static String hostOf(String url) {
        try {
            URI u = new URI(url);
            return (u.getHost() == null ? "" : u.getHost().toLowerCase(Locale.ROOT));
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private static String pathOf(String url) {
        try {
            URI u = new URI(url);
            String p = u.getPath();
            return p == null || p.isEmpty() ? "/" : p;
        } catch (URISyntaxException e) {
            return "/";
        }
    }

    private static int depthOfPath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) return 0;
        String s = path;
        if (s.startsWith("/")) s = s.substring(1);
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        if (s.isEmpty()) return 0;
        return s.split("/").length;
    }

    private static String toAbsoluteUrl(String href) {
        if (href == null || href.isBlank()) return "";
        if (href.startsWith("http://") || href.startsWith("https://")) return href;
        if (href.startsWith("//")) return "https:" + href;
        if (href.startsWith("/")) return BASE_URL.endsWith("/") ? BASE_URL.substring(0, BASE_URL.length() - 1) + href : BASE_URL + href;
        try {
            URI base = new URI(driver.getCurrentUrl());
            return base.resolve(href).toString();
        } catch (URISyntaxException e) {
            return href;
        }
    }

    private static List<String> collectInternalLinksOneLevel() {
        String baseHost = hostOf(BASE_URL);
        Set<String> urls = new LinkedHashSet<>();
        for (WebElement a : driver.findElements(By.cssSelector("a[href]"))) {
            String raw = a.getAttribute("href");
            if (raw == null || raw.startsWith("mailto:") || raw.startsWith("tel:") || raw.startsWith("javascript:")) continue;
            String href = toAbsoluteUrl(raw);
            if (!hostOf(href).equals(baseHost)) continue;
            String path = pathOf(href);
            if (depthOfPath(path) <= 1) {
                urls.add(href);
            }
        }
        urls.add(BASE_URL);
        return new ArrayList<>(urls);
    }

    private static void assertExternalLink(WebElement link) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        String href = link.getAttribute("href");
        if (href == null || href.isBlank()) return;
        String expectedHost = hostOf(toAbsoluteUrl(href));

        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        try {
            wait.until(d -> d.getWindowHandles().size() != before.size());
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> !d.getCurrentUrl().isEmpty());
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedHost),
                    "External link did not navigate to expected domain. Expected host: " + expectedHost + " actual: " + driver.getCurrentUrl());
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(d -> !d.getCurrentUrl().equals(BASE_URL));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedHost),
                    "External link did not navigate to expected domain in same tab.");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("jsfiddle.net"));
        }
    }

    // ============================
    // Tests
    // ============================

    @Test
    @Order(1)
    @DisplayName("Base page loads and one-level internal pages are reachable")
    void baseAndInternalPagesReachable() {
        openBase();
        List<String> internal = collectInternalLinksOneLevel();
        Assertions.assertFalse(internal.isEmpty(), "No internal links found at one level.");
        for (String url : internal) {
            driver.navigate().to(url);
            wait.until(d -> d.getCurrentUrl().startsWith("https://"));
            Assertions.assertEquals(hostOf(BASE_URL), hostOf(driver.getCurrentUrl()),
                    "Internal navigation landed on unexpected host: " + driver.getCurrentUrl());
            Assertions.assertFalse(driver.getPageSource().isEmpty(), "Page appears empty: " + url);
        }
        openBase();
    }

    @Test
    @Order(2)
    @DisplayName("External links on base and one-level pages open correct domains")
    void externalLinksPolicy() {
        openBase();
        Set<String> pages = new LinkedHashSet<>(collectInternalLinksOneLevel());
        for (String p : pages) {
            driver.navigate().to(p);
            wait.until(ExpectedConditions.urlContains("jsfiddle.net"));
            List<WebElement> externals = driver.findElements(By.cssSelector("a[href]"))
                    .stream()
                    .filter(a -> {
                        String href = a.getAttribute("href");
                        if (href == null || href.startsWith("#") || href.startsWith("javascript:") || href.startsWith("mailto:")) return false;
                        return !hostOf(toAbsoluteUrl(href)).equals(hostOf(BASE_URL));
                    })
                    .collect(Collectors.toList());
            for (WebElement link : externals) {
                assertExternalLink(link);
            }
        }
        openBase();
    }

    @Test
    @Order(3)
    @DisplayName("Sorting dropdown (if present) cycles options and affects order")
    void sortingDropdownIfPresent() {
        openBase();
        // JSFiddle home may not have sorting; try any visible select
        List<WebElement> selects = driver.findElements(By.cssSelector("select[id*='sort' i], select[name*='sort' i], select"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No select dropdown found; skipping sort test.");
        WebElement select = selects.get(0);
        Select sel = new Select(select);
        List<WebElement> options = sel.getOptions();
        Assumptions.assumeTrue(options.size() >= 2, "Not enough options to exercise sorting.");
        String before = sel.getFirstSelectedOption().getText().trim();

        // Snapshot of a repeated section (heuristic)
        List<String> baseline = driver.findElements(By.cssSelector("section, article, .post, .card, .list"))
                .stream().map(WebElement::getText).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        sel.selectByIndex(options.size() - 1);
        String afterSel1 = sel.getFirstSelectedOption().getText().trim();
        List<String> after1 = driver.findElements(By.cssSelector("section, article, .post, .card, .list"))
                .stream().map(WebElement::getText).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        sel.selectByIndex(0);
        String afterSel2 = sel.getFirstSelectedOption().getText().trim();
        List<String> after2 = driver.findElements(By.cssSelector("section, article, .post, .card, .list"))
                .stream().map(WebElement::getText).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        Assertions.assertNotEquals(before, afterSel1, "Selecting another option did not change selection.");
        Assertions.assertNotEquals(afterSel1, afterSel2, "Selecting back did not change selection.");
        Assertions.assertTrue(!baseline.equals(after1) || !after1.equals(after2) || !baseline.equals(after2),
                "Sorting did not appear to change the page content ordering (acceptable if static).");
        openBase();
    }

    @Test
    @Order(4)
    @DisplayName("Menu (burger) actions if available: open/close, About (external), Home")
    void menuBurgerActionsIfAvailable() {
        openBase();
        // Hamburger/menu button candidates
        By[] burgers = new By[] {
                By.cssSelector("button[aria-label*='menu' i], .hamburger, .navbar-toggler, .bm-burger-button"),
                By.xpath("//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu')]")
        };
        Optional<WebElement> burger = Optional.empty();
        for (By by : burgers) {
            burger = first(by);
            if (burger.isPresent()) break;
        }
        Assumptions.assumeTrue(burger.isPresent(), "No burger/menu button found; skipping.");

        wait.until(ExpectedConditions.elementToBeClickable(burger.get())).click();

        // Home / All Items
        By home = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'home') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'all items')]");
        if (elementExists(home)) {
            clickIfPresent(home);
            Assertions.assertEquals(hostOf(BASE_URL), hostOf(driver.getCurrentUrl()), "Home navigation left base host.");
        }

        // About (external)
        By about = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'about')]");
        if (elementExists(about)) {
            assertExternalLink(driver.findElement(about));
        }

        // Reset App State (generic)
        By reset = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset app state') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset')]");
        if (elementExists(reset)) {
            Assertions.assertTrue(clickIfPresent(reset), "Reset App State click failed.");
        }

        // Logout (if any)
        By logout = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log out')]");
        if (elementExists(logout)) {
            clickIfPresent(logout);
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Logout did not return to base site.");
        }

        // Close menu if still open
        clickIfPresent(burgers[0]);
        openBase();
    }

    @Test
    @Order(5)
    @DisplayName("Login behavior (if a login form is available)")
    void loginBehaviorIfPresent() {
        openBase();

        // Try to navigate to sign-in if a link exists
        By signInLink = By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login')]");
        if (elementExists(signInLink)) {
            clickIfPresent(signInLink);
        }

        Optional<WebElement> user = first(By.cssSelector("input[type='email'], input[name*='user' i], input[id*='user' i], input[name*='email' i], input[id*='email' i]"));
        Optional<WebElement> pass = first(By.cssSelector("input[type='password'], input[name*='pass' i], input[id*='pass' i]"));

        Optional<WebElement> submit = Optional.empty();
        if (user.isPresent() || pass.isPresent()) {
            // Generic submit candidates
            By[] submits = new By[] {
                    By.cssSelector("button[type='submit']"),
                    By.cssSelector("input[type='submit']"),
                    By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login')]"),
                    By.xpath("//input[@type='submit' or @type='button'][contains(translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login') or contains(translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in')]")
            };
            for (By by : submits) {
                if (elementExists(by)) { submit = first(by); break; }
            }
        }

        Assumptions.assumeTrue(user.isPresent() && pass.isPresent() && submit.isPresent(), "No native login form detected; skipping login tests.");

        // Negative: invalid credentials
        clearAndType(By.cssSelector("input[type='email'], input[name*='user' i], input[id*='user' i], input[name*='email' i], input[id*='email' i]"), "invalid@example.com");
        clearAndType(By.cssSelector("input[type='password'], input[name*='pass' i], input[id*='pass' i]"), "wrong");
        wait.until(ExpectedConditions.elementToBeClickable(submit.get())).click();

        boolean error = elementExists(By.cssSelector(".error, .alert, [role='alert']")) ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("invalid") ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("error");
        Assertions.assertTrue(error || driver.getCurrentUrl().contains("login") || driver.getCurrentUrl().startsWith(BASE_URL),
                "Invalid login did not show an error or remain on a login page.");

        // Positive login not possible (no credentials provided)
        Assumptions.assumeTrue(false, "No valid credentials available; skipping positive login.");
    }
}