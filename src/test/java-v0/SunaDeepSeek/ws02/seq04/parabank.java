package SunaDeepSeek.ws02.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        Assertions.assertTrue(driver.getTitle().contains("ParaBank"), "Home page title should contain 'ParaBank'");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should be redirected to account overview after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("p.error")));
        Assertions.assertTrue(errorMessage.getText().contains("An internal error has occurred"), 
            "Should display error message for invalid login");
    }

    @Test
    @Order(4)
    public void testAccountOverviewPage() {
        login();
        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Accounts Overview")));
        accountsOverviewLink.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement accountTable = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("accountTable")));
        Assertions.assertTrue(accountTable.isDisplayed(), "Accounts table should be visible");
    }

    @Test
    @Order(5)
    public void testTransferFunds() {
        login();
        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Transfer Funds")));
        transferFundsLink.click();

        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        WebElement amountField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("amount")));
        Assertions.assertTrue(amountField.isDisplayed(), "Transfer funds form should be visible");
    }

    @Test
    @Order(6)
    public void testBillPay() {
        login();
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Bill Pay")));
        billPayLink.click();

        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        WebElement payeeNameField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("payee.name")));
        Assertions.assertTrue(payeeNameField.isDisplayed(), "Bill pay form should be visible");
    }

    @Test
    @Order(7)
    public void testFindTransactions() {
        login();
        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Find Transactions")));
        findTransactionsLink.click();

        wait.until(ExpectedConditions.urlContains("findtrans.htm"));
        WebElement accountIdField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("accountId")));
        Assertions.assertTrue(accountIdField.isDisplayed(), "Find transactions form should be visible");
    }

    @Test
    @Order(8)
    public void testUpdateContactInfo() {
        login();
        WebElement updateContactLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Update Contact Info")));
        updateContactLink.click();

        wait.until(ExpectedConditions.urlContains("updateprofile.htm"));
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("customer.firstName")));
        Assertions.assertTrue(firstNameField.isDisplayed(), "Update contact form should be visible");
    }

    @Test
    @Order(9)
    public void testRequestLoan() {
        login();
        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Request Loan")));
        requestLoanLink.click();

        wait.until(ExpectedConditions.urlContains("requestloan.htm"));
        WebElement loanAmountField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("amount")));
        Assertions.assertTrue(loanAmountField.isDisplayed(), "Request loan form should be visible");
    }

    @Test
    @Order(10)
    public void testLogout() {
        login();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.htm"));
        WebElement loginButton = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[value='Log In']")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Should be redirected to login page after logout");
    }

    @Test
    @Order(11)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test About Us link
        testExternalLink("About Us", "parasoft.com");
        
        // Test Services link
        testExternalLink("Services", "parasoft.com");
        
        // Test Products link
        testExternalLink("Products", "parasoft.com");
        
        // Test Locations link
        testExternalLink("Locations", "parasoft.com");
        
        // Test Admin Page link
        testExternalLink("Admin Page", "parasoft.com");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText(linkText)));
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String originalWindow = driver.getWindowHandle();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "External link should open page with domain: " + expectedDomain);
        
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void login() {
        driver.get(BASE_URL);
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }
}