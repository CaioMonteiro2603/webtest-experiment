package Qwen3.ws04.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
    public void testPageLoad() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        assertEquals("Demo AUT", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("form.html"));
    }

    @Test
    @Order(2)
    public void testValidFormSubmission() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        firstNameField.sendKeys("John");
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        lastNameField.sendKeys("Doe");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("john.doe@example.com");
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("1234567890");
        WebElement addressField = driver.findElement(By.id("address"));
        addressField.sendKeys("123 Main St");
        WebElement cityField = driver.findElement(By.id("city"));
        cityField.sendKeys("Anytown");
        WebElement stateField = driver.findElement(By.id("state"));
        stateField.sendKeys("CA");
        WebElement zipField = driver.findElement(By.id("zip"));
        zipField.sendKeys("12345");
        WebElement countryField = driver.findElement(By.id("country"));
        countryField.sendKeys("USA");
        WebElement commentField = driver.findElement(By.id("comment"));
        commentField.sendKeys("Test comment");
        
        // Select gender radio button
        WebElement maleRadio = driver.findElement(By.id("male"));
        maleRadio.click();
        
        // Select interests checkboxes
        WebElement sportsCheckbox = driver.findElement(By.id("sports"));
        sportsCheckbox.click();
        WebElement readingCheckbox = driver.findElement(By.id("reading"));
        readingCheckbox.click();
        
        // Select education dropdown
        WebElement educationDropdown = driver.findElement(By.id("education"));
        educationDropdown.sendKeys("Bachelor's");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        // Verify success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".alert-success")).getText().contains("Form submitted successfully"));
    }

    @Test
    @Order(3)
    public void testInvalidFormSubmission() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Submit empty form
        WebElement submitButton = driver.findElement(By.id("submit"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", submitButton);
        submitButton.click();
        
        // Verify error messages
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        String validationMessage = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", firstNameField);
        assertTrue(validationMessage.contains("Please fill out") || validationMessage.contains("required"));
    }

    @Test
    @Order(4)
    public void testFormReset() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        firstNameField.sendKeys("John");
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        lastNameField.sendKeys("Doe");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("john.doe@example.com");

        // Reset form
        WebElement resetButton = driver.findElement(By.id("reset"));
        resetButton.click();
        
        // Verify fields are cleared
        assertEquals("", firstNameField.getAttribute("value"));
        assertEquals("", lastNameField.getAttribute("value"));
        assertEquals("", emailField.getAttribute("value"));
    }

    @Test
    @Order(5)
    public void testFormValidation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        emailField.sendKeys("invalid-email"); // Invalid email format

        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", submitButton);
        submitButton.click();
        
        // Check that validation error is shown
        String validationMessage = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", emailField);
        assertTrue(validationMessage.contains("Please") && validationMessage.contains("@"));
    }

    @Test
    @Order(6)
    public void testDropdownOptions() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Check options exist
        WebElement educationDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("education")));
        assertNotNull(driver.findElement(By.xpath("//option[text()='']")));
        assertNotNull(driver.findElement(By.xpath("//option[text()='High School']")));
        assertNotNull(driver.findElement(By.xpath("//option[text()=\"Bachelor's\"]")));
        assertNotNull(driver.findElement(By.xpath("//option[text()=\"Master's\"]")));
        assertNotNull(driver.findElement(By.xpath("//option[text()='PhD']")));
    }

    @Test
    @Order(7)
    public void testCheckboxSelection() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        // Check checkboxes are present
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sports")));
        WebElement sportsCheckbox = driver.findElement(By.id("sports"));
        WebElement readingCheckbox = driver.findElement(By.id("reading"));
        WebElement musicCheckbox = driver.findElement(By.id("music"));
        WebElement moviesCheckbox = driver.findElement(By.id("movies"));
        
        // Check they are not selected by default
        assertFalse(sportsCheckbox.isSelected());
        assertFalse(readingCheckbox.isSelected());
        assertFalse(musicCheckbox.isSelected());
        assertFalse(moviesCheckbox.isSelected());
        
        // Select checkboxes
        sportsCheckbox.click();
        readingCheckbox.click();
        
        // Verify selection
        assertTrue(sportsCheckbox.isSelected());
        assertTrue(readingCheckbox.isSelected());
    }
}