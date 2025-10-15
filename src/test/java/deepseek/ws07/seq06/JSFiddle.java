package deepseek.ws07.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsFiddleTest {

    private static WebDriver driver;
    private static final String BASE_URL = "https://jsfiddle.net/";
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
        driver.get(BASE_URL);
        WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".CodeMirror")));
        Assertions.assertTrue(editor.isDisplayed(), "JSFiddle editor should be loaded");
    }

    @Test
    @Order(2)
    public void testRunButton() {
        driver.get(BASE_URL);
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#run")));
        runButton.click();
        
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#result")));
        Assertions.assertTrue(resultFrame.isDisplayed(), "Result frame should be visible after run");
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        githubLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("github.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(4)
    public void testEditorTabs() {
        driver.get(BASE_URL);
        
        WebElement htmlTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#tabs li:nth-child(1)")));
        htmlTab.click();
        WebElement htmlEditor = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".CodeMirror-code")));
        Assertions.assertTrue(htmlEditor.isDisplayed(), "HTML editor should be visible");

        WebElement cssTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#tabs li:nth-child(2)")));
        cssTab.click();
        WebElement cssEditor = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".CodeMirror-code")));
        Assertions.assertTrue(cssEditor.isDisplayed(), "CSS editor should be visible");

        WebElement jsTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#tabs li:nth-child(3)")));
        jsTab.click();
        WebElement jsEditor = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".CodeMirror-code")));
        Assertions.assertTrue(jsEditor.isDisplayed(), "JS editor should be visible");
    }

    @Test
    @Order(5)
    public void testConsolePanel() {
        driver.get(BASE_URL);
        WebElement consoleTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#tabs li:nth-child(4)")));
        consoleTab.click();
        
        WebElement consolePanel = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".console-panel")));
        Assertions.assertTrue(consolePanel.isDisplayed(), "Console panel should be visible");
    }

    @Test
    @Order(6)
    public void testSaveButton() {
        driver.get(BASE_URL);
        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#save")));
        saveButton.click();
        
        WebElement loginPrompt = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".login-dialog")));
        Assertions.assertTrue(loginPrompt.isDisplayed(), "Login prompt should appear when saving");
    }
}