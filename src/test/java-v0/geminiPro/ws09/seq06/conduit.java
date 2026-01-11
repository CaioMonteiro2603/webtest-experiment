package geminiPro.ws09.seq06;

import org.junit.jupiter.api.AfterAll;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 test suite for the RealWorld "Conduit" demo application.
 * This suite covers the full user lifecycle including registration, login,
 * creating, editing, commenting on, and deleting articles.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15);

    // Static variables to hold state (user credentials, article details) across ordered tests
    private static String testUsername;
    private static String testEmail;
    private static String testPassword = "password123";
    private static String testArticleTitle = "My Test Article Title " + System.currentTimeMillis();
    private static String testArticleBody = "This is the main body of the test article.";
    private static String testArticleComment = "This is a great test comment.";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use headless mode via arguments ONLY
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        
        long timestamp = System.currentTimeMillis();
        testUsername = "geminiUser" + timestamp;
        testEmail = "gemini.user." + timestamp + "@test.com";
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Registers a new, unique user for the test run. Registration automatically logs the user in.
     */
    @Test
    @Order(1)
    void userRegistrationTest() {
        driver.get(BASE_URL + "#/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Username']"))).sendKeys(testUsername);
        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys(testEmail);
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(testPassword);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Verify successful registration by checking for the username in the nav bar
        WebElement userProfileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//ul[contains(@class, 'navbar-nav')]//a[contains(., '" + testUsername + "')]")));
        assertTrue(userProfileLink.isDisplayed(), "User registration failed or user was not logged in automatically.");
    }

    /**
     * Tests logging out and then logging back in with both invalid and valid credentials.
     */
    @Test
    @Order(2)
    void loginAndLogoutTest() {
        // First, log out from the session created by registration
        driver.get(BASE_URL + "#/settings");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Or click here to logout.')]"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='#/login']"))); // Wait for "Sign in" link

        // Test Failed Login
        driver.get(BASE_URL + "#/login");
        performLogin(testEmail, "wrongpassword");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-messages")));
        assertTrue(errorMessage.getText().contains("is invalid"), "Error message for invalid login was not found.");

        // Test Successful Login
        performLogin(testEmail, testPassword);
        WebElement userProfileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//ul[contains(@class, 'navbar-nav')]//a[contains(., '" + testUsername + "')]")));
        assertTrue(userProfileLink.isDisplayed(), "Successful login failed.");
    }

    /**
     * Creates a new article as the logged-in user.
     */
    @Test
    @Order(3)
    void createArticleTest() {
        loginAsTestUser();
        driver.get(BASE_URL + "#/editor");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']"))).sendKeys(testArticleTitle);
        driver.findElement(By.cssSelector("input[placeholder*='What']")).sendKeys("About Testing");
        driver.findElement(By.cssSelector("textarea[placeholder*='Write your article']")).sendKeys(testArticleBody);
        driver.findElement(By.cssSelector("input[placeholder='Enter tags']")).sendKeys("testing");
        driver.findElement(By.cssSelector("button[type='button']")).click(); // Publish Article

        // Verify the article page is displayed correctly
        WebElement articleHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals(testArticleTitle, articleHeader.getText(), "Article title on the page does not match the created title.");
    }
    
    /**
     * Posts a comment on the previously created article.
     */
    @Test
    @Order(4)
    void postCommentOnArticleTest() {
        loginAsTestUser();
        // Navigate directly to the article page
        driver.get(BASE_URL + "#/articles/" + sanitizeForUrl(testArticleTitle));

        // Post a comment
        WebElement commentInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea[placeholder='Write a comment...']")));
        commentInput.sendKeys(testArticleComment);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Post Comment']"))).click();
        
        // Verify the comment appears
        WebElement postedComment = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//p[@class='card-text' and text()='" + testArticleComment + "']")));
        assertTrue(postedComment.isDisplayed(), "The posted comment was not found on the page.");
    }


    /**
     * Deletes the article created in the previous test.
     */
    @Test
    @Order(5)
    void deleteArticleTest() {
        loginAsTestUser();
        driver.get(BASE_URL + "#/articles/" + sanitizeForUrl(testArticleTitle));

        // Click delete and accept the confirmation alert
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Delete Article')]"))).click();
        driver.switchTo().alert().accept();

        // Verify redirection to home page
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "#/"));
        
        // Verify the article is gone from the user's profile
        driver.get(BASE_URL + "#/@" + testUsername);
        WebElement noArticlesMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(., 'No articles are here... yet.')]")));
        assertTrue(noArticlesMessage.isDisplayed(), "Article was not successfully deleted from user profile.");
    }

    /**
     * Verifies that the external "Thinkster" link in the footer opens correctly.
     */
    @Test
    @Order(6)
    void externalLinkTest() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        WebElement thinksterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[@href='https://thinkster.io']")));
        thinksterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("thinkster.io"));
        assertTrue(driver.getCurrentUrl().contains("thinkster.io"), "URL of the new tab should contain 'thinkster.io'.");

        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals(1, driver.getWindowHandles().size(), "Should have switched back to the original window.");
    }
    
    // --- Helper Methods ---

    private void performLogin(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Email']"))).sendKeys(email);
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }
    
    private void loginAsTestUser() {
        driver.get(BASE_URL + "#/login");
        performLogin(testEmail, testPassword);
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//ul[contains(@class, 'navbar-nav')]//a[contains(., '" + testUsername + "')]")));
    }
    
    private String sanitizeForUrl(String text) {
        return text.toLowerCase().replaceAll("\\s+", "-");
    }
}