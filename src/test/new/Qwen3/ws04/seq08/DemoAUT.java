package Qwen3.ws04.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    public void testFormSubmission() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));
        WebElement lastNameField = driver.findElement(By.name("lastName"));
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement genderMale = driver.findElement(By.id("male"));
        WebElement subscribeCheckbox = driver.findElement(By.name("subscribe"));
        WebElement countrySelect = driver.findElement(By.id("country"));
        WebElement messageField = driver.findElement(By.name("message"));
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

        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));
        String validationMessage = firstNameField.getAttribute("validationMessage");
        assertNotNull(validationMessage);
        assertFalse(validationMessage.isEmpty());
    }

    @Test
    @Order(3)
    public void testInvalidEmailFormat() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid-email");
        submitButton.click();
        
        String validationMessage = emailField.getAttribute("validationMessage");
        assertNotNull(validationMessage);
        assertTrue(validationMessage.contains("Please enter a valid email address") || validationMessage.contains("Please include an '@'"));
    }

    @Test
    @Order(4)
    public void testPasswordValidation() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));
        WebElement lastNameField = driver.findElement(By.name("lastName"));
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        emailField.sendKeys("john.doe@example.com");
        passwordField.sendKeys("123");
        
        String validationMessage = passwordField.getAttribute("validationMessage");
        assertNotNull(validationMessage);
        assertTrue(validationMessage.contains("minimum") || validationMessage.contains("least"));
    }

    @Test
    @Order(5)
    public void testFormReset() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstName")));
        WebElement lastNameField = driver.findElement(By.name("lastName"));
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement resetButton = driver.findElement(By.cssSelector("button[type='reset']"));
        
        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        emailField.sendKeys("john.doe@example.com");
        passwordField.sendKeys("password123");
        
        resetButton.click();
        
        firstNameField = driver.findElement(By.name("firstName"));
        lastNameField = driver.findElement(By.name("lastName"));
        emailField = driver.findElement(By.name("email"));
        passwordField = driver.findElement(By.name("password"));
        
        assertTrue(firstNameField.getAttribute("value").isEmpty());
        assertTrue(lastNameField.getAttribute("value").isEmpty());
        assertTrue(emailField.getAttribute("value").isEmpty());
        assertTrue(passwordField.getAttribute("value").isEmpty());
    }

    @Test
    @Order(6)
    public void testCountryDropdownOptions() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement countrySelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("country")));
        Select select = new Select(countrySelect);
        List<WebElement> options = select.getOptions();
        
        assertTrue(options.size() >= 4);
        assertEquals("Select a country", options.get(0).getText());
        assertEquals("United States", options.get(1).getText());
        assertEquals("Canada", options.get(2).getText());
        assertEquals("United Kingdom", options.get(3).getText());
    }

    @Test
    @Order(7)
    public void testGenderSelection() {
        driver.get("https://katalon-test.s3.amazonaws.com/aut/html/form.html");
        
        WebElement genderMale = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("male")));
        WebElement genderFemale = driver.findElement(By.id("female"));
        WebElement genderOther = driver.findElement(By.id("other"));
        
        genderMale.click();
        assertTrue(genderMale.isSelected());
        
        genderFemale.click();
        assertTrue(genderFemale.isSelected());
        
        genderOther.click();
        assertTrue(genderOther.isSelected());
    }
}