package geminiPro.ws06.seq04;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A comprehensive JUnit 5 test suite for the 'automationintesting.online' website.
 * This suite uses Selenium WebDriver with Firefox in headless mode to test key user flows,
 * including contact form submission, room booking, and admin panel authentication.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_URL = BASE_URL + "#/admin";

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use argument for headless mode as required
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHomepage() {
        driver.get(BASE_URL);
        // Wait for a key element of the homepage to be visible to ensure the page is loaded
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".hotel-logo")));
    }

    @Test
    @Order(1)
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testHomePageLoadsSuccessfully() {
        assertEquals("Restful-booker-platform", driver.getTitle(), "The page title is incorrect.");
        WebElement welcomeHeader = driver.findElement(By.cssSelector(".jumbotron h1"));
        assertTrue(welcomeHeader.isDisplayed() && welcomeHeader.getText().contains("Welcome to Shady Meadows"),
            "The main welcome header is not visible or has incorrect text.");
        assertTrue(driver.findElement(By.id("submitContact")).isDisplayed(), "The contact form submit button should be visible.");
    }

    @Test
    @Order(2)
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void testContactFormSubmissionSuccess() {
        // Generate unique data for the submission
        String uniqueName = "TestUser " + UUID.randomUUID().toString().substring(0, 8);
        String subject = "Inquiry about booking";
        String message = "This is an automated test message. Please disregard.";

        // Fill out and submit the form
        driver.findElement(By.id("name")).sendKeys(uniqueName);
        driver.findElement(By.id("email")).sendKeys("test@example.com");
        driver.findElement(By.id("phone")).sendKeys("12345678901");
        driver.findElement(By.id("subject")).sendKeys(subject);
        driver.findElement(By.id("description")).sendKeys(message);
        driver.findElement(By.id("submitContact")).click();

        // Wait for and verify the success message
        WebElement successMessageHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".contact-form-submission h2")));

        String expectedHeaderText = "Thanks for getting in touch, " + uniqueName + "!";
        assertEquals(expectedHeaderText, successMessageHeader.getText(), "The success message header text is incorrect.");
    }

    @Test
    @Order(3)
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testContactFormShowsValidationErrors() {
        // Attempt to submit an empty form
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submitContact")));
        submitButton.click();

        // Wait for multiple validation error messages to appear
        List<WebElement> errorMessages = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.cssSelector(".alert.alert-danger"), 3));

        assertFalse(errorMessages.isEmpty(), "Validation error messages should be displayed for an empty form.");

        // Assert that specific error messages are present
        assertTrue(errorMessages.stream().anyMatch(e -> e.getText().contains("must not be blank")),
            "Expected 'must not be blank' validation error.");
        assertTrue(errorMessages.stream().anyMatch(e -> e.getText().contains("size must be between")),
            "Expected 'size must be between' validation error.");
    }
    
    @Test
    @Order(4)
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testRoomBookingFlow() {
        // Start the booking process by clicking the first available "Book this room" button
        WebElement bookRoomButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".openBooking")));
        bookRoomButton.click();
        
        // Fill out the booking details in the form that appears
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstname"))).sendKeys("Gemini");
        driver.findElement(By.name("lastname")).sendKeys("Test");
        driver.findElement(By.name("email")).sendKeys("gemini.test@example.com");
        driver.findElement(By.name("phone")).sendKeys("09876543210");
        
        // Click the final book button
        driver.findElement(By.cssSelector(".book-room-button")).click();
        
        // Wait for the confirmation modal and verify its content
        WebElement confirmationModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".confirmation-modal")));
        WebElement modalHeader = confirmationModal.findElement(By.cssSelector(".modal-header h3"));
        
        assertEquals("Booking Successful!", modalHeader.getText(), "Booking confirmation header is incorrect.");
        
        // Close the modal and assert its disappearance
        confirmationModal.findElement(By.cssSelector(".modal-footer button")).click();
        wait.until(ExpectedConditions.invisibilityOf(confirmationModal));
        
        // A simple check to ensure the modal is gone
        assertThrows(NoSuchElementException.class, () -> driver.findElement(By.cssSelector(".confirmation-modal")),
                "The confirmation modal should no longer be present in the DOM after closing.");
    }

    @Test
    @Order(5)
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void testAdminLoginSuccess() {
        driver.get(ADMIN_URL);
        performAdminLogin("admin", "password");

        // After successful login, the logout link should appear
        WebElement logoutLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("a[href='#/admin/logout']")));

        assertTrue(driver.getCurrentUrl().contains("#/admin"), "URL should indicate being in the admin section.");
        assertTrue(logoutLink.isDisplayed(), "Logout link should be visible after a successful login.");
    }

    @Test
    @Order(6)
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testAdminLoginFailure() {
        driver.get(ADMIN_URL);
        performAdminLogin("admin", "wrongpassword");

        // On failure, the app stays on the login page and shows no error message.
        // We assert that the post-login element (logout link) does NOT appear.
        assertThrows(TimeoutException.class, () -> {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("a[href='#/admin/logout']")));
        }, "Logout link should not be visible after a failed login attempt.");

        // Verify that we are still on the login page
        assertTrue(driver.findElement(By.id("username")).isDisplayed(),
            "Username field should still be visible after a failed login attempt.");
    }

    @Test
    @Order(7)
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testAdminLogout() {
        // First, log in successfully
        driver.get(ADMIN_URL);
        performAdminLogin("admin", "password");

        // Wait for logout link and click it
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='#/admin/logout']")));
        logoutLink.click();

        // After logout, the login form should reappear
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        assertTrue(usernameField.isDisplayed(), "Username field should be visible after logging out.");
        assertTrue(driver.getCurrentUrl().contains("#/admin"), "URL should remain on the admin login page after logout.");
    }

    /**
     * Helper method to perform the login action on the admin page.
     * @param username The username to enter.
     * @param password The password to enter.
     */
    private void performAdminLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("doLogin")).click();
    }
}