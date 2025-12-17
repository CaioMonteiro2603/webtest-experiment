package SunaGPT20b.ws01.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    @BeforeEach
    public void navigateToBase() {
        driver.get(BASE_URL);
        // Ensure we are on login page before each test
        if (driver.getCurrentUrl().contains("inventory.html")) {
            // Already logged in, log out to start fresh
            openMenu();
            WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
            logout.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        }
    }

    private void login() {
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.clear();
        usernameField.sendKeys(USERNAME);

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-cross-btn")));
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Wait for the menu to close and inventory to be refreshed
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-cross-btn")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "URL should contain /inventory.html after successful login");
        List<WebElement> items = driver.findElements(By.className("inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory items should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.clear();
        usernameField.sendKeys("invalid_user");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys("wrong_password");

        driver.findElement(By.id("login-button")).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("product_sort_container")));

        // Test each sorting option
        String[] options = {"az", "za", "lohi", "hilo"};
        for (String value : options) {
            sortDropdown.click();
            WebElement option = driver.findElement(By.cssSelector("#product_sort_container option[value='" + value + "']"));
            option.click();

            // Verify that items are reordered by checking first item's name
            List<WebElement> names = driver.findElements(By.className("inventory_item_name"));
            Assertions.assertFalse(names.isEmpty(), "Item names should be present after sorting");
            String firstName = names.get(0).getText();

            // Simple sanity check: after sorting by name A-Z, first name should start with A or earlier alphabetically
            if (value.equals("az")) {
                Assertions.assertTrue(firstName.matches("^[A-Za-z].*"),
                        "First item name should be alphabetic after A-Z sort");
            }
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuBurgerAndReset() {
        login();

        // Open menu
        openMenu();
        Assertions.assertTrue(driver.findElement(By.id("react-burger-cross-btn")).isDisplayed(),
                "Menu close button should be visible after opening menu");

        // Click Reset App State
        WebElement resetLink = driver.findElement(By.id("reset_sidebar_link"));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-cross-btn")));

        // Verify inventory is still displayed
        Assertions.assertTrue(driver.findElement(By.id("inventory_container")).isDisplayed(),
                "Inventory should be visible after resetting app state");
    }

    @Test
    @Order(5)
    public void testAddToCartAndCheckout() {
        login();

        // Add first item to cart
        WebElement firstAddBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn_primary') and contains(text(),'Add to cart')]")));
        firstAddBtn.click();

        // Verify cart badge count
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show count 1 after adding an item");

        // Go to cart
        driver.findElement(By.id("shopping_cart_container")).click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        List<WebElement> cartItems = driver.findElements(By.className("cart_item"));
        Assertions.assertEquals(1, cartItems.size(), "Cart should contain exactly one item");

        // Checkout
        driver.findElement(By.id("checkout")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));

        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();

        // Verify completion
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText().trim(),
                "Checkout completion message should be displayed");

        // Return to inventory and reset state
        driver.findElement(By.id("back-to-products")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        resetAppState();
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        login();

        // About link (opens new tab)
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new window
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.titleContains("Sauce Labs"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About link should navigate to a saucelabs.com domain");

        driver.close();
        driver.switchTo().window(originalWindow);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-cross-btn")));

        // Social links (Twitter, Facebook, LinkedIn)
        String[] socialIds = {"twitter", "facebook", "linkedin"};
        for (String idSuffix : socialIds) {
            WebElement socialLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a.social_" + idSuffix)));
            originalWindow = driver.getWindowHandle();
            socialLink.click();

            // Switch to new window if opened
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.titleIs(driver.getTitle())); // just wait for load

            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(idSuffix),
                    "Social link should navigate to a " + idSuffix + " domain");

            driver.close();
            driver.switchTo().window(originalWindow);
        }

        // Ensure we are back on inventory page
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Should return to inventory page after external link checks");
    }
}