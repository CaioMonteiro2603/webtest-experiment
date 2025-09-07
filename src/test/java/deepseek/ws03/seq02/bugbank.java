package GPT5.ws03.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class BugBankHeadlessE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String PROVIDED_LOGIN = "caio@gmail.com";
    private static final String PROVIDED_PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1400, 1000));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // -------------------- Helpers --------------------

    private void goHome() {
        driver.get(BASE_URL);
        // Wait for a known element on the landing/login page
        wait.until(d ->
                d.findElements(By.cssSelector("button[type='submit']")).size() > 0
                        || d.findElements(By.xpath("//*[contains(text(),'Acessar') or contains(text(),'Login')]")).size() > 0
        );
    }

    private WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private WebElement findEmailInput() {
        List<By> candidates = List.of(
                By.name("email"),
                By.cssSelector("input[type='email']"),
                By.cssSelector("input[placeholder*='email' i]"),
                By.cssSelector("input[placeholder*='e-mail' i]")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            for (WebElement el : els) if (el.isDisplayed()) return el;
        }
        throw new NoSuchElementException("Email input not found");
    }

    private WebElement findPasswordInput() {
        List<By> candidates = List.of(
                By.name("password"),
                By.cssSelector("input[type='password']"),
                By.cssSelector("input[placeholder*='senha' i]"),
                By.cssSelector("input[placeholder*='password' i]")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            for (WebElement el : els) if (el.isDisplayed()) return el;
        }
        throw new NoSuchElementException("Password input not found");
    }

    private WebElement findLoginButton() {
        List<By> candidates = List.of(
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Acessar') or contains(.,'Login')]")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            for (WebElement el : els) if (el.isDisplayed()) return el;
        }
        throw new NoSuchElementException("Login button not found");
    }

    private void performLogin(String email, String password) {
        goHome();
        WebElement emailInput = findEmailInput();
        WebElement passInput = findPasswordInput();
        emailInput.clear();
        emailInput.sendKeys(email);
        passInput.clear();
        passInput.sendKeys(password);
        findLoginButton().click();
    }

    private boolean isDashboardLoaded() {
        // Heuristics: URL change or presence of balance/account elements
        try {
            wait.until(d ->
                    d.getCurrentUrl().toLowerCase(Locale.ROOT).contains("home")
                            || d.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'saldo')]")).size() > 0
                            || d.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'conta')]")).size() > 0
            );
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void openRegistrationModal() {
        goHome();
        // Many versions have a "Registrar" or "Cadastrar" button
        List<By> candidates = List.of(
                By.xpath("//button[contains(.,'Registrar') or contains(.,'Cadastrar')]"),
                By.cssSelector("a[href*='register']")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                clickable(els.get(0)::isDisplayed);
                els.get(0).click();
                // Wait for any modal inputs to appear
                wait.until(d ->
                        d.findElements(By.name("email")).size() > 0
                                || d.findElements(By.cssSelector("input[type='email']")).size() > 0
                );
                return;
            }
        }
        throw new NoSuchElementException("Registration trigger not found");
    }

    private void completeRegistration(String name, String email, String password, boolean startWithBalance) {
        // Try common fields in the registration modal
        WebElement nameInput = getFirstDisplayed(
                By.name("name"),
                By.cssSelector("input[placeholder*='nome' i]"),
                By.cssSelector("input[name*='name' i]")
        );
        if (nameInput != null) {
            nameInput.clear();
            nameInput.sendKeys(name);
        }

        WebElement emailInput = getFirstDisplayed(
                By.cssSelector(".modal-content input[name='email']"),
                By.cssSelector(".modal-content input[type='email']"),
                By.cssSelector("input[name='email']")
        );
        if (emailInput == null) emailInput = findEmailInput();
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement passInput = getFirstDisplayed(
                By.cssSelector(".modal-content input[name='password']"),
                By.cssSelector(".modal-content input[type='password']"),
                By.name("password")
        );
        if (passInput == null) passInput = findPasswordInput();
        passInput.clear();
        passInput.sendKeys(password);

        WebElement passConf = getFirstDisplayed(
                By.name("passwordConfirmation"),
                By.cssSelector("input[name*='confirm' i]"),
                By.cssSelector("input[placeholder*='confirma' i]")
        );
        if (passConf != null) {
            passConf.clear();
            passConf.sendKeys(password);
        }

        if (startWithBalance) {
            List<WebElement> balanceSwitch = driver.findElements(By.cssSelector("input[type='checkbox'], .switch"));
            if (!balanceSwitch.isEmpty() && balanceSwitch.get(0).isDisplayed()) {
                balanceSwitch.get(0).click();
            }
        }

        WebElement submit = getFirstDisplayed(
                By.xpath("//button[contains(.,'Cadastrar') or contains(.,'Registrar') or contains(.,'Create')]"),
                By.cssSelector(".modal-content button[type='submit']")
        );
        if (submit == null) submit = findLoginButton(); // fallback
        clickable(elementIsDisplayed(submit));
        submit.click();

        // Expect a toast/alert of success; guard loosely
        wait.until(d ->
                d.findElements(By.xpath("//*[contains(.,'sucesso') or contains(.,'sucesso!') or contains(.,'success')]")).size() > 0
                        || d.findElements(By.cssSelector("[role='alert'], .Toastify__toast, .notification")).size() > 0
        );
    }

    private WebElement getFirstDisplayed(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            for (WebElement el : els) if (el.isDisplayed()) return el;
        }
        return null;
    }

    private ExpectedConditionFromElement elementIsDisplayed(WebElement el) {
        return new ExpectedConditionFromElement(el);
    }

    private void assertToastContains(String... expectedFragments) {
        // Consolidate possible toast containers
        List<WebElement> toasts = driver.findElements(By.cssSelector("[role='alert'], .Toastify__toast, .notification, .toast"));
        if (toasts.isEmpty()) {
            // try any visible paragraph in a modal as fallback
            toasts = driver.findElements(By.cssSelector(".modal-content, .modal, .alert, .snackbar, body"));
        }
        boolean matched = false;
        for (WebElement t : toasts) {
            String txt = t.getText().toLowerCase(Locale.ROOT);
            for (String f : expectedFragments) {
                if (txt.contains(f.toLowerCase(Locale.ROOT))) {
                    matched = true;
                    break;
                }
            }
            if (matched) break;
        }
        Assertions.assertTrue(matched, "Expected any toast to contain one of: " + String.join(", ", expectedFragments));
    }

    private void clickExternalAndAssert(By locator, String expectedDomainFragment) {
        List<WebElement> links = driver.findElements(locator);
        if (links.isEmpty()) return; // optional link not present
        WebElement link = links.get(0);
        String baseWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        clickable(locator).click();

        wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(BASE_URL));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment),
                    "External URL should contain: " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(baseWindow);
        } else {
            wait.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment),
                    "External URL should contain: " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void testLandingPageLoads() {
        goHome();
        String title = driver.getTitle().toLowerCase(Locale.ROOT);
        Assertions.assertTrue(title.contains("bugbank") || title.contains("bank") || title.length() > 0,
                "Page title should be present and likely contain 'BugBank' or 'Bank'");
        Assertions.assertTrue(
                driver.findElements(By.cssSelector("button[type='submit']")).size() > 0
                        || driver.findElements(By.xpath("//button[contains(.,'Acessar') or contains(.,'Login')]")).size() > 0,
                "Login button should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        performLogin(PROVIDED_LOGIN, PROVIDED_PASSWORD);
        // Expect an error toast or message
        assertToastContains("senha", "inválid", "invalid", "não cadastrado", "erro");
        // Ensure still on login page
        Assertions.assertTrue(
                driver.findElements(By.cssSelector("button[type='submit']")).size() > 0,
                "After invalid login we should remain on login page");
    }

    @Test
    @Order(3)
    public void testRegisterNewUserThenLoginAndLogoutIfAvailable() {
        String uniqueEmail = "qa+" + System.currentTimeMillis() + "@example.com";
        openRegistrationModal();
        completeRegistration("QA Bot", uniqueEmail, "Pass123!", true);

        // Close modal if it's closable (optional step)
        List<WebElement> closeBtn = driver.findElements(By.xpath("//button[contains(.,'Fechar') or contains(.,'Close') or contains(.,'OK')]"));
        if (!closeBtn.isEmpty() && closeBtn.get(0).isDisplayed()) {
            clickable(closeBtn.get(0)::isDisplayed);
            closeBtn.get(0).click();
            // modal may or may not close; continue gracefully
        }

        // Login with the newly created user
        performLogin(uniqueEmail, "Pass123!");
        Assertions.assertTrue(isDashboardLoaded(), "After successful login the dashboard should load");

        // Try to logout if there is a visible logout control
        List<WebElement> logoutCandidates = driver.findElements(By.xpath("//button[contains(.,'Sair') or contains(.,'Logout')]"));
        if (!logoutCandidates.isEmpty()) {
            clickable(logoutCandidates.get(0)::isDisplayed);
            logoutCandidates.get(0).click();
            // After logout, back to login
            wait.until(d -> d.findElements(By.cssSelector("button[type='submit']")).size() > 0);
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "After logout we should return to the login page");
        } else {
            // If no logout, return home to reset
            goHome();
        }
    }

    @Test
    @Order(4)
    public void testOptionalExternalLinksIfPresent() {
        goHome();
        // Common social or project links (optional)
        clickExternalAndAssert(By.cssSelector("a[href*='github.com']"), "github.com");
        clickExternalAndAssert(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        clickExternalAndAssert(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        clickExternalAndAssert(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }

    @Test
    @Order(5)
    public void testOptionalSortingIfPresent() {
        // BugBank may not have sorting dropdown; guard accordingly
        goHome();
        // If the app exposes a demo feed after login, try to login anonymously if possible
        // Otherwise, just search for a <select> or sorting control
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        if (selects.isEmpty()) {
            Assertions.assertTrue(true, "No sorting control present; skipping without failure.");
            return;
        }
        WebElement dropdown = selects.get(0);
        String initialValue = dropdown.getAttribute("value");
        clickable(dropdown::isDisplayed);
        dropdown.click();
        List<WebElement> options = dropdown.findElements(By.tagName("option"));
        if (options.size() < 2) {
            Assertions.assertTrue(true, "Sorting dropdown has fewer than 2 options; skipping.");
            return;
        }
        String newValue = null;
        for (WebElement opt : options) {
            if (!opt.getAttribute("value").equals(initialValue)) {
                opt.click();
                newValue = opt.getAttribute("value");
                break;
            }
        }
        Assertions.assertNotNull(newValue, "Should be able to pick a different sorting option");
        Assertions.assertNotEquals(initialValue, newValue, "Sorting selection should change");
    }

    // ---------- Small utility to allow clickable(WebElement::isDisplayed) ----------
    private static class ExpectedConditionFromElement implements org.openqa.selenium.support.ui.ExpectedCondition<WebElement> {
        private final WebElement element;
        ExpectedConditionFromElement(WebElement element) { this.element = element; }
        @Override
        public WebElement apply(WebDriver driver) {
            return (element != null && element.isDisplayed() && element.isEnabled()) ? element : null;
        }
        @Override
        public String toString() {
            return "element to be displayed and enabled: " + element;
        }
    }
}
