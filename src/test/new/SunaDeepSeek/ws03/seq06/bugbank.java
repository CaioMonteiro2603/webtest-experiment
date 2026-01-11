package SunaDeepSeek.ws03.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN_EMAIL = "caio@gmail.com";
    private static final String LOGIN_PASSWORD = "123";

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
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Informe seu e-mail' or @type='email']")));
        emailField.sendKeys(LOGIN_EMAIL);
        
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Informe sua senha' or @type='password']"));
        passwordField.sendKeys(LOGIN_PASSWORD);
        
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar') or @type='submit']"));
        loginButton.click();
        
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), 'Bem vindo') or contains(text(), 'bem vindo')]")));
        Assertions.assertTrue(welcomeMessage.isDisplayed(), "Login failed - welcome message not displayed");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Informe seu e-mail' or @type='email']")));
        emailField.sendKeys("invalid@email.com");
        
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Informe sua senha' or @type='password']"));
        passwordField.sendKeys("wrongpassword");
        
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Acessar') or @type='submit']"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), 'Usuário ou senha inválido') or contains(text(), 'usuário ou senha')]")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        testLoginWithValidCredentials();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[@aria-label='Menu' or contains(text(), 'Menu')]")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(), 'Todos os itens') or contains(text(), 'itens')]")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank"), "All Items navigation failed");
        
        // Test About (external)
        menuButton.click();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(), 'Sobre') or contains(text(), 'sobre')]")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs"), "About page not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Reset App State
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(), 'Resetar') or contains(text(), 'resetar')]")));
        reset.click();
        
        // Test Logout
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(), 'Sair') or contains(text(), 'sair')]")));
        logout.click();
        
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Acessar') or @type='submit']")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Logout failed - login button not visible");
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        
        // Twitter
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, 'twitter') or contains(@href, 'x.com')]")));
        twitter.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter") || driver.getCurrentUrl().contains("x.com"), "Twitter link not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Facebook
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, 'facebook')]")));
        facebook.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook"), "Facebook link not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // LinkedIn
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href, 'linkedin')]")));
        linkedin.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin"), "LinkedIn link not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testAccountOperations() {
        testLoginWithValidCredentials();
        
        // Test account balance display
        WebElement balance = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(@class, 'balance') or contains(text(), 'Saldo')]")));
        Assertions.assertTrue(balance.isDisplayed(), "Balance not displayed");
        
        // Test transfer page navigation
        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Transferência') or contains(text(), 'transferência')]")));
        transferButton.click();
        
        WebElement transferForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//form[contains(@id, 'transfer') or contains(@class, 'transfer')]")));
        Assertions.assertTrue(transferForm.isDisplayed(), "Transfer form not displayed");
        
        // Return to home
        WebElement homeButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Voltar') or contains(text(), 'voltar')]")));
        homeButton.click();
        
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(), 'Bem vindo') or contains(text(), 'bem vindo')]")));
        Assertions.assertTrue(welcomeMessage.isDisplayed(), "Return to home failed");
    }
}