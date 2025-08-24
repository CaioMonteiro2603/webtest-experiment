package GPT4.ws09.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class RealWorldTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Conduit"));
        Assertions.assertTrue(driver.getTitle().contains("Conduit"), "Homepage title does not contain 'Conduit'");
    }

    @Test
    @Order(2)
    public void testSignInNavigation() {
        driver.get(BASE_URL);
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='#login']")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("#login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#login"), "URL does not contain '#login'");
    }

    @Test
    @Order(3)
    public void testSignUpNavigation() {
        driver.get(BASE_URL);
        WebElement signUp = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='#register']")));
        signUp.click();
        wait.until(ExpectedConditions.urlContains("#register"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#register"), "URL does not contain '#register'");
    }

    @Test
    @Order(4)
    public void testFooterTwitterLink() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'twitter.com')]")));
        String originalWindow = driver.getWindowHandle();
        footerLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("twitter.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "External link did not navigate to Twitter");
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testFooterGitHubLink() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'github.com')]")));
        String originalWindow = driver.getWindowHandle();
        footerLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("github.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "External link did not navigate to GitHub");
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testGlobalFeedPresence() {
        driver.get(BASE_URL);
        WebElement globalFeed = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[text()='Global Feed']")));
        Assertions.assertTrue(globalFeed.isDisplayed(), "Global Feed tab is not displayed");
    }

    @Test
    @Order(7)
    public void testPopularTagsPresent() {
        driver.get(BASE_URL);
        WebElement tagsBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("tag-list")));
        List<WebElement> tags = tagsBox.findElements(By.tagName("a"));
        Assertions.assertFalse(tags.isEmpty(), "No popular tags found");
    }

    @Test
    @Order(8)
    public void testArticleLinksClickable() {
        driver.get(BASE_URL);
        List<WebElement> articles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".preview-link")));
        if (!articles.isEmpty()) {
            WebElement firstArticle = wait.until(ExpectedConditions.elementToBeClickable(articles.get(0)));
            firstArticle.click();
            wait.until(ExpectedConditions.urlMatches(".*/article/.*"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/article/"), "Did not navigate to article detail page");
        } else {
            Assertions.fail("No article previews found on the homepage");
        }
    }

    @Test
    @Order(9)
    public void testTryNavigateToSettingsWithoutLogin() {
        driver.get(BASE_URL);
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='#settings']")));
        settingsLink.click();
        wait.until(ExpectedConditions.urlContains("#settings"));
        WebElement errorPrompt = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@href='#login']")));
        Assertions.assertTrue(errorPrompt.isDisplayed(), "User should be redirected to login when accessing settings unauthenticated");
    }
}
