package GPT5.ws04.seq09;

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
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    // Robust locators (with fallbacks)
    private static final By FORM_CONTAINER = By.cssSelector("form, .container, body");
    private static final By FIRST_NAME = By.cssSelector("input#first-name, input[name='firstName'], input[id*='first'][type='text']");
    private static final By LAST_NAME = By.cssSelector("input#last-name, input[name='lastName'], input[id*='last'][type='text']");
    private static final By GENDER_MALE = By.cssSelector("input[type='radio'][value='male'], input[type='radio'][id*='male'], input[type='radio'][name*='gender']");
    private static final By DOB = By.cssSelector("input#dob, input[type='date'], input[name*='dob']");
    private static final By ADDRESS = By.cssSelector("textarea#address, textarea[name='address'], textarea[id*='address']");
    private static final By EMAIL = By.cssSelector("input#email, input[type='email'], input[name='email']");
    private static final By PASSWORD = By.cssSelector("input#password, input[type='password'][name='password'], input[id*='password']");
    private static final By COMPANY = By.cssSelector("input#company, input[name='company']");
    private static final By ROLE_SELECT = By.cssSelector("select#role, select[name='role'], select[id*='role'], select");
    private static final By EXPECTATIONS_MULTI = By.cssSelector("select#expectation, select[name='expectation'], select[multiple]");
    private static final By DEV_CHECKBOX = By.cssSelector("input[type='checkbox'][id*='development'], input[type='checkbox'][name*='development'], input[type='checkbox']");
    private static final By SLIDER = By.cssSelector("input[type='range'], input[id*='slider']");
    private static final By SUBMIT_BUTTON = By.cssSelector("button#submit, button[type='submit'], button.btn, input[type='submit']");

    private static final By SUCCESS_MESSAGE = By.cssSelector("#submit-msg, .success, .alert-success, [data-test='submit-success']");
    private static final By ERROR_MESSAGE = By.cssSelector(".error, .help-block, .alert-danger, [data-test='error']");

    // "Menu / burger" and other e-commerce-ish items are not part of this site;
    // tests will guard with presence checks and behave accordingly.
    private static final By BURGER_BUTTON = By.id("react-burger-menu-btn");
    private static final By SIDE_MENU = By.cssSelector(".bm-menu-wrap");
    private static final By MENU_ALL_ITEMS = By.id("inventory_sidebar_link");
    private static final By MENU_ABOUT = By.id("about_sidebar_link");
    private static final By MENU_LOGOUT = By.id("logout_sidebar_link");
    private static final By MENU_RESET = By.id("reset_sidebar_link");

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

    /* ================= Helpers ================= */

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(FORM_CONTAINER));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL.substring(0, BASE_URL.indexOf("/", 8))), "Should be on the same origin as BASE_URL");
    }

    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private void safeClick(By locator) {
        if (isPresent(locator)) {
            waitClickable(locator).click();
        }
    }

    private Select firstSelect(By locator) {
        List<WebElement> selects = driver.findElements(locator);
        Assumptions.assumeFalse(selects.isEmpty(), "No select found for " + locator);
        return new Select(selects.get(0));
    }

    private void assertExternalLink(WebElement link, String expectedDomainContains) {
        String original = driver.getWindowHandle();
        Set<String> old = driver.getWindowHandles();
        waitClickable(By.xpath(".")); // NOP, ensure DOM stable
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(link)); 
        el.click();
        // Wait for either new tab or same-tab nav
        wait.until(d -> d.getWindowHandles().size() > old.size() || driver.getCurrentUrl().contains(expectedDomainContains));
        if (driver.getWindowHandles().size() > old.size()) {
            Set<String> diff = new HashSet<>(driver.getWindowHandles());
            diff.removeAll(old);
            String newHandle = diff.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainContains));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainContains), "External URL should contain: " + expectedDomainContains);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomainContains));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainContains), "External URL should contain: " + expectedDomainContains);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(FORM_CONTAINER));
        }
    }

    private static String hostOf(String href) {
        try {
            return Optional.ofNullable(new URI(href).getHost()).orElse("");
        } catch (Exception e) {
            return "";
        }
    }

    /* ================= Tests ================= */

    @Test
    @Order(1)
    public void pageLoads_CoreElementsPresent() {
        openBase();
        Assertions.assertAll(
                () -> Assertions.assertTrue(isPresent(FIRST_NAME), "First name input should be present"),
                () -> Assertions.assertTrue(isPresent(LAST_NAME), "Last name input should be present"),
                () -> Assertions.assertTrue(isPresent(EMAIL), "Email input should be present"),
                () -> Assertions.assertTrue(isPresent(PASSWORD), "Password input should be present"),
                () -> Assertions.assertTrue(isPresent(ROLE_SELECT), "Role select should be present"),
                () -> Assertions.assertTrue(isPresent(SUBMIT_BUTTON), "Submit button should be present")
        );
    }

    @Test
    @Order(2)
    public void sortingDropdown_RoleSelect_ChangesSelection() {
        openBase();
        Select role = firstSelect(ROLE_SELECT);
        List<String> options = role.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
        Assumptions.assumeTrue(options.size() >= 2, "Need at least two role options to test");
        String initial = role.getFirstSelectedOption().getText();
        role.selectByIndex(1);
        String after = role.getFirstSelectedOption().getText();
        Assertions.assertNotEquals(initial, after, "Selecting a different role should change the selected option");
        role.selectByIndex(0);
        String restored = role.getFirstSelectedOption().getText();
        Assertions.assertEquals(role.getOptions().get(0).getText(), restored, "Re-selecting index 0 should restore first option");
    }

    @Test
    @Order(3)
    public void form_SubmitWithInvalidEmail_ShowsNativeValidationOrError() {
        openBase();
        waitVisible(FIRST_NAME).clear();
        driver.findElement(FIRST_NAME).sendKeys("Caio");
        waitVisible(LAST_NAME).clear();
        driver.findElement(LAST_NAME).sendKeys("Tester");
        if (isPresent(GENDER_MALE)) safeClick(GENDER_MALE);
        if (isPresent(DOB)) {
            WebElement dob = driver.findElement(DOB);
            try { dob.clear(); } catch (Exception ignored) {}
            dob.sendKeys("01011990"); // tolerant format for many date inputs
        }
        if (isPresent(ADDRESS)) {
            WebElement addr = driver.findElement(ADDRESS);
            addr.clear(); addr.sendKeys("Rua Exemplo, 123 - São Paulo");
        }
        waitVisible(EMAIL).clear();
        driver.findElement(EMAIL).sendKeys("invalid-email"); // invalid
        waitVisible(PASSWORD).clear();
        driver.findElement(PASSWORD).sendKeys("123456");
        if (isPresent(COMPANY)) {
            WebElement company = driver.findElement(COMPANY);
            company.clear(); company.sendKeys("Katalon Co.");
        }
        // Submit with invalid email
        waitClickable(SUBMIT_BUTTON).click();

        // Check native HTML5 validation or custom error presence
        String validationMessage = driver.findElement(EMAIL).getDomProperty("validationMessage");
        boolean hasHtml5Validation = validationMessage != null && validationMessage.trim().length() > 0;
        boolean hasCustomError = isPresent(ERROR_MESSAGE);
        Assertions.assertTrue(hasHtml5Validation || hasCustomError, "Invalid email should cause validation error");
    }

    @Test
    @Order(4)
    public void form_FillAllAndSubmit_ShowsSuccessMessage() {
        openBase();
        // Fill required fields with a valid email
        waitVisible(FIRST_NAME).clear();
        driver.findElement(FIRST_NAME).sendKeys("Caio");
        waitVisible(LAST_NAME).clear();
        driver.findElement(LAST_NAME).sendKeys("Silva");
        if (isPresent(GENDER_MALE)) safeClick(GENDER_MALE);
        if (isPresent(DOB)) {
            WebElement dob = driver.findElement(DOB);
            try { dob.clear(); } catch (Exception ignored) {}
            dob.sendKeys("02021992");
        }
        if (isPresent(ADDRESS)) {
            WebElement addr = driver.findElement(ADDRESS);
            addr.clear(); addr.sendKeys("Av. Paulista, 1000");
        }
        waitVisible(EMAIL).clear();
        driver.findElement(EMAIL).sendKeys("caio.tester@example.com");
        waitVisible(PASSWORD).clear();
        driver.findElement(PASSWORD).sendKeys("123456");
        if (isPresent(COMPANY)) {
            WebElement company = driver.findElement(COMPANY);
            company.clear(); company.sendKeys("QA Ltd");
        }
        if (isPresent(ROLE_SELECT)) {
            Select role = firstSelect(ROLE_SELECT);
            if (role.getOptions().size() > 1) role.selectByIndex(1);
        }
        if (isPresent(EXPECTATIONS_MULTI)) {
            Select ex = firstSelect(EXPECTATIONS_MULTI);
            // Try selecting up to 2 options if multiple
            List<WebElement> opts = ex.getOptions();
            if (opts.size() > 0) ex.selectByIndex(0);
            if (opts.size() > 1 && ex.isMultiple()) ex.selectByIndex(1);
        }
        if (isPresent(DEV_CHECKBOX)) {
            // tick first development checkbox
            WebElement dev = driver.findElements(DEV_CHECKBOX).get(0);
            if (!dev.isSelected()) dev.click();
        }
        if (isPresent(SLIDER)) {
            try {
                WebElement slider = driver.findElement(SLIDER);
                slider.sendKeys(Keys.ARROW_RIGHT);
                slider.sendKeys(Keys.ARROW_RIGHT);
            } catch (Exception ignored) {}
        }

        // Submit
        waitClickable(SUBMIT_BUTTON).click();

        // Assert success
        boolean successBlock = isPresent(SUCCESS_MESSAGE);
        boolean successText = driver.getPageSource().toLowerCase().contains("success");
        Assertions.assertTrue(successBlock || successText, "A success indication should appear after valid submission");
    }

    @Test
    @Order(5)
    public void externalLinks_OnBasePage_OpenAndAreConstrainedToTheirDomains() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href^='http']"));
        // Filter external only (exclude same host)
        List<WebElement> externals = anchors.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    String host = hostOf(href);
                    return href != null && !host.isEmpty() && !host.contains(baseHost);
                })
                .collect(Collectors.toList());

        // Visit up to 3 external links one level deep
        int visits = Math.min(3, externals.size());
        for (int i = 0; i < visits; i++) {
            WebElement link = externals.get(i);
            String href = link.getAttribute("href");
            String host = hostOf(href);
            if (host.isEmpty()) continue;
            assertExternalLink(link, host);
        }

        // If none exist, at least assert no crash and page is still present
        if (visits == 0) {
            Assertions.assertTrue(isPresent(FORM_CONTAINER), "No external links found; base form should remain visible");
        }
    }

    @Test
    @Order(6)
    public void burgerMenu_Reset_AllItems_Logout_AreNotApplicable_ButGuarded() {
        openBase();
        // Since this site doesn't have a burger/menu, validate absence (guard for independence)
        Assertions.assertTrue(driver.findElements(BURGER_BUTTON).isEmpty(), "Burger/menu button should not exist on this site");
        Assertions.assertTrue(driver.findElements(SIDE_MENU).isEmpty(), "Side menu should not exist on this site");
        Assertions.assertTrue(driver.findElements(MENU_ALL_ITEMS).isEmpty(), "All Items menu should not exist on this site");
        Assertions.assertTrue(driver.findElements(MENU_ABOUT).isEmpty(), "About menu should not exist on this site");
        Assertions.assertTrue(driver.findElements(MENU_LOGOUT).isEmpty(), "Logout menu should not exist on this site");
        Assertions.assertTrue(driver.findElements(MENU_RESET).isEmpty(), "Reset App State menu should not exist on this site");
    }
}
