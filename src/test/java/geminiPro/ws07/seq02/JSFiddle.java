package geminiPRO.ws07.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * A complete JUnit 5 test suite for the JSFiddle online code editor.
 * This test uses Selenium WebDriver with Firefox running in headless mode.
 * It handles the complexities of iframes for code input and output rendering.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddleTest {

    // Constants for configuration
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // WebDriver and WebDriverWait instances shared across all tests
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- WebDriver Lifecycle ---

    @BeforeAll
    static void setup() {
        // As per requirements, initialize Firefox in headless mode via arguments
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080)); // A larger window is good for complex layouts
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHomeAndPrepare() {
        driver.get(BASE_URL);
        // Handle the cookie consent banner that may appear on first load
        handleCookieBanner();
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    @DisplayName("Should load the page successfully and verify the main UI elements")
    void testPageLoadsSuccessfully() {
        Assertions.assertEquals("JSFiddle - Code Playground", driver.getTitle(), "Page title is incorrect.");
        WebElement runButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("run")));
        Assertions.assertTrue(runButton.isDisplayed(), "The 'Run' button should be visible on page load.");
    }

    @Test
    @Order(2)
    @DisplayName("Should execute simple HTML and verify the result")
    void testHtmlExecution() {
        String htmlCode = "<h1>Hello, Gemini!</h1>";
        String expectedText = "Hello, Gemini!";

        // Enter code and run
        enterCodeIntoPane("html", htmlCode);
        clickRun();

        // Switch to result iframe and assert
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("result")));
        WebElement resultHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals(expectedText, resultHeader.getText(), "The H1 tag in the result pane has incorrect text.");

        // Switch back to the main context
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(3)
    @DisplayName("Should apply CSS and verify the style of an element")
    void testCssStylingExecution() {
        String htmlCode = "<p>This text should be blue.</p>";
        String cssCode = "p { color: blue; }";
        String expectedColor = "rgba(0, 0, 255, 1)"; // "blue" is typically represented as this RGBA value

        // Enter code and run
        enterCodeIntoPane("html", htmlCode);
        enterCodeIntoPane("css", cssCode);
        clickRun();

        // Switch to result iframe and assert style
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("result")));
        WebElement paragraph = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("p")));
        String actualColor = paragraph.getCssValue("color");
        Assertions.assertEquals(expectedColor, actualColor, "The paragraph text color is not blue.");

        driver.switchTo().defaultContent();
    }

    @Test
    @Order(4)
    @DisplayName("Should execute JavaScript and verify DOM manipulation")
    void testJavaScriptExecution() {
        String htmlCode = "<div id=\"test-div\">Initial text</div>";
        String jsCode = "document.getElementById('test-div').textContent = 'JS was here!';";
        String expectedText = "JS was here!";

        // Enter code and run
        enterCodeIntoPane("html", htmlCode);
        enterCodeIntoPane("javascript", jsCode);
        clickRun();

        // Switch to result iframe and assert content change
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("result")));
        WebElement testDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("test-div")));
        Assertions.assertEquals(expectedText, testDiv.getText(), "The div content was not updated by JavaScript.");

        driver.switchTo().defaultContent();
    }

    @Test
    @Order(5)
    @DisplayName("Should open the sign-in modal and show an error for invalid login")
    void testSignInModalAndInvalidLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();
        WebElement loginModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-box")));
        Assertions.assertTrue(loginModal.isDisplayed(), "Login modal should be visible after clicking 'Sign in'.");

        // Attempt invalid login
        driver.findElement(By.id("id_username")).sendKeys("invalid-user");
        driver.findElement(By.id("id_password")).sendKeys("invalid-password");
        driver.findElement(By.cssSelector("#login-box button[type='submit']")).click();

        // Assert error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#login-box .error")));
        Assertions.assertTrue(errorMessage.getText().contains("Please enter a correct username and password."), "Error message for invalid credentials was not shown.");

        // Close the modal
        driver.findElement(By.cssSelector("#login-box .close")).click();
        wait.until(ExpectedConditions.invisibilityOf(loginModal));
        Assertions.assertFalse(loginModal.isDisplayed(), "Login modal should be hidden after closing.");
    }

    @Test
    @Order(6)
    @DisplayName("Should open the 'Docs' link in a new tab")
    void testFooterDocsLink() {
        String originalWindow = driver.getWindowHandle();
        WebElement docsLink = driver.findElement(By.linkText("Docs"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", docsLink);
        wait.until(ExpectedConditions.elementToBeClickable(docsLink)).click();

        // Wait for and switch to the new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert content on the new Docs page
        wait.until(ExpectedConditions.urlContains("docs.jsfiddle.net"));
        Assertions.assertTrue(driver.getTitle().contains("JSFiddle"), "Docs page title is incorrect.");

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);

        // Assert we are back on the main page
        Assertions.assertEquals("JSFiddle - Code Playground", driver.getTitle(), "Should be back on the main JSFiddle page.");
    }

    // --- Helper Methods ---

    /**
     * Handles the cookie consent banner if it appears.
     */
    private void handleCookieBanner() {
        try {
            WebElement cookieOkButton = wait.withTimeout(Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(By.id("cookies-ok")));
            cookieOkButton.click();
        } catch (TimeoutException e) {
            // Banner did not appear, which is fine.
        }
    }

    /**
     * Clicks the main 'Run' button.
     */
    private void clickRun() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("run"))).click();
    }



    /**
     * Enters a given string of code into the specified editor pane (html, css, or javascript).
     * This method handles switching to the correct iframe for the editor.
     * @param paneType The type of pane ("html", "css", or "javascript").
     * @param code The code to enter.
     */
    private void enterCodeIntoPane(String paneType, String code) {
        // The editor is inside a nested iframe structure. First switch to the panel's direct iframe.
        String frameId = "editor-" + paneType;
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id(frameId)));

        // Inside this, the CodeMirror editor has its own iframe.
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(0));

        // Now find the active line element and send keys.
        // Sending keys to the hidden textarea is often more reliable.
        WebElement codeMirrorTextarea = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("textarea")));
        
        // Use Javascript to set the value, which is faster and more reliable for complex editors.
        ((JavascriptExecutor)driver).executeScript("arguments[0].value = arguments[1];", codeMirrorTextarea, code);
        
        // This Javascript triggers the editor to update its display based on the textarea's new value
        ((JavascriptExecutor)driver).executeScript("window.editor.setValue(arguments[0])", code);


        driver.switchTo().defaultContent();
    }
}