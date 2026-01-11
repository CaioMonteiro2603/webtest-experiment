package SunaDeepSeek.ws03.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

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
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Insira seu email']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Insira sua senha']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        
        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("home"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//p[contains(text(), 'Olá')]")));
        
        Assertions.assertTrue(welcomeMessage.isDisplayed(), "Login was not successful");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Insira seu email']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Insira sua senha']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        
        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//p[contains(text(), 'Usuário ou senha inválido')]")));
        
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        // First login
        driver.get(BASE_URL);
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(@class, 'menu-button')]")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Todos os itens')]")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("home"));
        
        // Test About (external link)
        menuButton.click();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Sobre')]")));
        about.click();
        
        // Switch to new tab and verify URL
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "About page not opened");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Logout
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Sair')]")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("login"));
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        login();
        
        // Twitter link
        testExternalLink("Twitter", "twitter.com");
        
        // Facebook link
        testExternalLink("Facebook", "facebook.com");
        
        // LinkedIn link
        testExternalLink("LinkedIn", "linkedin.com");
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        driver.get(BASE_URL);
        login();
        
        // Add some item to cart to create state
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("(//button[contains(text(), 'Adicionar ao carrinho')])[1]")));
        addToCartButton.click();
        
        // Reset state
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(@class, 'menu-button')]")));
        menuButton.click();
        
        WebElement resetState = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), 'Resetar estado')]")));
        resetState.click();
        
        // Verify cart is empty
        List<WebElement> cartItems = driver.findElements(By.className("cart-item"));
        Assertions.assertEquals(0, cartItems.size(), "App state was not reset properly");
    }

    private void login() {
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Insira seu email']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Insira sua senha']"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
        
        emailField.clear();
        passwordField.clear();
        emailField.sendKeys(LOGIN_EMAIL);
        passwordField.sendKeys(LOGIN_PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("home"));
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), '" + linkText + "')]")));
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            linkText + " link did not open correct domain");
        
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}