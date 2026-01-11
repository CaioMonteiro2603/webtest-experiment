package geminiPro.ws07.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for jsfiddle.net.
 * This suite covers the core functionality of the code editor, including running HTML and JavaScript,
 * as well as site navigation and handling of external links.
 * It uses Selenium WebDriver with headless Firefox.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

    private static final String BASE_URL = "https://jsfiddle.net/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private final By runButton = By.id("run");
    private final By docsLink = By.cssSelector("a[href='/docs/']");
    private final By aboutLink = By.cssSelector("a[href='/about/']");
    private final By signInButton = By.cssSelector("a[href*='signin']");
    
    private final By htmlPanel = By.cssSelector(".panel-html .CodeMirror");
    private final By jsPanel = By.cssSelector(".panel-js .CodeMirror");
    private final By resultFrame = By.name("result");
    

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED headless mode via arguments
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        // JSFiddle can be slow to load, especially in headless mode; a longer wait is prudent.
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHomeAndHandlePopups() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        // Handle potential cookie/ad consent popups that can block other elements
        try {
            // This is a common pattern for consent banners
            WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Accept all')]")));
            acceptButton.click();
        } catch (Exception e) {
            // Popup did not appear, which is fine. Continue with the test.
        }
    }

    @Test
    @Order(1)
    void testHomePageElementsAndTitle() {
        assertEquals("JSFiddle - Code Playground", driver.getTitle(), "Page title is incorrect.");
        assertTrue(driver.findElement(runButton).isDisplayed(), "'Run' button should be visible.");
        assertTrue(driver.findElement(signInButton).isDisplayed(), "'Sign in' button should be visible.");
    }

    @Test
    @Order(2)
    void testRunSimpleHtmlAndAssertOutput() {
        String htmlContent = "<h1>Hello, JSFiddle!</h1>";
        String script = "arguments[0].CodeMirror.setValue(arguments[1])";

        // Enter HTML code
        WebElement htmlEditor = wait.until(ExpectedConditions.elementToBeClickable(htmlPanel));
        ((JavascriptExecutor)driver).executeScript(script, htmlEditor, htmlContent);

        // Run the code
        driver.findElement(runButton).click();

        // Assert the output
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(resultFrame));
        WebElement resultHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Hello, JSFiddle!", resultHeader.getText(), "The HTML output in the result pane is incorrect.");
        
        driver.switchTo().defaultContent();
    }
    
    @Test
    @Order(3)
    void testRunHtmlWithJavaScriptAndAssertExecution() {
        String htmlContent = "<div id=\"test-div\">Initial text</div>";
        String jsContent = "document.getElementById('test-div').textContent = 'JavaScript was executed!';";
        String htmlScript = "arguments[0].CodeMirror.setValue(arguments[1])";
        String jsScript = "arguments[0].CodeMirror.setValue(arguments[1])";

        // Enter HTML
        WebElement htmlEditor = wait.until(ExpectedConditions.elementToBeClickable(htmlPanel));
        ((JavascriptExecutor)driver).executeScript(htmlScript, htmlEditor, htmlContent);

        // Enter JavaScript
        WebElement jsEditor = wait.until(ExpectedConditions.elementToBeClickable(jsPanel));
        ((JavascriptExecutor)driver).executeScript(jsScript, jsEditor, jsContent);

        // Run the code
        driver.findElement(runButton).click();

        // Assert the output
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(resultFrame));
        WebElement resultDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("test-div")));
        // Wait for the text to be updated by the script
        wait.until(ExpectedConditions.textToBe(By.id("test-div"), "JavaScript was executed!"));
        assertEquals("JavaScript was executed!", resultDiv.getText(), "The JavaScript did not update the HTML content as expected.");

        driver.switchTo().defaultContent();
    }
    
    @Test
    @Order(4)
    void testSidebarNavigation() {
        // Test navigation to Docs
        WebElement docsElement = wait.until(ExpectedConditions.presenceOfElementLocated(docsLink));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", docsElement);
        wait.until(ExpectedConditions.urlContains("/docs/"));
        assertEquals("Documentation - JSFiddle", driver.getTitle(), "Title of the Docs page is incorrect.");
        
        // Test navigation to About
        WebElement aboutElement = wait.until(ExpectedConditions.presenceOfElementLocated(aboutLink));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", aboutElement);
        wait.until(ExpectedConditions.urlContains("/about/"));
        assertEquals("About JSFiddle", driver.getTitle(), "Title of the About page is incorrect.");
    }
    
    @Test
    @Order(5)
    void testFooterExternalLinks() {
        // These links are in the footer and may require scrolling into view
        WebElement footer = driver.findElement(By.tagName("footer"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", footer);
        
        handleExternalLink(footer.findElement(By.cssSelector("a[href*='github.com/jsfiddle']")), "github.com");
        handleExternalLink(footer.findElement(By.cssSelector("a[href*='twitter.com/jsfiddle']")), "twitter.com");
        handleExternalLink(footer.findElement(By.cssSelector("a[href*='facebook.com/jsfiddle']")), "facebook.com");
    }

    /**
     * Handles clicking an external link, verifying the new tab's URL, closing it, and returning control.
     * @param linkElement The WebElement of the link to click.
     * @param expectedDomain The domain expected in the new tab's URL.
     */
    private void handleExternalLink(WebElement linkElement, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(linkElement)).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        
        if (newWindow == null) {
            fail("New window did not open for link with expected domain: " + expectedDomain);
        }
        
        driver.switchTo().window(newWindow);
        wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL of the new tab should contain " + expectedDomain);
        driver.close();
        
        driver.switchTo().window(originalWindow);
        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
        assertTrue(driver.getTitle().contains("JSFiddle"), "Should be back on a JSFiddle page.");
    }
}