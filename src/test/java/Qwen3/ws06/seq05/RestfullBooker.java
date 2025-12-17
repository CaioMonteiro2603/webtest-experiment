package Qwen3.ws06.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
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
    public void testPageLoadAndTitle() {
        driver.get("https://automationintesting.online/");
        
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("Automation Testing"), "Page title should contain Automation Testing");
        
        WebElement header = driver.findElement(By.cssSelector("header h1"));
        assertTrue(header.isDisplayed(), "Header should be displayed");
        assertEquals("Automation Testing", header.getText(), "Header text should match expected value");
    }

    @Test
    @Order(2)
    public void testNavigationMenu() {
        driver.get("https://automationintesting.online/");
        
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() > 0, "Should have navigation links");
        
        for (WebElement link : navLinks) {
            assertTrue(link.isDisplayed(), "Navigation link should be displayed");
            assertNotNull(link.getAttribute("href"), "Navigation link should have href attribute");
        }
    }

    @Test
    @Order(3)
    public void testContactForm() {
        driver.get("https://automationintesting.online/");
        
        // Find contact form
        WebElement contactForm = driver.findElement(By.id("contact-form"));
        assertTrue(contactForm.isDisplayed(), "Contact form should be displayed");
        
        // Check form fields
        List<WebElement> formFields = driver.findElements(By.cssSelector("#contact-form input, #contact-form textarea"));
        assertTrue(formFields.size() > 0, "Should have form fields");
        
        // Check for contact information
        List<WebElement> contactInfo = driver.findElements(By.cssSelector(".contact-info p"));
        assertTrue(contactInfo.size() > 0, "Should have contact information");
    }

    @Test
    @Order(4)
    public void testServicesSection() {
        driver.get("https://automationintesting.online/");
        
        // Find services section
        WebElement servicesSection = driver.findElement(By.id("services"));
        assertTrue(servicesSection.isDisplayed(), "Services section should be displayed");
        
        // Check services cards
        List<WebElement> serviceCards = driver.findElements(By.cssSelector(".service-card"));
        assertTrue(serviceCards.size() > 0, "Should have service cards");
        
        for (WebElement card : serviceCards) {
            assertTrue(card.isDisplayed(), "Service card should be displayed");
        }
    }

    @Test
    @Order(5)
    public void testTestimonialsSection() {
        driver.get("https://automationintesting.online/");
        
        // Find testimonials section
        WebElement testimonialsSection = driver.findElement(By.id("testimonials"));
        assertTrue(testimonialsSection.isDisplayed(), "Testimonials section should be displayed");
        
        // Check testimonials
        List<WebElement> testimonialCards = driver.findElements(By.cssSelector(".testimonial-card"));
        assertTrue(testimonialCards.size() > 0, "Should have testimonial cards");
        
        for (WebElement card : testimonialCards) {
            assertTrue(card.isDisplayed(), "Testimonial card should be displayed");
        }
    }

    @Test
    @Order(6)
    public void testAboutSection() {
        driver.get("https://automationintesting.online/");
        
        // Find about section
        WebElement aboutSection = driver.findElement(By.id("about"));
        assertTrue(aboutSection.isDisplayed(), "About section should be displayed");
        
        // Check about content
        WebElement aboutContent = driver.findElement(By.cssSelector("#about p"));
        assertTrue(aboutContent.isDisplayed(), "About content should be displayed");
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get("https://automationintesting.online/");
        
        // Check footer
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed(), "Footer should be displayed");
        
        // Find footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0, "Should have footer links");
        
        for (WebElement link : footerLinks) {
            assertTrue(link.isDisplayed(), "Footer link should be displayed");
            assertNotNull(link.getAttribute("href"), "Footer link should have href attribute");
        }
    }

    @Test
    @Order(8)
    public void testExternalLinksInFooter() {
        driver.get("https://automationintesting.online/");
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() > 0, "Should have footer links");
        
        String mainWindowHandle = driver.getWindowHandle();
        
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                // Click external links that open in new tabs
                link.click();
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(mainWindowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("github.com") || 
                           currentUrl.contains("twitter.com") ||
                           currentUrl.contains("linkedin.com"),
                           "External link should point to a valid domain");
                
                driver.close();
                driver.switchTo().window(mainWindowHandle);
            }
        }
    }

    @Test
    @Order(9)
    public void testContactFormSubmission() {
        driver.get("https://automationintesting.online/");
        
        // Navigate to contact form
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        
        // Fill in form fields
        WebElement nameInput = driver.findElement(By.id("name"));
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement subjectInput = driver.findElement(By.id("subject"));
        WebElement messageInput = driver.findElement(By.id("message"));
        WebElement submitButton = driver.findElement(By.cssSelector("#contact-form button[type='submit']"));
        
        nameInput.sendKeys("Test User");
        emailInput.sendKeys("test@example.com");
        subjectInput.sendKeys("Test Subject");
        messageInput.sendKeys("This is a test message");
        
        // Submit form
        submitButton.click();
        
        // If submission successful, page should reload or show success message
        // We test that the page didn't throw an error
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("contact"), "Should remain on contact page after submission");
    }

    @Test
    @Order(10)
    public void testResponsiveDesignElements() {
        driver.get("https://automationintesting.online/");
        
        // Test for mobile navigation elements
        try {
            WebElement mobileMenuToggle = driver.findElement(By.cssSelector(".menu-toggle"));
            assertTrue(mobileMenuToggle.isDisplayed(), "Mobile menu toggle should be displayed");
        } catch (NoSuchElementException ignored) {
            // Not all pages may have mobile menu toggle
        }
        
        // Check basic semantic elements
        List<WebElement> mainHeaders = driver.findElements(By.tagName("h1"));
        assertEquals(1, mainHeaders.size(), "Should have one main header");
        
        List<WebElement> navigationElements = driver.findElements(By.tagName("nav"));
        assertTrue(navigationElements.size() > 0, "Should have navigation elements");
        
        List<WebElement> sectionElements = driver.findElements(By.tagName("section"));
        assertTrue(sectionElements.size() > 0, "Should have section elements");
    }

    @Test
    @Order(11)
    public void testAccessibilityElements() {
        driver.get("https://automationintesting.online/");
        
        // Test for heading hierarchy
        List<WebElement> h1Elements = driver.findElements(By.tagName("h1"));
        assertEquals(1, h1Elements.size(), "Should have one H1 element");
        
        List<WebElement> h2Elements = driver.findElements(By.tagName("h2"));
        assertTrue(h2Elements.size() > 0, "Should have H2 elements");
        
        // Test for alt text on images
        List<WebElement> imgElements = driver.findElements(By.tagName("img"));
        for (WebElement img : imgElements) {
            String altText = img.getAttribute("alt");
            assertNotNull(altText, "Image should have alt text");
        }
        
        // Test for links have meaningful text
        List<WebElement> linkElements = driver.findElements(By.tagName("a"));
        for (WebElement link : linkElements) {
            String linkText = link.getText().trim();
            assertFalse(linkText.isEmpty(), "Link should have meaningful text");
            assertNotNull(link.getAttribute("href"), "Link should have href attribute");
        }
    }

    @Test
    @Order(12)
    public void testPageNavigation() {
        driver.get("https://automationintesting.online/");
        
        // Click on different navigation links and verify navigation
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        for (WebElement link : navLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                // Click link and wait for navigation
                link.click();
                
                // Wait for the page to load
                wait.until(ExpectedConditions.urlContains(href));
                
                // Verify we're on the correct page
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains(href), "Should navigate to the correct page");
                
                // Go back to main page for next test
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains("automationintesting.online"));
            }
        }
    }

    @Test
    @Order(13)
    public void testSocialMediaLinks() {
        driver.get("https://automationintesting.online/");
        
        // Find social media links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".social-links a"));
        assertTrue(socialLinks.size() > 0, "Should have social media links");
        
        String mainWindowHandle = driver.getWindowHandle();
        
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty()) {
                // Click should open in new tab
                link.click();
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(mainWindowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                String currentUrl = driver.getCurrentUrl();
                // Check for expected domains
                assertTrue(currentUrl.contains("twitter.com") || 
                           currentUrl.contains("facebook.com") ||
                           currentUrl.contains("linkedin.com") ||
                           currentUrl.contains("github.com"),
                           "Social media link should point to a valid social platform");
                
                driver.close();
                driver.switchTo().window(mainWindowHandle);
            }
        }
    }
}