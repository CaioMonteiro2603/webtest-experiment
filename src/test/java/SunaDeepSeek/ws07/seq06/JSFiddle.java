package SunaDeepSeek.ws07.seq06;

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
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddleWebTest {
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

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        Assertions.assertTrue(driver.getTitle().contains("JSFiddle"), "Page title should contain 'JSFiddle'");
    }

    @Test
    @Order(2)
    public void testEditorPage() {
        driver.get(BASE_URL);
        WebElement editorLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/show/']")));
        editorLink.click();
        wait.until(ExpectedConditions.urlContains("/show/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/show/"), "Should be on editor page");
    }

    @Test
    @Order(3)
    public void testDocumentationPage() {
        driver.get(BASE_URL);
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/docs/']")));
        docsLink.click();
        wait.until(ExpectedConditions.urlContains("/docs/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/docs/"), "Should be on documentation page");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']:not([href*='jsfiddle.net'])"));
        
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (href.contains("twitter") || href.contains("facebook") || href.contains("linkedin")) {
                String originalWindow = driver.getWindowHandle();
                link.click();
                
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(originalWindow)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                Assertions.assertTrue(driver.getCurrentUrl().contains(href.split("//")[1].split("/")[0]), 
                    "External URL should contain expected domain");
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(5)
    public void testLoginForm() {
        driver.get(BASE_URL);
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/login/']")));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("/login/"));

        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[name='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("test@example.com");
        passwordField.sendKeys("invalidpassword");
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}