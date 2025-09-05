package geminiPRO.ws02.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for parabank.parasoft.com using Selenium WebDriver with headless Firefox.
 * This suite covers user registration, login, account operations, transactions, and navigation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParaBankTest {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    // User credentials will be generated during the registration test to ensure idempotency.
    private static String registeredUsername;
    private static String registeredPassword = "password123";
    private static String customerId;
    private static String initialAccountId;
    private static String newAccountId;

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Setup and Teardown ---

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED headless mode via arguments
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // Generate a unique username for this test run
        registeredUsername = "testuser" + System.currentTimeMillis();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        driver.get(BASE_URL);
    }

    @AfterEach
    void logoutIfLoggedIn() {
        // This acts as a "reset state" to ensure each test starts fresh from the login page.
        try {
            if (driver.findElements(By.linkText("Log Out")).size() > 0) {
                driver.findElement(By.linkText("Log Out")).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            }
        } catch (Exception e) {
            // Ignore if logout link is not present or other errors occur during cleanup.
        }
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    void testRegisterNewUser() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName"))).sendKeys("Gemini");
        driver.findElement(By.id("customer.lastName")).sendKeys("Pro");
        driver.findElement(By.id("customer.address.street")).sendKeys("123 AI Lane");
        driver.findElement(By.id("customer.address.city")).sendKeys("Googleville");
        driver.findElement(By.id("customer.address.state")).sendKeys("CA");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("94043");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("555-0100");
        driver.findElement(By.id("customer.ssn")).sendKeys("987-65-4321");

        // Use the generated unique username
        driver.findElement(By.id("customer.username")).sendKeys(registeredUsername);
        driver.findElement(By.id("customer.password")).sendKeys(registeredPassword);
        driver.findElement(By.id("repeatedPassword")).sendKeys(registeredPassword);

        driver.findElement(By.cssSelector("input[value='Register']")).click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel p")));
        assertTrue(welcomeMessage.getText().contains("Your account was created successfully."), "Registration success message not found.");
        
        WebElement welcomeTitle = driver.findElement(By.className("title"));
        assertTrue(welcomeTitle.getText().contains("Welcome " + registeredUsername), "Welcome title is incorrect.");
    }
    
    @Test
    @Order(2)
    void testInvalidLogin() {
        performLogin("invalidUser", "wrongPassword");
        WebElement errorTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertEquals("The username and password could not be verified.", errorTitle.getText(), "Error message for invalid login is incorrect.");
    }
    
    @Test
    @Order(3)
    void testSuccessfulLogin() {
        performLogin(registeredUsername, registeredPassword);
        WebElement accountsOverviewTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        assertEquals("Accounts Overview", accountsOverviewTitle.getText(), "Not on the Accounts Overview page after login.");
        assertTrue(driver.findElement(By.linkText("Log Out")).isDisplayed(), "Logout link should be visible after login.");
    }
    
    @Test
    @Order(4)
    void testOpenNewAccount() {
        performLogin(registeredUsername, registeredPassword);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account"))).click();
        
        WebElement accountTypeDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("type")));
        new Select(accountTypeDropdown).selectByVisibleText("SAVINGS");
        
        // Capture the first account ID to transfer from
        initialAccountId = driver.findElement(By.cssSelector("#fromAccountId option")).getAttribute("value");

        driver.findElement(By.cssSelector("input.button[value='Open New Account']")).click();

        WebElement successTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        assertEquals("Account Opened!", successTitle.getText(), "Success message for opening an account did not appear.");
        
        WebElement newAccountIdElement = driver.findElement(By.id("newAccountId"));
        assertTrue(newAccountIdElement.isDisplayed(), "New account ID link should be visible.");
        newAccountId = newAccountIdElement.getText(); // Save for the next test
    }
    
    @Test
    @Order(5)
    void testFundTransfer() {
        // This test depends on the previous test to have created a new account.
        if (initialAccountId == null || newAccountId == null) {
            fail("Cannot run fund transfer test because account IDs were not captured from the previous test.");
        }
        
        performLogin(registeredUsername, registeredPassword);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount"))).sendKeys("50.25");
        new Select(driver.findElement(By.id("fromAccountId"))).selectByValue(initialAccountId);
        new Select(driver.findElement(By.id("toAccountId"))).selectByValue(newAccountId);

        driver.findElement(By.cssSelector("input.button[value='Transfer']")).click();
        
        WebElement successTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        assertEquals("Transfer Complete!", successTitle.getText(), "Transfer success message is incorrect.");
        
        WebElement amountTransferred = driver.findElement(By.id("amount"));
        assertEquals("$50.25", amountTransferred.getText(), "Amount transferred displayed on confirmation page is incorrect.");
    }

    @Test
    @Order(6)
    void testFindTransactionByDate() {
        performLogin(registeredUsername, registeredPassword);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions"))).click();

        WebElement findByDateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("criteria.onDate")));
        // Use today's date for the search
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        findByDateInput.sendKeys(today);
        
        // Click the corresponding find button for this input field
        driver.findElement(By.cssSelector("button[ng-click=\"criteria.searchType = 'DATE'\"]")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        WebElement resultsTitle = driver.findElement(By.cssSelector("h1.title"));
        assertEquals("Transaction Results", resultsTitle.getText(), "Did not navigate to transaction results page.");
    }
    
    @Test
    @Order(7)
    void testFooterExternalLinks() {
        // No login required for this test
        handleExternalLink(driver.findElement(By.linkText("www.parasoft.com")), "parasoft.com");
        handleExternalLink(driver.findElement(By.linkText("forums.parasoft.com")), "forums.parasoft.com");
    }

    // --- Helper Methods ---

    /**
     * Performs login action.
     * @param username The username to use.
     * @param password The password to use.
     */
    private void performLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
    }
    
    /**
     * Handles clicking an external link, verifying the new tab's URL, closing it, and returning control.
     * @param linkElement The WebElement of the link to click.
     * @param expectedDomain The domain expected in the new tab's URL (e.g., "parasoft.com").
     */
    private void handleExternalLink(WebElement linkElement, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(linkElement)).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        
        if (newWindow == null) {
            fail("New window did not open for link with expected domain: " + expectedDomain);
        }
        
        driver.switchTo().window(newWindow);
        // Wait for URL to contain the domain, as page load might take time.
        wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL of the new tab should contain " + expectedDomain);
        driver.close();
        
        driver.switchTo().window(originalWindow);
        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
    }
}