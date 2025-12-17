package deepseek.ws07.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

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
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h3.text-center")));
        Assertions.assertTrue(title.getText().contains("Test your JavaScript"));
    }

    @Test
    @Order(2)
    public void testEditorInteraction() {
        driver.get(BASE_URL);
        
        // Switch to editor iframe
        WebElement editorFrame = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("iframe[name='result']")));
        driver.switchTo().frame(editorFrame);
        
        // Verify default content
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#run")));
        Assertions.assertTrue(runButton.isDisplayed());
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(3)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Blog link
        WebElement blogLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Blog")));
        blogLink.click();
        wait.until(ExpectedConditions.urlContains("blog"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/blog/"));
        
        // Test Documentation link
        driver.findElement(By.linkText("Documentation")).click();
        wait.until(ExpectedConditions.urlContains("doc"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("doc/"));
        
        // Test back to home
        driver.findElement(By.cssSelector(".logo")).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(4)
    public void testLoginModal() {
        driver.get(BASE_URL);
        
        // Open login modal
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Login")));
        loginButton.click();
        
        // Verify modal appears
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".auth-modal")));
        Assertions.assertTrue(modal.isDisplayed());
        
        // Attempt invalid login
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#login-email")));
        emailField.sendKeys("invalid@email.com");
        driver.findElement(By.cssSelector("#login-pwd")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("#login-submit")).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert.alert-error")));
        Assertions.assertTrue(errorMessage.getText().contains("Wrong credentials"));
    }

    @Test
    @Order(5)
    public void testCodePanes() {
        driver.get(BASE_URL);
        
        WebElement htmlPane = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#html-field")));
        WebElement cssPane = driver.findElement(By.cssSelector("#css-field"));
        WebElement jsPane = driver.findElement(By.cssSelector("#js-field"));
        
        Assertions.assertTrue(htmlPane.isDisplayed());
        Assertions.assertTrue(cssPane.isDisplayed());
        Assertions.assertTrue(jsPane.isDisplayed());
    }

    @Test
    @Order(6)
    public void testSocialLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        String originalWindow = driver.getWindowHandle();
        driver.findElement(By.cssSelector("a[href='https://twitter.com/jsfiddle']")).click();
        
        // Switch to new window and verify
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test Terms link
        WebElement termsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Terms")));
        termsLink.click();
        wait.until(ExpectedConditions.urlContains("terms"));
        
        // Test Privacy Policy link
        driver.findElement(By.linkText("Privacy")).click();
        wait.until(ExpectedConditions.urlContains("privacy"));
        
        // Verify content appears 
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".container > h1")));
    }
}