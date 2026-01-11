package SunaGPT20b.ws10.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest{

    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String VALID_USER = "superadmin@brasilagritest.com.br";
    private static final String VALID_PASS = "10203040";

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

    @BeforeEach
    public void navigateToBase() {
        driver.get(BASE_URL);
    }

    private void login(String user, String pass) {
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        username.clear();
        username.sendKeys(user);
        password.clear();
        password.sendKeys(pass);
        loginBtn.click();
    }


    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
    }

    private void closeMenuIfOpen() {
        try {
            WebElement closeBtn = driver.findElement(By.id("react-burger-cross-btn"));
            closeBtn.click();
        } catch (NoSuchElementException ignored) {
        }
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-item-reset")));
        resetLink.click();
        closeMenuIfOpen();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(VALID_USER, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("/inventory"));
        List<WebElement> items = driver.findElements(By.className("inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory items should be displayed after successful login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid@example.com", "wrongpass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(VALID_USER, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("/inventory"));
        By sortLocator = By.cssSelector("select[data-test='product_sort_container']");
        WebElement sortElement = wait.until(ExpectedConditions.elementToBeClickable(sortLocator));
        Select sortSelect = new Select(sortElement);

        // Name (A to Z)
        sortSelect.selectByVisibleText("Name (A to Z)");
        WebElement firstItemAtoZ = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
        String firstNameAtoZ = firstItemAtoZ.getText();

        // Name (Z to A)
        sortSelect.selectByVisibleText("Name (Z to A)");
        WebElement firstItemZtoA = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
        String firstNameZtoA = firstItemZtoA.getText();

        Assertions.assertNotEquals(firstNameAtoZ, firstNameZtoA,
                "First item name should change when sorting order is reversed.");

        // Price (low to high)
        sortSelect.selectByVisibleText("Price (low to high)");
        WebElement firstPriceLow = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_price")));
        double priceLow = Double.parseDouble(firstPriceLow.getText().replace("$", ""));

        // Price (high to low)
        sortSelect.selectByVisibleText("Price (high to low)");
        WebElement firstPriceHigh = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_price")));
        double priceHigh = Double.parseDouble(firstPriceHigh.getText().replace("$", ""));

        Assertions.assertTrue(priceLow <= priceHigh,
                "Price after low-to-high sort should be less than or equal to price after high-to-low sort.");
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(VALID_USER, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("/inventory"));
        openMenu();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-item-all-items")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "URL should contain /inventory after selecting All Items.");
        closeMenuIfOpen();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(VALID_USER, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("/inventory"));
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-item-about")));
        String originalWindow = driver.getWindowHandle();
        Set<String> existingWindows = driver.getWindowHandles();

        aboutLink.click();

        // Wait for new window
        wait.until(driver -> driver.getWindowHandles().size() > existingWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(existingWindows);
        String newWindow = newWindows.iterator().next();

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlMatches(".*saucelabs\\.com.*"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "External About link should navigate to a saucelabs domain.");

        driver.close();
        driver.switchTo().window(originalWindow);
        closeMenuIfOpen();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(VALID_USER, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("/inventory"));
        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-item-logout")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "User should be redirected to login page after logout.");
    }

    @Test
    @Order(7)
    public void testResetAppStateClearsCart() {
        login(VALID_USER, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("/inventory"));
        // Add first item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@id,'add-to-cart')]")));
        addToCart.click();

        // Verify cart badge shows 1
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 after adding an item.");

        // Reset app state
        resetAppState();

        // Verify cart badge is gone
        List<WebElement> badges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should be cleared after resetting app state.");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinksExternal() {
        login(VALID_USER, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("/inventory"));
        // Define expected domains for each social link
        List<String[]> links = new ArrayList<>();
        links.add(new String[]{"a[href*='twitter.com']", "twitter.com"});
        links.add(new String[]{"a[href*='facebook.com']", "facebook.com"});
        links.add(new String[]{"a[href*='linkedin.com']", "linkedin.com"});

        String originalWindow = driver.getWindowHandle();

        for (String[] pair : links) {
            By locator = By.cssSelector(pair[0]);
            List<WebElement> elements = driver.findElements(locator);
            if (elements.isEmpty()) {
                continue; // Skip if link not present
            }
            WebElement link = elements.get(0);
            Set<String> beforeHandles = driver.getWindowHandles();
            link.click();

            // Wait for new window/tab
            wait.until(d -> d.getWindowHandles().size() > beforeHandles.size());
            Set<String> afterHandles = driver.getWindowHandles();
            afterHandles.removeAll(beforeHandles);
            String newHandle = afterHandles.iterator().next();

            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(pair[1]));
            Assertions.assertTrue(driver.getCurrentUrl().contains(pair[1]),
                    "Social link should open a page containing domain: " + pair[1]);

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}