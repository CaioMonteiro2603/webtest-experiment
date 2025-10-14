package Qwen3.ws10.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritestTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("dashboard"), "Login should redirect to dashboard");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        // Wait for error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed on invalid login");
    }

    @Test
    @Order(3)
    public void testDashboardNavigation() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Test main navigation menu
        WebElement menuToggle = driver.findElement(By.cssSelector(".menu-toggle"));
        menuToggle.click();
        
        // Wait for menu to open
        WebElement menu = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".sidebar-menu")));
        assertTrue(menu.isDisplayed(), "Sidebar menu should be displayed after clicking toggle");
        
        // Test clicking on dashboard link
        WebElement dashboardLink = driver.findElement(By.linkText("Dashboard"));
        dashboardLink.click();
        
        // Verify we're still on dashboard
        wait.until(ExpectedConditions.urlContains("dashboard"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("dashboard"), "Should stay on dashboard page");
    }

    @Test
    @Order(4)
    public void testUserManagement() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to user management
        WebElement usersLink = driver.findElement(By.linkText("Usuários"));
        usersLink.click();
        
        // Wait for users page
        wait.until(ExpectedConditions.urlContains("users"));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("users"), "Should navigate to users page");
        
        // Verify user table is displayed
        WebElement usersTable = driver.findElement(By.cssSelector(".users-table"));
        assertTrue(usersTable.isDisplayed(), "Users table should be displayed");
    }

    @Test
    @Order(5)
    public void testReportsSection() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to reports
        WebElement reportsLink = driver.findElement(By.linkText("Relatórios"));
        reportsLink.click();
        
        // Wait for reports page
        wait.until(ExpectedConditions.urlContains("reports"));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("reports"), "Should navigate to reports page");
        
        // Verify report container is displayed
        WebElement reportsContainer = driver.findElement(By.cssSelector(".reports-container"));
        assertTrue(reportsContainer.isDisplayed(), "Reports container should be displayed");
    }

    @Test
    @Order(6)
    public void testProfileSettings() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to profile
        WebElement profileLink = driver.findElement(By.cssSelector("[data-testid='profile-link']"));
        profileLink.click();
        
        // Wait for profile page
        wait.until(ExpectedConditions.urlContains("profile"));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("profile"), "Should navigate to profile page");
        
        // Verify profile form is displayed
        WebElement profileForm = driver.findElement(By.cssSelector(".profile-form"));
        assertTrue(profileForm.isDisplayed(), "Profile form should be displayed");
    }

    @Test
    @Order(7)
    public void testLogoutFunctionality() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Logout
        WebElement logoutButton = driver.findElement(By.linkText("Sair"));
        logoutButton.click();
        
        // Wait for login page to appear
        wait.until(ExpectedConditions.urlContains("login"));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("login"), "Should redirect to login page after logout");
        
        // Verify login form is present
        WebElement loginForm = driver.findElement(By.cssSelector(".login-form"));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed after logout");
    }

    @Test
    @Order(8)
    public void testExternalLinksInFooter() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first to get to main application
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("superadmin@brasilagritest.com.br");
        passwordField.sendKeys("10203040");
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        String parentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(parentWindow)) {
                driver.switchTo().window(window);
                driver.close();
            }
        }
        
        // Check footer links (if they exist)
        try {
            List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
            for (WebElement link : footerLinks) {
                String href = link.getAttribute("href");
                if (href != null && !href.isEmpty() && !href.contains("brasilagritest.com")) {
                    // This looks like an external link
                    link.click();
                    
                    // Switch to new tab
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("brasilagritest.com") || currentUrl.contains("external"), 
                              "Should be able to navigate to external links");
                    
                    driver.close();
                    driver.switchTo().window(parentWindow);
                    break; // Test just one external link
                }
            }
        } catch (Exception e) {
            // External links might not exist or be accessible
        }
    }
}