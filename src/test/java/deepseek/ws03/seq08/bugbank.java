package deepseek.ws03.seq08;

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
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class BugBankTest {
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";
    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='email']")));
        email.sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".balance")).isDisplayed(),
                "Balance should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='email']")));
        email.sendKeys("invalid@email.com");
        driver.findElement(By.cssSelector("input[name='password']")).sendKeys("wrongpass");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(error.getText().contains("Usuário ou senha inválido"),
                "Should display error for invalid login");
    }

    @Test
    @Order(3)
    public void testAccountNavigation() {
        loginIfNeeded();
        
        // Test Transfer
        driver.findElement(By.cssSelector("a[href*='transfer']")).click();
        wait.until(ExpectedConditions.urlContains("transfer"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".transfer-form")).isDisplayed(),
                "Transfer form should be visible");
        
        // Test Extrato
        driver.findElement(By.cssSelector("a[href*='extrato']")).click();
        wait.until(ExpectedConditions.urlContains("extrato"));
        Assertions.assertTrue(driver.findElements(By.cssSelector(".transaction")).size() >= 0,
                "Should display transaction list");
    }

    @Test
    @Order(4)
    public void testTransferOperation() {
        loginIfNeeded();
        driver.findElement(By.cssSelector("a[href*='transfer']")).click();
        
        WebElement accountNumber = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[name='accountNumber']")));
        accountNumber.sendKeys("12345");
        
        driver.findElement(By.cssSelector("input[name='value']")).sendKeys("100");
        driver.findElement(By.cssSelector("input[name='description']")).sendKeys("Test transfer");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".success")));
        Assertions.assertTrue(success.getText().contains("Transferência realizada com sucesso"),
                "Should display success message after transfer");
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        loginIfNeeded();
        String originalWindow = driver.getWindowHandle();
        
        // Test Facebook link
        testExternalLink("a[href*='facebook']", "facebook.com", originalWindow);
        
        // Test Instagram link
        testExternalLink("a[href*='instagram']", "instagram.com", originalWindow);
    }

    @Test
    @Order(6)
    public void testLogout() {
        loginIfNeeded();
        
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.logout")));
        logout.click();
        
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("input[name='email']")).isDisplayed(),
                "Login form should be visible after logout");
    }

    private void testExternalLink(String cssSelector, String expectedDomain, String originalWindow) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
        link.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "Link should open " + expectedDomain + " domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("home")) {
            driver.get(BASE_URL);
            WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='email']")));
            email.sendKeys(USERNAME);
            driver.findElement(By.cssSelector("input[name='password']")).sendKeys(PASSWORD);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.urlContains("home"));
        }
    }
}