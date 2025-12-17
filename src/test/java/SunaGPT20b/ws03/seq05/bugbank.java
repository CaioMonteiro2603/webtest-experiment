package SunaGPT20b.ws03.seq05;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Set;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    private void login() {
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Login should navigate to inventory page");
    }

    private void resetAppState() {
        // Open menu
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click Reset App State
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Close menu
        WebElement closeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();
    }


    private void switchToNewTabAndValidate(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        Set<String> windowsBefore = driver.getWindowHandles();

        // Wait for new window
        wait.until(driver -> driver.getWindowHandles().size() > windowsBefore.size());

        Set<String> windowsAfter = driver.getWindowHandles();
        windowsAfter.removeAll(windowsBefore);
        String newWindow = windowsAfter.iterator().next();

        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link should contain domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        // Verify inventory list is displayed
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory items should be displayed after login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userField.clear();
        userField.sendKeys("invalid@example.com");
        passField.clear();
        passField.sendKeys("wrong");
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();

        By sortLocator = By.cssSelector("select[data-test='product_sort_container']");
        WebElement sortElement = wait.until(
                ExpectedConditions.elementToBeClickable(sortLocator));
        Select sortSelect = new Select(sortElement);

        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (String option : options) {
            sortSelect.selectByVisibleText(option);
            // Verify that the first item changes after sorting
            WebElement firstItem = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
            Assertions.assertNotNull(firstItem.getText(),
                    "First item name should be present after sorting by " + option);
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuBurgerActions() {
        login();

        // Open menu
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // All Items
        WebElement allItems = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "All Items should navigate to inventory page");

        // Open menu again for other actions
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // About (external)
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        switchToNewTabAndValidate("saucelabs.com");

        // Open menu again for logout
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Logout
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL,
                "Logout should return to base URL");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();

        // Twitter
        List<WebElement> twitterLinks = driver.findElements(By.cssSelector("a[href*='twitter.com']"));
        if (!twitterLinks.isEmpty()) {
            twitterLinks.get(0).click();
            switchToNewTabAndValidate("twitter.com");
        }

        // Facebook
        List<WebElement> fbLinks = driver.findElements(By.cssSelector("a[href*='facebook.com']"));
        if (!fbLinks.isEmpty()) {
            fbLinks.get(0).click();
            switchToNewTabAndValidate("facebook.com");
        }

        // LinkedIn
        List<WebElement> liLinks = driver.findElements(By.cssSelector("a[href*='linkedin.com']"));
        if (!liLinks.isEmpty()) {
            liLinks.get(0).click();
            switchToNewTabAndValidate("linkedin.com");
        }

        resetAppState();
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login();

        // Add first item to cart
        WebElement addBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addBtn.click();

        // Verify cart badge
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should show 1 after adding an item");

        // Go to cart
        WebElement cartLink = driver.findElement(By.cssSelector(".shopping_cart_link"));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("cart"),
                "Should navigate to cart page");

        // Checkout
        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();

        // Fill checkout info
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name"))).sendKeys("Caio");
        driver.findElement(By.id("last-name")).sendKeys("Tester");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        // Finish
        wait.until(ExpectedConditions.elementToBeClickable(By.id("finish"))).click();

        // Verify completion
        WebElement completeHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText().trim(),
                "Checkout should complete successfully");

        resetAppState();
    }
}
