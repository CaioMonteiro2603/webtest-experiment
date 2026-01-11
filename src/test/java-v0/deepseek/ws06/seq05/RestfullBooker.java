package deepseek.ws06.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

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
    public void testRoomBookingFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstname")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("lastname"));
        lastName.sendKeys("Doe");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("1234567890");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.getText().contains("Booking Successful!"));
    }

    @Test
    @Order(2)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();
        
        WebElement firstNameError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstname-error")));
        Assertions.assertEquals("First name is required", firstNameError.getText());
        
        WebElement lastNameError = driver.findElement(By.id("lastname-error"));
        Assertions.assertEquals("Last name is required", lastNameError.getText());
    }

    @Test
    @Order(3)
    public void testEmailValidation() {
        driver.get(BASE_URL);
        
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        email.sendKeys("invalid-email");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        WebElement emailError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email-error")));
        Assertions.assertEquals("Please enter a valid email", emailError.getText());
    }

    @Test
    @Order(4)
    public void testPhoneValidation() {
        driver.get(BASE_URL);
        
        WebElement phone = wait.until(ExpectedConditions.elementToBeClickable(By.id("phone")));
        phone.sendKeys("123");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        WebElement phoneError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone-error")));
        Assertions.assertTrue(phoneError.getText().contains("Phone number is invalid"));
    }

    @Test
    @Order(5)
    public void testResetButton() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstname")));
        firstName.sendKeys("Test");
        
        WebElement resetButton = driver.findElement(By.cssSelector("button[type='reset']"));
        resetButton.click();
        
        Assertions.assertEquals("", firstName.getAttribute("value"));
    }

    @Test
    @Order(6)
    public void testPrivacyPolicyLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Privacy Policy")));
        privacyLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("privacy.html"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}