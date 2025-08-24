package deepseek.ws03.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class BugBankTest {
    private static WebDriver driver;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
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
    public void testValidLogin() {
        driver.get(BASE_URL);
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"), "Login failed or didn't redirect to home page");
        Assertions.assertTrue(driver.findElements(By.cssSelector(".account-info")).size() > 0, "Account info not displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-message")));
        Assertions.assertTrue(errorMessage.getText().contains("Login inválido"), 
            "Expected error message not displayed");
    }

    @Test
    @Order(3)
    public void testAccountOperations() {
        login();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Test account balance visibility
        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".balance-value")));
        Assertions.assertTrue(balanceElement.isDisplayed(), "Balance not displayed");

        // Test transfer navigation
        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".transfer-button")));
        transferButton.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".transfer-form")));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".transfer-form")).isDisplayed(), 
            "Transfer form not displayed");
    }

    @Test
    @Order(4)
    public void testNavigationMenu() {
        login();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Test home navigation
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".home-link")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"), "Home page not loaded");

        // Test statements navigation
        WebElement statementsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".statements-link")));
        statementsLink.click();
        wait.until(ExpectedConditions.urlContains("statements"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("statements"), "Statements page not loaded");
    }

    @Test
    @Order(5)
    public void testLogout() {
        login();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".logout-button")));
        logoutButton.click();
        
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), 
            "Logout didn't redirect to login page");
        Assertions.assertTrue(driver.findElements(By.cssSelector("input[type='email']")).size() > 0, 
            "Login form not visible after logout");
    }

    private void login() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
    }
}