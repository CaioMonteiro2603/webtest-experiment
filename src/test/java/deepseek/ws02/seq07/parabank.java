package deepseek.ws02.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParaBankTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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
    public void testLoginSuccess() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Accounts Overview"), "Accounts Overview page should be displayed after login");
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

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.error")));
        Assertions.assertTrue(errorElement.getText().contains("An internal error has occurred"), "Error message should be displayed");
    }

    @Test
    @Order(3)
    public void testNavigationToOpenNewAccount() {
        driver.get(BASE_URL);
        login();

        WebElement openNewAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openNewAccountLink.click();

        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        WebElement accountTypeDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("type")));
        Assertions.assertTrue(accountTypeDropdown.isDisplayed(), "Open New Account page should be displayed");
    }

    @Test
    @Order(4)
    public void testNavigationToTransferFunds() {
        driver.get(BASE_URL);
        login();

        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();

        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        WebElement transferButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[value='Transfer']")));
        Assertions.assertTrue(transferButton.isDisplayed(), "Transfer Funds page should be displayed");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Test Twitter
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Twitter")));
        twitterLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should be on Twitter");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Facebook")));
        facebookLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Should be on Facebook");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("LinkedIn")));
        linkedinLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "Should be on LinkedIn");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testLogout() {
        driver.get(BASE_URL);
        login();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.htm"));
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[value='Log In']")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Should be back on login page after logout");
    }

    private void login() {
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }

    private void switchToNewWindow() {
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
}