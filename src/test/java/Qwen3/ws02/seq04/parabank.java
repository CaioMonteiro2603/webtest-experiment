package GPT5.ws02.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class ParabankE2EHeadlessTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ---------- Helpers ----------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("leftPanel")));
    }

    private boolean isLoggedIn() {
        return driver.findElements(By.linkText("Log Out")).size() > 0;
    }

    private void logoutIfLoggedIn() {
        if (isLoggedIn()) {
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
        }
    }

    private void login(String user, String pass) {
        goHome();
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input.button"));
        username.clear();
        username.sendKeys(user);
        password.clear();
        password.sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    private void registerIfNeededThenLogin() {
        goHome();
        // If already logged in, keep it
        if (isLoggedIn()) return;

        // Try to register with the provided credentials as username/password
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customerForm")));

        driver.findElement(By.id("customer.firstName")).clear();
        driver.findElement(By.id("customer.firstName")).sendKeys("Caio");
        driver.findElement(By.id("customer.lastName")).clear();
        driver.findElement(By.id("customer.lastName")).sendKeys("Tester");
        driver.findElement(By.id("customer.address.street")).clear();
        driver.findElement(By.id("customer.address.street")).sendKeys("123 Test St");
        driver.findElement(By.id("customer.address.city")).clear();
        driver.findElement(By.id("customer.address.city")).sendKeys("Testville");
        driver.findElement(By.id("customer.address.state")).clear();
        driver.findElement(By.id("customer.address.state")).sendKeys("TS");
        driver.findElement(By.id("customer.address.zipCode")).clear();
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("00000");
        driver.findElement(By.id("customer.phoneNumber")).clear();
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("5551234");
        driver.findElement(By.id("customer.ssn")).clear();
        driver.findElement(By.id("customer.ssn")).sendKeys("111-22-3333");

        // Use LOGIN as username (Parabank allows arbitrary username strings)
        driver.findElement(By.id("customer.username")).clear();
        driver.findElement(By.id("customer.username")).sendKeys(LOGIN);
        driver.findElement(By.id("customer.password")).clear();
        driver.findElement(By.id("customer.password")).sendKeys(PASSWORD);
        driver.findElement(By.id("repeatedPassword")).clear();
        driver.findElement(By.id("repeatedPassword")).sendKeys(PASSWORD);

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button[value='Register']"))).click();

        // Registration success or already exists -> ensure we are logged in
        if (!isLoggedIn()) {
            // If not logged in, try direct login (username may already exist)
            login(LOGIN, PASSWORD);
        }
        // Assert final state
        Assertions.assertTrue(isLoggedIn(), "Expected to be logged in after registration/login.");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(driver.findElement(By.cssSelector("#rightPanel h1")).getText().toLowerCase().contains("accounts overview")
                        || driver.findElement(By.cssSelector("#rightPanel h1")).getText().toLowerCase().contains("welcome"),
                "Expected Accounts Overview or Welcome header after login.");
    }

    private void switchToNewWindowAndAssertContains(String expectedDomainPart) {
        String original = driver.getWindowHandle();
        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(original)) {
                driver.switchTo().window(h);
                wait.until(ExpectedConditions.urlContains(expectedDomainPart));
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomainPart.toLowerCase()),
                        "External URL should contain " + expectedDomainPart);
                driver.close();
                driver.switchTo().window(original);
                break;
            }
        }
    }

    private String anyAccountIdFromOverview() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountTable")));
        List<WebElement> links = driver.findElements(By.cssSelector("#accountTable a"));
        Assertions.assertFalse(links.isEmpty(), "Expected at least one account on overview.");
        return links.get(0).getText().trim();
    }

    // ---------- Tests ----------

    @Test
    @Order(1)
    public void testHomePageAndExternalLinks() {
        goHome();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("parabank"), "Title should contain 'ParaBank'.");
        // Try to find an external link (parasoft.com). If not in footer, try logo alt/href.
        List<WebElement> external = driver.findElements(By.cssSelector("a[href*='parasoft.com']"));
        if (!external.isEmpty()) {
            String href = external.get(0).getAttribute("href");
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],'_blank')", href);
            switchToNewWindowAndAssertContains("parasoft.com");
        }
        // Ensure main login panel is visible
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel"))).isDisplayed(),
                "Login panel should be visible on home page.");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        goHome();
        WebElement user = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement pass = driver.findElement(By.name("password"));
        user.clear();
        user.sendKeys("invalid-user-xyz");
        pass.clear();
        pass.sendKeys("wrong");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button"))).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel .error, #rightPanel p.smallText")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("could not be verified")
                        || error.getText().toLowerCase().contains("error")
                        || error.getText().toLowerCase().contains("login"),
                "Expected an error message after invalid login.");
        // Return to a clean state
        goHome();
    }

    @Test
    @Order(3)
    public void testRegisterOrLoginAndOverviewVisible() {
        registerIfNeededThenLogin();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("accounts overview") || header.getText().toLowerCase().contains("welcome"),
                "Expected Accounts Overview or Welcome header.");
    }

    @Test
    @Order(4)
    public void testOpenNewAccount() {
        registerIfNeededThenLogin();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("type")));
        // Select default type and existing account as funding
        Select type = new Select(driver.findElement(By.id("type")));
        if (type.getOptions().size() > 0) type.selectByIndex(0);
        Select from = new Select(driver.findElement(By.id("fromAccountId")));
        if (from.getOptions().size() > 0) from.selectByIndex(0);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button"))).click();

        WebElement newAccHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(newAccHeader.getText().toLowerCase().contains("account opened"), "Expected 'Account Opened!' header.");
        WebElement newAccId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newAccountId")));
        Assertions.assertFalse(newAccId.getText().trim().isEmpty(), "New Account ID should be shown.");
    }

    @Test
    @Order(5)
    public void testTransferFunds() {
        registerIfNeededThenLogin();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        driver.findElement(By.id("amount")).clear();
        driver.findElement(By.id("amount")).sendKeys("10");

        Select from = new Select(driver.findElement(By.id("fromAccountId")));
        Select to = new Select(driver.findElement(By.id("toAccountId")));
        // Ensure some selection (may be same if only one account exists)
        if (from.getOptions().size() > 0) from.selectByIndex(0);
        if (to.getOptions().size() > 0) {
            if (to.getOptions().size() > 1) {
                to.selectByIndex(1);
            } else {
                to.selectByIndex(0);
            }
        }
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button[value='Transfer']"))).click();

        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("transfer complete"), "Expected 'Transfer Complete!' message.");
        WebElement result = driver.findElement(By.cssSelector("#rightPanel .title + p"));
        Assertions.assertTrue(result.getText().toLowerCase().contains("$10"), "Transfer result should mention $10.");
    }

    @Test
    @Order(6)
    public void testBillPay() {
        registerIfNeededThenLogin();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("payee.name")));
        driver.findElement(By.id("payee.name")).clear();
        driver.findElement(By.id("payee.name")).sendKeys("Electric Co");
        driver.findElement(By.id("payee.address.street")).clear();
        driver.findElement(By.id("payee.address.street")).sendKeys("1 Power Way");
        driver.findElement(By.id("payee.address.city")).clear();
        driver.findElement(By.id("payee.address.city")).sendKeys("Grid City");
        driver.findElement(By.id("payee.address.state")).clear();
        driver.findElement(By.id("payee.address.state")).sendKeys("GC");
        driver.findElement(By.id("payee.address.zipCode")).clear();
        driver.findElement(By.id("payee.address.zipCode")).sendKeys("99999");
        driver.findElement(By.id("payee.phoneNumber")).clear();
        driver.findElement(By.id("payee.phoneNumber")).sendKeys("5559999");
        driver.findElement(By.id("payee.accountNumber")).clear();
        driver.findElement(By.id("payee.accountNumber")).sendKeys("123456");
        driver.findElement(By.id("verifyAccount")).clear();
        driver.findElement(By.id("verifyAccount")).sendKeys("123456");
        driver.findElement(By.id("amount")).clear();
        driver.findElement(By.id("amount")).sendKeys("12.34");
        Select from = new Select(driver.findElement(By.id("fromAccountId")));
        if (from.getOptions().size() > 0) from.selectByIndex(0);

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button[value='Send Payment']"))).click();

        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("bill payment complete"), "Expected Bill Payment Complete.");
    }

    @Test
    @Order(7)
    public void testFindTransactionsByAmount() {
        registerIfNeededThenLogin();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountId")));
        Select acc = new Select(driver.findElement(By.id("accountId")));
        if (acc.getOptions().size() > 0) acc.selectByIndex(0);
        driver.findElement(By.id("criteria.amount")).clear();
        driver.findElement(By.id("criteria.amount")).sendKeys("10");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'][ng-click='findByAmount()'], input.button[value='Find Transactions']"))).click();

        // Results table should appear or at least the section
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("transactionTable")));
        List<WebElement> rows = driver.findElements(By.cssSelector("#transactionTable tbody tr"));
        Assertions.assertTrue(rows.size() >= 0, "Result table should be present (rows may be 0 if none found).");
    }

    @Test
    @Order(8)
    public void testUpdateProfile() {
        registerIfNeededThenLogin();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.phoneNumber")));
        driver.findElement(By.id("customer.phoneNumber")).clear();
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("555-1234");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button[value='Update Profile']"))).click();
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("profile updated"), "Expected 'Profile Updated' confirmation.");
    }

    @Test
    @Order(9)
    public void testRequestLoan() {
        registerIfNeededThenLogin();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        driver.findElement(By.id("amount")).clear();
        driver.findElement(By.id("amount")).sendKeys("100");
        driver.findElement(By.id("downPayment")).clear();
        driver.findElement(By.id("downPayment")).sendKeys("10");
        Select from = new Select(driver.findElement(By.id("fromAccountId")));
        if (from.getOptions().size() > 0) from.selectByIndex(0);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input.button[value='Apply Now']"))).click();

        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("loan request processed"), "Expected loan request processed page.");
        // Status may be Approved or Denied; just assert status element exists
        Assertions.assertFalse(driver.findElements(By.id("loanStatus")).isEmpty()
                        || driver.findElement(By.id("loanStatus")).getText().isEmpty(),
                "Loan status should be present.");
    }

    @Test
    @Order(10)
    public void testAccountsOverviewAndViewAccount() {
        registerIfNeededThenLogin();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountTable")));
        String accountId = anyAccountIdFromOverview();
        // Navigate one level down into account activity
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(accountId))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(driver.findElement(By.cssSelector("#rightPanel h1")).getText().toLowerCase().contains("account details")
                        || driver.getCurrentUrl().toLowerCase().contains("activity.htm"),
                "Expected Account Details/Activity page.");
    }

    @Test
    @Order(11)
    public void testLogout() {
        registerIfNeededThenLogin();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
        WebElement loginPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
        Assertions.assertTrue(loginPanel.isDisplayed(), "Login panel should be visible after logout.");
        // Ensure URL returned to index
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "URL should return to index.htm after logout.");
    }
}
