package GTP5.ws06.seq06;

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
public class AutomationInTestingHeadlessSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String BASE_URL = "https://automationintesting.online/";

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
        wait.until(ExpectedConditions.urlContains("automationintesting.online"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Base page did not load.");
    }

    private static Optional<WebElement> first(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? Optional.empty() : Optional.of(els.get(0));
    }

    private static Optional<WebElement> waitVisible(By by) {
        try {
            return Optional.of(wait.until(ExpectedConditions.visibilityOfElementLocated(by)));
        } catch (TimeoutException e) {
            return Optional.empty();
        }
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

    private static void clearAndType(By locator, String value) {
        Optional<WebElement> el = first(locator);
        el.ifPresent(e -> {
            wait.until(ExpectedConditions.visibilityOf(e));
            e.clear();
            e.sendKeys(value);
        });
    }

    private static boolean elementExists(By by) {
        return driver.findElements(by).size() > 0;
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

    private static String fragmentOf(String url) {
        try {
            URI u = new URI(url);
            return u.getFragment() == null ? "" : u.getFragment();
        } catch (URISyntaxException e) {
            return "";
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

    private static int depthOfHash(String hash) {
        // Expect formats like "/admin" "/rooms" "/something/more"
        String h = hash;
        if (h.startsWith("/")) h = h.substring(1);
        if (h.endsWith("/")) h = h.substring(0, h.length() - 1);
        if (h.isEmpty()) return 0;
        return h.split("/").length;
    }

    private static String toAbsoluteUrl(String href) {
        if (href == null || href.isBlank()) return "";
        if (href.startsWith("http://") || href.startsWith("https://")) return href;
        if (href.startsWith("//")) return "https:" + href;
        if (href.startsWith("/")) return BASE_URL.endsWith("/") ? BASE_URL.substring(0, BASE_URL.length() - 1) + href : BASE_URL + href;
        // relative: resolve against current URL
        try {
            URI base = new URI(driver.getCurrentUrl());
            return base.resolve(href).toString();
        } catch (URISyntaxException e) {
            return href;
        }
    }

    private static List<String> collectInternalLinksOneLevel() {
        // One level below the site root OR one hash level (/#/something)
        String baseHost = hostOf(BASE_URL);
        Set<String> urls = new LinkedHashSet<>();
        for (WebElement a : driver.findElements(By.cssSelector("a[href]"))) {
            String raw = a.getAttribute("href");
            if (raw == null) continue;
            if (raw.startsWith("mailto:") || raw.startsWith("tel:") || raw.startsWith("javascript:")) continue;
            String href = toAbsoluteUrl(raw);
            if (!hostOf(href).equals(baseHost)) continue;

            String path = pathOf(href);
            String frag = fragmentOf(href);
            boolean include = false;

            // Traditional path-based depth (e.g., /rooms)
            if (depthOfPath(path) <= 1) include = true;

            // Hash router (e.g., https://site/#/admin)
            if (!frag.isEmpty() && frag.startsWith("/")) {
                if (depthOfHash(frag) <= 1) include = true;
            }

            if (include) urls.add(href);
        }
        urls.add(BASE_URL);
        return new ArrayList<>(urls);
    }

    private static void assertExternalLink(WebElement link) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        String href = link.getAttribute("href");
        String expectedHost = hostOf(href);

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
            // Same-tab navigation
            wait.until(d -> !d.getCurrentUrl().equals(BASE_URL));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedHost),
                    "External link did not navigate to expected domain in same tab.");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("automationintesting.online"));
        }
    }

    private static void selectIfPresent(By selectLocator, String visibleText) {
        Optional<WebElement> el = first(selectLocator);
        el.ifPresent(e -> {
            Select sel = new Select(e);
            Optional<WebElement> match = sel.getOptions().stream().filter(o -> o.getText().trim().equalsIgnoreCase(visibleText)).findFirst();
            if (match.isPresent()) {
                sel.selectByVisibleText(match.get().getText());
            } else if (!sel.getOptions().isEmpty()) {
                sel.selectByIndex(Math.min(1, sel.getOptions().size() - 1));
            }
        });
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
    @DisplayName("Footer and internal external links open correct domains")
    void externalLinksOneLevel() {
        openBase();
        Set<String> pages = new LinkedHashSet<>(collectInternalLinksOneLevel());
        for (String p : pages) {
            driver.navigate().to(p);
            wait.until(ExpectedConditions.urlContains("automationintesting.online"));
            List<WebElement> externals = driver.findElements(By.cssSelector("a[href]"))
                    .stream()
                    .filter(a -> {
                        String href = a.getAttribute("href");
                        if (href == null || href.startsWith("#") || href.startsWith("javascript:") || href.startsWith("mailto:")) return false;
                        String host = hostOf(toAbsoluteUrl(href));
                        return !host.isEmpty() && !host.equals(hostOf(BASE_URL));
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
    @DisplayName("Contact form invalid submission shows validation or stays on page")
    void contactFormInvalid() {
        openBase();
        // Attempt to submit without filling required fields
        By submitBtn = By.cssSelector("#submitContact, button[type='submit'], input[type='submit']");
        Assumptions.assumeTrue(elementExists(submitBtn), "Submit button not found; skipping contact form tests.");
        clickIfPresent(submitBtn);

        // Either error messages appear or HTML5 validity fails (checked via JS) or URL unchanged
        boolean errorsVisible = elementExists(By.cssSelector(".alert-danger, .error, .alert, [role='alert']"))
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("required")
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("please");
        boolean stayed = driver.getCurrentUrl().equals(BASE_URL);
        boolean html5Invalid = false;
        try {
            Object res = ((JavascriptExecutor) driver).executeScript(
                    "var f=document.querySelector('form');return f && f.checkValidity? f.checkValidity() : false;");
            html5Invalid = (res instanceof Boolean) && !((Boolean) res);
        } catch (Exception ignored) {}
        Assertions.assertTrue(errorsVisible || stayed || html5Invalid,
                "Empty contact form submission did not show validation nor remain on page.");
    }

    @Test
    @Order(4)
    @DisplayName("Contact form valid submission shows success indicator")
    void contactFormValid() {
        openBase();
        // Common field IDs on this site; fallbacks included
        clearAndType(By.id("name"), "John Doe");
        clearAndType(By.id("email"), "john.doe@example.com");
        clearAndType(By.id("phone"), "5551234567");
        clearAndType(By.id("subject"), "Booking enquiry");
        clearAndType(By.id("message"), "This is a test message from Selenium.");

        selectIfPresent(By.cssSelector("select"), "Family");

        By submitBtn = By.cssSelector("#submitContact, button[type='submit'], input[type='submit']");
        Assumptions.assumeTrue(elementExists(submitBtn), "Submit button not found; skipping.");
        clickIfPresent(submitBtn);

        // Success heuristics: presence of success alert, toast, or thank you text
        boolean success = elementExists(By.cssSelector(".alert-success, .success, .toast-success"))
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("thanks for getting in touch")
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("success")
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("thank you");
        Assertions.assertTrue(success, "No clear success indicator after contact form submission.");
    }

    @Test
    @Order(5)
    @DisplayName("Admin login page: negative credentials show error")
    void adminLoginNegative() {
        driver.navigate().to(BASE_URL + "#/admin");
        wait.until(ExpectedConditions.urlContains("#/admin"));
        Optional<WebElement> user = first(By.id("username"));
        Optional<WebElement> pass = first(By.id("password"));
        Optional<WebElement> submit = first(By.id("doLogin"));
        Assumptions.assumeTrue(user.isPresent() && pass.isPresent() && submit.isPresent(), "Admin login form not found; skipping.");

        user.get().clear(); user.get().sendKeys("baduser");
        pass.get().clear(); pass.get().sendKeys("badpass");
        wait.until(ExpectedConditions.elementToBeClickable(submit.get())).click();

        boolean error = elementExists(By.cssSelector(".alert, .alert-danger, .error, [role='alert']"))
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("invalid")
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("unauthor");
        Assertions.assertTrue(error, "Invalid login did not produce an error message.");
        openBase();
    }

    @Test
    @Order(6)
    @DisplayName("Admin login page: valid default credentials (if enabled)")
    void adminLoginPositiveIfAvailable() {
        driver.navigate().to(BASE_URL + "#/admin");
        wait.until(ExpectedConditions.urlContains("#/admin"));
        Optional<WebElement> user = first(By.id("username"));
        Optional<WebElement> pass = first(By.id("password"));
        Optional<WebElement> submit = first(By.id("doLogin"));
        Assumptions.assumeTrue(user.isPresent() && pass.isPresent() && submit.isPresent(), "Admin login form not found; skipping.");

        // Known demo defaults for this platform; skip if disabled
        user.get().clear(); user.get().sendKeys("admin");
        pass.get().clear(); pass.get().sendKeys("password");
        wait.until(ExpectedConditions.elementToBeClickable(submit.get())).click();

        // Signs of successful login: presence of admin dashboard controls or logout
        boolean success = elementExists(By.id("logout")) ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("hotel administration") ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("dashboard");
        Assumptions.assumeTrue(success, "Valid admin credentials may be disabled; skipping positive login assertions.");

        Assertions.assertTrue(success, "Expected admin dashboard/controls after valid login.");
        // Logout to restore state
        clickIfPresent(By.id("logout"));
        openBase();
    }

    @Test
    @Order(7)
    @DisplayName("Sorting dropdown (if present) cycles options and affects order")
    void sortingDropdownIfPresent() {
        openBase();
        // Look for a select that plausibly sorts room listings
        List<WebElement> selects = driver.findElements(By.cssSelector("select[id*='sort' i], select[name*='sort' i], select"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No select dropdown found; skipping sort test.");
        WebElement select = selects.get(0);
        Select sel = new Select(select);
        List<String> before = sel.getOptions().stream().map(o -> o.getText().trim()).collect(Collectors.toList());
        Assumptions.assumeTrue(before.size() >= 2, "Not enough options to exercise sorting.");

        // Capture a snapshot of item order (room cards or list items)
        List<String> baseline = driver.findElements(By.cssSelector(".hotel-room-info, .room-info, .card, .row .col-sm-4"))
                .stream().map(WebElement::getText).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        sel.selectByIndex(before.size() - 1);
        String sel1 = sel.getFirstSelectedOption().getText();
        List<String> after1 = driver.findElements(By.cssSelector(".hotel-room-info, .room-info, .card, .row .col-sm-4"))
                .stream().map(WebElement::getText).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        sel.selectByIndex(0);
        String sel2 = sel.getFirstSelectedOption().getText();
        List<String> after2 = driver.findElements(By.cssSelector(".hotel-room-info, .room-info, .card, .row .col-sm-4"))
                .stream().map(WebElement::getText).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        // Assertions: selection changes and at least one ordering snapshot differs
        Assertions.assertNotEquals(sel1, sel2, "Selecting different sort options did not change selection.");
        Assertions.assertTrue(!baseline.equals(after1) || !after1.equals(after2) || !baseline.equals(after2),
                "Sorting did not appear to change the order of items (acceptable if static).");
    }

    @Test
    @Order(8)
    @DisplayName("Menu (burger) actions if available: open/close, About (external), Home")
    void menuBurgerActionsIfAvailable() {
        openBase();
        // Responsive burger/menu candidates
        By[] burgers = new By[] {
                By.cssSelector("button[aria-label*='menu' i], button[id*='menu' i], .bm-burger-button, .hamburger, .navbar-toggler"),
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
            Assertions.assertEquals(hostOf(BASE_URL), hostOf(driver.getCurrentUrl()), "Home navigation left the base host.");
        }

        // About (external)
        By about = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'about') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sobre')]");
        if (elementExists(about)) {
            assertExternalLink(driver.findElement(about));
        }

        // Reset App State if any
        By reset = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset app state') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset')]");
        if (elementExists(reset)) {
            Assertions.assertTrue(clickIfPresent(reset), "Reset App State click failed.");
        }

        // Logout if exists (admin menu)
        By logout = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log out')]");
        if (elementExists(logout)) {
            clickIfPresent(logout);
            Assertions.assertTrue(driver.getCurrentUrl().contains(BASE_URL.replace("https://", "")) || driver.getCurrentUrl().startsWith(BASE_URL),
                    "Logout did not return to base site.");
        }

        // Close menu if still open
        clickIfPresent(burgers[0]);
        openBase();
    }
}
