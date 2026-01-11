package SunaDeepSeek.ws03.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
        
        WebElement passwordField = driver.findElement(By.id("senha"));
        passwordField.sendKeys(LOGIN_PASSWORD);
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"), "Login should redirect to home page");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        emailField.sendKeys("invalid@email.com");
        
        WebElement passwordField = driver.findElement(By.id("senha"));
        passwordField.sendKeys("wrongpassword");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert.alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Usuário ou senha inválido"), 
            "Should show invalid credentials error");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Todos os itens')]")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"), "Should be on home page");
        
        // Open menu again
        menuButton.click();
        
        // Test About (external)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Sobre')]")));
        about.click();
        
        // Switch to new tab and verify URL
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "About should open GitHub");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton.click();
        
        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Sair')]")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "Should be back on login page");
    }

    @Test
    @Order(4)
    public void testSocialLinks() {
        login();
        
        // Twitter
        testExternalLink("Twitter", "twitter.com");
        
        // Facebook
        testExternalLink("Facebook", "facebook.com");
        
        // LinkedIn
        testExternalLink("LinkedIn", "linkedin.com");
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".navbar-toggler")));
        menuButton.click();
        
        // Click Reset
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Resetar estado')]")));
        reset.click();
        
        // Verify reset confirmation
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert.alert-success")));
        Assertions.assertTrue(alert.getText().contains("Dados resetados com sucesso!"), 
            "Should show reset confirmation");
    }

    private void login() {
        driver.get(BASE_URL);
        if (driver.getCurrentUrl().contains("login")) {
            WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
            emailField.sendKeys(LOGIN_EMAIL);
            
            WebElement passwordField = driver.findElement(By.id("senha"));
            passwordField.sendKeys(LOGIN_PASSWORD);
            
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
            loginButton.click();
            
            wait.until(ExpectedConditions.urlContains("home"));
        }
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.partialLinkText(linkText));
        if (links.size() > 0) {
            String originalWindow = driver.getWindowHandle();
            links.get(0).click();
            
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.contentEquals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                linkText + " should open " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}