package deepseek.ws10.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgriTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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
    public void testLoginPageLoads() {
        driver.get(BASE_URL);
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("form")));
        assertTrue(loginForm.isDisplayed(), "Login form should be displayed");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[name='email']")));
        emailField.sendKeys(USERNAME);
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        passwordField.sendKeys(PASSWORD);
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".dashboard")));
        assertTrue(dashboard.isDisplayed(), "Dashboard should be visible after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[name='email']")));
        emailField.sendKeys("invalid@email.com");
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        passwordField.sendKeys("wrongpassword");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(4)
    public void testDashboardNavigation() {
        login();
        
        WebElement reportsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='reports']")));
        reportsLink.click();
        
        WebElement reportsPage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".reports-page")));
        assertTrue(reportsPage.isDisplayed(), "Reports page should load");
    }

    @Test
    @Order(5)
    public void testUserManagement() {
        login();
        
        WebElement usersLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='users']")));
        usersLink.click();
        
        WebElement usersTable = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".users-table")));
        assertTrue(usersTable.isDisplayed(), "Users table should be visible");
        
        WebElement addUserButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn-primary")));
        addUserButton.click();
        
        WebElement newUserForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".user-form")));
        assertTrue(newUserForm.isDisplayed(), "New user form should appear");
    }

    @Test
    @Order(6)
    public void testSettingsPage() {
        login();
        
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='settings']")));
        settingsLink.click();
        
        WebElement settingsForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".settings-form")));
        assertTrue(settingsForm.isDisplayed(), "Settings form should appear");
    }

    @Test
    @Order(7)
    public void testLogout() {
        login();
        
        WebElement profileMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".user-dropdown")));
        profileMenu.click();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='logout']")));
        logoutButton.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("form")));
        assertTrue(loginForm.isDisplayed(), "Login form should appear after logout");
    }

    @Test
    @Order(8)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Assuming there's a footer link - adjust selector as needed
        try {
            WebElement externalLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("footer a")));
            testExternalLink(externalLink, "brasilagri");
        } catch (TimeoutException e) {
            System.out.println("No external links found to test");
        }
    }

    private void login() {
        if (!driver.getCurrentUrl().contains("dashboard")) {
            driver.get(BASE_URL);
            WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='email']")));
            emailField.sendKeys(USERNAME);
            
            WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
            passwordField.sendKeys(PASSWORD);
            
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
            loginButton.click();
            
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".dashboard")));
        }
    }

    private void testExternalLink(WebElement link, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "External link should open " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}