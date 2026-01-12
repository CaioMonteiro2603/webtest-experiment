package deepseek.ws04.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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
            By.xpath("//h2[contains(text(),'HTML Form')]")));
        assertTrue(formTitle.isDisplayed(), "Form title should be displayed");
    }

    @Test
    @Order(2)
    public void testFormSubmission() {
        driver.get(BASE_URL);
        
        // Fill form
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");
        
        WebElement gender = driver.findElement(By.id("gender"));
        Select genderSelect = new Select(gender);
        genderSelect.selectByValue("male");
        
        WebElement dob = driver.findElement(By.id("dob"));
        dob.sendKeys("01/01/1990");
        
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main St");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("Test1234");
        
        WebElement company = driver.findElement(By.id("company"));
        company.sendKeys("ACME Inc");
        
        WebElement role = driver.findElement(By.id("role"));
        Select roleSelect = new Select(role);
        roleSelect.selectByValue("QA");
        
        WebElement expectation = driver.findElement(By.id("expectation"));
        Select expectationSelect = new Select(expectation);
        expectationSelect.selectByValue("good");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        // Verify submission
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("submit-msg")));
        assertTrue(successMessage.getText().contains("Successfully submitted"), 
            "Should display success message after submission");
    }

    @Test
    @Order(3)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();

        WebElement firstNameError = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("first-name-error")));
        assertTrue(firstNameError.isDisplayed(), "Should show required field error");
    }

    @Test
    @Order(4)
    public void testEmailValidation() {
        driver.get(BASE_URL);
        
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
        email.sendKeys("invalid-email");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();

        WebElement emailError = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.id("email-error")));
        assertTrue(emailError.isDisplayed(), "Should show email validation error");
    }

    @Test
    @Order(5)
    public void testResetButton() {
        driver.get(BASE_URL);
        
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Test");
        
        WebElement resetButton = driver.findElement(By.id("reset"));
        resetButton.click();

        assertEquals("", firstName.getAttribute("value"), "Reset should clear form fields");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Katalon link
        WebElement katalonLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'katalon.com')]")));
        testExternalLink(katalonLink, "katalon.com");

        // Test Selenium link
        WebElement seleniumLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'selenium.dev')]")));
        testExternalLink(seleniumLink, "selenium.dev");
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