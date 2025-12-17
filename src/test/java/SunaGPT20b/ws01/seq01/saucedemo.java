package SunaGPT20b.ws01.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo{

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).clear();
        driver.findElement(By.id("user-name")).sendKeys(user);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(pass);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginBtn.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    private void logout() {
        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-menu-btn"))); // menu opened
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Wait for the menu to close automatically
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("reset_sidebar_link")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "After login URL should contain /inventory.html");
        WebElement inventoryContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(), "Inventory container should be displayed");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).clear();
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginBtn.click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        WebElement sortContainer = wait.until(ExpectedConditions.elementToBeClickable(By.id("product_sort_container")));
        Select sortSelect = new Select(sortContainer);

        // A to Z
        sortSelect.selectByVisibleText("Name (A to Z)");
        List<WebElement> itemsAtoZ = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(itemsAtoZ.get(0).getText().compareTo(itemsAtoZ.get(1).getText()) <= 0,
                "Items should be sorted A to Z");

        // Z to A
        sortSelect.selectByVisibleText("Name (Z to A)");
        List<WebElement> itemsZtoA = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(itemsZtoA.get(0).getText().compareTo(itemsZtoA.get(1).getText()) >= 0,
                "Items should be sorted Z to A");

        // Price low to high
        sortSelect.selectByVisibleText("Price (low to high)");
        List<WebElement> pricesLowHigh = driver.findElements(By.cssSelector(".inventory_item_price"));
        double firstLow = Double.parseDouble(pricesLowHigh.get(0).getText().replace("$", ""));
        double secondLow = Double.parseDouble(pricesLowHigh.get(1).getText().replace("$", ""));
        Assertions.assertTrue(firstLow <= secondLow, "Prices should be low to high");

        // Price high to low
        sortSelect.selectByVisibleText("Price (high to low)");
        List<WebElement> pricesHighLow = driver.findElements(By.cssSelector(".inventory_item_price"));
        double firstHigh = Double.parseDouble(pricesHighLow.get(0).getText().replace("$", ""));
        double secondHigh = Double.parseDouble(pricesHighLow.get(1).getText().replace("$", ""));
        Assertions.assertTrue(firstHigh >= secondHigh, "Prices should be high to low");

        resetAppState();
        logout();
    }

    @Test
    @Order(4)
    public void testMenuBurgerAndReset() {
        login(USERNAME, PASSWORD);
        openMenu();

        // Verify All Items link returns to inventory
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should navigate to inventory page");

        // Verify About link opens external page
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open Saucelabs domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Verify Logout works
        openMenu();
        logout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/index.html"),
                "After logout should be back at login page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        // Scroll to footer if needed
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        String[][] links = {
                {"twitter", "twitter.com"},
                {"facebook", "facebook.com"},
                {"linkedin", "linkedin.com"}
        };

        for (String[] pair : links) {
            String id = "footer_" + pair[0];
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
            String originalWindow = driver.getWindowHandle();
            link.click();

            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            Assertions.assertTrue(driver.getCurrentUrl().contains(pair[1]),
                    "Social link should open domain containing " + pair[1]);
            driver.close();
            driver.switchTo().window(originalWindow);
        }

        resetAppState();
        logout();
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login(USERNAME, PASSWORD);
        // Add first item to cart
        WebElement firstAddBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        firstAddBtn.click();

        // Verify cart badge shows 1
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should display 1 item");

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart.html"), "Should be on cart page");

        // Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));

        // Fill checkout info
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("continue")));
        continueBtn.click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));

        // Finish
        WebElement finishBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishBtn.click();
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement thankYou = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(thankYou.getText().toUpperCase().contains("THANK YOU"),
                "Checkout completion message should contain 'THANK YOU'");

        // Reset state
        resetAppState();
        logout();
    }
}