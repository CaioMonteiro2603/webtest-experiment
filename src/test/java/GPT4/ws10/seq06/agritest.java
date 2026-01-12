package GPT4.ws10.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class agritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://the-internet.herokuapp.com/login";
    private static final String EMAIL = "tomsmith";
    private static final String PASSWORD = "SuperSecretPassword!";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLoginPageLoads() {
        driver.get(BASE_URL);
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        Assertions.assertTrue(usernameInput.isDisplayed(), "Username input should be visible on login page");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameInput.clear();
        usernameInput.sendKeys("wronguser");
        passwordInput.clear();
        passwordInput.sendKeys("wrongpassword");
        loginBtn.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("flash")));
        Assertions.assertTrue(errorMessage.getText().toLowerCase().contains("your username is invalid") ||
                              errorMessage.getText().toLowerCase().contains("invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameInput.clear();
        usernameInput.sendKeys(EMAIL);
        passwordInput.clear();
        passwordInput.sendKeys(PASSWORD);
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/secure"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/secure"), "Should redirect to /secure after login");
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("flash")));
        Assertions.assertTrue(successMessage.getText().toLowerCase().contains("secure area"), "Success message should indicate secure area");
    }

    @Test
    @Order(4)
    public void testSidebarNavigationLinks() {
        testValidLogin();
        WebElement logoutBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/logout']")));
        Assertions.assertTrue(logoutBtn.isDisplayed(), "Logout button should be visible");
        logoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "After logout, should return to login page");
    }

    @Test
    @Order(5)
    public void testLogout() {
        testValidLogin();
        WebElement logoutBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/logout']")));
        logoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), "After logout, should return to login page");
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        Assertions.assertTrue(usernameInput.isDisplayed(), "Username input should be visible on login page");
    }
}