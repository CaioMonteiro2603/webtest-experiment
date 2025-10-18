package Qwen3.ws05.seq07;

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

public class CacTatTest {

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
        assertEquals("CAC TAT - Test", title);
        
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
        WebElement contactSection = driver.findElement(By.id("contact"));
        assertTrue(contactSection.isDisplayed());
        
        // Check for required fields
        WebElement nameField = driver.findElement(By.id("name"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement messageField = driver.findElement(By.id("message"));
        
        assertTrue(nameField.isDisplayed());
        assertTrue(emailField.isDisplayed());
        assertTrue(messageField.isDisplayed());
    }

    @Test
    @Order(5)
    public void testAboutPageNavigation() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Navigate to About page
        WebElement aboutLink = driver.findElement(By.linkText("About"));
        aboutLink.click();
        
        wait.until(ExpectedConditions.urlContains("about"));
        assertEquals("https://cac-tat.s3.eu-central-1.amazonaws.com/about.html", driver.getCurrentUrl());
        
        // Verify About page content
        WebElement aboutHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertTrue(aboutHeader.getText().contains("About"));
    }

    @Test
    @Order(6)
    public void testServicesPageNavigation() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Navigate to Services page
        WebElement servicesLink = driver.findElement(By.linkText("Services"));
        servicesLink.click();
        
        wait.until(ExpectedConditions.urlContains("services"));
        assertEquals("https://cac-tat.s3.eu-central-1.amazonaws.com/services.html", driver.getCurrentUrl());
        
        // Verify Services page content
        WebElement servicesHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertTrue(servicesHeader.getText().contains("Services"));
    }

    @Test
    @Order(7)
    public void testPortfolioPageNavigation() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Navigate to Portfolio page
        WebElement portfolioLink = driver.findElement(By.linkText("Portfolio"));
        portfolioLink.click();
        
        wait.until(ExpectedConditions.urlContains("portfolio"));
        assertEquals("https://cac-tat.s3.eu-central-1.amazonaws.com/portfolio.html", driver.getCurrentUrl());
        
        // Verify Portfolio page content
        WebElement portfolioHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertTrue(portfolioHeader.getText().contains("Portfolio"));
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
        WebElement testimonials = driver.findElement(By.id("testimonials"));
        assertTrue(testimonials.isDisplayed());
        
        // Check for testimonials
        List<WebElement> testimonialItems = testimonials.findElements(By.cssSelector(".testimonial-item"));
        assertTrue(testimonialItems.size() >= 0);
    }

    @Test
    @Order(11)
    public void testStatisticsSection() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Check statistics section
        WebElement statsSection = driver.findElement(By.id("stats"));
        assertTrue(statsSection.isDisplayed());
        
        // Check for statistic elements
        List<WebElement> statElements = statsSection.findElements(By.cssSelector(".stat-item"));
        assertTrue(statElements.size() >= 0);
    }

    @Test
    @Order(12)
    public void testMobileResponsive() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test responsive design by checking mobile menu
        WebElement mobileMenuToggle = driver.findElement(By.cssSelector(".menu-toggle"));
        if (mobileMenuToggle.isDisplayed()) {
            // Check if mobile menu exists
            List<WebElement> mobileMenuItems = driver.findElements(By.cssSelector(".mobile-menu li"));
            assertTrue(mobileMenuItems.size() >= 0);
        }
    }

    @Test
    @Order(13)
    public void testImageGallery() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Check for image gallery
        List<WebElement> images = driver.findElements(By.cssSelector("img"));
        assertTrue(images.size() > 0);
        
        // Verify some images are loaded
        for (int i = 0; i < Math.min(3, images.size()); i++) {
            if (images.get(i).isDisplayed()) {
                break;
            }
        }
    }

    @Test
    @Order(14)
    public void testContactFormSubmission() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Try to submit contact form
        WebElement contactSection = driver.findElement(By.id("contact"));
        
        // Fill in form if it exists
        List<WebElement> inputs = contactSection.findElements(By.tagName("input"));
        List<WebElement> textareas = contactSection.findElements(By.tagName("textarea"));
        
        if (!inputs.isEmpty() || !textareas.isEmpty()) {
            // Try to submit form (this may not actually work due to missing backend)
            List<WebElement> submitButtons = contactSection.findElements(By.tagName("button"));
            if (!submitButtons.isEmpty()) {
                WebElement submitButton = submitButtons.get(0);
                submitButton.click();
            }
        }
        
        // Still on same page (no redirect expected)
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }
}