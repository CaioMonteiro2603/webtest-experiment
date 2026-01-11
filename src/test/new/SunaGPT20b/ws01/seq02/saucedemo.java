package SunaGPT20b.ws01.seq02;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import java.util.Set;
import java.util.ArrayList;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static final String BASE_URL = "https://www.saucedemo.com/";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).clear();
        driver.findElement(By.id("user-name")).sendKeys(user);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(pass);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    private void logout() {
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlContains("index"));
    }

    private void openMenu() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu")));
    }

    private void resetAppState() {
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link"))).click();
        // Wait for the inventory page to be stable again
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "After login, URL should contain inventory.html");
        WebElement inventory = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventory.isDisplayed(), "Inventory container should be displayed after login");
        // Clean up
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).clear();
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        driver.findElement(By.id("login-button")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(error.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        By sortLocator = By.className("product_sort_container");
        WebElement sortElement = wait.until(ExpectedConditions.elementToBeClickable(sortLocator));
        Select sortSelect = new Select(sortElement);

        // Capture first item name for each option to verify change
        List<String> firstItemTexts = new ArrayList<>();

        for (WebElement option : sortSelect.getOptions()) {
            sortSelect.selectByVisibleText(option.getText());
            // Wait for sorting to apply (first item should be visible)
            WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".inventory_item_name")));
            firstItemTexts.add(firstItem.getText());
        }

        // Ensure that at least one ordering changed
        boolean changed = false;
        for (int i = 1; i < firstItemTexts.size(); i++) {
            if (!firstItemTexts.get(i).equals(firstItemTexts.get(0))) {
                changed = true;
                break;
            }
        }
        Assertions.assertTrue(changed, "Sorting options should change the order of items");

        // Clean up
        logout();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(USERNAME, PASSWORD);
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "Clicking All Items should stay on inventory page");
        logout();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USERNAME, PASSWORD);
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new window
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }

        // Verify external domain
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open a Saucelabs domain");

        // Close external tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        logout();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USERNAME, PASSWORD);
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();
        wait.until(ExpectedConditions.urlContains("index"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index"),
                "After logout, URL should contain index");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login(USERNAME, PASSWORD);
        // Add an item to cart to change state
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']"))).click();
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 after adding an item");

        // Reset state
        resetAppState();

        // Verify cart badge cleared
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should be cleared after resetting app state");
        logout();
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        // Social links selectors
        By[] socialLinkLocators = new By[]{
                By.cssSelector("a[href*='twitter']"),
                By.cssSelector("a[href*='facebook']"),
                By.cssSelector("a[href*='linkedin']")
        };
        String[] expectedDomains = new String[]{
                "twitter",
                "facebook",
                "linkedin"
        };

        String originalWindow = driver.getWindowHandle();

        for (int i = 0; i < socialLinkLocators.length; i++) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(socialLinkLocators[i]));
            link.click();

            // Wait for new window
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String win : windows) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    break;
                }
            }

            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomains[i]),
                    "Social link should open a page containing " + expectedDomains[i]);

            // Close and return
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        logout();
    }

    @Test
    @Order(9)
    public void testAddToCartAndCheckout() {
        login(USERNAME, PASSWORD);
        // Add first two items to cart
        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector("button[data-test^='add-to-cart']")));
        Assertions.assertTrue(addButtons.size() >= 2, "There should be at least two add-to-cart buttons");
        addButtons.get(0).click();
        addButtons.get(1).click();

        // Verify cart badge shows 2
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("2", badge.getText(), "Cart badge should show 2 after adding two items");

        // Go to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container"))).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("cart.html"), "Should navigate to cart page");

        // Checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout"))).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        // Fill checkout info
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // Overview page
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish"))).click();

        // Confirmation
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement thankYou = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(thankYou.getText().toUpperCase().contains("THANK YOU"),
                "Checkout completion page should contain thank you message");

        // Reset state for next tests
        resetAppState();
        logout();
    }
}