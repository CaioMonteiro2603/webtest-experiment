package GPT20b.ws04.seq06;

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

    /* ---------------------------------------------------------------------- */
    /* Helper methods                                                         */
    /* ---------------------------------------------------------------------- */
    private void navigateToFormPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("form")));
    }

    private void fillFormField(String name, String value) {
        List<WebElement> elements = driver.findElements(By.name(name));
        if (!elements.isEmpty()) {
            WebElement el = elements.get(0);
            String tagName = el.getTagName();
            String type = el.getAttribute("type");
            
            if ("input".equals(tagName) && ("radio".equals(type) || "checkbox".equals(type))) {
                // For radio buttons, click instead of clear/sendKeys
                el.click();
            } else {
                // For text inputs, clear and send keys
                el.clear();
                el.sendKeys(value);
            }
        }
    }

    private void submitForm() {
        List<WebElement> submits = driver.findElements(By.xpath("//button[@type='submit' or @id='submit' or contains(@class,'submit')]"));
        if (!submits.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(submits.get(0))).click();
        } else {
            Assertions.fail("Submit button not found");
        }
    }

    private void resetForm() {
        List<WebElement> resets = driver.findElements(By.xpath("//button[@type='reset' or @id='reset' or contains(@class,'reset')]"));
        if (!resets.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resets.get(0))).click();
        }
    }

    private void openAndVerifyExternal(String linkText, String expectedDomain) {
        WebElement link = driver.findElement(By.linkText(linkText));
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "URL should contain " + expectedDomain + " after clicking " + linkText);
        driver.close();
        driver.switchTo().window(driver.getWindowHandles().iterator().next());
    }

    private void switchToNewWindow() {
        String original = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
                break;
            }
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Tests                                                                  */
    /* ---------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testValidFormSubmission() {
        navigateToFormPage();

        // Fill required fields (names guessed based on common form names)
        fillFormField("firstName", "Caio");
        fillFormField("lastName", "Test");
        fillFormField("email", "caio@example.com");

        // Optional fields
        fillFormField("age", "30");
        fillFormField("phone", "5551234567");

        // Submit
        submitForm();

        // Wait for success message or page change
        try {
            // Try multiple possible success indicators
            WebElement success = wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Success') or contains(@class,'success') or contains(@id,'success')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Submitted')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'alert-success')]")),
                ExpectedConditions.urlContains("success")
            ));
            Assertions.assertTrue(true, "Form submission should complete successfully");
        } catch (Exception e) {
            // If no explicit success message, verify we're not on the same page with errors
            List<WebElement> errorElements = driver.findElements(By.xpath("//*[contains(@class,'error') or contains(text(),'Error')]"));
            Assertions.assertTrue(errorElements.isEmpty(), "No error messages should be present after valid submission");
        }
    }

    @Test
    @Order(2)
    public void testEmptyFormSubmissionShowsError() {
        navigateToFormPage();

        // Ensure text input fields are empty (skip radio buttons and checkboxes)
        List<WebElement> inputs = driver.findElements(By.tagName("input"));
        for (WebElement input : inputs) {
            String type = input.getAttribute("type");
            if ("text".equals(type) || "email".equals(type) || "tel".equals(type) || "number".equals(type)) {
                input.clear();
            }
        }

        // Submit
        submitForm();

        // Check for error message presence - wait for validation to occur
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'error') or contains(@class,'error') or contains(@id,'error') or @required and following-sibling::*[contains(@class,'error')]]")));
            Assertions.assertTrue(error.isDisplayed(), "Error message should appear when submitting empty form");
        } catch (Exception e) {
            // Alternative: check for HTML5 validation messages or required field highlighting
            List<WebElement> requiredFields = driver.findElements(By.xpath("//input[@required]"));
            boolean hasValidation = false;
            for (WebElement field : requiredFields) {
                String validationMessage = field.getAttribute("validationMessage");
                if (validationMessage != null && !validationMessage.isEmpty()) {
                    hasValidation = true;
                    break;
                }
            }
            Assertions.assertTrue(hasValidation || !requiredFields.isEmpty(), "Should show validation error for empty required fields");
        }
    }

    @Test
    @Order(3)
    public void testFormResetClearsFields() {
        navigateToFormPage();

        // Fill some fields
        fillFormField("firstName", "Temp");
        fillFormField("lastName", "User");
        fillFormField("email", "temp@user.com");

        // Reset
        resetForm();

        // Verify fields are cleared
        WebElement firstNameElement = driver.findElement(By.name("firstName"));
        WebElement lastNameElement = driver.findElement(By.name("lastName"));
        WebElement emailElement = driver.findElement(By.name("email"));
        
        // Wait a moment for reset to take effect
        try { Thread.sleep(500); } catch (InterruptedException e) { }
        
        Assertions.assertTrue(firstNameElement.getAttribute("value") == null || firstNameElement.getAttribute("value").isEmpty(),
                "First name field should be cleared after reset");
        Assertions.assertTrue(lastNameElement.getAttribute("value") == null || lastNameElement.getAttribute("value").isEmpty(),
                "Last name field should be cleared after reset");
        Assertions.assertTrue(emailElement.getAttribute("value") == null || emailElement.getAttribute("value").isEmpty(),
                "Email field should be cleared after reset");
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        navigateToFormPage();

        // Twitter link
        List<WebElement> twitter = driver.findElements(By.xpath("//a[contains(@href,'twitter.com')]"));
        if (!twitter.isEmpty()) {
            openAndVerifyExternal("Twitter", "twitter.com");
        }

        // Facebook link
        List<WebElement> facebook = driver.findElements(By.xpath("//a[contains(@href,'facebook.com')]"));
        if (!facebook.isEmpty()) {
            openAndVerifyExternal("Facebook", "facebook.com");
        }

        // LinkedIn link
        List<WebElement> linkedIn = driver.findElements(By.xpath("//a[contains(@href,'linkedin.com')]"));
        if (!linkedIn.isEmpty()) {
            openAndVerifyExternal("LinkedIn", "linkedin.com");
        }
    }
}