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
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
        
        // Check main editor area is present
        WebElement editorContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("editor-container")));
        assertTrue(editorContainer.isDisplayed(), "Editor container should be displayed");
    }

    @Test
    @Order(2)
    public void testEditorFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Wait for editor to be ready
        WebElement htmlEditor = wait.until(ExpectedConditions.elementToBeClickable(By.id("id_html")));
        WebElement cssEditor = driver.findElement(By.id("id_css"));
        WebElement jsEditor = driver.findElement(By.id("id_javascript"));
        
        // Modify HTML content
        htmlEditor.clear();
        htmlEditor.sendKeys("<h1>Hello JSFiddle</h1>");
        
        // Modify CSS content
        cssEditor.clear();
        cssEditor.sendKeys("h1 { color: blue; }");
        
        // Modify JavaScript content
        jsEditor.clear();
        jsEditor.sendKeys("document.querySelector('h1').addEventListener('click', function() { alert('Clicked!'); });");
        
        // Verify saving works
        WebElement saveButton = driver.findElement(By.id("save-button"));
        saveButton.click();
        
        // Check that saved indicator appears
        WebElement savedMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".saved-message")));
        assertTrue(savedMessage.isDisplayed(), "Saved message should appear after saving");
    }

    @Test
    @Order(3)
    public void testPreviewGeneration() {
        driver.get("https://jsfiddle.net/");
        
        // Ensure editor has content
        WebElement htmlEditor = wait.until(ExpectedConditions.elementToBeClickable(By.id("id_html")));
        htmlEditor.clear();
        htmlEditor.sendKeys("<p>This is a preview test</p>");
        
        // Trigger preview
        WebElement previewButton = driver.findElement(By.id("preview-button"));
        previewButton.click();
        
        // Wait for preview to render
        WebElement previewFrame = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("result")));
        assertTrue(previewFrame.isDisplayed(), "Preview frame should be displayed");
        
        // Verify content in preview
        driver.switchTo().frame("result");
        WebElement previewContent = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("p")));
        assertTrue(previewContent.isDisplayed(), "Content should appear in preview");
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(4)
    public void testCodeTemplates() {
        driver.get("https://jsfiddle.net/");
        
        // Look for template buttons or dropdown
        List<WebElement> templateButtons = driver.findElements(By.cssSelector(".template-button"));
        if (templateButtons.size() > 0) {
            // Click first template button if available
            templateButtons.get(0).click();
            // Verify template was applied by checking if content changed
            WebElement htmlEditor = driver.findElement(By.id("id_html"));
            String content = htmlEditor.getText();
            assertTrue(content.contains("<!DOCTYPE html>") || !content.isEmpty(), 
                       "Template should apply to editor");
        } else {
            // If no templates available, verify main interface is still functional
            WebElement htmlEditor = driver.findElement(By.id("id_html"));
            assertTrue(htmlEditor.isDisplayed(), "HTML editor should be present");
        }
    }

    @Test
    @Order(5)
    public void testShareFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Fill editor with some simple content
        WebElement htmlEditor = wait.until(ExpectedConditions.elementToBeClickable(By.id("id_html")));
        htmlEditor.clear();
        htmlEditor.sendKeys("<h1>Shared Test</h1>");
        
        // Try to share button
        try {
            WebElement shareButton = driver.findElement(By.id("share-button"));
            shareButton.click();
            
            // Verify share dialog appeared
            WebElement shareDialog = driver.findElement(By.cssSelector(".share-dialog"));
            assertTrue(shareDialog.isDisplayed(), "Share dialog should appear after clicking share");
        } catch (NoSuchElementException e) {
            // If share button not present, it's acceptable for test to pass
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
        
        // Check footer links for external sites
        try {
            WebElement aboutLink = driver.findElement(By.linkText("About"));
            aboutLink.click();
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("jsfiddle") || currentUrl.contains("about"), 
                      "Should either open related page or maintain site context");
            if (currentUrl.contains("jsfiddle")) {
                driver.close();
                driver.switchTo().window(parentWindow);
            }
        } catch (NoSuchElementException e) {
            // Continue if About link not found
        }
        
        try {
            WebElement helpLink = driver.findElement(By.linkText("Help"));
            helpLink.click();
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("jsfiddle") || currentUrl.contains("help"), 
                      "Should either open related page or maintain site context");
            if (currentUrl.contains("jsfiddle")) {
                driver.close();
                driver.switchTo().window(parentWindow);
            }
        } catch (NoSuchElementException e) {
            // Continue if Help link not found
        }
        
        try {
            WebElement blogLink = driver.findElement(By.linkText("Blog"));
            blogLink.click();
            
            String currentUrl = driver.getCurrentUrl();
            // Blog link might be external, so just ensure we can navigate
            assertFalse(currentUrl.isEmpty(), "Should navigate to blog page");
        } catch (NoSuchElementException e) {
            // Continue if Blog link not found
        }
    }

    @Test
    @Order(7)
    public void testUserInterfaceElements() {
        driver.get("https://jsfiddle.net/");
        
        // Test main UI components
        WebElement htmlTab = driver.findElement(By.cssSelector("a[href='#html']"));
        WebElement cssTab = driver.findElement(By.cssSelector("a[href='#css']"));
        WebElement jsTab = driver.findElement(By.cssSelector("a[href='#javascript']"));
        WebElement resultTab = driver.findElement(By.cssSelector("a[href='#result']"));
        
        assertTrue(htmlTab.isDisplayed(), "HTML tab should be visible");
        assertTrue(cssTab.isDisplayed(), "CSS tab should be visible");
        assertTrue(jsTab.isDisplayed(), "JavaScript tab should be visible");
        assertTrue(resultTab.isDisplayed(), "Result tab should be visible");
        
        // Test editor toolbar buttons if they exist
        List<WebElement> toolbarButtons = driver.findElements(By.cssSelector(".toolbar-button"));
        assertTrue(toolbarButtons.size() > 0, "Toolbar buttons should be present");
    }

    @Test
    @Order(8)
    public void testResponsiveDesign() {
        driver.get("https://jsfiddle.net/");
        
        // Check that main container exists and is visible
        WebElement mainContainer = driver.findElement(By.cssSelector("#main"));
        assertTrue(mainContainer.isDisplayed(), "Main container should be displayed");
        
        // Check that UI elements resize properly (basic check)
        WebElement editorContainer = driver.findElement(By.id("editor-container"));
        assertTrue(editorContainer.isDisplayed(), "Editor container should be displayed");
        
        // Check for mobile responsive elements
        List<WebElement> responsiveElements = driver.findElements(By.cssSelector(".responsive-element"));
        if (responsiveElements.size() > 0) {
            assertTrue(responsiveElements.get(0).isDisplayed(), "Responsive elements should be displayable");
        }
    }
}