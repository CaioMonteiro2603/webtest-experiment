package SunaQwen3.ws06.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String LOGIN_PAGE_URL = BASE_URL + "login";
    private static final String INVENTORY_PAGE_URL = BASE_URL + "inventory";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

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

    @Test
    @Order(1)
    public void testLoginPageLoadsSuccessfully() {
        driver.get(LOGIN_PAGE_URL);
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("login"), "URL should contain 'login' path after navigating to login page");

        WebElement usernameField = wait.until(visibilityOfElementLocated(By.name("username")));
        Assertions.assertTrue(usernameField.isDisplayed(), "Username field should be visible on login page");

        WebElement passwordField = driver.findElement(By.name("password"));
        Assertions.assertTrue(passwordField.isDisplayed(), "Password field should be visible on login page");

        WebElement loginButton = driver.findElement(By.id("login"));
        Assertions.assertTrue(loginButton.isDisplayed(), "Login button should be visible on login page");
    }

    @Test
    @Order(2)
    public void testValidLoginRedirectsToInventory() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys(USERNAME);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.id("login"));
        loginButton.click();

        wait.until(urlContains("inventory"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("inventory"), "After valid login, URL should contain 'inventory'");

        WebElement inventoryContainer = wait.until(visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(), "Inventory container should be visible after login");
    }

    @Test
    @Order(3)
    public void testInvalidLoginShowsErrorMessage() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys("invalid_user");

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("wrong_password");

        WebElement loginButton = driver.findElement(By.id("login"));
        loginButton.click();

        WebElement errorElement = wait.until(visibilityOfElementLocated(By.cssSelector(".error-message")));
        Assertions.assertTrue(errorElement.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    public void testMenuBurgerButtonFunctionality() {
        navigateToInventoryPage();

        WebElement menuButton = wait.until(elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement allItemsLink = wait.until(visibilityOfElementLocated(By.id("inventory_sidebar_link")));
        Assertions.assertTrue(allItemsLink.isDisplayed(), "All Items link should be visible in menu");

        WebElement aboutLink = driver.findElement(By.id("about_sidebar_link"));
        Assertions.assertTrue(aboutLink.isDisplayed(), "About link should be visible in menu");

        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        Assertions.assertTrue(logoutLink.isDisplayed(), "Logout link should be visible in menu");

        WebElement resetAppStateLink = driver.findElement(By.id("reset_sidebar_link"));
        Assertions.assertTrue(resetAppStateLink.isDisplayed(), "Reset App State link should be visible in menu");

        // Close menu
        WebElement closeMenuButton = wait.until(elementToBeClickable(By.cssSelector("button.close-menu")));
        closeMenuButton.click();

        wait.until(invisibilityOfElementLocated(By.id("react-burger-cross-btn")));
    }

    @Test
    @Order(5)
    public void testMenuAllItemsRedirectsToInventory() {
        navigateToInventoryPage();

        WebElement menuButton = wait.until(elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement allItemsLink = wait.until(elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        wait.until(urlContains("inventory"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("inventory"), "Clicking All Items should navigate to inventory page");
    }

    @Test
    @Order(6)
    public void testMenuAboutOpensExternalPageInNewTab() {
        navigateToInventoryPage();

        WebElement menuButton = wait.until(elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        String originalWindow = driver.getWindowHandle();

        WebElement aboutLink = wait.until(elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        wait.until(numberOfWindowsToBe(2));
        Set<String> afterTabs = driver.getWindowHandles();
        afterTabs.remove(originalWindow);
        String newTab = afterTabs.iterator().next();
        driver.switchTo().window(newTab);

        String aboutUrl = driver.getCurrentUrl();
        Assertions.assertTrue(aboutUrl.contains("saucelabs.com"), "About link should open a page containing 'saucelabs.com'");

        driver.close();
        driver.switchTo().window(originalWindow);

        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Driver should be back on original window after closing new tab");
    }

    @Test
    @Order(7)
    public void testMenuLogoutRedirectsToLoginPage() {
        navigateToInventoryPage();

        WebElement menuButton = wait.until(elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement logoutLink = wait.until(elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(urlContains("login"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("login"), "After logout, URL should contain 'login'");
    }

    @Test
    @Order(8)
    public void testMenuResetAppStateClearsCart() {
        navigateToInventoryPage();

        // Add an item to cart first
        WebElement addToCartButton = wait.until(elementToBeClickable(By.cssSelector("button.btn_inventory")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item after adding");

        // Open menu and reset app state
        WebElement menuButton = wait.until(elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement resetAppStateLink = wait.until(elementToBeClickable(By.id("reset_sidebar_link")));
        resetAppStateLink.click();

        // Wait for menu to close
        wait.until(invisibilityOfElementLocated(By.id("react-burger-cross-btn")));

        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(0, cartBadges.size(), "Cart badge should not be present after reset app state");
    }

    @Test
    @Order(9)
    public void testFooterTwitterLinkOpensInNewTab() {
        navigateToInventoryPage();

        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();

        wait.until(numberOfWindowsToBe(2));
        Set<String> afterTabs = driver.getWindowHandles();
        afterTabs.remove(originalWindow);
        String newTab = afterTabs.iterator().next();
        driver.switchTo().window(newTab);

        String twitterUrl = driver.getCurrentUrl();
        Assertions.assertTrue(twitterUrl.contains("twitter.com"), "Twitter link should open a page containing 'twitter.com'");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testFooterFacebookLinkOpensInNewTab() {
        navigateToInventoryPage();

        String originalWindow = driver.getWindowHandle();

        WebElement facebookLink = wait.until(elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();

        wait.until(numberOfWindowsToBe(2));
        Set<String> afterTabs = driver.getWindowHandles();
        afterTabs.remove(originalWindow);
        String newTab = afterTabs.iterator().next();
        driver.switchTo().window(newTab);

        String facebookUrl = driver.getCurrentUrl();
        Assertions.assertTrue(facebookUrl.contains("facebook.com"), "Facebook link should open a page containing 'facebook.com'");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(11)
    public void testFooterLinkedInLinkOpensInNewTab() {
        navigateToInventoryPage();

        String originalWindow = driver.getWindowHandle();

        WebElement linkedinLink = wait.until(elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedinLink.click();

        wait.until(numberOfWindowsToBe(2));
        Set<String> afterTabs = driver.getWindowHandles();
        afterTabs.remove(originalWindow);
        String newTab = afterTabs.iterator().next();
        driver.switchTo().window(newTab);

        String linkedinUrl = driver.getCurrentUrl();
        Assertions.assertTrue(linkedinUrl.contains("linkedin.com"), "LinkedIn link should open a page containing 'linkedin.com'");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(12)
    public void testSortingDropdownOptions() {
        navigateToInventoryPage();

        WebElement sortDropdown = wait.until(elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();

        // Test Name (A to Z)
        sortDropdown.sendKeys("az");
        wait.until(textToBePresentInElementValue(By.cssSelector(".product_sort_container"), "az"));

        List<WebElement> items = wait.until(visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        String firstItemText = items.get(0).getText();
        Assertions.assertTrue(firstItemText.compareTo(items.get(1).getText()) <= 0,
                "Items should be sorted A to Z by name");

        // Test Name (Z to A)
        sortDropdown = wait.until(elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        sortDropdown.sendKeys("za");
        wait.until(textToBePresentInElementValue(By.cssSelector(".product_sort_container"), "za"));

        items = wait.until(visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        firstItemText = items.get(0).getText();
        Assertions.assertTrue(firstItemText.compareTo(items.get(1).getText()) >= 0,
                "Items should be sorted Z to A by name");

        // Test Price (low to high)
        sortDropdown = wait.until(elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        sortDropdown.sendKeys("lohi");
        wait.until(textToBePresentInElementValue(By.cssSelector(".product_sort_container"), "lohi"));

        List<WebElement> prices = wait.until(visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        double firstPrice = parsePrice(prices.get(0).getText());
        double secondPrice = parsePrice(prices.get(1).getText());
        Assertions.assertTrue(firstPrice <= secondPrice,
                "Items should be sorted low to high by price");

        // Test Price (high to low)
        sortDropdown = wait.until(elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        sortDropdown.sendKeys("hilo");
        wait.until(textToBePresentInElementValue(By.cssSelector(".product_sort_container"), "hilo"));

        prices = wait.until(visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        firstPrice = parsePrice(prices.get(0).getText());
        secondPrice = parsePrice(prices.get(1).getText());
        Assertions.assertTrue(firstPrice >= secondPrice,
                "Items should be sorted high to low by price");
    }

    private void navigateToInventoryPage() {
        driver.get(INVENTORY_PAGE_URL);
        if (!driver.getCurrentUrl().contains("inventory")) {
            // If not on inventory, log in
            driver.get(LOGIN_PAGE_URL);
            WebElement usernameField = wait.until(visibilityOfElementLocated(By.name("username")));
            usernameField.sendKeys(USERNAME);
            WebElement passwordField = driver.findElement(By.name("password"));
            passwordField.sendKeys(PASSWORD);
            WebElement loginButton = driver.findElement(By.id("login"));
            loginButton.click();
            wait.until(urlContains("inventory"));
        }
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replaceAll("[^\\d.]", ""));
    }
}