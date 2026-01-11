package SunaGPT20b.ws09.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static WebDriver driver;
    private static WebDriverWait wait;

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

    /* -------------------- Helper Methods -------------------- */

    private static void login(String email, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in"))).click();

        WebElement emailField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Email']")));
        emailField.clear();
        emailField.sendKeys(email);

        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.clear();
        passwordField.sendKeys(password);

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Verify login succeeded by waiting for the "Your Feed" or "Global Feed" link
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Your Feed")));
        } catch (TimeoutException e) {
            // Try alternative locator for logged in state
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Global Feed")));
        }
    }

    private static void logout() {
        // Navigate to Settings page and click logout button
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Settings"))).click();
        WebElement logoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Or click here to logout')]")));
        logoutBtn.click();
        // Verify we are back on the login page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in")));
    }

    private static String getLoggedInUsername() {
        WebElement userLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href^='#/@']")));
        return userLink.getText().trim();
    }

    private static void openNewPost() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("New Post"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']")));
    }

    private static void publishArticle(String title, String description, String body, String tags) {
        driver.findElement(By.cssSelector("input[placeholder='Article Title']")).sendKeys(title);
        driver.findElement(By.cssSelector("input[placeholder=\"What's this article about?\"]")).sendKeys(description);
        driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']")).sendKeys(body);
        driver.findElement(By.cssSelector("input[placeholder='Enter tags']")).sendKeys(tags);
        driver.findElement(By.xpath("//button[contains(text(),'Publish Article')]")).click();

        // Verify article page loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals(title,
                driver.findElement(By.tagName("h1")).getText(),
                "Published article title should match the input title");
    }

    private static void deleteCurrentArticle() {
        WebElement deleteBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Delete Article')]")));
        deleteBtn.click();
        // After deletion we are redirected to home
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Your Feed")));
        } catch (TimeoutException e) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Global Feed")));
        }
    }

    private static void assertArticleNotInProfile(String username, String articleTitle) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(username))).click();
        List<WebElement> titles = driver.findElements(By.linkText(articleTitle));
        Assertions.assertTrue(titles.isEmpty(),
                "Article titled '" + articleTitle + "' should not be present in the profile list");
    }

    private static void clickExternalLinkAndValidate(String partialHref, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        Set<String> existingWindows = driver.getWindowHandles();

        WebElement link = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='" + partialHref + "']")));
        link.click();

        // Wait for new window
        wait.until(driver -> driver.getWindowHandles().size() > existingWindows.size());

        Set<String> allWindows = driver.getWindowHandles();
        allWindows.removeAll(existingWindows);
        String newWindow = allWindows.iterator().next();

        driver.switchTo().window(newWindow);
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link should navigate to a URL containing '" + expectedDomain + "'");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    /* -------------------- Test Cases -------------------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login("test@example.com", "test123");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/"),
                "URL should contain '#/' after successful login");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in"))).click();

        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys("test@example.com");
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Try multiple possible error message selectors
        WebElement error = null;
        try {
            error = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        } catch (TimeoutException e) {
            try {
                error = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
            } catch (TimeoutException e2) {
                error = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(@class,'error')]")));
            }
        }
        Assertions.assertTrue(error.isDisplayed(),
                "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testCreateAndDeleteArticle() {
        login("test@example.com", "test123");
        String title = "Selenium Test Article";
        String description = "Testing article creation";
        String body = "This is the body of the Selenium test article.";
        String tags = "test,selenium";

        openNewPost();
        publishArticle(title, description, body, tags);
        deleteCurrentArticle();

        String username = getLoggedInUsername();
        assertArticleNotInProfile(username, title);
        logout();
    }

    @Test
    @Order(4)
    public void testNavigationLinks() {
        login("test@example.com", "test123");

        // Home
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home"))).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/"),
                "Home link should navigate to the main feed");

        // Settings
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Settings"))).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/settings"),
                "Settings link should navigate to settings page");

        // Profile
        String username = getLoggedInUsername();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(username))).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/@" + username),
                "Profile link should navigate to the user's profile page");

        logout();
    }

    @Test
    @Order(5)
    public void testExternalFooterLinks() {
        login("test@example.com", "test123");

        // GitHub link
        clickExternalLinkAndValidate("github.com", "github.com");

        // Twitter link (if present)
        try {
            clickExternalLinkAndValidate("twitter.com", "twitter.com");
        } catch (TimeoutException ignored) {
            // Some deployments may not have a Twitter link; ignore if not found
        }

        logout();
    }

    @Test
    @Order(6)
    public void testLogoutFunctionality() {
        login("test@example.com", "test123");
        // Perform logout via Settings page
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Settings"))).click();
        WebElement logoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Or click here to logout')]")));
        logoutBtn.click();

        // Verify we are back on the login page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/login"),
                "After logout, URL should contain '#/login'");
    }
}