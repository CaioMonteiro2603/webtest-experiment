package SunaGPT20b.ws08.seq10;

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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("username")));
        usernameField.clear();
        usernameField.sendKeys(username);
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(password);
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("/catalog"));
    }

    private void resetAppState() {
        // Open burger menu and click Reset App State if present
        List<WebElement> menuButtons = driver.findElements(By.id("react-burger-menu-btn"));
        if (!menuButtons.isEmpty()) {
            WebElement menuBtn = menuButtons.get(0);
            menuBtn.click();
            WebElement resetLink = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
            resetLink.click();
            // Wait for the menu to close
            wait.until(ExpectedConditions.invisibilityOf(resetLink));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog"),
                "After login, URL should contain /catalog");
        WebElement inventoryHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.inventory_list")));
        Assertions.assertTrue(inventoryHeader.isDisplayed(),
                "Inventory list should be displayed after successful login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("username")));
        usernameField.clear();
        usernameField.sendKeys("invalidUser");
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys("wrongPass");
        driver.findElement(By.id("login-button")).click();
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertEquals("Invalid username or password.", errorMsg.getText(),
                "Error message text should match expected");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        // Ensure we are on the inventory page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select[data-test='product_sort_container']")));
        WebElement sortDropdown = driver.findElement(By.cssSelector("select[data-test='product_sort_container']"));
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (String option : options) {
            sortDropdown.click();
            WebElement opt = sortDropdown.findElement(By.xpath(String.format(".//option[.='%s']", option)));
            opt.click();
            // Verify that the first item reflects the sorting order
            WebElement firstItem = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.inventory_item_name")));
            Assertions.assertTrue(firstItem.isDisplayed(),
                    "First inventory item should be displayed after sorting by " + option);
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testBurgerMenuNavigation() {
        login(USERNAME, PASSWORD);
        // Open burger menu
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // All Items
        WebElement allItemsLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/catalog"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog"),
                "All Items should navigate to /catalog");

        // Open menu again for About link
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // About (external)
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new window
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"),
                "About link should open a GitHub page");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again for Logout
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Logout
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to base URL");

        // Login again for Reset App State
        login(USERNAME, PASSWORD);
        // Open menu and reset
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Verify that cart badge is cleared
        List<WebElement> cartBadge = driver.findElements(By.cssSelector("span.shopping_cart_badge"));
        Assertions.assertTrue(cartBadge.isEmpty(),
                "Cart badge should be cleared after resetting app state");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        // Scroll to footer
        WebElement footer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("footer")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", footer);

        String[][] links = {
                {"twitter", "twitter.com"},
                {"facebook", "facebook.com"},
                {"linkedin", "linkedin.com"}
        };

        for (String[] linkInfo : links) {
            String id = linkInfo[0];
            String expectedDomain = linkInfo[1];
            List<WebElement> elems = driver.findElements(By.cssSelector("a[data-test='" + id + "']"));
            if (elems.isEmpty()) {
                continue; // Skip if not present
            }
            WebElement link = elems.get(0);
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Wait for new window
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "Social link " + id + " should open a page containing " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login(USERNAME, PASSWORD);
        // Add first item to cart
        WebElement firstAddButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        firstAddButton.click();

        // Verify cart badge shows 1
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span.shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should display count 1 after adding an item");

        // Go to cart
        WebElement cartLink = driver.findElement(By.id("shopping_cart_container"));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("/cart"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart"),
                "Should navigate to cart page");

        // Checkout
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
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText(),
                "Checkout completion message should be displayed");

        // Return to inventory and reset state
        driver.findElement(By.id("back-to-products")).click();
        resetAppState();
    }
}