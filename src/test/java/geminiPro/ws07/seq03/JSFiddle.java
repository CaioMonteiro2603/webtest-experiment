package geminiPRO.ws07.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

/**
 * A complete JUnit 5 test suite for the JSFiddle website using Selenium WebDriver
 * with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddleE2ETest {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://jsfiddle.net/";

    // --- Selenium WebDriver ---
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private static final By RUN_BUTTON = By.id("run");
    private static final By SAVE_BUTTON = By.id("save");
    private static final By SIGN_IN_BUTTON = By.id("login");
    private static final By HELP_DROPDOWN = By.xpath("//a[normalize-space()='Help']");
    private static final By BLOG_LINK = By.linkText("Blog");
    
    // iframe Locators
    private static final By HTML_PANEL_IFRAME = By.cssSelector("#panel_html iframe[name^='html']");
    private static final By CSS_PANEL_IFRAME = By.cssSelector("#panel_css iframe[name^='css']");
    private static final By RESULT_IFRAME = By.name("result");

    // Locators within iframes
    private static final By CODE_MIRROR_INPUT_AREA = By.cssSelector(".CodeMirror-scroll");
    private static final By LOGIN_MODAL_IFRAME = By.cssSelector("iframe.modal-iframe");
    private static final By LOGIN_MODAL_EMAIL_INPUT = By.id("id_login");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        // JSFiddle can be slow to load its complex UI
        options.setPageLoadTimeout(Duration.ofSeconds(20)); 
        driver = new FirefoxDriver(options);
        // A longer wait is prudent for this dynamic application
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    /**
     * Helper method to close the initial "What's New" modal if it appears.
     */
    private void closeWelcomeModalIfPresent() {
        // Use a short, temporary wait to avoid slowing down tests if the modal isn't there.
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
        try {
            WebElement closeButton = shortWait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='tour-popover']//button[text()='Close']")));
            closeButton.click();
            wait.until(ExpectedConditions.invisibilityOf(closeButton));
        } catch (Exception e) {
            // Modal did not appear, which is fine.
        }
    }
    
    @Test
    @Order(1)
    void testHomePageLoadAndInitialState() {
        driver.get(BASE_URL);
        Assertions.assertEquals("JSFiddle - Code Playground", driver.getTitle(), "Page title is incorrect.");
        
        closeWelcomeModalIfPresent();
        
        Assertions.assertTrue(wait.until(ExpectedConditions.elementToBeClickable(RUN_BUTTON)).isDisplayed(), "Run button is not displayed.");
        Assertions.assertTrue(driver.findElement(SAVE_BUTTON).isDisplayed(), "Save button is not displayed.");
        Assertions.assertTrue(driver.findElement(SIGN_IN_BUTTON).isDisplayed(), "Sign in button is not displayed.");
    }
    
    @Test
    @Order(2)
    void testCoreFunctionality_CodeRunAndVerify() {
        driver.get(BASE_URL);
        closeWelcomeModalIfPresent();

        String htmlContent = "<h1>Hello, Gemini!</h1>";
        String cssContent = "h1 { color: rgb(0, 0, 255); }"; // Use RGB for easier comparison

        // Enter HTML
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(HTML_PANEL_IFRAME));
        WebElement htmlEditor = wait.until(ExpectedConditions.elementToBeClickable(CODE_MIRROR_INPUT_AREA));
        // Use JavascriptExecutor for reliable input into CodeMirror
        ((JavascriptExecutor) driver).executeScript("arguments[0].CodeMirror.setValue(arguments[1]);", htmlEditor, htmlContent);
        driver.switchTo().defaultContent();

        // Enter CSS
        driver.switchTo().frame(driver.findElement(CSS_PANEL_IFRAME));
        WebElement cssEditor = wait.until(ExpectedConditions.elementToBeClickable(CODE_MIRROR_INPUT_AREA));
        ((JavascriptExecutor) driver).executeScript("arguments[0].CodeMirror.setValue(arguments[1]);", cssEditor, cssContent);
        driver.switchTo().defaultContent();

        // Run the fiddle
        driver.findElement(RUN_BUTTON).click();

        // Verify the output in the result frame
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(RESULT_IFRAME));
        WebElement resultHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        
        // Assert text content
        Assertions.assertEquals("Hello, Gemini!", resultHeader.getText(), "The H1 text in the result is incorrect.");
        
        // Assert CSS color
        String colorValue = resultHeader.getCssValue("color");
        Color blue = Color.fromString("blue");
        Assertions.assertEquals(blue.asRgba(), Color.fromString(colorValue).asRgba(), "The H1 color in the result is not blue.");
        
        driver.switchTo().defaultContent();
    }
    
    @Test
    @Order(3)
    void testSaveFunctionality() {
        driver.get(BASE_URL);
        closeWelcomeModalIfPresent();
        
        String initialUrl = driver.getCurrentUrl();
        
        // Save the default empty fiddle
        wait.until(ExpectedConditions.elementToBeClickable(SAVE_BUTTON)).click();
        
        // Wait for the URL to change, indicating a save operation has completed
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(initialUrl)));
        
        String newUrl = driver.getCurrentUrl();
        Assertions.assertTrue(newUrl.length() > initialUrl.length(), "URL did not change after clicking Save.");
        Assertions.assertFalse(newUrl.endsWith("/"), "Saved URL should have a unique hash and not be the base URL.");
    }
    
    @Test
    @Order(4)
    void testLoginModalInteraction() {
        driver.get(BASE_URL);
        closeWelcomeModalIfPresent();

        wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_BUTTON)).click();

        // Wait for the modal iframe and switch to it
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(LOGIN_MODAL_IFRAME));

        // Attempt an invalid login
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_MODAL_EMAIL_INPUT)).sendKeys("invalid@test.com");
        driver.findElement(By.id("id_password")).sendKeys("invalidpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Assert error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".errorlist")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message was not displayed for invalid login attempt.");
        Assertions.assertTrue(errorMessage.getText().contains("Please enter a correct username and password."), "Error message text is incorrect.");

        driver.switchTo().defaultContent();

        // Close the modal
        wait.until(ExpectedConditions.elementToBeClickable(By.className("close-icon"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOGIN_MODAL_IFRAME));
    }

    @Test
    @Order(5)
    void testExternalHelpLink() {
        driver.get(BASE_URL);
        closeWelcomeModalIfPresent();
        
        String originalWindow = driver.getWindowHandle();
        
        wait.until(ExpectedConditions.elementToBeClickable(HELP_DROPDOWN)).click();
        wait.until(ExpectedConditions.elementToBeClickable(BLOG_LINK)).click();

        // Wait for the new window/tab and switch to it
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);

        // Assert the URL of the new tab
        wait.until(ExpectedConditions.urlContains("medium.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("medium.com/jsfiddle"), "The 'Blog' link did not navigate to Medium.");

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Did not return to the original JSFiddle page.");
    }
}