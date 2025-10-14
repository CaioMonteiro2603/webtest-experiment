package Qwen3.ws03.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugBankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
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

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("dashboard"), "Login should redirect to dashboard");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://bugbank.netlify.app/");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='error-message']")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed on invalid login");
    }

    @Test
    @Order(3)
    public void testAccountOperations() {
        driver.get("https://bugbank.netlify.app/");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));

        // Account balance check
        WebElement balance = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='balance-value']")));
        assertNotNull(balance.getText(), "Balance should be displayed");

        // Click on Transfer button
        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Transferir')]")));
        transferButton.click();

        // Verify on transfer page
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("transfer"), "Should navigate to transfer page");
    }

    @Test
    @Order(4)
    public void testTransactionHistory() {
        driver.get("https://bugbank.netlify.app/");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));

        // View transaction history
        WebElement historyLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Histórico")));
        historyLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("history"), "Should navigate to transaction history page");
    }

    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get("https://bugbank.netlify.app/");

        String parentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(parentWindow)) {
                driver.switchTo().window(window);
                driver.close();
            }
        }

        // Test footer links - Terms of Service
        try {
            WebElement termsLink = driver.findElement(By.linkText("Termos de Serviço"));
            termsLink.click();
            
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("terms"), "Should open Terms of Service link");
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // If element doesn't exist, continue
        }

        // Test footer links - Privacy Policy
        try {
            WebElement privacyLink = driver.findElement(By.linkText("Política de Privacidade"));
            privacyLink.click();

            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("privacy"), "Should open Privacy Policy link");
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // If element doesn't exist, continue
        }

        // Test footer links - About Us
        try {
            WebElement aboutLink = driver.findElement(By.linkText("Sobre Nós"));
            aboutLink.click();

            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("about"), "Should open About Us link");
            driver.close();
            driver.switchTo().window(parentWindow);
        } catch (NoSuchElementException e) {
            // If element doesn't exist, continue
        }
    }
    
    @Test
    @Order(6)
    public void testProfileSettings() {
        driver.get("https://bugbank.netlify.app/");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(), 'Entrar')]"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        // Wait for dashboard to load
        wait.until(ExpectedConditions.urlContains("dashboard"));

        // Go to profile settings
        WebElement profileButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='profile-button']")));
        profileButton.click();

        // Wait for profile page to load
        wait.until(ExpectedConditions.urlContains("profile"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("profile"), "Should navigate to profile page");
    }
}