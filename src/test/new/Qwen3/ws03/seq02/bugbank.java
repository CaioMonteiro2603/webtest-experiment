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
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Informe seu e-mail']")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Informe sua senha']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));
        loginButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        assertTrue(driver.getCurrentUrl().contains("home"));
        assertTrue(driver.getTitle().contains("BugBank"));
    }

    @Test
    @Order(2)
    public void testInvalidCredentialsError() {
        driver.get("https://bugbank.netlify.app/");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Informe seu e-mail']")));
        emailField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Informe sua senha']"));
        passwordField.sendKeys("wrongpassword");
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));
        loginButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(), 'Email ou senha inválidos')]")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Email ou senha inválidos"));
    }

    @Test
    @Order(3)
    public void testMenuActions() {
        driver.get("https://bugbank.netlify.app/");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Informe seu e-mail']")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Informe sua senha']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));
        loginButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Test Menu Button
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'MuiButtonBase-root')]")));
        menuButton.click();
        
        // Test Home Page in Menu
        WebElement homeLink = driver.findElement(By.xpath("//*[text()='Home']"));
        homeLink.click();
        assertEquals("https://bugbank.netlify.app/home", driver.getCurrentUrl());

        // Test Transfer
        driver.get("https://bugbank.netlify.app/home");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'MuiButtonBase-root')]")));
        menuButton.click();
        WebElement transferLink = driver.findElement(By.xpath("//*[text()='Transferência']"));
        transferLink.click();
        assertTrue(driver.getCurrentUrl().contains("transfer"));

        // Test Deposit
        driver.get("https://bugbank.netlify.app/home");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'MuiButtonBase-root')]")));
        menuButton.click();
        WebElement depositLink = driver.findElement(By.xpath("//*[text()='Depósito']"));
        depositLink.click();
        assertTrue(driver.getCurrentUrl().contains("deposit"));

        // Test Withdrawal
        driver.get("https://bugbank.netlify.app/home");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'MuiButtonBase-root')]")));
        menuButton.click();
        WebElement withdrawalLink = driver.findElement(By.xpath("//*[text()='Saque']"));
        withdrawalLink.click();
        assertTrue(driver.getCurrentUrl().contains("withdrawal"));

        // Test Statement
        driver.get("https://bugbank.netlify.app/home");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'MuiButtonBase-root')]")));
        menuButton.click();
        WebElement statementLink = driver.findElement(By.xpath("//*[text()='Extrato']"));
        statementLink.click();
        assertTrue(driver.getCurrentUrl().contains("statement"));

        // Test Logout
        driver.get("https://bugbank.netlify.app/home");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'MuiButtonBase-root')]")));
        menuButton.click();
        WebElement logoutLink = driver.findElement(By.xpath("//*[text()='Sair']"));
        logoutLink.click();
        assertTrue(driver.getCurrentUrl().contains("index"));
    }

    @Test
    @Order(4)
    public void testFooterLinks() {
        driver.get("https://bugbank.netlify.app/");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Test About Us
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[text()='Sobre Nós']")));
        aboutLink.click();
        assertEquals("https://bugbank.netlify.app/about", driver.getCurrentUrl());

        // Test Contact
        driver.get("https://bugbank.netlify.app/");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement contactLink = driver.findElement(By.xpath("//*[text()='Contato']"));
        contactLink.click();
        assertTrue(driver.getCurrentUrl().contains("contact"));

        // Test Terms and Conditions
        driver.get("https://bugbank.netlify.app/");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement termsLink = driver.findElement(By.xpath("//*[text()='Termos e Condições']"));
        termsLink.click();
        assertTrue(driver.getCurrentUrl().contains("terms"));
    }

    @Test
    @Order(5)
    public void testRegisterUser() {
        driver.get("https://bugbank.netlify.app/");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[text()='Cadastre-se']")));
        registerLink.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Informe seu nome']")));
        nameField.sendKeys("John Doe");
        WebElement emailField = driver.findElement(By.xpath("//input[@placeholder='Informe seu e-mail']"));
        emailField.sendKeys("johndoe@example.com");
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Informe sua senha']"));
        passwordField.sendKeys("password123");
        WebElement confirmPasswordField = driver.findElement(By.xpath("//input[@placeholder='Informe a senha novamente']"));
        confirmPasswordField.sendKeys("password123");
        
        WebElement registerButton = driver.findElement(By.xpath("//button[contains(text(), 'Cadastrar')]"));
        registerButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        assertTrue(driver.getCurrentUrl().contains("home"));
        assertTrue(driver.getTitle().contains("BugBank"));
    }

    @Test
    @Order(6)
    public void testTransferMoney() {
        driver.get("https://bugbank.netlify.app/");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Informe seu e-mail']")));
        emailField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Informe sua senha']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar')]"));
        loginButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Test Transfer Money
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[text()='Transferência']")));
        transferLink.click();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement toAccountField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Informe o número da conta']")));
        toAccountField.sendKeys("12345");
        WebElement amountField = driver.findElement(By.xpath("//input[@placeholder='Informe o valor da transferência']"));
        amountField.sendKeys("100");
        WebElement transferButton = driver.findElement(By.xpath("//button[contains(text(), 'Transferir')]"));
        transferButton.click();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        assertTrue(driver.getCurrentUrl().contains("home"));
    }

}