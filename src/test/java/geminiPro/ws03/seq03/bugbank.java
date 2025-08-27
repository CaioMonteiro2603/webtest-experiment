package geminiPRO.ws03.seq03;

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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A complete JUnit 5 test suite for the BugBank website using Selenium WebDriver
 * with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugBankE2ETest {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://bugbank.netlify.app/";

    // --- Dynamic Test Data ---
    private static String testEmail;
    private static String testName;
    private static String testPassword;
    private static String accountNumber;
    private static String accountDigit;

    // --- Selenium WebDriver ---
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    // Login Page
    private static final By REGISTER_BUTTON_HOME = By.xpath("//button[text()='Registrar']");
    private static final By LOGIN_EMAIL_INPUT = By.xpath("//div[@class='card__login']//input[@name='email']");
    private static final By LOGIN_PASSWORD_INPUT = By.xpath("//div[@class='card__login']//input[@name='password']");
    private static final By LOGIN_ACCESS_BUTTON = By.xpath("//button[text()='Acessar']");

    // Registration Page
    private static final By REGISTER_EMAIL_INPUT = By.xpath("//div[@class='card__register']//input[@name='email']");
    private static final By REGISTER_NAME_INPUT = By.xpath("//input[@name='name']");
    private static final By REGISTER_PASSWORD_INPUT = By.xpath("//form[@class='styles__ContainerFormRegister-sc-7fhc7g-0 khLSMu']//input[@name='password']");
    private static final By REGISTER_CONFIRM_PASSWORD_INPUT = By.name("passwordConfirmation");
    private static final By ADD_BALANCE_TOGGLE = By.id("toggleAddBalance");
    private static final By REGISTER_SUBMIT_BUTTON = By.xpath("//button[text()='Cadastrar']");

    // Common
    private static final By MODAL_TEXT = By.id("modalText");
    private static final By MODAL_CLOSE_BUTTON = By.id("btnCloseModal");

    // Logged In Pages
    private static final By LOGOUT_BUTTON = By.id("btnExit");
    private static final By BALANCE_TEXT = By.id("textBalance");
    private static final By WELCOME_TEXT = By.id("textName");
    private static final By TRANSFER_LINK = By.id("btn-TRANSFERÊNCIA");
    private static final By STATEMENT_LINK = By.id("btn-EXTRATO");
    
    // Transfer Page
    private static final By TRANSFER_ACCOUNT_NUMBER_INPUT = By.xpath("//input[@name='accountNumber']");
    private static final By TRANSFER_DIGIT_INPUT = By.xpath("//input[@name='digit']");
    private static final By TRANSFER_VALUE_INPUT = By.xpath("//input[@name='transferValue']");
    private static final By TRANSFER_DESCRIPTION_INPUT = By.xpath("//input[@name='description']");
    private static final By CONFIRM_TRANSFER_BUTTON = By.xpath("//button[text()='Transferir agora']");
    private static final By BACK_BUTTON = By.id("btnBack");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Generate unique user data for the test run
        long timestamp = System.currentTimeMillis();
        testEmail = "tester" + timestamp + "@gemini.com";
        testName = "Gemini Tester";
        testPassword = "password123";
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testUserRegistrationWithInitialBalance() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_BUTTON_HOME)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(REGISTER_EMAIL_INPUT)).sendKeys(testEmail);
        driver.findElement(REGISTER_NAME_INPUT).sendKeys(testName);
        driver.findElement(REGISTER_PASSWORD_INPUT).sendKeys(testPassword);
        driver.findElement(REGISTER_CONFIRM_PASSWORD_INPUT).sendKeys(testPassword);
        driver.findElement(ADD_BALANCE_TOGGLE).click();
        driver.findElement(REGISTER_SUBMIT_BUTTON).click();

        WebElement modalTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        String successMessage = modalTextElement.getText();
        Assertions.assertTrue(successMessage.contains("foi criada com sucesso"), "Registration success message is incorrect.");

        // Extract account number from the success message
        Pattern pattern = Pattern.compile("(\\d+)-(\\d)");
        Matcher matcher = pattern.matcher(successMessage);
        Assertions.assertTrue(matcher.find(), "Could not find account number in success message.");
        accountNumber = matcher.group(1);
        accountDigit = matcher.group(2);

        Assertions.assertNotNull(accountNumber, "Account number was not extracted.");
        Assertions.assertNotNull(accountDigit, "Account digit was not extracted.");

        driver.findElement(MODAL_CLOSE_BUTTON).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_TEXT));
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_EMAIL_INPUT));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("#"), "Did not return to the login page after registration.");
    }

    @Test
    @Order(2)
    void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_EMAIL_INPUT)).sendKeys(testEmail);
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys("wrongpassword");
        driver.findElement(LOGIN_ACCESS_BUTTON).click();

        WebElement modalTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        Assertions.assertTrue(modalTextElement.getText().contains("Usuário ou senha inválido"), "Error message for invalid login is incorrect.");

        driver.findElement(MODAL_CLOSE_BUTTON).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_TEXT));
    }

    @Test
    @Order(3)
    void testSuccessfulLoginAndLogout() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_EMAIL_INPUT)).sendKeys(testEmail);
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys(testPassword);
        driver.findElement(LOGIN_ACCESS_BUTTON).click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_TEXT));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("/home"), "URL did not redirect to /home after login.");
        Assertions.assertTrue(welcomeMessage.getText().contains(testName), "Welcome message does not contain the correct user name.");

        WebElement balanceElement = driver.findElement(BALANCE_TEXT);
        Assertions.assertEquals("R$ 1.000,00", balanceElement.getText(), "Initial balance is incorrect.");

        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_ACCESS_BUTTON));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Did not return to the main login page after logout.");
    }
    
    @Test
    @Order(4)
    void testTransferWithInsufficientFunds() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_EMAIL_INPUT)).sendKeys(testEmail);
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys(testPassword);
        driver.findElement(LOGIN_ACCESS_BUTTON).click();
        
        wait.until(ExpectedConditions.elementToBeClickable(TRANSFER_LINK)).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(TRANSFER_ACCOUNT_NUMBER_INPUT)).sendKeys("999");
        driver.findElement(TRANSFER_DIGIT_INPUT).sendKeys("9");
        driver.findElement(TRANSFER_VALUE_INPUT).sendKeys("2000"); // More than initial balance
        driver.findElement(TRANSFER_DESCRIPTION_INPUT).sendKeys("Test insufficient funds");
        driver.findElement(CONFIRM_TRANSFER_BUTTON).click();
        
        WebElement modalTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        Assertions.assertTrue(modalTextElement.getText().contains("saldo suficiente"), "Error message for insufficient funds is incorrect.");

        driver.findElement(MODAL_CLOSE_BUTTON).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_TEXT));
    }

    @Test
    @Order(5)
    void testSuccessfulFundTransfer() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_EMAIL_INPUT)).sendKeys(testEmail);
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys(testPassword);
        driver.findElement(LOGIN_ACCESS_BUTTON).click();

        wait.until(ExpectedConditions.elementToBeClickable(TRANSFER_LINK)).click();

        double transferAmount = 150.00;
        wait.until(ExpectedConditions.visibilityOfElementLocated(TRANSFER_ACCOUNT_NUMBER_INPUT)).sendKeys("123");
        driver.findElement(TRANSFER_DIGIT_INPUT).sendKeys("4");
        driver.findElement(TRANSFER_VALUE_INPUT).sendKeys(String.valueOf(transferAmount));
        driver.findElement(TRANSFER_DESCRIPTION_INPUT).sendKeys("Test successful transfer");
        driver.findElement(CONFIRM_TRANSFER_BUTTON).click();

        WebElement modalTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        Assertions.assertTrue(modalTextElement.getText().contains("realizada com sucesso"), "Transfer success message not found.");

        driver.findElement(MODAL_CLOSE_BUTTON).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_TEXT));

        // Verify balance and statement
        wait.until(ExpectedConditions.visibilityOfElementLocated(BALANCE_TEXT));
        Assertions.assertEquals("R$ 850,00", driver.findElement(BALANCE_TEXT).getText(), "Balance did not update correctly after transfer.");

        wait.until(ExpectedConditions.elementToBeClickable(STATEMENT_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textStatement")));
        
        WebElement transactionValue = driver.findElement(By.xpath("//p[contains(@id, 'textTransferValue')]"));
        Assertions.assertTrue(transactionValue.getText().contains("-R$ 150,00"), "Transaction value is not correctly reflected in the statement.");
    }
}