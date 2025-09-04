package geminiPRO.ws02.seq08;

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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParaBankComprehensiveTest {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    // User credentials will be generated during the registration test
    private static String generatedUsername;
    private static String generatedPassword;
    private static String fromAccountId;

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);

        // Generate unique credentials for this test run
        long timestamp = System.currentTimeMillis();
        generatedUsername = "user" + timestamp;
        generatedPassword = "password123";
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    // Helper method for logging in
    private void login(String username, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
    }
    
    // Helper method for logging out
    private void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
    }

    @Test
    @Order(1)
    @DisplayName("Test New User Registration")
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName"))).sendKeys("Gemini");
        driver.findElement(By.id("customer.lastName")).sendKeys("Pro");
        driver.findElement(By.id("customer.address.street")).sendKeys("123 AI Lane");
        driver.findElement(By.id("customer.address.city")).sendKeys("Googleville");
        driver.findElement(By.id("customer.address.state")).sendKeys("CA");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("94043");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("555-0100");
        driver.findElement(By.id("customer.ssn")).sendKeys("123-456-7890");
        driver.findElement(By.id("customer.username")).sendKeys(generatedUsername);
        driver.findElement(By.id("customer.password")).sendKeys(generatedPassword);
        driver.findElement(By.id("repeatedPassword")).sendKeys(generatedPassword);
        driver.findElement(By.cssSelector("input[value='Register']")).click();

        WebElement welcomeTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("title")));
        assertTrue(welcomeTitle.getText().contains(generatedUsername), "Welcome title should contain the new username.");
        
        WebElement successMessage = driver.findElement(By.xpath("//p[contains(text(),'Your account was created successfully')]"));
        assertTrue(successMessage.isDisplayed(), "Success message should be displayed after registration.");
        
        // Capture the initial account ID for later tests
        WebElement accountLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#accountTable a.ng-binding")));
        fromAccountId = accountLink.getText();
        
        logout();
    }
    
    @Test
    @Order(2)
    @DisplayName("Test Login with Invalid Credentials")
    void testLoginWithInvalidCredentials() {
        login("invalidUser", "invalidPassword");
        WebElement errorTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        assertEquals("Error!", errorTitle.getText(), "Error title should be displayed for failed login.");
        
        WebElement errorMessage = driver.findElement(By.xpath("//p[contains(text(),'An internal error has occurred and has been logged.')]"));
        assertTrue(errorMessage.isDisplayed(), "A clear error message should be displayed.");
    }

    @Test
    @Order(3)
    @DisplayName("Test Successful Login and Logout")
    void testSuccessfulLoginAndLogout() {
        login(generatedUsername, generatedPassword);
        
        WebElement accountsOverviewTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Accounts Overview']")));
        assertTrue(accountsOverviewTitle.isDisplayed(), "Accounts Overview title should be visible after login.");
        
        logout();
        
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        assertTrue(usernameInput.isDisplayed(), "Username input should be visible after logout.");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test Open New Account")
    void testOpenNewAccount() {
        login(generatedUsername, generatedPassword);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account"))).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Open New Account']")));
        
        // Open a SAVINGS account
        Select accountType = new Select(driver.findElement(By.id("type")));
        accountType.selectByVisibleText("SAVINGS");
        
        // Wait for potential AJAX to complete before clicking
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Open New Account']"))).click();

        WebElement successTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Account Opened!']")));
        assertTrue(successTitle.isDisplayed(), "Account opened success title should be displayed.");
        
        WebElement newAccountIdLink = driver.findElement(By.id("newAccountId"));
        assertTrue(newAccountIdLink.isDisplayed(), "Link to new account ID should be visible.");
        
        logout();
    }

    @Test
    @Order(5)
    @DisplayName("Test Fund Transfer")
    void testTransferFunds() {
        login(generatedUsername, generatedPassword);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds"))).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transfer Funds']")));
        
        driver.findElement(By.id("amount")).sendKeys("10.00");
        
        Select fromAccountDropdown = new Select(driver.findElement(By.id("fromAccountId")));
        fromAccountDropdown.selectByIndex(0);
        
        Select toAccountDropdown = new Select(driver.findElement(By.id("toAccountId")));
        // Ensure there are at least two accounts to transfer between
        wait.until(d -> new Select(d.findElement(By.id("toAccountId"))).getOptions().size() > 1);
        toAccountDropdown.selectByIndex(1);
        
        driver.findElement(By.cssSelector("input[value='Transfer']")).click();
        
        WebElement resultTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transfer Complete!']")));
        assertTrue(resultTitle.isDisplayed(), "Transfer complete title should be shown.");
        
        logout();
    }
    
    @Test
    @Order(6)
    @DisplayName("Test Bill Payment")
    void testBillPayment() {
        login(generatedUsername, generatedPassword);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay"))).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("payee.name"))).sendKeys("Test Payee");
        driver.findElement(By.name("payee.address.street")).sendKeys("456 Bill St");
        driver.findElement(By.name("payee.address.city")).sendKeys("Payville");
        driver.findElement(By.name("payee.address.state")).sendKeys("TX");
        driver.findElement(By.name("payee.address.zipCode")).sendKeys("75001");
        driver.findElement(By.name("payee.phoneNumber")).sendKeys("555-0200");
        driver.findElement(By.name("payee.accountNumber")).sendKeys("98765");
        driver.findElement(By.name("verifyAccount")).sendKeys("98765");
        driver.findElement(By.name("amount")).sendKeys("50.00");
        
        new Select(driver.findElement(By.name("fromAccountId"))).selectByVisibleText(fromAccountId);
        
        driver.findElement(By.cssSelector("input[value='Send Payment']")).click();
        
        WebElement resultTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Bill Payment Complete')]")));
        assertTrue(resultTitle.isDisplayed(), "Bill payment complete title should be displayed.");
        assertTrue(driver.findElement(By.id("payeeName")).getText().contains("Test Payee"), "Payee name should be correct.");
        
        logout();
    }
    
    @Test
    @Order(7)
    @DisplayName("Test Find Transactions by Date")
    void testFindTransactionsByDate() {
        login(generatedUsername, generatedPassword);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions"))).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Find Transactions']")));
        
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        driver.findElement(By.id("criteria.onDate")).sendKeys(today);
        driver.findElement(By.xpath("(//button[text()='Find Transactions'])[1]")).click();
        
        WebElement resultsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transaction Results']")));
        assertTrue(resultsTitle.isDisplayed(), "Transaction results should be displayed.");
        
        List<WebElement> results = driver.findElements(By.cssSelector("#transactionTable > tbody > tr"));
        assertFalse(results.isEmpty(), "Transaction results table should not be empty.");
        
        logout();
    }

    @Test
    @Order(8)
    @DisplayName("Test Forum External Link")
    void testForumExternalLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Forum"))).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);
        
        assertTrue(wait.until(ExpectedConditions.urlContains("parasoft.com/forum")), "URL should contain parasoft.com/forum");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        assertTrue(driver.getTitle().contains("ParaBank"), "Should have switched back to the original ParaBank window.");
    }
}