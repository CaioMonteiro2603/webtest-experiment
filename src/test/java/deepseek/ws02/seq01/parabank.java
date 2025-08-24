package deepseek.ws02.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class ParaBankTest {
    private static WebDriver driver;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
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
        driver.get(BASE_URL);
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Login failed or didn't redirect to overview page");
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Accounts Overview"), 
            "Accounts Overview page not loaded");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("p.error")));
        Assertions.assertTrue(errorMessage.getText().contains("An internal error has occurred"), 
            "Expected error message not displayed");
    }

    @Test
    @Order(3)
    public void testAccountNavigations() {
        login();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Test Open New Account
        WebElement openAccountLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Open New Account")));
        openAccountLink.click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Open New Account"), 
            "Open New Account page not loaded");
        
        // Test Accounts Overview
        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Accounts Overview")));
        accountsOverviewLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Accounts Overview"), 
            "Accounts Overview page not loaded after navigation");
    }

    @Test
    @Order(4)
    public void testTransferFunds() {
        login();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Transfer Funds")));
        transferFundsLink.click();
        
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Transfer Funds"), 
            "Transfer Funds page not loaded");
        
        Select fromAccount = new Select(wait.until(ExpectedConditions.elementToBeClickable(By.id("fromAccountId"))));
        Select toAccount = new Select(driver.findElement(By.id("toAccountId")));
        WebElement amountField = driver.findElement(By.id("amount"));
        WebElement transferButton = driver.findElement(By.cssSelector("input[value='Transfer']"));
        
        fromAccount.selectByIndex(0);
        toAccount.selectByIndex(1);
        amountField.sendKeys("100");
        transferButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(successMessage.getText().contains("Transfer Complete"), 
            "Transfer not completed successfully");
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        login();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String originalWindow = driver.getWindowHandle();
        
        // Test About Us
        WebElement aboutUsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("About Us")));
        aboutUsLink.click();
        wait.until(ExpectedConditions.urlContains("about.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("About Us"), 
            "About Us page not loaded");
        
        // Test Services
        driver.get(BASE_URL);
        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Services")));
        servicesLink.click();
        wait.until(ExpectedConditions.urlContains("services.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Services"), 
            "Services page not loaded");
    }

    @Test
    @Order(6)
    public void testLogout() {
        login();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Log Out")));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("index.htm"), 
            "Logout didn't redirect to home page");
        Assertions.assertTrue(driver.findElement(By.cssSelector("#leftPanel h2")).getText().contains("Customer Login"), 
            "Login form not visible after logout");
    }

    private void login() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }
}