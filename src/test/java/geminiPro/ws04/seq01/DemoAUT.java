package geminiPro.ws04.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * A JUnit 5 test suite for a static web form using Selenium WebDriver with Firefox in headless mode.
 * This suite verifies the form's initial state, fills out all fields, submits the form,
 * and validates the success message and data submission.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KatalonFormTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    // --- Locators ---
    private static final By FIRST_NAME_INPUT = By.id("firstName");
    private static final By LAST_NAME_INPUT = By.id("lastName");
    private static final By GENDER_MALE_RADIO = By.xpath("//label[normalize-space()='Male']/input[@name='gender']");
    private static final By DATE_OF_BIRTH_INPUT = By.id("dob");
    private static final By ADDRESS_INPUT = By.id("address");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By COMPANY_INPUT = By.id("company");
    private static final By ROLE_DROPDOWN = By.id("role");
    private static final By EXPECTATION_DROPDOWN = By.id("expectation");
    private static final By DEVELOPMENT_BOOKS_CHECKBOX = By.xpath("//label[normalize-space()='Read books']/input");
    private static final By DEVELOPMENT_MEDIA_CHECKBOX = By.xpath("//label[normalize-space()='Watch online courses']/input");
    private static final By COMMENT_TEXTAREA = By.id("comment");
    private static final By SUBMIT_BUTTON = By.id("submit");
    private static final By SUCCESS_MESSAGE = By.id("submit-msg");
    private static final By PAGE_HEADER = By.tagName("h1");


    @BeforeAll
    static void setup() {
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
    @DisplayName("Should load the page and verify its initial state")
    void testPageInitialState() {
        driver.get(BASE_URL);
        
        // 1. Verify page title and header
        Assertions.assertEquals("Form", driver.getTitle(), "The page title is incorrect.");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_HEADER));
        Assertions.assertEquals("Job Application", header.getText(), "The main page header is incorrect.");

        // 2. Verify a few key form fields are initially empty or unselected
        Assertions.assertEquals("", driver.findElement(FIRST_NAME_INPUT).getAttribute("value"), "First Name field should be empty.");
        Assertions.assertEquals("", driver.findElement(ADDRESS_INPUT).getAttribute("value"), "Address field should be empty.");
        Assertions.assertFalse(driver.findElement(GENDER_MALE_RADIO).isSelected(), "Male gender radio button should not be selected by default.");
        
        Select roleSelect = new Select(driver.findElement(ROLE_DROPDOWN));
        Assertions.assertEquals("Manager", roleSelect.getFirstSelectedOption().getText(), "Default role should be 'Manager'.");
    }

    @Test
    @Order(2)
    @DisplayName("Should fill out all form fields and submit")
    void testFullFormSubmission() {
        // Navigate again to ensure a clean slate, though the ordered execution implies it
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME_INPUT));

        // Fill text-based inputs
        driver.findElement(FIRST_NAME_INPUT).sendKeys("John");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Doe");
        driver.findElement(DATE_OF_BIRTH_INPUT).sendKeys("05/30/1990"); // MM/DD/YYYY format
        driver.findElement(ADDRESS_INPUT).sendKeys("123 Maple Street");
        driver.findElement(EMAIL_INPUT).sendKeys("john.doe@example.com");
        driver.findElement(PASSWORD_INPUT).sendKeys("SecureP@ss123");
        driver.findElement(COMPANY_INPUT).sendKeys("Tech Corp");
        
        // Select radio button
        driver.findElement(GENDER_MALE_RADIO).click();

        // Select from dropdowns
        Select roleSelect = new Select(driver.findElement(ROLE_DROPDOWN));
        roleSelect.selectByVisibleText("QA");
        
        Select expectationSelect = new Select(driver.findElement(EXPECTATION_DROPDOWN));
        expectationSelect.selectByVisibleText("High salary");
        
        // Select checkboxes
        driver.findElement(DEVELOPMENT_BOOKS_CHECKBOX).click();
        driver.findElement(DEVELOPMENT_MEDIA_CHECKBOX).click();

        // Fill textarea
        driver.findElement(COMMENT_TEXTAREA).sendKeys("This is an automated test submission.");

        // Submit the form
        driver.findElement(SUBMIT_BUTTON).click();
    }

    @Test
    @Order(3)
    @DisplayName("Should verify the successful submission message and URL parameters")
    void testSubmissionVerification() {
        // This test assumes the form was submitted in the previous test
        
        // 1. Verify the success message is displayed
        WebElement successMsgElement = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertEquals("Successfully submitted!", successMsgElement.getText(), "The success message is incorrect or not found.");

        // 2. Verify the URL contains the submitted data as query parameters
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertAll("Verify URL contains submitted data",
            () -> Assertions.assertTrue(currentUrl.contains("firstName=John"), "URL should contain the first name."),
            () -> Assertions.assertTrue(currentUrl.contains("lastName=Doe"), "URL should contain the last name."),
            () -> Assertions.assertTrue(currentUrl.contains("gender=male"), "URL should contain the gender."),
            () -> Assertions.assertTrue(currentUrl.contains("email=john.doe%40example.com"), "URL should contain the encoded email."),
            () -> Assertions.assertTrue(currentUrl.contains("role=QA"), "URL should contain the selected role."),
            () -> Assertions.assertTrue(currentUrl.contains("comment=This+is+an+automated"), "URL should contain the encoded comment.")
        );
    }
}