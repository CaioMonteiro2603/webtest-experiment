package geminiPRO.ws05.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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

/**
 * A complete JUnit 5 test suite for the CAC TAT form page using Selenium WebDriver
 * with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacTatE2ETest {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    // --- Selenium WebDriver ---
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private static final By PAGE_TITLE_HEADER = By.id("title");
    private static final By FIRST_NAME_INPUT = By.id("firstName");
    private static final By LAST_NAME_INPUT = By.id("lastName");
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PHONE_INPUT = By.id("phone");
    private static final By HELP_TEXTAREA = By.id("open-text-area");
    private static final By PRODUCT_DROPDOWN = By.id("product");
    private static final By SUPPORT_TYPE_FEEDBACK_RADIO = By.xpath("//input[@type='radio' and @value='feedback']");
    private static final By NOTIFICATIONS_EMAIL_RADIO = By.xpath("//input[@type='radio' and @value='email']");
    private static final By NOTIFICATIONS_PHONE_CHECKBOX = By.id("phone-checkbox");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");
    private static final By ERROR_MESSAGE = By.className("error");
    private static final By SUCCESS_MESSAGE = By.className("success");
    private static final By PRIVACY_LINK = By.id("privacy");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        // Use a longer timeout as the success/error messages have a 3-second animation
        wait = new WebDriverWait(driver, Duration.ofSeconds(10)); 
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testPageLoadAndTitle() {
        driver.get(BASE_URL);
        Assertions.assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle(), "The page title is incorrect.");

        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE_TITLE_HEADER));
        Assertions.assertEquals("CAC TAT", header.getText(), "The main page header is incorrect.");
    }

    @Test
    @Order(2)
    void testSubmitWithMissingRequiredFields() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
        Assertions.assertEquals("Valide os campos obrigatórios!", errorMessage.getText(), "The error message for required fields is incorrect.");
    }

    @Test
    @Order(3)
    void testSuccessfulFormSubmission() {
        driver.get(BASE_URL);

        // Fill in required fields
        driver.findElement(FIRST_NAME_INPUT).sendKeys("Gemini");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Tester");
        driver.findElement(EMAIL_INPUT).sendKeys("gemini.tester@example.com");
        driver.findElement(HELP_TEXTAREA).sendKeys("This is a test submission from an automated script. Please disregard.");
        
        // Click submit
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
        
        // Assert navigation and success message
        wait.until(ExpectedConditions.urlContains("success.html"));
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertEquals("Mensagem enviada com sucesso.", successMessage.getText(), "The success message is incorrect or not found.");
    }
    
    @Test
    @Order(4)
    void testFullFormInteraction() {
        driver.get(BASE_URL);

        // Fill all fields
        driver.findElement(FIRST_NAME_INPUT).sendKeys("Gemini");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Pro");
        driver.findElement(EMAIL_INPUT).sendKeys("gemini.pro@example.com");
        driver.findElement(PHONE_INPUT).sendKeys("1234567890");
        driver.findElement(HELP_TEXTAREA).sendKeys("This is a complete test submission from an automated script. Please disregard.");

        // Select from dropdown
        Select productSelect = new Select(driver.findElement(PRODUCT_DROPDOWN));
        productSelect.selectByValue("youtube");

        // Select radio button
        driver.findElement(SUPPORT_TYPE_FEEDBACK_RADIO).click();
        
        // Select a different radio button for contact preference
        driver.findElement(NOTIFICATIONS_EMAIL_RADIO).click();
        
        // Select a checkbox
        driver.findElement(NOTIFICATIONS_PHONE_CHECKBOX).click();

        // Submit
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();

        // Assert success
        wait.until(ExpectedConditions.urlContains("success.html"));
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
        Assertions.assertTrue(successMessage.isDisplayed(), "The success message was not displayed after a full form submission.");
    }

    @Test
    @Order(5)
    void testPrivacyPolicyPageNavigation() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(PRIVACY_LINK)).click();
        
        // The privacy page opens on the same tab, so no window handle switching is needed.
        wait.until(ExpectedConditions.urlContains("privacy.html"));
        
        Assertions.assertEquals("Central de Atendimento ao Cliente TAT - Política de privacidade", driver.getTitle(), "The privacy page title is incorrect.");
        
        WebElement privacyHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
        Assertions.assertTrue(privacyHeader.isDisplayed(), "The header on the privacy page was not found.");
    }
}