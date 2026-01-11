package SunaQwen3.ws05.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);
        assertTrue(driver.getTitle().contains("Swag Labs"), "Page title should contain 'Swag Labs'");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page");
        assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed(), "Inventory list should be displayed");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Epic sadface"), "Error message should contain 'Epic sadface'");
    }

    @Test
    @Order(3)
    void testSortingDropdown() {
        // Ensure we're on inventory page
        driver.get(BASE_URL.replace("index.html", "inventory.html"));

        WebElement sortingDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortingDropdown.click();

        // Test Name (A to Z)
        sortingDropdown.findElement(By.cssSelector("option[value='az']")).click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.className("inventory_item_name"))));
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        assertTrue(items.size() > 0, "At least one item should be present");
        String firstItemName = items.get(0).getText();
        assertTrue(firstItemName.compareTo(items.get(items.size()-1).getText()) <= 0, "Items should be sorted A to Z");

        // Test Name (Z to A)
        sortingDropdown = driver.findElement(By.className("product_sort_container"));
        sortingDropdown.click();
        sortingDropdown.findElement(By.cssSelector("option[value='za']")).click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.className("inventory_item_name"))));
        items = driver.findElements(By.className("inventory_item_name"));
        firstItemName = items.get(0).getText();
        assertTrue(firstItemName.compareTo(items.get(items.size()-1).getText()) >= 0, "Items should be sorted Z to A");

        // Test Price (low to high)
        sortingDropdown = driver.findElement(By.className("product_sort_container"));
        sortingDropdown.click();
        sortingDropdown.findElement(By.cssSelector("option[value='lohi']")).click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.className("inventory_item_price"))));
        List<WebElement> prices = driver.findElements(By.className("inventory_item_price"));
        assertTrue(prices.size() > 0, "At least one price should be present");
        double firstPrice = Double.parseDouble(prices.get(0).getText().replace("$", ""));
        double lastPrice = Double.parseDouble(prices.get(prices.size()-1).getText().replace("$", ""));
        assertTrue(firstPrice <= lastPrice, "Prices should be sorted low to high");

        // Test Price (high to low)
        sortingDropdown = driver.findElement(By.className("product_sort_container"));
        sortingDropdown.click();
        sortingDropdown.findElement(By.cssSelector("option[value='hilo']")).click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.className("inventory_item_price"))));
        prices = driver.findElements(By.className("inventory_item_price"));
        firstPrice = Double.parseDouble(prices.get(0).getText().replace("$", ""));
        lastPrice = Double.parseDouble(prices.get(prices.size()-1).getText().replace("$", ""));
        assertTrue(firstPrice >= lastPrice, "Prices should be sorted high to low");
    }

    @Test
    @Order(4)
    void testMenuFunctionality() {
        driver.get(BASE_URL.replace("index.html", "inventory.html"));

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Wait for menu to open
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("menu_button")));

        // Click All Items
        WebElement allItemsLink = driver.findElement(By.id("inventory_sidebar_link"));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should remain on inventory page after clicking All Items");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("menu_button")));

        // Click About (external)
        WebElement aboutLink = driver.findElement(By.id("about_sidebar_link"));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should redirect to saucelabs.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("menu_button")));

        // Click Reset App State
        WebElement resetLink = driver.findElement(By.id("reset_sidebar_link"));
        resetLink.click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.className("inventory_item"))));
        assertTrue(driver.findElements(By.className("inventory_item")).size() > 0, "Inventory items should be visible after reset");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("menu_button")));

        // Click Logout
        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"), "Should be redirected to login page after logout");
    }

    @Test
    @Order(5)
    void testFooterSocialLinks() {
        driver.get(BASE_URL.replace("index.html", "inventory.html"));

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should redirect to twitter.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook")));
        facebookLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should redirect to facebook.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin")));
        linkedinLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should redirect to linkedin.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    void testAddRemoveFromCart() {
        driver.get(BASE_URL.replace("index.html", "inventory.html"));

        // Add first item to cart
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".btn_inventory")));
        assertTrue(addToCartButtons.size() > 0, "At least one add to cart button should be present");
        addToCartButtons.get(0).click();

        // Wait for button to change to remove
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".btn_inventory"), "Remove"));

        // Check cart badge
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Remove item from cart
        WebElement removeButton = driver.findElement(By.cssSelector(".btn_inventory"));
        removeButton.click();

        // Wait for button to change back to add to cart
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".btn_inventory"), "Add to cart"));

        // Cart badge should disappear
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals(0, driver.findElements(By.cssSelector(".shopping_cart_badge")).size(), "Cart badge should not be visible");
    }

    @Test
    @Order(7)
    void testCheckoutProcess() {
        driver.get(BASE_URL.replace("index.html", "inventory.html"));

        // Add an item to cart
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".btn_inventory")));
        addToCartButtons.get(0).click();

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.className("shopping_cart_link")));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // Proceed to checkout
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutButton.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));

        // Fill in checkout info
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        WebElement lastNameField = driver.findElement(By.id("last-name"));
        WebElement zipCodeField = driver.findElement(By.id("postal-code"));

        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        zipCodeField.sendKeys("12345");

        WebElement continueButton = driver.findElement(By.id("continue"));
        continueButton.click();

        // Wait for next page
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));

        // Finish checkout
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishButton.click();

        // Wait for confirmation
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
        assertTrue(completeHeader.isDisplayed(), "Complete header should be displayed");
        assertTrue(completeHeader.getText().contains("Thank you"), "Completion message should contain 'Thank you'");
    }
}