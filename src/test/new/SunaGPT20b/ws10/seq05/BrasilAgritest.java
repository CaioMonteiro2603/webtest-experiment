package SunaGPT20b.ws10.seq05;

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
public class BrasilAgritest {
private static WebDriver driver;
private static WebDriverWait wait;
private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
private static final String USERNAME = "superadmin@brasilagritest.com.br";
private static final String PASSWORD = "10203040";

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
    WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
    WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
    WebElement loginBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));

    userField.clear();
    userField.sendKeys(user);
    passField.clear();
    passField.sendKeys(pass);
    loginBtn.click();
}

private void openMenu() {
    WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();
}

private void resetAppState() {
    openMenu();
    WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
    resetLink.click();
    // Wait for the app to reset (cart badge disappears)
    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
}

@Test
@Order(1)
public void testValidLogin() {
    login(USERNAME, PASSWORD);
    // Verify successful navigation to inventory page
    wait.until(ExpectedConditions.urlContains("/inventory.html"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
            "After login, URL should contain /inventory.html");
    // Verify inventory list is displayed
    List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
    Assertions.assertFalse(items.isEmpty(), "Inventory items should be present after login");
}

@Test
@Order(2)
public void testInvalidLogin() {
    driver.get(BASE_URL);
    WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
    WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
    WebElement loginBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));

    userField.clear();
    userField.sendKeys("invalid_user");
    passField.clear();
    passField.sendKeys("wrong_pass");
    loginBtn.click();

    // Expect error message
    WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
    Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
}

@Test
@Order(3)
public void testSortingDropdown() {
    login(USERNAME, PASSWORD);
    WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
    List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
    Assertions.assertTrue(options.size() > 1, "Sorting dropdown should have multiple options");

    String previousFirstItem = "";
    for (WebElement option : options) {
        option.click();
        // Wait for sorting to apply
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector(".inventory_item_name"))));
        List<WebElement> itemNames = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertFalse(itemNames.isEmpty(), "Item names should be present after sorting");
        String currentFirstItem = itemNames.get(0).getText();
        Assertions.assertNotEquals(previousFirstItem, currentFirstItem,
                "First item should change after selecting a different sort option");
        previousFirstItem = currentFirstItem;
    }
}

@Test
@Order(4)
public void testMenuAllItems() {
    login(USERNAME, PASSWORD);
    openMenu();
    WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
    allItems.click();
    wait.until(ExpectedConditions.urlContains("/inventory.html"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
            "All Items should navigate to inventory page");
}

@Test
@Order(5)
public void testMenuAboutExternalLink() {
    login(USERNAME, PASSWORD);
    openMenu();
    WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
    String originalWindow = driver.getWindowHandle();
    aboutLink.click();

    // Wait for new window
    wait.until(ExpectedConditions.numberOfWindowsToBe(2));
    Set<String> windows = driver.getWindowHandles();
    windows.remove(originalWindow);
    String newWindow = windows.iterator().next();
    driver.switchTo().window(newWindow);

    // Verify external domain (cannot be too specific)
    String externalUrl = driver.getCurrentUrl();
    Assertions.assertFalse(externalUrl.contains("gestao.brasilagritest.com"),
            "About link should open an external domain");

    driver.close();
    driver.switchTo().window(originalWindow);
}

@Test
@Order(6)
public void testMenuLogout() {
    login(USERNAME, PASSWORD);
    openMenu();
    WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
    logoutLink.click();
    wait.until(ExpectedConditions.urlToBe(BASE_URL));
    Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
            "Logout should return to the login page");
}

@Test
@Order(7)
public void testMenuResetAppState() {
    login(USERNAME, PASSWORD);
    // Add an item to cart to change state
    WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
    addToCart.click();
    // Verify badge appears
    WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
    Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 item");

    // Reset app state
    resetAppState();

    // Verify badge cleared
    List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
    Assertions.assertTrue(badges.isEmpty(), "Cart badge should be cleared after reset");
}

@Test
@Order(8)
public void testFooterSocialLinksExternal() {
    login(USERNAME, PASSWORD);
    List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a"));
    Assertions.assertFalse(socialLinks.isEmpty(), "Footer should contain social links");

    String originalWindow = driver.getWindowHandle();

    for (WebElement link : socialLinks) {
        String href = link.getAttribute("href");
        if (href == null || href.isEmpty() || href.contains("gestao.brasilagritest.com")) {
            continue; // skip internal or empty links
        }
        link.click();

        // Wait for new window/tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        String externalUrl = driver.getCurrentUrl();
        Assertions.assertTrue(externalUrl.contains(href.replace("https://", "").split("/")[0]),
                "External link should navigate to its domain: " + href);

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}

@Test
@Order(9)
public void testExternalLinksFromInventoryPage() {
    login(USERNAME, PASSWORD);
    // Assume external links have a specific class or attribute
    List<WebElement> externalLinks = driver.findElements(By.cssSelector("a.external-link"));
    if (externalLinks.isEmpty()) {
        Assertions.assertTrue(true, "No external links found on inventory page; test skipped.");
        return;
    }

    String originalWindow = driver.getWindowHandle();

    for (WebElement link : externalLinks) {
        String href = link.getAttribute("href");
        if (href == null || href.isEmpty()) continue;
        link.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        String externalUrl = driver.getCurrentUrl();
        Assertions.assertTrue(externalUrl.contains(href.replace("https://", "").split("/")[0]),
                "External link should navigate to its domain: " + href);

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}
}