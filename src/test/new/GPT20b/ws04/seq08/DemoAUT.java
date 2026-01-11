package GPT20b.ws04.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* Helper to navigate to the form page */
    private void navigateToForm() {
        driver.get(BASE_URL);
    }

    /* Test 1: Verify form and field presence */
    @Test
    @Order(1)
    public void testFormPresence() {
        navigateToForm();
        By formLocator = By.tagName("form");
        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(formLocator));
        Assertions.assertNotNull(form, "Form element should be present on the page.");

        List<WebElement> inputs = driver.findElements(By.cssSelector("form input, form textarea, form select"));
        Assertions.assertFalse(inputs.isEmpty(), "Form should contain input elements.");
    }

    /* Test 2: Verify that required fields are present */
    @Test
    @Order(2)
    public void testRequiredFields() {
        navigateToForm();
        List<WebElement> requiredFields = driver.findElements(By.cssSelector("input[required], textarea[required], select[required]"));
        Assertions.assertTrue(!requiredFields.isEmpty(), "Form should have required fields.");

        for (WebElement field : requiredFields) {
            Assertions.assertTrue(field.isDisplayed(), "Required field should be visible.");
        }
    }

    /* Test 3: Fill all fields and submit the form */
    @Test
    @Order(3)
    public void testFormSubmission() {
        navigateToForm();
        // Fill required fields
        List<WebElement> requiredFields = driver.findElements(By.cssSelector("input[required], textarea[required], select[required]"));
        for (WebElement field : requiredFields) {
            if (field.getTagName().equalsIgnoreCase("select")) {
                field.sendKeys("Test");
            } else {
                field.clear();
                field.sendKeys("Test");
            }
        }

        // Submit
        By submitLocator = By.cssSelector("input[type='submit'], button[type='submit']");
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(submitLocator));
        submitBtn.click();

        // After submission, verify that the form is still present and no error elements
        By errorLocator = By.cssSelector(".error, .alert, .validation-error");
        List<WebElement> errors = driver.findElements(errorLocator);
        Assertions.assertTrue(errors.isEmpty(), "No validation error should appear after filling all required fields.");

        By formLocator = By.tagName("form");
        Assertions.assertTrue(driver.findElement(formLocator).isDisplayed(), "Form should remain visible after submission.");
    }

    /* Test 4: Verify reset button clears all input fields */
    @Test
    @Order(4)
    public void testFormReset() {
        navigateToForm();
        // Fill some fields
        List<WebElement> fields = driver.findElements(By.cssSelector("form input:not([type='submit']):not([type='reset']), form textarea, form select"));
        for (WebElement field : fields) {
            if (!field.getAttribute("type").equalsIgnoreCase("submit") &&
                !field.getAttribute("type").equalsIgnoreCase("reset") &&
                !field.getAttribute("type").equalsIgnoreCase("radio") &&
                field.isEnabled()) {
                field.clear();
                field.sendKeys("Test");
            }
        }

        // Click reset
        By resetLocator = By.cssSelector("input[type='reset'], button[type='reset']");
        WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(resetLocator));
        resetBtn.click();

        // Verify all fields are cleared
        for (WebElement field : fields) {
            String value = field.getAttribute("value");
            Assertions.assertTrue(value == null || value.isEmpty(),
                    "Field should be cleared after reset: " + field.getAttribute("outerHTML"));
        }
    }

    /* Test 5: Verify external links open a new window and contain expected domain */
    @Test
    @Order(5)
    public void testExternalLinks() {
        navigateToForm();

        // Find all links that start with http
        List<WebElement> links = driver.findElements(By.xpath("//a[starts-with(@href,'http')]"));
        Assertions.assertTrue(!links.isEmpty(), "There should be at least one external link.");

        String baseHost = "katalon-test.s3.amazonaws.com";
        String originalWindow = driver.getWindowHandle();

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            if (href.contains(baseHost)) {
                // Skip internal links
                continue;
            }

            // Click external link
            link.click();

            // Wait for new window
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Set<String> handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    // Assert the URL contains the domain part of href
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                            "External link URL should contain the expected domain: " + href);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}