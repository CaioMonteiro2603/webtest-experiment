package SunaDeepSeek.ws10.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("gestao.brasilagritest.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("gestao.brasilagritest.com"), "Login failed - not redirected to main page");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".text-red-500, .text-red-600, .text-error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        testSuccessfulLogin();
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[data-testid='menu-button'], button[aria-label*='menu'], button[class*='menu']")));
        menuButton.click();
        
        List<WebElement> menuItems = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.cssSelector("nav a, .menu-item, [role='menuitem']"), 0));
        Assertions.assertTrue(menuItems.size() > 0, "Menu items not displayed");
        
        // Test menu close
        menuButton.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.cssSelector("nav[class*='open'], .menu-open")));
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        testSuccessfulLogin();
        
        // Example test for an external link (adjust selector as needed)
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href*='http']"));
        if (externalLinks.size() > 0) {
            String originalWindow = driver.getWindowHandle();
            
            for (WebElement link : externalLinks) {
                String href = link.getAttribute("href");
                if (href != null && !href.contains("brasilagritest")) {
                    link.click();
                    
                    wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                    for (String windowHandle : driver.getWindowHandles()) {
                        if (!originalWindow.equals(windowHandle)) {
                            driver.switchTo().window(windowHandle);
                            Assertions.assertTrue(driver.getCurrentUrl().contains(
                                href.split("//")[1].split("/")[0]), 
                                "External link domain mismatch");
                            driver.close();
                            driver.switchTo().window(originalWindow);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Test
    @Order(5)
    public void testLogout() {
        testSuccessfulLogin();
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[data-testid='menu-button'], button[aria-label*='menu'], button[class*='menu']")));
        menuButton.click();
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='logout'], button[aria-label*='sair'], [data-testid='logout']")));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "Logout failed - not redirected to login page");
    }
}