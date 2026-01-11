package Qwen3.ws07.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

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
    public void testPageLoad() {
        driver.get("https://jsfiddle.net/");
        assertEquals("JSFiddle - Online JavaScript Editor", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"));
    }

    @Test
    @Order(2)
    public void testNavigationMenu() {
        driver.get("https://jsfiddle.net/");
        
        // Click Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().contains("/"));
        
        // Click Examples link
        driver.get("https://jsfiddle.net/");
        WebElement examplesLink = driver.findElement(By.linkText("Examples"));
        examplesLink.click();
        wait.until(ExpectedConditions.urlContains("/examples"));
        assertTrue(driver.getCurrentUrl().contains("/examples"));
        
        // Navigate back to home
        driver.get("https://jsfiddle.net/");
        
        // Click Documentation link
        WebElement documentationLink = driver.findElement(By.linkText("Documentation"));
        documentationLink.click();
        wait.until(ExpectedConditions.urlContains("/docs"));
        assertTrue(driver.getCurrentUrl().contains("/docs"));
    }

    @Test
    @Order(3)
    public void testCreateNewFiddle() {
        driver.get("https://jsfiddle.net/");
        
        // Click "Create New Fiddle" button
        WebElement createFiddleButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Create New Fiddle")));
        createFiddleButton.click();
        
        // Wait for fiddle editor to load
        WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("editor")));
        assertTrue(editor.isDisplayed());
        
        // Verify editor elements
        assertTrue(driver.findElement(By.id("js")).isDisplayed());
        assertTrue(driver.findElement(By.id("css")).isDisplayed());
        assertTrue(driver.findElement(By.id("html")).isDisplayed());
        assertTrue(driver.findElement(By.id("result")).isDisplayed());
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get("https://jsfiddle.net/");
        
        // Click GitHub link
        WebElement githubLink = driver.findElement(By.cssSelector("[href*='github']"));
        githubLink.click();
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("github.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
        
        // Click Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("[href*='twitter']"));
        twitterLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
        
        // Click Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("[href*='facebook']"));
        facebookLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }

    @Test
    @Order(5)
    public void testExamplesPage() {
        driver.get("https://jsfiddle.net/examples/");
        
        // Verify examples page loaded
        assertEquals("Examples - JSFiddle", driver.getTitle());
        
        // Check that examples are displayed
        WebElement examplesContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".examples")));
        assertTrue(examplesContainer.isDisplayed());
        
        // Check if there are example fiddles
        if (driver.findElements(By.cssSelector(".example-fiddle")).size() > 0) {
            WebElement firstExample = driver.findElement(By.cssSelector(".example-fiddle"));
            assertTrue(firstExample.isDisplayed());
        }
    }

    @Test
    @Order(6)
    public void testSearchFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Search for a term
        WebElement searchField = wait.until(ExpectedConditions.elementToBeClickable(By.id("search")));
        searchField.sendKeys("javascript");
        
        // Submit search
        WebElement searchButton = driver.findElement(By.cssSelector("button[type='submit']"));
        searchButton.click();
        
        // Check if search results are displayed
        WebElement searchResults = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-results")));
        assertTrue(searchResults.isDisplayed());
    }

    @Test
    @Order(7)
    public void testEditorFunctionality() {
        driver.get("https://jsfiddle.net/");
        
        // Click Create New Fiddle
        WebElement createFiddleButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Create New Fiddle")));
        createFiddleButton.click();
        
        // Verify editor is displayed
        WebElement jsEditor = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("js")));
        assertTrue(jsEditor.isDisplayed());
        
        // Input simple JavaScript code
        jsEditor.sendKeys("console.log('Hello from JSFiddle');\nalert('JS Fiddle Test');");
        
        // Click Run button
        WebElement runButton = driver.findElement(By.cssSelector("button[title='Run']"));
        runButton.click();
        
        // Check that result is updated
        WebElement resultFrame = driver.findElement(By.id("result"));
        assertTrue(resultFrame.isDisplayed());
    }

    @Test
    @Order(8)
    public void testResponsiveNavigation() {
        driver.get("https://jsfiddle.net/");
        
        // Verify navigation bar is present
        WebElement navBar = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("nav")));
        assertTrue(navBar.isDisplayed());
        
        // Check that key navigation items are present
        assertTrue(driver.findElement(By.linkText("Home")).isDisplayed());
        assertTrue(driver.findElement(By.linkText("Examples")).isDisplayed());
        assertTrue(driver.findElement(By.linkText("Documentation")).isDisplayed());
        assertTrue(driver.findElement(By.linkText("Create New Fiddle")).isDisplayed());
    }
}