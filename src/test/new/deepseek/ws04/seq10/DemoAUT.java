package deepseek.ws04.seq10;

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
    private static WebDriver driver;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
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
        
        // Fill form
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("John");
        
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");
        
        WebElement gender = driver.findElement(By.id("gender"));
        Select genderSelect = new Select(gender);
        genderSelect.selectByVisibleText("Male");
        
        WebElement dob = driver.findElement(By.id("dob"));
        dob.sendKeys("01/01/1990");
        
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main St");
        
        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");
        
        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("secure123");
        
        WebElement company = driver.findElement(By.id("company"));
        company.sendKeys("ACME Inc");
        
        WebElement role = driver.findElement(By.id("role"));
        Select roleSelect = new Select(role);
        roleSelect.selectByVisibleText("QA");
        
        WebElement jobExpectation = driver.findElement(By.id("expectation"));
        Select expectationSelect = new Select(jobExpectation);
        expectationSelect.selectByVisibleText("Good teamwork");
        expectationSelect.selectByVisibleText("High salary");
        
        WebElement submitButton = driver.findElement(By.id("submit"));
        submitButton.click();
        
        // Verify submission
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(text(), 'Successfully submitted!')]")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Form submission failed");
    }

    @Test
    @Order(2)
    public void testRequiredFieldValidation() {
        driver.get(BASE_URL);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submitButton.click();
        
        try {
            WebElement firstName = driver.findElement(By.id("first-name"));
            String validationMessage = firstName.getAttribute("validationMessage");
            Assertions.assertNotNull(validationMessage);
            Assertions.assertFalse(validationMessage.isEmpty(), "Required field validation failed");
        } catch (Exception e) {
            Assertions.fail("Required field validation failed");
        }
    }

    @Test
    @Order(3)
    public void testFormReset() {
        driver.get(BASE_URL);
        
        // Fill some fields
        WebElement firstName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("first-name")));
        firstName.sendKeys("Test");
        
        try {
            ((WebElement) ((JavascriptExecutor) driver).executeScript("return document.querySelector('input[type=\"reset\"]')")).click();
        } catch (Exception e) {
            Assertions.fail("Form reset failed - reset button not found");
        }
        
        Assertions.assertEquals("", firstName.getAttribute("value"), "Form reset failed");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test Help link
        testExternalLink("Help", "katalon.com");
        
        // Test Privacy Policy link
        testExternalLink("Privacy Policy", "katalon.com");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        try {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), '" + linkText + "')]")));
            link.click();
            
            // Switch to new window if opened
            if (driver.getWindowHandles().size() > 1) {
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(mainWindow)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                    linkText + " link failed - wrong domain");
                driver.close();
                driver.switchTo().window(mainWindow);
            }
        } catch (Exception e) {
            try {
                driver.switchTo().window(mainWindow);
            } catch (Exception ignored) {}
            Assertions.fail(linkText + " link failed - " + e.getMessage());
        }
    }
}