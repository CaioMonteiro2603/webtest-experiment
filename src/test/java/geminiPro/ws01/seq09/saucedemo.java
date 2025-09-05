package geminiPRO.ws01.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * JUnit 5 test suite for saucedemo.com using Selenium WebDriver with headless Firefox.
 * This suite covers login, product sorting, cart management, checkout, navigation, and external links.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoTest {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String VALID_USERNAME = "standard_user";
    private static final String LOCKED_OUT_USERNAME = "locked_out_user";
    private static final String PASSWORD = "secret_sauce";

    private static WebDriver driver;
    private static WebDriverWait wait;

    // Locators
    private final By usernameInput = By.id("user-name");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By inventoryContainer = By.id("inventory_container");
    private final By errorMessageContainer = By.cssSelector("h3[data-test='error']");
    private final By burgerMenuButton = By.id("react-burger-menu-btn");
    private final By logoutSidebarLink = By.id("logout_sidebar_link");
    private final By resetSidebarLink = By.id("reset_sidebar_link");
    private final By aboutSidebarLink = By.id("about_sidebar_link");
    private final By closeMenuButton = By.id("react-burger-cross-btn");
    private final By sortContainer = By.cssSelector(".product_sort_container");
    private final By inventoryItemNames = By.cssSelector(".inventory_item_name");
    private final By inventoryItemPrices = By.cssSelector(".inventory_item_price");
    private final By addToCartBackpackButton = By.id("add-to-cart-sauce-labs-backpack");
    private final By cartBadge = By.cssSelector(".shopping_cart_badge");
    private final By cartLink = By.cssSelector(".shopping_cart_link");
    private final By checkoutButton = By.id("checkout");
    private final By firstNameInput = By.id("first-name");
    private final By lastNameInput = By.id("last-name");
    private final By postalCodeInput = By.id("postal-code");
    private final By continueButton = By.id("continue");
    private final By finishButton = By.id("finish");
    private final By completeHeader = By.cssSelector(".complete-header");

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED headless mode via arguments
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToLogin() {
        driver.get(BASE_URL);
        // Ensure we are on the login page before each test by checking URL and elements
        assertTrue(driver.getCurrentUrl().contains("index.html"), "Should be on the login page.");
        assertTrue(driver.findElements(loginButton).size() > 0, "Login button should be present.");
    }
    
    // --- Test Methods ---

    @Test
    @Order(1)
    void testInvalidLogin() {
        performLogin("invalid_user", "invalid_password");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessageContainer));
        assertTrue(errorMessage.getText().contains("Username and password do not match any user"), "Error message for invalid credentials did not appear as expected.");
    }

    @Test
    @Order(2)
    void testLockedOutUser() {
        performLogin(LOCKED_OUT_USERNAME, PASSWORD);
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessageContainer));
        assertTrue(errorMessage.getText().contains("Sorry, this user has been locked out"), "Error message for locked out user did not appear as expected.");
    }

    @Test
    @Order(3)
    void testSuccessfulLogin() {
        performLogin(VALID_USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("/inventory.html"), "Login was not successful, did not redirect to inventory page.");
        assertTrue(driver.findElement(inventoryContainer).isDisplayed(), "Inventory container should be visible after login.");
    }
    
    @Test
    @Order(4)
    void testProductSorting() {
        performLogin(VALID_USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        // Test Name (A to Z) - Default
        List<String> namesAz = getElementTextList(inventoryItemNames);
        List<String> sortedNamesAz = new ArrayList<>(namesAz);
        Collections.sort(sortedNamesAz);
        assertEquals(sortedNamesAz, namesAz, "Products should be sorted by name A-Z by default.");

        // Test Name (Z to A)
        new Select(driver.findElement(sortContainer)).selectByValue("za");
        List<String> namesZa = getElementTextList(inventoryItemNames);
        List<String> sortedNamesZa = new ArrayList<>(namesZa);
        Collections.sort(sortedNamesZa, Collections.reverseOrder());
        assertEquals(sortedNamesZa, namesZa, "Products should be sorted by name Z-A.");

        // Test Price (low to high)
        new Select(driver.findElement(sortContainer)).selectByValue("lohi");
        List<Double> pricesLohi = getPrices();
        List<Double> sortedPricesLohi = new ArrayList<>(pricesLohi);
        Collections.sort(sortedPricesLohi);
        assertEquals(sortedPricesLohi, pricesLohi, "Products should be sorted by price low-to-high.");

        // Test Price (high to low)
        new Select(driver.findElement(sortContainer)).selectByValue("hilo");
        List<Double> pricesHilo = getPrices();
        List<Double> sortedPricesHilo = new ArrayList<>(pricesHilo);
        Collections.sort(sortedPricesHilo, Collections.reverseOrder());
        assertEquals(sortedPricesHilo, pricesHilo, "Products should be sorted by price high-to-low.");
    }
    
    @Test
    @Order(5)
    void testFullCheckoutFlow() {
        // 1. Login and add item
        performLogin(VALID_USERNAME, PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(addToCartBackpackButton)).click();
        assertEquals("1", driver.findElement(cartBadge).getText(), "Cart badge should show 1 item.");
        
        // 2. Go to cart and start checkout
        driver.findElement(cartLink).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));
        wait.until(ExpectedConditions.elementToBeClickable(checkoutButton)).click();
        
        // 3. Fill user information
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        driver.findElement(firstNameInput).sendKeys("Gemini");
        driver.findElement(lastNameInput).sendKeys("Pro");
        driver.findElement(postalCodeInput).sendKeys("12345");
        driver.findElement(continueButton).click();
        
        // 4. Finish checkout
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        wait.until(ExpectedConditions.elementToBeClickable(finishButton)).click();
        
        // 5. Assert completion
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(completeHeader));
        assertEquals("THANK YOU FOR YOUR ORDER", successMessage.getText(), "Checkout completion message is incorrect.");
        
        // 6. Cleanup by resetting state
        resetAppState();
        assertFalse(isElementPresent(cartBadge), "Cart badge should not be present after resetting app state.");
    }
    
    @Test
    @Order(6)
    void testSideMenuActions() {
        performLogin(VALID_USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        // Test Reset App State
        wait.until(ExpectedConditions.elementToBeClickable(addToCartBackpackButton)).click();
        assertEquals("1", driver.findElement(cartBadge).getText(), "Cart badge should be 1 before reset.");
        resetAppState();
        assertFalse(isElementPresent(cartBadge), "Cart badge should be gone after reset.");
        
        // Test Logout
        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutSidebarLink)).click();
        assertTrue(driver.getCurrentUrl().contains("index.html"), "Should be redirected to login page after logout.");
        assertTrue(isElementPresent(loginButton), "Login button should be visible after logout.");
    }

    @Test
    @Order(7)
    void testAboutLink() {
        performLogin(VALID_USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));

        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(aboutSidebarLink));
        
        handleExternalLink(aboutLink, "saucelabs.com");
        
        // Ensure we are back on the inventory page
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be back on the inventory page.");
    }
    
    @Test
    @Order(8)
    void testFooterSocialLinks() {
        performLogin(VALID_USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
        
        handleExternalLink(driver.findElement(By.linkText("Twitter")), "twitter.com");
        handleExternalLink(driver.findElement(By.linkText("Facebook")), "facebook.com");
        handleExternalLink(driver.findElement(By.linkText("LinkedIn")), "linkedin.com");
        
        // Final check to ensure we are still on the inventory page
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should have returned to the inventory page after checking all social links.");
    }
    
    // --- Helper Methods ---

    /**
     * Performs login action.
     * @param username The username to use.
     * @param password The password to use.
     */
    private void performLogin(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).sendKeys(username);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginButton).click();
    }

    /**
     * Resets the application state using the burger menu.
     * Assumes the user is logged in and on a page with the burger menu.
     */
    private void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(burgerMenuButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(resetSidebarLink)).click();
        // The menu closes automatically after reset, but we wait for clickability of the close button
        // as a proxy for the animation to finish before proceeding.
        wait.until(ExpectedConditions.elementToBeClickable(closeMenuButton)).click();
    }
    
    /**
     * Checks if an element is present on the page.
     * @param by The locator for the element.
     * @return true if the element is found, false otherwise.
     */
    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Extracts text from a list of WebElements.
     * @param locator The locator for the list of elements.
     * @return A list of strings.
     */
    private List<String> getElementTextList(By locator) {
        return driver.findElements(locator).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    /**
     * Extracts and parses prices from the inventory item price elements.
     * @return A list of doubles representing the prices.
     */
    private List<Double> getPrices() {
        return getElementTextList(inventoryItemPrices).stream()
                .map(price -> Double.parseDouble(price.replace("$", "")))
                .collect(Collectors.toList());
    }
    
    /**
     * Handles clicking an external link, verifying the new tab's URL, closing it, and returning control.
     * @param linkElement The WebElement of the link to click.
     * @param expectedDomain The domain expected in the new tab's URL (e.g., "twitter.com").
     */
    private void handleExternalLink(WebElement linkElement, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(linkElement)).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        Set<String> allWindows = driver.getWindowHandles();
        String newWindow = allWindows.stream().filter(handle -> !handle.equals(originalWindow)).findFirst().orElse(null);
        
        if (newWindow == null) {
            fail("New window did not open for link with expected domain: " + expectedDomain);
        }

        driver.switchTo().window(newWindow);
        wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL of the new tab should contain " + expectedDomain);
        driver.close();
        
        driver.switchTo().window(originalWindow);
        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
    }
}