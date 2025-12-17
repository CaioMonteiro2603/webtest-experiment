package geminiPro.ws09.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

/**
 * A complete JUnit 5 test suite for the RealWorld demo application (Conduit)
 * using Selenium WebDriver with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://demo.realworld.io/";

    // --- Dynamic Test Data ---
    private static String testUsername;
    private static String testEmail;
    private static final String TEST_PASSWORD = "Password123!";
  

    // --- Selenium WebDriver ---
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private static final By SIGN_UP_LINK = By.xpath("//a[@href='#/register']");
    private static final By SIGN_IN_LINK = By.xpath("//a[@href='#/login']");
    private static final By USERNAME_INPUT = By.cssSelector("input[placeholder='Username']");
    private static final By EMAIL_INPUT = By.cssSelector("input[placeholder='Email']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[placeholder='Password']");
    private static final By SIGN_UP_BUTTON = By.xpath("//button[text()='Sign up']");
    private static final By SIGN_IN_BUTTON = By.xpath("//button[text()='Sign in']");
    private static final By NEW_ARTICLE_LINK = By.xpath("//a[contains(@href,'#/editor')]");
    private static final By SETTINGS_LINK = By.xpath("//a[contains(@href,'#/settings')]");
    private static final By LOGOUT_BUTTON = By.xpath("//button[contains(text(),'Or click here to logout.')]");
    private static final By GITHUB_LINK = By.xpath("//a[contains(text(),'Fork on GitHub')]");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        // This is a Single Page Application (SPA), so waits need to be robust
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        long timestamp = System.currentTimeMillis();
        testUsername = "gemini" + timestamp;
        testEmail = "gemini" + timestamp + "@test.com";
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_UP_LINK)).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(testUsername);
        driver.findElement(EMAIL_INPUT).sendKeys(testEmail);
        driver.findElement(PASSWORD_INPUT).sendKeys(TEST_PASSWORD);
        driver.findElement(SIGN_UP_BUTTON).click();
        
        // After registration, the user's name should appear in the nav bar
        By userProfileLink = By.xpath("//a[contains(@href,'#/@" + testUsername + "')]");
        WebElement profileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(userProfileLink));
        Assertions.assertTrue(profileLink.isDisplayed(), "User profile link not found after registration.");
    }

    @Test
    @Order(2)
    void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(testEmail);
        driver.findElement(PASSWORD_INPUT).sendKeys("WrongPassword");
        driver.findElement(SIGN_IN_BUTTON).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".swal-title")));
        Assertions.assertEquals("Login failed!", errorMessage.getText(), "Error message for invalid login is incorrect.");
        driver.findElement(By.cssSelector(".swal-button--confirm")).click(); // Dismiss modal
    }
    
    @Test
    @Order(3)
    void testSuccessfulLoginAndLogout() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(testEmail);
        driver.findElement(PASSWORD_INPUT).sendKeys(TEST_PASSWORD);
        driver.findElement(SIGN_IN_BUTTON).click();
        
        // "Your Feed" tab is a good indicator of a successful login
        WebElement yourFeedTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[text()='Your Feed']")));
        Assertions.assertTrue(yourFeedTab.isDisplayed(), "'Your Feed' tab not visible after login.");
        
        // Logout
        driver.findElement(SETTINGS_LINK).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        
        // "Sign in" link reappearing is a good indicator of a successful logout
        wait.until(ExpectedConditions.visibilityOfElementLocated(SIGN_IN_LINK));
        Assertions.assertTrue(driver.findElement(SIGN_IN_LINK).isDisplayed(), "Sign In link not visible after logout.");
    }
    
    @Test
    @Order(4)
    void testCreateCommentAndCleanUpArticle() {
        // --- 1. Login ---
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(testEmail);
        driver.findElement(PASSWORD_INPUT).sendKeys(TEST_PASSWORD);
        driver.findElement(SIGN_IN_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(NEW_ARTICLE_LINK));

        // --- 2. Create Article ---
        driver.findElement(NEW_ARTICLE_LINK).click();
        String articleTitle = "Test Article by " + testUsername;
        String articleBody = "This is the body of the test article.";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']"))).sendKeys(articleTitle);
        driver.findElement(By.cssSelector("input[placeholder*='what this article is about']")).sendKeys("A test");
        driver.findElement(By.cssSelector("textarea[placeholder*='Write your article']")).sendKeys(articleBody);
        driver.findElement(By.xpath("//button[contains(text(),'Publish Article')]")).click();

        // --- 3. Verify Article Creation and Add Comment ---
        WebElement titleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals(articleTitle, titleElement.getText(), "Article title is incorrect after creation.");
        
        String commentText = "This is a test comment.";
        driver.findElement(By.cssSelector("textarea[placeholder*='Write a comment']")).sendKeys(commentText);
        driver.findElement(By.xpath("//button[contains(text(),'Post Comment')]")).click();
        
        WebElement postedComment = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[text()='" + commentText + "']")));
        Assertions.assertTrue(postedComment.isDisplayed(), "Posted comment not found.");

        // --- 4. Clean Up: Delete Article ---
        
        driver.findElement(By.xpath("//button[contains(text(),'Delete Article')]")).click();
        
        // The app should redirect to the home page.
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "#/"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("#/"), "Did not redirect to home page after deleting article.");
    }
    
    @Test
    @Order(5)
    void testExternalGitHubLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        wait.until(ExpectedConditions.elementToBeClickable(GITHUB_LINK)).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);
        
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com/gothinkster/realworld"), "The GitHub link did not navigate to the correct repository.");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Did not return to the RealWorld app page.");
    }
}