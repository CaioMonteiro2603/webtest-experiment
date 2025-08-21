package geminiPro.ws07.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * A comprehensive JUnit 5 test suite for the JSFiddle online IDE.
 * This suite uses Selenium WebDriver with Firefox in headless mode to test core functionalities
 * like running code snippets, interacting with the UI (menus, settings), and verifying external links.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddleTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jsfiddle.net/";

    // --- Locators ---
    private static final By RUN_BUTTON = By.id("run");
    private static final By RESULT_IFRAME = By.name("result");
    private static final By HTML_EDITOR_CONTAINER = By.cssSelector(".window.html .CodeMirror");
    private static final By BURGER_MENU_BUTTON = By.id("show-mobile-menu");
    private static final By NEW_FIDDLE_LINK = By.linkText("New fiddle");
    private static final By SETTINGS_BUTTON = By.cssSelector("a[title='Settings']");
    private static final By SETTINGS_MODAL = By.id("options");
    private static final By THEME_DROPDOWN = By.id("option-theme");
    private static final By MODAL_CLOSE_BUTTON = By.cssSelector("#options .actions a.button");
    private static final By FOOTER_DOCS_LINK = By.linkText("Docs");
    private static final By FOOTER_GITHUB_LINK = By.linkText("GitHub");
    private static final By SIGN_IN_BUTTON = By.linkText("Sign in");
    private static final By SIGN_UP_BUTTON = By.linkText("Sign up");
    private static final By LOGIN_FORM = By.id("login-form");
    private static final By SIGNUP_FORM = By.id("signup-form");
    private static final By COOKIE_CONSENT_BUTTON = By.xpath("//button/p[text()='AGREE']");


    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new Firefox-Driver(options);
        // JSFiddle can be slow to load, a slightly longer wait is beneficial
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @BeforeEach
    void goToBaseUrlAndHandleConsent() {
        driver.get(BASE_URL);
        // Handle the GDPR consent banner which can obscure other elements
        try {
            wait.withTimeout(Duration.ofSeconds(5));
            WebElement consentButton = wait.until(ExpectedConditions.elementToBeClickable(COOKIE_CONSENT_BUTTON));
            consentButton.click();
        } catch (TimeoutException e) {
            // Consent banner did not appear, which is fine.
        } finally {
            wait.withTimeout(Duration.ofSeconds(15)); // Reset to default wait
        }
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Helper to inject text into a CodeMirror editor instance since sendKeys can be unreliable.
     * @param editorLocator Locator for the .CodeMirror element wrapper.
     * @param code The string of code to inject.
     */
    private void setCodeMirrorText(By editorLocator, String code) {
        WebElement codeMirrorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(editorLocator));
        String script = "arguments[0].CodeMirror.setValue(arguments[1]);";
        ((JavascriptExecutor) driver).executeScript(script, codeMirrorElement, code);
    }
    
    private void validateExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "URL should contain '" + expectedDomain + "'. Actual: " + driver.getCurrentUrl());
        
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    @DisplayName("Should load the page and verify the initial editor layout")
    void testPageLoadAndInitialLayout() {
        Assertions.assertEquals("JSFiddle - Code Playground", driver.getTitle(), "Page title is incorrect.");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(HTML_EDITOR_CONTAINER)).isDisplayed(), "HTML editor panel is not visible.");
        Assertions.assertTrue(driver.findElement(By.cssSelector(".window.css .CodeMirror")).isDisplayed(), "CSS editor panel is not visible.");
        Assertions.assertTrue(driver.findElement(By.cssSelector(".window.js .CodeMirror")).isDisplayed(), "JavaScript editor panel is not visible.");
    }

    @Test
    @Order(2)
    @DisplayName("Should run a simple HTML snippet and verify the result in the iframe")
    void testRunSimpleFiddle() {
        String htmlContent = "<h1>Hello Fiddle!</h1>";
        setCodeMirrorText(HTML_EDITOR_CONTAINER, htmlContent);

        wait.until(ExpectedConditions.elementToBeClickable(RUN_BUTTON)).click();

        // Wait for the iframe to be available and switch to it
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(RESULT_IFRAME));

        // Verify the content inside the iframe
        WebElement resultHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("Hello Fiddle!", resultHeader.getText(), "The result in the iframe is incorrect.");

        // Switch back to the main document context
        driver.switchTo().defaultContent();
    }
    
    @Test
    @Order(3)
    @DisplayName("Should open the settings modal and change the theme")
    void testSettingsModalInteraction() {
        wait.until(ExpectedConditions.elementToBeClickable(SETTINGS_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SETTINGS_MODAL));
        
        Select themeSelect = new Select(driver.findElement(THEME_DROPDOWN));
        themeSelect.selectByValue("dark");

        // Assert that the body tag gets the correct theme class
        WebElement body = driver.findElement(By.tagName("body"));
        wait.until(ExpectedConditions.attributeContains(body, "class", "theme-dark"));
        Assertions.assertTrue(body.getAttribute("class").contains("theme-dark"), "Theme was not changed to dark.");

        wait.until(ExpectedConditions.elementToBeClickable(MODAL_CLOSE_BUTTON)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(SETTINGS_MODAL));
    }

    @Test
    @Order(4)
    @DisplayName("Should navigate to the login and signup modals")
    void testLoginAndSignupNavigation() {
        // Test Sign In
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_FORM));
        Assertions.assertTrue(driver.findElement(LOGIN_FORM).isDisplayed(), "Login form modal did not appear.");
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); // Close modal
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOGIN_FORM));
        
        // Test Sign Up
        wait.until(ExpectedConditions.elementToBeClickable(SIGN_UP_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SIGNUP_FORM));
        Assertions.assertTrue(driver.findElement(SIGNUP_FORM).isDisplayed(), "Signup form modal did not appear.");
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); // Close modal
    }
    
    @Test
    @Order(5)
    @DisplayName("Should verify footer external links to Docs and GitHub")
    void testFooterExternalLinks() {
        WebElement footer = driver.findElement(By.id("footer"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", footer);
        
        validateExternalLink(FOOTER_DOCS_LINK, "docs.jsfiddle.net");
        validateExternalLink(FOOTER_GITHUB_LINK, "github.com/jsfiddle");
    }
}