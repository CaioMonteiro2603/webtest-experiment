package geminiPRO.ws03.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for bugbank.netlify.app using Selenium WebDriver with headless Firefox.
 * This suite covers user registration, login, funds transfer, and statement validation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugBankTest {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static String registeredEmail;
    private static final String registeredPassword = "Password123!";
    private static final String registeredName = "Gemini User";
    private static String accountNumber;
    private static String accountDigit;

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private final By registerButtonLogin = By.cssSelector(".login__buttons button:not([type='submit'])");
    private final By emailInput = By.cssSelector("div.card__login input[type='email']");
    private final By passwordInput = By.cssSelector("div.card__login input[name='password']");
    private final By loginButton = By.cssSelector(".login__buttons button[type='submit']");
    private final By modalText = By.id("modalText");
    private final By closeModalButton = By.id("btnCloseModal");

    private final By registerEmailInput = By.cssSelector("div.card__register input[type='email']");
    private final By registerNameInput = By.cssSelector("div.card__register input[type='name']");
    private final By registerPasswordInput = By.cssSelector("div.card__register input[name='password']");
    private final By registerPasswordConfInput = By.cssSelector("div.card__register input[name='passwordConfirmation']");
    private final By balanceToggle = By.id("toggleAddBalance");
    private final By registerSubmitButton = By.cssSelector("button.button__child");
    
    private final By transferButton = By.id("btn-TRANSFERENCIA");
    private final By statementButton = By.id("btn-EXTRATO");
    private final By logoutButton = By.id("btnExit");
    private final By balanceText = By.id("textBalance");
    
    private final By accountNumberInput = By.cssSelector("input[name='accountNumber']");
    private final By accountDigitInput = By.cssSelector("input[name='digit']");
    private final By transferValueInput = By.cssSelector("input[name='transferValue']");
    private final By descriptionInput = By.cssSelector("input[name='description']");
    private final By confirmTransferButton = By.cssSelector("button[type='submit']");
    
    // --- Setup and Teardown ---

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED headless mode via arguments
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        registeredEmail = "test.user." + System.currentTimeMillis() + "@bugbank.com";
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        driver.get(BASE_URL);
    }

    @AfterEach
    void logoutIfLoggedIn() {
        try {
            if (driver.findElements(logoutButton).size() > 0) {
                wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton));
            }
        } catch (Exception e) {
            // Ignore if logout link is not present or other errors occur during cleanup.
        }
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    void testRegisterNewUserWithBalance() {
        wait.until(ExpectedConditions.elementToBeClickable(registerButtonLogin)).click();
        
        // Wait for register page elements
        wait.until(ExpectedConditions.visibilityOfElementLocated(registerEmailInput)).sendKeys(registeredEmail);
        driver.findElement(registerNameInput).sendKeys(registeredName);
        driver.findElement(registerPasswordInput).sendKeys(registeredPassword);
        driver.findElement(registerPasswordConfInput).sendKeys(registeredPassword);
        driver.findElement(balanceToggle).click(); // Create account with balance
        driver.findElement(registerSubmitButton).click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        String successText = modal.getText();
        
        assertTrue(successText.startsWith("A conta"), "Registration success modal did not show expected text.");
        
        // Extract account number and digit using Regex
        Pattern pattern = Pattern.compile("(\\d+)-(\\d)");
        Matcher matcher = pattern.matcher(successText);
        if (matcher.find()) {
            accountNumber = matcher.group(1);
            accountDigit = matcher.group(2);
        } else {
            fail("Could not extract account number from registration success message: " + successText);
        }
        
        assertNotNull(accountNumber, "Account number should not be null.");
        assertNotNull(accountDigit, "Account digit should not be null.");
        
        driver.findElement(closeModalButton).click();
    }
    
    @Test
    @Order(2)
    void testInvalidLogin() {
        performLogin("invalid@email.com", "wrongpassword");
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        assertTrue(modal.getText().contains("Usuário ou senha inválido"), "Error message for invalid login is incorrect.");
        driver.findElement(closeModalButton).click();
    }

    @Test
    @Order(3)
    void testSuccessfulLogin() {
        performLogin(registeredEmail, registeredPassword);
        wait.until(ExpectedConditions.urlContains("/home"));
        WebElement balance = wait.until(ExpectedConditions.visibilityOfElementLocated(balanceText));
        assertTrue(balance.getText().contains("R$ 1.000,00"), "Initial balance should be R$ 1.000,00.");
        assertTrue(driver.findElement(By.id("textName")).getText().contains(registeredName), "Welcome message does not contain the registered user's name.");
    }

    @Test
    @Order(4)
    void testFundTransfer() {
        if (accountNumber == null || accountDigit == null) {
            fail("Cannot run fund transfer test; account details were not captured from registration.");
        }

        performLogin(registeredEmail, registeredPassword);
        wait.until(ExpectedConditions.urlContains("/home"));
        
        // Navigate to Transfer page
        wait.until(ExpectedConditions.elementToBeClickable(transferButton)).click();
        wait.until(ExpectedConditions.urlContains("/transferencia"));

        // Fill and submit transfer form
        wait.until(ExpectedConditions.visibilityOfElementLocated(accountNumberInput)).sendKeys(accountNumber);
        driver.findElement(accountDigitInput).sendKeys(accountDigit);
        driver.findElement(transferValueInput).sendKeys("150.50");
        driver.findElement(descriptionInput).sendKeys("Test Transfer");
        driver.findElement(confirmTransferButton).click();
        
        // Assert success
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        assertTrue(modal.getText().contains("sucesso"), "Transfer success message not found.");
        driver.findElement(closeModalButton).click();

        // Assert balance is updated
        wait.until(ExpectedConditions.urlContains("/home"));
        WebElement updatedBalance = wait.until(ExpectedConditions.visibilityOfElementLocated(balanceText));
        assertEquals("R$ 849,50", updatedBalance.getText(), "Balance was not correctly updated after transfer.");
    }
    
    @Test
    @Order(5)
    void testViewStatement() {
        // This test relies on the transfer from the previous test.
        performLogin(registeredEmail, registeredPassword);
        wait.until(ExpectedConditions.urlContains("/home"));

        wait.until(ExpectedConditions.elementToBeClickable(statementButton)).click();
        wait.until(ExpectedConditions.urlContains("/extrato"));

        WebElement statementContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bank-statement")));
        
        // Check for the transfer made in the previous test
        WebElement transferDescription = statementContainer.findElement(By.xpath("//p[text()='Test Transfer']"));
        WebElement transferValue = statementContainer.findElement(By.xpath("//p[text()='- R$ 150,50']"));
        
        assertNotNull(transferDescription, "Transfer description not found in statement.");
        assertNotNull(transferValue, "Transfer value not found in statement.");
    }

    @Test
    @Order(6)
    void testNavigationAndLogout() {
        performLogin(registeredEmail, registeredPassword);
        wait.until(ExpectedConditions.urlContains("/home"));
        
        // Navigate to transfer
        wait.until(ExpectedConditions.elementToBeClickable(transferButton)).click();
        wait.until(ExpectedConditions.urlContains("/transferencia"));
        assertTrue(driver.getCurrentUrl().endsWith("/transferencia"), "URL should point to transfer page.");

        // Navigate to statement
        wait.until(ExpectedConditions.elementToBeClickable(statementButton)).click();
        wait.until(ExpectedConditions.urlContains("/extrato"));
        assertTrue(driver.getCurrentUrl().endsWith("/extrato"), "URL should point to statement page.");
        
        // Logout
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/home")));
        assertTrue(driver.getCurrentUrl().endsWith("/"), "Should return to the login page after logout.");
        assertTrue(driver.findElement(loginButton).isDisplayed(), "Login button should be visible after logout.");
    }

    // --- Helper Methods ---

    /**
     * Performs the login action.
     * @param email The user's email.
     * @param password The user's password.
     */
    private void performLogin(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();
    }
}