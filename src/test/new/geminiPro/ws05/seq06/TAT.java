package geminiPro.ws05.seq06;

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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 test suite for the "Central de Atendimento ao Cliente TAT" application.
 * This suite uses Selenium WebDriver with headless Firefox to test form submissions,
 * dynamic success/error messages, and navigation to the privacy policy page.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration SHORT_WAIT_TIMEOUT = Duration.ofSeconds(3);

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private static final By FIRST_NAME_INPUT = By.id("firstName");
    private static final By LAST_NAME_INPUT = By.id("lastName");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By TEXT_AREA = By.id("open-text-area");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");
    private static final By SUCCESS_MESSAGE = By.cssSelector(".success");
    private static final By ERROR_MESSAGE = By.cssSelector(".error");

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
     * Verifies the initial page title and the visibility of the main header.
     */
    @Test
    @Order(1)
    void pageTitleAndHeaderTest() {
        assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle(), "The page title is incorrect.");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        assertEquals("CAC TAT", header.getText(), "The main header text is incorrect.");
    }

    /**
     * Fills and submits the form with valid data, then verifies the success message appears and disappears.
     */
    @Test
    @Order(2)
    void successfulFormSubmissionTest() {
        fillRequiredFields();
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();

        WebElement successElement = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        assertEquals("Mensagem enviada com sucesso.", successElement.getText(), "Success message is not correct.");

        // Verify the success message disappears after its timeout
        WebDriverWait shortWait = new WebDriverWait(driver, SHORT_WAIT_TIMEOUT);
        shortWait.until(ExpectedConditions.invisibilityOf(successElement));
    }

    /**
     * Attempts to submit the form with empty required fields and verifies the error message.
     */
    @Test
    @Order(3)
    void requiredFieldsErrorTest() {
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
        assertEquals("Valide os campos obrigatórios!", errorElement.getText(), "Error message for required fields is not correct.");
        
        // Verify the error message disappears after its timeout
        WebDriverWait shortWait = new WebDriverWait(driver, SHORT_WAIT_TIMEOUT);
        shortWait.until(ExpectedConditions.invisibilityOf(errorElement));
    }

    /**
     * Verifies that the phone number field only accepts numeric input.
     */
    @Test
    @Order(4)
    void nonNumericPhoneInputTest() {
        WebElement phoneInput = driver.findElement(PHONE_INPUT);
        phoneInput.sendKeys("abcdefg");
        assertEquals("", phoneInput.getAttribute("value"), "Phone input should not accept non-numeric characters.");
    }

    /**
     * Tests various form interactions including dropdowns, radio buttons, and checkboxes.
     */
    @Test
    @Order(5)
    void advancedFormInteractionsTest() {
        fillRequiredFields();

        // Select a product from dropdown
        Select productSelect = new Select(driver.findElement(By.id("product")));
        productSelect.selectByVisibleText("YouTube");
        assertEquals("youtube", productSelect.getFirstSelectedOption().getAttribute("value"), "YouTube should be selected.");

        // Select a radio button
        driver.findElement(By.xpath("//input[@type='radio' and @value='feedback']")).click();
        
        // Select a checkbox and assert its state
        WebElement checkbox = driver.findElement(By.id("phone-checkbox"));
        checkbox.click();
        assertTrue(checkbox.isSelected(), "Phone preference checkbox should be selected.");

        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
        WebElement successElement = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        assertEquals("Mensagem enviada com sucesso.", successElement.getText(), "Success message is not correct.");
    }
    
    /**
     * Tests the file upload functionality by programmatically creating and uploading a temporary file.
     */
    @Test
    @Order(6)
    void fileUploadTest() throws IOException {
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, "This is a test file.".getBytes());

        WebElement fileInput = driver.findElement(By.id("file-upload"));
        fileInput.sendKeys(tempFile.toAbsolutePath().toString());

        // The value attribute of the file input will contain a fake path, but the file name should be present.
        String uploadedFileName = fileInput.getAttribute("value");
        assertTrue(uploadedFileName.contains(tempFile.getFileName().toString()), "File name not found in input value after upload.");

        Files.delete(tempFile);
    }

    /**
     * Verifies that the "Política de Privacidade" link opens in a new tab with the correct content.
     */
    @Test
    @Order(7)
    void privacyPolicyLinkTest() {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Política de Privacidade"))).click();
        
        // Wait for new tab to open and switch to it
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Verify content on the new tab
        wait.until(ExpectedConditions.titleIs("Central de Atendimento ao Cliente TAT - Política de privacidade"));
        WebElement privacyHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        assertEquals("CAC TAT - Política de privacidade", privacyHeader.getText(), "Privacy policy page header is incorrect.");

        // Close the new tab and switch back to the original
        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle(), "Should have switched back to the original page.");
    }

    // --- Helper Methods ---

    /**
     * Fills all the required fields of the form with valid data.
     */
    private void fillRequiredFields() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME_INPUT)).sendKeys("Gemini");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Pro");
        driver.findElement(EMAIL_INPUT).sendKeys("gemini.pro@google.test");
        driver.findElement(TEXT_AREA).sendKeys("This is a test message. It must be long enough to be valid.");
    }
}