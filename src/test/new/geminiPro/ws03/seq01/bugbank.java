package geminiPro.ws03.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A comprehensive JUnit 5 test suite for the BugBank application.
 * This suite uses Selenium WebDriver with Firefox in headless mode to test user registration,
 * login, fund transfers, statement viewing, and logout functionality.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Site and User Credentials ---
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    // A unique email is generated for each run to ensure test independence
    private static final String USER_EMAIL = "user" + System.currentTimeMillis() + "@test.com";
    private static final String USER_NAME = "Test User";
    private static final String USER_PASSWORD = "password123";

    // --- Locators ---
    // Registration Page
    private static final By REGISTER_BUTTON_HOME = By.xpath("//button[normalize-space()='Registrar']");
    private static final By EMAIL_INPUT_REGISTER = By.xpath("(//input[@type='email'])[2]");
    private static final By NAME_INPUT_REGISTER = By.xpath("//input[@type='name']");
    private static final By PASSWORD_INPUT_REGISTER = By.xpath("(//input[@type='password'])[2]");
    private static final By PASSWORD_CONFIRM_INPUT = By.xpath("(//input[@type='password'])[3]");
    private static final By BALANCE_TOGGLE = By.xpath("//label[@for='toggleAddBalance']");
    private static final By SUBMIT_REGISTER_BUTTON = By.xpath("//button[normalize-space()='Cadastrar']");

    // Login Page
    private static final By EMAIL_INPUT_LOGIN = By.xpath("(//input[@type='email'])[1]");
    private static final By PASSWORD_INPUT_LOGIN = By.xpath("(//input[@type='password'])[1]");
    private static final By LOGIN_BUTTON = By.xpath("//button[normalize-space()='Acessar']");

    // Modal
    private static final By MODAL_TEXT = By.id("modalText");
    private static final By MODAL_CLOSE_BUTTON = By.id("btnCloseModal");

    // Home Page (Post-Login)
    private static final By WELCOME_TEXT = By.xpath("//p[contains(text(), 'bem vindo')]");
    private static final By BALANCE_TEXT = By.id("textBalance");
    private static final By TRANSFER_BUTTON = By.id("btn-TRANSFERER");
    private static final By STATEMENT_BUTTON = By.id("btn-EXTRATO");
    private static final By LOGOUT_BUTTON = By.id("btnExit");

    // Transfer Page
    private static final By ACCOUNT_NUMBER_INPUT = By.xpath("//input[@name='accountNumber']");
    private static final By DIGIT_INPUT = By.xpath("//input[@name='digit']");
    private static final By TRANSFER_VALUE_INPUT = By.xpath("//input[@name='transferValue']");
    private static final By DESCRIPTION_INPUT = By.xpath("//input[@name='description']");
    private static final By CONFIRM_TRANSFER_BUTTON = By.xpath("//button[@type='submit' and normalize-space()='Transferir agora']");
    
    // Statement Page
    private static final By STATEMENT_PAGE_HEADER = By.xpath("//p[text()='Extrato']");
    private static final By BACK_BUTTON = By.id("btnBack");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Extracts a numeric value from the balance string (e.g., "R$ 1.000,00").
     */
    private double getBalanceFromString(String balanceText) {
        Pattern pattern = Pattern.compile("[0-9.,]+");
        Matcher matcher = pattern.matcher(balanceText);
        if (matcher.find()) {
            String numberStr = matcher.group(0).replace(".", "").replace(",", ".");
            return Double.parseDouble(numberStr);
        }
        throw new IllegalArgumentException("Could not parse balance from string: " + balanceText);
    }
    
    @Test
    @Order(1)
    @DisplayName("Should successfully register a new user with balance")
    void testRegisterNewUser() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_BUTTON_HOME)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT_REGISTER)).sendKeys(USER_EMAIL);
        driver.findElement(NAME_INPUT_REGISTER).sendKeys(USER_NAME);
        driver.findElement(PASSWORD_INPUT_REGISTER).sendKeys(USER_PASSWORD);
        driver.findElement(PASSWORD_CONFIRM_INPUT).sendKeys(USER_PASSWORD);
        driver.findElement(BALANCE_TOGGLE).click();
        driver.findElement(SUBMIT_REGISTER_BUTTON).click();

        WebElement modalTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        Assertions.assertTrue(modalTextElement.getText().contains("criada com sucesso"), "Registration success message not found.");
        
        // Extract and verify account number from modal
        String modalText = modalTextElement.getText();
        Assertions.assertTrue(modalText.matches("A conta \\d{3}-\\d foi criada com sucesso"), "Account number format is incorrect in success message.");
        
        wait.until(ExpectedConditions.elementToBeClickable(MODAL_CLOSE_BUTTON)).click();
    }
    
    @Test
    @Order(2)
    @DisplayName("Should fail to login with invalid password")
    void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT_LOGIN)).sendKeys(USER_EMAIL);
        driver.findElement(PASSWORD_INPUT_LOGIN).sendKeys("wrongpassword");
        driver.findElement(LOGIN_BUTTON).click();

        WebElement modalTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        Assertions.assertTrue(modalTextElement.getText().contains("Usuário ou senha inválido"), "Invalid credentials error message not found.");
        wait.until(ExpectedConditions.elementToBeClickable(MODAL_CLOSE_BUTTON)).click();
    }

    @Test
    @Order(3)
    @DisplayName("Should login successfully and display home page")
    void testSuccessfulLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT_LOGIN)).sendKeys(USER_EMAIL);
        driver.findElement(PASSWORD_INPUT_LOGIN).sendKeys(USER_PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home") || driver.getCurrentUrl().equals(BASE_URL), "URL did not navigate to home page after login.");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_TEXT)).isDisplayed(), "Welcome text is not visible on the home page.");
        
        // Initial balance should be R$ 1.000,00 as registered with balance
        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(BALANCE_TEXT));
        Assertions.assertEquals(1000.0, getBalanceFromString(balanceElement.getText()), "Initial balance is not R$ 1.000,00.");
    }
    
    @Test
    @Order(4)
    @DisplayName("Should perform a successful transfer and update balance")
    void testSuccessfulTransfer() {
        // Login first
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT_LOGIN)).sendKeys(USER_EMAIL);
        driver.findElement(PASSWORD_INPUT_LOGIN).sendKeys(USER_PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        
        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(BALANCE_TEXT));
        double initialBalance = getBalanceFromString(balanceElement.getText());

        wait.until(ExpectedConditions.elementToBeClickable(TRANSFER_BUTTON)).click();
        wait.until(ExpectedConditions.urlContains("/transfer"));

        // Fill transfer details
        double transferValue = 150.0;
        wait.until(ExpectedConditions.visibilityOfElementLocated(ACCOUNT_NUMBER_INPUT)).sendKeys("123");
        driver.findElement(DIGIT_INPUT).sendKeys("4");
        driver.findElement(TRANSFER_VALUE_INPUT).sendKeys(String.valueOf(transferValue));
        driver.findElement(DESCRIPTION_INPUT).sendKeys("Test Transfer");
        driver.findElement(CONFIRM_TRANSFER_BUTTON).click();

        // Verify success
        WebElement modalTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        Assertions.assertTrue(modalTextElement.getText().contains("Transferencia realizada com sucesso"), "Transfer success message not found.");
        wait.until(ExpectedConditions.elementToBeClickable(MODAL_CLOSE_BUTTON)).click();
        
        // Go back to home and verify updated balance
        wait.until(ExpectedConditions.elementToBeClickable(BACK_BUTTON)).click();
        wait.until(ExpectedConditions.urlContains("/home"));

        WebElement newBalanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(BALANCE_TEXT));
        double newBalance = getBalanceFromString(newBalanceElement.getText());
        Assertions.assertEquals(initialBalance - transferValue, newBalance, 0.01, "Balance was not correctly updated after transfer.");
    }

    @Test
    @Order(5)
    @DisplayName("Should navigate to statement page and return")
    void testViewStatement() {
        // Login first
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT_LOGIN)).sendKeys(USER_EMAIL);
        driver.findElement(PASSWORD_INPUT_LOGIN).sendKeys(USER_PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));

        wait.until(ExpectedConditions.elementToBeClickable(STATEMENT_BUTTON)).click();
        wait.until(ExpectedConditions.urlContains("/bank-statement"));
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(STATEMENT_PAGE_HEADER)).isDisplayed(), "Statement page header not found.");
        
        // Verify the transfer from the previous test is listed
        Assertions.assertFalse(driver.findElements(By.xpath("//p[text()='Test Transfer']")).isEmpty(), "Transfer description not found in statement.");
        Assertions.assertFalse(driver.findElements(By.xpath("//p[text()='- R$ 150,00']")).isEmpty(), "Transfer value not found in statement.");
        
        wait.until(ExpectedConditions.elementToBeClickable(BACK_BUTTON)).click();
        wait.until(ExpectedConditions.urlContains("/home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home") || driver.getCurrentUrl().equals(BASE_URL), "Did not return to the home page from statement.");
    }
    
    @Test
    @Order(6)
    @DisplayName("Should log out successfully and return to the login page")
    void testLogout() {
        // Login first
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT_LOGIN)).sendKeys(USER_EMAIL);
        driver.findElement(PASSWORD_INPUT_LOGIN).sendKeys(USER_PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));

        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Logout did not redirect to the base URL.");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_BUTTON)).isDisplayed(), "Login button not visible after logout.");
    }
}