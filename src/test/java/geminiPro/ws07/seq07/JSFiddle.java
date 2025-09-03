package geminiPRO.ws07.seq07;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A comprehensive JUnit 5 test suite for the JSFiddle online IDE.
 * This suite covers user registration, login, the core functionality of creating,
 * running, and saving a "fiddle", interacting with settings, and verifying external links.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddleComprehensiveTest {

    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15); // Increased for this complex app

    // Unique user credentials for each test run
    private static final String UNIQUE_RUN_ID = String.valueOf(System.currentTimeMillis()).substring(5);
    private static final String USERNAME = "gemini-user-" + UNIQUE_RUN_ID;
    private static final String EMAIL = "gemini.user." + UNIQUE_RUN_ID + "@example.com";
    private static final String PASSWORD = "Password123!";
    
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new Firefox-Driver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    void testUserRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign up"))).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_username"))).sendKeys(USERNAME);
        driver.findElement(By.id("id_email")).sendKeys(EMAIL);
        driver.findElement(By.id("id_password1")).sendKeys(PASSWORD);
        driver.findElement(By.id("id_password2")).sendKeys(PASSWORD);
        
        // This site has a very simple "I'm not a robot" checkbox that can be clicked directly.
        driver.findElement(By.id("id_captcha_1")).click();
        
        driver.findElement(By.xpath("//button[text()='Register']")).click();
        
        WebElement userDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-dropdown")));
        assertTrue(userDropdown.getText().contains(USERNAME), "Username should be displayed in the top bar after registration.");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        performLogoutIfLoggedIn();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in"))).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_username"))).sendKeys(USERNAME);
        driver.findElement(By.id("id_password")).sendKeys("incorrect-password");
        driver.findElement(By.xpath("//button[text()='Sign in']")).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".errorlist")));
        assertTrue(errorMessage.getText().contains("Please enter a correct username and password"), "Error message should be displayed for invalid login.");
    }

    @Test
    @Order(3)
    void testSuccessfulLoginAndLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in"))).click();
        performLogin(USERNAME, PASSWORD);
        
        WebElement userDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-dropdown")));
        assertTrue(userDropdown.getText().contains(USERNAME), "Username should be displayed after successful login.");
        
        userDropdown.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign out"))).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in")));
        assertTrue(driver.findElement(By.linkText("Sign in")).isDisplayed(), "Sign in link should be visible after logout.");
    }

    @Test
    @Order(4)
    void testCoreFiddleFunctionality_CreateRunAndSave() {
        performLogin(USERNAME, PASSWORD);
        
        // Write code into the panels
        String htmlCode = "<h1>Hello, JSFiddle!</h1>";
        String jsCode = "document.querySelector('h1').style.color = 'rgb(0, 0, 255)';"; // Use RGB for consistency
        setCodeInPanel("HTML", htmlCode);
        setCodeInPanel("JavaScript", jsCode);
        
        // Run the code
        wait.until(ExpectedConditions.elementToBeClickable(By.id("run"))).click();
        
        // Verify the result in the iframe
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("result")));
        WebElement resultHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Hello, JSFiddle!", resultHeader.getText(), "Result header text is incorrect.");
        assertEquals("rgb(0, 0, 255)", resultHeader.getCssValue("color"), "Result header color is not blue.");
        driver.switchTo().defaultContent();
        
        // Save the fiddle
        wait.until(ExpectedConditions.elementToBeClickable(By.id("save"))).click();
        
        // Verify the URL changes to indicate a saved fiddle
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL)));
        assertTrue(driver.getCurrentUrl().matches(BASE_URL + "[\\w\\d/]+"), "URL should change after saving the fiddle.");
    }

    @Test
    @Order(5)
    void testSidebarSettingsInteraction() {
        performLogin(USERNAME, PASSWORD);
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("js-framework-choice"))).click();
        
        WebElement sidebar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sidebar")));
        
        // Change framework
        Select frameworkDropdown = new Select(sidebar.findElement(By.name("framework")));
        frameworkDropdown.selectByVisibleText("jQuery 3.6.0");
        
        // Close and re-open to check if selection persisted in UI
        driver.findElement(By.cssSelector("#sidebar .sidebar-close-button a")).click();
        wait.until(ExpectedConditions.invisibilityOf(sidebar));

        WebElement frameworkChoice = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("js-framework-choice")));
        assertTrue(frameworkChoice.getText().contains("jQuery"), "Framework choice should be updated to jQuery.");
    }

    @Test
    @Order(6)
    void testExternalLinkInFooter() {
        String originalWindow = driver.getWindowHandle();
        
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@title='JSFiddle on Twitter']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", twitterLink);
        twitterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        assertTrue(driver.getCurrentUrl().contains("twitter.com/jsfiddle"), "Should have opened the JSFiddle Twitter page.");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should have switched back to the JSFiddle main page.");
    }

    // --- Helper Methods ---

    private void performLogin(String username, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_username"))).sendKeys(username);
        driver.findElement(By.id("id_password")).sendKeys(password);
        driver.findElement(By.xpath("//button[text()='Sign in']")).click();
    }

    private void performLogoutIfLoggedIn() {
        // A simple check for the user dropdown; if it exists, log out.
        if (!driver.findElements(By.id("user-dropdown")).isEmpty()) {
            driver.findElement(By.id("user-dropdown")).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign out"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in")));
        }
    }

    private void setCodeInPanel(String panelName, String code) {
        // CodeMirror editors are complex; direct interaction is flaky. JS is the most reliable way.
        String script = "arguments[0].CodeMirror.setValue(arguments[1]);";
        WebElement panel;
        switch (panelName.toUpperCase()) {
            case "HTML":
                panel = driver.findElement(By.id("HTML"));
                break;
            case "CSS":
                panel = driver.findElement(By.id("CSS"));
                break;
            case "JAVASCRIPT":
                panel = driver.findElement(By.id("JavaScript"));
                break;
            default:
                fail("Invalid panel name provided: " + panelName);
                return;
        }
        ((JavascriptExecutor) driver).executeScript(script, panel, code);
    }
}