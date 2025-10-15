package GPT20b.ws01.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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
public class SauceDemoWebTests {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    private static WebDriver driver;
    private static WebDriverWait wait;

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

    /* ---------------------------------------------------------------------- */
    /* Helper methods                                                         */
    /* ---------------------------------------------------------------------- */
    private void navigateToLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
    }

    private void login() {
        navigateToLogin();
        WebElement userField = driver.findElement(By.id("user-name"));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        userField.clear(); userField.sendKeys(USERNAME);
        passField.clear(); passField.sendKeys(PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    private void resetAppState() {
        WebElement burger = driver.findElement(By.id("react-burger-menu-btn"));
        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();
        WebElement reset = driver.findElement(By.id("reset_sidebar_link"));
        wait.until(ExpectedConditions.elementToBeClickable(reset)).click();
        // wait for inventory to reload
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    private void openAndCloseNewTab() {
        String original = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        driver.close();
        driver.switchTo().window(original);
    }

    /* ---------------------------------------------------------------------- */
    /* Tests                                                                  */
    /* ---------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/v1/inventory.html"),
                "Should navigate to inventory page after login");
        WebElement title = driver.findElement(By.className("title"));
        Assertions.assertEquals("PRODUCTS", title.getText(),
                "Inventory page should show 'PRODUCTS' title");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigateToLogin();
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_pass");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.getText().contains("Username and password do not match"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        login();
        List<WebElement> itemsBefore = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertFalse(itemsBefore.isEmpty(), "Inventory items should be present");

        // Sort ascending by name
        WebElement sortSelect = driver.findElement(By.id("product_sort_container"));
        wait.until(ExpectedConditions.elementToBeClickable(sortSelect)).click();
        sortSelect.findElement(By.cssSelector("option[value='az']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortSelect, "Featured (A to Z)"));
        String firstAsc = driver.findElement(By.cssSelector(".inventory_item_name")).getText();

        // Sort descending by name
        sortSelect.findElement(By.cssSelector("option[value='za']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortSelect, "Featured (Z to A)"));
        String firstDesc = driver.findElement(By.cssSelector(".inventory_item_name")).getText();

        // Sort by price low to high
        sortSelect.findElement(By.cssSelector("option[value='lo']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortSelect, "Price (low to high)"));
        String firstLow = driver.findElement(By.cssSelector(".inventory_item_name")).getText();

        // Sort by price high to low
        sortSelect.findElement(By.cssSelector("option[value='hi']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(sortSelect, "Price (high to low)"));
        String firstHigh = driver.findElement(By.cssSelector(".inventory_item_name")).getText();

        Assertions.assertNotEquals(firstAsc, firstDesc, "Name sorting should change order");
        Assertions.assertNotEquals(firstLow, firstHigh, "Price sorting should change order");
    }

    @Test
    @Order(4)
    public void testMenuFunctions() {
        login();

        // Open burger menu
        WebElement burger = driver.findElement(By.id("react-burger-menu-btn"));
        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();

        // Click All Items (should return to inventory)
        WebElement allItems = driver.findElement(By.id("inventory_sidebar_link"));
        wait.until(ExpectedConditions.elementToBeClickable(allItems)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/v1/inventory.html"),
                "All Items should navigate back to inventory page");

        // Re-open menu
        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();

        // Click About (external)
        WebElement about = driver.findElement(By.id("about_sidebar_link"));
        wait.until(ExpectedConditions.elementToBeClickable(about)).click();
        openAndCloseNewTab();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/v1/inventory.html"),
                "After closing About, should return to inventory page");

        // Re-open menu
        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();

        // Click Reset App State
        resetAppState();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/v1/inventory.html"),
                "After reset, should be on inventory page");

        // Re-open menu
        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();

        // Click Logout
        WebElement logout = driver.findElement(By.id("logout_sidebar_link"));
        wait.until(ExpectedConditions.elementToBeClickable(logout)).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/v1/index.html"),
                "Logout should navigate to login page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        String original = driver.getWindowHandle();

        // Twitter link
        WebElement twitter = driver.findElement(By.cssSelector("a[href*='twitter.com']"));
        twitter.click();
        openAndCloseNewTab();
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"),
                "Twitter link should open Twitter domain");

        // Facebook link
        driver.switchTo().window(original);
        WebElement facebook = driver.findElement(By.cssSelector("a[href*='facebook.com']"));
        facebook.click();
        openAndCloseNewTab();
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"),
                "Facebook link should open Facebook domain");

        // LinkedIn link
        driver.switchTo().window(original);
        WebElement linkedin = driver.findElement(By.cssSelector("a[href*='linkedin.com']"));
        linkedin.click();
        openAndCloseNewTab();
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"),
                "LinkedIn link should open LinkedIn domain");
    }

    @Test
    @Order(6)
    public void testAddRemoveCart() {
        login();
        WebElement firstAdd = driver.findElement(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']"));
        wait.until(ExpectedConditions.elementToBeClickable(firstAdd)).click();

        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 after adding item");

        WebElement remove = driver.findElement(By.cssSelector("button[data-test='remove-sauce-labs-backpack']"));
        wait.until(ExpectedConditions.elementToBeClickable(remove)).click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("shopping_cart_badge")));
        Assertions.assertFalse(driver.findElements(By.id("shopping_cart_badge")).size() > 0,
                "Cart badge should disappear after removing item");
    }

    @Test
    @Order(7)
    public void testCheckoutFlow() {
        login();
        WebElement add = driver.findElement(By.cssSelector("button[data-test='add-to-cart-sauce-labs-bike-light']"));
        wait.until(ExpectedConditions.elementToBeClickable(add)).click();

        driver.findElement(By.id("shopping_cart_container")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout")));

        driver.findElement(By.id("checkout")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));

        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("finish")));
        driver.findElement(By.id("finish")).click();

        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText(),
                "Thank you message should be displayed after completing order");

        // Reset state for potential further tests
        resetAppState();
    }
}