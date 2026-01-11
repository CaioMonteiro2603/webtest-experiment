package SunaDeepSeek.ws02.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        Assertions.assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle());
        Assertions.assertTrue(driver.findElement(By.cssSelector("img[title='ParaBank']")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"));
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.clear();
        usernameField.sendKeys("invalid@email.com");
        passwordField.clear();
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("p.error")));
        Assertions.assertTrue(errorMessage.getText().contains("An internal error has occurred"));
    }

    @Test
    @Order(4)
    public void testAccountOverviewPage() {
        login();
        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Accounts Overview")));
        accountsOverviewLink.click();

        WebElement accountsOverviewTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertEquals("Accounts Overview", accountsOverviewTitle.getText());
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"));
    }

    @Test
    @Order(5)
    public void testTransferFundsPage() {
        login();
        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Transfer Funds")));
        transferFundsLink.click();

        WebElement transferFundsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertEquals("Transfer Funds", transferFundsTitle.getText());
        Assertions.assertTrue(driver.getCurrentUrl().contains("transfer.htm"));
    }

    @Test
    @Order(6)
    public void testBillPayPage() {
        login();
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Bill Pay")));
        billPayLink.click();

        WebElement billPayTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertEquals("Bill Payment Service", billPayTitle.getText());
        Assertions.assertTrue(driver.getCurrentUrl().contains("billpay.htm"));
    }

    @Test
    @Order(7)
    public void testFindTransactionsPage() {
        login();
        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Find Transactions")));
        findTransactionsLink.click();

        WebElement findTransactionsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertEquals("Find Transactions", findTransactionsTitle.getText());
        Assertions.assertTrue(driver.getCurrentUrl().contains("findtrans.htm"));
    }

    @Test
    @Order(8)
    public void testUpdateContactInfoPage() {
        login();
        WebElement updateContactLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Update Contact Info")));
        updateContactLink.click();

        WebElement updateProfileTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertEquals("Update Profile", updateProfileTitle.getText());
        Assertions.assertTrue(driver.getCurrentUrl().contains("updateprofile.htm"));
    }

    @Test
    @Order(9)
    public void testRequestLoanPage() {
        login();
        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Request Loan")));
        requestLoanLink.click();

        WebElement requestLoanTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertEquals("Apply for a Loan", requestLoanTitle.getText());
        Assertions.assertTrue(driver.getCurrentUrl().contains("requestloan.htm"));
    }

    @Test
    @Order(10)
    public void testLogout() {
        login();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Log Out")));
        logoutLink.click();

        WebElement loginPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("loginPanel")));
        Assertions.assertTrue(loginPanel.isDisplayed());
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"));
    }

    @Test
    @Order(11)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        testExternalLink("About Us", "parasoft.com/products/");
        testExternalLink("Services", "parasoft.com/services/");
        testExternalLink("Products", "parasoft.com/products/");
        testExternalLink("Locations", "parasoft.com/locations/");
        testExternalLink("Admin Page", "parabank.parasoft.com/parabank/admin.htm");
    }

    private void testExternalLink(String linkText, String expectedUrlPart) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText(linkText)));
        link.click();

        wait.until(ExpectedConditions.urlContains(expectedUrlPart));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedUrlPart));
        
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("index.htm"));
    }

    private void login() {
        driver.get(BASE_URL);
        if (driver.findElements(By.id("loginPanel")).size() > 0) {
            WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }
}