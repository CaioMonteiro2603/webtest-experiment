package deepseek.ws09.seq01;

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
    public void testHomePageNavigation() {
        driver.get(BASE_URL);
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        Assertions.assertTrue(driver.findElement(By.tagName("app-home-page")).isDisplayed(), "Home page should be displayed");
    }

    @Test
    @Order(2)
    public void testSignInFunctionality() {
        driver.get(BASE_URL + "#/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("test@example.com");
        passwordField.sendKeys("password");
        signInButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("h1 a")));
        String articleTitle = articleLink.getText();
        articleLink.click();

        WebElement articleHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals(articleTitle, articleHeader.getText(), "Article title should match the clicked link");
    }

    @Test
    @Order(4)
    public void testFollowUser() {
        driver.get(BASE_URL + "#/login");
        login("test@example.com", "password");

        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("testuser")));
        profileLink.click();
        wait.until(ExpectedConditions.urlContains("testuser"));

        WebElement followButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-outline-secondary")));
        followButton.click();
        WebElement unfollowButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-secondary")));
        Assertions.assertTrue(unfollowButton.isDisplayed(), "Follow button should change to Unfollow");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement gitHubLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("GitHub")));
        
        String originalWindow = driver.getWindowHandle();
        gitHubLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                wait.until(ExpectedConditions.urlContains("github.com"));
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(6)
    public void testTagFiltering() {
        driver.get(BASE_URL);
        WebElement tagLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("dragons")));
        tagLink.click();
        wait.until(ExpectedConditions.urlContains("tag/dragons"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".article-preview")).isDisplayed(), "Articles should be filtered by tag");
    }

    @Test
    @Order(7)
    public void testLogout() {
        driver.get(BASE_URL + "#/login");
        login("test@example.com", "password");

        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Settings")));
        settingsLink.click();
        wait.until(ExpectedConditions.urlContains("settings"));

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-outline-danger")));
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        Assertions.assertTrue(driver.findElement(By.linkText("Sign in")).isDisplayed(), "Should be logged out successfully");
    }

    private void login(String email, String password) {
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(email);
        passwordField.sendKeys(password);
        signInButton.click();
        wait.until(ExpectedConditions.urlContains(BASE_URL));
    }
}