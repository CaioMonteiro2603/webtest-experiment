package SunaQwen3.ws07.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JsFiddleTestSuite {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final String LOGIN_PAGE_URL = BASE_URL;
    private static final String INVENTORY_PAGE_URL = BASE_URL;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        assertTrue(title.toLowerCase().contains("jsfiddle"), "Page title should contain 'jsfiddle'");
        assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"), "Current URL should contain jsfiddle.net");
    }

    @Test
    @Order(2)
    public void testNavigationToExamplesPage() {
        driver.get(BASE_URL);
        By examplesLink = By.linkText("Examples");
        WebElement examples = wait.until(ExpectedConditions.elementToBeClickable(examplesLink));
        examples.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/demos/"));
        assertTrue(driver.getCurrentUrl().contains("/demos/"), "Should navigate to /demos/ path after clicking Examples");
    }

    @Test
    @Order(3)
    public void testNavigationToDocumentationPage() {
        driver.get(BASE_URL);
        By documentationLink = By.linkText("Documentation");
        WebElement documentation = wait.until(ExpectedConditions.elementToBeClickable(documentationLink));
        documentation.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/docs/"));
        assertTrue(driver.getCurrentUrl().contains("/docs/"), "Should navigate to /docs/ path after clicking Documentation");
    }

    @Test
    @Order(4)
    public void testNavigationToSupportPage() {
        driver.get(BASE_URL);
        By supportLink = By.linkText("Support");
        WebElement support = wait.until(ExpectedConditions.elementToBeClickable(supportLink));
        support.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/support/"));
        assertTrue(driver.getCurrentUrl().contains("/support/"), "Should navigate to /support/ path after clicking Support");
    }

    @Test
    @Order(5)
    public void testNavigationToBlogPage() {
        driver.get(BASE_URL);
        By blogLink = By.linkText("Blog");
        WebElement blog = wait.until(ExpectedConditions.elementToBeClickable(blogLink));
        blog.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("blog.jsfiddle.net"));
        assertTrue(driver.getCurrentUrl().contains("blog.jsfiddle.net"), "Should navigate to blog.jsfiddle.net after clicking Blog");
    }

    @Test
    @Order(6)
    public void testNavigationToPricingPage() {
        driver.get(BASE_URL);
        By pricingLink = By.linkText("Pricing");
        WebElement pricing = wait.until(ExpectedConditions.elementToBeClickable(pricingLink));
        pricing.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/pricing/"));
        assertTrue(driver.getCurrentUrl().contains("/pricing/"), "Should navigate to /pricing/ path after clicking Pricing");
    }

    @Test
    @Order(7)
    public void testNavigationToAboutPage() {
        driver.get(BASE_URL);
        By aboutLink = By.linkText("About");
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        about.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/about/"));
        assertTrue(driver.getCurrentUrl().contains("/about/"), "Should navigate to /about/ path after clicking About");
    }

    @Test
    @Order(8)
    public void testNavigationToPrivacyPolicyPage() {
        driver.get(BASE_URL);
        By privacyLink = By.linkText("Privacy Policy");
        WebElement privacy = wait.until(ExpectedConditions.elementToBeClickable(privacyLink));
        privacy.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/privacy/"));
        assertTrue(driver.getCurrentUrl().contains("/privacy/"), "Should navigate to /privacy/ path after clicking Privacy Policy");
    }

    @Test
    @Order(9)
    public void testNavigationToTermsOfServicePage() {
        driver.get(BASE_URL);
        By termsLink = By.linkText("Terms of Service");
        WebElement terms = wait.until(ExpectedConditions.elementToBeClickable(termsLink));
        terms.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/terms/"));
        assertTrue(driver.getCurrentUrl().contains("/terms/"), "Should navigate to /terms/ path after clicking Terms of Service");
    }

    @Test
    @Order(10)
    public void testNavigationToStatusPage() {
        driver.get(BASE_URL);
        By statusLink = By.linkText("Status");
        WebElement status = wait.until(ExpectedConditions.elementToBeClickable(statusLink));
        status.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("status.jsfiddle.com"));
        assertTrue(driver.getCurrentUrl().contains("status.jsfiddle.com"), "Should navigate to status.jsfiddle.com after clicking Status");
    }

    @Test
    @Order(11)
    public void testNavigationToTwitterLink() {
        driver.get(BASE_URL);
        By twitterLink = By.cssSelector("a[href='https://twitter.com/js_fiddle']");
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        String originalWindow = driver.getWindowHandle();
        twitter.click();

        wait.until(webDriver -> webDriver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "New tab should be Twitter domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(12)
    public void testNavigationToGitHubLink() {
        driver.get(BASE_URL);
        By githubLink = By.cssSelector("a[href='https://github.com/jsfiddle/jsfiddle']");
        WebElement github = wait.until(ExpectedConditions.elementToBeClickable(githubLink));
        String originalWindow = driver.getWindowHandle();
        github.click();

        wait.until(webDriver -> webDriver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("github.com"), "New tab should be GitHub domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(13)
    public void testNavigationToStackOverflowLink() {
        driver.get(BASE_URL);
        By stackOverflowLink = By.cssSelector("a[href='https://stackoverflow.com/questions/tagged/jsfiddle']");
        WebElement stackOverflow = wait.until(ExpectedConditions.elementToBeClickable(stackOverflowLink));
        String originalWindow = driver.getWindowHandle();
        stackOverflow.click();

        wait.until(webDriver -> webDriver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("stackoverflow.com"), "New tab should be Stack Overflow domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(14)
    public void testNavigationToLinkedInLink() {
        driver.get(BASE_URL);
        By linkedInLink = By.cssSelector("a[href='https://www.linkedin.com/company/jsfiddle']");
        WebElement linkedIn = wait.until(ExpectedConditions.elementToBeClickable(linkedInLink));
        String originalWindow = driver.getWindowHandle();
        linkedIn.click();

        wait.until(webDriver -> webDriver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "New tab should be LinkedIn domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(15)
    public void testNavigationToYouTubeLink() {
        driver.get(BASE_URL);
        By youtubeLink = By.cssSelector("a[href='https://www.youtube.com/user/JSFiddleNet']");
        WebElement youtube = wait.until(ExpectedConditions.elementToBeClickable(youtubeLink));
        String originalWindow = driver.getWindowHandle();
        youtube.click();

        wait.until(webDriver -> webDriver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("youtube.com"), "New tab should be YouTube domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(16)
    public void testNavigationToRSSFeedLink() {
        driver.get(BASE_URL);
        By rssLink = By.cssSelector("a[href='https://feeds.feedburner.com/JsFiddleBlog']");
        WebElement rss = wait.until(ExpectedConditions.elementToBeClickable(rssLink));
        String originalWindow = driver.getWindowHandle();
        rss.click();

        wait.until(webDriver -> webDriver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("feedburner.com"), "New tab should be FeedBurner domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(17)
    public void testTryItNowButtonNavigation() {
        driver.get(BASE_URL);
        By tryItNowButton = By.cssSelector("a.btn-primary[href='/']");
        WebElement tryItNow = wait.until(ExpectedConditions.elementToBeClickable(tryItNowButton));
        tryItNow.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/"));
        assertTrue(driver.getCurrentUrl().endsWith("/") || driver.getCurrentUrl().contains("/#!"), "Should navigate to main fiddle editor");
    }

    @Test
    @Order(18)
    public void testLoginButtonOpensLoginForm() {
        driver.get(BASE_URL);
        By loginButton = By.cssSelector("a[href='/login']");
        WebElement login = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        login.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"), "Should navigate to /login path after clicking Login");
    }

    @Test
    @Order(19)
    public void testSignUpButtonNavigation() {
        driver.get(BASE_URL);
        By signUpButton = By.cssSelector("a[href='/signup']");
        WebElement signUp = wait.until(ExpectedConditions.elementToBeClickable(signUpButton));
        signUp.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/signup"));
        assertTrue(driver.getCurrentUrl().contains("/signup"), "Should navigate to /signup path after clicking Sign Up");
    }

    @Test
    @Order(20)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        By searchInput = By.name("q");
        By searchButton = By.cssSelector("input[type='submit'][value='Search']");

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        input.clear();
        input.sendKeys("javascript");

        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(searchButton));
        button.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/search/"));
        assertTrue(driver.getCurrentUrl().contains("/search/"), "Should navigate to /search/ path after search");
    }

    @Test
    @Order(21)
    public void testNavigationToJavaScriptGuide() {
        driver.get(BASE_URL);
        By jsGuideLink = By.linkText("JavaScript Guide");
        WebElement jsGuide = wait.until(ExpectedConditions.elementToBeClickable(jsGuideLink));
        jsGuide.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/docs/guides/javascript/"));
        assertTrue(driver.getCurrentUrl().contains("/docs/guides/javascript/"), "Should navigate to JavaScript Guide");
    }

    @Test
    @Order(22)
    public void testNavigationToHTMLGuide() {
        driver.get(BASE_URL);
        By htmlGuideLink = By.linkText("HTML Guide");
        WebElement htmlGuide = wait.until(ExpectedConditions.elementToBeClickable(htmlGuideLink));
        htmlGuide.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/docs/guides/html/"));
        assertTrue(driver.getCurrentUrl().contains("/docs/guides/html/"), "Should navigate to HTML Guide");
    }

    @Test
    @Order(23)
    public void testNavigationToCSSGuide() {
        driver.get(BASE_URL);
        By cssGuideLink = By.linkText("CSS Guide");
        WebElement cssGuide = wait.until(ExpectedConditions.elementToBeClickable(cssGuideLink));
        cssGuide.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/docs/guides/css/"));
        assertTrue(driver.getCurrentUrl().contains("/docs/guides/css/"), "Should navigate to CSS Guide");
    }

    @Test
    @Order(24)
    public void testNavigationToAPIReference() {
        driver.get(BASE_URL);
        By apiLink = By.linkText("API Reference");
        WebElement api = wait.until(ExpectedConditions.elementToBeClickable(apiLink));
        api.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/docs/api/"));
        assertTrue(driver.getCurrentUrl().contains("/docs/api/"), "Should navigate to API Reference");
    }

    @Test
    @Order(25)
    public void testNavigationToEmbeddingGuide() {
        driver.get(BASE_URL);
        By embeddingLink = By.linkText("Embedding Guide");
        WebElement embedding = wait.until(ExpectedConditions.elementToBeClickable(embeddingLink));
        embedding.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/docs/guides/embedding/"));
        assertTrue(driver.getCurrentUrl().contains("/docs/guides/embedding/"), "Should navigate to Embedding Guide");
    }

    @Test
    @Order(26)
    public void testNavigationToKeyboardShortcuts() {
        driver.get(BASE_URL);
        By shortcutsLink = By.linkText("Keyboard Shortcuts");
        WebElement shortcuts = wait.until(ExpectedConditions.elementToBeClickable(shortcutsLink));
        shortcuts.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/docs/guides/keyboard-shortcuts/"));
        assertTrue(driver.getCurrentUrl().contains("/docs/guides/keyboard-shortcuts/"), "Should navigate to Keyboard Shortcuts");
    }

    @Test
    @Order(27)
    public void testNavigationToChangelog() {
        driver.get(BASE_URL);
        By changelogLink = By.linkText("Changelog");
        WebElement changelog = wait.until(ExpectedConditions.elementToBeClickable(changelogLink));
        changelog.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/docs/changelog/"));
        assertTrue(driver.getCurrentUrl().contains("/docs/changelog/"), "Should navigate to Changelog");
    }

    @Test
    @Order(28)
    public void testNavigationToFAQ() {
        driver.get(BASE_URL);
        By faqLink = By.linkText("FAQ");
        WebElement faq = wait.until(ExpectedConditions.elementToBeClickable(faqLink));
        faq.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/docs/faq/"));
        assertTrue(driver.getCurrentUrl().contains("/docs/faq/"), "Should navigate to FAQ");
    }

    @Test
    @Order(29)
    public void testNavigationToContactPage() {
        driver.get(BASE_URL);
        By contactLink = By.linkText("Contact");
        WebElement contact = wait.until(ExpectedConditions.elementToBeClickable(contactLink));
        contact.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/support/contact/"));
        assertTrue(driver.getCurrentUrl().contains("/support/contact/"), "Should navigate to Contact page");
    }

    @Test
    @Order(30)
    public void testNavigationToReportABugPage() {
        driver.get(BASE_URL);
        By bugLink = By.linkText("Report a bug");
        WebElement bug = wait.until(ExpectedConditions.elementToBeClickable(bugLink));
        bug.click();

        wait.until(webDriver -> webDriver.getCurrentUrl().contains("/support/bug/"));
        assertTrue(driver.getCurrentUrl().contains("/support/bug/"), "Should navigate to Report a Bug page");
    }
}