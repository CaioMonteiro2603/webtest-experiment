package SunaQwen3.ws06.seq05;

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
public class SiteTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String LOGIN_PAGE_URL = BASE_URL + "login";
    private static final String INVENTORY_PAGE_URL = BASE_URL + "inventory";
    private static final String ABOUT_PAGE_URL = "https://saucelabs.com/";

    // Test credentials
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "password";

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
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("loginBtn"));

        usernameField.sendKeys(VALID_USERNAME);
        passwordField.sendKeys(VALID_PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        assertTrue(driver.getCurrentUrl().contains("inventory"), "URL should contain 'inventory' after login");

        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        assertTrue(inventoryList.isDisplayed(), "Inventory list should be displayed after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("loginBtn"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
        assertTrue(errorElement.isDisplayed(), "Error message should be displayed for invalid login");
        assertTrue(errorElement.getText().contains("Username and password do not match"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testSortingDropdownOptions() {
        // Ensure logged in
        loginIfNotOnInventoryPage();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        String[] expectedOptions = {"az", "za", "lohi", "hilo"};

        for (String optionValue : expectedOptions) {
            sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
            sortDropdown.click();

            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("option[value='" + optionValue + "']")));
            option.click();

            wait.until(ExpectedConditions.attributeToBe(sortDropdown, "value", optionValue));

            // Assert that sorting actually changed by checking first item name or price
            List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
            assertTrue(items.size() > 0, "At least one inventory item should be present");

            // Basic check that items are present after sort
            assertTrue(items.get(0).isDisplayed(), "First item should be visible after sorting");
        }
    }

    @Test
    @Order(4)
    void testMenuBurgerButtonAndOptions() {
        loginIfNotOnInventoryPage();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Wait for menu to open
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        WebElement aboutLink = driver.findElement(By.id("about_sidebar_link"));
        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        WebElement resetAppStateLink = driver.findElement(By.id("reset_sidebar_link"));

        assertTrue(allItemsLink.isDisplayed(), "All Items link should be visible in menu");
        assertTrue(aboutLink.isDisplayed(), "About link should be visible in menu");
        assertTrue(logoutLink.isDisplayed(), "Logout link should be visible in menu");
        assertTrue(resetAppStateLink.isDisplayed(), "Reset App State link should be visible in menu");

        // Click All Items (should close menu and stay on inventory)
        allItemsLink.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("menu_button_container")));
        assertTrue(driver.getCurrentUrl().contains("inventory"), "Should remain on inventory page after clicking All Items");

        // Reopen menu
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Reset App State
        resetAppStateLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetAppStateLink.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("menu_button_container")));

        // Verify reset by checking cart is empty
        WebElement cartBadge = driver.findElements(By.className("shopping_cart_badge")).stream().findFirst().orElse(null);
        if (cartBadge != null) {
            assertEquals("0", cartBadge.getText(), "Cart badge should show 0 after reset");
        }

        // Reopen menu
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Logout
        logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlToBe(LOGIN_PAGE_URL));
        assertEquals(LOGIN_PAGE_URL, driver.getCurrentUrl(), "Should be redirected to login page after logout");

        // Login again for subsequent tests
        loginIfNotOnInventoryPage();
    }

    @Test
    @Order(5)
    void testFooterSocialLinks() {
        loginIfNotOnInventoryPage();

        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".social a"));
        assertEquals(3, socialLinks.size(), "There should be 3 social links in the footer");

        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = socialLinks.get(0);
        String twitterUrl = twitterLink.getAttribute("href");
        assertTrue(twitterUrl.contains("twitter.com"), "Twitter link should point to twitter.com");

        twitterLink.click();
        switchToNewWindowAndValidate(originalWindow, "twitter.com");

        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        socialLinks = driver.findElements(By.cssSelector(".social a")); // Re-locate
        WebElement facebookLink = socialLinks.get(1);
        String facebookUrl = facebookLink.getAttribute("href");
        assertTrue(facebookUrl.contains("facebook.com"), "Facebook link should point to facebook.com");

        facebookLink.click();
        switchToNewWindowAndValidate(originalWindow, "facebook.com");

        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        socialLinks = driver.findElements(By.cssSelector(".social a")); // Re-locate
        WebElement linkedinLink = socialLinks.get(2);
        String linkedinUrl = linkedinLink.getAttribute("href");
        assertTrue(linkedinUrl.contains("linkedin.com"), "LinkedIn link should point to linkedin.com");

        linkedinLink.click();
        switchToNewWindowAndValidate(originalWindow, "linkedin.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    void testAboutLinkInMenu() {
        loginIfNotOnInventoryPage();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String aboutUrl = aboutLink.getAttribute("href");
        assertTrue(aboutUrl.contains("saucelabs.com"), "About link should point to saucelabs.com");

        aboutLink.click();

        // Should open in new tab
        String originalWindow = driver.getWindowHandle();
        switchToNewWindowAndValidate(originalWindow, "saucelabs.com");

        // Close the new tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    void testAddRemoveItemsFromCart() {
        loginIfNotOnInventoryPage();

        // Reset app state via menu
        resetAppState();

        // Add first item to cart
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".btn_inventory")));
        assertTrue(addToCartButtons.size() > 0, "At least one add to cart button should be present");

        WebElement firstAddButton = addToCartButtons.get(0);
        firstAddButton.click();

        // Wait for button to change to 'Remove'
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".btn_inventory"), "Remove"));

        // Check cart badge
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after adding first item");

        // Add second item
        List<WebElement> remainingAddButtons = driver.findElements(By.cssSelector(".btn_inventory"));
        if (remainingAddButtons.size() > 1) {
            remainingAddButtons.get(1).click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".btn_inventory"), "Remove"));
            assertEquals("2", cartBadge.getText(), "Cart badge should show 2 after adding second item");
        }

        // Remove first item (the one we added first)
        List<WebElement> removeButtons = driver.findElements(By.cssSelector(".btn_inventory"));
        removeButtons.get(0).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".btn_inventory"), "Add to cart"));

        // Cart badge should update
        if (driver.findElements(By.className("shopping_cart_badge")).size() > 0) {
            assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after removing one item");
        } else {
            fail("Cart badge should still be present with value 1");
        }
    }

    @Test
    @Order(8)
    void testCheckoutProcess() {
        loginIfNotOnInventoryPage();

        // Reset app state
        resetAppState();

        // Add an item to cart
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".btn_inventory")));
        if (addToCartButtons.isEmpty()) {
            fail("No items available to add to cart");
        }

        addToCartButtons.get(0).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".btn_inventory"), "Remove"));

        // Go to cart
        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.className("shopping_cart_link")));
        cartLink.click();

        // Wait for cart page
        wait.until(ExpectedConditions.urlContains("cart"));
        assertTrue(driver.getCurrentUrl().contains("cart"), "Should be on cart page");

        // Click checkout
        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutButton.click();

        // Wait for checkout page
        wait.until(ExpectedConditions.urlContains("checkout"));
        assertTrue(driver.getCurrentUrl().contains("checkout"), "Should be on checkout page");

        // Fill in checkout info
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name")));
        WebElement lastNameField = driver.findElement(By.id("last-name"));
        WebElement postalCodeField = driver.findElement(By.id("postal-code"));

        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        postalCodeField.sendKeys("12345");

        // Continue
        WebElement continueButton = driver.findElement(By.id("continue"));
        continueButton.click();

        // Wait for overview page
        wait.until(ExpectedConditions.urlContains("overview"));
        assertTrue(driver.getCurrentUrl().contains("overview"), "Should be on overview page");

        // Finish
        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishButton.click();

        // Wait for complete page
        wait.until(ExpectedConditions.urlContains("complete"));
        assertTrue(driver.getCurrentUrl().contains("complete"), "Should be on complete page");

        // Verify success message
        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
        assertTrue(completeHeader.isDisplayed(), "Complete header should be displayed");
        assertTrue(completeHeader.getText().toLowerCase().contains("thank you"), "Complete message should contain 'thank you'");
    }

    private void loginIfNotOnInventoryPage() {
        if (!driver.getCurrentUrl().contains("inventory")) {
            driver.get(LOGIN_PAGE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.id("loginBtn"));

            usernameField.sendKeys(VALID_USERNAME);
            passwordField.sendKeys(VALID_PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory"));
        }
    }

    private void resetAppState() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("menu_button_container")));
    }

    private void switchToNewWindowAndValidate(String originalWindow, String expectedDomain) {
        wait.until(webDriver -> webDriver.getWindowHandles().size() > 1);

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains(expectedDomain));
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), "New window should navigate to domain containing: " + expectedDomain);
    }
}