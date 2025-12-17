package GPT5.ws01.seq08;

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
    private static final String LOGIN = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ---------- Helper methods ----------

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        userField.clear();
        userField.sendKeys(user);
        WebElement passField = driver.findElement(By.id("password"));
        passField.clear();
        passField.sendKeys(pass);
        WebElement btn = driver.findElement(By.id("login-button"));
        btn.click();
    }

    private void ensureLoggedIn() {
        if (!driver.getCurrentUrl().contains("/inventory.html")) {
            login(LOGIN, PASSWORD);
            wait.until(ExpectedConditions.urlContains("/inventory.html"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        }
    }

    private void openMenu() {
        ensureLoggedIn();
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burger.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-cross-btn")));
    }

    private void closeMenuIfOpen() {
        List<WebElement> cross = driver.findElements(By.id("react-burger-cross-btn"));
        if (!cross.isEmpty() && cross.get(0).isDisplayed()) {
            cross.get(0).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.bm-menu-wrap")));
        }
    }

    private void resetAppStateFromMenu() {
        openMenu();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        closeMenuIfOpen();
        // Verify cart badge cleared (if existed)
        Assertions.assertEquals(0, driver.findElements(By.className("shopping_cart_badge")).size(),
                "Cart badge should be cleared after Reset App State");
    }

    private String switchToNewWindowIfAny(String originalHandle) {
        wait.until(d -> d.getWindowHandles().size() > 1 || !driver.getCurrentUrl().contains("saucedemo.com"));
        Set<String> handles = new HashSet<>(driver.getWindowHandles());
        handles.remove(originalHandle);
        if (!handles.isEmpty()) {
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            return newHandle;
        }
        return originalHandle;
    }

    private List<String> getItemNames() {
        List<WebElement> names = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        return names.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getItemPrices() {
        List<WebElement> prices = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        return prices.stream()
                .map(WebElement::getText)
                .map(s -> s.replace("$", "").trim())
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    // ---------- Tests ----------

    @Test
    @Order(1)
    public void testInvalidLoginShowsError() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("invalid_pass");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("username and password do not match"),
                "Invalid login should show appropriate error");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/index.html"), "Should remain on login page");
    }

    @Test
    @Order(2)
    public void testValidLoginNavigatesToInventory() {
        login(LOGIN, PASSWORD);
        WebElement inventory = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventory.isDisplayed(), "Inventory container should be visible");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "URL should be inventory.html");
    }

    @Test
    @Order(3)
    public void testSortingDropdownAllOptions() {
        ensureLoggedIn();
        Select sort = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("select.product_sort_container"))));

        sort.selectByValue("az");
        List<String> az = getItemNames();

        sort.selectByValue("za");
        List<String> za = getItemNames();
        List<String> azSorted = new ArrayList<>(az);
        Collections.sort(azSorted);
        List<String> zaSorted = new ArrayList<>(az);
        Collections.sort(zaSorted, Comparator.reverseOrder());

        Assertions.assertAll(
                () -> Assertions.assertNotEquals(az.get(0), za.get(0), "First item should change between A-Z and Z-A"),
                () -> Assertions.assertEquals(azSorted, az, "A-Z option should sort names ascending"),
                () -> Assertions.assertEquals(zaSorted, za, "Z-A option should sort names descending")
        );

        sort.selectByValue("lohi");
        List<Double> lohi = getItemPrices();

        sort.selectByValue("hilo");
        List<Double> hilo = getItemPrices();

        List<Double> lohiSorted = new ArrayList<>(lohi);
        Collections.sort(lohiSorted);
        List<Double> hiloSorted = new ArrayList<>(lohi);
        Collections.sort(hiloSorted, Comparator.reverseOrder());

        Assertions.assertAll(
                () -> Assertions.assertNotEquals(lohi.get(0), hilo.get(0), "First price should change between Lo->Hi and Hi->Lo"),
                () -> Assertions.assertEquals(lohiSorted, lohi, "Low to High should sort prices ascending"),
                () -> Assertions.assertEquals(hiloSorted, hilo, "High to Low should sort prices descending")
        );
    }

    @Test
    @Order(4)
    public void testMenuOpenClose() {
        ensureLoggedIn();
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burger.click();
        WebElement cross = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-cross-btn")));
        Assertions.assertTrue(cross.isDisplayed(), "Menu cross button should be visible when menu open");
        cross.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div.bm-menu-wrap")));
        Assertions.assertEquals(0, driver.findElements(By.id("react-burger-cross-btn")).size(), "Menu should be closed");
    }

    @Test
    @Order(5)
    public void testMenuAllItemsNavigatesToInventory() {
        ensureLoggedIn();
        // Navigate off inventory first (cart)
        wait.until(ExpectedConditions.elementToBeClickable(By.className("shopping_cart_link"))).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "All Items should navigate to inventory");
        closeMenuIfOpen();
    }

    @Test
    @Order(6)
    public void testMenuAboutExternalLink() {
        ensureLoggedIn();
        String originalHandle = driver.getWindowHandle();
        openMenu();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();

        // About may open same tab or new tab; handle both
        String currentHandle = switchToNewWindowIfAny(originalHandle);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("saucelabs.com"),
                ExpectedConditions.urlContains("saucedemo.com") // fallback while redirecting
        ));
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("saucelabs.com"), "About should navigate to saucelabs.com domain");

        if (!currentHandle.equals(originalHandle)) {
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            // if in same tab, go back to the app
            driver.navigate().back();
        }
        ensureLoggedIn(); // make sure we're back to inventory
    }

    @Test
    @Order(7)
    public void testAddRemoveItemsAndResetAppState() {
        ensureLoggedIn();
        // Add two items to cart
        List<WebElement> addButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item .pricebar button")));
        addButtons.get(0).click();
        addButtons.get(1).click();

        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("2", badge.getText(), "Cart badge should show 2 after adding two items");

        // Reset App State from menu clears cart
        resetAppStateFromMenu();
        Assertions.assertEquals(0, driver.findElements(By.className("shopping_cart_badge")).size(),
                "Cart badge should be removed after reset");
    }

    @Test
    @Order(8)
    public void testCheckoutFlowToCompletion() {
        ensureLoggedIn();
        // Add first available item
        WebElement firstAdd = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item .pricebar button")));
        firstAdd.click();
        // Go to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.className("shopping_cart_link"))).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        // Proceed to checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name"))).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement complete = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(complete.getText().toUpperCase().contains("THANK YOU FOR YOUR ORDER"),
                "Completion message should be visible");
        // Back Home and reset
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        resetAppStateFromMenu();
    }

    @Test
    @Order(9)
    public void testFooterSocialLinksExternal() {
        ensureLoggedIn();
        String original = driver.getWindowHandle();

        // Twitter
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='twitter.com']")));
        twitter.click();
        String hTwitter = switchToNewWindowIfAny(original);
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open twitter.com");
        if (!hTwitter.equals(original)) { driver.close(); driver.switchTo().window(original); } else { driver.navigate().back(); }

        // Facebook
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='facebook.com']")));
        facebook.click();
        String hFacebook = switchToNewWindowIfAny(original);
        wait.until(ExpectedConditions.urlContains("facebook.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open facebook.com");
        if (!hFacebook.equals(original)) { driver.close(); driver.switchTo().window(original); } else { driver.navigate().back(); }

        // LinkedIn
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='linkedin.com']")));
        linkedin.click();
        String hLinked = switchToNewWindowIfAny(original);
        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open linkedin.com");
        if (!hLinked.equals(original)) { driver.close(); driver.switchTo().window(original); } else { driver.navigate().back(); }

        ensureLoggedIn(); // return to a known state
    }

    @Test
    @Order(10)
    public void testMenuLogoutReturnsToLogin() {
        ensureLoggedIn();
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlContains("/index.html"));
        Assertions.assertTrue(driver.findElement(By.id("login-button")).isDisplayed(), "Login button should be visible after logout");
    }
}
