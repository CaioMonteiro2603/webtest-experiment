package GPT20b.ws02.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class ParabankTest {
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Helper methods ---------- */

    private void loginValid() {
        driver.get(BASE_URL);
        By userField = By.id("userName");
        By pwField = By.id("userPassword");
        By loginBtn = By.xpath("//input[@type='submit' and @value='Log In']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(userField)).sendKeys(USERNAME);
        wait.until(ExpectedConditions.visibilityOfElementLocated(pwField)).sendKeys(PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        // After successful login, the accounts table should be visible
        By accountsTable = By.cssSelector("table");
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(accountsTable));
        Assertions.assertTrue(table.isDisplayed(), "Accounts table not displayed after login.");
    }

    private void loginInvalid(String usr, String pwd) {
        driver.get(BASE_URL);
        By userField = By.id("userName");
        By pwField = By.id("userPassword");
        By loginBtn = By.xpath("//input[@type='submit' and @value='Log In']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(userField)).sendKeys(usr);
        wait.until(ExpectedConditions.visibilityOfElementLocated(pwField)).sendKeys(pwd);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    private void navigateBack() {
        driver.navigate().back();
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        loginValid();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        loginInvalid("wronguser", "wrongpass");
        By errorMsg = By.className("validationError");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
        Assertions.assertTrue(error.getText().contains("incorrect")
            || error.getText().contains("incorrectly")
            || error.getText().contains("invalid"),
            "Unexpected error message for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginValid();

        // Assumption: a select element for sorting exists
        List<WebElement> sortEls = driver.findElements(By.cssSelector("select#sortBy"));
        Assumptions.assumeTrue(!sortEls.isEmpty(), "Sorting dropdown not present; skipping test.");

        Select sortSelect = new Select(sortEls.get(0));
        String[] options = sortSelect.getOptions().stream()
                .map(WebElement::getText)
                .toArray(String[]::new);

        // Grab item names before and after each sort to verify change
        for (String option : options) {
            sortSelect.selectByVisibleText(option);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("tbody tr:last-child > td:nth-child(1)"))); // wait for table update
            List<WebElement> names = driver.findElements(By.cssSelector("table tbody tr td:nth-child(1)"));
            Assertions.assertFalse(names.isEmpty(), "No account names found after sorting.");
            String firstName = names.get(0).getText();
            Assertions.assertNotNull(firstName, "First account name should not be null.");
        }
    }

    @Test
    @Order(4)
    public void testNavigationLinks() {
        loginValid();

        // Find all relative links in the top nav bar
        List<WebElement> navLinks = driver.findElements(By.cssSelector("ul.nav-list a[href^='http']"));
        // If none, try relative paths
        if (navLinks.isEmpty()) {
            navLinks = driver.findElements(By.cssSelector("ul.nav-list a:not([href^='http'])"));
        }

        String originalUrl = driver.getCurrentUrl();
        for (WebElement link : navLinks) {
            String href = link.getAttribute("href");
            // open link
            link.click();

            // Wait for URL to change
            wait.until(driver -> !driver.getCurrentUrl().equals(originalUrl));
            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                "Navigated URL does not contain expected path: " + href);

            // Navigate back
            navigateBack();
            wait.until(ExpectedConditions.urlToBe(originalUrl));
        }
    }

    @Test
    @Order(5)
    public void testExternalAboutLink() {
        loginValid();

        // Search for any link that contains 'about' and is external
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href*='about']"));
        if (externalLinks.isEmpty()) {
            externalLinks = driver.findElements(By.cssSelector("a[href^='http']").stream()
                    .filter(e -> !e.getAttribute("href").contains("parabank.parasoft.com"))
                    .toArray(WebElement[]::new));
        }

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            String originalWindow = driver.getWindowHandle();
            link.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains("parabank.parasoft.com") || driver.getCurrentUrl().contains("about"),
                            "External About link URL does not contain expected domain.");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        loginValid();

        // Social links usually have recognizable text or href domains
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(
                "a[href*='twitter.com'], a[href*='facebook.com'], a[href*='linkedin.com']"));
        Assertions.assertFalse(socialLinks.isEmpty(), "No social links found in footer.");

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            link.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                            "Opened link URL does not contain expected domain: " + href);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}