package Qwen3.ws02.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class ParaBankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement customerName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("customer-name")));
        assertTrue(customerName.getText().contains("Caio"), "Customer name should appear after login");

        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "URL should contain overview.htm after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrong");
        loginButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        assertTrue(error.isDisplayed(), "Error message should be visible");
        assertTrue(error.getText().contains("could not be validated"), "Error message should indicate failure");
    }

    @Test
    @Order(3)
    void testNavigationToAccountsOverview() {
        loginIfNecessary();

        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountsOverviewLink.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should navigate to accounts overview");
    }

    @Test
    @Order(4)
    void testNavigationToTransferFunds() {
        loginIfNecessary();

        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();

        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        assertTrue(driver.getCurrentUrl().contains("transfer.htm"), "Should navigate to transfer funds page");

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Transfer Funds", pageTitle.getText(), "Page title should be 'Transfer Funds'");
    }

    @Test
    @Order(5)
    void testTransferFundsSuccess() {
        loginIfNecessary();

        driver.get("https://parabank.parasoft.com/parabank/transfer.htm");

        WebElement amountField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        WebElement fromAccountId = driver.findElement(By.id("fromAccountId"));
        WebElement toAccountId = driver.findElement(By.id("toAccountId"));

        // Use first and second account IDs (assuming exist)
        String fromId = fromAccountId.getText().split("\n")[0];
        String toId = toAccountId.getText().split("\n")[1];

        fromAccountId.sendKeys(fromId);
        toAccountId.sendKeys(toId);
        amountField.sendKeys("50");

        WebElement transferButton = driver.findElement(By.xpath("//input[@value='Transfer']"));
        transferButton.click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transfer Complete!']")));
        assertTrue(confirmation.isDisplayed(), "Transfer confirmation should appear");

        assertTrue(driver.getCurrentUrl().contains("transfer.htm"), "Should remain on transfer page");
        assertTrue(driver.getPageSource().contains("50"), "Transferred amount should be displayed");
    }

    @Test
    @Order(6)
    void testNavigationToBillPay() {
        loginIfNecessary();

        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();

        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        assertTrue(driver.getCurrentUrl().contains("billpay.htm"), "Should navigate to bill pay page");

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Bill Payment Service", pageTitle.getText(), "Page title should be 'Bill Payment Service'");
    }

    @Test
    @Order(7)
    void testBillPaymentSuccess() {
        loginIfNecessary();

        driver.get("https://parabank.parasoft.com/parabank/billpay.htm");

        WebElement payeeName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("payee.name")));
        payeeName.sendKeys("Test Payee");

        driver.findElement(By.id("payee.address.street")).sendKeys("123 Main St");
        driver.findElement(By.id("payee.address.city")).sendKeys("New York");
        driver.findElement(By.id("payee.address.state")).sendKeys("NY");
        driver.findElement(By.id("payee.address.zipCode")).sendKeys("10001");
        driver.findElement(By.id("payee.phoneNumber")).sendKeys("555-1234");
        driver.findElement(By.id("payee.accountNumber")).sendKeys("500");
        driver.findElement(By.id("verifyAccount")).sendKeys("500");
        driver.findElement(By.id("amount")).sendKeys("40");

        WebElement fromAccountId = driver.findElement(By.id("fromAccountId"));
        String firstAccountId = new Select(fromAccountId).getOptions().get(1).getText();
        fromAccountId.sendKeys(firstAccountId);

        WebElement sendPaymentButton = driver.findElement(By.xpath("//input[@value='Send Payment']"));
        sendPaymentButton.click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Bill Payment Complete']")));
        assertTrue(confirmation.isDisplayed(), "Bill payment confirmation should appear");
    }

    @Test
    @Order(8)
    void testNavigationToFindTransactions() {
        loginIfNecessary();

        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions")));
        findTransactionsLink.click();

        wait.until(ExpectedConditions.urlContains("activity.htm"));
        assertTrue(driver.getCurrentUrl().contains("activity.htm"), "Should navigate to find transactions page");

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Find Transactions", pageTitle.getText(), "Page title should be 'Find Transactions'");
    }

    @Test
    @Order(9)
    void testFindTransactionsSuccess() {
        loginIfNecessary();

        driver.get("https://parabank.parasoft.com/parabank/activity.htm");

        WebElement accountIdField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountId")));
        accountIdField.sendKeys("1000"); // Assuming account exists

        WebElement findButton = driver.findElement(By.xpath("//input[@value='Find']"));
        findButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transactionTable")));
        assertTrue(driver.getPageSource().contains("Transactions for Account"), "Transaction results should load");
    }

    @Test
    @Order(10)
    void testNavigationToUpdateContactInfo() {
        loginIfNecessary();

        WebElement updateContactInfoLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info")));
        updateContactInfoLink.click();

        wait.until(ExpectedConditions.urlContains("updateprofile.htm"));
        assertTrue(driver.getCurrentUrl().contains("updateprofile.htm"), "Should navigate to update contact info");

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Update Profile", pageTitle.getText(), "Page title should be 'Update Profile'");
    }

    @Test
    @Order(11)
    void testUpdateContactInfoSuccess() {
        loginIfNecessary();

        driver.get("https://parabank.parasoft.com/parabank/updateprofile.htm");

        WebElement customerFirstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName")));
        WebElement customerLastName = driver.findElement(By.id("customer.lastName"));
        WebElement customerAddress = driver.findElement(By.id("customer.address.street"));
        WebElement customerCity = driver.findElement(By.id("customer.address.city"));
        WebElement customerState = driver.findElement(By.id("customer.address.state"));
        WebElement customerZipCode = driver.findElement(By.id("customer.address.zipCode"));
        WebElement customerPhone = driver.findElement(By.id("customer.phoneNumber"));

        customerFirstName.clear();
        customerFirstName.sendKeys("Caio");
        customerLastName.clear();
        customerLastName.sendKeys("Silva");
        customerAddress.clear();
        customerAddress.sendKeys("456 Oak Ave");
        customerCity.clear();
        customerCity.sendKeys("Los Angeles");
        customerState.clear();
        customerState.sendKeys("CA");
        customerZipCode.clear();
        customerZipCode.sendKeys("90001");
        customerPhone.clear();
        customerPhone.sendKeys("555-6789");

        WebElement updateProfileButton = driver.findElement(By.xpath("//input[@value='Update Profile']"));
        updateProfileButton.click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        assertFalse(confirmation.isDisplayed(), "There should be no error on profile update");

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Your profile has been updated.')]")));
        assertTrue(successMessage.isDisplayed(), "Success message should appear");
    }

    @Test
    @Order(12)
    void testNavigationToRequestLoan() {
        loginIfNecessary();

        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan")));
        requestLoanLink.click();

        wait.until(ExpectedConditions.urlContains("loan.htm"));
        assertTrue(driver.getCurrentUrl().contains("loan.htm"), "Should navigate to loan request page");

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Loan Request", pageTitle.getText(), "Page title should be 'Loan Request'");
    }

    @Test
    @Order(13)
    void testRequestLoanApproved() {
        loginIfNecessary();

        driver.get("https://parabank.parasoft.com/parabank/loan.htm");

        WebElement amountField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        amountField.sendKeys("10000");

        WebElement downPaymentField = driver.findElement(By.id("downPayment"));
        downPaymentField.sendKeys("2000");

        WebElement fromAccountId = driver.findElement(By.id("fromAccountId"));
        String firstAccountId = new Select(fromAccountId).getOptions().get(1).getText();
        fromAccountId.sendKeys(firstAccountId);

        WebElement applyButton = driver.findElement(By.xpath("//input[@value='Apply Now']"));
        applyButton.click();

        WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loanStatus")));
        assertTrue(result.getText().contains("Approved"), "Loan should be approved");
    }

    @Test
    @Order(14)
    void testNavigationToAdminPage() {
        loginIfNecessary();

        WebElement adminPageLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("ParaBank Admin Page")));
        String originalWindow = driver.getWindowHandle();
        adminPageLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String adminUrl = driver.getCurrentUrl();
        assertTrue(adminUrl.contains("parabank"), "Admin page URL should belong to parabank domain");
        assertTrue(driver.getPageSource().contains("Admin"), "Admin page should load content");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(15)
    void testFooterAboutUsExternalLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement aboutUsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutUsLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String aboutUrl = driver.getCurrentUrl();
        assertTrue(aboutUrl.contains("parasoft.com"), "About Us link should redirect to Parasoft domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(16)
    void testFooterServicesExternalLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String servicesUrl = driver.getCurrentUrl();
        assertTrue(servicesUrl.contains("parasoft.com"), "Services link should redirect to Parasoft domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(17)
    void testFooterProductsExternalLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement productsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Products")));
        productsLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String productsUrl = driver.getCurrentUrl();
        assertTrue(productsUrl.contains("parasoft.com"), "Products link should redirect to Parasoft domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(18)
    void testFooterLocationsExternalLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement locationsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Locations")));
        locationsLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String locationsUrl = driver.getCurrentUrl();
        assertTrue(locationsUrl.contains("parasoft.com"), "Locations link should redirect to Parasoft domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(19)
    void testLogout() {
        loginIfNecessary();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should return to home page after logout");
        assertTrue(driver.findElement(By.xpath("//input[@value='Log In']")).isDisplayed(), "Login button should be visible");
    }

    private void loginIfNecessary() {
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            driver.get(BASE_URL);
            try {
                WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
                WebElement passwordField = driver.findElement(By.name("password"));
                WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

                usernameField.clear();
                passwordField.clear();
                usernameField.sendKeys(USERNAME);
                passwordField.sendKeys(PASSWORD);
                loginButton.click();

                wait.until(ExpectedConditions.urlContains("overview.htm"));
            } catch (TimeoutException e) {
                // Already logged in possibly
            }
        }
    }
}