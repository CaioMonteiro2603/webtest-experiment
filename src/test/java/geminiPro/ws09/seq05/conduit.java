package geminiPRO.ws09.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * JUnit 5 test suite for the RealWorld demo application "Conduit".
 * This suite uses Selenium WebDriver with headless Firefox to test the full lifecycle of
 * a user's interaction with the blogging platform, including registration, login,
 * creating, editing, and deleting articles.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RealWorldAppE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // A unique user is generated for each test suite run to ensure a clean state.
    private static String testUsername;
    private static String testEmail;
    private static String testPassword;

    private static final String BASE_URL = "https://demo.realworld.io/";

    // --- Locators ---
    private final By signUpLink = By.xpath("//a[@href='#/register']");
    private final By signInLink = By.xpath("//a[@href='#/login']");
    private final By usernameInput = By.xpath("//input[@placeholder='Username']");
    private final By emailInput = By.xpath("//input[@placeholder='Email']");
    private final By passwordInput = By.xpath("//input[@placeholder='Password']");
    private final By submitButton = By.xpath("//button[@type='submit']");
    private final By settingsLink = By.xpath("//a[contains(@href, '/settings')]");
    private final By logoutButton = By.xpath("//button[contains(text(), 'logout')]");
    private final By newArticleLink = By.xpath("//a[contains(@href, '/editor')]");
    
    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new Firefox-Driver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Generate unique credentials for this test run
        long timestamp = System.currentTimeMillis();
        testUsername = "gemini-user-" + timestamp;
        testEmail = "gemini.user." + timestamp + "@test.com";
        testPassword = "password123";
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setup() {
        driver.get(BASE_URL);
    }
    
    /**
     * Helper method to perform login. Assumes user is already registered.
     */
    private void performLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(submitButton));
        driver.findElement(emailInput).sendKeys(testEmail);
        driver.findElement(passwordInput).sendKeys(testPassword);
        driver.findElement(submitButton).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(testUsername)));
    }
    
    /**
     * Helper method to perform logout from any authenticated page.
     */
    private void performLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(settingsLink)).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(signInLink));
    }

    @Test
    @Order(1)
    @DisplayName("Should register a new user successfully")
    void testUserRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(signUpLink)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).sendKeys(testUsername);
        driver.findElement(emailInput).sendKeys(testEmail);
        driver.findElement(passwordInput).sendKeys(testPassword);
        driver.findElement(submitButton).click();

        // After registration, the user is automatically logged in.
        WebElement userProfileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(testUsername)));
        Assertions.assertTrue(userProfileLink.isDisplayed(), "Username should be visible in the nav bar after registration.");
    }
    
    @Test
    @Order(2)
    @DisplayName("Should show an error for invalid login credentials")
    void testInvalidLogin() {
        Assumptions.assumeTrue(testUsername != null, "User registration must succeed for this test to run.");
        performLogout(); // Ensure we are logged out from the previous test

        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(submitButton));
        driver.findElement(emailInput).sendKeys(testEmail);
        driver.findElement(passwordInput).sendKeys("wrongpassword");
        driver.findElement(submitButton).click();
        
        WebElement errorMessages = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMessages.getText().contains("is invalid"), "Error message for invalid credentials did not appear.");
    }
    
    @Test
    @Order(3)
    @DisplayName("Should log in and out successfully")
    void testSuccessfulLoginAndLogout() {
        Assumptions.assumeTrue(testUsername != null, "User registration must succeed for this test to run.");
        performLogin();
        Assertions.assertTrue(
            driver.findElement(By.linkText(testUsername)).isDisplayed(),
            "Username link should be visible after login."
        );
        
        performLogout();
        Assertions.assertTrue(
            driver.findElement(signInLink).isDisplayed(),
            "Sign In link should be visible after logout."
        );
    }
    
    @Test
    @Order(4)
    @DisplayName("Should create, edit, and then delete an article")
    void testCreateEditDeleteArticle() {
        Assumptions.assumeTrue(testUsername != null, "User registration must succeed for this test to run.");
        long timestamp = System.currentTimeMillis();
        String initialTitle = "Test Article Title " + timestamp;
        String updatedTitle = "Updated Article Title " + timestamp;
        String articleBody = "This is the body of the test article.";
        
        performLogin();
        
        // --- Create Article ---
        wait.until(ExpectedConditions.elementToBeClickable(newArticleLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Article Title']")));
        driver.findElement(By.xpath("//input[@placeholder='Article Title']")).sendKeys(initialTitle);
        driver.findElement(By.xpath("//input[contains(@placeholder, 'this article about?')]")).sendKeys("Testing");
        driver.findElement(By.xpath("//textarea[contains(@placeholder, 'Write your article')]")).sendKeys(articleBody);
        driver.findElement(By.xpath("//button[contains(text(), 'Publish Article')]")).click();
        
        // --- Assert Creation ---
        WebElement articleTitleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals(initialTitle, articleTitleElement.getText(), "Article title after creation is incorrect.");
        
        // --- Edit Article ---
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(., 'Edit Article')]"))).click();
        WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Article Title']")));
        titleInput.clear();
        titleInput.sendKeys(updatedTitle);
        driver.findElement(By.xpath("//button[contains(text(), 'Publish Article')]")).click();
        
        // --- Assert Edit ---
        WebElement updatedArticleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals(updatedTitle, updatedArticleTitle.getText(), "Article title after editing is incorrect.");
        
        // --- Delete Article ---
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Delete Article')]"))).click();
        // Browser confirmation dialog for deletion - not standard Selenium, but simple alert
        // No alert appears in this app's flow, it deletes directly.
        
        // --- Assert Deletion ---
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "#/"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("#/"), "Should be redirected to the homepage after deleting an article.");
    }
    
    @Test
    @Order(5)
    @DisplayName("Should filter the Global Feed by a popular tag")
    void testGlobalFeedAndTagFiltering() {
        By articlePreview = By.cssSelector(".article-preview");
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(articlePreview, 1));
        
        List<WebElement> popularTags = driver.findElements(By.cssSelector(".sidebar .tag-list a"));
        Assumptions.assumeFalse(popularTags.isEmpty(), "Popular tags list should not be empty.");
        
        WebElement firstTag = popularTags.get(0);
        String tagName = firstTag.getText();
        firstTag.click();
        
        WebElement activeTagTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".feed-toggle .nav-link.active")));
        Assertions.assertEquals(tagName, activeTagTab.getText(), "The clicked tag should be the active tab.");
        
        // Wait for articles with the tag to load
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(articlePreview, 0));
        List<WebElement> filteredArticles = driver.findElements(articlePreview);
        
        Assertions.assertTrue(
            filteredArticles.stream().allMatch(article -> article.getText().contains(tagName)),
            "All articles in the filtered feed should contain the selected tag."
        );
    }
}