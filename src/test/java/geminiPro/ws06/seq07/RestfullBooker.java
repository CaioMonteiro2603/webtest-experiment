package geminiPRO.ws06.seq07;

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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A comprehensive JUnit 5 end-to-end test suite for the Restful Booker Platform.
 * This suite tests the user-facing booking and contact submission flows,
 * as well as the administrator panel for login, data verification, and navigation.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingPlatformE2ETest {

    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_URL = BASE_URL + "#/admin";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // Unique data generated once for the entire test suite execution
    private static final String UNIQUE_RUN_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final String CONTACT_NAME = "Gemini User " + UNIQUE_RUN_ID;
    private static final String CONTACT_SUBJECT = "Test Inquiry " + UNIQUE_RUN_ID;
    private static final String BOOKING_FIRSTNAME = "Gemini";
    private static final String BOOKING_LASTNAME = "Booking " + UNIQUE_RUN_ID;

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED: Use arguments for headless mode
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
        // Most tests navigate to a specific page, so a generic BASE_URL visit is not needed here.
    }

    @Test
    @Order(1)
    void testContactFormEndToEnd() {
        driver.get(BASE_URL);
        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        nameField.sendKeys(CONTACT_NAME);
        driver.findElement(By.id("email")).sendKeys("contact@example.com");
        driver.findElement(By.id("phone")).sendKeys("11122233344");
        driver.findElement(By.id("subject")).sendKeys(CONTACT_SUBJECT);
        driver.findElement(By.id("description")).sendKeys("This is a test message for verification.");
        driver.findElement(By.id("submitContact")).click();

        WebElement confirmationMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class, 'contact')]/div/h2")));
        
        String expectedText = "Thanks for getting in touch " + CONTACT_NAME + "!";
        assertEquals(expectedText, confirmationMessage.getText(), "Confirmation message after form submission is incorrect.");
    }

    @Test
    @Order(2)
    void testRoomBookingEndToEnd() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Book this room']"))).click();
        
        // Select a date range in the next month to ensure availability
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".rbc-calendar")));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Next']"))).click();
        
        // Wait for the next month's calendar to be active and select the 1st and 2nd
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='1']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='2']"))).click();

        WebElement firstnameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstname")));
        firstnameField.sendKeys(BOOKING_FIRSTNAME);
        driver.findElement(By.name("lastname")).sendKeys(BOOKING_LASTNAME);
        driver.findElement(By.name("email")).sendKeys("booking@example.com");
        driver.findElement(By.name("phone")).sendKeys("55566677788");
        driver.findElement(By.xpath("//button[text()='Book']")).click();
        
        WebElement successModalTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='Booking Successful!']")));
        assertTrue(successModalTitle.isDisplayed(), "Booking success modal should be displayed.");
        
        // Cleanly close the modal to end the test
        driver.findElement(By.xpath("//button[text()='Close']")).click();
        wait.until(ExpectedConditions.invisibilityOf(successModalTitle));
    }

    @Test
    @Order(3)
    void testAdminLoginFailure() {
        driver.get(ADMIN_URL);
        performAdminLogin("admin", "invalidPassword");
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doLogin")));
        // Simple assertion: if login succeeds, the button is gone. If it fails, it's still there.
        assertTrue(loginButton.isDisplayed(), "Login button should remain visible after a failed login attempt.");
    }

    @Test
    @Order(4)
    void testAdminLoginAndLogoutSuccess() {
        driver.get(ADMIN_URL);
        performAdminLogin("admin", "password");
        
        WebElement logoutLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Logout")));
        assertTrue(logoutLink.isDisplayed(), "Logout link should be visible after a successful login.");
        
        logoutLink.click();
        
        WebElement loginFormHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='Log into your account']")));
        assertTrue(loginFormHeader.isDisplayed(), "Login form should be visible after logout.");
    }

    @Test
    @Order(5)
    void testAdminPanelDataVerification() {
        driver.get(ADMIN_URL);
        performAdminLogin("admin", "password");
        
        // 1. Verify Message
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Messages"))).click();
        wait.until(ExpectedConditions.urlContains("#/admin/messages"));
        
        List<WebElement> messages = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("[data-testid='message']")));
        
        boolean messageFound = messages.stream()
            .anyMatch(msg -> msg.findElement(By.tagName("p")).getText().equals(CONTACT_SUBJECT));
        assertTrue(messageFound, "Message created in testContactFormEndToEnd should be visible in the admin panel.");
        
        // 2. Verify Booking
        driver.findElement(By.linkText("Rooms")).click();
        wait.until(ExpectedConditions.urlContains("#/admin")); // Rooms is the default admin view
        
        WebElement bookingRow = findBookingRowByLastName(BOOKING_LASTNAME);
        assertTrue(bookingRow.isDisplayed(), "Booking created in testRoomBookingEndToEnd should be visible.");
        assertTrue(bookingRow.getText().contains(BOOKING_FIRSTNAME), "Booking row should contain the correct first name.");
    }

    @Test
    @Order(6)
    void testExternalLinkNavigation() {
        driver.get(BASE_URL);
        String originalWindowHandle = driver.getWindowHandle();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Automation in testing"))).click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindowHandle.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("shady-meadows.com"));
        assertTrue(driver.getCurrentUrl().contains("shady-meadows.com"), "The new window should navigate to the external domain.");
        
        driver.close();
        driver.switchTo().window(originalWindowHandle);
        
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Driver should switch back to the original window.");
    }
    
    // --- Helper Methods ---

    private void performAdminLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("doLogin")).click();
    }
    
    private WebElement findBookingRowByLastName(String lastName) {
        List<WebElement> allRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("[data-testid^='bookingRow']")));
        return allRows.stream()
            .filter(row -> row.getText().contains(lastName))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Booking row with last name '" + lastName + "' not found."));
    }
}