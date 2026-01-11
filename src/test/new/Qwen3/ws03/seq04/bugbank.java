package Qwen3.ws03.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class bugbank {
    private static WebDriver driver;
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
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[id='email'], input[name='email']")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[id='password'], input[name='password']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"));
        assertTrue(driver.findElement(By.cssSelector("h1")).getText().contains("Dashboard"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[id='email'], input[name='email']")));
        emailField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[id='password'], input[name='password']"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Invalid"));
    }

    @Test
    @Order(3)
    public void testNavigation() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[id='email'], input[name='email']")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[id='password'], input[name='password']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertEquals("https://bugbank.netlify.app/dashboard", driver.getCurrentUrl());

        // Navigate to Transfer
        WebElement transferLink = driver.findElement(By.linkText("Transfer"));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer"));
        assertTrue(driver.getCurrentUrl().contains("transfer"));
        
        // Navigate to Statement
        WebElement statementLink = driver.findElement(By.linkText("Statement"));
        statementLink.click();
        wait.until(ExpectedConditions.urlContains("statement"));
        assertTrue(driver.getCurrentUrl().contains("statement"));

        // Navigate to Profile
        WebElement profileLink = driver.findElement(By.linkText("Profile"));
        profileLink.click();
        wait.until(ExpectedConditions.urlContains("profile"));
        assertTrue(driver.getCurrentUrl().contains("profile"));

        // Navigate back to Dashboard
        WebElement dashboardLink = driver.findElement(By.linkText("Dashboard"));
        dashboardLink.click();
        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"));
    }

    @Test
    @Order(4)
    public void testTransferFunctionality() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[id='email'], input[name='email']")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[id='password'], input[name='password']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to Transfer
        WebElement transferLink = driver.findElement(By.linkText("Transfer"));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer"));

        // Fill transfer form
        WebElement toAccountField = driver.findElement(By.cssSelector("input[id='toAccount'], input[name='toAccount']"));
        toAccountField.sendKeys("12345");
        WebElement amountField = driver.findElement(By.cssSelector("input[id='amount'], input[name='amount']"));
        amountField.sendKeys("100");
        WebElement descriptionField = driver.findElement(By.cssSelector("input[id='description'], input[name='description'], textarea[id='description'], textarea[name='description']"));
        descriptionField.sendKeys("Test transfer");
        WebElement transferButton = driver.findElement(By.cssSelector("button[type='submit']"));
        transferButton.click();

        // Wait for confirmation
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success-message")));
        assertTrue(driver.findElement(By.cssSelector(".success-message")).isDisplayed());
    }

    @Test
    @Order(5)
    public void testStatementFunctionality() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[id='email'], input[name='email']")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[id='password'], input[name='password']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to Statement
        WebElement statementLink = driver.findElement(By.linkText("Statement"));
        statementLink.click();
        wait.until(ExpectedConditions.urlContains("statement"));
        
        // Select account and date range
        WebElement accountSelect = driver.findElement(By.cssSelector("select[id='account'], select[name='account']"));
        accountSelect.click();
        WebElement accountOption = driver.findElement(By.xpath("//option[@value='12345']"));
        accountOption.click();
        
        // Click on Generate Statement
        WebElement generateButton = driver.findElement(By.cssSelector("button[type='submit']"));
        generateButton.click();
        
        // Wait for statement to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".statement-content")));
        assertTrue(driver.findElement(By.cssSelector(".statement-content")).isDisplayed());
    }

    @Test
    @Order(6)
    public void testProfileFunctionality() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[id='email'], input[name='email']")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[id='password'], input[name='password']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        
        // Navigate to Profile
        WebElement profileLink = driver.findElement(By.linkText("Profile"));
        profileLink.click();
        wait.until(ExpectedConditions.urlContains("profile"));
        
        // Update profile information
        WebElement nameField = driver.findElement(By.cssSelector("input[id='fullName'], input[name='fullName']"));
        nameField.clear();
        nameField.sendKeys("Caio Test");
        WebElement phoneField = driver.findElement(By.cssSelector("input[id='phone'], input[name='phone']"));
        phoneField.clear();
        phoneField.sendKeys("555-1234");
        WebElement saveButton = driver.findElement(By.cssSelector("button[type='submit']"));
        saveButton.click();
        
        // Wait for confirmation
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success-message")));
        assertTrue(driver.findElement(By.cssSelector(".success-message")).isDisplayed());
    }

    @Test
    @Order(7)
    public void testLogout() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email'], input[id='email'], input[name='email']")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password'], input[id='password'], input[name='password']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        assertTrue(driver.getCurrentUrl().contains("dashboard"));

        // Click on Logout
        WebElement logoutButton = driver.findElement(By.linkText("Logout"));
        logoutButton.click();
        wait.until(ExpectedConditions.urlContains("index"));
        assertTrue(driver.getCurrentUrl().contains("index"));
        assertTrue(driver.findElement(By.cssSelector("input[type='email'], input[id='email'], input[name='email']")).isDisplayed());
    }
}