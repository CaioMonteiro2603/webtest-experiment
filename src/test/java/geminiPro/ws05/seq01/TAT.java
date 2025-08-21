package geminiPro.ws05.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * A JUnit 5 test suite for the "Central de Atendimento ao Cliente TAT" form.
 * This suite uses Selenium WebDriver with Firefox in headless mode to verify form functionality,
 * including successful submissions, error handling for invalid data, and navigation to the privacy policy page.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacTatTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    // --- Locators ---
    private static final By FIRST_NAME_INPUT = By.id("firstName");
    private static final By LAST_NAME_INPUT = By.id("lastName");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By HELP_TEXT_AREA = By.id("open-text-area");
    private static final By PRODUCT_DROPDOWN = By.id("product");
    private static final By PHONE_CONTACT_CHECKBOX = By.cssSelector("input[type='checkbox'][value='telefone']");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");
    private static final By SUCCESS_MESSAGE = By.cssSelector("span.success");
    private static final By ERROR_MESSAGE = By.cssSelector("span.error");
    private static final By PRIVACY_POLICY_LINK = By.cssSelector("a[href='privacy.html']");
    private static final By PRIVACY_PAGE_TITLE = By.id("title");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        // Set a longer timeout for the error/success messages to appear, as they have a 3-second transition
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @BeforeEach
    void goToBaseUrl() {
        driver.get(BASE_URL);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should load the page and verify the application title")
    void testPageLoadAndTitleVerification() {
        String expectedTitle = "Central de Atendimento ao Cliente TAT";
        Assertions.assertEquals(expectedTitle, driver.getTitle(), "The page title is incorrect.");
    }

    @Test
    @Order(2)
    @DisplayName("Should fail to submit when required fields are empty")
    void testSubmitWithEmptyRequiredFieldsFails() {
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
        Assertions.assertEquals("Valide os campos obrigatórios!", errorMessage.getText(), "Error message for empty fields is incorrect.");
    }

    @Test
    @Order(3)
    @DisplayName("Should fail to submit when email format is invalid")
    void testSubmitWithInvalidEmailFails() {
        driver.findElement(FIRST_NAME_INPUT).sendKeys("John");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Doe");
        driver.findElement(EMAIL_INPUT).sendKeys("invalid-email-format");
        driver.findElement(HELP_TEXT_AREA).sendKeys("This is a test.");
        driver.findElement(SUBMIT_BUTTON).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
        Assertions.assertEquals("Valide os campos obrigatórios!", errorMessage.getText(), "Error message for invalid email is incorrect.");
    }

    @Test
    @Order(4)
    @DisplayName("Should fail if phone is preferred contact but number is not provided")
    void testSubmitWithPhoneAsPreferenceButEmptyFails() {
        driver.findElement(FIRST_NAME_INPUT).sendKeys("Jane");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Smith");
        driver.findElement(EMAIL_INPUT).sendKeys("jane.smith@example.com");
        driver.findElement(HELP_TEXT_AREA).sendKeys("Requesting phone support.");
        driver.findElement(PHONE_CONTACT_CHECKBOX).click();
        driver.findElement(SUBMIT_BUTTON).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
        Assertions.assertEquals("Valide os campos obrigatórios!", errorMessage.getText(), "Error message for missing phone number did not appear.");
    }

    @Test
    @Order(5)
    @DisplayName("Should fill all fields and submit the form successfully")
    void testFillAndSubmitFormSuccessfully() {
        // Fill text fields
        driver.findElement(FIRST_NAME_INPUT).sendKeys("Caique");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Monteiro");
        driver.findElement(EMAIL_INPUT).sendKeys("caiomonteiropro@gmail.com");
        driver.findElement(PHONE_INPUT).sendKeys("14999999999");
        driver.findElement(HELP_TEXT_AREA).sendKeys("Testing the form submission with all fields filled correctly. This is a detailed comment to ensure the text area works as expected.");

        // Select from dropdown
        Select productDropdown = new Select(driver.findElement(PRODUCT_DROPDOWN));
        productDropdown.selectByValue("youtube");

        // Select radio button
        driver.findElement(By.cssSelector("input[type='radio'][value='elogio']")).click();

        // Select checkbox
        driver.findElement(By.cssSelector("input[type='checkbox'][value='email']")).click();

        // Click submit
        driver.findElement(SUBMIT_BUTTON).click();

        // Verify success message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertEquals("Mensagem enviada com sucesso.", successMessage.getText(), "Success message did not appear or was incorrect.");
    }

    @Test
    @Order(6)
    @DisplayName("Should navigate to the privacy policy page")
    void testPrivacyPolicyPageNavigation() {
        wait.until(ExpectedConditions.elementToBeClickable(PRIVACY_POLICY_LINK)).click();
        wait.until(ExpectedConditions.urlContains("privacy.html"));

        // The privacy page doesn't have a unique title tag, but it has a unique H1 with id="title"
        WebElement privacyPageHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(PRIVACY_PAGE_TITLE));
        Assertions.assertEquals("CAC TAT - Política de privacidade", privacyPageHeader.getText(), "The header on the privacy policy page is incorrect.");
    }
}