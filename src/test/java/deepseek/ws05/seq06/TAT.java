package deepseek.ws05.seq06;

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
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
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
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        WebElement lastName = driver.findElement(By.id("lastName"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement phone = driver.findElement(By.id("phone"));
        WebElement textarea = driver.findElement(By.id("open-text-area"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        email.sendKeys("john.doe@example.com");
        phone.sendKeys("1234567890");
        textarea.sendKeys("This is a test message");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Form should be submitted successfully");
    }

    @Test
    @Order(2)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be shown for required fields");
    }

    @Test
    @Order(3)
    public void testProductLinks() {
        driver.get(BASE_URL);
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Produtos")));
        productLink.click();

        wait.until(ExpectedConditions.urlContains("produtos.html"));
        WebElement productTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(productTitle.getText().contains("Produtos"), "Should navigate to Products page");
    }

    @Test
    @Order(4)
    public void testContactLink() {
        driver.get(BASE_URL);
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contato")));
        contactLink.click();

        wait.until(ExpectedConditions.urlContains("contato.html"));
        WebElement contactTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(contactTitle.getText().contains("Contato"), "Should navigate to Contact page");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testMobilePhoneInput() {
        driver.get(BASE_URL);
        WebElement phone = wait.until(ExpectedConditions.elementToBeClickable(By.id("phone")));
        phone.sendKeys("abc123");
        
        Assertions.assertEquals("", phone.getAttribute("value"), "Non-numeric input should be filtered out");
    }
}