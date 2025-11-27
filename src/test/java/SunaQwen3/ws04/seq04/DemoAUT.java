package SunaQwen3.ws04.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class WebUITestSuite {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String USERNAME = "katalon";
    private static final String PASSWORD = "katalon";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        assertTrue(title.contains("Form"), "Page title should contain 'Form'");
        assertTrue(driver.getCurrentUrl().contains("form.html"), "URL should contain form.html");
    }

    @Test
    @Order(2)
    void testFormFieldsArePresent() {
        driver.get(BASE_URL);
        
        // Wait for form to be present
        By formSelector = By.cssSelector("form[action='submit']");
        wait.until(ExpectedConditions.presenceOfElementLocated(formSelector));
        
        // Check required fields
        By firstNameField = By.name("first-name");
        By lastNameField = By.name("last-name");
        By genderRadio = By.cssSelector("input[type='radio'][name='gender']");
        By dobField = By.name("dob");
        By addressField = By.name("address");
        By emailField = By.name("email");
        By passwordField = By.name("password");
        By companyField = By.name("company");
        By roleField = By.name("role");
        By acceptCheckbox = By.cssSelector("input[type='checkbox'][name='accept']");
        By submitButton = By.cssSelector("button[type='submit']");

        assertTrue(driver.findElements(firstNameField).size() > 0, "First name field should be present");
        assertTrue(driver.findElements(lastNameField).size() > 0, "Last name field should be present");
        assertTrue(driver.findElements(genderRadio).size() > 0, "Gender radio buttons should be present");
        assertTrue(driver.findElements(dobField).size() > 0, "Date of birth field should be present");
        assertTrue(driver.findElements(addressField).size() > 0, "Address field should be present");
        assertTrue(driver.findElements(emailField).size() > 0, "Email field should be present");
        assertTrue(driver.findElements(passwordField).size() > 0, "Password field should be present");
        assertTrue(driver.findElements(companyField).size() > 0, "Company field should be present");
        assertTrue(driver.findElements(roleField).size() > 0, "Role dropdown should be present");
        assertTrue(driver.findElements(acceptCheckbox).size() > 0, "Accept terms checkbox should be present");
        assertTrue(driver.findElements(submitButton).size() > 0, "Submit button should be present");
    }

    @Test
    @Order(3)
    void testFormSubmissionWithValidData() {
        driver.get(BASE_URL);
        
        By firstNameField = By.name("first-name");
        By lastNameField = By.name("last-name");
        By genderMaleRadio = By.cssSelector("input[type='radio'][name='gender'][value='male']");
        By dobField = By.name("dob");
        By addressField = By.name("address");
        By emailField = By.name("email");
        By passwordField = By.name("password");
        By companyField = By.name("company");
        By roleField = By.name("role");
        By acceptCheckbox = By.cssSelector("input[type='checkbox'][name='accept']");
        By submitButton = By.cssSelector("button[type='submit']");
        By successMessage = By.cssSelector(".alert-success");

        // Fill form
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(firstNameField));
        firstName.sendKeys("John");

        WebElement lastName = driver.findElement(lastNameField);
        lastName.sendKeys("Doe");

        WebElement genderRadio = driver.findElement(genderMaleRadio);
        if (!genderRadio.isSelected()) {
            genderRadio.click();
        }

        WebElement dob = driver.findElement(dobField);
        dob.sendKeys("1990-01-01");

        WebElement address = driver.findElement(addressField);
        address.sendKeys("123 Main St");

        WebElement email = driver.findElement(emailField);
        email.sendKeys("john.doe@example.com");

        WebElement password = driver.findElement(passwordField);
        password.sendKeys("SecurePass123!");

        WebElement company = driver.findElement(companyField);
        company.sendKeys("Test Company");

        WebElement role = driver.findElement(roleField);
        role.sendKeys("QA Engineer");

        WebElement accept = driver.findElement(acceptCheckbox);
        if (!accept.isSelected()) {
            accept.click();
        }

        WebElement submit = driver.findElement(submitButton);
        submit.click();

        // Wait for success message
        wait.until(ExpectedConditions.presenceOfElementLocated(successMessage));
        WebElement message = driver.findElement(successMessage);
        assertTrue(message.getText().contains("successfully"), "Success message should confirm submission");
    }

    @Test
    @Order(4)
    void testFormSubmissionWithMissingRequiredFields() {
        driver.get(BASE_URL);
        
        By submitButton = By.cssSelector("button[type='submit']");
        By errorMessages = By.cssSelector(".invalid-feedback");

        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        submit.click();

        // Wait for error messages to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(errorMessages));
        List<WebElement> errors = driver.findElements(errorMessages);
        assertTrue(errors.size() >= 2, "There should be at least 2 validation errors for required fields");
    }

    @Test
    @Order(5)
    void testEmailFieldValidation() {
        driver.get(BASE_URL);
        
        By emailField = By.name("email");
        By submitButton = By.cssSelector("button[type='submit']");
        By errorMessages = By.cssSelector(".invalid-feedback");

        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(emailField));
        email.sendKeys("invalid-email");
        
        WebElement submit = driver.findElement(submitButton);
        submit.click();

        // Wait for validation
        wait.until(ExpectedConditions.presenceOfElementLocated(errorMessages));
        List<WebElement> errors = driver.findElements(errorMessages);
        boolean hasEmailError = false;
        for (WebElement error : errors) {
            if (error.getText().toLowerCase().contains("email")) {
                hasEmailError = true;
                break;
            }
        }
        assertTrue(hasEmailError, "Should show email validation error for invalid format");
    }

    @Test
    @Order(6)
    void testPasswordStrengthValidation() {
        driver.get(BASE_URL);
        
        By passwordField = By.name("password");
        By submitButton = By.cssSelector("button[type='submit']");
        By errorMessages = By.cssSelector(".invalid-feedback");

        WebElement password = wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        password.sendKeys("weak");
        
        WebElement submit = driver.findElement(submitButton);
        submit.click();

        // Wait for validation
        wait.until(ExpectedConditions.presenceOfElementLocated(errorMessages));
        List<WebElement> errors = driver.findElements(errorMessages);
        boolean hasPasswordError = false;
        for (WebElement error : errors) {
            if (error.getText().toLowerCase().contains("password")) {
                hasPasswordError = true;
                break;
            }
        }
        assertTrue(hasPasswordError, "Should show password strength requirement error");
    }

    @Test
    @Order(7)
    void testTermsAndConditionsCheckboxValidation() {
        driver.get(BASE_URL);
        
        By acceptCheckbox = By.cssSelector("input[type='checkbox'][name='accept']");
        By submitButton = By.cssSelector("button[type='submit']");
        By errorMessages = By.cssSelector(".invalid-feedback");

        // Do not check the checkbox
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        submit.click();

        // Wait for validation
        wait.until(ExpectedConditions.presenceOfElementLocated(errorMessages));
        List<WebElement> errors = driver.findElements(errorMessages);
        boolean hasAcceptError = false;
        for (WebElement error : errors) {
            if (error.getText().toLowerCase().contains("accept")) {
                hasAcceptError = true;
                break;
            }
        }
        assertTrue(hasAcceptError, "Should show error for not accepting terms and conditions");
    }

    @Test
    @Order(8)
    void testExternalLinksInFooter() {
        driver.get(BASE_URL);
        
        By twitterLink = By.cssSelector("footer a[href*='twitter.com']");
        By facebookLink = By.cssSelector("footer a[href*='facebook.com']");
        By linkedinLink = By.cssSelector("footer a[href*='linkedin.com']");

        // Store original window handle
        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        String twitterHref = twitter.getAttribute("href");
        assertTrue(twitterHref.contains("twitter.com"), "Twitter link should point to twitter.com");
        twitter.click();

        // Switch to new tab
        String newWindow = wait.until(d -> {
            for (String handle : d.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    return handle;
                }
            }
            return null;
        });
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "New tab should be Twitter domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(facebookLink));
        String facebookHref = facebook.getAttribute("href");
        assertTrue(facebookHref.contains("facebook.com"), "Facebook link should point to facebook.com");
        facebook.click();

        newWindow = wait.until(d -> {
            for (String handle : d.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    return handle;
                }
            }
            return null;
        });
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "New tab should be Facebook domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(linkedinLink));
        String linkedinHref = linkedin.getAttribute("href");
        assertTrue(linkedinHref.contains("linkedin.com"), "LinkedIn link should point to linkedin.com");
        linkedin.click();

        newWindow = wait.until(d -> {
            for (String handle : d.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    return handle;
                }
            }
            return null;
        });
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "New tab should be LinkedIn domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}