package GPT5.ws01.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String INVENTORY_URL = "https://www.saucedemo.com/v1/inventory.html";
    private static final String CART_URL = "https://www.saucedemo.com/v1/cart.html";

    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setupClass() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1280, 900));
    }

    @AfterAll
    public static void teardownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    public void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login_button_container")));
    }

    // ---------------------- Helpers ----------------------

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("/inventory.html")) {
            login(USERNAME, PASSWORD);
        }
    }

    private void login(String user, String pass) {
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement button = driver.findElement(By.id("login-button"));
        username.clear();
        username.sendKeys(user);
        password.clear();
        password.sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(button)).click();
    }

    private void openMenu() {
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burger.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));
    }

    private void closeMenuIfOpen() {
        List<WebElement> closeBtns = driver.findElements(By.id("react-burger-cross-btn"));
        if (!closeBtns.isEmpty() && closeBtns.get(0).isDisplayed()) {
            closeBtns.get(0).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("inventory_sidebar_link")));
        }
    }

    private void resetAppState() {
        openMenu();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        closeMenuIfOpen();
        // Ensure cart badge is gone
        Assertions.assertEquals(0, driver.findElements(By.className("shopping_cart_badge")).size(), "Cart badge should be cleared after reset.");
    }

    private List<String> getInventoryItemNames() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list")));
        List<WebElement> names = driver.findElements(By.cssSelector(".inventory_item_name"));
        return names.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getInventoryPrices() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list")));
        List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        return prices.stream().map(e -> Double.parseDouble(e.getText().replace("$", ""))).collect(Collectors.toList());
    }

    private void switchToNewWindowIfOpenedAndAssertDomain(String expectedDomainFragment) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        // Wait briefly for potential new window
        try {
            wait.until(d -> driver.getWindowHandles().size() > before.size());
        } catch (TimeoutException ignored) {
            // No new window; remain in same tab
        }
        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newWin = after.iterator().next();
            driver.switchTo().window(newWin);
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment),
                    "Expected external URL to contain " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(original);
        } else {
            // Same tab navigation
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment),
                    "Expected external URL to contain " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("/inventory.html"));
        }
    }

    // ---------------------- Tests ----------------------

    @Test
    @Order(1)
    public void testLoginPageLoads() {
        Assertions.assertTrue(driver.getCurrentUrl().contains("/index.html"), "Base login URL should contain index.html");
        Assertions.assertTrue(driver.findElement(By.id("login_button_container")).isDisplayed(), "Login container should be visible");
        Assertions.assertTrue(driver.findElement(By.id("user-name")).isDisplayed(), "Username input should be visible");
        Assertions.assertTrue(driver.findElement(By.id("password")).isDisplayed(), "Password input should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        login("bad_user", "bad_password");
        // Error container variants
        By errSelector = By.cssSelector("h3[data-test='error'], .error-message-container");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errSelector));
        Assertions.assertTrue(error.getText().length() > 0, "Expected an error message for invalid credentials");
        // Ensure still on login
        Assertions.assertTrue(driver.getCurrentUrl().contains("/index.html"), "Should remain on login after invalid attempt");
    }

    @Test
    @Order(3)
    public void testValidLoginAndInventoryVisible() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlToBe(INVENTORY_URL));
        Assertions.assertEquals(INVENTORY_URL, driver.getCurrentUrl(), "Should navigate to inventory after login");

        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertTrue(items.size() > 0, "Inventory list should not be empty");
    }

    @Test
    @Order(4)
    public void testSortingDropdownAllOptionsAffectOrder() {
        loginIfNeeded();
        resetAppState();

        Select sort = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select.product_sort_container"))));

        // Default (A to Z)
        sort.selectByVisibleText("Name (A to Z)");
        List<String> aToZ = getInventoryItemNames();

        // Z to A
        sort.selectByVisibleText("Name (Z to A)");
        List<String> zToA = getInventoryItemNames();
        Assertions.assertNotEquals(aToZ.get(0), zToA.get(0), "First item should differ between A->Z and Z->A sorts");

        // Price Low to High
        sort.selectByVisibleText("Price (low to high)");
        List<Double> lowHigh = getInventoryPrices();
        List<Double> lowHighSorted = new ArrayList<>(lowHigh);
        Collections.sort(lowHighSorted);
        Assertions.assertEquals(lowHighSorted, lowHigh, "Prices should be ascending with Low->High sort");

        // Price High to Low
        sort.selectByVisibleText("Price (high to low)");
        List<Double> highLow = getInventoryPrices();
        List<Double> highLowSorted = new ArrayList<>(highLow);
        highLowSorted.sort(Collections.reverseOrder());
        Assertions.assertEquals(highLowSorted, highLow, "Prices should be descending with High->Low sort");
    }

    @Test
    @Order(5)
    public void testMenuOpenCloseAndAllItems() {
        loginIfNeeded();

        // Navigate to Cart to verify All Items brings us back
        driver.findElement(By.id("shopping_cart_container")).click();
        wait.until(ExpectedConditions.urlToBe(CART_URL));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("/cart.html"), "Should be on cart page");

        openMenu();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlToBe(INVENTORY_URL));
        Assertions.assertEquals(INVENTORY_URL, driver.getCurrentUrl(), "All Items should navigate to inventory");

        // Open and close
        openMenu();
        Assertions.assertTrue(driver.findElement(By.id("logout_sidebar_link")).isDisplayed(), "Logout link should be visible when menu open");
        closeMenuIfOpen();
        Assertions.assertTrue(driver.findElements(By.id("logout_sidebar_link")).isEmpty() || !driver.findElement(By.id("logout_sidebar_link")).isDisplayed(),
                "Logout link should be hidden after closing menu");
    }

    @Test
    @Order(6)
    public void testMenuAboutExternal() {
        loginIfNeeded();
        openMenu();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        switchToNewWindowIfOpenedAndAssertDomain("saucelabs.com");
    }

    @Test
    @Order(7)
    public void testResetAppStateFromMenu() {
        loginIfNeeded();

        // Add one item then reset
        List<WebElement> addButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("button.btn_inventory")));
        Assertions.assertTrue(addButtons.size() > 0, "Expected add to cart buttons");
        addButtons.get(0).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));

        resetAppState();
        Assertions.assertEquals(0, driver.findElements(By.className("shopping_cart_badge")).size(), "Cart badge should be cleared after reset");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinksExternal() {
        loginIfNeeded();

        Map<String, String> expectedDomains = new HashMap<>();
        expectedDomains.put("Twitter", "twitter.com");
        expectedDomains.put("Facebook", "facebook.com");
        expectedDomains.put("LinkedIn", "linkedin.com");

        for (Map.Entry<String, String> entry : expectedDomains.entrySet()) {
            String linkText = entry.getKey();
            String domain = entry.getValue();
            List<WebElement> links = driver.findElements(By.xpath("//footer//a[contains(., '" + linkText + "')]"));
            if (!links.isEmpty()) {
                String originalWindow = driver.getWindowHandle();
                int beforeCount = driver.getWindowHandles().size();

                wait.until(ExpectedConditions.elementToBeClickable(links.get(0))).click();

                // If new window opens, switch; otherwise same-tab navigation
                try {
                    wait.until(d -> driver.getWindowHandles().size() != beforeCount || !driver.getCurrentUrl().contains("/inventory.html"));
                } catch (TimeoutException ignored) {}

                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > beforeCount) {
                    handles.remove(originalWindow);
                    String newWin = handles.iterator().next();
                    driver.switchTo().window(newWin);
                    wait.until(ExpectedConditions.urlContains(domain));
                    Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(domain), "Expected to navigate to domain: " + domain);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                } else {
                    wait.until(ExpectedConditions.urlContains(domain));
                    Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(domain), "Expected to navigate to domain: " + domain);
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlContains("/inventory.html"));
                }
            } // if no link found, skip silently
        }
    }

    @Test
    @Order(9)
    public void testAddToCartAndCheckoutFlow() {
        loginIfNeeded();
        resetAppState();

        // Add first two items
        List<WebElement> addButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("button.btn_inventory")));
        int toAdd = Math.min(2, addButtons.size());
        for (int i = 0; i < toAdd; i++) {
            wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(i))).click();
        }
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals(String.valueOf(toAdd), cartBadge.getText(), "Cart badge should reflect number of items added");

        // Go to cart
        driver.findElement(By.id("shopping_cart_container")).click();
        wait.until(ExpectedConditions.urlToBe(CART_URL));
        List<WebElement> cartItems = driver.findElements(By.cssSelector(".cart_item"));
        Assertions.assertEquals(toAdd, cartItems.size(), "Cart should contain the added items");

        // Checkout step one
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".checkout_button"))).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.cssSelector(".cart_button")).click(); // continue

        // Step two
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        Assertions.assertTrue(driver.findElements(By.cssSelector(".summary_total_label")).size() > 0, "Order summary should be present");

        // Finish
        driver.findElement(By.cssSelector(".cart_button")).click(); // finish
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(header.getText().toUpperCase().contains("THANK YOU"), "Completion message should acknowledge the order");
    }

    @Test
    @Order(10)
    public void testLogoutFromMenu() {
        loginIfNeeded();
        openMenu();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("/index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/index.html"), "Should be back on login page after logout");
        // Ensure login form is present again
        Assertions.assertTrue(driver.findElement(By.id("login_button_container")).isDisplayed(), "Login form should be visible post-logout");
    }
}
