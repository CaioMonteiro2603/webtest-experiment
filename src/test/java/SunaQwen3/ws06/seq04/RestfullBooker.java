package SunaQwen3.ws06.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String LOGIN_PAGE_URL = BASE_URL + "login";
    private static final String ABOUT_PAGE_URL = "https://saucelabs.com/";
    private static final String TWITTER_URL = "https://twitter.com/";
    private static final String FACEBOOK_URL = "https://www.facebook.com/";
    private static final String LINKEDIN_URL = "https://www.linkedin.com/";
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
    public void testValidLogin() {
        driver.get(LOGIN_PAGE_URL);
        assertEquals(LOGIN_PAGE_URL, driver.getCurrentUrl(), "Should be on login page");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        assertTrue(driver.getCurrentUrl().contains("inventory"), "Should be redirected to inventory page");
        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        assertTrue(inventoryList.isDisplayed(), "Inventory list should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLoginCredentials() {
        driver.get(LOGIN_PAGE_URL);
        assertEquals(LOGIN_PAGE_URL, driver.getCurrentUrl(), "Should be on login page");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Username and password do not match"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        loginIfNecessary();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();

        // Test Name (A to Z)
        sortDropdown.sendKeys("Name (A to Z)");
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Name (A to Z)"));
        WebElement firstItemName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
        String firstItemTextAtoZ = firstItemName.getText();

        // Test Name (Z to A)
        sortDropdown.click();
        sortDropdown.sendKeys("Name (Z to A)");
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Name (Z to A)"));
        String firstItemTextZtoA = firstItemName.getText();
        assertNotEquals(firstItemTextAtoZ, firstItemTextZtoA, "Sorting by name should change order");

        // Test Price (low to high)
        sortDropdown.click();
        sortDropdown.sendKeys("Price (low to high)");
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Price (low to high)"));
        WebElement firstItemPriceLow = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_price")));
        double firstPriceLow = Double.parseDouble(firstItemPriceLow.getText().replace("$", ""));

        // Test Price (high to low)
        sortDropdown.click();
        sortDropdown.sendKeys("Price (high to low)");
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Price (high to low)"));
        WebElement firstItemPriceHigh = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_price")));
        double firstPriceHigh = Double.parseDouble(firstItemPriceHigh.getText().replace("$", ""));
        assertTrue(firstPriceHigh >= firstPriceLow, "Price high to low should show higher price first");
    }

    @Test
    @Order(4)
    public void testMenuBurgerButtonAllItems() {
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        assertTrue(driver.getCurrentUrl().contains("inventory"), "Should remain on inventory page after clicking All Items");
    }

    @Test
    @Order(5)
    public void testMenuBurgerButtonAbout() {
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        String originalWindow = driver.getWindowHandle();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains(ABOUT_PAGE_URL), "About link should open Saucelabs page");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testMenuBurgerButtonLogout() {
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue(driver.getCurrentUrl().contains("login"), "Should be redirected to login page after logout");
    }

    @Test
    @Order(7)
    public void testMenuBurgerButtonResetAppState() {
        loginIfNecessary();

        // Add an item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart should have 1 item");

        // Open menu and reset app state
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Verify cart is empty
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals(0, driver.findElements(By.cssSelector(".shopping_cart_badge")).size(), "Cart badge should disappear after reset");
    }

    @Test
    @Order(8)
    public void testFooterTwitterLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-twitter']")));
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains(TWITTER_URL), "Twitter link should open Twitter domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testFooterFacebookLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-facebook']")));
        facebookLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains(FACEBOOK_URL), "Facebook link should open Facebook domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testFooterLinkedInLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-linkedin']")));
        linkedinLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains(LINKEDIN_URL), "LinkedIn link should open LinkedIn domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(11)
    public void testAddRemoveItemFromCart() {
        loginIfNecessary();

        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart should show 1 item");

        WebElement removeFromCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='remove-sauce-labs-backpack']")));
        removeFromCartButton.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals(0, driver.findElements(By.cssSelector(".shopping_cart_badge")).size(), "Cart should be empty after removing item");
    }

    @Test
    @Order(12)
    public void testCheckoutProcess() {
        loginIfNecessary();

        // Add item to cart
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".shopping_cart_link")));
        cartLink.click();

        // Proceed to checkout
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='checkout']")));
        checkoutButton.click();

        // Fill in checkout info
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='firstName']")));
        WebElement lastNameField = driver.findElement(By.cssSelector("[data-test='lastName']"));
        WebElement postalCodeField = driver.findElement(By.cssSelector("[data-test='postalCode']"));

        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        postalCodeField.sendKeys("12345");

        WebElement continueButton = driver.findElement(By.cssSelector("[data-test='continue']"));
        continueButton.click();

        // Finish checkout
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='finish']")));
        finishButton.click();

        // Verify success message
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        assertTrue(completeHeader.isDisplayed(), "Checkout complete header should be displayed");
        assertEquals("Thank you for your order!", completeHeader.getText(), "Checkout success message should match");
    }

    private void loginIfNecessary() {
        if (!driver.getCurrentUrl().contains("inventory")) {
            driver.get(LOGIN_PAGE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("doLogin"));

            usernameField.clear();
            passwordField.clear();
            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory"));
        }
    }
}