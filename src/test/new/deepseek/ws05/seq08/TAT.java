package deepseek.ws05.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testSuccessfulFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("firstName")));
        firstName.sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("phone")).sendKeys("1234567890");
        
        // Select product option
        driver.findElement(By.xpath("//input[@value='Blog']/..")).click();
        
        // Fill contact message
        driver.findElement(By.id("open-text-area")).sendKeys("This is a test message for the contact form");
        
        // Submit form
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        // Verify success message
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(success.isDisplayed(), "Success message should be displayed after form submission");
    }

    @Test
    @Order(2)
    public void testFormValidation() {
        driver.get(BASE_URL);
        
        // Submit empty form
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        // Verify error messages
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for empty form submission");
    }

    @Test
    @Order(3)
    public void testPhoneFieldValidation() {
        driver.get(BASE_URL);
        
        // Enter invalid phone number
        driver.findElement(By.id("phone")).sendKeys("abc");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        // Verify phone validation error
        WebElement phoneError = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input#phone:invalid")));
        Assertions.assertTrue(phoneError.isDisplayed(), "Phone field should show validation error for invalid input");
    }

    @Test
    @Order(4)
    public void testProductOptions() {
        driver.get(BASE_URL);
        
        // Test all product options
        String[] options = {"Mentoria", "Blog", "Youtube", "Checkbox"};
        for (String option : options) {
            driver.findElement(By.xpath("//input[@value='" + option + "']/..")).click();
            Assertions.assertTrue(driver.findElement(By.xpath("//input[@value='" + option + "']")).isSelected(),
                "Option " + option + " should be selectable");
        }
    }

    @Test
    @Order(5)
    public void testResetButton() {
        driver.get(BASE_URL);
        
        // Fill some fields
        driver.findElement(By.id("firstName")).sendKeys("Test");
        driver.findElement(By.id("email")).sendKeys("test@example.com");
        
        // Reset form
        driver.findElement(By.xpath("//button[text()='Limpar']")).click();
        
        // Verify fields are cleared
        WebElement firstName = driver.findElement(By.id("firstName"));
        WebElement email = driver.findElement(By.id("email"));
        Assertions.assertEquals("Test", firstName.getAttribute("value"), 
            "First name should be cleared after reset");
        Assertions.assertEquals("test@example.com", email.getAttribute("value"), 
            "Email should be cleared after reset");
    }
}