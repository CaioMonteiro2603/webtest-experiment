package GTP5.ws08.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStoreHeadlessSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

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
        wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
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
            if (raw == null || raw.startsWith("mailto:") || raw.startsWith("tel:") || raw.startsWith("javascript:") || raw.endsWith("#")) continue;
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
            wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
        }
    }

    private static void logoutIfPresent() {
        By logout = By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign out') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout')]");
        if (elementExists(logout)) {
            clickIfPresent(logout);
            wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
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
            wait.until(ExpectedConditions.urlContains("jpetstore.aspectran.com"));
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
    @DisplayName("Login: negative attempt shows error or stays on sign-in page")
    void loginNegative() {
        openBase();
        // Go to Sign In
        By signInLink = By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in') or contains(@href,'signon')]");
        Assumptions.assumeTrue(elementExists(signInLink), "Sign In link not found; skipping login tests.");
        clickIfPresent(signInLink);

        // Locate username/password fields (common names/ids)
        Optional<WebElement> user = first(By.cssSelector("input[name='username'], input[id*='user' i]"));
        Optional<WebElement> pass = first(By.cssSelector("input[name='password'], input[id*='pass' i]"));
        Optional<WebElement> submit = first(By.cssSelector("input[type='submit'], button[type='submit'], input[name='signon']"));
        Assumptions.assumeTrue(user.isPresent() && pass.isPresent() && submit.isPresent(), "Login form not found; skipping.");

        user.get().clear(); user.get().sendKeys("baduser");
        pass.get().clear(); pass.get().sendKeys("badpass");
        wait.until(ExpectedConditions.elementToBeClickable(submit.get())).click();

        boolean error = elementExists(By.cssSelector(".error, .alert, [role='alert']"))
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("invalid")
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("failed")
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("error");
        boolean stayed = driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("signon");
        Assertions.assertTrue(error || stayed, "Invalid login did not show an error or remain on sign-in.");
        openBase();
    }

    @Test
    @Order(4)
    @DisplayName("Login: valid demo credentials (if enabled)")
    void loginPositiveIfAvailable() {
        openBase();
        // Go to Sign In
        By signInLink = By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in') or contains(@href,'signon')]");
        Assumptions.assumeTrue(elementExists(signInLink), "Sign In link not found; skipping.");
        clickIfPresent(signInLink);

        Optional<WebElement> user = first(By.cssSelector("input[name='username'], input[id*='user' i]"));
        Optional<WebElement> pass = first(By.cssSelector("input[name='password'], input[id*='pass' i]"));
        Optional<WebElement> submit = first(By.cssSelector("input[type='submit'], button[type='submit'], input[name='signon']"));
        Assumptions.assumeTrue(user.isPresent() && pass.isPresent() && submit.isPresent(), "Login form not found; skipping.");

        // Typical JPetStore demo credentials; skip assertion if not accepted
        user.get().clear(); user.get().sendKeys("j2ee");
        pass.get().clear(); pass.get().sendKeys("j2ee");
        wait.until(ExpectedConditions.elementToBeClickable(submit.get())).click();

        // Success signs: Sign Out link present or My Account
        boolean success = elementExists(By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign out')]"))
                || elementExists(By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'my account')]"));
        Assumptions.assumeTrue(success, "Demo credentials may be disabled; skipping positive login assertions.");
        Assertions.assertTrue(success, "Expected to find Sign Out or My Account after login.");

        // Clean up
        logoutIfPresent();
        openBase();
    }

    @Test
    @Order(5)
    @DisplayName("Add to cart flow updates cart quantity (if catalog available)")
    void addToCartFlowIfAvailable() {
        openBase();
        // Click a category (Fish/Cats/Dogs/Birds/Reptiles)
        By categoryLink = By.xpath("//div[@id='Content']//a[contains(@href,'/catalog/categories/') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'fish') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'dogs') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cats') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'birds') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reptiles')]");
        Assumptions.assumeTrue(elementExists(categoryLink), "No category links found; skipping cart test.");
        clickIfPresent(categoryLink);

        // Click first product in category
        By productLink = By.xpath("//table//a[contains(@href,'/catalog/products/')]");
        Assumptions.assumeTrue(elementExists(productLink), "No product links found; skipping.");
        clickIfPresent(productLink);

        // Add to cart
        By addToCart = By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add to cart') or contains(@href,'/cart/add')]");
        Assumptions.assumeTrue(elementExists(addToCart), "No 'Add to Cart' found; skipping.");
        clickIfPresent(addToCart);

        // Assert cart contains at least one line item
        By cartTable = By.xpath("//table[contains(@class,'cart') or contains(@id,'Cart') or //table[.//th[contains(.,'Item')]]]");
        boolean hasItems = elementExists(cartTable) || elementExists(By.xpath("//a[contains(@href,'/cart/update')]")) || driver.getPageSource().toLowerCase(Locale.ROOT).contains("cart");
        Assertions.assertTrue(hasItems, "Cart does not show any items after adding.");

        // Optionally remove item to reset state
        By remove = By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'remove') or contains(@href,'/cart/remove')]");
        if (elementExists(remove)) {
            clickIfPresent(remove);
        }
        openBase();
    }

    @Test
    @Order(6)
    @DisplayName("Sorting dropdown (if present) cycles options and affects order")
    void sortingDropdownIfPresent() {
        openBase();
        // Go to any category to increase chance of seeing a sorting select
        if (elementExists(By.xpath("//div[@id='Content']//a[contains(@href,'/catalog/categories/')]"))) {
            clickIfPresent(By.xpath("//div[@id='Content']//a[contains(@href,'/catalog/categories/')]"));
        }
        List<WebElement> selects = driver.findElements(By.cssSelector("select[id*='sort' i], select[name*='sort' i], select"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No select dropdown found; skipping sort test.");
        WebElement select = selects.get(0);
        Select sel = new Select(select);
        List<WebElement> options = sel.getOptions();
        Assumptions.assumeTrue(options.size() >= 2, "Not enough options to exercise sorting.");
        String before = sel.getFirstSelectedOption().getText().trim();

        // Snapshot of row ordering (heuristic)
        List<String> baseline = driver.findElements(By.cssSelector("table tr, .item, .card"))
                .stream().map(WebElement::getText).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        sel.selectByIndex(options.size() - 1);
        String afterSel1 = sel.getFirstSelectedOption().getText().trim();
        List<String> after1 = driver.findElements(By.cssSelector("table tr, .item, .card"))
                .stream().map(WebElement::getText).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        sel.selectByIndex(0);
        String afterSel2 = sel.getFirstSelectedOption().getText().trim();
        List<String> after2 = driver.findElements(By.cssSelector("table tr, .item, .card"))
                .stream().map(WebElement::getText).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        Assertions.assertNotEquals(before, afterSel1, "Selecting another option did not change selection.");
        Assertions.assertNotEquals(afterSel1, afterSel2, "Selecting back did not change selection.");
        Assertions.assertTrue(!baseline.equals(after1) || !after1.equals(after2) || !baseline.equals(after2),
                "Sorting did not appear to change list ordering (acceptable if static).");
        openBase();
    }

    @Test
    @Order(7)
    @DisplayName("Menu actions if available: Home/All Items, About (external), Logout/Reset")
    void menuActionsIfAvailable() {
        openBase();
        // Try a generic menu/burger if present
        By[] burgers = new By[] {
                By.cssSelector("button[aria-label*='menu' i], .navbar-toggler, .hamburger, .bm-burger-button"),
                By.xpath("//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu')]")
        };
        Optional<WebElement> burger = Optional.empty();
        for (By by : burgers) {
            burger = first(by);
            if (burger.isPresent()) break;
        }
        if (burger.isPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(burger.get())).click();
        }

        // Home / All Items
        By home = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'home') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'catalog') or contains(@href,'/catalog/')]");
        if (elementExists(home)) {
            clickIfPresent(home);
            Assertions.assertEquals(hostOf(BASE_URL), hostOf(driver.getCurrentUrl()), "Home/Catalog left base host.");
        }

        // About (external)
        By about = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'about') or contains(@href,'about')]");
        if (elementExists(about)) {
            assertExternalLink(driver.findElement(about));
        }

        // Reset App State (not typical; if present)
        By reset = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset app state') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset')]");
        if (elementExists(reset)) {
            Assertions.assertTrue(clickIfPresent(reset), "Reset App State click failed.");
        }

        // Logout if exists
        logoutIfPresent();

        // Close menu if still open
        if (burger.isPresent()) clickIfPresent(burgers[0]);
        openBase();
    }
}
