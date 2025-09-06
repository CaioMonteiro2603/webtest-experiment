package geminiPRO.ws03.seq10;

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
 * A comprehensive JUnit 5 test suite for the BugBank application using Selenium WebDriver with Firefox in headless mode.
 * This suite covers the full user lifecycle including registration, login, fund transfer, statement verification, and logout.
 * Tests are ordered to follow a logical user flow, with each step building upon the previous one.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugBankE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Site and User Details ---
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String EMAIL = "caio@gmail.com";
    private static final String NAME = "Caio Gemini";
    private static final String PASSWORD = "123";

    // --- Test Data Storage ---
    private static String accountNumber;
    private static String accountDigit;

    // --- Locators ---
    // Registration Modal Locators
    private static final By REGISTER_HOME_BUTTON = By.xpath("//button[text()='Registrar']");
    private static final By REGISTER_MODAL = By.className("card__register");
    private static final By EMAIL_REGISTER_INPUT = By.cssSelector(".card__register input[name='email']");
    private static final By NAME_REGISTER_INPUT = By.cssSelector(".card__register input[name='name']");
    private static final By PASSWORD_REGISTER_INPUT = By.cssSelector(".card__register input[name='password']");
    private static final By PASSWORD_CONFIRM_INPUT = By.cssSelector(".card__register input[name='passwordConfirmation']");
    private static final By BALANCE_TOGGLE = By.id("toggleAddBalance");
    private static final By REGISTER_SUBMIT_BUTTON = By.xpath("//button[text()='Cadastrar']");
    
    // Login Form Locators
    private static final By EMAIL_LOGIN_INPUT = By.cssSelector(".card__login input[name='email']");
    private static final By PASSWORD_LOGIN_INPUT = By.cssSelector(".card__login input[name='password']");
    private static final By LOGIN_BUTTON = By.xpath("//button[text()='Acessar']");

    // General & Modal Locators
    private static final By MODAL_TEXT = By.id("modalText");
    private static final By MODAL_CLOSE_BUTTON = By.id("btnCloseModal");

    // Home Page (Post-Login) Locators
    private static final By WELCOME_TEXT = By.id("textName");
    private static final By BALANCE_TEXT = By.id("textBalance");
    private static final By LOGOUT_BUTTON = By.id("btnExit");
    private static final By TRANSFER_BUTTON = By.id("btn-TRANSFER");
    private static final By STATEMENT_BUTTON = By.id("btn-EXTRATO");

    // Transfer Page Locators
    private static final By TRANSFER_ACCOUNT_INPUT = By.name("accountNumber");
    private static final By TRANSFER_DIGIT_INPUT = By.name("digit");
    private static final By TRANSFER_VALUE_INPUT = By.name("transferValue");
    private static final By TRANSFER_DESCRIPTION_INPUT = By.name("description");
    private static final By TRANSFER_SUBMIT_BUTTON = By.xpath("//button[text()='Transferir agora']");

    // Statement Page Locators
    private static final By STATEMENT_TRANSACTIONS = By.id("listaExtrato");

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // Use arguments as strictly required
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("🧪 Test New User Registration with Initial Balance")
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_HOME_BUTTON)).click();

        WebElement registerForm = wait.until(ExpectedConditions.visibilityOfElementLocated(REGISTER_MODAL));
        Assertions.assertTrue(registerForm.isDisplayed(), "Registration modal should be visible.");

        driver.findElement(EMAIL_REGISTER_INPUT).sendKeys(EMAIL);
        driver.findElement(NAME_REGISTER_INPUT).sendKeys(NAME);
        driver.findElement(PASSWORD_REGISTER_INPUT).sendKeys(PASSWORD);
        driver.findElement(PASSWORD_CONFIRM_INPUT).sendKeys(PASSWORD);
        driver.findElement(BALANCE_TOGGLE).click();
        driver.findElement(REGISTER_SUBMIT_BUTTON).click();

        WebElement modalTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        String successMessage = modalTextElement.getText();
        
        Assertions.assertTrue(successMessage.contains("foi criada com sucesso"), "Success message should confirm account creation.");

        // Regex to extract account number and digit from the success message like "A conta 123-4 foi criada com sucesso"
        Pattern pattern = Pattern.compile("(\\d+)-(\\d+)");
        Matcher matcher = pattern.matcher(successMessage);
        if (matcher.find()) {
            accountNumber = matcher.group(1);
            accountDigit = matcher.group(2);
        }
        Assertions.assertNotNull(accountNumber, "Could not extract account number from success message.");
        Assertions.assertNotNull(accountDigit, "Could not extract account digit from success message.");

        driver.findElement(MODAL_CLOSE_BUTTON).click();
    }

    @Test
    @Order(2)
    @DisplayName("🧪 Test Login with Invalid and Valid Credentials")
    void testLoginFunctionality() {
        driver.get(BASE_URL);

        // --- Invalid Login Attempt ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_LOGIN_INPUT)).sendKeys(EMAIL);
        driver.findElement(PASSWORD_LOGIN_INPUT).sendKeys("wrong-password");
        driver.findElement(LOGIN_BUTTON).click();

        WebElement errorModal = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        Assertions.assertTrue(errorModal.getText().contains("Usuário ou senha inválido"), "Error message for invalid credentials should be displayed.");
        driver.findElement(MODAL_CLOSE_BUTTON).click();
        
        // Wait for modal to disappear
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_TEXT));

        // --- Valid Login Attempt ---
        WebElement emailInput = driver.findElement(EMAIL_LOGIN_INPUT);
        emailInput.clear();
        emailInput.sendKeys(EMAIL);

        WebElement passwordInput = driver.findElement(PASSWORD_LOGIN_INPUT);
        passwordInput.clear();
        passwordInput.sendKeys(PASSWORD);
        driver.findElement(LOGIN_BUTTON).click();
        
        wait.until(ExpectedConditions.urlContains("/home"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("/home"), "Should be redirected to home page after login.");

        WebElement welcomeTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(WELCOME_TEXT));
        Assertions.assertTrue(welcomeTextElement.getText().contains(NAME), "Welcome message should contain the user's name.");
    }
    
    @Test
    @Order(3)
    @DisplayName("🧪 Test Fund Transfer and Balance Update")
    void testFundTransfer() {
        Assertions.assertNotNull(accountNumber, "Account number must be available for transfer test.");
        
        // Navigate to Transfer page
        wait.until(ExpectedConditions.elementToBeClickable(TRANSFER_BUTTON)).click();
        wait.until(ExpectedConditions.urlContains("/transfer"));

        // Fill transfer form and submit
        driver.findElement(TRANSFER_ACCOUNT_INPUT).sendKeys(accountNumber);
        driver.findElement(TRANSFER_DIGIT_INPUT).sendKeys(accountDigit);
        driver.findElement(TRANSFER_VALUE_INPUT).sendKeys("150.50");
        driver.findElement(TRANSFER_DESCRIPTION_INPUT).sendKeys("Test Transfer");
        driver.findElement(TRANSFER_SUBMIT_BUTTON).click();

        // Assert success message
        WebElement successModal = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        Assertions.assertTrue(successModal.getText().contains("sucesso"), "Transfer success message should be displayed.");
        driver.findElement(MODAL_CLOSE_BUTTON).click();

        // Assert balance update on home page
        wait.until(ExpectedConditions.urlContains("/home"));
        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(BALANCE_TEXT));
        
        // Initial balance was R$ 1.000,00. After R$ 150,50 transfer, it should be R$ 849,50
        Assertions.assertEquals("R$ 849,50", balanceElement.getText(), "Balance should be updated after the transfer.");
    }
    
    @Test
    @Order(4)
    @DisplayName("🧪 Test Bank Statement Verification")
    void testBankStatement() {
        wait.until(ExpectedConditions.elementToBeClickable(STATEMENT_BUTTON)).click();
        wait.until(ExpectedConditions.urlContains("/bank-statement"));
        
        WebElement transactions = wait.until(ExpectedConditions.visibilityOfElementLocated(STATEMENT_TRANSACTIONS));
        
        // Check for the opening balance entry
        WebElement openingBalance = transactions.findElement(By.xpath("//p[text()='Saldo em conta']"));
        Assertions.assertTrue(openingBalance.isDisplayed(), "Opening balance description should be present.");
        WebElement openingBalanceValue = transactions.findElement(By.xpath("//p[text()='R$ 1.000,00']"));
        Assertions.assertTrue(openingBalanceValue.isDisplayed(), "Opening balance value should be R$ 1.000,00.");
        
        // Check for the transfer transaction
        WebElement transferDescription = transactions.findElement(By.xpath("//p[text()='Test Transfer']"));
        Assertions.assertTrue(transferDescription.isDisplayed(), "Transfer description should be present in the statement.");
        WebElement transferValue = transactions.findElement(By.xpath("//p[text()='- R$ 150,50']"));
        Assertions.assertTrue(transferValue.isDisplayed(), "Transfer value should be present in the statement.");
    }

    @Test
    @Order(5)
    @DisplayName("🧪 Test Logout Functionality")
    void testLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "User should be redirected to the login page after logout.");
        
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_BUTTON));
        Assertions.assertTrue(loginButton.isDisplayed(), "Login button should be visible after logging out.");
    }
}