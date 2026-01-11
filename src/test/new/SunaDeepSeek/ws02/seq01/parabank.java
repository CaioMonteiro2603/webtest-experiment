package SunaDeepSeek.ws02.seq01;

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
public class parabank {
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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("img[title='ParaBank']")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).getText().contains("Accounts Overview"));
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("p.error")));
        Assertions.assertTrue(errorMessage.getText().contains("An internal error has occurred"));
    }

    @Test
    @Order(4)
    public void testAccountServicesLinks() {
        driver.get(BASE_URL);
        login();

        List<WebElement> servicesLinks = driver.findElements(By.cssSelector("ul[class='services'] li a"));
        Assertions.assertTrue(servicesLinks.size() > 5, "Should have multiple account services links");

        for (WebElement link : servicesLinks) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("parabank")) {
                link.click();
                wait.until(ExpectedConditions.urlContains(href.substring(href.lastIndexOf("/") + 1)));
                Assertions.assertTrue(driver.findElement(By.cssSelector("h1.title")).isDisplayed());
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains("overview.htm"));
            }
        }
    }

    @Test
    @Order(5)
    public void testTransferFunds() {
        driver.get(BASE_URL);
        login();

        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Transfer Funds")));
        transferFundsLink.click();

        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        Select fromAccount = new Select(driver.findElement(By.id("fromAccountId")));
        Select toAccount = new Select(driver.findElement(By.id("toAccountId")));
        WebElement amountField = driver.findElement(By.id("amount"));
        WebElement transferButton = driver.findElement(By.cssSelector("input[value='Transfer']"));

        fromAccount.selectByIndex(0);
        toAccount.selectByIndex(1);
        amountField.sendKeys("100");
        transferButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(successMessage.getText().contains("Transfer Complete"));
    }

    @Test
    @Order(6)
    public void testBillPay() {
        driver.get(BASE_URL);
        login();

        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Bill Pay")));
        billPayLink.click();

        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        WebElement payeeName = driver.findElement(By.name("payee.name"));
        WebElement address = driver.findElement(By.name("payee.address.street"));
        WebElement city = driver.findElement(By.name("payee.address.city"));
        WebElement state = driver.findElement(By.name("payee.address.state"));
        WebElement zipCode = driver.findElement(By.name("payee.address.zipCode"));
        WebElement phone = driver.findElement(By.name("payee.phoneNumber"));
        WebElement account = driver.findElement(By.name("payee.accountNumber"));
        WebElement verifyAccount = driver.findElement(By.name("verifyAccount"));
        WebElement amount = driver.findElement(By.name("amount"));
        WebElement sendPaymentButton = driver.findElement(By.cssSelector("input[value='Send Payment']"));

        payeeName.sendKeys("Test Payee");
        address.sendKeys("123 Test St");
        city.sendKeys("Test City");
        state.sendKeys("CA");
        zipCode.sendKeys("90210");
        phone.sendKeys("5551234567");
        account.sendKeys("12345");
        verifyAccount.sendKeys("12345");
        amount.sendKeys("50");
        sendPaymentButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(successMessage.getText().contains("Bill Payment Complete"));
    }

    @Test
    @Order(7)
    public void testFindTransactions() {
        driver.get(BASE_URL);
        login();

        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Find Transactions")));
        findTransactionsLink.click();

        wait.until(ExpectedConditions.urlContains("findtrans.htm"));
        WebElement accountId = driver.findElement(By.id("accountId"));
        WebElement findTransactionsButton = driver.findElement(By.cssSelector("button[type='submit']"));

        Select accountSelect = new Select(accountId);
        accountSelect.selectByIndex(0);
        findTransactionsButton.click();

        WebElement resultsTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("transactionTable")));
        Assertions.assertTrue(resultsTable.findElements(By.tagName("tr")).size() > 1);
    }

    @Test
    @Order(8)
    public void testRequestLoan() {
        driver.get(BASE_URL);
        login();

        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Request Loan")));
        requestLoanLink.click();

        wait.until(ExpectedConditions.urlContains("requestloan.htm"));
        WebElement amount = driver.findElement(By.id("amount"));
        WebElement downPayment = driver.findElement(By.id("downPayment"));
        WebElement fromAccountId = driver.findElement(By.id("fromAccountId"));
        WebElement applyNowButton = driver.findElement(By.cssSelector("input[value='Apply Now']"));

        amount.sendKeys("5000");
        downPayment.sendKeys("1000");
        new Select(fromAccountId).selectByIndex(0);
        applyNowButton.click();

        WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(result.getText().contains("Loan Request Processed"));
    }

    @Test
    @Order(9)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        login();

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("ul[class='footerlinks'] li a"));
        Assertions.assertTrue(footerLinks.size() > 0, "Should have footer links");

        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.contains("parabank")) {
                String originalWindow = driver.getWindowHandle();
                link.click();
                
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!originalWindow.contentEquals(windowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                Assertions.assertTrue(driver.getCurrentUrl().contains(href.substring(href.indexOf("//") + 2).split("/")[0]));
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(10)
    public void testLogout() {
        driver.get(BASE_URL);
        login();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(driver.findElement(By.name("username")).isDisplayed());
    }

    private void login() {
        if (driver.getCurrentUrl().contains("index.htm")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }
}