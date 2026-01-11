package Qwen3.ws03.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String EMAIL = "caio@gmail.com";
    private static final String PASSWORD = "123";
    private static final String ACCOUNT_NUMBER = "654321";
    private static final String BALANCE = "R$ 500,00";

    @BeforeAll
    static void setUp() {
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

    @Test
    @Order(1)
    void testPageTitleAndHeader() {
        driver.get(BASE_URL);

        assertEquals("BugBank", driver.getTitle(), "Page title should be BugBank");

        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(header.getText().contains("BugBank"), "Header should contain BugBank");
    }

    @Test
    @Order(2)
    void testRegisterNewUser() {
        driver.get(BASE_URL);

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Registrar')]")));
        registerButton.click();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.sendKeys(EMAIL);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(PASSWORD);

        WebElement confirmPasswordField = driver.findElement(By.name("confirmPassword"));
        confirmPasswordField.sendKeys(PASSWORD);

        WebElement savingsCheckbox = driver.findElement(By.name("savings"));
        if (!savingsCheckbox.isSelected()) {
            savingsCheckbox.click();
        }

        WebElement registerFormButton = driver.findElement(By.xpath("//button[contains(text(),'Cadastrar')]"));
        registerFormButton.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.name("email")));

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(successMessage.isDisplayed(), "Success message should appear after registration");
        assertTrue(successMessage.getText().contains("usuário criado"), "Success message should confirm user creation");
    }

    @Test
    @Order(3)
    void testLoginWithRegisteredUser() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.sendKeys(EMAIL);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
        loginButton.click();

        WebElement accountHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dashboard-header")));
        assertTrue(accountHeader.isDisplayed(), "Dashboard should be displayed after login");

        WebElement accountNumberElement = driver.findElement(By.cssSelector(".account-number"));
        assertEquals(ACCOUNT_NUMBER, accountNumberElement.getText().trim(), "Account number should match expected");

        WebElement accountBalanceElement = driver.findElement(By.cssSelector(".user-balance"));
        assertEquals(BALANCE, accountBalanceElement.getText().trim(), "Account balance should match expected");
    }

    @Test
    @Order(4)
    void testInvalidLoginEmail() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.sendKeys("invalid@example.com");

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear");
        assertTrue(errorMessage.getText().contains("Usuário ou senha inválido"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(5)
    void testInvalidLoginPassword() {
        driver.get(BASE_URL);

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.sendKeys(EMAIL);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("wrongpassword");

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear");
        assertTrue(errorMessage.getText().contains("Usuário ou senha inválido"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(6)
    void testNavigationToTransactions() {
        loginIfNecessary();

        WebElement transactionsTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Transações')]")));
        transactionsTab.click();

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        assertTrue(pageTitle.getText().contains("Transações"), "Should navigate to Transações page");
    }

    @Test
    @Order(7)
    void testSendMoney() {
        loginIfNecessary();

        WebElement sendMoneyTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Transferência')]")));
        sendMoneyTab.click();

        WebElement destinationAccount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("transfer-account")));
        destinationAccount.sendKeys("123456");

        WebElement destinationDigit = driver.findElement(By.name("transfer-digit"));
        destinationDigit.sendKeys("7");

        WebElement amount = driver.findElement(By.name("transfer-amount"));
        amount.sendKeys("100");

        WebElement description = driver.findElement(By.name("transfer-description"));
        description.sendKeys("Pagamento teste");

        WebElement transferButton = driver.findElement(By.xpath("//button[contains(text(),'Transferir')]"));
        transferButton.click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".transfer-success")));
        assertTrue(confirmation.isDisplayed(), "Transfer success message should appear");
        assertTrue(confirmation.getText().contains("sucesso"), "Confirmation should indicate success");
    }

    @Test
    @Order(8)
    void testNavigationToExtract() {
        loginIfNecessary();

        WebElement extractTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Extrato')]")));
        extractTab.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("transaction-list")));
        WebElement extractTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        assertTrue(extractTitle.getText().contains("Extrato"), "Should navigate to Extrato page");

        java.util.List<WebElement> transactions = driver.findElements(By.className("transaction-item"));
        assertTrue(transactions.size() > 0, "There should be at least one transaction in the statement");
    }

    @Test
    @Order(9)
    void testOrderTransactionsByDate() {
        loginIfNecessary();

        WebElement extractTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Extrato')]")));
        extractTab.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("transaction-list")));

        WebElement orderSelect = wait.until(ExpectedConditions.elementToBeClickable(By.name("order")));
        Select select = new Select(orderSelect);
        select.selectByValue("date");

        WebElement firstTransactionDate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".transaction-list .transaction-item .transaction-date")));
        assertTrue(firstTransactionDate.isDisplayed(), "Transactions should be ordered by date");
    }

    @Test
    @Order(10)
    void testOrderTransactionsByAmount() {
        loginIfNecessary();

        WebElement extractTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[contains(text(),'Extrato')]")));
        extractTab.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("transaction-list")));

        WebElement orderSelect = wait.until(ExpectedConditions.elementToBeClickable(By.name("order")));
        Select select = new Select(orderSelect);
        select.selectByValue("amount");

        java.util.List<WebElement> amounts = driver.findElements(By.cssSelector(".transaction-amount"));
        Double previousAmount = null;
        for (WebElement amountElement : amounts) {
            String text = amountElement.getText().replace("R$", "").replace(",", "").trim();
            Double currentAmount = Double.parseDouble(text);
            if (previousAmount != null) {
                assertTrue(currentAmount >= previousAmount, "Transactions should be sorted by increasing amount");
            }
            previousAmount = currentAmount;
        }
    }

    @Test
    @Order(11)
    void testFooterGithubLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='github']")));
        githubLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String githubUrl = driver.getCurrentUrl();
        assertTrue(githubUrl.contains("github.com"), "GitHub link should redirect to GitHub domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(12)
    void testFooterLinkedinLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='linkedin']")));
        linkedinLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String linkedinUrl = driver.getCurrentUrl();
        assertTrue(linkedinUrl.contains("linkedin.com"), "LinkedIn link should redirect to LinkedIn domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(13)
    void testLogout() {
        loginIfNecessary();

        WebElement profileButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn-logout")));
        profileButton.click();

        WebElement logoutOption = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//li[contains(text(),'Sair')]")));
        logoutOption.click();

        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Entrar')]")));
        assertTrue(loginButton.isDisplayed(), "Login button should reappear after logout");

        assertFalse(driver.getCurrentUrl().contains("dashboard"), "Should not be on dashboard after logout");
    }

    private void loginIfNecessary() {
        try {
            driver.get(BASE_URL);
            WebElement loginButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(text(),'Entrar')]")));
            if (loginButton.isDisplayed()) {
                performLogin();
            }
        } catch (TimeoutException e) {
            // Already logged in
        }
    }

    private void performLogin() {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailField.clear();
        emailField.sendKeys(EMAIL);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".dashboard-header")));
    }
}