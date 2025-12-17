package geminiPro.ws05.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * A complete JUnit 5 test suite for the CAC TAT Customer Support form.
 * This test uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    // Constants for configuration
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final String LONG_TEXT = "This is a long test text. It is being used to fill a text area that requires a significant amount of characters to properly test the application's behavior under such conditions. Repetition is key for length: test, test, test.";

    // WebDriver and WebDriverWait instances shared across all tests
    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators for form elements
    private final By firstNameInput = By.id("firstName");
    private final By lastNameInput = By.id("lastName");
    private final By emailInput = By.id("email");
    private final By phoneInput = By.id("phone");
    private final By productSelect = By.id("product");
    private final By supportTypeFeedbackRadio = By.xpath("//input[@type='radio' and @value='feedback']");
    private final By contactEmailCheckbox = By.xpath("//input[@type='checkbox' and @value='email']");
    private final By openTextArea = By.id("open-text-area");
    private final By fileUploadInput = By.id("file-upload");
    private final By sendButton = By.cssSelector("button[type='submit']");
    private final By successMessage = By.cssSelector("span.success");
    private final By errorMessage = By.cssSelector("span.error");

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
    @DisplayName("Should load the page and verify the application title")
    void testPageTitleAndHeader() {
        Assertions.assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle(), "Page title is incorrect.");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        Assertions.assertEquals("CAC TAT", header.getText(), "Application header text is incorrect.");
    }

    @Test
    @Order(2)
    @DisplayName("Should fill all required fields and submit the form successfully")
    void testSuccessfulFormSubmission() {
        // Arrange: Fill the form
        fillRequiredFields();

        // Act: Submit the form
        driver.findElement(sendButton).click();

        // Assert: Verify the success message
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        Assertions.assertEquals("Mensagem enviada com sucesso.", message.getText(), "Success message text is incorrect.");
    }

    @Test
    @Order(3)
    @DisplayName("Should show an error when submitting with a missing required field (email)")
    void testErrorOnSubmissionWithMissingEmail() {
        // Arrange: Fill all required fields except email
        driver.findElement(firstNameInput).sendKeys("Caio");
        driver.findElement(lastNameInput).sendKeys("Augusto");
        // Email is intentionally left blank
        driver.findElement(openTextArea).sendKeys("This is a test.");
        driver.findElement(contactEmailCheckbox).click(); // This makes email required

        // Act: Submit the form
        driver.findElement(sendButton).click();

        // Assert: Verify the error message
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        Assertions.assertEquals("Valide os campos obrigatórios!", message.getText(), "Error message text is incorrect.");
    }

    @Test
    @Order(4)
    @DisplayName("Should prevent non-numeric input in the phone number field")
    void testPhoneNumberFieldAcceptsOnlyNumbers() {
        WebElement phoneField = driver.findElement(phoneInput);
        phoneField.sendKeys("abcdefg!@#");
        Assertions.assertEquals("", phoneField.getAttribute("value"), "Phone field should be empty after entering non-numeric characters.");
    }

    @Test
    @Order(5)
    @DisplayName("Should successfully upload a file")
    void testFileUpload() throws IOException {
        // Arrange: Create a temporary file to upload
        Path tempFile = Files.createTempFile("test-upload", ".txt");
        Files.write(tempFile, "This is the file content.".getBytes());

        // Act: Upload the file and fill other required fields
        driver.findElement(fileUploadInput).sendKeys(tempFile.toAbsolutePath().toString());
        fillRequiredFields();
        driver.findElement(sendButton).click();

        // Assert: Verify the form submission is successful
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        Assertions.assertTrue(message.isDisplayed(), "Form should be submitted successfully after file upload.");

        // Cleanup
        Files.deleteIfExists(tempFile);
    }

    @Test
    @Order(6)
    @DisplayName("Should display the privacy policy page in a new tab")
    void testPrivacyPolicyLinkOpensInNewTab() {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Política de Privacidade"))).click();

        // Wait for the new tab to open and switch to it
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert content on the new privacy policy page
        wait.until(ExpectedConditions.urlContains("privacy.html"));
        WebElement privacyTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        Assertions.assertEquals("CAC TAT - Política de privacidade", privacyTitle.getText(), "Privacy page title is incorrect.");

        // Close the new tab and switch back to the original
        driver.close();
        driver.switchTo().window(originalWindow);

        // Assert that we are back on the main form page
        WebElement mainTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        Assertions.assertEquals("CAC TAT", mainTitle.getText(), "Should be back on the main application page.");
    }

    @Test
    @Order(7)
    @DisplayName("Should select a product from the dropdown by its value")
    void testSelectProductByValue() {
        Select productDropdown = new Select(driver.findElement(productSelect));
        productDropdown.selectByValue("mentoria");
        WebElement selectedOption = productDropdown.getFirstSelectedOption();
        Assertions.assertEquals("Mentoria", selectedOption.getText(), "The selected product should be 'Mentoria'.");
    }

    // --- Helper Methods ---

    /**
     * Fills all the required fields of the form with valid data.
     */
    private void fillRequiredFields() {
        driver.findElement(firstNameInput).sendKeys("Caio");
        driver.findElement(lastNameInput).sendKeys("Augusto");
        driver.findElement(emailInput).sendKeys("caio.augusto@geminipro.com");
        new Select(driver.findElement(productSelect)).selectByVisibleText("YouTube");
        driver.findElement(supportTypeFeedbackRadio).click();
        driver.findElement(contactEmailCheckbox).click();
        driver.findElement(openTextArea).sendKeys(LONG_TEXT);
    }
}