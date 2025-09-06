package geminiPRO.ws07.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * A comprehensive JUnit 5 test suite for the JSFiddle online IDE.
 * This suite uses Selenium WebDriver with Firefox in headless mode to test the core
 * functionality of creating and running a fiddle, as well as verifying UI interactions and external links.
 * A key feature of this test is its handling of multiple iframes for code input and results.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsFiddleTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    // --- Locators ---
    private static final By RUN_BUTTON = By.cssSelector("a[href='/_display/']");
    private static final By COOKIE_ACCEPT_BUTTON = By.xpath("//a[normalize-space()='AGREE']");
    
    // Locators for iframe elements
    private static final By HTML_IFRAME = By.cssSelector("div.window.html > .CodeMirror > div > iframe");
    private static final By CSS_IFRAME = By.cssSelector("div.window.css > .CodeMirror > div > iframe");
    private static final By JS_IFRAME = By.cssSelector("div.window.js > .CodeMirror > div > iframe");
    private static final By RESULT_IFRAME = By.name("result");

    // Locator for the text area inside the CodeMirror iframe
    private static final By CODE_MIRROR_TEXTAREA = By.className("CodeMirror-line");

    // Locators for external links
    private static final By DOCS_LINK = By.linkText("Docs");
    private static final By TWITTER_LINK = By.linkText("Twitter");

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments as strictly required
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void loadPageAndHandleCookies() {
        driver.get(BASE_URL);
        // Handle the cookie consent banner which can obscure other elements
        try {
            wait.withTimeout(Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(COOKIE_ACCEPT_BUTTON)).click();
        } catch (TimeoutException e) {
            // Cookie banner did not appear, which is fine. Continue test.
        }
    }

    /**
     * Helper method to inject code into a CodeMirror editor instance within an iframe.
     * Standard sendKeys is unreliable with CodeMirror, so using JavascriptExecutor is more robust.
     * @param iframeLocator The locator for the iframe containing the editor.
     * @param code The string of code to inject into the editor.
     */
    private void setCodeMirrorValue(By iframeLocator, String code) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeLocator));
        WebElement codeMirror = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("CodeMirror")));
        // Use JavascriptExecutor to set the value
        ((JavascriptExecutor) driver).executeScript("arguments[0].CodeMirror.setValue(arguments[1]);", codeMirror, code);
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(1)
    @DisplayName("🧪 Test Core Functionality: Create and Run a Fiddle")
    void testCreateAndRunFiddle() {
        // --- Code Snippets ---
        String htmlCode = "<h1>Hello World!</h1>";
        String cssCode = "h1 { color: rgb(0, 0, 255); font-family: sans-serif; }"; // Blue color
        String jsCode = "document.querySelector('h1').textContent += ' from JavaScript!';";

        // --- Inject Code into Editors ---
        setCodeMirrorValue(HTML_IFRAME, htmlCode);
        setCodeMirrorValue(CSS_IFRAME, cssCode);
        setCodeMirrorValue(JS_IFRAME, jsCode);

        // --- Run the Fiddle ---
        wait.until(ExpectedConditions.elementToBeClickable(RUN_BUTTON)).click();

        // --- Verify the Result in the Result IFrame ---
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(RESULT_IFRAME));
        
        WebElement resultHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));

        // Assertions
        Assertions.assertEquals("Hello World! from JavaScript!", resultHeader.getText(), "The H1 text content is incorrect.");
        Assertions.assertEquals("rgb(0, 0, 255)", resultHeader.getCssValue("color"), "The H1 color style is not applied correctly.");
        
        // Return to the main page context
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(2)
    @DisplayName("🧪 Test External Links Navigation")
    void testExternalLinks() {
        // Test Docs link
        testExternalLink(DOCS_LINK, "docs.jsfiddle.net");
        // Test Twitter link
        testExternalLink(TWITTER_LINK, "twitter.com");
    }

    /**
     * Helper method to test an external link that opens in a new tab.
     * @param locator The locator of the link to click.
     * @param expectedDomain The domain expected in the new tab's URL.
     */
    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        // Scroll to element to avoid interception issues in headless mode
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
        link.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL of the new tab should contain " + expectedDomain);
        
        driver.close();
        driver.switchTo().window(originalWindow);

        // Verify we are back on the main page
        Assertions.assertEquals(1, driver.getWindowHandles().size(), "Should be back to one window.");
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should have returned to the JSFiddle site.");
    }
    
    @Test
    @Order(3)
    @DisplayName("🧪 Test UI Save Action Shows Login Prompt for Anonymous User")
    void testAnonymousSavePrompt() {
        // Click the "Save" button in the top bar
        wait.until(ExpectedConditions.elementToBeClickable(By.id("save"))).click();
        
        // An anonymous user should be prompted to sign in. The URL changes to include /login/
        wait.until(ExpectedConditions.urlContains("/login/"));
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login/"), "Anonymous save should redirect to a login/signup page.");
        
        // Verify a key element on the login page
        WebElement loginHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Sign in to your account')]")));
        Assertions.assertTrue(loginHeader.isDisplayed(), "Login page header should be visible.");
    }
}