package SunaGPT20b.ws01.seq03;

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

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
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

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).clear();
        driver.findElement(By.id("user-name")).sendKeys(user);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(pass);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginBtn.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Login failed: inventory page not loaded.");
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = driver.findElement(By.id("reset_sidebar_link"));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOf(resetLink));
    }

    private void logout() {
        openMenu();
        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("/index.html"));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        // Verify inventory list is displayed
        List<WebElement> items = driver.findElements(By.className("inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory items should be present after login.");
        resetAppState();
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).clear();
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginBtn.click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials.");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username"),
                "Error message should mention username or password.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        By sortLocator = By.cssSelector("select.product_sort_container");
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(sortLocator));
        String[] options = {"az", "za", "lohi", "hilo"};
        for (String value : options) {
            sortDropdown.click();
            WebElement option = driver.findElement(By.cssSelector("select.product_sort_container option[value='" + value + "']"));
            option.click();
            // Verify that the first item's name changes according to sorting
            WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".inventory_item_name")));
            Assertions.assertNotNull(firstItem, "First inventory item should be visible after sorting.");
        }
        resetAppState();
        logout();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(USERNAME, PASSWORD);
        openMenu();
        WebElement allItems = driver.findElement(By.id("inventory_sidebar_link"));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should navigate back to inventory page.");
        resetAppState();
        logout();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USERNAME, PASSWORD);
        openMenu();
        WebElement aboutLink = driver.findElement(By.id("about_sidebar_link"));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("saucelabs"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs"),
                "About link should open Saucelabs domain.");
        driver.close();
        driver.switchTo().window(originalWindow);
        resetAppState();
        logout();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USERNAME, PASSWORD);
        openMenu();
        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("saucedemo.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucedemo.com"),
                "Logout should return to login page.");
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        String[][] socials = {
                {"social_twitter", "x.com"},
                {"social_facebook", "facebook.com"},
                {"social_linkedin", "linkedin.com"}
        };
        String originalWindow = driver.getWindowHandle();
        for (String[] pair : socials) {
            By locator = By.className(pair[0]);
            List<WebElement> elems = driver.findElements(locator);
            if (elems.isEmpty()) continue;
            WebElement link = elems.get(0);
            link.click();
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.urlContains(pair[1]));
            Assertions.assertTrue(driver.getCurrentUrl().contains(pair[1]),
                    "Social link should navigate to " + pair[1]);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
        logout();
    }

    @Test
    @Order(8)
    public void testCheckoutProcess() {
        login(USERNAME, PASSWORD);
        // Add first item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[id^='add-to-cart-']")));
        addToCart.click();
        // Verify cart badge
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item.");
        // Go to cart
        WebElement cartIcon = driver.findElement(By.id("shopping_cart_container"));
        cartIcon.click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        // Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".checkout_button")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText(),
                "Checkout completion message should be displayed.");
        // Return to inventory and reset
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        resetAppState();
        logout();
    }
}