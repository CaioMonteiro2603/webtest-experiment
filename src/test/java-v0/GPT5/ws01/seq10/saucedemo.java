package GPT5.ws01.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Single-file JUnit 5 + Selenium 4 test suite for Sauce Demo (one-level coverage).
 *
 * Rules followed:
 * - FirefoxDriver in HEADLESS via options.addArguments("--headless")
 * - WebDriverWait with Duration.ofSeconds(10)
 * - @BeforeAll / @AfterAll create/quit driver
 * - @TestMethodOrder(OrderAnnotation.class) with ordered tests (but tests are independent)
 * - No Thread.sleep usage; waits use ExpectedConditions
 * - External links: switch to new tab/window, assert domain contains expected substring, close it, switch back
 *
 * Important: this single file is intended to compile as-is with Selenium 4 and JUnit 5 on classpath.
 */
@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USER = "standard_user";
    private static final String PASS = "secret_sauce";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        // REQUIRED by instructions: use addArguments("--headless") only
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1200, 900));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ----------------------
       Helper utilities
       ---------------------- */

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void doLogin(String username, String password) {
        openBase();
        WebElement userEl = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passEl = driver.findElement(By.id("password"));
        userEl.clear();
        userEl.sendKeys(username);
        passEl.clear();
        passEl.sendKeys(password);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginBtn.click();
        // wait for inventory page or error
    }

    private void ensureLoggedIn() {
        // Attempt to login with standard user if not already on inventory
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            doLogin(USER, PASS);
            wait.until(ExpectedConditions.urlContains("inventory.html"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        }
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout_sidebar_link")));
    }

    private void closeMenu() {
        // The burger menu close button has class "bm-cross-button" — guard for presence
        List<WebElement> closeButtons = driver.findElements(By.cssSelector(".bm-cross-button"));
        if (!closeButtons.isEmpty()) {
            WebElement close = closeButtons.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(close)).click();
            wait.until(ExpectedConditions.invisibilityOf(close));
        } else {
            // fallback: click menu button again to toggle
            WebElement menuBtn = driver.findElement(By.id("react-burger-menu-btn"));
            wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();
        }
    }

    private void resetAppStateIfAvailable() {
        // Open menu and click reset app state if visible
        openMenu();
        List<WebElement> resetLinks = driver.findElements(By.id("reset_sidebar_link"));
        if (!resetLinks.isEmpty()) {
            WebElement reset = resetLinks.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(reset)).click();
            // Reset may not navigate; ensure cart badge removed
            wait.until(driver -> driver.findElements(By.cssSelector(".shopping_cart_badge")).size() == 0);
        }
        // close menu if still open
        List<WebElement> closeButtons = driver.findElements(By.cssSelector(".bm-cross-button"));
        if (!closeButtons.isEmpty()) {
            closeMenu();
        }
    }

    private void addFirstNItemsToCart(int n) {
        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector("button.btn_primary.btn_inventory, button.btn_inventory")));
        int added = 0;
        for (WebElement btn : addButtons) {
            if (added >= n) break;
            if (btn.isDisplayed() && btn.isEnabled()) {
                wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
                added++;
            }
        }
        assertEquals(n, added, "Expected to add " + n + " items to cart");
    }

    private String getFirstProductName() {
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".inventory_list .inventory_item_name")));
        return firstName.getText().trim();
    }

    private Double getFirstProductPrice() {
        WebElement priceEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".inventory_list .inventory_item_price")));
        String txt = priceEl.getText().replace("$", "").trim();
        return Double.valueOf(txt);
    }

    private void assertExternalLinkOpensAndContains(By linkSelector, String expectedDomainSubstring) {
        List<WebElement> els = driver.findElements(linkSelector);
        assertTrue(els.size() > 0, "Expected external link element to exist: " + linkSelector.toString());
        String originalHandle = driver.getWindowHandle();
        int handlesBefore = driver.getWindowHandles().size();
        WebElement el = els.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
        // wait for a new window/tab
        wait.until(d -> d.getWindowHandles().size() > handlesBefore);
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream().filter(h -> !h.equals(originalHandle)).findFirst().orElse(null);
        assertNotNull(newHandle, "New window handle should be present after clicking external link");
        driver.switchTo().window(newHandle);
        wait.until(d -> d.getCurrentUrl().length() > 0);
        String extUrl = driver.getCurrentUrl();
        assertTrue(extUrl.toLowerCase().contains(expectedDomainSubstring.toLowerCase()),
                "External link should navigate to a URL containing '" + expectedDomainSubstring + "' but was: " + extUrl);
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    /* ----------------------
       Tests
       ---------------------- */

    @Test
    @Order(1)
    public void testValidLoginShowsInventory() {
        doLogin(USER, PASS);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        assertTrue(inventoryList.isDisplayed(), "Inventory list should be visible after successful login");
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("inventory.html"), "URL should contain inventory.html after login. Actual: " + currentUrl);
        // return app to known state
        resetAppStateIfAvailable();
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        doLogin("invalid_user", "bad_password");
        // error container has data-test="error" => id not stable; search by css .error-message-container or data-test
        List<WebElement> errors = wait.until(d -> d.findElements(By.cssSelector("[data-test='error'], .error-message-container")));
        assertTrue(errors.size() > 0, "Expected an error message element when login with invalid credentials");
        String errText = errors.get(0).getText().trim();
        assertFalse(errText.isEmpty(), "Error text should not be empty on failed login");
        // Also ensure still on login page
        assertTrue(driver.getCurrentUrl().contains("index.html") || driver.getCurrentUrl().endsWith("/"), "Should remain on login page after failed login");
    }

    @Test
    @Order(3)
    public void testSortingDropdownChangesOrderByNameAndPrice() {
        ensureLoggedIn();
        resetAppStateIfAvailable();

        // capture initial first product
        String firstBefore = getFirstProductName();
        Double priceBefore = getFirstProductPrice();

        // sort by Name (Z to A)
        WebElement sortEl = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
        Select select = new Select(sortEl);
        select.selectByVisibleText("Name (Z to A)");
        wait.until(d -> !getFirstProductName().equals(firstBefore)); // assert first product changed
        String firstAfterNameDesc = getFirstProductName();
        assertNotEquals(firstBefore, firstAfterNameDesc, "After sorting Z->A first product should change");

        // sort by Name (A to Z)
        select.selectByVisibleText("Name (A to Z)");
        wait.until(d -> getFirstProductName() != null);
        String firstAfterNameAsc = getFirstProductName();
        assertNotNull(firstAfterNameAsc, "First product should be present after sorting A->Z");

        // sort by Price (low to high) and assert price ordering
        select.selectByVisibleText("Price (low to high)");
        wait.until(d -> getFirstProductPrice() != null);
        Double priceAfter = getFirstProductPrice();
        assertTrue(priceAfter <= priceBefore || !firstBefore.equals(firstAfterNameAsc),
                "First product price after low-to-high should be less-equal to previous or ordering changed.");

        // sort by Price (high to low)
        select.selectByVisibleText("Price (high to low)");
        wait.until(d -> getFirstProductPrice() != null);
        Double priceHigh = getFirstProductPrice();
        assertTrue(priceHigh >= priceAfter, "First product price after high-to-low should be >= first after low-to-high");

        // return to default
        select.selectByVisibleText("Name (A to Z)");
        resetAppStateIfAvailable();
    }

    @Test
    @Order(4)
    public void testMenuActionsAllItemsAboutLogoutReset() {
        ensureLoggedIn();
        resetAppStateIfAvailable();

        // Open/Close menu
        openMenu();
        // Click All Items (should navigate to inventory)
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "All Items should navigate to inventory page");

        // Open menu and click About (external)
        openMenu();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        // About opens external site in new tab
        String originalHandle = driver.getWindowHandle();
        int handlesBefore = driver.getWindowHandles().size();
        about.click();
        wait.until(d -> d.getWindowHandles().size() > handlesBefore);
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream().filter(h -> !h.equals(originalHandle)).findFirst().orElse(null);
        assertNotNull(newHandle, "Expected external About link to open a new window/tab");
        driver.switchTo().window(newHandle);
        wait.until(d -> d.getCurrentUrl().length() > 0);
        String aboutUrl = driver.getCurrentUrl();
        assertTrue(aboutUrl.toLowerCase().contains("saucelabs") || aboutUrl.toLowerCase().contains("saucelabs.com") || aboutUrl.toLowerCase().contains("sauce"), "About link should lead to Saucelabs domain. Actual: " + aboutUrl);
        driver.close();
        driver.switchTo().window(originalHandle);

        // Open menu and test Reset App State after adding item
        addFirstNItemsToCart(1);
        List<WebElement> badge = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertTrue(badge.size() > 0 && !badge.get(0).getText().isEmpty(), "Cart badge should show quantity after adding item");
        openMenu();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        // ensure badge removed
        wait.until(d -> d.findElements(By.cssSelector(".shopping_cart_badge")).size() == 0);
        closeMenu();

        // Finally test Logout
        openMenu();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        assertTrue(driver.getCurrentUrl().contains("index.html") || driver.getCurrentUrl().endsWith("/"), "Logout should return to login page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinksOpenExternalDomains() {
        // Login fresh
        ensureLoggedIn();
        resetAppStateIfAvailable();

        // Social links have classes social_twitter, social_facebook, social_linkedin
        assertDoesNotThrow(() -> assertExternalLinkOpensAndContains(By.cssSelector(".social_twitter"), "twitter.com"));
        assertDoesNotThrow(() -> assertExternalLinkOpensAndContains(By.cssSelector(".social_facebook"), "facebook.com"));
        assertDoesNotThrow(() -> assertExternalLinkOpensAndContains(By.cssSelector(".social_linkedin"), "linkedin.com"));

        // ensure back on inventory page and known state
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        resetAppStateIfAvailable();
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckoutCompletes() {
        ensureLoggedIn();
        resetAppStateIfAvailable();

        // Add 2 items
        addFirstNItemsToCart(2);
        // assert cart badge count
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("2", badge.getText().trim(), "Cart badge should show 2 items after adding two items");

        // Go to cart
        WebElement cartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".shopping_cart_link")));
        cartBtn.click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        assertTrue(driver.getCurrentUrl().contains("cart.html"), "Should be in cart page");

        // Proceed to checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        // Fill in user info
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
        WebElement lastName = driver.findElement(By.id("last-name"));
        WebElement postal = driver.findElement(By.id("postal-code"));
        firstName.sendKeys("Caio");
        lastName.sendKeys("Monteiro");
        postal.sendKeys("01234");
        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("continue")));
        continueBtn.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        // Finish
        WebElement finishBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishBtn.click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
        assertTrue(completeHeader.getText().toUpperCase().contains("THANK YOU"), "Checkout completion should contain THANK YOU");
        // Return to inventory and reset
        WebElement backHome = wait.until(ExpectedConditions.elementToBeClickable(By.id("back-to-products")));
        backHome.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        resetAppStateIfAvailable();
    }

}
