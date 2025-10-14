package Qwen3.ws03.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugBankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
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
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
        assertTrue(driver.getTitle().contains("Dashboard"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".error-message")).getText().contains("Invalid credentials"));
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get("https://bugbank.netlify.app/dashboard");

        // Click Account link
        WebElement accountLink = driver.findElement(By.linkText("Account"));
        accountLink.click();
        wait.until(ExpectedConditions.urlContains("/account"));
        assertTrue(driver.getCurrentUrl().contains("/account"));

        // Navigate back to dashboard
        driver.get("https://bugbank.netlify.app/dashboard");

        // Click Transactions link
        WebElement transactionsLink = driver.findElement(By.linkText("Transactions"));
        transactionsLink.click();
        wait.until(ExpectedConditions.urlContains("/transactions"));
        assertTrue(driver.getCurrentUrl().contains("/transactions"));

        // Navigate back to dashboard
        driver.get("https://bugbank.netlify.app/dashboard");

        // Click Transfer link
        WebElement transferLink = driver.findElement(By.linkText("Transfer"));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("/transfer"));
        assertTrue(driver.getCurrentUrl().contains("/transfer"));

        // Navigate back to dashboard
        driver.get("https://bugbank.netlify.app/dashboard");

        // Click Profile link
        WebElement profileLink = driver.findElement(By.linkText("Profile"));
        profileLink.click();
        wait.until(ExpectedConditions.urlContains("/profile"));
        assertTrue(driver.getCurrentUrl().contains("/profile"));

        // Navigate back to dashboard
        driver.get("https://bugbank.netlify.app/dashboard");

        // Click Logout
        WebElement logoutButton = driver.findElement(By.xpath("//button[contains(text(), 'Logout')]"));
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().contains("/"));
        
        // Re-login to continue testing
        testValidLogin();
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get("https://bugbank.netlify.app/");

        // Click Terms link
        WebElement termsLink = driver.findElement(By.linkText("Terms"));
        termsLink.click();
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("bugbank.netlify.app"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);

        // Click Privacy link
        WebElement privacyLink = driver.findElement(By.linkText("Privacy"));
        privacyLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("bugbank.netlify.app"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }

    @Test
    @Order(5)
    public void testTransferFunds() {
        driver.get("https://bugbank.netlify.app/dashboard");

        // Click Transfer link
        WebElement transferLink = driver.findElement(By.linkText("Transfer"));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("/transfer"));
        assertTrue(driver.getCurrentUrl().contains("/transfer"));

        // Fill transfer form
        WebElement fromAccount = wait.until(ExpectedConditions.elementToBeClickable(By.id("fromAccount")));
        fromAccount.sendKeys("1234");
        WebElement toAccount = driver.findElement(By.id("toAccount"));
        toAccount.sendKeys("5678");
        WebElement amount = driver.findElement(By.id("amount"));
        amount.sendKeys("100");
        WebElement description = driver.findElement(By.id("description"));
        description.sendKeys("Test transfer");

        // Submit transfer
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
        submitButton.click();

        // Check for success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success-message")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".success-message")).getText().contains("Transfer completed successfully"));
    }

    @Test
    @Order(6)
    public void testAccountDetails() {
        driver.get("https://bugbank.netlify.app/dashboard");

        // Click Account link
        WebElement accountLink = driver.findElement(By.linkText("Account"));
        accountLink.click();
        wait.until(ExpectedConditions.urlContains("/account"));
        assertTrue(driver.getCurrentUrl().contains("/account"));

        // Check account information is displayed
        WebElement accountInfo = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".account-info")));
        assertTrue(accountInfo.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".account-info")).getText().contains("Account Details"));
    }

    @Test
    @Order(7)
    public void testTransactionsList() {
        driver.get("https://bugbank.netlify.app/dashboard");

        // Click Transactions link
        WebElement transactionsLink = driver.findElement(By.linkText("Transactions"));
        transactionsLink.click();
        wait.until(ExpectedConditions.urlContains("/transactions"));
        assertTrue(driver.getCurrentUrl().contains("/transactions"));

        // Check that transactions list is displayed
        WebElement transactionsList = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".transactions-list")));
        assertTrue(transactionsList.isDisplayed());
        
        // Check if there are transactions
        if (driver.findElements(By.cssSelector(".transaction-item")).size() > 0) {
            WebElement firstTransaction = driver.findElement(By.cssSelector(".transaction-item"));
            assertTrue(firstTransaction.isDisplayed());
        }
    }
}