package deepseek.ws07.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoading() {
        driver.get(BASE_URL);
        WebElement editor = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#editor")));
        Assertions.assertTrue(editor.isDisplayed(), "Editor should be visible on home page");
    }

    @Test
    @Order(2)
    public void testLoginButton() {
        driver.get(BASE_URL);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/login']")));
        loginBtn.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".login-form, .auth-modal, [class*='login']")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Login form should appear");
    }

    @Test
    @Order(3)
    public void testCodeExecution() {
        driver.get(BASE_URL);
        
        // Wait for page to fully load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#editor")));
        
        // Click into HTML panel using a more robust selector
        WebElement htmlPanel = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".panel-html")));
        htmlPanel.click();
        
        // Clear and enter new code
        WebElement htmlTextarea = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#html textarea")));
        htmlTextarea.clear();
        htmlTextarea.sendKeys("<h1>Test</h1>");
        
        // Click Run button
        WebElement runBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#run")));
        runBtn.click();
        
        // Check result in output frame
        driver.switchTo().frame("result");
        WebElement output = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.tagName("h1")));
        Assertions.assertEquals("Test", output.getText(), "Output should match entered HTML");
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(4)
    public void testPanelResizing() {
        driver.get(BASE_URL);
        
        // Look for any resize handle with common class names
        WebElement resizeHandle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".resize-handle, .vertical-resize-handle, .gutter, [class*='resize']")));
        
        // Just verify element exists and is displayed
        Assertions.assertTrue(resizeHandle.isDisplayed(), "Resize handle should be visible");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test Documentation link with updated selector
        WebElement docLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='docs'], a[href*='documentation']")));
        docLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("docs") || driver.getCurrentUrl().contains("documentation"),
            "Should open documentation in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testNavigationTabs() throws InterruptedException {
        driver.get(BASE_URL);
        
        // Look for navigation links in header or nav element
        WebElement aboutTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("nav a[href*='about'], header a[href*='about'], .nav a[href*='about']")));
        aboutTab.click();
        
        // Wait for page content or navigation change
        Thread.sleep(1000);
        Assertions.assertTrue(driver.getCurrentUrl().contains("about") || 
            driver.findElements(By.cssSelector(".about-content, [class*='about']")).size() > 0,
            "About page content should be visible");
        
        // Test Blog tab
        WebElement blogTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("nav a[href*='blog'], header a[href*='blog'], .nav a[href*='blog']")));
        blogTab.click();
        
        Thread.sleep(1000);
        Assertions.assertTrue(driver.getCurrentUrl().contains("blog") || 
            driver.findElements(By.cssSelector(".blog-content, [class*='blog']")).size() > 0,
            "Blog page content should be visible");
    }

    @Test
    @Order(7)
    public void testEmbedOption() {
        driver.get(BASE_URL);
        
        // Look for embed/share button with multiple possible selectors
        WebElement embedBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[class*='embed'], .share-btn, [title*='embed'], [title*='share']")));
        embedBtn.click();
        
        // Look for modal or popup with embed options
        WebElement embedModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".embed-modal, .share-modal, .modal, [class*='embed']")));
        Assertions.assertTrue(embedModal.isDisplayed(), "Embed modal should appear");
    }
}