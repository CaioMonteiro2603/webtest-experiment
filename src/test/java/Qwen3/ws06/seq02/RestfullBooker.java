package Qwen3.ws06.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class AutomationTestingTest {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
    public void testPageLoad() {
        driver.get("https://automationintesting.online/");
        
        wait.until(ExpectedConditions.titleContains("Automation Testing"));
        assertTrue(driver.getTitle().contains("Automation Testing"));
        assertTrue(driver.getCurrentUrl().contains("automationintesting.online"));
    }

    @Test
    @Order(2)
    public void testHomePageElements() {
        driver.get("https://automationintesting.online/");
        
        // Verify header elements
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("header")));
        assertTrue(header.isDisplayed());
        
        // Verify navigation links
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() > 0);
        
        // Verify main headline
        WebElement headline = driver.findElement(By.cssSelector("h1"));
        assertTrue(headline.isDisplayed());
        assertTrue(headline.getText().length() > 0);
        
        // Verify welcome message
        WebElement welcomeMessage = driver.findElement(By.cssSelector(".welcome-message"));
        assertTrue(welcomeMessage.isDisplayed());
    }

    @Test
    @Order(3)
    public void testBookARoom() {
        driver.get("https://automationintesting.online/");
        
        // Click on Book a Room link
        WebElement bookRoomLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Book a Room")));
        bookRoomLink.click();
        
        // Wait for room booking page
        wait.until(ExpectedConditions.urlContains("booking"));
        assertTrue(driver.getCurrentUrl().contains("booking"));
        
        // Verify booking form exists
        WebElement bookingForm = driver.findElement(By.cssSelector(".booking-form"));
        assertTrue(bookingForm.isDisplayed());
        
        // Fill booking form
        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstname")));
        firstNameField.sendKeys("John");
        WebElement lastNameField = driver.findElement(By.id("lastname"));
        lastNameField.sendKeys("Doe");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("john.doe@example.com");
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("1234567890");
        
        // Try to submit
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Verify form submitted
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".confirmation-message")));
        assertTrue(driver.getCurrentUrl().contains("booking"));
    }

    @Test
    @Order(4)
    public void testContactPage() {
        driver.get("https://automationintesting.online/");
        
        // Navigate to contact page
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();
        
        // Wait for contact page
        wait.until(ExpectedConditions.urlContains("contact"));
        assertTrue(driver.getCurrentUrl().contains("contact"));
        
        // Verify contact form exists
        WebElement contactForm = driver.findElement(By.cssSelector(".contact-form"));
        assertTrue(contactForm.isDisplayed());
        
        // Verify contact information is present
        WebElement contactInfo = driver.findElement(By.cssSelector(".contact-info"));
        assertTrue(contactInfo.isDisplayed());
    }

    @Test
    @Order(5)
    public void testContactFormSubmission() {
        driver.get("https://automationintesting.online/contact");
        
        // Fill contact form
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("Jane Smith");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("jane.smith@example.com");
        WebElement subjectField = driver.findElement(By.id("subject"));
        subjectField.sendKeys("Test Subject");
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("This is a test message.");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Verify success message
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success-message")));
        assertTrue(driver.getCurrentUrl().contains("contact"));
    }

    @Test
    @Order(6)
    public void testAboutPage() {
        driver.get("https://automationintesting.online/");
        
        // Navigate to about page
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();
        
        // Wait for about page
        wait.until(ExpectedConditions.urlContains("about"));
        assertTrue(driver.getCurrentUrl().contains("about"));
        
        // Verify about content
        WebElement aboutContent = driver.findElement(By.cssSelector(".about-content"));
        assertTrue(aboutContent.isDisplayed());
        
        // Verify mission statement
        WebElement missionStatement = driver.findElement(By.cssSelector(".mission-statement"));
        assertTrue(missionStatement.isDisplayed());
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get("https://automationintesting.online/");
        
        // Test Privacy Policy link
        WebElement privacyLink = driver.findElement(By.linkText("Privacy Policy"));
        privacyLink.click();
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("privacy"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
        
        // Test Terms of Service link
        driver.get("https://automationintesting.online/");
        WebElement termsLink = driver.findElement(By.linkText("Terms of Service"));
        termsLink.click();
        originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("terms"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testNavigationMenu() {
        driver.get("https://automationintesting.online/");
        
        // Test Home navigation
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        assertEquals("https://automationintesting.online/", driver.getCurrentUrl());
        
        // Test About navigation
        driver.get("https://automationintesting.online/");
        WebElement aboutNav = driver.findElement(By.linkText("About"));
        aboutNav.click();
        assertTrue(driver.getCurrentUrl().contains("about"));
        
        // Go back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("automationintesting.online"));
        
        // Test Contact navigation
        WebElement contactNav = driver.findElement(By.linkText("Contact"));
        contactNav.click();
        assertTrue(driver.getCurrentUrl().contains("contact"));
    }

    @Test
    @Order(9)
    public void testRoomsPage() {
        driver.get("https://automationintesting.online/");
        
        // Navigate to rooms page
        WebElement roomsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Rooms")));
        roomsLink.click();
        
        // Wait for rooms page
        wait.until(ExpectedConditions.urlContains("rooms"));
        assertTrue(driver.getCurrentUrl().contains("rooms"));
        
        // Verify rooms grid exists
        WebElement roomsGrid = driver.findElement(By.cssSelector(".rooms-grid"));
        assertTrue(roomsGrid.isDisplayed());
        
        // Verify at least one room is listed
        List<WebElement> rooms = driver.findElements(By.cssSelector(".room-card"));
        assertTrue(rooms.size() > 0);
        
        // Verify room details are present
        WebElement firstRoom = rooms.get(0);
        assertTrue(firstRoom.isDisplayed());
    }

    @Test
    @Order(10)
    public void testAccessibilityFeatures() {
        driver.get("https://automationintesting.online/");
        
        // Verify page title
        assertTrue(driver.getTitle().length() > 0);
        
        // Verify page has a main landmark
        WebElement mainContent = driver.findElement(By.cssSelector("main"));
        assertTrue(mainContent.isDisplayed());
        
        // Verify navigation landmarks
        List<WebElement> navElements = driver.findElements(By.cssSelector("nav"));
        assertTrue(navElements.size() > 0);
        
        // Verify footer exists
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed());
    }
}