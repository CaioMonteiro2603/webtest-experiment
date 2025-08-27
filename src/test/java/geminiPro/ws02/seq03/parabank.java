package geminiPRO.ws02.seq03;

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

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A complete JUnit 5 test suite for the ParaBank website using Selenium WebDriver
 * with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParaBankE2ETest {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String FIRST_NAME = "Gemini";
    private static final String LAST_NAME = "Tester";
    private static final String STREET = "123 Automation Ln";
    private static final String CITY = "Testville";
    private static final String STATE = "CA";
    private static final String ZIP_CODE = "90210";
    private static final String PHONE = "1234567890";
    private static final String SSN = "123-45-678";

    // --- Dynamic Test Data ---
    private static String testUsername;
    private static String testPassword;
    private static String primaryAccountId;
    private static String newAccountId;

    // --- Selenium WebDriver ---
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private static final By REGISTER_LINK = By.linkText("Register");
    private static final By LOGIN_USERNAME_INPUT = By.name("username");
    private static final By LOGIN_PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.cssSelector("input[value='Log In']");
    private static final By LOGOUT_LINK = By.linkText("Log Out");
    private static final By ACCOUNTS_OVERVIEW_TITLE = By.xpath("//h1[text()='Accounts Overview']");
    private static final By ERROR_MESSAGE_TITLE = By.xpath("//h1[text()='Error!']");
    private static final By WELCOME_MESSAGE_TEXT = By.cssSelector(".smallText");
    private static final By REGISTRATION_SUCCESS_MESSAGE = By.xpath("//p[contains(text(),'Your account was created successfully.')]");
    private static final By OPEN_NEW_ACCOUNT_LINK = By.linkText("Open New Account");
    private static final By TRANSFER_FUNDS_LINK = By.linkText("Transfer Funds");
    private static final By ACCOUNT_OPENED_TITLE = By.xpath("//h1[text()='Account Opened!']");
    private static final By NEW_ACCOUNT_ID_LINK = By.id("newAccountId");
    private static final By ACCOUNT_TABLE = By.id("accountTable");
    private static final By TRANSFER_AMOUNT_INPUT = By.id("amount");
    private static final By FROM_ACCOUNT_DROPDOWN = By.id("fromAccountId");
    private static final By TO_ACCOUNT_DROPDOWN = By.id("toAccountId");
    private static final By TRANSFER_BUTTON = By.cssSelector("input[value='Transfer']");
    private static final By TRANSFER_COMPLETE_MESSAGE = By.xpath("//h1[text()='Transfer Complete!']");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // Generate a unique username for each test run to ensure idempotency
        testUsername = "user" + System.currentTimeMillis();
        testPassword = "password123";
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void performLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_USERNAME_INPUT));
    }

    @Test
    @Order(1)
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_LINK)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName"))).sendKeys(FIRST_NAME);
        driver.findElement(By.id("customer.lastName")).sendKeys(LAST_NAME);
        driver.findElement(By.id("customer.address.street")).sendKeys(STREET);
        driver.findElement(By.id("customer.address.city")).sendKeys(CITY);
        driver.findElement(By.id("customer.address.state")).sendKeys(STATE);
        driver.findElement(By.id("customer.address.zipCode")).sendKeys(ZIP_CODE);
        driver.findElement(By.id("customer.phoneNumber")).sendKeys(PHONE);
        driver.findElement(By.id("customer.ssn")).sendKeys(SSN);
        driver.findElement(By.id("customer.username")).sendKeys(testUsername);
        driver.findElement(By.id("customer.password")).sendKeys(testPassword);
        driver.findElement(By.id("repeatedPassword")).sendKeys(testPassword);
        driver.findElement(By.cssSelector("input[value='Register']")).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(REGISTRATION_SUCCESS_MESSAGE));
        Assertions.assertTrue(successMessage.isDisplayed(), "Registration success message was not found.");

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_MESSAGE_TEXT));
        Assertions.assertTrue(welcomeMessage.getText().contains(FIRST_NAME + " " + LAST_NAME), "Welcome message is incorrect.");

        performLogout();
    }

    @Test
    @Order(2)
    void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_USERNAME_INPUT)).sendKeys(testUsername);
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys("wrongpassword");
        driver.findElement(LOGIN_BUTTON).click();

        WebElement errorTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_TITLE));
        Assertions.assertTrue(errorTitle.isDisplayed(), "Error message title was not displayed for invalid login.");
        WebElement errorText = driver.findElement(By.cssSelector(".error"));
        Assertions.assertTrue(errorText.getText().contains("The username and password could not be verified."), "Error message text is incorrect.");
    }

    @Test
    @Order(3)
    void testSuccessfulLoginAndLogout() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_USERNAME_INPUT)).sendKeys(testUsername);
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys(testPassword);
        driver.findElement(LOGIN_BUTTON).click();

        WebElement overviewTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_OVERVIEW_TITLE));
        Assertions.assertTrue(overviewTitle.isDisplayed(), "User was not redirected to the Accounts Overview page after login.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "URL is incorrect after login.");

        performLogout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "User was not returned to the home page after logout.");
    }

    @Test
    @Order(4)
    void testOpenNewAccount() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_USERNAME_INPUT)).sendKeys(testUsername);
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys(testPassword);
        driver.findElement(LOGIN_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_OVERVIEW_TITLE));
        
        // Store the ID of the first account for later use
        primaryAccountId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#accountTable tbody tr:first-child td:first-child a"))).getText();

        wait.until(ExpectedConditions.elementToBeClickable(OPEN_NEW_ACCOUNT_LINK)).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("type")));
        Select accountTypeDropdown = new Select(driver.findElement(By.id("type")));
        accountTypeDropdown.selectByVisibleText("SAVINGS");
        
        // Wait briefly for the DOM to update if necessary before clicking
        wait.until(ExpectedConditions.elementToBeClickable(By.id("fromAccountId")));
        driver.findElement(By.cssSelector("input[value='Open New Account']")).click();

        WebElement accountOpenedTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNT_OPENED_TITLE));
        Assertions.assertTrue(accountOpenedTitle.isDisplayed(), "Account Opened confirmation page was not displayed.");
        
        WebElement newAccountLink = wait.until(ExpectedConditions.visibilityOfElementLocated(NEW_ACCOUNT_ID_LINK));
        newAccountId = newAccountLink.getText();
        Assertions.assertNotNull(newAccountId, "New account ID was not found.");
        
        newAccountLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountId")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("activity.htm?id=" + newAccountId), "Did not navigate to the new account's activity page.");
    }

    @Test
    @Order(5)
    void testFundTransfer() {
        Assertions.assertNotNull(primaryAccountId, "Primary account ID is null. 'testOpenNewAccount' might have failed.");
        Assertions.assertNotNull(newAccountId, "New account ID is null. 'testOpenNewAccount' might have failed.");

        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_USERNAME_INPUT)).sendKeys(testUsername);
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys(testPassword);
        driver.findElement(LOGIN_BUTTON).click();

        // Get initial balances
        wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_OVERVIEW_TITLE));
        double fromAccountInitialBalance = getAccountBalance(primaryAccountId);
        double toAccountInitialBalance = getAccountBalance(newAccountId);

        wait.until(ExpectedConditions.elementToBeClickable(TRANSFER_FUNDS_LINK)).click();

        double amountToTransfer = 150.0;
        wait.until(ExpectedConditions.visibilityOfElementLocated(TRANSFER_AMOUNT_INPUT)).sendKeys(String.valueOf(amountToTransfer));
        
        Select fromAccountSelect = new Select(driver.findElement(FROM_ACCOUNT_DROPDOWN));
        fromAccountSelect.selectByValue(primaryAccountId);
        
        Select toAccountSelect = new Select(driver.findElement(TO_ACCOUNT_DROPDOWN));
        toAccountSelect.selectByValue(newAccountId);
        
        driver.findElement(TRANSFER_BUTTON).click();

        WebElement transferComplete = wait.until(ExpectedConditions.visibilityOfElementLocated(TRANSFER_COMPLETE_MESSAGE));
        Assertions.assertTrue(transferComplete.isDisplayed(), "Transfer completion message not found.");
        
        // Verify balances after transfer
        driver.findElement(By.linkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_OVERVIEW_TITLE));
        
        double fromAccountFinalBalance = getAccountBalance(primaryAccountId);
        double toAccountFinalBalance = getAccountBalance(newAccountId);

        Assertions.assertEquals(fromAccountInitialBalance - amountToTransfer, fromAccountFinalBalance, 0.01, "Sender's balance is incorrect after transfer.");
        Assertions.assertEquals(toAccountInitialBalance + amountToTransfer, toAccountFinalBalance, 0.01, "Receiver's balance is incorrect after transfer.");
    }

    private double getAccountBalance(String accountId) {
        // Find the row for the given account ID and get the balance from the next cell
        WebElement balanceCell = driver.findElement(By.xpath("//a[text()='" + accountId + "']/parent::td/following-sibling::td"));
        // Remove currency symbols and commas before parsing
        String balanceText = balanceCell.getText().replaceAll("[^\\d.]", "");
        return Double.parseDouble(balanceText);
    }
}