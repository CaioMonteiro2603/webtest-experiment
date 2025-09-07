package deepseek.ws09.seq01;

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
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class RealWorldWebTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("a.navbar-brand")));
        Assertions.assertTrue(logo.isDisplayed(), "Conduit logo should be displayed");
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL), 
            "Current URL should match base URL");
    }

    @Test
    @Order(2)
    public void testNavigationBetweenPages() {
        driver.get(BASE_URL);
        
        WebElement globalFeed = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.nav-link[href='/']")));
        globalFeed.click();
        wait.until(ExpectedConditions.urlContains("#/"));
        
        WebElement popularTag = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".tag-list a")));
        String tagName = popularTag.getText();
        popularTag.click();
        
        wait.until(ExpectedConditions.urlContains("#/tag/"));
        WebElement feedTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".feed-toggle .nav-link.active")));
        Assertions.assertTrue(feedTitle.getText().contains(tagName),
            "Tag feed should display the selected tag");
    }

    @Test
    @Order(3)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "#/login");
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        // Invalid credentials test
        emailField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password"),
            "Error message for invalid login should be displayed");
        
        // Successful login test (using test credentials)
        emailField.clear();
        passwordField.clear();
        emailField.sendKeys("testuser@realworld.io");
        passwordField.sendKeys("password123");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("#/"));
        WebElement userProfile = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("a[href*='#/@']")));
        Assertions.assertTrue(userProfile.isDisplayed(), 
            "User profile link should be visible after login");
    }

    @Test
    @Order(4)
    public void testArticleOperations() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")))
            .sendKeys("testuser@realworld.io");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("password123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        // Create new article
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='#/editor']"))).click();
        wait.until(ExpectedConditions.urlContains("#/editor"));
        
        WebElement titleField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[placeholder='Article Title']")));
        titleField.sendKeys("Test Article Title");
        
        driver.findElement(By.cssSelector("input[placeholder*='this article about']"))
            .sendKeys("Test description");
        driver.findElement(By.cssSelector("textarea[placeholder*='article']"))
            .sendKeys("Test article body");
        driver.findElement(By.cssSelector("input[placeholder*='tags']"))
            .sendKeys("test");
        driver.findElement(By.cssSelector("button[type='button']")).click();
        
        WebElement publishButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit']")));
        publishButton.click();
        
        wait.until(ExpectedConditions.urlContains("#/articles/"));
        WebElement articleTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".article-page h1")));
        Assertions.assertEquals("Test Article Title", articleTitle.getText(),
            "Article title should match what was entered");
        
        // Delete article
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".article-actions button.btn-outline-danger")));
        deleteButton.click();
        
        wait.until(ExpectedConditions.urlContains("#/"));
    }

    @Test
    @Order(5)
    public void testUserProfileNavigation() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")))
            .sendKeys("testuser@realworld.io");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("password123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='#/@']")));
        profileLink.click();
        
        wait.until(ExpectedConditions.urlContains("#/@"));
        WebElement profileUsername = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".user-info h4")));
        Assertions.assertTrue(profileUsername.getText().contains("testuser"),
            "Profile page should display the correct username");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        
        String originalWindow = driver.getWindowHandle();
        githubLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), 
            "Github link should open github.com in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}