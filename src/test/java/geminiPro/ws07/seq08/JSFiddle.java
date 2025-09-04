package geminiPRO.ws07.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsFiddleTest {

    private static final String BASE_URL = "https://jsfiddle.net/";
    // JSFiddle can be slow to load all its frames and editors, so a longer timeout is safer.
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private final By runButton = By.cssSelector("a[title='Run code']");
    private final By resultFrame = By.name("result");
    private final By htmlFrame = By.name("html");
    private final By cssFrame = By.name("css");
    private final By jsFrame = By.name("js");
    private final By codeMirrorEditor = By.className("CodeMirror-code");
    private final By settingsButton = By.cssSelector("a[title='Settings']");
    private final By layoutTabsRadio = By.cssSelector("label[for='layout-tabs']");
    private final By docsLinkFooter = By.linkText("Docs");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        // JSFiddle may show a GDPR consent banner that can interfere with clicks.
        // This preference attempts to mitigate it for Firefox.
        options.addPreference("geo.provider.testing", true);
        options.addPreference("geo.prompt.testing", true);
        options.addPreference("geo.prompt.testing.allow", false);
        
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        driver.get(BASE_URL);
        // Sometimes a "welcome" or "tips" modal can appear. We try to close it if it's present.
        try {
            wait.withTimeout(Duration.ofSeconds(3));
            WebElement gotItButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("got-it")));
            gotItButton.click();
        } catch (TimeoutException e) {
            // Modal did not appear, which is fine. Continue with the test.
        } finally {
            wait.withTimeout(TIMEOUT); // Reset wait timeout to the default
        }
    }

    /**
     * Helper method to enter text into a CodeMirror editor within an iframe.
     * It handles switching to the frame, clearing existing content, and typing new content.
     */
    private void enterCode(By frameLocator, String code) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
        WebElement editor = wait.until(ExpectedConditions.visibilityOfElementLocated(codeMirrorEditor));
        
        // CodeMirror can be tricky. A robust way to clear it is to select all and delete.
        new Actions(driver)
            .click(editor) // Focus the editor
            .keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL) // Select all
            .sendKeys(Keys.DELETE)
            .sendKeys(code)
            .perform();

        driver.switchTo().defaultContent();
    }
    
    @Test
    @Order(1)
    @DisplayName("Verify Home Page Initial Layout and Title")
    void testHomePageInitialLayout() {
        assertEquals("JSFiddle - Code Playground", driver.getTitle(), "Page title should be correct.");
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(runButton)).isDisplayed(), "Run button should be visible.");
        assertTrue(wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(htmlFrame)).findElement(By.tagName("body")).isDisplayed(), "HTML editor frame should be present.");
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(2)
    @DisplayName("Create and Run a Simple Fiddle")
    void testCreateAndRunFiddle() {
        String htmlCode = "<h1>Hello, World!</h1>";
        String cssCode = "h1 { color: rgb(0, 0, 255); font-family: sans-serif; }";
        String jsCode = "document.querySelector('h1').textContent = 'Hello, Gemini!';";
        
        // Enter code into each panel
        enterCode(htmlFrame, htmlCode);
        enterCode(cssFrame, cssCode);
        enterCode(jsFrame, jsCode);

        // Run the code
        wait.until(ExpectedConditions.elementToBeClickable(runButton)).click();
        
        // Switch to the result frame and verify the output
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(resultFrame));
        
        WebElement resultHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        
        assertAll("Verify the output of the executed fiddle",
            () -> assertEquals("Hello, Gemini!", resultHeader.getText(), "JavaScript should have changed the H1 text."),
            () -> assertEquals("rgb(0, 0, 255)", resultHeader.getCssValue("color"), "CSS should have made the H1 text blue.")
        );
        
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(3)
    @DisplayName("Change Editor Layout to Tabs")
    void testLayoutChange() {
        wait.until(ExpectedConditions.elementToBeClickable(settingsButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(layoutTabsRadio)).click();
        
        // In tabbed layout, the classic grid disappears.
        By classicLayout = By.id("content");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(classicLayout));
        
        // And the new tabbed elements appear.
        By tabsContainer = By.className("tabs");
        assertTrue(driver.findElement(tabsContainer).isDisplayed(), "Tabbed layout container should now be visible.");
    }
    
    @Test
    @Order(4)
    @DisplayName("Verify Footer Docs Link Opens in a New Tab")
    void testFooterDocsLink() {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(docsLinkFooter));
        
        // Scroll to the element to ensure it's not obscured
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        String newWindow = driver.getWindowHandles().stream()
                               .filter(handle -> !handle.equals(originalWindow))
                               .findFirst()
                               .orElseThrow(() -> new RuntimeException("New window did not open"));

        driver.switchTo().window(newWindow);
        
        assertTrue(wait.until(ExpectedConditions.urlContains("docs.jsfiddle.net")), "URL of the new tab should be for JSFiddle Docs.");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        assertEquals("JSFiddle - Code Playground", driver.getTitle(), "Should have switched back to the main page.");
    }
}