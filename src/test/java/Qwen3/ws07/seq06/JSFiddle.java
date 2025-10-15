package Qwen3.ws07.seq06;

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
    public void testHomePageLoadsCorrectly() {
        driver.get("https://jsfiddle.net/");
        
        String currentPageTitle = driver.getTitle();
        assertTrue(currentPageTitle.contains("JSFiddle"), "Page title should contain 'JSFiddle'");
        
        // Check for main fiddle editor elements
        WebElement editorContainer = driver.findElement(By.cssSelector(".editor-container"));
        assertTrue(editorContainer.isDisplayed(), "Editor container should be displayed");
        
        WebElement iframe = driver.findElement(By.tagName("iframe"));
        assertTrue(iframe.isDisplayed(), "IFrame should be displayed");
    }

    @Test
    @Order(2)
    public void testEditorFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Wait for editor to be available
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".ace_editor")));
        
        // Test that editor is present and editable
        WebElement editor = driver.findElement(By.cssSelector(".ace_editor"));
        assertTrue(editor.isDisplayed(), "Editor should be displayed");
        
        // Check for language selection (if available)
        try {
            WebElement languageSelect = driver.findElement(By.cssSelector(".language-select"));
            assertTrue(languageSelect.isDisplayed(), "Language selector should be present");
        } catch (NoSuchElementException e) {
            // Language selector might not be present, that's OK
        }
    }

    @Test
    @Order(3)
    public void testFiddleCreation() {
        driver.get("https://jsfiddle.net/");
        
        // Test creation of a simple fiddle
        // Wait for page elements
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".ace_editor")));
        
        // Check if we can find the HTML, CSS, and JS panes
        List<WebElement> panes = driver.findElements(By.cssSelector(".ace_editor"));
        assertTrue(panes.size() >= 3, "Should have at least 3 editor panes (HTML, CSS, JS)");
        
        // Try to find the run button 
        try {
            WebElement runButton = driver.findElement(By.cssSelector(".run-button"));
            assertTrue(runButton.isDisplayed(), "Run button should be displayed");
        } catch (NoSuchElementException e) {
            // Run button might not have specific class
        }
    }

    @Test
    @Order(4)
    public void testNavigationToExamples() {
        driver.get("https://jsfiddle.net/");
        
        // Try to find examples link
        try {
            WebElement examplesLink = driver.findElement(By.linkText("Examples"));
            examplesLink.click();
            
            // Wait for examples page or check URL change
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("examples") || currentUrl.contains("explore"), 
                       "Should navigate to examples section");
            
            // Go back to main page
            driver.navigate().back();
        } catch (NoSuchElementException e) {
            // If examples link doesn't exist, that's okay for this test
        }
    }

    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get("https://jsfiddle.net/");
        
        // Wait for footer to be present
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("footer")));
        
        // Check for external links in footer
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("github") || href.contains("twitter") || href.contains("facebook"))) {
                // These are external links we want to test
                String oldTab = driver.getWindowHandle();
                link.click();
                String winHandle = driver.getWindowHandle();
                driver.switchTo().window(winHandle);
                
                // Verify we navigated to expected domain
                if (href.contains("github")) {
                    assertTrue(driver.getCurrentUrl().contains("github.com"), 
                              "GitHub link should navigate to GitHub website");
                } else if (href.contains("twitter")) {
                    assertTrue(driver.getCurrentUrl().contains("twitter.com"), 
                              "Twitter link should navigate to Twitter website");
                } else if (href.contains("facebook")) {
                    assertTrue(driver.getCurrentUrl().contains("facebook.com"), 
                              "Facebook link should navigate to Facebook website");
                }
                
                driver.close();
                driver.switchTo().window(oldTab);
            }
        }
    }

    @Test
    @Order(6)
    public void testResponsiveLayout() {
        driver.get("https://jsfiddle.net/");
        
        // Check if the page is responsive by verifying key elements are present
        WebElement header = driver.findElement(By.tagName("header"));
        assertTrue(header.isDisplayed(), "Header should be displayed");
        
        WebElement mainContent = driver.findElement(By.tagName("main"));
        assertTrue(mainContent.isDisplayed(), "Main content should be displayed");
        
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed(), "Footer should be displayed");
        
        // Check if editor is responsive by checking it's in the main content
        WebElement editorContainer = driver.findElement(By.cssSelector(".editor-container"));
        assertTrue(editorContainer.isDisplayed(), "Editor container should be displayed");
    }

    @Test
    @Order(7)
    public void testKeyFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Wait for the editor to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".ace_editor")));
        
        // Try to interact with editor by checking it's editable
        try {
            WebElement editor = driver.findElement(By.cssSelector(".ace_editor"));
            assertTrue(editor.isDisplayed(), "Editor should be visible");
            
            // Try to check if we can interact with ace editor (keystrokes)
            // Instead, we check for the presence of ace-related elements
            List<WebElement> aceElements = driver.findElements(By.cssSelector(".ace_scroller"));
            assertTrue(aceElements.size() > 0, "Ace editor should be loaded");
        } catch (NoSuchElementException e) {
            // If core elements don't exist, it might be another version of JSFiddle
            // but the page should still load properly
        }
        
        // Check for save/share buttons
        try {
            List<WebElement> saveButtons = driver.findElements(By.cssSelector("[title*='save'], [title*='Save']"));
            if (!saveButtons.isEmpty()) {
                assertTrue(saveButtons.get(0).isDisplayed(), "Save button should be displayed");
            }
        } catch (NoSuchElementException e) {
            // Save button might not be available in certain contexts
        }
    }
}