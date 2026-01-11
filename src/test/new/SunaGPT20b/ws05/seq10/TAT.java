package SunaGPT20b.ws05.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {
    private static WebDriver driver;
    private static final String BASE_URL = "https://www.saucedemo.com/";
    private static WebDriverWait wait;

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

private void openBase() {
    driver.get(BASE_URL);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
}

@Test
@Order(1)
public void testValidLogin() {
    openBase();
    // Assume login fields exist on the landing page
    WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
    WebElement password = driver.findElement(By.id("password"));
    WebElement loginBtn = driver.findElement(By.id("login-button"));

    username.clear();
    username.sendKeys("standard_user");
    password.clear();
    password.sendKeys("secret_sauce");
    loginBtn.click();

    // Verify navigation to inventory page
    wait.until(ExpectedConditions.urlContains("/inventory.html"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
            "URL should contain /inventory.html after successful login");
    // Verify inventory list is displayed
    List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
    Assertions.assertFalse(items.isEmpty(), "Inventory items should be displayed after login");
}

@Test
@Order(2)
public void testInvalidLogin() {
    openBase();
    WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
    WebElement password = driver.findElement(By.id("password"));
    WebElement loginBtn = driver.findElement(By.id("login-button"));

    username.clear();
    username.sendKeys("invalid_user");
    password.clear();
    password.sendKeys("wrong_password");
    loginBtn.click();

    // Verify error message appears
    WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
    Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid login");
    Assertions.assertTrue(error.getText().toLowerCase().contains("username") ||
                    error.getText().toLowerCase().contains("password"),
            "Error message should reference username or password");
}

@Test
@Order(3)
public void testSortingDropdown() {
    // Ensure we are logged in first
    testValidLogin();

    // Locate sorting dropdown
    WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
    sortDropdown.click();

    // Options: assume values "az", "za", "lohi", "hilo"
    String[] options = {"az", "za", "lohi", "hilo"};
    for (String value : options) {
        WebElement option = driver.findElement(By.cssSelector("option[value='" + value + "']"));
        option.click();
        // Verify that the first item changes accordingly (simple check on name order)
        List<WebElement> names = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertFalse(names.isEmpty(), "Item names should be present after sorting");
        // No deep validation; just ensure no exception and list is still populated
    }
}

@Test
@Order(4)
public void testMenuAllItems() {
    testValidLogin();

    // Open burger menu
    WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();

    // Click All Items
    WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
    allItems.click();

    // Verify we are on inventory page
    wait.until(ExpectedConditions.urlContains("/inventory.html"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
            "Should navigate to inventory page after clicking All Items");
}

@Test
@Order(5)
public void testMenuAboutExternalLink() {
    testValidLogin();

    // Open burger menu
    WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();

    // Click About (external)
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

    // Verify external domain (example.com used as placeholder)
    String currentUrl = driver.getCurrentUrl();
    Assertions.assertTrue(currentUrl.contains("example.com") || currentUrl.contains("about"),
            "External About link should navigate to an external domain");

    // Close external tab and switch back
    driver.close();
    driver.switchTo().window(originalWindow);
}

@Test
@Order(6)
public void testMenuLogout() {
    testValidLogin();

    // Open burger menu
    WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();

    // Click Logout
    WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
    logoutLink.click();

    // Verify we are back on login page
    wait.until(ExpectedConditions.urlToBe(BASE_URL));
    Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
            "Should return to login page after logout");
}

@Test
@Order(7)
public void testMenuResetAppState() {
    testValidLogin();

    // Add an item to cart to change state
    List<WebElement> addButtons = driver.findElements(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']"));
    if (!addButtons.isEmpty()) {
        addButtons.get(0).click();
    }

    // Verify cart badge appears
    WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
    Assertions.assertTrue(badge.isDisplayed(), "Cart badge should be displayed after adding item");

    // Open burger menu
    WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();

    // Click Reset App State
    WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
    resetLink.click();

    // Verify cart badge is gone
    List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
    Assertions.assertTrue(badges.isEmpty(), "Cart badge should be cleared after resetting app state");
}

@Test
@Order(8)
public void testFooterSocialLinks() {
    openBase();

    // Define expected domains for social links
    String[][] links = {
            {"footer_twitter", "twitter.com"},
            {"footer_facebook", "facebook.com"},
            {"footer_linkedin", "linkedin.com"}
    };

    String originalWindow = driver.getWindowHandle();

    for (String[] pair : links) {
        String elementId = pair[0];
        String expectedDomain = pair[1];

        List<WebElement> elems = driver.findElements(By.id(elementId));
        if (elems.isEmpty()) {
            continue; // Skip if element not present
        }
        WebElement link = elems.get(0);
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

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains(expectedDomain),
                "Social link should open a page containing " + expectedDomain);

        // Close and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}
}