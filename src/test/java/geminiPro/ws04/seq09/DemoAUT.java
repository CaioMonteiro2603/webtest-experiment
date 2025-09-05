package geminiPRO.ws04.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 test suite for a sample Katalon form.
 * This suite covers form filling, submission, and validation of the results.
 * It uses Selenium WebDriver with headless Firefox.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KatalonFormTest {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators for Form Page ---
    private final By firstNameInput = By.id("first-name");
    private final By lastNameInput = By.id("last-name");
    private final By genderRadioButtons = By.name("gender");
    private final By dobInput = By.id("dob");
    private final By addressInput = By.id("address");
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By companyInput = By.id("company");
    private final By roleDropdown = By.id("role");
    private final By jobExpectationCheckboxes = By.name("expectation");
    private final By waysOfDevelopmentCheckboxes = By.name("development");
    private final By commentTextarea = By.id("comment");
    private final By submitButton = By.id("submit");
    
    // --- Locators for Submission Result Page ---
    private final By successMessage = By.id("submit-msg");
    private final By resultFirstName = By.id("first-name");
    private final By resultLastName = By.id("last-name");
    private final By resultGender = By.id("gender");
    private final By resultDob = By.id("dob");
    private final By resultAddress = By.id("address");
    private final By resultEmail = By.id("email");
    private final By resultPassword = By.id("password");
    private final By resultCompany = By.id("company");
    private final By resultRole = By.id("role");
    private final By resultJobExpectation = By.id("expectation");
    private final By resultWaysOfDevelopment = By.id("development");
    private final By resultComment = By.id("comment");
    
    // --- Test Data ---
    private final String testFirstName = "Gemini";
    private final String testLastName = "Pro";
    private final String testGender = "Male";
    private final String testDob = "05/30/1990";
    private final String testAddress = "123 AI Avenue";
    private final String testEmail = "gemini.pro@google.com";
    private final String testPassword = "supersecretpassword";
    private final String testCompany = "Google";
    private final String testRole = "Manager";
    private final String testExpectation = "High salary";
    private final String testDevelopmentWay1 = "Read books";
    private final String testDevelopmentWay2 = "Join tech cons";
    private final String testComment = "This is a test comment submitted by an automated script.";

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED headless mode via arguments
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != driver) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToForm() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(submitButton));
    }

    @Test
    @Order(1)
    void testPageTitleAndHeader() {
        assertEquals("Sample Web Form", driver.getTitle(), "The page title is incorrect.");
        WebElement header = driver.findElement(By.tagName("h1"));
        assertEquals("User Information", header.getText(), "The main header text is incorrect.");
    }
    
    @Test
    @Order(2)
    void testSuccessfulFormSubmission() {
        // Fill text inputs
        driver.findElement(firstNameInput).sendKeys(testFirstName);
        driver.findElement(lastNameInput).sendKeys(testLastName);
        driver.findElement(dobInput).sendKeys(testDob);
        driver.findElement(addressInput).sendKeys(testAddress);
        driver.findElement(emailInput).sendKeys(testEmail);
        driver.findElement(passwordInput).sendKeys(testPassword);
        driver.findElement(companyInput).sendKeys(testCompany);
        driver.findElement(commentTextarea).sendKeys(testComment);
        
        // Select gender radio button
        selectRadioButtonByValue(genderRadioButtons, testGender);

        // Select role from dropdown
        new Select(driver.findElement(roleDropdown)).selectByVisibleText(testRole);
        
        // Select job expectation checkboxes
        selectCheckboxByValue(jobExpectationCheckboxes, testExpectation);
        
        // Select ways of development checkboxes
        selectCheckboxByValue(waysOfDevelopmentCheckboxes, testDevelopmentWay1);
        selectCheckboxByValue(waysOfDevelopmentCheckboxes, testDevelopmentWay2);
        
        // Submit the form
        driver.findElement(submitButton).click();
        
        // --- Assertions on the Result Page ---
        wait.until(ExpectedConditions.urlContains("?"));
        assertTrue(driver.getCurrentUrl().contains("first-name=" + testFirstName), "URL should contain submitted first name.");
        
        assertEquals("Successfully submitted!", driver.findElement(successMessage).getText(), "Success message is incorrect.");
        assertEquals(testFirstName, driver.findElement(resultFirstName).getText(), "First name on result page is incorrect.");
        assertEquals(testLastName, driver.findElement(resultLastName).getText(), "Last name on result page is incorrect.");
        assertEquals(testGender, driver.findElement(resultGender).getText(), "Gender on result page is incorrect.");
        assertEquals(testDob, driver.findElement(resultDob).getText(), "Date of Birth on result page is incorrect.");
        assertEquals(testAddress, driver.findElement(resultAddress).getText(), "Address on result page is incorrect.");
        assertEquals(testEmail, driver.findElement(resultEmail).getText(), "Email on result page is incorrect.");
        assertEquals(testPassword, driver.findElement(resultPassword).getText(), "Password on result page is incorrect.");
        assertEquals(testCompany, driver.findElement(resultCompany).getText(), "Company on result page is incorrect.");
        assertEquals(testRole, driver.findElement(resultRole).getText(), "Role on result page is incorrect.");
        
        String expectedDevelopmentWays = testDevelopmentWay1 + ", " + testDevelopmentWay2;
        assertEquals(testExpectation, driver.findElement(resultJobExpectation).getText(), "Job expectation on result page is incorrect.");
        assertEquals(expectedDevelopmentWays, driver.findElement(resultWaysOfDevelopment).getText(), "Ways of development on result page are incorrect.");
        assertEquals(testComment, driver.findElement(resultComment).getText(), "Comment on result page is incorrect.");
    }

    @Test
    @Order(3)
    void testSubmittingWithOnlyRequiredFields() {
        // This form doesn't enforce required fields, so this test verifies
        // that it submits correctly with only a subset of data filled.
        driver.findElement(firstNameInput).sendKeys(testFirstName);
        driver.findElement(lastNameInput).sendKeys(testLastName);
        driver.findElement(submitButton).click();

        wait.until(ExpectedConditions.urlContains("?"));
        assertTrue(driver.getCurrentUrl().contains("first-name=" + testFirstName), "URL should contain submitted data.");

        // Assert submitted data is present
        assertEquals(testFirstName, driver.findElement(resultFirstName).getText(), "First name should be present on results page.");
        assertEquals(testLastName, driver.findElement(resultLastName).getText(), "Last name should be present on results page.");
        
        // Assert other fields are empty, reflecting the form's behavior
        assertEquals("", driver.findElement(resultGender).getText(), "Gender should be empty.");
        assertEquals("", driver.findElement(resultAddress).getText(), "Address should be empty.");
    }
    
    // --- Helper Methods ---
    
    /**
     * Finds and clicks a radio button within a group by its value attribute.
     * @param locator The locator for the radio button group (e.g., By.name("gender")).
     * @param value The value attribute of the radio button to select.
     */
    private void selectRadioButtonByValue(By locator, String value) {
        List<WebElement> radioButtons = driver.findElements(locator);
        for (WebElement button : radioButtons) {
            if (button.getAttribute("value").equalsIgnoreCase(value)) {
                button.click();
                return;
            }
        }
        throw new org.openqa.selenium.NoSuchElementException("Cannot locate radio button with value: " + value);
    }
    
    /**
     * Finds and clicks a checkbox within a group by its value attribute.
     * @param locator The locator for the checkbox group (e.g., By.name("expectation")).
     * @param value The value attribute of the checkbox to select.
     */
    private void selectCheckboxByValue(By locator, String value) {
        List<WebElement> checkboxes = driver.findElements(locator);
        for (WebElement checkbox : checkboxes) {
            if (checkbox.getAttribute("value").equals(value)) {
                checkbox.click();
                return;
            }
        }
        throw new org.openqa.selenium.NoSuchElementException("Cannot locate checkbox with value: " + value);
    }
}