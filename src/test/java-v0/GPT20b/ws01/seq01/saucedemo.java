package GPT20b.ws01.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Automated test suite for https://www.saucedemo.com/v1/index.html
 * Uses Selenium 4 with Firefox in headless mode and JUnit 5.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
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
        if (driver != null) {
            driver.quit();
        }
    }

    /* ------------------------------ Helpers -------------------------------- */

    private void loginIfNeeded() {
        if (driver.getCurrentUrl().contains("inventory")) {
            return; // already logged in
        }
        driver.get(BASE_URL);
        WebElement userInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        userInput.clear();
        userInput.sendKeys(USERNAME);
        passInput.clear();
        passInput.sendKeys(PASSWORD);
        loginBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item")));
    }

    private void logout() {
        openBurgerMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Logout']")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void resetAppState() {
        openBurgerMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Reset App State']")));
        resetLink.click();
        // Modal appears; wait for it to disappear
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".modal-body")));
    }

    private void openBurgerMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button#react-burger-menu-btn")));
        if (!menuBtn.isDisplayed()) {
            Assertions.fail("Burger menu button not visible");
        }
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[text()='All Items']")));
    }

    private List<String> getItemNames() {
        List<WebElement> elements = driver.findElements(By.cssSelector(".inventory_item_name"));
        List<String> names = new ArrayList<>();
        for (WebElement el : elements) {
            names.add(el.getText());
        }
        return names;
    }

    private List<Double> getItemPrices() {
        List<WebElement> elements = driver.findElements(By.cssSelector(".inventory_item_price"));
        List<Double> prices = new ArrayList<>();
        for (WebElement el : elements) {
            String text = el.getText().replace("$", "").trim();
            try {
                prices.add(Double.parseDouble(text));
            } catch (NumberFormatException e) {
                prices.add(Double.NaN);
            }
        }
        return prices;
    }

    private void clickAddToCartByIndex(int index) {
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button.btn_inventory"));
        if (index < 0 || index >= addButtons.size()) {
            Assertions.fail("Invalid add-to-cart index");
        }
        WebElement btn = addButtons.get(index);
        wait.until(ExpectedConditions.elementToBeClickable(btn));
        btn.click();
    }

    private int getCartBadgeCount() {
        List<WebElement> badges = driver.findElements(By.cssSelector("#shopping_cart_badge"));
        if (badges.isEmpty()) {
            return 0;
        }
        String text = badges.get(0).getText().trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /* ------------------------------ Tests -------------------------------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        loginIfNeeded();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "URL should 'inventory' after login");
        Assertions.assertFalse(getItemNames().isEmpty(),
                "Inventory items should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();
        WebElement userInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

        userInput.clear();
        userInput.sendKeys("invalid_user");
        passInput.clear();
        passInput.sendKeys("invalid_pass");
        loginBtn.click();

        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("username and password do not match"),
                "Expected invalid credentials error message");
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        loginIfNeeded();
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item")));

        // Capture initial order
        List<String> namesInitial = getItemNames();
        List<Double> pricesInitial = getItemPrices();

        String[] options = {
                "Name (A to Z)",
                "Name (Z to A)",
                "Price (low to high)",
                "Price (high to low)"
        };

        for (String opt : options) {
            Select sortSelect = new Select(
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select#inventory_sort_container"))));
            sortSelect.selectByVisibleText(opt);

            // Wait for the list to refresh
            wait.until(driver -> getItemNames().size() == namesInitial.size());

            List<String> names = getItemNames();
            List<Double> prices = getItemPrices();

            switch (opt) {
                case "Name (A to Z)":
                    List<String> expectedAsc = new ArrayList<>(namesInitial);
                    Collections.sort(expectedAsc);
                    Assertions.assertEquals(expectedAsc, names,
                            "Items should be sorted alphabetically A to Z");
                    break;
                case "Name (Z to A)":
                    List<String> expectedDesc = new ArrayList<>(namesInitial);
                    expectedDesc.sort(Collections.reverseOrder());
                    Assertions.assertEquals(expectedDesc, names,
                            "Items should be sorted alphabetically Z to A");
                    break;
                case "Price (low to high)":
                    List<Double> expectedLowHigh = new ArrayList<>(pricesInitial);
                    expectedLowHigh.sort(Double::compareTo);
                    Assertions.assertEquals(expectedLowHigh, prices,
                            "Items should be sorted by price low to high");
                    break;
                case "Price (high to low)":
                    List<Double> expectedHighLow = new ArrayList<>(pricesInitial);
                    expectedHighLow.sort(Collections.reverseOrder());
                    Assertions.assertEquals(expectedHighLow, prices,
                            "Items should be sorted by price high to low");
                    break;
                default:
                    Assertions.fail("Unknown sorting option");
            }
        }
    }

    @Test
    @Order(4)
    public void testBurgerMenuOperations() {
        loginIfNeeded();

        // All Items
        openBurgerMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='All Items']")));
        allItemsLink.click();
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Should navigate back to inventory after All Items link");

        // About (external link)
        openBurgerMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='About']")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        Set<String> handlesAfter = driver.getWindowHandles();
        if (handlesAfter.size() > 1) { // new tab opened
            for (String handle : handlesAfter) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                    Assertions.assertTrue(driver.getCurrentUrl().contains("sauce"),
                            "About page should be served by saucedemo domain");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
        } else {
            wait.until(ExpectedConditions.urlContains("about"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                    "About link should navigate within the same tab");
        }

        // Reset App State
        openBurgerMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[text()='Reset App State']")));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".modal-body")));
        Assertions.assertEquals(0, getCartBadgeCount(),
                "Cart should be empty after reset");

        // Logout
        openBurgerMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Logout']")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Should return to login page after logout");
    }

    @Test
    @Order(5)
    public void testAddToCartAndCheckout() {
        loginIfNeeded();

        // Add first two items
        clickAddToCartByIndex(0);
        clickAddToCartByIndex(1);
        Assertions.assertEquals(2, getCartBadgeCount(),
                "Cart badge should display count 2 after adding two items");

        // Go to cart
        WebElement cartLink = driver.findElement(By.id("shopping_cart_container"));
        cartLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout")));

        // Click checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();

        // Fill checkout details
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");

        // Finish checkout
        WebElement finishBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishBtn.click();

        // Verify receipt
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText(),
                "Order success message should be displayed");

        // Return to inventory
        WebElement backHome = wait.until(ExpectedConditions.elementToBeClickable(By.id("back-to-products")));
        backHome.click();
        resetAppState();
        logout();
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));

        String[] socialSelectors = {
                "//a[contains(@href, 'twitter.com')]",
                "//a[contains(@href, 'facebook.com')]",
                "//a[contains(@href, 'linkedin.com')]"
        };

        String originalWindow = driver.getWindowHandle();

        for (String selector : socialSelectors) {
            List<WebElement> links = driver.findElements(By.xpath(selector));
            if (links.isEmpty()) {
                Assertions.fail("Expected social link with selector " + selector);
            }
            for (WebElement link : links) {
                link.click();
                Set<String> allHandles = driver.getWindowHandles();
                if (allHandles.size() > 1) {
                    for (String handle : allHandles) {
                        if (!handle.equals(originalWindow)) {
                            driver.switchTo().window(handle);
                            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                            Assertions.assertTrue(driver.getCurrentUrl().contains("//"),
                                    "External social link should have a valid URL");
                            driver.close();
                            driver.switchTo().window(originalWindow);
                            break;
                        }
                    }
                } else {
                    Assertions.assertTrue(driver.getCurrentUrl().contains("twitter") ||
                            driver.getCurrentUrl().contains("facebook") ||
                            driver.getCurrentUrl().contains("linkedin"),
                            "Footer link should navigate to expected external domain");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
                }
            }
        }
    }
}