package Qwen3.ws04.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class KatalonFormTest {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
    public void testPageLoad() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        wait.until(ExpectedConditions.titleContains("Automation Form"));
        assertTrue(driver.getTitle().contains("Automation Form"));
        assertTrue(driver.getCurrentUrl().contains("form.html"));
    }

    @Test
    @Order(2)
    public void testInputFields() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test text input field
        WebElement textField = wait.until(ExpectedConditions.elementToBeClickable(By.id("textField")));
        textField.clear();
        textField.sendKeys("Test Text");
        assertEquals("Test Text", textField.getAttribute("value"));
        
        // Test email input field
        WebElement emailField = driver.findElement(By.id("emailField"));
        emailField.clear();
        emailField.sendKeys("test@example.com");
        assertEquals("test@example.com", emailField.getAttribute("value"));
        
        // Test password input field
        WebElement passwordField = driver.findElement(By.id("passwordField"));
        passwordField.clear();
        passwordField.sendKeys("password123");
        assertEquals("password123", passwordField.getAttribute("value"));
    }

    @Test
    @Order(3)
    public void testRadioButtons() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Select first radio button
        WebElement radio1 = driver.findElement(By.id("radio1"));
        radio1.click();
        assertTrue(radio1.isSelected());
        
        // Select second radio button
        WebElement radio2 = driver.findElement(By.id("radio2"));
        radio2.click();
        assertTrue(radio2.isSelected());
        
        // Ensure first is deselected
        assertFalse(radio1.isSelected());
    }

    @Test
    @Order(4)
    public void testCheckBoxes() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Check first checkbox
        WebElement checkbox1 = driver.findElement(By.id("checkbox1"));
        checkbox1.click();
        assertTrue(checkbox1.isSelected());
        
        // Check second checkbox
        WebElement checkbox2 = driver.findElement(By.id("checkbox2"));
        checkbox2.click();
        assertTrue(checkbox2.isSelected());
        
        // Uncheck first checkbox
        checkbox1.click();
        assertFalse(checkbox1.isSelected());
    }

    @Test
    @Order(5)
    public void testDropDownSelect() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement selectElement = driver.findElement(By.id("selectElement"));
        selectElement.click();
        
        WebElement option1 = driver.findElement(By.cssSelector("option[value='option1']"));
        option1.click();
        
        // Verify selection
        assertTrue(option1.isSelected());
    }

    @Test
    @Order(6)
    public void testTextArea() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement textArea = driver.findElement(By.id("textArea"));
        textArea.clear();
        textArea.sendKeys("This is a test message in the textarea.");
        assertEquals("This is a test message in the textarea.", textArea.getText());
    }

    @Test
    @Order(7)
    public void testFileUpload() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement fileInput = driver.findElement(By.id("fileInput"));
        String filePath = System.getProperty("user.dir") + "/src/test/resources/testfile.txt";
        fileInput.sendKeys(filePath);
        
        // Note: Cannot directly verify file upload without server-side check
        // Just ensuring the element is interactable
        assertTrue(fileInput.isDisplayed());
    }

    @Test
    @Order(8)
    public void testSubmitForm() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Fill form fields
        WebElement textField = wait.until(ExpectedConditions.elementToBeClickable(By.id("textField")));
        textField.sendKeys("Test Name");
        
        WebElement emailField = driver.findElement(By.id("emailField"));
        emailField.sendKeys("test@example.com");
        
        WebElement passwordField = driver.findElement(By.id("passwordField"));
        passwordField.sendKeys("password123");
        
        WebElement radio2 = driver.findElement(By.id("radio2"));
        radio2.click();
        
        WebElement checkbox2 = driver.findElement(By.id("checkbox2"));
        checkbox2.click();
        
        WebElement selectElement = driver.findElement(By.id("selectElement"));
        selectElement.click();
        WebElement option1 = driver.findElement(By.cssSelector("option[value='option1']"));
        option1.click();
        
        WebElement textArea = driver.findElement(By.id("textArea"));
        textArea.sendKeys("Test message");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.id("submitButton"));
        submitButton.click();
        
        // Verify submission (page should reload or show confirmation)
        wait.until(ExpectedConditions.urlContains("form.html")); // This might change depending on actual implementation
        assertTrue(driver.getCurrentUrl().contains("form.html"));
    }

    @Test
    @Order(9)
    public void testResetForm() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Fill some fields
        WebElement textField = wait.until(ExpectedConditions.elementToBeClickable(By.id("textField")));
        textField.sendKeys("Test Data");
        
        WebElement checkbox = driver.findElement(By.id("checkbox1"));
        checkbox.click();
        
        WebElement radio = driver.findElement(By.id("radio1"));
        radio.click();
        
        // Reset form
        WebElement resetButton = driver.findElement(By.id("resetButton"));
        resetButton.click();
        
        // Verify fields are cleared/reset
        assertEquals("", textField.getAttribute("value"));
        assertFalse(checkbox.isSelected());
        assertFalse(radio.isSelected());
    }

    @Test
    @Order(10)
    public void testValidation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Test required validation by submitting empty form
        WebElement submitButton = driver.findElement(By.id("submitButton"));
        submitButton.click();
        
        // No direct assertion for validation errors since the page behavior isn't fully specified
        // We simply verify we're still on the same page after trying to submit
        assertTrue(driver.getCurrentUrl().contains("form.html"));
    }
}