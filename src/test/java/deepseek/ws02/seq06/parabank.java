package deepseek.ws02.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Accounts Overview"), "Accounts Overview page should be displayed after login");
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
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.error")));
        Assertions.assertTrue(errorMessage.getText().contains("An internal error has occurred"), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testAccountNavigation() {
        testValidLogin(); // Ensure logged in first
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Test Open New Account
        WebElement openNewAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openNewAccountLink.click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        WebElement openAccountTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertTrue(openAccountTitle.getText().contains("Open New Account"), "Open New Account page should be displayed");

        // Test Accounts Overview
        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountsOverviewLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement accountsOverviewTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertTrue(accountsOverviewTitle.getText().contains("Accounts Overview"), "Accounts Overview page should be displayed");
    }

    @Test
    @Order(4)
    public void testTransferFunds() {
        testValidLogin(); // Ensure logged in first
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        WebElement transferTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertTrue(transferTitle.getText().contains("Transfer Funds"), "Transfer Funds page should be displayed");

        Select fromAccount = new Select(driver.findElement(By.id("fromAccountId")));
        Select toAccount = new Select(driver.findElement(By.id("toAccountId")));
        WebElement amountField = driver.findElement(By.id("amount"));
        WebElement transferButton = driver.findElement(By.cssSelector("input[value='Transfer']"));

        fromAccount.selectByIndex(0);
        toAccount.selectByIndex(1);
        amountField.sendKeys("100");
        transferButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertTrue(successMessage.getText().contains("Transfer Complete"), "Transfer should be completed successfully");
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Test About Us link
        WebElement aboutUsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutUsLink.click();
        wait.until(ExpectedConditions.urlContains("about.htm"));
        WebElement aboutTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertTrue(aboutTitle.getText().contains("ParaSoft Demo Website"), "About Us page should be displayed");

        // Test Contact link
        WebElement contactLink = wait.until(Expected