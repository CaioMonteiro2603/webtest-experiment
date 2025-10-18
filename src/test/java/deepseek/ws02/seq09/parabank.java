package deepseek.ws02.seq09;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParaBankTest {

    private static WebDriver driver;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        Assertions.assertTrue(element.isDisplayed(), "Username input field should be displayed");
        Assertions.assertTrue(driver.getCurrentUrl().contains("parabank"), "Current URL should contain 'parabank'");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        username.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        loginButton.click();

        WebElement accountsOverview = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(),'Accounts Overview')]")));
        Assertions.assertTrue(accountsOverview.isDisplayed(), "Accounts Overview header should be displayed after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        username.sendKeys("invalid@email.com");
        password.sendKeys("wrongpassword");
        loginButton.click();

        WebElement error = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[contains(text(),'Please enter a username and password.') or contains(text(),'The username and password could not be verified.')]")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Test About Us link
        WebElement aboutUs = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutUs.click();
        
        WebElement aboutHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1")));
        Assertions.assertTrue(aboutHeader.getText().contains("ParaSoft Demo Website"), "About Us page should load with correct header");

        driver.navigate().back();

        // Test Services link
        WebElement services = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        services.click();
        
        WebElement servicesHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1")));
        Assertions.assertTrue(servicesHeader.getText().contains("Available Bookstore SOAP services"), "Services page should load with correct header");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Test Admin Page link
        WebElement adminPage = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        adminPage.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("admin"), "Admin page URL should contain 'admin'");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testAccountServicesNavigation() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // First login
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));
        username.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        loginButton.click();

        // Test Open New Account
        WebElement openNewAccount = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openNewAccount.click();
        
        WebElement newAccountHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(),'Open New Account')]")));
        Assertions.assertTrue(newAccountHeader.isDisplayed(), "Open New Account page should load");

        // Test Transfer Funds
        WebElement transferFunds = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFunds.click();
        
        WebElement transferHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(),'Transfer Funds')]")));
        Assertions.assertTrue(transferHeader.isDisplayed(), "Transfer Funds page should load");
    }

    @Test
    @Order(7)
    public void testLogout() {
        // First login
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));
        username.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        loginButton.click();

        // Test logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logout.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginPanel")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Login form should be visible after logout");
    }
}