package SunaDeepSeek.ws09.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser123";
    private static final String PASSWORD = "testpass123";

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
    public void testSignInPage() {
        driver.get(BASE_URL + "login");
        wait.until(ExpectedConditions.urlContains("/login"));
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        signInButton.click();
        
        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a[href*='@testuser123']")).isDisplayed());
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        wait.until(ExpectedConditions.urlContains("/login"));
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpass");
        signInButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.preview-link:first-child")));
        String articleTitle = articleLink.findElement(By.tagName("h1")).getText();
        articleLink.click();
        
        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement articleTitleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals(articleTitle, articleTitleElement.getText());
    }

    @Test
    @Order(5)
    public void testProfilePage() {
        driver.get(BASE_URL + "@testuser123");
        wait.until(ExpectedConditions.urlContains("/@testuser123"));
        
        WebElement profileName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h4")));
        Assertions.assertTrue(profileName.getText().contains("testuser123"));
        
        List<WebElement> articles = driver.findElements(By.cssSelector("a.preview-link"));
        Assertions.assertTrue(articles.size() > 0);
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        githubLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testNewArticleCreation() {
        driver.get(BASE_URL + "editor");
        wait.until(ExpectedConditions.urlContains("/editor"));
        
        WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Article Title']")));
        WebElement descriptionField = driver.findElement(
            By.cssSelector("input[placeholder=\"What's this article about?\"]"));
        WebElement bodyField = driver.findElement(
            By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        WebElement publishButton = driver.findElement(
            By.cssSelector("button[type='button']"));
        
        titleField.sendKeys("Test Article Title");
        descriptionField.sendKeys("Test Article Description");
        bodyField.sendKeys("Test Article Body Content");
        publishButton.click();
        
        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals("Test Article Title", articleTitle.getText());
    }

    @Test
    @Order(8)
    public void testLogout() {
        driver.get(BASE_URL);
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='settings']")));
        settingsLink.click();
        
        wait.until(ExpectedConditions.urlContains("/settings"));
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.btn-outline-danger")));
        logoutButton.click();
        
        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.findElements(By.cssSelector("a[href*='login']")).size() > 0);
    }
}