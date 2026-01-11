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
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.name("firstname")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.name("lastname"));
        lastName.sendKeys("Doe");
        
        WebElement email = driver.findElement(By.name("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement phone = driver.findElement(By.name("phone"));
        phone.sendKeys("1234567890");
        
        WebElement submitButton = driver.findElement(By.id("submitContact"));
        submitButton.click();
        
        String toastMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'alert')]//h3"))).getText();
        Assertions.assertEquals("Thanks for getting in touch Test", toastMessage);
    }

    @Test
    @Order(2)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submitContact")));
        submitButton.click();
        
        String firstNameError = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='firstname']/following-sibling::span"))).getText();
        Assertions.assertEquals("Name must be between 3 and 30 characters", firstNameError);
        
        String lastNameError = driver.findElement(
                By.xpath("//input[@name='lastname']/following-sibling::span")).getText();
        Assertions.assertEquals("Name must be between 3 and 30 characters", lastNameError);
    }

    @Test
    @Order(3)
    public void testEmailValidation() {
        driver.get(BASE_URL);
        
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.name("email")));
        email.sendKeys("invalid-email");
        
        WebElement submitButton = driver.findElement(By.id("submitContact"));
        submitButton.click();
        
        String emailError = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='email']/following-sibling::span"))).getText();
        Assertions.assertEquals("Must be a valid email address", emailError);
    }

    @Test
    @Order(4)
    public void testPhoneValidation() {
        driver.get(BASE_URL);
        
        WebElement phone = wait.until(ExpectedConditions.elementToBeClickable(By.name("phone")));
        phone.sendKeys("123");
        
        WebElement submitButton = driver.findElement(By.id("submitContact"));
        submitButton.click();
        
        String phoneError = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='phone']/following-sibling::span"))).getText();
        Assertions.assertEquals("Must be a valid phone number", phoneError);
    }

    @Test
    @Order(5)
    public void testResetButton() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.name("firstname")));
        firstName.sendKeys("Test");
        
        WebElement resetButton = driver.findElement(By.id("clearContact"));
        resetButton.click();
        
        Assertions.assertEquals("", firstName.getAttribute("value"));
    }

    @Test
    @Order(6)
    public void testPrivacyPolicyLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Privacy Policy')]")));
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