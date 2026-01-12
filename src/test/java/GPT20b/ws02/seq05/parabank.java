package GPT20b.ws02.seq05;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
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
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ------------------------------------------------------------------ */
    /* Helper methods                                                    */
    /* ------------------------------------------------------------------ */

    private void navigateToLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    private void login(String user, String pass) {
        navigateToLogin();
        WebElement userField = driver.findElement(By.name("username"));
        WebElement passField = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input[type='submit'][value='Log In']"));
        userField.clear();
        userField.sendKeys(user);
        passField.clear();
        passField.sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    private void assertLoggedIn() {
        // We expect the Accounts Overview page
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[@class='title' and contains(text(),'Accounts Overview')]")));
        assertNotNull(header, "Accounts Overview header not found after login");
    }

    private void logoutIfLoggedIn() {
        // Try to find the Logout link; if not found, do nothing
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Log Out"));
        if (!logoutLinks.isEmpty()) {
            WebElement logout = logoutLinks.get(0);
            if (logout.isDisplayed()) {
                wait.until(ExpectedConditions.elementToBeClickable(logout)).click();
                wait.until(ExpectedConditions.urlContains("index.htm"));
                // Verify login form back
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            }
        }
    }

    private Set<String> gatherExternalLinks() {
        Set<String> links = new HashSet<>();
        List<WebElement> anchors = driver.findElements(By.tagName("a"));
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (href.startsWith("http") && !href.contains("parabank.parasoft.com")) {
                links.add(href);
            }
        }
        return links;
    }

    private void testExternalLink(String url) {
        String original = driver.getWindowHandle();
        driver.get(url); // Open directly to avoid potential target handling
        wait.until(ExpectedConditions.urlToBe(url));
        // Verify the domain is different and contains the given URL
        assertTrue(driver.getCurrentUrl().contains(url),
                "External link did not navigate to expected URL: " + url);
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertEquals(original, driver.getWindowHandle(), "Did not return to original window");
    }

    /* ------------------------------------------------------------------ */
    /* Tests                                                              */
    /* ------------------------------------------------------------------ */

    @Test
    @Order(1)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_password");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.error")));
        assertTrue(error.getText().contains("The username and password could not be verified."),
                "Error message not displayed for invalid credentials");
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testValidLoginLogout() {
        login(USERNAME, PASSWORD);
        assertLoggedIn();
        logoutIfLoggedIn();
        // Verify back to login
        assertTrue(driver.getCurrentUrl().contains("index.htm"),
                "Did not return to login page after logout");
    }

    @Test
    @Order(3)
    public void testAccountOverviewDisplay() {
        login(USERNAME, PASSWORD);
        assertLoggedIn();
        List<WebElement> rows = driver.findElements(By.cssSelector("table#accountTable tbody tr"));
        assertFalse(rows.isEmpty(), "No account rows found on the overview page");
        // Verify column headers
        WebElement header = driver.findElement(By.cssSelector("table#accountTable thead tr"));
        assertTrue(header.getText().contains("Account") && header.getText().contains("Balance"),
                "Account table headers missing");
        logoutIfLoggedIn();
    }

    @Test
    @Order(4)
    public void testTransferFundsNavigation() {
        login(USERNAME, PASSWORD);
        assertLoggedIn();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Transfer Funds")));
        link.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        WebElement transferHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h1[@class='title']")));
        assertTrue(transferHeader.getText().contains("Transfer Funds"),
                "Did not navigate to Transfer Funds page");
        // Return to overview
        driver.navigate().back();
        assertLoggedIn();
        logoutIfLoggedIn();
    }

    @Test
    @Order(5)
    public void testExternalLinksFromOverview() {
        login(USERNAME, PASSWORD);
        assertLoggedIn();
        Set<String> externalLinks = gatherExternalLinks();
        for (String link : externalLinks) {
            testExternalLink(link);
        }
        logoutIfLoggedIn();
    }

    @Test
    @Order(6)
    public void testLogoutFunctionality() {
        login(USERNAME, PASSWORD);
        assertLoggedIn();
        logoutIfLoggedIn();
        // Confirm login form is visible
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.name("username")));
        assertTrue(userField.isDisplayed(), "Login form not visible after logout");
    }
}