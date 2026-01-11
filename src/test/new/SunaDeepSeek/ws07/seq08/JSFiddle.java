package SunaDeepSeek.ws07.seq08;

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on JSFiddle homepage");
    }

    @Test
    @Order(2)
    public void testEditorPageNavigation() {
        driver.get(BASE_URL);
        WebElement editorLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/user/']")));
        editorLink.click();
        wait.until(ExpectedConditions.urlContains("/user/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/user/"), "Should navigate to editor page");
    }

    @Test
    @Order(3)
    public void testDocumentationLink() {
        driver.get(BASE_URL);
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='https://docs.jsfiddle.net']")));
        docsLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("docs.jsfiddle.net"), 
                    "Should navigate to docs page");
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']:not([href*='jsfiddle.net'])"));
        
        for (WebElement link : externalLinks) {
            String originalWindow = driver.getWindowHandle();
            String href = link.getAttribute("href");
            
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
            wait.until(ExpectedConditions.elementToBeClickable(link));
            link.click();
            
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href.split("/")[2]), 
                        "External link should open correct domain");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
        }
    }

    @Test
    @Order(5)
    public void testLoginButton() {
        driver.get(BASE_URL);
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/login/']")));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("/login/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login/"), "Should navigate to login page");
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a[href^='http']"));
        
        for (WebElement link : footerLinks) {
            String originalWindow = driver.getWindowHandle();
            String href = link.getAttribute("href");
            
            link.click();
            
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href.split("/")[2]), 
                        "Footer link should open correct domain");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
        }
    }
}