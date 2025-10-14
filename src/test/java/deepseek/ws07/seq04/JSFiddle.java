package deepseek.ws07.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
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
    public void testPageLoad() {
        driver.get(BASE_URL);
        WebElement editorPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("CodeMirror")));
        Assertions.assertTrue(editorPanel.isDisplayed());
    }

    @Test
    @Order(2)
    public void testRunCode() {
        driver.get(BASE_URL);
        
        // Clear default content
        WebElement htmlEditor = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'CodeMirror') and contains(@class,'html')]")));
        htmlEditor.click();
        driver.findElement(By.cssSelector("body")).sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        
        // Enter simple HTML
        htmlEditor.findElement(By.cssSelector("textarea")).sendKeys("<h1>Test</h1>");
        
        // Click Run button
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("run")));
        runButton.click();

        // Verify result
        WebElement resultFrame = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("result")));
        driver.switchTo().frame(resultFrame);
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.tagName("h1")));
        Assertions.assertEquals("Test", heading.getText());
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(3)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Documentation link
        testNavigationLink("Documentation", "https://doc.jsfiddle.net/");
        
        // Test Blog link
        testNavigationLink("Blog", "https://blog.jsfiddle.net/");
        
        // Test Support link
        testNavigationLink("Support", "https://support.jsfiddle.net/");
    }

    private void testNavigationLink(String linkText, String expectedUrl) {
        WebElement navLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//nav//a[contains(text(),'" + linkText + "')]")));
        navLink.click();

        if (!expectedUrl.contains("jsfiddle")) {
            // External links open in new tab
            String originalWindow = driver.getWindowHandle();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedUrl));
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedUrl));
            driver.get(BASE_URL); // Return to main page
        }
    }

    @Test
    @Order(4)
    public void testLoginButton() {
        driver.get(BASE_URL);
        
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@class,'login-btn')]")));
        loginButton.click();

        WebElement loginModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("login-modal")));
        Assertions.assertTrue(loginModal.isDisplayed());
        
        // Close modal
        WebElement closeButton = driver.findElement(By.xpath("//button[@class='close']"));
        closeButton.click();
        wait.until(ExpectedConditions.invisibilityOf(loginModal));
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testSocialLink("Twitter", "twitter.com");
        
        // Test GitHub link
        testSocialLink("GitHub", "github.com");
    }

    private void testSocialLink(String platform, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement socialLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//footer//a[contains(@href,'" + platform.toLowerCase() + "')]")));
        socialLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}