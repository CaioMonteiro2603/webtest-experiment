package deepseek.ws02.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class ParaBankTest {
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
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        username.sendKeys("invalid@email.com");
        password.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("p.error")));
        Assertions.assertTrue(errorElement.getText().contains("An internal error has occurred and has been logged"), 
            "Expected error message for invalid login");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        username.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Accounts Overview"),
            "Expected to see Accounts Overview after login");
    }

    @Test
    @Order(3)
    public void testAccountNavigation() {
        login();
        
        // Test Open New Account
        WebElement openAccountLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Open New Account")));
        openAccountLink.click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        WebElement openAccountHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(openAccountHeader.getText().contains("Open New Account"),
            "Expected to be on Open New Account page");

        // Test Accounts Overview
        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Accounts Overview")));
        accountsOverviewLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement accountsOverviewHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(accountsOverviewHeader.getText().contains("Accounts Overview"),
            "Expected to be on Accounts Overview page");

        // Test Transfer Funds
        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Transfer Funds")));
        transferFundsLink.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        WebElement transferHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(transferHeader.getText().contains("Transfer Funds"),
            "Expected to be on Transfer Funds page");
    }

    @Test
    @Order(4)
    public void testBillPayFunctionality() {
        login();
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Bill Pay")));
        billPayLink.click();
        wait.until(ExpectedConditions.urlContains("billpay.htm"));

        WebElement payeeName = wait.until(ExpectedConditions.elementToBeClickable(By.name("payee.name")));
        WebElement address = driver.findElement(By.name("payee.address.street"));
        WebElement city = driver.findElement(By.name("payee.address.city"));
        WebElement state = driver.findElement(By.name("payee.address.state"));
        WebElement zipCode = driver.findElement(By.name("payee.address.zipCode"));
        WebElement phone = driver.findElement(By.name("payee.phoneNumber"));
        WebElement account = driver.findElement(By.name("payee.accountNumber"));
        WebElement verifyAccount = driver.findElement(By.name("verifyAccount"));
        WebElement amount = driver.findElement(By.name("amount"));
        WebElement sendPaymentButton = driver.findElement(By.cssSelector("input[value='Send Payment']"));

        payeeName.sendKeys("Test Payee");
        address.sendKeys("123 Test St");
        city.sendKeys("Test City");
        state.sendKeys("CA");
        zipCode.sendKeys("12345");
        phone.sendKeys("1234567890");
        account.sendKeys("12345");
        verifyAccount.sendKeys("12345");
        amount.sendKeys("100");
        sendPaymentButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(successMessage.getText().contains("Bill Payment Complete"),
            "Expected bill payment success message");
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        login();
        String originalWindow = driver.getWindowHandle();

        // Test About Us link
        WebElement aboutUsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("About Us")));
        aboutUsLink.click();
        wait.until(ExpectedConditions.urlContains("about.htm"));
        WebElement aboutHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(aboutHeader.getText().contains("ParaSoft Demo Website"),
            "Expected to be on About Us page");

        // Test Services link (external)
        driver.get(BASE_URL);
        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Services")));
        servicesLink.click();
        assertExternalLink("parasoft.com", originalWindow);
    }

    private void login() {
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            driver.get(BASE_URL);
            WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
            WebElement password = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

            username.sendKeys(USERNAME);
            password.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }

    private void assertExternalLink(String expectedDomain, String originalWindow) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "Expected to be on " + expectedDomain + " after clicking external link");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}