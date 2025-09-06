package geminiPRO.ws09.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * A comprehensive JUnit 5 test suite for the RealWorld "Conduit" demo application.
 * This suite covers the full user and content lifecycle including registration, login,
 * creating, commenting on, and deleting articles.
 * It uses Selenium WebDriver with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RealWorldAppTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

    // --- Test User and Article Details ---
    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final String USERNAME = "gemini-" + UNIQUE_ID;
    private static final String EMAIL = "gemini-" + UNIQUE_ID + "@test.com";
    private static final String PASSWORD = "password123";
    private static final String ARTICLE_TITLE = "My Test Article " + UNIQUE_ID;
    private static final String ARTICLE_SLUG = "my-test-article-" + UNIQUE_ID;

    // --- Locators ---
    // Header
    private static final By SIGN_IN_LINK = By.cssSelector("a[href='#/login']");
    private static final By SIGN_UP_LINK = By.cssSelector("a[href='#/register']");
    private static final By NEW_ARTICLE_LINK = By.cssSelector("a[href='#/editor']");
    private static final By SETTINGS_LINK = By.cssSelector("a[href='#/settings']");

    // Forms
    private static final By USERNAME_INPUT = By.cssSelector("input[placeholder='Username']");
    private static final By EMAIL_INPUT = By.cssSelector("input[type='email']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password']");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");
    private static final By ERROR_MESSAGES = By.cssSelector(".error-messages li");
    
    // Article Editor
    private static final By ARTICLE_TITLE_INPUT = By.cssSelector("input[placeholder='Article Title']");
    private static final By ARTICLE_DESC_INPUT = By.cssSelector("input[placeholder*='description']");
    private static final By ARTICLE_BODY_TEXTAREA = By.cssSelector("textarea[placeholder*='Markdown']");
    private static final By PUBLISH_ARTICLE_BUTTON = By.xpath("//button[contains(text(),'Publish Article')]");
    
    // Article Page
    private static final By ARTICLE_PAGE_TITLE = By.tagName("h1");
    private static final By DELETE_ARTICLE_BUTTON = By.xpath("//button[contains(text(), 'Delete Article')]");
    private static final By COMMENT_TEXTAREA = By.cssSelector("textarea[placeholder='Write a comment...']");
    private static final By POST_COMMENT_BUTTON = By.xpath("//button[text()='Post Comment']");
    private static final By COMMENT_CARD = By.className("card-text");

    // Settings
    private static final By LOGOUT_BUTTON = By.xpath("//button[contains(text(),'Or click here to logout.')]");

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments as required
        driver = new FirefoxDriver(options);
        // Use a longer wait for this SPA which can have slow API responses
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    // Helper to log in
    private void login() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(EMAIL);
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(SUBMIT_BUTTON).click();
        // Wait for user-specific link to appear in header, confirming login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='#/@" + USERNAME + "']")));
    }

    @Test
    @Order(1)
    @DisplayName("🧪 Test New User Registration")
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_UP_LINK)).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(USERNAME);
        driver.findElement(EMAIL_INPUT).sendKeys(EMAIL);
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(SUBMIT_BUTTON).click();

        // After registration, user is logged in and redirected. Verify by looking for a post-login element.
        WebElement newArticleLink = wait.until(ExpectedConditions.visibilityOfElementLocated(NEW_ARTICLE_LINK));
        Assertions.assertTrue(newArticleLink.isDisplayed(), "User should be logged in and see 'New Article' link after registration.");
    }
    
    @Test
    @Order(2)
    @DisplayName("🧪 Test Login with Invalid/Valid Credentials and Logout")
    void testLoginAndLogout() {
        driver.get(BASE_URL + "#/login");

        // --- Invalid Login ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(EMAIL);
        driver.findElement(PASSWORD_INPUT).sendKeys("wrongpassword");
        driver.findElement(SUBMIT_BUTTON).click();
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGES));
        Assertions.assertTrue(errorMessage.getText().contains("is invalid"), "Error message should be shown for invalid credentials.");

        // --- Valid Login ---
        login();
        
        // --- Logout ---
        wait.until(ExpectedConditions.elementToBeClickable(SETTINGS_LINK)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        
        // Verify logout by checking for the "Sign in" link's reappearance
        wait.until(ExpectedConditions.visibilityOfElementLocated(SIGN_IN_LINK));
        Assertions.assertTrue(driver.findElement(SIGN_IN_LINK).isDisplayed(), "'Sign in' link should be visible after logout.");
    }

    @Test
    @Order(3)
    @DisplayName("🧪 Test Article Creation")
    void testCreateArticle() {
        login();
        wait.until(ExpectedConditions.elementToBeClickable(NEW_ARTICLE_LINK)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(ARTICLE_TITLE_INPUT)).sendKeys(ARTICLE_TITLE);
        driver.findElement(ARTICLE_DESC_INPUT).sendKeys("This is a test article description.");
        driver.findElement(ARTICLE_BODY_TEXTAREA).sendKeys("This is the main body of the test article, written in markdown.");
        
        driver.findElement(PUBLISH_ARTICLE_BUTTON).click();

        WebElement titleOnPage = wait.until(ExpectedConditions.visibilityOfElementLocated(ARTICLE_PAGE_TITLE));
        Assertions.assertEquals(ARTICLE_TITLE, titleOnPage.getText(), "Article title on page should match the created title.");
    }

    @Test
    @Order(4)
    @DisplayName("🧪 Test Adding and Deleting a Comment")
    void testAddAndRemoveComment() {
        login();
        driver.get(BASE_URL + "#/article/" + ARTICLE_SLUG);
        
        String commentText = "This is a test comment from Gemini.";
        
        // Add comment
        WebElement commentBox = wait.until(ExpectedConditions.visibilityOfElementLocated(COMMENT_TEXTAREA));
        commentBox.sendKeys(commentText);
        driver.findElement(POST_COMMENT_BUTTON).click();
        
        // Verify comment appears
        WebElement postedComment = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[text()='" + commentText + "']")));
        Assertions.assertTrue(postedComment.isDisplayed(), "Posted comment should be visible.");

        // Delete comment
        WebElement deleteIcon = postedComment.findElement(By.xpath("./ancestor::div[@class='card']//i[contains(@class, 'ion-trash-a')]"));
        wait.until(ExpectedConditions.elementToBeClickable(deleteIcon)).click();

        // Verify comment is gone
        wait.until(ExpectedConditions.invisibilityOf(postedComment));
        List<WebElement> comments = driver.findElements(By.xpath("//p[text()='" + commentText + "']"));
        Assertions.assertTrue(comments.isEmpty(), "Comment should be deleted and no longer visible.");
    }
    
    @Test
    @Order(5)
    @DisplayName("🧪 Test Article Deletion")
    void testDeleteArticle() {
        login();
        driver.get(BASE_URL + "#/article/" + ARTICLE_SLUG);

        wait.until(ExpectedConditions.elementToBeClickable(DELETE_ARTICLE_BUTTON)).click();
        
        // After deletion, we should be redirected to the home page.
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "#/"));
        
        // Verify deletion by checking the user's article list
        driver.get(BASE_URL + "#/@" + USERNAME);
        WebElement noArticlesMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(), 'No articles are here... yet.')]")));
        Assertions.assertTrue(noArticlesMessage.isDisplayed(), "Article should be deleted and not appear on user's profile.");
    }
}