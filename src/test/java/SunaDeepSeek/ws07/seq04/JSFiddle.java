package SunaDeepSeek.ws07.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        
        WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".CodeMirror")));
        Assertions.assertTrue(editor.isDisplayed(), "Editor should be visible");
    }

    @Test
    @Order(2)
    public void testEditorComponents() {
        driver.get(BASE_URL);
        
        List<WebElement> panels = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".CodeMirror")));
        Assertions.assertEquals(4, panels.size(), "Should have 4 editor panels (HTML, CSS, JS, Result)");
    }

    @Test
    @Order(3)
    public void testRunButtonFunctionality() {
        driver.get(BASE_URL);
        
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("run")));
        runButton.click();
        
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#result")));
        Assertions.assertTrue(resultFrame.isDisplayed(), "Result frame should be visible after run");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("Twitter", "twitter.com");
        
        // Test Blog link
        testExternalLink("Blog", "jsfiddle.net/blog");
    }

    @Test
    @Order(5)
    public void testMenuOptions() {
        driver.get(BASE_URL);
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".menu-button")));
        menuButton.click();
        
        // Test Documentation link
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Documentation")));
        docsLink.click();
        wait.until(ExpectedConditions.urlContains("docs"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("docs"), 
            "Should navigate to documentation page");
    }

    @Test
    @Order(6)
    public void testSaveFunctionality() {
        driver.get(BASE_URL);
        
        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("save")));
        saveButton.click();
        
        WebElement loginModal = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".login-modal")));
        Assertions.assertTrue(loginModal.isDisplayed(), 
            "Login modal should appear when trying to save without login");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText(linkText)));
        link.click();
        
        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify domain
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "External link should open " + expectedDomain);
        
        // Close tab and switch back
        driver.close();
        driver.switchTo().window(mainWindow);
    }
}