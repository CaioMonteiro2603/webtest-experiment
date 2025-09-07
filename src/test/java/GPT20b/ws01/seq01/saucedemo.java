package GPT20b.ws01.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class SauceDemoTest {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Helper Methods ---------- */

    private void goToBase() {
        driver.get(BASE_URL);
    }

    private void login(String username, String password) {
        goToBase();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-button")).click();
    }

    private void waitForVisibility(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void waitForClickability(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private void resetAppState() {
        By burgerBtn = By.id("react-burger-menu-btn");
        waitForClickability(burgerBtn);
        driver.findElement(burgerBtn).click();

        By resetButton = By.xpath("//button[contains(text(),'Reset App State')]");
        waitForClickability(resetButton);
        driver.findElement(resetButton).click();

        // Close the menu
        driver.findElement(burgerBtn).click();

        // Verify we are back on inventory page
        waitForVisibility(By.className("inventory_list"));
    }

    private void addFirstItemToCart() {
        By firstAddBtn = By.cssSelector(".btn_inventory");
        waitForClickability(firstAddBtn);
        driver.findElement(firstAddBtn).click();
    }

    private String getCartCount() {
        By badge = By.id("shopping_cart_badge");
        return driver.findElements(badge).stream()
                .findFirst()
                .map(WebElement::getText)
                .orElse("");
    }

    private double getFirstItemPrice() {
        By price = By.cssSelector(".inventory_item_price");
        waitForVisibility(price);
        String priceText = driver.findElement(price).getText(); // e.g., "$29.99"
        return Double.parseDouble(priceText.replace("$", ""));
    }

    private double getSecondItemPrice() {
        By prices = By.cssSelector(".inventory_item_price");
        List<WebElement> priceElements = driver.findElements(prices);
        if (priceElements.size() < 2) {
            return 0.0;
        }
        String priceText = priceElements.get(1).getText();
        return Double.parseDouble(priceText.replace("$", ""));
    }

    private String getFirstItemName() {
        By name = By.cssSelector(".inventory_item_name");
        waitForVisibility(name);
        return driver.findElement(name).getText();
    }

    private String getSecondItemName() {
        By names = By.cssSelector(".inventory_item_name");
        List<WebElement> nameElements = driver.findElements(names);
        if (nameElements.size() < 2) {
            return "";
        }
        return nameElements.get(1).getText();
    }

    private void selectSortOptionByText(String visibleText) {
        By sortDropdown = By.id("product_sort_container");
        waitForClickability(sortDropdown);
        WebElement dropdown = driver.findElement(sortDropdown);
        dropdown.click();
        By option = By.xpath(String.format("//option[normalize-space(text())='%s']", visibleText));
        waitForClickability(option);
        driver.findElement(option).click();
    }

    /* ---------- Test Cases ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login("standard_user", "secret_sauce");
        waitForVisibility(By.className("inventory_list"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/inventory.html") ||
                driver.getCurrentUrl().contains("/index.html") ,
                "The URL should contain the inventory page identifier after successful login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "invalid_pass");
        By errorLocator = By.cssSelector("h3[data-test='error']");
        waitForVisibility(errorLocator);
        String errorText = driver.findElement(errorLocator).getText();
        Assertions.assertTrue(
                errorText.contains("Username and password do not match any user in this service"),
                "Error message should indicate invalid credentials.");
    }

    @Test
    @Order(3)
    public void testLockedUserLogin() {
        login("locked_out_user", "secret_sauce");
        By errorLocator = By.cssSelector("h3[data-test='error']");
        waitForVisibility(errorLocator);
        String errorText = driver.findElement(errorLocator).getText();
        Assertions.assertTrue(
                errorText.contains("Sorry, this user has been locked out."),
                "Error message should indicate locked out user.");
    }

    @Test
    @Order(4)
    public void testSortingOptions() {
        login("standard_user", "secret_sauce");
        waitForVisibility(By.className("inventory_list"));

        // Default A to Z
        String firstName = getFirstItemName();
        String secondName = getSecondItemName();
        Assertions.assertTrue(
                firstName.compareTo(secondName) < 0,
                "Default sorting should be alphabetical ascending.");

        // Price low to high
        selectSortOptionByText("Price (low to high)");
        double firstPriceLow = getFirstItemPrice();
        double secondPriceLow = getSecondItemPrice();
        Assertions.assertTrue(
                firstPriceLow <= secondPriceLow,
                "Price low to high sorting failed.");

        // Price high to low
        selectSortOptionByText("Price (high to low)");
        double firstPriceHigh = getFirstItemPrice();
        double secondPriceHigh = getSecondItemPrice();
        Assertions.assertTrue(
                firstPriceHigh >= secondPriceHigh,
                "Price high to low sorting failed.");

        // Name Z to A
        selectSortOptionByText("Name (Z to A)");
        String firstNameDesc = getFirstItemName();
        String secondNameDesc = getSecondItemName();
        Assertions.assertTrue(
                firstNameDesc.compareTo(secondNameDesc) > 0,
                "Name Z to A sorting failed.");
    }

    @Test
    @Order(5)
    public void testBurgerMenuAllItems() {
        login("standard_user", "secret_sauce");
        By burgerBtn = By.id("react-burger-menu-btn");
        waitForClickability(burgerBtn);
        driver.findElement(burgerBtn).click();

        By allItemsLink = By.id("inventory_sidebar_link");
        waitForClickability(allItemsLink);
        driver.findElement(allItemsLink).click();

        waitForVisibility(By.className("inventory_list"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/inventory.html"),
                "Clicking All Items should navigate to inventory page.");
    }

    @Test
    @Order(6)
    public void testBurgerMenuAboutLink() {
        login("standard_user", "secret_sauce");
        By burgerBtn = By.id("react-burger-menu-btn");
        waitForClickability(burgerBtn);
        driver.findElement(burgerBtn).click();

        By aboutLink = By.id("about_sidebar_link");
        waitForClickability(aboutLink);
        WebElement about = driver.findElement(aboutLink);
        String originalWindow = driver.getWindowHandle();
        about.click();

        // Wait for new window
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        Assertions.assertTrue(
                driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should open Saucelabs domain externally.");
        driver.close();
        driver.switchTo().window(originalWindow);
        // Close the burger menu
        driver.findElement(burgerBtn).click();
    }

    @Test
    @Order(7)
    public void testBurgerMenuLogout() {
        login("standard_user", "secret_sauce");
        By burgerBtn = By.id("react-burger-menu-btn");
        waitForClickability(burgerBtn);
        driver.findElement(burgerBtn).click();

        By logoutLink = By.id("logout_sidebar_link");
        waitForClickability(logoutLink);
        driver.findElement(logoutLink).click();

        // After logout, login button should be present
        waitForVisibility(By.id("login-button"));
        Assertions.assertTrue(
                driver.findElement(By.id("login-button")).isDisplayed(),
                "Login button should be visible after logout.");
    }

    @Test
    @Order(8)
    public void testBurgerMenuResetAppState() {
        login("standard_user", "secret_sauce");
        addFirstItemToCart();
        Assertions.assertEquals(
                "1",
                getCartCount(),
                "Cart badge should show 1 after adding an item.");

        resetAppState();
        Assertions.assertEquals(
                "",
                getCartCount(),
                "Cart badge should be empty after reset app state.");
    }

    @Test
    @Order(9)
    public void testAddRemoveToCartBadge() {
        login("standard_user", "secret_sauce");
        addFirstItemToCart();
        Assertions.assertEquals(
                "1",
                getCartCount(),
                "Cart badge should show 1 after adding an item.");

        // Remove item
        By removeBtn = By.xpath("//button[contains(text(),'Remove')]");
        waitForClickability(removeBtn);
        driver.findElement(removeBtn).click();

        // Wait for badge to disappear
        wait.until(driver1 -> driver1.findElements(By.id("shopping_cart_badge")).size() == 0);
        Assertions.assertEquals(
                "",
                getCartCount(),
                "Cart badge should be empty after removing the item.");
    }

    @Test
    @Order(10)
    public void testCheckoutProcess() {
        login("standard_user", "secret_sauce");
        addFirstItemToCart();
        addFirstItemToCart(); // add second item

        // Go to cart
        By cartIcon = By.id("shopping_cart_container");
        waitForClickability(cartIcon);
        driver.findElement(cartIcon).click();
        waitForVisibility(By.id("checkout"));

        // Checkout
        By checkoutButton = By.id("checkout");
        waitForClickability(checkoutButton);
        driver.findElement(checkoutButton).click();

        // Fill info
        waitForVisibility(By.id("first-name"));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");

        By continueBtn = By.id("continue");
        waitForClickability(continueBtn);
        driver.findElement(continueBtn).click();

        // Finish
        By finishBtn = By.id("finish");
        waitForClickability(finishBtn);
        driver.findElement(finishBtn).click();

        // Verify finish page
        By header = By.className("complete-header");
        waitForVisibility(header);
        String headerText = driver.findElement(header).getText();
        Assertions.assertEquals(
                "THANK YOU FOR YOUR ORDER",
                headerText,
                "Finish page should contain the thank you message.");

        // Navigate back to home (reset)
        resetAppState();
    }

    @Test
    @Order(11)
    public void testFooterSocialLinks() {
        login("standard_user", "secret_sauce");
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            By link = By.cssSelector("a[href*='" + domain + "']");
            List<WebElement> links = driver.findElements(link);
            Assertions.assertFalse(
                    links.isEmpty(),
                    "Footer should contain a link to " + domain);
            WebElement socialLink = links.get(0);
            String originalWindow = driver.getWindowHandle();
            socialLink.click();

            // Wait for new window
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            Assertions.assertTrue(
                    driver.getCurrentUrl().contains(domain),
                    "Social link should open domain " + domain);

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}