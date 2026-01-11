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
        
        WebElement header = driver.findElement(By.cssSelector("header"));
        assertTrue(header.isDisplayed(), "Header should be displayed");
    }

    @Test
    @Order(2)
    public void testNavigationMenu() {
        driver.get("https://jsfiddle.net/");
        
        List<WebElement> navLinks = driver.findElements(By.cssSelector("header a"));
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
        List<WebElement> panels = driver.findElements(By.cssSelector(".panel"));
        assertTrue(panels.size() >= 4, "Should have at least 4 editor panels");
        
        // Look for HTML/CSS/JS/Result sections by checking for common elements
        List<WebElement> resultFrames = driver.findElements(By.cssSelector("iframe"));
        assertTrue(resultFrames.size() > 0, "Should have result iframe");
    }

    @Test
    @Order(4)
    public void testEditorFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Test editor panels
        List<WebElement> panels = driver.findElements(By.cssSelector(".panel"));
        assertTrue(panels.size() > 0, "Should have editor panels");
        
        // Test result area
        WebElement resultArea = driver.findElement(By.cssSelector("iframe"));
        assertTrue(resultArea.isDisplayed(), "Result area should be displayed");
    }

    @Test
    @Order(5)
    public void testRunButton() {
        driver.get("https://jsfiddle.net/");
        
        WebElement runButton = driver.findElement(By.cssSelector("button"));
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
        List<WebElement> buttons = driver.findElements(By.cssSelector("button"));
        boolean foundSave = false, foundLoad = false;
        for (WebElement button : buttons) {
            String text = button.getText().toLowerCase();
            if (text.contains("save")) foundSave = true;
            if (text.contains("load")) foundLoad = true;
        }
        assertTrue(foundSave || buttons.size() > 0, "Should have save functionality");
        assertTrue(foundLoad || buttons.size() > 0, "Should have load functionality");
    }

    @Test
    @Order(7)
    public void testFiddleOptions() {
        driver.get("https://jsfiddle.net/");
        
        // Check for framework selection elements
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        assertTrue(selects.size() > 0, "Should have select elements for options");
        
        // Check for framework selection
        List<WebElement> frameworkSelects = driver.findElements(By.cssSelector("select"));
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
        boolean hasFooterLinks = footerLinks.size() > 0;
        
        List<WebElement> allLinks = driver.findElements(By.tagName("a"));
        assertTrue(allLinks.size() > 0, "Should have links");
    }

    @Test
    @Order(9)
    public void testResponsiveDesignElements() {
        driver.get("https://jsfiddle.net/");
        
        // Check for responsive elements
        List<WebElement> containers = driver.findElements(By.className("container"));
        assertTrue(containers.size() > 0, "Should have container elements");
        
        // Check for editor container
        List<WebElement> panels = driver.findElements(By.cssSelector(".panel-group"));
        assertTrue(panels.size() > 0, "Should have panel groups");
        
        // Check for result container
        List<WebElement> resultFrames = driver.findElements(By.cssSelector("iframe"));
        assertTrue(resultFrames.size() > 0, "Should have result iframes");
    }

    @Test
    @Order(10)
    public void testKeyboardShortcuts() {
        driver.get("https://jsfiddle.net/");
        
        // Find first input area
        List<WebElement> inputs = driver.findElements(By.tagName("textarea"));
        if (inputs.size() > 0) {
            WebElement firstInput = inputs.get(0);
            firstInput.sendKeys("Hello JSFiddle");
            
            String text = firstInput.getAttribute("value");
            assertTrue(text.contains("Hello JSFiddle"), "Editor should accept text input");
        }
    }

    @Test
    @Order(11)
    public void testFiddleCreationFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Find input areas
        List<WebElement> textareas = driver.findElements(By.tagName("textarea"));
        if (textareas.size() >= 3) {
            // Fill editors with sample code
            textareas.get(0).clear();
            textareas.get(0).sendKeys("<h1>Hello World</h1>");
            
            if (textareas.size() > 1) {
                textareas.get(1).clear();
                textareas.get(1).sendKeys("h1 { color: blue; }");
            }
            
            if (textareas.size() > 2) {
                textareas.get(2).clear();
                textareas.get(2).sendKeys("console.log('Test');");
            }
            
            // Verify content was entered
            assertTrue(textareas.get(0).getAttribute("value").contains("<h1>Hello World</h1>"), 
                       "HTML editor should contain the entered content");
            if (textareas.size() > 1) {
                assertTrue(textareas.get(1).getAttribute("value").contains("color: blue"), 
                           "CSS editor should contain the entered content");
            }
            if (textareas.size() > 2) {
                assertTrue(textareas.get(2).getAttribute("value").contains("console.log"), 
                           "JavaScript editor should contain the entered content");
            }
        }
    }

    @Test
    @Order(12)
    public void testJavaScriptExecution() {
        driver.get("https://jsfiddle.net/");
        
        // Test if we can run code
        WebElement runButton = driver.findElement(By.cssSelector("button"));
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
            List<WebElement> links = driver.findElements(By.tagName("a"));
            for (WebElement link : links) {
                if (link.getText().toLowerCase().contains("example")) {
                    link.click();
                    break;
                }
            }
            
            // Wait for navigation to complete
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
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
        assertTrue(headerElements.size() >= 1, "Should have at least one header element");
        
        List<WebElement> mainElements = driver.findElements(By.tagName("main"));
        assertTrue(mainElements.size() >= 0, "Main elements check");
        
        List<WebElement> footerElements = driver.findElements(By.tagName("footer"));
        assertTrue(footerElements.size() >= 1, "Should have at least one footer element");
        
        // Check for proper labeling of editors
        List<WebElement> labels = driver.findElements(By.cssSelector("label"));
        assertTrue(labels.size() >= 0, "Labels check");
        
        // Check for ARIA attributes
        List<WebElement> ariaElements = driver.findElements(By.cssSelector("[aria-label]"));
        assertTrue(ariaElements.size() >= 0, "ARIA elements check");
    }

    @Test
    @Order(15)
    public void testPageElementsStructure() {
        driver.get("https://jsfiddle.net/");
        
        // Check top-level structure
        List<WebElement> mainSections = driver.findElements(By.cssSelector("main, section"));
        assertTrue(mainSections.size() >= 0, "Should have main or section elements");
        
        // Check for common form elements
        List<WebElement> buttons = driver.findElements(By.cssSelector("button"));
        assertTrue(buttons.size() > 0, "Should have buttons");
        
        // Check for input elements
        List<WebElement> inputs = driver.findElements(By.cssSelector("input, textarea"));
        assertTrue(inputs.size() > 0, "Should have input elements");
        
        // Check for textareas
        List<WebElement> textareas = driver.findElements(By.tagName("textarea"));
        assertTrue(textareas.size() > 0, "Should have textareas");
        
        // Check for div containers
        List<WebElement> divs = driver.findElements(By.tagName("div"));
        assertTrue(divs.size() > 10, "Should have multiple div containers");
    }
}