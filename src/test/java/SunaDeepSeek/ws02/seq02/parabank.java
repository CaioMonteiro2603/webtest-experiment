package SunaDeepSeek.ws02.seq02;

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        Assertions.assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle());
        Assertions.assertTrue(driver.findElement(By.cssSelector("img[title='ParaBank']")).isDisplayed());
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

        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Accounts Overview"));
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("error")));
        Assertions.assertTrue(errorMessage.getText().contains("The username and password could not be verified."));
    }

    @Test
    @Order(4)
    public void testAccountServicesLinks() {
        login();
        
        // Test Open New Account
        driver.findElement(By.linkText("Open New Account")).click();
        wait.until(ExpectedConditions.titleContains("Open Account"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("openaccount.htm"));
        
        // Test Accounts Overview
        driver.findElement(By.linkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.titleContains("Accounts Overview"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"));
        
        // Test Transfer Funds
        driver.findElement(By.linkText("Transfer Funds")).click();
        wait.until(ExpectedConditions.titleContains("Transfer Funds"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("transfer.htm"));
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        login();
        
        // Test About Us link
        driver.findElement(By.linkText("About Us")).click();
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about.htm"));
        
        // Test Services link
        driver.findElement(By.linkText("Services")).click();
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("services.htm"));
        
        // Test Products link
        driver.findElement(By.linkText("Products")).click();
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("products.htm"));
        
        // Test Locations link
        driver.findElement(By.linkText("Locations")).click();
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("locations.htm"));
        
        // Test Admin Page link
        driver.findElement(By.linkText("Admin Page")).click();
        wait.until(ExpectedConditions.titleContains("Administration"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("admin.htm"));
    }

    @Test
    @Order(6)
    public void testBillPayFunctionality() {
        login();
        
        driver.findElement(By.linkText("Bill Pay")).click();
        wait.until(ExpectedConditions.titleContains("Bill Pay"));
        
        WebElement payeeName = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("payee.name")));
        payeeName.sendKeys("Test Payee");
        driver.findElement(By.name("payee.address.street")).sendKeys("123 Test St");
        driver.findElement(By.name("payee.address.city")).sendKeys("Test City");
        driver.findElement(By.name("payee.address.state")).sendKeys("CA");
        driver.findElement(By.name("payee.address.zipCode")).sendKeys("12345");
        driver.findElement(By.name("payee.phoneNumber")).sendKeys("1234567890");
        driver.findElement(By.name("payee.accountNumber")).sendKeys("12345");
        driver.findElement(By.name("verifyAccount")).sendKeys("12345");
        driver.findElement(By.name("amount")).sendKeys("100");
        
        driver.findElement(By.cssSelector("input[value='Send Payment']")).click();
        
        WebElement confirmation = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("title")));
        Assertions.assertTrue(confirmation.getText().contains("Bill Payment Complete"));
    }

    @Test
    @Order(7)
    public void testRequestLoan() {
        login();
        
        driver.findElement(By.linkText("Request Loan")).click();
        wait.until(ExpectedConditions.titleContains("Loan Request"));
        
        WebElement amountField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("amount")));
        amountField.sendKeys("10000");
        driver.findElement(By.name("downPayment")).sendKeys("1000");
        driver.findElement(By.cssSelector("input[value='Apply Now']")).click();
        
        WebElement result = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("title")));
        Assertions.assertTrue(result.getText().contains("Loan Request Processed"));
    }

    @Test
    @Order(8)
    public void testLogout() {
        login();
        
        driver.findElement(By.linkText("Log Out")).click();
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"));
        Assertions.assertTrue(driver.findElement(By.name("username")).isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL);
        if (driver.findElements(By.name("username")).size() > 0) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("title")));
        }
    }
}