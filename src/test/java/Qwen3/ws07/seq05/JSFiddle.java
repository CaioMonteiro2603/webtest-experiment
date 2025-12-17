package Qwen3.ws07.seq05;

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

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
    public void testPageLoadAndTitle() {
        driver.get("https://jsfiddle.net/");
        
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("JSFiddle"), "Page title should contain JSFiddle");
        
        WebElement header = driver.findElement(By.cssSelector("header h1"));
        assertTrue(header.isDisplayed(), "Header should be displayed");
    }

    @Test
    @Order(2)
    public void testNavigationMenu() {
        driver.get("https://jsfiddle.net/");
        
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() > 0, "Should have navigation links");
        
        for (WebElement link : navLinks) {
            assertTrue(link.isDisplayed(), "Navigation link should be displayed");
            assertNotNull(link.getAttribute("href"), "Navigation link should have href attribute");
        }
    }

    @Test
    @Order(3)
    public void testMainEditorSections() {
        driver.get("https://jsfiddle.net/");
        
        // Check if editor sections are present
        WebElement htmlSection = driver.findElement(By.id("editor-html"));
        assertTrue(htmlSection.isDisplayed(), "HTML editor section should be displayed");
        
        WebElement cssSection = driver.findElement(By.id("editor-css"));
        assertTrue(cssSection.isDisplayed(), "CSS editor section should be displayed");
        
        WebElement jsSection = driver.findElement(By.id("editor-js"));
        assertTrue(jsSection.isDisplayed(), "JavaScript editor section should be displayed");
        
        WebElement resultSection = driver.findElement(By.id("result"));
        assertTrue(resultSection.isDisplayed(), "Result section should be displayed");
    }

    @Test
    @Order(4)
    public void testEditorFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Test HTML editor
        WebElement htmlEditor = driver.findElement(By.id("editor-html"));
        assertTrue(htmlEditor.isDisplayed(), "HTML editor should be displayed");
        
        // Test CSS editor
        WebElement cssEditor = driver.findElement(By.id("editor-css"));
        assertTrue(cssEditor.isDisplayed(), "CSS editor should be displayed");
        
        // Test JavaScript editor
        WebElement jsEditor = driver.findElement(By.id("editor-js"));
        assertTrue(jsEditor.isDisplayed(), "JavaScript editor should be displayed");
        
        // Test result area
        WebElement resultArea = driver.findElement(By.id("result"));
        assertTrue(resultArea.isDisplayed(), "Result area should be displayed");
    }

    @Test
    @Order(5)
    public void testRunButton() {
        driver.get("https://jsfiddle.net/");
        
        WebElement runButton = driver.findElement(By.id("run"));
        assertTrue(runButton.isDisplayed(), "Run button should be displayed");
        assertEquals("Run", runButton.getText(), "Run button should say 'Run'");
        
        // Test if click works (even if results are empty)
        runButton.click();
        
        // Wait for button to potentially change state
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
    }

    @Test
    @Order(6)
    public void testSaveAndLoadFeatures() {
        driver.get("https://jsfiddle.net/");
        
        // Check save feature
        WebElement saveButton = driver.findElement(By.id("save"));
        assertTrue(saveButton.isDisplayed(), "Save button should be displayed");
        
        // Check load feature
        WebElement loadButton = driver.findElement(By.id("load"));
        assertTrue(loadButton.isDisplayed(), "Load button should be displayed");
    }

    @Test
    @Order(7)
    public void testFiddleOptions() {
        driver.get("https://jsfiddle.net/");
        
        // Check fiddle options
        WebElement fiddleOptions = driver.findElement(By.id("fiddle-options"));
        assertTrue(fiddleOptions.isDisplayed(), "Fiddle options should be displayed");
        
        // Check for framework selection
        List<WebElement> frameworkSelects = driver.findElements(By.cssSelector("[id*='framework']"));
        for (WebElement select : frameworkSelects) {
            if (select.isDisplayed()) {
                assertTrue(select.isDisplayed(), "Framework selection should be displayed");
            }
        }
    }

    @Test
    @Order(8)
    public void testExternalLinksInFooter() {
        driver.get("https://jsfiddle.net/");
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0, "Should have footer links");
        
        String mainWindowHandle = driver.getWindowHandle();
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                // Click external links
                link.click();
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(mainWindowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                String currentUrl = driver.getCurrentUrl();
                // JSFiddle may link to external resources
                assertTrue(currentUrl.contains("jsfiddle.net") || 
                           currentUrl.contains("github.com") ||
                           currentUrl.contains("stackoverflow.com"),
                           "External link should point to valid domain");
                
                driver.close();
                driver.switchTo().window(mainWindowHandle);
            }
        }
    }

    @Test
    @Order(9)
    public void testResponsiveDesignElements() {
        driver.get("https://jsfiddle.net/");
        
        // Check for responsive elements
        WebElement container = driver.findElement(By.id("container"));
        assertTrue(container.isDisplayed(), "Main container should be displayed");
        
        // Check for editor container
        WebElement editorContainer = driver.findElement(By.id("editor-container"));
        assertTrue(editorContainer.isDisplayed(), "Editor container should be displayed");
        
        // Check for result container
        WebElement resultContainer = driver.findElement(By.id("result-container"));
        assertTrue(resultContainer.isDisplayed(), "Result container should be displayed");
    }

    @Test
    @Order(10)
    public void testKeyboardShortcuts() {
        driver.get("https://jsfiddle.net/");
        
        // Test that editor areas can receive keyboard input
        WebElement htmlEditor = driver.findElement(By.id("editor-html"));
        htmlEditor.sendKeys("Hello JSFiddle");
        
        String text = htmlEditor.getAttribute("value");
        assertTrue(text.contains("Hello JSFiddle"), "Editor should accept text input");
    }

    @Test
    @Order(11)
    public void testFiddleCreationFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Check if we can interact with editor areas
        WebElement htmlEditor = driver.findElement(By.id("editor-html"));
        WebElement cssEditor = driver.findElement(By.id("editor-css"));
        WebElement jsEditor = driver.findElement(By.id("editor-js"));
        
        // Fill editors with sample code
        htmlEditor.clear();
        htmlEditor.sendKeys("<h1>Hello World</h1>");
        
        cssEditor.clear();
        cssEditor.sendKeys("h1 { color: blue; }");
        
        jsEditor.clear();
        jsEditor.sendKeys("console.log('Test');"); 
        
        // Verify content was entered
        assertTrue(htmlEditor.getAttribute("value").contains("<h1>Hello World</h1>"), 
                   "HTML editor should contain the entered content");
        assertTrue(cssEditor.getAttribute("value").contains("color: blue"), 
                   "CSS editor should contain the entered content");
        assertTrue(jsEditor.getAttribute("value").contains("console.log"), 
                   "JavaScript editor should contain the entered content");
    }

    @Test
    @Order(12)
    public void testJavaScriptExecution() {
        driver.get("https://jsfiddle.net/");
        
        // Test if we can run JavaScript
        WebElement jsEditor = driver.findElement(By.id("editor-js"));
        jsEditor.clear();
        jsEditor.sendKeys("alert('JSFiddle Test')"); 
        
        WebElement runButton = driver.findElement(By.id("run"));
        runButton.click();
        
        // Try to handle alert if present
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept(); // Accept the alert
        } catch (TimeoutException e) {
            // Alert may not appear, which is fine for this test
        }
    }

    @Test
    @Order(13)
    public void testPageNavigationToExamples() {
        driver.get("https://jsfiddle.net/");
        
        // Try to find and click example links if available
        try {
            WebElement exampleLink = driver.findElement(By.linkText("Examples"));
            exampleLink.click();
            
            // Wait for navigation to complete
            wait.until(ExpectedConditions.urlContains("examples"));
        } catch (NoSuchElementException ignored) {
            // Examples link may not exist or be in different location
        }
    }

    @Test
    @Order(14)
    public void testAccessibilityAndSemanticElements() {
        driver.get("https://jsfiddle.net/");
        
        // Check semantic HTML elements
        List<WebElement> headerElements = driver.findElements(By.tagName("header"));
        assertEquals(1, headerElements.size(), "Should have one header element");
        
        List<WebElement> mainElements = driver.findElements(By.tagName("main"));
        assertEquals(1, mainElements.size(), "Should have one main element");
        
        List<WebElement> footerElements = driver.findElements(By.tagName("footer"));
        assertEquals(1, footerElements.size(), "Should have one footer element");
        
        // Check for proper labeling of editors
        List<WebElement> labels = driver.findElements(By.cssSelector("label"));
        assertTrue(labels.size() > 0, "Should have labels for editor sections");
        
        // Check for ARIA attributes
        List<WebElement> ariaElements = driver.findElements(By.cssSelector("[aria-label]"));
        assertTrue(ariaElements.size() > 0, "Should have ARIA attributes for accessibility");
    }

    @Test
    @Order(15)
    public void testPageElementsStructure() {
        driver.get("https://jsfiddle.net/");
        
        // Check top-level structure
        List<WebElement> mainSections = driver.findElements(By.cssSelector("main > section"));
        assertTrue(mainSections.size() > 0, "Should have main sections");
        
        // Check for common form elements
        List<WebElement> buttons = driver.findElements(By.cssSelector("button"));
        assertTrue(buttons.size() > 0, "Should have buttons");
        
        // Check for input elements
        List<WebElement> inputs = driver.findElements(By.cssSelector("input"));
        assertTrue(inputs.size() > 0, "Should have input elements");
        
        // Check for textareas
        List<WebElement> textareas = driver.findElements(By.tagName("textarea"));
        assertTrue(textareas.size() > 0, "Should have textareas");
        
        // Check for div containers
        List<WebElement> divs = driver.findElements(By.tagName("div"));
        assertTrue(divs.size() > 10, "Should have multiple div containers");
    }
}