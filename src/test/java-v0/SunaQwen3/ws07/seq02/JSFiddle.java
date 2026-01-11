package SunaQwen3.ws07.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {
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
    public void testHomePageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.equals(BASE_URL) || currentUrl.equals(BASE_URL + "/"), 
                   "Browser should be on the home page URL");

        String title = driver.getTitle();
        assertTrue(title.contains("JSFiddle") || title.contains("Online JavaScript Editor"), 
                   "Page title should contain 'JSFiddle' or 'Online JavaScript Editor'");
    }

    @Test
    @Order(2)
    public void testNavigationToExamplesPage() {
        driver.get(BASE_URL);
        By examplesLink = By.cssSelector("a[href='/examples']");
        WebElement examples = wait.until(ExpectedConditions.elementToBeClickable(examplesLink));
        examples.click();

        wait.until(ExpectedConditions.urlContains("/examples"));
        assertTrue(driver.getCurrentUrl().contains("/examples"), 
                   "Should navigate to /examples page");
    }

    @Test
    @Order(3)
    public void testNavigationToApiPage() {
        driver.get(BASE_URL);
        By apiLink = By.cssSelector("a[href='/api']");
        WebElement api = wait.until(ExpectedConditions.elementToBeClickable(apiLink));
        api.click();

        wait.until(ExpectedConditions.urlContains("/api"));
        assertTrue(driver.getCurrentUrl().contains("/api"), 
                   "Should navigate to /api page");
    }

    @Test
    @Order(4)
    public void testNavigationToHelpPage() {
        driver.get(BASE_URL);
        By helpLink = By.cssSelector("a[href='/help']");
        WebElement help = wait.until(ExpectedConditions.elementToBeClickable(helpLink));
        help.click();

        wait.until(ExpectedConditions.urlContains("/help"));
        assertTrue(driver.getCurrentUrl().contains("/help"), 
                   "Should navigate to /help page");
    }

    @Test
    @Order(5)
    public void testNavigationToBlogPage() {
        driver.get(BASE_URL);
        By blogLink = By.cssSelector("a[href='https://blog.jsfiddle.net/']");
        WebElement blog = wait.until(ExpectedConditions.elementToBeClickable(blogLink));
        blog.click();

        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("blog.jsfiddle.net"), 
                   "New tab should be on blog.jsfiddle.net domain");

        // Close tab and return
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(6)
    public void testFooterTwitterLink() {
        driver.get(BASE_URL);
        By twitterLink = By.cssSelector("footer a[href='https://twitter.com/js_fiddle']");
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        twitter.click();

        String originalHandle = driver.getWindowHandle();
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("twitter.com"), 
                   "Twitter link should open a URL containing 'twitter.com'");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(7)
    public void testFooterFacebookLink() {
        driver.get(BASE_URL);
        By facebookLink = By.cssSelector("footer a[href='https://www.facebook.com/jsfiddle']");
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(facebookLink));
        facebook.click();

        String originalHandle = driver.getWindowHandle();
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("facebook.com"), 
                   "Facebook link should open a URL containing 'facebook.com'");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(8)
    public void testFooterLinkedInLink() {
        driver.get(BASE_URL);
        By linkedInLink = By.cssSelector("footer a[href='https://www.linkedin.com/company/jsfiddle']");
        WebElement linkedIn = wait.until(ExpectedConditions.elementToBeClickable(linkedInLink));
        linkedIn.click();

        String originalHandle = driver.getWindowHandle();
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), 
                   "LinkedIn link should open a URL containing 'linkedin.com'");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(9)
    public void testNavigationToPricingPage() {
        driver.get(BASE_URL);
        By pricingLink = By.cssSelector("a[href='/pricing']");
        WebElement pricing = wait.until(ExpectedConditions.elementToBeClickable(pricingLink));
        pricing.click();

        wait.until(ExpectedConditions.urlContains("/pricing"));
        assertTrue(driver.getCurrentUrl().contains("/pricing"), 
                   "Should navigate to /pricing page");
    }

    @Test
    @Order(10)
    public void testNavigationToTeamPage() {
        driver.get(BASE_URL);
        By teamLink = By.cssSelector("a[href='/team']");
        WebElement team = wait.until(ExpectedConditions.elementToBeClickable(teamLink));
        team.click();

        wait.until(ExpectedConditions.urlContains("/team"));
        assertTrue(driver.getCurrentUrl().contains("/team"), 
                   "Should navigate to /team page");
    }

    @Test
    @Order(11)
    public void testNavigationToPrivacyPage() {
        driver.get(BASE_URL);
        By privacyLink = By.cssSelector("a[href='/privacy']");
        WebElement privacy = wait.until(ExpectedConditions.elementToBeClickable(privacyLink));
        privacy.click();

        wait.until(ExpectedConditions.urlContains("/privacy"));
        assertTrue(driver.getCurrentUrl().contains("/privacy"), 
                   "Should navigate to /privacy page");
    }

    @Test
    @Order(12)
    public void testNavigationToTermsPage() {
        driver.get(BASE_URL);
        By termsLink = By.cssSelector("a[href='/terms']");
        WebElement terms = wait.until(ExpectedConditions.elementToBeClickable(termsLink));
        terms.click();

        wait.until(ExpectedConditions.urlContains("/terms"));
        assertTrue(driver.getCurrentUrl().contains("/terms"), 
                   "Should navigate to /terms page");
    }

    @Test
    @Order(13)
    public void testNavigationToStatusPage() {
        driver.get(BASE_URL);
        By statusLink = By.cssSelector("a[href='https://status.jsfiddle.net/']");
        WebElement status = wait.until(ExpectedConditions.elementToBeClickable(statusLink));
        status.click();

        String originalHandle = driver.getWindowHandle();
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("status.jsfiddle.net"), 
                   "Status link should open a URL containing 'status.jsfiddle.net'");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(14)
    public void testNavigationToContactPage() {
        driver.get(BASE_URL);
        By contactLink = By.cssSelector("a[href='/contact']");
        WebElement contact = wait.until(ExpectedConditions.elementToBeClickable(contactLink));
        contact.click();

        wait.until(ExpectedConditions.urlContains("/contact"));
        assertTrue(driver.getCurrentUrl().contains("/contact"), 
                   "Should navigate to /contact page");
    }

    @Test
    @Order(15)
    public void testNavigationToEmbedPage() {
        driver.get(BASE_URL);
        By embedLink = By.cssSelector("a[href='/embed']");
        WebElement embed = wait.until(ExpectedConditions.elementToBeClickable(embedLink));
        embed.click();

        wait.until(ExpectedConditions.urlContains("/embed"));
        assertTrue(driver.getCurrentUrl().contains("/embed"), 
                   "Should navigate to /embed page");
    }

    @Test
    @Order(16)
    public void testNavigationToKeyboardShortcutsPage() {
        driver.get(BASE_URL);
        By shortcutsLink = By.cssSelector("a[href='/shortcuts']");
        WebElement shortcuts = wait.until(ExpectedConditions.elementToBeClickable(shortcutsLink));
        shortcuts.click();

        wait.until(ExpectedConditions.urlContains("/shortcuts"));
        assertTrue(driver.getCurrentUrl().contains("/shortcuts"), 
                   "Should navigate to /shortcuts page");
    }

    @Test
    @Order(17)
    public void testNavigationToChangelogPage() {
        driver.get(BASE_URL);
        By changelogLink = By.cssSelector("a[href='/changelog']");
        WebElement changelog = wait.until(ExpectedConditions.elementToBeClickable(changelogLink));
        changelog.click();

        wait.until(ExpectedConditions.urlContains("/changelog"));
        assertTrue(driver.getCurrentUrl().contains("/changelog"), 
                   "Should navigate to /changelog page");
    }

    @Test
    @Order(18)
    public void testNavigationToSecurityPage() {
        driver.get(BASE_URL);
        By securityLink = By.cssSelector("a[href='/security']");
        WebElement security = wait.until(ExpectedConditions.elementToBeClickable(securityLink));
        security.click();

        wait.until(ExpectedConditions.urlContains("/security"));
        assertTrue(driver.getCurrentUrl().contains("/security"), 
                   "Should navigate to /security page");
    }

    @Test
    @Order(19)
    public void testNavigationToAccessibilityPage() {
        driver.get(BASE_URL);
        By accessibilityLink = By.cssSelector("a[href='/accessibility']");
        WebElement accessibility = wait.until(ExpectedConditions.elementToBeClickable(accessibilityLink));
        accessibility.click();

        wait.until(ExpectedConditions.urlContains("/accessibility"));
        assertTrue(driver.getCurrentUrl().contains("/accessibility"), 
                   "Should navigate to /accessibility page");
    }

    @Test
    @Order(20)
    public void testNavigationToCookiesPage() {
        driver.get(BASE_URL);
        By cookiesLink = By.cssSelector("a[href='/cookies']");
        WebElement cookies = wait.until(ExpectedConditions.elementToBeClickable(cookiesLink));
        cookies.click();

        wait.until(ExpectedConditions.urlContains("/cookies"));
        assertTrue(driver.getCurrentUrl().contains("/cookies"), 
                   "Should navigate to /cookies page");
    }
}