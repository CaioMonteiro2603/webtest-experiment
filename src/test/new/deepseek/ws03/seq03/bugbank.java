package deepseek.ws03.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank  {
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
    public void testRegisterAndLogin() {
        driver.get(BASE_URL);
        
        // Wait for page to load and register button to be clickable
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[id='register']"))).click();
        
        // Fill registration form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email']"))).sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[name='name']")).sendKeys("Caio Test");
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("input[name='passwordConfirmation']")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        // Verify registration success
        WebElement modalTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-title")));
        Assertions.assertTrue(modalTitle.getText().contains("foi criada com sucesso"));
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".modal-footer button")));
        closeButton.click();

        // Login with registered credentials
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[id='login']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email']"))).sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        // Verify login success
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home__Text-sc-1auj767-4")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Olá"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[id='login']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email']"))).sendKeys("invalid@email.com");
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home__ErrorText-sc-1auj767-11")));
        Assertions.assertTrue(errorMessage.getText().contains("Usuário ou senha inválido"));
    }

    @Test
    @Order(3)
    public void testTransferFunds() {
        loginIfNeeded();
        
        // Wait for balance element to be visible
        String balanceBefore = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home__Text-sc-1auj767-6"))).getText();
        
        driver.findElement(By.cssSelector("button[id='btn-TRANSFERÊNCIA']")).click();
        WebElement accountNumberField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='accountNumber']")));
        accountNumberField.sendKeys("12345");
        driver.findElement(By.cssSelector("input[name='amount']")).sendKeys("100");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        WebElement modalTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-title")));
        Assertions.assertTrue(modalTitle.getText().contains("sucesso"));
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".modal-footer button")));
        closeButton.click();
        
        // Wait for balance to be visible again
        String balanceAfter = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home__Text-sc-1auj767-6"))).getText();
        Assertions.assertNotEquals(balanceBefore, balanceAfter);
    }

    @Test
    @Order(4)
    public void testNavigation() {
        loginIfNeeded();
        
        // Test Extrato navigation
        driver.findElement(By.cssSelector("button[id='btn-EXTRATO']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".bank-statement__Container-sc-7n8uh8-0")));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1")).getText().contains("Extrato"));
        
        // Test Back button
        driver.findElement(By.cssSelector("button[id='btnBack']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home__Container-sc-1auj767-0")));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".home__Text-sc-1auj767-4")).isDisplayed());
    }

    @Test
    @Order(5)
    public void testLogout() {
        loginIfNeeded();
        driver.findElement(By.cssSelector("button[id='btnExit']")).click();
        
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[id='login']")));
        Assertions.assertTrue(loginButton.isDisplayed());
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().equals(BASE_URL) || driver.findElements(By.cssSelector("button[id='login']")).size() > 0) {
            driver.get(BASE_URL);
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[id='login']"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email']"))).sendKeys(USERNAME);
            driver.findElement(By.cssSelector("input[name='password']")).sendKeys(PASSWORD);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home__Text-sc-1auj767-4")));
        }
    }
}