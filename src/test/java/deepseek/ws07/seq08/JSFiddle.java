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
            By.cssSelector(".login-btn")));
        loginBtn.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".auth-modal")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Login form should appear");
    }

    @Test
    @Order(3)
    public void testCodeExecution() {
        driver.get(BASE_URL);
        
        // Clear default code
        WebElement htmlPane = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#html .CodeMirror")));
        htmlPane.click();
        driver.findElement(By.cssSelector("#html .CodeMirror textarea")).sendKeys("<h1>Test</h1>");
        
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
        WebElement resizeHandle = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".vertical-resize-handle")));
        
        // Drag action would be complex to simulate, so we just verify element exists
        Assertions.assertTrue(resizeHandle.isDisplayed(), "Resize handle should be visible");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test Documentation link
        WebElement docLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='docs']")));
        docLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("docs.jsfiddle.net"),
            "Should open documentation in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testNavigationTabs() {
        driver.get(BASE_URL);
        
        // Test About tab
        WebElement aboutTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='about']")));
        aboutTab.click();
        
        WebElement aboutContent = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".about-content")));
        Assertions.assertTrue(aboutContent.isDisplayed(), "About page content should be visible");
        
        // Test Blog tab
        WebElement blogTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='blog']")));
        blogTab.click();
        
        WebElement blogContent = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".blog-content")));
        Assertions.assertTrue(blogContent.isDisplayed(), "Blog page content should be visible");
    }

    @Test
    @Order(7)
    public void testEmbedOption() {
        driver.get(BASE_URL);
        
        WebElement embedBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".embed-btn")));
        embedBtn.click();
        
        WebElement embedModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".embed-modal")));
        Assertions.assertTrue(embedModal.isDisplayed(), "Embed modal should appear");
    }
}