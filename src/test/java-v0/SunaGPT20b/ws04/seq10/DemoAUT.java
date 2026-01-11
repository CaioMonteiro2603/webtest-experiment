package SunaGPT20b.ws04.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {
    private static WebDriver driver;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static WebDriverWait wait;

@BeforeAll
public static void setUpAll() {
    FirefoxOptions options = new FirefoxOptions();
    options.addArguments("--headless");
    driver = new FirefoxDriver(options);
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
}

@AfterAll
public static void tearDownAll() {
    if (driver != null) {
        driver.quit();
    }
}

private void openBasePage() {
    driver.get(BASE_URL);
    // Wait for the form to be present
    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("infoForm")));
}

private void fillForm(String firstName, String lastName, String email,
                      String password, String gender, String country, boolean agreeTerms) {
    // Text fields
    WebElement firstNameEl = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
    firstNameEl.clear();
    firstNameEl.sendKeys(firstName);

    WebElement lastNameEl = driver.findElement(By.id("last-name"));
    lastNameEl.clear();
    lastNameEl.sendKeys(lastName);

    WebElement emailEl = driver.findElement(By.id("email"));
    emailEl.clear();
    emailEl.sendKeys(email);

    WebElement passwordEl = driver.findElement(By.id("password"));
    passwordEl.clear();
    passwordEl.sendKeys(password);

    // Gender radio
    if (gender != null) {
        List<WebElement> genderOptions = driver.findElements(By.name("gender"));
        for (WebElement opt : genderOptions) {
            if (opt.getAttribute("value").equalsIgnoreCase(gender)) {
                if (!opt.isSelected()) {
                    opt.click();
                }
                break;
            }
        }
    }

    // Country dropdown
    if (country != null) {
        Select countrySelect = new Select(driver.findElement(By.id("country")));
        countrySelect.selectByVisibleText(country);
    }

    // Terms checkbox
    WebElement termsChk = driver.findElement(By.id("terms"));
    if (agreeTerms != termsChk.isSelected()) {
        termsChk.click();
    }
}

private void submitForm() {
    WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[normalize-space()='Submit' or @type='submit']")));
    submitBtn.click();
}

@Test
@Order(1)
public void testPageLoads() {
    openBasePage();
    Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
            "The browser should be on the base URL after navigation.");
    Assertions.assertTrue(driver.findElement(By.id("infoForm")).isDisplayed(),
            "The form should be visible on the page.");
}

@Test
@Order(2)
public void testEmptyFormValidation() {
    openBasePage();
    submitForm();

    // Expect at least one validation message to appear
    List<WebElement> errors = driver.findElements(By.cssSelector(".error, .validation-message, .invalid"));
    Assertions.assertFalse(errors.isEmpty(),
            "Validation messages should be displayed when submitting an empty form.");
}

@Test
@Order(3)
public void testSuccessfulSubmission() {
    openBasePage();
    fillForm("Alice", "Smith", "alice.smith@example.com",
            "SecurePass123", "female", "United States", true);
    submitForm();

    // Wait for a success indicator (generic text containing 'success' or 'thank')
    By successLocator = By.xpath("//*[contains(translate(text(),'SUCCESS','success'),'success') " +
            "or contains(translate(text(),'THANK','thank'),'thank')]");
    WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(successLocator));
    Assertions.assertTrue(successMsg.isDisplayed(),
            "A success message should be displayed after a valid form submission.");
}

@Test
@Order(4)
public void testInvalidEmailValidation() {
    openBasePage();
    fillForm("Bob", "Jones", "invalid-email",
            "AnotherPass123", "male", "Canada", true);
    submitForm();

    // Look for an email‑specific validation message
    List<WebElement> emailErrors = driver.findElements(By.xpath(
            "//*[contains(translate(text(),'email','EMAIL'),'email') " +
            "and (contains(translate(text(),'invalid','INVALID'),'invalid') " +
            "or contains(translate(text(),'required','REQUIRED'),'required')]"));
    Assertions.assertFalse(emailErrors.isEmpty(),
            "An email validation error should be shown for an invalid email address.");
}
}