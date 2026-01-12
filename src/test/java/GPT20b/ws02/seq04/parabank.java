package GPT20b.ws02.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

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
    public static void initDriver() {
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
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                         */
    /* --------------------------------------------------------------------- */

    private static void navigateTo(String url) {
        driver.get(url);
    }

    private static void logIn() {
        navigateTo(BASE_URL);
        // Input fields may have name or id; try both
        WebElement userField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='username'], input#username")));
        WebElement passField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='password'], input#password")));
        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='submit'][value='Log In']")));
        loginBtn.click();
        // Wait for account summary page
        wait.until(ExpectedConditions.urlContains("/overview"));
        assertTrue(driver.getCurrentUrl().contains("/overview"),
                "Login should navigate to account overview page");
    }

    private static void logOut() {
        // Locate Logout link
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Log Out")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("/index.htm"));
        assertTrue(driver.getCurrentUrl().contains("/index.htm"),
                "Logout should navigate back to index page");
    }

    private static void openAndVerifyExternalLink(String partialHref, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + partialHref + "']"));
        if (links.isEmpty()) return; // nothing to test
        WebElement link = links.get(0);
        String originalWindow = driver.getWindowHandle();
        String originalUrl = driver.getCurrentUrl();
        link.click();
        // Determine if new window opened
        Set<String> handles = driver.getWindowHandles();
        if (handles.size() > 1) {
            handles.remove(originalWindow);
            String newWindow = handles.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(driver1 -> driver1.getCurrentUrl().contains(expectedDomain));
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            // Same tab
            wait.until(driver1 -> driver1.getCurrentUrl().contains(expectedDomain));
        }
        // return to original page
        driver.get(originalUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountTable")));
    }

    /* --------------------------------------------------------------------- */
    /* Tests                                                                  */
    /* --------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testInvalidLogin() {
        navigateTo(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='username'], input#username")));
        WebElement passField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='password'], input#password")));
        userField.clear();
        userField.sendKeys("wrong_user");
        passField.clear();
        passField.sendKeys("wrong_pass");
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='submit'][value='Log In']")));
        loginBtn.click();
        String alert = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("error"))).getText();
        assertNotNull(alert, "Error message should appear for invalid credentials");
    }

    @Test
    @Order(2)
    public void testValidLoginAndLogout() {
        logIn();
        // Verify account summary table exists
        List<WebElement> accounts = driver.findElements(By.cssSelector("table#accountTable"));
        assertFalse(accounts.isEmpty(), "Account summary table should be displayed after login");
        logOut();
    }

    @Test
    @Order(3)
    public void testNewAccountPageNavigation() {
        logIn();
        // Click New Account link
        WebElement newAccLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Open New Account")));
        newAccLink.click();
        wait.until(ExpectedConditions.urlContains("/openaccount"));
        assertTrue(driver.getCurrentUrl().contains("/openaccount"),
                "New Account page should load");
        // Return to account overview
        WebElement backLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Accounts Overview")));
        backLink.click();
        wait.until(ExpectedConditions.urlContains("/overview"));
        logOut();
    }

    @Test
    @Order(4)
    public void testTransferFundsPageNavigation() {
        logIn();
        // Click Transfer Funds link
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Transfer Funds")));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("/transfer"));
        assertTrue(driver.getCurrentUrl().contains("/transfer"),
                "Transfer Funds page should load");
        // Return to account overview
        WebElement backLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Accounts Overview")));
        backLink.click();
        wait.until(ExpectedConditions.urlContains("/overview"));
        logOut();
    }

    @Test
    @Order(5)
    public void testAccountSummarySortingIfExists() {
        logIn();
        // Check for a sort dropdown; if present test sorting
        List<WebElement> sortElements = driver.findElements(By.name("accountId"));
        if (sortElements.isEmpty()) {
            // No sorting available; test passes
            System.out.println("No sorting dropdown found; skipping sorting test.");
        } else {
            WebElement sortDropdown = sortElements.get(0);
            if (!sortDropdown.isDisplayed()) {
                System.out.println("Sorting dropdown not displayed; skipping.");
            } else {
                // Grab initial first account number
                WebElement firstRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("table#accountTable tbody tr:first-child td:first-child")));
                String firstAccountBefore = firstRow.getText();

                // Select each option
                List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
                for (WebElement option : options) {
                    String value = option.getAttribute("value");
                    option.click();
                    // Wait for table to update
                    wait.until(driver1 -> {
                        WebElement newFirstRow = driver1.findElement(
                                By.cssSelector("table#accountTable tbody tr:first-child td:first-child"));
                        return !newFirstRow.getText().equals(firstAccountBefore);
                    });
                    String newFirst = driver.findElement(
                            By.cssSelector("table#accountTable tbody tr:first-child td:first-child")).getText();
                    assertNotEquals(firstAccountBefore, newFirst,
                            "Sorting by '" + value + "' should change order of first account");
                }
            }
        }
        logOut();
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        logIn();
        // Twitter
        openAndVerifyExternalLink("twitter.com", "twitter.com");
        // Facebook
        openAndVerifyExternalLink("facebook.com", "facebook.com");
        // LinkedIn
        openAndVerifyExternalLink("linkedin.com", "linkedin.com");
        logOut();
    }
}