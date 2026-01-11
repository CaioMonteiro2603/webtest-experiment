package GPT20b.ws03.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

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

    /* ---------- Helper Methods ---------- */

    private void navigateToLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Registrar')]")));
    }

    private void performLogin(String user, String pass) {
        WebElement userEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='email' and @type='email']")));
        WebElement passEl = driver.findElement(By.xpath("//input[@name='password' and @type='password']"));
        WebElement loginEl = driver.findElement(By.xpath("//button[@type='submit' and contains(text(), 'Acessar')]"));

        userEl.clear();
        userEl.sendKeys(user);
        passEl.clear();
        passEl.sendKeys(pass);
        loginEl.click();
    }

    private void ensureLoggedIn() {
        if (!isLoggedIn()) {
            navigateToLoginPage();
            performLogin(USERNAME, PASSWORD);
            Assertions.assertTrue(isLoggedIn(), "Failed to log in with valid credentials.");
        }
    }

    private boolean isLoggedIn() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(), 'Saldo')]")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void logout() {
        try {
            WebElement exitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Sair')]")));
            exitButton.click();
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(), 'Registrar')]")));
        } catch (NoSuchElementException | TimeoutException ignored) {
        }
    }

    private void resetAppState() {
        try {
            WebElement burger = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("btn-toggle"))
            );
            burger.click();

            WebElement reset = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Resetar')]"))
            );
            reset.click();

            // Fecha o menu lateral se necessário
            WebElement closeMenu = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("btn-toggle"))
            );
            closeMenu.click();

        } catch (NoSuchElementException | TimeoutException ignored) {
        }
    }

    /* ---------- Example Test ---------- */

    @Test
    @Order(1)
    public void testLoginSuccessfully() {
        navigateToLoginPage();
        performLogin(USERNAME, PASSWORD);
        Assertions.assertTrue(isLoggedIn(), "User should be logged in.");
    }

    @Test
    @Order(2)
    public void testResetState() {
        ensureLoggedIn();
        resetAppState();
        Assertions.assertTrue(isLoggedIn(), "User should remain logged in after reset.");
    }

    @Test
    @Order(3)
    public void testLogout() {
        ensureLoggedIn();
        logout();
        Assertions.assertFalse(isLoggedIn(), "User should be logged out.");
    }
}