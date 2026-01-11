```java
package geminiPro.ws05.seq07;

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

/**
 * A comprehensive JUnit 5 test suite for the Restful Booker Platform.
 * This suite covers the public-facing booking and contact forms, as well as
 * the admin panel for login, navigation, and verification of created data.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

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
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'contact')]/div/h2")));
        String expectedConfirmation = "Thanks for getting in touch " + CONTACT_NAME + "!";
        assertEquals(expectedConfirmation, confirmation.getText(), "Confirmation message should be correct.");
    }

    @Test
    @Order(2)
    void testRoomBookingFlow() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Book this room')]"))).click();

        // Select dates: today and tomorrow
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".rbc-calendar")));
        
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
        assertTrue(driver