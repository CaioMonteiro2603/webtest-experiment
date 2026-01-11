package SunaGPT20b.ws03.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

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

    /** Helper: perform login if not already logged in */
    private void ensureLoggedIn() {
        driver.get(BASE_URL);
        if (driver.getCurrentUrl().contains("/inventory.html")) {
            return; // already logged in
        }
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test='username']"))).sendKeys(USER_EMAIL);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test='password']"))).sendKeys(USER_PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test='login-button']"))).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Login failed – inventory page not displayed");
    }

    /** Helper: open the burger menu */
    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));
    }

    /** Helper: click a menu item by its id */
    private void clickMenuItem(String itemId) {
        openMenu();
        WebElement item = wait.until(ExpectedConditions.elementToBeClickable(By.id(itemId)));
        item.click();
    }

    /** Helper: reset app state via menu */
    private void resetAppState() {
        clickMenuItem("reset_sidebar_link");
        // after reset, the inventory page should be visible
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    /** Helper: verify external link opens correct domain */
    private void verifyExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();

        // wait for new window
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link did not navigate to expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test='username']"))).sendKeys(USER_EMAIL);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test='password']"))).sendKeys(USER_PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test='login-button']"))).click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "After login URL should contain /inventory.html");

        Assertions.assertTrue(wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container"))
        ).isDisplayed(), "Inventory container should be visible after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test='username']"))).sendKeys("wrong@example.com");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test='password']"))).sendKeys("badpass");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test='login-button']"))).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(error.getText().toLowerCase().contains("username") ||
                        error.getText().toLowerCase().contains("password"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        ensureLoggedIn();
        By sortLocator = By.cssSelector("select.product_sort_container");
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(sortLocator));
        Select select = new Select(sortSelect);

        // Capture first item name for each option to verify change
        String firstItemBefore = driver.findElements(By.cssSelector(".inventory_item_name")).get(0).getText();

        // Iterate through all options
        for (WebElement option : select.getOptions()) {
            select.selectByVisibleText(option.getText());
            // Wait for sorting to reflect (first item may change)
            wait.until(driver -> {
                List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
                return items.size() > 0 && !items.get(0).getText().equals(firstItemBefore);
            });
            String firstItemAfter = driver.findElements(By.cssSelector(".inventory_item_name")).get(0).getText();
            Assertions.assertNotEquals(firstItemBefore, firstItemAfter,
                    "Sorting option '" + option.getText() + "' should change the order of items");
        }
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        ensureLoggedIn();
        clickMenuItem("inventory_sidebar_link");
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items menu should navigate to inventory page");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternal() {
        ensureLoggedIn();
        clickMenuItem("about_sidebar_link");
        // About opens external site in new tab
        verifyExternalLink(By.cssSelector("a[href*='github.com']"), "github.com");
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        ensureLoggedIn();
        clickMenuItem("logout_sidebar_link");
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to the login page");
    }

    @Test
    @Order(7)
    public void testResetAppState() {
        ensureLoggedIn();
        // Add an item to cart to change state
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button.btn_inventory"));
        if (!addButtons.isEmpty()) {
            addButtons.get(0).click();
            WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
            Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 item");
        }
        // Reset state
        resetAppState();
        // Verify cart badge cleared
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should be cleared after reset");
    }

    @Test
    @Order(8)
    public void testAddToCartAndCheckout() {
        ensureLoggedIn();
        // Add first two items to cart
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button.btn_inventory"));
        Assertions.assertTrue(addButtons.size() >= 2, "At least two items should be present");
        addButtons.get(0).click();
        addButtons.get(1).click();

        // Verify cart badge shows 2
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("2", badge.getText(), "Cart badge should show 2 items");

        // Go to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container"))).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart.html"), "Should be on cart page");

        // Checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        // Fill checkout info
        wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name"))).sendKeys("Test");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("last-name"))).sendKeys("User");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("postal-code"))).sendKeys("12345");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("continue"))).click();

        // Finish
        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish"))).click();
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement completeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(completeMsg.isDisplayed(), "Checkout complete message should be displayed");
        Assertions.assertTrue(completeMsg.getText().toLowerCase().contains("thank"),
                "Completion message should contain thank you text");

        // Reset state for other tests
        resetAppState();
    }

    @Test
    @Order(9)
    public void testFooterExternalLinks() {
        ensureLoggedIn();
        // Twitter
        verifyExternalLink(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        // Facebook
        verifyExternalLink(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        // LinkedIn
        verifyExternalLink(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
    }
}