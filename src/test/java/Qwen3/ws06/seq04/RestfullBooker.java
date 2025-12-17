package Qwen3.ws06.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

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
    public void testHomePageLoad() {
        driver.get("https://automationintesting.online/");
        String title = driver.getTitle();
        assertEquals("Automation Testing", title);
        assertTrue(driver.getCurrentUrl().contains("automationintesting.online"));
    }

    @Test
    @Order(2)
    public void testNavigationToContactPage() {
        driver.get("https://automationintesting.online/");
        
        // Find contact link and click it
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();
        
        wait.until(ExpectedConditions.urlContains("contact"));
        assertTrue(driver.getCurrentUrl().contains("contact"));
        
        // Verify contact page elements
        WebElement contactHeader = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
        assertTrue(contactHeader.getText().contains("Contact"));
        
        // Navigate back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("automationintesting.online"));
        assertEquals("https://automationintesting.online/", driver.getCurrentUrl());
    }

    @Test
    @Order(3)
    public void testContactFormSubmission() {
        driver.get("https://automationintesting.online/contact");
        
        // Fill in form fields
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("Test User");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("test@example.com");
        
        WebElement subjectField = driver.findElement(By.id("subject"));
        subjectField.sendKeys("Test Subject");
        
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("This is a test message for the contact form");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Verify success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("Message sent successfully"));
    }

    @Test
    @Order(4)
    public void testHomePageContent() {
        driver.get("https://automationintesting.online/");
        
        // Verify main heading
        WebElement mainHeading = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
        assertTrue(mainHeading.getText().contains("Automation Testing"));
        
        // Verify navigation elements
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        assertTrue(navLinks.size() >= 3);
        
        // Verify feature sections
        List<WebElement> featureSections = driver.findElements(By.cssSelector(".feature"));
        assertTrue(featureSections.size() >= 2);
        
        // Verify footer elements
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed());
    }

    @Test
    @Order(5)
    public void testInvalidContactFormSubmission() {
        driver.get("https://automationintesting.online/contact");
        
        // Submit form without filling any fields
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Verify error messages appear
        WebElement errorElements = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error")));
        assertTrue(errorElements.isDisplayed());
    }

    @Test
    @Order(6)
    public void testNavigationThroughAllPages() {
        driver.get("https://automationintesting.online/");
        
        // Navigate to About page
        WebElement aboutLink = driver.findElement(By.linkText("About"));
        aboutLink.click();
        wait.until(ExpectedConditions.urlContains("about"));
        assertTrue(driver.getCurrentUrl().contains("about"));
        
        // Navigate back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("automationintesting.online"));
        
        // Navigate to Services page
        WebElement servicesLink = driver.findElement(By.linkText("Services"));
        servicesLink.click();
        wait.until(ExpectedConditions.urlContains("services"));
        assertTrue(driver.getCurrentUrl().contains("services"));
        
        // Navigate back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("automationintesting.online"));
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get("https://automationintesting.online/");
        
        // Get footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() >= 2);
        
        String originalHandle = driver.getWindowHandle();
        
        // Test external links in footer (assuming they open in new tabs)
        for (int i = 0; i < Math.min(2, footerLinks.size()); i++) {
            WebElement link = footerLinks.get(i);
            if (link.getAttribute("href") != null && 
                (link.getAttribute("href").contains("github") || 
                 link.getAttribute("href").contains("twitter") ||
                 link.getAttribute("href").contains("linkedin"))) {
                link.click();
                Set<String> handles = driver.getWindowHandles();
                for (String handle : handles) {
                    if (!handle.equals(originalHandle)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
                // Verify it's an external domain
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains("github.com") || 
                           currentUrl.contains("twitter.com") ||
                           currentUrl.contains("linkedin.com"));
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }
    }

    @Test
    @Order(8)
    public void testResponsiveDesign() {
        driver.get("https://automationintesting.online/");
        
        // Test various screen sizes
        Dimension[] screenSizes = {
            new Dimension(1920, 1080),
            new Dimension(1200, 800),
            new Dimension(768, 1024),
            new Dimension(375, 667)
        };
        
        for (Dimension size : screenSizes) {
            driver.manage().window().setSize(size);
            // Verify core elements are still visible
            WebElement header = driver.findElement(By.tagName("header"));
            WebElement navigation = driver.findElement(By.cssSelector("nav"));
            WebElement mainContent = driver.findElement(By.cssSelector("main"));
            
            assertTrue(header.isDisplayed(), "Header not displayed on " + size.width + "x" + size.height);
            assertTrue(navigation.isDisplayed(), "Navigation not displayed on " + size.width + "x" + size.height);
            assertTrue(mainContent.isDisplayed(), "Main content not displayed on " + size.width + "x" + size.height);
        }
    }

    @Test
    @Order(9)
    public void testContactPageFormValidation() {
        driver.get("https://automationintesting.online/contact");
        
        // Populate only required fields for validation
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("Test");
        
        // Submit without email to trigger validation
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Wait for validation error
        try {
            WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-email")));
            assertTrue(errorMessage.isDisplayed());
        } catch (TimeoutException e) {
            // If no specific error element, check for general validation
            assertTrue(driver.findElements(By.cssSelector(".error")).size() > 0);
        }
    }
}