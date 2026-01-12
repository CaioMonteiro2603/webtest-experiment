package SunaQwen3.ws02.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.name("username")).sendKeys(LOGIN);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//input[@value='Log In']")).click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should be redirected to overview after login");
        WebElement welcomeText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Accounts Overview']")));
        assertNotNull(welcomeText, "Accounts Overview header should be present");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.name("username")).sendKeys("invaliduser");
        driver.findElement(By.name("password")).sendKeys("wrongpass");
        driver.findElement(By.xpath("//input[@value='Log In']")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertEquals("The username and password could not be verified.", error.getText(), "Error message should appear for invalid credentials");
    }

    @Test
    @Order(3)
    public void testAccountsOverviewLinks() {
        testValidLogin(); // Ensure logged in

        List<WebElement> accountLinks = driver.findElements(By.cssSelector("a[href*='account.htm?id=']"));
        assertFalse(accountLinks.isEmpty(), "Should have at least one account link");

        for (WebElement link : accountLinks) {
            String accountId = link.getText();
            link.click();
            wait.until(ExpectedConditions.urlContains("account.htm"));
            assertTrue(driver.getCurrentUrl().contains("account.htm"), "Should navigate to account details page");
            assertTrue(driver.getPageSource().contains(accountId), "Account ID should be present on the page");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }

    @Test
    @Order(4)
    public void testBillPayNavigation() {
        testValidLogin(); // Ensure logged in

        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();

        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        assertTrue(driver.getCurrentUrl().contains("billpay.htm"), "Should navigate to Bill Pay page");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Bill Payment Service']")));
        assertNotNull(header, "Bill Payment Service header should be present");
    }

    @Test
    @Order(5)
    public void testTransferFundsNavigation() {
        testValidLogin(); // Ensure logged in

        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();

        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        assertTrue(driver.getCurrentUrl().contains("transfer.htm"), "Should navigate to Transfer Funds page");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transfer Funds']")));
        assertNotNull(header, "Transfer Funds header should be present");
    }

    @Test
    @Order(6)
    public void testFindTransactionsNavigation() {
        testValidLogin(); // Ensure logged in

        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions")));
        requestLoanLink.click();

        wait.until(ExpectedConditions.urlContains("findtrans.htm"));
        assertTrue(driver.getCurrentUrl().contains("findtrans.htm"), "Should navigate to Find Transactions page");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Find Transactions']")));
        assertNotNull(header, "Find Transactions header should be present");
    }

    @Test
    @Order(7)
    public void testRequestLoanNavigation() {
        testValidLogin(); // Ensure logged in

        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan")));
        requestLoanLink.click();

        wait.until(ExpectedConditions.urlContains("loan.htm"));
        assertTrue(driver.getCurrentUrl().contains("loan.htm"), "Should navigate to Request Loan page");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Loan Application']")));
        assertNotNull(header, "Loan Application header should be present");
    }

    @Test
    @Order(8)
    public void testAdminPageExternalLink() {
        testValidLogin(); // Ensure logged in

        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        adminLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "Admin page should open on parasoft.com domain");

        // Close tab and return
        driver.close();
        driver.switchTo().window(originalWindow);

        // Verify back on original page
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should return to overview page after closing admin tab");
    }

    @Test
    @Order(9)
    public void testFooterExternalLinks() {
        testValidLogin(); // Ensure logged in

        // Store original window
        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        switchToNewWindow(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open twitter.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        switchToNewWindow(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open facebook.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedinLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        switchToNewWindow(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open linkedin.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testLogoutFunctionality() {
        testValidLogin(); // Ensure logged in

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.htm"));
        assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should return to home page after logout");
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@value='Log In']")));
        assertNotNull(loginButton, "Login button should be visible after logout");
    }

    @Test
    @Order(11)
    public void testRegisterLink() {
        driver.get(BASE_URL);

        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();

        wait.until(ExpectedConditions.urlContains("register.htm"));
        assertTrue(driver.getCurrentUrl().contains("register.htm"), "Should navigate to registration page");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Signing up is easy!']")));
        assertNotNull(header, "Registration header should be present");
    }

    @Test
    @Order(12)
    public void testContactLink() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Contact")));

        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();

        wait.until(ExpectedConditions.urlContains("contact.htm"));
        assertTrue(driver.getCurrentUrl().contains("contact.htm"), "Should navigate to contact page");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Contact Us']")));
        assertNotNull(header, "Contact Us header should be present");
    }

    private void switchToNewWindow(String originalWindow) {
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                return;
            }
        }
        fail("No new window opened");
    }
}