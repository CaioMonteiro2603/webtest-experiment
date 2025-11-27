package SunaDeepSeek.ws07.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsFiddleWebTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

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
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL));
        
        // Verify main elements
        Assertions.assertTrue(driver.findElements(By.cssSelector(".editor")).size() > 0);
        Assertions.assertTrue(driver.findElements(By.cssSelector(".result")).size() > 0);
        Assertions.assertTrue(driver.findElements(By.cssSelector(".run")).size() > 0);
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Documentation link
        testNavLink(By.linkText("Documentation"), "https://doc.jsfiddle.net/");
        
        // Test Blog link
        testNavLink(By.linkText("Blog"), "https://blog.jsfiddle.net/");
        
        // Test Support link
        testNavLink(By.linkText("Support"), "https://support.jsfiddle.net/");
    }

    @Test
    @Order(3)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        
        // Test GitHub link
        testExternalLink(By.cssSelector("a[href*='github.com']"), "github.com");
    }

    @Test
    @Order(4)
    public void testEditorFunctionality() {
        driver.get(BASE_URL);
        
        // Clear default content
        WebElement htmlEditor = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".CodeMirror textarea")));
        htmlEditor.clear();
        htmlEditor.sendKeys("<h1>Test Heading</h1>");
        
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".run")));
        runButton.click();
        
        // Switch to result frame and verify output
        driver.switchTo().frame("result");
        WebElement heading = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("Test Heading", heading.getText());
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(5)
    public void testSaveFunctionality() {
        driver.get(BASE_URL);
        
        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".save")));
        saveButton.click();
        
        // Should redirect to login page
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"));
    }

    private void testNavLink(By locator, String expectedUrl) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        
        wait.until(ExpectedConditions.urlContains(expectedUrl));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedUrl));
        
        // Go back to home page for next test
        driver.get(BASE_URL);
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain and close
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}