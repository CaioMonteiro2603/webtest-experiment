package deepseek.ws06.seq03;

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
    public void testRoomBooking() {
        driver.get(BASE_URL);
        
        // Wait for page to fully load
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete'"));
        
        // Click calendar icon first to open date picker
        WebElement checkInIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rbc-date-picker .rbc-date-picker-button")));
        checkInIcon.click();
        WebElement nextDay = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@class='day'][not(contains(@class,'disabled'))][1]")));
        nextDay.click();

        // Wait a moment for date selection to process
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        // Click calendar icon for check-out
        WebElement checkOutIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rbc-date-picker:nth-of-type(2) .rbc-date-picker-button")));
        checkOutIcon.click();
        WebElement thirdDay = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@class='day'][not(contains(@class,'disabled'))][3]")));
        thirdDay.click();

        // Book the room
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.book-room")));
        bookButton.click();

        // Fill booking form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstname"))).sendKeys("John");
        driver.findElement(By.id("lastname")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("phone")).sendKeys("1234567890");
        driver.findElement(By.cssSelector(".btn.btn-outline-primary.float-right")).click();

        // Verify successful booking
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        Assertions.assertTrue(successAlert.getText().contains("Booking Successful!"));
    }

    @Test
    @Order(2)
    public void testContactForm() {
        driver.get(BASE_URL);
        
        // Wait for page to fully load
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete'"));
        
        // Open contact form
        WebElement contactButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-target='#contactModal']")));
        contactButton.click();

        // Fill contact form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys("Test User");
        driver.findElement(By.id("email")).sendKeys("test@example.com");
        driver.findElement(By.id("phone")).sendKeys("1234567890");
        driver.findElement(By.id("subject")).sendKeys("Test Subject");
        driver.findElement(By.id("description")).sendKeys("This is a test message");
        driver.findElement(By.id("submitContact")).click();

        // Verify success message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".contact")));
        Assertions.assertTrue(successMessage.getText().contains("Thanks for getting in touch"));
    }

    @Test
    @Order(3)
    public void testInvalidBookingDates() {
        driver.get(BASE_URL);
        
        // Wait for page to fully load
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete'"));
        
        // Try to book with same check-in and check-out date
        WebElement checkInIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rbc-date-picker .rbc-date-picker-button")));
        checkInIcon.click();
        WebElement sameDay = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@class='day'][not(contains(@class,'disabled'))][1]")));
        sameDay.click();

        // Wait a moment for date selection to process
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        WebElement checkOutIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rbc-date-picker:nth-of-type(2) .rbc-date-picker-button")));
        checkOutIcon.click();
        WebElement sameDayOut = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@class='day'][not(contains(@class,'disabled'))][1]")));
        sameDayOut.click();

        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("must be after Check In"));
    }

    @Test
    @Order(4)
    public void testRoomInformation() {
        driver.get(BASE_URL);
        
        // Wait for page to fully load
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete'"));
        
        // Click on first room details
        WebElement roomDetails = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".room-details")));
        roomDetails.click();

        // Verify room details are displayed
        WebElement roomInfo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".hotel-room-info")));
        Assertions.assertTrue(roomInfo.isDisplayed());
        Assertions.assertTrue(driver.findElement(By.cssSelector(".room-details")).isDisplayed());
    }

    @Test
    @Order(5)
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        
        // Wait for page to fully load
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete'"));
        
        // Test About link
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='about']")));
        aboutLink.click();
        Assertions.assertTrue(wait.until(ExpectedConditions.urlContains("about")));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".about-content")).isDisplayed());

        // Test Rooms link
        WebElement roomsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='rooms']")));
        roomsLink.click();
        Assertions.assertTrue(wait.until(ExpectedConditions.urlContains("rooms")));
        
        // Test Home link by clicking logo
        WebElement logoLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-brand")));
        logoLink.click();
        Assertions.assertTrue(wait.until(ExpectedConditions.urlContains("index.html")) || wait.until(ExpectedConditions.urlContains(BASE_URL.endsWith("/") ? BASE_URL.substring(0, BASE_URL.length()-1) : BASE_URL)));
    }

    @Test
    @Order(6)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        // Wait for login form to load
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete'"));
        
        // Fill invalid credentials
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys("invalid");
        driver.findElement(By.id("password")).sendKeys("wrong");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Bad credentials"));
    }

    @Test
    @Order(7)
    public void testPrivacyPolicyLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        // Wait for page to fully load
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete'"));
        
        // Try to find privacy policy link with broader selector
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='privacy']")));
        privacyLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("privacy-policy"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}