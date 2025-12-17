package deepseek.ws04.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
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
    public void testFormSubmission() {
        driver.get(BASE_URL);

        // Fill out form fields
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("gender")).sendKeys("Male");
        driver.findElement(By.id("dob")).sendKeys("01/01/1990");
        driver.findElement(By.id("address")).sendKeys("123 Main St");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("password")).sendKeys("Passw0rd!");
        driver.findElement(By.id("company")).sendKeys("Acme Inc");
        
        // Select role and expectation
        Select roleSelect = new Select(driver.findElement(By.id("role")));
        roleSelect.selectByVisibleText("QA");
        
        Select expectationSelect = new Select(driver.findElement(By.id("expectation")));
        expectationSelect.selectByVisibleText("High salary");
        expectationSelect.selectByVisibleText("Good teamwork");
        
        // Submit form
        driver.findElement(By.id("submit")).click();

        // Verify submission
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(success.getText().contains("Successfully submitted!"),
                "Form submission should be successful");
    }

    @Test
    @Order(2)
    public void testFormValidation() {
        driver.get(BASE_URL);
        
        // Submit empty form
        driver.findElement(By.id("submit")).click();

        // Verify validation errors
        WebElement firstNameError = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("#first-name + .invalid-feedback")));
        Assertions.assertTrue(firstNameError.isDisplayed(),
                "First name validation error should be displayed");
        
        WebElement lastNameError = driver.findElement(By.cssSelector("#last-name + .invalid-feedback"));
        Assertions.assertTrue(lastNameError.isDisplayed(),
                "Last name validation error should be displayed");
    }

    @Test
    @Order(3)
    public void testFormReset() {
        driver.get(BASE_URL);
        
        // Fill some fields
        driver.findElement(By.id("first-name")).sendKeys("Test");
        driver.findElement(By.id("last-name")).sendKeys("User");
        
        // Reset form
        driver.findElement(By.id("reset")).click();

        // Verify fields are cleared
        Assertions.assertTrue(driver.findElement(By.id("first-name")).getText().isEmpty(),
                "First name should be cleared after reset");
        Assertions.assertTrue(driver.findElement(By.id("last-name")).getText().isEmpty(),
                "Last name should be cleared after reset");
    }

    @Test
    @Order(4)
    public void testLinkNavigation() {
        driver.get(BASE_URL);
        
        // Test Help link
        String originalWindow = driver.getWindowHandle();
        WebElement helpLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Help")));
        helpLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("katalon-test"),
                "Help link should navigate to correct URL");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testCommentField() {
        driver.get(BASE_URL);
        
        WebElement comment = wait.until(ExpectedConditions.elementToBeClickable(By.id("comment")));
        comment.sendKeys("This is a test comment with more than 20 characters");
        
        Assertions.assertTrue(comment.getAttribute("value").length() > 20,
                "Should allow long text in comment field");
    }
}