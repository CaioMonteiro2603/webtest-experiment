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
        username.clear();
        username.sendKeys("invalid@user.com");
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys("wrongpass");
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
        
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("error")));
        Assertions.assertTrue(error.getText().contains("The username and password could not be verified"));
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
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        // Test external links by verifying they navigate to the expected URLs
        testFooterLink(js, "//a[contains(text(),'About Us')]", "about.solutions.parasoft.com");
        testFooterLink(js, "//a[contains(text(),'Services')]", "solutions.parasoft.com");
        testFooterLink(js, "//a[contains(text(),'Products')]", "solutions.parasoft.com");
        testFooterLink(js, "//a[contains(text(),'Locations')]", "solutions.parasoft.com");
        
        // Test Admin Page - this should open in same window
        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Admin Page')]")));
        adminLink.click();
        wait.until(ExpectedConditions.urlContains("admin.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("admin.htm"));
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

    private void testFooterLink(JavascriptExecutor js, String xpath, String expectedDomain) {
        String originalUrl = driver.getCurrentUrl();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        String href = link.getAttribute("href");
        Assertions.assertTrue(href.contains(expectedDomain) || href.contains("parasoft.com"));
        
        // Open link in new tab
        js.executeScript("window.open(arguments[0].href, '_blank');", link);
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!driver.getWindowHandle().equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Wait for page to load and verify URL contains expected domain
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        
        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(driver.getWindowHandles().iterator().next());
        
        // Navigate back to original URL if needed
        if (!driver.getCurrentUrl().equals(originalUrl)) {
            driver.get(BASE_URL);
        }
    }
}