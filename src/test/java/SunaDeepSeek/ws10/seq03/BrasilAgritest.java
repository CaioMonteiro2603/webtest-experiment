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
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-danger")
        ));
        Assertions.assertTrue(errorMessage.getText().contains("credenciais inválidas"), 
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
        
        // Verify dashboard loaded
        WebElement dashboardTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h1")
        ));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), 
            "Should redirect to dashboard after login");
        Assertions.assertTrue(dashboardTitle.getText().contains("Dashboard"), 
            "Dashboard title should be visible");
    }

    @Test
    @Order(4)
    public void testNavigationMenu() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            testSuccessfulLogin();
        }
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")
        ));
        menuButton.click();
        
        // Verify menu items
        List<WebElement> menuItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".navbar-nav .nav-link")
        ));
        Assertions.assertTrue(menuItems.size() > 0, "Menu items should be visible");
        
        // Test each menu item
        for (WebElement item : menuItems) {
            String itemText = item.getText();
            if (itemText.contains("Sair")) {
                // Skip logout for now to maintain session
                continue;
            }
            
            item.click();
            wait.until(ExpectedConditions.urlContains(itemText.toLowerCase()));
            Assertions.assertTrue(driver.getCurrentUrl().contains(itemText.toLowerCase()),
                "Should navigate to " + itemText + " page");
        }
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
            if (!href.contains("brasilagritest.com")) {
                String originalWindow = driver.getWindowHandle();
                
                // Open link in new tab
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
                Assertions.assertFalse(driver.getCurrentUrl().contains("brasilagritest.com"),
                    "Should be on external site");
                
                // Close tab and switch back
                driver.close();
                driver.switchTo().window(originalWindow);
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
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")
        ));
        menuButton.click();
        
        // Click logout
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Sair')]")
        ));
        logoutButton.click();
       
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"), 
            "Should redirect to login page after logout");
    }
}