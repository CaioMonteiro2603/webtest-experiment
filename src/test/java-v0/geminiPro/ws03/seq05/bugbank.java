package geminiPro.ws03.seq05;

import org.junit.jupiter.api.*;
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
 * JUnit 5 test suite for bugbank.netlify.app using Selenium WebDriver with headless Firefox.
 * This suite covers user registration, login, fund transfers, and statement verification.
 * It creates a new, unique user for each test suite run to ensure independence and a clean state.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // A unique user is generated during the registration test to ensure a clean state.
    private static String testEmail;
    private static String testName;
    private static String testPassword;
    private static String testAccountNumber;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final double INITIAL_BALANCE = 1000.00;

    // Locators
    private final By emailInput = By.xpath("//div[input[@type='email']]/input");
    private final By passwordInput = By.xpath("//div[input[@type='password']]/input");
    private final By nameInput = By.xpath("//div[input[@type='name']]/input");
    private final By modalText = By.id("modalText");
    private final By closeModalButton = By.id("btnCloseModal");
    private final By loginButton = By.xpath("//button[normalize-space()='Acessar']");
    private final By registerButton = By.xpath("//button[normalize-space()='Registrar']");
    private final By submitRegistrationButton = By.xpath("//button[normalize-space()='Cadastrar']");

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Generate unique credentials for this test run
        long timestamp = System.currentTimeMillis();
        testEmail = "bugbank.user." + timestamp + "@test.com";
        testName = "Gemini User";
        testPassword = "password123";
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

    /**
     * Helper method to log in using the dynamically generated test credentials.
     */
    private void performLogin() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(testEmail);
        driver.findElement(passwordInput).sendKeys(testPassword);
        driver.findElement(loginButton).click();
        wait.until(ExpectedConditions.urlContains("/home"));
    }
    
    /**
     * Helper method to parse currency strings like "R$ 1.000,00" into a Double.
     * @param amount The string amount to parse.
     * @return The amount as a Double.
     */
    private double parseCurrency(String amount) {
        return Double.parseDouble(
            amount.replace("R$", "")
                  .trim()
                  .replace(".", "")
                  .replace(",", ".")
        );
    }

    @Test
    @Order(1)
    @DisplayName("Should register a new user with an initial balance")
    void testUserRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(registerButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(submitRegistrationButton));

        // Fill out registration form
        driver.findElement(emailInput).sendKeys(testEmail);
        driver.findElement(nameInput).sendKeys(testName);
        driver.findElement(passwordInput).sendKeys(testPassword);
        driver.findElement(By.xpath("//div[input[@type='password']]/following-sibling::input")).sendKeys(testPassword); // Password confirmation

        // Toggle the 'add balance' switch
        driver.findElement(By.id("toggleAddBalance")).click();
        
        driver.findElement(submitRegistrationButton).click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        String successMessage = modal.getText();
        
        // Extract account number from the success message "A conta XXX-X foi criada com sucesso"
        Pattern pattern = Pattern.compile("(\\d+-\\d+)");
        Matcher matcher = pattern.matcher(successMessage);
        Assertions.assertTrue(matcher.find(), "Could not find account number in success message.");
        testAccountNumber = matcher.group(1);

        Assertions.assertTrue(successMessage.contains("foi criada com sucesso"), "Registration success message is incorrect.");
        
        driver.findElement(closeModalButton).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL)); // Should be redirected to login
    }

    @Test
    @Order(2)
    @DisplayName("Should show an error for invalid login credentials")
    void testInvalidLogin() {
        // This test assumes registration was successful and testEmail is populated.
        Assumptions.assumeTrue(testEmail != null, "User registration must succeed for this test to run.");
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(testEmail);
        driver.findElement(passwordInput).sendKeys("wrongpassword");
        driver.findElement(loginButton).click();
        
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        Assertions.assertTrue(modal.getText().matches("Usuário ou senha inválido\\.?"), "Error message for wrong password is not correct.");
        
        driver.findElement(closeModalButton).click();
        
        // Wait for modal to be gone before next action
        wait.until(ExpectedConditions.invisibilityOf(modal));

        driver.findElement(emailInput).clear();
        driver.findElement(emailInput).sendKeys("nonexistent@user.com");
        driver.findElement(passwordInput).clear();
        driver.findElement(passwordInput).sendKeys(testPassword);
        driver.findElement(loginButton).click();

        modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        Assertions.assertTrue(modal.getText().matches("Usuário ou senha inválido\\.?"), "Error message for non-existent user is not correct.");
        driver.findElement(closeModalButton).click();
    }
    
    @Test
    @Order(3)
    @DisplayName("Should log in successfully and then log out")
    void testSuccessfulLoginAndLogout() {
        Assumptions.assumeTrue(testEmail != null, "User registration must succeed for this test to run.");
        performLogin();

        WebElement welcomeText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textName")));
        Assertions.assertTrue(welcomeText.getText().contains(testName), "Welcome message does not contain the correct user name.");
        
        WebElement accountNumberText = driver.findElement(By.id("textAccountNumber"));
        Assertions.assertTrue(accountNumberText.getText().contains(testAccountNumber), "Displayed account number is incorrect.");

        WebElement balanceText = driver.findElement(By.id("textBalance"));
        double currentBalance = parseCurrency(balanceText.getText().split(" ")[1]);
        Assertions.assertEquals(INITIAL_BALANCE, currentBalance, "Initial balance is incorrect.");
        
        driver.findElement(By.id("btn-SAIR")).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertTrue(driver.findElement(loginButton).isDisplayed(), "Should be on login page after logout.");
    }
    
    @Test
    @Order(4)
    @DisplayName("Should perform a fund transfer and verify the balance update")
    void testFundTransferAndVerifyBalance() {
        Assumptions.assumeTrue(testEmail != null, "User registration must succeed for this test to run.");
        performLogin();

        double initialBalance = parseCurrency(wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[@id='textBalance']/span"))).getText());
        double transferAmount = 150.75;
        
        driver.findElement(By.id("btn-TRANSFERENCIA")).click();
        wait.until(ExpectedConditions.urlContains("/transfer"));
        
        // Fill transfer form
        driver.findElement(By.xpath("//input[@name='accountNumber']")).sendKeys("123");
        driver.findElement(By.xpath("//input[@name='digit']")).sendKeys("4");
        driver.findElement(By.xpath("//input[@name='transferValue']")).sendKeys(String.valueOf(transferAmount));
        driver.findElement(By.xpath("//input[@name='description']")).sendKeys("Test Transfer");
        
        driver.findElement(By.xpath("//button[normalize-space()='Transferir agora']")).click();
        
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        Assertions.assertEquals("Transferencia realizada com sucesso", modal.getText());
        driver.findElement(closeModalButton).click();
        
        // Go back and verify balance
        wait.until(ExpectedConditions.invisibilityOf(modal));
        driver.findElement(By.id("btnBack")).click();
        wait.until(ExpectedConditions.urlContains("/home"));

        WebElement updatedBalanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[@id='textBalance']/span")));
        double finalBalance = parseCurrency(updatedBalanceElement.getText());

        Assertions.assertEquals(initialBalance - transferAmount, finalBalance, 0.01, "The final balance after transfer is incorrect.");
    }

    @Test
    @Order(5)
    @DisplayName("Should verify the transaction on the statement page")
    void testStatementAfterTransfer() {
        Assumptions.assumeTrue(testEmail != null, "User registration must succeed for this test to run.");
        // This test relies on the transfer from the previous test.
        performLogin();
        
        driver.findElement(By.id("btn-EXTRATO")).click();
        wait.until(ExpectedConditions.urlContains("/bank-statement"));

        // Wait for transaction list to be populated. The list is inside a div with id 'listaExtrato'.
        WebElement transactionList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("listaExtrato")));
        
        WebElement description = transactionList.findElement(By.xpath("//p[text()='Test Transfer']"));
        WebElement value = description.findElement(By.xpath("./following-sibling::p"));

        Assertions.assertNotNull(description, "Transaction description 'Test Transfer' not found in statement.");
        Assertions.assertEquals("- R$ 150,75", value.getText(), "Transaction value is incorrect in the statement.");
    }
}