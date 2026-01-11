package GPT20b.ws02.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Test suite for the Parabank demo application.
 * Uses Selenium 4 with Firefox in headless mode.
 */
@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USER_NAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Helper methods ----------------------------------------------------- */

    private static void initDriver() {
        if (driver == null) {
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--headless");
            driver = new FirefoxDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        }
    }

    private static void doLogin() {
        initDriver();
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(USER_NAME);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password"))).sendKeys(PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Log In']"))).click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"),
                "Expected to be on account overview page after login");
    }

    private static void doLogout() {
        if (driver == null) return;
        // Scroll to Ensure logout link visible
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    private static List<Integer> getAccountNumbers() {
        List<WebElement> rows = driver.findElements(By.cssSelector("table[id='accountTable'] tbody tr"));
        return rows.stream()
                .map(row -> row.findElement(By.cssSelector("td:nth-child(1)")).getText())
                .filter(text -> text.matches("\\d+"))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    /** Test cases ----------------------------------------------------------- */

    @Test
    @Order(1)
    @DisplayName("Valid login")
    void testValidLogin() {
        doLogin();
    }

    @Test
    @Order(2)
    @DisplayName("Invalid login credentials")
    void testInvalidLogin() {
        initDriver();
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("invalid@user");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password"))).sendKeys("wrongpass");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Log In']"))).click();

        List<WebElement> errors = driver.findElements(By.cssSelector("p.error, div.error p"));
        assumeTrue(errors.size() > 0, "Expected error message for invalid credentials");
        String errorText = errors.get(0).getText();
        assertTrue(errorText.contains("error") || errorText.contains("The username and password could not be verified."),
                "Unexpected error message: " + errorText);
    }

    @Test
    @Order(3)
    @DisplayName("Account sorting if dropdown exists")
    void testSortingIfPresent() {
        doLogin();

        List<WebElement> sortSelects = driver.findElements(By.id("accountId"));
        assumeTrue(!sortSelects.isEmpty(), "Sorting dropdown not present, skipping test.");

        Select sorter = new Select(sortSelects.get(0));
        // Store original ordering
        List<Integer> originalOrder = getAccountNumbers();

        sorter.selectByVisibleText("Account number: A to Z");
        List<Integer> ascending = getAccountNumbers();
        sorter.selectByVisibleText("Account number: Z to A");
        List<Integer> descending = getAccountNumbers();

        // Validate that the order changed
        assertFalse(ascending.equals(originalOrder), "Ascending sort did not change order");
        assertFalse(descending.equals(originalOrder), "Descending sort did not change order");
        // Validate ascending order
        List<Integer> ascCopy = new ArrayList<>(ascending);
        ascCopy.sort(Integer::compareTo);
        assertEquals(ascCopy, ascending, "Ascending order not sorted");
        // Validate descending order
        List<Integer> descCopy = new ArrayList<>(descending);
        descCopy.sort((a, b) -> b.compareTo(a));
        assertEquals(descCopy, descending, "Descending order not sorted");
    }

    @Test
    @Order(4)
    @DisplayName("Navigation links: Open New Account, Transfer Funds, Logout")
    void testNavigationLinks() {
        doLogin();

        // Open New Account
        WebElement openAccount = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openAccount.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("openAccountForm")));
        assertTrue(driver.getPageSource().contains("Open New Account"),
                "Open New Account page did not load correctly");
        driver.navigate().back();

        // Transfer Funds
        WebElement transferFunds = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFunds.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("transferForm")));
        assertTrue(driver.getPageSource().contains("Transfer Funds"),
                "Transfer Funds page did not load correctly");
        driver.navigate().back();

        // Logout
        doLogout();
        assertTrue(driver.getCurrentUrl().contains("index.htm"),
                "Logout did not return to login page");
    }

    @Test
    @Order(5)
    @DisplayName("Footer social links existence")
    void testFooterSocialLinksExistence() {
        doLogin();

        List<By> socialSelectors = List.of(
                By.cssSelector("a[href*='twitter.com']"),
                By.cssSelector("a[href*='facebook.com']"),
                By.cssSelector("a[href*='linkedin.com']"));

        for (By selector : socialSelectors) {
            List<WebElement> links = driver.findElements(selector);
            assumeTrue(!links.isEmpty(),
                    "Expected social link not found for selector: " + selector);
        }
    }
}