package deepseek.ws09.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
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
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";

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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".banner")));
        Assertions.assertTrue(banner.isDisplayed());
    }

    @Test
    @Order(2)
    public void testGlobalFeedNavigation() {
        driver.get(BASE_URL);
        WebElement globalFeed = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Global Feed')]")));
        globalFeed.click();
        wait.until(ExpectedConditions.urlContains("/#/"));
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        Assertions.assertTrue(articles.size() > 0);
    }

    @Test
    @Order(3)
    public void testArticleCreation() {
        loginIfNeeded();
        
        WebElement newArticle = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='#/editor']")));
        newArticle.click();
        
        wait.until(ExpectedConditions.urlContains("/editor"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']")))
            .sendKeys("Test Article");
        driver.findElement(By.cssSelector("input[placeholder=\"What's this article about?\"]"))
            .sendKeys("Test Description");
        driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']"))
            .sendKeys("Test content");
        driver.findElement(By.cssSelector("input[placeholder='Enter tags']"))
            .sendKeys("test");
        driver.findElement(By.cssSelector("button[type='button']")).click();
        
        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".article-page h1")));
        Assertions.assertTrue(articleTitle.getText().contains("Test Article"));
    }

    @Test
    @Order(4)
    public void testValidLogin() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")))
            .sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[type='password']"))
            .sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        wait.until(ExpectedConditions.urlContains("/#/"));
        WebElement userProfile = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("a[href='#/@testuser']")));
        Assertions.assertTrue(userProfile.isDisplayed());
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")))
            .sendKeys("invalid@user.com");
        driver.findElement(By.cssSelector("input[type='password']"))
            .sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(6)
    public void testUserProfileNavigation() {
        loginIfNeeded();
        
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='#/@testuser']")));
        profileLink.click();
        
        wait.until(ExpectedConditions.urlContains("/#/@testuser"));
        WebElement profileHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".user-info h4")));
        Assertions.assertTrue(profileHeader.getText().contains(USERNAME));
    }

    @Test
    @Order(7)
    public void testSettingsPage() {
        loginIfNeeded();
        
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='#/settings']")));
        settingsLink.click();
        
        wait.until(ExpectedConditions.urlContains("/#/settings"));
        WebElement settingsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(settingsHeader.getText().contains("Your Settings"));
    }

    @Test
    @Order(8)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test GitHub link instead of About link
        driver.findElement(By.cssSelector("a[href='https://github.com/gothinkster/angular-realworld-example-app']")).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginIfNeeded() {
        if (driver.findElements(By.cssSelector("a[href='#/login']")).size() > 0) {
            driver.get(BASE_URL + "#/login");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")))
                .sendKeys(USERNAME);
            driver.findElement(By.cssSelector("input[type='password']"))
                .sendKeys(PASSWORD);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.urlContains("/#/"));
        }
    }
}