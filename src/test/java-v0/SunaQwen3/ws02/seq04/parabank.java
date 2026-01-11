package SunaQwen3.ws02.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    static void setUp() {
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
    void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle(), "Page title should match after navigating to base URL");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should be redirected to overview page after login");
        assertTrue(driver.getPageSource().contains("Accounts Overview"), "Accounts Overview header should be present");
    }

    @Test
    @Order(2)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invaliduser");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertEquals("The username and password could not be verified.", errorMessage.getText(),
                "Error message should appear for invalid credentials");
    }

    @Test
    @Order(3)
    void testAccessAdminPageViaFooter() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

        WebElement adminLink = driver.findElement(By.linkText("Admin Page"));
        assertTrue(adminLink.getAttribute("href").endsWith("/parabank/admin.htm"), "Admin link should point to correct path");

        adminLink.click();
        wait.until(ExpectedConditions.titleIs("ParaBank | Admin"));
        assertTrue(driver.getCurrentUrl().contains("admin.htm"), "Should navigate to admin page");
        assertTrue(driver.getPageSource().contains("Parameter"), "Admin page should contain configuration section");

        driver.navigate().back();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should return to home page");
    }

    @Test
    @Order(4)
    void testExternalLinksInFooter() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

        String originalWindow = driver.getWindowHandle();

        // Test Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
        facebookLink.click();

        switchToNewWindowExcluding(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open in new tab with correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
        twitterLink.click();

        switchToNewWindowExcluding(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open in new tab with correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
        linkedinLink.click();

        switchToNewWindowExcluding(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open in new tab with correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    void testAboutLinkInHeader() {
        // First log in
        performLogin();

        String originalWindow = driver.getWindowHandle();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        switchToNewWindowExcluding(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "About link should redirect to parasoft.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    void testContactLinkInHeader() {
        // First log in
        performLogin();

        String originalWindow = driver.getWindowHandle();

        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();

        switchToNewWindowExcluding(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "Contact link should redirect to parasoft.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    void testLogoutFunctionality() {
        performLogin();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to base URL after logout");
        assertTrue(driver.getPageSource().contains("Customer Login"), "Login form should be visible after logout");
    }

    @Test
    @Order(8)
    void testRegisterNewUserLink() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

        WebElement registerLink = driver.findElement(By.linkText("Register"));
        registerLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Register"));
        assertTrue(driver.getCurrentUrl().contains("register.htm"), "Should navigate to registration page");
        assertTrue(driver.getPageSource().contains("Register an Account"), "Registration form header should be present");
    }

    @Test
    @Order(9)
    void testAccessAllAccountsOverview() {
        performLogin();

        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountsOverviewLink.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should be on accounts overview page");
        assertTrue(driver.getPageSource().contains("Account"), "Account table should be present");
    }

    @Test
    @Order(10)
    void testAccessBillPayPage() {
        performLogin();

        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();

        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        assertTrue(driver.getCurrentUrl().contains("billpay.htm"), "Should navigate to bill pay page");
        assertTrue(driver.getPageSource().contains("Pay a Bill"), "Bill pay form header should be present");
    }

    @Test
    @Order(11)
    void testAccessTransferFundsPage() {
        performLogin();

        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();

        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        assertTrue(driver.getCurrentUrl().contains("transfer.htm"), "Should navigate to transfer funds page");
        assertTrue(driver.getPageSource().contains("Transfer Funds"), "Transfer funds form header should be present");
    }

    @Test
    @Order(12)
    void testAccessFindTransactionsPage() {
        performLogin();

        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions")));
        findTransactionsLink.click();

        wait.until(ExpectedConditions.urlContains("activity.htm"));
        assertTrue(driver.getCurrentUrl().contains("activity.htm"), "Should navigate to find transactions page");
        assertTrue(driver.getPageSource().contains("Find Transactions"), "Find transactions header should be present");
    }

    @Test
    @Order(13)
    void testAccessUpdateContactInfoPage() {
        performLogin();

        WebElement updateProfileLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info")));
        updateProfileLink.click();

        wait.until(ExpectedConditions.urlContains("update.htm"));
        assertTrue(driver.getCurrentUrl().contains("update.htm"), "Should navigate to update profile page");
        assertTrue(driver.getPageSource().contains("Update Profile"), "Update profile header should be present");
    }

    @Test
    @Order(14)
    void testAccessRequestLoanPage() {
        performLogin();

        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan")));
        requestLoanLink.click();

        wait.until(ExpectedConditions.urlContains("loan.htm"));
        assertTrue(driver.getCurrentUrl().contains("loan.htm"), "Should navigate to loan request page");
        assertTrue(driver.getPageSource().contains("Loan Request"), "Loan request header should be present");
    }

    private void performLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }

    private void switchToNewWindowExcluding(String originalWindow) {
        wait.until(webDriver -> webDriver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
}