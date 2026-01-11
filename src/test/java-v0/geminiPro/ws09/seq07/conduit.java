package geminiPro.ws09.seq07;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A comprehensive JUnit 5 test suite for the RealWorld demo application.
 * This suite covers user registration, login, creating and managing articles,
 * commenting, favoriting, and tag-based filtering.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15); // Increased for this slow app

    // Unique data for each test run to ensure isolation
    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final String USERNAME = "gemini-user-" + UNIQUE_ID;
    private static final String EMAIL = "gemini-user-" + UNIQUE_ID + "@example.com";
    private static final String PASSWORD = "password123";
    private static final String ARTICLE_TITLE = "Test Article " + UNIQUE_ID;
    private static final String ARTICLE_TAG = "test-tag-" + UNIQUE_ID;

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static String createdArticleUrl;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    void testUserRegistration() {
        driver.findElement(By.linkText("Sign up")).click();
        wait.until(ExpectedConditions.urlContains("#/register"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Username']"))).sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys(EMAIL);
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//button[text()='Sign up']")).click();

        WebElement userProfileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(., '" + USERNAME + "')]")));
        assertTrue(userProfileLink.isDisplayed(), "Username should be visible in the navbar after registration.");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        // Ensure logged out state
        if (isLoggedIn()) {
            performLogout();
        }

        driver.findElement(By.linkText("Sign in")).click();
        wait.until(ExpectedConditions.urlContains("#/login"));

        performLogin(EMAIL, "wrong-password");

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".swal-title")));
        assertEquals("Login failed!", errorMessage.getText(), "Error message for invalid credentials should be shown.");
        driver.findElement(By.cssSelector(".swal-button--confirm")).click(); // Close popup
    }

    @Test
    @Order(3)
    void testSuccessfulLoginAndLogout() {
        driver.findElement(By.linkText("Sign in")).click();
        wait.until(ExpectedConditions.urlContains("#/login"));
        performLogin(EMAIL, PASSWORD);

        WebElement userProfileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(., '" + USERNAME + "')]")));
        assertTrue(userProfileLink.isDisplayed(), "Username should be visible after successful login.");
        
        performLogout();
        
        WebElement signInLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in")));
        assertTrue(signInLink.isDisplayed(), "Sign in link should be visible after logout.");
    }
    
    @Test
    @Order(4)
    void testCreateAndPublishArticle() {
        ensureLoggedIn();
        driver.findElement(By.cssSelector("a[href='#/editor']")).click();
        wait.until(ExpectedConditions.urlContains("#/editor"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']"))).sendKeys(ARTICLE_TITLE);
        driver.findElement(By.cssSelector("input[placeholder*='this article about']")).sendKeys("About testing");
        driver.findElement(By.cssSelector("textarea[placeholder*='Write your article']")).sendKeys("This is the article body.");
        driver.findElement(By.cssSelector("input[placeholder*='Enter tags']")).sendKeys(ARTICLE_TAG);
        driver.findElement(By.xpath("//button[contains(text(), 'Publish Article')]")).click();

        wait.until(ExpectedConditions.urlContains("#/articles/"));
        WebElement articleTitleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals(ARTICLE_TITLE, articleTitleElement.getText(), "Article title on the page should match the created title.");
        createdArticleUrl = driver.getCurrentUrl(); // Save URL for subsequent tests
    }

    @Test
    @Order(5)
    void testAddAndRemoveComment() {
        ensureLoggedIn();
        driver.get(createdArticleUrl); // Navigate to the created article

        String commentText = "This is a test comment. " + UNIQUE_ID;
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea[placeholder*='Write a comment']"))).sendKeys(commentText);
        driver.findElement(By.xpath("//button[text()='Post Comment']")).click();

        WebElement postedComment = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[text()='" + commentText + "']")));
        assertTrue(postedComment.isDisplayed(), "The new comment should be visible on the page.");

        WebElement commentCard = postedComment.findElement(By.xpath("./ancestor::div[@class='card']"));
        WebElement deleteButton = commentCard.findElement(By.cssSelector(".mod-options .ion-trash-a"));
        deleteButton.click();

        wait.until(ExpectedConditions.invisibilityOf(postedComment));
        assertFalse(isElementPresent(By.xpath("//p[text()='" + commentText + "']")), "Comment should be removed after deletion.");
    }
    
    @Test
    @Order(6)
    void testFavoriteAndUnfavoriteArticle() {
        ensureLoggedIn();
        driver.get(createdArticleUrl);

        WebElement favoriteButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Favorite Article')]")));
        favoriteButton.click();

        WebElement unfavoriteButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Unfavorite Article')]")));
        assertTrue(unfavoriteButton.getText().contains("1"), "Favorite count should be 1.");

        driver.findElement(By.linkText(USERNAME)).click();
        wait.until(ExpectedConditions.urlContains("#/@" + USERNAME));
        driver.findElement(By.linkText("Favorited Articles")).click();

        WebElement favoritedArticleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='" + ARTICLE_TITLE + "']")));
        assertTrue(favoritedArticleTitle.isDisplayed(), "Article should appear in the favorited list.");
        
        // Unfavorite
        favoritedArticleTitle.click();
        wait.until(ExpectedConditions.urlContains("#/articles/"));
        WebElement unfavBtnOnArticlePage = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Unfavorite Article')]")));
        unfavBtnOnArticlePage.click();

        WebElement favBtnAgain = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Favorite Article')]")));
        assertTrue(favBtnAgain.getText().contains("0"), "Favorite count should return to 0.");
    }
    
    @Test
    @Order(7)
    void testFilterByTag() {
        ensureLoggedIn();
        driver.get(BASE_URL); // Go to homepage
        
        WebElement tagLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='" + ARTICLE_TAG + "']")));
        tagLink.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@class='nav-link active' and contains(text(), '" + ARTICLE_TAG + "')]")));
        assertTrue(driver.getCurrentUrl().contains("#/tag/" + ARTICLE_TAG), "URL should reflect tag filtering.");

        WebElement articleInList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='" + ARTICLE_TITLE + "']")));
        assertTrue(articleInList.isDisplayed(), "Article should be visible in the tag-filtered feed.");
    }
    
    @Test
    @Order(8)
    void testExternalThinksterLinkInFooter() {
        String originalWindow = driver.getWindowHandle();
        
        WebElement thinksterLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Thinkster')]")));
        thinksterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("thinkster.io"));
        assertTrue(driver.getCurrentUrl().contains("thinkster.io"), "Should have opened the Thinkster website.");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should have returned to the RealWorld app.");
    }
    
    // --- Helper Methods ---

    private void performLogin(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']"))).sendKeys(email);
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
        driver.findElement(By.xpath("//button[text()='Sign in']")).click();
    }
    
    private void ensureLoggedIn() {
        if (!isLoggedIn()) {
            driver.findElement(By.linkText("Sign in")).click();
            wait.until(ExpectedConditions.urlContains("#/login"));
            performLogin(EMAIL, PASSWORD);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(USERNAME)));
        }
    }

    private boolean isLoggedIn() {
        List<WebElement> userLinks = driver.findElements(By.xpath("//a[contains(., '" + USERNAME + "')]"));
        return !userLinks.isEmpty();
    }
    
    private void performLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#/settings']"))).click();
        wait.until(ExpectedConditions.urlContains("#/settings"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'logout')]"))).click();
    }
    
    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}