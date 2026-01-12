package geminiPro.ws02.seq07;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A comprehensive JUnit 5 test suite for the ParaBank website.
 * This suite covers user registration, login, account services like opening new accounts,
 * transferring funds, and viewing account activity.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String PASSWORD = "123";
    // Use a unique username for each test run to ensure idempotency
    private static final String USERNAME = "caiogemini" + System.currentTimeMillis();
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static String newAccountId; // To store the ID of a newly created account

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments for headless mode as required
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
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
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    void testUserRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register"))).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName"))).sendKeys("Caio");
        driver.findElement(By.id("customer.lastName")).sendKeys("Gemini");
        driver.findElement(By.id("customer.address.street")).sendKeys("123 Test St");
        driver.findElement(By.id("customer.address.city")).sendKeys("Testville");
        driver.findElement(By.id("customer.address.state")).sendKeys("TS");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("12345");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("555-1234");
        driver.findElement(By.id("customer.ssn")).sendKeys("123-456-7890");

        // Unique credentials
        driver.findElement(By.id("customer.username")).sendKeys(USERNAME);
        driver.findElement(By.id("customer.password")).sendKeys(PASSWORD);
        driver.findElement(By.id("repeatedPassword")).sendKeys(PASSWORD);

        driver.findElement(By.xpath("//input[@value='Register']")).click();

        WebElement welcomeTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Welcome')]")));
        assertEquals("Welcome " + USERNAME, welcomeTitle.getText(), "Registration should be successful and show a welcome message.");

        WebElement welcomeText = driver.findElement(By.xpath("//p[contains(text(), 'Your account was created successfully')]"));
        assertTrue(welcomeText.isDisplayed(), "Confirmation text for successful registration should be visible.");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        // Ensure logged out from previous test
        if (isLoggedIn()) {
            performLogout();
        }
        performLogin(USERNAME, "wrong_password");
        WebElement errorTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        assertEquals("The username and password could not be verified.", errorTitle.getText().trim(), "Error message for invalid login should be displayed.");
    }

    @Test
    @Order(3)
    void testSuccessfulLoginAndLogout() {
        performLogin(USERNAME, PASSWORD);
        WebElement accountsOverviewTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Accounts Overview')]")));
        assertTrue(accountsOverviewTitle.isDisplayed(), "User should be redirected to the Accounts Overview page.");
        assertTrue(isLoggedIn(), "Login status check should return true.");

        performLogout();
        WebElement loginPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
        assertTrue(loginPanel.isDisplayed(), "User should be returned to the home page with login panel after logout.");
    }

    @Test
    @Order(4)
    void testOpenNewAccount() {
        ensureLoggedIn();
        driver.findElement(By.linkText("Open New Account")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("type")));
        Select accountTypeDropdown = new Select(driver.findElement(By.id("type")));
        accountTypeDropdown.selectByVisibleText("SAVINGS");

        // Wait for the 'from account' dropdown to be populated and select the first option
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("//select[@id='fromAccountId']/option"), 0));
        Select fromAccountDropdown = new Select(driver.findElement(By.id("fromAccountId")));
        fromAccountDropdown.selectByIndex(0);
        
        driver.findElement(By.xpath("//input[@value='Open New Account']")).click();

        WebElement confirmationTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Account Opened!']")));
        assertTrue(confirmationTitle.isDisplayed(), "Confirmation title for new account should be visible.");

        WebElement newAccountIdElement = driver.findElement(By.id("newAccountId"));
        newAccountId = newAccountIdElement.getText();
        assertNotNull(newAccountId, "A new account ID should be generated and displayed.");

        newAccountIdElement.click();
        wait.until(ExpectedConditions.urlContains("activity.htm?id=" + newAccountId));
        WebElement accountDetailsTitle = driver.findElement(By.xpath("//h1[text()='Account Details']"));
        assertTrue(accountDetailsTitle.isDisplayed(), "Clicking new account ID should lead to the account details page.");
    }

    @Test
    @Order(5)
    void testFundTransfer() {
        ensureLoggedIn();
        driver.findElement(By.linkText("Transfer Funds")).click();

        WebElement amountField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        amountField.sendKeys("150");

        Select fromAccountDropdown = new Select(driver.findElement(By.id("fromAccountId")));
        fromAccountDropdown.selectByIndex(0);
       
        
        Select toAccountDropdown = new Select(driver.findElement(By.id("toAccountId")));
        // Select the other account. If there's only one, this will fail as expected.
        toAccountDropdown.selectByIndex(1);

        driver.findElement(By.xpath("//input[@value='Transfer']")).click();
        
        WebElement transferCompleteTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transfer Complete!']")));
        assertTrue(transferCompleteTitle.isDisplayed(), "Transfer complete message should be shown.");
        
        WebElement amountText = driver.findElement(By.id("amount"));
        assertEquals("$150.00", amountText.getText(), "Transferred amount should be correctly displayed on confirmation.");
    }

    @Test
    @Order(6)
    void testAccountActivityCheck() {
        ensureLoggedIn();
        driver.findElement(By.linkText("Accounts Overview")).click();
        
        // Click on the first account in the overview table
        WebElement firstAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//table[@id='accountTable']//a")));
        firstAccountLink.click();
        
        WebElement accountDetailsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Account Details']")));
        assertTrue(accountDetailsTitle.isDisplayed(), "Should navigate to the Account Details page.");

        WebElement transactionTable = driver.findElement(By.id("transactionTable"));
        assertTrue(transactionTable.isDisplayed(), "Transaction table should be visible on the account details page.");
    }

    @Test
    @Order(7)
    void testUpdateContactInfo() {
        ensureLoggedIn();
        driver.findElement(By.linkText("Update Contact Info")).click();

        WebElement phoneField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.phoneNumber")));
        String newPhoneNumber = "555-9876";
        phoneField.clear();
        phoneField.sendKeys(newPhoneNumber);

        driver.findElement(By.xpath("//input[@value='Update Profile']")).click();

        WebElement successTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Profile Updated']")));
        assertTrue(successTitle.isDisplayed(), "Confirmation for profile update should be visible.");
    }
    
    @Test
    @Order(8)
    void testExternalForumLink() {
        ensureLoggedIn();
        String originalWindow = driver.getWindowHandle();
        
        driver.findElement(By.linkText("Forum")).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("parasoft.com/forum"));
        assertTrue(driver.getCurrentUrl().contains("parasoft.com/forum/"), "URL of new window should contain the forum domain.");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        assertTrue(driver.getCurrentUrl().contains("parabank/overview.htm"), "Should have returned to the ParaBank accounts page.");
    }
    
    // --- Helper Methods ---

    private void performLogin(String user, String pass) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(user);
        driver.findElement(By.name("password")).sendKeys(pass);
        driver.findElement(By.xpath("//input[@value='Log In']")).click();
    }
    
    private void ensureLoggedIn() {
        if (!isLoggedIn()) {
            performLogin(USERNAME, PASSWORD);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Accounts Overview')]")));
        }
    }

    private boolean isLoggedIn() {
        // A reliable way to check for login is the presence of the "Log Out" link
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Log Out"));
        return !logoutLinks.isEmpty();
    }

    private void performLogout() {
        try {
            WebElement logoutLink = driver.findElement(By.linkText("Log Out"));
            wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
        } catch (NoSuchElementException e) {
            // Already logged out or on a page without the link, do nothing.
        }
    }
}