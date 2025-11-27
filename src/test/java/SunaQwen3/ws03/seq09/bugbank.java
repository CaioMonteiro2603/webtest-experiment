package deepseek.ws03.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class BugBankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement registerButton = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//button[contains(text(),'Registrar')]")));
        assertTrue(registerButton.isDisplayed(), "Register button should be visible");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[placeholder='Informe seu e-mail']")));
        WebElement passwordField = driver.findElement(
            By.cssSelector("input[placeholder='Informe sua senha']"));
        WebElement loginButton = driver.findElement(
            By.xpath("//button[contains(text(),'Acessar')]"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//p[contains(text(),'bem vindo') or contains(text(),'Bem vindo')]")));
        assertTrue(welcomeMessage.isDisplayed(), "Welcome message should appear after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("input[placeholder='Informe seu e-mail']")));
        WebElement passwordField = driver.findElement(
            By.cssSelector("input[placeholder='Informe sua senha']"));
        WebElement loginButton = driver.findElement(
            By.xpath("//button[contains(text(),'Acessar')]"));

        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//p[contains(text(),'Usuário ou senha inválido')]")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(4)
    public void testAccountOperations() {
        login();
        
        // Test balance display
        WebElement balanceElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".balance")));
        assertTrue(balanceElement.isDisplayed(), "Balance should be visible");

        // Test transfer button
        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Transferência')]")));
        transferButton.click();
        
        WebElement transferForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".transfer-form")));
        assertTrue(transferForm.isDisplayed(), "Transfer form should appear");
    }

    @Test
    @Order(5)
    public void testNavigationMenu() {
        login();
        
        // Test statements button
        WebElement statementsButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Extrato')]")));
        statementsButton.click();
        
        WebElement statementsList = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".statements")));
        assertTrue(statementsList.isDisplayed(), "Statements should be visible");

        // Test logout
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Sair')]")));
        logoutButton.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".login-form")));
        assertTrue(loginForm.isDisplayed(), "Login form should appear after logout");
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'github')]")));
        testExternalLink(githubLink, "github.com");

        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'linkedin')]")));
        testExternalLink(linkedinLink, "linkedin.com");
    }

    private void login() {
        driver.get(BASE_URL);
        if (!driver.getCurrentUrl().equals(BASE_URL + "home")) {
            WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[placeholder='Informe seu e-mail']")));
            WebElement passwordField = driver.findElement(
                By.cssSelector("input[placeholder='Informe sua senha']"));
            WebElement loginButton = driver.findElement(
                By.xpath("//button[contains(text(),'Acessar')]"));

            emailField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            
            wait.until(ExpectedConditions.urlContains("home"));
        }
    }

    private void testExternalLink(WebElement link, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        link.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "External link should open " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}