package SunaDeepSeek.ws07.seq10;

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
        
        WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("a.navbar-brand img")));
        Assertions.assertTrue(logo.isDisplayed(), "JSFiddle logo should be visible");
    }

    @Test
    @Order(2)
    public void testEditorPageNavigation() {
        driver.get(BASE_URL);
        WebElement editorLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/show/']")));
        editorLink.click();
        
        wait.until(ExpectedConditions.urlContains("/show/"));
        WebElement runButton = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("button#run")));
        Assertions.assertTrue(runButton.isDisplayed(), "Run button should be visible on editor page");
    }

    @Test
    @Order(3)
    public void testDocumentationLink() {
        driver.get(BASE_URL);
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/docs/']")));
        docsLink.click();
        
        wait.until(ExpectedConditions.urlContains("/docs/"));
        WebElement docsTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(docsTitle.getText().contains("Documentation"), 
            "Documentation page should have correct title");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), 
            "Should be on Twitter domain");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        githubLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), 
            "Should be on GitHub domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "login/");
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("login-email")));
        WebElement passwordField = driver.findElement(By.id("login-password"));
        WebElement loginButton = driver.findElement(By.id("login-submit"));
        
        emailField.sendKeys("test@example.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), 
            "Error message should be displayed for invalid login");
    }

    @Test
    @Order(6)
    public void testNavigationMenu() {
        driver.get(BASE_URL);
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")));
        menuButton.click();
        
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/about/']")));
        aboutLink.click();
        
        wait.until(ExpectedConditions.urlContains("/about/"));
        WebElement aboutTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertTrue(aboutTitle.getText().contains("About"), 
            "About page should have correct title");
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        ((JavascriptExecutor)driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        
        List<WebElement> footerLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("footer a")));
        Assertions.assertTrue(footerLinks.size() > 0, "Footer should contain links");
        
        for (WebElement link : footerLinks) {
            Assertions.assertTrue(link.isDisplayed(), "Footer link should be visible");
        }
    }
}