package geminiPRO.ws05.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Set;

/**
 * JUnit 5 test suite for the "Central de Atendimento ao Cliente TAT" form page.
 * This suite uses Selenium WebDriver with headless Firefox to validate form submissions
 * and navigation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacTatE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    // --- Locators for Form Elements ---
    private final By firstNameInput = By.id("firstName");
    private final By lastNameInput = By.id("lastName");
    private final By emailInput = By.id("email");
    private final By phoneInput = By.id("phone");
    private final By helpTextArea = By.id("open-text-area");
    private final By productDropdown = By.id("product");
    private final By serviceTypeFeedbackRadio = By.xpath("//input[@name='atendimento-tipo' and @value='feedback']");
    private final By contactPreferenceEmailRadio = By.xpath("//input[@name='contact-type' and @value='email']");
    private final By newsletterCheckbox = By.id("email-checkbox");
    private final By submitButton = By.xpath("//button[@type='submit']");
    private final By successMessage = By.cssSelector("span.success");
    private final By errorMessage = By.cssSelector("span.error");
    private final By privacyPolicyLink = By.linkText("Política de Privacidade");

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        // Using a longer timeout as the dynamic messages can sometimes be slow to appear.
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
    @DisplayName("Should load the page and display the correct title")
    void testPageTitle() {
        String expectedTitle = "Central de Atendimento ao Cliente TAT";
        Assertions.assertEquals(expectedTitle, driver.getTitle(), "The page title is incorrect.");
    }

    @Test
    @Order(2)
    @DisplayName("Should successfully submit the form with all required fields")
    void testSuccessfulFormSubmission() {
        // --- Fill the form with valid data ---
        driver.findElement(firstNameInput).sendKeys("Gemini");
        driver.findElement(lastNameInput).sendKeys("Pro");
        driver.findElement(emailInput).sendKeys("gemini.pro@test.com");
        driver.findElement(phoneInput).sendKeys("11999998888");
        driver.findElement(helpTextArea).sendKeys("This is a test submission for a customer service request. Please disregard.");

        new Select(driver.findElement(productDropdown)).selectByVisibleText("Cursos");
        driver.findElement(serviceTypeFeedbackRadio).click();
        driver.findElement(contactPreferenceEmailRadio).click();
        driver.findElement(newsletterCheckbox).click();

        // --- Submit the form ---
        driver.findElement(submitButton).click();

        // --- Assert success message ---
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
        Assertions.assertEquals("Mensagem enviada com sucesso.", success.getText(), "Success message is not correct or not visible.");
    }

    @Test
    @Order(3)
    @DisplayName("Should display an error message when submitting with missing required fields")
    void testFailedFormSubmissionWithMissingFields() {
        // --- Fill only non-mandatory fields or leave mandatory ones blank ---
        driver.findElement(helpTextArea).sendKeys("Attempting to submit with missing required fields.");

        // --- Submit the form ---
        driver.findElement(submitButton).click();

        // --- Assert error message ---
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        Assertions.assertEquals("Valide os campos obrigatórios!", error.getText(), "Error message is not correct or not visible.");
    }

    @Test
    @Order(4)
    @DisplayName("Should open the privacy policy page in a new tab")
    void testPrivacyPolicyLinkOpensInNewTab() {
        String originalWindow = driver.getWindowHandle();
        Assertions.assertEquals(1, driver.getWindowHandles().size(), "There should be only one window open initially.");

        // --- Click the link that opens a new tab ---
        driver.findElement(privacyPolicyLink).click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // --- Switch to the new tab ---
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = new ArrayList<>(allWindows).get(0);
        driver.switchTo().window(newWindow);

        // --- Assert the content of the new tab ---
        String expectedPrivacyTitle = "Central de Atendimento ao Cliente TAT - Política de privacidade";
        wait.until(ExpectedConditions.titleIs(expectedPrivacyTitle));
        Assertions.assertEquals(expectedPrivacyTitle, driver.getTitle(), "The privacy policy page title is incorrect.");

        // --- Close the new tab and switch back ---
        driver.close();
        driver.switchTo().window(originalWindow);

        // --- Assert that we are back on the main page ---
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Should be back on the main application page.");
    }

    @Test
    @Order(5)
    @DisplayName("Should display an error message when phone is required but not filled")
    void testErrorWhenPhoneIsRequired() {
        // --- Fill the form but mark 'Telefone' as the preferred contact without providing a number ---
        driver.findElement(firstNameInput).sendKeys("Gemini");
        driver.findElement(lastNameInput).sendKeys("Pro");
        driver.findElement(emailInput).sendKeys("gemini.phone@test.com");
        driver.findElement(helpTextArea).sendKeys("Testing phone number validation.");
        
        // Select 'Telefone' as the preferred contact method, which makes the phone field mandatory.
        driver.findElement(By.xpath("//input[@name='contact-type' and @value='phone']")).click();
        
        // --- Submit the form ---
        driver.findElement(submitButton).click();

        // --- Assert error message ---
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        Assertions.assertEquals("Valide os campos obrigatórios!", error.getText(), "Error message for missing required phone is not correct.");
    }
}