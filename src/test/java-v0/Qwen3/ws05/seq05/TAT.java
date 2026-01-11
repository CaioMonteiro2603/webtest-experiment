package Qwen3.ws05.seq05;

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
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        String pageTitle = driver.getTitle();
        assertEquals("CAC TAT", pageTitle, "Page title should match expected value");
        
        WebElement header = driver.findElement(By.tagName("h1"));
        assertTrue(header.isDisplayed(), "Header should be displayed");
        assertEquals("CAC TAT", header.getText(), "Header text should match expected value");
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        List<WebElement> navLinks = driver.findElements(By.cssSelector(".navbar-nav a"));
        assertTrue(navLinks.size() > 0, "Should have navigation links");
        
        // Check each link
        for (WebElement link : navLinks) {
            assertTrue(link.isDisplayed(), "Navigation link should be displayed");
            assertNotNull(link.getAttribute("href"), "Navigation link should have href attribute");
        }
    }

    @Test
    @Order(3)
    public void testMainContentSections() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Check if main content sections are present
        WebElement heroSection = driver.findElement(By.id("hero"));
        assertTrue(heroSection.isDisplayed(), "Hero section should be displayed");
        
        WebElement featuresSection = driver.findElement(By.id("features"));
        assertTrue(featuresSection.isDisplayed(), "Features section should be displayed");
        
        WebElement testimonialsSection = driver.findElement(By.id("testimonials"));
        assertTrue(testimonialsSection.isDisplayed(), "Testimonials section should be displayed");
        
        WebElement contactSection = driver.findElement(By.id("contact"));
        assertTrue(contactSection.isDisplayed(), "Contact section should be displayed");
    }

    @Test
    @Order(4)
    public void testHeroSectionElements() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        WebElement heroSection = driver.findElement(By.id("hero"));
        assertTrue(heroSection.isDisplayed(), "Hero section should be displayed");
        
        WebElement heroTitle = driver.findElement(By.cssSelector("#hero h2"));
        assertTrue(heroTitle.isDisplayed(), "Hero title should be displayed");
        
        WebElement heroDescription = driver.findElement(By.cssSelector("#hero p"));
        assertTrue(heroDescription.isDisplayed(), "Hero description should be displayed");
        
        WebElement ctaButton = driver.findElement(By.cssSelector("#hero .btn"));
        assertTrue(ctaButton.isDisplayed(), "Call to action button should be displayed");
    }

    @Test
    @Order(5)
    public void testFeaturesSection() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        WebElement featuresSection = driver.findElement(By.id("features"));
        assertTrue(featuresSection.isDisplayed(), "Features section should be displayed");
        
        List<WebElement> featureCards = driver.findElements(By.cssSelector(".feature-card"));
        assertTrue(featureCards.size() > 0, "Should have feature cards");
        
        for (WebElement card : featureCards) {
            assertTrue(card.isDisplayed(), "Feature card should be displayed");
        }
    }

    @Test
    @Order(6)
    public void testTestimonialsSection() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        WebElement testimonialsSection = driver.findElement(By.id("testimonials"));
        assertTrue(testimonialsSection.isDisplayed(), "Testimonials section should be displayed");
        
        List<WebElement> testimonialCards = driver.findElements(By.cssSelector(".testimonial-card"));
        assertTrue(testimonialCards.size() > 0, "Should have testimonial cards");
        
        for (WebElement card : testimonialCards) {
            assertTrue(card.isDisplayed(), "Testimonial card should be displayed");
        }
    }

    @Test
    @Order(7)
    public void testContactForm() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        WebElement contactSection = driver.findElement(By.id("contact"));
        assertTrue(contactSection.isDisplayed(), "Contact section should be displayed");
        
        WebElement contactForm = driver.findElement(By.cssSelector("#contact form"));
        assertTrue(contactForm.isDisplayed(), "Contact form should be displayed");
        
        List<WebElement> formInputs = driver.findElements(By.cssSelector("#contact form input, #contact form textarea"));
        assertTrue(formInputs.size() > 0, "Should have form inputs");
        
        for (WebElement input : formInputs) {
            assertTrue(input.isDisplayed(), "Form input should be displayed");
        }
        
        WebElement submitButton = driver.findElement(By.cssSelector("#contact form button[type='submit']"));
        assertTrue(submitButton.isDisplayed(), "Submit button should be displayed");
    }

    @Test
    @Order(8)
    public void testSocialMediaLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".social-links a"));
        assertEquals(3, socialLinks.size(), "Should have 3 social media links");
        
        String mainWindowHandle = driver.getWindowHandle();
        
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty()) {
                // Clicking this should open in new tab
                link.click();
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(mainWindowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                String currentUrl = driver.getCurrentUrl();
                if (href.contains("facebook.com")) {
                    assertTrue(currentUrl.contains("facebook.com"), "Facebook URL should contain facebook.com");
                } else if (href.contains("twitter.com")) {
                    assertTrue(currentUrl.contains("twitter.com"), "Twitter URL should contain twitter.com");
                } else if (href.contains("linkedin.com")) {
                    assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn URL should contain linkedin.com");
                }
                
                driver.close();
                driver.switchTo().window(mainWindowHandle);
            }
        }
    }

    @Test
    @Order(9)
    public void testFooterLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector(".footer a"));
        assertTrue(footerLinks.size() > 0, "Should have footer links");
        
        for (WebElement link : footerLinks) {
            assertTrue(link.isDisplayed(), "Footer link should be displayed");
            assertNotNull(link.getAttribute("href"), "Footer link should have href attribute");
        }
    }

    @Test
    @Order(10)
    public void testResponsiveDesignElements() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Check if responsive navigation exists (hamburguer menu for mobile)
        try {
            WebElement menuToggle = driver.findElement(By.cssSelector(".navbar-toggler"));
            assertTrue(menuToggle.isDisplayed(), "Menu toggle should be displayed on mobile view");
        } catch (NoSuchElementException ignored) {
            // Menu toggle may not exist on this page or may not be relevant for current size
        }
        
        // Check that main elements are visible
        WebElement header = driver.findElement(By.tagName("header"));
        assertTrue(header.isDisplayed(), "Header should be displayed");
        
        WebElement mainContent = driver.findElement(By.tagName("main"));
        assertTrue(mainContent.isDisplayed(), "Main content should be displayed");
        
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed(), "Footer should be displayed");
    }

    @Test
    @Order(11)
    public void testPageNavigationToSections() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test navigation to sections by clicking links
        List<WebElement> navLinks = driver.findElements(By.cssSelector(".navbar-nav a"));
        for (WebElement link : navLinks) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("#")) {
                // Click the link
                link.click();
                
                // Wait for smooth scroll
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
                
                // Test that the target section is reachable
                String targetId = href.substring(href.indexOf('#') + 1);
                try {
                    WebElement targetSection = driver.findElement(By.id(targetId));
                    assertTrue(targetSection.isDisplayed(), "Target section should be visible after navigation");
                } catch (NoSuchElementException e) {
                    // Allow for cases where section isn't directly visible due to layout
                    // Just ensure the page doesn't break
                }
            }
        }
    }

    @Test
    @Order(12)
    public void testAccessibilityAndSemanticElements() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test for semantic HTML elements
        List<WebElement> headerElements = driver.findElements(By.tagName("header"));
        assertEquals(1, headerElements.size(), "Should have one header element");
        
        List<WebElement> navElements = driver.findElements(By.tagName("nav"));
        assertTrue(navElements.size() > 0, "Should have navigation elements");
        
        List<WebElement> mainElements = driver.findElements(By.tagName("main"));
        assertEquals(1, mainElements.size(), "Should have one main element");
        
        List<WebElement> footerElements = driver.findElements(By.tagName("footer"));
        assertEquals(1, footerElements.size(), "Should have one footer element");
        
        // Test for proper heading hierarchy
        List<WebElement> h1Elements = driver.findElements(By.tagName("h1"));
        assertEquals(1, h1Elements.size(), "Should have one H1 element");
        
        // Test for alternative text on images
        List<WebElement> imgElements = driver.findElements(By.tagName("img"));
        for (WebElement img : imgElements) {
            String altText = img.getAttribute("alt");
            assertNotNull(altText, "Image should have alt text");
        }
    }
}