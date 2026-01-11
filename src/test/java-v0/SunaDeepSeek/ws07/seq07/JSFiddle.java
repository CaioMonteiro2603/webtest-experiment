package SunaDeepSeek.ws07.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL));
    }

    @Test
    @Order(2)
    public void testEditorPage() {
        driver.get(BASE_URL);
        WebElement editorLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/show/']")));
        editorLink.click();
        wait.until(ExpectedConditions.urlContains("/show/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/show/"));
    }

    @Test
    @Order(3)
    public void testDocumentationLink() {
        driver.get(BASE_URL);
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/documentation/']")));
        docsLink.click();
        wait.until(ExpectedConditions.urlContains("/documentation/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/documentation/"));
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']:not([href*='jsfiddle.net'])"));
        
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (href.contains("twitter.com") || href.contains("facebook.com") || href.contains("linkedin.com")) {
                String originalWindow = driver.getWindowHandle();
                link.click();
                
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(originalWindow)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                Assertions.assertTrue(driver.getCurrentUrl().contains(href.split("/")[2]));
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(5)
    public void testLoginForm() {
        driver.get(BASE_URL + "login/");
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("login")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("testuser");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed());
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}