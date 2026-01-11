package geminiPro.ws04.seq06;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 test suite for a simple HTML form page from Katalon.
 * This suite uses Selenium WebDriver with headless Firefox to test form field interactions
 * and validates submission by inspecting the URL query parameters.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use headless mode via arguments ONLY
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
    }

    /**
     * Verifies the initial state of the form, ensuring all expected elements are present.
     */
    @Test
    @Order(1)
    void initialPageStateTest() {
        // Assert page title
        assertEquals("Demo AUT", driver.getTitle(), "Page title is incorrect.");

        // Assert main header is visible
        WebElement header = driver.findElement(By.xpath("//h1[text()='HTML Form Example']"));
        assertTrue(header.isDisplayed(), "Main header should be visible.");

        // Assert key form fields are present
        assertTrue(driver.findElement(By.id("firstName")).isDisplayed(), "First Name input is missing.");
        assertTrue(driver.findElement(By.id("lastName")).isDisplayed(), "Last Name input is missing.");
        assertTrue(driver.findElement(By.id("email")).isDisplayed(), "Email input is missing.");
        assertTrue(driver.findElement(By.id("role")).isDisplayed(), "Role dropdown is missing.");
        assertTrue(driver.findElement(By.id("submit")).isDisplayed(), "Submit button is missing.");
    }

    /**
     * Fills out the entire form with valid data and verifies that the data is correctly
     * appended to the URL as query parameters upon submission.
     */
    @Test
    @Order(2)
    void successfulFormSubmissionTest() throws UnsupportedEncodingException {
        // --- Test Data ---
        String firstName = "Gemini";
        String lastName = "Pro";
        String gender = "Male";
        String dob = "09/02/2025";
        String address = "123 AI Avenue";
        String email = "gemini.pro@google.test";
        String password = "superSecretPassword123";
        String company = "Google";
        String role = "Manager";
        String expectation = "Excellent";
        String comment = "This is a test comment with spaces.";

        // --- Fill Form ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        driver.findElement(By.id("firstName")).sendKeys(firstName);
        driver.findElement(By.id("lastName")).sendKeys(lastName);
        driver.findElement(By.xpath("//input[@name='gender' and @value='" + gender + "']")).click();
        driver.findElement(By.id("dob")).sendKeys(dob);
        driver.findElement(By.id("address")).sendKeys(address);
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("company")).sendKeys(company);

        Select roleSelect = new Select(driver.findElement(By.id("role")));
        roleSelect.selectByVisibleText(role);

        Select expectationSelect = new Select(driver.findElement(By.id("expectation")));
        expectationSelect.selectByVisibleText(expectation);

        driver.findElement(By.xpath("//label[normalize-space()='Read books']//input")).click();
        driver.findElement(By.id("comment")).sendKeys(comment);

        // --- Submit Form ---
        wait.until(ExpectedConditions.elementToBeClickable(By.id("submit"))).click();

        // --- Verify URL ---
        // The page reloads with form data in the URL. Wait for a key parameter to appear.
        wait.until(ExpectedConditions.urlContains("firstName=" + firstName));
        String currentUrl = driver.getCurrentUrl();
        
        // Assert all submitted values are present in the final URL
        assertTrue(currentUrl.contains("firstName=" + encode(firstName)), "First name not found in URL.");
        assertTrue(currentUrl.contains("lastName=" + encode(lastName)), "Last name not found in URL.");
        assertTrue(currentUrl.contains("gender=" + encode(gender)), "Gender not found in URL.");
        assertTrue(currentUrl.contains("dob=" + encode(dob)), "Date of birth not found in URL.");
        assertTrue(currentUrl.contains("address=" + encode(address)), "Address not found in URL.");
        assertTrue(currentUrl.contains("email=" + encode(email)), "Email not found in URL.");
        assertTrue(currentUrl.contains("password=" + encode(password)), "Password not found in URL.");
        assertTrue(currentUrl.contains("company=" + encode(company)), "Company not found in URL.");
        assertTrue(currentUrl.contains("role=" + encode(role)), "Role not found in URL.");
        assertTrue(currentUrl.contains("expectation=" + encode(expectation)), "Expectation not found in URL.");
        assertTrue(currentUrl.contains("comment=" + encode(comment)), "Comment not found in URL.");
        assertTrue(currentUrl.contains("ways_of_development=Read+books"), "Ways of development not found in URL.");
    }

    /**
     * Tests submitting the form with no data entered to verify it handles empty values correctly.
     */
    @Test
    @Order(3)
    void emptyFormSubmissionTest() {
        // Submit the form without filling any fields
        wait.until(ExpectedConditions.elementToBeClickable(By.id("submit"))).click();
        
        // Wait for the page to reload
        wait.until(ExpectedConditions.urlContains("firstName="));
        String currentUrl = driver.getCurrentUrl();

        // Verify that the required parameters are present but empty
        assertTrue(currentUrl.contains("firstName="), "firstName parameter is missing.");
        assertTrue(currentUrl.contains("lastName="), "lastName parameter is missing.");
        assertTrue(currentUrl.contains("address="), "address parameter is missing.");
        assertTrue(currentUrl.contains("email="), "email parameter is missing.");
        
        // Role has a default value, so it won't be empty
        assertTrue(currentUrl.contains("role=Manager"), "Default role should be submitted.");
    }

    /**
     * Helper method to URL-encode a string for verification in the URL query.
     *
     * @param value The string to encode.
     * @return The URL-encoded string.
     * @throws UnsupportedEncodingException if the encoding is not supported.
     */
    private String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
    }
}