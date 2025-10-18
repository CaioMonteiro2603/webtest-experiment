package deepseek.ws06.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class HotelBookingTest {
    private static final String BASE_URL = "https://automationintesting.online/";
    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testHomePageLoading() {
        driver.get(BASE_URL);
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".hotel-header")));
        Assertions.assertTrue(header.getText().contains("Shady Meadows"),
            "Hotel name should be displayed in header");
    }

    @Test
    @Order(2)
    public void testRoomInformation() {
        driver.get(BASE_URL);
        List<WebElement> rooms = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
            By.cssSelector(".room")));
        Assertions.assertTrue(rooms.size() > 0, "At least one room should be displayed");
        
        WebElement firstRoom = rooms.get(0);
        String roomDescription = firstRoom.findElement(By.cssSelector(".room-desc")).getText();
        Assertions.assertFalse(roomDescription.isEmpty(), "Room should have description");
    }

    @Test
    @Order(3)
    public void testBookingForm() {
        driver.get(BASE_URL);
        
        // Open booking form for first room
        WebElement bookBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".room .btn-outline-primary")));
        bookBtn.click();
        
        // Fill booking form
        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".form")));
        form.findElement(By.name("firstname")).sendKeys("John");
        form.findElement(By.name("lastname")).sendKeys("Doe");
        form.findElement(By.name("email")).sendKeys("john.doe@example.com");
        form.findElement(By.name("phone")).sendKeys("1234567890");
        
        // Submit form
        form.findElement(By.cssSelector(".btn-primary")).click();
        
        // Verify success message
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(success.getText().contains("Booking Successful"),
            "Booking success message should be displayed");
    }

    @Test
    @Order(4)
    public void testInvalidBookingForm() {
        driver.get(BASE_URL);
        
        // Open booking form
        WebElement bookBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".room .btn-outline-primary")));
        bookBtn.click();
        
        // Submit empty form
        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".form")));
        form.findElement(By.cssSelector(".btn-primary")).click();
        
        // Verify errors
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(error.isDisplayed(), 
            "Error message should be displayed for invalid form");
    }

    @Test
    @Order(5)
    public void testContactForm() {
        driver.get(BASE_URL);
        driver.findElement(By.cssSelector("#collapseBanner")).click();
        
        WebElement contactForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".contact-form")));
        contactForm.findElement(By.name("name")).sendKeys("Test User");
        contactForm.findElement(By.name("email")).sendKeys("test@example.com");
        contactForm.findElement(By.name("phone")).sendKeys("1234567890");
        contactForm.findElement(By.name("subject")).sendKeys("Test Subject");
        contactForm.findElement(By.name("description")).sendKeys("Test message");
        contactForm.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-success")));
        Assertions.assertTrue(success.isDisplayed(), 
            "Contact form success message should be displayed");
    }

    @Test
    @Order(6)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Test Admin link
        driver.findElement(By.linkText("Admin")).click();
        wait.until(ExpectedConditions.urlContains("admin"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("admin"), 
            "Should navigate to admin page");
        
        // Test API link
        driver.findElement(By.linkText("API")).click();
        wait.until(ExpectedConditions.urlContains("docs/index"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("docs/index"), 
            "Should navigate to API docs");
            
        // Test Report link
        driver.findElement(By.linkText("Report")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("test-report"), 
            "Should navigate to test report");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // GitHub link
        driver.findElement(By.cssSelector("a[href*='github']")).click();
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"),
            "Should open GitHub link in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Twitter link
        driver.findElement(By.cssSelector("a[href*='twitter']")).click();
        switchToNewWindow(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"),
            "Should open Twitter link in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void switchToNewWindow(String originalWindow) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
    }
}