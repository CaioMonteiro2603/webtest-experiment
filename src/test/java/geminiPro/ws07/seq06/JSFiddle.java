package geminiPro.ws07.seq06;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 test suite for the JSFiddle online code editor.
 * This suite uses Selenium WebDriver with headless Firefox to test the core functionality,
 * such as code execution within an iframe, menu navigation, and external links.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15); // Increased for this complex app

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators for dynamic elements
    private static final By HTML_EDITOR_PANE = By.xpath("//div[@id='panel_html']//div[contains(@class,'CodeMirror-scroll')]");
    private static final By JS_EDITOR_PANE = By.xpath("//div[@id='panel_js']//div[contains(@class,'CodeMirror-scroll')]");
    private static final By RUN_BUTTON = By.id("run");
    private static final By RESULT_IFRAME = By.name("result");

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use headless mode via arguments ONLY
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize(); // Headless can still benefit from a larger viewport
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Verifies the initial layout, handles the cookie consent, and ensures editor panels are visible.
     */
    @Test
    @Order(1)
    void initialLayoutAndCookieConsentTest() {
        driver.get(BASE_URL);
        assertEquals("JSFiddle - Code Playground", driver.getTitle(), "Page title is incorrect.");

        // Handle the cookie consent banner to prevent it from obscuring other elements
        WebElement cookieConsentButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("cookie-notice-ok")));
        cookieConsentButton.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("cookie-notice")));

        // Verify main components are visible
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(HTML_EDITOR_PANE)).isDisplayed(), "HTML editor pane is not visible.");
        assertTrue(driver.findElement(JS_EDITOR_PANE).isDisplayed(), "JavaScript editor pane is not visible.");
        assertTrue(driver.findElement(RUN_BUTTON).isDisplayed(), "Run button is not visible.");
    }

    /**
     * Tests the core functionality: entering HTML and JS, running the code,
     * and verifying the output inside the result iframe.
     */
    @Test
    @Order(2)
    void helloWorldExecutionTest() {
        driver.get(BASE_URL);
        // Wait for editors to be ready and clear any default text
        WebElement htmlEditor = wait.until(ExpectedConditions.visibilityOfElementLocated(HTML_EDITOR_PANE));
        WebElement jsEditor = driver.findElement(JS_EDITOR_PANE);

        // Enter code into the panels. We send keys to the scrollable div of the editor.
        htmlEditor.click();
        htmlEditor.findElement(By.cssSelector("textarea")).sendKeys("<div id='greeting'></div>");

        jsEditor.click();
        jsEditor.findElement(By.cssSelector("textarea")).sendKeys("document.getElementById('greeting').innerText = 'Hello, World!';");

        // Run the code
        wait.until(ExpectedConditions.elementToBeClickable(RUN_BUTTON)).click();

        // Switch to the result iframe and verify the output
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(RESULT_IFRAME));
        WebElement greetingDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("greeting")));
        assertEquals("Hello, World!", greetingDiv.getText(), "The JavaScript code did not execute correctly.");

        // Switch back to the main document context
        driver.switchTo().defaultContent();
    }

    /**
     * Tests opening a settings menu and interacting with a dropdown to select a framework.
     */
    @Test
    @Order(3)
    void settingsMenuAndFrameworkSelectionTest() {
        driver.get(BASE_URL);
        // Open JavaScript settings
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#panel_js a[title='JavaScript Settings']"))).click();

        // Wait for the dropdown and select jQuery
        WebElement frameworkDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("js-framework")));
        // We interact with the visible select-like element first
        frameworkDropdown.click();
        // Then we click the actual option from the list that appears
        WebElement jqueryOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[contains(text(), 'jQuery 3.6.0')]")));
        jqueryOption.click();

        // Verify the selection is displayed
        WebElement selectedFramework = driver.findElement(By.xpath("//div[@id='js-framework']//span[contains(text(), 'jQuery 3.6.0')]"));
        assertTrue(selectedFramework.isDisplayed(), "jQuery was not successfully selected in the settings.");
    }

    /**
     * Tests the "New Fiddle" functionality to ensure it resets the application state.
     */
    @Test
    @Order(4)
    void newFiddleStateResetTest() {
        driver.get(BASE_URL);
        // Add some text to an editor to create a state to be reset
        WebElement htmlEditorTextarea = wait.until(ExpectedConditions.visibilityOfElementLocated(HTML_EDITOR_PANE))
                                             .findElement(By.cssSelector("textarea"));
        htmlEditorTextarea.sendKeys("Some text to be cleared.");
        
        // Open the main menu and click "New Fiddle"
        wait.until(ExpectedConditions.elementToBeClickable(By.id("show-libs"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("New Fiddle"))).click();

        // Verify the editor is now empty
        // Re-find the element after navigation
        WebElement newHtmlEditorTextarea = wait.until(ExpectedConditions.visibilityOfElementLocated(HTML_EDITOR_PANE))
                                                .findElement(By.cssSelector("textarea"));
        // After reset, the value attribute of the textarea should be empty
        wait.until(d -> newHtmlEditorTextarea.getAttribute("value").isEmpty());
        assertEquals("", newHtmlEditorTextarea.getAttribute("value"), "New Fiddle did not clear the HTML editor.");
    }

    /**
     * Verifies that the external links in the footer open correctly in new tabs.
     */
    @Test
    @Order(5)
    void externalFooterLinksTest() {
        driver.get(BASE_URL);
        // Twitter, GitHub, and Dribbble are the main external links
        verifyExternalLink(By.linkText("Twitter"), "twitter.com");
        verifyExternalLink(By.linkText("GitHub"), "github.com");
        verifyExternalLink(By.linkText("Dribbble"), "dribbble.com");
    }

    // --- Helper Methods ---

    /**
     * Clicks a link, switches to the new tab, verifies the URL, closes the tab, and switches back.
     *
     * @param locator        The locator for the link element.
     * @param expectedDomain The domain expected in the new tab's URL.
     */
    private void verifyExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();

        // Wait for the new window or tab to open
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // Switch to the new window
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Verify the URL of the new tab
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL of the new tab should contain " + expectedDomain);

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals(1, driver.getWindowHandles().size(), "Should have switched back and have only one window open.");
    }
}