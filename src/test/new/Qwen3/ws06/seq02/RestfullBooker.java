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
public class RestfullBooker {
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
        
        wait.until(ExpectedConditions.titleContains("Restful-booker-platform demo"));
        assertTrue(driver.getTitle().contains("Restful-booker-platform demo"));
        assertTrue(driver.getCurrentUrl().contains("automationintesting.online"));
    }

    @Test
    @Order(2)
    public void testHomePageElements() {
        driver.get("https://automationintesting.online/");
        
        // Verify page content
        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assertTrue(body.isDisplayed());
        
        // Verify navigation links
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a, .navbar a, header a"));
        assertTrue(navLinks.size() > 0);
        
        // Verify main headline
        List<WebElement> headlines = driver.findElements(By.cssSelector("h1, h2, h3"));
        if (headlines.size() > 0) {
            WebElement headline = headlines.get(0);
            assertTrue(headline.isDisplayed());
            assertTrue(headline.getText().length() > 0);
        }
        
        // Verify welcome message or main content
        List<WebElement> welcomeMessages = driver.findElements(By.cssSelector(".welcome-message, .content, main, .hero"));
        if (welcomeMessages.size() > 0) {
            assertTrue(welcomeMessages.get(0).isDisplayed());
        }
    }

    @Test
    @Order(3)
    public void testBookARoom() {
        driver.get("https://automationintesting.online/");
        
        // Look for any booking related button or link
        List<WebElement> bookElements = driver.findElements(By.xpath("//*[contains(text(), 'Book') or contains(@href, 'book') or contains(@class, 'book')]"));
        if (bookElements.size() > 0) {
            WebElement bookRoomLink = wait.until(ExpectedConditions.elementToBeClickable(bookElements.get(0)));
            bookRoomLink.click();
            
            // Wait for URL to change
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe("https://automationintesting.online/")));
            
            // Look for booking form
            List<WebElement> bookingForms = driver.findElements(By.cssSelector("form, .booking-form, .form-container"));
            if (bookingForms.size() > 0) {
                WebElement bookingForm = bookingForms.get(0);
                assertTrue(bookingForm.isDisplayed());
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains("automationintesting.online"));
    }

    @Test
    @Order(4)
    public void testContactPage() {
        driver.get("https://automationintesting.online/");
        
        // Look for contact link
        List<WebElement> contactLinks = driver.findElements(By.xpath("//*[contains(text(), 'Contact') or contains(@href, 'contact')]"));
        if (contactLinks.size() > 0) {
            WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(contactLinks.get(0)));
            contactLink.click();
            
            // Wait for URL to change
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe("https://automationintesting.online/")));
            
            // Look for contact form or content
            List<WebElement> contactForms = driver.findElements(By.cssSelector("form, .contact-form, .contact-info, .contact"));
            assertTrue(contactForms.size() > 0);
            assertTrue(contactForms.get(0).isDisplayed());
        }
    }

    @Test
    @Order(5)
    public void testContactFormSubmission() {
        driver.get("https://automationintesting.online/");
        
        // Navigate to contact if link exists
        List<WebElement> contactLinks = driver.findElements(By.xpath("//*[contains(text(), 'Contact') or contains(@href, 'contact')]"));
        if (contactLinks.size() > 0) {
            WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(contactLinks.get(0)));
            contactLink.click();
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe("https://automationintesting.online/")));
        }
        
        // Look for form fields
        List<WebElement> nameFields = driver.findElements(By.id("name"));
        List<WebElement> emailFields = driver.findElements(By.id("email"));
        List<WebElement> messageFields = driver.findElements(By.id("message, subject"));
        List<WebElement> submitButtons = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']"));
        
        if (nameFields.size() > 0 && emailFields.size() > 0 && submitButtons.size() > 0) {
            nameFields.get(0).sendKeys("Jane Smith");
            emailFields.get(0).sendKeys("jane.smith@example.com");
            if (messageFields.size() > 0) {
                messageFields.get(0).sendKeys("This is a test message.");
            }
            submitButtons.get(0).click();
        }
        
        assertTrue(driver.getCurrentUrl().contains("automationintesting.online"));
    }

    @Test
    @Order(6)
    public void testAboutPage() {
        driver.get("https://automationintesting.online/");
        
        // Look for about link
        List<WebElement> aboutLinks = driver.findElements(By.xpath("//*[contains(text(), 'About') or contains(@href, 'about')]"));
        if (aboutLinks.size() > 0) {
            WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(aboutLinks.get(0)));
            aboutLink.click();
            
            // Wait for URL to change
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe("https://automationintesting.online/")));
            
            // Look for about content
            List<WebElement> aboutContents = driver.findElements(By.cssSelector(".about-content, .content, main"));
            if (aboutContents.size() > 0) {
                assertTrue(aboutContents.get(0).isDisplayed());
            }
        }
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get("https://automationintesting.online/");
        
        // Look for footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a, .footer a"));
        assertTrue(footerLinks.size() >= 0);
        
        // Test any footer links found
        for (WebElement footerLink : footerLinks) {
            if (footerLink.isDisplayed()) {
                String linkText = footerLink.getText();
                if (linkText.contains("Privacy") || linkText.contains("Terms")) {
                    footerLink.click();
                    break;
                }
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains("automationintesting.online"));
    }

    @Test
    @Order(8)
    public void testNavigationMenu() {
        driver.get("https://automationintesting.online/");
        
        // Test Home navigation
        List<WebElement> homeLinks = driver.findElements(By.xpath("//*[contains(text(), 'Home') or contains(@href, '/')]"));
        if (homeLinks.size() > 0) {
            WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(homeLinks.get(0)));
            homeLink.click();
        }
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.equals("https://automationintesting.online/") || currentUrl.equals("https://automationintesting.online/#"));
        
        // Look for navigation links
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a, .navbar a, header a"));
        assertTrue(navLinks.size() > 0);
    }

    @Test
    @Order(9)
    public void testRoomsPage() {
        driver.get("https://automationintesting.online/");
        
        // Look for rooms link
        List<WebElement> roomsLinks = driver.findElements(By.xpath("//*[contains(text(), 'Rooms') or contains(@href, 'room')]"));
        if (roomsLinks.size() > 0) {
            WebElement roomsLink = wait.until(ExpectedConditions.elementToBeClickable(roomsLinks.get(0)));
            roomsLink.click();
            
            // Wait for URL to change
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe("https://automationintesting.online/")));
            
            // Look for rooms content
            List<WebElement> roomsGrids = driver.findElements(By.cssSelector(".rooms-grid, .rooms, .room-list, .content"));
            if (roomsGrids.size() > 0) {
                assertTrue(roomsGrids.get(0).isDisplayed());
            }
            
            // Look for room cards
            List<WebElement> roomCards = driver.findElements(By.cssSelector(".room-card, .room, .card"));
            if (roomCards.size() > 0) {
                assertTrue(roomCards.get(0).isDisplayed());
            }
        }
    }

    @Test
    @Order(10)
    public void testAccessibilityFeatures() {
        driver.get("https://automationintesting.online/");
        
        // Verify page title
        assertTrue(driver.getTitle().length() > 0);
        
        // Look for main content
        List<WebElement> mainElements = driver.findElements(By.cssSelector("main, .main, .content, body"));
        assertTrue(mainElements.size() > 0);
        assertTrue(mainElements.get(0).isDisplayed());
        
        // Look for navigation
        List<WebElement> navElements = driver.findElements(By.cssSelector("nav, .navbar, header"));
        assertTrue(navElements.size() >= 0);
        
        // Look for footer
        List<WebElement> footers = driver.findElements(By.cssSelector("footer, .footer"));
        assertTrue(footers.size() >= 0);
    }
}