package GPT20b.ws08.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore{

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void initDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* -------------------- Utility methods -------------------- */

    /**
     * Returns the first element that matches any of the given CSS selectors.
     */
    private WebElement findElement(String... cssSelectors) {
        for (String sel : cssSelectors) {
            List<WebElement> els = driver.findElements(By.cssSelector(sel));
            if (!els.isEmpty()) {
                return els.get(0);
            }
        }
        throw new NoSuchElementException("Could not find element matching selectors: "
                + String.join(", ", cssSelectors));
    }

    /**
     * Clicks a link that contains the given fragment, verifies the URL contains the expected domain,
     * then closes the new window/tab and switches back to the original window.
     */
    private void openAndVerifyExternalLink(String hrefFragment, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + hrefFragment + "']"));
        if (links.isEmpty()) {
            return; // Nothing to test
        }
        WebElement link = links.get(0);
        String originalHandle = driver.getWindowHandle();
        Set<String> handlesBefore = driver.getWindowHandles();

        link.click();

        Set<String> handlesAfter = driver.getWindowHandles();
        if (handlesAfter.size() > handlesBefore.size()) {
            handlesAfter.removeAll(handlesBefore);
            String newWindow = handlesAfter.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(d1 -> d1.getCurrentUrl().contains(expectedDomain));
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            wait.until(d1 -> d1.getCurrentUrl().contains(expectedDomain));
            driver.navigate().back();
        }
    }

    /* -------------------- Tests -------------------- */

    @Test
    @Order(1)
    public void testLoginPageElementsPresent() {
        driver.navigate().to(BASE_URL + "jpetstore/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("body")));
        assertTrue(driver.findElements(By.cssSelector("input[name='username'], input[id='username']"))
                .size() > 0, "Username input should be present");
        assertTrue(driver.findElements(By.cssSelector("input[type='password'], input[id='password']"))
                .size() > 0, "Password input should be present");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        driver.navigate().to(BASE_URL + "jpetstore/login.do?");
        WebElement username = findElement("input[name='username'], input[id='username']");
        WebElement password = findElement("input[type='password'], input[id='password']");
        WebElement submit = findElement("button[type='submit'], input[type='submit']");

        username.clear();
        username.sendKeys("invalidUser");
        password.clear();
        password.sendKeys("wrongPassword");
        submit.click();

        List<WebElement> errorMsg = driver.findElements(By.cssSelector(".error, .alert-danger"));
        assertFalse(errorMsg.isEmpty(), "Error message should display for invalid credentials");
    }

    @Test
    @Order(3)
    public void testValidLoginAndNavigateToHome() {
        driver.navigate().to(BASE_URL + "jpetstore/login.do?");
        WebElement username = findElement("input[name='username'], input[id='username']");
        WebElement password = findElement("input[type='password'], input[id='password']");
        WebElement submit = findElement("button[type='submit'], input[type='submit']");

        username.clear();
        username.sendKeys("standard_user");
        password.clear();
        password.sendKeys("secret_sauce");
        submit.click();

        wait.until(ExpectedConditions.urlContains("jpetstore"));
        assertTrue(driver.getCurrentUrl().contains("jpetstore"), "Should be on home page after login");

        // ensure we are on Home containing product listing
        assertFalse(driver.findElements(By.cssSelector(".productList, .product-container, .product")).isEmpty(),
                "Product listing should be visible after login");
    }

    @Test
    @Order(4)
    public void testSortingDropdownChangesOrder() {
        // Assume we are on home page via previous test or start fresh
        driver.navigate().to(BASE_URL + "jpetstore/");
        WebElement sortDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("select[name='sortBy'], select[name='sort']")));
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        assertTrue(options.size() > 1, "Sorting dropdown should have multiple options");

        String initialValue = options.get(0).getAttribute("value");
        options.get(1).click();
        String newValue = sortDropdown.getAttribute("value");
        assertNotEquals(initialValue, newValue, "Sorting selection should change the value");

        // Verify the order of first product changed
        List<WebElement> products = driver.findElements(By.cssSelector(".product, .product-item"));
        assertFalse(products.isEmpty(), "Products should be present after sorting");
        String firstProductBefore = products.get(0).getText();

        options.get(2).click(); // another option
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(
                products.get(0), firstProductBefore)));
        String firstProductAfter = products.get(0).getText();
        assertNotEquals(firstProductBefore, firstProductAfter,
                "First product name should change after changing sort option");
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        // Visit home if necessary
        driver.navigate().to(BASE_URL + "jpetstore/");
        // Add an item to cart
        List<WebElement> addButtons = driver.findElements(By.cssSelector("input[value='ADD TO CART'], .add-to-cart, button[contains(text(),'Add')]"));
        if (!addButtons.isEmpty()) {
            addButtons.get(0).click();
            WebElement cartCount = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".cartCount, .shopping_cart_badge, .cart-badge")));
            assertEquals("1", cartCount.getText(), "Cart badge should show 1 after adding item");
        }

        // Reset shopping cart
        WebElement resetBtn = findElement("a[href*='reset'], button#resetCart, .reset-cart");
        resetBtn.click();
        wait.until(ExpectedConditions.numberOfElementsToBe(
                By.cssSelector(".cartCount, .shopping_cart_badge, .cart-badge"), 0));
        // Cart should be empty
        List<WebElement> badges = driver.findElements(By.cssSelector(".cartCount, .shopping_cart_badge, .cart-badge"));
        assertTrue(badges.isEmpty() || badges.get(0).getText().equals("0"),
                "Cart badge should be zero after reset");
    }

    @Test
    @Order(6)
    public void testBurgerMenuOptions() {
        driver.navigate().to(BASE_URL + "jpetstore/");
        // Assume there's a burger menu with id 'menuButton'
        WebElement menuBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#menuButton, button[aria-label='Menu'], .menu-btn, .navbar-toggler")));
        menuBtn.click();

        // All Items (typically the home link)
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='jpetstore']")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("jpetstore"));
        assertFalse(driver.findElements(By.cssSelector(".productList, .product, .product-container")).isEmpty(),
                "All Items should display product list");

        // Reopen menu
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#menuButton, button[aria-label='Menu'], .menu-btn, .navbar-toggler")));
        menuBtn.click();

        // About link (external)
        openAndVerifyExternalLink("about", "about");

        // Reopen menu
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#menuButton, button[aria-label='Menu'], .menu-btn, .navbar-toggler")));
        menuBtn.click();

        // Reset App State
        WebElement resetLink = findElement("a[href*='reset'], button#resetCart, .reset-cart");
        resetLink.click();
        wait.until(ExpectedConditions.numberOfElementsToBe(
                By.cssSelector(".cartCount, .shopping_cart_badge, .cart-badge"), 0));

        // Reopen menu
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#menuButton, button[aria-label='Menu'], .menu-btn, .navbar-toggler")));
        menuBtn.click();

        // Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='logout'], button#logout, .logout-link, a[href*='signout']")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue(driver.getCurrentUrl().contains("login"), "Should return to login after logout");
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        driver.navigate().to(BASE_URL + "jpetstore/");
        openAndVerifyExternalLink("twitter.com", "twitter.com");
        openAndVerifyExternalLink("facebook.com", "facebook.com");
        openAndVerifyExternalLink("linkedin.com", "linkedin.com");
    }

    @Test
    @Order(8)
    public void testAddToCartAndCheckout() {
        driver.navigate().to(BASE_URL + "jpetstore/");
        // Ensure cart icon is present
        WebElement cartIcon = findElement(".cartIcon, .shopping_cart, .cart-icon, .cart");
        assertNotNull(cartIcon, "Cart icon should be present");

        // Add first item to cart
        List<WebElement> addButtons = driver.findElements(By.cssSelector("input[value='ADD TO CART'], .add-to-cart, button[contains(text(),'Add')]"));
        if (!addButtons.isEmpty()) {
            WebElement addBtn = addButtons.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(addBtn));
            addBtn.click();

            WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".cartCount, .shopping_cart_badge, .cart-badge")));
            assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after adding item");
        }

        // Go to cart
        WebElement cartLink = findElement("a[href*='cart'], button#cart, .cart-link, .view-cart");
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart"));

        // Verify cart has the item
        List<WebElement> cartItems = driver.findElements(By.cssSelector(".cartItem, tr.cart_row, .cart-item"));
        assertFalse(cartItems.isEmpty(), "Cart should contain at least one item");

        // Proceed to checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button#checkout, a[href*='checkout'], .checkout-btn")));
        checkoutBtn.click();

        // Fill payment details (simple: use placeholders if any)
        WebElement firstName = findElement("input[name='firstName'], input[id='firstName'], input[name='billing.firstName']");
        WebElement lastName = findElement("input[name='lastName'], input[id='lastName'], input[name='billing.lastName']");
        WebElement address = findElement("input[name='address'], input[id='address'], input[name='billing.address']");
        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button#continue, button[type='submit'], .continue-btn")));

        if (firstName != null) {
            firstName.clear();
            firstName.sendKeys("John");
        }
        if (lastName != null) {
            lastName.clear();
            lastName.sendKeys("Doe");
        }
        if (address != null) {
            address.clear();
            address.sendKeys("123 Main St");
        }
        continueBtn.click();

        // Finish checkout
        WebElement finishBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button#finish, button[type='submit'], .finish-btn")));
        finishBtn.click();

        // Assert success message
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".orderConfirmation, .complete-header, .success-message")));
        assertNotNull(successMsg, "Checkout should display a success message");
    }
}