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
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys(LOGIN_EMAIL);
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(LOGIN_PASSWORD);
        
        WebElement loginButton = driver.findElement(By.id("login"));
        loginButton.click();
        
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(text(), 'Bem vindo')]")));
        Assertions.assertTrue(welcomeMessage.isDisplayed(), "Login failed - welcome message not displayed");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("invalid@email.com");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("wrongpassword");
        
        WebElement loginButton = driver.findElement(By.id("login"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(text(), 'Usuário ou senha inválido')]")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        testLoginWithValidCredentials();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[aria-label='Menu']")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Todos os itens')]")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains(BASE_URL), "All Items navigation failed");
        
        // Test About (external)
        menuButton.click();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Sobre')]")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About page not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Reset App State
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Resetar')]")));
        reset.click();
        
        // Test Logout
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Sair')]")));
        logout.click();
        
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Logout failed - login button not visible");
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        
        // Twitter
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        twitter.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Facebook
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='facebook.com']")));
        facebook.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // LinkedIn
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='linkedin.com']")));
        linkedin.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testAccountOperations() {
        testLoginWithValidCredentials();
        
        // Test account balance display
        WebElement balance = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("div[class*='balance']")));
        Assertions.assertTrue(balance.isDisplayed(), "Balance not displayed");
        
        // Test transfer page navigation
        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Transferência')]")));
        transferButton.click();
        
        WebElement transferForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("transfer-form")));
        Assertions.assertTrue(transferForm.isDisplayed(), "Transfer form not displayed");
        
        // Return to home
        WebElement homeButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Voltar')]")));
        homeButton.click();
        
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(text(), 'Bem vindo')]")));
        Assertions.assertTrue(welcomeMessage.isDisplayed(), "Return to home failed");
    }
}