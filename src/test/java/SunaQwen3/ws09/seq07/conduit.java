package deepseek.ws09.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RealWorldTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser@example.com";
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
            By.cssSelector(".banner h1")));
        Assertions.assertEquals("conduit", banner.getText().toLowerCase());
    }

    @Test
    @Order(2)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("h1.preview-link")));
        String articleTitle = articleLink.getText();
        articleLink.click();

        WebElement articleHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.article-title")));
        Assertions.assertEquals(articleTitle, articleHeader.getText());
    }

    @Test
    @Order(3)
    public void testUserRegistration() {
        driver.get(BASE_URL + "register");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Username']")));
        WebElement email = driver.findElement(By.cssSelector("input[placeholder='Email']"));
        WebElement password = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        WebElement signUpButton = driver.findElement(By.cssSelector("button[type='submit']"));

        String timestamp = String.valueOf(System.currentTimeMillis());
        username.sendKeys("testuser" + timestamp);
        email.sendKeys("test" + timestamp + "@example.com");
        password.sendKeys(PASSWORD);
        signUpButton.click();

        WebElement feedTabs = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".feed-toggle")));
        Assertions.assertTrue(feedTabs.isDisplayed());
    }

    @Test
    @Order(4)
    public void testUserLogin() {
        driver.get(BASE_URL + "login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Email']")));
        WebElement password = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        signInButton.click();

        WebElement userProfile = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("a[href*='@']")));
        Assertions.assertTrue(userProfile.isDisplayed());
    }

    @Test
    @Order(5)
    public void testArticleCreation() {
        driver.get(BASE_URL);
        login();
        
        WebElement newArticleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='editor']")));
        newArticleLink.click();

        WebElement title = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Article Title']")));
        WebElement description = driver.findElement(
            By.cssSelector("input[placeholder=\"What's this article about?\"]"));
        WebElement body = driver.findElement(
            By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        WebElement publishButton = driver.findElement(
            By.cssSelector("button[type='button']"));

        title.sendKeys("Test Article");
        description.sendKeys("Test Description");
        body.sendKeys("Test Body");
        publishButton.click();

        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals("Test Article", articleTitle.getText());
    }

    private void login() {
        driver.get(BASE_URL + "login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Email']")));
        WebElement password = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        signInButton.click();
        wait.until(ExpectedConditions.urlContains("/"));
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github']")));
        githubLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        driver.getWindowHandles().forEach(windowHandle -> {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
                driver.close();
            }
        });
        
        driver.switchTo().window(originalWindow);
    }
}