package SunaDeepSeek.ws07.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static WebDriver driver;
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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        Assertions.assertEquals("JSFiddle - Code Playground", driver.getTitle());
        
        WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("a.brand")));
        Assertions.assertTrue(logo.isDisplayed(), "JSFiddle logo should be visible");
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Docs link
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='/docs/']")));
        docsLink.click();
        wait.until(ExpectedConditions.urlContains("/docs/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/docs/"), 
            "Should be on docs page");
        
        // Test Blog link (external)
        driver.get(BASE_URL);
        WebElement blogLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='blog.jsfiddle.net']")));
        blogLink.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("blog.jsfiddle.net"), 
            "Should be on blog page");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(3)
    public void testEditorPageElements() {
        driver.get(BASE_URL + "show/");
        
        WebElement htmlPanel = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".panel.html")));
        Assertions.assertTrue(htmlPanel.isDisplayed(), "HTML panel should be visible");
        
        WebElement cssPanel = driver.findElement(By.cssSelector(".panel.css"));
        Assertions.assertTrue(cssPanel.isDisplayed(), "CSS panel should be visible");
        
        WebElement jsPanel = driver.findElement(By.cssSelector(".panel.javascript"));
        Assertions.assertTrue(jsPanel.isDisplayed(), "JS panel should be visible");
        
        WebElement resultPanel = driver.findElement(By.cssSelector(".panel.result"));
        Assertions.assertTrue(resultPanel.isDisplayed(), "Result panel should be visible");
    }

    @Test
    @Order(4)
    public void testRunButtonFunctionality() {
        driver.get(BASE_URL + "show/");
        
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#run")));
        runButton.click();
        
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("iframe[name='result']")));
        Assertions.assertTrue(resultFrame.isDisplayed(), "Result frame should be visible after run");
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalFooterLink("Twitter", "a[href*='twitter.com/jsfiddle']", "twitter.com");
        
        // Test GitHub link
        testExternalFooterLink("GitHub", "a[href*='github.com/jsfiddle']", "github.com");
    }

    private void testExternalFooterLink(String linkName, String selector, String expectedDomain) {
        driver.get(BASE_URL);
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(selector)));
        link.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            linkName + " link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testLoginModal() {
        driver.get(BASE_URL);
        
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.login")));
        loginButton.click();
        
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".modal-content")));
        Assertions.assertTrue(modal.isDisplayed(), "Login modal should appear");
        
        WebElement emailField = driver.findElement(By.cssSelector("#login-email"));
        WebElement passwordField = driver.findElement(By.cssSelector("#login-password"));
        WebElement submitButton = driver.findElement(By.cssSelector("#login-submit"));
        
        Assertions.assertAll(
            () -> Assertions.assertTrue(emailField.isDisplayed(), "Email field should be visible"),
            () -> Assertions.assertTrue(passwordField.isDisplayed(), "Password field should be visible"),
            () -> Assertions.assertTrue(submitButton.isDisplayed(), "Submit button should be visible")
        );
        
        // Close modal
        WebElement closeButton = driver.findElement(By.cssSelector(".modal-header .close"));
        closeButton.click();
        wait.until(ExpectedConditions.invisibilityOf(modal));
    }
}