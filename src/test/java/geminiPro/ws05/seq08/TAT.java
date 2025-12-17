package geminiPro.ws05.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private final By firstNameInput = By.id("firstName");
    private final By lastNameInput = By.id("lastName");
    private final By emailInput = By.id("email");
    private final By phoneInput = By.id("phone");
    private final By productDropdown = By.id("product");
    private final By helpRadioButton = By.cssSelector("input[type='radio'][value='ajuda']");
    private final By phoneCheckbox = By.id("phone-checkbox");
    private final By openTextArea = By.id("open-text-area");
    private final By fileUploadInput = By.id("file-upload");
    private final By submitButton = By.cssSelector("button[type='submit']");
    private final By successMessage = By.className("success");
    private final By errorMessage = By.className("error");
    private final By privacyPolicyLink = By.linkText("Política de Privacidade");

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

    private void fillRequiredFields() {
        driver.findElement(firstNameInput).sendKeys("Gemini");
        driver.findElement(lastNameInput).sendKeys("Pro");
        driver.findElement(emailInput).sendKeys("gemini.pro@google.com");
        driver.findElement(openTextArea).sendKeys("This is a test request.");
    }

    @Test
    @Order(1)
    @DisplayName("Verify Page Title and Header")
    void testPageTitleAndHeader() {
        assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle(), "Page title should be correct.");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("CAC TAT", header.getText(), "Main header text should be 'CAC TAT'.");
    }

    @Test
    @Order(2)
    @DisplayName("Submit Form Successfully with All Fields")
    void testSuccessfulFormSubmission() {
        fillRequiredFields();
        new Select(driver.findElement(productDropdown)).selectByValue("youtube");
        driver.findElement(helpRadioButton).click();
        driver.findElement(phoneCheckbox).click(); // This makes the phone field required
        driver.findElement(phoneInput).sendKeys("1234567890");

        driver.findElement(submitButton).click();

        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        assertEquals("Mensagem enviada com sucesso.", success.getText(), "Success message should be displayed.");
    }

    @Test
    @Order(3)
    @DisplayName("Show Error on Submitting with Empty Required Fields")
    void testErrorOnEmptyRequiredFields() {
        driver.findElement(submitButton).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        assertEquals("Valide os campos obrigatórios!", error.getText(), "Error message for empty fields should be displayed.");
    }

    @Test
    @Order(4)
    @DisplayName("Show Error on Submitting with Invalid Email")
    void testErrorOnInvalidEmail() {
        driver.findElement(firstNameInput).sendKeys("Test");
        driver.findElement(lastNameInput).sendKeys("User");
        driver.findElement(emailInput).sendKeys("invalid-email-format");
        driver.findElement(openTextArea).sendKeys("Some text.");

        driver.findElement(submitButton).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        assertEquals("Valide os campos obrigatórios!", error.getText(), "Error message for invalid email should be displayed.");
    }
    
    @Test
    @Order(5)
    @DisplayName("Show Error on Non-Numeric Phone when Required")
    void testErrorOnNonNumericPhone() {
        fillRequiredFields();
        driver.findElement(phoneCheckbox).click(); // This makes the phone field required
        driver.findElement(phoneInput).sendKeys("not-a-number");
        
        driver.findElement(submitButton).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        assertEquals("Valide os campos obrigatórios!", error.getText(), "Error message for non-numeric phone should be displayed.");
    }

    @Test
    @Order(6)
    @DisplayName("Verify Privacy Policy Link Opens in a New Tab")
    void testPrivacyPolicyLink() {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(privacyPolicyLink)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("privacy.html"));
        assertEquals("Central de Atendimento ao Cliente TAT - Política de privacidade", driver.getTitle(), "Privacy page title is incorrect.");

        driver.close();
        driver.switchTo().window(originalWindow);

        assertTrue(driver.findElement(By.id("title")).isDisplayed(), "Should have returned to the main form page.");
    }

    @Test
@Order(7)
@DisplayName("Successfully Upload a File")
void testFileUpload() throws IOException {
    Path tempFile = null;
    try {
        // Arrange: Create a temporary file to upload
        tempFile = Files.createTempFile("test-upload", ".txt");
        Files.write(tempFile, "This is a test file.".getBytes());
        File file = tempFile.toFile();

        // Act
        fillRequiredFields();
        WebElement fileInputElement = driver.findElement(fileUploadInput);
        fileInputElement.sendKeys(file.getAbsolutePath());
        driver.findElement(submitButton).click();
        
        // Assert
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        assertEquals("Mensagem enviada com sucesso.", success.getText(), "Form should submit successfully with a file upload.");

    } finally {
        // Teardown: Clean up the temporary file
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }
}
}