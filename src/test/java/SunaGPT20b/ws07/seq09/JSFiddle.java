package deepseek.ws07.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddleTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final String TEST_CODE_HTML = "<h1>Hello World</h1>";
    private static final String TEST_CODE_CSS = "h1 { color: blue; }";
    private static final String TEST_CODE_JS = "console.log('test');";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement editorPanel = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#editor")));
        assertTrue(editorPanel.isDisplayed(), "Editor panel should be visible");
    }

    @Test
    @Order(2)
    public void testCodeEditing() {
        driver.get(BASE_URL);
        
        // Switch to HTML panel
        WebElement htmlTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#tabs li:nth-child(1)")));
        htmlTab.click();
        
        WebElement htmlEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#html-editor")));
        htmlEditor.clear();
        htmlEditor.sendKeys(TEST_CODE_HTML);
        
        // Switch to CSS panel
        WebElement cssTab = driver.findElement(By.cssSelector("#tabs li:nth-child(2)"));
        cssTab.click();
        
        WebElement cssEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#css-editor")));
        cssEditor.clear();
        cssEditor.sendKeys(TEST_CODE_CSS);
        
        // Switch to JS panel
        WebElement jsTab = driver.findElement(By.cssSelector("#tabs li:nth-child(3)"));
        jsTab.click();
        
        WebElement jsEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#js-editor")));
        jsEditor.clear();
        jsEditor.sendKeys(TEST_CODE_JS);
        
        // Verify content persists
        htmlTab.click();
        assertEquals(TEST_CODE_HTML, htmlEditor.getAttribute("value"), 
            "HTML content should persist");
    }

    @Test
    @Order(3)
    public void testCodeExecution() {
        driver.get(BASE_URL);
        
        // Set test code
        WebElement htmlEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#html-editor")));
        htmlEditor.clear();
        htmlEditor.sendKeys(TEST_CODE_HTML);
        
        // Run the code
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#run")));
        runButton.click();
        
        // Check output
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#result")));
        driver.switchTo().frame(resultFrame);
        
        WebElement output = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h1")));
        assertEquals("Hello World", output.getText(), "Output should match the HTML code");
        
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(4)
    public void testSaveToAnonymousBin() {
        driver.get(BASE_URL);
        
        // Set test code
        WebElement htmlEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#html-editor")));
        htmlEditor.clear();
        htmlEditor.sendKeys(TEST_CODE_HTML);
        
        // Click save button
        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#save")));
        saveButton.click();
        
        // Wait for save to complete
        WebElement shareLink = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#shareurl")));
        assertTrue(shareLink.getAttribute("value").contains("jsfiddle.net"),
            "Share link should contain jsfiddle.net domain");
    }

    @Test
    @Order(5)
    public void testMenuNavigation() {
        driver.get(BASE_URL);
        
        // Click Documentation link
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='https://doc.jsfiddle.net/']")));
        docsLink.click();
        
        // Verify new tab opened
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains("doc.jsfiddle.net"), 
            "Should navigate to documentation site");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testPanelResizing() {
        driver.get(BASE_URL);
        
        // Find and drag the resizer
        WebElement resizer = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".horizontal-resizer")));
        
        Actions actions = new Actions(driver);
        actions.clickAndHold(resizer)
               .moveByOffset(0, 50)
               .release()
               .perform();
        
        WebElement editorWrapper = driver.findElement(By.cssSelector("#editor"));
        assertTrue(editorWrapper.getSize().height > 300, 
            "Editor height should increase after resizing");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        testExternalLink(githubLink, "github.com");

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        testExternalLink(twitterLink, "twitter.com");
    }

    private void testExternalLink(WebElement link, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "External link should open " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}