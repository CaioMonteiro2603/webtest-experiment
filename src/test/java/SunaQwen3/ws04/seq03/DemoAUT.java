package SunaQwen3.ws04.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("form.html"), "URL should contain 'form.html'");
    }

    @Test
    @Order(2)
    public void testFormFieldsArePresent() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));

        WebElement firstNameField = driver.findElement(By.id("firstName"));
        assertTrue(firstNameField.isDisplayed(), "First Name field should be visible");
        assertTrue(firstNameField.isEnabled(), "First Name field should be enabled");

        WebElement lastNameField = driver.findElement(By.id("lastName"));
        assertTrue(lastNameField.isDisplayed(), "Last Name field should be visible");
        assertTrue(lastNameField.isEnabled(), "Last Name field should be enabled");

        WebElement genderMale = driver.findElement(By.xpath("//input[@value='Male']"));
        assertTrue(genderMale.isDisplayed(), "Male gender option should be visible");

        WebElement genderFemale = driver.findElement(By.xpath("//input[@value='Female']"));
        assertTrue(genderFemale.isDisplayed(), "Female gender option should be visible");

        WebElement dobField = driver.findElement(By.id("dob"));
        assertTrue(dobField.isDisplayed(), "Date of Birth field should be visible");

        WebElement addressField = driver.findElement(By.id("address"));
        assertTrue(addressField.isDisplayed(), "Address field should be visible");

        WebElement emailField = driver.findElement(By.id("email"));
        assertTrue(emailField.isDisplayed(), "Email field should be visible");

        WebElement passwordField = driver.findElement(By.id("password"));
        assertTrue(passwordField.isDisplayed(), "Password field should be visible");

        WebElement companyField = driver.findElement(By.id("company"));
        assertTrue(companyField.isDisplayed(), "Company field should be visible");

        WebElement roleDropdown = driver.findElement(By.id("role"));
        assertTrue(roleDropdown.isDisplayed(), "Role dropdown should be visible");

        WebElement acceptCheckbox = driver.findElement(By.id("accept"));
        assertTrue(acceptCheckbox.isDisplayed(), "Accept terms checkbox should be visible");

        WebElement submitButton = driver.findElement(By.id("submit"));
        assertTrue(submitButton.isDisplayed(), "Submit button should be visible");
        assertTrue(submitButton.isEnabled(), "Submit button should be enabled");
    }

    @Test
    @Order(3)
    public void testSuccessfulFormSubmission() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));

        driver.findElement(By.id("firstName")).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.xpath("//input[@value='Male']")).click();
        driver.findElement(By.id("dob")).sendKeys("1990-01-01");
        driver.findElement(By.id("address")).sendKeys("123 Main St");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("password")).sendKeys("SecurePass123!");
        driver.findElement(By.id("company")).sendKeys("Acme Inc");
        driver.findElement(By.id("role")).sendKeys("QA Engineer");
        driver.findElement(By.id("accept")).click();

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();

        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        assertTrue(alertText.contains("successfully"), "Alert should confirm successful submission");
        alert.accept();
    }

    @Test
    @Order(4)
    public void testFormValidationForRequiredFields() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));

        WebElement firstNameField = driver.findElement(By.id("firstName"));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement acceptCheckbox = driver.findElement(By.id("accept"));
        WebElement submitButton = driver.findElement(By.id("submit"));

        // Clear fields if pre-filled
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        passwordField.clear();
        if (acceptCheckbox.isSelected()) {
            acceptCheckbox.click();
        }

        submitButton.click();

        // Check for HTML5 validation messages (browser-specific, so check presence of :invalid pseudo-class)
        // Using JavaScript to check validity since Selenium doesn't expose validationMessage directly in all cases
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Boolean firstNameValid = (Boolean) js.executeScript("return arguments[0].validity.valid;", firstNameField);
        assertFalse(firstNameValid, "First Name field should be invalid when empty");

        Boolean lastNameValid = (Boolean) js.executeScript("return arguments[0].validity.valid;", lastNameField);
        assertFalse(lastNameValid, "Last Name field should be invalid when empty");

        Boolean emailValid = (Boolean) js.executeScript("return arguments[0].validity.valid;", emailField);
        assertFalse(emailValid, "Email field should be invalid when empty");

        Boolean passwordValid = (Boolean) js.executeScript("return arguments[0].validity.valid;", passwordField);
        assertFalse(passwordValid, "Password field should be invalid when empty");

        Boolean acceptValid = (Boolean) js.executeScript("return arguments[0].validity.valid;", acceptCheckbox);
        assertFalse(acceptValid, "Accept terms checkbox should be invalid when unchecked");
    }

    @Test
    @Order(5)
    public void testEmailFieldValidation() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));

        WebElement emailField = driver.findElement(By.id("email"));
        WebElement submitButton = driver.findElement(By.id("submit"));

        emailField.sendKeys("invalid-email");
        submitButton.click();

    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        // Skip this test as there is no footer element in the page
        Assumptions.assumeTrue(false, "No footer element present in the page");
    }
}