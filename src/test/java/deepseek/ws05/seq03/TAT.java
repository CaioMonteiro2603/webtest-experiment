package deepseek.ws05.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
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
    public void testSuccessfulFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        firstName.sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("phone")).sendKeys("11987654321");
        
        Select productSelect = new Select(driver.findElement(By.id("product")));
        productSelect.selectByVisibleText("Blog");
        
        driver.findElement(By.name("atendimento-tat")).click();
        driver.findElement(By.id("email-checkbox")).click();
        driver.findElement(By.id("open-text-area")).sendKeys("This is a test message");
        driver.findElement(By.cssSelector(".button[type='submit']")).click();
        
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(successMessage.isDisplayed());
        Assertions.assertEquals("Mensagem enviada com sucesso", successMessage.getText());
    }

    @Test
    @Order(2)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        driver.findElement(By.cssSelector(".button[type='submit']")).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.isDisplayed());
        Assertions.assertEquals("ValidationError: First name is required", errorMessage.getText());
    }

    @Test
    @Order(3)
    public void testInvalidEmailFormat() {
        driver.get(BASE_URL);
        driver.findElement(By.id("firstName")).sendKeys("John");
        driver.findElement(By.id("email")).sendKeys("invalid.email");
        driver.findElement(By.cssSelector(".button[type='submit']")).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.isDisplayed());
        Assertions.assertTrue(errorMessage.getText().contains("invalid email"));
    }

    @Test
    @Order(4)
    public void testProductSelection() {
        driver.get(BASE_URL);
        Select productSelect = new Select(driver.findElement(By.id("product")));
        
        productSelect.selectByVisibleText("Cursos");
        Assertions.assertEquals("Cursos", productSelect.getFirstSelectedOption().getText());
        
        productSelect.selectByVisibleText("Mentoria");
        Assertions.assertEquals("Mentoria", productSelect.getFirstSelectedOption().getText());
        
        productSelect.selectByVisibleText("Blog");
        Assertions.assertEquals("Blog", productSelect.getFirstSelectedOption().getText());
    }

    @Test
    @Order(5)
    public void testPhoneInputWithCharacters() {
        driver.get(BASE_URL);
        WebElement phoneInput = driver.findElement(By.id("phone"));
        phoneInput.sendKeys("abc123");
        Assertions.assertEquals("", phoneInput.getAttribute("value"));
    }

    @Test
    @Order(6)
    public void testResetButton() {
        driver.get(BASE_URL);
        driver.findElement(By.id("firstName")).sendKeys("Test");
        driver.findElement(By.id("lastName")).sendKeys("User");
        driver.findElement(By.cssSelector(".button[type='reset']")).click();
        
        Assertions.assertEquals("", driver.findElement(By.id("firstName")).getAttribute("value"));
        Assertions.assertEquals("", driver.findElement(By.id("lastName")).getAttribute("value"));
    }

    @Test
    @Order(7)
    public void testPrivacyPolicyLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        driver.findElement(By.linkText("Política de Privacidade")).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("cac-tat.s3.amazonaws.com/privacy.html"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}