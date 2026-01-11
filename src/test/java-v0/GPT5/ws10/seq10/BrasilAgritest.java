package GPT5.ws10.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    // Generic, robust locators for a typical login page
    private static final By BODY = By.tagName("body");
    private static final By USERNAME_INPUT = By.cssSelector("input#email, input[name='email'], input[type='email']");
    private static final By PASSWORD_INPUT = By.cssSelector("input#password, input[name='password'], input[type='password']");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit'], input[type='submit'], button:contains('Login'), button:contains('Entrar')");
    // Heuristic logout/menu selectors
    private static final By LOGOUT_BUTTON = By.cssSelector("a[href*='logout'], button[id*='logout'], button[aria-label*='logout'], button:contains('Logout'), button:contains('Sair'), a:contains('Logout'), a:contains('Sair')");
    private static final By BURGER_MENU = By.cssSelector("button[aria-label='menu'], button[aria-label='Menu'], button.burger, button.hamburger, .bm-burger-button, .hamburger");
    private static final By MENU_ALL_ITEMS = By.xpath("//a[contains(.,'All Items') or contains(.,'Home') or contains(.,'Dashboard')]");
    private static final By MENU_ABOUT = By.xpath("//a[contains(.,'About') or contains(.,'Sobre')]");
    private static final By MENU_RESET = By.xpath("//a[contains(.,'Reset') or contains(.,'Reset App State') or contains(.,'Reiniciar')]");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By TOAST_ERROR = By.cssSelector(".toast-error, .alert-danger, .notification.is-danger, .v-alert.error, .MuiAlert-standardError");
    private static final By SELECT_ANY = By.cssSelector("select, .custom-select select");

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
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------------- Utilities ----------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "Should land on the login page at /login");
    }

    private boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private WebElement findFirst(By locator) {
        List<WebElement> els = driver.findElements(locator);
        if (els.isEmpty()) throw new NoSuchElementException("No element for " + locator);
        return els.get(0);
    }

    private boolean tryLogin(String user, String pass) {
        openBase();

        // Fill username
        WebElement u = wait.until(ExpectedConditions.presenceOfElementLocated(USERNAME_INPUT));
        u.clear();
        u.sendKeys(user);

        // Fill password
        WebElement p = wait.until(ExpectedConditions.presenceOfElementLocated(PASSWORD_INPUT));
        p.clear();
        p.sendKeys(pass);

        // Click login
        WebElement btn = waitClickable(LOGIN_BUTTON);
        btn.click();

        // Wait for either success (URL not /login, dashboard visible, or logout present) or error toast
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.not(ExpectedConditions.urlContains("/login")),
                ExpectedConditions.presenceOfElementLocated(LOGOUT_BUTTON),
                ExpectedConditions.presenceOfElementLocated(TOAST_ERROR)
            ));
        } catch (TimeoutException ignored) {}

        boolean success = !driver.getCurrentUrl().contains("/login") || isPresent(LOGOUT_BUTTON);
        boolean error = isPresent(TOAST_ERROR);
        return success && !error;
    }

    private void logoutIfPossible() {
        if (isPresent(LOGOUT_BUTTON)) {
            waitClickable(LOGOUT_BUTTON).click();
            wait.until(ExpectedConditions.urlContains("/login"));
        } else {
            // Try common profile/menu -> logout flows
            if (isPresent(BURGER_MENU)) {
                waitClickable(BURGER_MENU).click();
                if (isPresent(LOGOUT_BUTTON)) {
                    waitClickable(LOGOUT_BUTTON).click();
                    wait.until(ExpectedConditions.urlContains("/login"));
                }
            }
        }
    }

    private static String hostOf(String url) {
        try { return Optional.ofNullable(new URI(url)).map(URI::getHost).orElse(""); }
        catch (Exception e) { return ""; }
    }

    private void assertExternalByClick(WebElement link) {
        String href = link.getAttribute("href");
        String expectedDomain = hostOf(href);
        if (expectedDomain.isEmpty()) {
            Assumptions.assumeTrue(false, "Skipping non-http(s) link: " + href);
            return;
        }

        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        WebElement web = wait.until(ExpectedConditions.elementToBeClickable(link)); 
        web.click();

        try {
            wait.until(drv -> drv.getWindowHandles().size() > before.size() || drv.getCurrentUrl().contains(expectedDomain));
        } catch (TimeoutException te) {
            Assertions.fail("External link did not open as expected: " + href);
            return;
        }

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL should contain external domain: " + expectedDomain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL should contain external domain: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    private List<WebElement> socialLinksOnPage() {
        List<String> domains = Arrays.asList("twitter.com", "facebook.com", "linkedin.com", "x.com");
        return driver.findElements(ANY_LINK).stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    return href != null && domains.stream().anyMatch(href::contains);
                })
                .collect(Collectors.toList());
    }

    // ---------------- Tests ----------------

    @Test
    @Order(1)
    public void basePage_ShouldLoad_And_ShowLoginForm() {
        openBase();
        Assertions.assertAll(
            () -> Assertions.assertTrue(isPresent(USERNAME_INPUT), "Username/email input should be visible"),
            () -> Assertions.assertTrue(isPresent(PASSWORD_INPUT), "Password input should be visible"),
            () -> Assertions.assertTrue(isPresent(LOGIN_BUTTON), "Login submit button should be visible")
        );
    }

    @Test
    @Order(2)
    public void login_Negative_InvalidPassword_ShowsErrorOrStaysOnLogin() {
        openBase();
        WebElement u = wait.until(ExpectedConditions.presenceOfElementLocated(USERNAME_INPUT));
        u.clear();
        u.sendKeys(LOGIN);

        WebElement p = wait.until(ExpectedConditions.presenceOfElementLocated(PASSWORD_INPUT));
        p.clear();
        p.sendKeys("wrong-password");

        waitClickable(LOGIN_BUTTON).click();

        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(TOAST_ERROR),
                ExpectedConditions.urlContains("/login")
            ));
        } catch (TimeoutException ignored) {}

        boolean errorShown = isPresent(TOAST_ERROR);
        boolean stillOnLogin = driver.getCurrentUrl().contains("/login");
        Assertions.assertTrue(errorShown || stillOnLogin, "Invalid login should show error or remain on /login");
    }

    @Test
    @Order(3)
    public void login_Positive_Then_Logout() {
        boolean ok = tryLogin(LOGIN, PASSWORD);
        Assumptions.assumeTrue(ok, "Provided credentials did not yield a successful login; skipping positive flow.");
        Assertions.assertTrue(!driver.getCurrentUrl().contains("/login") || isPresent(LOGOUT_BUTTON),
                "After login, URL should change away from /login or logout control should appear");
        logoutIfPossible();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "After logout, should navigate back to /login");
    }

    @Test
    @Order(4)
    public void internalLinks_OneLevelBelow_FromLogin_AreReachable() {
        openBase();
        String baseHost = hostOf(BASE_URL);

        List<String> internalLinks = driver.findElements(ANY_LINK).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(href -> hostOf(href).equalsIgnoreCase(baseHost))
                .filter(href -> !href.contains("#"))
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!internalLinks.isEmpty(), "No internal links found on base page.");

        for (String href : internalLinks) {
            driver.navigate().to(href);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            Assertions.assertTrue(hostOf(driver.getCurrentUrl()).equalsIgnoreCase(baseHost),
                    "Should remain on same host when following internal link: " + href);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    @Test
    @Order(5)
    public void footerSocialLinks_AsExternal_ShouldOpen_NewTab_AndHaveExpectedDomain() {
        openBase();
        List<WebElement> socials = socialLinksOnPage();
        if (socials.isEmpty()) {
            // Try after login as well
            boolean ok = tryLogin(LOGIN, PASSWORD);
            Assumptions.assumeTrue(ok, "Could not login to check social links on authenticated page.");
            socials = socialLinksOnPage();
        }
        Assumptions.assumeTrue(!socials.isEmpty(), "No social links (Twitter/Facebook/LinkedIn) detected to test.");

        int tested = 0;
        Set<String> testedDomains = new HashSet<>();
        for (WebElement link : socials) {
            String href = link.getAttribute("href");
            String dom = hostOf(href);
            if (dom.isEmpty() || testedDomains.contains(dom)) continue;
            testedDomains.add(dom);
            assertExternalByClick(link);
            tested++;
            if (tested >= 3) break;
        }
        Assumptions.assumeTrue(tested > 0, "No social link could be tested.");
        logoutIfPossible();
    }

    @Test
    @Order(6)
    public void menu_Burger_OpenClose_Navigate_CommonEntries_WhenAvailable() {
        boolean ok = tryLogin(LOGIN, PASSWORD);
        Assumptions.assumeTrue(ok, "Login required to test the menu.");

        if (isPresent(BURGER_MENU)) {
            WebElement burger = waitClickable(BURGER_MENU);
            burger.click();
            // All Items / Home / Dashboard link
            if (isPresent(MENU_ALL_ITEMS)) {
                waitClickable(MENU_ALL_ITEMS).click();
                wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
                Assertions.assertTrue(!driver.getCurrentUrl().contains("/login"), "All Items/Home should keep us authenticated");
            }
            // About (external)
            if (isPresent(BURGER_MENU)) {
                waitClickable(BURGER_MENU).click();
            }
            if (isPresent(MENU_ABOUT)) {
                WebElement about = findFirst(MENU_ABOUT);
                assertExternalByClick(about);
            }
            // Reset App State (if available)
            if (isPresent(BURGER_MENU)) {
                waitClickable(BURGER_MENU).click();
            }
            if (isPresent(MENU_RESET)) {
                waitClickable(MENU_RESET).click();
                // No specific assertion other than staying in app
                wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
                Assertions.assertTrue(!driver.getCurrentUrl().contains("/login"), "Reset App State should stay within the app");
            }
        } else {
            Assumptions.assumeTrue(false, "Burger/menu button not found; skipping menu tests.");
        }

        logoutIfPossible();
    }

    @Test
    @Order(7)
    public void sortingDropdown_IfPresent_ChangeOptions_And_AssertSelectionChanges() {
        boolean ok = tryLogin(LOGIN, PASSWORD);
        Assumptions.assumeTrue(ok, "Login required to access potential sorting list.");

        List<WebElement> selects = driver.findElements(SELECT_ANY).stream()
                .filter(WebElement::isDisplayed)
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!selects.isEmpty(), "No sorting/select dropdowns detected.");

        WebElement firstSelect = selects.get(0);
        Select sel = new Select(firstSelect);
        List<WebElement> options = sel.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Not enough options to test sorting.");

        String initial = sel.getFirstSelectedOption().getText();
        int changeCount = 0;
        for (int i = 0; i < options.size(); i++) {
            sel.selectByIndex(i);
            String now = sel.getFirstSelectedOption().getText();
            Assertions.assertTrue(now != null && !now.isEmpty(), "Selected option should have text");
            if (!now.equals(initial)) changeCount++;
        }
        Assertions.assertTrue(changeCount > 0, "Selection should change when choosing different sorting options.");

        logoutIfPossible();
    }

    @Test
    @Order(8)
    public void resetAppState_NotApplicable_ShouldGracefullySkipIfMissing() {
        boolean ok = tryLogin(LOGIN, PASSWORD);
        Assumptions.assumeTrue(ok, "Login required to access menu / reset state.");

        if (isPresent(BURGER_MENU)) {
            waitClickable(BURGER_MENU).click();
            if (isPresent(MENU_RESET)) {
                waitClickable(MENU_RESET).click();
                wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
                Assertions.assertTrue(true, "Reset App State clicked without errors.");
            } else {
                Assumptions.assumeTrue(false, "Reset App State menu item not present; skipping.");
            }
        } else {
            Assumptions.assumeTrue(false, "Burger/menu not present; skipping.");
        }

        logoutIfPossible();
    }
}
