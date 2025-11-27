package SunaQwen3.ws01.seq02;

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
public class SwagLabsTestSuite {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String LOGIN = "standard_user";
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
        login(LOGIN, PASSWORD);

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page after login");

        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertTrue(inventoryList.isDisplayed(), "Inventory list should be displayed after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        login("invalid_user", PASSWORD);

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorElement.isDisplayed(), "Error message container should be displayed");
        assertTrue(errorElement.getText().contains("Username and password do not match"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testSortingLowToHigh() {
        navigateToInventory();
        selectSortingOption("lohi");

        List<WebElement> priceElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        double previousPrice = 0;
        for (WebElement priceElement : priceElements) {
            double currentPrice = Double.parseDouble(priceElement.getText().replace("$", ""));
            assertTrue(currentPrice >= previousPrice, "Prices should be in ascending order");
            previousPrice = currentPrice;
        }
    }

    @Test
    @Order(4)
    void testSortingHighToLow() {
        navigateToInventory();
        selectSortingOption("hilo");

        List<WebElement> priceElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        double previousPrice = Double.MAX_VALUE;
        for (WebElement priceElement : priceElements) {
            double currentPrice = Double.parseDouble(priceElement.getText().replace("$", ""));
            assertTrue(currentPrice <= previousPrice, "Prices should be in descending order");
            previousPrice = currentPrice;
        }
    }

    @Test
    @Order(5)
    void testSortingAtoZ() {
        navigateToInventory();
        selectSortingOption("az");

        List<WebElement> nameElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        String previousName = "";
        for (WebElement nameElement : nameElements) {
            String currentName = nameElement.getText();
            assertTrue(currentName.compareToIgnoreCase(previousName) >= 0, "Names should be in alphabetical order");
            previousName = currentName;
        }
    }

    @Test
    @Order(6)
    void testSortingZtoA() {
        navigateToInventory();
        selectSortingOption("za");

        List<WebElement> nameElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        String previousName = "ZZZZZ";
        for (WebElement nameElement : nameElements) {
            String currentName = nameElement.getText();
            assertTrue(currentName.compareToIgnoreCase(previousName) <= 0, "Names should be in reverse alphabetical order");
            previousName = currentName;
        }
    }

    @Test
    @Order(7)
    void testAddToCartAndRemove() {
        navigateToInventory();
        resetAppState();

        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        WebElement removeFromCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='remove-sauce-labs-backpack']")));
        removeFromCartButton.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, cartBadges.size(), "Cart badge should not be displayed after removing item");
    }

    @Test
    @Order(8)
    void testMenuAllItems() {
        navigateToInventory();

        openMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should remain on inventory page after clicking All Items");
    }

    @Test
    @Order(9)
    void testMenuAboutExternal() {
        navigateToInventory();

        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }
        assertNotNull(newWindow, "New window should be opened for About link");

        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should redirect to saucelabs.com domain");

        driver.close();
        driver.switchTo().window(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should return to inventory page after closing About tab");
    }

    @Test
    @Order(10)
    void testMenuLogout() {
        navigateToInventory();

        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Should be redirected to login page after logout");

        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='login-button']")));
        assertTrue(loginButton.isDisplayed(), "Login button should be displayed on login page");
    }

    @Test
    @Order(11)
    void testMenuResetAppState() {
        navigateToInventory();

        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item before reset");

        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, cartBadges.size(), "Cart should be empty after reset app state");
    }

    @Test
    @Order(12)
    void testFooterTwitterLink() {
        navigateToInventory();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-twitter']")));
        twitterLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }
        assertNotNull(newWindow, "New window should be opened for Twitter link");

        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should redirect to twitter.com domain");

        driver.close();
        driver.switchTo().window(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should return to inventory page after closing Twitter tab");
    }

    @Test
    @Order(13)
    void testFooterFacebookLink() {
        navigateToInventory();

        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-facebook']")));
        facebookLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }
        assertNotNull(newWindow, "New window should be opened for Facebook link");

        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should redirect to facebook.com domain");

        driver.close();
        driver.switchTo().window(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should return to inventory page after closing Facebook tab");
    }

    @Test
    @Order(14)
    void testFooterLinkedInLink() {
        navigateToInventory();

        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-linkedin']")));
        linkedinLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }
        assertNotNull(newWindow, "New window should be opened for LinkedIn link");

        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should redirect to linkedin.com domain");

        driver.close();
        driver.switchTo().window(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should return to inventory page after closing LinkedIn tab");
    }

    @Test
    @Order(15)
    void testCheckoutProcess() {
        navigateToInventory();
        resetAppState();

        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();

        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".shopping_cart_link")));
        cartLink.click();

        wait.until(ExpectedConditions.urlContains("cart.html"));
        assertTrue(driver.getCurrentUrl().contains("cart.html"), "Should navigate to cart page");

        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='checkout']")));
        checkoutButton.click();

        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        assertTrue(driver.getCurrentUrl().contains("checkout-step-one.html"), "Should navigate to checkout step one");

        WebElement firstNameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='firstName']")));
        firstNameField.sendKeys("John");

        WebElement lastNameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='lastName']")));
        lastNameField.sendKeys("Doe");

        WebElement postalCodeField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='postalCode']")));
        postalCodeField.sendKeys("12345");

        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='continue']")));
        continueButton.click();

        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        assertTrue(driver.getCurrentUrl().contains("checkout-step-two.html"), "Should navigate to checkout step two");

        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='finish']")));
        finishButton.click();

        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        assertTrue(driver.getCurrentUrl().contains("checkout-complete.html"), "Should navigate to checkout complete page");

        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        assertTrue(completeHeader.isDisplayed(), "Complete header should be displayed");
        assertTrue(completeHeader.getText().contains("Thank you for your order"), "Should display thank you message");
    }

    private void login(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='username']")));
        usernameField.clear();
        usernameField.sendKeys(username);

        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='password']")));
        passwordField.clear();
        passwordField.sendKeys(password);

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='login-button']")));
        loginButton.click();
    }

    private void navigateToInventory() {
        driver.get(BASE_URL);
        login(LOGIN, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    private void selectSortingOption(String optionValue) {
        WebElement sortingDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortingDropdown.click();

        String optionSelector = String.format("option[value='%s']", optionValue);
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(optionSelector)));
        option.click();

        // Wait for sorting to complete by checking that the dropdown selection is updated
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), optionValue));
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#menu_button_container button")));
        menuButton.click();

        // Wait for menu to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("menu_container")));
    }

    private void resetAppState() {
        try {
            openMenu();
            WebElement resetLink = driver.findElement(By.id("reset_sidebar_link"));
            if (resetLink.isDisplayed()) {
                resetLink.click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
            }
            // Close menu
            WebElement closeButton = driver.findElement(By.cssSelector("#menu_button_container button"));
            closeButton.click();
        } catch (NoSuchElementException | TimeoutException e) {
            // Menu or reset link not available, continue
        }
    }
}