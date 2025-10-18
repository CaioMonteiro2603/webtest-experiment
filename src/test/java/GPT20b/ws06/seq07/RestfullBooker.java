package GPT20b.ws06.seq07;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
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
public class AutomationInTestingTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String USER_EMAIL = "standard_user";
    private static final String USER_PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
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
        throw new NoSuchElementException("Could not find element using locators: " + locators);
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement email = findElementWithFallback(
                List.of(By.id("email"), By.name("email"), By.xpath("//input[@placeholder='Email']")));
        WebElement password = findElementWithFallback(
                List.of(By.id("password"), By.name("password"), By.xpath("//input[@placeholder='Password']")));
        WebElement loginBtn = findElementWithFallback(
                List.of(By.id("loginBtn"), By.xpath("//button[text()='Login']")));

        email.clear();
        email.sendKeys(USER_EMAIL);
        password.clear();
        password.sendKeys(USER_PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Should navigate to inventory page after successful login");
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
                "Logout should return to base URL");
    }

    private List<WebElement> openBurgerMenuAndGetItems() {
        WebElement burgerBtn = findElementWithFallback(
                List.of(By.id("menu-toggle"), By.cssSelector(".burger-menu")));
        wait.until(ExpectedConditions.elementToBeClickable(burgerBtn));
        burgerBtn.click();
        return wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.cssSelector(".menu-list a, .menu-item")));
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
        wait.until(ExpectedConditions.urlContains("inventory"));
    }

    private List<String> getProductNames() {
        List<WebElement> nameEls = driver.findElements(By.cssSelector(".product-title, .item-name"));
        List<String> names = new ArrayList<>();
        for (WebElement el : nameEls) {
            names.add(el.getText());
        }
        return names;
    }

    private List<Double> getProductPrices() {
        List<WebElement> priceEls = driver.findElements(By.cssSelector(".product-price, .item-price"));
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
        List<WebElement> badges = driver.findElements(By.cssSelector(".cart-count, #cartBadge"));
        return badges.isEmpty() ? null : badges.get(0);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testPageLoads() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("automation"),
                "Page title should contain 'automation'");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        login();
        List<WebElement> items = driver.findElements(By.cssSelector(".product-card, .item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory should not be empty after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement email = findElementWithFallback(
                List.of(By.id("email"), By.name("email")));
        WebElement password = findElementWithFallback(
                List.of(By.id("password"), By.name("password")));
        WebElement loginBtn = findElementWithFallback(
                List.of(By.id("loginBtn"), By.xpath("//button[text()='Login']")));

        email.clear();
        email.sendKeys("invalid@example.com");
        password.clear();
        password.sendKeys("wrongpass");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message, .alert-danger")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be visible for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error text should mention invalid login");
    }

    @Test
    @Order(4)
    public void testSortingOptions() {
        login();

        List<WebElement> sortSelectors = driver.findElements(By.cssSelector("select#sort, .sort-dropdown"));
        Assumptions.assumeTrue(!sortSelectors.isEmpty(), "Sorting dropdown not present");

        WebElement sortDropdown = sortSelectors.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
        sortDropdown.click();

        WebElement nameAscOption = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//option[contains(text(),'Name A-Z')]")));
        nameAscOption.click();

        List<String> namesAsc = getProductNames();
        List<String> expectedAsc = new ArrayList<>(namesAsc);
        Collections.sort(expectedAsc);
        Assertions.assertEquals(expectedAsc, namesAsc,
                "Sorting Name A-Z should order product names alphabetically");

        sortDropdown.click();
        WebElement priceLowToHighOption = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//option[contains(text(),'Price low to high')]")));
        priceLowToHighOption.click();

        List<Double> pricesLowHigh = getProductPrices();
        List<Double> expectedLowHigh = new ArrayList<>(pricesLowHigh);
        Collections.sort(expectedLowHigh);
        Assertions.assertEquals(expectedLowHigh, pricesLowHigh,
                "Sorting Price low to high should order prices ascending");
    }

    @Test
    @Order(5)
    public void testBurgerMenuInteraction() {
        login();

        List<WebElement> menuItems = openBurgerMenuAndGetItems();

        // All Items
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("All Items")) {
                wait.until(ExpectedConditions.elementToBeClickable(item));
                item.click();
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "All Items should navigate to inventory page");

        // About
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
                        "About link should open external about page");
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }

        // Reset App State
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("Reset App State")) {
                wait.until(ExpectedConditions.elementToBeClickable(item));
                item.click();
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Reset App State should return to inventory page");

        // Logout
        logout();
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        login();
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a[href^='http']"));
        Assumptions.assumeTrue(!footerLinks.isEmpty(), "No external footer links found");

        String originalHandle = driver.getWindowHandle();
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href == null || !href.startsWith("http")) continue;

            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                    "Opened link URL should contain the href: " + href);
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(7)
    public void testCartAddRemove() {
        login();

        WebElement addBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".add-to-cart, button.addToCart")));
        addBtn.click();

        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-count, #cartBadge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should display count 1");

        WebElement removeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".remove-from-cart, button.removeFromCart")));
        removeBtn.click();

        Assertions.assertTrue(driver.findElements(By.cssSelector(".cart-count, #cartBadge")).isEmpty(),
                "Cart badge should be removed after removing item");
    }

    @Test
    @Order(8)
    public void testCheckoutFlow() {
        login();

        List<WebElement> addButtons = driver.findElements(By.cssSelector(".add-to-cart, button.addToCart"));
        Assertions.assertTrue(addButtons.size() >= 2, "Need at least two items to add");
        addButtons.get(0).click();
        addButtons.get(1).click();

        WebElement cartLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/cart'], .cart-link")));
        cartLink.click();

        wait.until(ExpectedConditions.urlContains("/cart"));

        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkout"), By.cssSelector(".checkout-button")));
        checkoutBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#checkout-form, .checkout-form")));

        driver.findElement(By.id("firstName")).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("postalCode")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout/confirmation"));
        WebElement successMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".checkout-success, h2")));
        Assertions.assertTrue(successMsg.isDisplayed(),
                "Checkout success message should be visible");
    }

    @Test
    @Order(9)
    public void testResetAppStateIndependence() {
        login();

        List<WebElement> menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("Reset App State")) {
                wait.until(ExpectedConditions.elementToBeClickable(item));
                item.click();
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "After reset, should return to inventory page");
    }
}