package SunaQwen3.ws02.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.titleContains;

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
    public void testValidLogin() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().contains("ParaBank"), "Page title should contain 'ParaBank'");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should be redirected to overview page after login");
        Assertions.assertTrue(driver.getPageSource().contains("Accounts Overview"), "Accounts Overview page should be displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invaliduser");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should appear for invalid login");
    }

    @Test
    @Order(3)
    public void testAccessAdminPageViaMenu() {
        // Ensure logged in
        loginIfNot();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        menuButton.click();

        wait.until(ExpectedConditions.urlContains("admin.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("admin.htm"), "Should navigate to admin page");
        Assertions.assertTrue(driver.getTitle().contains("ParaBank Administration"), "Admin page title should be correct");
    }

    @Test
    @Order(4)
    public void testAccessAboutPageExternalLink() {
        loginIfNot();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutLink.click();

        wait.until(ExpectedConditions.urlContains("about.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about.htm"), "Should navigate to about page");
        Assertions.assertTrue(driver.getTitle().contains("About"), "About page title should contain 'About'");
    }

    @Test
    @Order(5)
    public void testAccessServicesPage() {
        loginIfNot();

        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();

        wait.until(ExpectedConditions.urlContains("services.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("services.htm"), "Should navigate to services page");
        Assertions.assertTrue(driver.getTitle().contains("Services"), "Services page title should be correct");
    }

    @Test
    @Order(6)
    public void testAccessContactPage() {
        loginIfNot();

        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();

        wait.until(ExpectedConditions.urlContains("contact.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("contact.htm"), "Should navigate to contact page");
        Assertions.assertTrue(driver.getTitle().contains("Contact"), "Contact page title should be correct");
    }

    @Test
    @Order(7)
    public void testFooterTwitterLinkExternal() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, 'twitter.com')]")));
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open in new tab with correct domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testFooterFacebookLinkExternal() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, 'facebook.com')]")));
        facebookLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("facebook.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open in new tab with correct domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testFooterLinkedInLinkExternal() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, 'linkedin.com')]")));
        linkedinLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open in new tab with correct domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testOpenNewAccount() {
        loginIfNot();

        WebElement openNewAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openNewAccountLink.click();

        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("openaccount.htm"), "Should navigate to open account page");

        WebElement accountTypeDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("type")));
        accountTypeDropdown.click();
        driver.findElement(By.cssSelector("#type option[value='1']")).click(); // Savings

        WebElement fromAccountId = driver.findElement(By.id("fromAccountId"));
        fromAccountId.click();
        driver.findElement(By.cssSelector("#fromAccountId option:first-child")).click();

        WebElement openAccountButton = driver.findElement(By.xpath("//input[@value='Open New Account']"));
        openAccountButton.click();

        WebElement confirmationMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".title")));
        Assertions.assertTrue(confirmationMessage.getText().contains("Account Opened!"), "Account should be opened successfully");
        Assertions.assertTrue(driver.getCurrentUrl().contains("accountoverview.htm"), "Should redirect to account overview after opening new account");
    }

    @Test
    @Order(11)
    public void testTransferFunds() {
        loginIfNot();

        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();

        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("transfer.htm"), "Should navigate to transfer funds page");

        WebElement amountField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        amountField.sendKeys("100");

        WebElement fromAccount = driver.findElement(By.id("fromAccountId"));
        fromAccount.click();
        driver.findElement(By.cssSelector("#fromAccountId option:first-child")).click();

        WebElement toAccount = driver.findElement(By.id("toAccountId"));
        toAccount.click();
        List<WebElement> toAccountOptions = driver.findElements(By.cssSelector("#toAccountId option"));
        if (toAccountOptions.size() > 1) {
            toAccountOptions.get(1).click();
        } else {
            toAccountOptions.get(0).click();
        }

        WebElement transferButton = driver.findElement(By.xpath("//input[@value='Transfer']"));
        transferButton.click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".title")));
        Assertions.assertTrue(confirmation.getText().contains("Transfer Complete!"), "Funds transfer should be successful");
    }

    @Test
    @Order(12)
    public void testBillPay() {
        loginIfNot();

        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();

        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("billpay.htm"), "Should navigate to bill pay page");

        // Fill payee information
        driver.findElement(By.id("payee.name")).sendKeys("John Doe");
        driver.findElement(By.id("payee.address.street")).sendKeys("123 Main St");
        driver.findElement(By.id("payee.address.city")).sendKeys("Anytown");
        driver.findElement(By.id("payee.address.state")).sendKeys("CA");
        driver.findElement(By.id("payee.address.zipCode")).sendKeys("90210");
        driver.findElement(By.id("payee.phoneNumber")).sendKeys("555-123-4567");
        driver.findElement(By.id("payee.accountNumber")).sendKeys("123456789");
        driver.findElement(By.id("verifyAccount")).sendKeys("123456789");

        // Fill payment details
        driver.findElement(By.id("amount")).sendKeys("50");

        WebElement fromAccountId = driver.findElement(By.id("fromAccountId"));
        fromAccountId.click();
        driver.findElements(By.cssSelector("#fromAccountId option")).get(0).click();

        // Send payment
        driver.findElement(By.xpath("//input[@value='Send Payment']")).click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".title")));
        Assertions.assertTrue(confirmation.getText().contains("Bill Payment to John Doe Complete"), "Bill payment should be successful");
    }

    @Test
    @Order(13)
    public void testFindTransactions() {
        loginIfNot();

        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions")));
        findTransactionsLink.click();

        wait.until(ExpectedConditions.urlContains("findtrans.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("findtrans.htm"), "Should navigate to find transactions page");

        // Search by transaction ID
        WebElement transactionIdField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("transactionId")));
        transactionIdField.sendKeys("12345");
        driver.findElement(By.xpath("//input[@value='Find']")).click();

        // Verify results
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".title")));
        Assertions.assertTrue(driver.getPageSource().contains("Transaction"), "Search results should be displayed");
    }

    @Test
    @Order(14)
    public void testUpdateContactInfo() {
        loginIfNot();

        WebElement updateProfileLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info")));
        updateProfileLink.click();

        wait.until(ExpectedConditions.urlContains("updateprofile.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("updateprofile.htm"), "Should navigate to update profile page");

        // Update fields
        WebElement customerFirstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName")));
        customerFirstName.clear();
        customerFirstName.sendKeys("Caio");

        WebElement customerLastName = driver.findElement(By.id("customer.lastName"));
        customerLastName.clear();
        customerLastName.sendKeys("Silva");

        WebElement customerAddress = driver.findElement(By.id("customer.address.street"));
        customerAddress.clear();
        customerAddress.sendKeys("456 Oak Ave");

        WebElement customerCity = driver.findElement(By.id("customer.address.city"));
        customerCity.clear();
        customerCity.sendKeys("Springfield");

        WebElement customerState = driver.findElement(By.id("customer.address.state"));
        customerState.clear();
        customerState.sendKeys("IL");

        WebElement customerZipCode = driver.findElement(By.id("customer.address.zipCode"));
        customerZipCode.clear();
        customerZipCode.sendKeys("62701");

        WebElement customerPhone = driver.findElement(By.id("customer.phoneNumber"));
        customerPhone.clear();
        customerPhone.sendKeys("555-987-6543");

        // Submit
        driver.findElement(By.xpath("//input[@value='Update Profile']")).click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".title")));
        Assertions.assertTrue(confirmation.getText().contains("Profile Updated"), "Profile should be updated successfully");
    }

    @Test
    @Order(15)
    public void testLogout() {
        loginIfNot();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(titleContains("ParaBank"));
        Assertions.assertTrue(driver.getTitle().contains("ParaBank"), "Should return to home page after logout");
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should be redirected to home page after logout");
        Assertions.assertTrue(driver.getPageSource().contains("Customer Login"), "Login form should be visible after logout");
    }

    private void loginIfNot() {
        driver.get(BASE_URL);
        try {
            WebElement logoutLink = driver.findElement(By.linkText("Log Out"));
            if (logoutLink.isDisplayed()) {
                return; // Already logged in
            }
        } catch (NoSuchElementException e) {
            // Not logged in, proceed with login
        }

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }
}