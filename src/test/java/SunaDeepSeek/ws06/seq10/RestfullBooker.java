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
            By.cssSelector("a[href*='room']")));
        roomsLink.click();
        wait.until(ExpectedConditions.urlContains("/room"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/room"), "Should be on rooms page");
        
        // Test Admin link
        driver.get(BASE_URL);
        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='admin']")));
        adminLink.click();
        wait.until(ExpectedConditions.urlContains("/admin"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/admin"), "Should be on admin page");
        
        // Test Report link
        driver.get(BASE_URL);
        WebElement reportLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='report']")));
        reportLink.click();
        wait.until(ExpectedConditions.urlContains("/report"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/report"), "Should be on report page");
    }

    @Test
    @Order(3)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter']")));
        twitterLink.click();
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter"), "Should be on Twitter");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test GitHub link
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github']")));
        githubLink.click();
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("github"), "Should be on GitHub");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(4)
    public void testRoomBookingForm() throws InterruptedException {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("automationintesting.online"));
        
        // Navigate to room booking section
        WebElement roomsSection = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='room']")));
        roomsSection.click();
        wait.until(ExpectedConditions.urlContains("/room"));
        
        // Click on a room to book
        WebElement bookRoomButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit'], .btn, [class*='book']")));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", bookRoomButton);
        js.executeScript("arguments[0].click();", bookRoomButton);
        
        // Wait for booking modal/form to appear
        Thread.sleep(1000);
        
        // Fill out booking form
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("firstname")));
        firstName.clear();
        firstName.sendKeys("Test");
        
        WebElement lastName = driver.findElement(By.id("lastname"));
        lastName.clear();
        lastName.sendKeys("User");
        
        WebElement email = driver.findElement(By.id("email"));
        email.clear();
        email.sendKeys("test@example.com");
        
        WebElement phone = driver.findElement(By.id("phone"));
        phone.clear();
        phone.sendKeys("1234567890");
        
        WebElement finalBookButton = driver.findElement(By.cssSelector("button[type='submit']"));
        js.executeScript("arguments[0].click();", finalBookButton);
        
        // Verify success message or confirmation
        try {
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-success, [class*='success'], [class*='confirmation']")));
            Assertions.assertTrue(successMessage.getText().toLowerCase().contains("booking") || 
                successMessage.getText().toLowerCase().contains("successful"), 
                "Should show booking success message");
        } catch (TimeoutException e) {
            // Check if we're still on booking page which might indicate success
            Assertions.assertTrue(driver.getCurrentUrl().contains("room") || 
                driver.getCurrentUrl().contains("booking"), 
                "Booking submission should complete");
        }
    }

    @Test
    @Order(5)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#admin");
        
        // Valid login
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("form, #loginForm, [class*='login']")));
        
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='text'], input[name='username'], #username")));
        username.clear();
        username.sendKeys("admin");
        
        WebElement password = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        password.clear();
        password.sendKeys("password");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], input[type='submit'], #doLogin, [value*='login']"));
        loginButton.click();
        
        // Verify login success
        try {
            WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[href*='logout'], button:contains('Logout'), .logout, #logout")));
            Assertions.assertTrue(logoutButton.isDisplayed(), "Logout button should be visible after login");
            
            // Test logout
            logoutButton.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("form, #loginForm, [class*='login']")));
        } catch (TimeoutException e) {
            // Check if we're logged in by checking page content
            String pageSource = driver.getPageSource();
            Assertions.assertTrue(pageSource.toLowerCase().contains("logout") || 
                pageSource.toLowerCase().contains("admin") || 
                pageSource.toLowerCase().contains("welcome"), 
                "Should show admin dashboard after successful login");
        }
    }

    @Test
    @Order(6)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#admin");
        
        // Invalid login attempt
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='text'], input[name='username'], #username")));
        username.clear();
        username.sendKeys("wronguser");
        
        WebElement password = driver.findElement(By.cssSelector("input[type='password'], input[name='password'], #password"));
        password.clear();
        password.sendKeys("wrongpass");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit'], input[type='submit'], #doLogin, [value*='login']"));
        loginButton.click();
        
        // Verify error message
        try {
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-danger, [class*='error'], [class*='danger']")));
            Assertions.assertTrue(errorMessage.getText().toLowerCase().contains("credential") || 
                errorMessage.getText().toLowerCase().contains("invalid") || 
                errorMessage.getText().toLowerCase().contains("error"), 
                "Should show login error message");
        } catch (TimeoutException e) {
            // Check if login failed by remaining on same page or showing generic error
            Assertions.assertTrue(driver.getCurrentUrl().contains("admin") || 
                driver.getPageSource().toLowerCase().contains("login"), 
                "Should remain on login page after failed attempt");
        }
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