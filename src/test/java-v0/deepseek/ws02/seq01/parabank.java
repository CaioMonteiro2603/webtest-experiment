package deepseek.ws02.seq01;

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
    private static final String LOGIN = "caio@gmail.com";
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
    public void testLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview"));
        WebElement accountsOverview = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountTable")));
        Assertions.assertTrue(accountsOverview.isDisplayed(), "Accounts overview should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("p.error")));
        Assertions.assertTrue(errorMessage.getText().contains("Error"), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testAccountServicesNavigation() {
        driver.get(BASE_URL);
        login();

        WebElement openNewAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openNewAccountLink.click();
        wait.until(ExpectedConditions.urlContains("openaccount"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Open New Account"), "Should navigate to Open New Account page");

        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountsOverviewLink.click();
        wait.until(ExpectedConditions.urlContains("overview"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Accounts Overview"), "Should navigate back to Accounts Overview");

        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();
        wait.until(ExpectedConditions.urlContains("transfer"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Transfer Funds"), "Should navigate to Transfer Funds page");

        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();
        wait.until(ExpectedConditions.urlContains("billpay"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Bill Pay Service"), "Should navigate to Bill Pay page");
    }

    @Test
    @Order(4)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        login();

        String currentWindow = driver.getWindowHandle();
        
        WebElement aboutUsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutUsLink.click();
        wait.until(ExpectedConditions.urlContains("about"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("About Us"), "Should navigate to About Us page");

        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();
        wait.until(ExpectedConditions.urlContains("services"));
        Assertions.assertTrue(driver.findElement(By.tagName("h1")).getText().contains("Services"), "Should navigate to Services page");

        WebElement parasoftLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("www.parasoft.com")));
        parasoftLink.click();
        switchToNewWindowAndAssertDomain("parasoft.com");

        driver.switchTo().window(currentWindow);
    }

    @Test
    @Order(5)
    public void testRequestLoanFunctionality() {
        driver.get(BASE_URL);
        login();

        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan")));
        requestLoanLink.click();
        wait.until(ExpectedConditions.urlContains("requestloan"));

        WebElement loanAmountField = wait.until(ExpectedConditions.elementToBeClickable(By.id("amount")));
        WebElement downPaymentField = driver.findElement(By.id("downPayment"));
        WebElement applyNowButton = driver.findElement(By.cssSelector("input[value='Apply Now']"));

        loanAmountField.sendKeys("1000");
        downPaymentField.sendKeys("100");
        applyNowButton.click();

        WebElement resultMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#loanStatus")));
        Assertions.assertTrue(resultMessage.isDisplayed(), "Loan application result should be displayed");
    }

    @Test
    @Order(6)
    public void testLogout() {
        driver.get(BASE_URL);
        login();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.htm"));
        WebElement loginPanel = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginPanel")));
        Assertions.assertTrue(loginPanel.isDisplayed(), "Should be back to login page after logout");
    }

    private void login() {
        if (driver.getCurrentUrl().contains("index.htm") && driver.findElements(By.name("username")).size() > 0) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

            usernameField.sendKeys(LOGIN);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("overview"));
        }
    }

    private void switchToNewWindowAndAssertDomain(String expectedDomain) {
        String currentWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        driver.close();
        driver.switchTo().window(currentWindow);
    }
}