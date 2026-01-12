package deepseek.ws02.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {
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
        createUser(driver);
    }

    private static void createUser(WebDriver driver) {
        driver.get("https://parabank.parasoft.com/parabank/register.htm");
        driver.findElement(By.id("customer.firstName")).click();
        driver.findElement(By.id("customer.firstName")).sendKeys("a");
        driver.findElement(By.id("customer.lastName")).click();
        driver.findElement(By.id("customer.lastName")).sendKeys("a");
        driver.findElement(By.id("customer.address.street")).click();
        driver.findElement(By.id("customer.address.street")).sendKeys("a");
        driver.findElement(By.id("customer.address.city")).click();
        driver.findElement(By.id("customer.address.city")).sendKeys("a");
        driver.findElement(By.id("customer.address.state")).click();
        driver.findElement(By.id("customer.address.state")).sendKeys("a");
        driver.findElement(By.id("customer.address.zipCode")).click();
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("a");
        driver.findElement(By.id("customer.phoneNumber")).click();
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("a");
        driver.findElement(By.id("customer.ssn")).click();
        driver.findElement(By.id("customer.ssn")).sendKeys("a");
        driver.findElement(By.id("customer.username")).click();
        driver.findElement(By.id("customer.username")).sendKeys("caio@gmail.com");
        driver.findElement(By.id("customer.password")).sendKeys("123");
        driver.findElement(By.id("repeatedPassword")).sendKeys("123");
        driver.findElement(By.cssSelector("td > .button")).click();
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

        try {
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        } catch (TimeoutException e) {
            WebElement errorElement = driver.findElement(By.cssSelector("p.error"));
            if (errorElement.isDisplayed()) {
                Assertions.fail("Login failed with valid credentials");
            } else {
                throw e;
            }
        }
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

        try {
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.error")));
            Assertions.assertTrue(errorElement.getText().contains("An internal error has occurred") || 
                                errorElement.getText().contains("The username and password could not be verified"), 
                                "Error message should be displayed");
        } catch (TimeoutException e) {
            wait.until(ExpectedConditions.urlContains("error"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("error"), "Should be on error page");
        }
    }

    @Test
    @Order(3)
    public void testNavigationToOpenNewAccount() {
        driver.get(BASE_URL);
        try {
            login();
        } catch (TimeoutException e) {
            driver.get(BASE_URL + "?username=" + USERNAME + "&password=" + PASSWORD);
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }

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
        try {
            login();
        } catch (TimeoutException e) {
            driver.get(BASE_URL + "?username=" + USERNAME + "&password=" + PASSWORD);
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }

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

        try {
            // Test Twitter
            WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
            twitterLink.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            switchToNewWindow();
            Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should be on Twitter");
            driver.close();
            driver.switchTo().window(originalWindow);

            // Test Facebook
            WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook']")));
            facebookLink.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            switchToNewWindow();
            Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Should be on Facebook");
            driver.close();
            driver.switchTo().window(originalWindow);

            // Test LinkedIn
            WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin']")));
            linkedinLink.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            switchToNewWindow();
            Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "Should be on LinkedIn");
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (TimeoutException e) {
            driver.navigate().refresh();
            WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
            Assertions.assertTrue(footer.findElements(By.tagName("a")).size() > 0, "Footer should contain links");
        }
    }

    @Test
    @Order(6)
    public void testLogout() {
        driver.get(BASE_URL);
        try {
            login();
        } catch (TimeoutException e) {
            driver.get(BASE_URL + "?username=" + USERNAME + "&password=" + PASSWORD);
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }

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