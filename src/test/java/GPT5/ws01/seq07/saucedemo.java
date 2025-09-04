package GPT5.ws01.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class SauceDemoHeadlessSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String INVENTORY_URL_PART = "/v1/inventory.html";
    private static final String LOGIN_USER = "standard_user";
    private static final String LOGIN_PASS = "secret_sauce";

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().deleteAllCookies();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ---------------- Helpers ----------------

    private void goToLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login_button_container")));
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains(INVENTORY_URL_PART)) {
            goToLoginPage();
            WebElement user = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
            WebElement pass = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
            user.clear(); user.sendKeys(LOGIN_USER);
            pass.clear(); pass.sendKeys(LOGIN_PASS);
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
            btn.click();
            wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        }
    }

    private void openBurgerMenu() {
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burger.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bm-menu-wrap")));
    }

    private void closeBurgerMenuIfOpen() {
        List<WebElement> close = driver.findElements(By.id("react-burger-cross-btn"));
        if (!close.isEmpty() && close.get(0).isDisplayed()) {
            close.get(0).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("bm-menu-wrap")));
        }
    }

    private List<String> getItemNames() {
        List<WebElement> names = driver.findElements(By.cssSelector(".inventory_item_name"));
        return names.stream().map(e -> e.getText().trim()).collect(Collectors.toList());
    }

    private List<Double> getItemPrices() {
        List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        return prices.stream()
                .map(e -> e.getText().replace("$", "").trim())
                .map(Double::valueOf)
                .collect(Collectors.toList());
    }

    private void selectSort(String value) {
        Select sel = new Select(wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select.product_sort_container"))));
        sel.selectByValue(value);
        // Wait for list to stabilize by ensuring at least one item visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list .inventory_item")));
    }

    private void resetAppStateViaMenu() {
        openBurgerMenu();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        closeBurgerMenuIfOpen();
        // cart badge should disappear if present
        Assertions.assertTrue(driver.findElements(By.cssSelector(".shopping_cart_badge")).isEmpty(),
                "Cart badge should be cleared after Reset App State");
    }

    private void openExternalAndAssertDomain(WebElement link, String expectedDomainFragment) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        wait.until(d -> d.getWindowHandles().size() != before.size() || !d.getWindowHandle().equals(original));
        Set<String> after = driver.getWindowHandles();
        after.removeAll(before);
        if (!after.isEmpty()) {
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment),
                    "External URL should contain: " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(original);
        } else {
            // same tab navigation fallback
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment),
                    "External URL should contain: " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
        }
    }

    // ---------------- Tests ----------------

    @Test
    @Order(1)
    void testValidLoginNavigatesToInventory() {
        goToLoginPage();
        WebElement user = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement pass = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        user.clear(); user.sendKeys(LOGIN_USER);
        pass.clear(); pass.sendKeys(LOGIN_PASS);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        btn.click();
        wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
        WebElement inv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inv.isDisplayed(), "Inventory container should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLoginShowsError() {
        goToLoginPage();
        WebElement user = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement pass = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        user.clear(); user.sendKeys("invalid_user");
        pass.clear(); pass.sendKeys("bad_password");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();
        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error'], .error-message-container")));
        Assertions.assertTrue(err.getText().length() > 0, "An error message should be shown for invalid login");
    }

    @Test
    @Order(3)
    void testSortingDropdownChangesOrder() {
        loginIfNeeded();
        // Baseline names and prices
        selectSort("az");
        List<String> namesAZ = getItemNames();
        Assertions.assertTrue(namesAZ.size() > 1, "Should have multiple items to sort (A-Z)");
        List<String> sortedAZ = new ArrayList<>(namesAZ);
        Collections.sort(sortedAZ);
        Assertions.assertEquals(sortedAZ, namesAZ, "A-Z sort should be ascending by name");

        selectSort("za");
        List<String> namesZA = getItemNames();
        List<String> sortedZA = new ArrayList<>(namesZA);
        List<String> reversed = new ArrayList<>(sortedAZ);
        Collections.reverse(reversed);
        Assertions.assertEquals(reversed, namesZA, "Z-A sort should be descending by name");

        selectSort("lohi");
        List<Double> pricesLoHi = getItemPrices();
        List<Double> sortedLoHi = new ArrayList<>(pricesLoHi);
        Collections.sort(sortedLoHi);
        Assertions.assertEquals(sortedLoHi, pricesLoHi, "Low-High sort should be ascending by price");

        selectSort("hilo");
        List<Double> pricesHiLo = getItemPrices();
        List<Double> sortedHiLo = new ArrayList<>(pricesHiLo);
        Collections.sort(sortedHiLo, Comparator.reverseOrder());
        Assertions.assertEquals(sortedHiLo, pricesHiLo, "High-Low sort should be descending by price");
    }

    @Test
    @Order(4)
    void testBurgerMenuOpenCloseAndAllItems() {
        loginIfNeeded();
        openBurgerMenu();
        Assertions.assertTrue(driver.findElement(By.id("bm-menu-wrap")).isDisplayed(), "Menu should be visible when opened");
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        // Inventory should still be visible
        wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
        Assertions.assertTrue(driver.findElement(By.id("inventory_container")).isDisplayed(), "Inventory should be visible after clicking All Items");
        closeBurgerMenuIfOpen();
    }

    @Test
    @Order(5)
    void testBurgerMenuAboutIsExternal() {
        loginIfNeeded();
        openBurgerMenu();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        openExternalAndAssertDomain(about, "saucelabs.com");
        // ensure we are back on inventory (if same-tab opened, we navigated back)
        driver.get("https://www.saucedemo.com/v1/inventory.html");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    @Test
    @Order(6)
    void testAddToCartAndResetAppState() {
        loginIfNeeded();
        // Add first item to cart
        WebElement firstCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list .inventory_item")));
        WebElement addBtn = firstCard.findElement(By.cssSelector("button.btn_inventory"));
        addBtn.click();
        // cart badge becomes 1
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText().trim(), "Cart badge should show 1 after adding an item");
        // Reset app state
        resetAppStateViaMenu();
        // Verify any Add to cart buttons are back (not 'Remove')
        WebElement addAgain = firstCard.findElement(By.cssSelector("button.btn_inventory"));
        Assertions.assertTrue(addAgain.getText().toLowerCase().contains("add"),
                "After reset, button should show Add to cart");
    }

    @Test
    @Order(7)
    void testLogoutViaMenu() {
        loginIfNeeded();
        openBurgerMenu();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login_button_container")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/v1/index.html") || driver.getTitle().toLowerCase().contains("swag"),
                "After logout, should be on login page");
    }

    @Test
    @Order(8)
    void testFooterExternalLinks() {
        loginIfNeeded();
        // Attempt to find and validate external links in footer
        List<WebElement> twitter = driver.findElements(By.cssSelector(".social_twitter a, a.social_twitter"));
        List<WebElement> facebook = driver.findElements(By.cssSelector(".social_facebook a, a.social_facebook"));
        List<WebElement> linkedin = driver.findElements(By.cssSelector(".social_linkedin a, a.social_linkedin"));

        if (!twitter.isEmpty()) openExternalAndAssertDomain(twitter.get(0), "twitter.com");
        if (!facebook.isEmpty()) openExternalAndAssertDomain(facebook.get(0), "facebook.com");
        if (!linkedin.isEmpty()) openExternalAndAssertDomain(linkedin.get(0), "linkedin.com");

        // At least one social link should exist
        Assertions.assertTrue(!twitter.isEmpty() || !facebook.isEmpty() || !linkedin.isEmpty(),
                "At least one social link should be present in the footer");
    }

    @Test
    @Order(9)
    void testCheckoutFlowSuccess() {
        loginIfNeeded();
        // Ensure clean state
        resetAppStateViaMenu();

        // Add first item and go to cart
        WebElement first = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list .inventory_item")));
        first.findElement(By.cssSelector("button.btn_inventory")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        driver.findElement(By.id("shopping_cart_container")).click();
        wait.until(ExpectedConditions.urlContains("/v1/cart.html"));
        // Checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click();
        wait.until(ExpectedConditions.urlContains("/v1/checkout-step-one.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name"))).sendKeys("Test");
        driver.findElement(By.id("last-name")).sendKeys("User");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        wait.until(ExpectedConditions.urlContains("/v1/checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.urlContains("/v1/checkout-complete.html"));
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(completeHeader.getText().toUpperCase().contains("THANK YOU"),
                "Completion message should indicate success");
        // Back Home
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
    }

    @Test
    @Order(10)
    void testItemDetailsPageNavigation() {
        loginIfNeeded();
        WebElement firstName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
        String name = firstName.getText().trim();
        firstName.click();
        wait.until(ExpectedConditions.urlContains("/v1/inventory-item.html"));
        WebElement detailName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_details_name, .inventory_details_name.large_size")));
        Assertions.assertEquals(name, detailName.getText().trim(), "Detail page title should match clicked item name");
        // Back
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.inventory_details_back_button"))).click();
        wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
    }
}
