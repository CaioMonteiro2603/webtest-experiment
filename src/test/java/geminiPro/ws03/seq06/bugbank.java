package geminiPro.ws03.seq06;

import org.junit.jupiter.api.AfterAll;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for bugbank.netlify.app using Selenium WebDriver with headless Firefox.
 * This suite covers user registration, login, fund transfers, statement verification, and logout.
 * A primary user is created and its state is used across ordered tests.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15); // Increased for this React app

    // Static variables to hold state between ordered tests
    private static String primaryUserEmail;
    private static String primaryUserPassword = "password123";
    private static String primaryUserName = "Gemini Primary";
    private static String primaryUserAccountNumber; // Format: "123-4"
    private static double initialBalance = 1000.0;

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private static final By REGISTER_BUTTON_HOME = By.xpath("//button[text()='Registrar']");
    private static final By LOGIN_EMAIL_INPUT = By.xpath("//div[not(contains(@class, 'login__modal'))]//input[@type='email']");
    private static final By LOGIN_PASSWORD_INPUT = By.xpath("//div[not(contains(@class, 'login__modal'))]//input[@type='password']");
    private static final By LOGIN_ACCESS_BUTTON = By.xpath("//button[text()='Acessar']");
    private static final By MODAL_TEXT = By.id("modalText");
    private static final By MODAL_CLOSE_BUTTON = By.id("btnCloseModal");
    private static final By HOME_PAGE_BALANCE = By.id("textBalance");
    private static final By LOGOUT_BUTTON = By.id("btnExit");

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        primaryUserEmail = "primary.user." + System.currentTimeMillis() + "@gemini.test";
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Registers a new primary user with a starting balance. This user will be used in subsequent tests.
     */
    @Test
    @Order(1)
    void userRegistrationTest() {
        driver.get(BASE_URL);
        primaryUserAccountNumber = registerNewUser(primaryUserEmail, primaryUserName, primaryUserPassword, true);
        assertNotNull(primaryUserAccountNumber, "Registration failed or account number was not retrieved.");
        assertTrue(primaryUserAccountNumber.matches("\\d+-\\d"), "Account number format is incorrect.");
    }

    /**
     * Tests both failed and successful login scenarios using the registered primary user.
     */
    @Test
    @Order(2)
    void loginFunctionalityTest() {
        driver.get(BASE_URL);

        // Test Case 1: Failed Login (Wrong Password)
        performLogin(primaryUserEmail, "wrongpassword");
        WebElement errorModal = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        assertTrue(errorModal.getText().contains("Usuário ou senha inválido"), "Error message for invalid login is incorrect.");
        wait.until(ExpectedConditions.elementToBeClickable(MODAL_CLOSE_BUTTON)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_TEXT)); // Wait for modal to disappear

        // Test Case 2: Successful Login
        performLogin(primaryUserEmail, primaryUserPassword);
        wait.until(ExpectedConditions.urlContains("/home"));
        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(HOME_PAGE_BALANCE));
        String expectedBalance = String.format("R$ %.2f", initialBalance).replace(".", ",");
        assertTrue(balanceElement.getText().contains(expectedBalance), "Balance on home page is not correct after login.");
    }

    /**
     * Tests the fund transfer functionality by creating a second user and transferring funds to them.
     */
    @Test
    @Order(3)
    void fundTransferTest() {
        // Step 1: Create a secondary user to receive funds
        String secondaryUserEmail = "secondary.user." + System.currentTimeMillis() + "@gemini.test";
        String secondaryAccountNumber = registerNewUser(secondaryUserEmail, "Gemini Secondary", "password456", false);
        assertNotNull(secondaryAccountNumber, "Registration for secondary user failed.");

        // Step 2: Log in as the primary user
        performLogin(primaryUserEmail, primaryUserPassword);
        wait.until(ExpectedConditions.urlContains("/home"));
        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(HOME_PAGE_BALANCE));
        double balanceBefore = parseBalance(balanceElement.getText());

        // Step 3: Perform the transfer
        double transferAmount = 150.55;
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-TRANSFERÊNCIA"))).click();

        // Fill transfer form
        String[] accountParts = secondaryAccountNumber.split("-");
        WebElement transferModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("transfer__modal")));
        transferModal.findElement(By.name("accountNumber")).sendKeys(accountParts[0]);
        transferModal.findElement(By.name("digit")).sendKeys(accountParts[1]);
        transferModal.findElement(By.name("value")).sendKeys(String.valueOf(transferAmount).replace(".", ","));
        transferModal.findElement(By.name("description")).sendKeys("Test Transfer");
        
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Transferir agora']"))).click();

        // Step 4: Assert success and updated balance
        WebElement successModal = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        assertTrue(successModal.getText().contains("sucesso"), "Transfer success message not found.");
        wait.until(ExpectedConditions.elementToBeClickable(MODAL_CLOSE_BUTTON)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_TEXT));

        // Re-fetch balance element after DOM update
        WebElement balanceElementAfter = driver.findElement(HOME_PAGE_BALANCE);
        double balanceAfter = parseBalance(balanceElementAfter.getText());
        assertEquals(balanceBefore - transferAmount, balanceAfter, 0.01, "Balance did not update correctly after transfer.");
    }

    /**
     * Verifies that the transaction from the previous test appears correctly on the account statement page.
     */
    @Test
    @Order(4)
    void statementVerificationTest() {
        performLogin(primaryUserEmail, primaryUserPassword);
        wait.until(ExpectedConditions.urlContains("/home"));

        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-EXTRATO"))).click();
        wait.until(ExpectedConditions.urlContains("/extrato"));
        
        WebElement statementContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("statement")));

        // Find the specific transaction based on description and value
        WebElement descriptionElement = statementContainer.findElement(By.xpath("//p[text()='Test Transfer']"));
        WebElement valueElement = descriptionElement.findElement(By.xpath("./following-sibling::p"));

        assertNotNull(descriptionElement, "Transaction description 'Test Transfer' not found on statement.");
        assertEquals("- R$ 150,55", valueElement.getText(), "Transaction amount on statement is incorrect.");
    }

    /**
     * Tests the logout functionality.
     */
    @Test
    @Order(5)
    void logoutTest() {
        performLogin(primaryUserEmail, primaryUserPassword);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGOUT_BUTTON));
        
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
        
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.findElement(LOGIN_ACCESS_BUTTON).isDisplayed(), "Login button should be visible after logout.");
    }

    // --- Helper Methods ---

    /**
     * Registers a new user and extracts their account number from the success modal.
     *
     * @param email The email for the new user.
     * @param name The name of the new user.
     * @param password The password for the new user.
     * @param withBalance True to create the account with a starting balance.
     * @return The account number as a string (e.g., "123-4"), or null on failure.
     */
    private String registerNewUser(String email, String name, String password, boolean withBalance) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(REGISTER_BUTTON_HOME)).click();

        WebElement registrationModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("login__modal")));
        registrationModal.findElement(By.xpath(".//input[@type='email']")).sendKeys(email);
        registrationModal.findElement(By.xpath(".//input[@placeholder='Informe seu Nome']")).sendKeys(name);
        registrationModal.findElement(By.xpath(".//div[contains(@class, 'password__input')][1]/input")).sendKeys(password);
        registrationModal.findElement(By.xpath(".//div[contains(@class, 'password__input')][2]/input")).sendKeys(password);

        if (withBalance) {
            registrationModal.findElement(By.id("toggleAddBalance")).click();
        }

        registrationModal.findElement(By.xpath(".//button[text()='Cadastrar']")).click();

        WebElement successModal = wait.until(ExpectedConditions.visibilityOfElementLocated(MODAL_TEXT));
        String modalText = successModal.getText();
        String accountNumber = extractAccountNumber(modalText);
        
        wait.until(ExpectedConditions.elementToBeClickable(MODAL_CLOSE_BUTTON)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(MODAL_TEXT));
        
        return accountNumber;
    }
    
    /**
     * Fills the login form and clicks the access button.
     *
     * @param email The user's email.
     * @param password The user's password.
     */
    private void performLogin(String email, String password) {
        if (email == null) {
            fail("Cannot log in; user email is null, registration might have failed.");
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_EMAIL_INPUT)).clear();
        driver.findElement(LOGIN_EMAIL_INPUT).sendKeys(email);
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_PASSWORD_INPUT)).clear();
        driver.findElement(LOGIN_PASSWORD_INPUT).sendKeys(password);
        
        wait.until(ExpectedConditions.elementToBeClickable(LOGIN_ACCESS_BUTTON)).click();
    }
    
    /**
     * Extracts an account number (e.g., "123-4") from a string using regex.
     *
     * @param text The text containing the account number.
     * @return The account number string, or null if not found.
     */
    private String extractAccountNumber(String text) {
        Pattern pattern = Pattern.compile("(\\d+-\\d)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Parses a balance string like "R$ 1.000,00" into a double.
     *
     * @param balanceText The text to parse.
     * @return The balance as a double.
     */
    private double parseBalance(String balanceText) {
        return Double.parseDouble(
            balanceText.replaceAll("[^\\d,]", "")
                       .replace(".", "")
                       .replace(",", ".")
        );
    }
}