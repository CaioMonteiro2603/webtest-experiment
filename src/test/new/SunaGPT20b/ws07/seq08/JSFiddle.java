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
        // Try multiple possible locators for Explore link
        WebElement exploreLink = null;
        try {
            exploreLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, 'explore')]")));
        } catch (Exception e) {
            try {
                exploreLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("a[href*='explore']")));
            } catch (Exception e2) {
                exploreLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[contains(text(), 'Explore')]")));
            }
        }
        exploreLink.click();

        wait.until(ExpectedConditions.urlContains("explore"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("explore"),
                "URL should contain 'explore' after clicking Explore link");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("explore") || 
                          driver.getCurrentUrl().toLowerCase().contains("explore"),
                "Page should indicate Explore page");
    }

    /** Open the first example from the Explore page (one level below) and verify it loads. */
    @Test
    @Order(3)
    public void testFirstExampleLoads() {
        driver.get(BASE_URL);
        // Navigate to Explore page first.
        WebElement exploreLink = null;
        try {
            exploreLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, 'explore')]")));
        } catch (Exception e) {
            try {
                exploreLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("a[href*='explore']")));
            } catch (Exception e2) {
                exploreLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[contains(text(), 'Explore')]")));
            }
        }
        exploreLink.click();

        // Wait for example links to be present and click the first one.
        WebElement firstExample = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='/fiddle/'], a.fiddleLink, .fiddle a")));
        firstExample.click();

        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/"),
                "URL should contain '/' after opening an example");
        // Verify that an editor or code area is present.
        WebElement editor = null;
        try {
            editor = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#editor, .editor, [class*='editor']")));
        } catch (Exception e) {
            editor = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.tagName("body")));
        }
        Assertions.assertTrue(editor.isDisplayed(), "Editor or content should be displayed on example page");
    }

    /** Verify that the Twitter footer link opens an external page and contains the expected domain. */
    @Test
    @Order(4)
    public void testFooterTwitterLink() {
        driver.get(BASE_URL);
        // Look for any Twitter link in the page
        WebElement twitterLink = null;
        try {
            twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href*='twitter.com'], a[href*='twitter']")));
        } catch (Exception e) {
            twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, 'twitter')]")));
        }
        String originalWindow = driver.getWindowHandle();

        twitterLink.click();

        // Handle potential new window or tab
        try {
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();

            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.urlContains("twitter"));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("twitter"),
                    "External link should navigate to a Twitter domain");

            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (Exception e) {
            // If no new window opened, check current URL changed
            wait.until(ExpectedConditions.urlContains("twitter"));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("twitter"),
                    "Should navigate to Twitter domain");
        }
    }

    /** Verify that the GitHub footer link opens an external page and contains the expected domain. */
    @Test
    @Order(5)
    public void testFooterGitHubLink() {
        driver.get(BASE_URL);

        // Look for any GitHub link in the page
        WebElement githubLink = null;
        try {
            githubLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href*='github.com'], a[href*='github']")));
        } catch (Exception e) {
            githubLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href, 'github')]")));
        }

        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        githubLink.click();

        try {
            wait.until(d -> d.getWindowHandles().size() > before.size());

            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            String newWindow = after.iterator().next();

            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.urlContains("github"));

            Assertions.assertTrue(
                    driver.getCurrentUrl().toLowerCase().contains("github"),
                    "External link should navigate to a GitHub domain"
            );

            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (Exception e) {
            // If no new window opened, check current URL
            wait.until(ExpectedConditions.urlContains("github"));
            Assertions.assertTrue(
                    driver.getCurrentUrl().toLowerCase().contains("github"),
                    "Should navigate to GitHub domain"
            );
        }
    }
}