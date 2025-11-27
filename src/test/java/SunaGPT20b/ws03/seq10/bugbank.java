package deepseek.ws03.seq10;

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
    private static WebDriver driver;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";
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
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys(LOGIN);
        password.sendKeys(PASSWORD);
        loginButton.click();

        WebElement dashboard = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(), 'Dashboard')]")));
        Assertions.assertTrue(dashboard.isDisplayed(), "Login failed - dashboard not displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys("invalid@email.com");
        password.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(text(), 'Credenciais inválidas')]")));
        Assertions.assertTrue(errorElement.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testMainNavigation() {
        loginIfNeeded();
        
        // Test Transactions
        navigateAndVerify("//a[contains(text(), 'Transações')]", "transactions", "Extrato");
        
        // Test Transfer
        navigateAndVerify("//a[contains(text(), 'Transferir')]", "transfer", "Transferir Valores");
        
        // Test Payments
        navigateAndVerify("//a[contains(text(), 'Pagamentos')]", "payments", "Pagamentos");
    }

    @Test
    @Order(4)
    public void testAccountActions() {
        loginIfNeeded();
        
        // Test account balance visibility
        WebElement balance = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class, 'account-balance')]")));
        Assertions.assertTrue(balance.isDisplayed(), "Account balance not displayed");
        
        // Test transfer money
        driver.findElement(By.xpath("//a[contains(text(), 'Transferir')]")).click();
        WebElement transferForm = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//form[contains(@class, 'transfer-form')]")));
        Assertions.assertTrue(transferForm.isDisplayed(), "Transfer form not displayed");
        
        // Return to dashboard
        driver.findElement(By.xpath("//a[contains(text(), 'Dashboard')]")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(), 'Dashboard')]")));
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        loginIfNeeded();
        
        // Test About
        testExternalLink("Sobre", "netlify.com");
        
        // Test Privacy
        testExternalLink("Privacidade", "netlify.com");
        
        // Test Terms
        testExternalLink("Termos", "netlify.com");
    }

    @Test
    @Order(6)
    public void testLogout() {
        loginIfNeeded();
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Sair')]")));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("login"));
        WebElement loginForm = driver.findElement(By.cssSelector("form"));
        Assertions.assertTrue(loginForm.isDisplayed(), "Logout failed - login form not visible");
    }

    private void navigateAndVerify(String linkXpath, String expectedUrlPart, String expectedTitle) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(linkXpath)));
        link.click();
        
        wait.until(ExpectedConditions.urlContains(expectedUrlPart));
        WebElement titleElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//h2[contains(text(), '" + expectedTitle + "')]")));
        Assertions.assertTrue(titleElement.isDisplayed(), expectedTitle + " page not displayed");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String mainWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), '" + linkText + "')]")));
        link.click();
        
        // Switch to new window if opened
        if (driver.getWindowHandles().size() > 1) {
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(mainWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                linkText + " link failed - wrong domain");
            driver.close();
            driver.switchTo().window(mainWindow);
        }
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("dashboard")) {
            driver.get(BASE_URL);
            WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
            WebElement password = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

            email.sendKeys(LOGIN);
            password.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("dashboard"));
        }
    }
}