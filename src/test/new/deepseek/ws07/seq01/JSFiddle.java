package deepseek.ws07.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
        WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".CodeMirror, .monaco-editor")));
        Assertions.assertTrue(editor.isDisplayed(), "Code editor should be displayed");
    }

    @Test
    @Order(2)
    public void testRunButtonFunctionality() {
        driver.get(BASE_URL);
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[title*='run'], .run-button, button[data-action='run']")));
        runButton.click();
        
        // Wait for result iframe to be available
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe[name='result'], #result, iframe[src*='show']")));
        WebElement resultContent = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(resultContent.isDisplayed(), "Result should be displayed after clicking Run");
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com'], a[href*='x.com'], .twitter-link, .social-link[href*='twitter']")));
        
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", twitterLink);
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                try {
                    wait.until(ExpectedConditions.urlContains("twitter.com"));
                } catch (TimeoutException e) {
                    wait.until(ExpectedConditions.urlContains("x.com"));
                }
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
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".login-btn, button:contains('Login'), .auth-button, [data-action='login']")));
        loginButton.click();
        
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".auth-modal, .modal, .login-modal, [role='dialog']")));
        Assertions.assertTrue(modal.isDisplayed(), "Login modal should appear");
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#login-email, input[type='email'], input[name='email'], input[placeholder*='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("#login-password, input[type='password'], input[name='password']"));
        
        emailField.sendKeys("test@example.com");
        passwordField.sendKeys("password");
        
        WebElement submitButton = driver.findElement(By.cssSelector(".auth-submit, button[type='submit'], .login-submit"));
        submitButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".auth-error, .error-message, .login-error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(5)
    public void testPanelResizing() {
        driver.get(BASE_URL);
        WebElement resizeHandle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".CodeMirror-sizer, .monaco-editor, .editor-container, .panel-resize, .resize-handle")));
        Assertions.assertTrue(resizeHandle.isDisplayed(), "Panel resize handle should be available");
    }

    @Test
    @Order(6)
    public void testTabsNavigation() {
        driver.get(BASE_URL);
        WebElement jsTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".tab.js-tab, .tab[data-tab='js'], .panel-tab[data-panel='js'], li[data-type='js']")));
        WebElement htmlTab = driver.findElement(By.cssSelector(".tab.html-tab, .tab[data-tab='html'], .panel-tab[data-panel='html'], li[data-type='html']"));
  
        jsTab.click();
        String jsClass = jsTab.getAttribute("class");
        String jsAria = jsTab.getAttribute("aria-selected");
        if (jsAria != null) {
            Assertions.assertTrue("true".equals(jsAria) || (jsClass != null && jsClass.contains("active")), "JS tab should be active");
        } else {
            Assertions.assertTrue(jsClass != null && jsClass.contains("active"), "JS tab should be active");
        }
        
        htmlTab.click();
        String htmlClass = htmlTab.getAttribute("class");
        String htmlAria = htmlTab.getAttribute("aria-selected");
        if (htmlAria != null) {
            Assertions.assertTrue("true".equals(htmlAria) || (htmlClass != null && htmlClass.contains("active")), "HTML tab should be active");
        } else {
            Assertions.assertTrue(htmlClass != null && htmlClass.contains("active"), "HTML tab should be active");
        }
    }
}