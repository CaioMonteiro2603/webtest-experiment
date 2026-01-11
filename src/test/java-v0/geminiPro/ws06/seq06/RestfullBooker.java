package geminiPro.ws06.seq06;

import org.junit.jupiter.api.AfterAll;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 test suite for the automationintesting.online demo website.
 * This suite covers the user-facing room booking flow, the contact form,
 * and the admin panel login and message verification.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_URL = BASE_URL + "#/admin";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "password";

    // Static variable to share state between tests (contact form -> admin panel)
    private static String contactFormName = "GeminiPro_" + System.currentTimeMillis();
    private static String contactFormSubject = "Test Inquiry";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use headless mode via arguments ONLY
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Tests the contact form submission on the main page.
     */
    @Test
    @Order(1)
    void contactFormSubmissionTest() {
        driver.get(BASE_URL);

        // Fill out the contact form
        driver.findElement(By.id("name")).sendKeys(contactFormName);
        driver.findElement(By.id("email")).sendKeys("gemini.pro@test.com");
        driver.findElement(By.id("phone")).sendKeys("12345678901");
        driver.findElement(By.id("subject")).sendKeys(contactFormSubject);
        driver.findElement(By.id("description")).sendKeys("This is a test message from an automated script.");

        // Submit and verify success message
        wait.until(ExpectedConditions.elementToBeClickable(By.id("submitContact"))).click();
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class, 'contact-form')]/div/h2")));

        assertTrue(successMessage.getText().contains("Thanks for getting in touch"), "Contact form success message is incorrect.");
    }

    /**
     * Tests the complete room booking flow from date selection to confirmation.
     */
    @Test
    @Order(2)
    void roomBookingFlowTest() {
        driver.get(BASE_URL);

        // Open the booking calendar for the first room
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".openBooking"))).click();

        // Select dates (e.g., next available 2 days)
        // This clicks the 23rd and 24th, assuming they are available. A more robust solution
        // would calculate future dates, but this is sufficient for this demo site.
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='23']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='24']"))).click();

        // Fill in booking details
        driver.findElement(By.name("firstname")).sendKeys("Gemini");
        driver.findElement(By.name("lastname")).sendKeys("Pro");
        driver.findElement(By.name("email")).sendKeys("gemini.booking@test.com");
        driver.findElement(By.name("phone")).sendKeys("09876543210");

        // Click the final Book button
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".book-room"))).click();
        
        // Assert booking success
        WebElement successModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".confirmation-modal")));
        WebElement successHeader = successModal.findElement(By.tagName("h3"));
        assertEquals("Booking Successful!", successHeader.getText(), "Booking success modal header is incorrect.");
        
        // Close the modal
        successModal.findElement(By.xpath(".//button[text()='Close']")).click();
        wait.until(ExpectedConditions.invisibilityOf(successModal));
    }

    /**
     * Tests the admin panel login with both invalid and valid credentials.
     */
    @Test
    @Order(3)
    void adminLoginFunctionalityTest() {
        driver.get(ADMIN_URL);

        // Test failed login
        performAdminLogin(ADMIN_USER, "wrongpassword");
        WebElement errorPane = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-danger")));
        assertTrue(errorPane.isDisplayed(), "Error message should be displayed for invalid login.");

        // Test successful login
        performAdminLogin(ADMIN_USER, ADMIN_PASS);
        wait.until(ExpectedConditions.urlContains("#/admin/report"));
        assertTrue(driver.findElement(By.id("logout")).isDisplayed(), "Logout button should be visible after successful login.");
    }

    /**
     * Logs into the admin panel and verifies that the message sent in the first test is visible.
     */
    @Test
    @Order(4)
    void viewMessagesInAdminPanelTest() {
        driver.get(ADMIN_URL);
        performAdminLogin(ADMIN_USER, ADMIN_PASS);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("message")));

        // Find the specific message sent by the contact form test
        String messageXpath = String.format("//div[@data-testid='message' and .//p[text()='%s'] and .//p[text()='%s']]",
            contactFormName, contactFormSubject);
        
        WebElement messageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(messageXpath)));
        assertTrue(messageElement.isDisplayed(), "Message from contact form test was not found in the admin panel.");
    }

    /**
     * Verifies that the external GitHub link in the footer opens correctly in a new tab.
     */
    @Test
    @Order(5)
    void externalGitHubLinkTest() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("view the project on GitHub")));
        githubLink.click();
        
        // Wait for new tab and switch to it
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Verify the URL and close the tab
        wait.until(ExpectedConditions.urlContains("github.com"));
        assertTrue(driver.getCurrentUrl().contains("github.com/mpeiretti/restful-booker-platform"), "External link URL is incorrect.");
        
        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should have switched back to the original window.");
    }


    // --- Helper Methods ---

    /**
     * Fills the admin login form and clicks the login button.
     *
     * @param username The username to enter.
     * @param password The password to enter.
     */
    private void performAdminLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("doLogin")).click();
    }
}