package SunaQwen3.ws09.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass123";

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
    void testValidLogin() {
        driver.get(BASE_URL);
        assertTrue(driver.getTitle().contains("Conduit"), "Page title should contain 'Conduit'");

        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        signIn.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.cssSelector("button[type='submit']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(signInButton).click();

        By homeLink = By.xpath("//a[contains(text(), 'conduit')]");
        wait.until(ExpectedConditions.elementToBeClickable(homeLink));

        assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to home after login");
        By yourFeed = By.xpath("//a[contains(text(), 'Your Feed')]");
        assertTrue(driver.findElement(yourFeed).isDisplayed(), "Your Feed link should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");
        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.cssSelector("button[type='submit']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        driver.findElement(passwordField).sendKeys("wrongpass");
        driver.findElement(signInButton).click();

        By error = By.cssSelector(".error-messages li");
        String errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(error)).getText();
        assertTrue(errorMessage.contains("email or password"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testNavigationMenu() {
        // Ensure logged in
        testValidLogin();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menu.click();

        By allArticles = By.xpath("//a[contains(text(), 'Home')]");
        By newPost = By.xpath("//a[contains(text(), 'New Post')]");
        By settings = By.xpath("//a[contains(text(), 'Settings')]");
        By profile = By.xpath("//a[contains(text(), '@') and not(contains(text(), 'Sign'))]");

        assertTrue(driver.findElement(allArticles).isDisplayed(), "Home link should be in menu");
        assertTrue(driver.findElement(newPost).isDisplayed(), "New Post link should be in menu");
        assertTrue(driver.findElement(settings).isDisplayed(), "Settings link should be in menu");
        assertTrue(driver.findElement(profile).isDisplayed(), "Profile link should be in menu");

        // Click Settings and verify
        driver.findElement(settings).click();
        wait.until(ExpectedConditions.urlContains("#/settings"));
        assertTrue(driver.getCurrentUrl().contains("#/settings"), "Should navigate to settings page");

        // Reopen menu and click Home
        driver.findElement(menuButton).click();
        driver.findElement(allArticles).click();
        wait.until(ExpectedConditions.urlContains("#/"));
        assertTrue(driver.getCurrentUrl().contains("#/"), "Should return to home page");
    }

    @Test
    @Order(4)
    void testFooterExternalLinks() {
        driver.get(BASE_URL);

        By twitterLink = By.cssSelector("a[href='https://twitter.com/gothinkster']");
        By facebookLink = By.cssSelector("a[href='https://www.facebook.com/thinkster.io']");
        By linkedinLink = By.cssSelector("a[href='https://www.linkedin.com/company/thinkster-io']");

        // Test Twitter link
        String originalWindow = driver.getWindowHandle();
        driver.findElement(twitterLink).click();
        switchToNewWindow();
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        driver.findElement(facebookLink).click();
        switchToNewWindow();
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        driver.findElement(linkedinLink).click();
        switchToNewWindow();
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    void testArticleInteraction() {
        testValidLogin();

        // Create a new article
        By newPostLink = By.xpath("//a[contains(text(), 'New Post')]");
        WebElement newPost = wait.until(ExpectedConditions.elementToBeClickable(newPostLink));
        newPost.click();

        By titleField = By.cssSelector("input[placeholder='Article Title']");
        By aboutField = By.cssSelector("input[placeholder=\"What's this article about?\"]");
        By bodyField = By.cssSelector("textarea[placeholder='Write your article (in markdown)']");
        By publishButton = By.cssSelector("button[type='submit']");

        String articleTitle = "Test Article Title";
        String articleAbout = "This is about section";
        String articleBody = "This is the body of the test article.";

        wait.until(ExpectedConditions.visibilityOfElementLocated(titleField)).sendKeys(articleTitle);
        driver.findElement(aboutField).sendKeys(articleAbout);
        driver.findElement(bodyField).sendKeys(articleBody);
        driver.findElement(publishButton).click();

        // Wait for article to be published
        By articleTitleHeader = By.cssSelector(".article-page h1");
        wait.until(ExpectedConditions.visibilityOfElementLocated(articleTitleHeader));

        assertEquals(articleTitle, driver.findElement(articleTitleHeader).getText(),
                "Published article title should match input");

        // Like the article
        By favoriteButton = By.cssSelector(".btn-outline-primary");
        WebElement favoriteBtn = wait.until(ExpectedConditions.elementToBeClickable(favoriteButton));
        favoriteBtn.click();

        // Wait for favorite count to update
        By favoriteCount = By.cssSelector(".btn-outline-primary .counter");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(favoriteCount, "1"));

        assertEquals("1", driver.findElement(favoriteCount).getText(),
                "Favorite count should be updated to 1");
    }

    @Test
    @Order(6)
    void testUserProfile() {
        testValidLogin();

        By profileLink = By.xpath("//a[contains(@href, '/@') and not(contains(text(), 'Sign'))]");
        WebElement profile = wait.until(ExpectedConditions.elementToBeClickable(profileLink));
        profile.click();

        By profileUsername = By.cssSelector(".profile-page h4");
        wait.until(ExpectedConditions.visibilityOfElementLocated(profileUsername));

        assertTrue(driver.getCurrentUrl().contains("/@"), "Should navigate to user profile page");
        assertTrue(profileUsername.toString().contains(USERNAME) || driver.findElement(profileUsername).getText().contains(USERNAME),
                "Profile should display correct username");
    }

    @Test
    @Order(7)
    void testLogout() {
        testValidLogin();

        By settingsLink = By.xpath("//a[contains(text(), 'Settings')]");
        WebElement settings = wait.until(ExpectedConditions.elementToBeClickable(settingsLink));
        settings.click();

        By logoutButton = By.cssSelector("button.btn-outline-danger");
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
        logout.click();

        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        wait.until(ExpectedConditions.elementToBeClickable(signInLink));

        assertTrue(driver.getCurrentUrl().contains("#/"), "Should return to home after logout");
        assertTrue(driver.findElement(signInLink).isDisplayed(), "Sign in link should be visible after logout");
    }

    private void switchToNewWindow() {
        String originalWindow = driver.getWindowHandle();
        wait.until(webDriver -> {
            Set<String> handles = webDriver.getWindowHandles();
            return handles.size() > 1;
        });
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }
    }
}