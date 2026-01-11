package deepseek.ws05.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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
    public void testPageLoad() {
        driver.get(BASE_URL);
        WebElement formTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h1")));
        assertEquals("TAT Contact Form", formTitle.getText(), "Page title should match");
    }

    @Test
    @Order(2)
    public void testSuccessfulFormSubmission() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("lastName"));
        lastName.sendKeys("Doe");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement phone = driver.findElement(By.id("phone"));
        phone.sendKeys("11987654321");
        
        WebElement productSelect = driver.findElement(By.id("product"));
        Select productDropdown = new Select(productSelect);
        productDropdown.selectByValue("blog");
        
        WebElement emailRadio = driver.findElement(By.cssSelector("input[value='email']"));
        emailRadio.click();
        
        WebElement message = driver.findElement(By.id("email"));
        message.sendKeys("This is a test message");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".success")));
        assertTrue(successMessage.isDisplayed(), "Success message should be displayed");
    }

    @Test
    @Order(3)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit']")));
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".error")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear for missing required fields");
    }

    @Test
    @Order(4)
    public void testPhoneFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement phone = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("phone")));
        phone.sendKeys("invalid");
        
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".error")));
        assertTrue(errorMessage.getText().contains("Phone"), "Should show phone validation error");
    }

    @Test
    @Order(5)
    public void testProductSelection() {
        driver.get(BASE_URL);
        
        WebElement productSelect = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("product")));
        Select productDropdown = new Select(productSelect);
        
        productDropdown.selectByValue("courses");
        assertEquals("courses", productDropdown.getFirstSelectedOption().getAttribute("value"),
            "Should select courses option");
        
        productDropdown.selectByValue("mentoring");
        assertEquals("mentoring", productDropdown.getFirstSelectedOption().getAttribute("value"),
            "Should select mentoring option");
    }

    @Test
    @Order(6)
    public void testContactMethodSelection() {
        driver.get(BASE_URL);
        
        WebElement emailRadio = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[value='email']")));
        emailRadio.click();
        assertTrue(emailRadio.isSelected(), "Email radio should be selected");
        
        WebElement phoneRadio = driver.findElement(By.cssSelector("input[value='phone']"));
        phoneRadio.click();
        assertTrue(phoneRadio.isSelected(), "Phone radio should be selected");
    }

    @Test
    @Order(7)
    public void testResetButton() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("firstName")));
        firstName.sendKeys("Test");
        
        WebElement resetButton = driver.findElement(By.cssSelector("button[type='reset']"));
        resetButton.click();

        assertEquals("", firstName.getAttribute("value"), "Reset should clear the form");
    }

    @Test
    @Order(8)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github']")));
        testExternalLink(githubLink, "github.com");

        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='linkedin']")));
        testExternalLink(linkedinLink, "linkedin.com");
    }

    private void testExternalLink(WebElement link, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "External link should open " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}