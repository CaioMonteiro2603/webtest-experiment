package SunaQwen3.ws04.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

@TestMethodOrder(OrderAnnotation.class)
public class FormPageTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String EXPECTED_TITLE = "Student Registration Form";

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
    public void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertEquals(EXPECTED_TITLE, title, "Page title should match expected value");
        Assertions.assertTrue(driver.getCurrentUrl().contains("form.html"), "URL should contain form.html");
    }

    @Test
    @Order(2)
    public void testFormElementsArePresent() {
        driver.get(BASE_URL);

        // Wait for form to be visible
        wait.until(visibilityOfElementLocated(By.tagName("form")));

        // Verify key form elements are present
        Assertions.assertTrue(isElementPresent(By.id("first-name")), "First name field should be present");
        Assertions.assertTrue(isElementPresent(By.id("last-name")), "Last name field should be present");
        Assertions.assertTrue(isElementPresent(By.id("email")), "Email field should be present");
        Assertions.assertTrue(isElementPresent(By.id("password")), "Password field should be present");
        Assertions.assertTrue(isElementPresent(By.id("gender")), "Gender field should be present");
        Assertions.assertTrue(isElementPresent(By.id("hobby")), "Hobby field should be present");
        Assertions.assertTrue(isElementPresent(By.id("subject")), "Subject field should be present");
        Assertions.assertTrue(isElementPresent(By.id("profession")), "Profession field should be present");
        Assertions.assertTrue(isElementPresent(By.id("image")), "Image upload field should be present");
        Assertions.assertTrue(isElementPresent(By.id("address")), "Address field should be present");
        Assertions.assertTrue(isElementPresent(By.id("state")), "State dropdown should be present");
        Assertions.assertTrue(isElementPresent(By.id("submit")), "Submit button should be present");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);
        wait.until(visibilityOfElementLocated(By.tagName("form")));

        // Fill out form with valid data
        WebElement firstName = wait.until(elementToBeClickable(By.id("first-name")));
        firstName.sendKeys("John");

        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Doe");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("john.doe@example.com");

        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("SecurePass123!");

        // Select gender
        WebElement gender = driver.findElement(By.cssSelector("input[value='male']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", gender);
        wait.until(elementToBeClickable(gender));
        gender.click();

        // Select hobby
        WebElement hobby = driver.findElement(By.cssSelector("input[value='reading']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", hobby);
        wait.until(elementToBeClickable(hobby));
        hobby.click();

        // Select subject
        WebElement subject = driver.findElement(By.cssSelector("input[value='computer science']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", subject);
        wait.until(elementToBeClickable(subject));
        subject.click();

        // Select profession
        WebElement profession = driver.findElement(By.cssSelector("input[value='student']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", profession);
        wait.until(elementToBeClickable(profession));
        profession.click();

        // Select state
        WebElement state = driver.findElement(By.id("state"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", state);
        wait.until(elementToBeClickable(state));
        state.click();
        state.sendKeys("NCR");
        state.sendKeys(Keys.RETURN);

        // Fill address
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("123 Main Street, New Delhi");

        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
        wait.until(elementToBeClickable(submitButton));
        submitButton.click();

        // Wait for success message
        wait.until(visibilityOfElementLocated(By.className("alert-success")));
        WebElement successMessage = driver.findElement(By.className("alert-success"));
        Assertions.assertTrue(successMessage.isDisplayed(), "Success message should be displayed after form submission");
        Assertions.assertTrue(successMessage.getText().contains("successfully"), "Success message should confirm successful submission");
    }

    @Test
    @Order(4)
    public void testFormSubmissionWithInvalidEmail() {
        driver.get(BASE_URL);
        wait.until(visibilityOfElementLocated(By.tagName("form")));

        // Fill out form with invalid email
        WebElement firstName = wait.until(elementToBeClickable(By.id("first-name")));
        firstName.sendKeys("Jane");

        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Smith");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("invalid-email");

        WebElement password = driver.findElement(By.id("password"));
        password.sendKeys("SecurePass123!");

        // Select gender
        WebElement gender = driver.findElement(By.cssSelector("input[value='female']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", gender);
        wait.until(elementToBeClickable(gender));
        gender.click();

        // Select hobby
        WebElement hobby = driver.findElement(By.cssSelector("input[value='music']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", hobby);
        wait.until(elementToBeClickable(hobby));
        hobby.click();

        // Select subject
        WebElement subject = driver.findElement(By.cssSelector("input[value='arts']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", subject);
        wait.until(elementToBeClickable(subject));
        subject.click();

        // Select profession
        WebElement profession = driver.findElement(By.cssSelector("input[value='teacher']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", profession);
        wait.until(elementToBeClickable(profession));
        profession.click();

        // Select state
        WebElement state = driver.findElement(By.id("state"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", state);
        wait.until(elementToBeClickable(state));
        state.click();
        state.sendKeys("Uttar Pradesh");
        state.sendKeys(Keys.RETURN);

        // Fill address
        WebElement address = driver.findElement(By.id("address"));
        address.sendKeys("456 Oak Avenue, Lucknow");

        // Submit form
        WebElement submitButton = driver.findElement(By.id("submit"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);
        wait.until(elementToBeClickable(submitButton));
        submitButton.click();

        // Check for HTML5 validation (form should not submit)
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "URL should remain the same on invalid submission");
    }

    @Test
    @Order(5)
    public void testRequiredFieldsValidation() {
        driver.get(BASE_URL);
        wait.until(visibilityOfElementLocated(By.tagName("form")));

        // Try to submit empty form
        WebElement submitButton = wait.until(elementToBeClickable(By.id("submit")));
        submitButton.click();

        // Check that required fields show validation
        WebElement firstNameField = driver.findElement(By.id("first-name"));
        Assertions.assertTrue(
            "true".equals(firstNameField.getAttribute("required")) ||
            !firstNameField.getAttribute("validationMessage").isEmpty(),
            "First name field should have required validation"
        );

        // Verify URL hasn't changed (form not submitted)
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Form should not submit with empty required fields");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get(BASE_URL);
        wait.until(visibilityOfElementLocated(By.tagName("footer")));

        // Find all footer links
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));

        // Store original window handle
        String originalWindow = driver.getWindowHandle();
        Set<String> existingWindows = driver.getWindowHandles();

        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;

            // Open link in new tab using JavaScript
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);

            // Wait for new window to appear
            wait.until(numberOfWindowsToBe(existingWindows.size() + 1));

            // Switch to new window
            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(existingWindows);
            String newWindow = newWindows.iterator().next();
            driver.switchTo().window(newWindow);

            // Verify URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            if (href.contains("facebook.com")) {
                Assertions.assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open correct domain");
            } else if (href.contains("twitter.com")) {
                Assertions.assertTrue(currentUrl.contains("twitter.com"), "Twitter link should open correct domain");
            } else if (href.contains("linkedin.com")) {
                Assertions.assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open correct domain");
            } else if (href.contains("instagram.com")) {
                Assertions.assertTrue(currentUrl.contains("instagram.com"), "Instagram link should open correct domain");
            }

            // Close current window and switch back
            driver.close();
            driver.switchTo().window(originalWindow);

            // Update existing windows set
            existingWindows = driver.getWindowHandles();
        }
    }

    @Test
    @Order(7)
    public void testImageUploadField() {
        driver.get(BASE_URL);
        wait.until(visibilityOfElementLocated(By.tagName("form")));

        WebElement imageInput = wait.until(elementToBeClickable(By.id("image")));
        Assertions.assertEquals("file", imageInput.getAttribute("type"), "Image input should be of type file");

        // Note: We can't test actual file upload in headless mode reliably
        // But we can verify the element is present and functional
        Assertions.assertTrue(imageInput.isDisplayed(), "Image upload field should be displayed");
    }

    @Test
    @Order(8)
    public void testFormResetFunctionality() {
        driver.get(BASE_URL);
        wait.until(visibilityOfElementLocated(By.tagName("form")));

        // Fill some fields
        WebElement firstName = wait.until(elementToBeClickable(By.id("first-name")));
        firstName.sendKeys("Test");

        WebElement email = driver.findElement(By.id("email"));
        email.sendKeys("test@example.com");

        // Find and click reset button (assuming there's a reset button)
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        WebElement resetButton = null;
        for (WebElement button : buttons) {
            if ("reset".equalsIgnoreCase(button.getAttribute("type"))) {
                resetButton = button;
                break;
            }
        }

        if (resetButton != null) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", resetButton);
            wait.until(elementToBeClickable(resetButton));
            resetButton.click();

            // Verify fields are cleared
            Assertions.assertEquals("", firstName.getAttribute("value"), "First name field should be cleared after reset");
            Assertions.assertEquals("", email.getAttribute("value"), "Email field should be cleared after reset");
        }
    }

    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}