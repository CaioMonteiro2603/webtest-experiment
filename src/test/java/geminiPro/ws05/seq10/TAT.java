package geminiPRO.ws05.seq10;

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
 * A JUnit 5 test suite for the "Central de Atendimento ao Cliente TAT" website.
 * This suite uses Selenium WebDriver with Firefox in headless mode to test form submissions,
 * error handling, and navigation to its privacy policy page.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacTatFormTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static Path tempFile;

    // --- Locators ---
    private static final By FIRST_NAME_INPUT = By.id("firstName");
    private static final By LAST_NAME_INPUT = By.id("lastName");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By PRODUCT_DROPDOWN = By.id("product");
    private static final By HELP_TEXTAREA = By.id("open-text-area");
    private static final By FILE_UPLOAD_INPUT = By.id("file-upload");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");
    private static final By SUCCESS_MESSAGE = By.className("success");
    private static final By ERROR_MESSAGE = By.className("error");
    private static final By PRIVACY_POLICY_LINK = By.linkText("Política de Privacidade");
    private static final By PRIVACY_PAGE_TITLE = By.id("title");

    @BeforeAll
    static void setUp() throws IOException {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments as strictly required
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Create a temporary file for the upload test
        tempFile = Files.createTempFile("test-upload", ".txt");
        Files.write(tempFile, "This is a test file.".getBytes());
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (driver != null) {
            driver.quit();
        }
        // Clean up the temporary file
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @BeforeEach
    void loadPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cac-tat-title")));
    }

    @Test
    @Order(1)
    @DisplayName("🧪 Test Successful Form Submission with All Interaction Types")
    void testSuccessfulFormSubmission() {
        // Fill text fields
        driver.findElement(FIRST_NAME_INPUT).sendKeys("Caio");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Gemini");
        driver.findElement(EMAIL_INPUT).sendKeys("caio.gemini@test.com");
        driver.findElement(PHONE_INPUT).sendKeys("19999999999");
        driver.findElement(HELP_TEXTAREA).sendKeys("This is a detailed test description for the text area. It should be long enough to be realistic.");

        // Select from dropdown
        Select productSelect = new Select(driver.findElement(PRODUCT_DROPDOWN));
        productSelect.selectByValue("youtube");

        // Select a radio button
        driver.findElement(By.cssSelector("input[name='atendimento-tipo'][value='Elogio']")).click();

        // Select a checkbox
        driver.findElement(By.cssSelector("input[name='meio-contato'][value='email']")).click();

        // Submit form
        driver.findElement(SUBMIT_BUTTON).click();

        // Assert success message
        WebElement successMessageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertEquals("Mensagem enviada com sucesso.", successMessageElement.getText(), "Success message text is incorrect.");
    }

    @Test
    @Order(2)
    @DisplayName("🧪 Test Error Message on Submission with Missing Required Fields")
    void testErrorOnMissingRequiredFields() {
        // Submit form without filling required fields
        driver.findElement(SUBMIT_BUTTON).click();

        // Assert error message
        WebElement errorMessageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
        Assertions.assertEquals("Valide os campos obrigatórios!", errorMessageElement.getText(), "Error message text is incorrect.");
    }

    @Test
    @Order(3)
    @DisplayName("🧪 Test Form Submission with File Upload")
    void testFileUpload() {
        // Fill required fields
        driver.findElement(FIRST_NAME_INPUT).sendKeys("Caio");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Upload");
        driver.findElement(EMAIL_INPUT).sendKeys("caio.upload@test.com");
        driver.findElement(HELP_TEXTAREA).sendKeys("Testing file upload functionality.");

        // "Upload" the file by sending its path to the input element
        driver.findElement(FILE_UPLOAD_INPUT).sendKeys(tempFile.toAbsolutePath().toString());

        // Submit form
        driver.findElement(SUBMIT_BUTTON).click();

        // Assert success message
        WebElement successMessageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertTrue(successMessageElement.isDisplayed(), "Success message should be visible after submitting with a file.");
    }

    @Test
    @Order(4)
    @DisplayName("🧪 Test Navigation to Privacy Policy Page in a New Tab")
    void testPrivacyPolicyLink() {
        String originalWindow = driver.getWindowHandle();
        
        // Ensure there is only one window before clicking
        Assertions.assertEquals(1, driver.getWindowHandles().size(), "There should be only one window open initially.");

        // Click the link that opens a new tab
        wait.until(ExpectedConditions.elementToBeClickable(PRIVACY_POLICY_LINK)).click();
        
        // Wait for the new window or tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // Loop through whatever windows are open and switch to the new one
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Wait for the new page to load and assert content
        wait.until(ExpectedConditions.urlContains("privacy.html"));
        WebElement privacyTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(PRIVACY_PAGE_TITLE));
        Assertions.assertEquals("CAC TAT - Política de privacidade", privacyTitle.getText(), "Privacy page title is incorrect.");

        // Close the new window and switch back to the original
        driver.close();
        driver.switchTo().window(originalWindow);

        // Assert that we are back on the main page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle(), "Should have returned to the main page.");
    }
}