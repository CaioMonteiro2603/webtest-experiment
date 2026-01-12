package GPT20b.ws02.seq02;

import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void init() {
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
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- LOGIN RELATED TESTS ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.navigate().to(BASE_URL);
        // Find username field
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Log In']")));

        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        loginButton.click();

        // Wait for account summary element to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("title")));

        assertTrue(driver.getCurrentUrl().contains("overview"),
                "URL after login should contain 'overview'");
        assertTrue(driver.findElements(By.className("title")).size() > 0,
                "Account summary title should be present after login");

        // Log out to reset state
        logOut();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.navigate().to(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Log In']")));

        userField.clear();
        userField.sendKeys("unknown_user");
        passField.clear();
        passField.sendKeys("wrong_pass");
        loginButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials");
        String errorText = errorMsg.getText();
        assertFalse(errorText.isEmpty(), "Error message should contain text");
    }

    @Test
    @Order(3)
    public void testLogout() {
        loginAndMaintainSession(); // helper to log in
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        // After logout, login button should appear again
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Log In']")));

        assertTrue(driver.findElements(By.cssSelector("input[type='submit'][value='Log In']")).size() > 0,
                "Login button should be visible after logout");
    }

    /* ---------- EXTERNAL LINKS TESTS ---------- */

    @Test
    @Order(4)
    public void testFooterExternalLinks() {
        loginAndMaintainSession();
    }

    /* ---------- MENU NAVIGATION TESTS ---------- */

    @Test
    @Order(5)
    public void testMenuNavigation() {
        loginAndMaintainSession();

        // Open menu
        WebElement openAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openAccountLink.click();

        assertTrue(driver.getCurrentUrl().contains("openaccount"),
                "URL should contain 'openaccount' after clicking Open Account");

        // Back to home
        driver.navigate().back();
    }

    /* ---------- HELPER METHODS ---------- */

    private void loginAndMaintainSession() {
        driver.navigate().to(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Log In']")));

        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("title")));
    }

    private void logOut() {
        if (driver.findElements(By.linkText("Log Out")).size() > 0) {
            WebElement logout = driver.findElement(By.linkText("Log Out"));
            logout.click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Log In']")));
        }
    }

 
}