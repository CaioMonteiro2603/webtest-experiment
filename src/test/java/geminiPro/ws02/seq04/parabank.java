package geminiPro.ws02.seq04;

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
import java.util.Set;

/**
 * JUnit 5 test suite for the Parabank site using Selenium WebDriver in Headless Firefox mode.
 * Covers login, key features (one level below base), and external links.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN_USER = "caio@gmail.com";
    private static final String LOGIN_PASS = "123";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final String ACCOUNTS_OVERVIEW_HEADER_TEXT = "Accounts Overview";

    /**
     * Initializes the WebDriver (Firefox headless) and WebDriverWait once before all tests.
     */
    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        // Use addArguments for headless mode as required
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        driver.manage().window().maximize(); // Maximizing helps with consistent element locations
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

    /**
     * Closes the WebDriver after all tests have completed.
     */
    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Navigates to the base URL and waits for the login form to be visible.
     */
    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    /**
     * Logs in with the predefined credentials. Assumes on the login page.
     */
    private void login() {
        navigateToHome();
        wait.until(ExpectedConditions.elementToBeClickable(By.name("username"))).sendKeys(LOGIN_USER);
        driver.findElement(By.name("password")).sendKeys(LOGIN_PASS);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']"))).click();

        // Wait for successful login by checking for the Accounts Overview header
        wait.until(ExpectedConditions.textToBe(By.tagName("h1"), ACCOUNTS_OVERVIEW_HEADER_TEXT));
    }

    /**
     * Helper to perform the DB reset via the Admin Page.
     * Assumes logged in.
     */
    private void resetAppState() {
        // 1. Navigate to Admin Page
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page"))).click();
        wait.until(ExpectedConditions.urlContains("admin.htm"));

        // 2. Click the Clean button (Database Cleanup)
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[value='CLEAN']"))).click();

        // 3. Assert success message and navigate back to home (login page)
        wait.until(ExpectedConditions.textToBe(By.cssSelector("body"), "Database Cleaned"));
        navigateToHome();
    }

    /**
     * Handles external link testing: switches window, asserts URL, closes window, switches back.
     * @param linkElement The WebElement to click (the external link).
     * @param expectedDomain The expected domain name (e.g., "parasoft.com").
     * @param originalHandle The handle of the initial window.
     */
    private void assertLinkExternal(WebElement linkElement, String expectedDomain, String originalHandle) {
        linkElement.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindowHandles = driver.getWindowHandles();
        String newWindowHandle = allWindowHandles.stream()
                .filter(handle -> !handle.equals(originalHandle))
                .findFirst()
                .orElseThrow(() -> new AssertionError("New window handle not found."));

        driver.switchTo().window(newWindowHandle);
        try {
            // Wait for the new page to load and assert the URL contains the expected domain
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link URL does not contain expected domain: " + expectedDomain);
        } finally {
            // Close the new window/tab and switch back to the original one
            driver.close();
            driver.switchTo().window(originalHandle);
            wait.until(ExpectedConditions.urlContains(BASE_URL.substring(0, BASE_URL.lastIndexOf('/')))); // Wait for the original window to be active
        }
    }

    /**
     * Order 1: Test valid login credentials and assert successful navigation to Accounts Overview.
     */
    @Test
    @Order(1)
    public void testValidLoginAndLogout() {
        navigateToHome();
        
        // Ensure database is in clean state
        // First login to access admin page
        wait.until(ExpectedConditions.elementToBeClickable(By.name("username"))).sendKeys(LOGIN_USER);
        driver.findElement(By.name("password")).sendKeys(LOGIN_PASS);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']"))).click();
        wait.until(ExpectedConditions.textToBe(By.tagName("h1"), ACCOUNTS_OVERVIEW_HEADER_TEXT));
        
        // Reset app state
        resetAppState();
        
        // --- Login ---
        wait.until(ExpectedConditions.elementToBeClickable(By.name("username"))).sendKeys(LOGIN_USER);
        driver.findElement(By.name("password")).sendKeys(LOGIN_PASS);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']"))).click();

        // Assert successful login
        WebElement accountsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals(ACCOUNTS_OVERVIEW_HEADER_TEXT, accountsHeader.getText(), "Expected Accounts Overview page after login.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "URL should contain overview.htm after successful login.");
        
        // --- Logout ---
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
        
        // Assert successful logout (back to login page)
        wait.until(ExpectedConditions.urlContains("index.htm"));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Login button should be visible after logout.");
    }

    /**
     * Order 2: Test invalid login credentials and assert the error message.
     */
    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigateToHome();

        wait.until(ExpectedConditions.elementToBeClickable(By.name("username"))).sendKeys("bad_user");
        driver.findElement(By.name("password")).sendKeys("bad_pass");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']"))).click();

        // Assert error message
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.error")));
        Assertions.assertEquals("The username and password could not be verified.", error.getText(), "Expected invalid login error message.");
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("index.htm"), "URL should remain index.htm after failed login.");
    }

    /**
     * Order 3: Test Open New Account functionality.
     */
    @Test
    @Order(3)
    public void testOpenNewAccount() {
        navigateToHome();
        // Start clean, log in
        wait.until(ExpectedConditions.elementToBeClickable(By.name("username"))).sendKeys(LOGIN_USER);
        driver.findElement(By.name("password")).sendKeys(LOGIN_PASS);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']"))).click();
        
        // Wait for successful login
        try {
            wait.until(ExpectedConditions.textToBe(By.tagName("h1"), ACCOUNTS_OVERVIEW_HEADER_TEXT));
        } catch (Exception e) {
            // If we get "Error!" as header, try to logout and login again
            if (driver.findElement(By.tagName("h1")).getText().equals("Error!")) {
                wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
                wait.until(ExpectedConditions.urlContains("index.htm"));
                
                // Try login again
                wait.until(ExpectedConditions.elementToBeClickable(By.name("username"))).sendKeys(LOGIN_USER);
                driver.findElement(By.name("password")).sendKeys(LOGIN_PASS);
                wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']"))).click();
                wait.until(ExpectedConditions.textToBe(By.tagName("h1"), ACCOUNTS_OVERVIEW_HEADER_TEXT));
            }
        }

        // Navigate to 'Open New Account'
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account"))).click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));

        // Select 'CHECKING' and the first existing account
        WebElement accountTypeDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("type")));
        Select accountType = new Select(accountTypeDropdown);
        accountType.selectByVisibleText("CHECKING");

        // Click Open New Account button
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Open New Account']"))).click();

        // Assert success
        WebElement confirmationHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("Account Opened!", confirmationHeader.getText(), "Expected 'Account Opened!' confirmation.");
        
        // Logout to clean up session for next test
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
    }

    /**
     * Order 4: Test Transfer Funds functionality.
     */
    @Test
    @Order(4)
    public void testTransferFunds() {
        navigateToHome();
        // Start clean, log in
        wait.until(ExpectedConditions.elementToBeClickable(By.name("username"))).sendKeys(LOGIN_USER);
        driver.findElement(By.name("password")).sendKeys(LOGIN_PASS);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']"))).click();
        
        // Wait for successful login
        try {
            wait.until(ExpectedConditions.textToBe(By.tagName("h1"), ACCOUNTS_OVERVIEW_HEADER_TEXT));
        } catch (Exception e) {
            // If we get "Error!" as header, try to logout and login again
            if (driver.findElement(By.tagName("h1")).getText().equals("Error!")) {
                wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
                wait.until(ExpectedConditions.urlContains("index.htm"));
                
                // Try login again
                wait.until(ExpectedConditions.elementToBeClickable(By.name("username"))).sendKeys(LOGIN_USER);
                driver.findElement(By.name("password")).sendKeys(LOGIN_PASS);
                wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']"))).click();
                wait.until(ExpectedConditions.textToBe(By.tagName("h1"), ACCOUNTS_OVERVIEW_HEADER_TEXT));
            }
        }

        // Navigate to 'Transfer Funds'
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds"))).click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));

        // Enter amount and select accounts (assuming at least two exist)
        wait.until(ExpectedConditions.elementToBeClickable(By.id("amount"))).sendKeys("10.00");
        
        // Select accounts - relying on default options or first two if multiple exist
        // Note: The specific account IDs are dynamic, relying on the default selection or first available.
        
        // Click Transfer button
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Transfer']"))).click();

        // Assert success
        WebElement confirmationHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("Transfer Complete!", confirmationHeader.getText(), "Expected 'Transfer Complete!' confirmation.");
        
        // Assert transaction details are displayed (e.g., amount transferred)
        WebElement amountCell = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        Assertions.assertTrue(amountCell.getText().contains("$10.00"), "Transaction summary should show transferred amount.");
        
        // Logout to clean up session for next test
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
    }
    
    /**
     * Order 5: Test the external links present on the home and logged-in pages.
     */
    @Test
    @Order(5)
    public void testExternalLinks() {
        navigateToHome();
        String originalHandle = driver.getWindowHandle();
        
        // 1. Test 'About Us' link (visible on login page)
        WebElement aboutUsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        assertLinkExternal(aboutUsLink, "parasoft.com", originalHandle);
        
        // 2. Test 'Parasoft' footer link (visible on login page)
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Parasoft")));
        assertLinkExternal(footerLink, "parasoft.com", originalHandle);
        
        // The instructions ask for links on the base page AND one level below.
        // Let's log in to check links there too, assuming they might differ.
        login();
        originalHandle = driver.getWindowHandle();
        
        // 3. Test 'About Us' link again (visible on logged-in page)
        WebElement loggedInAboutUsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        assertLinkExternal(loggedInAboutUsLink, "parasoft.com", originalHandle);
        
        // 4. Test 'Parasoft' footer link again (visible on logged-in page)
        WebElement loggedInFooterLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Parasoft")));
        assertLinkExternal(loggedInFooterLink, "parasoft.com", originalHandle);
        
        // Logout to ensure next test starts from a clean slate (login page)
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
    }
}