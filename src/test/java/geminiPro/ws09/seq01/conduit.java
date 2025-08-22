package GPT4.ws09.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

/**
 * A comprehensive JUnit 5 test suite for the RealWorld "Conduit" demo application.
 * This suite uses Selenium WebDriver with Firefox in headless mode to test the full user journey,
 * including user registration, login, full CRUD operations on articles, and logout.
 * A unique user is created for each test run to ensure idempotency.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConduitRealWorldTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Site and User Credentials ---
    private static final String BASE_URL = "https://demo.realworld.io/";
    // A unique user is generated once for the entire test suite run
    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final String USERNAME = "testuser-" + UNIQUE_ID;
    private static final String EMAIL = "testuser-" + UNIQUE_ID + "@example.com";
    private static final String PASSWORD = "password123";
    private static String articleUrlSlug = ""; // Will be set after creating an article

    // --- Locators ---
    // Header
    private static final By SIGN_UP_LINK = By.linkText("Sign up");
    private static final By SIGN_IN_LINK = By.linkText("Sign in");
    private static final By NEW_ARTICLE_LINK = By.cssSelector("a[href='#/editor']");
    private static final By SETTINGS_LINK = By.cssSelector("a[href='#/settings']");

    // Forms
    private static final By USERNAME_INPUT = By.cssSelector("input[placeholder='Username']");
    private static final By EMAIL_INPUT_PLACEHOLDER = By.cssSelector("input[placeholder='Email']");
    private static final By PASSWORD_INPUT_PLACEHOLDER = By.cssSelector("input[placeholder='Password']");
    private static final By EMAIL_INPUT_TYPE = By.cssSelector("input[type='email']");
    private static final By PASSWORD_INPUT_TYPE = By.cssSelector("input[type='password']");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");
    private static final By ERROR_MESSAGES = By.cssSelector(".error-messages li");

    // Article Editor
    private static final By ARTICLE_TITLE_INPUT = By.cssSelector("input[placeholder='Article Title']");
    private static final By ARTICLE_ABOUT_INPUT = By.cssSelector("input[placeholder=\"What's this article about?\"]");
    private static final By ARTICLE_BODY_TEXTAREA = By.cssSelector("textarea[placeholder*='Write your article']");
    private static final By ARTICLE_TAGS_INPUT = By.cssSelector("input[placeholder='Enter tags']");
    private static final By PUBLISH_ARTICLE_BUTTON = By.xpath("//button[normalize-space()='Publish Article']");

    // Article Page
    private static final By ARTICLE_PAGE_TITLE = By.tagName("h1");
    private static final By EDIT_ARTICLE_BUTTON = By.xpath("//a[contains(., 'Edit Article')]");
    private static final By DELETE_ARTICLE_BUTTON = By.xpath("//button[contains(., 'Delete Article')]");

    // Settings Page
    private static final By LOGOUT_BUTTON = By.xpath("//button[contains(text(), 'Or click here to logout.')]");

    // Footer
    private static final By FOOTER_LINK = By.linkText("Thinkster");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    /**
     * Helper method to perform login.
     */
    private void login(String email, String password) {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT_TYPE)).sendKeys(email);
        driver.findElement(PASSWORD_INPUT_TYPE).sendKeys(password);
        driver.findElement(SUBMIT_BUTTON).click();
    }
    
    private void validateExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "URL should contain '" + expectedDomain + "'. Actual: " + driver.getCurrentUrl());
        
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    @DisplayName("Should register a new user successfully")
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_UP_LINK)).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(USERNAME);
        driver.findElement(EMAIL_INPUT_PLACEHOLDER).sendKeys(EMAIL);
        driver.findElement(PASSWORD_INPUT_PLACEHOLDER).sendKeys(PASSWORD);
        driver.findElement(SUBMIT_BUTTON).click();

        By userProfileLink = By.linkText(USERNAME);
        wait.until(ExpectedConditions.visibilityOfElementLocated(userProfileLink));
        Assertions.assertTrue(driver.findElement(userProfileLink).isDisplayed(), "Registration failed or user was not logged in.");
    }

    @Test
    @Order(2)
    @DisplayName("Should fail to log in with an invalid password")
    void testInvalidLogin() {
        // First, ensure we are logged out
        driver.get(BASE_URL + "#/settings");
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SIGN_IN_LINK));

        // Attempt invalid login
        driver.findElement(SIGN_IN_LINK).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT_TYPE)).sendKeys(EMAIL);
        driver.findElement(PASSWORD_INPUT_TYPE).sendKeys("wrongpassword");
        driver.findElement(SUBMIT_BUTTON).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGES));
        Assertions.assertTrue(errorMessage.getText().contains("is invalid"), "Error message for invalid login was incorrect or not found.");
    }
    
    @Test
    @Order(3)
    @DisplayName("Should create, edit, and then delete an article")
    void testArticleCrudLifecycle() {
        // --- 1. LOGIN and CREATE ---
        login(EMAIL, PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(NEW_ARTICLE_LINK)).click();

        String articleTitle = "My Test Article " + UNIQUE_ID;
        String articleBody = "This is the body of the test article.";
        wait.until(ExpectedConditions.visibilityOfElementLocated(ARTICLE_TITLE_INPUT)).sendKeys(articleTitle);
        driver.findElement(ARTICLE_ABOUT_INPUT).sendKeys("A test");
        driver.findElement(ARTICLE_BODY_TEXTAREA).sendKeys(articleBody);
        driver.findElement(ARTICLE_TAGS_INPUT).sendKeys("testing");
        driver.findElement(PUBLISH_ARTICLE_BUTTON).click();

        // Assert creation
        WebElement createdTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(ARTICLE_PAGE_TITLE));
        Assertions.assertEquals(articleTitle, createdTitle.getText(), "Article was not created or title is incorrect.");
        // Save the URL slug for the next step
        String currentUrl = driver.getCurrentUrl();
        articleUrlSlug = currentUrl.substring(currentUrl.lastIndexOf('/') + 1);

        // --- 2. EDIT ---
        wait.until(ExpectedConditions.elementToBeClickable(EDIT_ARTICLE_BUTTON)).click();
        String updatedBodyText = "\nThis is the updated body.";
        wait.until(ExpectedConditions.visibilityOfElementLocated(ARTICLE_BODY_TEXTAREA)).sendKeys(updatedBodyText);
        driver.findElement(PUBLISH_ARTICLE_BUTTON).click();
        
        // Assert edit
        wait.until(ExpectedConditions.urlContains(articleUrlSlug)); // Wait for redirect back to article
        WebElement articleContent = driver.findElement(By.cssSelector(".article-content"));
        Assertions.assertTrue(articleContent.getText().contains(updatedBodyText), "Article body was not updated correctly.");

        // --- 3. DELETE ---
        wait.until(ExpectedConditions.elementToBeClickable(DELETE_ARTICLE_BUTTON)).click();
        
        // Assert deletion by checking for redirect to home and absence of "Your Feed"
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "#/"));
        Assertions.assertTrue(driver.findElements(By.linkText("Your Feed")).isEmpty(), "User was not redirected to the global feed after deleting the only article.");
    }

    @Test
    @Order(4)
    @DisplayName("Should successfully log out from the settings page")
    void testSuccessfulLogout() {
        login(EMAIL, PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(SETTINGS_LINK)).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGOUT_BUTTON)).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(SIGN_IN_LINK));
        Assertions.assertTrue(driver.findElement(SIGN_UP_LINK).isDisplayed(), "Logout failed, Sign Up link is not visible.");
    }

    @Test

    @Order(5)
    @DisplayName("Should verify the footer external link to Thinkster")
    void testFooterExternalLink() {
        WebElement footer = driver.findElement(By.tagName("footer"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", footer);
        
        validateExternalLink(FOOTER_LINK, "thinkster.io");
    }
}