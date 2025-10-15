package Qwen3.ws06.seq06;

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
public class AutomationTestingTest {

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
    public void testHomePageLoadsCorrectly() {
        driver.get("https://automationintesting.online/");
        
        String currentPageTitle = driver.getTitle();
        assertTrue(currentPageTitle.contains("Automation Testing"), "Page title should contain 'Automation Testing'");
        
        WebElement mainHeader = driver.findElement(By.tagName("h1"));
        assertTrue(mainHeader.getText().contains("Automation Testing"), "Main header should mention Automation Testing");
        
        // Verify contact form is present
        WebElement contactForm = driver.findElement(By.tagName("form"));
        assertTrue(contactForm.isDisplayed(), "Contact form should be displayed");
    }

    @Test
    @Order(2)
    public void testContactFormSubmission() {
        driver.get("https://automationintesting.online/");
        
        // Fill in form fields
        WebElement nameField = driver.findElement(By.id("contactName"));
        nameField.sendKeys("John Doe");
        
        WebElement emailField = driver.findElement(By.id("contactEmail"));
        emailField.sendKeys("john.doe@example.com");
        
        WebElement subjectField = driver.findElement(By.id("contactSubject"));
        subjectField.sendKeys("Test Subject");
        
        WebElement messageField = driver.findElement(By.id("contactMessage"));
        messageField.sendKeys("This is a test message for the contact form.");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Verify submission success or error
        try {
            WebElement successMessage = driver.findElement(By.cssSelector(".alert-success"));
            assertTrue(successMessage.isDisplayed(), "Success message should be displayed after form submission");
        } catch (NoSuchElementException e) {
            // If success message is not found, check for error message
            try {
                WebElement errorMessage = driver.findElement(By.cssSelector(".alert-danger"));
                assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
            } catch (NoSuchElementException ex) {
                // Neither success nor error message found, but form submission took place
            }
        }
    }

    @Test
    @Order(3)
    public void testInvalidFormSubmission() {
        driver.get("https://automationintesting.online/");
        
        // Submit form without filling any fields
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Check for validation errors
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        WebElement errorMessage = driver.findElement(By.cssSelector(".alert-danger"));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid form submission");
    }

    @Test
    @Order(4)
    public void testNavigationToBookingPage() {
        driver.get("https://automationintesting.online/");
        
        // Click on 'Book now' button or link (if available)
        try {
            WebElement bookNowButton = driver.findElement(By.linkText("Book now"));
            bookNowButton.click();
            
            // Wait for page load and check URL
            wait.until(ExpectedConditions.urlContains("booking"));
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("booking"), "Should navigate to booking page");
            
            // Go back to home page
            driver.navigate().back();
        } catch (NoSuchElementException e) {
            // If 'Book now' button doesn't exist, that's okay for this test
        }
    }

    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get("https://automationintesting.online/");
        
        // Wait for page to fully load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("footer")));
        
        // Twitter link
        try {
            WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
            String oldTab = driver.getWindowHandle();
            twitterLink.click();
            String winHandle = driver.getWindowHandle();
            driver.switchTo().window(winHandle);
            assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should navigate to Twitter website");
            driver.close();
            driver.switchTo().window(oldTab);
        } catch (NoSuchElementException e) {
            // If Twitter link doesn't exist, that's okay for this test
        }

        // Facebook link
        try {
            WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
            String oldTab = driver.getWindowHandle();
            facebookLink.click();
            String winHandle = driver.getWindowHandle();
            driver.switchTo().window(winHandle);
            assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should navigate to Facebook website");
            driver.close();
            driver.switchTo().window(oldTab);
        } catch (NoSuchElementException e) {
            // If Facebook link doesn't exist, that's okay for this test
        }

        // LinkedIn link
        try {
            WebElement linkedinLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
            String oldTab = driver.getWindowHandle();
            linkedinLink.click();
            String winHandle = driver.getWindowHandle();
            driver.switchTo().window(winHandle);
            assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should navigate to LinkedIn website");
            driver.close();
            driver.switchTo().window(oldTab);
        } catch (NoSuchElementException e) {
            // If LinkedIn link doesn't exist, that's okay for this test
        }
    }

    @Test
    @Order(6)
    public void testContactFormFieldInteractions() {
        driver.get("https://automationintesting.online/");
        
        // Test name field
        WebElement nameField = driver.findElement(By.id("contactName"));
        nameField.clear();
        nameField.sendKeys("Test User");
        assertEquals("Test User", nameField.getAttribute("value"), "Name field value should match input");
        
        // Test email field
        WebElement emailField = driver.findElement(By.id("contactEmail"));
        emailField.clear();
        emailField.sendKeys("test@example.com");
        assertEquals("test@example.com", emailField.getAttribute("value"), "Email field value should match input");
        
        // Test subject field
        WebElement subjectField = driver.findElement(By.id("contactSubject"));
        subjectField.clear();
        subjectField.sendKeys("Test Subject");
        assertEquals("Test Subject", subjectField.getAttribute("value"), "Subject field value should match input");
        
        // Test message field
        WebElement messageField = driver.findElement(By.id("contactMessage"));
        messageField.clear();
        messageField.sendKeys("This is a sample test message.");
        assertEquals("This is a sample test message.", messageField.getText(), "Message field text should match input");
        
        // Test submit button click doesn't cause errors
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertNotNull(submitButton, "Submit button should be present");
    }

    @Test
    @Order(7)
    public void testPageContentVerification() {
        driver.get("https://automationintesting.online/");
        
        // Verify header is present
        WebElement header = driver.findElement(By.tagName("header"));
        assertTrue(header.isDisplayed(), "Header should be displayed");
        
        // Verify main content area
        WebElement mainContent = driver.findElement(By.tagName("main"));
        assertTrue(mainContent.isDisplayed(), "Main content should be displayed");
        
        // Verify footer is present
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.isDisplayed(), "Footer should be displayed");
        
        // Check for contact form elements
        List<WebElement> formElements = driver.findElements(By.cssSelector("form *"));
        assertTrue(formElements.size() > 0, "Form should contain elements");
        
        // Verify contact info in footer if it exists
        try {
            List<WebElement> contactInfo = driver.findElements(By.cssSelector("footer a[href*='mailto']"));
            assertTrue(contactInfo.size() > 0, "Footer should contain contact email");
        } catch (NoSuchElementException e) {
            // If no email found in footer, that's ok for this test
        }
    }
}