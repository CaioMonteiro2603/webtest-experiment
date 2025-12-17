package geminiPro.ws06.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * JUnit 5 test suite for the automationintesting.online platform.
 * This suite uses Selenium WebDriver with headless Firefox to test the end-to-end user
 * journeys of booking a room and sending a contact message, including verification
 * in the admin panel. It also tests the admin login functionality.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_URL = BASE_URL + "#/admin";

    // --- Locators ---
    private final By contactNameInput = By.id("name");
    private final By contactEmailInput = By.id("email");
    private final By contactPhoneInput = By.id("phone");
    private final By contactSubjectInput = By.id("subject");
    private final By contactMessageInput = By.id("description");
    private final By submitContactButton = By.id("submitContact");

    private final By adminUsernameInput = By.id("username");
    private final By adminPasswordInput = By.id("password");
    private final By adminLoginButton = By.id("doLogin");
    private final By adminLogoutButton = By.linkText("Logout");
    
    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setup() {
        driver.get(BASE_URL);
    }
    
    /**
     * Helper method to log into the admin panel.
     */
    private void performAdminLogin() {
        driver.get(ADMIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(adminUsernameInput)).sendKeys("admin");
        driver.findElement(adminPasswordInput).sendKeys("password");
        driver.findElement(adminLoginButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(adminLogoutButton));
    }

    @Test
    @Order(1)
    @DisplayName("Should submit a contact message and verify it in the admin panel")
    void testContactFormSubmissionAndAdminVerification() {
        long timestamp = System.currentTimeMillis();
        String uniqueName = "Gemini User " + timestamp;
        String uniqueSubject = "Inquiry " + timestamp;

        // --- Submit Contact Form ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(contactNameInput)).sendKeys(uniqueName);
        driver.findElement(contactEmailInput).sendKeys("gemini.user@test.com");
        driver.findElement(contactPhoneInput).sendKeys("01234567890");
        driver.findElement(contactSubjectInput).sendKeys(uniqueSubject);
        driver.findElement(contactMessageInput).sendKeys("This is a test inquiry. Please disregard.");
        driver.findElement(submitContactButton).click();

        // --- Assert Submission Confirmation ---
        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class, 'contact')]/div/h2")
        ));
        Assertions.assertTrue(
            confirmation.getText().contains("Thanks for getting in touch " + uniqueName + "!"),
            "Confirmation message is incorrect."
        );

        // --- Verify in Admin Panel ---
        performAdminLogin();
        // The default view after admin login shows messages. We just need to find our message.
        By messageRowLocator = By.xpath(
            "//div[@data-testid='message' and contains(., '" + uniqueSubject + "')]"
        );
        WebElement messageRow = wait.until(ExpectedConditions.visibilityOfElementLocated(messageRowLocator));
        Assertions.assertTrue(messageRow.isDisplayed(), "The submitted message was not found in the admin panel.");
        Assertions.assertTrue(messageRow.getText().contains(uniqueName), "Message row does not contain the correct name.");
    }
    
    @Test
    @Order(2)
    @DisplayName("Should book a room and verify the booking in the admin panel")
    void testRoomBookingAndAdminVerification() {
        long timestamp = System.currentTimeMillis();
        String firstName = "Gemini";
        String lastName = "Booker" + timestamp;
        
        // --- Start Booking Process ---
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Book this room']"))).click();
        
        // --- Select Dates ---
        // A simple approach: click the next available Monday and then Wednesday.
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rbc-calendar"))).click();
        // Assuming the calendar opens on the current month.
        // This clicks the *next* available Monday, making the test more robust against running on a weekend.
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[contains(@class, 'rbc-row-segment')][1]//button[text()='15'])[1]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[contains(@class, 'rbc-row-segment')][1]//button[text()='17'])[1]"))).click();

        // --- Fill Details and Confirm Booking ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstname"))).sendKeys(firstName);
        driver.findElement(By.name("lastname")).sendKeys(lastName);
        driver.findElement(By.name("email")).sendKeys("gemini.booker@test.com");
        driver.findElement(By.name("phone")).sendKeys("11987654321");
        driver.findElement(By.xpath("//button[text()='Book']")).click();
        
        // --- Assert Booking Confirmation ---
        WebElement confirmationModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".confirmation-modal")));
        Assertions.assertTrue(
            confirmationModal.findElement(By.tagName("h3")).getText().contains("Booking Successful!"),
            "Booking confirmation modal title is incorrect."
        );
        // Close the modal
        confirmationModal.findElement(By.xpath("//button[text()='Close']")).click();
        wait.until(ExpectedConditions.invisibilityOf(confirmationModal));

        // --- Verify in Admin Panel ---
        performAdminLogin();
        driver.get(BASE_URL + "#/admin"); // Navigate to the report page which shows bookings
        
        By bookingRowLocator = By.xpath("//div[@data-testid='booking-row' and contains(., '" + lastName + "')]");
        WebElement bookingRow = wait.until(ExpectedConditions.visibilityOfElementLocated(bookingRowLocator));
        
        Assertions.assertTrue(bookingRow.isDisplayed(), "The new booking was not found in the admin panel.");
        Assertions.assertTrue(bookingRow.getText().contains(firstName), "Booking row does not contain the correct first name.");
    }

    @Test
    @Order(3)
    @DisplayName("Should show an error message for invalid admin login")
    void testAdminInvalidLogin() {
        driver.get(ADMIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(adminUsernameInput)).sendKeys("admin");
        driver.findElement(adminPasswordInput).sendKeys("wrongpassword");
        driver.findElement(adminLoginButton).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert.alert-danger")
        ));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message for invalid login should be visible.");
    }

    @Test
    @Order(4)
    @DisplayName("Should successfully log in and out of the admin panel")
    void testAdminSuccessfulLoginAndLogout() {
        performAdminLogin();
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/admin"), "URL should be the admin dashboard after login.");
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(adminLogoutButton));
        Assertions.assertTrue(logoutButton.isDisplayed(), "Logout button should be visible after login.");
        
        logoutButton.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(adminUsernameInput));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/admin/login"), "URL should be the admin login page after logout.");
        Assertions.assertTrue(driver.findElement(adminUsernameInput).isDisplayed(), "Username input should be visible after logout.");
    }
}