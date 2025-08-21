package geminiPro.ws06.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A comprehensive JUnit 5 test suite for the AutomationInTesting hotel booking platform.
 * This suite uses Selenium WebDriver with Firefox in headless mode to test the contact form,
 * room booking process, and admin panel login/logout functionality.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AutomationInTestingTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://automationintesting.online/";

    // --- Locators ---
    // Contact Form
    private static final By CONTACT_NAME_INPUT = By.id("name");
    private static final By CONTACT_EMAIL_INPUT = By.id("email");
    private static final By CONTACT_PHONE_INPUT = By.id("phone");
    private static final By CONTACT_SUBJECT_INPUT = By.id("subject");
    private static final By CONTACT_MESSAGE_TEXTAREA = By.id("description");
    private static final By CONTACT_SUBMIT_BUTTON = By.id("submitContact");
    private static final By CONTACT_SUCCESS_MESSAGE = By.xpath("//h2[contains(text(),'Thanks for getting in touch')]");

    // Room Booking
    private static final By BOOK_THIS_ROOM_BUTTON = By.cssSelector(".openBooking");
    private static final By BOOKING_FIRSTNAME_INPUT = By.name("firstname");
    private static final By BOOKING_LASTNAME_INPUT = By.name("lastname");
    private static final By BOOKING_EMAIL_INPUT = By.name("email");
    private static final By BOOKING_PHONE_INPUT = By.name("phone");
    private static final By BOOKING_SUBMIT_BUTTON = By.cssSelector("button.book-room");
    private static final By BOOKING_SUCCESS_MODAL_TITLE = By.cssSelector("div.confirmation-modal h3");
    private static final By CALENDAR_UI = By.cssSelector(".rbc-calendar");
    private static final By CALENDAR_NEXT_MONTH_BUTTON = By.cssSelector("button.rbc-next-button");

    // Admin Panel
    private static final By ADMIN_USERNAME_INPUT = By.id("username");
    private static final By ADMIN_PASSWORD_INPUT = By.id("password");
    private static final By ADMIN_LOGIN_BUTTON = By.id("doLogin");
    private static final By ADMIN_ERROR_MESSAGE = By.cssSelector(".alert-danger");
    private static final By ADMIN_LOGOUT_LINK = By.linkText("Logout");
    private static final By ADMIN_INBOX_LINK = By.id("message");


    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    @BeforeEach
    void goToBaseUrl() {
        driver.get(BASE_URL);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should load the page and verify its initial state")
    void testPageLoadAndInitialState() {
        Assertions.assertEquals("Restful-booker-platform demo", driver.getTitle(), "The page title is incorrect.");
        WebElement heroImage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("hotel-logoUrl")));
        Assertions.assertTrue(heroImage.isDisplayed(), "The main hotel logo/image is not visible.");
    }
    
    @Test
    @Order(2)
    @DisplayName("Should fail to submit contact form when subject is too short")
    void testContactFormSubmissionError() {
        // Scroll to the form to ensure it's in view
        WebElement contactForm = driver.findElement(CONTACT_NAME_INPUT);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", contactForm);

        wait.until(ExpectedConditions.visibilityOf(contactForm));
        driver.findElement(CONTACT_NAME_INPUT).sendKeys("Test User");
        driver.findElement(CONTACT_EMAIL_INPUT).sendKeys("test@example.com");
        driver.findElement(CONTACT_PHONE_INPUT).sendKeys("12345678901");
        driver.findElement(CONTACT_SUBJECT_INPUT).sendKeys("Hi"); // Too short
        driver.findElement(CONTACT_MESSAGE_TEXTAREA).sendKeys("This message should not be sent.");
        driver.findElement(CONTACT_SUBMIT_BUTTON).click();
        
        // Assert that the success message does NOT appear
        Assertions.assertTrue(driver.findElements(CONTACT_SUCCESS_MESSAGE).isEmpty(), "Success message should not appear for an invalid submission.");
    }

    @Test
    @Order(3)
    @DisplayName("Should submit the contact form successfully")
    void testContactFormSubmissionSuccess() {
        WebElement contactForm = driver.findElement(CONTACT_NAME_INPUT);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", contactForm);
        
        wait.until(ExpectedConditions.visibilityOf(contactForm));
        driver.findElement(CONTACT_NAME_INPUT).sendKeys("Jane Doe");
        driver.findElement(CONTACT_EMAIL_INPUT).sendKeys("jane.doe@example.com");
        driver.findElement(CONTACT_PHONE_INPUT).sendKeys("09876543210");
        driver.findElement(CONTACT_SUBJECT_INPUT).sendKeys("Inquiry about booking");
        driver.findElement(CONTACT_MESSAGE_TEXTAREA).sendKeys("This is a message sent via an automated test.");
        driver.findElement(CONTACT_SUBMIT_BUTTON).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(CONTACT_SUCCESS_MESSAGE));
        Assertions.assertTrue(successMessage.getText().contains("Jane Doe"), "Success message does not contain the user's name.");
    }

    @Test
    @Order(4)
    @DisplayName("Should book a room successfully")
    void testRoomBookingProcessSuccess() {
        // Find the first "Book this room" button and click it
        wait.until(ExpectedConditions.elementToBeClickable(BOOK_THIS_ROOM_BUTTON)).click();
        
        // Wait for the calendar to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(CALENDAR_UI));
        
        // Select check-in and check-out dates for next month
        LocalDate today = LocalDate.now();
        LocalDate checkinDate = today.plusMonths(1).withDayOfMonth(1);
        LocalDate checkoutDate = checkinDate.plusDays(2);
        
        String currentMonthYear = driver.findElement(By.className("rbc-toolbar-label")).getText();
        String targetMonthYear = checkinDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        while (!currentMonthYear.equalsIgnoreCase(targetMonthYear)) {
            driver.findElement(CALENDAR_NEXT_MONTH_BUTTON).click();
            wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.className("rbc-toolbar-label"))));
            currentMonthYear = driver.findElement(By.className("rbc-toolbar-label")).getText();
        }

        driver.findElement(By.xpath("//div[contains(@class, 'rbc-date-cell') and not(contains(@class, 'rbc-off-range'))]/button[text()='" + checkinDate.getDayOfMonth() + "']")).click();
        driver.findElement(By.xpath("//div[contains(@class, 'rbc-date-cell') and not(contains(@class, 'rbc-off-range'))]/button[text()='" + checkoutDate.getDayOfMonth() + "']")).click();

        // Fill in booking details
        driver.findElement(BOOKING_FIRSTNAME_INPUT).sendKeys("Test");
        driver.findElement(BOOKING_LASTNAME_INPUT).sendKeys("Booker");
        driver.findElement(BOOKING_EMAIL_INPUT).sendKeys("test.booker@example.com");
        driver.findElement(BOOKING_PHONE_INPUT).sendKeys("11223344556");

        driver.findElement(BOOKING_SUBMIT_BUTTON).click();

        WebElement successTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(BOOKING_SUCCESS_MODAL_TITLE));
        Assertions.assertEquals("Booking Successful!", successTitle.getText(), "Booking success modal title is incorrect.");
    }
    
    @Test
    @Order(5)
    @DisplayName("Should fail admin login with invalid credentials")
    void testAdminPanelInvalidLogin() {
        driver.get(BASE_URL + "#/admin");
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADMIN_USERNAME_INPUT)).sendKeys("admin");
        driver.findElement(ADMIN_PASSWORD_INPUT).sendKeys("wrongpassword");
        driver.findElement(ADMIN_LOGIN_BUTTON).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ADMIN_ERROR_MESSAGE));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message for invalid admin login did not appear.");
    }

    @Test
    @Order(6)
    @DisplayName("Should successfully log in and log out of the admin panel")
    void testAdminPanelSuccessfulLoginAndLogout() {
        driver.get(BASE_URL + "#/admin");
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADMIN_USERNAME_INPUT)).sendKeys("admin");
        driver.findElement(ADMIN_PASSWORD_INPUT).sendKeys("password");
        driver.findElement(ADMIN_LOGIN_BUTTON).click();

        // Assert successful login by checking for the inbox or logout link
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADMIN_LOGOUT_LINK));
        Assertions.assertTrue(driver.findElement(ADMIN_INBOX_LINK).isDisplayed(), "Admin inbox is not visible after login.");
        
        // Log out
        driver.findElement(ADMIN_LOGOUT_LINK).click();
        
        // Assert successful logout by checking for the login form's username field
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADMIN_USERNAME_INPUT));
        Assertions.assertTrue(driver.findElement(ADMIN_LOGIN_BUTTON).isDisplayed(), "Admin login button is not visible after logout.");
    }
}