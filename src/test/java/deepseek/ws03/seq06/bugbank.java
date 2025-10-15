package deepseek.ws03.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugBankTest {

    private static WebDriver driver;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";
    private static WebDriverWait wait;

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
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".balance")));
        Assertions.assertTrue(welcomeMessage.isDisplayed(), "Login should be successful and balance should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testTransferMoney() {
        testValidLogin();
        
        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Transfer']")));
        transferButton.click();

        WebElement accountNumber = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Account number']")));
        WebElement transferAmount = driver.findElement(By.cssSelector("input[placeholder='Amount']"));
        WebElement transferDescription = driver.findElement(By.cssSelector("input[placeholder='Description']"));
        WebElement confirmButton = driver.findElement(By.cssSelector("button[type='submit']"));

        accountNumber.sendKeys("1234");
        transferAmount.sendKeys("10");
        transferDescription.sendKeys("Test transfer");
        confirmButton.click();

        WebElement confirmationMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success")));
        Assertions.assertTrue(confirmationMessage.isDisplayed(), "Transfer should be completed successfully");
    }

    @Test
    @Order(4)
    public void testTransactionHistory() {
        testValidLogin();
        
        WebElement transactionsButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Transactions']")));
        transactionsButton.click();

        WebElement transactionList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".transactions")));
        Assertions.assertTrue(transactionList.isDisplayed(), "Transaction history should be visible");
    }

    @Test
    @Order(5)
    public void testLogout() {
        testValidLogin();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Logout']")));
        logoutButton.click();

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".login-form")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Should return to login page after logout");
    }

    @Test
    @Order(6)
    public void testSocialLinks() {
        driver.get(BASE_URL);
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook']")));
        facebookLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("facebook.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}