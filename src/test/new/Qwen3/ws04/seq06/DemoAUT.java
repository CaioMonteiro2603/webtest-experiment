package Qwen3.ws04.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
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
    public void testFormPageLoadsCorrectly() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        String currentPageTitle = driver.getTitle();
        assertEquals("Demo AUT", currentPageTitle, "Page title should be 'Demo AUT'");
        
        WebElement formElement = driver.findElement(By.tagName("form"));
        assertTrue(formElement.isDisplayed(), "Form element should be displayed");
        
        WebElement heading = driver.findElement(By.tagName("h1"));
        assertEquals("Demo AUT", heading.getText(), "Heading should be 'Demo AUT'");
    }

    @Test
    @Order(2)
    public void testRequiredFieldsValidation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        // Check for validation error messages
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message"));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed when submitting empty form");
    }

    @Test
    @Order(3)
    public void testValidFormSubmission() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Fill in required fields
        WebElement nameField = driver.findElement(By.id("firstName"));
        nameField.sendKeys("John Doe");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("john.doe@example.com");
        
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("123-456-7890");
        
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("This is a test message");
        
        // Select a radio button
        WebElement radioOption = driver.findElement(By.id("sex"));
        radioOption.click();
        
        // Select checkbox
        WebElement checkbox = driver.findElement(By.id("exp-0"));
        checkbox.click();
        
        // Select from dropdown
        WebElement dropdown = driver.findElement(By.id("continents"));
        dropdown.sendKeys("Asia");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        // Verify submission success
        WebElement successMessage = driver.findElement(By.cssSelector(".success-message"));
        assertTrue(successMessage.isDisplayed(), "Success message should be displayed after form submission");
        
        String successText = successMessage.getText();
        assertTrue(successText.contains("Thank you"), "Success message should contain 'Thank you'");
    }

    @Test
    @Order(4)
    public void testFormWithInvalidEmail() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Fill in fields with invalid email
        WebElement nameField = driver.findElement(By.id("firstName"));
        nameField.sendKeys("Jane Smith");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("invalid-email");
        
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("098-765-4321");
        
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("Another test message");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        // Check for error message related to email
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message"));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid email");
        assertTrue(errorMessage.getText().toLowerCase().contains("email"), "Error message should relate to email field");
    }

    @Test
    @Order(5)
    public void testFormFieldInteractions() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test name field
        WebElement nameField = driver.findElement(By.id("firstName"));
        nameField.clear();
        nameField.sendKeys("Test User");
        assertEquals("Test User", nameField.getAttribute("value"), "Name field value should match input");
        
        // Test email field
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.clear();
        emailField.sendKeys("test@example.com");
        assertEquals("test@example.com", emailField.getAttribute("value"), "Email field value should match input");
        
        // Test phone field
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.clear();
        phoneField.sendKeys("555-123-4567");
        assertEquals("555-123-4567", phoneField.getAttribute("value"), "Phone field value should match input");
        
        // Test textarea field
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.clear();
        messageField.sendKeys("Sample test message");
        assertEquals("Sample test message", messageField.getAttribute("value"), "Message field text should match input");
        
        // Test checkbox
        WebElement checkbox = driver.findElement(By.id("exp-0"));
        assertFalse(checkbox.isSelected(), "Checkbox should initially be unchecked");
        checkbox.click();
        assertTrue(checkbox.isSelected(), "Checkbox should be selected after click");
        
        // Test radio buttons
        WebElement radioOption1 = driver.findElement(By.id("sex"));
        WebElement radioOption2 = driver.findElement(By.id("exp-1"));
        
        assertFalse(radioOption1.isSelected(), "Radio option 1 should be initially unselected");
        
        radioOption1.click();
        assertTrue(radioOption1.isSelected(), "Radio option 1 should be selected");
        
        // Test dropdown selection
        WebElement dropdown = driver.findElement(By.id("continents"));
        dropdown.sendKeys("Europe");
        assertEquals("europe", dropdown.getAttribute("value"), "Dropdown selection should match input");
    }
}