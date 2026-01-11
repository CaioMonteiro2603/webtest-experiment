package geminiPro.ws04.seq08;

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

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Form Input Locators
    private final By firstNameInput = By.id("first-name");
    private final By lastNameInput = By.id("last-name");
    private final By genderMaleRadio = By.xpath("//input[@name='gender' and @value='Male']");
    private final By dateOfBirthInput = By.id("dob");
    private final By addressInput = By.id("address");
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By companyInput = By.id("company");
    private final By roleDropdown = By.id("role");
    private final By jobExpectationTeamwork = By.xpath("//input[@name='expectation' and @value='Good teamwork']");
    private final By developmentWayCourses = By.xpath("//input[@name='development' and @value='Take online courses']");
    private final By commentTextarea = By.id("comment");
    private final By submitButton = By.id("submit");
    private final By pageHeader = By.tagName("h1");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToForm() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    @DisplayName("Verify Initial Page State and Title")
    void testPageInitialState() {
        assertEquals("Demo AUT", driver.getTitle(), "Page title should be 'Demo Form'");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader));
        assertEquals("Student Registration Form", header.getText(), "Page header should be correct.");
        assertTrue(driver.findElement(submitButton).isDisplayed(), "Submit button should be visible.");
        assertTrue(driver.findElement(submitButton).isEnabled(), "Submit button should be enabled.");
    }

    @Test
    @Order(2)
    @DisplayName("Test Form Submission with Valid Data")
    void testFormSubmissionWithAllFields() throws UnsupportedEncodingException {
        // --- Arrange: Prepare test data ---
        String firstName = "Gemini";
        String lastName = "Pro";
        String gender = "Male";
        String dob = "2025-09-04";
        String address = "123 AI Avenue";
        String email = "gemini.pro@google.com";
        String password = "supersecretpassword";
        String company = "Google";
        String role = "QA";
        String expectation = "Good teamwork";
        String development = "Take online courses";
        String comment = "This is a test comment.";

        // --- Act: Fill out the form ---
        driver.findElement(firstNameInput).sendKeys(firstName);
        driver.findElement(lastNameInput).sendKeys(lastName);
        driver.findElement(genderMaleRadio).click();
        driver.findElement(dateOfBirthInput).sendKeys(dob);
        driver.findElement(addressInput).sendKeys(address);
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(companyInput).sendKeys(company);
        
        Select roleSelect = new Select(driver.findElement(roleDropdown));
        roleSelect.selectByVisibleText(role);
        
        driver.findElement(jobExpectationTeamwork).click();
        driver.findElement(developmentWayCourses).click();
        driver.findElement(commentTextarea).sendKeys(comment);
        
        driver.findElement(submitButton).click();

        // --- Assert: Verify the submission by checking URL parameters ---
        wait.until(ExpectedConditions.urlContains("first-name=" + firstName));
        
        String currentUrl = driver.getCurrentUrl();
        String decodedUrl = URLDecoder.decode(currentUrl, StandardCharsets.UTF_8.name());

        assertAll("Verify submitted data in URL parameters",
            () -> assertTrue(decodedUrl.contains("first-name=" + firstName), "First name should be in URL."),
            () -> assertTrue(decodedUrl.contains("last-name=" + lastName), "Last name should be in URL."),
            () -> assertTrue(decodedUrl.contains("gender=" + gender), "Gender should be in URL."),
            () -> assertTrue(decodedUrl.contains("dob=" + dob), "Date of birth should be in URL."),
            () -> assertTrue(decodedUrl.contains("address=" + address), "Address should be in URL."),
            () -> assertTrue(decodedUrl.contains("email=" + email), "Email should be in URL."),
            () -> assertTrue(decodedUrl.contains("password=" + password), "Password should be in URL."),
            () -> assertTrue(decodedUrl.contains("company=" + company), "Company should be in URL."),
            () -> assertTrue(decodedUrl.contains("role=" + role), "Role should be in URL."),
            () -> assertTrue(decodedUrl.contains("expectation=" + expectation), "Job expectation should be in URL."),
            () -> assertTrue(decodedUrl.contains("development=" + development), "Ways of development should be in URL."),
            () -> assertTrue(decodedUrl.contains("comment=" + comment), "Comment should be in URL.")
        );
    }
}