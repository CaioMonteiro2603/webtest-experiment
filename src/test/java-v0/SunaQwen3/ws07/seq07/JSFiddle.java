package SunaQwen3.ws07.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final String EXTERNAL_LINKEDIN = "linkedin.com";
    private static final String EXTERNAL_TWITTER = "twitter.com";
    private static final String EXTERNAL_FACEBOOK = "facebook.com";

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
    public void testHomePageLoadsSuccessfully() {
        driver.get(BASE_URL);
        
        // Assert page title contains expected text
        assertTrue(driver.getTitle().toLowerCase().contains("jsfiddle"), 
                   "Page title should contain 'jsfiddle'");
        
        // Assert URL is correct
        assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"), 
                   "Current URL should contain jsfiddle.net");
        
        // Assert main editor container is present
        WebElement editorContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("div[id^='container']")));
        assertTrue(editorContainer.isDisplayed(), "Editor container should be visible");
    }

    @Test
    @Order(2)
    public void testRunButtonExecutesCode() {
        driver.get(BASE_URL);
        
        // Switch back to main content
        driver.switchTo().defaultContent();
        
        // Find and click the Run button
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[title='Run (Ctrl + Enter)']")));
        runButton.click();
        
        // Switch back to result frame and verify content
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
            By.cssSelector("iframe[name='result']")));
        
        // Check if body is present in result (basic execution)
        WebElement resultBody = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assertNotNull(resultBody, "Result frame body should be present after running");
        
        // Switch back to default content
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(3)
    public void testChangeLanguageToJavaScript() {
        driver.get(BASE_URL);
        
        // Click on HTML select dropdown
        WebElement htmlSelect = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("select[name='html']")));
        htmlSelect.click();
        
        // Select JavaScript option
        WebElement jsOption = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("select[name='html'] option[value='javascript']")));
        jsOption.click();
        
        // Verify selection changed
        assertEquals("javascript", htmlSelect.getAttribute("value"), 
                     "HTML language should be set to JavaScript");
    }

    @Test
    @Order(4)
    public void testExternalFooterLinksOpenInNewTab() {
        driver.get(BASE_URL);
        
        // Store original window handle
        String originalWindow = driver.getWindowHandle();
        
        // Test LinkedIn footer link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("footer a[href*='linkedin']")));
        linkedinLink.click();
        
        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains(EXTERNAL_LINKEDIN), 
                   "LinkedIn link should open a URL containing '" + EXTERNAL_LINKEDIN + "'");
        
        // Close current tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("footer a[href*='twitter']")));
        twitterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains(EXTERNAL_TWITTER), 
                   "Twitter link should open a URL containing '" + EXTERNAL_TWITTER + "'");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("footer a[href*='facebook']")));
        facebookLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains(EXTERNAL_FACEBOOK), 
                   "Facebook link should open a URL containing '" + EXTERNAL_FACEBOOK + "'");
        
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testSaveFiddleFunctionality() {
        driver.get(BASE_URL);
        
        // Wait for save button to be clickable
        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[title='Save (Ctrl + S)']")));
        
        // Click save button
        saveButton.click();
        
        // Wait for URL to change (indicating save)
        wait.until(webDriver -> {
            String url = webDriver.getCurrentUrl();
            return url.contains("/gists/") || url.contains("/user/") || url.contains("/save");
        });
        
        // Assert current URL has changed from base
        assertNotEquals(BASE_URL, driver.getCurrentUrl(), 
                        "URL should change after saving fiddle");
    }

    @Test
    @Order(6)
    public void testFullscreenModeToggle() {
        driver.get(BASE_URL);
        
        // Store initial URL
        String initialUrl = driver.getCurrentUrl();
        
        // Click fullscreen button
        WebElement fullscreenButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[title='Fullscreen (F11)']")));
        fullscreenButton.click();
        
        // Wait for URL to include /show/
        wait.until(ExpectedConditions.urlContains("/show/"));
        assertTrue(driver.getCurrentUrl().contains("/show/"), 
                   "URL should contain '/show/' in fullscreen mode");
        
        // Press Escape to exit fullscreen (simulate)
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        
        // Wait for navigation back to original
        wait.until(ExpectedConditions.urlToBe(initialUrl));
        assertEquals(initialUrl, driver.getCurrentUrl(), 
                     "Should return to original URL after exiting fullscreen");
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        
        // Find search input
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[type='search']")));
        
        // Enter search term
        searchInput.clear();
        searchInput.sendKeys("javascript");
        
        // Submit search
        searchInput.sendKeys(Keys.RETURN);
        
        // Wait for results or redirect
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/search/"),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".result-list li"))
        ));
        
        // Assert URL contains search or results are present
        assertTrue(driver.getCurrentUrl().contains("/search/") || 
                   driver.findElements(By.cssSelector(".result-list li")).size() > 0,
                   "Search should lead to search page or show results");
    }

    @Test
    @Order(8)
    public void testNavigationToAboutPage() {
        driver.get(BASE_URL);
        
        // Click on About link in footer
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("footer a[href='/about']")));
        aboutLink.click();
        
        // Wait for page to load
        wait.until(ExpectedConditions.titleContains("About"));
        
        // Assert we are on about page
        assertTrue(driver.getCurrentUrl().contains("/about"), 
                   "Should navigate to /about page");
        assertTrue(driver.getTitle().contains("About"), 
                   "Page title should contain 'About'");
    }

    @Test
    @Order(9)
    public void testLibrarySelectionDropdown() {
        driver.get(BASE_URL);
        
        // Open JavaScript libraries dropdown
        WebElement libDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("select[name='resources']")));
        libDropdown.click();
        
        // Get all options
        List<WebElement> options = libDropdown.findElements(By.tagName("option"));
        assertFalse(options.isEmpty(), "Library dropdown should have options");
        
        // Select first non-empty option
        for (WebElement option : options) {
            if (!option.getAttribute("value").isEmpty()) {
                option.click();
                break;
            }
        }
        
        // Reopen and verify selection
        libDropdown.click();
        String selectedValue = libDropdown.getAttribute("value");
        assertFalse(selectedValue.isEmpty(), "A library should be selected");
        
        // Click to close
        libDropdown.click();
    }

    @Test
    @Order(10)
    public void testResponsiveMenuToggle() {
        driver.get(BASE_URL);
        
        // Find menu toggle button (hamburger)
        List<WebElement> menuToggles = driver.findElements(By.cssSelector("button.navbar-toggle"));
        if (!menuToggles.isEmpty()) {
            WebElement menuToggle = menuToggles.get(0);
            
            // Click to open menu
            menuToggle.click();
            
            // Wait for collapse to be visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.navbar-collapse.in")));
            
            // Click to close
            menuToggle.click();
            
            // Wait for collapse to be hidden
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("div.navbar-collapse.in")));
        }
        // If no toggle exists, it's not a responsive layout - test passes vacuously
    }
}