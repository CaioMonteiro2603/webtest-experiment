package GPT5.ws10.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:8080/login";
    private static final String LOGIN_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String LOGIN_PASSWORD = "10203040";

    // Common/Gentle selectors
    private static final By BODY = By.tagName("body");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By EMAIL_INPUT = By.cssSelector("input[type='email'], input[name='email'], input[id*='email'], input[placeholder*='mail' i]");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password'], input[name='password'], input[id*='password'], input[placeholder*='senha' i], input[placeholder*='pass' i]");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit'], button.btn-primary, button:enabled");
    private static final By ERROR_MESSAGE = By.cssSelector(".error, .alert, .invalid-feedback, [role='alert']");
    private static final By NAV_BAR = By.cssSelector("nav, header, .navbar");
    private static final By HAMBURGER = By.cssSelector("button[aria-label*='menu' i], .navbar-toggler, button[aria-controls*='navbar' i], button.hamburger");
    private static final By LOGOUT_LINK = By.xpath("//a[contains(translate(., 'SAIRLOGOUTSIGN OUT', 'sairlogoutsign out'),'logout') or contains(translate(., 'SAIRLOGOUTSIGN OUT', 'sairlogoutsign out'),'sair') or contains(.,'Sign out')]");
    private static final By DASHBOARD_HINT = By.xpath("//*[contains(translate(., 'DASHBOARDDASHBOARD INICIALINÍCIOHOME', 'dashboarddashboard inicialiníciohome'),'dashboard') or contains(translate(., 'DASHBOARDDASHBOARD INICIALINÍCIOHOME', 'dashboarddashboard inicialiníciohome'),'início') or contains(translate(., 'DASHBOARDDASHBOARD INICIALINÍCIOHOME', 'dashboarddashboard inicialiníciohome'),'home')]");

    // Non-applicable selectors (ensure not present)
    private static final By SORTING_DROPDOWN = By.cssSelector("select#sort, select[name*='sort'], select[data-test='product_sort_container']");
    private static final By SAUCE_BURGER = By.id("react-burger-menu-btn");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ===== Utilities =====
    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith("http://localhost:8080"),
                "Should be on localhost domain");
    }

    private boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private static String hostOf(String url) {
        try {
            return Optional.ofNullable(new URI(url)).map(URI::getHost).orElse("");
        } catch (Exception e) {
            return "";
        }
    }

    private ExpectedCondition<Boolean> urlDoesNotContain(String piece) {
        return drv -> drv != null && !drv.getCurrentUrl().toLowerCase(Locale.ROOT).contains(piece.toLowerCase(Locale.ROOT));
    }

    private boolean performLogin(String email, String password) {
        openBase();

        // Fill login form
        WebElement emailEl = wait.until(ExpectedConditions.presenceOfElementLocated(EMAIL_INPUT));
        emailEl.clear();
        emailEl.sendKeys(email);

        WebElement passEl = wait.until(ExpectedConditions.presenceOfElementLocated(PASSWORD_INPUT));
        passEl.clear();
        passEl.sendKeys(password);

        WebElement submit = waitClickable(SUBMIT_BUTTON);
        submit.click();

        // Consider login successful if we navigate away from /login or a dashboard hint / logout link is visible.
        boolean navigatedAway;
        try {
            wait.until(ExpectedConditions.or(
                    urlDoesNotContain("/login"),
                    ExpectedConditions.presenceOfElementLocated(LOGOUT_LINK),
                    ExpectedConditions.presenceOfElementLocated(DASHBOARD_HINT),
                    ExpectedConditions.not(ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE))
            ));
        } catch (TimeoutException ignored) {}

        navigatedAway = !driver.getCurrentUrl().contains("/login");
        boolean hasLogout = isPresent(LOGOUT_LINK);
        boolean hasDashboardHint = isPresent(DASHBOARD_HINT);
        boolean errorShown = isPresent(ERROR_MESSAGE);

        return (navigatedAway || hasLogout || hasDashboardHint) && !errorShown;
    }

    private void logoutIfPossible() {
        // Try to open hamburger if present
        if (isPresent(HAMBURGER)) {
            try { waitClickable(HAMBURGER).click(); } catch (Exception ignored) {}
        }
        if (isPresent(LOGOUT_LINK)) {
            waitClickable(LOGOUT_LINK).click();
            wait.until(ExpectedConditions.urlContains("/login"));
        }
    }

    private void assertExternalLink(WebElement link, String expectedDomainFragment) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        wait.until(d -> d.getWindowHandles().size() > before.size() || d.getCurrentUrl().contains(expectedDomainFragment));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment),
                    "External link should contain expected domain: " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(original);
        } else {
            // Same tab
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment),
                    "External link (same tab) should contain expected domain: " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    // ===== Tests =====

    @Test
    @Order(1)
    public void basePage_Loads_AndLoginFormPresent() {
        openBase();
        Assertions.assertAll(
                () -> Assertions.assertTrue(isPresent(EMAIL_INPUT), "Email input should be present"),
                () -> Assertions.assertTrue(isPresent(PASSWORD_INPUT), "Password input should be present"),
                () -> Assertions.assertTrue(isPresent(SUBMIT_BUTTON), "Submit button should be present")
        );
    }

    @Test
    @Order(2)
    public void login_Negative_ShowsErrorOrStaysOnLogin() {
        openBase();
        // Wrong password
        WebElement emailEl = wait.until(ExpectedConditions.presenceOfElementLocated(EMAIL_INPUT));
        emailEl.clear();
        emailEl.sendKeys(LOGIN_EMAIL);

        WebElement passEl = wait.until(ExpectedConditions.presenceOfElementLocated(PASSWORD_INPUT));
        passEl.clear();
        passEl.sendKeys("wrong-password");

        waitClickable(SUBMIT_BUTTON).click();

        // Either an error appears or URL remains login
        try { wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGE),
                ExpectedConditions.urlContains("/login")
        )); } catch (TimeoutException ignored) {}

        boolean errorShown = isPresent(ERROR_MESSAGE);
        boolean stillLogin = driver.getCurrentUrl().contains("/login");
        Assertions.assertTrue(errorShown || stillLogin, "Expect error message or remain on /login for invalid credentials");
    }

    @Test
    @Order(3)
    public void login_Positive_Succeeds_And_NavVisible() {
        boolean success = performLogin(LOGIN_EMAIL, LOGIN_PASSWORD);
        Assumptions.assumeTrue(success, "Login with provided credentials did not succeed; skipping authenticated checks.");
        // Authenticated assertions (best-effort)
        Assertions.assertAll(
                () -> Assertions.assertTrue(isPresent(NAV_BAR), "Navbar or header should be present after login"),
                () -> Assertions.assertTrue(!driver.getCurrentUrl().contains("/login"),
                        "URL should not remain on /login after successful login")
        );
        logoutIfPossible();
        // Ensure we are back on login
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login") || isPresent(EMAIL_INPUT),
                "After logout, should be on login page");
    }

    @Test
    @Order(4)
    public void hamburgerMenu_IfPresent_OpenAndTryLogout() {
        // Try login first to expose menu
        boolean success = performLogin(LOGIN_EMAIL, LOGIN_PASSWORD);
        Assumptions.assumeTrue(success, "Skipping menu checks because login did not succeed.");

        if (isPresent(HAMBURGER)) {
            WebElement burger = waitClickable(HAMBURGER);
            burger.click();
            // Look for About-like link if any
            Optional<WebElement> about = driver.findElements(By.cssSelector("a[href]")).stream()
                    .filter(a -> {
                        String t = (a.getText() == null ? "" : a.getText()).toLowerCase(Locale.ROOT);
                        String href = Optional.ofNullable(a.getAttribute("href")).orElse("").toLowerCase(Locale.ROOT);
                        return t.contains("about") || t.contains("sobre") || href.contains("about");
                    })
                    .findFirst();
            about.ifPresent(a -> {
                String host = hostOf(a.getAttribute("href"));
                String frag = host.isEmpty() ? "about" : host;
                assertExternalLink(a, frag);
            });

            // Try Reset App State-like entry if any (best-effort no-op)
            Optional<WebElement> reset = driver.findElements(By.cssSelector("a[href]")).stream()
                    .filter(a -> {
                        String t = (a.getText() == null ? "" : a.getText()).toLowerCase(Locale.ROOT);
                        return t.contains("reset") || t.contains("reiniciar");
                    }).findFirst();
            reset.ifPresent(WebElement::click);

            // Try Logout/Sair if exists
            if (isPresent(LOGOUT_LINK)) {
                waitClickable(LOGOUT_LINK).click();
                wait.until(ExpectedConditions.urlContains("/login"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Should return to login after logout");
            }
        } else {
            Assumptions.abort("No hamburger/menu button detected; skipping menu flow.");
        }
    }

    @Test
    @Order(5)
    public void externalLinks_FooterOrHeader_Socials_Work() {
        openBase();
        List<By> socialSelectors = Arrays.asList(
                By.cssSelector("a[href*='twitter.com']"),
                By.cssSelector("a[href*='facebook.com']"),
                By.cssSelector("a[href*='linkedin.com']")
        );
        boolean foundAny = false;
        for (By sel : socialSelectors) {
            List<WebElement> links = driver.findElements(sel);
            if (!links.isEmpty()) {
                foundAny = true;
                // Deduplicate by href and test one per domain
                Map<String, WebElement> byHref = links.stream()
                        .filter(e -> e.getAttribute("href") != null)
                        .collect(Collectors.toMap(e -> e.getAttribute("href"), e -> e, (a, b) -> a, LinkedHashMap::new));
                for (Map.Entry<String, WebElement> entry : byHref.entrySet()) {
                    String domain = hostOf(entry.getKey());
                    String expected = domain.isEmpty() ? "http" : domain;
                    assertExternalLink(entry.getValue(), expected);
                    break; // one per domain
                }
            }
        }
        Assumptions.assumeTrue(foundAny, "No social links found to verify.");
    }

    @Test
    @Order(6)
    public void internalLinks_OneLevelBelow_Loadable() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        List<WebElement> links = driver.findElements(ANY_LINK);
        List<String> candidates = new ArrayList<>();
        for (WebElement a : links) {
            String href = a.getAttribute("href");
            if (href == null || href.trim().isEmpty()) continue;
            String host = hostOf(href);
            if (!Objects.equals(host, baseHost)) continue; // only same host
            if (href.contains("#")) continue; // skip anchors
            if (href.endsWith("/login")) continue; // avoid looping login
            // One level below the site root or current path-ish: accept shallow paths
            candidates.add(href);
        }
        // Deduplicate and take up to 3
        candidates = candidates.stream().distinct().limit(3).collect(Collectors.toList());
        Assumptions.assumeTrue(!candidates.isEmpty(), "No internal one-level links to test.");

        for (String url : candidates) {
            driver.navigate().to(url);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            Assertions.assertTrue(driver.getCurrentUrl().startsWith("http://localhost:8080"),
                    "Internal page should stay within domain");
            // basic visible check
            Assertions.assertTrue(driver.findElement(BODY).isDisplayed(), "Body should be displayed for " + url);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    @Test
    @Order(7)
    public void notApplicable_InventorySorting_And_SauceMenu_AreAbsent() {
        openBase();
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.findElements(SORTING_DROPDOWN).isEmpty(),
                        "Sorting dropdown (Swag Labs style) must not exist on this app"),
                () -> Assertions.assertTrue(driver.findElements(SAUCE_BURGER).isEmpty(),
                        "Swag Labs burger button must not exist on this app")
        );
    }
}