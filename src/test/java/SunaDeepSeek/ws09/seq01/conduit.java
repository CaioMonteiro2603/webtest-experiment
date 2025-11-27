package SunaDeepSeek.ws09.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class RealWorldAppTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        Assertions.assertTrue(driver.getTitle().contains("Conduit"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.navbar-brand")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/']")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        
        // Test Sign in link
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/login']")));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.text-xs-center")).getText().contains("Sign in"));
        
        // Test Sign up link
        WebElement signUpLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/register']")));
        signUpLink.click();
        wait.until(ExpectedConditions.urlContains("/register"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.text-xs-center")).getText().contains("Sign up"));
    }

    @Test
    @Order(3)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL + "login");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("testuser@example.com");
        passwordField.sendKeys("password");
        signInButton.click();
        
        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.nav-link[href='/settings']")).isDisplayed());
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrongpassword");
        signInButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(5)
    public void testArticleNavigation() {
        // First login
        driver.get(BASE_URL + "login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.sendKeys("testuser@example.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/"));
        
        // Click on first article
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.preview-link:first-child")));
        String articleTitle = articleLink.findElement(By.cssSelector("h1")).getText();
        articleLink.click();
        
        wait.until(ExpectedConditions.urlContains("/article/"));
        Assertions.assertEquals(articleTitle, driver.findElement(By.cssSelector("h1")).getText());
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("a[href*='twitter.com']", "twitter.com");
        
        // Test GitHub link
        testExternalLink("a[href*='github.com']", "github.com");
    }

    private void testExternalLink(String linkSelector, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement externalLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(linkSelector)));
        externalLink.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testSettingsPage() {
        // First login
        driver.get(BASE_URL + "login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.sendKeys("testuser@example.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/"));
        
        // Navigate to settings
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='/settings']")));
        settingsLink.click();
        wait.until(ExpectedConditions.urlContains("/settings"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.text-xs-center")).getText().contains("Your Settings"));
    }

    @Test
    @Order(8)
    public void testLogout() {
        // First login
        driver.get(BASE_URL + "login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.sendKeys("testuser@example.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/"));
        
        // Logout
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[contains(text(), 'Logout')]")));
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.findElements(By.cssSelector("a.nav-link[href='/settings']")).isEmpty());
    }
}

