package deepseek.ws10.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
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
        
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[name='email']")));
        email.sendKeys(USERNAME);
        
        WebElement password = driver.findElement(By.cssSelector("input[name='password']"));
        password.sendKeys(PASSWORD);
        
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        loginBtn.click();
        
        WebElement dashboard = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".dashboard-header, .page-header, h1, h2, .content-header")));
        Assertions.assertTrue(dashboard.isDisplayed(), "Dashboard should be visible after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[name='email']")));
        email.clear();
        email.sendKeys("invalid@email.com");
        
        WebElement password = driver.findElement(By.cssSelector("input[name='password']"));
        password.clear();
        password.sendKeys("wrongpassword");
        
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        loginBtn.click();
        
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger, .error-message, .invalid-feedback")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        loginIfNeeded();
        
        // Test Farms navigation
        WebElement farmsMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='farms'], a[href*='farm'], .nav-link[href*='farm']")));
        farmsMenu.click();
        
        WebElement farmsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".page-header, h1, h2, .content-header")));
        Assertions.assertTrue(farmsHeader.getText().toLowerCase().contains("farm"),
            "Farms page should load");
        
        // Test Reports navigation
        WebElement reportsMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='reports'], a[href*='report'], .nav-link[href*='report']")));
        reportsMenu.click();
        
        WebElement reportsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".page-header, h1, h2, .content-header")));
        Assertions.assertTrue(reportsHeader.getText().toLowerCase().contains("report"),
            "Reports page should load");
    }

    @Test
    @Order(4)
    public void testFarmCreation() {
        loginIfNeeded();
        driver.findElement(By.cssSelector("a[href*='farms'], a[href*='farm'], .nav-link[href*='farm']")).click();
        
        WebElement createBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn-primary, .btn-success, button[type='button']")));
        createBtn.click();
        
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[name='name'], input[id='name'], input[placeholder*='name']")));
        nameField.sendKeys("Test Farm");
        
        WebElement saveBtn = driver.findElement(By.cssSelector("button[type='submit'], .btn-save, .btn-primary"));
        saveBtn.click();
        
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success, .success-message, .toast-success")));
        Assertions.assertTrue(success.isDisplayed(),
            "Success message should appear after farm creation");
    }

    @Test
    @Order(5)
    public void testLogout() {
        loginIfNeeded();
        
        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".user-menu, .dropdown-toggle, .user-dropdown")));
        userMenu.click();
        
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='logout'], .logout-btn, .dropdown-item[href*='logout']")));
        logoutBtn.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[name='email'], form[action*='login'], .login-form")));
        Assertions.assertTrue(loginForm.isDisplayed(),
            "Login form should appear after logout");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        loginIfNeeded();
        String originalWindow = driver.getWindowHandle();
        
        // Test Help link
        WebElement helpLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='help'], .help-link, a[target='_blank']")));
        helpLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("brasilagri"),
            "Help link should open correct URL");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("dashboard") && !driver.getCurrentUrl().contains("farm") && !driver.getCurrentUrl().contains("report")) {
            driver.get(BASE_URL);
            try {
                WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[name='email']")));
                email.clear();
                email.sendKeys(USERNAME);
                driver.findElement(By.cssSelector("input[name='password']")).sendKeys(PASSWORD);
                driver.findElement(By.cssSelector("button[type='submit']")).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".dashboard-header, .page-header, h1, h2, .content-header")));
            } catch (Exception e) {
                // Already logged in or different page structure
            }
        }
    }
}