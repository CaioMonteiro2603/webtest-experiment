package SunaGPT20b.ws02.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class ParabankTestSuite {

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

private void loginIfNeeded() {
    driver.get(BASE_URL);
    // If already logged in, the logout link will be present
    List<WebElement> logoutLinks = driver.findElements(By.linkText("Log Out"));
    if (!logoutLinks.isEmpty()) {
        return; // already logged in
    }
    // Perform login
    WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
    usernameField.clear();
    usernameField.sendKeys(USERNAME);
    WebElement passwordField = driver.findElement(By.name("password"));
    passwordField.clear();
    passwordField.sendKeys(PASSWORD);
    WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));
    loginButton.click();
    // Verify login success
    wait.until(ExpectedConditions.urlContains("parabank"));
    Assertions.assertTrue(driver.findElements(By.linkText("Log Out")).size() > 0,
            "Login failed – Log Out link not found.");
}

@Test
@Order(1)
public void testValidLogin() {
    driver.get(BASE_URL);
    WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
    usernameField.clear();
    usernameField.sendKeys(USERNAME);
    WebElement passwordField = driver.findElement(By.name("password"));
    passwordField.clear();
    passwordField.sendKeys(PASSWORD);
    WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));
    loginButton.click();

    wait.until(ExpectedConditions.urlContains("parabank"));
    Assertions.assertTrue(driver.findElements(By.linkText("Log Out")).size() > 0,
            "Valid login should display Log Out link.");
}

@Test
@Order(2)
public void testInvalidLogin() {
    driver.get(BASE_URL);
    WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
    usernameField.clear();
    usernameField.sendKeys("invalid@example.com");
    WebElement passwordField = driver.findElement(By.name("password"));
    passwordField.clear();
    passwordField.sendKeys("wrongpass");
    WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));
    loginButton.click();

    // Expect error message
    WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("p.error")));
    Assertions.assertTrue(errorMsg.isDisplayed(),
            "Error message should be displayed for invalid credentials.");
    Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("error"),
            "Error message should contain the word 'error'.");
}

@Test
@Order(3)
public void testMenuNavigationAndReset() {
    loginIfNeeded();

    // Open the menu (burger) button if present
    List<WebElement> menuButtons = driver.findElements(By.id("menuButton"));
    if (!menuButtons.isEmpty()) {
        WebElement menuButton = menuButtons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(menuButton)).click();
    }

    // Click "Accounts Overview" (All Items equivalent)
    WebElement accountsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
    accountsLink.click();
    wait.until(ExpectedConditions.titleContains("Accounts Overview"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"),
            "Should navigate to Accounts Overview page.");

    // Click "About" (external link)
    List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
    if (!aboutLinks.isEmpty()) {
        String originalWindow = driver.getWindowHandle();
        WebElement aboutLink = aboutLinks.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();

        // Switch to new window/tab
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("parasoft.com"),
                "External About link should lead to Parasoft domain.");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    // Click "Log Out"
    WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
    logoutLink.click();
    wait.until(ExpectedConditions.titleContains("ParaBank"));
    Assertions.assertTrue(driver.findElements(By.cssSelector("input[value='Log In']")).size() > 0,
            "After logout, login button should be visible.");
}

