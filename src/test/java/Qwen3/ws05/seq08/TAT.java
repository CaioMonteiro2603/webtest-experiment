package Qwen3.ws05.seq08;

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
        assertEquals("CAC TAT - Test Automation", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();
        assertTrue(driver.getCurrentUrl().contains("about.html"));
        
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        WebElement servicesLink = driver.findElement(By.linkText("Services"));
        servicesLink.click();
        assertTrue(driver.getCurrentUrl().contains("services.html"));
        
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        assertTrue(driver.getCurrentUrl().contains("contact.html"));
        
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        homeLink.click();
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".menu-toggle")));
        menuButton.click();
        
        WebElement menuLinks = driver.findElement(By.cssSelector(".menu"));
        assertTrue(menuLinks.isDisplayed());
        
        List<WebElement> links = menuLinks.findElements(By.tagName("a"));
        assertEquals(4, links.size());
        
        WebElement aboutLink = links.get(0);
        assertEquals("About", aboutLink.getText());
        
        WebElement servicesLink = links.get(1);
        assertEquals("Services", servicesLink.getText());
        
        WebElement contactLink = links.get(2);
        assertEquals("Contact", contactLink.getText());
        
        WebElement homeLink = links.get(3);
        assertEquals("Home", homeLink.getText());
    }

    @Test
    @Order(4)
    public void testFormSubmission() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/contact.html");
        
        WebElement nameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement messageField = driver.findElement(By.id("message"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        nameField.sendKeys("John Doe");
        emailField.sendKeys("john.doe@example.com");
        messageField.sendKeys("Test message for contact form");
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success-message")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("Thank you for your message"));
    }

    @Test
    @Order(5)
    public void testSocialMediaLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".social-links a"));
        assertEquals(3, socialLinks.size());
        
        String originalWindow = driver.getWindowHandle();
        
        for (int i = 0; i < socialLinks.size(); i++) {
            WebElement link = socialLinks.get(i);
            String href = link.getAttribute("href");
            assertNotNull(href);
            link.click();
            
            Set<String> windowHandles = driver.getWindowHandles();
            String newWindow = windowHandles.stream()
                    .filter(w -> !w.equals(originalWindow))
                    .findFirst()
                    .orElse(null);
            
            if (newWindow != null) {
                driver.switchTo().window(newWindow);
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("twitter.com") || 
                           currentUrl.contains("facebook.com") || 
                           currentUrl.contains("linkedin.com"));
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(6)
    public void testServicesPage() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/services.html");
        
        assertEquals("Services - CAC TAT", driver.getTitle());
        
        List<WebElement> serviceCards = driver.findElements(By.cssSelector(".service-card"));
        assertTrue(serviceCards.size() > 0);
        
        for (WebElement card : serviceCards) {
            assertTrue(card.isDisplayed());
            WebElement title = card.findElement(By.cssSelector(".service-title"));
            assertTrue(title.isDisplayed());
            WebElement description = card.findElement(By.cssSelector(".service-description"));
            assertTrue(description.isDisplayed());
        }
    }

    @Test
    @Order(7)
    public void testAboutPage() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/about.html");
        
        assertEquals("About - CAC TAT", driver.getTitle());
        
        WebElement aboutContent = driver.findElement(By.cssSelector(".about-content"));
        assertTrue(aboutContent.isDisplayed());
        
        WebElement teamSection = driver.findElement(By.cssSelector(".team-section"));
        assertTrue(teamSection.isDisplayed());
    }

    @Test
    @Order(8)
    public void testContactPage() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/contact.html");
        
        assertEquals("Contact - CAC TAT", driver.getTitle());
        
        WebElement contactForm = driver.findElement(By.cssSelector(".contact-form"));
        assertTrue(contactForm.isDisplayed());
        
        List<WebElement> formFields = contactForm.findElements(By.tagName("input"));
        assertTrue(formFields.size() >= 2);
        
        WebElement messageField = contactForm.findElement(By.tagName("textarea"));
        assertTrue(messageField.isDisplayed());
    }
}