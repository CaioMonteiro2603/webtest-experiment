package Qwen3.ws02.seq01;

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
public class ParaBankTest {

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
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("welcome.htm"), "Login should redirect to welcome page");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.errorMessage")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed on invalid login");
    }

    @Test
    @Order(3)
    public void testAccountOverview() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        // Wait for account overview to load
        wait.until(ExpectedConditions.urlContains("welcome.htm"));

        // View account details
        WebElement accountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("accounts.htm"), "Should navigate to accounts overview page");
    }

    @Test
    @Order(4)
    public void testTransferFunds() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("caio@gmail.com");
        passwordField.sendKeys("123");
        loginButton.click();

        // Navigate to transfer funds
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("transfer.htm"), "Should navigate to transfer funds page");

        // Fill form
        WebElement fromAccount = driver.findElement(By.id("fromAccountId"));
        WebElement toAccount = driver.findElement(By.id("toAccountId"));
        WebElement amount = driver.findElement(By.id("amount"));
        WebElement transferButton = driver.findElement(By.xpath("//input[@value='Transfer']"));

        fromAccount.sendKeys("12345");
        toAccount.sendKeys("12346");
        amount.sendKeys("100");

        transferButton.click();

        // Verify successful transfer
        WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.message")));
        assertTrue(successMessage.getText().contains("Transfer Complete"), "Transfer should be complete successfully");
    }

    @Test
    @Order(5)
    public void testServices() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        // Click services link directly since we don't have a valid login to test other areas
        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("services.htm"), "Should navigate to services page");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        String parentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(parentWindow)) {
                driver.switchTo().window(window);
                driver.close();
            }
        }

        // Footer links - About Us
        WebElement aboutLink = driver.findElement(By.cssSelector("a[href*='about']"));
        aboutLink.click();
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("about"), "Should open About Us link");
        driver.close();
        driver.switchTo().window(parentWindow);

        // Footer links - Contact Us
        WebElement contactLink = driver.findElement(By.cssSelector("a[href*='contact']"));
        contactLink.click();

        currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("contact"), "Should open Contact Us link");
        driver.close();
        driver.switchTo().window(parentWindow);
    }
}