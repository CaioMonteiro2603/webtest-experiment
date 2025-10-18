package GPT20b.ws04.seq07;

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
public class RealWorldDemoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USER_EMAIL = "standard_user@example.com";
    private static final String USER_PASSWORD = "secret_sauce";

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

    /* ---------- Helper methods ---------- */

    private WebElement findElementWithFallback(List<By> locators) {
        for (By locator : locators) {
            try {
                return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            } catch (Exception ignored) {
            }
        }
        throw new NoSuchElementException("Element not found using any of the provided locators: " + locators);
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement emailEl = findElementWithFallback(
                List.of(By.cssSelector("input[type='email']"), By.id("email"), By.name("email")));
        emailEl.clear();
        emailEl.sendKeys(USER_EMAIL);

        WebElement pwEl = findElementWithFallback(
                List.of(By.cssSelector("input[type='password']"), By.id("password"), By.name("password")));
        pwEl.clear();
        pwEl.sendKeys(USER_PASSWORD);

        WebElement loginBtn = findElementWithFallback(
                List.of(By.cssSelector("button[type='submit']"), By.id("login-button")));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"),
                "Expected to land on home page after successful login");
    }

    private void logout() {
        List<WebElement> menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("Logout")) {
                wait.until(ExpectedConditions.elementToBeClickable(item));
                item.click();
                break;
            }
        }
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to the base URL");
    }

    private List<WebElement> openBurgerMenuAndGetItems() {
        WebElement burgerBtn = findElementWithFallback(
                List.of(By.cssSelector(".menu-toggle"), By.id("burger-menu-btn")));
        wait.until(ExpectedConditions.elementToBeClickable(burgerBtn));
        burgerBtn.click();
        return wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.cssSelector(".menu-list a")));
    }

    private void resetAppState() {
        List<WebElement> menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("Reset App State")) {
                wait.until(ExpectedConditions.elementToBeClickable(item));
                item.click();
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("/home"));
    }

    private List<String> getProductNames() {
        List<WebElement> items = driver.findElements(By.cssSelector(".product-card .product-title"));
        return items.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getProductPrices() {
        List<WebElement> priceEls = driver.findElements(By.cssSelector(".product-card .product-price"));
        List<Double> prices = new ArrayList<>();
        for (WebElement el : priceEls) {
            String text = el.getText().replace("$", "").trim();
            try {
                prices.add(Double.parseDouble(text));
            } catch (NumberFormatException ignored) {
            }
        }
        return prices;
    }

    private WebElement getCartBadge() {
        List<WebElement> badges = driver.findElements(By.cssSelector(".cart-count"));
        return badges.isEmpty() ? null : badges.get(0);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        List<WebElement> productCards = driver.findElements(By.cssSelector(".product-card"));
        Assertions.assertFalse(productCards.isEmpty(), "Product list should not be empty after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailEl = findElementWithFallback(
                List.of(By.cssSelector("input[type='email']"), By.id("email")));
        emailEl.clear();
        emailEl.sendKeys("invalid@user.com");

        WebElement pwEl = findElementWithFallback(
                List.of(By.cssSelector("input[type='password']"), By.id("password")));
        pwEl.clear();
        pwEl.sendKeys("bad_password");

        WebElement loginBtn = findElementWithFallback(
                List.of(By.cssSelector("button[type='submit']"), By.id("login-button")));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be visible for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message should mention invalid login");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        List<String> originalNames = getProductNames();

        WebElement sortSelect = findElementWithFallback(
                List.of(By.cssSelector("select#sort"), By.name("sort")));
        wait.until(ExpectedConditions.elementToBeClickable(sortSelect));
        sortSelect.click();

        WebElement nameDescOption = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//option[@value='name_desc']")));
        nameDescOption.click();

        List<String> descNames = getProductNames();
        List<String> expected = new ArrayList<>(originalNames);
        Collections.reverse(expected);
        Assertions.assertEquals(expected, descNames, "Sorting by name descending should reverse order");

        sortSelect.click();
        WebElement priceLowHighOption = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//option[@value='price_low_to_high']")));
        priceLowHighOption.click();

        List<Double> lowHighPrices = getProductPrices();
        List<Double> sortedLowHigh = new ArrayList<>(lowHighPrices);
        Collections.sort(sortedLowHigh);
        Assertions.assertEquals(sortedLowHigh, lowHighPrices,
                "Sorting by price low to high should order prices ascending");

        sortSelect.click();
        WebElement priceHighLowOption = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//option[@value='price_high_to_low']")));
        priceHighLowOption.click();

        List<Double> highLowPrices = getProductPrices();
        List<Double> sortedHighLow = new ArrayList<>(highLowPrices);
        sortedHighLow.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedHighLow, highLowPrices,
                "Sorting by price high to low should order prices descending");
    }

    @Test
    @Order(4)
    public void testBurgerMenuInteractions() {
        login();

        // All Items
        List<WebElement> menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("All Items")) {
                wait.until(ExpectedConditions.elementToBeClickable(item));
                item.click();
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"),
                "All Items should show the home/products page");

        // About (external)
        menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("About")) {
                String originalHandle = driver.getWindowHandle();
                item.click();
                wait.until(d -> d.getWindowHandles().size() > 1);
                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(originalHandle)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
                Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                        "About link should navigate to an external about page");
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }

        // Reset App State
        resetAppState();
        Assertions.assertNull(getCartBadge(), "Cart badge should be cleared after reset");

        // Logout
        logout();
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        login();
        List<String> domains = List.of("twitter.com", "facebook.com", "linkedin.com");
        for (String domain : domains) {
            List<WebElement> links = driver.findElements(
                    By.xpath("//a[contains(@href,'" + domain + "')]"));
            Assertions.assertFalse(links.isEmpty(), "Footer link to " + domain + " should exist");
            WebElement link = links.get(0);
            String originalHandle = driver.getWindowHandle();
            link.click();
            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String h : driver.getWindowHandles()) {
                if (!h.equals(originalHandle)) {
                    driver.switchTo().window(h);
                    break;
                }
            }
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                    "Footer link should navigate to " + domain);
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(6)
    public void testAddRemoveCart() {
        login();
        WebElement firstAddBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button.add-to-cart")));
        firstAddBtn.click();

        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-count")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should display count 1");

        WebElement firstRemoveBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button.remove-from-cart")));
        firstRemoveBtn.click();

        Assertions.assertTrue(driver.findElements(By.cssSelector(".cart-count")).isEmpty(),
                "Cart badge should be removed after removing last item");
    }

    @Test
    @Order(7)
    public void testCheckoutFlow() {
        login();

        List<WebElement> addButtons = driver.findElements(
                By.cssSelector("button.add-to-cart"));
        Assertions.assertTrue(addButtons.size() >= 2, "Expect at least two items to add");
        addButtons.get(0).click();
        addButtons.get(1).click();

        WebElement cartLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/cart']")));
        cartLink.click();

        wait.until(ExpectedConditions.urlContains("/cart"));

        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkout-button")));
        checkoutBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout-form")));

        driver.findElement(By.id("firstName")).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("phone")).sendKeys("5551234567");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout/confirmation"));
        WebElement successMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".checkout-success")));
        Assertions.assertTrue(successMsg.isDisplayed(),
                "Checkout completion message should be visible");
    }

    @Test
    @Order(8)
    public void testResetAppStateIndependence() {
        login();
        resetAppState();
        Assertions.assertNull(getCartBadge(), "Cart badge should be cleared after reset");
    }
}