package deepseek.ws07.seq10;

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
    private static WebDriver driver;
    private static final String BASE_URL = "https://jsfiddle.net/";
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
    public void testPageLoad() {
        driver.get(BASE_URL);
        WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.className("CodeMirror")));
        Assertions.assertTrue(editor.isDisplayed(), "Editor not loaded");
    }

    @Test
    @Order(2)
    public void testRunButton() {
        driver.get(BASE_URL);
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("run")));
        runButton.click();
        
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("result")));
        Assertions.assertTrue(resultFrame.isDisplayed(), "Result frame not displayed after run");
    }

    @Test
    @Order(3)
    public void testLoginForm() {
        driver.get(BASE_URL);
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Login')]")));
        loginButton.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("login-form")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Login form not displayed");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("Twitter", "twitter.com");
        
        // Test GitHub link
        testExternalLink("GitHub", "github.com");
    }

    @Test
    @Order(5)
    public void testEditorTabs() {
        driver.get(BASE_URL);
        
        // Test HTML tab
        WebElement htmlTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'HTML')]")));
        htmlTab.click();
        WebElement htmlEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class, 'CodeMirror') and contains(@class, 'html')]")));
        Assertions.assertTrue(htmlEditor.isDisplayed(), "HTML editor not displayed");
        
        // Test CSS tab
        WebElement cssTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'CSS')]")));
        cssTab.click();
        WebElement cssEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class, 'CodeMirror') and contains(@class, 'css')]")));
        Assertions.assertTrue(cssEditor.isDisplayed(), "CSS editor not displayed");
        
        // Test JS tab
        WebElement jsTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'JS')]")));
        jsTab.click();
        WebElement jsEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class, 'CodeMirror') and contains(@class, 'javascript')]")));
        Assertions.assertTrue(jsEditor.isDisplayed(), "JS editor not displayed");
    }

    @Test
    @Order(6)
    public void testConsoleTab() {
        driver.get(BASE_URL);
        
        WebElement consoleTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Console')]")));
        consoleTab.click();
        
        WebElement consolePanel = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("console")));
        Assertions.assertTrue(consolePanel.isDisplayed(), "Console panel not displayed");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, '" + expectedDomain + "')]")));
        link.click();
        
        // Switch to new window if opened
        if (driver.getWindowHandles().size() > 1) {
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(mainWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                linkText + " link failed - wrong domain");
            driver.close();
            driver.switchTo().window(mainWindow);
        }
    }
}