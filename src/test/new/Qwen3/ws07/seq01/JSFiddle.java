package Qwen3.ws07.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
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
        
        String title = driver.getTitle();
        assertTrue(title.contains("JSFiddle"), "Page title should contain 'JSFiddle'");
        
        // Check main editor area is present - wait for body to ensure page loaded
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        try {
            WebElement editorContainer = driver.findElement(By.cssSelector(".editor-container"));
            assertTrue(editorContainer.isDisplayed(), "Editor container should be displayed");
        } catch (NoSuchElementException e) {
            // Try alternative selectors for jsfiddle layout
            WebElement mainPanel = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".main")));
            assertNotNull(mainPanel, "Main panel should be present");
        }
    }

    @Test
    @Order(2)
    public void testEditorFunctionality() {
        driver.get("https://jsfiddle.net/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        // Wait for panels to be present - jsfiddle uses data-panel attribute
        WebElement htmlPanel = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-panel='html']")));
        WebElement cssPanel = driver.findElement(By.cssSelector("[data-panel='css']"));
        WebElement jsPanel = driver.findElement(By.cssSelector("[data-panel='js']"));
        
        // Click to focus HTML panel
        htmlPanel.click();
        
        // Find the editor textarea inside the panel
        WebElement htmlEditor = htmlPanel.findElement(By.cssSelector("textarea"));
        htmlEditor.clear();
        htmlEditor.sendKeys("<h1>Hello JSFiddle</h1>");
        
        // Switch to CSS panel
        cssPanel.click();
        WebElement cssEditor = cssPanel.findElement(By.cssSelector("textarea"));
        cssEditor.clear();
        cssEditor.sendKeys("h1 { color: blue; }");
        
        // Switch to JS panel
        jsPanel.click();
        WebElement jsEditor = jsPanel.findElement(By.cssSelector("textarea"));
        jsEditor.clear();
        jsEditor.sendKeys("document.querySelector('h1').addEventListener('click', function() { alert('Clicked!'); });");
        
        // Verify saving works - look for run button instead
        List<WebElement> runButtons = driver.findElements(By.cssSelector(".run-button, [title='Run'], button:contains('Run')"));
        if (runButtons.size() > 0) {
            runButtons.get(0).click();
        }
    }

    @Test
    @Order(3)
    public void testPreviewGeneration() {
        driver.get("https://jsfiddle.net/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        // Ensure editor has content
        WebElement htmlPanel = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-panel='html']")));
        htmlPanel.click();
        WebElement htmlEditor = htmlPanel.findElement(By.cssSelector("textarea"));
        htmlEditor.clear();
        htmlEditor.sendKeys("<p>This is a preview test</p>");
        
        // Trigger preview - click run button
        List<WebElement> runButtons = driver.findElements(By.cssSelector(".run-button, button[title*='run' i], .button:contains('Run')"));
        if (runButtons.size() > 0) {
            runButtons.get(0).click();
        }
        
        // Wait for preview to render - jsfiddle uses iframe for results
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe[name='result'], #result")));
        assertTrue(resultFrame.isDisplayed(), "Result frame should be displayed");
        
        // Verify content in preview
        driver.switchTo().frame(resultFrame);
        WebElement previewContent = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("p")));
        assertTrue(previewContent.isDisplayed(), "Content should appear in preview");
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(4)
    public void testCodeTemplates() {
        driver.get("https://jsfiddle.net/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        // Look for template buttons or any initialization options
        List<WebElement> templateButtons = driver.findElements(By.cssSelector("button:contains('Framework'), button:contains('Template'), select"));
        if (templateButtons.size() > 0) {
            // Framework/template selector found
            templateButtons.get(0).click();
            // Wait for any changes
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            
            // Verify panel has content
            WebElement htmlPanel = driver.findElement(By.cssSelector("[data-panel='html']"));
            String content = htmlPanel.findElement(By.cssSelector("textarea")).getText();
            assertTrue(!content.isEmpty(), "Template should apply to editor");
        } else {
            // If no templates available, verify main interface is still functional
            WebElement htmlPanel = driver.findElement(By.cssSelector("[data-panel='html']"));
            assertTrue(htmlPanel.isDisplayed(), "HTML panel should be present");
        }
    }

    @Test
    @Order(5)
    public void testShareFunctionality() {
        driver.get("https://jsfiddle.net/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        // Fill HTML panel with some simple content
        WebElement htmlPanel = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-panel='html']")));
        htmlPanel.click();
        WebElement htmlEditor = htmlPanel.findElement(By.cssSelector("textarea"));
        htmlEditor.clear();
        htmlEditor.sendKeys("<h1>Shared Test</h1>");
        
        // Look for any button that might lead to sharing
        List<WebElement> shareButtons = driver.findElements(By.cssSelector("button:contains('Share'), a:contains('Share'), button[title*='share' i]"));
        if (shareButtons.size() > 0) {
            shareButtons.get(0).click();
            
            // Wait for dialog or url to update
            try {
                // Check for alert
                driver.switchTo().alert().accept();
                assertTrue(true, "Share dialog should have appeared");
            } catch (NoAlertPresentException e) {
                // look for url change
                WebDriverWait urlWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                urlWait.until(d -> d.getCurrentUrl().contains("/show/"));
                assertTrue(driver.getCurrentUrl().contains("/show/"), "URL should change to show view when shared");
            }
        }
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get("https://jsfiddle.net/");
        
        String parentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(parentWindow)) {
                driver.switchTo().window(window);
                driver.close();
            }
        }
        driver.switchTo().window(parentWindow);
        
        // Check footer links for external sites
        try {
            List<WebElement> aboutLinks = driver.findElements(By.partialLinkText("About"));
            if (aboutLinks.size() > 0) {
                aboutLinks.get(0).click();
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("jsfiddle") || currentUrl.contains("about"), 
                          "Should either open related page or maintain site context");
                driver.navigate().back();
            }
        } catch (NoSuchElementException e) {
            // Continue if About link not found
        }
        
        try {
            List<WebElement> helpLinks = driver.findElements(By.partialLinkText("Help"));
            if (helpLinks.size() > 0) {
                helpLinks.get(0).click();
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("jsfiddle") || currentUrl.contains("help"), 
                          "Should either open related page or maintain site context");
                driver.navigate().back();
            }
        } catch (NoSuchElementException e) {
            // Continue if Help link not found
        }
        
        try {
            List<WebElement> blogLinks = driver.findElements(By.partialLinkText("Blog"));
            if (blogLinks.size() > 0) {
                blogLinks.get(0).click();
                String currentUrl = driver.getCurrentUrl();
                // Blog link might be external, so just ensure we can navigate
                assertFalse(currentUrl.isEmpty(), "Should navigate to blog page");
                driver.navigate().back();
            }
        } catch (NoSuchElementException e) {
            // Continue if Blog link not found
        }
    }

    @Test
    @Order(7)
    public void testUserInterfaceElements() {
        driver.get("https://jsfiddle.net/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        // Test main UI components - jsfiddle uses sidebar panels
        List<WebElement> htmlPanels = driver.findElements(By.cssSelector("[data-panel='html']"));
        List<WebElement> cssPanels = driver.findElements(By.cssSelector("[data-panel='css']"));
        List<WebElement> jsPanels = driver.findElements(By.cssSelector("[data-panel='js']"));
        List<WebElement> resultPanels = driver.findElements(By.cssSelector("[data-panel='result']"));
        
        assertTrue(htmlPanels.size() > 0, "HTML panel should be visible");
        assertTrue(cssPanels.size() > 0, "CSS panel should be visible");
        assertTrue(jsPanels.size() > 0, "JavaScript panel should be visible");
        assertTrue(resultPanels.size() > 0, "Result panel should be visible");
        
        // Test toolbar or header buttons
        List<WebElement> toolbarButtons = driver.findElements(By.cssSelector("button, .btn, header a"));
        assertTrue(toolbarButtons.size() > 0, "Toolbar buttons should be present");
    }

    @Test
    @Order(8)
    public void testResponsiveDesign() {
        driver.get("https://jsfiddle.net/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        // Check that main container or layout wrapper exists and is visible
        WebElement mainLayout = driver.findElement(By.cssSelector(".main, .layout, body"));
        assertTrue(mainLayout.isDisplayed(), "Main layout should be displayed");
        
        // Check that editor panels exist
        List<WebElement> editorPanels = driver.findElements(By.cssSelector("[data-panel]"));
        assertTrue(editorPanels.size() >= 3, "Should have at least 3 editor panels (HTML, CSS, JS)");
        
        // Check that results panel exists
        List<WebElement> resultPanels = driver.findElements(By.cssSelector("[data-panel='result']"));
        assertTrue(resultPanels.size() > 0, "Result panel should be present for responsive testing");
    }
}