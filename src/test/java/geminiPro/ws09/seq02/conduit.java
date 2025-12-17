package geminiPro.ws09.seq02;

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
 * A complete JUnit 5 test suite for the RealWorld "Conduit" demo application.
 * This test uses Selenium WebDriver with Firefox in headless mode and covers
 * user registration, login, article creation/editing/deletion, and commenting.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    // Constants for configuration
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // WebDriver and WebDriverWait instances shared across all tests
    private static WebDriver driver;
    private static WebDriverWait wait;

    // Static user data to be generated once for tests that might depend on a common user
    private static String testUsername;
    private static String testEmail;
    private static String testPassword;

    // --- WebDriver Lifecycle ---

    @BeforeAll
    static void setup() {
        // As per requirements, initialize Firefox in headless mode via arguments
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);

        // Generate unique user credentials for the test run
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        testUsername = "testuser-" + uniqueId;
        testEmail = "testuser-" + uniqueId + "@example.com";
        testPassword = "password123";
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        driver.get(BASE_URL);
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    @DisplayName("Should register a new user successfully and be logged in")
    void testUserRegistration() {
        registerUser(testUsername, testEmail, testPassword);
        
        // After registration, user is automatically logged in.
        // Verify the username appears in the navbar.
        By userProfileLink = By.linkText(testUsername);
        WebElement profileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(userProfileLink));
        Assertions.assertTrue(profileLink.isDisplayed(), "Username should be visible in the navbar after registration.");
    }

    @Test
    @Order(2)
    @DisplayName("Should log out and log back in successfully")
    void testLoginAndLogout() {
        // This test assumes the user from test 1 is still conceptually "registered"
        // For independence, we ensure login works from a logged-out state.
        // We log in first, then log out, then log back in.
        performLogin(testEmail, testPassword);
        WebElement profileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(testUsername)));
        Assertions.assertTrue(profileLink.isDisplayed(), "Login before logout failed.");

        performLogout();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in")));

        performLogin(testEmail, testPassword);
        profileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(testUsername)));
        Assertions.assertTrue(profileLink.isDisplayed(), "Username should be visible after logging back in.");
    }

    @Test
    @Order(3)
    @DisplayName("Should create, edit, and then delete an article")
    void testCreateEditDeleteArticle() {
        performLogin(testEmail, testPassword);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(testUsername)));

        // --- Create Article ---
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("New Article"))).click();
        
        String articleTitle = "My Test Article " + UUID.randomUUID().toString().substring(0, 8);
        String articleBody = "This is the body of the test article.";
        String updatedBody = "This is the updated body of the article.";
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']"))).sendKeys(articleTitle);
        driver.findElement(By.cssSelector("input[placeholder*='this article about']")).sendKeys("Testing");
        driver.findElement(By.cssSelector("textarea[placeholder*='Write your article']")).sendKeys(articleBody);
        driver.findElement(By.xpath("//button[text()='Publish Article']")).click();
        
        // --- Verify Creation ---
        WebElement titleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals(articleTitle, titleElement.getText(), "Article title after creation is incorrect.");

        // --- Edit Article ---
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Edit Article"))).click();
        WebElement bodyTextarea = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea[placeholder*='Write your article']")));
        bodyTextarea.clear();
        bodyTextarea.sendKeys(updatedBody);
        driver.findElement(By.xpath("//button[text()='Publish Article']")).click();

        // --- Verify Edit ---
        WebElement updatedBodyElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='article-content']//p")));
        Assertions.assertEquals(updatedBody, updatedBodyElement.getText(), "Article body was not updated correctly.");

        // --- Delete Article ---
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Delete Article')]"))).click();
        // The page automatically redirects to home after deletion.
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "#/"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("#/"), "Should be redirected to home page after deleting article.");
    }

    @Test
    @Order(4)
    @DisplayName("Should filter articles by a popular tag")
    void testFilterArticlesByTag() {
        List<WebElement> tags = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".sidebar .tag-list a"), 0));
        WebElement firstTag = tags.get(0);
        String tagName = firstTag.getText();
        
        firstTag.click();
        
        WebElement activeTagTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='feed-toggle']//a[contains(@class, 'active')]")));
        Assertions.assertEquals(tagName, activeTagTab.getText(), "The active tab should show the selected tag name.");
    }

    @Test
    @Order(5)
    @DisplayName("Should post a comment on an article")
    void testPostCommentOnArticle() {
        performLogin(testEmail, testPassword);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(testUsername)));

        // Find and click the first article in the Global Feed
        List<WebElement> articles = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".article-preview"), 0));
        articles.get(0).click();

        // Write and post a comment
        String commentText = "This is a test comment. " + UUID.randomUUID().toString().substring(0, 8);
        WebElement commentInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form.comment-form textarea")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", commentInput);
        commentInput.sendKeys(commentText);
        driver.findElement(By.xpath("//button[text()='Post Comment']")).click();
        
        // Verify the comment appears in the list
        WebElement newComment = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[text()='" + commentText + "']")));
        Assertions.assertTrue(newComment.isDisplayed(), "The new comment was not found on the page.");
    }

    // --- Helper Methods ---
    
    private void registerUser(String username, String email, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign up"))).click();
        wait.until(ExpectedConditions.urlContains("#/register"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Username']"))).sendKeys(username);
        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys(email);
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }
    
    private void performLogin(String email, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in"))).click();
        wait.until(ExpectedConditions.urlContains("#/login"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Email']"))).sendKeys(email);
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    private void performLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Settings"))).click();
        wait.until(ExpectedConditions.urlContains("#/settings"));
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Or click here to logout.')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", logoutButton);
        logoutButton.click();
    }
}