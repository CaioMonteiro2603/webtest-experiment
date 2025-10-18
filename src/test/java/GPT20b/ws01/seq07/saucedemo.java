package GPT20b.ws01.seq07;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoWebTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USER = "standard_user";
    private static final String PASS = "secret_sauce";

    @BeforeAll
    static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ====================== Helper Methods ====================== */

    private void login() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        driver.findElement(By.id("user-name")).sendKeys(USER);
        driver.findElement(By.id("password")).sendKeys(PASS);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "Should navigate to inventory page after login");
    }

    private void logout() {
        openBurgerMenu(2); // clicks 'Logout'
        wait.until(ExpectedConditions.urlContains("index.html"));
    }

    private void openBurgerMenu(int menuIndex) {
        // menuIndex: 0 All Items, 1 About, 2 Logout, 3 Reset, 4 Close
        WebElement burgerBtn = wait.until ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burgerBtn.click();
        List<WebElement> menuItems = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".bm-item-list li a")));
        if (menuIndex < menuItems.size()) {
            menuItems.get(menuIndex).click();
        }
    }

    private void resetAppState() {
        openBurgerMenu(3); // Reset
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    private List<String> getDisplayedProductNames() {
        List<WebElement> nameEls = driver.findElements(By.cssSelector(".inventory_item_name"));
        return nameEls.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getDisplayedProductPrices() {
        List<WebElement> priceEls = driver.findElements(By.cssSelector(".inventory_item_price"));
        List<Double> prices = new ArrayList<>();
        for (WebElement el : priceEls) {
            String text = el.getText().replace("$", "").trim();
            prices.add(Double.parseDouble(text));
        }
        return prices;
    }

    private WebElement getCartBadge() {
        List<WebElement> badges = driver.findElements(By.id("shopping_cart_badge"));
        return badges.isEmpty() ? null : badges.get(0);
    }

    /* ====================== Tests ====================== */

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);
        login();
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory should contain at least one item after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_pass");
        driver.findElement(By.id("login-button")).click();

        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(error.getText().toLowerCase().contains("username and password do not match")
                || error.getText().toLowerCase().contains("wrong username and password"),
                "Error text should indicate bad credentials");
    }

    @Test
    @Order(3)
    void testSortingOptions() {
        login();

        // Capture original order
        List<String> originalNames = getDisplayedProductNames();

        // Name Z to A
        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        WebElement zToAOption = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//option[@value='za']")));
        zToAOption.click();
        List<String> zToA = getDisplayedProductNames();
        List<String> expectedZToA = new ArrayList<>(originalNames);
        Collections.sort(expectedZToA, Collections.reverseOrder());
        Assertions.assertEquals(expectedZToA, zToA, "Sorting Z to A should reverse the name order");

        // Price low to high
        sortDropdown.click();
        WebElement lowHighOption = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//option[@value='lohi']")));
        lowHighOption.click();
        List<Double> lowHigh = getDisplayedProductPrices();
        List<Double> sortedLowHigh = new ArrayList<>(lowHigh);
        Collections.sort(sortedLowHigh);
        Assertions.assertEquals(sortedLowHigh, lowHigh, "Sorting low to high price should order accordingly");

        // Price high to low
        sortDropdown.click();
        WebElement highLowOption = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//option[@value='hilo']")));
        highLowOption.click();
        List<Double> highLow = getDisplayedProductPrices();
        List<Double> sortedHighLow = new ArrayList<>(highLow);
        sortedHighLow.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedHighLow, highLow, "Sorting high to low price should reverse order");
    }

    @Test
    @Order(4)
    void testMenuInteractions() {
        login();

        // All Items should keep us on inventory
        openBurgerMenu(0);
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "All Items should navigate to inventory page");

        // About – external link
        driver.findElement(By.id("react-burger-menu-btn")).click();
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        String originalHandle = driver.getWindowHandle();
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        Assertions.assertFalse(driver.getCurrentUrl().contains("saucedemo.com"),
                "About page should be an external domain");
        driver.close();
        driver.switchTo().window(originalHandle);

        // Reset App State – expect cart to be cleared
        resetAppState();
        Assertions.assertNull(getCartBadge(), "Cart badge should be removed after reset");

        // Logout
        logout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Should return to login page after logout");
    }

    @Test
    @Order(5)
    void testFooterExternalLinks() {
        login();

        List<String> expectedDomains = List.of("twitter.com", "facebook.com", "linkedin.com");
        for (String domain : expectedDomains) {
            WebElement link = driver.findElement(By.cssSelector("a[href*='" + domain + "']"));
            link.click();

            String originalHandle = driver.getWindowHandle();
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                    "Should navigate to expected domain: " + domain);
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(6)
    void testAddRemoveCart() {
        login();

        WebElement firstAddBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[id^='add-to-cart']")));
        firstAddBtn.click();

        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1");

        WebElement firstRemoveBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[id^='remove-from-cart']")));
        firstRemoveBtn.click();

        Assertions.assertEquals(0, driver.findElements(By.id("shopping_cart_badge")).size(),
                "Cart badge should disappear after removing item");
    }

    @Test
    @Order(7)
    void testCheckoutFlow() {
        login();

        // Add two items
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button[id^='add-to-cart']"));
        for (int i = 0; i < 2; i++) {
            addButtons.get(i).click();
        }

        // Go to cart
        driver.findElement(By.id("shopping_cart_container")).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // Proceed to checkout
        driver.findElement(By.id("checkout")).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));

        // Fill checkout details
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();

        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText().trim(),
                "Order confirmation message should be present");

        // Return to inventory for cleanup
        driver.findElement(By.id("back-to-products")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "Should return to inventory after checkout");
    }
}