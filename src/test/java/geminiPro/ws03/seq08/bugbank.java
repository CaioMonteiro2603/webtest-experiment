package geminiPro.ws03.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final Duration TIMEOUT = Duration.ofSeconds(15); // Increased timeout for this app

    // User credentials and info will be generated/captured during the tests
    private static String userEmail;
    private static String userName;
    private static String userPassword;
    private static String userAccountNumber;
    private static String userAccountDigit;
    private static final String INITIAL_BALANCE = "R$ 1.000,00";

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private final By registerButtonHome = By.xpath("//button[contains(., 'Registrar')]");
    private final By emailInput = By.xpath("//div[label[text()='Email']]/input[@name='email']");
    private final By passwordInput = By.xpath("//div[label[text()='Senha']]/input[@name='password']");
    private final By loginButton = By.xpath("//button[contains(., 'Acessar')]");
    private final By modalText = By.id("modalText");
    private final By closeModalButton = By.id("btnCloseModal");
    private final By logoutButton = By.id("btnExit");
    private final By transferButton = By.id("btn-TRANSFERÊNCIA");
    private final By statementButton = By.id("btn-EXTRATO");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, TIMEOUT);

        long timestamp = System.currentTimeMillis();
        userEmail = "testuser" + timestamp + "@test.com";
        userName = "Test User " + timestamp;
        userPassword = "password123";
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String email, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();
    }

    @Test
    @Order(1)
    @DisplayName("Test New User Registration with Balance")
    void testUserRegistration() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(registerButtonHome)).click();

        wait.until(ExpectedConditions.urlContains("/register"));
        
        // Registration form locators
        By regEmailInput = By.xpath("//div[label[text()='Email']]/input");
        By regNameInput = By.xpath("//div[label[text()='Nome']]/input");
        By regPasswordInput = By.xpath("//div[label[text()='Senha']]/input");
        By regPassConfirmInput = By.xpath("//div[label[text()='Confirmação da senha']]/input");
        By balanceToggle = By.id("toggleAddBalance");
        By registerSubmitButton = By.xpath("//button[contains(., 'Cadastrar')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(regEmailInput)).sendKeys(userEmail);
        driver.findElement(regNameInput).sendKeys(userName);
        driver.findElement(regPasswordInput).sendKeys(userPassword);
        driver.findElement(regPassConfirmInput).sendKeys(userPassword);
        driver.findElement(balanceToggle).click();
        driver.findElement(registerSubmitButton).click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        String successMessage = modal.getText();
        
        assertTrue(successMessage.startsWith("A conta"), "Success modal should show account creation message.");

        // Extract account number and digit from the success message
        Pattern pattern = Pattern.compile("(\\d+)-(\\d)");
        Matcher matcher = pattern.matcher(successMessage);
        if (matcher.find()) {
            userAccountNumber = matcher.group(1);
            userAccountDigit = matcher.group(2);
        }
        
        assertNotNull(userAccountNumber, "Should have extracted account number.");
        assertNotNull(userAccountDigit, "Should have extracted account digit.");
        
        driver.findElement(closeModalButton).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(closeModalButton));
    }

    @Test
    @Order(2)
    @DisplayName("Test Login with Invalid Credentials")
    void testLoginWithInvalidCredentials() {
        login("invalid@email.com", "wrongpassword");
        
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        assertTrue(
            modal.getText().contains("Usuário ou senha inválido"), 
            "Error message for invalid credentials should be displayed."
        );

        driver.findElement(closeModalButton).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(closeModalButton));
    }

    @Test
    @Order(3)
    @DisplayName("Test Successful Login and Logout")
    void testSuccessfulLoginAndLogout() {
        assertNotNull(userEmail, "User must be registered before login test.");
        login(userEmail, userPassword);

        wait.until(ExpectedConditions.urlContains("/home"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textName")));
        assertTrue(welcomeMessage.getText().contains(userName), "Welcome message should contain the user's name.");

        WebElement balance = driver.findElement(By.id("textBalance"));
        assertTrue(balance.getText().contains(INITIAL_BALANCE), "Initial balance should be correct.");
        
        driver.findElement(logoutButton).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.findElement(loginButton).isDisplayed(), "Should be returned to the login page after logout.");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test Fund Transfer and Balance Update")
    void testFundTransfer() {
        assertNotNull(userEmail, "User must be registered before transfer test.");
        login(userEmail, userPassword);
        wait.until(ExpectedConditions.urlContains("/home"));

        driver.findElement(transferButton).click();
        wait.until(ExpectedConditions.urlContains("/transfer"));
        
        String transferValue = "150.50";
        String expectedNewBalance = "R$ 849,50";

        // Transfer form locators
        By accountNumberInput = By.name("accountNumber");
        By digitInput = By.name("digit");
        By transValueInput = By.name("transferValue");
        By descriptionInput = By.name("description");
        By submitTransferButton = By.xpath("//button[contains(.,'Transferir agora')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(accountNumberInput)).sendKeys("222");
        driver.findElement(digitInput).sendKeys("2");
        driver.findElement(transValueInput).sendKeys(transferValue);
        driver.findElement(descriptionInput).sendKeys("Test Transfer");
        driver.findElement(submitTransferButton).click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalText));
        assertEquals("Transferencia realizada com sucesso", modal.getText(), "Success message for transfer should be displayed.");
        driver.findElement(closeModalButton).click();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-BACK"))).click();
        wait.until(ExpectedConditions.urlContains("/home"));

        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textBalance")));
        assertEquals(expectedNewBalance, balanceElement.getText(), "Balance should be updated after transfer.");
        
        driver.findElement(logoutButton).click();
    }

    @Test
    @Order(5)
    @DisplayName("Test Transaction Appears in Statement")
    void testTransactionInStatement() {
        assertNotNull(userEmail, "User must be registered before statement test.");
        login(userEmail, userPassword);
        wait.until(ExpectedConditions.urlContains("/home"));
        
        driver.findElement(statementButton).click();
        wait.until(ExpectedConditions.urlContains("/statement"));
        
        WebElement statementTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("textStatement")));
        assertEquals("Extrato", statementTitle.getText(), "Should be on the Statement page.");
        
        By transactionDescription = By.id("textTransferValue");
        By transactionValue = By.id("textTransferReal");

        List<WebElement> descriptions = driver.findElements(transactionDescription);
        List<WebElement> values = driver.findElements(transactionValue);

        boolean transferFound = false;
        for (int i = 0; i < descriptions.size(); i++) {
            if (descriptions.get(i).getText().contains("Transferência enviada")) {
                if (values.get(i).getText().contains("-R$ 150,50")) {
                    transferFound = true;
                    break;
                }
            }
        }
        
        assertTrue(transferFound, "The transfer of R$ 150,50 should be listed in the statement.");

        driver.findElement(logoutButton).click();
    }
}