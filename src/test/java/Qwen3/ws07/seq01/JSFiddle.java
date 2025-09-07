package Qwen3.ws07.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsFiddleTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jsfiddle.net/";

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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        assertTrue(driver.findElement(By.id("editor")).isDisplayed(), "Main editor area should be visible on home page.");
    }

    @Test
    @Order(2)
    public void testCreateNewFiddle() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("editAction"))).click();
        
        // After clicking "New", the editor should be ready
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("#iframeEditor")));
        // We are now inside the editor iframe. Check for a common element.
        driver.switchTo().defaultContent(); // Switch back to main content to check other elements
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("panelJS")));
        assertTrue(driver.findElement(By.id("panelJS")).isDisplayed(), "JavaScript panel should be visible in a new fiddle.");
    }

    @Test
    @Order(3)
    public void testRunFiddle() {
        driver.get(BASE_URL);
        // Navigate to a simple existing fiddle to run
        driver.get("https://jsfiddle.net/caio/dL3nb8q9/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("editor")));
        
        // Click Run button
        driver.findElement(By.id("run")).click();
        
        // Wait for the result iframe to load
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("resultiframe")));
        // Check if the result contains expected content (from the fiddle's JS output)
        String bodyText = driver.findElement(By.tagName("body")).getText();
        driver.switchTo().defaultContent(); // Switch back

        // The fiddle outputs "Hello from JSFiddle!" to the result pane's body
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector("#result iframe#resultiframe"), "Hello from JSFiddle!"));
    }

    @Test
    @Order(4)
    public void testExplorePage() {
        // Navigate to Explore page (one level deep)
        driver.get(BASE_URL + "explore/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.items-grid")));

        assertTrue(driver.findElement(By.cssSelector("div.items-grid")).isDisplayed(),
                "Items grid should be visible on Explore page.");
        
        // Verify that multiple fiddle items are present
        assertTrue(driver.findElements(By.cssSelector("div.items-grid .item")).size() > 5,
                "There should be multiple fiddle items listed on the Explore page.");
    }

    @Test
    @Order(5)
    public void testLoginPageNavigation() {
        driver.get(BASE_URL);
        // Click Login link
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Login"))).click();
        
        // Assert navigation to login page (one level deep)
        wait.until(ExpectedConditions.urlContains("/user/login/"));
        assertEquals("https://jsfiddle.net/user/login/", driver.getCurrentUrl(), "URL should be the login page.");
        assertTrue(driver.findElement(By.name("username")).isDisplayed(), "Username field should be present on login page.");
    }

    @Test
    @Order(6)
    public void testDashboardAccessWithoutLogin_Redirects() {
        // Try to access Dashboard without login
        driver.get("https://jsfiddle.net/user/dashboard/");
        
        // Should be redirected to login page
        wait.until(ExpectedConditions.urlContains("/user/login/"));
        assertTrue(driver.getCurrentUrl().contains("/user/login/"),
                "Accessing Dashboard without login should redirect to login page.");
    }

    @Test
    @Order(7)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Click LinkedIn link in footer
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedinLink.click();
        assertExternalLinkAndReturn(originalWindow, "linkedin.com");

        // Click YouTube link in footer
        driver.get(BASE_URL); // Reset state
        originalWindow = driver.getWindowHandle();
        WebElement youtubeLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='youtube.com']")));
        youtubeLink.click();
        assertExternalLinkAndReturn(originalWindow, "youtube.com");
        
        // Click Twitter link in footer
        driver.get(BASE_URL); // Reset state
        originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        assertExternalLinkAndReturn(originalWindow, "twitter.com");
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