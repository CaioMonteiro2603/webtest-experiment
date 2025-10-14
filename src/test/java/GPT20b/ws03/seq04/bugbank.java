package GPT20b.ws03.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugbankHeadlessTest {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void init() {
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

    /* ---------------------------------------------------------------------- */
    /* Helper methods                                                         */
    /* ---------------------------------------------------------------------- */

    private WebElement findElement(String... cssSelectors) {
        for (String sel : cssSelectors) {
            List<WebElement> elements = driver.findElements(By.cssSelector(sel));
            if (!elements.isEmpty()) {
                return elements.get(0);
            }
        }
        throw new NoSuchElementException(
                "Unable to locate element using selectors: " + Arrays.toString(cssSelectors));
    }

    private void navigateTo(String url) {
        driver.navigate().to(url);
    }

    private void logIn() {
        navigateTo(BASE_URL);
        WebElement usernameField = findElement(
                "input#user-name",
                "input#login-username",
                "input[name='username']",
                "input[name='user']",
                "input[placeholder='Username']",
                "input[placeholder='Email']");
        WebElement passwordField = findElement(
                "input#password",
                "input#login-password",
                "input[type='password']",
                "input[placeholder='Password']");

        usernameField.clear();
        usernameField.sendKeys(USERNAME);
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = findElement(
                "input#login-button",
                "button#login-button",
                "button.w-100",
                "button[type='submit']");
        loginButton.click();

        // Wait for inventory items or some indicator of successful login
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/home"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".item-card")),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item"))));
        assertTrue(driver.getCurrentUrl().contains("/home") ||
                        driver.getCurrentUrl().contains("/index") &&
                        (driver.findElements(By.cssSelector(".item-card")).size() > 0 ||
                         driver.findElements(By.cssSelector(".inventory_item")).size() > 0),
                "Login did not navigate to expected page or items not loaded");
    }

    private void logOut() {
        // Burger menu might toggle
        WebElement burger = driver.findElement(By.cssSelector(".burger-menu-button, button#cartbtn"));
        try {
            burger.click();
            WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href*='logout'], a#logout, a[href*='login'], button#logout")));
            logout.click();
            wait.until(ExpectedConditions.urlContains("/index"));
            assertTrue(driver.getCurrentUrl().contains("/index") ||
                        driver.getCurrentUrl().contains("/"),
                    "Logout did not return to login page");
        } catch (NoSuchElementException e) {
            // Fallback: direct click on sign out link if present
            WebElement logoutLink = findElement("a.logout, a#logout");
            logoutLink.click();
            wait.until(ExpectedConditions.urlContains("/index"));
            assertTrue(driver.getCurrentUrl().contains("/index") ||
                        driver.getCurrentUrl().contains("/"),
                    "Logout via link did not return to login page");
        }
    }

    private void resetAppState() {
        // Attempt to click "Reset App State" link if present
        List<WebElement> resetLinks = driver.findElements(By.cssSelector("a#resetapp, a[href*='reset']"));
        if (!resetLinks.isEmpty()) {
            WebElement reset = resetLinks.get(0);
            reset.click();
            wait.until(ExpectedConditions.urlContains("/home"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".item-card")));
        }
    }

    private void openAndVerifyExternalLink(String partialHref, String domain) {
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + partialHref + "']"));
        if (links.isEmpty()) return; // nothing to test
        WebElement link = links.get(0);
        String originalWindow = driver.getWindowHandle();
        Set<String> currentHandles = driver.getWindowHandles();
        link.click();
        Set<String> newHandles = driver.getWindowHandles();
        if (newHandles.size() > currentHandles.size()) {
            newHandles.removeAll(currentHandles);
            String newWindow = newHandles.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(d -> d.getCurrentUrl().contains(domain));
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            // Same tab
            wait.until(d -> d.getCurrentUrl().contains(domain));
            driver.navigate().back();
        }
        // After returning, ensure we are back on inventory
        wait.until(ExpectedConditions.urlContains("/home"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".item-card")));
    }

    /* ---------------------------------------------------------------------- */
    /* Test methods                                                          */
    /* ---------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testInvalidLogin() {
        navigateTo(BASE_URL);
        WebElement usernameField = findElement(
                "input#user-name",
                "input[placeholder='Username']",
                "input[placeholder='Email']");
        WebElement passwordField = findElement(
                "input#password",
                "input[type='password']",
                "input[placeholder='Password']");
        usernameField.clear();
        usernameField.sendKeys("invalid_user");
        passwordField.clear();
        passwordField.sendKeys("wrong_pass");
        WebElement loginButton = findElement(
                "input#login-button",
                "button.w-100",
                "button[type='submit']");
        loginButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".error-messages, .error, h3[role='alert'], div.alert")));
        assertNotNull(errorMsg, "Error message should be displayed");
        String text = errorMsg.getText().trim();
        assertTrue(text.toLowerCase().contains("username") || text.toLowerCase().contains("password"),
                "Error message should mention invalid credentials");
    }

    @Test
    @Order(2)
    public void testLoginAndLogout() {
        logIn();
        // Verify items present
        List<WebElement> items = driver.findElements(By.cssSelector(".item-card, .inventory_item"));
        assertFalse(items.isEmpty(), "Inventory items should be present after login");
        logOut();
    }

    @Test
    @Order(3)
    public void testAddToCartAndCheckout() {
        logIn();
        // Add first item to cart
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button.add-to-cart, button.btn-primary"));
        assertFalse(addButtons.isEmpty(), "Add to cart buttons should be present");
        addButtons.get(0).click();

        // Check cart badge
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".cart-count, .shopping_cart_badge")));
        assertEquals("1", badge.getText(), "Cart badge should show 1 item");

        // Go to cart
        WebElement cartIcon = findElement("a#cart, button#cartbtn, button[aria-label='cart']",
                                        ".cart-icon");
        cartIcon.click();

        wait.until(ExpectedConditions.urlContains("/cart"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".cart-item, .cart_products")));

        // Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button#checkout, button.checkout, button.btn-success")));
        checkoutBtn.click();

        // Fill checkout form
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input#first-name, input[name='firstName']")));
        WebElement firstName = findElement("input#first-name", "input[name='firstName']");
        WebElement lastName = findElement("input#last-name", "input[name='lastName']");
        WebElement postalCode = findElement("input#postal-code", "input[name='postalCode']");

        firstName.clear();
        firstName.sendKeys("John");
        lastName.clear();
        lastName.sendKeys("Doe");
        postalCode.clear();
        postalCode.sendKeys("12345");

        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button#continue, button.btn-primary, input[name='continue']")));
        continueButton.click();

        // Finish and verify confirmation
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button#finish, button.btn-success")));
        finishButton.click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".complete-header, h2.title")));
        assertEquals("Thank you for your order!", confirmation.getText().trim(),
                "Checkout confirmation message mismatch");
        resetAppState();
        logOut();
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        logIn();
        // Locate sorting dropdown
        List<WebElement> sortDropdowns = driver.findElements(By.cssSelector("select#sort-products, select#sort-dropdown"));
        if (sortDropdowns.isEmpty()) {
            logOut();
            return; // No sorting control present
        }
        WebElement sortDropdown = sortDropdowns.get(0);
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        assertFalse(options.isEmpty(), "Sorting dropdown should have options");

        // Store initial first item name
        WebElement firstItemName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".item-card .item-name, .inventory_item_name")));
        String initialName = firstItemName.getText();

        for (WebElement option : options) {
            String value = option.getAttribute("value");
            if (value == null || value.isEmpty()) continue;
            option.click();
            WebElement currentFirst = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".item-card .item-name, .inventory_item_name")));
            assertNotEquals(initialName, currentFirst.getText(),
                    "First item name should change after selecting sort option: " + value);
            initialName = currentFirst.getText();
        }
        logOut();
    }

    @Test
    @Order(5)
    public void testBurgerMenuOptions() {
        logIn();
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".burger-menu-button, button#menu-btn", ".menu-button")));
        burger.click();

        // All Items (assuming linked to home)
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='home'], a#home-link, a[href*='index']")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/home"));
        assertTrue(driver.findElements(By.cssSelector(".item-card")).size() > 0,
                "All Items should display inventory");

        // Reopen menu
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".burger-menu-button, button#menu-btn", ".menu-button")));
        burger.click();

        // About page (external link)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='about'], a#about-link, a[href*='https']")));
        openAndVerifyExternalLink(aboutLink.getAttribute("href"), aboutLink.getAttribute("href"));

        // Reopen menu
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".burger-menu-button, button#menu-btn", ".menu-button")));
        burger.click();

        // Reset App State if available
        List<WebElement> resetLinks = driver.findElements(By.cssSelector("a[href*='reset'], a#resetapp"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            wait.until(ExpectedConditions.urlContains("/home"));
        }

        // Reopen menu
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".burger-menu-button, button#menu-btn", ".menu-button")));
        burger.click();

        // Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='logout'], a#logout, a[href*='login']")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("/index"));
        assertTrue(driver.getCurrentUrl().contains("/index"),
                "Logout did not return to login page");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        logIn();
        openAndVerifyExternalLink("twitter.com", "twitter.com");
        openAndVerifyExternalLink("facebook.com", "facebook.com");
        openAndVerifyExternalLink("linkedin.com", "linkedin.com");
        logOut();
    }
}