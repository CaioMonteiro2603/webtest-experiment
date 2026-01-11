package SunaDeepSeek.ws02.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("div[id='topPanel'] img")));
        Assertions.assertTrue(logo.isDisplayed(), "Logo should be visible");
        Assertions.assertTrue(driver.getTitle().contains("ParaBank"), 
            "Page title should contain 'ParaBank'");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1.title")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Accounts Overview"),
            "Should be logged in and see Accounts Overview");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.className("error")));
        Assertions.assertTrue(errorMessage.getText().contains("error"),
            "Should show error message for invalid login");
    }

    @Test
    @Order(4)
    public void testAccountServicesLinks() {
        login();
        
        List<WebElement> servicesLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("ul[class='services'] li a")));
        Assertions.assertTrue(servicesLinks.size() > 0, "Should have account services links");

        for (WebElement link : servicesLinks) {
            link.click();
            
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("index.htm")));
            Assertions.assertTrue(driver.getCurrentUrl().contains("parabank"),
                "Should navigate to ParaBank subpage");
            
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        List<WebElement> footerLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("div#footermain ul li a")));
        Assertions.assertTrue(footerLinks.size() > 0, "Should have footer links");

        for (WebElement link : footerLinks) {
            String originalWindow = driver.getWindowHandle();
            String linkUrl = link.getAttribute("href");
            if (linkUrl != null && linkUrl.startsWith("http") && !linkUrl.contains("parabank")) {
                ((JavascriptExecutor)driver).executeScript("window.open(arguments[0]);", linkUrl);
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!originalWindow.equals(windowHandle)) {
                        driver.switchTo().window(windowHandle);
                        Assertions.assertTrue(driver.getCurrentUrl().contains(linkUrl.substring(0, 15)),
                            "External link should open correct domain");
                        driver.close();
                        driver.switchTo().window(originalWindow);
                        break;
                    }
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testOpenNewAccount() {
        login();
        
        WebElement openNewAccountLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Open New Account")));
        openNewAccountLink.click();
        
        WebElement accountTypeDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("type")));
        accountTypeDropdown.click();
        
        List<WebElement> options = driver.findElements(By.cssSelector("select#type option"));
        Assertions.assertTrue(options.size() >= 2, "Should have multiple account type options");
        
        WebElement openAccountButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='Open New Account']")));
        openAccountButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("div#rightPanel h1.title")));
        Assertions.assertTrue(successMessage.getText().contains("Account Opened!"),
            "Should show account opened confirmation");
    }

    @Test
    @Order(7)
    public void testTransferFunds() {
        login();
        
        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Transfer Funds")));
        transferFundsLink.click();
        
        WebElement amountField = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("amount")));
        amountField.sendKeys("100");
        
        WebElement transferButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='Transfer']")));
        transferButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("div#rightPanel h1.title")));
        Assertions.assertTrue(successMessage.getText().contains("Transfer Complete!"),
            "Should show transfer complete confirmation");
    }

    @Test
    @Order(8)
    public void testBillPay() {
        login();
        
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Bill Pay")));
        billPayLink.click();
        
        WebElement payeeName = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("payee.name")));
        payeeName.sendKeys("Test Payee");
        
        WebElement address = driver.findElement(By.name("payee.address.street"));
        address.sendKeys("123 Test St");
        
        WebElement city = driver.findElement(By.name("payee.address.city"));
        city.sendKeys("Test City");
        
        WebElement state = driver.findElement(By.name("payee.address.state"));
        state.sendKeys("CA");
        
        WebElement zipCode = driver.findElement(By.name("payee.address.zipCode"));
        zipCode.sendKeys("12345");
        
        WebElement phone = driver.findElement(By.name("payee.phoneNumber"));
        phone.sendKeys("1234567890");
        
        WebElement account = driver.findElement(By.name("payee.accountNumber"));
        account.sendKeys("12345");
        
        WebElement verifyAccount = driver.findElement(By.name("verifyAccount"));
        verifyAccount.sendKeys("12345");
        
        WebElement amount = driver.findElement(By.name("amount"));
        amount.sendKeys("100");
        
        WebElement sendPaymentButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[value='Send Payment']")));
        sendPaymentButton.click();
        
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("div#rightPanel h1.title")));
        Assertions.assertTrue(successMessage.getText().contains("Bill Payment Complete"),
            "Should show bill payment complete confirmation");
    }

    @Test
    @Order(9)
    public void testFindTransactions() {
        login();
        
        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Find Transactions")));
        findTransactionsLink.click();
        
        WebElement accountId = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("accountId")));
        accountId.click();
        
        WebElement findTransactionsButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[type='submit']")));
        findTransactionsButton.click();
        
        WebElement resultsTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("table#transactionTable")));
        Assertions.assertTrue(resultsTable.isDisplayed(), "Should show transactions results");
    }

    @Test
    @Order(10)
    public void testLogout() {
        login();
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Log Out")));
        logoutLink.click();
        
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("form[name='login']")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Should be logged out and see login form");
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }
}