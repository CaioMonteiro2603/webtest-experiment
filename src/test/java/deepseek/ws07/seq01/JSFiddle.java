package deepseek.ws07.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddleTest {

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
    public void testEditorLoading() {
        driver.get(BASE_URL);
        WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".CodeMirror")));
        Assertions.assertTrue(editor.isDisplayed(), "Code editor should be displayed");
    }

    @Test
    @Order(2)
    public void testRunButtonFunctionality() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("result")));
        driver.switchTo().defaultContent();
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("run")));
        runButton.click();
        
        // Wait for result iframe to reload
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("result")));
        WebElement resultContent = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(resultContent.isDisplayed(), "Result should be displayed after clicking Run");
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                wait.until(ExpectedConditions.urlContains("twitter.com"));
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(4)
    public void testLoginModal() {
        driver.get(BASE_URL);
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".login-btn")));
        loginButton.click();
        
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".auth-modal")));
        Assertions.assertTrue(modal.isDisplayed(), "Login modal should appear");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-email")));
        WebElement passwordField = driver.findElement(By.id("login-password"));
        
        emailField.sendKeys("test@example.com");
        passwordField.sendKeys("password");
        
        WebElement submitButton = driver.findElement(By.cssSelector(".auth-submit"));
        submitButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".auth-error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(5)
    public void testPanelResizing() {
        driver.get(BASE_URL);
        WebElement resizeHandle = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".CodeMirror-sizer")));
        Assertions.assertTrue(resizeHandle.isDisplayed(), "Panel resize handle should be available");
    }

    @Test
    @Order(6)
    public void testTabsNavigation() {
        driver.get(BASE_URL);
        WebElement jsTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tab.js-tab")));
        WebElement htmlTab = driver.findElement(By.cssSelector(".tab.html-tab"));
        WebElement cssTab = driver.findElement(By.cssSelector(".tab.css-tab"));
        WebElement resultTab = driver.findElement(By.cssSelector(".tab.result-tab"));
        
        jsTab.click();
        Assertions.assertTrue(jsTab.getAttribute("class").contains("active"), "JS tab should be active");
        
        htmlTab.click();
        Assertions.assertTrue(htmlTab.getAttribute("class").contains("active"), "HTML tab should be active");
    }
}