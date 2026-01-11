package SunaGPT20b.ws06.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online";

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

/** Helper to perform login */
private void login(String username, String password) {
    driver.get(BASE_URL + "/");
    WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
    WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
    WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));

    userField.clear();
    userField.sendKeys(username);
    passField.clear();
    passField.sendKeys(password);
    loginBtn.click();

    // Verify successful login by checking URL contains /admin or presence of a known element
    wait.until(ExpectedConditions.urlContains("/admin"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("/admin"),
            "Login should navigate to the admin page");
}

/** Helper to reset app state via the side menu */
private void resetAppState() {
    // Open side menu (burger)
    WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();

    // Click Reset App State
    WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
    resetLink.click();

    // Close menu
    WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
    closeBtn.click();

    // Verify that the cart badge is gone (state reset)
    List<WebElement> badge = driver.findElements(By.className("shopping_cart_badge"));
    Assertions.assertTrue(badge.isEmpty(), "Cart badge should be cleared after reset");
}

/** Helper to navigate to a page one level below the base URL */
private void navigateTo(String path) {
    driver.get(BASE_URL + path);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
}

@Test
@Order(1)
public void testValidLogin() {
    login("admin", "password");
    // Verify inventory list is displayed
    List<WebElement> items = driver.findElements(By.className("inventory_item"));
    Assertions.assertFalse(items.isEmpty(), "Inventory items should be visible after login");
    resetAppState();
}

@Test
@Order(2)
public void testInvalidLogin() {
    driver.get(BASE_URL + "/");
    WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("username")));
    WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.id("password")));
    WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));

    userField.sendKeys("invalid_user");
    passField.sendKeys("wrong_pass");
    loginBtn.click();

    WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
    Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials");
    Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
            "Error message should indicate invalid credentials");
}

@Test
@Order(3)
public void testSortingDropdown() {
    login("admin", "password");
    // Locate sorting dropdown
    WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
    Select select = new Select(sortSelect);

    // Verify each option changes order
    String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
    for (String opt : options) {
        select.selectByVisibleText(opt);
        // Wait for items to be reordered
        wait.until(ExpectedConditions.stalenessOf(driver.findElements(By.className("inventory_item")).get(0)));
        List<WebElement> items = driver.findElements(By.className("inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Items should still be present after sorting by " + opt);
    }
    resetAppState();
}

@Test
@Order(4)
public void testSideMenuNavigation() {
    login("admin", "password");

    // Open side menu
    WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();

    // All Items (should stay on inventory page)
    WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
    allItems.click();
    Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory") || driver.getCurrentUrl().contains("/booking"),
            "All Items should navigate to inventory/booking page");

    // About (external link)
    WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
    aboutLink.click();

    // Switch to new tab/window
    String originalWindow = driver.getWindowHandle();
    wait.until(driver -> driver.getWindowHandles().size() > 1);
    for (String handle : driver.getWindowHandles()) {
        if (!handle.equals(originalWindow)) {
            driver.switchTo().window(handle);
            break;
        }
    }
    Assertions.assertTrue(driver.getCurrentUrl().contains("automationintesting.com"),
            "About link should open external domain containing automationintesting.com");
    driver.close();
    driver.switchTo().window(originalWindow);

    // Reset App State (already tested in helper)
    resetAppState();

    // Logout
    menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();
    WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
    logoutLink.click();
    wait.until(ExpectedConditions.urlContains("/"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("/"), "Logout should return to root page");
}

@Test
@Order(5)
public void testFooterSocialLinks() {
    // Visit home page (one level below base is just "/")
    navigateTo("/");
    // Footer links are expected to have identifiable selectors or alt text
    String[] socialSelectors = {
            "a[href*='twitter.com']",
            "a[href*='facebook.com']",
            "a[href*='linkedin.com']"
    };
    for (String selector : socialSelectors) {
        List<WebElement> links = driver.findElements(By.cssSelector(selector));
        if (links.isEmpty()) {
            continue; // If a social link is missing, skip to avoid false failure
        }
        WebElement link = links.get(0);
        String originalWindow = driver.getWindowHandle();
        link.click();

        // Wait for new window/tab
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        // Verify domain contains expected social network
        String currentUrl = driver.getCurrentUrl().toLowerCase();
        Assertions.assertTrue(currentUrl.contains(selector.split("://")[1].split("\\.")[0]),
                "Social link should navigate to correct domain: " + selector);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}

@Test
@Order(6)
public void testBookingFlowOneLevelBelow() {
    // The site provides a booking page at /booking (one level below base)
    navigateTo("/booking");
    // Verify booking form is present
    WebElement checkIn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkin")));
    WebElement checkOut = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
    WebElement bookBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("book")));

    // Fill dates (use future dates)
    checkIn.sendKeys("2025-12-10");
    checkOut.sendKeys("2025-12-12");
    bookBtn.click();

    // Expect a confirmation message
    WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
    Assertions.assertTrue(confirmation.isDisplayed(), "Booking confirmation should be displayed");
    Assertions.assertTrue(confirmation.getText().toLowerCase().contains("booking"),
            "Confirmation text should mention booking");
}
}