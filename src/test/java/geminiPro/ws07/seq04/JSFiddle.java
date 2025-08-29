package geminiPRO.ws07.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A JUnit 5 test suite for the JSFiddle code playground.
 * It uses Selenium WebDriver with Firefox in headless mode to test core functionalities
 * like running code, changing settings, navigating menus, and verifying external links.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddleHeadlessFirefoxTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JavascriptExecutor js;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        js = (JavascriptExecutor) driver;
        // The cookie consent dialog can interfere with clicks.
        // We accept it once at the beginning.
        driver.get(BASE_URL);
        try {
            WebElement acceptCookiesButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("cookies-ok")));
            acceptCookiesButton.click();
        } catch (TimeoutException e) {
            // If the button is not found, we assume consent is already given or not required.
            System.out.println("Cookie consent dialog not found, proceeding.");
        }
    }

    @AfterAll
    static void teardownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        // Navigate to the base URL to ensure a clean state for each test
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleIs("JSFiddle - Code Playground"));
    }

    // Helper to set text in a CodeMirror editor since sendKeys is unreliable.
    private void setCodeMirrorText(String panelId, String code) {
        String script = String.format("document.getElementById('%s').querySelector('.CodeMirror').CodeMirror.setValue(arguments[0]);", panelId);
        js.executeScript(script, code);
    }

    @Test
    @Order(1)
    @DisplayName("Verify Page Load and Default Title")
    void testPageLoadAndTitle() {
        Assertions.assertEquals("JSFiddle - Code Playground", driver.getTitle(), "Page title should be correct on load.");
        WebElement runButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("run")));
        Assertions.assertTrue(runButton.isDisplayed(), "The 'Run' button should be visible.");
    }

    @Test
    @Order(2)
    @DisplayName("Execute Simple HTML and JS and Verify Output")
    void testRunSimpleCode() {
        String htmlCode = "<h1>Hello, Tester!</h1>";
        String jsCode = "document.querySelector('h1').style.color = 'rgb(0, 0, 255)';"; // Use RGB for consistency

        // Wait for editor panels to be ready before interacting
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("panel-html")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("panel-javascript")));

        setCodeMirrorText("panel-html", htmlCode);
        setCodeMirrorText("panel-javascript", jsCode);

        driver.findElement(By.id("run")).click();

        // Wait for the result iframe to be available and switch to it
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("result")));

        // Inside the iframe, wait for the h1 element and verify its content and style
        WebElement resultHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        String headerText = resultHeader.getText();
        String headerColor = resultHeader.getCssValue("color");

        Assertions.assertAll("Verify the output of the executed code",
            () -> Assertions.assertEquals("Hello, Tester!", headerText, "Result header text is incorrect."),
            () -> Assertions.assertEquals("rgb(0, 0, 255)", headerColor, "Result header color is not blue.")
        );

        // Switch back to the main document context
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(3)
    @DisplayName("Navigate to Sign In Modal and Verify Elements")
    void testSignInModalNavigation() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();

        WebElement signInModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-modal")));
        Assertions.assertTrue(signInModal.isDisplayed(), "Sign In modal should be visible after clicking the button.");

        WebElement usernameField = driver.findElement(By.id("id_username"));
        WebElement passwordField = driver.findElement(By.id("id_password"));

        Assertions.assertAll("Verify Sign In modal elements",
            () -> Assertions.assertTrue(usernameField.isDisplayed(), "Username field should be visible."),
            () -> Assertions.assertTrue(passwordField.isDisplayed(), "Password field should be visible.")
        );

        // Close the modal
        WebElement closeButton = driver.findElement(By.cssSelector("#login-modal .modal-close"));
        closeButton.click();
        wait.until(ExpectedConditions.invisibilityOf(signInModal));
        Assertions.assertFalse(signInModal.isDisplayed(), "Sign In modal should be closed.");
    }

    @Test
    @Order(4)
    @DisplayName("Verify Navigation to the 'Docs' Page")
    void testHeaderNavigationToDocs() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Docs"))).click();
        wait.until(ExpectedConditions.urlContains("/docs/"));

        Assertions.assertTrue(driver.getCurrentUrl().contains("/docs/"), "URL should navigate to the Docs section.");
        WebElement docsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Documentation')]")));
        Assertions.assertTrue(docsHeader.isDisplayed(), "Documentation header should be visible on the Docs page.");
    }

    @Test
    @Order(5)
    @DisplayName("Test External Link to Twitter in Footer")
    void testExternalFooterLinkToTwitter() {
        String originalWindow = driver.getWindowHandle();
        // Scroll to the footer to ensure the link is in view
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com/jsfiddle']")));
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        List<String> windowHandles = new ArrayList<>(allWindows);
        
        // Switch to the new window
        String newWindow = windowHandles.stream()
            .filter(handle -> !handle.equals(originalWindow))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("New window not found"));
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "New window URL should contain 'twitter.com'.");

        // Close the new window and switch back to the original
        driver.close();
        driver.switchTo().window(originalWindow);

        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Should have returned to the original JSFiddle URL.");
    }

    @Test
    @Order(6)
    @DisplayName("Test 'Tidy' button functionality")
    void testTidyButton() {
        String messyHtml = "<div>   <p>messy</p>   </div>";
        String expectedTidyHtml = "<div>\n  <p>messy</p>\n</div>";

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("panel-html")));
        setCodeMirrorText("panel-html", messyHtml);

        WebElement tidyButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("tidy")));
        tidyButton.click();
        
        // Wait a moment for the tidy operation to complete
        wait.until(ExpectedConditions.not(ExpectedConditions.attributeContains(By.cssSelector("#panel-html .CodeMirror-code"), "innerHTML", messyHtml)));

        String actualTidyHtml = (String) js.executeScript("return document.getElementById('panel-html').querySelector('.CodeMirror').CodeMirror.getValue();");

        // Normalize line endings for cross-platform compatibility
        Assertions.assertEquals(expectedTidyHtml.replaceAll("\r\n", "\n"), actualTidyHtml.replaceAll("\r\n", "\n"), "HTML code should be formatted by the Tidy button.");
    }
}