package deepseek.ws03.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".welcome-message")));
        Assertions.assertTrue(welcomeMessage.isDisplayed(), "Welcome message should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get(BASE_URL);
        login();

        WebElement transactionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transactions")));
        transactionsLink.click();
        wait.until(ExpectedConditions.urlContains("transactions"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Transactions"), "Should navigate to Transactions page");

        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer")));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Transfer"), "Should navigate to Transfer page");

        WebElement paymentsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Payments")));
        paymentsLink.click();
        wait.until(ExpectedConditions.urlContains("payments"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Payments"), "Should navigate to Payments page");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        login();

        String currentWindow = driver.getWindowHandle();
        
        WebElement helpLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Help")));
        helpLink.click();
        wait.until(ExpectedConditions.urlContains("help"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Help"), "Should navigate to Help page");

        if (driver.findElements(By.linkText("Netlify")).size() > 0) {
            WebElement netlifyLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Netlify")));
            netlifyLink.click();
            switchToNewWindowAndAssertDomain("netlify.com");
        }

        driver.switchTo().window(currentWindow);
    }

    @Test
    @Order(5)
    public void testTransferFunctionality() {
        driver.get(BASE_URL);
        login();

        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer")));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer"));

        WebElement accountNumberField = wait.until(ExpectedConditions.elementToBeClickable(By.name("accountNumber")));
        WebElement amountField = driver.findElement(By.name("amount"));
        WebElement transferButton = driver.findElement(By.cssSelector("button[type='submit']"));

        accountNumberField.sendKeys("12345-6");
        amountField.sendKeys("100");
        transferButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success-message")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Transfer success message should be displayed");
    }

    @Test
    @Order(6)
    public void testLogout() {
        driver.get(BASE_URL);
        login();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("login"));
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Should be back to login page after logout");
    }

    private void login() {
        if (driver.getCurrentUrl().endsWith("/") && driver.findElements(By.cssSelector("input[type='email']")).size() > 0) {
            WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[type='email']")));
            WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

            emailField.sendKeys(LOGIN);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("home"));
        }
    }

    private void switchToNewWindowAndAssertDomain(String expectedDomain) {
        String currentWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        driver.close();
        driver.switchTo().window(currentWindow);
    }
}