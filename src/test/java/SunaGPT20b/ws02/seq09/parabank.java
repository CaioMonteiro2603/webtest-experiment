package SunaGPT20b.ws02.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
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
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    public void goToBase() {
        driver.get(BASE_URL);
    }

    /** Logs in if not already logged in */
    private void loginIfNeeded() {
        if (!driver.findElements(By.xpath("//a[contains(@href, 'logout.htm')]")).isEmpty()) {
            return; // already logged in
        }
        WebElement user = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("username")));
        user.clear();
        user.sendKeys(USERNAME);

        WebElement pass = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("password")));
        pass.clear();
        pass.sendKeys(PASSWORD);

        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']")));
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("parabank"));
        Assertions.assertTrue(driver.findElements(By.xpath("//a[contains(@href, 'logout.htm')]")).size() > 0,
                "Login failed – logout link not found.");
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        WebElement user = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("username")));
        user.clear();
        user.sendKeys(USERNAME);

        WebElement pass = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("password")));
        pass.clear();
        pass.sendKeys(PASSWORD);

        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']")));
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("parabank"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("parabank"),
                "URL after login does not contain expected path.");
        Assertions.assertTrue(driver.findElements(By.xpath("//a[contains(@href, 'logout.htm')]")).size() > 0,
                "Logout link not present – login may have failed.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        WebElement user = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("username")));
        user.clear();
        user.sendKeys("invalid@example.com");

        WebElement pass = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("password")));
        pass.clear();
        pass.sendKeys("wrongpass");

        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']")));
        loginBtn.click();

        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(error.isDisplayed(),
                "Error message not displayed for invalid credentials.");
        Assertions.assertTrue(error.getText().toLowerCase().contains("error") || error.getText().toLowerCase().contains("invalid"),
                "Error message does not indicate invalid login.");
    }

    @Test
    @Order(3)
    public void testAccountsOverview() {
        loginIfNeeded();

        WebElement overviewLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        overviewLink.click();

        wait.until(ExpectedConditions.titleContains("Accounts Overview"));
        Assertions.assertTrue(driver.getTitle().contains("Accounts Overview"),
                "Page title does not contain 'Accounts Overview'.");

        Assertions.assertFalse(driver.findElements(By.id("accountTable")).isEmpty(),
                "Accounts table not found on Accounts Overview page.");
    }

    @Test
    @Order(4)
    public void testTransferFunds() {
        loginIfNeeded();

        WebElement transferLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferLink.click();

        wait.until(ExpectedConditions.titleContains("Transfer Funds"));
        Assertions.assertTrue(driver.getTitle().contains("Transfer Funds"),
                "Page title does not contain 'Transfer Funds'.");

        // Fill transfer form (using first available accounts)
        List<WebElement> fromOptions = driver.findElements(By.name("fromAccountId"));
        List<WebElement> toOptions = driver.findElements(By.name("toAccountId"));
        Assertions.assertFalse(fromOptions.isEmpty() && toOptions.isEmpty(),
                "Account dropdowns not found.");

        if (!fromOptions.isEmpty()) {
            new Select(fromOptions.get(0)).selectByIndex(0);
        }
        if (!toOptions.isEmpty()) {
            new Select(toOptions.get(0)).selectByIndex(0);
        }

        WebElement amount = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("amount")));
        amount.clear();
        amount.sendKeys("50");

        WebElement submit = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Transfer']")));
        submit.click();

        WebElement success = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("transferCompleteMessage")));
        Assertions.assertTrue(success.isDisplayed(),
                "Transfer success message not displayed.");
        Assertions.assertTrue(success.getText().toLowerCase().contains("transfer complete"),
                "Success message does not contain expected text.");
    }

    @Test
    @Order(5)
    public void testBillPay() {
        loginIfNeeded();

        WebElement billPayLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();

        wait.until(ExpectedConditions.titleContains("Bill Pay"));
        Assertions.assertTrue(driver.getTitle().contains("Bill Pay"),
                "Page title does not contain 'Bill Pay'.");

        // Simple verification that the form is present
        Assertions.assertFalse(driver.findElements(By.id("payee")).isEmpty(),
                "Bill Pay form not found.");
    }

    @Test
    @Order(6)
    public void testSortingDropdownIfPresent() {
        loginIfNeeded();

        // Some pages may contain a sorting dropdown; we check the Accounts Overview page
        WebElement overviewLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        overviewLink.click();

        List<WebElement> dropdowns = driver.findElements(By.id("sort"));
        if (dropdowns.isEmpty()) {
            // No sorting dropdown – test passes trivially
            return;
        }

        Select sortSelect = new Select(dropdowns.get(0));
        List<WebElement> options = sortSelect.getOptions();
        Assertions.assertTrue(options.size() > 1, "Sorting dropdown has insufficient options.");

        for (WebElement option : options) {
            sortSelect.selectByVisibleText(option.getText());
            // Verify that the page reflects the selected sort order
            // Simple check: ensure the selected option is indeed active
            Assertions.assertEquals(option.getText(),
                    new Select(driver.findElement(By.id("sort"))).getFirstSelectedOption().getText(),
                    "Selected sort option not applied.");
        }
    }

    @Test
    @Order(7)
    public void testBurgerMenuIfPresent() {
        loginIfNeeded();

        List<WebElement> menuButtons = driver.findElements(By.id("menuButton"));
        if (menuButtons.isEmpty()) {
            // No burger menu – nothing to test
            return;
        }

        WebElement menuBtn = menuButtons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        // Expect a menu panel with known items
        List<WebElement> menuItems = driver.findElements(By.cssSelector(".menu a"));
        Assertions.assertFalse(menuItems.isEmpty(), "Menu items not found after opening burger menu.");

        // Example actions: click Logout if present
        for (WebElement item : menuItems) {
            String text = item.getText().trim();
            if (text.equalsIgnoreCase("Logout")) {
                wait.until(ExpectedConditions.elementToBeClickable(item)).click();
                wait.until(ExpectedConditions.urlContains("login"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                        "Logout did not redirect to login page.");
                // Re‑login for subsequent tests
                loginIfNeeded();
                break;
            }
        }

        // Close menu (assuming same button toggles)
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();
    }

    @Test
    @Order(8)
    public void testExternalFooterLinks() {
        loginIfNeeded();

        // Locate external links in the footer that open in a new tab/window
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("footer a[target='_blank']"));
        Assertions.assertFalse(externalLinks.isEmpty(),
                "No external footer links found to test.");

        for (WebElement link : externalLinks) {
            String originalWindow = driver.getWindowHandle();
            Set<String> existingWindows = driver.getWindowHandles();

            wait.until(ExpectedConditions.elementToBeClickable(link)).click();

            // Wait for new window/tab
            wait.until(drv -> drv.getWindowHandles().size() > existingWindows.size());

            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(existingWindows);
            String newWindow = newWindows.iterator().next();

            driver.switchTo().window(newWindow);
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(
                    currentUrl.contains("parasoft.com") ||
                    currentUrl.contains("twitter.com") ||
                    currentUrl.contains("facebook.com") ||
                    currentUrl.contains("linkedin.com"),
                    "External link does not lead to an expected domain: " + currentUrl);

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}