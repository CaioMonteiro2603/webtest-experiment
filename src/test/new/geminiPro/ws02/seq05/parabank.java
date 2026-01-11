package geminiPro.ws02.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * JUnit 5 test suite for parabank.parasoft.com using Selenium WebDriver with headless Firefox.
 * This suite covers user registration, login, account management, fund transfers, and external link validation.
 * It creates a new user for each full test run to ensure test independence and state consistency.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // A unique user is generated during the registration test to ensure a clean state for each test suite run.
    private static String testUsername;
    private static String testPassword;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String FIRST_NAME = "Gemini";
    private static final String LAST_NAME = "Pro";

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Generate unique credentials for this test run
        long timestamp = System.currentTimeMillis();
        testUsername = "gemini_user_" + timestamp;
        testPassword = "password123";
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setup() {
        driver.get(BASE_URL);
    }

    /**
     * Helper method to log in using the dynamically generated test credentials.
     */
    private void performLogin() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(testUsername);
        driver.findElement(By.name("password")).sendKeys(testPassword);
        driver.findElement(By.cssSelector("input.button[value='Log In']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1.title")));
    }
    
    /**
     * Helper method to log out of the application.
     */
    private void performLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }
    
    /**
     * Helper method to parse currency strings like "$123.45" into a Double.
     * @param amount The string amount to parse.
     * @return The amount as a Double.
     */
    private double parseCurrency(String amount) {
        return Double.parseDouble(amount.replace("$", "").replace(",", ""));
    }

    @Test
    @Order(1)
    @DisplayName("Test new user registration")
    void testUserRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName")));

        // Fill out the registration form
        driver.findElement(By.id("customer.firstName")).sendKeys(FIRST_NAME);
        driver.findElement(By.id("customer.lastName")).sendKeys(LAST_NAME);
        driver.findElement(By.id("customer.address.street")).sendKeys("123 Test St");
        driver.findElement(By.id("customer.address.city")).sendKeys("Testville");
        driver.findElement(By.id("customer.address.state")).sendKeys("TS");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("12345");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("555-1234");
        driver.findElement(By.id("customer.ssn")).sendKeys("123-456-7890");
        driver.findElement(By.id("customer.username")).sendKeys(testUsername);
        driver.findElement(By.id("customer.password")).sendKeys(testPassword);
        driver.findElement(By.id("repeatedPassword")).sendKeys(testPassword);
        driver.findElement(By.cssSelector("input.button[value='Register']")).click();

        // Verify registration success and capture customer ID
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#rightPanel p")));
        Assertions.assertTrue(successMessage.getText().contains("Your account was created successfully."), "Success message not found.");
        
        // Extract customer ID for potential future use (optional)
        String welcomeText = driver.findElement(By.cssSelector("div#rightPanel h1")).getText();
        Assertions.assertEquals("Welcome " + testUsername, welcomeText, "Welcome message is not correct.");
        
        performLogout();
    }
    
    @Test
    @Order(2)
    @DisplayName("Test invalid login with wrong credentials")
    void testInvalidLogin() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("invalidUser");
        driver.findElement(By.name("password")).sendKeys("invalidPassword");
        driver.findElement(By.cssSelector("input.button[value='Log In']")).click();
        
        WebElement errorTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error h1")));
        Assertions.assertEquals("Error!", errorTitle.getText(), "Error title is incorrect.");
        Assertions.assertTrue(driver.getPageSource().contains("The username and password could not be verified."), "Error message is incorrect.");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test successful login and subsequent logout")
    void testSuccessfulLoginAndLogout() {
        Assumptions.assumeTrue(testUsername != null, "User registration must succeed for this test to run.");
        performLogin();

        WebElement overviewTitle = driver.findElement(By.cssSelector("h1.title"));
        Assertions.assertEquals("Accounts Overview", overviewTitle.getText(), "Not on the Accounts Overview page after login.");
        
        WebElement welcomeMessage = driver.findElement(By.className("smallText"));
        Assertions.assertTrue(welcomeMessage.getText().contains(FIRST_NAME) && welcomeMessage.getText().contains(LAST_NAME), "Welcome message is incorrect.");

        performLogout();
        Assertions.assertTrue(driver.findElement(By.name("username")).isDisplayed(), "Login form should be visible after logout.");
    }

    @Test
    @Order(4)
    @DisplayName("Test opening a new savings account")
    void testOpenNewAccount() {
        Assumptions.assumeTrue(testUsername != null, "User registration must succeed for this test to run.");
        performLogin();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='openaccount']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("type")));

        Select accountType = new Select(driver.findElement(By.id("type")));
        accountType.selectByVisibleText("SAVINGS");

        // Click the button to open the account
        driver.findElement(By.cssSelector("input.button[value='Open New Account']")).click();

        WebElement successTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertEquals("Account Opened!", successTitle.getText(), "Account opening success message not displayed.");
        
        WebElement newAccountIdLink = driver.findElement(By.id("newAccountId"));
        Assertions.assertTrue(newAccountIdLink.isDisplayed(), "New Account ID link should be visible.");
        
        performLogout();
    }

    @Test
    @Order(5)
    @DisplayName("Test fund transfer between accounts and verify balances")
    void testFundTransferAndVerify() {
        Assumptions.assumeTrue(testUsername != null, "User registration must succeed for this test to run.");
        performLogin();

        // Go to accounts overview to get initial data
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='overview']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountTable")));

        List<WebElement> accountRows = driver.findElements(By.cssSelector("#accountTable tbody tr"));
        // This test requires at least two accounts to function. The registration and new account test should provide them.
        Assumptions.assumeTrue(accountRows.size() >= 2, "Fund transfer test requires at least two accounts.");

        String fromAccountId = accountRows.get(0).findElement(By.cssSelector("td:nth-child(1) a")).getText();
        String toAccountId = accountRows.get(1).findElement(By.cssSelector("td:nth-child(1) a")).getText();
        double fromAccountInitialBalance = parseCurrency(accountRows.get(0).findElement(By.cssSelector("td:nth-child(2)")).getText());
        double toAccountInitialBalance = parseCurrency(accountRows.get(1).findElement(By.cssSelector("td:nth-child(2)")).getText());
        double transferAmount = 50.55;

        // Navigate to Transfer Funds
        driver.findElement(By.cssSelector("a[href*='transfer']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));

        driver.findElement(By.id("amount")).sendKeys(String.valueOf(transferAmount));
        new Select(driver.findElement(By.id("fromAccountId"))).selectByValue(fromAccountId);
        new Select(driver.findElement(By.id("toAccountId"))).selectByValue(toAccountId);
        driver.findElement(By.cssSelector("input.button[value='Transfer']")).click();
        
        // Verify transfer success
        WebElement resultTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertEquals("Transfer Complete!", resultTitle.getText(), "Transfer did not complete successfully.");
        
        // Go back to Accounts Overview and verify balances
        driver.findElement(By.cssSelector("a[href*='overview']")).click();
        wait.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOfElementLocated(By.id("accountTable"))));
        
        List<WebElement> updatedAccountRows = driver.findElements(By.cssSelector("#accountTable tbody tr"));
        double fromAccountFinalBalance = parseCurrency(updatedAccountRows.get(0).findElement(By.cssSelector("td:nth-child(2)")).getText());
        double toAccountFinalBalance = parseCurrency(updatedAccountRows.get(1).findElement(By.cssSelector("td:nth-child(2)")).getText());

        Assertions.assertEquals(fromAccountInitialBalance - transferAmount, fromAccountFinalBalance, 0.01, "From Account balance is incorrect.");
        Assertions.assertEquals(toAccountInitialBalance + transferAmount, toAccountFinalBalance, 0.01, "To Account balance is incorrect.");

        performLogout();
    }

    @Test
    @Order(6)
    @DisplayName("Verify external 'About Us' link opens correctly")
    void testExternalAboutUsLink() {
        String originalWindow = driver.getWindowHandle();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='about']"))).click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = new ArrayList<>(allWindows).get(0);
        
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("parabank.parasoft.com")));
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("www.parasoft.com"), "New window URL should be the Parasoft 'About Us' page.");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Driver should have switched back to the original window.");
        Assertions.assertTrue(driver.getTitle().contains("ParaBank"), "Should be back on the ParaBank site.");
    }
}