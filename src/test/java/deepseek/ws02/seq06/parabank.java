package deepseek.ws02.seq06;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        createUser(driver);
    }

    private static void createUser(WebDriver driver) {
        driver.get("https://parabank.parasoft.com/parabank/register.htm");
        driver.findElement(By.id("customer.firstName")).click();
        driver.findElement(By.id("customer.firstName")).sendKeys("a");
        driver.findElement(By.id("customer.lastName")).click();
        driver.findElement(By.id("customer.lastName")).sendKeys("a");
        driver.findElement(By.id("customer.address.street")).click();
        driver.findElement(By.id("customer.address.street")).sendKeys("a");
        driver.findElement(By.id("customer.address.city")).click();
        driver.findElement(By.id("customer.address.city")).sendKeys("a");
        driver.findElement(By.id("customer.address.state")).click();
        driver.findElement(By.id("customer.address.state")).sendKeys("a");
        driver.findElement(By.id("customer.address.zipCode")).click();
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("a");
        driver.findElement(By.id("customer.phoneNumber")).click();
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("a");
        driver.findElement(By.id("customer.ssn")).click();
        driver.findElement(By.id("customer.ssn")).sendKeys("a");
        driver.findElement(By.id("customer.username")).click();
        driver.findElement(By.id("customer.username")).sendKeys("caio@gmail.com");
        driver.findElement(By.id("customer.password")).sendKeys("123");
        driver.findElement(By.id("repeatedPassword")).sendKeys("123");
        driver.findElement(By.cssSelector("td > .button")).click();
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

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
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
}