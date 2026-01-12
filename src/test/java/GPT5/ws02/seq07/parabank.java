package GPT5.ws02.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN_USER = "caio@gmail.com";
    private static final String LOGIN_PASS = "123";

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().deleteAllCookies();
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
    static void teardown() {
        if (driver != null) driver.quit();
    }

    // ----------------- Helpers -----------------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("topPanel")));
    }

    private boolean isLoggedIn() {
        return driver.findElements(By.cssSelector("a[href*='logout.htm']")).size() > 0;
    }

    private void logoutIfLoggedIn() {
        if (isLoggedIn()) {
            WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='logout.htm']")));
            logout.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
        }
    }

    private void loginIfNeeded() {
        if (!isLoggedIn()) {
            goHome();
            WebElement user = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement pass = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
            user.clear(); user.sendKeys(LOGIN_USER);
            pass.clear(); pass.sendKeys(LOGIN_PASS);
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#loginPanel input[type='submit'][value='Log In'], #loginPanel input[type='submit']")));
            btn.click();
            // Either goes to Overview or shows error; guard for either
            wait.until(d -> d.findElements(By.id("leftPanel")).size() > 0 || d.findElements(By.cssSelector(".error")).size() > 0);
        }
    }

    private void navLeft(String linkText) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
        link.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
    }

    private String currentHeader() {
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        return header.getText().trim();
    }

    private String getFirstAccountId() {
        // Navigate to Accounts Overview and read the first account id (link text)
        navLeft("Accounts Overview");
        List<WebElement> accountLinks = driver.findElements(By.cssSelector("#accountTable a[href*='activity.htm']"));
        if (accountLinks.isEmpty()) {
            // Some skins use different selectors; fallback to any activity link in the right panel
            accountLinks = driver.findElements(By.cssSelector("#rightPanel a[href*='activity.htm']"));
        }
        Assertions.assertTrue(!accountLinks.isEmpty(), "At least one account link should be present on Accounts Overview");
        return accountLinks.get(0).getText().trim();
    }

    private void openExternalAndAssertDomain(WebElement link, String expectedDomainFragment) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        // Wait for a new window or same-tab navigation
        wait.until(d -> d.getWindowHandles().size() != before.size() || !d.getCurrentUrl().equals(BASE_URL));
        Set<String> after = new HashSet<>(driver.getWindowHandles());
        after.removeAll(before);
        if (!after.isEmpty()) {
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment),
                    "External URL should contain: " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(original);
        } else {
            // same tab
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment),
                    "External URL should contain: " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
        }
    }

    private void selectFirstDifferentOptions(Select a, Select b) {
        List<String> aValues = a.getOptions().stream().map(o -> o.getAttribute("value")).collect(Collectors.toList());
        List<String> bValues = b.getOptions().stream().map(o -> o.getAttribute("value")).collect(Collectors.toList());
        if (aValues.isEmpty() || bValues.isEmpty()) {
            return;
        }
        String firstA = aValues.get(0);
        String firstB = bValues.size() > 1 ? bValues.get(1) : bValues.get(0);
        if (firstA.equals(firstB) && bValues.size() > 1) {
            firstB = bValues.get(1);
        }
        a.selectByValue(firstA);
        b.selectByValue(firstB);
    }

    // ----------------- Tests -----------------

    @Test
    @Order(1)
    void homePageLoadsAndTitleVisible() {
        goHome();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("parabank"), "Page title should contain 'ParaBank'");
        Assertions.assertTrue(driver.findElement(By.id("topPanel")).isDisplayed(), "Top panel should be visible");
    }

    @Test
    @Order(2)
    void invalidLoginShowsError() {
        logoutIfLoggedIn();
        goHome();
        WebElement user = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement pass = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
        user.clear(); user.sendKeys("wrong_user@example.com");
        pass.clear(); pass.sendKeys("bad_password");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#loginPanel input[type='submit']"))).click();
        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(err.getText().length() > 0, "An error message should be shown for invalid login");
    }

    @Test
    @Order(3)
    void validLoginNavigatesToOverview() {
        loginIfNeeded();
        Assertions.assertTrue(isLoggedIn(), "User should be logged in");
        // On successful login, left panel should contain navigation links
        Assertions.assertTrue(driver.findElements(By.linkText("Accounts Overview")).size() > 0,
                "Accounts Overview link should be present after login");
        navLeft("Accounts Overview");
        Assertions.assertTrue(currentHeader().toLowerCase().contains("accounts overview"),
                "Header should indicate Accounts Overview");
    }

    @Test
    @Order(4)
    void topMenuInternalNavigationCoverage() {
        loginIfNeeded();

        navLeft("Accounts Overview");
        Assertions.assertTrue(currentHeader().toLowerCase().contains("accounts overview"), "Should be on Accounts Overview");

        navLeft("Transfer Funds");
        Assertions.assertTrue(currentHeader().toLowerCase().contains("transfer funds"), "Should be on Transfer Funds");

        navLeft("Bill Pay");
        Assertions.assertTrue(currentHeader().toLowerCase().contains("bill payment"), "Should be on Bill Pay");

        navLeft("Find Transactions");
        Assertions.assertTrue(currentHeader().toLowerCase().contains("find transactions"), "Should be on Find Transactions");

        navLeft("Update Contact Info");
        Assertions.assertTrue(currentHeader().toLowerCase().contains("update profile") || currentHeader().toLowerCase().contains("update contact"),
                "Should be on Update Contact Info");

        navLeft("Request Loan");
        String header = currentHeader().toLowerCase();
        Assertions.assertTrue(header.contains("request loan") || header.contains("loan request"), "Should be on Request Loan");
    }

    @Test
    @Order(5)
    void accountDetailsPageFromOverview() {
        loginIfNeeded();
        navLeft("Accounts Overview");
        String firstId = getFirstAccountId();
        driver.findElement(By.linkText(firstId)).click();
        wait.until(ExpectedConditions.urlContains("activity.htm"));
        WebElement detailsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(detailsHeader.getText().toLowerCase().contains("account activity") ||
                              detailsHeader.getText().toLowerCase().contains("account details"),
                "Header should indicate Account Activity/Details");
        // Verify account id is shown on page
        Assertions.assertTrue(driver.getPageSource().contains(firstId), "Account ID should be present on the details page");
    }

    @Test
    @Order(6)
    void transferFundsFlowShowsCompletion() {
        loginIfNeeded();
        navLeft("Transfer Funds");
        WebElement amount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        Select fromSel = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fromAccountId"))));
        Select toSel = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("toAccountId"))));
        amount.clear(); amount.sendKeys("1");
        if (fromSel.getOptions().size() > 0 && toSel.getOptions().size() > 0) {
            selectFirstDifferentOptions(fromSel, toSel);
        }
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Transfer'], #rightPanel form input[type='submit']"))).click();
        // Confirmation
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("complete"),
                "Transfer should show a completion header");
        Assertions.assertTrue(driver.getPageSource().toLowerCase().contains("successful") ||
                              driver.getPageSource().toLowerCase().contains("complete"),
                "Transfer result text should indicate success/completion");
    }

    @Test
    @Order(7)
    void billPayFlowShowsCompletion() {
        loginIfNeeded();
        navLeft("Bill Pay");
        // Fill required fields
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("payee.name"))).sendKeys("Test Payee");
        driver.findElement(By.name("payee.address.street")).sendKeys("1 Test Street");
        driver.findElement(By.name("payee.address.city")).sendKeys("Testville");
        driver.findElement(By.name("payee.address.state")).sendKeys("TS");
        driver.findElement(By.name("payee.address.zipCode")).sendKeys("12345");
        driver.findElement(By.name("payee.phoneNumber")).sendKeys("5551234567");
        String acct = getFirstAccountId();
        driver.findElement(By.name("payee.accountNumber")).sendKeys(acct);
        driver.findElement(By.name("verifyAccount")).sendKeys(acct);
        WebElement amount = driver.findElement(By.name("amount"));
        amount.clear(); amount.sendKeys("1");
        Select fromSel = new Select(driver.findElement(By.name("fromAccountId")));
        fromSel.selectByIndex(0);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Send Payment'], #rightPanel form input[type='submit']"))).click();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("complete"),
                "Bill Payment should show a completion header");
        Assertions.assertTrue(driver.getPageSource().toLowerCase().contains("bill payment complete") ||
                              driver.getPageSource().toLowerCase().contains("payment successful") ||
                              driver.getPageSource().toLowerCase().contains("complete"),
                "Bill payment result text should indicate completion");
    }

    @Test
    @Order(8)
    void findTransactionsByAmountShowsResultsOrEmptyState() {
        loginIfNeeded();
        navLeft("Find Transactions");
        // Choose account
        Select acctSel = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountId"))));
        acctSel.selectByIndex(0);
        // Find by amount
        WebElement amount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='criteria.amount'], #criteria\\.amount")));
        amount.clear(); amount.sendKeys("1");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("form[action*='findByAmount'] input[type='submit'], form[action*='findByAmount'] button[type='submit']"))).click();
        // Assert either table present or a 'No results' message area in rightPanel
        boolean hasTable = driver.findElements(By.id("transactionTable")).size() > 0 ||
                           driver.findElements(By.cssSelector("#rightPanel table")).size() > 0;
        boolean hasMessage = driver.findElements(By.cssSelector("#rightPanel")).get(0).getText().toLowerCase().contains("no results") ||
                             driver.findElement(By.id("rightPanel")).getText().toLowerCase().contains("no transactions");
        Assertions.assertTrue(hasTable || hasMessage, "Should show a results table or an empty state message");
    }

    @Test
    @Order(9)
    void updateContactInfoSavesSuccessfully() {
        loginIfNeeded();
        navLeft("Update Contact Info");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("customer.address.street"))).clear();
        driver.findElement(By.name("customer.address.street")).sendKeys("99 Automation Way");
        driver.findElement(By.name("customer.address.city")).clear();
        driver.findElement(By.name("customer.address.city")).sendKeys("Selenium City");
        driver.findElement(By.name("customer.address.state")).clear();
        driver.findElement(By.name("customer.address.state")).sendKeys("QA");
        driver.findElement(By.name("customer.address.zipCode")).clear();
        driver.findElement(By.name("customer.address.zipCode")).sendKeys("98765");
        driver.findElement(By.name("customer.phoneNumber")).clear();
        driver.findElement(By.name("customer.phoneNumber")).sendKeys("5559876543");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Update Profile'], input[type='submit'][value='Update Contact'], #rightPanel form input[type='submit']"))).click();
        // Assert success message
        WebElement right = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
        String text = right.getText().toLowerCase();
        Assertions.assertTrue(text.contains("profile updated") || text.contains("updated successfully") || text.contains("was updated"),
                "The page should indicate the profile was updated");
    }

    @Test
    @Order(10)
    void requestLoanDisplaysStatus() {
        loginIfNeeded();
        navLeft("Request Loan");
        WebElement amount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        WebElement down = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("downPayment")));
        Select fromSel = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fromAccountId"))));
        fromSel.selectByIndex(0);
        amount.clear(); amount.sendKeys("100");
        down.clear(); down.sendKeys("10");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Apply Now'], input[type='submit'][value='Submit'], #rightPanel form input[type='submit']"))).click();
        WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
        String text = panel.getText().toLowerCase();
        // Loan typically results in 'Approved' or 'Denied'
        Assertions.assertTrue(text.contains("approved") || text.contains("denied") || text.contains("status"),
                "Loan result should display an approval/denial status");
    }

    @Test
    @Order(11)
    void footerExternalParasoftLinkOpensExternally() {
        // This should work even when not logged in; test from home to cover base-level external link
        goHome();
        // Find any link to parasoft.com in footer or elsewhere
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='parasoft.com']"));
        Assertions.assertFalse(links.isEmpty(), "Expected at least one external link to parasoft.com");
        openExternalAndAssertDomain(links.get(0), "parasoft.com");
    }

    @Test
    @Order(12)
    void logoutReturnsToLogin() {
        loginIfNeeded();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='logout.htm']")));
        logout.click();
        WebElement loginPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
        Assertions.assertTrue(loginPanel.isDisplayed(), "Login panel should be visible after logout");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/index.htm"), "URL should return to index.htm after logout");
    }
}