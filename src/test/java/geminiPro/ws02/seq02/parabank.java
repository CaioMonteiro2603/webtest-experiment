package geminiPro.ws02.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A complete JUnit 5 test suite for the ParaBank website using Selenium WebDriver
 * with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    // Constants for test data and configuration
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // WebDriver and WebDriverWait instances are static to be shared across all tests
    private static WebDriver driver;
    private static WebDriverWait wait;

    // A static variable to hold a newly created account ID to be used in subsequent tests
    private static String newAccountId;

    // --- WebDriver Lifecycle ---

    @BeforeAll
    static void setup() {
        // As per requirements, initialize Firefox in headless mode via arguments
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        // Start each test from a clean slate at the home/login page
        driver.get(BASE_URL);
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    @DisplayName("Test login with invalid credentials should show an error")
    void testInvalidLogin() {
        performLogin("invalidUser", "invalidPassword");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(error.getText().contains("The username and password could not be verified."),
                "Error message for invalid credentials was not correct.");
    }

    @Test
    @Order(2)
    @DisplayName("Test successful login and subsequent logout")
    void testSuccessfulLoginAndLogout() {
        performLogin(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement accountsOverviewTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Accounts Overview']")));
        Assertions.assertTrue(accountsOverviewTitle.isDisplayed(), "Accounts Overview title should be visible after login.");

        performLogout();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should be redirected to the login page after logout.");
    }

    @Test
    @Order(3)
    @DisplayName("Verify Accounts Overview table is displayed after login")
    void testAccountsOverviewIsDisplayed() {
        performLogin(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement accountTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountTable")));
        Assertions.assertTrue(accountTable.isDisplayed(), "The accounts table should be displayed on the overview page.");
    }

    @Test
    @Order(4)
    @DisplayName("Test opening a new checking account")
    void testOpenNewCheckingAccount() {
        performLogin(USERNAME, PASSWORD);
        navigateTo("Open New Account");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("type"))).click();
        new Select(driver.findElement(By.id("type"))).selectByVisibleText("CHECKING");

        // Click the button to open a new account
        driver.findElement(By.xpath("//input[@value='Open New Account']")).click();

        // Assert success message and capture the new account ID
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Account Opened!']")));
        WebElement newAccountIdElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newAccountId")));
        newAccountId = newAccountIdElement.getText();
        Assertions.assertNotNull(newAccountId, "A new account ID should be generated and displayed.");
        System.out.println("New Account Created: " + newAccountId); // For debugging purposes
    }

    @Test
    @Order(5)
    @DisplayName("Test transferring funds between accounts")
    void testTransferFunds() {
        Assumptions.assumeTrue(newAccountId != null, "Skipping test: requires a new account to have been created in the previous test.");
        performLogin(USERNAME, PASSWORD);
        
        // Get the first account ID from the main overview page to transfer from
        String fromAccountId = getFirstAccountId();
        Assertions.assertNotNull(fromAccountId, "Could not find a source account ID for transfer.");

        navigateTo("Transfer Funds");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount"))).sendKeys("100");
        new Select(driver.findElement(By.id("fromAccountId"))).selectByValue(fromAccountId);
        new Select(driver.findElement(By.id("toAccountId"))).selectByValue(newAccountId);

        driver.findElement(By.xpath("//input[@value='Transfer']")).click();

        // Assert success
        WebElement confirmationTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transfer Complete!']")));
        Assertions.assertTrue(confirmationTitle.isDisplayed(), "Transfer completion message was not displayed.");
        Assertions.assertTrue(driver.getPageSource().contains("$100.00"), "Transfer confirmation details are incorrect.");
    }

    @Test
    @Order(6)
    @DisplayName("Test updating user contact information")
    void testUpdateContactInfo() {
        performLogin(USERNAME, PASSWORD);
        navigateTo("Update Contact Info");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName"))).clear();
        driver.findElement(By.id("customer.firstName")).sendKeys("Caio"); // Re-enter or update data
        driver.findElement(By.id("customer.address.city")).clear();
        driver.findElement(By.id("customer.address.city")).sendKeys("Porto Ferreira");

        driver.findElement(By.xpath("//input[@value='Update Profile']")).click();

        WebElement confirmationTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Profile Updated']")));
        Assertions.assertTrue(confirmationTitle.isDisplayed(), "Profile Updated confirmation was not displayed.");
        Assertions.assertTrue(driver.getPageSource().contains("Your profile has been updated successfully."), "Profile update success text not found.");
    }

    @Test
    @Order(7)
    @DisplayName("Test finding a transaction by date")
    void testFindTransactionsByDate() {
        performLogin(USERNAME, PASSWORD);
        String accountId = getFirstAccountId();
        Assertions.assertNotNull(accountId, "Could not find an account ID to search transactions for.");

        navigateTo("Find Transactions");
        new Select(driver.findElement(By.id("accountId"))).selectByValue(accountId);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        driver.findElement(By.id("criteria.onDate")).sendKeys(today);

        // This is the button within the 'find by date' form
        driver.findElement(By.xpath("//div[@id='rightPanel']//button[text()='Find Transactions']")).click();

        wait.until(ExpectedConditions.urlContains("findtrans.htm"));
        WebElement resultsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transaction Results']")));
        Assertions.assertTrue(resultsTitle.isDisplayed(), "Transaction Results page was not displayed.");
    }

    @Test
    @Order(8)
    @DisplayName("Verify the 'About Us' internal page link")
    void testInternalAboutUsLink() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us"))).click();
        wait.until(ExpectedConditions.urlContains("about.htm"));
        WebElement aboutUsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='ParaSoft Demo Website']")));
        Assertions.assertTrue(aboutUsTitle.isDisplayed(), "'About Us' page title is not correct.");
    }

    // --- Helper Methods ---

    /** A reusable helper to perform login. */
    private void performLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.xpath("//input[@value='Log In']")).click();
    }

    /** A reusable helper to perform logout. */
    private void performLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
    }

    /** Navigates to a page using the left-side menu. */
    private void navigateTo(String linkText) {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText))).click();
    }

    /** Retrieves the first account ID from the Accounts Overview table. */
    private String getFirstAccountId() {
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        List<WebElement> accountLinks = driver.findElements(By.xpath("//table[@id='accountTable']//a"));
        if (accountLinks.isEmpty()) {
            return null;
        }
        return accountLinks.get(0).getText();
    }
}