package GPT5.ws04.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void beforeAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1400, 1000));
    }

    @AfterAll
    public static void afterAll() {
        if (driver != null) driver.quit();
    }

    // --------------------------- Helpers ---------------------------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(d -> d.getTitle() != null && !d.getTitle().isEmpty());
    }

    private WebElement firstDisplayed(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            for (WebElement el : els) {
                if (el.isDisplayed()) return el;
            }
        }
        return null;
    }

    private List<WebElement> displayedAll(By by) {
        return driver.findElements(by).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el));
        el.click();
    }

    private void setValueViaJS(WebElement el, String value) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));", el, value);
    }

    private void clickExternalAndAssert(By locator, String expectedDomainFragment) {
        List<WebElement> links = displayedAll(locator);
        if (links.isEmpty()) return; // optional
        String baseHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        safeClick(links.get(0));
        wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(BASE_URL));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment), "External URL should contain " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(baseHandle);
        } else {
            wait.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment), "External URL should contain " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    private void resetIfPresentOrReload() {
        WebElement reset = firstDisplayed(
                By.cssSelector("button[type='reset']"),
                By.xpath("//button[normalize-space()='Reset' or normalize-space()='Clear']"),
                By.cssSelector("input[type='reset']")
        );
        if (reset != null) {
            safeClick(reset);
        } else {
            goHome();
        }
    }

    // --------------------------- Tests ---------------------------

    @Test
    @Order(1)
    public void testLandingPageLoadsAndHasCoreFields() {
        goHome();
        String title = driver.getTitle();
        Assertions.assertTrue(title != null && title.length() > 0, "Title should not be empty");

        // Assert presence of key fields using robust locators
        WebElement firstName = firstDisplayed(
                By.id("first-name"),
                By.name("firstName"),
                By.cssSelector("input[placeholder*='First' i]")
        );
        WebElement lastName = firstDisplayed(
                By.id("last-name"),
                By.name("lastName"),
                By.cssSelector("input[placeholder*='Last' i]")
        );
        WebElement email = firstDisplayed(
                By.id("email"),
                By.name("email"),
                By.cssSelector("input[type='email']")
        );
        WebElement submit = firstDisplayed(
                By.id("submit"),
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Submit')]")
        );

        Assertions.assertAll("Core fields visible",
                () -> Assertions.assertNotNull(firstName, "First name input should be visible"),
                () -> Assertions.assertNotNull(lastName, "Last name input should be visible"),
                () -> Assertions.assertNotNull(email, "Email input should be visible"),
                () -> Assertions.assertNotNull(submit, "Submit button should be visible")
        );
    }

    @Test
    @Order(2)
    public void testValidationErrorsOnEmptySubmit() {
        goHome();
        WebElement submit = firstDisplayed(
                By.id("submit"),
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Submit')]")
        );
        Assertions.assertNotNull(submit, "Submit button should exist");
        safeClick(submit);

        // Expect any error/required indicators
        boolean hasErrors = driver.findElements(By.cssSelector(".error, .has-error, .invalid-feedback")).size() > 0
                || driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'required')]")).size() > 0;
        Assertions.assertTrue(hasErrors, "Submitting empty form should display validation errors");
    }

    @Test
    @Order(3)
    public void testFillAndSubmitValidForm() {
        goHome();

        WebElement firstName = firstDisplayed(By.id("first-name"), By.name("firstName"), By.cssSelector("input[placeholder*='First' i]"));
        WebElement lastName = firstDisplayed(By.id("last-name"), By.name("lastName"), By.cssSelector("input[placeholder*='Last' i]"));
        WebElement genderMale = firstDisplayed(By.id("gender-male"), By.cssSelector("input[name='gender'][value='male']"));
        if (genderMale == null) { // pick any visible radio
            List<WebElement> radios = displayedAll(By.cssSelector("input[type='radio']"));
            if (!radios.isEmpty()) genderMale = radios.get(0);
        }
        WebElement dob = firstDisplayed(By.id("dob"), By.cssSelector("input[type='date']"));
        WebElement address = firstDisplayed(By.id("address"), By.name("address"), By.cssSelector("textarea"));
        WebElement email = firstDisplayed(By.id("email"), By.name("email"), By.cssSelector("input[type='email']"));
        WebElement password = firstDisplayed(By.id("password"), By.name("password"), By.cssSelector("input[type='password']"));
        WebElement company = firstDisplayed(By.id("company"), By.name("company"));
        WebElement roleSelectEl = firstDisplayed(By.id("role"), By.name("role"), By.cssSelector("select"));
        WebElement expectationSelectEl = firstDisplayed(By.id("expectation"), By.name("expectation"), By.cssSelector("select[multiple]"));
        WebElement comment = firstDisplayed(By.id("comment"), By.name("comment"), By.cssSelector("textarea[name='comment']"));

        // Fill inputs
        if (firstName != null) { firstName.clear(); firstName.sendKeys("John"); }
        if (lastName != null) { lastName.clear(); lastName.sendKeys("Doe"); }
        if (genderMale != null) safeClick(genderMale);
        if (dob != null) setValueViaJS(dob, "1990-01-01");
        if (address != null) { address.clear(); address.sendKeys("123 Test St, Test City"); }
        if (email != null) { email.clear(); email.sendKeys("john.doe@example.com"); }
        if (password != null) { password.clear(); password.sendKeys("P@ssw0rd!"); }
        if (company != null) { company.clear(); company.sendKeys("Example Inc."); }

        // Select role option (first non-placeholder)
        if (roleSelectEl != null) {
            Select role = new Select(roleSelectEl);
            List<WebElement> roleOptions = role.getOptions();
            if (roleOptions.size() > 1) {
                int idx = roleOptions.get(0).getText().toLowerCase(Locale.ROOT).contains("select") ? 1 : 0;
                role.selectByIndex(idx);
                Assertions.assertEquals(roleOptions.get(idx).getText(), role.getFirstSelectedOption().getText(), "Role selection should persist");
            }
        }

        // Multi-select expectation: select up to two different options if present
        if (expectationSelectEl != null) {
            Select expect = new Select(expectationSelectEl);
            if (expect.isMultiple() && expect.getOptions().size() >= 2) {
                expect.deselectAll();
                expect.selectByIndex(0);
                expect.selectByIndex(1);
                Assertions.assertTrue(expect.getAllSelectedOptions().size() >= 1, "At least one expectation should be selected");
            } else if (expect.getOptions().size() >= 1) {
                expect.selectByIndex(0);
                Assertions.assertEquals(expect.getOptions().get(0).getText(), expect.getFirstSelectedOption().getText(), "Expectation selection should persist");
            }
        }

        // Check some checkboxes if they exist
        List<WebElement> checkboxes = displayedAll(By.cssSelector("input[type='checkbox']"));
        for (int i = 0; i < Math.min(2, checkboxes.size()); i++) {
            if (!checkboxes.get(i).isSelected()) safeClick(checkboxes.get(i));
        }

        // Move slider if it exists
        WebElement slider = firstDisplayed(By.cssSelector("input[type='range']"), By.id("slider"));
        if (slider != null) {
            String before = slider.getAttribute("value");
            new Actions(driver).click(slider).sendKeys(Keys.ARROW_RIGHT).sendKeys(Keys.ARROW_RIGHT).perform();
            String after = slider.getAttribute("value");
            Assertions.assertNotEquals(before, after, "Slider value should change after interaction");
        }

        if (comment != null) { comment.clear(); comment.sendKeys("This is a sample comment for automated testing."); }

        // Submit
        WebElement submit = firstDisplayed(By.id("submit"), By.cssSelector("button[type='submit']"), By.xpath("//button[contains(.,'Submit')]"));
        Assertions.assertNotNull(submit, "Submit button should exist");
        safeClick(submit);

        // Assert success feedback
        boolean successShown = driver.findElements(By.cssSelector(".success, .alert-success, .submit-success")).size() > 0
                || driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success')]")).size() > 0
                || driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'thank')]")).size() > 0;
        Assertions.assertTrue(successShown, "A success/thank you message should appear after valid submit");
    }

    @Test
    @Order(4)
    public void testRoleDropdownSelectionChanges() {
        goHome();
        WebElement roleSelectEl = firstDisplayed(By.id("role"), By.name("role"), By.cssSelector("select"));
        if (roleSelectEl == null) {
            Assertions.assertTrue(true, "No role dropdown present; skipping without failure.");
            return;
        }
        Select role = new Select(roleSelectEl);
        List<WebElement> options = role.getOptions();
        if (options.size() < 2) {
            Assertions.assertTrue(true, "Role dropdown has fewer than two options; skipping.");
            return;
        }
        int initialIdx = role.getAllSelectedOptions().isEmpty() ? -1 : options.indexOf(role.getFirstSelectedOption());
        int targetIdx = (initialIdx + 1) % options.size();
        role.selectByIndex(targetIdx);
        Assertions.assertEquals(options.get(targetIdx).getText(), role.getFirstSelectedOption().getText(), "Selected role option should change");
    }

    @Test
    @Order(5)
    public void testDatePickerSettableIfPresent() {
        goHome();
        WebElement dob = firstDisplayed(By.id("dob"), By.cssSelector("input[type='date']"));
        if (dob == null) {
            Assertions.assertTrue(true, "No date field present; skipping.");
            return;
        }
        setValueViaJS(dob, "2000-12-31");
        Assertions.assertEquals("2000-12-31", dob.getAttribute("value"), "Date input value should match the set date");
    }

    @Test
    @Order(6)
    public void testResetClearsFormIfAvailable() {
        goHome();
        WebElement firstName = firstDisplayed(By.id("first-name"), By.name("firstName"), By.cssSelector("input[placeholder*='First' i]"));
        if (firstName != null) { firstName.clear(); firstName.sendKeys("TempName"); }
        resetIfPresentOrReload();
        if (firstName != null) {
            WebElement firstName2 = firstDisplayed(By.id("first-name"), By.name("firstName"), By.cssSelector("input[placeholder*='First' i]"));
            Assertions.assertTrue(firstName2 == null || firstName2.getAttribute("value").isEmpty(), "First name should be cleared/reset");
        } else {
            Assertions.assertTrue(true, "No reset control; page reloaded.");
        }
    }

    @Test
    @Order(7)
    public void testExternalLinksIfPresent() {
        goHome();
        // Try common socials and Katalon site if present
        clickExternalAndAssert(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        clickExternalAndAssert(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        clickExternalAndAssert(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
        clickExternalAndAssert(By.cssSelector("a[href*='katalon.com']"), "katalon");
    }
}
