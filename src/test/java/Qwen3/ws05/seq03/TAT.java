package Qwen3.ws05.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacTatTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
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
        assertEquals("Test Automation Tutorial", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Click About link
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();
        wait.until(ExpectedConditions.urlContains("about.html"));
        assertTrue(driver.getCurrentUrl().contains("about.html"));
        
        // Navigate back to home
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Click Contact link
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("contact.html"));
        assertTrue(driver.getCurrentUrl().contains("contact.html"));
        
        // Navigate back to home
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Click Home link
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(3)
    public void testAboutPageContent() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/about.html");
        
        // Verify page title
        assertEquals("About - Test Automation Tutorial", driver.getTitle());
        
        // Verify content is present
        WebElement content = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("main")));
        assertTrue(content.isDisplayed());
        assertTrue(driver.getPageSource().contains("Test Automation Tutorial"));
    }

    @Test
    @Order(4)
    public void testContactForm() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/contact.html");
        
        // Fill contact form
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("John Doe");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("john.doe@example.com");
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("Test message for contact form");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Check for success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".alert-success")).getText().contains("Message sent successfully"));
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Click Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("[href*='twitter']"));
        twitterLink.click();
        String currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
        
        // Click Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("[href*='facebook']"));
        facebookLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
        
        // Click LinkedIn link
        WebElement linkedinLink = driver.findElement(By.cssSelector("[href*='linkedin']"));
        linkedinLink.click();
        currentWindowHandle = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(currentWindowHandle);
    }

    @Test
    @Order(6)
    public void testNavigationMenu() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Verify navigation menu is present
        WebElement navMenu = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("nav")));
        assertTrue(navMenu.isDisplayed());
        
        // Check menu items
        assertTrue(driver.findElement(By.linkText("Home")).isDisplayed());
        assertTrue(driver.findElement(By.linkText("About")).isDisplayed());
        assertTrue(driver.findElement(By.linkText("Contact")).isDisplayed());
    }

    @Test
    @Order(7)
    public void testFormValidation() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/contact.html");
        
        // Submit empty form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // Check for validation errors
        WebElement errorElements = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(driver.findElements(By.cssSelector(".error-message")).size() > 0);
    }

    @Test
    @Order(8)
    public void testResetForm() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/contact.html");
        
        // Fill form fields
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("Test User");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("test@example.com");
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("Test message");
        
        // Click reset button
        WebElement resetButton = driver.findElement(By.cssSelector("button[type='reset']"));
        resetButton.click();
        
        // Verify fields are cleared
        assertEquals("", nameField.getAttribute("value"));
        assertEquals("", emailField.getAttribute("value"));
        assertEquals("", messageField.getAttribute("value"));
    }
}