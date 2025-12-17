package Qwen3.ws04.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DemoAUT {
    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testFormPageLoad() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        String title = driver.getTitle();
        assertEquals("Katalon Form Test", title);
        assertTrue(driver.getCurrentUrl().contains("form.html"));
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Fill in form fields
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstNameField.sendKeys("John");
        
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        lastNameField.sendKeys("Doe");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("john.doe@example.com");
        
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("123-456-7890");
        
        // Select gender
        WebElement maleRadio = driver.findElement(By.id("male"));
        maleRadio.click();
        
        // Select hobbies
        WebElement readingCheckbox = driver.findElement(By.id("reading"));
        readingCheckbox.click();
        WebElement musicCheckbox = driver.findElement(By.id("music"));
        musicCheckbox.click();
        
        // Select country
        WebElement countrySelect = driver.findElement(By.id("country"));
        Select countryDropdown = new Select(countrySelect);
        countryDropdown.selectByVisibleText("United States");
        
        // Fill in message
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("This is a test message");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Verify success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("Form submitted successfully"));
    }

    @Test
    @Order(3)
    public void testInvalidFormSubmission() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Submit form without filling any fields
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Verify validation messages
        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorElement.isDisplayed());
    }

    @Test
    @Order(4)
    public void testFormFieldsValidation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test required fields
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        List<WebElement> errorFields = driver.findElements(By.cssSelector(".error-field"));
        assertTrue(errorFields.size() > 0);
        
        // Fill required fields
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstNameField.sendKeys("Jane");
        
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        lastNameField.sendKeys("Smith");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("jane.smith@example.com");
        
        // Fill other fields
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("098-765-4321");
        
        WebElement femaleRadio = driver.findElement(By.id("female"));
        femaleRadio.click();
        
        WebElement movieCheckbox = driver.findElement(By.id("movie"));
        movieCheckbox.click();
        
        WebElement countrySelect = driver.findElement(By.id("country"));
        Select countryDropdown = new Select(countrySelect);
        countryDropdown.selectByVisibleText("Canada");
        
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("Test message");
        
        // Submit form
        submitButton.click();
        
        // Verify success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed());
    }

    @Test
    @Order(5)
    public void testFormReset() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Fill form
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstNameField.sendKeys("Test");
        
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        lastNameField.sendKeys("User");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("test.user@example.com");
        
        // Find reset button
        WebElement resetButton = driver.findElement(By.cssSelector("button[type='reset']"));
        resetButton.click();
        
        // Verify form fields are reset
        String firstNameValue = firstNameField.getAttribute("value");
        String lastNameValue = lastNameField.getAttribute("value");
        String emailValue = emailField.getAttribute("value");
        
        assertEquals("", firstNameValue);
        assertEquals("", lastNameValue);
        assertEquals("", emailValue);
    }

    @Test
    @Order(6)
    public void testFormNavigation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Verify navigation elements exist
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() >= 2);
        
        // Verify page has form elements
        WebElement form = driver.findElement(By.tagName("form"));
        assertTrue(form.isDisplayed());
        
        // Verify form fields exist
        List<WebElement> formFields = driver.findElements(By.cssSelector("input, select, textarea"));
        assertTrue(formFields.size() > 0);
        
        // Verify submit button exists
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(submitButton.isDisplayed());
    }
}