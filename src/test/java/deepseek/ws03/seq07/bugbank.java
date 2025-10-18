package deepseek.ws03.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugBankTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLoginSuccess() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Olá"), "Home page should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.alert-error")));
        Assertions.assertTrue(errorElement.getText().contains("Usuário ou senha inválido"), 
            "Error message should be displayed");
    }

    @Test
    @Order(3)
    public void testAccountCreation() {
        driver.get(BASE_URL + "register");
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='name']")));
        WebElement emailField = driver.findElement(By.cssSelector("input[name='email']"));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        WebElement confirmPasswordField = driver.findElement(By.cssSelector("input[name='passwordConfirmation']"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        String timestamp = String.valueOf(System.currentTimeMillis());
        nameField.sendKeys("Test User " + timestamp);
        emailField.sendKeys("test" + timestamp + "@email.com");
        passwordField.sendKeys("123456");
        confirmPasswordField.sendKeys("123456");
        submitButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
        WebElement balanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.balance-value")));
        Assertions.assertTrue(balanceElement.getText().contains("R$"), "Account created and home page should show balance");
    }

    @Test
    @Order(4)
    public void testTransferBetweenAccounts() {
        driver.get(BASE_URL);
        login();

        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='transfer']")));
        transferButton.click();

        wait.until(ExpectedConditions.urlContains("transfer"));
        WebElement accountNumberField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='accountNumber']")));
        WebElement amountField = driver.findElement(By.cssSelector("input[name='value']"));
        WebElement descriptionField = driver.findElement(By.cssSelector("input[name='description']"));
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));

        accountNumberField.sendKeys("1234");
        amountField.sendKeys("100");
        descriptionField.sendKeys("Test transfer");
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.alert-success")));
        Assertions.assertTrue(successMessage.getText().contains("Transferência realizada com sucesso"),
            "Transfer should be successful");
    }

    @Test
    @Order(5)
    public void testLogout() {
        driver.get(BASE_URL);
        login();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.logout")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("login"));
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[type='submit']")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Should be back on login page after logout");
    }

    private void login() {
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[name='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
    }
}