package GPT20b.ws01.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void initDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* --------------------------------------------------------------------- */
    /* Helper Methods                                                         */
    /* --------------------------------------------------------------------- */

    private static void navigateTo(String url) {
        driver.get(url);
    }

    private static void login() {
        navigateTo(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        driver.findElement(By.id("user-name")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item")));
        assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Login should navigate to inventory page");
    }

    private static void logout() {
        // Open burger menu
        WebElement burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burgerBtn.click();
        // Click Logout link
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        assertTrue(driver.getCurrentUrl().contains("/index.html"),
                "Logout should return to login page");
    }

    private static void resetAppState() {
        WebElement burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burgerBtn.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item")));
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        if (!badges.isEmpty()) {
            assertEquals("0", badges.get(0).getText(), "Cart badge should be 0 after reset");
        }
    }

    private static void switchToNewWindow(String expectedDomain) {
        String original = driver.getWindowHandle();
        Set<String> handlesBefore = driver.getWindowHandles();
        // Wait for new window if opened
        wait.until(driver -> driver.getWindowHandles().size() > handlesBefore.size());
        Set<String> handlesAfter = driver.getWindowHandles();
        handlesAfter.removeAll(handlesBefore);
        String newWindow = handlesAfter.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(driver1 -> driver1.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(original);
    }

    private static void openLinkAndVerify(String hrefContains, String domain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='" + hrefContains + "']")));
        String current = driver.getCurrentUrl();
        link.click();
        if (!driver.getCurrentUrl().equals(current)) {
            // Same tab
            wait.until(driver1 -> driver1.getCurrentUrl().contains(domain));
        } else {
            // New tab/window
            switchToNewWindow(domain);
        }
    }

    /* --------------------------------------------------------------------- */
    /* Test Cases                                                             */
    /* --------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testInvalidLogin() {
        navigateTo(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_pass");
        driver.findElement(By.id("login-button")).click();
        String error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h3[data-test='error']"))).getText();
        assertTrue(error.contains("Username") || error.contains("Password"),
                "Error should indicate invalid credentials");
    }

    @Test
    @Order(2)
    public void testValidLoginAndLogout() {
        login();
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        assertFalse(items.isEmpty(), "Inventory should contain items after login");
        logout();
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        login();
        String[] sortValues = {"az", "za", "lo", "hi"};
        String previousFirst = driver.findElement(By.cssSelector(".inventory_item_name")).getText();

        for (String sortVal : sortValues) {
            WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("product_sort_container")));
            sortDropdown.click();
            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//option[@value='" + sortVal + "']")));
            option.click();
            // Wait until first item changes
            wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(
                    By.cssSelector(".inventory_item_name"), previousFirst)));
            String currentFirst = driver.findElement(By.cssSelector(".inventory_item_name")).getText();
            assertNotEquals(previousFirst, currentFirst,
                    "First item should change after sorting by value: " + sortVal);
            previousFirst = currentFirst;
        }
        logout();
    }

    @Test
    @Order(4)
    public void testBurgerMenuOptions() {
        login();
        // All Items
        WebElement burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burgerBtn.click();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item")));
        assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should navigate to inventory page");

        // About (internal page)
        burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burgerBtn.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        assertTrue(driver.getCurrentUrl().contains("/about.html"),
                "About should navigate to about page");

        // Reset App State
        burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burgerBtn.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item")));

        // Logout
        burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        burgerBtn.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        assertTrue(driver.getCurrentUrl().contains("/index.html"),
                "Logout should return to login page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        openLinkAndVerify("twitter.com", "twitter.com");
        openLinkAndVerify("facebook.com", "facebook.com");
        openLinkAndVerify("linkedin.com", "linkedin.com");
        logout();
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login();
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn_inventory.add_to_cart_button")));
        addBtn.click();
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", badge.getText(), "Cart badge should show 1 after adding item");

        WebElement cartIcon = driver.findElement(By.id("shopping_cart_container"));
        cartIcon.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart_item")));

        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        driver.findElement(By.id("finish")).click();

        String thankYou = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".complete-header"))).getText();
        assertEquals("Thank you for your order!", thankYou,
                "Checkout should display completion message");
        logout();
    }

    @Test
    @Order(7)
    public void testResetAppStateFunctionality() {
        login();
        // Add two items
        List<WebElement> addButtons = driver.findElements(By.cssSelector(".btn_inventory.add_to_cart_button"));
        addButtons.get(0).click();
        addButtons.get(1).click();
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".shopping_cart_badge")));
        assertEquals("2", badge.getText(), "Cart badge should show 2 items");

        resetAppState();

        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        if (!badges.isEmpty()) {
            assertEquals("0", badges.get(0).getText(), "Cart badge should be 0 after reset");
        }
        logout();
    }
}