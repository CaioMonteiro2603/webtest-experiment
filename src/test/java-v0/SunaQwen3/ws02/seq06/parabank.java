package SunaQwen3.ws02.seq06;

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
public class parabank {
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
        assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle());

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should be redirected to overview after login");
        assertTrue(driver.getPageSource().contains("Accounts Overview"), "Accounts Overview should be present");
    }

    @Test
    @Order(2)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertTrue(errorMessage.getText().contains("Please check your username and password"),
                "Error message should indicate invalid credentials");
        assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should remain on login page");
    }

    @Test
    @Order(3)
    void testAccessAdminPageFromFooter() {
        testValidLogin(); // Ensure we're logged in

        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        adminLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Admin"));
        assertTrue(driver.getCurrentUrl().contains("admin.htm"), "Should navigate to admin page");
        assertTrue(driver.getPageSource().contains("Parameter Name"), "Admin page content should load");
    }

    @Test
    @Order(4)
    void testExternalLinksInFooter() {
        testValidLogin(); // Ensure we're logged in

        // Find all footer links
        By footerLinksLocator = By.cssSelector("div.footer a");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(footerLinksLocator));
        java.util.List<WebElement> footerLinks = driver.findElements(footerLinksLocator);

        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            String target = link.getAttribute("target");
            String linkText = link.getText().toLowerCase();

            if (href == null || href.isEmpty() || href.startsWith("javascript")) {
                continue;
            }

            if (target != null && target.equals("_blank")) {
                String originalWindow = driver.getWindowHandle();
                link.click();

                // Wait for new window and switch
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(originalWindow)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }

                // Assert URL contains expected domain based on link text
                String currentUrl = driver.getCurrentUrl();
                if (linkText.contains("twitter")) {
                    assertTrue(currentUrl.contains("twitter.com") || currentUrl.contains("x.com"),
                            "Twitter link should open correct domain");
                } else if (linkText.contains("facebook")) {
                    assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open correct domain");
                } else if (linkText.contains("youtube")) {
                    assertTrue(currentUrl.contains("youtube.com"), "YouTube link should open correct domain");
                } else if (linkText.contains("blog")) {
                    assertTrue(currentUrl.contains("parasoft.com"), "Blog link should open correct domain");
                }

                // Close new tab and return
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(5)
    void testNavigationMenuOptions() {
        testValidLogin(); // Ensure we're logged in

        // Click on Customer Login menu (already logged in)
        WebElement customerLoginLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Customer Login")));
        customerLoginLink.click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle());

        // Re-login
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));

        // Test About link (external)
        String originalWindow = driver.getWindowHandle();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "About link should open parasoft.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Contact link
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("contact.htm"));
        assertTrue(driver.getPageSource().contains("Contact ParaBank"), "Contact page should load");

        // Navigate back to overview
        driver.get(BASE_URL.replace("index.htm", "overview.htm"));
        wait.until(ExpectedConditions.titleIs("ParaBank | Accounts Overview"));
    }

    @Test
    @Order(6)
    void testAccountServicesLinks() {
        testValidLogin(); // Ensure we're logged in

        // Test Open New Account
        WebElement openNewAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openNewAccountLink.click();
        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        assertTrue(driver.getPageSource().contains("Open New Account"), "Open New Account page should load");

        // Select account type and existing account
        WebElement accountTypeSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("type")));
        accountTypeSelect.click();
        driver.findElement(By.cssSelector("option[value='1']")).click(); // Savings

        WebElement fromAccountSelect = driver.findElement(By.id("fromAccountId"));
        fromAccountSelect.click();
        driver.findElement(By.cssSelector("#fromAccountId option:nth-child(2)")).click();

        WebElement openAccountButton = driver.findElement(By.xpath("//input[@value='Open New Account']"));
        openAccountButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("newAccountId")));
        String newAccountId = driver.findElement(By.id("newAccountId")).getText();
        assertFalse(newAccountId.isEmpty(), "New account ID should be generated");
        assertTrue(driver.getPageSource().contains("Account Opened!"), "Success message should appear");

        // Test Accounts Overview
        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountsOverviewLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getPageSource().contains("Accounts Overview"), "Should return to Accounts Overview");

        // Test Transfer Funds
        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        assertTrue(driver.getPageSource().contains("Transfer Funds"), "Transfer Funds page should load");

        // Fill transfer form
        WebElement amountField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        amountField.sendKeys("100");

        WebElement fromAccount = driver.findElement(By.id("fromAccountId"));
        fromAccount.click();
        driver.findElement(By.cssSelector("#fromAccountId option:nth-child(2)")).click();

        WebElement toAccount = driver.findElement(By.id("toAccountId"));
        toAccount.click();
        driver.findElement(By.cssSelector("#toAccountId option:nth-child(3)")).click();

        WebElement transferButton = driver.findElement(By.xpath("//input[@value='Transfer']"));
        transferButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".title")));
        assertTrue(driver.getPageSource().contains("Transfer Complete!"), "Transfer should complete successfully");

        // Test Bill Pay
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();
        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        assertTrue(driver.getPageSource().contains("Bill Payment Service"), "Bill Pay page should load");

        // Fill bill pay form
        WebElement payeeName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("payee.name")));
        payeeName.sendKeys("John Doe");

        driver.findElement(By.id("payee.address.street")).sendKeys("123 Main St");
        driver.findElement(By.id("payee.address.city")).sendKeys("Anytown");
        driver.findElement(By.id("payee.address.state")).sendKeys("CA");
        driver.findElement(By.id("payee.address.zipCode")).sendKeys("90210");
        driver.findElement(By.id("payee.phoneNumber")).sendKeys("555-1234");
        driver.findElement(By.id("payee.accountNumber")).sendKeys("45678");
        driver.findElement(By.id("verifyAccount")).sendKeys("45678");
        driver.findElement(By.id("amount")).sendKeys("50");

        WebElement fromAccountBill = driver.findElement(By.id("fromAccountId"));
        fromAccountBill.click();
        driver.findElement(By.cssSelector("#fromAccountId option:nth-child(2)")).click();

        WebElement sendPaymentButton = driver.findElement(By.xpath("//input[@value='Send Payment']"));
        sendPaymentButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".title")));
        assertTrue(driver.getPageSource().contains("Bill Payment to John Doe Complete"), "Bill payment should complete");

        // Test Find Transactions
        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions")));
        findTransactionsLink.click();
        wait.until(ExpectedConditions.urlContains("findtrans.htm"));
        assertTrue(driver.getPageSource().contains("Find Transactions"), "Find Transactions page should load");

        // Test Request Loan
        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan")));
        requestLoanLink.click();
        wait.until(ExpectedConditions.urlContains("loan.htm"));
        assertTrue(driver.getPageSource().contains("Loan Request"), "Loan Request page should load");

        driver.findElement(By.id("amount")).sendKeys("10000");
        driver.findElement(By.id("downPayment")).sendKeys("2000");

        WebElement fromAccountLoan = driver.findElement(By.id("fromAccountId"));
        fromAccountLoan.click();
        driver.findElement(By.cssSelector("#fromAccountId option:nth-child(2)")).click();

        WebElement applyNowButton = driver.findElement(By.xpath("//input[@value='Apply Now']"));
        applyNowButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".title")));
        assertTrue(driver.getPageSource().contains("Loan Application was Denied"), 
                "Loan application should be processed (may be denied based on criteria)");
    }

    @Test
    @Order(7)
    void testLogoutFunctionality() {
        testValidLogin(); // Ensure we're logged in

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.htm"));
        assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle());
        assertTrue(driver.getPageSource().contains("Customer Login"), "Login form should be visible after logout");

        // Try to access protected page directly
        driver.get(BASE_URL.replace("index.htm", "overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should be redirected to login when accessing protected page");
    }
}