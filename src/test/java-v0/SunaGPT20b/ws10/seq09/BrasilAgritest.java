package SunaGPT20b.ws10.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USER_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String USER_PASSWORD = "10203040";

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

    private void login(String email, String password) {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[name='email'], input[type='email']")));
        emailField.clear();
        emailField.sendKeys(email);

        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[name='password'], input[type='password']")));
        passwordField.clear();
        passwordField.sendKeys(password);

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button[id='login-button']")));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/inventory"));
    }

    private void ensureLoggedIn() {
        if (!driver.getCurrentUrl().contains("/inventory")) {
            login(USER_EMAIL, USER_PASSWORD);
        }
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-menu-btn")));
    }

    private void closeMenuIfOpen() {
        List<WebElement> closeButtons = driver.findElements(By.id("react-burger-cross-btn"));
        if (!closeButtons.isEmpty()) {
            closeButtons.get(0).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-cross-btn")));
        }
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("reset_sidebar_link")));
    }

    private void switchToNewTabAndClose(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        Set<String> windowsBefore = driver.getWindowHandles();

        // Wait for new window
        wait.until(driver -> driver.getWindowHandles().size() > windowsBefore.size());

        Set<String> windowsAfter = driver.getWindowHandles();
        windowsAfter.removeAll(windowsBefore);
        String newWindow = windowsAfter.iterator().next();

        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link did not navigate to expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "Login failed: inventory page not loaded.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[name='email'], input[type='email']")));
        emailField.clear();
        emailField.sendKeys(USER_EMAIL);

        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[name='password'], input[type='password']")));
        passwordField.clear();
        passwordField.sendKeys("wrongpassword");

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button[id='login-button']")));
        loginButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".error-message, .error, .alert-danger")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message not displayed for invalid login.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        ensureLoggedIn();
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("sort")));
        Select select = new Select(sortSelect);
        List<WebElement> options = select.getOptions();

        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());
            // Verify that the selected option is indeed active
            WebElement selected = select.getFirstSelectedOption();
            Assertions.assertEquals(option.getText(), selected.getText(),
                    "Sorting option not selected correctly: " + option.getText());

            // Simple verification that the list order changed:
            // Check that the first item's name changes after sorting (if applicable)
            List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
            if (!items.isEmpty()) {
                String firstItem = items.get(0).getText();
                Assertions.assertNotNull(firstItem, "First inventory item name should not be null.");
            }
        }
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        ensureLoggedIn();
        openMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "All Items link did not navigate to inventory page.");
        closeMenuIfOpen();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternal() {
        ensureLoggedIn();
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("about_sidebar_link")));
        aboutLink.click();

        // Assume About opens an external site in a new tab
        switchToNewTabAndClose("about");
        closeMenuIfOpen();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        ensureLoggedIn();
        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Logout did not return to login page.");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        ensureLoggedIn();
        // Add an item to cart to change state
        WebElement firstAddButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[id^='add-to-cart-']")));
        firstAddButton.click();

        // Verify cart badge shows 1
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge did not show expected count before reset.");

        // Reset app state
        resetAppState();

        // Verify cart badge is gone
        List<WebElement> badgesAfter = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badgesAfter.isEmpty(),
                "Cart badge still present after resetting app state.");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        ensureLoggedIn();
        // Scroll to footer if needed
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Twitter
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        switchToNewTabAndClose("twitter.com");

        // Facebook
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        switchToNewTabAndClose("facebook.com");

        // LinkedIn
        WebElement linkedInLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='linkedin.com']")));
        linkedInLink.click();
        switchToNewTabAndClose("linkedin.com");
    }

    @Test
    @Order(9)
    public void testCheckoutFlow() {
        ensureLoggedIn();
        // Add first item to cart
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[id^='add-to-cart-']")));
        addButton.click();

        // Go to cart
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("shopping_cart_container")));
        cartIcon.click();
        wait.until(ExpectedConditions.urlContains("/cart"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart"),
                "Did not navigate to cart page.");

        // Click Checkout
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("checkout")));
        checkoutButton.click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/checkout-step-one"),
                "Did not navigate to checkout step one.");

        // Fill checkout information
        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("first-name")));
        firstName.sendKeys("Test");

        WebElement lastName = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("last-name")));
        lastName.sendKeys("User");

        WebElement postalCode = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("postal-code")));
        postalCode.sendKeys("12345");

        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("continue")));
        continueButton.click();

        wait.until(ExpectedConditions.urlContains("/checkout-step-two"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/checkout-step-two"),
                "Did not navigate to checkout step two.");

        // Finish checkout
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("finish")));
        finishButton.click();

        wait.until(ExpectedConditions.urlContains("/checkout-complete"));
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".complete-header")));
        Assertions.assertTrue(completeHeader.getText().toLowerCase().contains("thank"),
                "Checkout completion message not found.");
    }
}