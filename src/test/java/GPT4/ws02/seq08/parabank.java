package GPT4.ws02.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN_USERNAME = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1920, 1080));
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

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));

        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(LOGIN_USERNAME, LOGIN_PASSWORD);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Log Out")));
        boolean isLoggedIn = driver.findElements(By.linkText("Log Out")).size() > 0;
        Assertions.assertTrue(isLoggedIn, "User should be logged in successfully");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("wronguser", "wrongpass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Should show login error message");
    }

    @Test
    @Order(3)
    public void testExternalLinkParaSoftOpensInNewTab() {
        driver.get(BASE_URL);
        WebElement paraSoftLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='parasoft.com']")));
        String originalWindow = driver.getWindowHandle();
        
        String originalUrl = driver.getCurrentUrl();
        paraSoftLink.click();

        Set<String> windows = driver.getWindowHandles();
        if (windows.size() > 1) {
            for (String win : windows) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    break;
                }
            }
            wait.until(ExpectedConditions.urlContains("parasoft.com"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "Should open parasoft.com in new tab");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(originalUrl)));
            Assertions.assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "Should navigate to parasoft.com");
        }
    }

    @Test
    @Order(4)
    public void testNavigateToAboutUs() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutLink.click();
        wait.until(ExpectedConditions.urlContains("about.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about.htm"), "Should navigate to About Us page");
    }

    @Test
    @Order(5)
    public void testNavigateToServices() {
        driver.get(BASE_URL);
        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();
        wait.until(ExpectedConditions.urlContains("services.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("services.htm"), "Should navigate to Services page");
    }

    @Test
    @Order(6)
    public void testNavigateToContact() {
        driver.get(BASE_URL);
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact Us")));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("contact.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("contact.htm"), "Should navigate to Contact Us page");
    }

    @Test
    @Order(7)
    public void testRequestLoanPageAccessAfterLogin() {
        login(LOGIN_USERNAME, LOGIN_PASSWORD);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Request Loan")));
        WebElement requestLoanLink = driver.findElement(By.linkText("Request Loan"));
        requestLoanLink.click();
        wait.until(ExpectedConditions.urlContains("requestloan.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("requestloan.htm"), "Should navigate to Request Loan page");

        WebElement logout = driver.findElement(By.linkText("Log Out"));
        logout.click();
    }

    @Test
    @Order(8)
    public void testLogoutFunctionality() {
        login(LOGIN_USERNAME, LOGIN_PASSWORD);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Log Out")));
        WebElement logoutLink = driver.findElement(By.linkText("Log Out"));
        logoutLink.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        Assertions.assertTrue(driver.findElement(By.name("username")).isDisplayed(), "Should be logged out and see login form");
    }

    @Test
    @Order(9)
    public void testRegisterPageAccessible() {
        driver.get(BASE_URL);
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();
        wait.until(ExpectedConditions.urlContains("register.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("register.htm"), "Should navigate to Register page");
    }
}