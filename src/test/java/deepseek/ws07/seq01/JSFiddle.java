package deepseek.ws07.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddleWebTest {

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("a.fiddleLogo")));
        Assertions.assertTrue(logo.isDisplayed(), "JSFiddle logo should be displayed");
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), 
            "Current URL should match base URL");
    }

    @Test
    @Order(2)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "user/login/");
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("loginform-email")));
        WebElement passwordField = driver.findElement(By.id("loginform-password"));
        WebElement loginButton = driver.findElement(
            By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Incorrect email or password"), 
            "Error message for invalid login should be displayed");
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get(BASE_URL);
        
        // Test About link (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("About")));
        aboutLink.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("jsfiddle.net/about"), 
            "About page should open in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(4)
    public void testSocialLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink(".social-link.twitter", "twitter.com");
        
        // Test GitHub link
        testExternalLink(".social-link.github", "github.com");
    }

    private void testExternalLink(String cssSelector, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement socialLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(cssSelector)));
        socialLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "Social link should open " + expectedDomain + " in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testEditorFunctionality() {
        driver.get(BASE_URL + "new/");
        
        WebElement htmlTab = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("panel_html")));
        htmlTab.click();
        
        WebElement htmlEditor = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#panel_html .CodeMirror textarea")));
        htmlEditor.sendKeys("<div id=\"test\">Hello World</div>");
        
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("run")));
        runButton.click();
        
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("iframe[name='result']")));
        driver.switchTo().frame(resultFrame);
        
        WebElement testDiv = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("test")));
        Assertions.assertEquals("Hello World", testDiv.getText(), 
            "HTML should be rendered in result frame");
        
        driver.switchTo().defaultContent();
    }
}