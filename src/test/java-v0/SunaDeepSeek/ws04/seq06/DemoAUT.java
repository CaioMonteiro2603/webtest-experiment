package SunaDeepSeek.ws04.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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
    public void testPageLoad() {
        driver.get(BASE_URL);
        Assertions.assertEquals("A Simple Form", driver.getTitle());
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        driver.get(BASE_URL);
        
        // Fill out form fields
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");
        
        WebElement gender = driver.findElement(By.id("gender"));
        gender.findElement(By.xpath(".//option[text()='Male']")).click();
        
        WebElement dob = driver.findElement(By.id("dob"));
        dob.sendKeys("01/01/1990");
        
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main St");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("Password123");
        
        WebElement company = driver.findElement(By.id("company"));
        company.sendKeys("ACME Inc");
        
        WebElement role = driver.findElement(By.id("role"));
        role.findElement(By.xpath(".//option[text()='QA']")).click();
        
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        List<WebElement> expectations = jobExpectation.findElements(By.tagName("option"));
        for (WebElement option : expectations) {
            if (option.getText().equals("High salary") || option.getText().equals("Challenging")) {
                option.click();
            }
        }
        
        WebElement waysOfDevelopment = driver.findElement(By.id("development"));
        waysOfDevelopment.findElement(By.xpath(".//option[text()='Read books']")).click();
        
        WebElement comment = driver.findElement(By.id("comment"));
        comment.sendKeys("This is a test comment");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        // Verify submission
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-msg")));
        Assertions.assertTrue(successMessage.getText().contains("Successfully submitted!"));
    }

    @Test
    @Order(3)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        // Submit form without filling required fields
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        // Verify validation messages
        WebElement firstName = driver.findElement(By.id("first-name"));
        Assertions.assertEquals("", firstName.getAttribute("value"));
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        Assertions.assertEquals("", lastName.getAttribute("value"));
        
        // Note: The actual form might show validation messages, but the example form doesn't have explicit validation
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Help link
        String originalWindow = driver.getWindowHandle();
        WebElement helpLink = driver.findElement(By.linkText("Help"));
        helpLink.click();
        
        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        // Verify external URL
        Assertions.assertTrue(driver.getCurrentUrl().contains("katalon.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}