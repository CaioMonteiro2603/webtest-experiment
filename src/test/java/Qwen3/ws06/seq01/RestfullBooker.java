package Qwen3.ws06.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AutomationTestingTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoad() {
        driver.get("https://automationintesting.online/");

        String title = driver.getTitle();
        assertTrue(title.contains("Automation Testing"), "Page title should contain 'Automation Testing'");

        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
        assertTrue(header.isDisplayed(), "Main header should be displayed");
    }

    @Test
    @Order(2)
    public void testContactFormSubmission() {
        driver.get("https://automationintesting.online/");

        // Navigate to contact page
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();

        // Wait for contact form to load
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement subjectInput = driver.findElement(By.id("subject"));
        WebElement descriptionTextArea = driver.findElement(By.id("description"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        // Fill form
        nameInput.sendKeys("John Doe");
        emailInput.sendKeys("john.doe@example.com");
        subjectInput.sendKeys("Test Subject");
        descriptionTextArea.sendKeys("This is a test message for the contact form.");

        // Submit form
        submitButton.click();

        // Verify form submission
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed(), "Success message should be displayed after form submission");
    }

    @Test
    @Order(3)
    public void testBookingFunctionality() {
        driver.get("https://automationintesting.online/");

        // Navigate to booking page
        WebElement bookingLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bookings")));
        bookingLink.click();

        // Wait for booking page
        wait.until(ExpectedConditions.urlContains("booking"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("booking"), "Should navigate to booking page");

        // Check if booking form is present
        WebElement bookingForm = driver.findElement(By.cssSelector("form[action='/booking']"));
        assertTrue(bookingForm.isDisplayed(), "Booking form should be present");
    }

    @Test
    @Order(4)
    public void testRoomListing() {
        driver.get("https://automationintesting.online/");

        // Navigate to rooms page
        WebElement roomsLink = driver.findElement(By.linkText("Rooms"));
        roomsLink.click();

        // Wait for rooms page to load
        wait.until(ExpectedConditions.urlContains("room"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("room"), "Should navigate to rooms page");

        // Check if rooms are listed
        List<WebElement> roomCards = driver.findElements(By.cssSelector(".room-card"));
        assertTrue(roomCards.size() > 0, "Room cards should be displayed");
    }

    @Test
    @Order(5)
    public void testUserAccountFunctionality() {
        driver.get("https://automationintesting.online/");

        // Navigate to account page
        WebElement accountLink = driver.findElement(By.linkText("Account"));
        accountLink.click();

        // Wait for account page to load
        wait.until(ExpectedConditions.urlContains("account"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("account"), "Should navigate to account page");

        // Check basic account information display
        WebElement accountHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2")));
        assertTrue(accountHeader.isDisplayed(), "Account header should be displayed");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get("https://automationintesting.online/");

        String parentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(parentWindow)) {
                driver.switchTo().window(window);
                driver.close();
            }
        }

        // Check for external links in footer
        try {
            WebElement githubLink = driver.findElement(By.cssSelector("a[href*='github']"));
            githubLink.click();
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("github"), "Should open GitHub link");
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // Continue if GitHub link not found
        }

        try {
            WebElement linkedinLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
            linkedinLink.click();

            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("linkedin"), "Should open LinkedIn link");
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // Continue if LinkedIn link not found
        }

        try {
            WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
            twitterLink.click();

            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("twitter"), "Should open Twitter link");
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // Continue if Twitter link not found
        }
    }

    @Test
    @Order(7)
    public void testNavigationMenu() {
        driver.get("https://automationintesting.online/");

        // Check main navigation links
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() > 0, "Navigation links should be present");

        // Test each main navigation link
        for (WebElement link : navLinks) {
            String linkText = link.getText();
            if (!linkText.isEmpty() && !linkText.equals("Bookings")) { // Skip book now link
                link.click();
                wait.until(ExpectedConditions.urlContains(linkText.toLowerCase()));
                
                // Go back to home page for next iteration
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains("automationintesting.online"));
            }
        }
    }

    @Test
    @Order(8)
    public void testPageResponsiveElements() {
        driver.get("https://automationintesting.online/");

        // Check for mobile-responsive elements
        WebElement mobileMenuButton = driver.findElement(By.cssSelector(".menu-toggle"));
        assertTrue(mobileMenuButton.isDisplayed(), "Mobile menu button should be displayed");

        // Check footer presence
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed(), "Footer should be displayed");
    }
}