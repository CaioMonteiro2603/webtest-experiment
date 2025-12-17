package SunaGPT20b.ws03.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String email, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    private void logoutIfLoggedIn() {
        if (driver.getCurrentUrl().contains("/inventory.html")) {
            openBurgerMenu();
            driver.findElement(By.id("logout_sidebar_link")).click();
            wait.until(ExpectedConditions.urlMatches(".*\\/"));
        }
    }

    private void openBurgerMenu() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-menu")));
    }

    private void resetAppState() {
        openBurgerMenu();
        driver.findElement(By.id("reset_sidebar_link")).click();
        // Wait for the badge to disappear if it was present
        wait.until(driver -> driver.findElements(By.className("shopping_cart_badge")).isEmpty());
    }

    private List<String> getInventoryItemNames() {
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        return items.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getInventoryItemPrices() {
        List<WebElement> priceEls = driver.findElements(By.cssSelector(".inventory_item_price"));
        List<Double> prices = new ArrayList<>();
        for (WebElement el : priceEls) {
            String txt = el.getText().replaceAll("[^0-9.]", "");
            prices.add(Double.parseDouble(txt));
        }
        return prices;
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "After login the URL should contain /inventory.html");
        Assertions.assertTrue(driver.findElements(By.id("inventory_container")).size() > 0,
                "Inventory container should be present after successful login");
        resetAppState();
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).sendKeys("wrong@example.com");
        driver.findElement(By.id("password")).sendKeys("badpass");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid credentials");
        logoutIfLoggedIn();
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USER_EMAIL, USER_PASSWORD);
        resetAppState();

        By sortSelect = By.cssSelector("select[data-test='product_sort_container']");
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortSelect));

        // Name (A to Z)
        new Select(dropdown).selectByVisibleText("Name (A to Z)");
        List<String> namesAsc = getInventoryItemNames();
        List<String> sortedAsc = new ArrayList<>(namesAsc);
        Collections.sort(sortedAsc);
        Assertions.assertEquals(sortedAsc, namesAsc, "Items should be sorted alphabetically A‑Z");

        // Name (Z to A)
        new Select(dropdown).selectByVisibleText("Name (Z to A)");
        List<String> namesDesc = getInventoryItemNames();
        List<String> sortedDesc = new ArrayList<>(namesDesc);
        Collections.sort(sortedDesc, Collections.reverseOrder());
        Assertions.assertEquals(sortedDesc, namesDesc, "Items should be sorted alphabetically Z‑A");

        // Price (low to high)
        new Select(dropdown).selectByVisibleText("Price (low to high)");
        List<Double> pricesLowHigh = getInventoryItemPrices();
        List<Double> sortedLowHigh = new ArrayList<>(pricesLowHigh);
        Collections.sort(sortedLowHigh);
        Assertions.assertEquals(sortedLowHigh, pricesLowHigh, "Items should be sorted by price low‑to‑high");

        // Price (high to low)
        new Select(dropdown).selectByVisibleText("Price (high to low)");
        List<Double> pricesHighLow = getInventoryItemPrices();
        List<Double> sortedHighLow = new ArrayList<>(pricesHighLow);
        Collections.sort(sortedHighLow, Collections.reverseOrder());
        Assertions.assertEquals(sortedHighLow, pricesHighLow, "Items should be sorted by price high‑to‑low");

        resetAppState();
        logoutIfLoggedIn();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(USER_EMAIL, USER_PASSWORD);
        resetAppState();

        openBurgerMenu();
        driver.findElement(By.id("inventory_sidebar_link")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should navigate back to inventory page");

        resetAppState();
        logoutIfLoggedIn();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USER_EMAIL, USER_PASSWORD);
        resetAppState();

        openBurgerMenu();
        driver.findElement(By.id("about_sidebar_link")).click();

        // Switch to new window
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        // Verify external domain (example: saucelabs.com)
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open a Saucelabs page");

        driver.close();
        driver.switchTo().window(originalWindow);

        resetAppState();
        logoutIfLoggedIn();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USER_EMAIL, USER_PASSWORD);
        resetAppState();

        openBurgerMenu();
        driver.findElement(By.id("logout_sidebar_link")).click();
        wait.until(ExpectedConditions.urlMatches(".*\\/"));
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL),
                "Logout should return to the login page");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login(USER_EMAIL, USER_PASSWORD);
        // Add an item to cart
        driver.findElement(By.xpath("//button[contains(text(),'Add to cart')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", driver.findElement(By.className("shopping_cart_badge")).getText(),
                "Cart badge should show 1 after adding an item");

        // Reset state
        resetAppState();
        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty(),
                "Cart badge should be cleared after resetting app state");

        logoutIfLoggedIn();
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login(USER_EMAIL, USER_PASSWORD);
        resetAppState();

        // Define expected domains
        Map<String, String> linkDomainMap = Map.of(
                "Twitter", "twitter.com",
                "Facebook", "facebook.com",
                "LinkedIn", "linkedin.com"
        );

        for (Map.Entry<String, String> entry : linkDomainMap.entrySet()) {
            String linkText = entry.getKey();
            String expectedDomain = entry.getValue();

            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
            String originalWindow = driver.getWindowHandle();
            link.click();

            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    linkText + " link should open a page containing " + expectedDomain);

            driver.close();
            driver.switchTo().window(originalWindow);
        }

        resetAppState();
        logoutIfLoggedIn();
    }

    @Test
    @Order(9)
    public void testAddToCartAndCheckout() {
        login(USER_EMAIL, USER_PASSWORD);
        resetAppState();

        // Add first item to cart
        driver.findElement(By.xpath("//button[contains(text(),'Add to cart')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));

        // Go to cart
        driver.findElement(By.id("shopping_cart_container")).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart.html"),
                "Should navigate to cart page");

        // Checkout
        driver.findElement(By.id("checkout")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();

        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement completeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeMsg.getText().trim(),
                "Checkout completion message should be displayed");

        resetAppState();
        logoutIfLoggedIn();
    }
}