package deepseek.ws09.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

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
        WebElement banner = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".banner h1")));
        assertEquals("conduit", banner.getText().toLowerCase(), 
            "Home page banner should display 'conduit'");
    }

    @Test
    @Order(2)
    public void testUserRegistration() {
        driver.get(BASE_URL + "#/register");
        
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[placeholder='Username']")));
        username.sendKeys("testuser_" + System.currentTimeMillis());
        
        WebElement email = driver.findElement(By.cssSelector("input[placeholder='Email']"));
        email.sendKeys("test" + System.currentTimeMillis() + "@example.com");
        
        WebElement password = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        password.sendKeys("testpass");
        
        WebElement signUpButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signUpButton.click();
        
        WebElement userFeed = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".feed-toggle")));
        assertTrue(userFeed.isDisplayed(), "User feed should be visible after registration");
    }

    @Test
    @Order(3)
    public void testUserLogin() {
        driver.get(BASE_URL + "#/login");
        
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[placeholder='Email']")));
        email.sendKeys("test@example.com");
        
        WebElement password = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        password.sendKeys(PASSWORD);
        
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();
        
        WebElement userFeed = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".feed-toggle")));
        assertTrue(userFeed.isDisplayed(), "User feed should be visible after login");
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");
        
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[placeholder='Email']")));
        email.sendKeys("invalid@example.com");
        
        WebElement password = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        password.sendKeys("wrongpass");
        
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".error-messages")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(5)
    public void testArticleCreation() {
        login();
        
        WebElement newArticle = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='#/editor']")));
        newArticle.click();
        
        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[placeholder='Article Title']")));
        title.sendKeys("Test Article " + System.currentTimeMillis());
        
        WebElement description = driver.findElement(By.cssSelector("input[placeholder=\"What's this article about?\"]"));
        description.sendKeys("Test description");
        
        WebElement body = driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        body.sendKeys("Test article body");
        
        WebElement tags = driver.findElement(By.cssSelector("input[placeholder='Enter tags']"));
        tags.sendKeys("test");
        
        WebElement publishButton = driver.findElement(By.cssSelector("button[type='button']"));
        publishButton.click();
        
        WebElement articleTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".article-page h1")));
        assertTrue(articleTitle.isDisplayed(), "Article page should display after creation");
    }

    @Test
    @Order(6)
    public void testArticleComment() {
        driver.get(BASE_URL + "#/article/test-article");
        login();
        
        WebElement commentField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("textarea[placeholder='Write a comment...']")));
        commentField.sendKeys("Test comment " + System.currentTimeMillis());
        
        WebElement postButton = driver.findElement(By.cssSelector("button[type='submit']"));
        postButton.click();
        
        List<WebElement> comments = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".card")));
        assertTrue(comments.size() > 0, "Comment should appear after posting");
    }

    @Test
    @Order(7)
    public void testProfileNavigation() {
        login();
        
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='#/@testuser']")));
        profileLink.click();
        
        WebElement profileHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".user-info h4")));
        assertEquals(USERNAME, profileHeader.getText(), 
            "Profile page should display correct username");
    }

    @Test
    @Order(8)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        testExternalLink(githubLink, "github.com");
    }

    private void login() {
        if (!driver.getCurrentUrl().contains("#/")) {
            driver.get(BASE_URL + "#/login");
            WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[placeholder='Email']")));
            email.sendKeys("test@example.com");
            
            WebElement password = driver.findElement(By.cssSelector("input[placeholder='Password']"));
            password.sendKeys(PASSWORD);
            
            WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
            signInButton.click();
            
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".feed-toggle")));
        }
    }

    private void testExternalLink(WebElement link, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "External link should open " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}