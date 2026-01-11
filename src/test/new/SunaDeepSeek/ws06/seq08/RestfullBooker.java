package SunaDeepSeek.ws06.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Shady"));
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals("Welcome to Shady Meadows B&B", header.getText());
    }

    @Test
    @Order(2)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[data-testid='username']")));
        usernameField.sendKeys(USERNAME);
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[data-testid='password']"));
        passwordField.sendKeys(PASSWORD);
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[data-testid='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("admin"));
        WebElement roomsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h2")));
        Assertions.assertTrue(roomsHeader.getText().contains("Rooms"));
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get(BASE_URL + "#/admin");
        login();
        
        // Test burger menu toggle
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")));
        menuButton.click();
        
        // Test All Items link
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='room']")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("admin"));
        
        // Test About link (external)
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='about']")));
        aboutLink.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Logout
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='logout']")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("login"));
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Twitter link
        testExternalLink(By.cssSelector("a[href*='twitter']"), "twitter.com");
        
        // Facebook link
        testExternalLink(By.cssSelector("a[href*='facebook']"), "facebook.com");
        
        // LinkedIn link
        testExternalLink(By.cssSelector("a[href*='linkedin']"), "linkedin.com");
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/admin");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[data-testid='username']")));
        usernameField.sendKeys("invalid");
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[data-testid='password']"));
        passwordField.sendKeys("invalid");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[data-testid='submit']"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert")));
        Assertions.assertTrue(errorMessage.getText().contains("Bad credentials"));
    }

    private void login() {
        if (driver.getCurrentUrl().contains("login")) {
            WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[data-testid='username']")));
            usernameField.sendKeys(USERNAME);
            
            WebElement passwordField = driver.findElement(By.cssSelector("input[data-testid='password']"));
            passwordField.sendKeys(PASSWORD);
            
            WebElement loginButton = driver.findElement(By.cssSelector("button[data-testid='submit']"));
            loginButton.click();
            
            wait.until(ExpectedConditions.urlContains("admin"));
        }
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}