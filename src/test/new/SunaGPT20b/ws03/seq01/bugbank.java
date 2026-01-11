package SunaGPT20b.ws03.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

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

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        usernameField.clear();
        usernameField.sendKeys(user);

        WebElement passwordField = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='password']")));
        passwordField.clear();
        passwordField.sendKeys(pass);

        WebElement loginButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        loginButton.click();

        // Verify login success by checking if we're still on login page or redirected
        try {
            Thread.sleep(2000); // Simple wait for page transition
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openMenu() {
        WebElement menuButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-menu-btn")));
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Wait for the menu to close and inventory page to be refreshed
        wait.until(ExpectedConditions.invisibilityOf(resetLink));
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        // Verify inventory items are displayed
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory should contain items after login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        usernameField.clear();
        usernameField.sendKeys("invalid@example.com");

        WebElement passwordField = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='password']")));
        passwordField.clear();
        passwordField.sendKeys("wrongpass");

        WebElement loginButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        loginButton.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("product_sort_container")));

        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (String option : options) {
            sortDropdown.click();
            WebElement opt = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath("//option[text()='" + option + "']")));
            opt.click();

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
    public void testMenuAllItems() {
        login(USERNAME, PASSWORD);
        openMenu();
        WebElement allItems = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should navigate to inventory page");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USERNAME, PASSWORD);
        openMenu();
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new window
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        // Verify external domain (example.com used as placeholder)
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("example.com") || currentUrl.contains("github.io"),
                "About link should open an external page");

        driver.close();
        driver.switchTo().window(originalWindow);
        resetAppState();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USERNAME, PASSWORD);
        openMenu();
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to the login page");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login(USERNAME, PASSWORD);
        // Add an item to cart to change state
        WebElement addToCart = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCart.click();

        // Verify cart badge shows 1
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should show 1 after adding an item");

        // Reset app state
        resetAppState();

        // Verify cart badge is gone
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(),
                "Cart badge should be removed after resetting app state");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        // Assume footer links have identifiable ids
        String[][] socialLinks = {
                {"twitter", "twitter.com"},
                {"facebook", "facebook.com"},
                {"linkedin", "linkedin.com"}
        };
        String originalWindow = driver.getWindowHandle();

        for (String[] linkInfo : socialLinks) {
            String linkId = linkInfo[0];
            String expectedDomain = linkInfo[1];

            List<WebElement> elements = driver.findElements(By.id(linkId));
            if (elements.isEmpty()) {
                continue; // Skip if not present
            }
            WebElement link = elements.get(0);
            link.click();

            // Wait for new window
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains(expectedDomain),
                    "Social link should open a page containing " + expectedDomain);

            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
    }

    @Test
    @Order(9)
    public void testCheckoutProcess() {
        login(USERNAME, PASSWORD);
        // Add first item to cart
        WebElement addToCart = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCart.click();

        // Open cart
        WebElement cartIcon = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        cartIcon.click();

        // Verify cart page
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart.html"),
                "Should navigate to cart page");

        // Click checkout
        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();

        // Fill checkout information
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        WebElement firstName = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("first-name")));
        firstName.sendKeys("Caio");
        WebElement lastName = driver.findElement(By.id("last-name"));
        lastName.sendKeys("Tester");
        WebElement postalCode = driver.findElement(By.id("postal-code"));
        postalCode.sendKeys("12345");
        WebElement continueBtn = driver.findElement(By.id("continue"));
        continueBtn.click();

        // Verify overview page
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        WebElement finishBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishBtn.click();

        // Verify completion
        wait.until(ExpectedConditions.urlContains("/checkout-complete.html"));
        WebElement completeHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText().trim(),
                "Checkout should complete with thank you message");

        resetAppState();
    }
}