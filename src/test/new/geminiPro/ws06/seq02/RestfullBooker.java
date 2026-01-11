package geminiPro.ws06.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A complete JUnit 5 test suite for the AutomationInTesting.online booking platform.
 * This test uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    // Constants for configuration
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // WebDriver and WebDriverWait instances shared across all tests
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- WebDriver Lifecycle ---

    @BeforeAll
    static void setup() {
        // As per requirements, initialize Firefox in headless mode via arguments
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080)); // Set a reasonable window size for headless
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        // Start each test from the home page
        driver.get(BASE_URL);
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    @DisplayName("Should load the page successfully and verify key sections")
    void testPageLoadsSuccessfully() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".room-title")));
        Assertions.assertEquals("Restful-booker-platform", driver.getTitle(), "Page title is incorrect.");
        WebElement welcomeHeader = driver.findElement(By.xpath("//h2[contains(text(), 'Welcome to Shady Meadows')]"));
        Assertions.assertTrue(welcomeHeader.isDisplayed(), "Welcome header should be visible.");
        WebElement contactForm = driver.findElement(By.id("submitContact"));
        Assertions.assertTrue(contactForm.isDisplayed(), "Contact form submit button should be visible.");
    }

    @Test
    @Order(2)
    @DisplayName("Should submit the contact form successfully")
    void testContactFormSuccessfulSubmission() {
        // Scroll to the contact form to ensure it's in view
        WebElement contactFormHeader = driver.findElement(By.xpath("//h2[contains(text(),'Contact')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", contactFormHeader);

        // Arrange & Act
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys("Caio Augusto");
        driver.findElement(By.id("email")).sendKeys("caio.augusto@geminipro.com");
        driver.findElement(By.id("phone")).sendKeys("19999999999");
        driver.findElement(By.id("subject")).sendKeys("Test Inquiry");
        driver.findElement(By.id("description")).sendKeys("This is a test message from an automated test script.");
        driver.findElement(By.id("submitContact")).click();

        // Assert
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(text(),'Thanks for getting in touch')]")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Success message should be displayed after submitting the contact form.");
    }

    @Test
    @Order(3)
    @DisplayName("Should show a validation error for missing subject in contact form")
    void testContactFormErrorOnMissingSubject() {
        // Arrange
        WebElement subjectField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("subject")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", subjectField);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name"))).sendKeys("Caio Augusto");
        driver.findElement(By.id("email")).sendKeys("caio.augusto@geminipro.com");
        driver.findElement(By.id("phone")).sendKeys("19999999999");
        driver.findElement(By.id("description")).sendKeys("This is a test message.");

        // Act
        driver.findElement(By.id("submitContact")).click();

        // Assert - using JavaScript to get the HTML5 validation message
        String validationMessage = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", subjectField);
        Assertions.assertFalse(validationMessage.isEmpty(), "HTML5 validation message should be present for the subject field.");
    }

    @Test
    @Order(4)
    @DisplayName("Should log in and log out of the admin panel successfully")
    void testAdminLoginAndLogoutSuccessfully() {
        // Navigate to admin panel
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='#/admin']"))).click();
        wait.until(ExpectedConditions.urlContains("/#/admin"));

        // Login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("doLogin")).click();

        // Verify successful login
        wait.until(ExpectedConditions.urlContains("/#/admin/"));
        WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='#/logout']")));
        Assertions.assertTrue(logoutButton.isDisplayed(), "Logout button should be visible after login.");

        // Logout
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains("/#/admin"));
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doLogin")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Should be on the admin login page after logout.");
    }

    @Test
    @Order(5)
    @DisplayName("Should show an error for invalid admin credentials")
    void testAdminLoginWithInvalidCredentials() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='#/admin']"))).click();
        wait.until(ExpectedConditions.urlContains("/#/admin"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("wrongpassword");
        driver.findElement(By.id("doLogin")).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login.");
    }

    @Test
    @Order(6)
    @DisplayName("Should successfully book a room for two nights")
    void testRoomBookingSuccessfully() {
        // Open the booking form
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".openBooking"))).click();
        
        // --- Calendar Interaction ---
        // Select the next two available days for booking
        LocalDate today = LocalDate.now();
        LocalDate checkinDate = today.plusDays(1);
        LocalDate checkoutDate = today.plusDays(3);
        
        String checkinDay = String.valueOf(checkinDate.getDayOfMonth());
        String checkoutDay = String.valueOf(checkoutDate.getDayOfMonth());

        // Handle month transitions if necessary
        if (checkinDate.getMonthValue() != today.getMonthValue()) {
             wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rbc-btn-group button.rbc-next-button"))).click();
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class,'rbc-day-bg') and not(contains(@class,'rbc-off-range-bg'))]/following-sibling::div[text()='" + checkinDay + "']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[contains(@class,'rbc-day-bg') and not(contains(@class,'rbc-off-range-bg'))]/following-sibling::div[text()='" + checkoutDay + "']"))).click();
        
        // --- Fill Details ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstname"))).sendKeys("Caio");
        driver.findElement(By.name("lastname")).sendKeys("Augusto");
        driver.findElement(By.name("email")).sendKeys("caio.augusto@geminipro.com");
        driver.findElement(By.name("phone")).sendKeys("19912345678");

        // --- Book and Assert ---
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Book']"))).click();

        WebElement modalTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='Booking Successful!']")));
        Assertions.assertTrue(modalTitle.isDisplayed(), "Booking successful modal title should be visible.");

        WebElement modalBody = driver.findElement(By.cssSelector(".ReactModal__Content .row p"));
        String expectedDates = String.format("%s - %s", checkinDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), checkoutDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Assertions.assertTrue(modalBody.getText().contains(expectedDates), "Booking confirmation should contain the correct dates.");

        // Close the modal
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Close']"))).click();
    }
}