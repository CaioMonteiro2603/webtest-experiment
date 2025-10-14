package GPT20b.ws01.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
private static final String USERNAME = "standard_user";
private static final String PASSWORD = "secret_sauce";

@TestMethodOrder(OrderAnnotation.class)
public class SaucedemoTests {
    private static WebDriver driver;
    private static WebDriverWait wait;
@BeforeAll
public static void init() {
    FirefoxOptions options = new FirefoxOptions();
    options.addArguments("--headless");
    driver = new FirefoxDriver(options);
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
}
@AfterAll
public static void teardown() {
    if (driver != null) {
        driver.quit();
    }
}
private void navigateToBase() {
    driver.get(BASE_URL);
}
private void login() {
    navigateToBase();
    // wait for login button enabled
    WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
    usernameField.clear();
    usernameField.sendKeys(USERNAME);
    WebElement passwordField = driver.findElement(By.id("password"));
    passwordField.clear();
    passwordField.sendKeys(PASSWORD);
    WebElement loginBtn = driver.findElement(By.id("login-button"));
    wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    // Wait for inventory container
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
}
private void logout() {
    WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();
    WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
    logoutLink.click();
    // verify login page
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
}
private void resetAppState() {
    WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();
    WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
    resetLink.click();
    // wait for inventory
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
}
@Test
@Order(1)
public void testValidLogin() {
    login();
    // Assert on url and presence
    String currentUrl = driver.getCurrentUrl();
    assertTrue(currentUrl.contains("inventory.html") || currentUrl.contains("inventory"),
            "Expected inventory page after login");
    // Ensure inventory items displayed
    List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
    assertFalse(items.isEmpty(), "Inventory items should be displayed");
}
@Test
@Order(2)
public void testInvalidLogin() {
    navigateToBase();
    WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
    usernameField.clear();
    usernameField.sendKeys("invalid_user");
    WebElement passwordField = driver.findElement(By.id("password"));
    passwordField.clear();
    passwordField.sendKeys("wrong_password");
    WebElement loginBtn = driver.findElement(By.id("login-button"));
    wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    // Wait for error message
    WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
    String errorText = errorMsg.getText();
    assertTrue(errorText.contains("Epic sadface"), "Error message should indicate authentication failure");
}
@Test
@Order(3)
public void testSortingOptions() {
    login();
    // Locate sort dropdown
    WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select#product_sort_container")));
    Select select = new Select(sortDropdown);
    List<WebElement> options = select.getOptions();
    for (WebElement option : options) {
        String visibleText = option.getText();
        // Select each
        select.selectByVisibleText(visibleText);
        // Wait for list to update by checking first item changes
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        // Get list of names or prices
        List<String> values = driver.findElements(By.cssSelector(".inventory_item_name")).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());

        if (visibleText.contains("Name")) {
            List<String> sorted = new ArrayList<>(values);
            if (visibleText.contains("(A to Z)")) {
                Collections.sort(sorted, Comparator.naturalOrder());
            } else {
                Collections.sort(sorted, Comparator.reverseOrder());
            }
            assertEquals(sorted, values, "Items should be sorted by name " + visibleText);
        } else if (visibleText.contains("Price")) {
            List<Double> prices = driver.findElements(By.cssSelector(".inventory_item_price")).stream()
                    .map(w -> Double.parseDouble(w.getText().replace("$", "")))
                    .collect(Collectors.toList());
            List<Double> sortedPrices = new ArrayList<>(prices);
            if (visibleText.contains("(low to high)")) {
                Collections.sort(sortedPrices);
            } else {
                Collections.sort(sortedPrices, Collections.reverseOrder());
            }
            assertEquals(sortedPrices, prices, "Items should be sorted by price " + visibleText);
        }
    }

    // Reset to default by selecting the first option
    select.selectByIndex(0);
}
@Test
@Order(4)
public void testBurgerMenuInteractions() {
    login();
    // Open menu
    WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();

    // All Items
    WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
    allItemsLink.click();
    // should stay on inventory page
    assertTrue(driver.getCurrentUrl().contains("inventory.html") || driver.getCurrentUrl().contains("inventory"),
            "Should stay on inventory page after clicking All Items");

    // Back to menu
    menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();

    // About (external)
    WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
    aboutLink.click();
    // new tab opened; switch
    switchToNewTabAndAssertContains("saucelabs.com");
    // close it and switch back
    driver.close();
    switchToLastTab();

    // Back to menu
    menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn.click();

    // Logout
    WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
    logoutLink.click();
    assertTrue(driver.getCurrentUrl().contains("index.html") || driver.getCurrentUrl().contains("login"),
            "Should be returned to login page after logout");

    // Re-login for next steps
    login();

    // Reset App State
    WebElement menuBtn2 = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
    menuBtn2.click();
    WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
    resetLink.click();
    // verify no items in cart
    List<WebElement> cartBadge = driver.findElements(By.cssSelector(".shopping_cart_badge"));
    assertTrue(cartBadge.isEmpty() || !cartBadge.get(0).isDisplayed(), "Cart badge should be hidden after reset");
}
private void switchToNewTabAndAssertContains(String domain) {
    Set<String> handles = driver.getWindowHandles();
    String current = driver.getWindowHandle();
    for (String handle : handles) {
        if (!handle.equals(current)) {
            driver.switchTo().window(handle);
            wait.until(ExpectedConditions.urlContains(domain));
            assertTrue(driver.getCurrentUrl().contains(domain), "New tab should contain domain: " + domain);
            break;
        }
    }
}
private void switchToLastTab() {
    Set<String> handles = driver.getWindowHandles();
    List<String> list = new ArrayList<>(handles);
    driver.switchTo().window(list.get(list.size() - 1));
}
@Test
@Order(5)
public void testFooterSocialLinks() {
    login();
    List<String> socialIds = Arrays.asList("twitter", "facebook", "linkedin");
    Map<String, String> domains = Map.of(
        "twitter", "twitter.com",
        "facebook", "facebook.com",
        "linkedin", "linkedin.com"
    );
    for (String id : socialIds) {
        By selector = By.cssSelector("footer a[href*='" + domains.get(id) + "']");
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(selector));
    }
}