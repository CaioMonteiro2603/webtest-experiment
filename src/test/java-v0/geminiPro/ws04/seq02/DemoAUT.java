package geminiPro.ws04.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * A complete JUnit 5 test suite for the Katalon Demo AUT form page.
 * This test uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    // Constants for configuration and locators
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // WebDriver and WebDriverWait instances shared across all tests
    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators for form elements
    private final By firstNameInput = By.id("first-name");
    private final By lastNameInput = By.id("last-name");
    private final By genderMaleRadio = By.xpath("//input[@name='gender' and @value='male']");
    private final By dobInput = By.id("dob");
    private final By addressInput = By.id("address");
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By companyInput = By.id("company");
    private final By roleSelect = By.id("role");
    private final By commentTextarea = By.id("comment");
    private final By submitButton = By.id("submit");

    // --- WebDriver Lifecycle ---

    @BeforeAll
    static void setup() {
        // As per requirements, initialize Firefox in headless mode via arguments
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToForm() {
        // Start each test with a fresh form page
        driver.get(BASE_URL);
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    @DisplayName("Should load the form page successfully and verify key elements")
    void testPageLoadsSuccessfully() {
        Assertions.assertEquals("Demo AUT", driver.getTitle(), "Page title is incorrect.");
        WebElement header = driver.findElement(By.xpath("//h1[text()='Submit form']"));
        Assertions.assertTrue(header.isDisplayed(), "Main header 'Submit form' should be visible.");
        Assertions.assertTrue(driver.findElement(submitButton).isDisplayed(), "Submit button should be visible.");
    }

    @Test
    @Order(2)
    @DisplayName("Should fill and submit the entire form and verify all data in the URL")
    void testFullFormSubmission() throws UnsupportedEncodingException {
        // --- Arrange: Prepare test data ---
        String firstName = "Caio";
        String lastName = "Augusto";
        String dob = "1990-08-24";
        String address = "123 Main St, Porto Ferreira";
        String email = "caio.augusto@geminipro.com";
        String password = "supersecretpassword123";
        String company = "Google";
        String role = "Manager";
        String comment = "This is a test comment with special characters: !@#$%^&*()";

        // --- Act: Fill and submit the form ---
        driver.findElement(firstNameInput).sendKeys(firstName);
        driver.findElement(lastNameInput).sendKeys(lastName);
        driver.findElement(genderMaleRadio).click();
        driver.findElement(dobInput).sendKeys(dob);
        driver.findElement(addressInput).sendKeys(address);
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(companyInput).sendKeys(company);
        new Select(driver.findElement(roleSelect)).selectByVisibleText(role);

        // Select multiple job expectations
        driver.findElement(By.xpath("//label[normalize-space()='High salary']/input")).click();
        driver.findElement(By.xpath("//label[normalize-space()='Challenging']/input")).click();

        driver.findElement(commentTextarea).sendKeys(comment);
        driver.findElement(submitButton).click();

        // --- Assert: Verify the URL parameters after submission ---
        wait.until(ExpectedConditions.urlContains("first-name=" + firstName));
        String currentUrl = driver.getCurrentUrl();
        String decodedUrl = URLDecoder.decode(currentUrl, StandardCharsets.UTF_8.toString());

        Assertions.assertAll("Verify all submitted data is present in the URL",
                () -> Assertions.assertTrue(decodedUrl.contains("first-name=" + firstName), "First name is missing or incorrect."),
                () -> Assertions.assertTrue(decodedUrl.contains("last-name=" + lastName), "Last name is missing or incorrect."),
                () -> Assertions.assertTrue(decodedUrl.contains("gender=male"), "Gender is missing or incorrect."),
                () -> Assertions.assertTrue(decodedUrl.contains("dob=" + dob), "Date of birth is missing or incorrect."),
                () -> Assertions.assertTrue(decodedUrl.contains("address=" + address), "Address is missing or incorrect."),
                () -> Assertions.assertTrue(decodedUrl.contains("email=" + email), "Email is missing or incorrect."),
                () -> Assertions.assertTrue(decodedUrl.contains("password=" + password), "Password is missing or incorrect."),
                () -> Assertions.assertTrue(decodedUrl.contains("company=" + company), "Company is missing or incorrect."),
                () -> Assertions.assertTrue(decodedUrl.contains("role=" + role), "Role is missing or incorrect."),
                () -> Assertions.assertTrue(decodedUrl.contains("expectation=High salary"), "Expectation 'High salary' is missing."),
                () -> Assertions.assertTrue(decodedUrl.contains("expectation=Challenging"), "Expectation 'Challenging' is missing."),
                () -> Assertions.assertTrue(decodedUrl.contains("comment=" + comment), "Comment is missing or incorrect.")
        );
    }

    @Test
    @Order(3)
    @DisplayName("Should correctly submit different selections from the Role dropdown")
    void testRoleDropdownSelection() {
        // Fill required fields
        driver.findElement(firstNameInput).sendKeys("Test");
        driver.findElement(lastNameInput).sendKeys("Role");

        // Test with "QA"
        new Select(driver.findElement(roleSelect)).selectByVisibleText("QA");
        driver.findElement(submitButton).click();
        wait.until(ExpectedConditions.urlContains("role=QA"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("role=QA"), "URL should contain 'role=QA'.");

        // Test with "Developer" (must re-fill fields after page reload)
        driver.findElement(firstNameInput).sendKeys("Test");
        driver.findElement(lastNameInput).sendKeys("Role");
        new Select(driver.findElement(roleSelect)).selectByVisibleText("Developer");
        driver.findElement(submitButton).click();
        wait.until(ExpectedConditions.urlContains("role=Developer"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("role=Developer"), "URL should contain 'role=Developer'.");
    }

    @Test
    @Order(4)
    @DisplayName("Should correctly handle multiple checkbox selections")
    void testMultipleCheckboxes() {
        // Arrange
        driver.findElement(firstNameInput).sendKeys("Test");
        driver.findElement(lastNameInput).sendKeys("Checkbox");

        // Act: Select multiple 'ways of development'
        driver.findElement(By.xpath("//label[normalize-space()='Read books']/input")).click();
        driver.findElement(By.xpath("//label[normalize-space()='Take online courses']/input")).click();
        driver.findElement(By.xpath("//label[normalize-space()='Contribute to open source']/input")).click();
        driver.findElement(submitButton).click();

        // Assert
        wait.until(ExpectedConditions.urlContains("development=Read+books"));
        String currentUrl = driver.getCurrentUrl();

        Assertions.assertAll("Verify multiple checkbox values are in the URL",
                () -> Assertions.assertTrue(currentUrl.contains("development=Read+books"), "Checkbox 'Read books' is missing."),
                () -> Assertions.assertTrue(currentUrl.contains("development=Take+online+courses"), "Checkbox 'Take online courses' is missing."),
                () -> Assertions.assertTrue(currentUrl.contains("development=Contribute+to+open+source"), "Checkbox 'Contribute to open source' is missing.")
        );
    }

    @Test
    @Order(5)
    @DisplayName("Should submit form with minimal data")
    void testMinimalFormSubmission() {
        String firstName = "Minimal";
        String lastName = "Test";

        driver.findElement(firstNameInput).sendKeys(firstName);
        driver.findElement(lastNameInput).sendKeys(lastName);
        driver.findElement(submitButton).click();

        wait.until(ExpectedConditions.urlContains("first-name=" + firstName));
        String currentUrl = driver.getCurrentUrl();

        Assertions.assertTrue(currentUrl.contains("first-name=" + firstName), "First name should be in the URL.");
        Assertions.assertTrue(currentUrl.contains("last-name=" + lastName), "Last name should be in the URL.");
        Assertions.assertFalse(currentUrl.contains("gender="), "Fields not filled should not appear in the URL.");
    }
}