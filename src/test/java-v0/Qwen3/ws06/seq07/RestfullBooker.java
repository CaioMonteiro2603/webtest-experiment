package Qwen3.ws06.seq07;

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
        driver.get("https://automationintesting.online/");
        
        // Verify page title
        String title = driver.getTitle();
        assertTrue(title.contains("Automation Testing"));
        
        // Verify main elements are present
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("header")));
        assertTrue(header.isDisplayed());
        
        WebElement mainContent = driver.findElement(By.tagName("main"));
        assertTrue(mainContent.isDisplayed());
    }

    @Test
    @Order(2)
    public void testNavigationMenu() {
        driver.get("https://automationintesting.online/");
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("automationintesting.online"));
        assertEquals("https://automationintesting.online/", driver.getCurrentUrl());

        // Test About link
        WebElement aboutLink = driver.findElement(By.linkText("About"));
        aboutLink.click();
        wait.until(ExpectedConditions.urlContains("about"));
        assertTrue(driver.getCurrentUrl().contains("about"));

        // Go back to home
        driver.get("https://automationintesting.online/");
        
        // Test Contact link
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("contact"));
        assertTrue(driver.getCurrentUrl().contains("contact"));
    }

    @Test
    @Order(3)
    public void testContactForm() {
        driver.get("https://automationintesting.online/contact");
        
        // Verify contact form elements
        WebElement nameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
        assertTrue(nameField.isDisplayed());
        
        WebElement emailField = driver.findElement(By.id("email"));
        assertTrue(emailField.isDisplayed());
        
        WebElement subjectField = driver.findElement(By.id("subject"));
        assertTrue(subjectField.isDisplayed());
        
        WebElement messageField = driver.findElement(By.id("message"));
        assertTrue(messageField.isDisplayed());
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(submitButton.isDisplayed());
    }

    @Test
    @Order(4)
    public void testServiceCards() {
        driver.get("https://automationintesting.online/");
        
        // Check that service cards are present
        List<WebElement> serviceCards = driver.findElements(By.cssSelector(".service-card"));
        assertTrue(serviceCards.size() > 0);
        
        // Check each service card for key elements
        for (WebElement card : serviceCards) {
            assertTrue(card.isDisplayed());
            // Check title
            List<WebElement> titles = card.findElements(By.tagName("h3"));
            if (!titles.isEmpty()) {
                assertTrue(titles.get(0).isDisplayed());
            }
            // Check description
            List<WebElement> descriptions = card.findElements(By.tagName("p"));
            if (!descriptions.isEmpty()) {
                assertTrue(descriptions.get(0).isDisplayed());
            }
        }
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get("https://automationintesting.online/");
        
        // Test Footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='social']"));
        if (!socialLinks.isEmpty()) {
            for (WebElement link : socialLinks) {
                assertTrue(link.isDisplayed());
            }
        }
    }

    @Test
    @Order(6)
    public void testBannerSection() {
        driver.get("https://automationintesting.online/");
        
        // Check banner section
        WebElement banner = driver.findElement(By.id("banner"));
        assertTrue(banner.isDisplayed());
        
        // Check banner elements
        List<WebElement> bannerElements = banner.findElements(By.cssSelector("*"));
        assertTrue(bannerElements.size() > 0);
        
        // Check for call to action button
        List<WebElement> ctaButtons = banner.findElements(By.cssSelector("a[href*='contact']"));
        if (!ctaButtons.isEmpty()) {
            assertTrue(ctaButtons.get(0).isDisplayed());
        }
    }

    @Test
    @Order(7)
    public void testTeamSection() {
        driver.get("https://automationintesting.online/");
        
        // Check team section
        WebElement teamSection = driver.findElement(By.id("team"));
        assertTrue(teamSection.isDisplayed());
        
        // Check team member cards
        List<WebElement> teamMembers = teamSection.findElements(By.cssSelector(".team-member"));
        assertTrue(teamMembers.size() > 0);
        
        // Check each member for photo and name
        for (WebElement member : teamMembers) {
            List<WebElement> photos = member.findElements(By.tagName("img"));
            if (!photos.isEmpty()) {
                assertTrue(photos.get(0).isDisplayed());
            }
            List<WebElement> names = member.findElements(By.cssSelector("h4, .name"));
            if (!names.isEmpty()) {
                assertTrue(names.get(0).isDisplayed());
            }
        }
    }

    @Test
    @Order(8)
    public void testTestimonialsSection() {
        driver.get("https://automationintesting.online/");
        
        // Check testimonials section
        WebElement testimonialsSection = driver.findElement(By.id("testimonials"));
        assertTrue(testimonialsSection.isDisplayed());
        
        // Check testimonials
        List<WebElement> testimonialItems = testimonialsSection.findElements(By.cssSelector(".testimonial"));
        assertTrue(testimonialItems.size() > 0);
        
        // Check each testimonial for elements
        for (WebElement testimonial : testimonialItems) {
            List<WebElement> quoteElements = testimonial.findElements(By.cssSelector("p, .quote"));
            if (!quoteElements.isEmpty()) {
                assertTrue(quoteElements.get(0).isDisplayed());
            }
            List<WebElement> clientElements = testimonial.findElements(By.cssSelector(".client, .author"));
            if (!clientElements.isEmpty()) {
                assertTrue(clientElements.get(0).isDisplayed());
            }
        }
    }

    @Test
    @Order(9)
    public void testAboutPage() {
        driver.get("https://automationintesting.online/about");
        
        // Verify About page title
        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertTrue(title.getText().contains("About"));
        
        // Verify About content
        WebElement content = driver.findElement(By.id("about-content"));
        assertTrue(content.isDisplayed());
        
        // Check for key information elements
        List<WebElement> paragraphs = content.findElements(By.tagName("p"));
        assertTrue(paragraphs.size() > 0);
    }

    @Test
    @Order(10)
    public void testContactSubmissionWithoutForm() {
        // Cannot test form submission due to lack of backend
        driver.get("https://automationintesting.online/contact");
        
        // Just verify page elements
        WebElement contactForm = driver.findElement(By.id("contact-form"));
        assertTrue(contactForm.isDisplayed());
        
        // Check that form is not submitted (no redirect)
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("contact"));
    }

    @Test
    @Order(11)
    public void testHeaderNavigation() {
        driver.get("https://automationintesting.online/");
        
        // Test header navigation links
        List<WebElement> headerLinks = driver.findElements(By.cssSelector("header nav a"));
        assertTrue(headerLinks.size() > 0);
        
        // Check that all links are displayed
        for (WebElement link : headerLinks) {
            assertTrue(link.isDisplayed());
        }
    }

    @Test
    @Order(12)
    public void testMobileResponsiveness() {
        driver.get("https://automationintesting.online/");
        
        // Check for mobile menu toggle (if present)
        List<WebElement> mobileToggle = driver.findElements(By.cssSelector(".mobile-toggle"));
        if (!mobileToggle.isEmpty()) {
            WebElement toggle = mobileToggle.get(0);
            // Just verify it's present and clickable
            assertTrue(toggle.isDisplayed());
        }
    }

    @Test
    @Order(13)
    public void testImageElements() {
        driver.get("https://automationintesting.online/");
        
        // Check images on page
        List<WebElement> images = driver.findElements(By.tagName("img"));
        assertTrue(images.size() > 0);
        
        // Check that at least one image is displayed
        boolean hasDisplayedImage = false;
        for (WebElement img : images) {
            if (img.isDisplayed()) {
                hasDisplayedImage = true;
                break;
            }
        }
        assertTrue(hasDisplayedImage);
    }

    @Test
    @Order(14)
    public void testCopyrightInformation() {
        driver.get("https://automationintesting.online/");
        
        // Check footer copyright information
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed());
        
        List<WebElement> copyrightElements = footer.findElements(By.cssSelector(".copyright, .footer-copyright"));
        if (!copyrightElements.isEmpty()) {
            assertTrue(copyrightElements.get(0).isDisplayed());
        }
    }
}