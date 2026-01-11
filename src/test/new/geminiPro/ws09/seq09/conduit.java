package geminiPro.ws09.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for the RealWorld demo application (Conduit).
 * This suite covers user registration, login, settings management, and the full
 * end-to-end flow of creating, commenting on, and deleting an article.
 * It uses Selenium WebDriver with headless Firefox.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static String testUsername;
    private static String testEmail;
    private static final String testPassword = "Password123!";

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private final By signUpLink = By.cssSelector("a[href='#/register']");
    private final By signInLink = By.cssSelector("a[href='#/login']");
    private final By newArticleLink = By.cssSelector("a[href='#/editor']");
    private final By settingsLink = By.cssSelector("a[href='#/settings']");
    private final By usernameInput = By.cssSelector("input[placeholder='Username']");
    private final By emailInput = By.cssSelector("input[placeholder='Email']");
    private final By passwordInput = By.cssSelector("input[placeholder='Password']");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By errorMessageList = By.cssSelector(".error-messages li");
    private final By articleTitleInput = By.cssSelector("input[placeholder='Article Title']");
    private final By articleAboutInput = By.cssSelector("input[placeholder=\"What's this article about?\"]");
    private final By articleBodyTextarea = By.cssSelector("textarea[placeholder='Write your article (in markdown)']");
    private final By articleTagsInput = By.cssSelector("input[placeholder='Enter tags']");
    private final By publishArticleButton = By.xpath("//button[contains(text(), 'Publish Article')]");
    private final By deleteArticleButton = By.xpath("//button[contains(text(), 'Delete Article')]");
    private final By commentTextarea = By.cssSelector("textarea[placeholder='Write a comment...']");
    private final By postCommentButton = By.xpath("//button[text()='Post Comment']");

    @BeforeAll
    static void setup() {
        long timestamp = System.currentTimeMillis();
        testUsername = "testuser" + timestamp;
        testEmail = "testuser" + timestamp + "@example.com";

        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED headless mode via arguments
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // This app can be slow
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
        // Wait for the main feed to appear as a signal the page is ready
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".feed-toggle")));
    }
    
    @AfterEach
    void logoutIfLoggedIn() {
        try {
            // The settings link is a reliable indicator of being logged in
            if (driver.findElements(settingsLink).size() > 0) {
                WebElement settingsElement = driver.findElement(settingsLink);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", settingsElement);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", settingsElement);
                wait.until(ExpectedConditions.urlContains("/settings"));
                WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Or click here to logout.')]")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutButton);
                wait.until(ExpectedConditions.presenceOfElementLocated(signInLink));
            }
        } catch (NoSuchElementException e) {
            // Already logged out or on a page without the settings link
        }
    }

    @Test
    @Order(1)
    void testUserRegistration() {
        driver.findElement(signUpLink).click();
        wait.until(ExpectedConditions.urlContains("/register"));
        
        driver.findElement(usernameInput).sendKeys(testUsername);
        driver.findElement(emailInput).sendKeys(testEmail);
        driver.findElement(passwordInput).sendKeys(testPassword);
        driver.findElement(submitButton).click();
        
        // After successful registration, the user's name should appear in the nav bar
        By userProfileLink = By.xpath(String.format("//a[contains(@href, '#/@%s')]", testUsername));
        WebElement userLink = wait.until(ExpectedConditions.visibilityOfElementLocated(userProfileLink));
        assertTrue(userLink.isDisplayed(), "User profile link not found in nav bar after registration.");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.findElement(signInLink).click();
        wait.until(ExpectedConditions.urlContains("/login"));

        driver.findElement(emailInput).sendKeys(testEmail);
        driver.findElement(passwordInput).sendKeys("wrongpassword");
        driver.findElement(submitButton).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessageList));
        assertEquals("email or password is invalid", error.getText(), "Error message for invalid login is incorrect.");
    }
    
    @Test
    @Order(3)
    void testArticleLifecycleE2E() {
        // --- 1. Login ---
        performLogin(testEmail, testPassword);
        
        // --- 2. Create Article ---
        driver.findElement(newArticleLink).click();
        wait.until(ExpectedConditions.urlContains("/editor"));
        
        String title = "Test Article Title " + System.currentTimeMillis();
        String about = "About Selenium Testing";
        String body = "This is the body of the test article written by a Selenium script.";
        String tag = "testing";
        
        driver.findElement(articleTitleInput).sendKeys(title);
        driver.findElement(articleAboutInput).sendKeys(about);
        driver.findElement(articleBodyTextarea).sendKeys(body);
        driver.findElement(articleTagsInput).sendKeys(tag + Keys.ENTER);
        driver.findElement(publishArticleButton).click();
        
        // --- 3. Verify Article ---
        wait.until(ExpectedConditions.urlContains("/articles/"));
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals(title, articleTitle.getText(), "Article title on page does not match created title.");
        
        // --- 4. Post a Comment ---
        String comment = "This is a test comment.";
        driver.findElement(commentTextarea).sendKeys(comment);
        driver.findElement(postCommentButton).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[text()='" + comment + "']")));

        // --- 5. Delete Article ---
        driver.findElement(deleteArticleButton).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "#/"));
        
        // --- 6. Verify Deletion ---
        By userProfileLink = By.xpath(String.format("//a[contains(@href, '#/@%s')]", testUsername));
        driver.findElement(userProfileLink).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".articles-toggle")));
        WebElement noArticlesMessage = driver.findElement(By.cssSelector(".article-preview"));
        assertEquals("No articles are here... yet.", noArticlesMessage.getText(), "Article should be deleted from user's profile.");
    }

    @Test
    @Order(4)
    void testUpdateSettings() {
        performLogin(testEmail, testPassword);
        
        driver.findElement(settingsLink).click();
        wait.until(ExpectedConditions.urlContains("/settings"));
        
        String newBio = "This is an updated bio from a Selenium test.";
        WebElement bioTextarea = driver.findElement(By.cssSelector("textarea[placeholder='Short bio about you']"));
        bioTextarea.clear();
        bioTextarea.sendKeys(newBio);
        driver.findElement(submitButton).click();
        
        // Navigate to profile to verify the change
        wait.until(ExpectedConditions.urlContains("/@"));
        WebElement bioText = driver.findElement(By.cssSelector(".user-info p"));
        assertEquals(newBio, bioText.getText(), "User bio was not updated correctly.");
    }

    @Test
    @Order(5)
    void testFooterExternalLink() {
        WebElement footer = driver.findElement(By.tagName("footer"));
        WebElement thinksterLink = footer.findElement(By.cssSelector("a[href*='thinkster.io']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", thinksterLink);
        handleExternalLink(thinksterLink, "thinkster.io");
    }

    // --- Helper Methods ---

    private void performLogin(String email, String password) {
        driver.findElement(signInLink).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(submitButton).click();
        By userProfileLink = By.xpath(String.format("//a[contains(@href, '#/@%s')]", testUsername));
        wait.until(ExpectedConditions.visibilityOfElementLocated(userProfileLink));
    }
    
    private void handleExternalLink(WebElement linkElement, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", linkElement);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", linkElement);
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        
        if (newWindow == null) {
            fail("New window did not open for link with expected domain: " + expectedDomain);
        }
        
        driver.switchTo().window(newWindow);
        wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL of the new tab should contain " + expectedDomain);
        driver.close();
        
        driver.switchTo().window(originalWindow);
        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
        assertTrue(driver.getTitle().contains("Conduit"), "Should have returned to the Conduit app.");
    }
}