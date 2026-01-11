package geminiPro.ws01.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A complete JUnit 5 test suite for the Sauce Demo website using Selenium WebDriver
 * with Firefox in headless mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    // --- Test Configuration ---
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String VALID_USERNAME = "standard_user";
    private static final String LOCKED_OUT_USERNAME = "locked_out_user";
    private static final String PASSWORD = "secret_sauce";
    private static final String INVALID_PASSWORD = "wrong_password";

    private static WebDriver driver;
    private static WebDriverWait wait;

    // --- Locators ---
    private static final By USERNAME_INPUT = By.id("user-name");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR_MESSAGE_CONTAINER = By.cssSelector("h3[data-test='error']");
    private static final By INVENTORY_CONTAINER = By.id("inventory_container");
    private static final By BURGER_MENU_BUTTON = By.id("react-burger-menu-btn");
    private static final By LOGOUT_LINK = By.id("logout_sidebar_link");
    private static final By ABOUT_LINK = By.id("about_sidebar_link");
    private static final By RESET_APP_STATE_LINK = By.id("reset_sidebar_link");
    private static final By SHOPPING_CART_LINK = By.id("shopping_cart_container");
    private static final By SHOPPING_CART_BADGE = By.cssSelector(".shopping_cart_badge");
    private static final By PRODUCT_SORT_DROPDOWN = By.cssSelector(".product_sort_container");
    private static final By INVENTORY_ITEM_NAME = By.cssSelector(".inventory_item_name");
    private static final By INVENTORY_ITEM_PRICE = By.cssSelector(".inventory_item_price");
    private static final By ADD_TO_CART_BACKPACK_BUTTON = By.id("add-to-cart-sauce-labs-backpack");
    private static final By CHECKOUT_BUTTON = By.id("checkout");
    private static final By FIRST_NAME_INPUT = By.id("first-name");
    private static final By LAST_NAME_INPUT = By.id("last-name");
    private static final By POSTAL_CODE_INPUT = By.id("postal-code");
    private static final By CONTINUE_BUTTON = By.id("continue");
    private static final By FINISH_BUTTON = By.id("finish");
    private static final By CHECKOUT_COMPLETE_HEADER = By.className("complete-header");
    private static final By TWITTER_LINK = By.linkText("Twitter");
    private static final By FACEBOOK_LINK = By.linkText("Facebook");
    private static final By LINKEDIN_LINK = By.linkText("LinkedIn");

    @BeforeAll
    public static void setup() {
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

    private void performLogin(String username, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT)).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).click();
    }

    private void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(RESET_APP_STATE_LINK)).click();
        // Click an element to close the menu, e.g., the inventory page link
        driver.findElement(By.id("inventory_sidebar_link")).click();
        // Wait for menu to be hidden
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".bm-menu-wrap[aria-hidden='false']")));
    }
    
    @Test
    @Order(1)
    void testLoginPageTitle() {
        driver.get(BASE_URL);
        Assertions.assertEquals("Swag Labs", driver.getTitle(), "Login page title is incorrect.");
    }
    
    @Test
    @Order(2)
    void testLoginWithInvalidCredentials() {
        performLogin(VALID_USERNAME, INVALID_PASSWORD);
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"),
            "Error message for invalid credentials was not correct.");
    }

    @Test
    @Order(3)
    void testLoginWithLockedOutUser() {
        performLogin(LOCKED_OUT_USERNAME, PASSWORD);
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE_CONTAINER));
        Assertions.assertTrue(errorMessage.getText().contains("Sorry, this user has been locked out."),
            "Error message for locked out user was not correct.");
    }

    @Test
    @Order(4)
    void testSuccessfulLoginAndLogout() {
        // Login
        performLogin(VALID_USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "Login was not successful or did not redirect correctly.");

        // Logout
        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_LINK)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_BUTTON));
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL) || driver.getCurrentUrl().contains("index.html"), "Logout did not return to the login page.");
    }

    @Test
    @Order(5)
    void testProductSorting() {
        performLogin(VALID_USERNAME, PASSWORD);
        
        // Default sort (A-Z)
        List<String> namesAZ = driver.findElements(INVENTORY_ITEM_NAME).stream().map(WebElement::getText).collect(Collectors.toList());
        List<String> sortedNamesAZ = new ArrayList<>(namesAZ);
        Collections.sort(sortedNamesAZ);
        Assertions.assertEquals(sortedNamesAZ, namesAZ, "Default product sort order (A-Z) is incorrect.");

        // Z-A
        new Select(driver.findElement(PRODUCT_SORT_DROPDOWN)).selectByValue("za");
        List<String> namesZA = driver.findElements(INVENTORY_ITEM_NAME).stream().map(WebElement::getText).collect(Collectors.toList());
        List<String> sortedNamesZA = new ArrayList<>(namesZA);
        Collections.sort(sortedNamesZA, Collections.reverseOrder());
        Assertions.assertEquals(sortedNamesZA, namesZA, "Product sort order (Z-A) is incorrect.");

        // Price (low to high)
        new Select(driver.findElement(PRODUCT_SORT_DROPDOWN)).selectByValue("lohi");
        List<Double> pricesLoHi = driver.findElements(INVENTORY_ITEM_PRICE).stream()
            .map(e -> Double.parseDouble(e.getText().replace("$", "")))
            .collect(Collectors.toList());
        List<Double> sortedPricesLoHi = new ArrayList<>(pricesLoHi);
        Collections.sort(sortedPricesLoHi);
        Assertions.assertEquals(sortedPricesLoHi, pricesLoHi, "Product sort order (Price low to high) is incorrect.");
        
        // Price (high to low)
        new Select(driver.findElement(PRODUCT_SORT_DROPDOWN)).selectByValue("hilo");
        List<Double> pricesHiLo = driver.findElements(INVENTORY_ITEM_PRICE).stream()
            .map(e -> Double.parseDouble(e.getText().replace("$", "")))
            .collect(Collectors.toList());
        List<Double> sortedPricesHiLo = new ArrayList<>(pricesHiLo);
        Collections.sort(sortedPricesHiLo, Collections.reverseOrder());
        Assertions.assertEquals(sortedPricesHiLo, pricesHiLo, "Product sort order (Price high to low) is incorrect.");
    }

    @Test
    @Order(6)
    void testFullCheckoutFlow() {
        performLogin(VALID_USERNAME, PASSWORD);
        resetAppState();

        // Add item to cart and verify badge
        wait.until(ExpectedConditions.elementToBeClickable(ADD_TO_CART_BACKPACK_BUTTON)).click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(SHOPPING_CART_BADGE));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge count is incorrect after adding an item.");

        // Go to cart and proceed to checkout
        driver.findElement(SHOPPING_CART_LINK).click();
        wait.until(ExpectedConditions.elementToBeClickable(CHECKOUT_BUTTON)).click();

        // Fill out checkout information
        wait.until(ExpectedConditions.visibilityOfElementLocated(FIRST_NAME_INPUT)).sendKeys("Gemini");
        driver.findElement(LAST_NAME_INPUT).sendKeys("Pro");
        driver.findElement(POSTAL_CODE_INPUT).sendKeys("12345");
        driver.findElement(CONTINUE_BUTTON).click();

        // Finish the order
        wait.until(ExpectedConditions.elementToBeClickable(FINISH_BUTTON)).click();

        // Verify completion
        WebElement confirmationHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(CHECKOUT_COMPLETE_HEADER));
        Assertions.assertEquals("Thank you for your order!", confirmationHeader.getText(), "Checkout completion message is incorrect.");
        
        // Verify cart is empty after purchase
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(INVENTORY_CONTAINER));
        Assertions.assertTrue(driver.findElements(SHOPPING_CART_BADGE).isEmpty(), "Cart should be empty after completing an order.");
    }
    
    @Test
    @Order(7)
    void testResetAppState() {
        performLogin(VALID_USERNAME, PASSWORD);

        // Add an item to ensure the state changes
        wait.until(ExpectedConditions.elementToBeClickable(ADD_TO_CART_BACKPACK_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SHOPPING_CART_BADGE));
        Assertions.assertEquals("1", driver.findElement(SHOPPING_CART_BADGE).getText(), "Cart should have 1 item before reset.");

        // Reset the state
        resetAppState();

        // Verify state has been reset (cart is empty)
        Assertions.assertTrue(driver.findElements(SHOPPING_CART_BADGE).isEmpty(), "Cart badge should not be present after resetting app state.");
    }

    @Test
    @Order(8)
    void testAboutLinkNavigatesToExternalSite() {
        performLogin(VALID_USERNAME, PASSWORD);
        String originalWindow = driver.getWindowHandle();

        wait.until(ExpectedConditions.elementToBeClickable(BURGER_MENU_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(ABOUT_LINK)).click();

        // Wait for the new window/tab and switch to it
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "The 'About' link did not navigate to saucelabs.com.");
        
        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "Did not return to the inventory page after closing the 'About' tab.");
    }

    @Test
    @Order(9)
    void testFooterSocialLinks() {
        performLogin(VALID_USERNAME, PASSWORD);
        String inventoryPageUrl = driver.getCurrentUrl();

        // Use a helper lambda to test each external link
        java.util.function.Consumer<By> testExternalLink = (locator) -> {
            String originalWindow = driver.getWindowHandle();
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));

            String newWindow = driver.getWindowHandles().stream()
                .filter(h -> !h.equals(originalWindow)).findFirst().get();
            driver.switchTo().window(newWindow);
            
            String expectedDomain = "";
            if (locator.equals(TWITTER_LINK)) expectedDomain = "x.com"; // Twitter is now X
            else if (locator.equals(FACEBOOK_LINK)) expectedDomain = "facebook.com";
            else if (locator.equals(LINKEDIN_LINK)) expectedDomain = "linkedin.com";

            final String domain = expectedDomain; // Final variable for lambda
            wait.until(d -> d.getCurrentUrl().contains(domain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain), "Link did not lead to the correct domain: " + domain);

            driver.close();
            driver.switchTo().window(originalWindow);
        };

        testExternalLink.accept(TWITTER_LINK);
        testExternalLink.accept(FACEBOOK_LINK);
        testExternalLink.accept(LINKEDIN_LINK);
        
        Assertions.assertEquals(inventoryPageUrl, driver.getCurrentUrl(), "The page URL changed after testing social links.");
    }
}