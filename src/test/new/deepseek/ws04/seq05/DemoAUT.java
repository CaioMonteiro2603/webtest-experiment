package deepseek.ws04.seq05;

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
    public void testFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");
        
        WebElement gender = driver.findElement(By.id("gender"));
        Select genderSelect = new Select(gender);
        genderSelect.selectByValue("male");
        
        WebElement dob = driver.findElement(By.id("dob"));
        dob.sendKeys("01/01/1990");
        
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main St");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("Test1234");
        
        WebElement company = driver.findElement(By.id("company"));
        company.sendKeys("ACME Inc");
        
        WebElement role = driver.findElement(By.id("role"));
        Select roleSelect = new Select(role);
        roleSelect.selectByValue("QA");
        
        WebElement expectation = driver.findElement(By.id("expectation"));
        Select expectationSelect = new Select(expectation);
        expectationSelect.selectByValue("Good");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"));
    }

    @Test
    @Order(2)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();
        
        WebElement firstNameError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name-error")));
        Assertions.assertTrue(firstNameError.getText().contains("First name is required"));
        
        WebElement lastNameError = driver.findElement(By.id("last-name-error"));
        Assertions.assertTrue(lastNameError.getText().contains("Last name is required"));
    }

    @Test
    @Order(3)
    public void testEmailValidation() {
        driver.get(BASE_URL);
        
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        email.sendKeys("invalid-email");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        WebElement emailError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email-error")));
        Assertions.assertTrue(emailError.getText().contains("Please enter a valid email"));
    }

    @Test
    @Order(4)
    public void testPasswordValidation() {
        driver.get(BASE_URL);
        
        WebElement password = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        password.sendKeys("weak");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        WebElement passwordError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password-error")));
        Assertions.assertTrue(passwordError.getText().contains("Password must be at least 8 characters"));
    }

    @Test
    @Order(5)
    public void testResetButton() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Test");
        
        WebElement resetButton = driver.findElement(By.id("reset"));
        resetButton.click();
        
        Assertions.assertEquals("", firstName.getAttribute("value"));
    }
}