package geminiPRO.ws04.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/**
 * A complete JUnit 5 test suite for the Katalon Demo AUT Form page using Selenium WebDriver
 * with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KatalonFormE2ETest {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    // --- Selenium WebDriver ---
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private static final By FIRST_NAME_INPUT = By.id("first-name");
    private static final By LAST_NAME_INPUT = By.id("last-name");
    private static final By GENDER_MALE_RADIO = By.xpath("//input[@name='gender' and @value='Male']");
    private static final By DOB_INPUT = By.id("dob");
    private static final By ADDRESS_INPUT = By.id("address");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By COMPANY_INPUT = By.id("company");
    private static final By ROLE_DROPDOWN = By.id("role");
    private static final By JOB_EXPECTATION_SALARY_CHECKBOX = By.xpath("//label[normalize-space()='High salary']/input");
    private static final By COMMENT_TEXTAREA = By.id("comment");
    private static final By SUBMIT_BUTTON = By.id("submit");
    private static final By PAGE_HEADER = By.tagName("h1");

    @BeforeAll
    public static void setup() {
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

    /**
     * Helper method to URL-encode strings for assertion in the URL.
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    @Order(1)
    void testPageLoadAndInitialState() {
        driver.get(BASE_URL);
        
        // Assert page title
        Assertions.assertEquals("Demo AUT", driver.getTitle(), "Page title is incorrect.");

        // Assert main header
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_HEADER));
        Assertions.assertEquals("General Form", header.getText(), "Page header text is incorrect.");

        // Assert key form elements are present
        Assertions.assertTrue(driver.findElement(FIRST_NAME_INPUT).isDisplayed(), "First Name input is not displayed.");
        Assertions.assertTrue(driver.findElement(LAST_NAME_INPUT).isDisplayed(), "Last Name input is not displayed.");
        Assertions.assertTrue(driver.findElement(SUBMIT_BUTTON).isEnabled(), "Submit button should be enabled on page load.");
    }
    
    @Test
    @Order(2)
    void testFormFillAndSubmit() {
        driver.get(BASE_URL);

        // --- Prepare test data ---
        final String firstName = "John";
        final String lastName = "Doe";
        final String dob = "05/20/1990";
        final String address = "123 Main Street";
        final String email = "john.doe@example.com";
        final String password = "SecurePassword123";
        final String company = "Example Corp";
        final String role = "Manager";
        final String comment = "This is a test comment.";
        
        // --- Fill out the form ---
        driver.findElement(FIRST_NAME_INPUT).sendKeys(firstName);
        driver.findElement(LAST_NAME_INPUT).sendKeys(lastName);
        driver.findElement(GENDER_MALE_RADIO).click();
        driver.findElement(DOB_INPUT).sendKeys(dob);
        driver.findElement(ADDRESS_INPUT).sendKeys(address);
        driver.findElement(EMAIL_INPUT).sendKeys(email);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(COMPANY_INPUT).sendKeys(company);

        // Handle dropdown
        Select roleSelect = new Select(driver.findElement(ROLE_DROPDOWN));
        roleSelect.selectByVisibleText(role);
        
        // Handle checkbox
        driver.findElement(JOB_EXPECTATION_SALARY_CHECKBOX).click();
        
        // Handle textarea
        driver.findElement(COMMENT_TEXTAREA).sendKeys(comment);

        // --- Submit the form ---
        driver.findElement(SUBMIT_BUTTON).click();

        // --- Assertions ---
        // The form submits using GET, appending data to the URL.
        // We wait until the URL contains one of the submitted parameters.
        wait.until(ExpectedConditions.urlContains("first-name="));
        
        String currentUrl = driver.getCurrentUrl();
        
        // Verify that the submitted data is present in the final URL as query parameters.
        Assertions.assertAll("Verify form data in URL after submission",
            () -> Assertions.assertTrue(currentUrl.contains("first-name=" + urlEncode(firstName)), "First name missing from URL."),
            () -> Assertions.assertTrue(currentUrl.contains("last-name=" + urlEncode(lastName)), "Last name missing from URL."),
            () -> Assertions.assertTrue(currentUrl.contains("gender=Male"), "Gender missing from URL."),
            () -> Assertions.assertTrue(currentUrl.contains("dob=" + urlEncode(dob)), "Date of Birth missing from URL."),
            () -> Assertions.assertTrue(currentUrl.contains("address=" + urlEncode(address)), "Address missing from URL."),
            () -> Assertions.assertTrue(currentUrl.contains("email=" + urlEncode(email)), "Email missing from URL."),
            () -> Assertions.assertTrue(currentUrl.contains("password=" + urlEncode(password)), "Password missing from URL."),
            () -> Assertions.assertTrue(currentUrl.contains("company=" + urlEncode(company)), "Company missing from URL."),
            () -> Assertions.assertTrue(currentUrl.contains("role=" + urlEncode(role)), "Role missing from URL."),
            () -> Assertions.assertTrue(currentUrl.contains("job_expectation=High-salary"), "Job expectation missing from URL."),
            () -> Assertions.assertTrue(currentUrl.contains("comment=" + urlEncode(comment)), "Comment missing from URL.")
        );
    }
}