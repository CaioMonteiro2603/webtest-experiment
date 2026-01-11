package Qwen3.ws06.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RestfullBooker {

    private static WebDriver driver;
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
    public void testHomePageLoad() {
        driver.get("https://automationintesting.online/");
        assertEquals("Automation Testing", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("/"));
    }

    @Test
    @Order(2)
    public void testNavigationToBookingPage() {
        driver.get("https://automationintesting.online/");
        WebElement bookingsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bookings")));
        bookingsLink.click();
        assertTrue(driver.getCurrentUrl().contains("/booking"));
        assertEquals("Bookings - Automation Testing", driver.getTitle());
    }

    @Test
    @Order(3)
    public void testBookingFormSubmission() {
        driver.get("https://automationintesting.online/booking");
        
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        WebElement lastNameField = driver.findElement(By.id("lastName"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement phoneField = driver.findElement(By.id("phone"));
        WebElement checkInField = driver.findElement(By.id("checkIn"));
        WebElement checkOutField = driver.findElement(By.id("checkOut"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        emailField.sendKeys("john.doe@example.com");
        phoneField.sendKeys("1234567890");
        checkInField.sendKeys("2023-12-01");
        checkOutField.sendKeys("2023-12-05");
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("Booking submitted successfully"));
    }

    @Test
    @Order(4)
    public void testContactFormSubmission() {
        driver.get("https://automationintesting.online/");
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        
        WebElement nameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement subjectField = driver.findElement(By.id("subject"));
        WebElement messageField = driver.findElement(By.id("message"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        nameField.sendKeys("John Doe");
        emailField.sendKeys("john.doe@example.com");
        subjectField.sendKeys("Test Subject");
        messageField.sendKeys("Test message for contact form");
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("Message sent successfully"));
    }

    @Test
    @Order(5)
    public void testInvalidBookingSubmission() {
        driver.get("https://automationintesting.online/booking");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(errorMessage.getText().contains("Please fill in all required fields"));
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get("https://automationintesting.online/");
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector(".footer a"));
        assertTrue(footerLinks.size() >= 3);
        
        String originalWindow = driver.getWindowHandle();
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty()) {
                link.click();
                
                Set<String> windowHandles = driver.getWindowHandles();
                String newWindow = windowHandles.stream()
                        .filter(w -> !w.equals(originalWindow))
                        .findFirst()
                        .orElse(null);
                
                if (newWindow != null) {
                    driver.switchTo().window(newWindow);
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("github.com") || 
                               currentUrl.contains("linkedin.com") || 
                               currentUrl.contains("twitter.com") || 
                               currentUrl.contains("facebook.com"));
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(7)
    public void testRoomListingAndSorting() {
        driver.get("https://automationintesting.online/");
        WebElement roomsLink = driver.findElement(By.linkText("Rooms"));
        roomsLink.click();
        
        WebElement sortDropdown = driver.findElement(By.id("sortOptions"));
        Select select = new Select(sortDropdown);
        
        select.selectByValue("priceAsc");
        WebElement firstRoom = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".room-item:first-child .price")));
        assertTrue(firstRoom.getText().contains("$"));
        
        select.selectByValue("priceDesc");
        firstRoom = driver.findElement(By.cssSelector(".room-item:first-child .price"));
        assertTrue(firstRoom.getText().contains("$"));
        
        select.selectByValue("nameAsc");
        WebElement firstRoomName = driver.findElement(By.cssSelector(".room-item:first-child .room-name"));
        assertTrue(firstRoomName.getText().length() > 0);
        
        select.selectByValue("nameDesc");
        firstRoomName = driver.findElement(By.cssSelector(".room-item:first-child .room-name"));
        assertTrue(firstRoomName.getText().length() > 0);
    }

    @Test
    @Order(8)
    public void testBookingDetails() {
        driver.get("https://automationintesting.online/booking");
        
       	WebElement firstNameField = driver.findElement(By.id("firstName"));
       	WebElement lastNameField = driver.findElement(By.id("lastName"));
       	WebElement emailField = driver.findElement(By.id("email"));
       	WebElement phoneField = driver.findElement(By.id("phone"));
       	WebElement checkInField = driver.findElement(By.id("checkIn"));
       	WebElement checkOutField = driver.findElement(By.id("checkOut"));
        
        assertTrue(firstNameField.isDisplayed());
        assertTrue(lastNameField.isDisplayed());
        assertTrue(emailField.isDisplayed());
        assertTrue(phoneField.isDisplayed());
        assertTrue(checkInField.isDisplayed());
        assertTrue(checkOutField.isDisplayed());
        
        assertEquals("text", firstNameField.getAttribute("type"));
        assertEquals("email", emailField.getAttribute("type"));
        assertEquals("tel", phoneField.getAttribute("type"));
        assertEquals("date", checkInField.getAttribute("type"));
        assertEquals("date", checkOutField.getAttribute("type"));
    }
}