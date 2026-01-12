package SunaGPT20b.ws03.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String VALID_USER = "caio@gmail.com";
    private static final String VALID_PASS = "123";

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1920, 1080));
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

    private void login(String user, String pass) {
        navigateToBase();
        WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.clear();
        usernameField.sendKeys(user);
        passwordField.clear();
        passwordField.sendKeys(pass);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/inventory"));
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu-wrap")));
    }

    private void closeMenu() {
        WebElement closeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("bm-menu-wrap")));
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = driver.findElement(By.id("reset_sidebar_link"));
        resetLink.click();
        // Ensure the cart badge disappears as a sign of reset
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("shopping_cart_badge")));
        closeMenu();
    }

    @Test
    @Order(1)
    public void testLoginSuccess() {
        login(VALID_USER, VALID_PASS);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "After valid login, URL should contain /inventory");
        WebElement inventoryList = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        Assertions.assertTrue(inventoryList.isDisplayed(),
                "Inventory list should be displayed after successful login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testLoginFailure() {
        navigateToBase();
        WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid@example.com");
        passwordField.sendKeys("wrong");
        loginButton.click();

        By errorLocator = By.cssSelector("[data-test='error']");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(errorLocator));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(VALID_USER, VALID_PASS);
        By sortLocator = By.cssSelector("[data-test='product_sort_container']");
        WebElement sortSelect = wait.until(
                ExpectedConditions.elementToBeClickable(sortLocator));
        Select select = new Select(sortSelect);

        // Capture first item name after each sort to verify ordering
        By firstItemName = By.cssSelector(".inventory_item_name");

        // Option: Name (A to Z)
        select.selectByVisibleText("Name (A to Z)");
        String firstAtoZ = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItemName)).getText();

        // Option: Name (Z to A)
        select.selectByVisibleText("Name (Z to A)");
        String firstZtoA = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItemName)).getText();
        Assertions.assertNotEquals(firstAtoZ, firstZtoA,
                "First item should differ between A‑Z and Z‑A sorting");

        // Option: Price (low to high)
        select.selectByVisibleText("Price (low to high)");
        String firstLow = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItemName)).getText();

        // Option: Price (high to low)
        select.selectByVisibleText("Price (high to low)");
        String firstHigh = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItemName)).getText();
        Assertions.assertNotEquals(firstLow, firstHigh,
                "First item should differ between price low‑high and high‑low sorting");

        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(VALID_USER, VALID_PASS);
        openMenu();
        WebElement allItemsLink = driver.findElement(By.id("inventory_sidebar_link"));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "Clicking All Items should keep user on inventory page");
        closeMenu();
        resetAppState();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(VALID_USER, VALID_PASS);
        openMenu();
        WebElement aboutLink = driver.findElement(By.id("about_sidebar_link"));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new window
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        // Verify external domain (example assumes about page redirects to a known domain)
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("github.com") || currentUrl.contains("netlify.app"),
                "External About link should navigate to an expected domain");

        driver.close();
        driver.switchTo().window(originalWindow);
        closeMenu();
        resetAppState();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(VALID_USER, VALID_PASS);
        openMenu();
        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL),
                "After logout, the user should be returned to the login page");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login(VALID_USER, VALID_PASS);
        // Add an item to cart to create state
        WebElement firstAddToCart = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        firstAddToCart.click();
        By badgeLocator = By.className("shopping_cart_badge");
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(badgeLocator));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 item after adding to cart");

        resetAppState();

        // Verify cart badge is gone
        Assertions.assertTrue(driver.findElements(badgeLocator).isEmpty(),
                "Cart badge should be removed after resetting app state");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login(VALID_USER, VALID_PASS);
        // Scroll to footer if necessary
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        String[] socialIds = {"twitter", "facebook", "linkedin"};
        for (String id : socialIds) {
            By linkLocator = By.cssSelector("a[data-test='" + id + "']");
            if (driver.findElements(linkLocator).isEmpty()) {
                continue; // Skip if not present
            }
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(linkLocator));
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Switch to new window/tab
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            String currentUrl = driver.getCurrentUrl().toLowerCase();
            Assertions.assertTrue(currentUrl.contains(id),
                    "External social link for " + id + " should navigate to a URL containing the domain name");

            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
    }
}