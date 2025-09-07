package Qwen3.ws09.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConduitTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String LOGIN_EMAIL = "testuser@example.com";
    private static final String LOGIN_PASSWORD = "password123";

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
    public void testHomePageLoadAndFeed() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-header")));

        assertEquals("Conduit", driver.getTitle(), "Page title should be 'Conduit'.");
        assertTrue(driver.findElement(By.cssSelector("app-header")).isDisplayed(), "Header should be visible.");
        assertTrue(driver.findElements(By.cssSelector("article[preview]")).size() > 0,
                "Feed should contain at least one article preview.");
    }

    @Test
    @Order(2)
    public void testGlobalFeedNavigation() {
        driver.get(BASE_URL);

        // Click Global Feed link
        WebElement globalFeedLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/']"))); // The home link represents Global Feed
        globalFeedLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.findElement(By.cssSelector("div.feed-toggle")).isDisplayed(),
                "Feed toggle should be visible on Global Feed page.");
    }

    @Test
    @Order(3)
    public void testArticleDetails() {
        driver.get(BASE_URL);

        // Click on the first article to view details (one level deep)
        WebElement firstArticleLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("article[preview] a.preview-link")));
        firstArticleLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("article.page-article")));
        assertTrue(driver.findElement(By.cssSelector("article.page-article")).isDisplayed(),
                "Full article page should be displayed.");
    }

    @Test
    @Order(4)
    public void testValidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign in")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']"))).sendKeys(LOGIN_EMAIL);
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(LOGIN_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("testuser")));
        assertTrue(driver.findElement(By.linkText("testuser")).isDisplayed(),
                "User profile link should be visible after successful login.");
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign in")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']"))).sendKeys("invalid@example.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.error-messages")));
        assertTrue(errorElement.isDisplayed(), "Error message should be displayed for invalid login.");
        assertTrue(errorElement.getText().contains("email or password is invalid"),
                "Error message text should indicate invalid credentials.");
    }

    @Test
    @Order(6)
    public void testCreateNewArticle() {
        // Ensure user is logged in
        testValidLogin();

        driver.findElement(By.linkText("New Article")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form")));

        // Fill in article details
        driver.findElement(By.cssSelector("input[formcontrolname='title']")).sendKeys("Test Article Title");
        driver.findElement(By.cssSelector("input[formcontrolname='description']")).sendKeys("Test article description.");
        driver.findElement(By.cssSelector("textarea[formcontrolname='body']")).sendKeys("This is the body of the test article.");
        driver.findElement(By.cssSelector("input[formcontrolname='tagList']")).sendKeys("test,article");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Wait for navigation to the new article page
        wait.until(ExpectedConditions.urlContains("/article/test-article-title"));
        assertTrue(driver.findElement(By.cssSelector("h1")).getText().contains("Test Article Title"),
                "New article page should be displayed after creation.");
    }

    @Test
    @Order(7)
    public void testUserRegistration_Negative() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign up")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Username']"))).sendKeys("testuser");
        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys(LOGIN_EMAIL); // Use existing email
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(LOGIN_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Expect an error for duplicate user
        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.error-messages")));
        assertTrue(errorElement.isDisplayed(), "Error message should be displayed for duplicate registration.");
    }

    @Test
    @Order(8)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // The page uses a placeholder link, but we'll check for any external-looking ones.
        // Since there aren't standard social links, we'll test the "Source" link which goes to GitHub.
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a[href^='http']"));
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href").contains("github.com")) {
                link.click();
                assertExternalLinkAndReturn(originalWindow, "github.com");
                break;
            }
        }
    }

    @Test
    @Order(9)
    public void testLogout() {
        // Ensure user is logged in
        testValidLogin();

        driver.findElement(By.linkText("testuser")).click(); // Open user menu
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign out"))).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign in")));
        assertTrue(driver.findElement(By.linkText("Sign in")).isDisplayed(),
                "Sign in link should be visible after logout.");
    }

    // --- Helper Methods ---

    private void assertExternalLinkAndReturn(String originalWindow, String expectedDomain) {
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        assertNotNull(newWindow, "A new window should have been opened for " + expectedDomain);
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "New window URL should contain " + expectedDomain + ". URL was: " + driver.getCurrentUrl());
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}