package Qwen3.ws09.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RealWorldTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testPageLoad() {
        driver.get("https://demo.realworld.io/");
        assertEquals("Conduit", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("demo.realworld.io"));
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get("https://demo.realworld.io/");
        
        // Click Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().contains("/"));
        
        // Click Sign In link
        driver.get("https://demo.realworld.io/");
        WebElement signInLink = driver.findElement(By.linkText("Sign in"));
        signInLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
        
        // Navigate back to home
        driver.get("https://demo.realworld.io/");
        
        // Click Sign Up link
        WebElement signUpLink = driver.findElement(By.linkText("Sign up"));
        signUpLink.click();
        wait.until(ExpectedConditions.urlContains("/register"));
        assertTrue(driver.getCurrentUrl().contains("/register"));
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        driver.get("https://demo.realworld.io/login");
        
        // Fill login form
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.sendKeys("test@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        passwordField.sendKeys("password");
        
        // Submit login
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Sign in')]"));
        loginButton.click();
        
        // Verify successful login
        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().contains("/"));
        assertTrue(driver.getTitle().contains("Conduit"));
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get("https://demo.realworld.io/login");
        
        // Fill invalid login form
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        passwordField.sendKeys("invalidpassword");
        
        // Submit login
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Sign in')]"));
        loginButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".error-messages")).getText().contains("email or password"));
    }

    @Test
    @Order(5)
    public void testArticleList() {
        driver.get("https://demo.realworld.io/");
        
        // Verify articles are loaded
        WebElement articlesContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-list")));
        assertTrue(articlesContainer.isDisplayed());
        
        // Check if articles are present
        if (driver.findElements(By.cssSelector(".article-preview")).size() > 0) {
            WebElement firstArticle = driver.findElement(By.cssSelector(".article-preview"));
            assertTrue(firstArticle.isDisplayed());
        }
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get("https://demo.realworld.io/");
        
        // Click Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("[href*='twitter']"));
        twitterLink.click();
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
        
        // Click Facebook link
        driver.get("https://demo.realworld.io/");
        WebElement facebookLink = driver.findElement(By.cssSelector("[href*='facebook']"));
        facebookLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
        
        // Click LinkedIn link
        driver.get("https://demo.realworld.io/");
        WebElement linkedinLink = driver.findElement(By.cssSelector("[href*='linkedin']"));
        linkedinLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }

    @Test
    @Order(7)
    public void testCreateArticle() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to login first
        WebElement signInLink = driver.findElement(By.linkText("Sign in"));
        signInLink.click();
        
        // Login
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.sendKeys("test@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        passwordField.sendKeys("password");
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Sign in')]"));
        loginButton.click();
        
        // Wait for login
        wait.until(ExpectedConditions.urlContains("/"));
        
        // Click "New Article" button
        WebElement newArticleButton = driver.findElement(By.linkText("New article"));
        newArticleButton.click();
        
        // Wait for editor page
        wait.until(ExpectedConditions.urlContains("/editor"));
        assertTrue(driver.getCurrentUrl().contains("/editor"));
        
        // Fill article form
        WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Article Title']")));
        titleField.sendKeys("Test Article Title");
        WebElement descriptionField = driver.findElement(By.cssSelector("input[placeholder='What's this article about?']"));
        descriptionField.sendKeys("Test article description");
        WebElement contentField = driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        contentField.sendKeys("This is the content of the test article.");
        WebElement tagField = driver.findElement(By.cssSelector("input[placeholder='Enter tags']"));
        tagField.sendKeys("test");
        
        // Submit article
        WebElement publishButton = driver.findElement(By.xpath("//button[contains(text(), 'Publish Article')]"));
        publishButton.click();
        
        // Verify article published
        wait.until(ExpectedConditions.urlContains("/article"));
        assertTrue(driver.getCurrentUrl().contains("/article"));
    }

    @Test
    @Order(8)
    public void testUserProfile() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to login first
        WebElement signInLink = driver.findElement(By.linkText("Sign in"));
        signInLink.click();
        
        // Login
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.sendKeys("test@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        passwordField.sendKeys("password");
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Sign in')]"));
        loginButton.click();
        
        // Wait for login
        wait.until(ExpectedConditions.urlContains("/"));
        
        // Click profile link (assuming it's in the top right)
        WebElement profileLink = driver.findElement(By.linkText("test@example.com"));
        profileLink.click();
        
        // Wait for profile page
        wait.until(ExpectedConditions.urlContains("/profile"));
        assertTrue(driver.getCurrentUrl().contains("/profile"));
        
        // Verify profile page loaded
        assertTrue(driver.getTitle().contains("Conduit"));
    }

    @Test
    @Order(9)
    public void testArticleComments() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to a specific article or use the first one
        if (driver.findElements(By.cssSelector(".article-preview")).size() > 0) {
            WebElement firstArticle = driver.findElement(By.cssSelector(".article-preview"));
            firstArticle.click();
            
            // Wait for article page
            wait.until(ExpectedConditions.urlContains("/article"));
            assertTrue(driver.getCurrentUrl().contains("/article"));
            
            // Check if comments section exists
            WebElement commentsSection = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".comment-list")));
            assertTrue(commentsSection.isDisplayed());
        }
    }

    @Test
    @Order(10)
    public void testResetFunctionality() {
        driver.get("https://demo.realworld.io/");
        
        // Navigate to login
        WebElement signInLink = driver.findElement(By.linkText("Sign in"));
        signInLink.click();
        
        // Fill login form
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.sendKeys("test@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        passwordField.sendKeys("password");
        
        // Click reset (clear form)
        WebElement resetButton = driver.findElement(By.xpath("//button[contains(text(), 'Reset')]"));
        if (resetButton != null) {
            resetButton.click();
            
            // Verify fields are cleared
            assertEquals("", emailField.getAttribute("value"));
            assertEquals("", passwordField.getAttribute("value"));
        }
    }
}