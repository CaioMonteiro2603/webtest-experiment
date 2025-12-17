package geminiPro.ws06.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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

/**
 * A complete JUnit 5 test suite for the AutomationInTesting hotel booking website
 * using Selenium WebDriver with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_URL = BASE_URL + "#/admin";

    // --- Dynamic Test Data ---
    private static String uniqueSubject;

    // --- Selenium WebDriver ---
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    // Contact Form
    private static final By CONTACT_NAME_INPUT = By.id("name");
    private static final By CONTACT_EMAIL_INPUT = By.id("email");
    private static final By CONTACT_PHONE_INPUT = By.id("phone");
    private static final By CONTACT_SUBJECT_INPUT = By.id("subject");
    private static final By CONTACT_MESSAGE_TEXTAREA = By.id("description");
    private static final By CONTACT_SUBMIT_BUTTON = By.id("submitContact");

    // Admin Panel
    private static final By ADMIN_USERNAME_INPUT = By.id("username");
    private static final By ADMIN_PASSWORD_INPUT = By.id("password");
    private static final By ADMIN_LOGIN_BUTTON = By.id("doLogin");
    private static final By ADMIN_LOGOUT_LINK = By.linkText("Logout");

    // Booking Modal
    private static final By BOOKING_FIRSTNAME_INPUT = By.name("firstname");
    private static final By BOOKING_LASTNAME_INPUT = By.name("lastname");
    private static final By BOOKING_EMAIL_INPUT = By.name("email");
    private static final By BOOKING_PHONE_INPUT = By.name("phone");
    private static final By BOOKING_BOOK_BUTTON = By.cssSelector(".book-room");
    private static final By BOOKING_SUCCESS_MODAL = By.cssSelector(".confirmation-modal");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        uniqueSubject = "Inquiry from Gemini Tester " + System.currentTimeMillis();
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testHomePageLoadsAndHeadersCorrect() {
        driver.get(BASE_URL);
        Assertions.assertEquals("Restful-booker-platform demo", driver.getTitle(), "Page title is incorrect.");
        WebElement welcomeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".hotel-description h2")));
        Assertions.assertEquals("Welcome to Shady Meadows B&B", welcomeHeader.getText(), "Welcome header is incorrect.");
    }

    @Test
    @Order(2)
    void testContactFormSuccessfulSubmission() {
        driver.get(BASE_URL);
        // Scroll to the form to ensure it's in view
        WebElement contactForm = driver.findElement(By.id("contactForm"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", contactForm);

        wait.until(ExpectedConditions.visibilityOfElementLocated(CONTACT_NAME_INPUT)).sendKeys("Gemini Tester");
        driver.findElement(CONTACT_EMAIL_INPUT).sendKeys("gemini@test.com");
        driver.findElement(CONTACT_PHONE_INPUT).sendKeys("0123456789012");
        driver.findElement(CONTACT_SUBJECT_INPUT).sendKeys(uniqueSubject);
        driver.findElement(CONTACT_MESSAGE_TEXTAREA).sendKeys("This is an automated test message. Please ignore.");
        driver.findElement(CONTACT_SUBMIT_BUTTON).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'contact-form')]/div/h2[text()='Thanks for getting in touch, Gemini Tester!']")
        ));
        Assertions.assertTrue(successMessage.isDisplayed(), "Contact form submission success message not found.");
    }

    @Test
    @Order(3)
    void testRoomBookingProcess() {
        driver.get(BASE_URL);
        // Find the first "Book this room" button and click it
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".openBooking")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", bookButton);
        bookButton.click();
        
        // Wait for the booking modal and calendar to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".rbc-calendar")));
        
        // Select the next two available, consecutive days for booking
        List<WebElement> availableDays = driver.findElements(By.cssSelector(".rbc-month-view .rbc-day-bg:not(.rbc-off-range-bg)"));
        wait.until(ExpectedConditions.elementToBeClickable(availableDays.get(0))).click();
        wait.until(ExpectedConditions.elementToBeClickable(availableDays.get(1))).click();

        // Fill in booking details
        driver.findElement(BOOKING_FIRSTNAME_INPUT).sendKeys("Gemini");
        driver.findElement(BOOKING_LASTNAME_INPUT).sendKeys("Booker");
        driver.findElement(BOOKING_EMAIL_INPUT).sendKeys("gemini.booker@test.com");
        driver.findElement(BOOKING_PHONE_INPUT).sendKeys("9876543210987");

        driver.findElement(BOOKING_BOOK_BUTTON).click();

        // Assert confirmation
        WebElement confirmationModal = wait.until(ExpectedConditions.visibilityOfElementLocated(BOOKING_SUCCESS_MODAL));
        Assertions.assertTrue(confirmationModal.isDisplayed(), "Booking confirmation modal did not appear.");
        WebElement successHeader = confirmationModal.findElement(By.tagName("h3"));
        Assertions.assertEquals("Booking Successful!", successHeader.getText(), "Booking success message is incorrect.");
        
        // Close the modal
        confirmationModal.findElement(By.cssSelector(".btn-outline-primary")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(BOOKING_SUCCESS_MODAL));
    }

    @Test
    @Order(4)
    void testAdminLoginWithInvalidCredentials() {
        driver.get(ADMIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADMIN_USERNAME_INPUT)).sendKeys("admin");
        driver.findElement(ADMIN_PASSWORD_INPUT).sendKeys("wrongpassword");
        driver.findElement(ADMIN_LOGIN_BUTTON).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message for invalid login was not shown.");
    }
    
    @Test
    @Order(5)
    void testAdminLoginAndMessageVerificationAndLogout() {
        driver.get(ADMIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADMIN_USERNAME_INPUT)).sendKeys("admin");
        driver.findElement(ADMIN_PASSWORD_INPUT).sendKeys("password");
        driver.findElement(ADMIN_LOGIN_BUTTON).click();
        
        // Verify login success
        wait.until(ExpectedConditions.urlContains("#/admin/messages"));
        WebElement messagesHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("messageList")));
        Assertions.assertTrue(messagesHeader.isDisplayed(), "Did not navigate to the messages page after admin login.");
        
        // Verify the message sent in the previous test is present
        By messageLocator = By.xpath("//div[@data-testid='message']//p[contains(text(), '" + uniqueSubject + "')]");
        WebElement submittedMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(messageLocator));
        Assertions.assertTrue(submittedMessage.isDisplayed(), "The message submitted via the contact form was not found in the admin panel.");

        // Logout
        driver.findElement(ADMIN_LOGOUT_LINK).click();
        
        // Verify logout success
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADMIN_LOGIN_BUTTON));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/admin"), "Did not return to the admin login page after logout.");
    }
}