package SunaDeepSeek.ws06.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Restful-booker-platform demo"));
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should be on homepage");
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Rooms link
        WebElement roomsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='/room/']")));
        roomsLink.click();
        wait.until(ExpectedConditions.urlContains("/room/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/room/"), "Should be on rooms page");
        
        // Test Admin link
        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='/admin/']")));
        adminLink.click();
        wait.until(ExpectedConditions.urlContains("/admin/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/admin/"), "Should be on admin page");
        
        // Test Report link
        WebElement reportLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='/report/']")));
        reportLink.click();
        wait.until(ExpectedConditions.urlContains("/report/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/report/"), "Should be on report page");
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should be on Twitter");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        githubLink.click();
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "Should be on GitHub");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(4)
    public void testRoomBookingForm() {
        driver.get(BASE_URL + "room/");
        
        // Fill out booking form
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstname")));
        firstName.sendKeys("Test");
        
        WebElement lastName = driver.findElement(By.id("lastname"));
        lastName.sendKeys("User");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("test@example.com");
        
        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("1234567890");
        
        WebElement bookButton = driver.findElement(By.cssSelector("button[type='submit']"));
        bookButton.click();
        
        // Verify success message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(successMessage.getText().contains("Booking Successful!"), 
            "Should show booking success message");
    }

    @Test
    @Order(5)
    public void testAdminLogin() {
        driver.get(BASE_URL + "admin/");
        
        // Valid login
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        username.sendKeys("admin");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("password");
        
        WebElement loginButton = driver.findElement(By.id("doLogin"));
        loginButton.click();
        
        // Verify login success
        wait.until(ExpectedConditions.urlContains("/admin/"));
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout")));
        Assertions.assertTrue(logoutButton.isDisplayed(), "Logout button should be visible after login");
        
        // Test logout
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains("/admin/"));
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Should be back to login form after logout");
    }

    @Test
    @Order(6)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "admin/");
        
        // Invalid login attempt
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        username.sendKeys("wronguser");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("wrongpass");
        
        WebElement loginButton = driver.findElement(By.id("doLogin"));
        loginButton.click();
        
        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Bad credentials"), 
            "Should show login error message");
    }

    private void switchToNewWindow(String originalWindow) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
}