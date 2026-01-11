package Qwen3.ws02.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class parabank {
    private static WebDriver driver;
    private static WebDriverWait wait;

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
    public void testValidLogin() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit'][value='Log In']"));
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2")));
        assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Welcome"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("invalid_user");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("invalid_password");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit'][value='Log In']"));
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("p.error")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Invalid"));
    }

    @Test
    @Order(3)
    public void testNavigation() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit'][value='Log In']"));
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2")));
        assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Welcome"));

        // Navigate to Accounts Overview
        WebElement accountsLink = driver.findElement(By.linkText("Accounts Overview"));
        accountsLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"));
        
        // Navigate to Transfer Funds
        WebElement transferFundsLink = driver.findElement(By.linkText("Transfer Funds"));
        transferFundsLink.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        assertTrue(driver.getCurrentUrl().contains("transfer.htm"));

        // Navigate to Bill Pay
        WebElement billPayLink = driver.findElement(By.linkText("Bill Pay"));
        billPayLink.click();
        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        assertTrue(driver.getCurrentUrl().contains("billpay.htm"));

        // Navigate back to Welcome
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        homeLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2")));
        assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Welcome"));
    }

    @Test
    @Order(4)
    public void testAccountsOverview() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit'][value='Log In']"));
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2")));
        WebElement accountsLink = driver.findElement(By.linkText("Accounts Overview"));
        accountsLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));

        List<WebElement> accountRows = driver.findElements(By.cssSelector("#accountTable tbody tr"));
        assertTrue(accountRows.size() > 0);
        
        WebElement accountDetailsButton = driver.findElement(By.cssSelector("a[href*='account.htm']"));
        accountDetailsButton.click();
        wait.until(ExpectedConditions.urlContains("account.htm"));
        assertTrue(driver.getCurrentUrl().contains("account.htm"));
        
        // Go back to Accounts Overview
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");

        List<WebElement> footerLinks = driver.findElements(By.cssSelector(".footer a"));
        assertTrue(footerLinks.size() >= 2);

        String originalHandle = driver.getWindowHandle();

        // Test About Us
        WebElement aboutUsLink = driver.findElement(By.linkText("About Us"));
        aboutUsLink.click();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("parasoft.com"));
        driver.close();
        driver.switchTo().window(originalHandle);

        //Test Contact
        WebElement contactLink = driver.findElement(By.linkText("Contact"));
        contactLink.click();
        handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("parasoft.com"));
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(6)
    public void testLogout() {
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        usernameField.sendKeys("caio@gmail.com");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("123");
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit'][value='Log In']"));
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2")));
        assertTrue(driver.findElement(By.cssSelector("h2")).getText().contains("Welcome"));

        WebElement logoutLink = driver.findElement(By.linkText("Log Out"));
        logoutLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        assertTrue(driver.getCurrentUrl().contains("index.htm") || driver.getCurrentUrl().contains("login.htm"));
        assertTrue(driver.findElement(By.name("username")).isDisplayed());
    }
}