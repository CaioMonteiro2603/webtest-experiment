package SunaDeepSeek.ws10.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgriTestSuite {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard"), "Login failed - not redirected to dashboard");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        // First login
        testSuccessfulLogin();
        
        // Test menu button
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")));
        menuButton.click();
        
        // Wait for menu to open
        WebElement menu = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".navbar-collapse")));
        Assertions.assertTrue(menu.isDisplayed(), "Menu did not open");

        // Test menu items
        List<WebElement> menuItems = driver.findElements(By.cssSelector(".navbar-nav .nav-link"));
        Assertions.assertTrue(menuItems.size() > 0, "No menu items found");

        // Close menu
        menuButton.click();
        wait.until(ExpectedConditions.invisibilityOf(menu));
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        // First login
        testSuccessfulLogin();
        
        // Get all external links
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']"));
        
        for (WebElement link : externalLinks) {
            String originalWindow = driver.getWindowHandle();
            String href = link.getAttribute("href");
            
            // Open link in new tab
            ((JavascriptExecutor)driver).executeScript("window.open(arguments[0])", href);
            
            // Switch to new tab
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            // Verify external domain
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertNotEquals(BASE_URL, currentUrl, "External link points to same domain");
            
            // Close tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(5)
    public void testLogout() {
        // First login
        testSuccessfulLogin();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")));
        menuButton.click();
        
        // Click logout
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Sair')]")));
        logoutButton.click();
        
        // Verify logout
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "Logout failed - not redirected to login page");
    }
}