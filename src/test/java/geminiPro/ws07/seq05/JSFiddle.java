package geminiPRO.ws07.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Set;

/**
 * JUnit 5 test suite for the JSFiddle online code editor.
 * This suite uses Selenium WebDriver with headless Firefox to test the core functionality,
 * such as running code snippets and verifying the output within an iframe, as well as
 * testing UI elements and external links.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddleE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static Actions actions;
    private static JavascriptExecutor js;

    private static final String BASE_URL = "https://jsfiddle.net/";

    // --- Locators ---
    private final By runButton = By.cssSelector("a#run");
    private final By resultIframe = By.name("result");
    private final By signInButton = By.id("login");
    private final By loginModal = By.id("login-modal");
    private final By loginEmailInput = By.id("id_login");

    // Locators for the CodeMirror editor panes
    private final By htmlEditor = By.xpath("//div[@id='panel-html']//div[contains(@class, 'CodeMirror-code')]");
    private final By cssEditor = By.xpath("//div[@id='panel-css']//div[contains(@class, 'CodeMirror-code')]");
    private final By jsEditor = By.xpath("//div[@id='panel-javascript']//div[contains(@class, 'CodeMirror-code')]");


    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        // JSFiddle can be heavy, a larger window size can prevent layout issues in headless
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // Increased wait for this complex SPA
        actions = new Actions(driver);
        js = (JavascriptExecutor) driver;
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setup() {
        driver.get(BASE_URL);
        // JSFiddle may show a cookie consent banner that can interfere with clicks
        acceptCookieConsent();
    }

    /**
     * Helper to click the cookie consent button if it appears.
     */
    private void acceptCookieConsent() {
        try {
            WebElement cookieButton = wait.withTimeout(Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'AGREE')]")));
            cookieButton.click();
        } catch (Exception e) {
            // If the banner is not present, we can continue.
        }
    }
    
    /**
     * Helper method to enter text into a CodeMirror editor instance.
     * Clicks the editor to focus it, then uses Actions to send keys.
     * @param editorLocator The locator for the CodeMirror code area.
     * @param code The string of code to enter.
     */
    private void enterCodeIntoEditor(By editorLocator, String code) {
        WebElement editor = wait.until(ExpectedConditions.visibilityOfElementLocated(editorLocator));
        actions.click(editor).sendKeys(code).perform();
    }

    @Test
    @Order(1)
    @DisplayName("Should load the page and display the correct title and editor layout")
    void testPageTitleAndDefaultLayout() {
        String expectedTitle = "JSFiddle - Code Playground";
        wait.until(ExpectedConditions.titleIs(expectedTitle));
        Assertions.assertEquals(expectedTitle, driver.getTitle(), "Page title is incorrect.");

        Assertions.assertAll("Verify editor panels are visible",
            () -> Assertions.assertTrue(driver.findElement(htmlEditor).isDisplayed(), "HTML editor panel is not visible."),
            () -> Assertions.assertTrue(driver.findElement(cssEditor).isDisplayed(), "CSS editor panel is not visible."),
            () -> Assertions.assertTrue(driver.findElement(jsEditor).isDisplayed(), "JavaScript editor panel is not visible."),
            () -> Assertions.assertTrue(driver.findElement(runButton).isDisplayed(), "Run button is not visible.")
        );
    }
    
    @Test
    @Order(2)
    @DisplayName("Should run HTML, CSS, and JS and verify the output in the result iframe")
    void testRunSimpleFiddleAndVerifyOutput() {
        String htmlCode = "<div id=\"test-div\">Initial Text</div>";
        String cssCode = "#test-div { color: rgb(255, 0, 0); font-size: 24px; }";
        String jsCode = "document.getElementById('test-div').textContent = 'Updated by JS';";

        // Enter code into the editors
        enterCodeIntoEditor(htmlEditor, htmlCode);
        enterCodeIntoEditor(cssEditor, cssCode);
        enterCodeIntoEditor(jsEditor, jsCode);

        // Run the fiddle
        wait.until(ExpectedConditions.elementToBeClickable(runButton)).click();
        
        // Wait for the result iframe and switch to it
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(resultIframe));
        
        // --- Assertions within the iframe ---
        WebElement resultDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("test-div")));
        
        Assertions.assertEquals("Updated by JS", resultDiv.getText(), "The text content was not updated by JavaScript.");
        Assertions.assertEquals("rgb(255, 0, 0)", resultDiv.getCssValue("color"), "The CSS color was not applied correctly.");
        Assertions.assertEquals("24px", resultDiv.getCssValue("font-size"), "The CSS font-size was not applied correctly.");

        // --- Switch back to the main document ---
        driver.switchTo().defaultContent();
        
        // Verify we are back in the main document context
        Assertions.assertTrue(driver.findElement(runButton).isDisplayed(), "Could not find the Run button after switching back from iframe.");
    }
    
    @Test
    @Order(3)
    @DisplayName("Should display the sign-in modal when the 'Sign in' button is clicked")
    void testLoginModalAppears() {
        wait.until(ExpectedConditions.elementToBeClickable(signInButton)).click();
        
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(loginModal));
        Assertions.assertTrue(modal.isDisplayed(), "Login modal should be visible.");
        
        Assertions.assertTrue(
            modal.findElement(loginEmailInput).isDisplayed(), 
            "Login email/username input is not visible in the modal."
        );
    }
    
    @ParameterizedTest(name = "External Link Test: {1}")
    @CsvSource({
        "a[href*='github.com/jsfiddle'], github.com",
        "a[href*='twitter.com/jsfiddle'], twitter.com",
        "a[href*='docs.jsfiddle.net'], docs.jsfiddle.net"
    })
    @Order(4)
    @DisplayName("Should open external footer links in a new tab")
    void testExternalFooterLinks(String selector, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
        // Scroll to the link to ensure it's clickable in headless mode
        js.executeScript("arguments[0].scrollIntoView(true);", link);
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = new ArrayList<>(allWindows).get(0);
        
        driver.switchTo().window(newWindow);
        
        // Wait for the new page's URL to stabilize and contain the expected domain
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(
            driver.getCurrentUrl().contains(expectedDomain), 
            "The new window URL should contain '" + expectedDomain + "'."
        );
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        Assertions.assertEquals(1, driver.getWindowHandles().size(), "Should have returned to a single window.");
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Should be back on the JSFiddle homepage.");
    }
}