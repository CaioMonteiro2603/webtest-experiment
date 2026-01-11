package geminiPro.ws06.seq09;

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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for automationintesting.online.
 * This suite covers the main user flows like booking a room and sending a message,
 * as well as the admin panel login/logout functionality.
 * It uses Selenium WebDriver with headless Firefox.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private final By bookRoomButton = By.cssSelector("button.btn-primary");
    private final By contactNameInput = By.id("name");
    private final By contactEmailInput = By.id("email");
    private final By contactPhoneInput = By.id("phone");
    private final By contactSubjectInput = By.id("subject");
    private final By contactMessageInput = By.id("description");
    private final By contactSubmitButton = By.id("submitContact");
    private final By contactFormSuccessMessage = By.cssSelector(".contact h2");
    
    private final By adminUsernameInput = By.id("username");
    private final By adminPasswordInput = By.id("password");
    private final By adminLoginButton = By.id("doLogin");
    private final By adminLogoutButton = By.id("logout");

    private final By bookingFirstnameInput = By.name("firstname");
    private final By bookingLastnameInput = By.name("lastname");
    private final By bookingEmailInput = By.name("email");
    private final By bookingPhoneInput = By.name("phone");
    private final By bookingSubmitButton = By.cssSelector("button.book-room");
    private final By bookingConfirmationModal = By.cssSelector(".confirmation-modal");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED headless mode via arguments
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize(); // Maximizing helps with element visibility in headless mode
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.btn-primary")));
    }

    @Test
    @Order(1)
    void testHomePageElementsAreVisible() {
        assertEquals("Restful-booker-platform demo", driver.getTitle(), "Page title is incorrect.");
        assertTrue(driver.findElement(By.cssSelector(".hotel-logo")).isDisplayed(), "Hotel logo should be visible.");
        assertTrue(driver.findElement(By.cssSelector(".jumbotron")).isDisplayed(), "Hero banner should be visible.");
        assertTrue(driver.findElement(contactSubmitButton).isDisplayed(), "Contact form submit button should be visible.");
    }

    @Test
    @Order(2)
    void testSubmitContactMessage() {
        String name = "Gemini Pro";
        String email = "gemini.pro@testing.com";
        String phone = "0123456789012";
        String subject = "Test Inquiry";
        String message = "This is an automated test message. Please disregard.";

        driver.findElement(contactNameInput).sendKeys(name);
        driver.findElement(contactEmailInput).sendKeys(email);
        driver.findElement(contactPhoneInput).sendKeys(phone);
        driver.findElement(contactSubjectInput).sendKeys(subject);
        driver.findElement(contactMessageInput).sendKeys(message);
        driver.findElement(contactSubmitButton).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(contactFormSuccessMessage));
        assertTrue(successMessage.getText().contains(name), "Success message should contain the sender's name.");
        assertTrue(successMessage.getText().contains("Thanks for getting in touch"), "Success message is incorrect.");
    }
    
    @Test
    @Order(3)
    void testRoomBookingFlow() {
        driver.findElement(bookRoomButton).click();
        
        // Select two available dates on the calendar
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".rbc-calendar")));
        List<WebElement> availableDays = driver.findElements(By.cssSelector(".rbc-month-view .rbc-day-bg:not(.rbc-off-range-bg)"));
        // Click the first and second available days to create a 1-night booking
        availableDays.get(0).click();
        availableDays.get(1).click();
        
        // Fill booking details
        driver.findElement(bookingFirstnameInput).sendKeys("Gemini");
        driver.findElement(bookingLastnameInput).sendKeys("Pro");
        driver.findElement(bookingEmailInput).sendKeys("gemini.booking@testing.com");
        driver.findElement(bookingPhoneInput).sendKeys("9876543210987");
        
        driver.findElement(bookingSubmitButton).click();
        
        // Assert confirmation
        WebElement confirmationModal = wait.until(ExpectedConditions.visibilityOfElementLocated(bookingConfirmationModal));
        assertTrue(confirmationModal.findElement(By.tagName("h3")).getText().contains("Booking Successful!"), "Booking success message is incorrect.");
    }
    
    @Test
    @Order(4)
    void testAdminPanelInvalidLogin() {
        driver.get(BASE_URL + "#/admin");
        wait.until(ExpectedConditions.visibilityOfElementLocated(adminLoginButton));
        
        driver.findElement(adminUsernameInput).sendKeys(ADMIN_USERNAME);
        driver.findElement(adminPasswordInput).sendKeys("wrongpassword");
        driver.findElement(adminLoginButton).click();
        
        // The site shows a subtle error. We'll wait for it.
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message for invalid login should be displayed.");
    }
    
    @Test
    @Order(5)
    void testAdminPanelLoginAndLogout() {
        driver.get(BASE_URL + "#/admin");
        wait.until(ExpectedConditions.visibilityOfElementLocated(adminLoginButton));

        // Login
        driver.findElement(adminUsernameInput).sendKeys(ADMIN_USERNAME);
        driver.findElement(adminPasswordInput).sendKeys(ADMIN_PASSWORD);
        driver.findElement(adminLoginButton).click();
        
        // Verify login success
        wait.until(ExpectedConditions.urlContains("#/admin/dashboard"));
        WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(adminLogoutButton));
        assertTrue(logoutButton.isDisplayed(), "Logout button should be visible after successful login.");
        
        // Logout
        logoutButton.click();
        
        // Verify logout success
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "#/admin"));
        assertTrue(driver.findElement(adminLoginButton).isDisplayed(), "Login button should be visible after logout.");
    }
    
    @Test
    @Order(6)
    void testFooterExternalLinks() {
        handleExternalLink(driver.findElement(By.cssSelector("a[href*='github.com/mpeiretti/']")), "github.com");
        handleExternalLink(driver.findElement(By.cssSelector("a[href*='automationintesting.com']")), "automationintesting.com");
    }
    
    /**
     * Handles clicking an external link, verifying the new tab's URL, closing it, and returning control.
     * @param linkElement The WebElement of the link to click.
     * @param expectedDomain The domain expected in the new tab's URL.
     */
    private void handleExternalLink(WebElement linkElement, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(linkElement)).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        
        if (newWindow == null) {
            fail("New window did not open for link with expected domain: " + expectedDomain);
        }
        
        driver.switchTo().window(newWindow);
        wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL of the new tab should contain " + expectedDomain);
        driver.close();
        
        driver.switchTo().window(originalWindow);
        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
        assertEquals("Restful-booker-platform demo", driver.getTitle(), "Should be back on the main page.");
    }
}