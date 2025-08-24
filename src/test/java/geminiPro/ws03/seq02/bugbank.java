package geminiPRO.ws03.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A complete JUnit 5 test suite for the BugBank application using Selenium WebDriver
 * with Firefox running in headless mode.
 * Note: This application is stateless and requires registration before most actions.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugBankE2ETest {

    // Constants for configuration
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);

    // WebDriver and WebDriverWait instances shared across all tests
    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- WebDriver Lifecycle ---

    @BeforeAll
    static void setup() {
        // As per requirements, initialize Firefox in headless mode via arguments
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        // Start each test from a clean slate at the home/login page
        driver.get(BASE_URL);
    }

    // --- Test Cases ---

    @Test
    @Order(1)
    @DisplayName("Should register a new user successfully")
    void testUserRegistrationSuccessfully() {
        String email = "test" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String password = "password123";

        String accountNumber = registerUser(email, "Test User", password, true);
        Assertions.assertNotNull(accountNumber, "Registration should be successful and return an account number.");

        // After successful registration, the app should be on the login page
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Acessar']")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Should be redirected to the login page after registration.");
    }

    @Test
    @Order(2)
    @DisplayName("Should show an error for invalid login credentials")
    void testLoginWithInvalidCredentials() {
        // Attempt to log in with credentials that have not been registered
        performLogin("nonexistent@user.com", "invalidpass");

        WebElement modalText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        Assertions.assertTrue(modalText.getText().contains("Usuário ou senha inválido"), "Error message for invalid login is not correct.");
    }

    @Test
    @Order(3)
    @DisplayName("Should log in successfully and then log out")
    void testSuccessfulLoginAndLogout() {
        // Step 1: Register a user to ensure a valid login state
        String email = "login" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String password = "password123";
        String name = "Login User";
        registerUser(email, name, password, false);

        // Step 2: Perform login
        performLogin(email, password);

        // Step 3: Verify successful login
        wait.until(ExpectedConditions.urlContains("/home"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textName")));
        Assertions.assertTrue(welcomeMessage.getText().contains(name), "Welcome message should contain the user's name.");

        // Step 4: Perform logout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnExit"))).click();

        // Step 5: Verify successful logout
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        WebElement accessButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[text()='Acessar']")));
        Assertions.assertTrue(accessButton.isDisplayed(), "Should be on the login page after logging out.");
    }

    @Test
    @Order(4)
    @DisplayName("Should perform a fund transfer and verify balance update")
    void testFundTransfer() {
        // Step 1: Register a user with a starting balance
        String email = "transfer" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String password = "password123";
        registerUser(email, "Transfer User", password, true);

        // Step 2: Log in and get initial balance
        performLogin(email, password);
        wait.until(ExpectedConditions.urlContains("/home"));
        double initialBalance = getBalance();
        Assertions.assertEquals(1000.0, initialBalance, "Initial balance should be R$ 1.000,00.");

        // Step 3: Perform transfer
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-TRANSFERÊNCIA"))).click();
        wait.until(ExpectedConditions.urlContains("/transfer"));

        // Fill transfer form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("accountNumber"))).sendKeys("123");
        driver.findElement(By.name("digit")).sendKeys("4");
        driver.findElement(By.name("transferValue")).sendKeys("150.50");
        driver.findElement(By.name("description")).sendKeys("Test Transfer");
        driver.findElement(By.xpath("//button[text()='Transferir agora']")).click();

        // Step 4: Verify transfer success
        WebElement modalText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        Assertions.assertTrue(modalText.getText().contains("Transferência realizada com sucesso"), "Success message for transfer was not found.");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnCloseModal"))).click();

        // Step 5: Go back and verify new balance
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnBack"))).click();
        wait.until(ExpectedConditions.urlContains("/home"));

        double finalBalance = getBalance();
        Assertions.assertEquals(849.50, finalBalance, "Final balance after transfer is incorrect.");
    }

    @Test
    @Order(5)
    @DisplayName("Should perform transfer and verify transaction in statement")
    void testTransferAndVerifyStatement() {
        // Step 1: Register and login
        String email = "statement" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String password = "password123";
        registerUser(email, "Statement User", password, true);
        performLogin(email, password);
        wait.until(ExpectedConditions.urlContains("/home"));

        // Step 2: Perform a unique transfer to easily identify it
        String transferAmount = "250.75";
        String transferDescription = "Payment for services";

        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-TRANSFERÊNCIA"))).click();
        wait.until(ExpectedConditions.urlContains("/transfer"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("accountNumber"))).sendKeys("456");
        driver.findElement(By.name("digit")).sendKeys("7");
        driver.findElement(By.name("transferValue")).sendKeys(transferAmount);
        driver.findElement(By.name("description")).sendKeys(transferDescription);
        driver.findElement(By.xpath("//button[text()='Transferir agora']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText"))); // Wait for modal
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnCloseModal"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnBack"))).click(); // Go back to home

        // Step 3: Navigate to statement and verify the transaction
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-EXTRATO"))).click();
        wait.until(ExpectedConditions.urlContains("/bank-statement"));

        WebElement descriptionElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[text()='" + transferDescription + "']")));
        WebElement valueElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(), '-R$ " + transferAmount.replace('.', ',') + "')]")));

        Assertions.assertAll("Transaction details in statement should be correct",
                () -> Assertions.assertEquals(transferDescription, descriptionElement.getText(), "Transaction description does not match."),
                () -> Assertions.assertTrue(valueElement.isDisplayed(), "Transaction value does not match.")
        );
    }


    // --- Helper Methods ---

    /**
     * Registers a new user. This is necessary for almost all tests as the app is stateless.
     * @param email The email to register.
     * @param name The name of the user.
     * @param password The user's password.
     * @param withBalance If true, creates the account with a starting balance.
     * @return The account number string (e.g., "123-4") upon success, or null on failure.
     */
    private String registerUser(String email, String name, String password, boolean withBalance) {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Registrar']"))).click();
        wait.until(ExpectedConditions.urlContains("/register"));

        // Fill out the registration form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='email']"))).sendKeys(email);
        driver.findElement(By.xpath("//div[label[text()='Nome']]/input")).sendKeys(name);
        driver.findElement(By.xpath("(//input[@type='password'])[1]")).sendKeys(password);
        driver.findElement(By.xpath("(//input[@type='password'])[2]")).sendKeys(password);

        if (withBalance) {
            driver.findElement(By.id("toggleAddBalance")).click();
        }

        driver.findElement(By.xpath("//button[text()='Cadastrar']")).click();

        WebElement modalTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modalText")));
        String modalText = modalTextElement.getText();

        // Extract account number from the success message
        Pattern pattern = Pattern.compile("(\\d+-\\d+)");
        Matcher matcher = pattern.matcher(modalText);
        String accountNumber = null;
        if (matcher.find()) {
            accountNumber = matcher.group(1);
        }

        // Close the modal to complete the process
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnCloseModal"))).click();

        return accountNumber;
    }

    /**
     * Performs a login action.
     * @param email The user's email.
     * @param password The user's password.
     */
    private void performLogin(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[label[text()='Email']]/input"))).sendKeys(email);
        driver.findElement(By.xpath("//div[label[text()='Senha']]/input")).sendKeys(password);
        driver.findElement(By.xpath("//button[text()='Acessar']")).click();
    }

    /**
     * Retrieves the current balance from the home screen.
     * @return The balance as a double.
     */
    private double getBalance() {
        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textBalance")));
        String balanceText = balanceElement.getText().replace("R$ ", "").replace(".", "").replace(",", ".");
        return Double.parseDouble(balanceText);
    }
}