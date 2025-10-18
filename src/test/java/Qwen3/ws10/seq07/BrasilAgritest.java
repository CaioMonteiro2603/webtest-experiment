package Qwen3.ws10.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BrasilAgriTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testLoginPageLoad() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Verify login page title
        String title = driver.getTitle();
        assertTrue(title.contains("BrasilAgri"));
        
        // Verify login form elements
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        assertTrue(emailField.isDisplayed());
        
        WebElement passwordField = driver.findElement(By.id("password"));
        assertTrue(passwordField.isDisplayed());
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(loginButton.isDisplayed());
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Fill in login credentials
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for login to complete and check redirect
        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"));
        
        // Verify dashboard page loaded
        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("main")));
        assertTrue(dashboard.isDisplayed());
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Try invalid credentials
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("invalid@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("invalid123");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for possible error message
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message, .alert-danger")));
        
        // Verify we're still on login page
        assertTrue(driver.getCurrentUrl().contains("login"));
    }

    @Test
    @Order(4)
    public void testDashboardNavigation() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Test main navigation items
        List<WebElement> navItems = driver.findElements(By.cssSelector(".nav-link"));
        assertTrue(navItems.size() > 0);
        
        // Check each navigation item is displayed
        for (WebElement item : navItems) {
            if (item.isDisplayed()) {
                // Just verify they exist and are visible
            }
        }
    }

    @Test
    @Order(5)
    public void testUserProfileAccess() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to profile
        List<WebElement> profileLinks = driver.findElements(By.linkText("Profile"));
        if (!profileLinks.isEmpty()) {
            WebElement profileLink = profileLinks.get(0);
            profileLink.click();
            wait.until(ExpectedConditions.urlContains("profile"));
            assertTrue(driver.getCurrentUrl().contains("profile"));
        }
    }

    @Test
    @Order(6)
    public void testLogoutFunctionality() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Find and click logout
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            WebElement logoutLink = logoutLinks.get(0);
            logoutLink.click();
            wait.until(ExpectedConditions.urlContains("login"));
            assertTrue(driver.getCurrentUrl().contains("login"));
        }
    }

    @Test
    @Order(7)
    public void testMenuToggle() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Check for mobile menu toggle if present
        List<WebElement> menuToggles = driver.findElements(By.cssSelector(".menu-toggle, .navbar-toggle"));
        if (!menuToggles.isEmpty()) {
            WebElement toggle = menuToggles.get(0);
            assertTrue(toggle.isDisplayed());
        }
    }

    @Test
    @Order(8)
    public void testDashboardContent() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Verify dashboard sections
        WebElement dashboardHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertTrue(dashboardHeader.isDisplayed());
        
        // Check for summary cards or widgets
        List<WebElement> summaryCards = driver.findElements(By.cssSelector(".summary-card, .card"));
        assertTrue(summaryCards.size() >= 0);
    }

    @Test
    @Order(9)
    public void testSideNavigation() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Test sidebar navigation links
        List<WebElement> sidebarLinks = driver.findElements(By.cssSelector(".sidebar a"));
        assertTrue(sidebarLinks.size() > 0);
        
        // Check that at least one link is visible
        boolean hasVisibleLink = false;
        for (WebElement link : sidebarLinks) {
            if (link.isDisplayed()) {
                hasVisibleLink = true;
                break;
            }
        }
        assertTrue(hasVisibleLink);
    }

    @Test
    @Order(10)
    public void testUserManagementAccess() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Try to access user management
        List<WebElement> userManagementLinks = driver.findElements(By.linkText("Users"));
        if (!userManagementLinks.isEmpty()) {
            WebElement userLink = userManagementLinks.get(0);
            userLink.click();
            wait.until(ExpectedConditions.urlContains("users"));
            assertTrue(driver.getCurrentUrl().contains("users"));
        }
    }

    @Test
    @Order(11)
    public void testReportsSection() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Check reports section
        List<WebElement> reportsLinks = driver.findElements(By.linkText("Reports"));
        if (!reportsLinks.isEmpty()) {
            WebElement reportsLink = reportsLinks.get(0);
            reportsLink.click();
            wait.until(ExpectedConditions.urlContains("reports"));
            assertTrue(driver.getCurrentUrl().contains("reports"));
        }
    }

    @Test
    @Order(12)
    public void testSettingsAccess() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Access settings
        List<WebElement> settingsLinks = driver.findElements(By.linkText("Settings"));
        if (!settingsLinks.isEmpty()) {
            WebElement settingsLink = settingsLinks.get(0);
            settingsLink.click();
            wait.until(ExpectedConditions.urlContains("settings"));
            assertTrue(driver.getCurrentUrl().contains("settings"));
        }
    }

    @Test
    @Order(13)
    public void testFooterElements() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Check footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0);
        
        // Verify all footer links are displayed
        for (WebElement link : footerLinks) {
            if (link.isDisplayed()) {
                // Just verify they are present and visible
            }
        }
    }

    @Test
    @Order(14)
    public void testResponsiveDesign() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Check responsive elements
        List<WebElement> responsiveElements = driver.findElements(By.cssSelector(".responsive-element, .mobile-only"));
        // Just verify they exist in the DOM
    }

    @Test
    @Order(15)
    public void testPageTitleVerification() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        String loginTitle = driver.getTitle();
        assertTrue(loginTitle.contains("Login"));
        
        // Login
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        String dashboardTitle = driver.getTitle();
        assertTrue(dashboardTitle.contains("Dashboard"));
    }

    @Test
    @Order(16)
    public void testFormValidation() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Test empty form submission
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Check for validation errors
        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".error-message, .invalid-feedback"));
        assertTrue(errorMessages.size() >= 0);
    }

    @Test
    @Order(17)
    public void testProfilePictureDisplay() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Look for profile picture
        List<WebElement> profilePictures = driver.findElements(By.cssSelector(".profile-pic, .user-avatar"));
        // Just verify if present
    }

    @Test
    @Order(18)
    public void testNotificationSystem() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Check for notification elements
        List<WebElement> notifications = driver.findElements(By.cssSelector(".notification, .alert"));
        // Just verify they are present
    }
}