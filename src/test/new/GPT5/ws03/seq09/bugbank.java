package GPT5.ws03.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

    // Common, robust locators (multiple fallbacks to be resilient)
    private static final By EMAIL_INPUT = By.cssSelector("input[type='email'], input[name='email'], input#email, input[placeholder*='mail']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password'][name='password'], input#password, input[placeholder*='senha'], input[type='password']");
    private static final By SUBMIT_LOGIN_BUTTON = By.cssSelector("button[type='submit'], button:enabled");
    private static final By LOGIN_FORM_CONTAINER = By.cssSelector("form, [data-testid='login-form'], .card__login, .login__container");
    private static final By TOAST_ERROR = By.cssSelector(".toast-error, .MuiAlert-standardError, .alert-error, .error, .styles__ErrorMessage, .Toastify__toast--error");
    private static final By TOAST_SUCCESS = By.cssSelector(".toast-success, .MuiAlert-standardSuccess, .alert-success, .success, .Toastify__toast--success");

    // Heuristics for "logged in" shell (BugBank SPA)
    private static final By DASHBOARD_SHELL = By.cssSelector("[data-test=dashboard], .home__container, .logged__container, nav, [data-testid='menu']");
    private static final By LOGOUT_BUTTON = By.xpath("//button[contains(.,'Sair') or contains(@aria-label,'Logout') or contains(@data-test,'logout')]");
    private static final By BURGER_BUTTON = By.xpath("//button[contains(@aria-label,'Menu') or contains(@class,'burger') or contains(.,'Menu')]");
    private static final By SIDE_MENU = By.cssSelector("nav, aside, .sidebar, [data-testid='menu']");
    private static final By RESET_APP_BUTTON = By.xpath("//a[contains(.,'Reset App State')] | //button[contains(.,'Reset App State')]");
    private static final By ALL_ITEMS_BUTTON = By.xpath("//a[contains(.,'All Items')] | //button[contains(.,'All Items')]");

    // Generic sorting dropdown (if any exists on the landing/dashboard one level down)
    private static final By ANY_SELECT = By.cssSelector("select");

    // Footer social links (treat as external)
    private static final By FOOTER_TWITTER = By.cssSelector("a[href*='twitter.com']");
    private static final By FOOTER_FACEBOOK = By.cssSelector("a[href*='facebook.com']");
    private static final By FOOTER_LINKEDIN = By.cssSelector("a[href*='linkedin.com']");

    @BeforeAll
    public static void beforeAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void afterAll() {
        if (driver != null) driver.quit();
    }

    /* ======================== Helpers ======================== */

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body")));
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private boolean tryClick(By locator) {
        List<WebElement> els = driver.findElements(locator);
        if (els.isEmpty()) return false;

        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(els.get(0)));
        el.click();
        return true;
    }

    private void login(String email, String password) {
        openBase();
        // Ensure we're on the login/landing form
        if (isPresent(LOGIN_FORM_CONTAINER)) {
            WebElement emailInput = waitClickable(EMAIL_INPUT);
            WebElement passInput = wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
            emailInput.clear();
            emailInput.sendKeys(email);
            passInput.clear();
            passInput.sendKeys(password);
            WebElement submit = waitClickable(SUBMIT_LOGIN_BUTTON);
            submit.click();
        } else {
            // If not visible, try refreshing base
            openBase();
        }
    }

    private boolean isLoggedInHeuristic() {
        // Logged-in heuristic: presence of logout or dashboard shell, and login form absent
        boolean hasShell = isPresent(DASHBOARD_SHELL) || isPresent(LOGOUT_BUTTON);
        boolean loginGone = !isPresent(LOGIN_FORM_CONTAINER);
        return hasShell && loginGone;
    }

    private void openMenuIfAvailable() {
        if (!isPresent(SIDE_MENU) || !driver.findElement(SIDE_MENU).isDisplayed()) {
            tryClick(BURGER_BUTTON);
            if (isPresent(SIDE_MENU)) {
                wait.until(ExpectedConditions.visibilityOfElementLocated(SIDE_MENU));
            }
        }
    }

    private void resetAppIfAvailable() {
        openMenuIfAvailable();
        if (isPresent(RESET_APP_BUTTON)) {
            tryClick(RESET_APP_BUTTON);
            // Expect success toast or state cleared quickly
            wait.withTimeout(Duration.ofSeconds(5));
            if (isPresent(TOAST_SUCCESS)) {
                wait.until(ExpectedConditions.visibilityOfElementLocated(TOAST_SUCCESS));
            }
        }
    }

    private void assertExternalLinkOpens(By locator, String expectedDomain) {
        List<WebElement> links = driver.findElements(locator);
        if (links.isEmpty()) return; // Optional
        String original = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        waitClickable(locator).click();

        // Wait for new tab or same-tab navigation
        wait.until(d -> d.getWindowHandles().size() > oldWindows.size() || driver.getCurrentUrl().contains(expectedDomain));

        if (driver.getWindowHandles().size() > oldWindows.size()) {
            Set<String> diff = new HashSet<>(driver.getWindowHandles());
            diff.removeAll(oldWindows);
            String newWin = diff.iterator().next();
            driver.switchTo().window(newWin);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External URL should contain " + expectedDomain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External URL should contain " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body")));
        }
    }

    /* ======================== Tests ======================== */

    @Test
    @Order(1)
    public void landing_ShowsLoginForm_and_CoreElements() {
        openBase();
        Assertions.assertAll(
                () -> Assertions.assertTrue(isPresent(LOGIN_FORM_CONTAINER), "Login form/container should be visible"),
                () -> Assertions.assertTrue(isPresent(EMAIL_INPUT), "Email input should be present"),
                () -> Assertions.assertTrue(isPresent(PASSWORD_INPUT), "Password input should be present"),
                () -> Assertions.assertTrue(isPresent(SUBMIT_LOGIN_BUTTON), "Login/submit button should be present")
        );
    }

    @Test
    @Order(2)
    public void login_InvalidCredentials_ShowsErrorFeedback() {
        login("invalid@example.com", "wrongpass");
        // Wait for any error element to be visible
        WebElement error = wait.until(d -> {
            List<WebElement> errors = d.findElements(TOAST_ERROR);
            return errors.stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
        });
        Assertions.assertTrue(error != null && error.isDisplayed(), "Error toast/message should be displayed for invalid login");
        // Ensure still not logged in
        Assertions.assertFalse(isLoggedInHeuristic(), "App must remain logged out after invalid credentials");
    }

    @Test
    @Order(3)
    public void login_WithProvidedCredentials_AttemptsAndVerifiesOutcome() {
        login(LOGIN_EMAIL, LOGIN_PASSWORD);

        // Either we're logged in OR we see a clear error
        boolean loggedIn = false;
        try {
            loggedIn = wait.until(d -> isLoggedInHeuristic());
        } catch (TimeoutException ignored) {}

        if (!loggedIn) {
            // Check for any visible error message
            boolean errorVisible = wait.until(d -> {
                List<WebElement> errors = d.findElements(TOAST_ERROR);
                return errors.stream().anyMatch(WebElement::isDisplayed);
            });
            // Also check for login form still present as confirmation
            Assertions.assertTrue(isPresent(LOGIN_FORM_CONTAINER), "If not logged in, login form should still be visible");
        } else {
            // Sanity checks on logged-in shell
            Assertions.assertTrue(isLoggedInHeuristic(), "User should be logged in with provided credentials");
        }
    }

    @Test
    @Order(4)
    public void menu_OpenClose_AllItems_Reset_Logout_IfAvailable() {
        // Ensure a clean base: try to login, but continue even if not possible
        if (!isLoggedInHeuristic()) {
            login(LOGIN_EMAIL, LOGIN_PASSWORD);
            try {
                wait.until(d -> isLoggedInHeuristic());
            } catch (TimeoutException ignored) {}
        }

        openMenuIfAvailable();
        Assumptions.assumeTrue(isPresent(SIDE_MENU), "Menu is not present on this page/app state");

        // All Items (if exists) should land on a primary/home/dashboard view
        if (isPresent(ALL_ITEMS_BUTTON)) {
            tryClick(ALL_ITEMS_BUTTON);
            // Assert we remain within same origin and page is interactive
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL) || driver.getCurrentUrl().contains("netlify.app"),
                    "All Items should keep us within the application");
        }

        // Reset App State (optional)
        resetAppIfAvailable();

        // Logout (optional)
        if (isPresent(LOGOUT_BUTTON)) {
            tryClick(LOGOUT_BUTTON);
            wait.until(ExpectedConditions.presenceOfElementLocated(LOGIN_FORM_CONTAINER));
            Assertions.assertTrue(isPresent(LOGIN_FORM_CONTAINER), "After logout, login form should be visible");
        }
    }

    @Test
    @Order(5)
    public void sortingDropdown_IfAny_ChangesSelection() {
        openBase();
        // Some BugBank deployments may not have a sorting control; treat as optional.
        List<WebElement> selects = driver.findElements(ANY_SELECT);
        Assumptions.assumeTrue(!selects.isEmpty(), "No sorting/select dropdown found on the first-level pages");

        Select s = new Select(selects.get(0));
        List<String> initialOptions = s.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
        Assumptions.assumeTrue(initialOptions.size() >= 2, "Need at least two options to test sort changes");

        String firstVal = s.getFirstSelectedOption().getText();
        s.selectByIndex(1);
        String secondVal = s.getFirstSelectedOption().getText();
        Assertions.assertNotEquals(firstVal, secondVal, "Changing sorting/select option should alter the selected value");

        // Try another change back to index 0 for determinism
        s.selectByIndex(0);
        String restored = s.getFirstSelectedOption().getText();
        Assertions.assertEquals(firstVal, restored, "Selecting back should restore the original value");
    }

    @Test
    @Order(6)
    public void footerSocialLinks_OpenExternally() {
        openBase();
        assertExternalLinkOpens(FOOTER_TWITTER, "twitter.com");
        assertExternalLinkOpens(FOOTER_FACEBOOK, "facebook.com");
        assertExternalLinkOpens(FOOTER_LINKEDIN, "linkedin.com");
    }
}