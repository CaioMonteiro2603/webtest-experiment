package geminiPRO.ws08.seq04;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A comprehensive JUnit 5 test suite for the JPetStore demo application.
 * It uses Selenium WebDriver with Firefox in headless mode to test core functionalities,
 * including login, product browsing, cart management, checkout, and external links.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStoreHeadlessFirefoxTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String CATALOG_URL = BASE_URL + "catalog";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

    @BeforeAll
    static void setupAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void teardownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setupEach() {
        driver.get(BASE_URL);
        // Navigate from the splash page to the main store
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Enter the Store"))).click();
        wait.until(ExpectedConditions.urlToBe(CATALOG_URL));
    }

    private void login(String username, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.name("signon")).click();
    }

    private void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out"))).click();
    }

    @Test
    @Order(1)
    @DisplayName("Test Page Title and Main Content")
    void testPageTitleAndHeader() {
        Assertions.assertEquals("JPetStore Demo", driver.getTitle(), "Page title should be correct.");
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("WelcomeContent")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome to JPetStore"), "Welcome message should be present.");
    }

    @Test
    @Order(2)
    @DisplayName("Test Invalid Login Attempt")
    void testInvalidLogin() {
        login("wrongUser", "wrongPass");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password."), "Error message for invalid login should be displayed.");
    }

    @Test
    @Order(3)
    @DisplayName("Test Successful Login and Logout")
    void testSuccessfulLoginAndLogout() {
        login(USERNAME, PASSWORD);
        WebElement welcomeContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("WelcomeContent")));
        Assertions.assertTrue(welcomeContent.getText().contains("Welcome ABC!"), "Welcome message for logged-in user should be present.");

        logout();
        WebElement signInLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign In")));
        Assertions.assertTrue(signInLink.isDisplayed(), "Sign In link should be visible after logout.");
    }

    @Test
    @Order(4)
    @DisplayName("Navigate Categories, Select Product, and Add to Cart")
    void testProductNavigationAndAddToCart() {
        login(USERNAME, PASSWORD);
        // Navigate to Fish
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#SidebarContent a[href*='FISH']"))).click();
        // Navigate to Angelfish
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01"))).click();
        // Select Large Angelfish
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-1"))).click();
        // Add to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();

        WebElement cartItemDescription = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(text(), 'Large Angelfish')]")));
        Assertions.assertTrue(cartItemDescription.isDisplayed(), "Large Angelfish should be in the shopping cart.");
    }

    @Test
    @Order(5)
    @DisplayName("Update Item Quantity and Remove Item from Cart")
    void testCartUpdateAndRemoval() {
        // Add an item to the cart to ensure the test is self-contained
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#SidebarContent a[href*='DOGS']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("K9-BD-01"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-6"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("updateCartQuantities")));

        // Update quantity
        WebElement quantityInput = driver.findElement(By.cssSelector("input[name='EST-6']"));
        quantityInput.clear();
        quantityInput.sendKeys("3");
        driver.findElement(By.name("updateCartQuantities")).click();

        // Assert updated total
        WebElement totalCost = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(text(), 'Sub Total: $55.50')]")));
        Assertions.assertTrue(totalCost.isDisplayed(), "Subtotal should be updated to reflect the new quantity.");

        // Remove item
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Remove"))).click();

        // Assert cart is empty
        WebElement emptyCartMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td/b[contains(text(),'Your cart is empty.')]")));
        Assertions.assertTrue(emptyCartMessage.isDisplayed(), "Cart should be empty after removing the item.");
    }

    @Test
    @Order(6)
    @DisplayName("Test Product Search Functionality")
    void testSearchFunctionality() {
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("keyword")));
        searchInput.sendKeys("Manx");
        driver.findElement(By.name("searchProducts")).click();

        WebElement searchResultLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("FL-DSH-01")));
        WebElement searchResultName = driver.findElement(By.xpath("//td[contains(text(), 'Manx')]"));

        Assertions.assertAll("Search results should be correct for 'Manx'",
            () -> Assertions.assertTrue(searchResultLink.isDisplayed(), "Product ID link for Manx should be visible."),
            () -> Assertions.assertTrue(searchResultName.isDisplayed(), "Product name 'Manx' should be in the results table.")
        );
    }

    @Test
    @Order(7)
    @DisplayName("Perform a Full Checkout Flow")
    void testCheckoutFlow() {
        login(USERNAME, PASSWORD);

        // Add an item to cart
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#SidebarContent a[href*='REPTILES']"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("RP-SN-01"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-11"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart"))).click();

        // Proceed to checkout
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Proceed to Checkout"))).click();

        // Continue to confirmation
        wait.until(ExpectedConditions.elementToBeClickable(By.name("newOrder"))).click();

        // Confirm order
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Confirm"))).click();

        // Assert success message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[contains(text(), 'Thank you, your order has been submitted.')]")));
        Assertions.assertTrue(successMessage.isDisplayed(), "Order confirmation message should be displayed.");
    }

    @Test
    @Order(8)
    @DisplayName("Verify User Information on My Account Page")
    void testMyAccountPage() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("My Account"))).click();
        wait.until(ExpectedConditions.urlContains("editAccountForm"));

        WebElement userIdField = driver.findElement(By.name("account.username"));
        Assertions.assertEquals(USERNAME, userIdField.getAttribute("value"), "Username on account page should match logged-in user.");

        List<WebElement> passwordFields = driver.findElements(By.name("account.password"));
        Assertions.assertEquals(2, passwordFields.size(), "There should be two password fields on the account page.");
    }

    @Test
    @Order(9)
    @DisplayName("Test External Link in Footer Opens in New Tab")
    void testExternalFooterLink() {
        String originalWindow = driver.getWindowHandle();
        Assertions.assertEquals(1, driver.getWindowHandles().size(), "There should be only one window open initially.");

        WebElement externalLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("www.aspectran.com")));
        externalLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> allWindows = driver.getWindowHandles();
        List<String> windowHandles = new ArrayList<>(allWindows);

        String newWindow = windowHandles.get(1); // Assuming the new window is the second one
        driver.switchTo().window(newWindow);

        // Assert on the new tab's URL without waiting for full page load
        wait.until(ExpectedConditions.urlContains("aspectran.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"), "URL of the new tab should contain 'aspectran.com'.");

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);

        wait.until(ExpectedConditions.numberOfWindowsToBe(1));
        Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"), "Driver should be focused on the original JPetStore window.");
    }
}