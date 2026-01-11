package geminiPro.ws02.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * A comprehensive JUnit 5 test suite for the ParaBank application.
 * This suite uses Selenium WebDriver with Firefox in headless mode to test user registration,
 * login, account creation, fund transfers, and logout functionality.
 * It creates a unique user for each test run to ensure idempotency.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Site and User Credentials ---
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    // A unique username is generated once for the entire test suite run
    private static final String USERNAME = "user" + System.currentTimeMillis();
    private static final String PASSWORD = "password123";
    private static String initialAccountId;
    private static String newAccountId;

    // --- Locators ---
    // General
    private static final By LOGOUT_LINK = By.linkText("Log Out");
    private static final By ERROR_TEXT = By.className("error");

    // Login
    private static final By USERNAME_INPUT = By.name("username");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.cssSelector("input[value='Log In']");

    // Registration
    private static final By REGISTER_LINK = By.linkText("Register");
    private static final By FIRST_NAME_INPUT = By.id("customer.firstName");
    private static final By LAST_NAME_INPUT = By.id("customer.lastName");
    private static final By ADDRESS_INPUT = By.id("customer.address.street");
    private static final By CITY_INPUT = By.id("customer.address.city");
    private static final By STATE_INPUT = By.id("customer.address.state");
    private static final By ZIP_CODE_INPUT = By.id("customer.address.zipCode");
    private static final By PHONE_INPUT = By.id("customer.phoneNumber");
    private static final By SSN_INPUT = By.id("customer.ssn");
    private static final By REG_USERNAME_INPUT = By.id("customer.username");
    private static final By REG_PASSWORD_INPUT = By.id("customer.password");
    private static final By REG_CONFIRM_INPUT = By.id("repeatedPassword");
    private static final By REGISTER_BUTTON = By.cssSelector("input[value='Register']");
    private static final By REG_SUCCESS_TEXT = By.xpath("//p[contains(text(), 'Your account was created successfully')]");


    // Accounts Overview
    private static final By ACCOUNTS_OVERVIEW_TITLE = By.xpath("//h1[text()='Accounts Overview']");

    // Left Menu
    private static final By OPEN_NEW_ACCOUNT_LINK = By.linkText("Open New Account");
    private static final By TRANSFER_FUNDS_LINK = By.linkText("Transfer Funds");

    // Open New Account
    private static final By ACCOUNT_TYPE_DROPDOWN = By.id("type");
    private static final By OPEN_ACCOUNT_BUTTON = By.cssSelector("input[value='Open New Account']");
    private static final By NEW_ACCOUNT_ID_LINK = By.id("newAccountId");
    
    // Transfer Funds
    private static final By TRANSFER_AMOUNT_INPUT = By.id("amount");
    private static final By FROM_ACCOUNT_DROPDOWN = By.id("fromAccountId");
    private static final By TO_ACCOUNT_DROPDOWN = By.id("toAccountId");
    private static final By TRANSFER_BUTTON = By.cssSelector("input[value='Transfer']");
    private static final By TRANSFER_COMPLETE_TITLE = By.xpath("//h1[text()='Transfer Complete!']");


    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should register a new user successfully")
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_LINK)).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME_INPUT)).sendKeys("Test");
        driver.findElement(LAST_NAME_INPUT).sendKeys("User");
        driver.findElement(ADDRESS_INPUT).sendKeys("123 Test St");
        driver.findElement(CITY_INPUT).sendKeys("Testville");
        driver.findElement(STATE_INPUT).sendKeys("TS");
        driver.findElement(ZIP_CODE_INPUT).sendKeys("12345");
        driver.findElement(PHONE_INPUT).sendKeys("555-1234");
        driver.findElement(SSN_INPUT).sendKeys("123-456-7890");
        driver.findElement(REG_USERNAME_INPUT).sendKeys(USERNAME);
        driver.findElement(REG_PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(REG_CONFIRM_INPUT).sendKeys(PASSWORD);
        driver.findElement(REGISTER_BUTTON).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(REG_SUCCESS_TEXT));
        Assertions.assertTrue(successMessage.isDisplayed(), "Registration success message not found.");
        Assertions.assertTrue(driver.findElement(By.xpath("//h1[contains(text(), 'Welcome " + USERNAME + "')]")).isDisplayed());
    }

    @Test
    @Order(2)
    @DisplayName("Should log out and then fail to log in with invalid credentials")
    void testLogoutAndInvalidLogin() {
        // This test depends on the user being logged in from the previous test
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));

        // Attempt invalid login
        driver.findElement(USERNAME_INPUT).sendKeys(USERNAME);
        driver.findElement(PASSWORD_INPUT).sendKeys("wrongpassword");
        driver.findElement(LOGIN_BUTTON).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_TEXT));
        Assertions.assertTrue(errorMessage.getText().contains("The username and password could not be verified."), "Error message for invalid login did not appear or was incorrect.");
    }
    
    @Test
    @Order(3)
    @DisplayName("Should log in successfully with valid credentials")
    void testSuccessfulLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(USERNAME);
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();

        WebElement overviewTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Accounts Overview')]")));
        Assertions.assertTrue(overviewTitle.isDisplayed(), "Login failed or did not navigate to Accounts Overview page.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "URL does not indicate the accounts overview page.");
    }

    @Test
    @Order(4)
    @DisplayName("Should open a new savings account")
    void testOpenNewAccount() {
        // This test depends on being logged in
        wait.until(ExpectedConditions.elementToBeClickable(OPEN_NEW_ACCOUNT_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNT_TYPE_DROPDOWN));

        // Wait for and get the initial account from dropdown
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#fromAccountId option")));
        initialAccountId = driver.findElement(By.cssSelector("#fromAccountId option")).getAttribute("value");

        Select accountType = new Select(driver.findElement(ACCOUNT_TYPE_DROPDOWN));
        accountType.selectByVisibleText("SAVINGS");
        driver.findElement(OPEN_ACCOUNT_BUTTON).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Account Opened!']")));
        WebElement newAccountLink = wait.until(ExpectedConditions.elementToBeClickable(NEW_ACCOUNT_ID_LINK));
        newAccountId = newAccountLink.getText(); // Store new account ID for next test

        Assertions.assertNotNull(newAccountId, "New account was not created or link not found.");
        Assertions.assertNotEquals(initialAccountId, newAccountId, "New account ID should be different from the initial account ID.");
    }

    @Test
    @Order(5)
    @DisplayName("Should transfer funds between accounts successfully")
    void testTransferFunds() {
        // This test depends on being logged in and having two accounts
        wait.until(ExpectedConditions.elementToBeClickable(TRANSFER_FUNDS_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(TRANSFER_AMOUNT_INPUT));
        
        // Ensure both account IDs from previous test are available
        Assertions.assertNotNull(initialAccountId, "Initial Account ID is null, cannot perform transfer.");
        Assertions.assertNotNull(newAccountId, "New Account ID is null, cannot perform transfer.");

        String transferAmount = "150";
        driver.findElement(TRANSFER_AMOUNT_INPUT).sendKeys(transferAmount);
        
        Select fromAccount = new Select(driver.findElement(FROM_ACCOUNT_DROPDOWN));
        fromAccount.selectByValue(initialAccountId);

        Select toAccount = new Select(driver.findElement(TO_ACCOUNT_DROPDOWN));
        toAccount.selectByValue(newAccountId);

        driver.findElement(TRANSFER_BUTTON).click();

        WebElement transferCompleteTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(TRANSFER_COMPLETE_TITLE));
        Assertions.assertTrue(transferCompleteTitle.isDisplayed(), "Transfer complete title not found.");

        WebElement successMessage = driver.findElement(By.xpath("//p[contains(text(), 'has been transferred')]"));
        Assertions.assertTrue(successMessage.getText().contains("$" + transferAmount + ".00"), "Transfer success message does not contain the correct amount.");
    }
}