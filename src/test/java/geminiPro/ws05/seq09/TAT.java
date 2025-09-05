package geminiPRO.ws05.seq09;

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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for the CAC TAT customer service form.
 * This suite covers form interactions, validation, file upload, and multi-tab navigation.
 * It uses Selenium WebDriver with headless Firefox.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacTatTest {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private final By firstNameInput = By.id("firstName");
    private final By lastNameInput = By.id("lastName");
    private final By emailInput = By.id("email");
    private final By phoneInput = By.id("phone");
    private final By productDropdown = By.id("product");
    private final By supportTypeRadio = By.name("atendimento-tat");
    private final By contactTypeRadio = By.name("contact-type");
    private final By emailCheckbox = By.id("email-checkbox");
    private final By openTextArea = By.id("open-text-area");
    private final By fileUploadInput = By.id("file-upload");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By successMessage = By.cssSelector("span.success");
    private final By errorMessage = By.cssSelector("span.error");
    private final By privacyPolicyLink = By.linkText("Política de Privacidade");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED headless mode via arguments
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
    }

    @Test
    @Order(1)
    void testPageTitle() {
        assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle(), "The page title is incorrect.");
    }

    @Test
    @Order(2)
    void testSuccessfulFormSubmission() {
        // Fill text fields
        driver.findElement(firstNameInput).sendKeys("Gemini");
        driver.findElement(lastNameInput).sendKeys("Pro");
        driver.findElement(emailInput).sendKeys("gemini.pro@test.com");
        driver.findElement(phoneInput).sendKeys("1234567890");

        // Select from dropdown
        new Select(driver.findElement(productDropdown)).selectByVisibleText("YouTube");

        // Select radio buttons and checkboxes
        selectRadioButtonByValue(supportTypeRadio, "Feedback");
        selectRadioButtonByValue(contactTypeRadio, "Telefone");
        driver.findElement(emailCheckbox).click();

        // Fill text area with a long text to ensure it works
        String longText = "This is a detailed comment about my user experience. ".repeat(10);
        driver.findElement(openTextArea).sendKeys(longText);

        // Submit form
        driver.findElement(submitButton).click();

        // Assert success
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        assertEquals("Mensagem enviada com sucesso.", success.getText(), "Success message is incorrect or not displayed.");
    }

    @Test
    @Order(3)
    void testRequiredFieldsValidation() {
        // Attempt to submit with no fields filled
        driver.findElement(submitButton).click();

        // Assert error message appears
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        assertEquals("Valide os campos obrigatórios!", error.getText(), "Error message for empty form is incorrect.");

        // Fill some fields but leave others empty
        driver.findElement(firstNameInput).sendKeys("Gemini");
        driver.findElement(lastNameInput).sendKeys("Pro");
        driver.findElement(openTextArea).sendKeys("Some text.");
        driver.findElement(submitButton).click();

        // Error message should still be visible because email is required
        assertTrue(error.isDisplayed(), "Error message should still be visible when email is missing.");
    }

    @Test
    @Order(4)
    void testFileUpload() throws IOException {
        // Create a temporary file to upload
        Path tempFile = Files.createTempFile("test-upload", ".txt");
        Files.writeString(tempFile, "This is the content of the test file.");

        // Fill required fields
        driver.findElement(firstNameInput).sendKeys("File");
        driver.findElement(lastNameInput).sendKeys("Uploader");
        driver.findElement(emailInput).sendKeys("file.uploader@test.com");
        driver.findElement(openTextArea).sendKeys("Uploading a file for testing.");

        // Use sendKeys to upload the file
        driver.findElement(fileUploadInput).sendKeys(tempFile.toAbsolutePath().toString());

        // Submit
        driver.findElement(submitButton).click();

        // Assert success
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        assertEquals("Mensagem enviada com sucesso.", success.getText(), "File upload submission failed.");

        // Clean up the temporary file
        Files.delete(tempFile);
    }
    
    @Test
    @Order(5)
    void testPrivacyPolicyLinkOpensInNewTab() {
        String originalWindow = driver.getWindowHandle();
        
        // Ensure there is only one window open initially
        assertEquals(1, driver.getWindowHandles().size(), "There should be only one window open initially.");
        
        // Click the privacy policy link
        driver.findElement(privacyPolicyLink).click();
        
        // Wait for the new tab to open
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        
        if (newWindow == null) {
            fail("New window for Privacy Policy did not open.");
        }

        // Switch to the new tab and verify its content
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        assertEquals("CAC TAT - Política de privacidade", driver.getTitle(), "Privacy Policy page title is incorrect.");
        
        // Close the new tab
        driver.close();
        
        // Switch back to the original tab
        driver.switchTo().window(originalWindow);
        
        // Verify we are back on the main page
        assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle(), "Should be back on the main form page.");
    }

    // --- Helper Methods ---

    /**
     * Finds and clicks a radio button within a group by its value attribute.
     * @param locator The locator for the radio button group (e.g., By.name("contact-type")).
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
}