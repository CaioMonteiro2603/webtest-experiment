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
        // Wait for page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        // Verify form and input fields exist
        findElement("form#login-form, form", "form[action*='login']", ".login-form", "#login-form");
        findElement("input#username, input[name='username'], input#email", "input[type='text']", "input[name='username']", "input#username");
        findElement("input#password, input[name='password']", "input[type='password']", "input#password");
        findElement("button#login, button[type='submit'], button.login", "button[type='submit']", "input[type='submit']", "button#submit");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        driver.navigate().to(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        findElement("input#username, input[name='username'], input#email", "input[type='text']", "input[name='username']", "input#username").sendKeys("invalid_user");
        findElement("input#password, input[type='password']", "input#password").sendKeys("wrong_pass");
        findElement("button#login, button[type='submit'], button.login", "button[type='submit']", "input[type='submit']", "button#submit").click();

        List<WebElement> errors = driver.findElements(By.cssSelector(".error, .alert-danger, .wrong-credentials, .alert", ".error-message"));
        assertFalse(errors.isEmpty(), "Login error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testValidLoginAndLogout() {
        driver.navigate().to(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        findElement("input#username, input[name='username'], input#email", "input[type='text']", "input[name='username']", "input#username").sendKeys("standard_user");
        findElement("input#password, input[type='password']", "input#password").sendKeys("secret_sauce");
        findElement("button#login, button[type='submit'], button.login", "button[type='submit']", "input[type='submit']", "button#submit").click();

        // Verify we are on the inventory page
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/inventory.html"),
                ExpectedConditions.urlContains("/overview"),
                ExpectedConditions.urlContains("/room")
        ));
        List<WebElement> items = driver.findElements(By.cssSelector(".product-card, .inventory_item, .room-card, .room-listing"));
        assertFalse(items.isEmpty(), "Inventory items should be visible after successful login");

        // Logout
        findElement("a#logout, button#logout, button.logout", "a[href*='logout']", "button.logout").click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.urlContains("/index.html"),
                ExpectedConditions.urlContains("/")
        ));
        assertTrue(driver.getCurrentUrl().contains("/login") || driver.getCurrentUrl().contains("/index.html") || driver.getCurrentUrl().equals(BASE_URL),
                "Should be back on login screen after logout");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        driver.navigate().to(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        findElement("input#username, input[name='username'], input#email", "input[type='text']", "input[name='username']", "input#username").sendKeys("standard_user");
        findElement("input#password, input[type='password']", "input#password").sendKeys("secret_sauce");
        findElement("button#login, button[type='submit'], button.login", "button[type='submit']", "input[type='submit']", "button#submit").click();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("select#sort-options, select[name='sort']", "select", ".sort-dropdown")));
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
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        findElement("input#username, input[name='username'], input#email", "input[type='text']", "input[name='username']", "input#username").sendKeys("standard_user");
        findElement("input#password, input[type='password']", "input#password").sendKeys("secret_sauce");
        findElement("button#login, button[type='submit'], button.login", "button[type='submit']", "input[type='submit']", "button#submit").click();

        // Open burger menu
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("nav .burger-menu, button#menu, .menu-toggle", "button.menu", ".menu-button", "button[aria-label*='menu']")));
        burger.click();

        // All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='all-items'], a#menu-home, nav a[href*='inventory']", "a[href*='inventory']", ".menu-item a", "nav a")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html") || ExpectedConditions.urlContains("/room"));
        assertFalse(driver.findElements(By.cssSelector(".product-card, .inventory_item, .room-card, .room-listing")).isEmpty(),
                "All Items should display the inventory list");

        // Reopen menu for About
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("nav .burger-menu, button#menu, .menu-toggle", "button.menu", ".menu-button", "button[aria-label*='menu']")));
        burger.click();
        openAndVerifyExternalLink("about", "about");

        // Reopen menu for Reset App State (if exists)
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("nav .burger-menu, button#menu, .menu-toggle", "button.menu", ".menu-button", "button[aria-label*='menu']")));
        burger.click();
        List<WebElement> resetLinks = driver.findElements(By.cssSelector("a[href*='reset']"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            wait.until(ExpectedConditions.urlContains("/inventory.html") || ExpectedConditions.urlContains("/room"));
        }

        // Reopen menu for Logout
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("nav .burger-menu, button#menu, .menu-toggle", "button.menu", ".menu-button", "button[aria-label*='menu']")));
        burger.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a#logout, a[href*='logout'], button#logout", "a[href*='logout']", ".logout-link", "button.logout")));
        logoutLink.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.urlContains("/index.html"),
                ExpectedConditions.urlContains("/")
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