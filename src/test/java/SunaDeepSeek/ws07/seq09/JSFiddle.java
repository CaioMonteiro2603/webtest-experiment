package SunaDeepSeek.ws07.seq09;

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
    }

    @Test
    @Order(2)
    public void testEditorPageNavigation() {
        driver.get(BASE_URL);
        WebElement editorLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/show/']")));
        editorLink.click();
        wait.until(ExpectedConditions.urlContains("/show/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/show/"), "Should navigate to editor page");
    }

    @Test
    @Order(3)
    public void testDocumentationLink() {
        driver.get(BASE_URL);
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/docs/']")));
        docsLink.click();
        wait.until(ExpectedConditions.urlContains("/docs/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/docs/"), "Should navigate to docs page");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String mainWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should open Twitter in new tab");
        driver.close();
        driver.switchTo().window(mainWindow);

        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        githubLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "Should open GitHub in new tab");
        driver.close();
        driver.switchTo().window(mainWindow);
    }

    @Test
    @Order(5)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "login/");
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("login-email")));
        WebElement passwordField = driver.findElement(By.id("login-password"));
        WebElement loginButton = driver.findElement(By.id("login-submit"));

        // Negative test case
        emailField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Should show error message for invalid login");
    }

    @Test
    @Order(6)
    public void testNavigationMenu() {
        driver.get(BASE_URL);
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggle")));
        menuButton.click();
        
        // Verify menu items
        List<WebElement> menuItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".nav.navbar-nav li a")));
        Assertions.assertTrue(menuItems.size() > 0, "Menu should have items");
        
        // Close menu
        menuButton.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.cssSelector(".nav.navbar-nav")));
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Scroll to footer
        ((JavascriptExecutor)driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        
        // Test footer links
        List<WebElement> footerLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("footer a")));
        Assertions.assertTrue(footerLinks.size() > 0, "Footer should have links");
    }

    @Test
    @Order(8)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='search']")));
        searchInput.sendKeys("test");
        searchInput.sendKeys(Keys.RETURN);
        
        wait.until(ExpectedConditions.urlContains("search="));
        Assertions.assertTrue(driver.getCurrentUrl().contains("search=test"), "Should perform search");
    }
}