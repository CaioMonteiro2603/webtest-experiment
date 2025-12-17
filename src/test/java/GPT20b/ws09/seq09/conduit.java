package GPT20b.ws09.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "guest@example.com";
    private static final String PASSWORD = "password";

    @BeforeAll
    public static void setUpDriver() {
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

    /* ---------- Helper Methods ---------- */

    private void performLogin(String user, String pass) {
        driver.get(BASE_URL);
        By emailField = By.cssSelector("input[name='email']");
        By passField = By.cssSelector("input[name='password']");
        By loginBtn = By.cssSelector("button[type='submit']");

        wait.until(ExpectedConditions.elementToBeClickable(emailField)).clear();
        driver.findElement(emailField).sendKeys(user);
        driver.findElement(passField).clear();
        driver.findElement(passField).sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    private void performLogout() {
        By logoutLink = By.linkText("Log out");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    private String getCurrentWindowHandle() {
        return driver.getWindowHandle();
    }


    private int getArticleCount() {
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        return articles.size();
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("realworld"),
                "Home page title does not contain 'RealWorld'");
    }

    @Test
    @Order(2)
    public void testLoginPagePresence() {
        driver.get(BASE_URL);
        Assertions.assertFalse(driver.findElements(By.cssSelector("input[name='email']")).isEmpty(),
                "Email input not found on login page");
        Assertions.assertFalse(driver.findElements(By.cssSelector("input[name='password']")).isEmpty(),
                "Password input not found on login page");
        Assertions.assertFalse(driver.findElements(By.cssSelector("button[type='submit']")).isEmpty(),
                "Login button not found on login page");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        performLogin(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/articles"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/articles"),
                "Valid login did not redirect to articles page");
        Assertions.assertFalse(driver.findElements(By.cssSelector(".article-preview")).isEmpty(),
                "No articles displayed after login");
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        By emailField = By.cssSelector("input[name='email']");
        By passField = By.cssSelector("input[name='password']");
        By loginBtn = By.cssSelector("button[type='submit']");

        wait.until(ExpectedConditions.elementToBeClickable(emailField)).clear();
        driver.findElement(emailField).sendKeys("wrong@example.com");
        driver.findElement(passField).clear();
        driver.findElement(passField).sendKeys("wrongpass");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        By errorMsg = By.cssSelector(".form-error");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
        Assertions.assertTrue(error.getText().toLowerCase().contains("invalid"),
                "Error message for invalid credentials not displayed");
    }

    @Test
    @Order(5)
    public void testFeedToggle() {
        performLogin(USERNAME, PASSWORD);
        By globalFeed = By.linkText("Global Feed");
        By yourFeed = By.linkText("Your Feed");

        // Ensure global feed is active initially
        wait.until(ExpectedConditions.elementToBeClickable(globalFeed)).click();
        int countGlobal = getArticleCount();

        // Switch to your feed
        wait.until(ExpectedConditions.elementToBeClickable(yourFeed)).click();
        int countYour = getArticleCount();

        // If user has no articles, counts may be zero but still distinct
        Assertions.assertTrue(countGlobal != countYour || countGlobal == 0,
                "Toggling between Global and Your Feed did not change article list");
    }

    @Test
    @Order(6)
    public void testBurgerMenuAllArticles() {
        performLogin(USERNAME, PASSWORD);
        By menuBtn = By.cssSelector("button[aria-label='Open Menu']");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By allArticlesLink = By.linkText("All Articles");
        wait.until(ExpectedConditions.elementToBeClickable(allArticlesLink)).click();

        wait.until(ExpectedConditions.urlContains("/articles"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/articles"),
                "Menu 'All Articles' did not navigate to articles page");
    }

    @Test
    @Order(7)
    public void testAboutExternalLink() {
        performLogin(USERNAME, PASSWORD);
        String originalHandle = getCurrentWindowHandle();

        By menuBtn = By.cssSelector("button[aria-label='Open Menu']");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By aboutLink = By.linkText("About");
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();

        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(originalHandle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("New window did not open"));

        driver.switchTo().window(newHandle);
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"),
                "About link did not open expected external domain");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(8)
    public void testLogout() {
        performLogin(USERNAME, PASSWORD);
        performLogout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Logout did not redirect to login page");
    }

    @Test
    @Order(9)
    public void testFooterSocialLinks() {
        performLogin(USERNAME, PASSWORD);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("footer a[target='_blank']"));
        Assertions.assertFalse(externalLinks.isEmpty(),
                "No external social links found in footer");

        String originalHandle = getCurrentWindowHandle();
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            link.click();

            Set<String> handles = driver.getWindowHandles();
            String newHandle = handles.stream()
                    .filter(h -> !h.equals(originalHandle))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("New window did not open"));

            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(href));
            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                    "External link URL does not contain expected domain: " + href);

            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(10)
    public void testFollowUserAndVerifyCount() {
        performLogin(USERNAME, PASSWORD);

        // Navigate to a user profile (first author in article list)
        List<WebElement> authors = driver.findElements(By.cssSelector(".article-meta .author"));
        Assertions.assertFalse(authors.isEmpty(),
                "No authors found on article list");
        WebElement firstAuthorLink = authors.get(0);
        firstAuthorLink.click();

        wait.until(ExpectedConditions.urlContains("/@"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/@"),
                "Did not navigate to user profile");

        // Record initial follower count
        By followerCount = By.cssSelector(".following");
        WebElement followerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(followerCount));
        String initialCountText = followerElement.getText().trim();
        int initialCount = Integer.parseInt(initialCountText);

        // Click Follow button
        By followBtn = By.cssSelector("button.follow-btn");
        WebElement followButton = wait.until(ExpectedConditions.elementToBeClickable(followBtn));
        followButton.click();

        // Verify follower count incremented
        WebElement followerElementAfter = wait.until(ExpectedConditions.visibilityOfElementLocated(followerCount));
        String afterCountText = followerElementAfter.getText().trim();
        int afterCount = Integer.parseInt(afterCountText);

        Assertions.assertEquals(initialCount + 1, afterCount,
                "Follower count did not increment after following user");
    }
}