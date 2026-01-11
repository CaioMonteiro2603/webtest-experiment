package SunaGPT20b.ws06.seq07;

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
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static final String BASE_URL = "https://automationintesting.online";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void resetAppState() {
        // Open burger menu
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click Reset App State
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Close menu (click X)
        WebElement closeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();
    }

    private void login(String username, String password) {
        driver.get(BASE_URL + "/login");
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("standard_user", "secret_sauce");
        // Verify navigation to inventory page
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "After login the URL should contain /inventory");
        // Verify inventory list is displayed
        List<WebElement> items = driver.findElements(By.className("inventory_item"));
        Assertions.assertTrue(items.size() > 0, "Inventory items should be visible after login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_password");
        // Verify error message displayed
        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(error.getText().toLowerCase().contains("username") ||
                        error.getText().toLowerCase().contains("password"),
                "Error message should reference username or password issue");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login("standard_user", "secret_sauce");
        // Ensure we are on inventory page
        wait.until(ExpectedConditions.urlContains("/inventory"));
        WebElement sortContainer = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("product_sort_container")));
        Select select = new Select(sortContainer);
        // Options expected: Name (A to Z), Name (Z to A), Price (low to high), Price (high to low)
        String[] optionValues = {"az", "za", "lohi", "hilo"};
        for (String value : optionValues) {
            select.selectByValue(value);
            // Verify that the first item's name or price reflects sorting
            List<WebElement> items = driver.findElements(By.className("inventory_item"));
            Assertions.assertFalse(items.isEmpty(),
                    "Inventory list should not be empty after sorting");
            // Simple check: ensure first item is displayed
            WebElement firstItem = items.get(0);
            Assertions.assertTrue(firstItem.isDisplayed(),
                    "First inventory item should be displayed after sorting");
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testBurgerMenuNavigation() {
        login("standard_user", "secret_sauce");
        wait.until(ExpectedConditions.urlContains("/inventory"));
        // Open burger menu
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // All Items
        WebElement allItems = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "All Items should navigate back to inventory page");
        // Re-open menu for next actions
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // About (external)
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new tab/window
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("automationintesting"),
                "About link should open a page containing 'automationintesting' in URL");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Re-open menu
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Logout
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Logout should return to login page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login("standard_user", "secret_sauce");
        wait.until(ExpectedConditions.urlContains("/inventory"));
        // Footer links - locate by CSS selector within footer
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a"));
        Assertions.assertFalse(socialLinks.isEmpty(),
                "Footer should contain social media links");
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href == null) {
                continue;
            }
            // Only handle external social domains
            if (href.contains("twitter.com") || href.contains("facebook.com") || href.contains("linkedin.com")) {
                String originalWindow = driver.getWindowHandle();
                // Click link
                link.click();
                // Wait for new window
                wait.until(driver -> driver.getWindowHandles().size() > 1);
                Set<String> windows = driver.getWindowHandles();
                windows.remove(originalWindow);
                String newWindow = windows.iterator().next();
                driver.switchTo().window(newWindow);
                Assertions.assertTrue(driver.getCurrentUrl().contains(
                        href.contains("twitter.com") ? "twitter.com" :
                        href.contains("facebook.com") ? "facebook.com" : "linkedin.com"),
                        "External social link should open correct domain");
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
        resetAppState();
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login("standard_user", "secret_sauce");
        wait.until(ExpectedConditions.urlContains("/inventory"));
        // Add first item to cart
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button.btn_inventory"));
        Assertions.assertFalse(addButtons.isEmpty(),
                "Add to cart buttons should be present");
        WebElement firstAdd = addButtons.get(0);
        firstAdd.click();

        // Verify cart badge shows '1'
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should display count 1 after adding an item");

        // Navigate to cart
        WebElement cartLink = driver.findElement(By.className("shopping_cart_link"));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("/cart"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart"),
                "Should be on cart page");

        // Proceed to checkout
        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one"));
        // Fill checkout info
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout-step-two"));
        // Finish checkout
        WebElement finishBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishBtn.click();

        wait.until(ExpectedConditions.urlContains("/checkout-complete"));
        WebElement completeHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(completeHeader.getText().toLowerCase().contains("thank"),
                "Checkout complete page should contain thank you message");

        // Return to inventory via Back Home
        WebElement backHome = driver.findElement(By.id("back-to-products"));
        backHome.click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
        resetAppState();
    }
}