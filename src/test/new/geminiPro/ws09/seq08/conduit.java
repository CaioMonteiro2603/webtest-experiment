package geminiPro.ws09.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final Duration TIMEOUT = Duration.ofSeconds(15); // Increased for SPA reactivity

    private static String testUsername;
    private static String testEmail;
    private static String testPassword;
    private static String uniqueArticleTitle;
    private static String uniqueArticleBody;


    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private final By signUpLink = By.linkText("Sign up");
    private final By signInLink = By.linkText("Sign in");
    private final By usernameInput = By.cssSelector("input[placeholder='Username']");
    private final By emailInput = By.cssSelector("input[placeholder='Email']");
    private final By passwordInput = By.cssSelector("input[placeholder='Password']");
    private final By newArticleLink = By.cssSelector("a[href='#/editor']");
    private final By settingsLink = By.cssSelector("a[href='#/settings']");
    private final By logoutButton = By.xpath("//button[contains(text(), 'Or click here to logout.')]");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);

        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        testUsername = "tester-" + uniqueId;
        testEmail = "tester-" + uniqueId + "@example.com";
        testPassword = "password123";
        uniqueArticleTitle = "Test Article Title " + uniqueId;
        uniqueArticleBody = "This is the body of the test article. " + uniqueId;
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

    private void login(String email, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(By.xpath("//button[text()='Sign in']")).click();
        // Wait for login to complete by checking for the "New Article" link
        wait.until(ExpectedConditions.presenceOfElementLocated(newArticleLink));
    }

    private void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(settingsLink)).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
        // Wait for logout to complete by checking for the "Sign in" link
        wait.until(ExpectedConditions.visibilityOfElementLocated(signInLink));
    }
    
    private void createArticle(String title, String about, String body, String tags) {
        wait.until(ExpectedConditions.elementToBeClickable(newArticleLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']"))).sendKeys(title);
        driver.findElement(By.cssSelector("input[placeholder=\"What's this article about?\"]")).sendKeys(about);
        driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']")).sendKeys(body);
        driver.findElement(By.cssSelector("input[placeholder='Enter tags']")).sendKeys(tags);
        driver.findElement(By.xpath("//button[contains(text(),'Publish Article')]")).click();
        // Wait for the article page to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".article-page h1")));
    }

    @Test
    @Order(1)
    @DisplayName("Test New User Registration")
    void testUserRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(signUpLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).sendKeys(testUsername);
        driver.findElement(emailInput).sendKeys(testEmail);
        driver.findElement(passwordInput).sendKeys(testPassword);
        driver.findElement(By.xpath("//button[text()='Sign up']")).click();

        // After registration, user is logged in. Verify by checking for their username in the nav bar.
        By userProfileLink = By.xpath("//a[contains(@href,'#/@" + testUsername + "')]");
        assertTrue(wait.until(ExpectedConditions.presenceOfElementLocated(userProfileLink)).isDisplayed(),
            "User profile link should be visible in the header after registration.");
    }

    @Test
    @Order(2)
    @DisplayName("Test Login with Invalid Credentials")
    void testLoginWithInvalidCredentials() {
        wait.until(ExpectedConditions.elementToBeClickable(signInLink)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(testEmail);
        driver.findElement(passwordInput).sendKeys("wrongpassword");
        driver.findElement(By.xpath("//button[text()='Sign in']")).click();
        
        WebElement errorMessages = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        assertTrue(errorMessages.getText().contains("is invalid"), "Error message should be displayed for invalid credentials.");
    }

    @Test
    @Order(3)
    @DisplayName("Test Successful Login and Logout")
    void testSuccessfulLoginAndLogout() {
        login(testEmail, testPassword);
        By userProfileLink = By.xpath("//a[contains(@href,'#/@" + testUsername + "')]");
        assertTrue(wait.until(ExpectedConditions.presenceOfElementLocated(userProfileLink)).isDisplayed(), "User should be logged in.");

        logout();
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(signInLink)).isDisplayed(), "User should be logged out.");
    }

    @Test
    @Order(4)
    @DisplayName("Create, Verify, and Delete an Article")
    void testArticleLifecycle() {
        login(testEmail, testPassword);
        
        // --- CREATE ---
        createArticle(uniqueArticleTitle, "About testing", uniqueArticleBody, "test");
        WebElement articleTitleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals(uniqueArticleTitle, articleTitleElement.getText(), "Article title on page should match created title.");

        // --- VERIFY ---
        WebElement articleBodyElement = driver.findElement(By.cssSelector(".article-content p"));
        assertEquals(uniqueArticleBody, articleBodyElement.getText(), "Article body on page should match created body.");
        
        // --- POST COMMENT ---
        String commentText = "This is a great test comment!";
        driver.findElement(By.cssSelector("textarea[placeholder='Write a comment...']")).sendKeys(commentText);
        driver.findElement(By.xpath("//button[text()='Post Comment']")).click();
        WebElement postedComment = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[text()='" + commentText + "']")));
        assertTrue(postedComment.isDisplayed(), "Posted comment should be visible.");

        // --- DELETE ---
        driver.findElement(By.xpath("//button[contains(text(), 'Delete Article')]")).click();
        
        // After deletion, we should be back on the home page, and the "Your Feed" should be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Your Feed")));
        assertTrue(driver.getCurrentUrl().endsWith("/"), "Should be redirected to the home page after deleting an article.");
    }
    
    @Test
    @Order(5)
    @DisplayName("Filter Article Feed by Tag")
    void testFilterByTag() {
        WebElement popularTagsSection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("sidebar")));
        // Click the first available tag
        WebElement firstTag = popularTagsSection.findElement(By.cssSelector(".tag-list .tag-default"));
        String tagName = firstTag.getText();
        firstTag.click();
        
        // Wait for the feed to update, indicated by the active tab showing the tag name
        WebElement activeTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".nav-pills .nav-link.active")));
        assertEquals("#" + tagName.toLowerCase(), activeTab.getText().toLowerCase(), "The active tab should show the selected tag name.");

        // Verify that all loaded articles contain the tag
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".article-preview"), 0));
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        for (WebElement article : articles) {
            List<String> articleTags = article.findElements(By.cssSelector(".tag-list li"))
                                            .stream()
                                            .map(WebElement::getText)
                                            .collect(Collectors.toList());
            assertTrue(articleTags.contains(tagName), "Each article in the filtered feed should contain the tag: " + tagName);
        }
    }
    
    @Test
    @Order(6)
    @DisplayName("Verify External GitHub Link in Footer")
    void testGitHubExternalLink() {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='github.com']")));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();

        driver.switchTo().window(newWindow);
        
        assertTrue(wait.until(ExpectedConditions.urlContains("github.com")), "URL of the new tab should contain 'github.com'");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        assertTrue(driver.findElement(By.className("home-page")).isDisplayed(), "Should have switched back to the Conduit main page.");
    }
}