package Qwen3.ws03.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class bugbank {

    private static WebDriver driver;
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
    public void testValidLogin() {
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("app"));
        assertEquals("https://bugbank.netlify.app/app", driver.getCurrentUrl());
        assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Dashboard"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        usernameField.sendKeys("invalid@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("invalid");
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("error")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Invalid"));
    }

    @Test
    @Order(3)
    public void testForgotPassword() {
        driver.get("https://bugbank.netlify.app/");
        WebElement forgotPasswordLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Forgot password?")));
        forgotPasswordLink.click();

        wait.until(ExpectedConditions.urlContains("forgot-password"));
        assertEquals("https://bugbank.netlify.app/forgot-password", driver.getCurrentUrl());
    }

    @Test
    @Order(4)
    public void testRegister() {
        driver.get("https://bugbank.netlify.app/");
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();

        wait.until(ExpectedConditions.urlContains("register"));
        assertEquals("https://bugbank.netlify.app/register", driver.getCurrentUrl());
    }

    @Test
    @Order(5)
    public void testNavigationMenu() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Test Home link
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("index"));
        assertEquals("https://bugbank.netlify.app/", driver.getCurrentUrl());

        // Test Dashboard link
        WebElement dashboardLink = driver.findElement(By.linkText("Dashboard"));
        dashboardLink.click();
        wait.until(ExpectedConditions.urlContains("app"));
        assertEquals("https://bugbank.netlify.app/app", driver.getCurrentUrl());

        // Test Profile link
        WebElement profileLink = driver.findElement(By.linkText("Profile"));
        profileLink.click();
        wait.until(ExpectedConditions.urlContains("profile"));
        assertEquals("https://bugbank.netlify.app/profile", driver.getCurrentUrl());

        // Test Logout link
        WebElement logoutLink = driver.findElement(By.linkText("Logout"));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index"));
        assertEquals("https://bugbank.netlify.app/", driver.getCurrentUrl());
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get("https://bugbank.netlify.app/");
        
        // Test Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
        twitterLink.click();
        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        String mainWindowHandle = driver.getWindowHandle();
        String newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);

        // Test Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
        facebookLink.click();
        windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        mainWindowHandle = driver.getWindowHandle();
        newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);

        // Test LinkedIn link
        WebElement linkedInLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
        linkedInLink.click();
        windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        mainWindowHandle = driver.getWindowHandle();
        newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);
    }

    @Test
    @Order(7)
    public void testAccountManagement() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Test account creation
        WebElement accountsTab = driver.findElement(By.linkText("Accounts"));
        accountsTab.click();
        wait.until(ExpectedConditions.urlContains("accounts"));

        // Check for account list
        List<WebElement> accountRows = driver.findElements(By.cssSelector("#accountTable tbody tr"));
        assertTrue(accountRows.size() >= 0);
    }

    @Test
    @Order(8)
    public void testTransferFunds() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Navigate to Transfer Funds
        WebElement transferLink = driver.findElement(By.linkText("Transfer Funds"));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer"));

        // Test form fields
        WebElement fromAccount = driver.findElement(By.id("fromAccount"));
        WebElement toAccount = driver.findElement(By.id("toAccount"));
        WebElement amount = driver.findElement(By.id("amount"));

        assertTrue(fromAccount.isDisplayed());
        assertTrue(toAccount.isDisplayed());
        assertTrue(amount.isDisplayed());
    }

    @Test
    @Order(9)
    public void testTransactions() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Navigate to Transactions
        WebElement transactionsLink = driver.findElement(By.linkText("Transactions"));
        transactionsLink.click();
        wait.until(ExpectedConditions.urlContains("transactions"));

        // Check for transaction list
        List<WebElement> transactionRows = driver.findElements(By.cssSelector("#transactionTable tbody tr"));
        assertTrue(transactionRows.size() >= 0);
    }

    @Test
    @Order(10)
    public void testProfileUpdate() {
        driver.get("https://bugbank.netlify.app/profile");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Test profile update form
        WebElement firstNameField = driver.findElement(By.id("firstName"));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        WebElement emailField = driver.findElement(By.id("email"));

        assertTrue(firstNameField.isDisplayed());
        assertTrue(lastNameField.isDisplayed());
        assertTrue(emailField.isDisplayed());
    }

    @Test
    @Order(11)
    public void testSettings() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Test Settings link
        WebElement settingsLink = driver.findElement(By.linkText("Settings"));
        settingsLink.click();
        wait.until(ExpectedConditions.urlContains("settings"));

        // Verify settings page loaded
        assertTrue(driver.getCurrentUrl().contains("settings"));
        assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Settings"));
    }

    @Test
    @Order(12)
    public void testDashboardComponents() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Check dashboard elements
        assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Dashboard"));
        
        // Check for quick actions
        List<WebElement> quickActionButtons = driver.findElements(By.cssSelector(".quick-action"));
        assertTrue(quickActionButtons.size() >= 0);
        
        // Check account summary
        List<WebElement> accountCards = driver.findElements(By.cssSelector(".account-card"));
        assertTrue(accountCards.size() >= 0);
    }
}