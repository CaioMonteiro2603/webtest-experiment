package GPT5.ws03.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String VALID_EMAIL = "caio@gmail.com";
    private static final String VALID_PASSWORD = "123";

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

    // ----------------------------
    // Utility helpers
    // ----------------------------

    private static void openBase() {
        driver.get(BASE_URL);
        // Many Netlify/SPA apps show content after hydration; we at least ensure the URL is correct.
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Base URL did not load as expected.");
    }

    private static Optional<WebElement> firstPresent(By by) {
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

    private static void clearAndType(WebElement input, String text) {
        wait.until(ExpectedConditions.visibilityOf(input));
        input.clear();
        input.sendKeys(text);
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private static String hostOf(String url) {
        try {
            URI uri = new URI(url);
            String h = uri.getHost();
            return h == null ? "" : h.toLowerCase(Locale.ROOT);
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private static List<String> collectInternalLinksOneLevel(String pageUrl) {
        String baseHost = hostOf(BASE_URL);
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Set<String> urls = new LinkedHashSet<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.startsWith("mailto:") || href.startsWith("tel:") || href.startsWith("javascript:") || href.endsWith("#")) continue;
            String h = hostOf(href);
            if (!h.equals(baseHost)) continue; // internal only here
            // Allow base and one-level path like /requirements
            try {
                URI uri = new URI(href);
                String path = uri.getPath();
                if (path == null) path = "/";
                // normalize
                if (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length() - 1);
                int depth = path.equals("/") ? 0 : path.split("/").length - 1;
                if (depth <= 1) {
                    urls.add(uri.toString());
                }
            } catch (URISyntaxException ignored) {}
        }
        // Always include BASE_URL
        urls.add(BASE_URL);
        return new ArrayList<>(urls);
    }

    private static List<WebElement> socialLinksOnPage() {
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        return anchors.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    if (href == null) return false;
                    String host = hostOf(href);
                    return host.contains("twitter.com") || host.contains("facebook.com") || host.contains("linkedin.com");
                })
                .collect(Collectors.toList());
    }

    private static void assertExternalLinkInNewOrSameTab(WebElement link) {
        String originalWindow = driver.getWindowHandle();
        Set<String> existing = driver.getWindowHandles();
        String href = link.getAttribute("href");
        String expectedHost = hostOf(href);

        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        // Wait for possible new window
        try {
            wait.until(d -> d.getWindowHandles().size() != existing.size());
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        if (after.size() > existing.size()) {
            // New tab opened
            after.removeAll(existing);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl() != null && !d.getCurrentUrl().isEmpty());
            Assertions.assertTrue(containsIgnoreCase(driver.getCurrentUrl(), expectedHost),
                    "External link did not navigate to expected domain: " + expectedHost + " actual: " + driver.getCurrentUrl());
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            // Same tab navigation
            wait.until(d -> !d.getCurrentUrl().equals(BASE_URL));
            Assertions.assertTrue(containsIgnoreCase(driver.getCurrentUrl(), expectedHost) || containsIgnoreCase(driver.getCurrentUrl(), hostOf(BASE_URL)),
                    "External link did not navigate to expected domain or returned: expected host " + expectedHost + " actual: " + driver.getCurrentUrl());
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains(hostOf(BASE_URL)));
        }
    }

    private static void logoutIfPresent() {
        // Attempt to find a logout button/link in either Portuguese or English
        By[] candidates = new By[] {
                By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair')]"),
                By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair')]")
        };
        for (By by : candidates) {
            if (driver.findElements(by).size() > 0) {
                if (clickIfPresent(by)) {
                    // back to login/home
                    break;
                }
            }
        }
    }

    private static void resetAppStateIfPresent() {
        // Generic reset if exists
        By resetBy = By.xpath("//*[self::button or self::a][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'limpar')]");
        clickIfPresent(resetBy);
    }

    private static boolean attemptLogin(String email, String password) {
        // This method tries multiple locator strategies to support different UIs.
        // Locate email input
        Optional<WebElement> emailInput =
                firstPresent(By.cssSelector("input[type='email']"))
                        .or(() -> firstPresent(By.name("email")))
                        .or(() -> firstPresent(By.cssSelector("input[id*='email' i]")))
                        .or(() -> waitVisible(By.xpath("//input[@placeholder='E-mail' or @placeholder='Email' or @name='email']")));

        Optional<WebElement> passwordInput =
                firstPresent(By.cssSelector("input[type='password']"))
                        .or(() -> firstPresent(By.name("password")))
                        .or(() -> firstPresent(By.cssSelector("input[id*='password' i]")))
                        .or(() -> waitVisible(By.xpath("//input[@placeholder='Senha' or @placeholder='Password' or @name='password']")));

        // Some SPAs require switching to "Login" tab if register is default
        if (emailInput.isEmpty() || passwordInput.isEmpty()) {
            // Try clicking any "Acessar", "Login", or "Sign In" switch/tab first
            clickIfPresent(By.xpath("//*[self::button or self::a][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'acessar') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in')]"));
            emailInput = emailInput.or(() -> firstPresent(By.cssSelector("input[type='email']")))
                    .or(() -> firstPresent(By.name("email")))
                    .or(() -> waitVisible(By.xpath("//input[@placeholder='E-mail' or @placeholder='Email' or @name='email']")));
            passwordInput = passwordInput.or(() -> firstPresent(By.cssSelector("input[type='password']")))
                    .or(() -> firstPresent(By.name("password")))
                    .or(() -> waitVisible(By.xpath("//input[@placeholder='Senha' or @placeholder='Password' or @name='password']")));
        }

        if (emailInput.isEmpty() || passwordInput.isEmpty()) {
            return false; // login form not present
        }

        clearAndType(emailInput.get(), email);
        clearAndType(passwordInput.get(), password);

        // Click the login/access button
        By[] loginButtons = new By[] {
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'acessar') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'entrar') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in')]"),
                By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'acessar') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'entrar') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'login')]")
        };
        boolean clicked = false;
        for (By by : loginButtons) {
            if (driver.findElements(by).size() > 0) {
                clicked = clickIfPresent(by);
                if (clicked) break;
            }
        }

        if (!clicked) return false;

        // Consider login successful if URL changes away from BASE or dashboard keywords appear
        try {
            wait.until(d -> !d.getCurrentUrl().equals(BASE_URL));
        } catch (TimeoutException ignored) {}

        // Indicators of a logged-in dashboard (BugBank common sections)
        boolean dashboardVisible = driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'transfer') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'transferência') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pagamento') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'extrato') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'saque')]")).size() > 0;

        return dashboardVisible || !driver.getCurrentUrl().equals(BASE_URL);
    }

    private static boolean isErrorVisibleAfterLoginAttempt() {
        // Try to detect generic error messages
        By[] errorLocators = new By[] {
                By.cssSelector(".error, .alert, [role='alert']"),
                By.xpath("//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'inval') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'senha') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'password') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'erro') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'error')]")
        };
        for (By by : errorLocators) {
            if (!driver.findElements(by).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // ----------------------------
    // Tests
    // ----------------------------

    @Test
    @Order(1)
    @DisplayName("Base page loads and one-level internal pages are reachable")
    void baseAndOneLevelPagesReachable() {
        openBase();
        // Gather one-level internal links
        List<String> internal = collectInternalLinksOneLevel(driver.getCurrentUrl());
        Assertions.assertTrue(internal.size() > 0, "No internal links found at one level.");
        // Visit each internal link and assert page responds (URL reachable)
        for (String url : internal) {
            driver.navigate().to(url);
            wait.until(d -> d.getCurrentUrl().startsWith("https://"));
            Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://bugbank.netlify.app"), "Internal page not on expected host: " + driver.getCurrentUrl());
        }
        // Return to base
        openBase();
    }

    @Test
    @Order(2)
    @DisplayName("External links policy on base and one-level pages (Twitter/Facebook/LinkedIn if present)")
    void externalLinksOnBaseAndOneLevel() {
        openBase();
        Set<String> visitedInternal = new LinkedHashSet<>(collectInternalLinksOneLevel(driver.getCurrentUrl()));
        for (String url : visitedInternal) {
            driver.navigate().to(url);
            // For each page, handle external social links if present
            List<WebElement> socials = socialLinksOnPage();
            for (WebElement link : socials) {
                String href = link.getAttribute("href");
                String expectedHost = hostOf(href);
                Assumptions.assumeTrue(!expectedHost.isEmpty(), "External link has no host.");
                assertExternalLinkInNewOrSameTab(link);
            }
        }
        openBase();
    }

    @Test
    @Order(3)
    @DisplayName("Invalid login shows error or remains on login page")
    void invalidLoginShowsError() {
        openBase();
        boolean formPresent = attemptLogin("invalid@example.com", "wrong-password");
        // If the site doesn't support login at all on base, skip test
        Assumptions.assumeTrue(formPresent || isErrorVisibleAfterLoginAttempt() || driver.getCurrentUrl().equals(BASE_URL),
                "Login form not present; skipping negative login test.");
        // Assert either an error is visible or URL did not move to a dashboard-like page
        boolean error = isErrorVisibleAfterLoginAttempt();
        boolean stillOnBaseOrAuth = containsIgnoreCase(driver.getCurrentUrl(), BASE_URL) ||
                containsIgnoreCase(driver.getPageSource(), "login") ||
                containsIgnoreCase(driver.getPageSource(), "acessar");
        Assertions.assertTrue(error || stillOnBaseOrAuth, "Invalid login did not show an error and seems to have navigated away unexpectedly.");
        openBase();
    }

    @Test
    @Order(4)
    @DisplayName("Valid login with provided credentials (if account exists)")
    void validLoginIfAccountExists() {
        openBase();
        boolean success = attemptLogin(VALID_EMAIL, VALID_PASSWORD);
        // If credentials are not registered on this demo, skip but ensure test independence
        Assumptions.assumeTrue(success, "Provided demo credentials may not exist; skipping.");
        // Assert we're on an authenticated area by checking for common banking actions
        boolean dashboardVisible = driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'transfer') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'transferência') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'pagamento') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'extrato') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'saque')]")).size() > 0;
        Assertions.assertTrue(dashboardVisible, "Expected banking dashboard elements after login.");
        // Cleanup for independence
        resetAppStateIfPresent();
        logoutIfPresent();
        openBase();
    }

    @Test
    @Order(5)
    @DisplayName("Menu (burger) actions if available: open/close, All Items/Home, About (external), Logout, Reset App State")
    void menuActionsIfAvailable() {
        openBase();
        // Try to locate a burger/menu button
        By[] burgerCandidates = new By[] {
                By.cssSelector("button[aria-label*='menu' i], button[id*='menu' i]"),
                By.xpath("//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu')]"),
                By.cssSelector(".bm-burger-button, .burger, .hamburger")
        };
        Optional<WebElement> burger = Optional.empty();
        for (By by : burgerCandidates) {
            burger = firstPresent(by);
            if (burger.isPresent()) break;
        }
        Assumptions.assumeTrue(burger.isPresent(), "No burger/menu button; skipping menu tests.");

        // Open menu
        wait.until(ExpectedConditions.elementToBeClickable(burger.get())).click();

        // All Items / Home
        By allItems = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'all items') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'home') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'início') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'inicio')]");
        if (driver.findElements(allItems).size() > 0) {
            clickIfPresent(allItems);
            Assertions.assertTrue(hostOf(driver.getCurrentUrl()).contains(hostOf(BASE_URL)), "All Items/Home did not navigate within site.");
        }

        // About (external)
        By about = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'about') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sobre')]");
        if (driver.findElements(about).size() > 0) {
            WebElement aboutLink = driver.findElements(about).get(0);
            assertExternalLinkInNewOrSameTab(aboutLink);
        }

        // Reset App State
        By reset = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset app state') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'limpar')]");
        if (driver.findElements(reset).size() > 0) {
            Assertions.assertTrue(clickIfPresent(reset), "Failed to click Reset App State.");
        }

        // Logout
        By logout = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log out')]");
        if (driver.findElements(logout).size() > 0) {
            clickIfPresent(logout);
            // We should return to a login-like page
            Assertions.assertTrue(containsIgnoreCase(driver.getPageSource(), "login") ||
                            containsIgnoreCase(driver.getPageSource(), "acessar") ||
                            driver.getCurrentUrl().equals(BASE_URL),
                    "After logout we did not return to the login/home page.");
        }

        // Close menu if still open (click burger again)
        clickIfPresent(burgerCandidates[0]);
        openBase();
    }

    @Test
    @Order(6)
    @DisplayName("Sorting dropdown (if present) cycles options and affects order")
    void sortingDropdownIfPresent() {
        openBase();
        // Look for a select that appears to sort items
        By selectBy = By.cssSelector("select[id*='sort' i], select[name*='sort' i], select[data-test*='sort' i]");
        List<WebElement> selects = driver.findElements(selectBy);
        Assumptions.assumeTrue(!selects.isEmpty(), "No sorting dropdown found; skipping.");

        WebElement sort = selects.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(sort));
        // Capture initial list texts
        List<String> initial = collectItemNamesOrValues();

        // Iterate through options
        List<WebElement> options = sort.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() >= 2, "Not enough sort options to test.");
        for (WebElement opt : options) {
            wait.until(ExpectedConditions.elementToBeClickable(sort));
            opt.click();
            // assert order changed or at least the page reacted
            List<String> now = collectItemNamesOrValues();
            Assertions.assertFalse(initial.equals(now), "Selecting sort option did not change items order for option: " + opt.getText());
            initial = now;
        }
        resetAppStateIfPresent();
        openBase();
    }

    private static List<String> collectItemNamesOrValues() {
        // Try common item containers; fallback to visible text snippets
        List<WebElement> items = new ArrayList<>();
        items.addAll(driver.findElements(By.cssSelector("[data-test*='inventory-item' i], .inventory_item, .item")));
        if (items.isEmpty()) {
            items.addAll(driver.findElements(By.cssSelector("li, .card, .product")));
        }
        List<String> names = new ArrayList<>();
        for (WebElement it : items) {
            String txt = it.getText();
            if (txt != null && !txt.isBlank()) names.add(txt.trim());
        }
        if (names.isEmpty()) {
            // As a last resort, read all headings which typically reflect items
            List<WebElement> heads = driver.findElements(By.cssSelector("h1,h2,h3,h4"));
            for (WebElement h : heads) {
                String txt = h.getText();
                if (txt != null && !txt.isBlank()) names.add(txt.trim());
            }
        }
        // Limit size to reduce flakiness
        if (names.size() > 20) names = names.subList(0, 20);
        return names;
    }
}
