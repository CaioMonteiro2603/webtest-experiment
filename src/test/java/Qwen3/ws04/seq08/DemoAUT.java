package Qwen3.ws04.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class KatalonFormTest {

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
    public void testFormSubmission() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement genderMale = driver.findElement(By.id("gender-male"));
        WebElement subscribeCheckbox = driver.findElement(By.id("subscribe"));
        WebElement countrySelect = driver.findElement(By.id("country"));
        WebElement messageField = driver.findElement(By.id("message"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        emailField.sendKeys("john.doe@example.com");
        passwordField.sendKeys("password123");
        genderMale.click();
        subscribeCheckbox.click();
        
        Select select = new Select(countrySelect);
        select.selectByVisibleText("United States");
        
        messageField.sendKeys("This is a test message");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("Form submitted successfully"));
    }

    @Test
    @Order(2)
    public void testRequiredFieldsValidation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(errorMessage.getText().contains("Please fill in all required fields"));
    }

    @Test
    @Order(3)
    public void testInvalidEmailFormat() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid-email");
        submitButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(errorMessage.getText().contains("Invalid email format"));
    }

    @Test
    @Order(4)
    public void testPasswordValidation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement firstNameField = driver.findElement(By.id("firstName"));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        emailField.sendKeys("john.doe@example.com");
        passwordField.sendKeys("123");
        submitButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(errorMessage.getText().contains("Password must be at least 8 characters"));
    }

    @Test
    @Order(5)
    public void testFormReset() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement firstNameField = driver.findElement(By.id("firstName"));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement resetButton = driver.findElement(By.cssSelector("button[type='reset']"));
        
        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        emailField.sendKeys("john.doe@example.com");
        passwordField.sendKeys("password123");
        
        resetButton.click();
        
        firstNameField = driver.findElement(By.id("firstName"));
        lastNameField = driver.findElement(By.id("lastName"));
        emailField = driver.findElement(By.id("email"));
        passwordField = driver.findElement(By.id("password"));
        
        assertTrue(firstNameField.getAttribute("value").isEmpty());
        assertTrue(lastNameField.getAttribute("value").isEmpty());
        assertTrue(emailField.getAttribute("value").isEmpty());
        assertTrue(passwordField.getAttribute("value").isEmpty());
    }

    @Test
    @Order(6)
    public void testCountryDropdownOptions() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement countrySelect = driver.findElement(By.id("country"));
        Select select = new Select(countrySelect);
        List<WebElement> options = select.getOptions();
        
        assertEquals(5, options.size());
        assertEquals("Select a country", options.get(0).getText());
        assertEquals("United States", options.get(1).getText());
        assertEquals("Canada", options.get(2).getText());
        assertEquals("United Kingdom", options.get(3).getText());
        assertEquals("Australia", options.get(4).getText());
    }

    @Test
    @Order(7)
    public void testGenderSelection() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement genderMale = driver.findElement(By.id("gender-male"));
        WebElement genderFemale = driver.findElement(By.id("gender-female"));
        WebElement genderOther = driver.findElement(By.id("gender-other"));
        
        genderMale.click();
        assertTrue(genderMale.isSelected());
        
        genderFemale.click();
        assertTrue(genderFemale.isSelected());
        
        genderOther.click();
        assertTrue(genderOther.isSelected());
    }
}