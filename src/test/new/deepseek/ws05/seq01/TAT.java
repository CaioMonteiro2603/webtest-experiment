package deepseek.ws05.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement phoneField = driver.findElement(By.id("phone"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        emailField.sendKeys("john.doe@example.com");
        phoneField.sendKeys("1234567890");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("success")));
        Assertions.assertTrue(successMessage.getText().contains("Your message has been sent"), "Form submission should be successful");
    }

    @Test
    @Order(2)
    public void testFormValidation() {
        driver.get(BASE_URL);
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();

        WebElement firstNameError = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName-error")));
        Assertions.assertTrue(firstNameError.isDisplayed(), "First name error should be displayed");
        WebElement lastNameError = driver.findElement(By.id("lastName-error"));
        Assertions.assertTrue(lastNameError.isDisplayed(), "Last name error should be displayed");
        WebElement emailError = driver.findElement(By.id("email-error"));
        Assertions.assertTrue(emailError.isDisplayed(), "Email error should be displayed");
    }

    @Test
    @Order(3)
    public void testOptionalFields() {
        driver.get(BASE_URL);
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        firstNameField.sendKeys("Jane");
        emailField.sendKeys("jane.doe@example.com");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("success")));
        Assertions.assertTrue(successMessage.getText().contains("Your message has been sent"), "Form should accept optional fields");
    }

    @Test
    @Order(4)
    public void testResetButton() {
        driver.get(BASE_URL);
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        WebElement resetButton = driver.findElement(By.cssSelector("button[type='reset']"));

        firstNameField.sendKeys("Test");
        resetButton.click();
        Assertions.assertEquals("", firstNameField.getAttribute("value"), "First name field should be reset");
    }

    @Test
    @Order(5)
    public void testExternalLink() {
        driver.get(BASE_URL);
        WebElement gitHubLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("GitHub")));
        gitHubLink.click();

        String currentWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("github.com"));
        driver.close();
        driver.switchTo().window(currentWindow);
    }
}