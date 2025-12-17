package geminiPro.ws03.seq07;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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

/**
 * A comprehensive JUnit 5 test suite for the BugBank demo website.
 * This suite covers user registration, login/logout, fund transfers, and statement checks.
 * It uses Selenium WebDriver with Firefox running in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USER_EMAIL = "caio" + System.currentTimeMillis() + "@gmail.com";
    private static final String USER_NAME = "Caio Gemini";
    private static final String USER_PASSWORD = "123";

    private static final String RECIPIENT_EMAIL = "recipient" + System.currentTimeMillis() + "@gmail.com";
    private static final String RECIPIENT_NAME = "Recipient User";
    private static final String RECIPIENT_PASSWORD = "456";

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    private static WebDriver driver;
    private static WebDriverWait wait;

    // To be populated by registration tests
    private static AccountDetails userAccount;
    private static AccountDetails recipientAccount;

    // Locators
    private final By emailInput = By.xpath("//div[label[text()='Email']]/input[@type='email']");
    private final By passwordInput = By.xpath("//div[label[text()='Senha']]/input[@type='password']");
    private final By loginButton = By.xpath("//button[text()='Acessar']");
    private final By modalText = By.id("modalText");
    private final By closeModalButton = By.id("btnCloseModal");
    private final By logoutButton = By.id("btnExit");


    // Helper class to store account details
    private static class AccountDetails {
        String accountNumber;
        String digit;
        String fullAccount;

        AccountDetails(String message) {
            Pattern pattern = Pattern.compile("(\\d+)-(\\d+)");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                this.accountNumber = matcher.group(1);
                this.digit = matcher.group(2);
                this.fullAccount = this.accountNumber + "-" + this.digit;
            }
        }
    }

    @BeforeAll
    void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);

        // Pre-test setup: Register both users needed for the transfer test
        userAccount = registerUser(USER_EMAIL, USER_NAME, USER_PASSWORD);
        recipientAccount = registerUser(RECIPIENT_EMAIL, RECIPIENT_NAME, RECIPIENT_PASSWORD);
        assertNotNull(userAccount, "Primary user registration must succeed for tests to run.");
        assertNotNull(recipientAccount, "Recipient user registration must succeed for transfer test.");
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

    @Test
    @Order(1)
    void testRegistrationWasSuccessful() {
        // This test implicitly validates the @BeforeAll setup
        assertNotNull(userAccount.fullAccount, "User account should have been created with a valid number.");
        assertNotNull(recipientAccount.fullAccount, "Recipient account should have been created with a valid number.");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        performLogin(USER_EMAIL, "wrong_password");
        String modalContent = getModalTextAndClose();
        assertTrue(modalContent.contains("Usuário ou senha inválido"), "Error message for invalid login should be displayed.");
    }

    @Test
    @Order(3)
    void testSuccessfulLoginAndLogout() {
        performLogin(USER_EMAIL, USER_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/home"));

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textName")));
        assertTrue(welcomeMessage.getText().contains(USER_NAME), "Welcome message should contain the user's name.");

        performLogout();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.findElement(loginButton).isDisplayed(), "Login button should be visible after logout.");
    }

    @Test
    @Order(4)
    void testViewBankStatement() {
        performLogin(USER_EMAIL, USER_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/home"));

        driver.findElement(By.id("btn-EXTRATO")).click();
        wait.until(ExpectedConditions.urlContains("/bank-statement"));
        
        WebElement statementTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textStatement")));
        assertEquals("Extrato", statementTitle.getText(), "Should navigate to the statement page.");

        driver.findElement(By.id("btnBack")).click();
        wait.until(ExpectedConditions.urlContains("/home"));
        assertTrue(driver.findElement(By.id("btn-TRANSFER")).isDisplayed(), "Should return to the home page.");
    }

    @Test
    @Order(5)
    void testFundTransfer() {
        performLogin(USER_EMAIL, USER_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/home"));

        double initialBalance = getBalance();
        double transferAmount = 150.0;
        
        driver.findElement(By.id("btn-TRANSFER")).click();
        wait.until(ExpectedConditions.urlContains("/transfer"));

        // Fill transfer form
        driver.findElement(By.xpath("//input[@placeholder='Informe o número da conta']")).sendKeys(recipientAccount.accountNumber);
        driver.findElement(By.xpath("//input[@placeholder='Informe o dígito da conta']")).sendKeys(recipientAccount.digit);
        driver.findElement(By.xpath("//input[@name='value']")).sendKeys(String.valueOf(transferAmount));
        driver.findElement(By.xpath("//input[@name='description']")).sendKeys("Test Transfer");
        driver.findElement(By.xpath("//button[text()='Transferir agora']")).click();

        String modalContent = getModalTextAndClose();
        assertTrue(modalContent.contains("sucesso"), "Transfer success message should be displayed.");

        wait.until(ExpectedConditions.urlContains("/home"));
        double finalBalance = getBalance();

        assertEquals(initialBalance - transferAmount, finalBalance, 0.01, "Balance should be updated after transfer.");
    }
    
    @Test
    @Order(6)
    void testTransferWithInsufficientFunds() {
        performLogin(USER_EMAIL, USER_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/home"));

        driver.findElement(By.id("btn-TRANSFER")).click();
        wait.until(ExpectedConditions.urlContains("/transfer"));

        // Attempt to transfer more than available balance
        driver.findElement(By.xpath("//input[@placeholder='Informe o número da conta']")).sendKeys(recipientAccount.accountNumber);
        driver.findElement(By.xpath("//input[@placeholder='Informe o dígito da conta']")).sendKeys(recipientAccount.digit);
        driver.findElement(By.xpath("//input[@name='value']")).sendKeys("999999");
        driver.findElement(By.xpath("//button[text()='Transferir agora']")).click();

        String modalContent = getModalTextAndClose();
        assertTrue(modalContent.contains("saldo suficiente"), "Insufficient funds message should be displayed.");
    }

    // --- Helper Methods ---

    private AccountDetails registerUser(String email, String name, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Registrar']"))).click();
        
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//form//input[@type='email']")));
        emailField.sendKeys(email);
        driver.findElement(By.xpath("//form//input[@type='name']")).sendKeys(name);
        driver.findElement(By.xpath("//form//input[@type='password']")).sendKeys(password);
        driver.findElement(By.name("passwordConfirmation")).sendKeys(password);
        driver.findElement(By.id("toggleAddBalance")).click(); // Create account with balance
        driver.findElement(By.xpath("//button[text()='Cadastrar']")).click();
        
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        String text = modal.getText();
        AccountDetails details = new AccountDetails(text);
        
        wait.until(ExpectedConditions.elementToBeClickable(closeModalButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalText));
        return details;
    }

    private void performLogin(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();
    }

    private void performLogout() {
        if (isLoggedIn()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
        }
    }

    private boolean isLoggedIn() {
        try {
            return driver.findElement(logoutButton).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private String getModalTextAndClose() {
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        String text = modal.getText();
        wait.until(ExpectedConditions.elementToBeClickable(closeModalButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalText));
        return text;
    }
    
    private double getBalance() {
        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textBalance")));
        String balanceText = balanceElement.getText().replace("R$ ", "").replace(".", "").replace(",", ".");
        return Double.parseDouble(balanceText);
    }
}