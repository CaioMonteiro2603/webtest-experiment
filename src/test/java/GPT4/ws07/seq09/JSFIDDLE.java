package GPT4.ws07.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddleTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    private void openHomePage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body")));
    }

    @Test
    @Order(1)
    public void testHomepageLoads() {
        openHomePage();
        String title = driver.getTitle().toLowerCase();
        Assertions.assertTrue(title.contains("jsfiddle"), "Homepage title should contain 'JSFiddle'");
    }

    @Test
    @Order(2)
    public void testEditorComponentsPresent() {
        openHomePage();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".CodeMirror")));
        List<WebElement> editors = driver.findElements(By.cssSelector(".CodeMirror"));
        Assertions.assertEquals(4, editors.size(), "There should be 4 CodeMirror editor panes (HTML, CSS, JS, Result)");
    }

    @Test
    @Order(3)
    public void testTopNavLinksPresent() {
        openHomePage();
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        Assertions.assertFalse(navLinks.isEmpty(), "Navigation links should be present in the top nav");
    }

    @Test
    @Order(4)
    public void testTryInSandboxButton() {
        openHomePage();
        List<WebElement> buttons = driver.findElements(By.cssSelector("a[href='/api/post/library/pure/']"));
        if (!buttons.isEmpty()) {
            WebElement sandbox = buttons.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(sandbox)).click();
            wait.until(ExpectedConditions.urlContains("/api/post/library/pure/"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/api/post/library/pure/"), "Should navigate to /api/post/library/pure/");
        } else {
            Assumptions.assumeTrue(false, "Try in Sandbox button not present - skipping test.");
        }
    }

    @Test
    @Order(5)
    public void testExternalTwitterLink() {
        openHomePage();
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com/jsfiddle']")));
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        twitter.click();
        wait.until(driver -> driver.getWindowHandles().size() > oldWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);
        driver.switchTo().window(newWindows.iterator().next());
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter URL should be opened");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testExternalFacebookLink() {
        openHomePage();
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='facebook.com/jsfiddle']"));
        if (!links.isEmpty()) {
            WebElement fb = links.get(0);
            String originalWindow = driver.getWindowHandle();
            Set<String> oldWindows = driver.getWindowHandles();
            fb.click();
            wait.until(driver -> driver.getWindowHandles().size() > oldWindows.size());
            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(oldWindows);
            driver.switchTo().window(newWindows.iterator().next());
            wait.until(ExpectedConditions.urlContains("facebook.com"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook URL should be opened");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            Assumptions.assumeTrue(false, "Facebook link not found");
        }
    }

    @Test
    @Order(7)
    public void testExternalGithubLink() {
        openHomePage();
        WebElement github = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github.com/jsfiddle']")));
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        github.click();
        wait.until(driver -> driver.getWindowHandles().size() > oldWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);
        driver.switchTo().window(newWindows.iterator().next());
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "GitHub URL should be opened");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testCreateFiddleWithoutLogin() {
        openHomePage();
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("run")));
        runButton.click();
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe.result-iframe")));
        Assertions.assertTrue(resultFrame.isDisplayed(), "Result iframe should be visible after clicking Run");
    }
}
