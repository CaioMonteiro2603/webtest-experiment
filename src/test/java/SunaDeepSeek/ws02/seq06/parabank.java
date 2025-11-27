package SunaDeepSeek.ws02.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParaBankTestSuite {
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
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Customer Login"));
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
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Accounts Overview"));
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
        Assertions.assertTrue(errorMessage.getText().contains("An internal error has occurred"));
    }

    @Test
    @Order(4)
    public void testAccountNavigation() {
        login();
        
        // Test Accounts Overview link
        WebElement accountsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Accounts Overview")));
        accountsLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Accounts Overview"));

        // Test Transfer Funds link
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Transfer Funds")));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Transfer Funds"));

        // Test Bill Pay link
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Bill Pay")));
        billPayLink.click();
        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Bill Pay"));
    }

    @Test
    @Order(5)
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
        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Admin Page")));
        adminLink.click();
        wait.until(ExpectedConditions.urlContains("admin.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1")).getText().contains("Administration"));
    }

    @Test
    @Order(6)
    public void testLogout() {
        login();
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Log Out")));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Customer Login"));
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

    private void testExternalLink(String linkText, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText(linkText)));
        link.click();
        
        // Wait for new window to open
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        // Switch to new window
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close window
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}