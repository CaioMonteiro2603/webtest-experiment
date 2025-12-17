package geminiPro.ws02.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

/**
 * A comprehensive JUnit 5 test suite for the ParaBank website using Selenium WebDriver with Firefox in headless mode.
 * This suite covers user registration, login, account creation, fund transfers, and navigation link validation.
 * It is designed to run sequentially, with later tests depending on the state created by earlier ones.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Site and User Details ---
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    // Using a unique username to ensure the registration test can be re-run.
    private static final String USERNAME = "gemini-user-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String PASSWORD = "password123";
    private static final String FIRST_NAME = "Gemini";
    private static final String LAST_NAME = "Tester";

    // --- Locators ---
    // Login / Logout
    private static final By USERNAME_INPUT = By.name("username");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.xpath("//input[@value='Log In']");
    private static final By LOGOUT_LINK = By.linkText("Log Out");
    private static final By LOGIN_ERROR_MESSAGE = By.cssSelector("p.error");
    private static final By ACCOUNTS_OVERVIEW_TITLE = By.xpath("//h1[text()='Accounts Overview']");

    // Registration
    private static final By REGISTER_LINK = By.linkText("Register");
    private static final By REG_FIRST_NAME = By.id("customer.firstName");
    private static final By REG_LAST_NAME = By.id("customer.lastName");
    private static final By REG_ADDRESS = By.id("customer.address.street");
    private static final By REG_CITY = By.id("customer.address.city");
    private static final By REG_STATE = By.id("customer.address.state");
    private static final By REG_ZIP_CODE = By.id("customer.address.zipCode");
    private static final By REG_PHONE = By.id("customer.phoneNumber");
    private static final By REG_SSN = By.id("customer.ssn");
    private static final By REG_USERNAME = By.id("customer.username");
    private static final By REG_PASSWORD = By.id("customer.password");
    private static final By REG_CONFIRM_PASSWORD = By.id("repeatedPassword");
    private static final By REGISTER_BUTTON = By.xpath("//input[@value='Register']");
    private static final By REG_SUCCESS_MESSAGE = By.xpath("//p[contains(text(),'Your account was created successfully')]");

    // Account Services
    private static final By OPEN_NEW_ACCOUNT_LINK = By.linkText("Open New Account");
    private static final By TRANSFER_FUNDS_LINK = By.linkText("Transfer Funds");
    private static final By ACCOUNT_TYPE_DROPDOWN = By.id("type");
    private static final By OPEN_NEW_ACCOUNT_BUTTON = By.xpath("//input[@value='Open New Account']");
    private static final By NEW_ACCOUNT_ID_LINK = By.id("newAccountId");
    private static final By TRANSFER_AMOUNT_INPUT = By.id("amount");
    private static final By FROM_ACCOUNT_DROPDOWN = By.id("fromAccountId");
    private static final By TO_ACCOUNT_DROPDOWN = By.id("toAccountId");
    private static final By TRANSFER_BUTTON = By.xpath("//input[@value='Transfer']");
    private static final By TRANSFER_COMPLETE_MESSAGE = By.xpath("//h1[text()='Transfer Complete!']");

    // Store account numbers for transfer
    private static String initialAccountId;
    private static String newAccountId;


    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments as strictly required
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
    @DisplayName("🧪 Test New User Registration")
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_LINK)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(REG_FIRST_NAME)).sendKeys(FIRST_NAME);
        driver.findElement(REG_LAST_NAME).sendKeys(LAST_NAME);
        driver.findElement(REG_ADDRESS).sendKeys("123 Test Street");
        driver.findElement(REG_CITY).sendKeys("Testville");
        driver.findElement(REG_STATE).sendKeys("TS");
        driver.findElement(REG_ZIP_CODE).sendKeys("12345");
        driver.findElement(REG_PHONE).sendKeys("555-1234");
        driver.findElement(REG_SSN).sendKeys("123-45-678");
        driver.findElement(REG_USERNAME).sendKeys(USERNAME);
        driver.findElement(REG_PASSWORD).sendKeys(PASSWORD);
        driver.findElement(REG_CONFIRM_PASSWORD).sendKeys(PASSWORD);
        driver.findElement(REGISTER_BUTTON).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(REG_SUCCESS_MESSAGE));
        Assertions.assertTrue(successMessage.isDisplayed(), "Registration success message should be visible.");
        Assertions.assertTrue(successMessage.getText().contains("Welcome " + USERNAME), "Welcome message should contain the new username.");
    }

    @Test
    @Order(2)
    @DisplayName("🧪 Test Login with Invalid and Valid Credentials, then Logout")
    void testLoginAndLogout() {
        // --- Invalid Login ---
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys("invalidUser");
        driver.findElement(PASSWORD_INPUT).sendKeys("invalidPassword");
        driver.findElement(LOGIN_BUTTON).click();
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_ERROR_MESSAGE));
        Assertions.assertTrue(errorMessage.getText().contains("An internal error has occurred"), "Error message for invalid login should be displayed.");

        // --- Valid Login ---
        driver.findElement(USERNAME_INPUT).clear();
        driver.findElement(USERNAME_INPUT).sendKeys(USERNAME);
        driver.findElement(PASSWORD_INPUT).clear();
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();
        WebElement accountsOverviewTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_OVERVIEW_TITLE));
        Assertions.assertTrue(accountsOverviewTitle.isDisplayed(), "Accounts Overview title should be visible after successful login.");

        // --- Logout ---
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        Assertions.assertTrue(driver.findElement(LOGIN_BUTTON).isDisplayed(), "Login button should be visible after logout.");
    }

    @Test
    @Order(3)
    @DisplayName("🧪 Test Opening a New Savings Account")
    void testOpenNewAccount() {
        // Log in first
        driver.get(BASE_URL);
        driver.findElement(USERNAME_INPUT).sendKeys(USERNAME);
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_OVERVIEW_TITLE));
        // Store the ID of the first account for the transfer test
        initialAccountId = driver.findElement(By.cssSelector("#accountTable tbody tr:first-child td:first-child a")).getText();

        wait.until(ExpectedConditions.elementToBeClickable(OPEN_NEW_ACCOUNT_LINK)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Open New Account']")));
        Select accountType = new Select(driver.findElement(ACCOUNT_TYPE_DROPDOWN));
        accountType.selectByVisibleText("SAVINGS");

        // Sometimes the 'from account' dropdown needs a moment to populate.
        // A small wait to ensure the default value is selected before clicking.
        wait.until(ExpectedConditions.textToBePresentInElementValue(By.id("fromAccountId"), initialAccountId));
        driver.findElement(OPEN_NEW_ACCOUNT_BUTTON).click();

        WebElement newAccountLink = wait.until(ExpectedConditions.visibilityOfElementLocated(NEW_ACCOUNT_ID_LINK));
        Assertions.assertTrue(newAccountLink.isDisplayed(), "Link to the new account ID should be displayed after creation.");
        newAccountId = newAccountLink.getText();
    }

    @Test
    @Order(4)
    @DisplayName("🧪 Test Fund Transfer Between Accounts")
    void testFundTransfer() {
        Assertions.assertNotNull(initialAccountId, "Initial account ID should have been set in the previous test.");
        Assertions.assertNotNull(newAccountId, "New account ID should have been set in the previous test.");

        wait.until(ExpectedConditions.elementToBeClickable(TRANSFER_FUNDS_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transfer Funds']")));

        driver.findElement(TRANSFER_AMOUNT_INPUT).sendKeys("100");
        Select fromAccount = new Select(driver.findElement(FROM_ACCOUNT_DROPDOWN));
        fromAccount.selectByValue(initialAccountId);
        Select toAccount = new Select(driver.findElement(TO_ACCOUNT_DROPDOWN));
        toAccount.selectByValue(newAccountId);

        driver.findElement(TRANSFER_BUTTON).click();

        WebElement transferComplete = wait.until(ExpectedConditions.visibilityOfElementLocated(TRANSFER_COMPLETE_MESSAGE));
        Assertions.assertTrue(transferComplete.isDisplayed(), "Transfer complete message should be displayed.");
    }
    
    @Test
    @Order(5)
    @DisplayName("🧪 Test External Footer Link")
    void testExternalLink() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_OVERVIEW_TITLE));
        String originalWindow = driver.getWindowHandle();
        
        WebElement visitUsLink = driver.findElement(By.linkText("www.parasoft.com"));
        // Scroll to footer to ensure visibility if needed
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", visitUsLink);
        wait.until(ExpectedConditions.elementToBeClickable(visitUsLink)).click();

        // Wait for the new tab and switch to it
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert on the new tab
        wait.until(ExpectedConditions.urlContains("parasoft.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "External link should navigate to parasoft.com.");
        
        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);

        // Assert we are back on the original page
        wait.until(ExpectedConditions.urlContains("parabank"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("parabank"), "Should have returned to the ParaBank site.");
    }
}