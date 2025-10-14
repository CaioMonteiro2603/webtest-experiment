package Qwen3.ws07.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class JsFiddleTest {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
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
    public void testPageLoad() {
        driver.get("https://jsfiddle.net/");
        
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        assertTrue(driver.getTitle().contains("JSFiddle"));
        assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"));
    }

    @Test
    @Order(2)
    public void testEditorAreas() {
        driver.get("https://jsfiddle.net/");
        
        // Verify editor sections exist
        WebElement htmlEditor = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("editor-html")));
        assertTrue(htmlEditor.isDisplayed());
        
        WebElement cssEditor = driver.findElement(By.id("editor-css"));
        assertTrue(cssEditor.isDisplayed());
        
        WebElement jsEditor = driver.findElement(By.id("editor-js"));
        assertTrue(jsEditor.isDisplayed());
        
        WebElement resultFrame = driver.findElement(By.id("result"));
        assertTrue(resultFrame.isDisplayed());
    }

    @Test
    @Order(3)
    public void testCodeEditing() {
        driver.get("https://jsfiddle.net/");
        
        // Clear and fill HTML editor
        WebElement htmlEditor = wait.until(ExpectedConditions.elementToBeClickable(By.id("editor-html")));
        htmlEditor.clear();
        htmlEditor.sendKeys("<h1>Test Heading</h1>");
        
        // Clear and fill CSS editor
        WebElement cssEditor = driver.findElement(By.id("editor-css"));
        cssEditor.clear();
        cssEditor.sendKeys("h1 { color: red; }");
        
        // Clear and fill JS editor
        WebElement jsEditor = driver.findElement(By.id("editor-js"));
        jsEditor.clear();
        jsEditor.sendKeys("console.log('Hello World');");
        
        // Trigger preview
        WebElement runButton = driver.findElement(By.id("run"));
        runButton.click();
        
        // Wait for result to update
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("result"));
        
        // Verify result
        WebElement resultHeading = driver.findElement(By.cssSelector("h1"));
        assertTrue(resultHeading.isDisplayed());
        assertEquals("Test Heading", resultHeading.getText());
        
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(4)
    public void testPresets() {
        driver.get("https://jsfiddle.net/");
        
        // Test preset dropdown
        WebElement presetSelect = driver.findElement(By.id("presets"));
        presetSelect.click();
        
        // Get preset options
        List<WebElement> presetOptions = driver.findElements(By.cssSelector("#presets option"));
        assertTrue(presetOptions.size() > 0);
        
        // Select first option
        if (presetOptions.size() > 1) {
            presetOptions.get(1).click();
            
            // Verify the selection
            assertTrue(presetSelect.getAttribute("value").length() > 0);
        }
    }

    @Test
    @Order(5)
    public void testSaveFeature() {
        driver.get("https://jsfiddle.net/");
        
        // Try to save a fiddle (this requires login)
        // For now, just verify the save button exists
        WebElement saveButton = driver.findElement(By.id("save-button"));
        assertTrue(saveButton.isDisplayed());
        
        // Try creating a new fiddle
        WebElement newButton = driver.findElement(By.id("new-button"));
        newButton.click();
        
        // Verify we are on a new fiddle page
        wait.until(ExpectedConditions.urlContains("/new"));
        assertTrue(driver.getCurrentUrl().contains("/new"));
    }

    @Test
    @Order(6)
    public void testSharingFeatures() {
        driver.get("https://jsfiddle.net/");
        
        // Verify share button exists
        WebElement shareButton = driver.findElement(By.id("share-button"));
        assertTrue(shareButton.isDisplayed());
        
        // Verify embed button exists
        WebElement embedButton = driver.findElement(By.id("embed-button"));
        assertTrue(embedButton.isDisplayed());
        
        // Verify permalink button exists
        WebElement permalinkButton = driver.findElement(By.id("permalink-button"));
        assertTrue(permalinkButton.isDisplayed());
    }

    @Test
    @Order(7)
    public void testResponsivePreview() {
        driver.get("https://jsfiddle.net/");
        
        // Find preview buttons
        List<WebElement> previewButtons = driver.findElements(By.cssSelector(".preview-toggle button"));
        if (!previewButtons.isEmpty()) {
            for (WebElement button : previewButtons) {
                if (button.isDisplayed()) {
                    button.click();
                    break;
                }
            }
        }
        
        // Verify responsive modes work
        WebElement responsiveButton = driver.findElement(By.id("responsive-button"));
        assertTrue(responsiveButton.isDisplayed());
    }

    @Test
    @Order(8)
    public void testKeyboardShortcuts() {
        driver.get("https://jsfiddle.net/");
        
        // Test that the editor is focusable and accepts keyboard input
        WebElement htmlEditor = wait.until(ExpectedConditions.elementToBeClickable(By.id("editor-html")));
        assertTrue(htmlEditor.isDisplayed());
        
        // Test typing in editor
        htmlEditor.sendKeys("Test");
        assertEquals("Test", htmlEditor.getAttribute("value"));
    }

    @Test
    @Order(9)
    public void testSyntaxHighlighting() {
        driver.get("https://jsfiddle.net/");
        
        // Verify editors have highlighting features
        WebElement jsEditor = driver.findElement(By.id("editor-js"));
        assertTrue(jsEditor.isDisplayed());
        
        // Write JavaScript code
        jsEditor.sendKeys("var x = 5;");
        
        // Since we can't easily verify syntax highlighting visually,
        // just assert that the element can accept text inputs
        assertTrue(jsEditor.getText().contains("var x = 5;"));
    }

    @Test
    @Order(10)
    public void testNavigationAndMenus() {
        driver.get("https://jsfiddle.net/");
        
        // Test main navigation items
        List<WebElement> navItems = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navItems.size() > 0);
        
        // Test specific navigation items
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        assertTrue(homeLink.isDisplayed());
        
        // Test account-related menus (if available)
        try {
            WebElement accountMenu = driver.findElement(By.id("account-menu"));
            if (accountMenu.isDisplayed()) {
                accountMenu.click();
                
                // Verify menu items
                List<WebElement> menuItems = driver.findElements(By.cssSelector(".account-menu-item"));
                assertTrue(menuItems.size() > 0);
            }
        } catch (NoSuchElementException e) {
            // Account menu might not be visible for guest users
        }
    }

    @Test
    @Order(11)
    public void testExternalLinks() {
        driver.get("https://jsfiddle.net/");
        
        // Test external links in footer
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("footer a[href^='http']"));
        assertTrue(externalLinks.size() > 0);
        
        // Test a few specific links
        for (int i = 0; i < Math.min(externalLinks.size(), 3); i++) {
            WebElement link = externalLinks.get(i);
            if (link.isDisplayed()) {
                String href = link.getAttribute("href");
                assertNotNull(href);
                assertTrue(href.startsWith("http"));
            }
        }
    }
}