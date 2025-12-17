package SunaGPT20b.ws01.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).sendKeys(user);
        driver.findElement(By.id("password")).sendKeys(pass);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    private void openMenu() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-menu-btn"))); // ensure menu opened
    }

    private void resetAppState() {
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link"))).click();
        // menu stays open; close it for cleanliness
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn"))).click();
    }

    private void switchToNewTabAndClose(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link did not navigate to expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Login did not navigate to inventory page.");
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0,
                "Inventory items not displayed after login.");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_pass");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message not displayed for invalid login.");
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL), "URL changed after failed login.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        By sortLocator = By.id("product_sort_container");
        WebElement sort = wait.until(ExpectedConditions.elementToBeClickable(sortLocator));
        sort.click();

        // A-Z
        sort.findElement(By.cssSelector("option[value='az']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_item_name")));
        List<WebElement> namesAz = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(namesAz.get(0).getText().compareTo(namesAz.get(1).getText()) <= 0,
                "A-Z sorting failed.");

        // Z-A
        sort.click();
        sort.findElement(By.cssSelector("option[value='za']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_item_name")));
        List<WebElement> namesZa = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(namesZa.get(0).getText().compareTo(namesZa.get(1).getText()) >= 0,
                "Z-A sorting failed.");

        // Low to High
        sort.click();
        sort.findElement(By.cssSelector("option[value='lohi']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_item_price")));
        List<WebElement> pricesLoHi = driver.findElements(By.className("inventory_item_price"));
        double firstPrice = Double.parseDouble(pricesLoHi.get(0).getText().replace("$", ""));
        double secondPrice = Double.parseDouble(pricesLoHi.get(1).getText().replace("$", ""));
        Assertions.assertTrue(firstPrice <= secondPrice, "Low-to-High sorting failed.");

        // High to Low
        sort.click();
        sort.findElement(By.cssSelector("option[value='hilo']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_item_price")));
        List<WebElement> pricesHiLo = driver.findElements(By.className("inventory_item_price"));
        double firstPriceHi = Double.parseDouble(pricesHiLo.get(0).getText().replace("$", ""));
        double secondPriceHi = Double.parseDouble(pricesHiLo.get(1).getText().replace("$", ""));
        Assertions.assertTrue(firstPriceHi >= secondPriceHi, "High-to-Low sorting failed.");

        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(USERNAME, PASSWORD);
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items menu did not navigate to inventory page.");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USERNAME, PASSWORD);
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link"))).click();
        switchToNewTabAndClose("saucelabs.com");
        resetAppState();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USERNAME, PASSWORD);
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Logout did not return to login page.");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login(USERNAME, PASSWORD);
        // Add an item to cart to create state
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack"))).click();
        Assertions.assertTrue(driver.findElements(By.id("shopping_cart_badge")).size() > 0,
                "Item not added to cart before reset.");
        resetAppState();
        Assertions.assertTrue(driver.findElements(By.id("shopping_cart_badge")).isEmpty(),
                "Reset App State did not clear cart.");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        // Twitter
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_twitter")));
        twitter.click();
        switchToNewTabAndClose("twitter.com");

        // Facebook
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_facebook")));
        facebook.click();
        switchToNewTabAndClose("facebook.com");

        // LinkedIn
        WebElement linkedIn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_linkedin")));
        linkedIn.click();
        switchToNewTabAndClose("linkedin.com");

        resetAppState();
    }

    @Test
    @Order(9)
    public void testAddToCartAndCheckout() {
        login(USERNAME, PASSWORD);
        // Add first item
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack"))).click();
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge count incorrect after adding item.");

        // Go to cart
        driver.findElement(By.id("shopping_cart_container")).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.findElements(By.className("cart_item")).size() > 0,
                "Cart page does not display added items.");

        // Checkout
        driver.findElement(By.id("checkout")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
        WebElement completeHeader = driver.findElement(By.className("complete-header"));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText(),
                "Checkout completion message not as expected.");

        // Return to inventory and reset
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        resetAppState();
    }
}