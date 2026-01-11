package Qwen3.ws09.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String EMAIL = "caio@gmail.com";
    private static final String PASSWORD = "123";
    private static final String USERNAME = "caio123";

    @BeforeAll
    static void setUp() {
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

    @Test
    @Order(1)
    void testHomePageLoadsSuccessfully() {
        driver.get(BASE_URL);

        String title = driver.getTitle();
        assertTrue(title.contains("conduit"), "Page title should contain 'conduit'");

        WebElement heroTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("conduit", heroTitle.getText(), "Hero title should be 'conduit'");
    }

    @Test
    @Order(2)
    void testNavigationToLogin() {
        driver.get(BASE_URL);

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        signInLink.click();

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));
        assertTrue(loginForm.isDisplayed(), "Login form should be visible");
    }

    @Test
    @Order(3)
    void testValidLogin() {
        driver.get(BASE_URL + "#/login");

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Email']")));
        emailField.sendKeys(EMAIL);

        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.sendKeys(PASSWORD);

        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();

        // Wait for redirect to home after login
        wait.until(ExpectedConditions.urlContains("#/"));

        WebElement feedTab = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Your Feed")));
        assertTrue(feedTab.isDisplayed(), "Should be logged in and see feed tab");
    }

    @Test
    @Order(4)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL + "#/login");

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Email']")));
        emailField.sendKeys("invalid@example.com");

        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.sendKeys("wrongpass");

        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        assertTrue(error.isDisplayed(), "Error message should appear");
        assertTrue(error.getText().contains("email or password"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(5)
    void testArticleFeedDisplays() {
        loginIfNecessary();

        WebElement globalFeed = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#/']")));
        globalFeed.click();

        java.util.List<WebElement> articles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".article-preview")));
        assertTrue(articles.size() > 0, "At least one article should be displayed in feed");
    }

    @Test
    @Order(6)
    void testArticleDetailsView() {
        loginIfNecessary();

        // Click on first article
        WebElement firstArticle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".article-preview h1")));
        firstArticle.click();

        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(articleTitle.isDisplayed(), "Article title should be visible");
    }

    @Test
    @Order(7)
    void testTagNavigation() {
        loginIfNecessary();

        // Wait for tags to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".sidebar")));

        java.util.List<WebElement> tagList = driver.findElements(By.cssSelector(".tag-list a.tag-pill"));
        if (tagList.size() > 0) {
            String tagName = tagList.get(0).getText();
            tagList.get(0).click();

            wait.until(ExpectedConditions.urlContains("/tag/" + tagName.toLowerCase()));

            WebElement feedTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".feed-toggle")));
            assertTrue(feedTitle.getText().contains(tagName), "Should navigate to tag page");
        }
    }

    @Test
    @Order(8)
    void testNewArticleNavigation() {
        loginIfNecessary();

        WebElement newPostLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("New Post")));
        newPostLink.click();

        WebElement formTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form h1")));
        assertEquals("Editor", formTitle.getText(), "Should navigate to new article form");
    }

    @Test
    @Order(9)
    void testProfilePageAccess() {
        loginIfNecessary();

        // Click on user profile
        WebElement usernameLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(USERNAME)));
        usernameLink.click();

        WebElement profileHeading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".user-info")));
        assertTrue(profileHeading.getText().contains(USERNAME), "Profile should show correct username");
    }

    @Test
    @Order(10)
    void testCreateNewArticle() {
        loginIfNecessary();

        driver.get(BASE_URL + "#/editor");

        WebElement titleField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']")));
        titleField.sendKeys("Test Article Title");

        WebElement aboutField = driver.findElement(By.cssSelector("input[placeholder='What\\'s this article about?']"));
        aboutField.sendKeys("This is a test article description");

        WebElement bodyField = driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        bodyField.sendKeys("This is the body of the test article.");

        WebElement tagsField = driver.findElement(By.cssSelector("input[placeholder='Enter tags']"));
        tagsField.sendKeys("test");

        WebElement publishButton = driver.findElement(By.cssSelector("button[type='submit']"));
        publishButton.click();

        // Wait for article to be published
        wait.until(ExpectedConditions.urlMatches(".*/#/article/.*"));

        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Test Article Title", articleTitle.getText(), "Published article should show correct title");
    }

    @Test
    @Order(11)
    void testEditArticle() {
        loginIfNecessary();

        // Navigate to user's articles
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(USERNAME)));
        profileLink.click();

        WebElement firstArticle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".article-preview h1")));
        firstArticle.click();

        WebElement editButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[routerlink*='editor']")));
        editButton.click();

        WebElement titleField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']")));
        titleField.clear();
        titleField.sendKeys("Updated Test Article Title");

        WebElement publishButton = driver.findElement(By.cssSelector("button[type='submit']"));
        publishButton.click();

        WebElement updatedTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Updated Test Article Title", updatedTitle.getText(), "Article title should be updated");
    }

    @Test
    @Order(12)
    void testDeleteArticle() {
        loginIfNecessary();

        // Navigate to user's articles
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(USERNAME)));
        profileLink.click();

        java.util.List<WebElement> articles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".article-preview")));
        if (articles.size() > 0) {
            WebElement firstArticleTitle = articles.get(0).findElement(By.cssSelector("h1"));
            String originalTitle = firstArticleTitle.getText();

            WebElement deleteButton = articles.get(0).findElement(By.cssSelector(".btn-outline-danger"));
            deleteButton.click();

            // Wait for deletion and refresh
            driver.navigate().refresh();

            wait.until(ExpectedConditions.stalenessOf(articles.get(0)));

            java.util.List<WebElement> articlePreviews = driver.findElements(By.cssSelector(".article-preview"));
            for (WebElement article : articlePreviews) {
                String title = article.findElement(By.cssSelector("h1")).getText();
                assertNotEquals(originalTitle, title, "Deleted article should not appear in list");
            }
        }
    }

    @Test
    @Order(13)
    void testFavoriteArticle() {
        loginIfNecessary();

        // Go to global feed
        WebElement globalFeed = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#/']")));
        globalFeed.click();

        java.util.List<WebElement> articles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".article-preview")));
        assertTrue(articles.size() > 0, "At least one article should be present");

        WebElement firstArticle = articles.get(0);
        WebElement favoriteButton = firstArticle.findElement(By.cssSelector(".btn-outline-primary"));
        String originalText = favoriteButton.getText().trim();

        favoriteButton.click();

        wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(favoriteButton, originalText)));

        String newText = firstArticle.findElement(By.cssSelector(".btn-primary")).getText().trim();
        assertTrue(newText.contains("1"), "Favorite count should increase to 1");
    }

    @Test
    @Order(14)
    void testSettingsNavigation() {
        loginIfNecessary();

        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Settings")));
        settingsLink.click();

        WebElement settingsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".settings-page h1")));
        assertEquals("Your Settings", settingsTitle.getText(), "Should navigate to settings page");
    }

    @Test
    @Order(15)
    void testLogoutFunctionality() {
        loginIfNecessary();

        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(USERNAME)));
        userMenu.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Or click here to logout.")));
        logoutButton.click();

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        assertTrue(signInLink.isDisplayed(), "Sign in link should reappear after logout");
    }

    @Test
    @Order(16)
    void testFooterLearnLink() {
        driver.get(BASE_URL);

        String originalWindow = driver.getWindowHandle();
        WebElement learnLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("conduit")));
        learnLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("github") || url.contains("conduit"), "Feedback link should open to related domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginIfNecessary() {
        driver.get(BASE_URL);
        try {
            WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
            if (signInLink.isDisplayed()) {
                driver.get(BASE_URL + "#/login");

                WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Email']")));
                emailField.sendKeys(EMAIL);

                WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
                passwordField.sendKeys(PASSWORD);

                WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
                signInButton.click();

                wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Your Feed")));
            }
        } catch (TimeoutException e) {
            // Already logged in
        }
    }
}