package deepseek.ws09.seq08;

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
public class conduit {
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass123";
    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testHomePageLoading() {
        driver.get(BASE_URL);
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".banner")));
        Assertions.assertTrue(banner.isDisplayed(), "Home page banner should be visible");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign in")));
        signIn.click();
        
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='email']")));
        email.sendKeys(USERNAME);
        
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement userProfile = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("a[href*='@" + USERNAME + "']")));
        Assertions.assertTrue(userProfile.isDisplayed(), "User profile link should appear after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign in")).click();
        
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='email']")));
        email.sendKeys("invalid@user.com");
        
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("wrongpass");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-messages")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        loginIfNeeded();
        
        // Click on first article
        WebElement firstArticle = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".article-preview h1")));
        firstArticle.click();
        
        WebElement articleContent = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".article-page .banner")));
        Assertions.assertTrue(articleContent.isDisplayed(), "Article page should load");
    }

    @Test
    @Order(5)
    public void testCreateArticle() {
        loginIfNeeded();
        
        driver.findElement(By.linkText("New Article")).click();
        
        WebElement title = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Article Title']")));
        title.sendKeys("Test Article");
        
        driver.findElement(By.cssSelector("input[placeholder='What\\'s this article about?']"))
            .sendKeys("Test Description");
        
        driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']"))
            .sendKeys("Test content");
        
        driver.findElement(By.cssSelector("input[placeholder='Enter tags']"))
            .sendKeys("test");
        
        driver.findElement(By.cssSelector("button[type='button']")).click();
        
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".article-page h1")));
        Assertions.assertEquals("Test Article", articleTitle.getText(), 
            "Created article title should match");
    }

    @Test
    @Order(6)
    public void testProfilePage() {
        loginIfNeeded();
        
        driver.findElement(By.cssSelector("a[href*='@" + USERNAME + "']")).click();
        
        WebElement profileHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".profile-page h4")));
        Assertions.assertTrue(profileHeader.getText().contains(USERNAME), 
            "Profile page should display username");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter']")));
        twitterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"),
            "Twitter link should open in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("/#/")) {
            driver.get(BASE_URL);
            driver.findElement(By.linkText("Sign in")).click();
            WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='email']")));
            email.sendKeys(USERNAME);
            driver.findElement(By.cssSelector("input[type='password']")).sendKeys(PASSWORD);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("a[href*='@" + USERNAME + "']")));
        }
    }
}