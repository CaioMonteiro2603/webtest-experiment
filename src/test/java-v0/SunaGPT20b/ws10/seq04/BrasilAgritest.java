package SunaGPT20b.ws10.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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

    /* -------------------- Helper Methods -------------------- */

    private static void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        userField.clear();
        userField.sendKeys(user);
        WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passField.clear();
        passField.sendKeys(pass);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginBtn.click();
    }

    private static void loginIfNeeded() {
        if (!isLoggedIn()) {
            login(USERNAME, PASSWORD);
            wait.until(ExpectedConditions.urlContains("/inventory"));
        }
    }

    private static boolean isLoggedIn() {
        return driver.getCurrentUrl().contains("/inventory") ||
               isElementPresent(By.id("react-burger-menu-btn"));
    }

    private static boolean isElementPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    private static WebElement getErrorMessageElement() {
        List<By> selectors = List.of(
                By.cssSelector("[data-test='error']"),
                By.cssSelector(".error-message"),
                By.cssSelector(".error"),
                By.cssSelector(".error-msg")
        );
        for (By sel : selectors) {
            List<WebElement> elems = driver.findElements(sel);
            if (!elems.isEmpty()) {
                return elems.get(0);
            }
        }
        return null;
    }

    /* -------------------- Test Cases -------------------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "After successful login the URL should contain '/inventory'");
        Assertions.assertTrue(isElementPresent(By.id("react-burger-menu-btn")),
                "Menu button should be visible after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        userField.clear();
        userField.sendKeys("invalid@example.com");
        WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passField.clear();
        passField.sendKeys("wrongpassword");
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginBtn.click();

        WebElement error = getErrorMessageElement();
        Assertions.assertNotNull(error, "An error message should be displayed for invalid credentials");
        Assertions.assertTrue(error.isDisplayed(), "Error message element should be visible");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginIfNeeded();

        // Locate the sorting dropdown (common selectors)
        By dropdownLocator = By.cssSelector("select[data-test='product_sort_container'], select#sort, select.product_sort_container");
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(dropdownLocator));
        Select select = new Select(dropdown);
        List<WebElement> options = select.getOptions();
        Assertions.assertFalse(options.isEmpty(), "Sorting dropdown should contain options");

        String previousFirstItem = "";
        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());

            // Wait for the first product name to be visible after sorting
            By firstItemLocator = By.cssSelector(".inventory_item_name, .item_name, .product_name");
            WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItemLocator));
            String currentFirstItem = firstItem.getText();
            Assertions.assertNotNull(currentFirstItem, "First item name should not be null after sorting");

            if (!previousFirstItem.isEmpty()) {
                Assertions.assertNotEquals(previousFirstItem, currentFirstItem,
                        "First item should change after selecting sorting option: " + option.getText());
            }
            previousFirstItem = currentFirstItem;
        }
    }

    @Test
    @Order(4)
    public void testMenuAllItemsAndLogout() {
        loginIfNeeded();

        // Open menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "All Items should navigate to the inventory page");

        // Open menu again for logout
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Logout should redirect back to the login page");
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        loginIfNeeded();

        // Add an item to the cart (generic selector)
        By addToCartLocator = By.cssSelector("button[data-test^='add-to-cart'], button.add-to-cart");
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(addToCartLocator));
        addToCart.click();

        // Verify cart badge shows 1
        By badgeLocator = By.cssSelector(".shopping_cart_badge");
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(badgeLocator));
        Assertions.assertEquals("1", badge.getText(),
                "Cart badge should display '1' after adding an item");

        // Open menu and reset app state
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Verify cart badge is removed
        boolean badgePresent = driver.findElements(badgeLocator).size() > 0;
        Assertions.assertFalse(badgePresent, "Cart badge should be removed after resetting app state");
    }

    @Test
    @Order(6)
    public void testMenuAboutExternalLink() {
        loginIfNeeded();

        // Open menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Wait for new window/tab
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        // Verify URL contains expected domain (generic check)
        wait.until(ExpectedConditions.urlMatches(".*(saucelabs|example)\\.com.*"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com") ||
                        driver.getCurrentUrl().contains("example.com"),
                "About link should open an external domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        loginIfNeeded();

        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        String[] selectors = {
                "a[href*='twitter.com']",
                "a[href*='facebook.com']",
                "a[href*='linkedin.com']"
        };

        for (int i = 0; i < selectors.length; i++) {
            List<WebElement> links = driver.findElements(By.cssSelector(selectors[i]));
            if (links.isEmpty()) {
                continue; // Social link may be optional
            }
            WebElement link = links.get(0);
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Wait for new window/tab
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            wait.until(ExpectedConditions.urlContains(domains[i]));
            Assertions.assertTrue(driver.getCurrentUrl().contains(domains[i]),
                    "Social link should open the expected domain: " + domains[i]);

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}