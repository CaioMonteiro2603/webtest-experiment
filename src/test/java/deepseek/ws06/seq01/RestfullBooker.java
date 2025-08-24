package deepseek.ws06.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class HotelBookingTest {
    private static WebDriver driver;
    private static final String BASE_URL = "https://automationintesting.online/";
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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals("Welcome to Shady Meadows Bed and Breakfast", heading.getText(), 
            "Home page title is incorrect");
    }

    @Test
    @Order(2)
    public void testRoomBookingFlow() {
        driver.get(BASE_URL);
        
        // Navigate to rooms page
        WebElement roomsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("navRooms")));
        roomsLink.click();
        
        // Verify rooms page loads
        wait.until(ExpectedConditions.urlContains("/#/rooms"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/#/rooms"), 
            "Not redirected to rooms page");
            
        // Click book button for first room
        WebElement bookBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Book')]")));
        bookBtn.click();
        
        // Fill booking form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstname")));
        driver.findElement(By.id("firstname")).sendKeys("John");
        driver.findElement(By.id("lastname")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@test.com");
        driver.findElement(By.id("phone")).sendKeys("1234567890");
        
        // Submit booking
        driver.findElement(By.xpath("//button[text()='Book']")).click();
        
        // Verify success message
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(successAlert.getText().contains("Booking Successful!"),
            "Booking confirmation not shown");
    }

    @Test
    @Order(3)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        // Login with valid credentials
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("username")));
        username.sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("doLogin")).click();
        
        // Verify admin dashboard loads
        wait.until(ExpectedConditions.urlContains("/#/admin"));
        WebElement dashboardTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".navbar-brand")));
        Assertions.assertTrue(dashboardTitle.getText().contains("Admin"),
            "Admin dashboard not loaded");
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        testExternalLink("//a[contains(@href,'twitter.com')]", "twitter.com");
        
        // Test Facebook link
        testExternalLink("//a[contains(@href,'facebook.com')]", "facebook.com");
    }

    private void testExternalLink(String xpath, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath(xpath)));
        link.click();

        // Switch to new window
        String newWindow = driver.getWindowHandles()
            .stream()
            .filter(handle -> !handle.equals(originalWindow))
            .findFirst()
            .orElse(null);
        driver.switchTo().window(newWindow);

        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "Link not pointing to expected domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}