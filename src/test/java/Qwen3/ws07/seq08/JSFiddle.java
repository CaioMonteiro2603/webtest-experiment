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
    public void testHomePageLoad() {
        driver.get("https://jsfiddle.net/");
        assertEquals("JSFiddle - Online JavaScript Editor", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"));
    }

    @Test
    @Order(2)
    public void testNavigationToEditor() {
        driver.get("https://jsfiddle.net/");
        WebElement editorLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Editor")));
        editorLink.click();
        assertTrue(driver.getCurrentUrl().contains("/editor"));
        assertEquals("JSFiddle - Editor", driver.getTitle());
    }

    @Test
    @Order(3)
    public void testFiddleCreation() {
        driver.get("https://jsfiddle.net/");
        WebElement createFiddleButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("new-fiddle")));
        createFiddleButton.click();
        
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
        WebElement createFiddleButton = driver.findElement(By.id("new-fiddle"));
        createFiddleButton.click();
        
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
        WebElement examplesLink = driver.findElement(By.linkText("Examples"));
        examplesLink.click();
        assertTrue(driver.getCurrentUrl().contains("/examples"));
        assertEquals("JSFiddle - Examples", driver.getTitle());
    }

    @Test
    @Order(6)
    public void testFiddleSearch() {
        driver.get("https://jsfiddle.net/");
        WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-input")));
        searchInput.sendKeys("jquery");
        
        WebElement searchButton = driver.findElement(By.id("search-button"));
        searchButton.click();
        
        List<WebElement> searchResults = driver.findElements(By.className("fiddle-item"));
        assertTrue(searchResults.size() > 0);
        
        for (WebElement result : searchResults) {
            assertTrue(result.isDisplayed());
        }
    }

    @Test
    @Order(7)
    public void testExternalLinksInFooter() {
        driver.get("https://jsfiddle.net/");
        WebElement footer = driver.findElement(By.className("footer"));
        List<WebElement> links = footer.findElements(By.tagName("a"));
        
        assertTrue(links.size() >= 3);
        
        String originalWindow = driver.getWindowHandle();
        
        for (int i = 0; i < links.size(); i++) {
            WebElement link = links.get(i);
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty()) {
                link.click();
                
                Set<String> windowHandles = driver.getWindowHandles();
                String newWindow = windowHandles.stream()
                        .filter(w -> !w.equals(originalWindow))
                        .findFirst()
                        .orElse(null);
                
                if (newWindow != null) {
                    driver.switchTo().window(newWindow);
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("github.com") || 
                               currentUrl.contains("twitter.com") || 
                               currentUrl.contains("linkedin.com") ||
                               currentUrl.contains("jsfiddle.net"));
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(8)
    public void testNavigationToAccount() {
        driver.get("https://jsfiddle.net/");
        WebElement accountLink = driver.findElement(By.linkText("Account"));
        accountLink.click();
        assertTrue(driver.getCurrentUrl().contains("/account"));
        assertEquals("JSFiddle - Account", driver.getTitle());
    }

    @Test
    @Order(9)
    public void testCodeEditingFunctionality() {
        driver.get("https://jsfiddle.net/");
        WebElement createFiddleButton = driver.findElement(By.id("new-fiddle"));
        createFiddleButton.click();
        
        WebElement cssTab = driver.findElement(By.id("tab-css"));
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