package SunaQwen3.ws07.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final String EXTERNAL_LINKEDIN = "linkedin.com";
    private static final String EXTERNAL_TWITTER = "twitter.com";
    private static final String EXTERNAL_FACEBOOK = "facebook.com";

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
    void testHomePageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("jsfiddle.net"), "URL should contain jsfiddle.net");
        assertTrue(driver.getTitle().contains("JSFiddle"), "Page title should contain JSFiddle");
    }

    @Test
    @Order(2)
    void testNavigationToExamplesPage() {
        driver.get(BASE_URL);
        By examplesLink = By.cssSelector("a[href='/examples']");
        WebElement examples = wait.until(ExpectedConditions.elementToBeClickable(examplesLink));
        examples.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/examples"));
        assertTrue(driver.getCurrentUrl().contains("/examples"), "Should navigate to /examples");
        assertTrue(driver.getTitle().contains("Examples"), "Title should reflect Examples page");
    }

    @Test
    @Order(3)
    void testNavigationToApiPage() {
        driver.get(BASE_URL);
        By apiLink = By.cssSelector("a[href='/api']");
        WebElement api = wait.until(ExpectedConditions.elementToBeClickable(apiLink));
        api.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/api"));
        assertTrue(driver.getCurrentUrl().contains("/api"), "Should navigate to /api");
        assertTrue(driver.getTitle().contains("API"), "Title should reflect API page");
    }

    @Test
    @Order(4)
    void testNavigationToAboutPage() {
        driver.get(BASE_URL);
        By aboutLink = By.cssSelector("a[href='/about']");
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        about.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/about"));
        assertTrue(driver.getCurrentUrl().contains("/about"), "Should navigate to /about");
        assertTrue(driver.getTitle().contains("About"), "Title should reflect About page");
    }

    @Test
    @Order(5)
    void testExternalFooterLinkedInLinkOpensInNewTab() {
        driver.get(BASE_URL);
        By linkedInLink = By.cssSelector("footer a[href*='linkedin']");
        WebElement linkedIn = wait.until(ExpectedConditions.elementToBeClickable(linkedInLink));
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open('');");
        driver.switchTo().window(driver.getWindowHandles().toArray(new String[0])[1]);
        driver.get(linkedIn.getAttribute("href"));

        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains(EXTERNAL_LINKEDIN), "LinkedIn URL should contain linkedin.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    void testExternalFooterTwitterLinkOpensInNewTab() {
        driver.get(BASE_URL);
        By twitterLink = By.cssSelector("footer a[href*='twitter']");
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open('');");
        driver.switchTo().window(driver.getWindowHandles().toArray(new String[0])[1]);
        driver.get(twitter.getAttribute("href"));

        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains(EXTERNAL_TWITTER), "Twitter URL should contain twitter.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    void testExternalFooterFacebookLinkOpensInNewTab() {
        driver.get(BASE_URL);
        By facebookLink = By.cssSelector("footer a[href*='facebook']");
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(facebookLink));
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open('');");
        driver.switchTo().window(driver.getWindowHandles().toArray(new String[0])[1]);
        driver.get(facebook.getAttribute("href"));

        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains(EXTERNAL_FACEBOOK), "Facebook URL should contain facebook.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    void testSearchFunctionalityReturnsResults() {
        driver.get(BASE_URL);
        By searchInput = By.name("q");
        By searchButton = By.cssSelector("input[type='submit'][value='Search']");

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        input.clear();
        input.sendKeys("javascript");

        WebElement button = driver.findElement(searchButton);
        button.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("q=javascript"));
        assertTrue(driver.getCurrentUrl().contains("q=javascript"), "URL should contain search query");

        By resultItems = By.cssSelector(".result");
        List<WebElement> results = driver.findElements(resultItems);
        assertTrue(results.size() > 0, "At least one search result should be displayed");
    }

    @Test
    @Order(9)
    void testNavigationToPricingPage() {
        driver.get(BASE_URL);
        By pricingLink = By.cssSelector("a[href='/pricing']");
        WebElement pricing = wait.until(ExpectedConditions.elementToBeClickable(pricingLink));
        pricing.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/pricing"));
        assertTrue(driver.getCurrentUrl().contains("/pricing"), "Should navigate to /pricing");
        assertTrue(driver.getTitle().contains("Pricing"), "Title should reflect Pricing page");
    }

    @Test
    @Order(10)
    void testNavigationToBlogPage() {
        driver.get(BASE_URL);
        By blogLink = By.cssSelector("a[href='https://blog.jsfiddle.net/']");
        WebElement blog = wait.until(ExpectedConditions.elementToBeClickable(blogLink));
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open('');");
        driver.switchTo().window(driver.getWindowHandles().toArray(new String[0])[1]);
        driver.get(blog.getAttribute("href"));

        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains("blog.jsfiddle.net"), "Blog URL should contain blog.jsfiddle.net");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(11)
    void testNavigationToSupportPage() {
        driver.get(BASE_URL);
        By supportLink = By.cssSelector("a[href='/support']");
        WebElement support = wait.until(ExpectedConditions.elementToBeClickable(supportLink));
        support.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/support"));
        assertTrue(driver.getCurrentUrl().contains("/support"), "Should navigate to /support");
        assertTrue(driver.getTitle().contains("Support"), "Title should reflect Support page");
    }

    @Test
    @Order(12)
    void testNavigationToPrivacyPolicyPage() {
        driver.get(BASE_URL);
        By privacyLink = By.cssSelector("a[href='/privacy']");
        WebElement privacy = wait.until(ExpectedConditions.elementToBeClickable(privacyLink));
        privacy.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/privacy"));
        assertTrue(driver.getCurrentUrl().contains("/privacy"), "Should navigate to /privacy");
        assertTrue(driver.getTitle().contains("Privacy"), "Title should reflect Privacy Policy");
    }

    @Test
    @Order(13)
    void testNavigationToTermsOfServicePage() {
        driver.get(BASE_URL);
        By termsLink = By.cssSelector("a[href='/terms']");
        WebElement terms = wait.until(ExpectedConditions.elementToBeClickable(termsLink));
        terms.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/terms"));
        assertTrue(driver.getCurrentUrl().contains("/terms"), "Should navigate to /terms");
        assertTrue(driver.getTitle().contains("Terms"), "Title should reflect Terms of Service");
    }

    @Test
    @Order(14)
    void testNavigationToStatusPage() {
        driver.get(BASE_URL);
        By statusLink = By.cssSelector("a[href='https://status.jsfiddle.net/']");
        WebElement status = wait.until(ExpectedConditions.elementToBeClickable(statusLink));
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open('');");
        driver.switchTo().window(driver.getWindowHandles().toArray(new String[0])[1]);
        driver.get(status.getAttribute("href"));

        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains("status.jsfiddle.net"), "Status URL should contain status.jsfiddle.net");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(15)
    void testNavigationToChangelogPage() {
        driver.get(BASE_URL);
        By changelogLink = By.cssSelector("a[href='/changelog']");
        WebElement changelog = wait.until(ExpectedConditions.elementToBeClickable(changelogLink));
        changelog.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/changelog"));
        assertTrue(driver.getCurrentUrl().contains("/changelog"), "Should navigate to /changelog");
        assertTrue(driver.getTitle().contains("Changelog"), "Title should reflect Changelog");
    }
}