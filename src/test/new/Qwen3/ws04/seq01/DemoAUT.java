package Qwen3.ws04.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testFormSubmission() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");

        // Fill out form
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement phoneField = driver.findElement(By.id("phone"));
        WebElement genderMale = driver.findElement(By.id("gender-male"));
        WebElement educationHighSchool = driver.findElement(By.id("education-highschool"));
        WebElement experience2Years = driver.findElement(By.id("experience-2"));
        WebElement jobTypeFullTime = driver.findElement(By.id("job-type-full-time"));
        WebElement commentArea = driver.findElement(By.id("comment"));

        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        emailField.sendKeys("john.doe@example.com");
        passwordField.sendKeys("password123");
        phoneField.sendKeys("1234567890");
        genderMale.click();
        educationHighSchool.click();
        experience2Years.click();
        jobTypeFullTime.click();
        commentArea.sendKeys("This is a test comment.");

        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        // Verify submission success
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed(), "Success message should be displayed after form submission");
    }

    @Test
    @Order(2)
    public void testFormValidation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");

        // Submit empty form
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        // Verify validation errors
        WebElement validationMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(validationMessage.isDisplayed(), "Validation error message should appear when submitting empty form");
    }

    @Test
    @Order(3)
    public void testFormClear() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");

        // Fill out form fields
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstNameField.sendKeys("Jane");
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        lastNameField.sendKeys("Smith");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("jane.smith@example.com");

        // Clear form
        WebElement clearButton = driver.findElement(By.id("clear"));
        clearButton.click();

        // Verify fields are cleared
        String firstNameValue = firstNameField.getAttribute("value");
        String lastNameValue = lastNameField.getAttribute("value");
        String emailValue = emailField.getAttribute("value");
        
        assertEquals("", firstNameValue, "First name field should be cleared");
        assertEquals("", lastNameValue, "Last name field should be cleared");
        assertEquals("", emailValue, "Email field should be cleared");
    }

    @Test
    @Order(4)
    public void testRequiredFields() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");

        // Try to submit without required fields
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        // Check for validation messages for required fields
        WebElement requiredFieldErrors = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='error']")));
        assertNotNull(requiredFieldErrors, "Should show validation errors for missing required fields");
    }

    @Test
    @Order(5)
    public void testSelectDropdowns() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");

        // Test country selection dropdown - using city field as it seems to be the actual dropdown in the form
        WebElement citySelect = driver.findElement(By.id("city"));
        Select cityDropdown = new Select(citySelect);
        cityDropdown.selectByVisibleText("New York");

        // Verify selection
        String selectedOption = cityDropdown.getFirstSelectedOption().getText();
        assertEquals("New York", selectedOption, "City should be selected correctly");
    }

    @Test
    @Order(6)
    public void testFormWithSpecialCharacters() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");

        // Fill form with special characters
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstNameField.sendKeys("John@#$%");
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        lastNameField.sendKeys("Doe!@#");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("john.doe+test@example.com");
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("(123) 456-7890");

        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        // Verify submission was processed
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed(), "Form with special characters should submit successfully");
    }
}