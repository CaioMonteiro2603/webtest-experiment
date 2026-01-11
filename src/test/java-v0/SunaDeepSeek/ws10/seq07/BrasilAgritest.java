package SunaDeepSeek.ws10.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
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
    public void testLoginPage() {
        driver.get(BASE_URL);
        
        // Verify login page elements
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        Assertions.assertTrue(usernameField.isDisplayed(), "Username field should be visible");
        Assertions.assertTrue(passwordField.isDisplayed(), "Password field should be visible");
        Assertions.assertTrue(loginButton.isDisplayed(), "Login button should be visible");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        // Verify successful login by checking dashboard elements
        WebElement dashboardHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1.dashboard-title")));
        Assertions.assertTrue(dashboardHeader.isDisplayed(), "Dashboard should be visible after login");
        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard"), "URL should contain 'dashboard' after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(4)
    public void testNavigationMenu() {
        // Ensure we're logged in
        testSuccessfulLogin();
        
        // Test menu button
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();
        
        // Verify menu items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory-link")));
        WebElement aboutLink = driver.findElement(By.id("about-link"));
        WebElement logoutLink = driver.findElement(By.id("logout-link"));
        WebElement resetLink = driver.findElement(By.id("reset-link"));
        
        Assertions.assertTrue(allItemsLink.isDisplayed(), "All Items link should be visible");
        Assertions.assertTrue(aboutLink.isDisplayed(), "About link should be visible");
        Assertions.assertTrue(logoutLink.isDisplayed(), "Logout link should be visible");
        Assertions.assertTrue(resetLink.isDisplayed(), "Reset link should be visible");
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        // Ensure we're logged in
        testSuccessfulLogin();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();
        
        // Test About link (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about-link")));
        aboutLink.click();
        
        // Switch to new tab and verify URL
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"), "About page URL should contain 'about'");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test social media links in footer
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".footer-social a"));
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href.contains("twitter") || href.contains("facebook") || href.contains("linkedin")) {
                link.click();
                
                // Switch to new tab and verify domain
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!originalWindow.equals(windowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                if (href.contains("twitter")) {
                    Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should be on Twitter domain");
                } else if (href.contains("facebook")) {
                    Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Should be on Facebook domain");
                } else if (href.contains("linkedin")) {
                    Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "Should be on LinkedIn domain");
                }
                
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        // Ensure we're logged in
        testSuccessfulLogin();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();
        
        // Click reset link
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset-link")));
        resetLink.click();
        
        // Verify reset confirmation
        WebElement resetMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".reset-confirmation")));
        Assertions.assertTrue(resetMessage.isDisplayed(), "Reset confirmation message should be displayed");
    }

    @Test
    @Order(7)
    public void testLogout() {
        // Ensure we're logged in
        testSuccessfulLogin();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();
        
        // Click logout link
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout-link")));
        logoutLink.click();
        
        // Verify we're back on login page
        WebElement loginButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("login-button")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Should be back on login page after logout");
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "URL should contain 'login' after logout");
    }
}