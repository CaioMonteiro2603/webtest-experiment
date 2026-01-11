package GPT5.ws01.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String INVENTORY_URL = "https://www.saucedemo.com/v1/inventory.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().deleteAllCookies();
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ---------- Helpers ----------

    private void openLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
    }

    private void loginIfNeeded() {
        if (!isOnInventory()) {
            openLoginPage();
            WebElement user = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
            WebElement pass = driver.findElement(By.id("password"));
            WebElement btn = driver.findElement(By.id("login-button"));
            user.clear();
            user.sendKeys(USERNAME);
            pass.clear();
            pass.sendKeys(PASSWORD);
            btn.click();
            wait.until(ExpectedConditions.urlContains("inventory"));
        }
    }

    private boolean isOnInventory() {
        return driver.getCurrentUrl().contains("inventory.html") ||
                driver.findElements(By.id("shopping_cart_container")).size() > 0;
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));
    }

    private void closeMenuIfOpen() {
        List<WebElement> closeBtns = driver.findElements(By.id("react-burger-cross-btn"));
        if (!closeBtns.isEmpty() && closeBtns.get(0).isDisplayed()) {
            closeBtns.get(0).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-cross-btn")));
        }
    }

    private void resetAppState() {
        // requires being logged in
        openMenu();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        closeMenuIfOpen();
        // cart badge should disappear (if present)
        Assertions.assertEquals(0, driver.findElements(By.className("shopping_cart_badge")).size(),
                "After Reset App State, cart badge should be cleared");
    }

    private void assertSorted(List<String> values, boolean ascending) {
        List<String> sorted = new ArrayList<>(values);
        sorted.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());
        Assertions.assertEquals(sorted, values, "List should be sorted " + (ascending ? "ascending" : "descending"));
    }

    private void assertPricesSorted(List<Double> prices, boolean ascending) {
        List<Double> sorted = new ArrayList<>(prices);
        sorted.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());
        Assertions.assertEquals(sorted, prices, "Prices should be sorted " + (ascending ? "low to high" : "high to low"));
    }

    // ---------- Tests ----------

    @Test
    @Order(1)
    public void testLoginPageVisible() {
        openLoginPage();
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("login_logo")));
        Assertions.assertTrue(logo.isDisplayed(), "Login logo should be visible");
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "URL should be login page");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        openLoginPage();
        driver.findElement(By.id("user-name")).sendKeys("locked_out_user");
        driver.findElement(By.id("password")).sendKeys("wrong");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("epic sadface") || error.getText().toLowerCase().contains("username"),
                "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testValidLoginNavigatesToInventory() {
        loginIfNeeded();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be on inventory page after login");
    }

    @Test
    @Order(4)
    public void testInventoryItemDetailAndBack() {
        loginIfNeeded();
        List<WebElement> items = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item")));
        Assertions.assertFalse(items.isEmpty(), "There should be at least one inventory item");
        WebElement firstItemLink = items.get(0).findElement(By.cssSelector(".inventory_item_name"));
        String firstName = firstItemLink.getText();
        wait.until(ExpectedConditions.elementToBeClickable(firstItemLink)).click();
        wait.until(ExpectedConditions.urlContains("inventory-item.html"));
        WebElement detailName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_details_name")));
        Assertions.assertEquals(firstName, detailName.getText(), "Detail page should show the same item name");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    @Test
    @Order(5)
    public void testSortingDropdownAllOptions() {
        loginIfNeeded();
        Select sort = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product_sort_container"))));

        // Name (A to Z)
        sort.selectByVisibleText("Name (A to Z)");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        List<String> namesAZ = driver.findElements(By.cssSelector(".inventory_item_name"))
                .stream().map(WebElement::getText).collect(Collectors.toList());
        assertSorted(new ArrayList<>(namesAZ), true);

        // Name (Z to A)
        sort.selectByVisibleText("Name (Z to A)");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        List<String> namesZA = driver.findElements(By.cssSelector(".inventory_item_name"))
                .stream().map(WebElement::getText).collect(Collectors.toList());
        assertSorted(new ArrayList<>(namesZA), false);

        // Price (low to high)
        sort.selectByVisibleText("Price (low to high)");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        List<Double> pricesLowHigh = driver.findElements(By.cssSelector(".inventory_item_price"))
                .stream().map(WebElement::getText)
                .map(s -> s.replace("$", ""))
                .map(Double::parseDouble).collect(Collectors.toList());
        assertPricesSorted(new ArrayList<>(pricesLowHigh), true);

        // Price (high to low)
        sort.selectByVisibleText("Price (high to low)");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        List<Double> pricesHighLow = driver.findElements(By.cssSelector(".inventory_item_price"))
                .stream().map(WebElement::getText)
                .map(s -> s.replace("$", ""))
                .map(Double::parseDouble).collect(Collectors.toList());
        assertPricesSorted(new ArrayList<>(pricesHighLow), false);
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckoutFlow() {
        loginIfNeeded();
        resetAppState(); // ensure clean cart

        List<WebElement> addButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item .btn_inventory")));
        Assertions.assertTrue(addButtons.size() >= 2, "Need at least two items to add to cart");
        wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(0))).click();
        wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(1))).click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("2", cartBadge.getText(), "Cart badge should be 2 after adding two items");

        driver.findElement(By.id("shopping_cart_container")).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        WebElement checkout = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkout.click();

        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("Test");
        driver.findElement(By.id("last-name")).sendKeys("User");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();

        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
        Assertions.assertTrue(completeHeader.getText().toUpperCase().contains("THANK YOU"),
                "Completion header should contain THANK YOU");

        // Return to products and reset state for independence
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        resetAppState();
    }

    @Test
    @Order(7)
    public void testMenuAllItemsAboutLogoutReset() {
        loginIfNeeded();

        // All Items
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(isOnInventory(), "All Items should take us to inventory");

        // About (external: saucelabs.com). It may open in same tab; handle both.
        openMenu();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String currentHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        about.click();
        // If new tab opens, switch; otherwise current URL changes
        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getWindowHandle().equals(currentHandle) || !d.getCurrentUrl().contains("inventory.html"));
        } catch (TimeoutException ignored) { }
        Set<String> after = driver.getWindowHandles();
        after.removeAll(before);
        if (!after.isEmpty()) {
            String newTab = after.iterator().next();
            driver.switchTo().window(newTab);
            wait.until(ExpectedConditions.urlContains("saucelabs.com"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About should navigate to saucelabs.com");
            driver.close();
            driver.switchTo().window(currentHandle);
        } else {
            // same tab
            wait.until(ExpectedConditions.urlContains("saucelabs.com"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About should navigate to saucelabs.com");
            driver.navigate().back();
        }
        // Ensure back on inventory
        driver.get(INVENTORY_URL);
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        // Reset App State
        resetAppState();

        // Logout
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Should be on login page after logout");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinksExternal() {
        loginIfNeeded();
        // Scroll to footer to ensure links are interactable
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        List<WebElement> socialLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("footer .social a")));
        Assertions.assertTrue(socialLinks.size() >= 3, "Footer should contain social links");

        Map<String, String> expectedDomains = new HashMap<>();
        expectedDomains.put("Twitter", "twitter.com");
        expectedDomains.put("Facebook", "facebook.com");
        expectedDomains.put("LinkedIn", "linkedin.com");

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            String expectedDomain = href.contains("twitter") ? "twitter.com" :
                    href.contains("facebook") ? "facebook.com" :
                            href.contains("linkedin") ? "linkedin.com" : null;
            if (expectedDomain == null) continue;

            String original = driver.getWindowHandle();
            Set<String> before = driver.getWindowHandles();
            wait.until(ExpectedConditions.elementToBeClickable(link)).click();

            // Wait for new tab or URL change
            try {
                wait.until(d -> d.getWindowHandles().size() > before.size());
                Set<String> after = driver.getWindowHandles();
                after.removeAll(before);
                String newHandle = after.iterator().next();
                driver.switchTo().window(newHandle);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Social link should open domain: " + expectedDomain);
                driver.close();
                driver.switchTo().window(original);
            } catch (TimeoutException e) {
                // fallback if opened in same tab
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Social link should navigate to domain: " + expectedDomain);
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains("inventory.html"));
            }
        }
        // Restore known state
        driver.get(INVENTORY_URL);
        resetAppState();
    }
}
