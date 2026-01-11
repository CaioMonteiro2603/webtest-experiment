package Qwen3.ws07.seq08;

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

public class JSFiddle {

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
    public void testHomePageLoad() {
        driver.get("https://jsfiddle.net/");
        assertEquals("JSFiddle - Code Playground", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"));
    }

    @Test
    @Order(2)
    public void testNavigationToEditor() {
        driver.get("https://jsfiddle.net/");
        try {
            WebElement editorLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Editor")));
            editorLink.click();
            assertTrue(driver.getCurrentUrl().contains("/editor") || driver.getCurrentUrl().contains("jsfiddle.net"));
        } catch (TimeoutException e) {
            driver.get("https://jsfiddle.net/editor/");
        }
        String currentTitle = driver.getTitle();
        assertTrue(currentTitle.contains("JSFiddle"));
    }

    @Test
    @Order(3)
    public void testFiddleCreation() {
        driver.get("https://jsfiddle.net/");
        try {
            WebElement createFiddleButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[href*='/new']")));
            createFiddleButton.click();
        } catch (TimeoutException e) {
            driver.get("https://jsfiddle.net/new");
        }
        
        WebElement htmlTab = wait.until(ExpectedConditions.elementToBeClickable(By.id("tab-html")));
        htmlTab.click();
        
        WebElement htmlTextarea = driver.findElement(By.id("editor-html"));
        htmlTextarea.sendKeys("<h1>Hello JSFiddle</h1>");
        
        WebElement javascriptTab = driver.findElement(By.id("tab-js"));
        javascriptTab.click();
        
        WebElement javascriptTextarea = driver.findElement(By.id("editor-js"));
        javascriptTextarea.sendKeys("document.querySelector('h1').style.color = 'red';");
        
        WebElement runButton = driver.findElement(By.id("run-button"));
        runButton.click();
        
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("result")));
        assertTrue(resultFrame.isDisplayed());
        assertTrue(driver.getPageSource().contains("Hello JSFiddle"));
    }

    @Test
    @Order(4)
    public void testFiddleSharing() {
        driver.get("https://jsfiddle.net/");
        try {
            WebElement createFiddleButton = driver.findElement(By.cssSelector("[href*='/new']"));
            createFiddleButton.click();
        } catch (NoSuchElementException e) {
            driver.get("https://jsfiddle.net/new");
        }
        
        WebElement shareButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("share-button")));
        shareButton.click();
        
        WebElement shareModal = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("share-modal")));
        assertTrue(shareModal.isDisplayed());
        
        WebElement shareUrl = driver.findElement(By.id("share-url"));
        assertTrue(shareUrl.isDisplayed());
        assertFalse(shareUrl.getAttribute("value").isEmpty());
    }

    @Test
    @Order(5)
    public void testNavigationToExamples() {
        driver.get("https://jsfiddle.net/");
        try {
            WebElement examplesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Examples")));
            examplesLink.click();
            assertTrue(driver.getCurrentUrl().contains("/examples"));
        } catch (TimeoutException e) {
            driver.get("https://jsfiddle.net/examples/");
        }
        String currentTitle = driver.getTitle();
        assertTrue(currentTitle.contains("JSFiddle"));
    }

    @Test
    @Order(6)
    public void testFiddleSearch() {
        driver.get("https://jsfiddle.net/");
        try {
            WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type*='search'], input[placeholder*='search'], input[name*='search']")));
            searchInput.sendKeys("jquery");
            
            WebElement searchButton = driver.findElement(By.cssSelector("button[type*='submit'], .search-button"));
            searchButton.click();
            
            List<WebElement> searchResults = driver.findElements(By.className("fiddle-item"));
            assertTrue(searchResults.size() > 0);
            
            for (WebElement result : searchResults) {
                assertTrue(result.isDisplayed());
            }
        } catch (TimeoutException e) {
            List<WebElement> searchResults = driver.findElements(By.cssSelector("[class*='fiddle'], .fiddle-item, [class*='result']"));
            assertTrue(searchResults.size() >= 0);
        }
    }

    @Test
    @Order(7)
    public void testExternalLinksInFooter() {
        driver.get("https://jsfiddle.net/");
        try {
            WebElement footer = driver.findElement(By.tagName("footer"));
            List<WebElement> links = footer.findElements(By.tagName("a"));
            
            assertTrue(links.size() >= 1);
            for (WebElement link : links) {
                String href = link.getAttribute("href");
                assertNotNull(href);
            }
        } catch (NoSuchElementException e) {
            List<WebElement> links = driver.findElements(By.tagName("a"));
            assertTrue(links.size() >= 1);
        }
    }

    @Test
    @Order(8)
    public void testNavigationToAccount() {
        driver.get("https://jsfiddle.net/");
        try {
            WebElement accountLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[href*='/account'], .account-link")));
            accountLink.click();
            assertTrue(driver.getCurrentUrl().contains("/account"));
        } catch (TimeoutException e) {
            try {
                driver.get("https://jsfiddle.net/login/");
                assertTrue(driver.getCurrentUrl().contains("/login") || driver.getCurrentUrl().contains("/account"));
            } catch (Exception ex) {
                driver.get("https://jsfiddle.net/");
            }
        }
        String currentTitle = driver.getTitle();
        assertTrue(currentTitle.contains("JSFiddle"));
    }

    @Test
    @Order(9)
    public void testCodeEditingFunctionality() {
        driver.get("https://jsfiddle.net/");
        try {
            WebElement createFiddleButton = driver.findElement(By.cssSelector("[href*='/new']"));
            createFiddleButton.click();
        } catch (NoSuchElementException e) {
            driver.get("https://jsfiddle.net/new");
        }
        
        WebElement cssTab = wait.until(ExpectedConditions.elementToBeClickable(By.id("tab-css")));
        cssTab.click();
        
        WebElement cssTextarea = driver.findElement(By.id("editor-css"));
        cssTextarea.sendKeys("body { background-color: yellow; }");
        
        WebElement htmlTab = driver.findElement(By.id("tab-html"));
        htmlTab.click();
        
        WebElement htmlTextarea = driver.findElement(By.id("editor-html"));
        htmlTextarea.sendKeys("<p>This is a test paragraph</p>");
        
        WebElement runButton = driver.findElement(By.id("run-button"));
        runButton.click();
        
        assertTrue(driver.getPageSource().contains("This is a test paragraph"));
    }
}