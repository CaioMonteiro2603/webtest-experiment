package geminiPro.ws04.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
 * A JUnit 5 test suite for a sample Katalon HTML form.
 * This suite uses Selenium WebDriver with Firefox in headless mode to fill out and submit the form,
 * then validates the submission by inspecting the resulting URL query parameters.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    // --- Locators ---
    private static final By FIRST_NAME_INPUT = By.id("first-name");
    private static final By LAST_NAME_INPUT = By.id("last-name");
    // Gender Radio Buttons will be located by value
    private static final By DOB_INPUT = By.id("dob");
    private static final By ADDRESS_INPUT = By.id("address");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By COMPANY_INPUT = By.id("company");
    private static final By ROLE_DROPDOWN = By.id("role");
    // Job Expectation Checkboxes will be located by value
    private static final By COMMENT_TEXTAREA = By.id("comment");
    private static final By SUBMIT_BUTTON = By.id("submit");
    
    // --- Test Data ---
    private static final String FIRST_NAME = "Caio";
    private static final String LAST_NAME = "Gemini";
    private static final String GENDER = "Male";
    private static final String DOB = "09/06/2025";
    private static final String ADDRESS = "123 Test Street, Porto Ferreira";
    private static final String EMAIL = "caio.gemini@example.com";
    private static final String PASSWORD = "secretPassword123";
    private static final String COMPANY = "Google";
    private static final String ROLE = "Manager";
    private static final String COMMENT = "This is a test comment.";


    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments as strictly required
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void loadPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(SUBMIT_BUTTON));
    }

    @Test
    @Order(1)
    @DisplayName("🧪 Test Full Form Submission with All Fields Populated")
    void testFullFormSubmission() throws UnsupportedEncodingException {
        // Fill text inputs
        driver.findElement(FIRST_NAME_INPUT).sendKeys(FIRST_NAME);
        driver.findElement(LAST_NAME_INPUT).sendKeys(LAST_NAME);
        driver.findElement(DOB_INPUT).sendKeys(DOB);
        driver.findElement(ADDRESS_INPUT).sendKeys(ADDRESS);
        driver.findElement(EMAIL_INPUT).sendKeys(EMAIL);
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(COMPANY_INPUT).sendKeys(COMPANY);
        driver.findElement(COMMENT_TEXTAREA).sendKeys(COMMENT);

        // Select gender (radio button)
        driver.findElement(By.xpath("//input[@name='gender' and @value='" + GENDER + "']")).click();
        
        // Select role from dropdown
        Select roleSelect = new Select(driver.findElement(ROLE_DROPDOWN));
        roleSelect.selectByVisibleText(ROLE);

        // Select job expectations (checkboxes)
        driver.findElement(By.xpath("//input[@name='expectation' and @value='High salary']")).click();
        driver.findElement(By.xpath("//input[@name='expectation' and @value='Challenging']")).click();
        
        // Select ways of development (checkboxes)
        driver.findElement(By.xpath("//input[@name='development' and @value='Read books']")).click();

        // Submit the form
        driver.findElement(SUBMIT_BUTTON).click();

        // Wait for the page to "reload" with query parameters
        wait.until(ExpectedConditions.urlContains("first-name=" + FIRST_NAME));

        String currentUrl = driver.getCurrentUrl();
        String decodedUrl = URLDecoder.decode(currentUrl, StandardCharsets.UTF_8.name());

        // Assert that all submitted values are present in the URL
        Assertions.assertAll("Verify all form data in URL",
            () -> Assertions.assertTrue(decodedUrl.contains("first-name=" + FIRST_NAME), "First name mismatch."),
            () -> Assertions.assertTrue(decodedUrl.contains("last-name=" + LAST_NAME), "Last name mismatch."),
            () -> Assertions.assertTrue(decodedUrl.contains("gender=" + GENDER), "Gender mismatch."),
            () -> Assertions.assertTrue(decodedUrl.contains("dob=" + DOB), "Date of Birth mismatch."),
            () -> Assertions.assertTrue(decodedUrl.contains("address=" + ADDRESS), "Address mismatch."),
            () -> Assertions.assertTrue(decodedUrl.contains("email=" + EMAIL), "Email mismatch."),
            () -> Assertions.assertTrue(decodedUrl.contains("password=" + PASSWORD), "Password mismatch."),
            () -> Assertions.assertTrue(decodedUrl.contains("company=" + COMPANY), "Company mismatch."),
            () -> Assertions.assertTrue(decodedUrl.contains("role=" + ROLE), "Role mismatch."),
            () -> Assertions.assertTrue(decodedUrl.contains("expectation=High salary"), "Expectation 'High salary' missing."),
            () -> Assertions.assertTrue(decodedUrl.contains("expectation=Challenging"), "Expectation 'Challenging' missing."),
            () -> Assertions.assertTrue(decodedUrl.contains("development=Read books"), "Development 'Read books' missing."),
            () -> Assertions.assertTrue(decodedUrl.contains("comment=" + COMMENT), "Comment mismatch.")
        );
    }

    @Test
    @Order(2)
    @DisplayName("🧪 Test Minimal Form Submission with Only Required Fields")
    void testMinimalFormSubmission() throws UnsupportedEncodingException {
        // Fill only a subset of fields
        driver.findElement(FIRST_NAME_INPUT).sendKeys(FIRST_NAME);
        driver.findElement(LAST_NAME_INPUT).sendKeys(LAST_NAME);
        driver.findElement(By.xpath("//input[@name='gender' and @value='Female']")).click();
        driver.findElement(EMAIL_INPUT).sendKeys(EMAIL);
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);

        // Submit the form
        driver.findElement(SUBMIT_BUTTON).click();

        // Wait for submission to complete
        wait.until(ExpectedConditions.urlContains("first-name=" + FIRST_NAME));

        String currentUrl = driver.getCurrentUrl();
        String decodedUrl = URLDecoder.decode(currentUrl, StandardCharsets.UTF_8.name());
        
        // Assert that the filled fields are in the URL
        Assertions.assertTrue(decodedUrl.contains("first-name=" + FIRST_NAME), "First name should be in URL.");
        Assertions.assertTrue(decodedUrl.contains("last-name=" + LAST_NAME), "Last name should be in URL.");
        Assertions.assertTrue(decodedUrl.contains("gender=Female"), "Gender should be in URL.");
        Assertions.assertTrue(decodedUrl.contains("email=" + EMAIL), "Email should be in URL.");
        Assertions.assertTrue(decodedUrl.contains("password=" + PASSWORD), "Password should be in URL.");

        // Assert that an unfilled field is NOT in the URL
        Assertions.assertFalse(decodedUrl.contains("company="), "Empty field 'company' should not be in URL.");
        Assertions.assertFalse(decodedUrl.contains("comment="), "Empty field 'comment' should not be in URL.");
    }
}