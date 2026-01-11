package GPT5.ws01.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Test data
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USER = "standard_user";
    private static final String PASS = "secret_sauce";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ----------------- Helpers -----------------

    private void goToLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login_button_container")));
    }

    private void login(String user, String pass) {
        goToLogin();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).clear();
        driver.findElement(By.id("user-name")).sendKeys(user);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(pass);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        btn.click();
        wait.until(ExpectedConditions.urlContains("/v1/inventory.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    private boolean isOnInventory() {
        return driver.getCurrentUrl().contains("/v1/inventory.html")
                && driver.findElements(By.id("inventory_container")).size() > 0;
    }

    private void openMenu() {
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burger.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));
    }

    private void resetAppStateViaMenu() {
        if (!isOnInventory()) login(USER, PASS);
        openMenu();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        // close menu to apply visuals
        WebElement close = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        close.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-cross-btn")));
    }

    private List<String> getVisibleItemNames() {
        List<WebElement> names = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        return names.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getVisibleItemPrices() {
        List<WebElement> prices = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        return prices.stream().map(e -> Double.parseDouble(e.getText().replace("$", ""))).collect(Collectors.toList());
    }

    private void selectSortOption(String visibleTextValue) {
        WebElement select = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        select.click();
        // Options have values: az, za, lohi, hilo
        select.findElement(By.cssSelector("option[value='" + visibleTextValue + "']")).click();
        // assert a tiny wait for DOM update by waiting for first item to be clickable
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list")));
    }

    private void assertOpensExternalAndReturn(By linkLocator, String expectedDomainPart) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(linkLocator));
        link.click();
        wait.until(d -> d.getWindowHandles().size() > before.size());
        Set<String> after = driver.getWindowHandles();
        after.removeAll(before);
        String newHandle = after.iterator().next();
        driver.switchTo().window(newHandle);
        wait.until(d -> d.getCurrentUrl().toLowerCase().contains(expectedDomainPart));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomainPart),
                "External link should navigate to domain containing: " + expectedDomainPart);
        driver.close();
        driver.switchTo().window(original);
    }

    // ----------------- Tests -----------------

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USER, PASS);
        Assertions.assertTrue(isOnInventory(), "User should land on inventory page after login");
        WebElement cart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shopping_cart_container")));
        Assertions.assertTrue(cart.isDisplayed(), "Cart icon should be visible");
        resetAppStateViaMenu();
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        goToLogin();
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();
        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(err.getText().toLowerCase().contains("username and password do not match".toLowerCase())
                        || err.getText().toLowerCase().contains("do not match any user".toLowerCase()),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdownAllOptions() {
        login(USER, PASS);

        // Capture initial states for comparison
        List<String> initialNames = getVisibleItemNames();
        List<Double> initialPrices = getVisibleItemPrices();

        // Name (A to Z)
        selectSortOption("az");
        List<String> namesAZ = getVisibleItemNames();
        List<String> sortedAZ = new ArrayList<>(namesAZ);
        Collections.sort(sortedAZ);
        Assertions.assertEquals(sortedAZ, namesAZ, "Names should be sorted A->Z");

        // Name (Z to A)
        selectSortOption("za");
        List<String> namesZA = getVisibleItemNames();
        List<String> sortedZA = new ArrayList<>(namesZA);
        sortedZA.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedZA, namesZA, "Names should be sorted Z->A");

        // Price (low to high)
        selectSortOption("lohi");
        List<Double> pricesLoHi = getVisibleItemPrices();
        List<Double> sortedLoHi = new ArrayList<>(pricesLoHi);
        Collections.sort(sortedLoHi);
        Assertions.assertEquals(sortedLoHi, pricesLoHi, "Prices should be sorted low->high");

        // Price (high to low)
        selectSortOption("hilo");
        List<Double> pricesHiLo = getVisibleItemPrices();
        List<Double> sortedHiLo = new ArrayList<>(pricesHiLo);
        sortedHiLo.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedHiLo, pricesHiLo, "Prices should be sorted high->low");

        // Ensure sorting changed something from initial at least once
        Assertions.assertAll(
                () -> Assertions.assertNotEquals(initialNames, namesZA, "At least one sort should change item order"),
                () -> Assertions.assertNotEquals(initialPrices, pricesLoHi, "At least one sort should change price order")
        );

        resetAppStateViaMenu();
    }

    @Test
    @Order(4)
    public void testMenuAllItemsAndReset() {
        login(USER, PASS);

        // Add item to create state
        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item .btn_primary.btn_inventory")));
        if (!addButtons.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(0))).click();
        }

        // Open menu and click Reset App State
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link"))).click();

        // Click All Items
        wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link"))).click();

        // Close menu
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-cross-btn")));

        // Verify cart is cleared (no badge)
        boolean hasBadge = driver.findElements(By.className("shopping_cart_badge")).size() > 0;
        Assertions.assertFalse(hasBadge, "Cart badge should be cleared after Reset App State");

        resetAppStateViaMenu();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USER, PASS);
        openMenu();
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();

        wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().contains("saucedemo"));
        Set<String> after = driver.getWindowHandles();

        // If it navigates in same tab, just assert domain; otherwise handle new tab
        if (after.size() == before.size()) {
            wait.until(d -> d.getCurrentUrl().toLowerCase().contains("saucelabs"));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("saucelabs"),
                    "About should navigate to Sauce Labs domain");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("/v1/inventory.html"));
        } else {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl().toLowerCase().contains("saucelabs"));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("saucelabs"),
                    "About should open Sauce Labs domain in new tab");
            driver.close();
            driver.switchTo().window(original);
        }

        resetAppStateViaMenu();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USER, PASS);
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlContains("/v1/index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/v1/index.html"), "Should be back at login page after logout");
    }

    @Test
    @Order(7)
    public void testFooterSocialExternalLinks() {
        login(USER, PASS);

        // Twitter
        assertOpensExternalAndReturn(By.cssSelector("footer .social_twitter a"), "twitter.com");
        // Facebook
        assertOpensExternalAndReturn(By.cssSelector("footer .social_facebook a"), "facebook.com");
        // LinkedIn
        assertOpensExternalAndReturn(By.cssSelector("footer .social_linkedin a"), "linkedin.com");

        resetAppStateViaMenu();
    }

    @Test
    @Order(8)
    public void testAddRemoveCartAndCheckoutFlow() {
        login(USER, PASS);

        // Add two items
        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item .btn_primary.btn_inventory")));
        int toAdd = Math.min(2, addButtons.size());
        for (int i = 0; i < toAdd; i++) {
            wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(i))).click();
        }

        // Verify cart badge
        WebElement cartContainer = wait.until(ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals(String.valueOf(toAdd), badge.getText(), "Cart badge should match number of added items");

        // Go to cart
        cartContainer.click();
        wait.until(ExpectedConditions.urlContains("/v1/cart.html"));
        Assertions.assertTrue(driver.findElements(By.cssSelector(".cart_item")).size() >= toAdd, "Cart should list added items");

        // Checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click();
        wait.until(ExpectedConditions.urlContains("/v1/checkout-step-one.html"));

        // Fill info
        wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name"))).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/v1/checkout-step-two.html"));
        Assertions.assertTrue(driver.findElements(By.cssSelector(".summary_info")).size() > 0, "Summary info should be visible");

        // Finish
        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish"))).click();
        wait.until(ExpectedConditions.urlContains("/v1/checkout-complete.html"));
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(header.getText().toUpperCase().contains("THANK YOU"), "Completion header should thank the user");

        // Back home and ensure cart cleared
        wait.until(ExpectedConditions.elementToBeClickable(By.id("back-to-products"))).click();
        wait.until(ExpectedConditions.urlContains("/v1/inventory.html"));
        boolean badgeExists = driver.findElements(By.className("shopping_cart_badge")).size() > 0;
        Assertions.assertFalse(badgeExists, "Cart badge should be cleared after order completion");

        resetAppStateViaMenu();
    }
}
