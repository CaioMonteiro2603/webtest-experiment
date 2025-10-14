package deepseek.ws02.seq04;

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.className("smallText")).getText().contains("Welcome"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(errorElement.getText().contains("An internal error has occurred"));
    }

    @Test
    @Order(3)
    public void testAccountServicesNavigation() {
        login();
        
        // Test Open New Account
        WebElement openNewAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openNewAccountLink.click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Open New Account"));
        
        // Test Accounts Overview
        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountsOverviewLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Accounts Overview"));
        
        // Test Transfer Funds
        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Transfer Funds"));
        
        // Test Bill Pay
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();
        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Bill Pay"));
        
        // Test Find Transactions
        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions")));
        findTransactionsLink.click();
        wait.until(ExpectedConditions.urlContains("findtrans.htm"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Find Transactions"));
        
        // Test Update Contact Info
        WebElement updateContactInfoLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info")));
        updateContactInfoLink.click();
        wait.until(ExpectedConditions.urlContains("updateprofile.htm"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Update Profile"));
        
        // Test Request Loan
        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan")));
        requestLoanLink.click();
        wait.until(ExpectedConditions.urlContains("requestloan.htm"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Apply for a Loan"));
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        login();
        
        // Test About Us
        testFooterLink("About Us", "about.htm", "ParaBank is a demo site");
        
        // Test Services
        testFooterLink("Services", "services.htm", "ParaBank offers a variety of services");
        
        // Test Products
        testFooterLink("Products", "products.htm", "ParaBank now offers a complete set of banking products");
        
        // Test Locations
        testFooterLink("Locations", "locations.htm", "ParaBank is now serving customers in multiple locations");
        
        // Test Forum
        testFooterLink("Forum", "forum.htm", "Customer Forum");
        
        // Test Site Map
        testFooterLink("Site Map", "sitemap.htm", "Site Map");
        
        // Test Contact Us
        testFooterLink("Contact Us", "contact.htm", "Customer Care");
    }

    private void testFooterLink(String linkText, String expectedUrlPath, String expectedContent) {
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
        footerLink.click();
        
        wait.until(ExpectedConditions.urlContains(expectedUrlPath));
        Assertions.assertTrue(driver.getPageSource().contains(expectedContent));
        
        // Return to overview page
        driver.findElement(By.linkText("Accounts Overview")).click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }

    @Test
    @Order(5)
    public void testLogout() {
        login();
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(driver.findElement(By.name("username")).isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }
}