package geminiPRO.ws06.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AutomationInTestingTest {

    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_URL = BASE_URL + "#/admin";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    // Credentials for the admin panel
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "password";

    // Unique data for the end-to-end test
    private static String uniqueSubject;

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);

        // Generate a unique subject line for the message test
        uniqueSubject = "Test Inquiry " + System.currentTimeMillis();
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
    }

    private void assertExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        // Scroll element into view to avoid clicks being intercepted
        WebElement link = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(wait.until(ExpectedConditions.urlContains(expectedDomain)), "URL should contain " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    @DisplayName("Verify Home Page Loads Correctly")
    void testHomePageLoadsCorrectly() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("hotel-logo")));
        assertEquals("Restful-booker-platform", driver.getTitle(), "Page title should be correct.");
        List<WebElement> rooms = driver.findElements(By.className("room"));
        assertFalse(rooms.isEmpty(), "Room listings should be present on the home page.");
    }

    @Test
    @Order(2)
    @DisplayName("Submit Contact Form and Verify Success Message")
    void testContactFormSubmission() {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nameInput);

        nameInput.sendKeys("Test User");
        driver.findElement(By.id("email")).sendKeys("test@example.com");
        driver.findElement(By.id("phone")).sendKeys("12345678901");
        driver.findElement(By.id("subject")).sendKeys("General Inquiry");
        driver.findElement(By.id("description")).sendKeys("This is a test message.");
        driver.findElement(By.id("submitContact")).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class, 'contact')]/div/h2[text()='Thanks for getting in touch!']")
        ));
        assertTrue(successMessage.isDisplayed(), "Success message should be displayed after form submission.");
    }

    @Test
    @Order(3)
    @DisplayName("Attempt Admin Login with Invalid Credentials")
    void testAdminLoginWithInvalidCredentials() {
        driver.get(ADMIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(ADMIN_USER);
        driver.findElement(By.id("password")).sendKeys("wrongpassword");
        driver.findElement(By.id("doLogin")).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be shown for invalid login.");
    }

    @Test
    @Order(4)
    @DisplayName("Perform Admin Login and Logout")
    void testAdminLoginAndLogout() {
        driver.get(ADMIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(ADMIN_USER);
        driver.findElement(By.id("password")).sendKeys(ADMIN_PASS);
        driver.findElement(By.id("doLogin")).click();

        WebElement logoutLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Logout")));
        assertTrue(logoutLink.isDisplayed(), "Logout link should be visible after successful login.");
        
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doLogin")));
        assertTrue(driver.getCurrentUrl().contains("#/admin"), "Should be returned to the admin login page after logout.");
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Submit Message and Verify in Admin Panel")
    void testEndToEndMessageVerification() {
        // Step 1: Submit the contact form with a unique subject
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nameInput);
        nameInput.sendKeys("E2E Test");
        driver.findElement(By.id("email")).sendKeys("e2e@test.com");
        driver.findElement(By.id("phone")).sendKeys("09876543210");
        driver.findElement(By.id("subject")).sendKeys(uniqueSubject);
        driver.findElement(By.id("description")).sendKeys("Verify this message appears in admin panel.");
        driver.findElement(By.id("submitContact")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h2[text()='Thanks for getting in touch!']")
        ));

        // Step 2: Login to the admin panel
        driver.get(ADMIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(ADMIN_USER);
        driver.findElement(By.id("password")).sendKeys(ADMIN_PASS);
        driver.findElement(By.id("doLogin")).click();

        // Step 3: Verify the message exists
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("message"))); // Wait for at least one message to load
        
        // Find the specific message by its unique subject
        By messageLocator = By.xpath("//div[@data-testid='message' and .//p[contains(text(), '" + uniqueSubject + "')]]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(messageLocator));
        
        WebElement foundMessage = driver.findElement(messageLocator);
        assertTrue(foundMessage.isDisplayed(), "The submitted message with unique subject '" + uniqueSubject + "' should be visible in the admin panel.");
    }
    
    @Test
    @Order(6)
    @DisplayName("Complete a Room Booking Flow")
    void testRoomBookingFlow() {
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".room-booking-form .btn-primary")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", bookButton);
        bookButton.click();

        // Select dates - click the next two available days
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rbc-calendar .rbc-button-link:not(.rbc-disabled)"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".rbc-calendar .rbc-button-link:not(.rbc-disabled)"))).click();
        
        // Fill booking details
        driver.findElement(By.name("firstname")).sendKeys("Gemini");
        driver.findElement(By.name("lastname")).sendKeys("Pro");
        driver.findElement(By.name("email")).sendKeys("gemini.pro.booking@test.com");
        driver.findElement(By.name("phone")).sendKeys("11234567890");
        
        driver.findElement(By.cssSelector(".book-room .btn-primary")).click();

        // Assert confirmation modal
        WebElement modalTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".confirmation-modal .modal-title")));
        assertEquals("Booking Successful!", modalTitle.getText(), "Booking confirmation modal title is incorrect.");
    }

    @Test
    @Order(7)
    @DisplayName("Verify GitHub External Link in Footer")
    void testGitHubExternalLink() {
        assertExternalLink(By.partialLinkText("GitHub"), "github.com");
    }
}