package Qwen3.ws10.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
    public void testLoginPageLoadsCorrectly() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        String currentPageTitle = driver.getTitle();
        assertTrue(currentPageTitle.contains("Login"), "Page title should contain 'Login'");
        
        WebElement mainHeader = driver.findElement(By.tagName("h1"));
        assertTrue(mainHeader.getText().contains("Login"), "Main header should contain Login");
        
        // Check if login form is present
        WebElement loginForm = driver.findElement(By.tagName("form"));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed");
        
        // Check for email and password fields
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement passwordField = driver.findElement(By.id("password"));
        assertTrue(emailField.isDisplayed(), "Email field should be displayed");
        assertTrue(passwordField.isDisplayed(), "Password field should be displayed");
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
        
        // Submit login form
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Verify successful login
        wait.until(ExpectedConditions.urlContains("dashboard"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("dashboard"), "Should be on dashboard page after login");
        
        WebElement dashboardHeader = driver.findElement(By.tagName("h1"));
        assertTrue(dashboardHeader.getText().contains("Dashboard"), "Dashboard header should be displayed");
    }

    @Test
    @Order(3)
    public void testInvalidLoginError() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Fill in invalid credentials
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("wrongpassword");
        
        // Submit login form
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Check for error message
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message"));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(4)
    public void testNavigationAndMenuFunctionality() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Test menu navigation
        WebElement menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        
        // Click on 'Dashboard' menu item (should refresh current page)
        WebElement dashboardLink = driver.findElement(By.linkText("Dashboard"));
        dashboardLink.click();
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("dashboard"), "Should stay on dashboard page");
        
        // Navigate to 'Users' page
        menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        WebElement usersLink = driver.findElement(By.linkText("Users"));
        usersLink.click();
        
        currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("users"), "Should navigate to users page");
        
        // Go back to dashboard
        driver.get("https://gestao.brasilagritest.com/dashboard");
        
        // Click on 'Logout'
        menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        WebElement logoutLink = driver.findElement(By.linkText("Logout"));
        logoutLink.click();
        
        // Verify logout successful
        currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("login"), "Should be back on login page after logout");
    }

    @Test
    @Order(5)
    public void testUserManagement() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to users page
        WebElement menuButton = driver.findElement(By.cssSelector(".menu-button"));
        menuButton.click();
        WebElement usersLink = driver.findElement(By.linkText("Users"));
        usersLink.click();
        
        // Wait for users page to load
        wait.until(ExpectedConditions.urlContains("users"));
        
        // Check if users table is displayed
        WebElement usersTable = driver.findElement(By.cssSelector("table"));
        assertTrue(usersTable.isDisplayed(), "Users table should be displayed");
        
        // Check if there are users listed
        List<WebElement> userRows = driver.findElements(By.cssSelector("tbody tr"));
        assertTrue(userRows.size() > 0, "Should have at least one user displayed");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Wait for footer to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("footer")));
        
        // Check for external links in footer
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("github") || href.contains("twitter") || href.contains("facebook"))) {
                // These are external links we want to test
                String oldTab = driver.getWindowHandle();
                link.click();
                String winHandle = driver.getWindowHandle();
                driver.switchTo().window(winHandle);
                
                // Verify we navigated to expected domain
                if (href.contains("github")) {
                    assertTrue(driver.getCurrentUrl().contains("github.com"), 
                              "GitHub link should navigate to GitHub website");
                } else if (href.contains("twitter")) {
                    assertTrue(driver.getCurrentUrl().contains("twitter.com"), 
                              "Twitter link should navigate to Twitter website");
                } else if (href.contains("facebook")) {
                    assertTrue(driver.getCurrentUrl().contains("facebook.com"), 
                              "Facebook link should navigate to Facebook website");
                }
                
                driver.close();
                driver.switchTo().window(oldTab);
            }
        }
    }

    @Test
    @Order(7)
    public void testDashboardFunctionality() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("superadmin@brasilagritest.com.br");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("10203040");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Check dashboard elements
        WebElement dashboardHeader = driver.findElement(By.tagName("h1"));
        assertTrue(dashboardHeader.getText().contains("Dashboard"), "Dashboard header should be displayed");
        
        // Check if summary cards are displayed
        List<WebElement> summaryCards = driver.findElements(By.cssSelector(".summary-card"));
        assertTrue(summaryCards.size() > 0, "Should have summary cards displayed");
        
        // Check for charts or statistics (if present)
        try {
            WebElement chart = driver.findElement(By.cssSelector(".chart-container"));
            assertTrue(chart.isDisplayed(), "Chart container should be displayed");
        } catch (NoSuchElementException e) {
            // Chart may not be present, that's okay
        }
    }
}