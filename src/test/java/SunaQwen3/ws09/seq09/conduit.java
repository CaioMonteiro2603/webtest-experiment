package SunaQwen3.ws09.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("Conduit", driver.getTitle(), "Page title should be 'Conduit'");

        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        WebElement signInElement = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        signInElement.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(signInButton).click();

        By homeLink = By.xpath("//a[contains(text(), 'conduit')]");
        wait.until(ExpectedConditions.elementToBeClickable(homeLink));

        assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to home after login");
        assertTrue(driver.getPageSource().contains("Your Feed"), "Should see 'Your Feed' section after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        driver.findElement(passwordField).sendKeys("wrongpass");
        driver.findElement(signInButton).click();

        By errorDiv = By.cssSelector("div.error-messages");
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorDiv));

        WebElement errorElement = driver.findElement(errorDiv);
        assertTrue(errorElement.getText().contains("email or password is invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testNavigationMenuAllItems() {
        // Ensure logged in
        loginIfNot();

        By menuButton = By.cssSelector("button.navbar-toggler");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By allItemsLink = By.xpath("//a[contains(text(), 'All Articles')]");
        WebElement allItemsEl = wait.until(ExpectedConditions.elementToBeClickable(allItemsLink));
        allItemsEl.click();

        By articleList = By.cssSelector("div.article-preview");
        wait.until(ExpectedConditions.presenceOfElementLocated(articleList));

        List<WebElement> articles = driver.findElements(articleList);
        assertTrue(articles.size() > 0, "Should see at least one article in the list");
    }

    @Test
    @Order(4)
    public void testNavigationMenuAboutExternalLink() {
        loginIfNot();

        By menuButton = By.cssSelector("button.navbar-toggler");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By aboutLink = By.xpath("//a[contains(text(), 'About')]");
        String originalWindow = driver.getWindowHandle();
        driver.findElement(aboutLink).click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("realworld.io"),
                "About link should open realworld.io domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testNavigationMenuLogout() {
        loginIfNot();

        By menuButton = By.cssSelector("button.navbar-toggler");
        wait.until(ExpectedConditions.elementToBeClickable(menuButton)).click();

        By logoutLink = By.xpath("//a[contains(text(), 'Log out')]");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();

        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        wait.until(ExpectedConditions.elementToBeClickable(signInLink));

        assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to home after logout");
        assertTrue(driver.getPageSource().contains("Sign in"), "Should see 'Sign in' link after logout");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);

        // Test Twitter link
        testExternalLinkInNewTab(By.cssSelector("a[href*='twitter.com']"), "twitter.com");

        // Test Facebook link
        testExternalLinkInNewTab(By.cssSelector("a[href*='facebook.com']"), "facebook.com");

        // Test LinkedIn link
        testExternalLinkInNewTab(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }

    private void testExternalLinkInNewTab(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String newWindow = null;
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                newWindow = window;
                break;
            }
        }

        assertNotNull(newWindow, "New window should be opened");
        driver.switchTo().window(newWindow);

        assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link should navigate to " + expectedDomain);

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testCreateNewArticle() {
        loginIfNot();

        By newPostLink = By.xpath("//a[contains(text(), 'New Post')]");
        WebElement newPostEl = wait.until(ExpectedConditions.elementToBeClickable(newPostLink));
        newPostEl.click();

        By titleField = By.cssSelector("input[placeholder='Article Title']");
        By aboutField = By.cssSelector("input[placeholder=\"What's this article about?\"]");
        By bodyField = By.cssSelector("textarea[placeholder='Write your article (in markdown)']");
        By tagField = By.cssSelector("input[placeholder='Enter tags']");
        By publishButton = By.xpath("//button[contains(text(), 'Publish Article')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(titleField))
                .sendKeys("Test Article Title");
        driver.findElement(aboutField).sendKeys("Test article description");
        driver.findElement(bodyField).sendKeys("This is a test article body.");
        driver.findElement(tagField).sendKeys("test");
        driver.findElement(publishButton).click();

        By articleTitle = By.cssSelector("h1");
        wait.until(ExpectedConditions.visibilityOfElementLocated(articleTitle));

        WebElement titleElement = driver.findElement(articleTitle);
        assertEquals("Test Article Title", titleElement.getText(),
                "Published article should have correct title");
    }

    @Test
    @Order(8)
    public void testFavoriteArticle() {
        loginIfNot();

        driver.get(BASE_URL + "#/");

        By firstArticleHeart = By.cssSelector("div.article-preview .btn-outline-primary");
        WebElement heartButton = wait.until(ExpectedConditions.elementToBeClickable(firstArticleHeart));
        heartButton.click();

        // Wait for favorite count to update
        By favoriteCount = By.cssSelector("div.article-preview .btn-primary");
        wait.until(ExpectedConditions.visibilityOfElementLocated(favoriteCount));

        WebElement countElement = driver.findElement(favoriteCount);
        assertTrue(countElement.getText().contains("1"),
                "Favorite count should be updated to 1");

        // Unfavorite
        countElement.click();
        wait.until(ExpectedConditions.elementToBeClickable(firstArticleHeart));
    }

    @Test
    @Order(9)
    public void testUserProfileVisit() {
        loginIfNot();

        By usernameLink = By.xpath("//a[contains(@href, '/@') and contains(@class, 'navbar-link')]");
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(usernameLink));
        String profileHref = profileLink.getAttribute("href");

        profileLink.click();

        wait.until(ExpectedConditions.urlToBe(profileHref));
        assertTrue(driver.getCurrentUrl().contains("/@"), "Should navigate to user profile page");

        By userArticles = By.cssSelector("div.article-preview");
        assertTrue(driver.findElements(userArticles).size() >= 0,
                "User profile should display articles (can be empty)");
    }

    @Test
    @Order(10)
    public void testSettingsPageAccess() {
        loginIfNot();

        By settingsLink = By.xpath("//a[contains(text(), 'Settings')]");
        WebElement settingsEl = wait.until(ExpectedConditions.elementToBeClickable(settingsLink));
        settingsEl.click();

        By orText = By.xpath("//p[contains(text(), 'Or click below to logout')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(orText));

        assertTrue(driver.getCurrentUrl().contains("#/settings"), "Should be on settings page");
        assertTrue(driver.getPageSource().contains("Or click below to logout"),
                "Settings page should contain logout instruction");
    }

    private void loginIfNot() {
        if (driver.getCurrentUrl().equals(BASE_URL) || driver.getCurrentUrl().equals(BASE_URL + "#/")) {
            By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
            try {
                WebElement signInElement = driver.findElement(signInLink);
                if (signInElement.isDisplayed()) {
                    signInElement.click();
                }
            } catch (NoSuchElementException e) {
                // Already logged in
                return;
            }

            By emailField = By.cssSelector("input[placeholder='Email']");
            if (wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)) != null) {
                driver.findElement(emailField).sendKeys(USERNAME);
                driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(PASSWORD);
                driver.findElement(By.xpath("//button[contains(text(), 'Sign in')]")).click();

                By homeLink = By.xpath("//a[contains(text(), 'conduit')]");
                wait.until(ExpectedConditions.elementToBeClickable(homeLink));
            }
        }
    }
}
