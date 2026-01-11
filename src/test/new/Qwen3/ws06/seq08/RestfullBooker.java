package Qwen3.ws06.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        assertEquals("Restful-booker-platform demo", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("/"));
    }

    @Test
    @Order(2)
    public void testNavigationToBookingPage() {
        driver.get("https://automationintesting.online/");
        WebElement bookThisRoomButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-outline-primary")));
        assertTrue(bookThisRoomButton.isDisplayed());
    }

    @Test
    @Order(3)
    public void testBookingFormSubmission() {
        driver.get("https://automationintesting.online/");
        
        WebElement bookThisRoomButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-outline-primary")));
        bookThisRoomButton.click();
        
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Firstname']")));
        WebElement lastNameField = driver.findElement(By.cssSelector("input[placeholder='Lastname']"));
        WebElement emailField = driver.findElement(By.cssSelector("input[placeholder='Email']"));
        WebElement phoneField = driver.findElement(By.cssSelector("input[placeholder='Phone']"));
        WebElement submitButton = driver.findElement(By.cssSelector("button.btn.btn-outline-primary.float-right"));
        
        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        emailField.sendKeys("john.doe@example.com");
        phoneField.sendKeys("1234567890");
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.alert.alert-success")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("Booking Successful"));
    }

    @Test
    @Order(4)
    public void testContactFormSubmission() {
        driver.get("https://automationintesting.online/");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        
        WebElement nameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Name']")));
        WebElement emailField = driver.findElement(By.cssSelector("input[placeholder='Email']"));
        WebElement phoneField = driver.findElement(By.cssSelector("input[placeholder='Phone']"));
        WebElement subjectField = driver.findElement(By.cssSelector("input[placeholder='Subject']"));
        WebElement messageField = driver.findElement(By.cssSelector("textarea[placeholder*='your message']"));
        WebElement submitButton = driver.findElement(By.cssSelector("button#submitContact"));
        
        nameField.sendKeys("John Doe");
        emailField.sendKeys("john.doe@example.com");
        phoneField.sendKeys("1234567890");
        subjectField.sendKeys("Test Subject");
        messageField.sendKeys("Test message for contact form");
        submitButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.alert.alert-success")));
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("Thanks for getting in touch"));
    }

    @Test
    @Order(5)
    public void testInvalidBookingSubmission() {
        driver.get("https://automationintesting.online/");
        
        WebElement bookThisRoomButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-outline-primary")));
        bookThisRoomButton.click();
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-outline-primary.float-right")));
        submitButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.alert-danger")));
        assertTrue(errorMessage.isDisplayed());
        assertTrue(errorMessage.getText().contains("All fields are required"));
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get("https://automationintesting.online/");
        
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertTrue(footerLinks.size() >= 1);
        
        boolean hasValidLink = false;
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && !href.contains("javascript:void")) {
                hasValidLink = true;
                break;
            }
        }
        assertTrue(hasValidLink);
    }

    @Test
    @Order(7)
    public void testRoomListingAndSorting() {
        driver.get("https://automationintesting.online/");
        
        WebElement roomDetails = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.col-sm-7")));
        assertTrue(roomDetails.isDisplayed());
        
        List<WebElement> roomItems = driver.findElements(By.cssSelector("div.col-sm-7 h4"));
        assertTrue(roomItems.size() > 0);
        
        WebElement priceElement = driver.findElement(By.cssSelector("div.col-sm-7 p"));
        assertTrue(priceElement.getText().contains("£"));
    }

    @Test
    @Order(8)
    public void testBookingDetails() {
        driver.get("https://automationintesting.online/");
        
        WebElement bookThisRoomButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn.btn-outline-primary")));
        bookThisRoomButton.click();
        
        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Firstname']")));
        WebElement lastNameField = driver.findElement(By.cssSelector("input[placeholder='Lastname']"));
        WebElement emailField = driver.findElement(By.cssSelector("input[placeholder='Email']"));
        WebElement phoneField = driver.findElement(By.cssSelector("input[placeholder='Phone']"));
        
        assertTrue(firstNameField.isDisplayed());
        assertTrue(lastNameField.isDisplayed());
        assertTrue(emailField.isDisplayed());
        assertTrue(phoneField.isDisplayed());
        
        assertEquals("text", firstNameField.getAttribute("type"));
        assertEquals("email", emailField.getAttribute("type"));
        assertEquals("text", phoneField.getAttribute("type"));
    }
}