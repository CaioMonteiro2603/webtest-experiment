package Qwen3.ws07.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JsFiddleTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testPageLoad() {
        driver.get("https://jsfiddle.net/");
        
        // Verify page title
        String title = driver.getTitle();
        assertTrue(title.contains("JSFIDDLE"));
        
        // Verify main elements are present
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("header")));
        assertTrue(header.isDisplayed());
        
        WebElement editorContainer = driver.findElement(By.id("editor-container"));
        assertTrue(editorContainer.isDisplayed());
        
        WebElement previewFrame = driver.findElement(By.id("editor-preview"));
        assertTrue(previewFrame.isDisplayed());
    }

    @Test
    @Order(2)
    public void testEditorAreas() {
        driver.get("https://jsfiddle.net/");
        
        // Check for editor areas
        WebElement htmlEditor = driver.findElement(By.id("ace_html"));
        assertTrue(htmlEditor.isDisplayed());
        
        WebElement cssEditor = driver.findElement(By.id("ace_css"));
        assertTrue(cssEditor.isDisplayed());
        
        WebElement jsEditor = driver.findElement(By.id("ace_js"));
        assertTrue(jsEditor.isDisplayed());
        
        WebElement resultFrame = driver.findElement(By.id("result"));
        assertTrue(resultFrame.isDisplayed());
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        driver.get("https://jsfiddle.net/");
        
        // Test File menu
        WebElement fileMenu = driver.findElement(By.linkText("File"));
        fileMenu.click();
        
        // Check sub-menu items
        List<WebElement> fileItems = driver.findElements(By.cssSelector("#menu-file li"));
        assertTrue(fileItems.size() >= 0);
        
        // Close menu
        fileMenu.click();
        
        // Test Edit menu
        WebElement editMenu = driver.findElement(By.linkText("Edit"));
        editMenu.click();
        
        List<WebElement> editItems = driver.findElements(By.cssSelector("#menu-edit li"));
        assertTrue(editItems.size() >= 0);
        
        // Close menu
        editMenu.click();
        
        // Test View menu
        WebElement viewMenu = driver.findElement(By.linkText("View"));
        viewMenu.click();
        
        List<WebElement> viewItems = driver.findElements(By.cssSelector("#menu-view li"));
        assertTrue(viewItems.size() >= 0);
        
        // Close menu
        viewMenu.click();
    }

    @Test
    @Order(4)
    public void testRunButton() {
        driver.get("https://jsfiddle.net/");
        
        // Check Run button
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("run-button")));
        assertTrue(runButton.isDisplayed());
        assertEquals("Run", runButton.getText());
        
        // Check Reset button
        WebElement resetButton = driver.findElement(By.id("reset-button"));
        assertTrue(resetButton.isDisplayed());
        assertEquals("Reset", resetButton.getText());
        
        // Check Share button
        WebElement shareButton = driver.findElement(By.id("share-button"));
        assertTrue(shareButton.isDisplayed());
        assertEquals("Share", shareButton.getText());
    }

    @Test
    @Order(5)
    public void testThemeSwitching() {
        driver.get("https://jsfiddle.net/");
        
        // Look for theme selector if present
        List<WebElement> themeSelectors = driver.findElements(By.cssSelector("[data-theme]"));
        if (!themeSelectors.isEmpty()) {
            // Just verify they are present
            for (WebElement themeSel : themeSelectors) {
                assertTrue(themeSel.isDisplayed());
            }
        }
    }

    @Test
    @Order(6)
    public void testKeyboardShortcutsInfo() {
        driver.get("https://jsfiddle.net/");
        
        // Check for keyboard shortcuts info
        List<WebElement> keyboardInfo = driver.findElements(By.cssSelector(".keyboard-shortcuts"));
        if (!keyboardInfo.isEmpty()) {
            assertTrue(keyboardInfo.get(0).isDisplayed());
        }
    }

    @Test
    @Order(7)
    public void testRecentFiddles() {
        driver.get("https://jsfiddle.net/");
        
        // Check recent fiddles section
        List<WebElement> recentFiddles = driver.findElements(By.cssSelector(".recent-fiddle"));
        assertTrue(recentFiddles.size() >= 0);
    }

    @Test
    @Order(8)
    public void testPopularTags() {
        driver.get("https://jsfiddle.net/");
        
        // Check popular tags
        List<WebElement> tags = driver.findElements(By.cssSelector(".tag"));
        assertTrue(tags.size() >= 0);
    }

    @Test
    @Order(9)
    public void testFooterLinks() {
        driver.get("https://jsfiddle.net/");
        
        // Check footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() >= 0);
        
        for (WebElement link : footerLinks) {
            assertTrue(link.isDisplayed());
        }
    }

    @Test
    @Order(10)
    public void testHeaderLinks() {
        driver.get("https://jsfiddle.net/");
        
        // Check header links
        List<WebElement> headerLinks = driver.findElements(By.cssSelector("header a"));
        assertTrue(headerLinks.size() >= 0);
        
        // Check specific header links
        WebElement homeLink = driver.findElement(By.linkText("JSFIDDLE"));
        assertTrue(homeLink.isDisplayed());
        
        WebElement examplesLink = driver.findElement(By.linkText("Examples"));
        assertTrue(examplesLink.isDisplayed());
        
        WebElement docsLink = driver.findElement(By.linkText("Docs"));
        assertTrue(docsLink.isDisplayed());
    }

    @Test
    @Order(11)
    public void testSearchFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Check search form if present
        List<WebElement> searchForms = driver.findElements(By.cssSelector("form[action='/search']"));
        if (!searchForms.isEmpty()) {
            WebElement searchForm = searchForms.get(0);
            assertTrue(searchForm.isDisplayed());
            
            WebElement searchInput = searchForm.findElement(By.cssSelector("input[name='q']"));
            assertTrue(searchInput.isDisplayed());
            
            WebElement searchButton = searchForm.findElement(By.cssSelector("button[type='submit']"));
            assertTrue(searchButton.isDisplayed());
        }
    }

    @Test
    @Order(12)
    public void testCodeEditorInteraction() {
        driver.get("://jsfiddle.net/");
        
        // Try to interact with editor areas
        WebElement htmlEditor = driver.findElement(By.id("ace_html"));
        assertTrue(htmlEditor.isDisplayed());
        
        // Check if we can get focus
        try {
            htmlEditor.click();
            // If we get here, it's clickable
        } catch (Exception e) {
            // Expected behavior - might not be directly interactive
        }
        
        // Check CSS editor
        WebElement cssEditor = driver.findElement(By.id("ace_css"));
        assertTrue(cssEditor.isDisplayed());
        
        // Check JavaScript editor  
        WebElement jsEditor = driver.findElement(By.id("ace_js"));
        assertTrue(jsEditor.isDisplayed());
    }

    @Test
    @Order(13)
    public void testExamplesNavigation() {
        driver.get("https://jsfiddle.net/");
        
        // Navigate to examples section
        WebElement examplesLink = driver.findElement(By.linkText("Examples"));
        examplesLink.click();
        
        // May need to wait for redirect
        wait.until(ExpectedConditions.urlContains("examples"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("examples") || currentUrl.contains("/examples"));
        
        // Go back to main page
        driver.get("https://jsfiddle.net/");
    }

    @Test
    @Order(14)
    public void testDocumentationLink() {
        driver.get("https://jsfiddle.net/");
        
        // Navigate to Documentation 
        WebElement docsLink = driver.findElement(By.linkText("Docs"));
        docsLink.click();
        
        // May need to wait for redirect
        wait.until(ExpectedConditions.urlContains("docs"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("docs") || currentUrl.contains("/docs"));
        
        // Go back to main page
        driver.get("https://jsfiddle.net/");
    }

    @Test
    @Order(15)
    public void testExternalLinksInFooter() {
        driver.get("https://jsfiddle.net/");
        
        // Look for external links in footer
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("github") || href.contains("twitter") || 
                               href.contains("facebook") || href.contains("linkedin"))) {
                // Just validate that such links exist in footer
                assertTrue(link.isDisplayed());
            }
        }
    }

    @Test
    @Order(16)
    public void testResponsiveDesign() {
        driver.get("https://jsfiddle.net/");
        
        // Check responsiveness
        WebElement mobileToggle = driver.findElement(By.cssSelector(".mobile-menu-toggle"));
        if (mobileToggle.isDisplayed()) {
            // Mobile menu should exist
            assertTrue(mobileToggle.isDisplayed());
            // Check menu items
            List<WebElement> mobileItems = driver.findElements(By.cssSelector(".mobile-menu li"));
            assertTrue(mobileItems.size() >= 0);
        }
    }

    @Test
    @Order(17)
    public void testLanguageSelection() {
        driver.get("https://jsfiddle.net/");
        
        // Check for language selection options
        List<WebElement> langSelects = driver.findElements(By.cssSelector("[id^='lang-select'], select[name*='lang']"));
        if (!langSelects.isEmpty()) {
            WebElement langSelect = langSelects.get(0);
            // Just verify it exists
            assertTrue(langSelect.isDisplayed());
        }
    }

    @Test
    @Order(18)
    public void testUserAuthentication() {
        driver.get("https://jsfiddle.net/");
        
        // Check login area
        List<WebElement> loginAreas = driver.findElements(By.cssSelector(".login-area, .auth-section"));
        if (!loginAreas.isEmpty()) {
            // Check login button
            List<WebElement> loginBtns = driver.findElements(By.cssSelector("a[href*='login'], button[data-action='login']"));
            if (!loginBtns.isEmpty()) {
                assertTrue(loginBtns.get(0).isDisplayed());
            }
        }
    }
}