package deepseek.ws03.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class BugBankTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys("invalid@email.com");
        password.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-error")));
        Assertions.assertTrue(errorElement.isDisplayed(), 
            "Expected error message for invalid credentials");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".dashboard")));
        Assertions.assertTrue(welcomeMessage.isDisplayed(),
            "Expected dashboard to be visible after login");
    }

    @Test
    @Order(3)
    public void testAccountOperations() {
        login();
        
        // Test deposit
        WebElement depositButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[aria-label='Deposit']")));
        depositButton.click();
        
        WebElement amountInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Amount']")));
        amountInput.sendKeys("100");
        
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit']")));
        confirmButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.isDisplayed(),
            "Expected deposit success message");
    }

    @Test
    @Order(4)
    public void testTransferFunds() {
        login();
        
        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[aria-label='Transfer']")));
        transferButton.click();
        
        WebElement amountInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Amount']")));
        amountInput.sendKeys("50");
        
        WebElement accountInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Account number']")));
        accountInput.sendKeys("123456");
        
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit']")));
        confirmButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.isDisplayed(),
            "Expected transfer success message");
    }

    @Test
    @Order(5)
    public void testNavigationMenu() {
        login();
        
        // Test statement navigation
        WebElement statementLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Statement")));
        statementLink.click();
        
        WebElement statementHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(statementHeader.getText().contains("Statement"),
            "Expected to be on statement page");
            
        // Test logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Logout")));
        logoutLink.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("form")));
        Assertions.assertTrue(loginForm.isDisplayed(),
            "Expected to see login form after logout");
    }

    private void login() {
        if (!driver.getCurrentUrl().equals(BASE_URL)) {
            driver.get(BASE_URL);
        }
        
        if (driver.findElements(By.cssSelector("form")).size() > 0) {
            WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
            WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
            WebElement loginButton = driver.findElement(By.cssSelector("button[type=list-submitlist]"));

            email.sendKeys(USERNAME);
            password.sendKeys(PASSWORD);
            loginButton.click();
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dashboard")));
        }
    }
}