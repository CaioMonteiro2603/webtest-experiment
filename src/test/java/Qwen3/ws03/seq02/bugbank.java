package Qwen3.ws03.seq02;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class bugbank {
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"));
        assertTrue(driver.getTitle().contains("BugBank"));
    }

    @Test
    @Order(2)
    public void testInvalidCredentialsError() {
        driver.get("https://bugbank.netlify.app/");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("wrongpassword");
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Email ou senha inválidos"));
    }

    @Test
    @Order(3)
    public void testMenuActions() {
        driver.get("https://bugbank.netlify.app/home");
        
        // Test Menu Button
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();
        
        // Test Home Page in Menu
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        homeLink.click();
        assertEquals("https://bugbank.netlify.app/home", driver.getCurrentUrl());

        // Test Transfer
        driver.get("https://bugbank.netlify.app/home");
        menuButton.click();
        WebElement transferLink = driver.findElement(By.linkText("Transferência"));
        transferLink.click();
        assertTrue(driver.getCurrentUrl().contains("transfer"));

        // Test Deposit
        driver.get("https://bugbank.netlify.app/home");
        menuButton.click();
        WebElement depositLink = driver.findElement(By.linkText("Depósito"));
        depositLink.click();
        assertTrue(driver.getCurrentUrl().contains("deposit"));

        // Test Withdrawal
        driver.get("https://bugbank.netlify.app/home");
        menuButton.click();
        WebElement withdrawalLink = driver.findElement(By.linkText("Saque"));
        withdrawalLink.click();
        assertTrue(driver.getCurrentUrl().contains("withdrawal"));

        // Test Statement
        driver.get("https://bugbank.netlify.app/home");
        menuButton.click();
        WebElement statementLink = driver.findElement(By.linkText("Extrato"));
        statementLink.click();
        assertTrue(driver.getCurrentUrl().contains("statement"));

        // Test Logout
        driver.get("https://bugbank.netlify.app/home");
        menuButton.click();
        WebElement logoutLink = driver.findElement(By.linkText("Sair"));
        logoutLink.click();
        assertTrue(driver.getCurrentUrl().contains("index"));
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get("https://bugbank.netlify.app/");
        
        // Test About Us
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre Nós")));
        aboutLink.click();
        assertEquals("https://bugbank.netlify.app/about", driver.getCurrentUrl());

        // Test Contact
        driver.get("https://bugbank.netlify.app/");
        WebElement contactLink = driver.findElement(By.linkText("Contato"));
        contactLink.click();
        assertTrue(driver.getCurrentUrl().contains("contact"));

        // Test Terms and Conditions
        driver.get("https://bugbank.netlify.app/");
        WebElement termsLink = driver.findElement(By.linkText("Termos e Condições"));
        termsLink.click();
        assertTrue(driver.getCurrentUrl().contains("terms"));
    }

    @Test
    @Order(5)
    public void testRegisterUser() {
        driver.get("https://bugbank.netlify.app/");
        
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Cadastre-se")));
        registerLink.click();

        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        nameField.sendKeys("John Doe");
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("johndoe@example.com");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("password123");
        WebElement confirmPasswordField = driver.findElement(By.id("confirmPassword"));
        confirmPasswordField.sendKeys("password123");
        
        WebElement registerButton = driver.findElement(By.cssSelector("button[type='submit']"));
        registerButton.click();

        wait.until(ExpectedConditions.urlContains("home"));
        assertTrue(driver.getCurrentUrl().contains("home"));
        assertTrue(driver.getTitle().contains("BugBank"));
    }

    @Test
    @Order(6)
    public void testTransferMoney() {
        driver.get("https://bugbank.netlify.app/home");
        
        // Test Transfer Money
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transferência")));
        transferLink.click();
        
        WebElement toAccountField = wait.until(ExpectedConditions.elementToBeClickable(By.id("toAccount")));
        toAccountField.sendKeys("12345");
        WebElement amountField = driver.findElement(By.id("amount"));
        amountField.sendKeys("100");
        WebElement transferButton = driver.findElement(By.cssSelector("button[type='submit']"));
        transferButton.click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".success")));
        assertTrue(driver.getCurrentUrl().contains("home"));
    }

}