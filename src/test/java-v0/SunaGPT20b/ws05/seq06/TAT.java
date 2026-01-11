package SunaGPT20b.ws05.seq06;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String VALID_USERNAME = "standard_user";
    private static final String VALID_PASSWORD = "secret_sauce";

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

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("login-button")));

        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Wait for the menu to close
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("reset_sidebar_link")));
    }

    private void switchToNewWindowAndAssert(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains(expectedDomain),
                "Expected URL to contain domain '" + expectedDomain + "' but was '" + currentUrl + "'");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "After login the URL should contain '/inventory.html'");
        WebElement inventoryContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(),
                "Inventory container should be displayed after successful login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_password");
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username") ||
                        errorMsg.getText().toLowerCase().contains("password"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(VALID_USERNAME, VALID_PASSWORD);
        // Ensure we are on inventory page
        wait.until(ExpectedConditions.urlContains("/inventory.html"));

        By sortSelectLocator = By.id("sort_container");
        Select sortSelect = new Select(wait.until(
                ExpectedConditions.elementToBeClickable(sortSelectLocator)));

        // Helper to fetch item names
        java.util.function.Supplier<List<String>> getItemNames = () -> {
            List<WebElement> nameElements = driver.findElements(By.cssSelector(".inventory_item_name"));
            List<String> names = new ArrayList<>();
            for (WebElement el : nameElements) {
                names.add(el.getText().trim());
            }
            return names;
        };

        // Helper to fetch item prices
        java.util.function.Supplier<List<Double>> getItemPrices = () -> {
            List<WebElement> priceElements = driver.findElements(By.cssSelector(".inventory_item_price"));
            List<Double> prices = new ArrayList<>();
            for (WebElement el : priceElements) {
                String txt = el.getText().replace("$", "").trim();
                prices.add(Double.parseDouble(txt));
            }
            return prices;
        };

        // A to Z
        sortSelect.selectByValue("az");
        List<String> namesAz = getItemNames.get();
        List<String> sortedAz = new ArrayList<>(namesAz);
        Collections.sort(sortedAz);
        Assertions.assertEquals(sortedAz, namesAz,
                "Items should be sorted alphabetically A to Z");

        // Z to A
        sortSelect.selectByValue("za");
        List<String> namesZa = getItemNames.get();
        List<String> sortedZa = new ArrayList<>(namesZa);
        Collections.sort(sortedZa, Collections.reverseOrder());
        Assertions.assertEquals(sortedZa, namesZa,
                "Items should be sorted alphabetically Z to A");

        // Price low to high
        sortSelect.selectByValue("lohi");
        List<Double> pricesLohi = getItemPrices.get();
        List<Double> sortedLohi = new ArrayList<>(pricesLohi);
        Collections.sort(sortedLohi);
        Assertions.assertEquals(sortedLohi, pricesLohi,
                "Items should be sorted by price low to high");

        // Price high to low
        sortSelect.selectByValue("hilo");
        List<Double> pricesHilo = getItemPrices.get();
        List<Double> sortedHilo = new ArrayList<>(pricesHilo);
        Collections.sort(sortedHilo, Collections.reverseOrder());
        Assertions.assertEquals(sortedHilo, pricesHilo,
                "Items should be sorted by price high to low");
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(VALID_USERNAME, VALID_PASSWORD);
        openMenu();
        WebElement allItemsLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Clicking All Items should keep user on inventory page");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(VALID_USERNAME, VALID_PASSWORD);
        openMenu();
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        switchToNewWindowAndAssert("saucelabs.com");
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(VALID_USERNAME, VALID_PASSWORD);
        openMenu();
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "After logout the URL should contain 'index.html'");
        WebElement loginBtn = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        Assertions.assertTrue(loginBtn.isDisplayed(),
                "Login button should be visible after logout");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login(VALID_USERNAME, VALID_PASSWORD);
        // Add first item to cart
        WebElement addToCartBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[id^='add-to-cart-']")));
        addToCartBtn.click();

        // Verify cart badge shows 1
        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(),
                "Cart badge should display count 1 after adding an item");

        // Reset app state
        resetAppState();

        // Verify cart badge is gone
        List<WebElement> badges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(),
                "Cart badge should be removed after resetting app state");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login(VALID_USERNAME, VALID_PASSWORD);
        // Twitter
        WebElement twitterLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        switchToNewWindowAndAssert("twitter.com");

        // Facebook
        WebElement facebookLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        switchToNewWindowAndAssert("facebook.com");

        // LinkedIn
        WebElement linkedInLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedInLink.click();
        switchToNewWindowAndAssert("linkedin.com");
    }
}