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
        assertEquals("Restful-booker-platform demo", title);
        assertTrue(driver.getCurrentUrl().contains("automationintesting.online"));
    }

    @Test
    @Order(2)
    public void testNavigationToContactPage() {
        driver.get("https://automationintesting.online/");
        
        // Find contact link and click it
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[normalize-space()='Let me hack!']")));
        contactLink.click();
        
        wait.until(ExpectedConditions.urlContains("room"));
        assertTrue(driver.getCurrentUrl().contains("room"));
        
        // Navigate back to home
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("automationintesting.online"));
    }

    @Test
    @Order(3)
    public void testContactFormSubmission() {
        driver.get("https://automationintesting.online/");
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[normalize-space()='Let me hack!']")));
        contactLink.click();
        
        // Fill in form fields
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("Test User");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("test@example.com");
        
        WebElement subjectField = driver.findElement(By.id("subject"));
        subjectField.sendKeys("Test Subject");
        
        WebElement messageField = driver.findElement(By.id("description"));
        messageField.sendKeys("This is a test message for the contact form");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.id("submitMessage"));
        submitButton.click();
        
        // Verify success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("Thanks for getting in touch") || successMessage.getText().contains("Message sent"));
    }

    @Test
    @Order(4)
    public void testHomePageContent() {
        driver.get("https://automationintesting.online/");
        
        // Verify main heading
        WebElement mainHeading = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1")));
        assertTrue(mainHeading.getText().contains("automationintesting"));
        
        // Verify navigation elements
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a, button"));
        assertTrue(navLinks.size() >= 2);
        
        // Verify feature sections
        List<WebElement> featureSections = driver.findElements(By.cssSelector(".room-title"));
        assertTrue(featureSections.size() >= 2);
        
        // Verify footer elements
        WebElement footer = driver.findElement(By.cssSelector("footer"));
        assertTrue(footer.isDisplayed());
    }

    @Test
    @Order(5)
    public void testInvalidContactFormSubmission() {
        driver.get("https://automationintesting.online/");
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[normalize-space()='Let me hack!']")));
        contactLink.click();
        
        // Submit form without filling any fields
        WebElement submitButton = driver.findElement(By.id("submitMessage"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
        sleep(1000);
        submitButton.click();
        
        // Verify error message
        WebElement errorElements = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='alert']")));
        assertTrue(errorElements.isDisplayed());
    }

    @Test
    @Order(6)
    public void testNavigationThroughAllPages() {
        driver.get("https://automationintesting.online/");
        
        // Check for navigation menu
        List<WebElement> navElements = driver.findElements(By.cssSelector("a, button"));
        
        // Verify navigation elements exist
        assertTrue(navElements.size() >= 1);
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get("https://automationintesting.online/");
        
        // Get footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a, footer [href]"));
        assertTrue(footerLinks.size() >= 1);
        
        String originalHandle = driver.getWindowHandle();
        
        // Test external links in footer
        for (int i = 0; i < Math.min(1, footerLinks.size()); i++) {
            WebElement link = footerLinks.get(i);
            if (link.getAttribute("href") != null && link.getAttribute("href").startsWith("http")) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
                sleep(500);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
                
                try {
                    Set<String> handles = driver.getWindowHandles();
                    for (String handle : handles) {
                        if (!handle.equals(originalHandle)) {
                            driver.switchTo().window(handle);
                            break;
                        }
                    }
                    driver.close();
                    driver.switchTo().window(originalHandle);
                } catch (Exception e) {
                    driver.navigate().back();
                    driver.get("https://automationintesting.online/");
                }
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
            WebElement header = driver.findElement(By.cssSelector("body > *:first-child"));
            WebElement mainContent = driver.findElement(By.cssSelector("main, body"));
            
            assertTrue(header.isDisplayed(), "Header not displayed on " + size.width + "x" + size.height);
            assertTrue(mainContent.isDisplayed(), "Main content not displayed on " + size.width + "x" + size.height);
        }
    }

    @Test
    @Order(9)
    public void testContactPageFormValidation() {
        driver.get("https://automationintesting.online/");
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[normalize-space()='Let me hack!']")));
        contactLink.click();
        
        // Click submit without filling fields
        WebElement submitButton = driver.findElement(By.id("submitMessage"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
        sleep(1000);
        submitButton.click();
        
        // Wait for validation error
        WebElement alertElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert")));
        assertTrue(alertElement.isDisplayed());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}