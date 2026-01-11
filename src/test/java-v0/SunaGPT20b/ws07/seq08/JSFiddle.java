package SunaGPT20b.ws07.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

    private static final String BASE_URL = "https://jsfiddle.net/";
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

    /** Verify that the home page loads and the title contains "JSFiddle". */
    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(body.isDisplayed(), "Home page body should be displayed");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jsfiddle"),
                "Page title should contain 'JSFiddle'");
    }

    /** Click the "Explore" navigation link and verify navigation to the explore page. */
    @Test
    @Order(2)
    public void testExplorePageNavigation() {
        driver.get(BASE_URL);
        // The Explore link is usually identified by its visible text.
        WebElement exploreLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[normalize-space()='Explore']")));
        exploreLink.click();

        wait.until(ExpectedConditions.urlContains("/explore"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/explore"),
                "URL should contain '/explore' after clicking Explore link");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("explore"),
                "Page title should indicate Explore page");
    }

    /** Open the first example from the Explore page (one level below) and verify it loads. */
    @Test
    @Order(3)
    public void testFirstExampleLoads() {
        driver.get(BASE_URL);
        // Navigate to Explore page first.
        WebElement exploreLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[normalize-space()='Explore']")));
        exploreLink.click();

        // Wait for example cards to be present and click the first one.
        WebElement firstExample = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("section a[href*='/fiddle/']")));
        firstExample.click();

        wait.until(ExpectedConditions.urlContains("/fiddle/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/fiddle/"),
                "URL should contain '/fiddle/' after opening an example");
        // Verify that the editor area is present.
        WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#editor")));
        Assertions.assertTrue(editor.isDisplayed(), "Editor should be displayed on example page");
    }

    /** Verify that the Twitter footer link opens an external page and contains the expected domain. */
    @Test
    @Order(4)
    public void testFooterTwitterLink() {
        driver.get(BASE_URL);
        // Footer Twitter link typically contains "twitter.com" in href.
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("footer a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();

        twitterLink.click();

        // Wait for a new window/tab if it opens.
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("twitter.com"),
                "External link should navigate to a Twitter domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    /** Verify that the GitHub footer link opens an external page and contains the expected domain. */
    @Test
    @Order(5)
    public void testFooterGitHubLink() {
        driver.get(BASE_URL);

        WebElement githubLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("footer a[href*='github.com']"))
        );

        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        githubLink.click();

        wait.until(d -> d.getWindowHandles().size() > before.size());

        Set<String> after = driver.getWindowHandles();
        after.removeAll(before);
        String newWindow = after.iterator().next();

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("github.com"));

        Assertions.assertTrue(
                driver.getCurrentUrl().toLowerCase().contains("github.com"),
                "External link should navigate to a GitHub domain"
        );

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}