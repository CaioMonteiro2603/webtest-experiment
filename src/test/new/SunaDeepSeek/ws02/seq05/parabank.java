package SunaDeepSeek.ws02.seq05;

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
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle());
        Assertions.assertTrue(driver.findElement(By.xpath("//img[contains(@src,'logo')]")).isDisplayed());
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

        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h1[contains(text(), 'Accounts Overview')]")));
        Assertions.assertTrue(welcomeMessage.isDisplayed());
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"));
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
            By.xpath("//p[contains(text(), 'The username and password could not be verified.')]")));
        Assertions.assertTrue(errorMessage.isDisplayed());
    }

    @Test
    @Order(4)
    public void testAccountServicesLinks() {
        login();
        
        // Test Open New Account
        driver.findElement(By.linkText("Open New Account")).click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        Assertions.assertTrue(driver.findElement(By.xpath("//h1[contains(text(), 'Open New Account')]")).isDisplayed());
        
        // Test Accounts Overview
        driver.findElement(By.linkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.xpath("//h1[contains(text(), 'Accounts Overview')]")).isDisplayed());
        
        // Test Transfer Funds
        driver.findElement(By.linkText("Transfer Funds")).click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        Assertions.assertTrue(driver.findElement(By.xpath("//h1[contains(text(), 'Transfer Funds')]")).isDisplayed());
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test About Us link
        testExternalLink("//a[contains(text(), 'About Us')]", "parasoft.com");
        
        // Test Services link
        testExternalLink("//a[contains(text(), 'Services')]", "parasoft.com");
        
        // Test Products link
        testExternalLink("//a[contains(text(), 'Products')]", "parasoft.com");
        
        // Test Locations link
        testExternalLink("//a[contains(text(), 'Locations')]", "parasoft.com");
        
        // Test Admin Page link
        driver.findElement(By.linkText("Admin Page")).click();
        wait.until(ExpectedConditions.urlContains("admin.htm"));
        Assertions.assertTrue(driver.findElement(By.xpath("//h1[contains(text(), 'Administration')]")).isDisplayed());
    }

    @Test
    @Order(6)
    public void testBillPayFunctionality() {
        login();
        
        driver.findElement(By.linkText("Bill Pay")).click();
        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        
        WebElement payeeName = wait.until(ExpectedConditions.elementToBeClickable(By.name("payee.name")));
        payeeName.sendKeys("Test Payee");
        driver.findElement(By.name("payee.address.street")).sendKeys("123 Test St");
        driver.findElement(By.name("payee.address.city")).sendKeys("Test City");
        driver.findElement(By.name("payee.address.state")).sendKeys("CA");
        driver.findElement(By.name("payee.address.zipCode")).sendKeys("90210");
        driver.findElement(By.name("payee.phoneNumber")).sendKeys("555-123-4567");
        driver.findElement(By.name("payee.accountNumber")).sendKeys("12345");
        driver.findElement(By.name("verifyAccount")).sendKeys("12345");
        driver.findElement(By.name("amount")).sendKeys("100");
        driver.findElement(By.cssSelector("input[value='Send Payment']")).click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h1[contains(text(), 'Bill Payment Complete')]")));
        Assertions.assertTrue(successMessage.isDisplayed());
    }

    @Test
    @Order(7)
    public void testRequestLoan() {
        login();
        
        driver.findElement(By.linkText("Request Loan")).click();
        wait.until(ExpectedConditions.urlContains("requestloan.htm"));
        
        WebElement amountField = wait.until(ExpectedConditions.elementToBeClickable(By.name("amount")));
        amountField.sendKeys("10000");
        driver.findElement(By.name("downPayment")).sendKeys("1000");
        driver.findElement(By.cssSelector("input[value='Apply Now']")).click();
        
        WebElement resultMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h1[contains(text(), 'Loan Request Processed')]")));
        Assertions.assertTrue(resultMessage.isDisplayed());
    }

    @Test
    @Order(8)
    public void testUpdateContactInfo() {
        login();
        
        driver.findElement(By.linkText("Update Contact Info")).click();
        wait.until(ExpectedConditions.urlContains("updateprofile.htm"));
        
        WebElement phoneField = wait.until(ExpectedConditions.elementToBeClickable(By.name("customer.phoneNumber")));
        phoneField.clear();
        phoneField.sendKeys("555-987-6543");
        driver.findElement(By.cssSelector("input[value='Update Profile']")).click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h1[contains(text(), 'Profile Updated')]")));
        Assertions.assertTrue(successMessage.isDisplayed());
    }

    @Test
    @Order(9)
    public void testLogout() {
        login();
        
        driver.findElement(By.linkText("Log Out")).click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username"))).isDisplayed());
        Assertions.assertTrue(driver.findElement(By.name("password")).isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }

    private void testExternalLink(String xpath, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        link.click();
        
        try {
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.contentEquals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
        
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (TimeoutException e) {
            // If new window doesn't open, link might be opening in same window
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
            driver.get(BASE_URL);
        }
    }
}