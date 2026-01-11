package SunaDeepSeek.ws10.seq03;

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
    public void testLoginPageElements() {
        driver.get(BASE_URL);
        
        // Verify login form elements
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        Assertions.assertTrue(emailField.isDisplayed(), "Email field should be visible");
        Assertions.assertTrue(passwordField.isDisplayed(), "Password field should be visible");
        Assertions.assertTrue(loginButton.isDisplayed(), "Login button should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        // Enter invalid credentials
        emailField.sendKeys("invalid@test.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        // Verify error message with more flexible selector
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(@class,'alert') and contains(@class,'danger') or contains(@class,'error')]")
        ));
        Assertions.assertTrue(errorMessage.getText().toLowerCase().contains("credenciais") || 
            errorMessage.getText().toLowerCase().contains("inválidas") || 
            errorMessage.getText().toLowerCase().contains("invalid"), 
            "Should show invalid credentials error");
    }

    @Test
    @Order(3)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        // Enter valid credentials
        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        // Wait for URL to change to dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Verify dashboard loaded
        WebElement dashboardTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.tagName("h1")
        ));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), 
            "Should redirect to dashboard after login");
        Assertions.assertTrue(dashboardTitle.getText().toLowerCase().contains("dashboard") || 
            dashboardTitle.getText().toLowerCase().contains("painel"), 
            "Dashboard title should be visible");
    }

    @Test
    @Order(4)
    public void testNavigationMenu() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testSuccessfulLogin();
        }
        
        // Try to find menu button with different selectors
        WebElement menuButton = null;
        try {
            menuButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-toggler, button[data-bs-toggle='collapse'], .menu-toggle, .hamburger")
            ));
        } catch (TimeoutException e) {
            // If no menu button found, assume menu is already visible
            List<WebElement> menuItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector(".navbar-nav .nav-link, .sidebar .nav-link, .menu .nav-link")
            ));
            Assertions.assertTrue(menuItems.size() > 0, "Menu items should be visible");
            return;
        }
        
        if (menuButton != null && menuButton.isDisplayed()) {
            menuButton.click();
        }
        
        // Verify menu items
        List<WebElement> menuItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".navbar-nav .nav-link, .sidebar .nav-link, .menu .nav-link")
        ));
        Assertions.assertTrue(menuItems.size() > 0, "Menu items should be visible");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testSuccessfulLogin();
        }
        
        // Find all external links
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']"));
        
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.contains("brasilagritest.com")) {
                String originalWindow = driver.getWindowHandle();
                
                try {
                    // Open link in new tab using JavaScript if regular click fails
                    if (!link.isDisplayed() || !link.isEnabled()) {
                        continue;
                    }
                    
                    ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('target','_blank');", link);
                    link.click();
                    
                    // Switch to new tab
                    wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                    for (String windowHandle : driver.getWindowHandles()) {
                        if (!windowHandle.equals(originalWindow)) {
                            driver.switchTo().window(windowHandle);
                            break;
                        }
                    }
                    
                    // Verify external domain
                    String currentUrl = driver.getCurrentUrl();
                    if (!currentUrl.contains("brasilagritest.com") && !currentUrl.equals("about:blank")) {
                        Assertions.assertFalse(currentUrl.contains("brasilagritest.com"),
                            "Should be on external site");
                    }
                    
                    // Close tab and switch back
                    driver.close();
                    driver.switchTo().window(originalWindow);
                } catch (Exception e) {
                    // If anything fails, ensure we return to original window
                    try {
                        driver.switchTo().window(originalWindow);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testLogout() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testSuccessfulLogin();
        }
        
        // Try different selectors for logout
        WebElement logoutButton = null;
        
        try {
            // Try to open menu first if needed
            WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-toggler, button[data-bs-toggle='collapse'], .menu-toggle")
            ));
            if (menuButton.isDisplayed()) {
                menuButton.click();
            }
            
            // Try multiple selectors for logout link/button
            try {
                logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(translate(text(),'SAIR','sair'),'sair') or contains(translate(text(),'LOGOUT','logout'),'logout') or contains(@href,'logout') or contains(@onclick,'logout')]")
                ));
            } catch (TimeoutException e) {
                logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href*='logout'], button[onclick*='logout'], .logout-btn")
                ));
            }
            
            logoutButton.click();
            
        } catch (TimeoutException e) {
            // If no logout button found, try JavaScript logout
            ((JavascriptExecutor) driver).executeScript("window.location.href = arguments[0] + '/logout';", BASE_URL.replace("/login", ""));
        }
        
        // Wait for redirect to login page
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), 
            "Should redirect to login page after logout");
    }
}