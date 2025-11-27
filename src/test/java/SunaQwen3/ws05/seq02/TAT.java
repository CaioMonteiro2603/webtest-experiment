package SunaQwen3.ws05.seq02;

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
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String LOGIN_PAGE_URL = BASE_URL;
    private static final String INVENTORY_PAGE_URL = BASE_URL.replace("index.html", "inventory.html");
    private static final String ABOUT_PAGE_URL = "https://saucelabs.com/";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(LOGIN_PAGE_URL);
        assertEquals("Swag Labs", driver.getTitle(), "Page title should be 'Swag Labs'");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page");
        assertTrue(driver.findElement(By.cssSelector(".inventory_list")).isDisplayed(), "Inventory list should be displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorMessage.isDisplayed(), "Error message container should be visible");
        assertTrue(errorMessage.getText().contains("Username and password do not match"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        loginIfNecessary();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();

        // Test Name (A to Z)
        sortDropdown.sendKeys("Name (A to Z)");
        sortDropdown.sendKeys(Keys.RETURN);
        verifyProductOrderByName(true);

        // Test Name (Z to A)
        sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        sortDropdown.sendKeys("Name (Z to A)");
        sortDropdown.sendKeys(Keys.RETURN);
        verifyProductOrderByName(false);

        // Test Price (low to high)
        sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        sortDropdown.sendKeys("Price (low to high)");
        sortDropdown.sendKeys(Keys.RETURN);
        verifyProductOrderbyPrice(true);

        // Test Price (high to low)
        sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        sortDropdown.sendKeys("Price (high to low)");
        sortDropdown.sendKeys(Keys.RETURN);
        verifyProductOrderbyPrice(false);
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        loginIfNecessary();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should remain on inventory page after clicking All Items");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
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
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open Sauce Labs website");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.stalenessOf(resetLink));
        assertTrue(driver.findElement(By.cssSelector(".inventory_item")).isDisplayed(), "Inventory items should be visible after reset");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(LOGIN_PAGE_URL));
        assertEquals(LOGIN_PAGE_URL, driver.getCurrentUrl(), "Should be redirected to login page after logout");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(INVENTORY_PAGE_URL);
        loginIfNecessary();

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
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open Twitter domain");
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
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open Facebook domain");
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
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open LinkedIn domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testAddRemoveItemsFromCart() {
        loginIfNecessary();

        // Add first item to cart
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".btn_inventory")));
        assertTrue(addToCartButtons.size() > 0, "At least one item should be available");
        WebElement firstAddButton = addToCartButtons.get(0);
        firstAddButton.click();

        // Verify cart badge updates
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Remove item from cart
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn_inventory")));
        removeButton.click();

        // Verify cart badge disappears
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, cartBadges.size(), "Cart badge should not be present after removing item");
    }

    private void loginIfNecessary() {
        if (driver.getCurrentUrl().equals(LOGIN_PAGE_URL)) {
            testValidLogin();
        } else {
            try {
                driver.get(INVENTORY_PAGE_URL);
                wait.until(ExpectedConditions.urlContains("inventory.html"));
            } catch (Exception e) {
                testValidLogin();
            }
        }
    }

    private void verifyProductOrderByName(boolean ascending) {
        List<WebElement> productNames = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        String[] names = productNames.stream()
                .map(WebElement::getText)
                .toArray(String[]::new);

        for (int i = 0; i < names.length - 1; i++) {
            int comparison = names[i].compareTo(names[i + 1]);
            if (ascending) {
                assertTrue(comparison <= 0, "Products should be sorted by name ascending");
            } else {
                assertTrue(comparison >= 0, "Products should be sorted by name descending");
            }
        }
    }

    private void verifyProductOrderbyPrice(boolean ascending) {
        List<WebElement> productPrices = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        double[] prices = productPrices.stream()
                .map(WebElement::getText)
                .map(text -> Double.parseDouble(text.replace("$", "")))
                .mapToDouble(Double::doubleValue)
                .toArray();

        for (int i = 0; i < prices.length - 1; i++) {
            if (ascending) {
                assertTrue(prices[i] <= prices[i + 1], "Products should be sorted by price ascending");
            } else {
                assertTrue(prices[i] >= prices[i + 1], "Products should be sorted by price descending");
            }
        }
    }
}