package geminiPRO.ws04.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
 * JUnit 5 test suite for a sample Katalon form page using Selenium WebDriver with headless Firefox.
 * This suite focuses on filling and submitting the form with various input types and asserting
 * the correctness of the submitted data in the resulting URL.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KatalonFormE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    // --- Locators for Form Elements ---
    private final By firstNameInput = By.id("first-name");
    private final By lastNameInput = By.id("last-name");
    private final By genderFemaleRadio = By.xpath("//input[@name='gender' and @value='Female']");
    private final By genderMaleRadio = By.xpath("//input[@name='gender' and @value='Male']");
    private final By dobInput = By.id("dob");
    private final By addressTextarea = By.id("address");
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By companyInput = By.id("company");
    private final By roleSelect = By.id("role");
    private final By expectationCheckboxChallenging = By.xpath("//label[normalize-space()='Challenging']//input");
    private final By expectationCheckboxTeamwork = By.xpath("//label[normalize-space()='Good teamwork']//input");
    private final By developmentCheckboxReadBooks = By.xpath("//label[normalize-space()='Read books']//input");
    private final By commentTextarea = By.id("comment");
    private final By submitButton = By.id("submit");
    private final By mainHeader = By.tagName("h1");

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setup() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    @DisplayName("Should load the page and display the correct title and header")
    void testPageTitleAndHeader() {
        String expectedTitle = "Sample Web Form";
        String expectedHeader = "HTML Form Example";

        Assertions.assertEquals(expectedTitle, driver.getTitle(), "Page title is incorrect.");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(mainHeader));
        Assertions.assertEquals(expectedHeader, header.getText(), "Main page header is incorrect.");
    }

    @Test
    @Order(2)
    @DisplayName("Should successfully submit the form with a complete set of data")
    void testSuccessfulFormSubmission() throws UnsupportedEncodingException {
        // --- Fill Form ---
        driver.findElement(firstNameInput).sendKeys("Jane");
        driver.findElement(lastNameInput).sendKeys("Doe");
        driver.findElement(genderFemaleRadio).click();
        driver.findElement(dobInput).sendKeys("05/22/1990");
        driver.findElement(addressTextarea).sendKeys("123 Main Street");
        driver.findElement(emailInput).sendKeys("jane.doe@example.com");
        driver.findElement(passwordInput).sendKeys("s3cureP@ssw0rd");
        driver.findElement(companyInput).sendKeys("Example Corp");
        new Select(driver.findElement(roleSelect)).selectByVisibleText("Quality Assurance");
        driver.findElement(expectationCheckboxChallenging).click();
        driver.findElement(developmentCheckboxReadBooks).click();
        driver.findElement(commentTextarea).sendKeys("This is a test comment.");

        // --- Submit Form ---
        driver.findElement(submitButton).click();
        wait.until(ExpectedConditions.urlContains("?first-name=Jane"));

        // --- Assert URL Parameters ---
        String submittedUrl = URLDecoder.decode(driver.getCurrentUrl(), StandardCharsets.UTF_8.name());

        Assertions.assertAll("Verify form data in submitted URL",
            () -> Assertions.assertTrue(submittedUrl.contains("first-name=Jane"), "First name is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("last-name=Doe"), "Last name is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("gender=Female"), "Gender is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("dob=1990-05-22"), "Date of birth is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("address=123 Main Street"), "Address is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("email=jane.doe@example.com"), "Email is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("password=s3cureP@ssw0rd"), "Password is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("company=Example Corp"), "Company is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("role=QA"), "Role is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("expectation=Challenging"), "Job expectation is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("development=Read+books"), "Ways of development is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("comment=This is a test comment."), "Comment is missing or incorrect.")
        );
    }
    
    @Test
    @Order(3)
    @DisplayName("Should correctly submit the form with a different set of data")
    void testDifferentDataSubmission() throws UnsupportedEncodingException {
        // --- Fill Form with Different Data ---
        driver.findElement(firstNameInput).sendKeys("John");
        driver.findElement(lastNameInput).sendKeys("Smith");
        driver.findElement(genderMaleRadio).click();
        driver.findElement(dobInput).sendKeys("11/15/1985");
        driver.findElement(addressTextarea).sendKeys("456 Oak Avenue, Apt 7B");
        driver.findElement(emailInput).sendKeys("john.smith@test.net");
        driver.findElement(passwordInput).sendKeys("anotherPass123!");
        driver.findElement(companyInput).sendKeys("Another Company Inc.");
        new Select(driver.findElement(roleSelect)).selectByVisibleText("Manager");
        driver.findElement(expectationCheckboxTeamwork).click();
        driver.findElement(commentTextarea).sendKeys("A different comment for this submission.");

        // --- Submit Form ---
        driver.findElement(submitButton).click();
        wait.until(ExpectedConditions.urlContains("?first-name=John"));

        // --- Assert URL Parameters ---
        String submittedUrl = URLDecoder.decode(driver.getCurrentUrl(), StandardCharsets.UTF_8.name());

        Assertions.assertAll("Verify second set of form data in submitted URL",
            () -> Assertions.assertTrue(submittedUrl.contains("first-name=John"), "First name is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("last-name=Smith"), "Last name is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("gender=Male"), "Gender is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("dob=1985-11-15"), "Date of birth is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("address=456 Oak Avenue, Apt 7B"), "Address is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("role=Manager"), "Role is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("expectation=Good teamwork"), "Job expectation is missing or incorrect."),
            () -> Assertions.assertTrue(submittedUrl.contains("comment=A different comment for this submission."), "Comment is missing or incorrect.")
        );
    }
}