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
        boolean isLoggedIn = driver.findElements(By.linkText("Log Out")).size() > 0;
        Assertions.assertTrue(isLoggedIn, "User should be logged in successfully");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("wronguser", "wrongpass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMsg.getText().contains("error"), "Should show login error message");
    }

    @Test
    @Order(3)
    public void testExternalLinkParaSoftOpensInNewTab() {
        driver.get(BASE_URL);
        WebElement paraSoftLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='parasoft.com']")));
        String originalWindow = driver.getWindowHandle();
        paraSoftLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
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
        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan")));
        requestLoanLink.click();
        wait.until(ExpectedConditions.urlContains("requestloan.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("requestloan.htm"), "Should navigate to Request Loan page");

        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logout.click();
    }

    @Test
    @Order(8)
    public void testLogoutFunctionality() {
        login(LOGIN_USERNAME, LOGIN_PASSWORD);
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
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
