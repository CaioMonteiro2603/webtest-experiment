package SunaGPT20b.ws03.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void login(String email, String password) {
        navigateToBase();
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginBtn.click();

        // Verify login success by checking inventory container presence
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    private void resetAppState() {
        // Open menu if not already open
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Close menu
        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();

        // Ensure we are back on inventory page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "After login the URL should contain 'inventory.html'");
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0,
                "Inventory items should be displayed after successful login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigateToBase();
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

        emailField.clear();
        emailField.sendKeys("invalid@example.com");
        passwordField.clear();
        passwordField.sendKeys("wrong");
        loginBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USER_EMAIL, USER_PASSWORD);
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sort_container")));
        Select select = new Select(sortSelect);

        // Capture first item name for each sort option
        String[] options = {"az", "za", "lohi", "hilo"};
        String previousFirstItem = "";

        for (String value : options) {
            select.selectByValue(value);
            // Wait for sorting to apply
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("inventory_item_name")));
            List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
            Assertions.assertFalse(items.isEmpty(), "Sorted items list should not be empty");
            String firstItem = items.get(0).getText();
            Assertions.assertNotEquals(previousFirstItem, firstItem,
                    "First item should change when sorting by " + value);
            previousFirstItem = firstItem;
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(USER_EMAIL, USER_PASSWORD);
        // Open menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        // Verify we are on inventory page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "URL should contain 'inventory.html' after selecting All Items");

        // Close menu
        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();
        resetAppState();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USER_EMAIL, USER_PASSWORD);
        // Open menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new window
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }

        // Verify external domain (example: github.io or similar)
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("github.io") || currentUrl.contains("netlify.app"),
                "External About link should navigate to a known external domain");

        // Close external tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);

        // Close menu
        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();
        resetAppState();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USER_EMAIL, USER_PASSWORD);
        // Open menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        // Verify we are back on login page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html") || driver.getCurrentUrl().equals(BASE_URL),
                "After logout the user should be on the login page");
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        login(USER_EMAIL, USER_PASSWORD);
        // Footer social links selectors (example IDs)
        String[] linkIds = {"twitter_link", "facebook_link", "linkedin_link"};
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};

        for (int i = 0; i < linkIds.length; i++) {
            List<WebElement> links = driver.findElements(By.id(linkIds[i]));
            if (links.isEmpty()) {
                continue; // Skip if not present
            }
            WebElement link = links.get(0);
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Switch to new window
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String win : windows) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    break;
                }
            }

            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains(domains[i]),
                    "Social link should navigate to " + domains[i]);

            // Close external tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
    }

    @Test
    @Order(8)
    public void testCheckoutProcess() {
        login(USER_EMAIL, USER_PASSWORD);
        // Add first item to cart
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']"));
        if (addButtons.isEmpty()) {
            addButtons = driver.findElements(By.cssSelector("button[id^='add-to-cart']"));
        }
        Assertions.assertFalse(addButtons.isEmpty(), "Add to cart button should be present");
        addButtons.get(0).click();

        // Verify cart badge
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.shopping_cart_link span.shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.className("shopping_cart_link")));
        cartLink.click();

        // Verify cart page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart_contents_container")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("cart.html"), "Should be on cart page");

        // Click Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();

        // Fill checkout info
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name"))).sendKeys("Caio");
        driver.findElement(By.id("last-name")).sendKeys("Tester");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // Finish checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish"))).click();

        // Verify completion message
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.complete-header")));
        Assertions.assertTrue(completeHeader.getText().toLowerCase().contains("thank"),
                "Checkout completion message should be displayed");

        // Return to inventory
        WebElement backHome = wait.until(ExpectedConditions.elementToBeClickable(By.id("back-to-products")));
        backHome.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));

        resetAppState();
    }
}