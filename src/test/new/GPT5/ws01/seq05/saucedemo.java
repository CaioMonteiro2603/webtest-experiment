package GPT5.ws01.seq05;

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

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String INVENTORY_URL_PART = "/inventory.html";

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

    // ---------- Helpers ----------

    private void navigateToLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void standardLogin() {
        navigateToLogin();
        WebElement user = driver.findElement(By.id("user-name"));
        WebElement pass = driver.findElement(By.id("password"));
        WebElement btn = driver.findElement(By.id("login-button"));
        user.clear();
        pass.clear();
        user.sendKeys("standard_user");
        pass.sendKeys("secret_sauce");
        btn.click();
        wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
        Assertions.assertTrue(driver.getCurrentUrl().contains(INVENTORY_URL_PART), "Expected to be on inventory page after login.");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    private void openBurgerMenu() {
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burger.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));
    }

    private void closeBurgerMenuIfOpen() {
        List<WebElement> close = driver.findElements(By.id("react-burger-cross-btn"));
        if (!close.isEmpty() && close.get(0).isDisplayed()) {
            wait.until(ExpectedConditions.elementToBeClickable(close.get(0))).click();
            wait.until(ExpectedConditions.invisibilityOf(close.get(0)));
        }
    }

    private void resetAppStateIfPossible() {
        if (!driver.getCurrentUrl().contains(INVENTORY_URL_PART)) {
            if (!driver.getCurrentUrl().contains("/v1/")) {
                driver.get(BASE_URL);
            } else {
                driver.get(driver.getCurrentUrl().replaceAll("(index|cart|checkout-step-one|checkout-step-two|checkout-complete)\\.html", "inventory.html"));
            }
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        openBurgerMenu();
        List<WebElement> reset = driver.findElements(By.id("reset_sidebar_link"));
        if (!reset.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(reset.get(0))).click();
            // badge should disappear if present
            wait.until(d -> driver.findElements(By.cssSelector(".shopping_cart_badge")).isEmpty());
        }
        closeBurgerMenuIfOpen();
    }

    private void switchToNewWindowAndAssertDomainThenClose(String expectedDomain) {
        String original = driver.getWindowHandle();
        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(original)) {
                driver.switchTo().window(h);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomain),
                        "Expected external url to contain: " + expectedDomain);
                driver.close();
                driver.switchTo().window(original);
                break;
            }
        }
    }

    private List<String> getInventoryItemNames() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list")));
        List<WebElement> names = driver.findElements(By.cssSelector(".inventory_item_name"));
        if (names.isEmpty()) { // v1 sometimes uses different selector; fall back
            names = driver.findElements(By.cssSelector(".inventory_item .inventory_item_name"));
        }
        return names.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getInventoryItemPrices() {
        List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
        if (prices.isEmpty()) {
            prices = driver.findElements(By.cssSelector(".inventory_item .pricebar .inventory_item_price"));
        }
        return prices.stream()
                .map(WebElement::getText)
                .map(t -> t.replace("$", "").trim())
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    private void selectSortOption(String visibleTextContains) {
        WebElement select = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        select.click();
        // open options dropdown and choose by text contains
        List<WebElement> options = driver.findElements(By.cssSelector(".product_sort_container option"));
        for (WebElement opt : options) {
            if (opt.getText().toLowerCase().contains(visibleTextContains.toLowerCase())) {
                opt.click();
                break;
            }
        }
        // wait a beat for DOM resort; assert first item presence to stabilize
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list")));
    }

    private void addFirstInventoryItemToCart() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list")));
        List<WebElement> addButtons = driver.findElements(By.cssSelector(".inventory_item button.btn_primary.btn_inventory"));
        if (addButtons.isEmpty()) { // fallback to any first button within first item
            addButtons = driver.findElements(By.cssSelector(".inventory_item:first-of-type button"));
        }
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(0)));
        btn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
    }

    // ---------- Tests ----------

    @Test
    @Order(1)
    public void testLoginPageElementsVisible() {
        navigateToLogin();
        Assertions.assertAll("Login page elements",
                () -> Assertions.assertTrue(driver.findElement(By.id("user-name")).isDisplayed(), "Username input not displayed"),
                () -> Assertions.assertTrue(driver.findElement(By.id("password")).isDisplayed(), "Password input not displayed"),
                () -> Assertions.assertTrue(driver.findElement(By.id("login-button")).isDisplayed(), "Login button not displayed")
        );
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        navigateToLogin();
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-test='error'], .error-message-container")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("epic sadface") ||
                        error.getText().toLowerCase().contains("error"),
                "Expected an error message after invalid login");
    }

    @Test
    @Order(3)
    public void testValidLoginSuccess() {
        standardLogin();
        WebElement header = driver.findElement(By.cssSelector(".product_label"));
        Assertions.assertTrue(header.getText().toLowerCase().contains("products"), "Inventory header should mention 'Products'.");
    }

    @Test
    @Order(4)
    public void testSortingDropdownByNameAndPrice() {
        standardLogin();
        // Initial order
        List<String> initialNames = getInventoryItemNames();
        Assertions.assertTrue(initialNames.size() > 1, "Should have at least two items to sort by name.");

        // Z to A
        selectSortOption("Name (Z to A)");
        List<String> zToANames = getInventoryItemNames();
        Assertions.assertNotEquals(initialNames.get(0), zToANames.get(0), "First item should change after Z to A sorting.");

        // A to Z (default)
        selectSortOption("Name (A to Z)");
        List<String> aToZNames = getInventoryItemNames();
        Assertions.assertNotEquals(zToANames.get(0), aToZNames.get(0), "First item should change back after A to Z sorting.");

        // Price (low to high)
        selectSortOption("low to high");
        List<Double> lohi = getInventoryItemPrices();
        Assertions.assertTrue(isNonDecreasing(lohi), "Prices should be non-decreasing for Low to High sort.");

        // Price (high to low)
        selectSortOption("high to low");
        List<Double> hilo = getInventoryItemPrices();
        Assertions.assertTrue(isNonIncreasing(hilo), "Prices should be non-increasing for High to Low sort.");
    }

    private boolean isNonDecreasing(List<Double> values) {
        for (int i = 1; i < values.size(); i++) if (values.get(i) < values.get(i - 1)) return false;
        return true;
    }

    private boolean isNonIncreasing(List<Double> values) {
        for (int i = 1; i < values.size(); i++) if (values.get(i) > values.get(i - 1)) return false;
        return true;
    }

    @Test
    @Order(5)
    public void testMenuOpenClose() {
        standardLogin();
        openBurgerMenu();
        Assertions.assertTrue(driver.findElement(By.id("inventory_sidebar_link")).isDisplayed(), "Menu should be open.");
        WebElement close = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        close.click();
        wait.until(ExpectedConditions.invisibilityOf(close));
        Assertions.assertTrue(driver.findElements(By.id("react-burger-cross-btn")).isEmpty() ||
                        !driver.findElements(By.id("react-burger-cross-btn")).get(0).isDisplayed(),
                "Menu should be closed.");
    }

    @Test
    @Order(6)
    public void testMenuAllItemsNavigation() {
        standardLogin();
        // Navigate elsewhere first (cart)
        driver.findElement(By.cssSelector("a.shopping_cart_link")).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        openBurgerMenu();
        driver.findElement(By.id("inventory_sidebar_link")).click();
        wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
        Assertions.assertTrue(driver.getCurrentUrl().contains(INVENTORY_URL_PART), "All Items should navigate to inventory.");
    }

    @Test
    @Order(7)
    public void testMenuAboutExternalLink() {
        standardLogin();
        openBurgerMenu();
        String originalWindow = driver.getWindowHandle();
        int before = driver.getWindowHandles().size();
        WebElement about = driver.findElement(By.id("about_sidebar_link"));
        // Some environments navigate same tab; to standardize, open via JS in new tab using href
        String href = about.getAttribute("href");
        if (href == null || href.isEmpty()) {
            about.click();
        } else {
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],'_blank')", href);
        }
        if (driver.getWindowHandles().size() > before) {
            switchToNewWindowAndAssertDomainThenClose("saucelabs.com");
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains("saucelabs.com"));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("saucelabs.com"),
                    "About should navigate to saucelabs.com");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
        }
        closeBurgerMenuIfOpen();
    }

    @Test
    @Order(8)
    public void testResetAppStateFromMenu() {
        standardLogin();
        addFirstInventoryItemToCart();
        Assertions.assertFalse(driver.findElements(By.cssSelector(".shopping_cart_badge")).isEmpty(),
                "Cart badge should be present after adding item.");
        openBurgerMenu();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        // After reset, badge disappears
        wait.until(d -> driver.findElements(By.cssSelector(".shopping_cart_badge")).isEmpty());
        Assertions.assertTrue(driver.findElements(By.cssSelector(".shopping_cart_badge")).isEmpty(),
                "Cart badge should be cleared after Reset App State.");
        closeBurgerMenuIfOpen();
    }

    @Test
    @Order(9)
    public void testFooterSocialExternalLinks() {
        standardLogin();
        List<String> domains = Arrays.asList("twitter.com", "facebook.com", "linkedin.com");
        for (String domain : domains) {
            List<WebElement> links = driver.findElements(By.cssSelector("footer .social a[href*='" + domain + "']"));
            if (!links.isEmpty()) {
                String href = links.get(0).getAttribute("href");
                String originalWindow = driver.getWindowHandle();
                int before = driver.getWindowHandles().size();
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],'_blank')", href);
                if (driver.getWindowHandles().size() > before) {
                    switchToNewWindowAndAssertDomainThenClose(domain);
                    driver.switchTo().window(originalWindow);
                } else {
                    links.get(0).click();
                    wait.until(ExpectedConditions.urlContains(domain));
                    Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(domain), "Expected external URL to contain " + domain);
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
                }
            }
        }
    }

    @Test
    @Order(10)
    public void testCheckoutFlowToCompletion() {
        standardLogin();
        resetAppStateIfPossible();
        addFirstInventoryItemToCart();
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.shopping_cart_link")));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        WebElement checkout = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkout.click();

        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        WebElement summary = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".summary_info")));
        Assertions.assertTrue(summary.isDisplayed(), "Order summary should be visible.");

        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement complete = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(complete.getText().toUpperCase().contains("THANK YOU"), "Completion message should thank the user.");
        // Return to inventory for clean state
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains(INVENTORY_URL_PART));
        resetAppStateIfPossible();
    }

    @Test
    @Order(11)
    public void testLogoutFromMenu() {
        standardLogin();
        openBurgerMenu();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("/index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("/index.html"), "Should be back on login page after logout.");
        // Ensure login form is visible
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button"))).isDisplayed(),
                "Login button should be visible after logout.");
    }
}