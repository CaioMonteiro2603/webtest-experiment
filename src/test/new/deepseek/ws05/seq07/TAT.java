package deepseek.ws05.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.File;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testSubmitFormWithValidData() {
        driver.get(BASE_URL);
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        WebElement lastName = driver.findElement(By.id("lastName"));
        WebElement email = driver.findElement(By.id("email"));
        WebElement phone = driver.findElement(By.id("phone"));
        WebElement textArea = driver.findElement(By.id("open-text-area"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        email.sendKeys("john.doe@example.com");
        phone.sendKeys("5511999999999");
        textArea.sendKeys("This is a test message");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Success message should be displayed");
    }

    @Test
    @Order(2)
    public void testSubmitFormWithEmptyRequiredFields() {
        driver.get(BASE_URL);
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
    }

    @Test
    @Order(3)
    public void testSubmitFormWithInvalidEmail() {
        driver.get(BASE_URL);
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys("invalid-email");
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
    }

    @Test
    @Order(4)
    public void testFileUpload() {
        driver.get(BASE_URL);
        WebElement fileUpload = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("file-upload")));
        
        // Create a temporary file for upload
        String filePath;
        try {
            File tempFile = File.createTempFile("testfile", ".txt");
            tempFile.deleteOnExit();
            filePath = tempFile.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test file", e);
        }
        
        fileUpload.sendKeys(filePath);

        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        WebElement uploadInfo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".upload-info")));
        Assertions.assertTrue(uploadInfo.getText().contains("testfile"), "File should be uploaded successfully");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement tatLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, 'tatcursos') or text()='TAT' or contains(text(), 'TAT')]")));
        tatLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("tatcursos.com.br"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}