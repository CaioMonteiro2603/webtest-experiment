package Qwen3.ws06.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfulBookerTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://automationintesting.online/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoadAndElements() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1[data-testid='hotel-name']")));

        assertEquals("Shady Meadows B&B", driver.findElement(By.cssSelector("h1[data-testid='hotel-name']")).getText(),
                "Hotel name header is incorrect.");
        assertTrue(driver.findElement(By.id(" DESC")).isDisplayed(), "Description section should be visible.");
        assertTrue(driver.findElements(By.cssSelector("div[data-testid='room-card']")).size() > 0,
                "At least one room card should be displayed.");
    }

    @Test
    @Order(2)
    public void testViewRoomDetails() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1[data-testid='hotel-name']")));

        // Click on the first room card to view details
        WebElement firstRoomCard = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("div[data-testid='room-card']:first-child button")));
        firstRoomCard.click();

        // Assert room details are displayed (one level deep)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-testid='room-details-modal']")));
        assertTrue(driver.findElement(By.cssSelector("div[data-testid='room-details-modal']")).isDisplayed(),
                "Room details modal should be visible after clicking a room.");
        
        // Close the modal
        driver.findElement(By.cssSelector("div[data-testid='room-details-modal'] button[aria-label='Close']")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[data-testid='room-details-modal']")));
    }

    @Test
    @Order(3)
    public void testBookingFormSubmission() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1[data-testid='hotel-name']")));

        // Click 'Book this room' on the first available room
        WebElement bookButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("div[data-testid='room-card']:first-child button[type='button']")));
        bookButton.click();

        // Wait for date pickers to be interactable
        wait.until(ExpectedConditions.elementToBeClickable(By.name("checkin"))).click();
        // Select a check-in date (e.g., the next available date in the calendar, which is usually the 2nd cell)
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.DayPicker-Day:not(.DayPicker-Day--outside):nth-child(2)"))).click();
        
        // Select a check-out date
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.DayPicker-Day:not(.DayPicker-Day--outside):nth-child(4)"))).click();

        // Fill in booking details
        driver.findElement(By.cssSelector("input[data-testid='firstname']")).sendKeys("Caio");
        driver.findElement(By.cssSelector("input[data-testid='lastname']")).sendKeys("Silva");
        driver.findElement(By.cssSelector("input[data-testid='email']")).sendKeys("caio.silva@example.com");
        driver.findElement(By.cssSelector("input[data-testid='phone']")).sendKeys("1234567890");

        // Submit booking
        driver.findElement(By.cssSelector("button[data-testid='submit-contact']")).click();

        // Assert booking confirmation (success message or update in UI)
        // The app updates the room card with a "Reserved" status.
        wait.until(ExpectedConditions.textToBePresentInElement(
            driver.findElement(By.cssSelector("div[data-testid='room-card']:first-child")),
            "Reserved"));
        assertTrue(driver.findElement(By.cssSelector("div[data-testid='room-card']:first-child")).getText().contains("Reserved"),
                "Room card should indicate 'Reserved' status after booking.");
    }

    @Test
    @Order(4)
    public void testContactFormSubmission() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1[data-testid='hotel-name']")));

        // Scroll to and fill contact form
        WebElement nameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[data-testid='ContactName']")));
        nameField.sendKeys("Test User");

        driver.findElement(By.cssSelector("input[data-testid='ContactEmail']")).sendKeys("test.user@example.com");
        driver.findElement(By.cssSelector("input[data-testid='ContactPhone']")).sendKeys("0987654321");
        driver.findElement(By.cssSelector("textarea[data-testid='ContactDescription']")).sendKeys("This is a test message from the automated suite.");

        // Submit contact form
        driver.findElement(By.cssSelector("button[data-testid='submit-contact']")).click();

        // Assert success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[data-testid='contact-success']")));
        assertTrue(successMessage.getText().contains("Thanks for getting in touch"), "Contact form success message should be displayed.");
    }

    @Test
    @Order(5)
    public void testAdminLoginAndLogout() {
        // Navigate to Admin Login page (one level deep)
        driver.get(BASE_URL + "admin");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1[data-testid='admin-title']")));

        assertEquals("Administration", driver.findElement(By.cssSelector("h1[data-testid='admin-title']")).getText(),
                "Admin page title is incorrect.");

        // Perform login
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Assert successful login (redirect to Admin Dashboard)
        wait.until(ExpectedConditions.urlContains("/admin/dashboard"));
        assertTrue(driver.findElement(By.cssSelector("a[data-testid='logout']")).isDisplayed(),
                "Logout link should be visible after successful admin login.");

        // Perform logout
        driver.findElement(By.cssSelector("a[data-testid='logout']")).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "admin"));
        assertTrue(driver.findElement(By.cssSelector("button[type='submit']")).isDisplayed(),
                "Login button should be visible after admin logout.");
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Find all footer links that are external
        List<WebElement> footerLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("footer a[href^='http']")));

        // There are 3 external links in the footer: Twitter, LinkedIn, BrowserStack
        assertTrue(footerLinks.size() >= 3, "There should be at least 3 external links in the footer.");

        // Click Twitter link
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href").contains("twitter.com")) {
                link.click();
                assertExternalLinkAndReturn(originalWindow, "twitter.com");
                break;
            }
        }

        // Click LinkedIn link
        driver.get(BASE_URL); // Reset
        originalWindow = driver.getWindowHandle();
        footerLinks = driver.findElements(By.cssSelector("footer a[href^='http']"));
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href").contains("linkedin.com")) {
                link.click();
                assertExternalLinkAndReturn(originalWindow, "linkedin.com");
                break;
            }
        }

        // Click BrowserStack link
        driver.get(BASE_URL); // Reset
        originalWindow = driver.getWindowHandle();
        footerLinks = driver.findElements(By.cssSelector("footer a[href^='http']"));
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href").contains("browserstack.com")) {
                link.click();
                assertExternalLinkAndReturn(originalWindow, "browserstack.com");
                break;
            }
        }
    }

    // --- Helper Methods ---

    private void assertExternalLinkAndReturn(String originalWindow, String expectedDomain) {
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        assertNotNull(newWindow, "A new window should have been opened for " + expectedDomain);
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "New window URL should contain " + expectedDomain + ". URL was: " + driver.getCurrentUrl());
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}