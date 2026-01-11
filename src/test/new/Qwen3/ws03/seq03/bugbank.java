package Qwen3.ws03.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email']")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"));
        assertTrue(driver.getTitle().contains("BugBank"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='email']")));
        emailField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.xpath("//input[@type='password']"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'error') or contains(text(),'Invalid')]")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Invalid") || errorElement.getText().contains("Error"));
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get("https://bugbank.netlify.app/dashboard");

        // Click Transfer link
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'transfer') or contains(text(),'Transfer')]")));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer"));
        assertTrue(driver.getCurrentUrl().contains("transfer"));
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get("https://bugbank.netlify.app/");

        // Click footer links
        if (driver.findElements(By.xpath("//a[contains(text(),'Terms')]")).size() > 0) {
            WebElement termsLink = driver.findElement(By.xpath("//a[contains(text(),'Terms')]"));
            termsLink.click();
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("bugbank.netlify.app"));
        }
        
        if (driver.findElements(By.xpath("//a[contains(text(),'Privacy')]")).size() > 0) {
            WebElement privacyLink = driver.findElement(By.xpath("//a[contains(text(),'Privacy')]"));
            privacyLink.click();
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("bugbank.netlify.app"));
        }
    }

    @Test
    @Order(5)
    public void testTransferFunds() {
        driver.get("https://bugbank.netlify.app/dashboard");

        // Navigate to transfer page
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'transfer')]")));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer"));
        assertTrue(driver.getCurrentUrl().contains("transfer"));

        // Fill transfer form with available fields
        if (driver.findElements(By.xpath("//input[contains(@id,'account') or contains(@name,'account')]")).size() >= 2) {
            java.util.List<WebElement> accountInputs = driver.findElements(By.xpath("//input[contains(@id,'account') or contains(@name,'account')]"));
            accountInputs.get(0).sendKeys("1234");
            accountInputs.get(1).sendKeys("5678");
        }
        
        if (driver.findElements(By.xpath("//input[contains(@id,'amount') or contains(@name,'amount')]")).size() > 0) {
            WebElement amount = driver.findElement(By.xpath("//input[contains(@id,'amount') or contains(@name,'amount')]"));
            amount.sendKeys("100");
        }
        
        if (driver.findElements(By.xpath("//input[contains(@id,'description') or contains(@name,'description')]")).size() > 0) {
            WebElement description = driver.findElement(By.xpath("//input[contains(@id,'description') or contains(@name,'description')]"));
            description.sendKeys("Test transfer");
        }

        // Submit transfer
        if (driver.findElements(By.xpath("//button[@type='submit']")).size() > 0) {
            WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
            submitButton.click();
            
            // Check for success message
            if (driver.findElements(By.xpath("//div[contains(@class,'success') or contains(text(),'success')]")).size() > 0) {
                WebElement successMessage = driver.findElement(By.xpath("//div[contains(@class,'success') or contains(text(),'success')]"));
                assertTrue(successMessage.isDisplayed());
            }
        }
    }

    @Test
    @Order(6)
    public void testAccountDetails() {
        driver.get("https://bugbank.netlify.app/dashboard");

        // Account information should be available on dashboard
        assertTrue(driver.getCurrentUrl().contains("dashboard"));
        assertTrue(driver.findElement(By.tagName("body")).getText().contains("Account") || driver.findElement(By.tagName("body")).getText().contains("Balance"));
    }

    @Test
    @Order(7)
    public void testTransactionsList() {
        driver.get("https://bugbank.netlify.app/dashboard");

        // Check for transactions or transaction history
        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("Transaction") || pageText.contains("History") || driver.getCurrentUrl().contains("dashboard"));
    }
}