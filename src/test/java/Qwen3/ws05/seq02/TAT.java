package Qwen3.ws05.seq02;

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
public class CacTatTest {
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
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        wait.until(ExpectedConditions.titleContains("TAT"));
        assertTrue(driver.getTitle().contains("TAT"));
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test About link
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();
        assertTrue(driver.getCurrentUrl().contains("about.html"));
        
        // Navigate back
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("index.html"));
        
        // Test Services link
        WebElement servicesLink = driver.findElement(By.linkText("Services"));
        servicesLink.click();
        assertTrue(driver.getCurrentUrl().contains("services.html"));
        
        // Navigate back
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("index.html"));
        
        // Test Contact link
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        assertTrue(driver.getCurrentUrl().contains("contact.html"));
        
        // Navigate back
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("index.html"));
    }

    @Test
    @Order(3)
    public void testContactForm() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/contact.html");
        
        // Fill contact form
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("John Doe");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("john.doe@example.com");
        
        WebElement subjectField = driver.findElement(By.id("subject"));
        subjectField.sendKeys("Test Subject");
        
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("This is a test message.");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        // Verify form submission (page reload or success message)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("success-message")));
        assertTrue(driver.getCurrentUrl().contains("contact.html"));
    }

    @Test
    @Order(4)
    public void testServicesPage() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/services.html");
        
        // Verify services list exists
        WebElement servicesList = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("services-list")));
        assertTrue(servicesList.isDisplayed());
        
        // Count services
        List<WebElement> services = driver.findElements(By.cssSelector(".service-item"));
        assertTrue(services.size() > 0);
    }

    @Test
    @Order(5)
    public void testAboutPage() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/about.html");
        
        // Verify about section exists
        WebElement aboutSection = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("about-section")));
        assertTrue(aboutSection.isDisplayed());
        
        // Verify team members section
        WebElement teamSection = driver.findElement(By.id("team-section"));
        assertTrue(teamSection.isDisplayed());
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("[href*='twitter.com']"));
        twitterLink.click();
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("twitter.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
        
        // Test Facebook link
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        WebElement facebookLink = driver.findElement(By.cssSelector("[href*='facebook.com']"));
        facebookLink.click();
        originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("facebook.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
        
        // Test LinkedIn link
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        WebElement linkedinLink = driver.findElement(By.cssSelector("[href*='linkedin.com']"));
        linkedinLink.click();
        originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testHeaderNavigation() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test Home link
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        assertEquals("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html", driver.getCurrentUrl());
        
        // Test Navigation menu items
        List<WebElement> navItems = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navItems.size() > 0);
        
        // Verify each navigation item is clickable
        for (WebElement item : navItems) {
            if (item.isDisplayed() && !item.getText().isEmpty()) {
                // Just verify they are clickable
                assertTrue(item.isEnabled());
            }
        }
    }

    @Test
    @Order(8)
    public void testImageGallery() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Find gallery images
        List<WebElement> galleryImages = driver.findElements(By.cssSelector(".gallery img"));
        if (galleryImages.size() > 0) {
            for (WebElement img : galleryImages) {
                assertTrue(img.isDisplayed());
            }
        }
    }

    @Test
    @Order(9)
    public void testAccessibility() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Verify title exists 
        assertTrue(driver.getTitle().length() > 0);
        
        // Verify main heading exists
        WebElement mainHeading = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
        assertTrue(mainHeading.isDisplayed());
        assertTrue(mainHeading.getText().length() > 0);
        
        // Verify page has a meta description
        WebElement metaDescription = driver.findElement(By.cssSelector("meta[name='description']"));
        assertTrue(metaDescription.getAttribute("content").length() > 0);
    }

}