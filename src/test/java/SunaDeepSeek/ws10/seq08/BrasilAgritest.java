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
        
        usernameField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        testSuccessfulLogin();
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.navbar-toggler")));
        menuButton.click();
        
        List<WebElement> menuItems = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.cssSelector(".navbar-collapse.show .nav-link"), 0));
        Assertions.assertTrue(menuItems.size() > 0, "Menu items not displayed");
        
        // Test menu close
        menuButton.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
            By.cssSelector(".navbar-collapse.show")));
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
                if (!href.contains("brasilagritest")) {
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
            By.cssSelector("button.navbar-toggler")));
        menuButton.click();
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Sair')]")));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "Logout failed - not redirected to login page");
    }
}