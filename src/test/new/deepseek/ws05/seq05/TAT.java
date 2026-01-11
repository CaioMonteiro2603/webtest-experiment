package deepseek.ws05.seq05;

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
public class TAT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("lastName"));
        lastName.sendKeys("Doe");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("11987654321");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("firstName")));
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
    }

    @Test
    @Order(2)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();
        
        WebElement firstNameError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".text-danger")));
        Assertions.assertTrue(firstNameError.getText().contains("First name is required") || 
                            firstNameError.getText().contains("required") || 
                            firstNameError.getText().contains("Nome é obrigatório"));
        
        WebElement lastNameError = driver.findElement(By.cssSelector("#lastName ~ .text-danger"));
        Assertions.assertTrue(lastNameError.getText().contains("Last name is required") || 
                            lastNameError.getText().contains("required") || 
                            lastNameError.getText().contains("Sobrenome é obrigatório"));
    }

    @Test
    @Order(3)
    public void testEmailValidation() {
        driver.get(BASE_URL);
        
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        email.sendKeys("invalid-email");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        WebElement emailError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#email ~ .text-danger")));
        Assertions.assertTrue(emailError.getText().contains("valid") || 
                            emailError.getText().contains("válido") ||
                            emailError.getText().toLowerCase().contains("email"));
    }

    @Test
    @Order(4)
    public void testPhoneValidation() {
        driver.get(BASE_URL);
        
        WebElement phone = wait.until(ExpectedConditions.elementToBeClickable(By.id("phone")));
        phone.sendKeys("123");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        WebElement phoneError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#phone ~ .text-danger")));
        Assertions.assertTrue(phoneError.getText().toLowerCase().contains("phone") || 
                            phoneError.getText().toLowerCase().contains("telefone") ||
                            phoneError.getText().toLowerCase().contains("invalid"));
    }

    @Test
    @Order(5)
    public void testResetButton() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstName.sendKeys("Test");
        
        firstName.clear();
        
        Assertions.assertEquals("", firstName.getAttribute("value"));
    }

    @Test
    @Order(6)
    public void testPrivacyPolicyLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Política de Privacidade")));
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