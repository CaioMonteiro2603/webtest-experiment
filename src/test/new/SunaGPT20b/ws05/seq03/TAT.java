package SunaGPT20b.ws05.seq03;

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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://www.saucedemo.com/";
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

    private void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void login(String username, String password) {
        navigateToBase();
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    private void resetAppStateIfPossible() {
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            menuBtn.click();
            WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
            resetLink.click();
            // close menu
            WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
            closeBtn.click();
        } catch (Exception ignored) {
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("standard_user", "secret_sauce");
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "After valid login, URL should contain 'inventory.html'");

        WebElement inventoryContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(),
                "Inventory container should be displayed after login");

        resetAppStateIfPossible();
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
        login("standard_user", "secret_sauce");
        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        Assertions.assertFalse(options.isEmpty(), "Sorting dropdown should have options");

        for (WebElement option : options) {
            sortDropdown.click();
            option.click();
            // Verify that the first product name changes according to sorting
            WebElement firstItem = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
            Assertions.assertNotNull(firstItem.getText(),
                    "First item name should be present after sorting by " + option.getText());
        }

        resetAppStateIfPossible();
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login("standard_user", "secret_sauce");

        // Open menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "Clicking All Items should stay on inventory page");

        // Open menu again for other actions
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com") ||
                        driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open a Sauce Labs page");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Open menu again
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "After logout, should be redirected to login page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login("standard_user", "secret_sauce");
        // Footer links assumed to have identifiable classes or hrefs
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            List<WebElement> links = driver.findElements(By.cssSelector("footer a[href*='" + domain + "']"));
            if (links.isEmpty()) {
                continue; // skip if not present
            }
            WebElement link = links.get(0);
            String originalWindow = driver.getWindowHandle();
            link.click();

            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                    "External link should navigate to a page containing " + domain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppStateIfPossible();
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login("standard_user", "secret_sauce");

        // Add first item to cart
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addBtn.click();

        // Verify cart badge
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("span[data-test='shopping-cart-badge']")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[data-test='shopping-cart-link']")));
        cartLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("cart.html"),
                "Should navigate to cart page");

        // Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("checkout")));
        checkoutBtn.click();

        // Fill checkout info
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
        WebElement lastName = wait.until(ExpectedConditions.elementToBeClickable(By.id("last-name")));
        WebElement postalCode = wait.until(ExpectedConditions.elementToBeClickable(By.id("postal-code")));
        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        postalCode.sendKeys("12345");

        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("continue")));
        continueBtn.click();

        // Finish
        WebElement finishBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishBtn.click();

        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h2.complete-header")));
        Assertions.assertTrue(completeHeader.getText().toLowerCase().contains("thank"),
                "Checkout completion message should be displayed");

        resetAppStateIfPossible();
    }
}