@Test
@Order(4)
public void testExternalFooterLinks() {
    driver.get(BASE_URL);
    // Footer social links (Twitter, Facebook, LinkedIn) – treat as external
    List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
    for (WebElement link : footerLinks) {
        String href = link.getAttribute("href");
        if (href == null || href.isEmpty()) continue;
        // Only handle external domains (not containing parabank.parasoft.com)
        if (!href.contains("parabank.parasoft.com")) {
            String originalWindow = driver.getWindowHandle();
            wait.until(ExpectedConditions.elementToBeClickable(link)).click();

            // Switch to new window/tab
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            Assertions.assertTrue(driver.getCurrentUrl().contains(href.replaceAll("https?://([^/]+).*", "$1")),
                    "External link should navigate to its domain.");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}

@Test
@Order(5)
public void testAccountSummaryDetails() {
    loginIfNeeded();

    // Navigate to "Accounts Overview"
    WebElement accountsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
    accountsLink.click();
    wait.until(ExpectedConditions.titleContains("Accounts Overview"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"),
            "Should be on Accounts Overview page.");

    // Verify that at least one account row is displayed
    List<WebElement> accountRows = driver.findElements(By.cssSelector("table#accountTable tbody tr"));
    Assertions.assertFalse(accountRows.isEmpty(), "Account table should contain at least one row.");

    // Click on first account's "Details" link
    WebElement detailsLink = accountRows.get(0).findElement(By.linkText("Details"));
    wait.until(ExpectedConditions.elementToBeClickable(detailsLink)).click();

    // Verify navigation to account details page
    wait.until(ExpectedConditions.titleContains("Account Details"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("account.htm"),
            "Should navigate to account details page.");

    // Verify balance element exists
    WebElement balance = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("balance")));
    Assertions.assertTrue(balance.isDisplayed(), "Balance should be displayed on account details page.");

    // Return to overview for clean state
    driver.navigate().back();
    wait.until(ExpectedConditions.titleContains("Accounts Overview"));
}

@Test
@Order(6)
public void testTransferFunds() {
    loginIfNeeded();

    // Navigate to Transfer Funds page
    WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
    transferLink.click();
    wait.until(ExpectedConditions.titleContains("Transfer Funds"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("transfer.htm"),
            "Should be on Transfer Funds page.");

    // Fill transfer form with valid data (use first two accounts)
    List<WebElement> fromSelect = driver.findElements(By.name("fromAccountId"));
    List<WebElement> toSelect = driver.findElements(By.name("toAccountId"));
    Assertions.assertFalse(fromSelect.isEmpty() && toSelect.isEmpty(),
            "From and To account dropdowns must be present.");

    Select from = new Select(fromSelect.get(0));
    Select to = new Select(toSelect.get(0));
    Assertions.assertTrue(from.getOptions().size() > 1 && to.getOptions().size() > 1,
            "Both dropdowns should have at least two options.");

    from.selectByIndex(0);
    to.selectByIndex(1);
    WebElement amountField = driver.findElement(By.name("amount"));
    amountField.clear();
    amountField.sendKeys("10");
    WebElement transferButton = driver.findElement(By.cssSelector("input[value='Transfer']"));
    transferButton.click();

    // Verify success message
    WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("p.title")));
    Assertions.assertTrue(successMsg.getText().toLowerCase().contains("transfer complete"),
            "Transfer should complete successfully.");
}

@Test
@Order(7)
public void testResetAppState() {
    loginIfNeeded();

    // Navigate to any page that changes state, e.g., Transfer Funds and perform a transfer
    WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
    transferLink.click();
    wait.until(ExpectedConditions.titleContains("Transfer Funds"));
    // Perform a minimal transfer to change state
    List<WebElement> fromSelect = driver.findElements(By.name("fromAccountId"));
    List<WebElement> toSelect = driver.findElements(By.name("toAccountId"));
    if (!fromSelect.isEmpty() && !toSelect.isEmpty()) {
        Select from = new Select(fromSelect.get(0));
        Select to = new Select(toSelect.get(0));
        if (from.getOptions().size() > 1 && to.getOptions().size() > 1) {
            from.selectByIndex(0);
            to.selectByIndex(1);
            WebElement amountField = driver.findElement(By.name("amount"));
            amountField.clear();
            amountField.sendKeys("1");
            driver.findElement(By.cssSelector("input[value='Transfer']")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.title")));
        }
    }

    // Click Reset App State (assumed to be a link in the menu)
    List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
    if (!resetLinks.isEmpty()) {
        WebElement resetLink = resetLinks.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(resetLink)).click();
        // Verify that we are back to the home page
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("index.htm"),
                "After resetting, should be on the home page.");
    } else {
        Assertions.fail("Reset App State link not found.");
    }
}
}