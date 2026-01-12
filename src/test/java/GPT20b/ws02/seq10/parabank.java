package GPT20b.ws02.seq10;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
        driver.get(BASE_URL);
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

    /* ---------- Utility Methods ---------- */

    private static boolean isLoggedIn() {
        try {
            return driver.findElements(By.id("accountSummary")).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void login() {
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@type='submit' and @value='Log In']"));

        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("overview"),
                "Login did not navigate to account summary page");
    }

    private static void logout() {
        List<WebElement> signOutLinks = driver.findElements(By.linkText("Log Out"));
        if (!signOutLinks.isEmpty()) {
            WebElement signOut = wait.until(
                    ExpectedConditions.elementToBeClickable(signOutLinks.get(0)));
            signOut.click();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
            Assertions.assertEquals(
                    BASE_URL,
                    driver.getCurrentUrl(),
                    "Logout did not redirect to login page");
        }
    }

    private static void loginIfNeeded() {
        if (!isLoggedIn()) {
            login();
        }
    }

    private static void logoutIfNeeded() {
        if (isLoggedIn()) {
            logout();
        }
    }

    private static void openExternalLink(By locator, String expectedDomain) {
        String original = driver.getWindowHandle();
        driver.findElement(locator).click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(original))
                .findFirst()
                .orElseThrow();
        driver.switchTo().window(newHandle);

        Assertions.assertTrue(
                driver.getCurrentUrl().contains(expectedDomain),
                "External link URL does not contain expected domain: " + expectedDomain);

        driver.close();
        driver.switchTo().window(original);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testLoginValid() {
        driver.get(BASE_URL);
        login();
        Assertions.assertTrue(isLoggedIn(), "User should be logged in after valid credentials");
        logoutIfNeeded();
    }

    @Test
    @Order(2)
    public void testLoginInvalid() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@type='submit' and @value='Log In']"));

        userField.clear();
        userField.sendKeys("wronguser");
        passField.clear();
        passField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(
                errorMsg.getText().toLowerCase().contains("error"),
                "Error message for invalid credentials not displayed");
        Assertions.assertEquals(
                BASE_URL,
                driver.getCurrentUrl(),
                "URL should remain on login page after failed login");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        loginIfNeeded();
        By sortByLocator = By.id("sortBy");
        List<WebElement> sortElements = driver.findElements(sortByLocator);
        Assumptions.assumeTrue(!sortElements.isEmpty(), "Sorting dropdown not present, skipping test.");

        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(sortByLocator));
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() > 1, "Sorting dropdown has insufficient options, skipping test.");

        // Capture initial order
        List<WebElement> rowsBefore = driver.findElements(By.cssSelector("table#accountTable tbody tr"));
        Assertions.assertFalse(rowsBefore.isEmpty(), "No account rows found before sorting");
        String firstBefore = rowsBefore.get(0).findElement(By.tagName("td")).getText();

        for (int i = 0; i < options.size(); i++) {
            sortDropdown.click();
            WebElement opt = options.get(i);
            opt.click();

            // Wait for table to refresh (assuming small delay)
            wait.until(ExpectedConditions.stalenessOf(rowsBefore.get(0)));

            List<WebElement> rowsAfter = driver.findElements(By.cssSelector("table#accountTable tbody tr"));
            String firstAfter = rowsAfter.get(0).findElement(By.tagName("td")).getText();

            Assertions.assertNotEquals(
                    firstBefore,
                    firstAfter,
                    "Sorting option '" + opt.getText() + "' did not change account order");
            firstBefore = firstAfter;
            rowsBefore = rowsAfter;
        }
        logoutIfNeeded();
    }

    @Test
    @Order(4)
    public void testMenuOptions() {
        loginIfNeeded();

        // Ensure we are on account summary
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("overview"),
                "Not on account summary page after login");

        // Test Transfer Funds
        List<WebElement> transferLinks = driver.findElements(By.linkText("Transfer Funds"));
        if (!transferLinks.isEmpty()) {
            WebElement transferLink = wait.until(
                    ExpectedConditions.elementToBeClickable(transferLinks.get(0)));
            transferLink.click();
            wait.until(ExpectedConditions.urlContains("transfer"));
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("transfer"),
                    "Did not navigate to transfer page");
        }

        // Return to summary
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("overview"));

        // Test Deposit
        List<WebElement> depositLinks = driver.findElements(By.linkText("Deposit"));
        if (!depositLinks.isEmpty()) {
            WebElement depoLink = wait.until(
                    ExpectedConditions.elementToBeClickable(depositLinks.get(0)));
            depoLink.click();
            wait.until(ExpectedConditions.urlContains("deposit"));
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("deposit"),
                    "Did not navigate to deposit page");
        }

        // Return to summary
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("overview"));

        // Test Logout
        logout();
        Assertions.assertEquals(
                BASE_URL,
                driver.getCurrentUrl(),
                "Logout did not redirect to login page");
        // Re-login for the rest
        login();
        Assertions.assertTrue(isLoggedIn(), "Re-login failed after logout");
        logoutIfNeeded();
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        loginIfNeeded();

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a[href]"));
        Assumptions.assumeTrue(!footerLinks.isEmpty(), "No footer links found, skipping test.");

        String baseDomain = "parabank.parasoft.com";
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (href.contains(baseDomain)) continue; // skip internal links

            openExternalLink(By.linkText(link.getText()), href);
        }
        logoutIfNeeded();
    }
}