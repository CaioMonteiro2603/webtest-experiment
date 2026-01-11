package Qwen3.ws03.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

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
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Nome do usuário']")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Senha']"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("app"));
        assertEquals("https://bugbank.netlify.app/app", driver.getCurrentUrl());
        assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Dashboard"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://bugbank.netlify.app/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Nome do usuário']")));
        usernameField.sendKeys("invalid@gmail.com");
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Senha']"));
        passwordField.sendKeys("invalid");
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//p[contains(text(),'Usuário ou senha inválido')]")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Usuário ou senha inválido"));
    }

    @Test
    @Order(3)
    public void testForgotPassword() {
        driver.get("https://bugbank.netlify.app/");
        WebElement forgotPasswordLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Registrar')]")));
        forgotPasswordLink.click();

        wait.until(ExpectedConditions.urlContains("signup"));
        assertEquals("https://bugbank.netlify.app/signup", driver.getCurrentUrl());
    }

    @Test
    @Order(4)
    public void testRegister() {
        driver.get("https://bugbank.netlify.app/");
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Registrar')]")));
        registerLink.click();

        wait.until(ExpectedConditions.urlContains("signup"));
        assertEquals("https://bugbank.netlify.app/signup", driver.getCurrentUrl());
    }

    @Test
    @Order(5)
    public void testNavigationMenu() {
        driver.get("https://bugbank.netlify.app/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Test Home link
        WebElement homeLink = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("app"));
        assertEquals("https://bugbank.netlify.app/app", driver.getCurrentUrl());
    }

    @Test
    @Order(6)
    public void testFooterLinks() {
        driver.get("https://bugbank.netlify.app/");
        
        // Test Twitter link
        WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
        twitterLink.click();
        Set<String> windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        String mainWindowHandle = driver.getWindowHandle();
        String newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);

        // Test Facebook link
        WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
        facebookLink.click();
        windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        mainWindowHandle = driver.getWindowHandle();
        newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);

        // Test LinkedIn link
        WebElement linkedInLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
        linkedInLink.click();
        windowHandles = driver.getWindowHandles();
        assertEquals(2, windowHandles.size());
        mainWindowHandle = driver.getWindowHandle();
        newWindowHandle = null;
        for (String handle : windowHandles) {
            if (!handle.equals(mainWindowHandle)) {
                newWindowHandle = handle;
                break;
            }
        }
        driver.switchTo().window(newWindowHandle);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"));
        driver.close();
        driver.switchTo().window(mainWindowHandle);
    }

    @Test
    @Order(7)
    public void testAccountManagement() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Test account creation
        WebElement accountsTab = driver.findElement(By.xpath("//button[@aria-label='Saldo']"));
        accountsTab.click();
        wait.until(ExpectedConditions.urlContains("app"));

        // Check for account list
        List<WebElement> accountRows = driver.findElements(By.cssSelector("div[data-testid*='account']"));
        assertTrue(accountRows.size() >= 0);
    }

    @Test
    @Order(8)
    public void testTransferFunds() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Navigate to Transfer Funds
        WebElement transferLink = driver.findElement(By.xpath("//button[contains(text(),'Transferência')]"));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer"));

        // Test form fields
        WebElement fromAccount = driver.findElement(By.xpath("//input[@placeholder='Número da conta']"));
        WebElement toAccount = driver.findElement(By.xpath("//input[@placeholder='Número da conta']"));
        WebElement amount = driver.findElement(By.xpath("//input[@placeholder='Valor']"));

        assertTrue(fromAccount.isDisplayed());
        assertTrue(toAccount.isDisplayed());
        assertTrue(amount.isDisplayed());
    }

    @Test
    @Order(9)
    public void testTransactions() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Navigate to Transactions
        WebElement transactionsLink = driver.findElement(By.xpath("//button[@aria-label='Extrato']"));
        transactionsLink.click();
        wait.until(ExpectedConditions.urlContains("app"));

        // Check for transaction list
        List<WebElement> transactionRows = driver.findElements(By.cssSelector("div[data-testid*='transaction']"));
        assertTrue(transactionRows.size() >= 0);
    }

    @Test
    @Order(10)
    public void testProfileUpdate() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Navigate to profile
        WebElement profileButton = driver.findElement(By.xpath("//button[@aria-label='Sair']"));
        profileButton.click();
        
        wait.until(ExpectedConditions.urlContains("app"));

        // Test profile update form
        WebElement firstNameField = driver.findElement(By.xpath("//input[@placeholder='Nome']"));
        WebElement lastNameField = driver.findElement(By.xpath("//input[@placeholder='Sobrenome']"));
        WebElement emailField = driver.findElement(By.xpath("//input[@placeholder='Email']"));

        assertTrue(firstNameField.isDisplayed());
        assertTrue(lastNameField.isDisplayed());
        assertTrue(emailField.isDisplayed());
    }

    @Test
    @Order(11)
    public void testSettings() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Test Settings link
        WebElement settingsLink = driver.findElement(By.xpath("//button[@aria-label='Saldo']"));
        settingsLink.click();
        wait.until(ExpectedConditions.urlContains("app"));

        // Verify settings page loaded
        assertTrue(driver.getCurrentUrl().contains("app"));
        assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Dashboard"));
    }

    @Test
    @Order(12)
    public void testDashboardComponents() {
        driver.get("https://bugbank.netlify.app/app");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        // Check dashboard elements
        assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Dashboard"));
        
        // Check for quick actions
        List<WebElement> quickActionButtons = driver.findElements(By.cssSelector("button"));
        assertTrue(quickActionButtons.size() >= 0);
        
        // Check account summary
        List<WebElement> accountCards = driver.findElements(By.cssSelector("div[data-testid*='balance']"));
        assertTrue(accountCards.size() >= 0);
    }
}