package deepseek.ws02.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class ParaBankTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
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
    public void testLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("index.htm"));

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".smallText b")).getText().contains("Welcome"));
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorElement.getText().contains("An internal error has occurred"));
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        driver.get(BASE_URL);
        testLoginPage();

        // Test Accounts Overview
        WebElement accountsOverview = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountsOverview.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".title")).getText().contains("Accounts Overview"));

        // Test Transfer Funds
        WebElement transferFunds = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFunds.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".title")).getText().contains("Transfer Funds"));

        // Test Bill Pay
        WebElement billPay = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPay.click();
        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector(".title")).getText().contains("Bill Pay"));

        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Error"));
        
        // Log back in for subsequent tests
        testLoginPage();
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Test Facebook
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook']")));
        facebookLink.click();
        switchToNewWindowAndAssertDomain("facebook.com", originalWindow);

        // Test Twitter
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
        twitterLink.click();
        switchToNewWindowAndAssertDomain("twitter.com", originalWindow);

        // Test LinkedIn
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin']")));
        linkedinLink.click();
        switchToNewWindowAndAssertDomain("linkedin.com", originalWindow);
    }

    private void switchToNewWindowAndAssertDomain(String domain, String originalWindow) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(domain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}