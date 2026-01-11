package deepseek.ws05.seq10;

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
public class TAT {
    private static WebDriver driver;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static WebDriverWait wait;

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
    public void testSuccessfulFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("lastName"));
        lastName.sendKeys("Doe");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("11987654321");
        
        Select productSelect = new Select(driver.findElement(By.id("product")));
        productSelect.selectByVisibleText("Cursos");
        
        WebElement message = driver.findElement(By.id("open-text-area"));
        message.sendKeys("This is a test message");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".success")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Form submission failed");
    }

    @Test
    @Order(2)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.getText().contains("Campo obrigatório"), "Required field validation failed");
    }

    @Test
    @Order(3)
    public void testInvalidEmailValidation() {
        driver.get(BASE_URL);
        
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        email.sendKeys("invalid-email");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.getText().contains("E-mail incorreto"), "Email validation failed");
    }

    @Test
    @Order(4)
    public void testPhoneNumberMask() {
        driver.get(BASE_URL);
        
        WebElement phone = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("phone")));
        phone.sendKeys("11987654321");
        
        Assertions.assertEquals("11987654321", phone.getAttribute("value"), "Phone mask not working");
    }

    @Test
    @Order(5)
    public void testResetForm() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        firstName.sendKeys("Test");
        
        WebElement resetButton = driver.findElement(By.xpath("//button[text()='Limpar']"));
        resetButton.click();
        
        Assertions.assertEquals("", firstName.getAttribute("value"), "Form reset failed");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Privacy Policy link
        testExternalLink("Privacidade", "cucumber.io");
        
        // Test Terms link
        testExternalLink("Termos", "cucumber.io");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), '" + linkText + "')]")));
        link.click();
        
        // Switch to new window if opened
        if (driver.getWindowHandles().size() > 1) {
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(mainWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                linkText + " link failed - wrong domain");
            driver.close();
            driver.switchTo().window(mainWindow);
        }
    }
}