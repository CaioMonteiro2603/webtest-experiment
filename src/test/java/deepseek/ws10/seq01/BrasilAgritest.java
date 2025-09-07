package deepseek.ws10.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgriWebTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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
    public void testLoginPageLoads() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("img[src*='logo.png']")));
        Assertions.assertTrue(logo.isDisplayed(), "Logo should be displayed");
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
            "Current URL should match login page");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[name='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".error-message")));
        Assertions.assertTrue(errorMessage.getText().contains("credenciais"),
            "Error message for invalid login should be displayed");
    }

    @Test
    @Order(3)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[name='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        WebElement dashboardHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".dashboard-header")));
        Assertions.assertTrue(dashboardHeader.isDisplayed(),
            "Dashboard should be visible after successful login");
    }

    @Test
    @Order(4)
    public void testNavigationMenu() {
        // First login
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name='email']")))
            .sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        // Test navigation to Reports
        WebElement reportsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/reports']")));
        reportsLink.click();
        
        wait.until(ExpectedConditions.urlContains("/reports"));
        WebElement reportsHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".reports-header")));
        Assertions.assertTrue(reportsHeader.isDisplayed(),
            "Reports page header should be visible");

        // Test navigation back to Dashboard
        WebElement dashboardLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/dashboard']")));
        dashboardLink.click();
        
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    @Test
    @Order(5)
    public void testLogout() {
        // First login
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name='email']")))
            .sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        // Logout
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".user-menu")));
        userMenu.click();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".logout-button")));
        logoutButton.click();
        
        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".login-form")));
        Assertions.assertTrue(loginForm.isDisplayed(),
            "Login form should be visible after logout");
    }
}