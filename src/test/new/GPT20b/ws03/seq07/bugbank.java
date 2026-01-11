package GPT20b.ws03.seq07;

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
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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
        throw new NoSuchElementException("None of the locators matched: " + locators);
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement emailEl = findElementWithFallback(
                List.of(By.id("email"), By.name("email"), By.id("login-email")));
        emailEl.clear();
        emailEl.sendKeys(USERNAME);

        WebElement pwEl = findElementWithFallback(
                List.of(By.id("password"), By.name("password"), By.id("login-password")));
        pwEl.clear();
        pwEl.sendKeys(PASSWORD);

        WebElement loginBtn = findElementWithFallback(
                List.of(By.id("loginButton"), By.cssSelector("button[type='submit']")));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard") || driver.getCurrentUrl().equals(BASE_URL),
                "Expected to navigate to dashboard or stay on main page after login");
    }

    private void logout() {
        List<WebElement> menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("Logout")) {
                item.click();
                break;
            }
        }
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Expected to return to login page after logout");
    }

    private List<WebElement> openBurgerMenuAndGetItems() {
        WebElement burgerBtn = findElementWithFallback(
                List.of(By.id("burger-menu-btn"), By.cssSelector("#burger-menu-btn")));
        wait.until(ExpectedConditions.elementToBeClickable(burgerBtn));
        burgerBtn.click();
        return wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.cssSelector(".menu-item-list a")));
    }

    private void resetAppState() {
        List<WebElement> menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("Reset App State")) {
                item.click();
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("dashboard"));
    }

    private List<String> getInventoryItemNames() {
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item .item_name"));
        return items.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getInventoryItemPrices() {
        List<WebElement> priceEls = driver.findElements(By.cssSelector(".inventory_item .item_price"));
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
        List<WebElement> badges = driver.findElements(By.id("cartBadge"));
        return badges.isEmpty() ? null : badges.get(0);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory should contain at least one item after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailEl = findElementWithFallback(
                List.of(By.id("email"), By.name("email")));
        emailEl.clear();
        emailEl.sendKeys("invalid@example.com");

        WebElement pwEl = findElementWithFallback(
                List.of(By.id("password"), By.name("password")));
        pwEl.clear();
        pwEl.sendKeys("wrongpass");

        WebElement loginBtn = findElementWithFallback(
                List.of(By.id("loginButton"), By.cssSelector("button[type='submit']")));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message, .alert, .error")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be visible for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid") || errorMsg.getText().toLowerCase().contains("error"),
                "Error message should mention invalid login");
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        login();
        List<String> originalOrder = getInventoryItemNames();

        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("select#sortDropdown, select[aria-label='Sort']")));
        sortDropdown.click();

        WebElement ascOption = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("option[value='za']")));
        ascOption.click();

        List<String> ascNames = getInventoryItemNames();
        List<String> expectedAsc = new ArrayList<>(originalOrder);
        Collections.reverse(expectedAsc);
        Assertions.assertEquals(expectedAsc, ascNames, "Sorting Z→A should reverse item order");

        sortDropdown.click();
        WebElement priceLowHigh = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("option[value='lohi']")));
        priceLowHigh.click();

        List<Double> lowHighPrices = getInventoryItemPrices();
        List<Double> sortedLowHigh = new ArrayList<>(lowHighPrices);
        Collections.sort(sortedLowHigh);
        Assertions.assertEquals(sortedLowHigh, lowHighPrices,
                "Sorting price low→high should order prices ascending");

        sortDropdown.click();
        WebElement priceHighLow = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("option[value='hilo']")));
        priceHighLow.click();

        List<Double> highLowPrices = getInventoryItemPrices();
        List<Double> sortedHighLow = new ArrayList<>(highLowPrices);
        sortedHighLow.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedHighLow, highLowPrices,
                "Sorting price high→low should order prices descending");
    }

    @Test
    @Order(4)
    public void testMenuInteractions() {
        login();

        // All Items: should stay on inventory
        List<WebElement> menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("All Items")) {
                item.click();
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard") || driver.getCurrentUrl().equals(BASE_URL),
                "All Items should keep user on main page");

        // About: external link
        menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("About")) {
                String originalHandle = driver.getWindowHandle();
                item.click();
                wait.until(d -> d.getWindowHandles().size() > 1);
                for (String h : driver.getWindowHandles()) {
                    if (!h.equals(originalHandle)) {
                        driver.switchTo().window(h);
                        break;
                    }
                }
                Assertions.assertTrue(driver.getCurrentUrl().contains("about") || driver.getCurrentUrl().contains("bugbank"),
                        "About page should be an external page");
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }

        // Reset App State
        resetAppState();
        Assertions.assertNull(getCartBadge(), "Cart badge should be absent after reset");

        // Logout
        logout();
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Logout should return to login page");
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
        WebElement firstAdd = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[data-action='add'], .add-to-cart-btn, button[aria-label*='Add']")));
        firstAdd.click();

        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("cartBadge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should display count 1");

        WebElement firstRemove = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button[data-action='remove'], .remove-btn, button[aria-label*='Remove']")));
        firstRemove.click();

        Assertions.assertTrue(driver.findElements(By.id("cartBadge")).isEmpty(),
                "Cart badge should be removed after removing last item");
    }

    @Test
    @Order(7)
    public void testCheckoutFlow() {
        login();

        // Add two items
        List<WebElement> addBtns = driver.findElements(
                By.cssSelector("button[data-action='add'], .add-to-cart-btn, button[aria-label*='Add']"));
        Assertions.assertTrue(addBtns.size() >= 2, "Expect at least two items to add");
        addBtns.get(0).click();
        addBtns.get(1).click();

        // Go to cart
        WebElement cartLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("cartLink")));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart"));

        // Checkout
        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkoutBtn")));
        checkoutBtn.click();

        // Fill user info
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName"))).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("postalCode")).sendKeys("12345");
        driver.findElement(By.id("continueBtn")).click();

        // Finish
        wait.until(ExpectedConditions.urlContains("complete"));
        WebElement successMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".success-message, .complete-message")));
        Assertions.assertTrue(successMsg.isDisplayed(),
                "Checkout completion message should be visible");
    }

    @Test
    @Order(8)
    public void testResetAppStateOnLogin() {
        login();
        resetAppState();
        Assertions.assertNull(getCartBadge(), "Cart badge should be cleared after reset");
    }

    @Test
    @Order(9)
    public void testLogoutFunctionality() {
        login();
        logout();
    }
}