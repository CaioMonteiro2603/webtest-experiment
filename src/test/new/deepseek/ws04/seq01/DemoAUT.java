package deepseek.ws04.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        WebElement lastNameField = driver.findElement(By.id("last-name"));
        WebElement genderRadio = driver.findElement(By.id("male"));
        WebElement dobField = driver.findElement(By.id("dob"));
        WebElement addressField = driver.findElement(By.id("address"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement companyField = driver.findElement(By.id("company"));
        WebElement roleDropdown = driver.findElement(By.id("role"));
        WebElement expectField = driver.findElement(By.id("expectation"));
        WebElement submitButton = driver.findElement(By.id("submit"));

        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        genderRadio.click();
        dobField.sendKeys("01/01/1990");
        addressField.sendKeys("123 Main St");
        emailField.sendKeys("john.doe@example.com");
        passwordField.sendKeys("password123");
        companyField.sendKeys("Acme Corp");
        new Select(roleDropdown).selectByVisibleText("QA");
        expectField.sendKeys("Looking for a challenging role.");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"), "Form submission should be successful");
    }

    @Test
    @Order(2)
    public void testFormValidation() {
        driver.get(BASE_URL);
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();

        WebElement firstNameError = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name-error")));
        Assertions.assertTrue(firstNameError.isDisplayed(), "First name error should be displayed");
        WebElement lastNameError = driver.findElement(By.id("last-name-error"));
        Assertions.assertTrue(lastNameError.isDisplayed(), "Last name error should be displayed");
        WebElement emailError = driver.findElement(By.id("email-error"));
        Assertions.assertTrue(emailError.isDisplayed(), "Email error should be displayed");
    }

    @Test
    @Order(3)
    public void testOptionalFields() {
        driver.get(BASE_URL);
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        WebElement lastNameField = driver.findElement(By.id("last-name"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement dobField = driver.findElement(By.id("dob"));
        WebElement submitButton = driver.findElement(By.id("submit"));

        firstNameField.sendKeys("Jane");
        lastNameField.sendKeys("Smith");
        emailField.sendKeys("jane.smith@example.com");
        dobField.sendKeys("01/01/1990");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"), "Form should accept optional fields");
    }

    @Test
    @Order(4)
    public void testExternalLink() {
        driver.get(BASE_URL);
        WebElement katalonLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Katalon Studio")));
        katalonLink.click();

        String currentWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("katalon.com"));
        driver.close();
        driver.switchTo().window(currentWindow);
    }

    @Test
    @Order(5)
    public void testResetButton() {
        driver.get(BASE_URL);
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        WebElement resetButton = driver.findElement(By.id("reset-form"));

        firstNameField.sendKeys("Test");
        resetButton.click();
        Assertions.assertEquals("", firstNameField.getAttribute("value"), "First name field should be reset");
    }
}