package geminiPRO.ws05.seq07;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A comprehensive JUnit 5 test suite for the Restful Booker Platform.
 * This suite covers the public-facing booking and contact forms, as well as
 * the admin panel for login, navigation, and verification of created data.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingPlatformComprehensiveTest {

    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_URL = BASE_URL + "#/admin";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // Unique data for this test run
    private static final String UNIQUE_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final String CONTACT_NAME = "Gemini Tester " + UNIQUE_ID;
    private static final String CONTACT_SUBJECT = "Inquiry " + UNIQUE_ID;
    private static final String BOOKING_FIRSTNAME = "Gemini";
    private static final String BOOKING_LASTNAME = "Booker " + UNIQUE_ID;

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    void testContactFormSubmission() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys(CONTACT_NAME);
        driver.findElement(By.id("email")).sendKeys("gemini.tester@example.com");
        driver.findElement(By.id("phone")).sendKeys("12345678901");
        driver.findElement(By.id("subject")).sendKeys(CONTACT_SUBJECT);
        driver.findElement(By.id("description")).sendKeys("This is a test message from an automated suite.");
        driver.findElement(By.id("submitContact")).click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'contact')]/div/h2")));
        String expectedConfirmation = "Thanks for getting in touch " + CONTACT_NAME + "!";
        assertEquals(expectedConfirmation, confirmation.getText(), "Confirmation message should be correct.");
    }

    @Test
    @Order(2)
    void testRoomBookingFlow() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Book this room']"))).click();

        // Select dates: today and tomorrow
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".rbc-calendar")));
        String today = String.valueOf(LocalDate.now().getDayOfMonth());
        String tomorrow = String.valueOf(LocalDate.now().plusDays(1).getDayOfMonth());
        
        // This clicks the first available day and the day after. A more robust solution for complex calendars might be needed.
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class, 'rbc-day-bg') and not(contains(@class, 'rbc-off-range-bg'))][1]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class, 'rbc-day-bg') and not(contains(@class, 'rbc-off-range-bg'))][2]"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstname"))).sendKeys(BOOKING_FIRSTNAME);
        driver.findElement(By.name("lastname")).sendKeys(BOOKING_LASTNAME);
        driver.findElement(By.name("email")).sendKeys("gemini.booker@example.com");
        driver.findElement(By.name("phone")).sendKeys("09876543210");
        driver.findElement(By.xpath("//button[text()='Book']")).click();

        WebElement successTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='Booking Successful!']")));
        assertTrue(successTitle.isDisplayed(), "Booking successful modal title should be visible.");
        
        // Close modal to finish test
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Close']"))).click();
        wait.until(ExpectedConditions.invisibilityOf(successTitle));
    }

    @Test
    @Order(3)
    void testAdminPanelInvalidLogin() {
        driver.get(ADMIN_URL);
        performAdminLogin("admin", "wrongpassword");
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doLogin")));
        // A failed login might keep the button text or show an error. Here it just stays.
        assertTrue(loginButton.isDisplayed(), "Login button should still be visible after a failed login attempt.");
    }

    @Test
    @Order(4)
    void testAdminPanelSuccessfulLoginAndLogout() {
        driver.get(ADMIN_URL);
        performAdminLogin("admin", "password");

        WebElement logoutLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Logout")));
        assertTrue(logoutLink.isDisplayed(), "Logout link should be visible after successful login.");
        assertTrue(driver.getCurrentUrl().contains("#/admin"), "URL should be the admin dashboard.");

        logoutLink.click();
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doLogin")));
        assertTrue(loginButton.isDisplayed(), "Login button should be visible after logout.");
    }

    @Test
    @Order(5)
    void testAdminPanelVerifiesNewMessageAndBooking() {
        driver.get(ADMIN_URL);
        performAdminLogin("admin", "password");
        
        // Verify new message exists
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Messages"))).click();
        wait.until(ExpectedConditions.urlContains("#/admin/messages"));
        
        List<WebElement> messageSubjects = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("[data-testid='message']"), 0));
        boolean messageFound = messageSubjects.stream()
            .anyMatch(msg -> msg.findElement(By.tagName("p")).getText().equals(CONTACT_SUBJECT));
        assertTrue(messageFound, "The contact message submitted earlier should be found in the admin panel.");

        // Verify new booking exists by navigating back to the main admin page (Rooms)
        driver.findElement(By.linkText("Rooms")).click();
        wait.until(ExpectedConditions.urlContains("#/admin/"));

        List<WebElement> bookingRows = driver.findElements(By.cssSelector("[data-testid^='bookingRow']"));
        boolean bookingFound = bookingRows.stream()
            .anyMatch(row -> row.getText().contains(BOOKING_LASTNAME));
        assertTrue(bookingFound, "The new room booking should be found in the admin panel.");
    }

    @Test
    @Order(6)
    void testAdminPanelNavigationLinks() {
        driver.get(ADMIN_URL);
        performAdminLogin("admin", "password");
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Rooms")));
        
        driver.findElement(By.linkText("Branding")).click();
        wait.until(ExpectedConditions.urlContains("#/admin/branding"));
        assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Branding"), "Branding page should load.");
        
        driver.findElement(By.linkText("Report")).click();
        wait.until(ExpectedConditions.urlContains("#/admin/report"));
        assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Report"), "Report page should load.");

        driver.findElement(By.linkText("Rooms")).click();
        wait.until(ExpectedConditions.urlContains("#/admin/")); // Main page is the rooms list
        assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Rooms"), "Rooms page should load.");
    }
    
    @Test
    @Order(7)
    void testExternalFooterLink() {
        String originalWindow = driver.getWindowHandle();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Automation in testing"))).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("shady-meadows.com"));
        assertTrue(driver.getCurrentUrl().contains("shady-meadows.com"), "Should have opened the external Shady Meadows site.");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        
        assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should have returned to the booking platform.");
    }

    // --- Helper Methods ---
    
    private void performAdminLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("doLogin")).click();
    }
}