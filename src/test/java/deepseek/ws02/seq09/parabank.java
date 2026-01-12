package deepseek.ws02.seq09;

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
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
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

        WebElement accountsOverview = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(),'Accounts Overview')] | //h1[contains(text(),'Account Summary')]")));
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
        
        WebElement aboutHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1 | //h2")));
        Assertions.assertTrue(aboutHeader.getText().contains("Para"), "About Us page should load with header containing 'Para'");

        driver.navigate().back();

        // Test Services link
        WebElement services = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        services.click();
        
        WebElement servicesHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1 | //h2")));
        Assertions.assertTrue(servicesHeader.getText().contains("Services") || servicesHeader.getText().contains("SOAP"), "Services page should load with correct header");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Test Admin Page link
        String originalWindow = driver.getWindowHandle();
        
        WebElement adminPage = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        adminPage.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("admin") || driver.getTitle().contains("Administration"), "Admin page should load with correct content");
        
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