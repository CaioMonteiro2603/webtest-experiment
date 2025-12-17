package SunaDeepSeek.ws02.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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
        Assertions.assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle());
        Assertions.assertTrue(driver.findElement(By.cssSelector("img[title='ParaBank']")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        username.sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
        
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Accounts Overview"));
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        username.sendKeys("invalid@user.com");
        driver.findElement(By.name("password")).sendKeys("wrongpass");
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
        
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("p.error")));
        Assertions.assertTrue(error.getText().contains("An internal error has occurred"));
    }

    @Test
    @Order(4)
    public void testAccountServicesLinks() {
        login();
        
        // Test Open New Account
        driver.findElement(By.linkText("Open New Account")).click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Open New Account"));
        
        // Test Accounts Overview
        driver.findElement(By.linkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Accounts Overview"));
        
        // Test Transfer Funds
        driver.findElement(By.linkText("Transfer Funds")).click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Transfer Funds"));
        
        // Test Bill Pay
        driver.findElement(By.linkText("Bill Pay")).click();
        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Bill Pay"));
        
        // Test Find Transactions
        driver.findElement(By.linkText("Find Transactions")).click();
        wait.until(ExpectedConditions.urlContains("findtrans.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Find Transactions"));
        
        // Test Update Contact Info
        driver.findElement(By.linkText("Update Contact Info")).click();
        wait.until(ExpectedConditions.urlContains("updateprofile.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Update Profile"));
        
        // Test Request Loan
        driver.findElement(By.linkText("Request Loan")).click();
        wait.until(ExpectedConditions.urlContains("requestloan.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Apply for a Loan"));
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test About Us
        testExternalLink("//a[contains(text(),'About Us')]", "parasoft.com");
        
        // Test Services
        testExternalLink("//a[contains(text(),'Services')]", "parasoft.com");
        
        // Test Products
        testExternalLink("//a[contains(text(),'Products')]", "parasoft.com");
        
        // Test Locations
        testExternalLink("//a[contains(text(),'Locations')]", "parasoft.com");
        
        // Test Admin Page
        testExternalLink("//a[contains(text(),'Admin Page')]", "parasoft.com");
    }

    @Test
    @Order(6)
    public void testLogout() {
        login();
        driver.findElement(By.linkText("Log Out")).click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(driver.findElement(By.name("username")).isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL);
        if (driver.findElements(By.name("username")).size() > 0) {
            WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
            username.sendKeys(USERNAME);
            driver.findElement(By.name("password")).sendKeys(PASSWORD);
            driver.findElement(By.cssSelector("input[value='Log In']")).click();
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }

    private void testExternalLink(String xpath, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        link.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}