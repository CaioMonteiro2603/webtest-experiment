package Qwen3.ws05.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

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
        String title = driver.getTitle();
        assertEquals("CAC TAT - Test Automation", title);
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Check for navigation links
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() >= 3);
        
        // Click on Home link
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"));
        
        // Click on About link
        WebElement aboutLink = driver.findElement(By.linkText("About"));
        aboutLink.click();
        wait.until(ExpectedConditions.urlContains("about.html"));
        assertTrue(driver.getCurrentUrl().contains("about.html"));
        
        // Return to Home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("index.html"));
        
        // Click on Contact link
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("contact.html"));
        assertTrue(driver.getCurrentUrl().contains("contact.html"));
    }

    @Test
    @Order(3)
    public void testContentPresence() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Verify main heading
        WebElement mainHeading = driver.findElement(By.cssSelector("h1"));
        assertTrue(mainHeading.isDisplayed());
        assertTrue(mainHeading.getText().contains("CAC TAT"));
        
        // Verify subheading
        WebElement subHeading = driver.findElement(By.cssSelector("h2"));
        assertTrue(subHeading.isDisplayed());
        
        // Verify content paragraphs
        List<WebElement> paragraphs = driver.findElements(By.cssSelector("p"));
        assertTrue(paragraphs.size() > 0);
        
        // Verify images
        List<WebElement> images = driver.findElements(By.cssSelector("img"));
        assertTrue(images.size() > 0);
    }

    @Test
    @Order(4)
    public void testAboutPage() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/about.html");
        
        // Verify About page title
        String title = driver.getTitle();
        assertEquals("CAC TAT - About", title);
        
        // Verify About content
        WebElement aboutHeader = driver.findElement(By.cssSelector("h1"));
        assertTrue(aboutHeader.getText().contains("About"));
        
        // Verify team section
        WebElement teamSection = driver.findElement(By.id("team"));
        assertTrue(teamSection.isDisplayed());
        
        // Verify mission statement
        WebElement missionParagraph = driver.findElement(By.cssSelector(".mission-statement"));
        assertTrue(missionParagraph.isDisplayed());
    }

    @Test
    @Order(5)
    public void testContactPage() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/contact.html");
        
        // Verify Contact page title
        String title = driver.getTitle();
        assertEquals("CAC TAT - Contact", title);
        
        // Verify contact form exists
        WebElement contactForm = driver.findElement(By.cssSelector("form"));
        assertTrue(contactForm.isDisplayed());
        
        // Verify form fields
        List<WebElement> formFields = driver.findElements(By.cssSelector("input, textarea"));
        assertTrue(formFields.size() >= 3);
        
        // Verify submit button
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(submitButton.isDisplayed());
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Get footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() >= 2);
        
        String originalHandle = driver.getWindowHandle();
        
        // Test first footer link (should be external)
        WebElement firstFooterLink = footerLinks.get(0);
        firstFooterLink.click();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("github.com") || 
                   driver.getCurrentUrl().contains("linkedin.com") ||
                   driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalHandle);
        
        // Test second footer link (if it's another external link)  
        if (footerLinks.size() > 1) {
            WebElement secondFooterLink = footerLinks.get(1);
            secondFooterLink.click();
            handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            assertTrue(driver.getCurrentUrl().contains("github.com") || 
                       driver.getCurrentUrl().contains("linkedin.com") ||
                       driver.getCurrentUrl().contains("twitter.com"));
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(7)
    public void testHomePageFeatures() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Verify feature sections
        List<WebElement> featureSections = driver.findElements(By.cssSelector(".feature-section"));
        assertTrue(featureSections.size() >= 2);
        
        // Verify buttons exist
        List<WebElement> buttons = driver.findElements(By.cssSelector("button"));
        assertTrue(buttons.size() >= 2);
        
        // Verify call-to-action section
        WebElement ctaSection = driver.findElement(By.cssSelector(".cta-section"));
        assertTrue(ctaSection.isDisplayed());
    }

    @Test
    @Order(8)
    public void testResponsiveDesign() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Verify responsive meta tag exists
        WebElement viewportMeta = driver.findElement(By.cssSelector("meta[name='viewport']"));
        assertTrue(viewportMeta.isDisplayed());
        
        // Check various screen sizes by resizing window
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().window().setSize(new Dimension(1200, 800));
        driver.manage().window().setSize(new Dimension(768, 1024));
        driver.manage().window().setSize(new Dimension(375, 667));
        
        // Verify core elements are still visible after resize
        WebElement header = driver.findElement(By.tagName("header"));
        WebElement navigation = driver.findElement(By.cssSelector("nav"));
        WebElement mainContent = driver.findElement(By.cssSelector("main"));
        
        assertTrue(header.isDisplayed());
        assertTrue(navigation.isDisplayed());
        assertTrue(mainContent.isDisplayed());
    }
}