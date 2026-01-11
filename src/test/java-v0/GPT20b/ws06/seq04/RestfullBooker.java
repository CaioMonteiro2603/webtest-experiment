package GPT20b.ws06.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static final String BASE_URL = "https://automationintesting.online/";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void initDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* --------------------------------------------------------------------- */
    /* Helper utilities                                                      */
    /* --------------------------------------------------------------------- */

    /** Finds the first element matching any of the given CSS selectors. */
    private WebElement findElement(String... cssSelectors) {
        for (String sel : cssSelectors) {
            List<WebElement> els = driver.findElements(By.cssSelector(sel));
            if (!els.isEmpty()) {
                return els.get(0);
            }
        }
        throw new NoSuchElementException("No element matched selectors: " + String.join(", ", cssSelectors));
    }

    /** Opens a link that contains the given fragment, verifies the resulting URL contains the expected domain,
     * then closes the new window/tab and returns to the original. */
    private void openAndVerifyExternalLink(String fragment, String domain) {
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + fragment + "']"));
        if (links.isEmpty()) return;
        WebElement link = links.get(0);
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        link.click();
        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newWindow = after.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(d1 -> d1.getCurrentUrl().contains(domain));
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(d1 -> d1.getCurrentUrl().contains(domain));
            driver.navigate().back();
        }
    }

    /* --------------------------------------------------------------------- */
    /* Test cases                                                            */
    /* --------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testLoginPageElementsPresent() {
        driver.navigate().to(BASE_URL);
        // Verify form and input fields exist
        findElement("form#login-form, form");
        findElement("input#username, input[name='username'], input#email");
        findElement("input#password, input[name='password']");
        findElement("button#login, button[type='submit'], button.login");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        driver.navigate().to(BASE_URL);
        findElement("input#username, input#email").sendKeys("invalid_user");
        findElement("input#password").sendKeys("wrong_pass");
        findElement("button#login, button.login").click();

        List<WebElement> errors = driver.findElements(By.cssSelector(".error, .alert-danger, .wrong-credentials"));
        assertFalse(errors.isEmpty(), "Login error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testValidLoginAndLogout() {
        driver.navigate().to(BASE_URL);
        findElement("input#username, input#email").sendKeys("standard_user");
        findElement("input#password").sendKeys("secret_sauce");
        findElement("button#login, button.login").click();

        // Verify we are on the inventory page
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/inventory.html"),
                ExpectedConditions.urlContains("/overview")
        ));
        List<WebElement> items = driver.findElements(By.cssSelector(".product-card, .inventory_item"));
        assertFalse(items.isEmpty(), "Inventory items should be visible after successful login");

        // Logout
        findElement("a#logout, button#logout, button.logout").click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.urlContains("/index.html")
        ));
        assertTrue(driver.getCurrentUrl().contains("/login") || driver.getCurrentUrl().contains("/index.html"),
                "Should be back on login screen after logout");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        driver.navigate().to(BASE_URL);
        findElement("input#username, input#email").sendKeys("standard_user");
        findElement("input#password").sendKeys("secret_sauce");
        findElement("button#login, button.login").click();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("select#sort-options, select[name='sort']")));
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        assertTrue(options.size() > 1, "Sorting dropdown should have at least two options");

        String firstValue = options.get(0).getAttribute("value");
        options.get(1).click();
        String secondValue = sortDropdown.getAttribute("value");
        assertNotEquals(firstValue, secondValue, "Selecting a different sort option should change the value");
    }

    @Test
    @Order(5)
    public void testBurgerMenuOptions() {
        driver.navigate().to(BASE_URL);
        findElement("input#username, input#email").sendKeys("standard_user");
        findElement("input#password").sendKeys("secret_sauce");
        findElement("button#login, button.login").click();

        // Open burger menu
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("nav .burger-menu, button#menu, .menu-toggle")));
        burger.click();

        // All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='all-items'], a#menu-home, nav a[href*='inventory']")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        assertFalse(driver.findElements(By.cssSelector(".product-card, .inventory_item")).isEmpty(),
                "All Items should display the inventory list");

        // Reopen menu for About
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("nav .burger-menu, button#menu, .menu-toggle")));
        burger.click();
        openAndVerifyExternalLink("about", "about");

        // Reopen menu for Reset App State (if exists)
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("nav .burger-menu, button#menu, .menu-toggle")));
        burger.click();
        List<WebElement> resetLinks = driver.findElements(By.cssSelector("a[href*='reset']"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            wait.until(ExpectedConditions.urlContains("/inventory.html"));
        }

        // Reopen menu for Logout
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("nav .burger-menu, button#menu, .menu-toggle")));
        burger.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a#logout, a[href*='logout'], button#logout")));
        logoutLink.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.urlContains("/index.html")
        ));
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.navigate().to(BASE_URL);
        openAndVerifyExternalLink("twitter.com", "twitter.com");
        openAndVerifyExternalLink("facebook.com", "facebook.com");
        openAndVerifyExternalLink("linkedin.com", "linkedin.com");
    }
}