package Qwen3.ws03.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class bugbank {
    private static WebDriver driver;
    private static WebDriverWait wait;

    private final String BASE_URL = "https://bugbank.netlify.app/";
    private final String LOGIN = "caio@gmail.com";
    private final String PASSWORD = "123";

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
    void testValidLogin_SuccessfulRedirectToDashboard() {
        driver.get(BASE_URL);

        // Switch to login tab if needed
        if (isElementPresent(By.xpath("//span[text()='Registrar']"))) {
            WebElement loginTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Entrar']")));
            loginTab.click();
        }

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[inputmode='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        // Wait for account balance to appear (logged-in state)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".css-1i29wca")));
        String accountText = driver.findElement(By.cssSelector(".css-1i29wca")).getText();
        assertTrue(accountText.contains("Bem vindo"), "Welcome message should appear after login");
        assertTrue(accountText.contains(LOGIN), "Welcome message should include user email");
    }

    @Test
    @Order(2)
    void testInvalidLogin_ErrorMessageDisplayed() {
        driver.get(BASE_URL);

        if (isElementPresent(By.xpath("//span[text()='Registrar']"))) {
            WebElement loginTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Entrar']")));
            loginTab.click();
        }

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[inputmode='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));

        emailField.sendKeys("invalid@invalid.com");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        // Error appears in toast
        WebElement errorToast = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Toastify__toast--error")));
        assertTrue(errorToast.isDisplayed(), "Error toast should be visible");
        assertTrue(errorToast.getText().contains("Usuário ou senha inválido"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testRegisterNewUser_ValidData_SuccessfulRedirect() {
        driver.get(BASE_URL);

        WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Registrar']")));
        registerTab.click();

        String newEmail = "testuser_" + System.currentTimeMillis() + "@example.com";
        String testName = "Test User";
        String testPass = "password123";

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[inputmode='email']")));
        WebElement nameField = driver.findElement(By.cssSelector("input[type='text']:not([inputmode])"));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement confirmPassField = driver.findElements(By.cssSelector("input[type='password']")).get(1);
        WebElement saveButton = driver.findElement(By.xpath("//button[contains(text(), 'Cadastrar')]"));

        emailField.sendKeys(newEmail);
        nameField.sendKeys(testName);
        passwordField.sendKeys(testPass);
        confirmPassField.sendKeys(testPass);
        
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Cadastrar']")));
        submitButton.click();

        // Success toast
        WebElement successToast = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Toastify__toast--success")));
        assertTrue(successToast.isDisplayed(), "Success toast should appear after registration");
        assertTrue(successToast.getText().contains("sucesso"), "Success message should confirm registration");
    }

    @Test
    @Order(4)
    void testLoginPageHeaderLogo_NavigatesToHome() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[alt*='Bug']/parent::button")));
        logo.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Clicking logo should return to home URL");
    }

    @Test
    @Order(5)
    void testFooterGitHubLink_OpenExternalInNewTab() {
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github']")));
        String originalWindow = driver.getWindowHandle();
        githubLink.sendKeys(Keys.CONTROL, Keys.RETURN); // Open in new tab

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("github.com"));
                assertTrue(driver.getCurrentUrl().contains("github.com"), "GitHub link should open github.com domain");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback to same tab
        driver.switchTo().window(originalWindow);
        githubLink.click();
        wait.until(ExpectedConditions.urlContains("github.com"));
        assertTrue(driver.getCurrentUrl().contains("github.com"), "GitHub link should redirect to github.com");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(6)
    void testFooterLinkedinLink_OpenExternalInNewTab() {
        driver.get(BASE_URL);
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin']")));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.sendKeys(Keys.CONTROL, Keys.RETURN);

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("linkedin.com"));
                assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open linkedin.com domain");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback
        driver.switchTo().window(originalWindow);
        linkedinLink.click();
        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should redirect");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(7)
    void testAccountBalanceVisibleAfterLogin() {
        loginIfNotAlready();

        WebElement balanceLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(), 'Saldo')]/following-sibling::p")));
        assertTrue(balanceLabel.isDisplayed(), "Balance amount should be visible");
        assertTrue(balanceLabel.getText().matches("R\\$\\s?\\d+(,\\d{2})?"), "Balance should be in currency format");
    }

    @Test
    @Order(8)
    void testTransactionHistory_EmptyInitially() {
        loginIfNotAlready();

        WebElement historySection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".css-0")));
        String historyText = historySection.getText();
        assertTrue(historyText.contains("sem transações") || historyText.contains("Nenhuma transação"), "Transaction history should be empty initially");
    }

    @Test
    @Order(9)
    void testTransferButton_NavigatesToTransferForm() {
        loginIfNotAlready();

        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='transfer-button']")));
        transferButton.click();

        WebElement formTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        assertEquals("Transferência", formTitle.getText(), "Transfer form should have correct title");
        assertTrue(isElementPresent(By.cssSelector("input[type='email']")), "Transfer form should have email field");
        assertTrue(isElementPresent(By.cssSelector("input[type='number']")), "Transfer form should have value field");
        assertTrue(isElementPresent(By.cssSelector("input[placeholder*='Descrição']")), "Transfer form should have description field");
        assertTrue(isElementPresent(By.xpath("//button[contains(text(), 'Transferir')]")), "Transfer form should have transfer button");
    }

    @Test
    @Order(10)
    void testExitButton_LogoutAndNavigatesToLogin() {
        loginIfNotAlready();

        WebElement exitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='exit-button']")));
        exitButton.click();

        // Wait until redirected to login-like view
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[text()='Entrar']")));
        assertTrue(isElementPresent(By.xpath("//button[text()='Entrar']")), "Login tab should be visible after logout");
        assertTrue(isElementPresent(By.cssSelector("input[inputmode='email']")), "Email input should be present");
    }

    @Test
    @Order(11)
    void testRegisterPageEmailInput_ValidatesOnInvalidFormat() {
        driver.get(BASE_URL);
        WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Registrar']")));
        registerTab.click();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[inputmode='email']")));
        emailField.sendKeys("invalid-email-format");
        emailField.click(); // Click away to trigger blur

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p[data-testid='email-error']")));
        WebElement alert = driver.findElement(By.cssSelector("p[data-testid='email-error']"));
        assertTrue(alert.isDisplayed(), "Validation alert should appear");
        assertTrue(alert.getText().contains("email"), "Alert should mention invalid email");
    }

    @Test
    @Order(12)
    void testPasswordStrengthIndicator_UpdatesOnInput() {
        driver.get(BASE_URL);
        WebElement registerTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Registrar']")));
        registerTab.click();

        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='password']")));
        WebElement strengthContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-testid='password-strength']")));
        
        // Check initial strength
        String initialStrength = strengthContainer.getAttribute("data-strength");
        assertEquals("weak", initialStrength, "Initial password strength should be weak");

        passwordField.sendKeys("123");

        wait.until(ExpectedConditions.attributeToBe(strengthContainer, "data-strength", "weak"));
        String weakStrength = strengthContainer.getAttribute("data-strength");
        assertEquals("weak", weakStrength, "Weak password should show weak strength");

        passwordField.clear();
        passwordField.sendKeys("StrongPass123!");

        wait.until(ExpectedConditions.attributeToBe(strengthContainer, "data-strength", "strong"));
        String strongStrength = strengthContainer.getAttribute("data-strength");
        assertEquals("strong", strongStrength, "Strong password should show strong strength");
    }

    private void loginIfNotAlready() {
        driver.get(BASE_URL);
        if (isElementPresent(By.xpath("//button[text()='Registrar']"))) {
            WebElement loginTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Entrar']")));
            loginTab.click();
        }

        if (!isElementPresent(By.cssSelector(".css-1i29wca"))) {
            WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[inputmode='email']")));
            WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));

            emailField.sendKeys(LOGIN);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".css-1i29wca")));
        }
    }

    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}