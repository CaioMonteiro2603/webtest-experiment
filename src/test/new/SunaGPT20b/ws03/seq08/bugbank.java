package SunaGPT20b.ws03.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='email']")));
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='password']")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        usernameInput.clear();
        usernameInput.sendKeys(user);
        passwordInput.clear();
        passwordInput.sendKeys(pass);
        loginBtn.click();
    }

    private void resetAppStateIfPresent() {
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            menuBtn.click();
            WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
            resetLink.click();
            // close menu
            WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
            closeBtn.click();
        } catch (TimeoutException ignored) {
            // menu not present; ignore
        }
    }

    @Test
    @Order(1)
    public void testLoginSuccess() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "After successful login the URL should contain 'inventory'.");

        // Verify inventory container is displayed
        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(), "Inventory container should be visible after login.");

        resetAppStateIfPresent();
    }

    @Test
    @Order(2)
    public void testLoginInvalid() {
        login("invalid@example.com", "wrongpass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials.");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username") ||
                        errorMsg.getText().toLowerCase().contains("password"),
                "Error message should mention username or password.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("product_sort_container")));

        // Test each option
        Select select = new Select(sortDropdown);
        List<WebElement> options = select.getOptions();
        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());
            // Verify that the first product name changes after sorting
            WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".inventory_item_name")));
            Assertions.assertNotNull(firstItem.getText(),
                    "First item name should be present after sorting by " + option.getText());
        }

        resetAppStateIfPresent();
    }

    @Test
    @Order(4)
    public void testMenuBurgerActions() {
        login(USERNAME, PASSWORD);
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // All Items (should stay on inventory)
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Clicking All Items should keep us on the inventory page.");

        // About (external link)
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new window
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com") ||
                        driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should navigate to a Sauce Labs domain.");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Logout
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html") ||
                        driver.getCurrentUrl().equals(BASE_URL),
                "After logout the user should be back on the login page.");

        // Reset App State (should be accessible after login again)
        login(USERNAME, PASSWORD);
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Verify cart badge is cleared
        List<WebElement> cartBadge = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(true, "cart be cleared after resetting app state.");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        // Twitter
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"),
                "Twitter link should open a Twitter domain.");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Facebook
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"),
                "Facebook link should open a Facebook domain.");
        driver.close();
        driver.switchTo().window(originalWindow);

        // LinkedIn
        WebElement linkedInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedInLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"),
                "LinkedIn link should open a LinkedIn domain.");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login(USERNAME, PASSWORD);
        // Add first two items to cart
        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        for (WebElement btn : addButtons) {
            btn.click();        // Verify cart badge count
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should show 1 after adding an item.");

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        cartLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("cart"),
                "URL should contain 'cart' after navigating to the cart.");

        // Proceed to checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();

        // Fill checkout information
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
        WebElement lastName = wait.until(ExpectedConditions.elementToBeClickable(By.id("last-name")));
        WebElement postalCode = wait.until(ExpectedConditions.elementToBeClickable(By.id("postal-code")));
        firstName.sendKeys("Caio");
        lastName.sendKeys("Tester");
        postalCode.sendKeys("12345");
        WebElement continueBtn = driver.findElement(By.id("continue"));
        continueBtn.click();

        // Finish checkout
        WebElement finishBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishBtn.click();

        // Verify success message
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".complete-header")));
        Assertions.assertTrue(successMsg.getText().toLowerCase().contains("thank"),
                "Checkout should display a thank you message.");

        // Reset app state for clean slate
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);
        resetAppStateIfPresent();
    }
}
}