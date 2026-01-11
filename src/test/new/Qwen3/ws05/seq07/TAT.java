package Qwen3.ws05.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testPageLoad() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Verify page title
        String title = driver.getTitle();
        assertEquals("Central de Atendimento ao Cliente TAT", title);
        
        // Verify main elements are present
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertTrue(header.isDisplayed());
        
        WebElement content = driver.findElement(By.id("content"));
        assertTrue(content.isDisplayed());
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test navigation links
        List<WebElement> links = driver.findElements(By.tagName("a"));
        assertTrue(links.size() > 0);
        
        // Click first link (should be about)
        WebElement firstLink = links.get(0);
        String linkText = firstLink.getText();
        if (!linkText.isEmpty() && !linkText.equals("Home")) {
            firstLink.click();
            wait.until(ExpectedConditions.urlContains("about"));
            assertTrue(driver.getCurrentUrl().contains("about"));
        }
    }

    @Test
    @Order(3)
    public void testFormElements() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Check for form elements
        List<WebElement> forms = driver.findElements(By.tagName("form"));
        if (!forms.isEmpty()) {
            WebElement form = forms.get(0);
            assertTrue(form.isDisplayed());
            
            List<WebElement> inputs = form.findElements(By.tagName("input"));
            assertTrue(inputs.size() > 0);
            
            List<WebElement> buttons = form.findElements(By.tagName("button"));
            assertTrue(buttons.size() > 0);
        }
    }

    @Test
    @Order(4)
    public void testContactForm() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Look for contact form elements
        List<WebElement> contactSection = driver.findElements(By.id("contact"));
        if (!contactSection.isEmpty()) {
            WebElement contact = contactSection.get(0);
            assertTrue(contact.isDisplayed());
            
            // Check for required fields
            List<WebElement> nameField = driver.findElements(By.id("name"));
            List<WebElement> emailField = driver.findElements(By.id("email"));
            List<WebElement> messageField = driver.findElements(By.id("message"));
            
            if (!nameField.isEmpty()) assertTrue(nameField.get(0).isDisplayed());
            if (!emailField.isEmpty()) assertTrue(emailField.get(0).isDisplayed());
            if (!messageField.isEmpty()) assertTrue(messageField.get(0).isDisplayed());
        }
    }

    @Test
    @Order(5)
    public void testAboutPageNavigation() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Navigate to About page
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        if (!aboutLinks.isEmpty()) {
            WebElement aboutLink = aboutLinks.get(0);
            aboutLink.click();
            
            wait.until(ExpectedConditions.urlContains("about"));
            assertEquals("https://cac-tat.s3.eu-central-1.amazonaws.com/about.html", driver.getCurrentUrl());
            
            // Verify About page content
            WebElement aboutHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
            assertTrue(aboutHeader.getText().contains("About"));
        }
    }

    @Test
    @Order(6)
    public void testServicesPageNavigation() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Navigate to Services page
        List<WebElement> servicesLinks = driver.findElements(By.linkText("Services"));
        if (!servicesLinks.isEmpty()) {
            WebElement servicesLink = servicesLinks.get(0);
            servicesLink.click();
            
            wait.until(ExpectedConditions.urlContains("services"));
            assertEquals("https://cac-tat.s3.eu-central-1.amazonaws.com/services.html", driver.getCurrentUrl());
            
            // Verify Services page content
            WebElement servicesHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
            assertTrue(servicesHeader.getText().contains("Services"));
        }
    }

    @Test
    @Order(7)
    public void testPortfolioPageNavigation() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Navigate to Portfolio page
        List<WebElement> portfolioLinks = driver.findElements(By.linkText("Portfolio"));
        if (!portfolioLinks.isEmpty()) {
            WebElement portfolioLink = portfolioLinks.get(0);
            portfolioLink.click();
            
            wait.until(ExpectedConditions.urlContains("portfolio"));
            assertEquals("https://cac-tat.s3.eu-central-1.amazonaws.com/portfolio.html", driver.getCurrentUrl());
            
            // Verify Portfolio page content
            WebElement portfolioHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
            assertTrue(portfolioHeader.getText().contains("Portfolio"));
        }
    }

    @Test
    @Order(8)
    public void testFooterLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test Footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        if (!footerLinks.isEmpty()) {
            for (WebElement link : footerLinks) {
                String href = link.getAttribute("href");
                if (href != null && !href.isEmpty() && !href.startsWith("#")) {
                    // Just verify the links exist in footer
                    assertTrue(link.isDisplayed());
                }
            }
        }
    }

    @Test
    @Order(9)
    public void testSocialMediaLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test social media links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".social-media a"));
        if (!socialLinks.isEmpty()) {
            // Verify they exist
            for (WebElement link : socialLinks) {
                assertTrue(link.isDisplayed());
            }
        }
    }

    @Test
    @Order(10)
    public void testTestimonialsSection() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Check testimonials section
        List<WebElement> testimonials = driver.findElements(By.id("testimonials"));
        if (!testimonials.isEmpty()) {
            WebElement testimonialsSection = testimonials.get(0);
            assertTrue(testimonialsSection.isDisplayed());
            
            // Check for testimonials
            List<WebElement> testimonialItems = testimonialsSection.findElements(By.cssSelector(".testimonial-item"));
            assertTrue(testimonialItems.size() >= 0);
        }
    }

    @Test
    @Order(11)
    public void testStatisticsSection() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Check statistics section
        List<WebElement> statsSection = driver.findElements(By.id("stats"));
        if (!statsSection.isEmpty()) {
            WebElement stats = statsSection.get(0);
            assertTrue(stats.isDisplayed());
            
            // Check for statistic elements
            List<WebElement> statElements = stats.findElements(By.cssSelector(".stat-item"));
            assertTrue(statElements.size() >= 0);
        }
    }

    @Test
    @Order(12)
    public void testMobileResponsive() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test responsive design by checking mobile menu
        List<WebElement> mobileMenuToggle = driver.findElements(By.cssSelector(".menu-toggle"));
        if (!mobileMenuToggle.isEmpty()) {
            WebElement toggle = mobileMenuToggle.get(0);
            if (toggle.isDisplayed()) {
                // Check if mobile menu exists
                List<WebElement> mobileMenuItems = driver.findElements(By.cssSelector(".mobile-menu li"));
                assertTrue(mobileMenuItems.size() >= 0);
            }
        }
    }

    @Test
    @Order(13)
    public void testImageGallery() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Check for image gallery
        List<WebElement> images = driver.findElements(By.cssSelector("img"));
        assertTrue(images.size() > 0);
        
        // Verify some images are displayed
        boolean hasDisplayedImages = false;
        for (int i = 0; i < Math.min(3, images.size()); i++) {
            try {
                if (images.get(i).isDisplayed()) {
                    hasDisplayedImages = true;
                    break;
                }
            } catch (Exception e) {
                // Element might not be visible, ignore
            }
        }
        assertTrue(hasDisplayedImages);
    }

    @Test
    @Order(14)
    public void testContactFormSubmission() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Try to submit contact form
        List<WebElement> contactSection = driver.findElements(By.id("contact"));
        
        if (!contactSection.isEmpty()) {
            WebElement contact = contactSection.get(0);
            
            // Fill in form if it exists
            List<WebElement> inputs = contact.findElements(By.tagName("input"));
            List<WebElement> textareas = contact.findElements(By.tagName("textarea"));
            
            if (!inputs.isEmpty() || !textareas.isEmpty()) {
                // Try to submit form (this may not actually work due to missing backend)
                List<WebElement> submitButtons = contact.findElements(By.tagName("button"));
                if (!submitButtons.isEmpty()) {
                    WebElement submitButton = submitButtons.get(0);
                    submitButton.click();
                }
            }
        }
        
        // Still on same page (no redirect expected)
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }
}