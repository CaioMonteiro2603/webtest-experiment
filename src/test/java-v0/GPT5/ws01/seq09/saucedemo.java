package GPT5.ws01.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    // Common locators (v1)
    private static final By USERNAME_INPUT = By.id("user-name");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");

    private static final By INVENTORY_CONTAINER = By.id("inventory_container");
    private static final By INVENTORY_ITEM_NAMES = By.cssSelector(".inventory_item_name");
    private static final By INVENTORY_ITEM_PRICES = By.cssSelector(".inventory_item_price");
    private static final By SORT_SELECT = By.cssSelector("select.product_sort_container");

    private static final By BURGER_BUTTON = By.id("react-burger-menu-btn");
    private static final By BURGER_CLOSE_BUTTON = By.id("react-burger-cross-btn");
    private static final By SIDE_MENU = By.cssSelector(".bm-menu-wrap");
    private static final By MENU_ALL_ITEMS = By.id("inventory_sidebar_link");
    private static final By MENU_ABOUT = By.id("about_sidebar_link");
    private static final By MENU_LOGOUT = By.id("logout_sidebar_link");
    private static final By MENU_RESET = By.id("reset_sidebar_link");

    private static final By CART_LINK = By.id("shopping_cart_container");
    private static final By CART_BADGE = By.cssSelector(".shopping_cart_badge");
    private static final By CART_LIST = By.cssSelector(".cart_list");
    private static final By CHECKOUT_BUTTON = By.cssSelector(".checkout_button");
    private static final By FIRST_NAME_INPUT = By.id("first-name");
    private static final By LAST_NAME_INPUT = By.id("last-name");
    private static final By POSTAL_CODE_INPUT = By.id("postal-code");
    private static final By CONTINUE_BUTTON = By.cssSelector(".cart_button, .btn_primary.cart_button");
    private static final By FINISH_BUTTON = By.cssSelector(".cart_button, .btn_action.cart_button");
    private static final By COMPLETE_CONTAINER = By.cssSelector("#checkout_complete_container, .checkout_complete_container");

    private static final By FOOTER_TWITTER = By.cssSelector("a.social_twitter");
    private static final By FOOTER_FACEBOOK = By.cssSelector("a.social_facebook");
    private static final By FOOTER_LINKEDIN = By.cssSelector("a.social_linkedin");

    @BeforeAll
    public static void beforeAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    }

    @AfterAll
    public static void afterAll() {
        if (driver != null) driver.quit();
    }

    /* ---------- Helpers ---------- */

    private void openLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_BUTTON));
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("/inventory.html")) {
            openLoginPage();
            wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT)).sendKeys(USERNAME);
            driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
            wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON)).click();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));
        }
    }

    private void openMenu() {
        if (driver.findElements(SIDE_MENU).isEmpty() || !driver.findElement(SIDE_MENU).isDisplayed()) {
            clickWhenClickable(BURGER_BUTTON);
            wait.until(ExpectedConditions.visibilityOfElementLocated(SIDE_MENU));
        }
    }

    private void closeMenuIfOpen() {
        if (!driver.findElements(BURGER_CLOSE_BUTTON).isEmpty()) {
            clickWhenClickable(BURGER_CLOSE_BUTTON);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(SIDE_MENU));
        }
    }

    private void clickWhenClickable(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
    }

    private void resetAppStateIfPossible() {
        if (driver.getCurrentUrl().contains("inventory.html")) {
            openMenu();
            List<WebElement> reset = driver.findElements(MENU_RESET);
            if (!reset.isEmpty()) {
                clickWhenClickable(MENU_RESET);
                // After reset, any cart badge should vanish
                wait.until(d -> driver.findElements(CART_BADGE).isEmpty());
            }
            closeMenuIfOpen();
        }
    }

    private void assertExternalLink(By locator, String expectedDomain) {
        // Works whether link opens new tab or same tab
        String original = driver.getWindowHandle();
        Set<String> old = driver.getWindowHandles();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();
        // Either new tab appears or URL changes
        if (driver.getWindowHandles().size() > old.size()) {
            Set<String> diff = new HashSet<>(driver.getWindowHandles());
            diff.removeAll(old);
            String newHandle = diff.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should contain domain: " + expectedDomain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should contain domain: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("saucedemo.com"));
        }
    }

    private List<String> getItemNames() {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(INVENTORY_ITEM_NAMES))
                .stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getItemPrices() {
        List<WebElement> els = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(INVENTORY_ITEM_PRICES));
        List<Double> prices = new ArrayList<>();
        for (WebElement e : els) {
            String t = e.getText().replace("$", "").trim();
            try {
                prices.add(Double.parseDouble(t));
            } catch (NumberFormatException ignored) {}
        }
        return prices;
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void loginPage_ShowsRequiredElements() {
        openLoginPage();
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.findElement(USERNAME_INPUT).isDisplayed(), "Username field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(PASSWORD_INPUT).isDisplayed(), "Password field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(LOGIN_BUTTON).isDisplayed(), "Login button should be visible")
        );
    }

    @Test
    @Order(2)
    public void login_WithInvalidCredentials_ShowsError() {
        openLoginPage();
        wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT)).sendKeys("invalid_user");
        driver.findElement(PASSWORD_INPUT).sendKeys("wrong_password");
        clickWhenClickable(LOGIN_BUTTON);
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error'], .error-message-container")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("epic sadface") || error.isDisplayed(), "Invalid login should show an error message");
    }

    @Test
    @Order(3)
    public void login_WithValidCredentials_NavigatesToInventory() {
        loginIfNeeded();
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should be inventory.html after login"),
                () -> Assertions.assertFalse(driver.findElements(INVENTORY_CONTAINER).isEmpty(), "Inventory container should be visible")
        );
        resetAppStateIfPossible();
    }

    @Test
    @Order(4)
    public void sortingDropdown_ChangesOrderOfItems() {
        loginIfNeeded();
        resetAppStateIfPossible();

        Select sort = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(SORT_SELECT)));

        // A to Z
        sort.selectByValue("az");
        List<String> azNames = getItemNames();
        Assertions.assertFalse(azNames.isEmpty(), "There should be items listed after sorting A-Z");
        List<String> sortedAZ = new ArrayList<>(azNames);
        Collections.sort(sortedAZ);
        Assertions.assertEquals(sortedAZ, azNames, "A-Z sorting should list items alphabetically");

        // Z to A
        sort.selectByValue("za");
        List<String> zaNames = getItemNames();
        List<String> sortedZA = new ArrayList<>(azNames);
        Collections.sort(sortedZA, Collections.reverseOrder());
        Assertions.assertEquals(sortedZA, zaNames, "Z-A sorting should list items in reverse alphabetical order");

        // Low to High price
        sort.selectByValue("lohi");
        List<Double> lohi = getItemPrices();
        List<Double> sortedLoHi = new ArrayList<>(lohi);
        Collections.sort(sortedLoHi);
        Assertions.assertEquals(sortedLoHi, lohi, "Low-High sorting should list prices ascending");

        // High to Low price
        sort.selectByValue("hilo");
        List<Double> hilo = getItemPrices();
        List<Double> sortedHiLo = new ArrayList<>(hilo);
        sortedHiLo.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedHiLo, hilo, "High-Low sorting should list prices descending");
    }

    @Test
    @Order(5)
    public void burgerMenu_OpenClose_AllItems() {
        loginIfNeeded();
        openMenu();
        Assertions.assertTrue(driver.findElement(SIDE_MENU).isDisplayed(), "Side menu should open");
        clickWhenClickable(MENU_ALL_ITEMS);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        closeMenuIfOpen();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "All Items should keep us on inventory");
    }

    @Test
    @Order(6)
    public void burgerMenu_About_IsExternal() {
        loginIfNeeded();
        openMenu();
        assertExternalLink(MENU_ABOUT, "saucelabs.com");
        // Ensure still in inventory after coming back
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        closeMenuIfOpen();
    }

    @Test
    @Order(7)
    public void footerSocialLinks_OpenExternally() {
        loginIfNeeded();
        // Links are present in footer of inventory page
        assertExternalLink(FOOTER_TWITTER, "twitter.com");
        assertExternalLink(FOOTER_FACEBOOK, "facebook.com");
        assertExternalLink(FOOTER_LINKEDIN, "linkedin.com");
    }

    @Test
    @Order(8)
    public void addToCart_CheckoutAndReset_Flow() {
        loginIfNeeded();
        resetAppStateIfPossible();

        // Add first two items to cart
        List<WebElement> addButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item .btn_primary.btn_inventory")));
        Assumptions.assumeTrue(addButtons.size() >= 2, "Need at least two items to add to cart");
        wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(0))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(CART_BADGE));
        Assertions.assertEquals("1", driver.findElement(CART_BADGE).getText(), "Cart badge should be 1 after adding first item");

        wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(1))).click();
        Assertions.assertEquals("2", driver.findElement(CART_BADGE).getText(), "Cart badge should be 2 after adding second item");

        // Go to cart
        clickWhenClickable(CART_LINK);
        wait.until(ExpectedConditions.urlContains("cart.html"));
        Assertions.assertFalse(driver.findElements(CART_LIST).isEmpty(), "Cart list should be visible");

        // Checkout
        clickWhenClickable(CHECKOUT_BUTTON);
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME_INPUT)).sendKeys("Test");
        driver.findElement(LAST_NAME_INPUT).sendKeys("User");
        driver.findElement(POSTAL_CODE_INPUT).sendKeys("12345");
        clickWhenClickable(CONTINUE_BUTTON);

        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        clickWhenClickable(FINISH_BUTTON);

        wait.until(ExpectedConditions.urlContains("checkout-complete"));
        Assertions.assertTrue(driver.findElements(COMPLETE_CONTAINER).size() > 0, "Completion container should be displayed on success");

        // Back to inventory and reset app state
        driver.navigate().to("https://www.saucedemo.com/v1/inventory.html");
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));
        resetAppStateIfPossible();
        Assertions.assertTrue(driver.findElements(CART_BADGE).isEmpty(), "Cart badge should be cleared after Reset App State");
    }

    @Test
    @Order(9)
    public void burgerMenu_Logout_ReturnsToLogin() {
        loginIfNeeded();
        openMenu();
        clickWhenClickable(MENU_LOGOUT);
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Should be back at login page after logout");
        // Ensure login form is visible
        Assertions.assertTrue(driver.findElement(LOGIN_BUTTON).isDisplayed(), "Login button should be visible on login page");
    }
}
