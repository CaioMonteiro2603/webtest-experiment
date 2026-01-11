package deepseek.ws10.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com";
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
    public void testValidLogin() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.sendKeys(USERNAME);
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        passwordField.sendKeys(PASSWORD);
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".dashboard-container")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.sendKeys("invalid@example.com");
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        passwordField.sendKeys("wrongpassword");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid credentials"));
    }

    @Test
    @Order(3)
    public void testDashboardNavigation() {
        testValidLogin();
        
        WebElement reportsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='/reports']")));
        reportsLink.click();
        
        wait.until(ExpectedConditions.urlContains("/reports"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".reports-container")).isDisplayed());
    }

    @Test
    @Order(4)
    public void testUserManagementNavigation() {
        testValidLogin();
        
        WebElement usersLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='/users']")));
        usersLink.click();
        
        wait.until(ExpectedConditions.urlContains("/users"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".users-container")).isDisplayed());
    }

    @Test
    @Order(5)
    public void testLogout() {
        testValidLogin();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='/logout']")));
        logoutButton.click();
        
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/login"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("button[type='submit']")).isDisplayed());
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        WebElement supportLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='support']")));
        supportLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("support"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}