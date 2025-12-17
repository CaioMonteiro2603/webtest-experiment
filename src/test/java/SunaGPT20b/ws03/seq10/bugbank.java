package SunaGPT20b.ws03.seq10;

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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
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

    /** Helper: perform login if not already logged in */
    private void ensureLoggedIn() {
        driver.get(BASE_URL);
        // Detect login page by presence of login button
        List<WebElement> loginButtons = driver.findElements(By.id("login-button"));
        if (!loginButtons.isEmpty()) {
            WebElement userField = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("user-name")));
            userField.clear();
            userField.sendKeys(USERNAME);

            WebElement passField = driver.findElement(By.id("password"));
            passField.clear();
            passField.sendKeys(PASSWORD);

            WebElement loginBtn = driver.findElement(By.id("login-button"));
            loginBtn.click();

            // Verify successful navigation
            wait.until(ExpectedConditions.urlContains("/inventory"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                    "Login should navigate to inventory page");
        }
    }

    /** Helper: open burger menu */
    private void openMenu() {
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-cross-btn")));
    }

    /** Helper: close burger menu */
    private void closeMenu() {
        WebElement closeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-cross-btn")));
    }

    /** Helper: reset app state via menu */
    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Wait for inventory page to reload
        wait.until(ExpectedConditions.urlContains("/inventory"));
        closeMenu();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        userField.clear();
        userField.sendKeys(USERNAME);

        WebElement passField = driver.findElement(By.id("password"));
        passField.clear();
        passField.sendKeys(PASSWORD);

        WebElement loginBtn = driver.findElement(By.id("login-button"));
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "After valid login, URL should contain '/inventory'");

        // Verify inventory list is displayed
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory items should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        userField.clear();
        userField.sendKeys("invalid@example.com");

        WebElement passField = driver.findElement(By.id("password"));
        passField.clear();
        passField.sendKeys("wrongpass");

        WebElement loginBtn = driver.findElement(By.id("login-button"));
        loginBtn.click();

        // Expect error container
        WebElement errorContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorContainer.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorContainer.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        ensureLoggedIn();

        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
        Select select = new Select(sortDropdown);

        // Name (A to Z)
        select.selectByVisibleText("Name (A to Z)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
        List<WebElement> namesAsc = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(isSorted(namesAsc, true), "Items should be sorted A to Z");

        // Name (Z to A)
        select.selectByVisibleText("Name (Z to A)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
        List<WebElement> namesDesc = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(isSorted(namesDesc, false), "Items should be sorted Z to A");

        // Price (low to high)
        select.selectByVisibleText("Price (low to high)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_price")));
        List<WebElement> pricesLow = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(isPriceSorted(pricesLow, true), "Items should be sorted low to high");

        // Price (high to low)
        select.selectByVisibleText("Price (high to low)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_price")));
        List<WebElement> pricesHigh = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(isPriceSorted(pricesHigh, false), "Items should be sorted high to low");
    }

    private boolean isSorted(List<WebElement> elements, boolean ascending) {
        for (int i = 1; i < elements.size(); i++) {
            String prev = elements.get(i - 1).getText();
            String curr = elements.get(i).getText();
            int cmp = prev.compareToIgnoreCase(curr);
            if (ascending && cmp > 0) return false;
            if (!ascending && cmp < 0) return false;
        }
        return true;
    }

    private boolean isPriceSorted(List<WebElement> elements, boolean lowToHigh) {
        for (int i = 1; i < elements.size(); i++) {
            double prev = parsePrice(elements.get(i - 1).getText());
            double curr = parsePrice(elements.get(i).getText());
            if (lowToHigh && prev > curr) return false;
            if (!lowToHigh && prev < curr) return false;
        }
        return true;
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replaceAll("[^0-9.]", ""));
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        ensureLoggedIn();

        // Open menu and verify items
        openMenu();

        // All Items
        WebElement allItems = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "All Items should navigate to inventory page");
        closeMenu();

        // About (external)
        openMenu();
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new tab
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank"),
                "About link should open a BugBank related external page");
        driver.close();
        driver.switchTo().window(originalWindow);
        closeMenu();

        // Reset App State
        openMenu();
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "Reset App State should return to inventory page");
        closeMenu();

        // Logout
        openMenu();
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to the login page");
    }

    @Test
    @Order(5)
    public void testExternalLinksPolicy() {
        ensureLoggedIn();

        // Footer social links (Twitter, Facebook, LinkedIn)
        String[] expectedDomains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : expectedDomains) {
            List<WebElement> links = driver.findElements(By.cssSelector("footer a[href*='" + domain + "']"));
            Assertions.assertFalse(links.isEmpty(),
                    "Footer should contain a link to " + domain);
            WebElement link = links.get(0);
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Switch to new tab
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                    "External link should open a page containing " + domain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        ensureLoggedIn();

        // Add first item to cart
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']"));
        if (addButtons.isEmpty()) {
            // Fallback: add any first visible add-to-cart button
            addButtons = driver.findElements(By.cssSelector("button[data-test^='add-to-cart']"));
        }
        Assertions.assertFalse(addButtons.isEmpty(), "Add to cart button should be present");
        WebElement addBtn = addButtons.get(0);
        addBtn.click();

        // Verify cart badge
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should show 1 item after adding to cart");

        // Go to cart
        WebElement cartIcon = driver.findElement(By.id("shopping_cart_container"));
        cartIcon.click();
        wait.until(ExpectedConditions.urlContains("/cart"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart"),
                "Should navigate to cart page");

        // Proceed to checkout
        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/checkout-step-one"),
                "Should be on checkout step one");

        // Fill checkout info
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout-step-two"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/checkout-step-two"),
                "Should be on checkout step two");

        // Finish purchase
        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-complete"));
        WebElement completeHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(completeHeader.getText().toLowerCase().contains("thank"),
                "Checkout complete page should contain thank you message");

        // Reset state for other tests
        resetAppState();
    }
}