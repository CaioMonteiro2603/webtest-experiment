package geminiPro.ws02.seq06;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for parabank.parasoft.com using Selenium WebDriver with headless Firefox.
 * This suite covers user registration, login, account overview, fund transfers,
 * transaction searching, and external link validation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // Credentials will be set dynamically during the registration test
    private static String testUsername;
    private static String testPassword = "password123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private static final By USERNAME_INPUT = By.name("username");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.cssSelector("input.button[value='Log In']");
    private static final By REGISTER_LINK = By.linkText("Register");
    private static final By LOGOUT_LINK = By.linkText("Log Out");
    private static final By ACCOUNTS_OVERVIEW_TITLE = By.xpath("//h1[text()='Accounts Overview']");

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use headless mode via arguments ONLY
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        // Generate a unique username for this test run to ensure idempotency
        testUsername = "geminiUser" + System.currentTimeMillis();
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
    }

    /**
     * Tests the user registration process. A new user is created for subsequent tests.
     */
    @Test
    @Order(1)
    void userRegistrationTest() {
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customerForm")));

        // Fill out registration form
        driver.findElement(By.id("customer.firstName")).sendKeys("Gemini");
        driver.findElement(By.id("customer.lastName")).sendKeys("Pro");
        driver.findElement(By.id("customer.address.street")).sendKeys("123 AI Lane");
        driver.findElement(By.id("customer.address.city")).sendKeys("Googleville");
        driver.findElement(By.id("customer.address.state")).sendKeys("CA");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("94043");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("555-1234");
        driver.findElement(By.id("customer.ssn")).sendKeys("123-456-7890");
        driver.findElement(By.id("customer.username")).sendKeys(testUsername);
        driver.findElement(By.id("customer.password")).sendKeys(testPassword);
        driver.findElement(By.id("repeatedPassword")).sendKeys(testPassword);

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button[value='Register']"))).click();

        // Assert successful registration
        WebElement welcomeTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@class='title']")));
        assertEquals("Welcome " + testUsername, welcomeTitle.getText(), "Welcome message after registration is incorrect.");
        assertTrue(driver.findElement(By.xpath("//p[contains(text(),'Your account was created successfully.')]")).isDisplayed(), "Success message should be displayed.");
    }

    /**
     * Tests both failed and successful login scenarios using the user created in the first test.
     */
    @Test
    @Order(2)
    void loginFunctionalityTest() {
        // Log out first, as registration causes an auto-login
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();

        // Test Case 1: Failed Login (wrong password)
        performLogin(testUsername, "wrongpassword");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        assertTrue(error.getText().contains("The username and password could not be verified."), "Error message for failed login is incorrect.");

        // Test Case 2: Successful Login
        performLogin(testUsername, testPassword);
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_OVERVIEW_TITLE)).isDisplayed(), "Accounts Overview title should be visible after successful login.");
        WebElement welcomeMessage = driver.findElement(By.xpath("//p[@class='smallText']"));
        assertTrue(welcomeMessage.getText().contains("Welcome"), "Welcome message on home page is missing or incorrect.");
    }


    /**
     * Performs a fund transfer between two accounts and verifies the result.
     */
    @Test
    @Order(3)
    void fundTransferTest() {
        loginAsTestUser();

        // This test requires at least two accounts. Let's create one.
        openNewAccount("SAVINGS");
        driver.findElement(By.linkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNTS_OVERVIEW_TITLE));

        // Get account IDs for transfer
        List<WebElement> accountLinks = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("//table[@id='accountTable']//a"), 1));
        String fromAccountId = accountLinks.get(0).getText();
        String toAccountId = accountLinks.get(1).getText();
        
        // Navigate to Transfer Funds
        driver.findElement(By.linkText("Transfer Funds")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transfer Funds']")));
        
        // Perform the transfer
        driver.findElement(By.id("amount")).sendKeys("150.00");
        new Select(driver.findElement(By.id("fromAccountId"))).selectByVisibleText(fromAccountId);
        new Select(driver.findElement(By.id("toAccountId"))).selectByVisibleText(toAccountId);
        
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button[value='Transfer']"))).click();
        
        // Assert success
        WebElement resultHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@class='title' and text()='Transfer Complete!']")));
        assertTrue(resultHeader.isDisplayed(), "Transfer completion message not found.");
        WebElement amountTransferred = driver.findElement(By.id("amount"));
        assertEquals("150.00", amountTransferred.getText(), "Amount shown on confirmation page is incorrect.");
    }
    
    /**
     * Tests the "Find Transactions" feature by searching for a transaction by amount.
     */
    @Test
    @Order(4)
    void findTransactionsTest() {
        loginAsTestUser();
        // A transfer was just made for $150.00, let's find it.
        driver.findElement(By.linkText("Find Transactions")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Find Transactions']")));

        // Select the first account
        WebElement accountDropdown = driver.findElement(By.id("accountId"));
        Select accountSelect = new Select(accountDropdown);
        wait.until(d -> accountSelect.getOptions().size() > 1); // Wait for dropdown to be populated
        accountSelect.selectByIndex(0);

        // Find by amount
        driver.findElement(By.id("criteria.amount")).sendKeys("150.00");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(.,'Find Transactions')]"))).click();
        
        // Assert results
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transaction Results']")));
        List<WebElement> results = driver.findElements(By.xpath("//table[@id='transactionResults']//tr"));
        assertTrue(results.size() > 1, "Should find at least one transaction result for the transfer.");
    }
    
    /**
     * Verifies the external links in the page footer.
     */
    @Test
    @Order(5)
    void externalFooterLinksTest() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("footerPanel")));
        
        verifyExternalLink(driver.findElement(By.linkText("www.parasoft.com")), "parasoft.com");
    }

    // --- Helper Methods ---

    private void performLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }

    private void loginAsTestUser() {
        if (testUsername == null) {
            fail("Cannot log in; user was not created in registration test.");
        }
        try {
            driver.findElement(LOGOUT_LINK);
        } catch (NoSuchElementException e) {
            performLogin(testUsername, testPassword);
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }
    
    private void openNewAccount(String accountType) {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Open New Account']")));
        Select typeSelect = new Select(driver.findElement(By.id("type")));
        if ("SAVINGS".equalsIgnoreCase(accountType)) {
            typeSelect.selectByValue("1");
        } else {
            typeSelect.selectByValue("0"); // CHECKING
        }
        
        // Wait for ajax to load account list before clicking
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.xpath("//select[@id='fromAccountId']/option"), 0));

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button[value='Open New Account']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Account Opened!']")));
    }

    private void verifyExternalLink(WebElement linkElement, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(linkElement)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL of the new tab should contain " + expectedDomain);

        driver.close();
        driver.switchTo().window(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("parabank.parasoft.com"), "Should have switched back to the original window.");
    }
}