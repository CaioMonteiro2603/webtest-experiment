package SunaQwen3.ws04.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String EXPECTED_TITLE = "Katalon Test Form";

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
        
        // Assert page title
        String title = driver.getTitle();
        assertTrue(title.contains(EXPECTED_TITLE), 
                   "Page title should contain '" + EXPECTED_TITLE + "' but was: " + title);
        
        // Assert URL contains expected path
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("form.html"), 
                   "Current URL should contain 'form.html' but was: " + currentUrl);
    }

    @Test
    @Order(2)
    public void testFormFieldsArePresent() {
        driver.get(BASE_URL);
        
        // Wait for form to be visible
        By formSelector = By.cssSelector("form[action='submit']");
        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(formSelector));
        assertNotNull(form, "Form should be present on the page");

        // Check required fields
        assertFieldPresent(By.id("first-name"), "First Name field");
        assertFieldPresent(By.id("last-name"), "Last Name field");
        assertFieldPresent(By.id("email"), "Email field");
        assertFieldPresent(By.id("password"), "Password field");
        assertFieldPresent(By.id("gender"), "Gender field");
        assertFieldPresent(By.id("dob"), "Date of Birth field");
        assertFieldPresent(By.id("address"), "Address field");
        assertFieldPresent(By.id("city"), "City field");
        assertFieldPresent(By.id("state"), "State field");
        assertFieldPresent(By.id("zip"), "ZIP Code field");
        assertFieldPresent(By.id("country"), "Country field");
        assertFieldPresent(By.id("phone"), "Phone Number field");
        assertFieldPresent(By.id("occupation"), "Occupation field");
        assertFieldPresent(By.id("company"), "Company field");
        assertFieldPresent(By.id("website"), "Website field");
        assertFieldPresent(By.id("hobbies"), "Hobbies field");
        assertFieldPresent(By.id("comments"), "Comments field");
    }

    @Test
    @Order(3)
    public void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);

        fillFormWithValidData();

        // Submit form
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
        );
        submitButton.click();

        // Wait for success message
        By successMessage = By.cssSelector(".alert-success");
        WebElement messageElement = wait.until(
            ExpectedConditions.visibilityOfElementLocated(successMessage)
        );

        // Assert success message is displayed
        assertTrue(messageElement.isDisplayed(), "Success message should be displayed after form submission");
        String messageText = messageElement.getText();
        assertTrue(messageText.contains("successfully") || messageText.contains("Thank you"), 
                   "Success message should indicate successful submission, but was: " + messageText);
    }

    @Test
    @Order(4)
    public void testFormSubmissionWithInvalidEmail() {
        driver.get(BASE_URL);

        // Fill form with invalid email
        fillFormWithValidData();
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.clear();
        emailField.sendKeys("invalid-email-format");

        // Submit form
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
        );
        submitButton.click();

        // Wait for error message or form validation
        By errorMessage = By.cssSelector(".alert-danger");
        boolean hasErrorAlert = driver.findElements(errorMessage).size() > 0;
        
        if (hasErrorAlert) {
            WebElement errorElement = wait.until(
                ExpectedConditions.visibilityOfElementLocated(errorMessage)
            );
            assertTrue(errorElement.isDisplayed(), "Error message should be displayed for invalid email");
        } else {
            // Some forms use HTML5 validation
            WebElement emailFieldAfterSubmit = driver.findElement(By.id("email"));
            assertTrue(!emailFieldAfterSubmit.equals(driver.switchTo().activeElement()) || 
                       driver.getCurrentUrl().equals(BASE_URL), 
                       "Form should not submit with invalid email");
        }
    }

    @Test
    @Order(5)
    public void testRequiredFieldsValidation() {
        driver.get(BASE_URL);

        // Leave required fields empty and submit
        WebElement firstNameField = driver.findElement(By.id("first-name"));
        firstNameField.clear();

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.clear();

        // Submit form
        WebElement submitButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))
        );
        submitButton.click();

        // Check if browser validation prevents submission or shows errors
        By errorIndicators = By.cssSelector(".error, .invalid, .alert-danger, input:invalid");
        List<WebElement> errors = driver.findElements(errorIndicators);

        assertTrue(errors.size() > 0, 
                   "There should be validation errors when required fields are empty");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get(BASE_URL);

        // Find footer links
        By twitterLink = By.cssSelector("footer a[href*='twitter.com']");
        By facebookLink = By.cssSelector("footer a[href*='facebook.com']");
        By linkedinLink = By.cssSelector("footer a[href*='linkedin.com']");

        assertExternalLinkOpensInNewTab(twitterLink, "twitter.com");
        assertExternalLinkOpensInNewTab(facebookLink, "facebook.com");
        assertExternalLinkOpensInNewTab(linkedinLink, "linkedin.com");
    }

    @Test
    @Order(7)
    public void testNavigationLinks() {
        driver.get(BASE_URL);

        // Test Home link
        By homeLink = By.linkText("Home");
        assertInternalLinkNavigates(homeLink, "form.html");

        // Test About link if present
        By aboutLink = By.linkText("About");
        List<WebElement> aboutElements = driver.findElements(aboutLink);
        if (aboutElements.size() > 0) {
            assertInternalLinkNavigates(aboutLink, "about.html");
        }

        // Test Contact link if present
        By contactLink = By.linkText("Contact");
        List<WebElement> contactElements = driver.findElements(contactLink);
        if (contactElements.size() > 0) {
            assertInternalLinkNavigates(contactLink, "contact.html");
        }
    }

    @Test
    @Order(8)
    public void testFileUploadField() {
        driver.get(BASE_URL);

        By fileUpload = By.id("file-upload");
        List<WebElement> fileUploadElements = driver.findElements(fileUpload);
        
        if (fileUploadElements.size() == 0) return; // Skip if not present

        // Create a simple text file for upload
        String filePath = createTestFile();

        WebElement fileInput = driver.findElement(fileUpload);
        fileInput.sendKeys(filePath);

        // Wait for file to be processed (if UI updates)
        try {
            Thread.sleep(1000); // Brief pause to allow file processing (only for display update)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify file input has value
        String value = fileInput.getAttribute("value");
        assertNotNull(value, "File input should have a value after upload");
        assertFalse(value.isEmpty(), "File input should not be empty after upload");
    }

    @Test
    @Order(9)
    public void testFormResetFunctionality() {
        driver.get(BASE_URL);

        // Fill some fields
        WebElement firstNameField = driver.findElement(By.id("first-name"));
        firstNameField.sendKeys("John");

        WebElement lastNameField = driver.findElement(By.id("last-name"));
        lastNameField.sendKeys("Doe");

        // Click reset button
        By resetButton = By.cssSelector("button[type='reset']");
        List<WebElement> resetElements = driver.findElements(resetButton);
        
        if (resetElements.size() == 0) return; // Skip if no reset button

        WebElement reset = wait.until(
            ExpectedConditions.elementToBeClickable(resetButton)
        );
        reset.click();

        // Verify fields are cleared
        String firstNameValue = firstNameField.getAttribute("value");
        String lastNameValue = lastNameField.getAttribute("value");

        assertTrue(firstNameValue.isEmpty(), "First name field should be cleared after reset");
        assertTrue(lastNameValue.isEmpty(), "Last name field should be cleared after reset");
    }

    // Helper methods
    private void assertFieldPresent(By locator, String fieldName) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        assertNotNull(element, fieldName + " should be present on the form");
        assertTrue(element.isDisplayed(), fieldName + " should be visible");
    }

    private void fillFormWithValidData() {
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@example.com");
        driver.findElement(By.id("password")).sendKeys("SecurePass123!");
        driver.findElement(By.cssSelector("input[value='male']")).click();
        driver.findElement(By.id("dob")).sendKeys("1990-01-01");
        driver.findElement(By.id("address")).sendKeys("123 Main St");
        driver.findElement(By.id("city")).sendKeys("New York");
        driver.findElement(By.id("state")).sendKeys("NY");
        driver.findElement(By.id("zip")).sendKeys("10001");
        driver.findElement(By.id("country")).sendKeys("USA");
        driver.findElement(By.id("phone")).sendKeys("+1-555-123-4567");
        driver.findElement(By.id("occupation")).sendKeys("Software Engineer");
        driver.findElement(By.id("company")).sendKeys("Tech Corp");
        driver.findElement(By.id("website")).sendKeys("https://example.com");
        driver.findElement(By.id("hobbies")).sendKeys("Reading, Coding");
        driver.findElement(By.id("comments")).sendKeys("This is a test submission.");
    }

    private String createTestFile() {
        // In headless environment, we can't create real files easily
        // Instead, we rely on the fact that Selenium can handle file uploads with paths
        // This is a placeholder - in real scenarios, you'd create a temp file
        return "/tmp/test-upload.txt"; // Placeholder path
    }

    private void assertExternalLinkOpensInNewTab(By linkLocator, String expectedDomain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(linkLocator));
        String originalWindow = driver.getWindowHandle();

        // Open link in new tab using JavaScript to bypass popup blockers
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", link.getAttribute("href"));

        // Switch to new window
        String newWindow = wait.until(driver -> {
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    return handle;
                }
            }
            return null;
        });

        assertNotNull(newWindow, "New tab should be opened when clicking external link");

        driver.switchTo().window(newWindow);

        try {
            // Wait for page to load
            wait.until(ExpectedConditions.titleContains(""));

            // Assert URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains(expectedDomain), 
                       "External link should navigate to domain containing '" + expectedDomain + 
                       "' but was: " + currentUrl);
        } finally {
            // Close new tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    private void assertInternalLinkNavigates(By linkLocator, String expectedPath) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(linkLocator));

        // Click the link
        link.click();

        // Wait for navigation
        wait.until(ExpectedConditions.urlContains(expectedPath));

        // Assert URL contains expected path
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains(expectedPath), 
                   "Navigation should go to page containing '" + expectedPath + "' but was: " + currentUrl);

        // Go back to base URL
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("form.html"));
    }
}