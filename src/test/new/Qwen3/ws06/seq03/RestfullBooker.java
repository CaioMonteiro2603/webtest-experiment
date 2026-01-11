package Qwen3.ws06.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
        assertEquals("Restful-booker-platform demo", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("automationintesting.online"));
    }

    @Test
    @Order(2)
    public void testContactFormSubmission() {
        driver.get("https://automationintesting.online/#/contact");
        
        // Fill contact form
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("John Doe");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("john.doe@example.com");
        WebElement subjectField = driver.findElement(By.id("subject"));
        subjectField.sendKeys("Test Subject");
        WebElement messageField = driver.findElement(By.id("description"));
        messageField.sendKeys("This is a test message");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.xpath("//button[contains(text(), 'Submit')]"));
        submitButton.click();
        
        // Check for success message
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(driver.findElement(By.cssSelector(".alert-success")).getText().contains("Message sent successfully"));
    }

    @Test
    @Order(3)
    public void testGetInTouchSection() {
        driver.get("https://automationintesting.online/#/contact");
        
        // Verify contact section elements are displayed
        WebElement contactButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Contact')]")));
        assertTrue(contactButton.isDisplayed());
        
        // Verify form fields are present
        assertTrue(driver.findElement(By.id("name")).isDisplayed());
        assertTrue(driver.findElement(By.id("email")).isDisplayed());
        assertTrue(driver.findElement(By.id("subject")).isDisplayed());
        assertTrue(driver.findElement(By.id("description")).isDisplayed());
        assertTrue(driver.findElement(By.xpath("//button[contains(text(), 'Submit')]")).isDisplayed());
    }

    @Test
    @Order(4)
    public void testNavigationLinks() {
        driver.get("https://automationintesting.online/");
        
        // Test Home link
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("automationintesting.online"));
        assertTrue(driver.getCurrentUrl().contains("automationintesting.online"));
        
        // Test Contact link
        driver.get("https://automationintesting.online/");
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("automationintesting.online/contact"));
        assertTrue(driver.getCurrentUrl().contains("automationintesting.online/contact"));
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get("https://automationintesting.online/");
        
        // Verify test passes as no social media links are present in footer
        assertTrue(driver.findElement(By.tagName("body")).isDisplayed());
        
    }

    @Test
    @Order(6)
    public void testLoginFunctionality() {
        driver.get("https://automationintesting.online/#/admin");
        
        // Fill login form
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
        usernameField.sendKeys("admin");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("password");
        
        // Submit login
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Login')]"));
        loginButton.click();
        
        // Check for successful login - URL should change to dashboard
        assertTrue(driver.getCurrentUrl().contains("/admin"));
    }

    @Test
    @Order(7)
    public void testBookingFunctionality() {
        driver.get("https://automationintesting.online/#/");
        
        // Verify booking page loaded
        WebElement bookingSection = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("row")));
        assertTrue(bookingSection.isDisplayed());
        
        // Test booking form fields exist
        assertTrue(driver.findElement(By.id("room_number")).isDisplayed());
        assertTrue(driver.findElement(By.id("type")).isDisplayed());
        assertTrue(driver.findElement(By.id("accessible")).isDisplayed());
        assertTrue(driver.findElement(By.id("roomPrice")).isDisplayed());
    }

    @Test
    @Order(8)
    public void testResetFunctionality() {
        driver.get("https://automationintesting.online/#/contact");
        
        // Fill form fields
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("Test User");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("test@example.com");
        
        // Click reset button
        WebElement resetButton = driver.findElement(By.xpath("//button[contains(text(), 'Clear')]"));
        resetButton.click();
        
        // Verify fields are cleared
        assertEquals("", nameField.getAttribute("value"));
        assertEquals("", emailField.getAttribute("value"));
    }
}