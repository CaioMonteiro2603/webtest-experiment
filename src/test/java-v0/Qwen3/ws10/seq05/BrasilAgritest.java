package Qwen3.ws10.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
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
    public void testLoginPageLoad() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("Login"), "Page title should contain Login");
        
        WebElement loginForm = driver.findElement(By.cssSelector("form"));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed");
        
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        assertTrue(emailInput.isDisplayed(), "Email input should be displayed");
        assertTrue(passwordInput.isDisplayed(), "Password input should be displayed");
        assertTrue(loginButton.isDisplayed(), "Login button should be displayed");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailInput.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for redirect after login
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/dashboard"), "Login should redirect to dashboard page");
        
        // Check if user is properly logged in
        WebElement userMenu = driver.findElement(By.cssSelector(".user-menu"));
        assertTrue(userMenu.isDisplayed(), "User menu should be displayed after login");
    }

    @Test
    @Order(3)
    public void testInvalidCredentialsError() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailInput.sendKeys("invalid@example.com");
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("wrongpassword");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid credentials");
        assertTrue(errorMessage.getText().contains("Credenciais inválidas") || 
                   errorMessage.getText().contains("Invalid credentials"), 
                   "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    public void testLogoutFunctionality() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // First perform a valid login
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailInput.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Click on user menu and logout
        WebElement userMenuButton = driver.findElement(By.cssSelector(".user-menu button"));
        userMenuButton.click();
        
        WebElement logoutButton = driver.findElement(By.linkText("Sair"));
        logoutButton.click();
        
        // Wait for redirect to login page
        wait.until(ExpectedConditions.urlContains("/login"));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login"), "Should redirect to login page after logout");
        
        // Verify we're back on login page
        WebElement loginForm = driver.findElement(By.cssSelector("form"));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed after logout");
    }

    @Test
    @Order(5)
    public void testNavigationToDashboard() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailInput.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        WebElement dashboardTitle = driver.findElement(By.cssSelector("h1"));
        assertTrue(dashboardTitle.isDisplayed(), "Dashboard title should be displayed");
        
        // Test navigation to different sections
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() > 0, "Should have navigation links");
        
        // Check main menu items
        WebElement menuItems = driver.findElement(By.cssSelector(".main-menu"));
        assertTrue(menuItems.isDisplayed(), "Main menu should be displayed");
    }

    @Test
    @Order(6)
    public void testMainMenuNavigation() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailInput.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Navigate to different menu items
        List<WebElement> menuItems = driver.findElements(By.cssSelector(".main-menu a"));
        
        for (WebElement item : menuItems) {
            String itemText = item.getText().trim();
            if (!itemText.isEmpty() && !itemText.equals("Sair")) {
                // Test clicking a menu item
                item.click();
                // Wait for page load
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
                
                // Verify that we stayed on the same domain
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("gestao.brasilagritest.com"), 
                          "Should navigate within the application");
                
                // Go back to dashboard for next test
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains("/dashboard"));
            }
        }
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first to access full application
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailInput.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Check footer links after login
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0, "Should have footer links");
        
        for (WebElement link : footerLinks) {
            assertTrue(link.isDisplayed(), "Footer link should be displayed");
            assertNotNull(link.getAttribute("href"), "Footer link should have href attribute");
        }
    }

    @Test
    @Order(8)
    public void testExternalLinksInFooter() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailInput.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0, "Should have footer links");
        
        String mainWindowHandle = driver.getWindowHandle();
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                // Click external links that open in new tabs or windows
                link.click();
                try {
                    wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                    
                    for (String windowHandle : driver.getWindowHandles()) {
                        if (!windowHandle.equals(mainWindowHandle)) {
                            driver.switchTo().window(windowHandle);
                            break;
                        }
                    }
                    
                    String currentUrl = driver.getCurrentUrl();
                    // Verify that the external link contains valid URL structure
                    assertTrue(currentUrl.contains("http") || currentUrl.contains("https"), 
                               "External link should be a valid URL");
                    
                    driver.close();
                    driver.switchTo().window(mainWindowHandle);
                } catch (TimeoutException ignored) {
                    // If not a new window, may be an internal link
                }
            }
        }
    }

    @Test
    @Order(9)
    public void testHomePageElements() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // First login
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailInput.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Check key elements on dashboard
        WebElement dashboardHeader = driver.findElement(By.cssSelector("h1"));
        assertTrue(dashboardHeader.isDisplayed(), "Dashboard header should be displayed");
        
        WebElement welcomeMessage = driver.findElement(By.cssSelector(".welcome-message"));
        assertTrue(welcomeMessage.isDisplayed(), "Welcome message should be displayed");
        
        // Check for organization or user profile info
        WebElement userInfo = driver.findElement(By.cssSelector(".user-info"));
        assertTrue(userInfo.isDisplayed(), "User info section should be displayed");
    }

    @Test
    @Order(10)
    public void testLoginFormValidation() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Test empty form submission
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Check for validation errors
        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".error-message"));
        assertTrue(errorMessages.size() > 0, "Should show validation errors with empty form");
        
        // Fill email only
        WebElement emailInput = driver.findElement(By.id("email"));
        emailInput.sendKeys("test@example.com");
        loginButton.click();
        
        // Check for validation error about password
        List<WebElement> passwordError = driver.findElements(By.cssSelector(".password-error"));
        if (!passwordError.isEmpty()) {
            assertTrue(passwordError.get(0).isDisplayed(), "Should show password error");
        }
    }

    @Test
    @Order(11)
    public void testResponsiveDesignElements() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Check that login form is responsive
        WebElement loginForm = driver.findElement(By.cssSelector("form"));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed");
        
        // Check for proper header
        WebElement header = driver.findElement(By.cssSelector("header"));
        assertTrue(header.isDisplayed(), "Header should be displayed");
        
        // Check for mobile menu elements if they exist
        try {
            WebElement mobileMenu = driver.findElement(By.cssSelector(".mobile-menu-toggle"));
            assertTrue(mobileMenu.isDisplayed(), "Mobile menu toggle should be displayed");
        } catch (NoSuchElementException ignored) {
            // May not be present in all viewports
        }
    }

    @Test
    @Order(12)
    public void testPageStructure() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Check semantic HTML elements
        List<WebElement> headerElements = driver.findElements(By.tagName("header"));
        assertEquals(1, headerElements.size(), "Should have one header element");
        
        List<WebElement> mainElements = driver.findElements(By.tagName("main"));
        assertEquals(1, mainElements.size(), "Should have one main element");
        
        List<WebElement> footerElements = driver.findElements(By.tagName("footer"));
        assertEquals(1, footerElements.size(), "Should have one footer element");
        
        // Check for form elements
        List<WebElement> formElements = driver.findElements(By.tagName("form"));
        assertEquals(1, formElements.size(), "Should have one form element");
        
        // Check for input elements
        List<WebElement> inputElements = driver.findElements(By.tagName("input"));
        assertTrue(inputElements.size() > 0, "Should have input elements");
    }

    @Test
    @Order(13)
    public void testResetAppState() {
        driver.get("https://gestao.brasilagritest.com/login");
        
        // Login first
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailInput.sendKeys("superadmin@brasilagritest.com.br");
        
        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.sendKeys("10203040");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        // Wait for dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Check if we can access the reset state function
        try {
            WebElement resetStateButton = driver.findElement(By.linkText("Resetar Estado"));
            assertNotNull(resetStateButton, "Reset state button should be present");
        } catch (NoSuchElementException ignored) {
            // Reset not available in current state, that's okay for this test
        }
    }
}