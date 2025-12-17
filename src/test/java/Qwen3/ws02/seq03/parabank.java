package Qwen3.ws02.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

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
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/parabank/overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("/parabank/overview.htm"));
        assertTrue(driver.getTitle().contains("ParaBank | Overview"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".error")).getText().contains("Error"));
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");

        // Click Personal Loans link
        WebElement personalLoans = driver.findElement(By.linkText("Personal Loans"));
        personalLoans.click();
        wait.until(ExpectedConditions.urlContains("/parabank/loan.htm"));
        assertTrue(driver.getCurrentUrl().contains("/parabank/loan.htm"));

        // Navigate back to overview
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");

        // Click Contact link
        WebElement contact = driver.findElement(By.linkText("Contact"));
        contact.click();
        wait.until(ExpectedConditions.urlContains("/parabank/contact.htm"));
        assertTrue(driver.getCurrentUrl().contains("/parabank/contact.htm"));

        // Navigate back to overview
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");

        // Click Home link
        WebElement home = driver.findElement(By.linkText("Home"));
        home.click();
        wait.until(ExpectedConditions.urlContains("/parabank/index.htm"));
        assertTrue(driver.getCurrentUrl().contains("/parabank/index.htm"));

        // Re-login to continue testing
        testValidLogin();
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        // Click Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("[href*='facebook']"));
        facebookLink.click();
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);

        // Click Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("[href*='twitter']"));
        twitterLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);

        // Click LinkedIn link
        WebElement linkedinLink = driver.findElement(By.cssSelector("[href*='linkedin']"));
        linkedinLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }

    @Test
    @Order(5)
    public void testContactForm() {
        driver.get("https://parabank.parasoft.com/parabank/contact.htm");

        // Fill contact form
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("name")));
        nameField.sendKeys("Test User");
        WebElement emailField = driver.findElement(By.name("email"));
        emailField.sendKeys("test@example.com");
        WebElement subjectField = driver.findElement(By.name("subject"));
        subjectField.sendKeys("Test Subject");
        WebElement messageField = driver.findElement(By.name("message"));
        messageField.sendKeys("Test message content");

        // Submit form
        WebElement submitButton = driver.findElement(By.xpath("//input[@value='Send Message']"));
        submitButton.click();

        // Check for success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".message")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".message")).getText().contains("Your message was sent successfully"));
    }

    @Test
    @Order(6)
    public void testTransferFunds() {
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");

        // Click Transfer Funds
        WebElement transferFunds = driver.findElement(By.linkText("Transfer Funds"));
        transferFunds.click();
        wait.until(ExpectedConditions.urlContains("/parabank/transfer.htm"));
        assertTrue(driver.getCurrentUrl().contains("/parabank/transfer.htm"));

        // Select source and destination accounts
        WebElement fromAccount = driver.findElement(By.name("fromAccountId"));
        fromAccount.sendKeys("12345");
        WebElement toAccount = driver.findElement(By.name("toAccountId"));
        toAccount.sendKeys("12346");

        // Fill amount and submit
        WebElement amount = driver.findElement(By.name("amount"));
        amount.sendKeys("100");
        WebElement submitButton = driver.findElement(By.xpath("//input[@value='Transfer']"));
        submitButton.click();

        // Check for success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".message")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".message")).getText().contains("The transfer was completed successfully"));
    }
}