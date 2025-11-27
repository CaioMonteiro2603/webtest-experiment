package SunaQwen3.ws05.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class SiteTest {
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
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page after login");
        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertTrue(inventoryContainer.isDisplayed(), "Inventory container should be visible");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        login("invalid_user", PASSWORD);
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Username and password do not match"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testLockedUserLogin() {
        driver.get(BASE_URL);
        login("locked_out_user", PASSWORD);
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("locked out"), "Error message should indicate user is locked out");
    }

    @Test
    @Order(4)
    void testSortingDropdown() {
        performLoginIfNecessary();
        WebElement sortSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".product_sort_container")));
        String[] sortOptions = {"za", "az", "lohi", "hilo"};
        for (String option : sortOptions) {
            sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
            sortSelect.sendKeys(option);
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), getSortOptionText(option)));
            validateSortOrder(option);
        }
    }

    @Test
    @Order(5)
    void testMenuAllItems() {
        performLoginIfNecessary();
        openMenu();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should return to inventory page");
    }

    @Test
    @Order(6)
    void testMenuAboutExternalLink() {
        performLoginIfNecessary();
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should redirect to Sauce Labs domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    void testMenuLogout() {
        performLoginIfNecessary();
        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"), "Should be redirected to login page after logout");
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        assertTrue(loginButton.isDisplayed(), "Login button should be visible on login page");
    }

    @Test
    @Order(8)
    void testMenuResetAppState() {
        performLoginIfNecessary();
        // Add an item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart should have 1 item");
        // Reset app state
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Verify cart is empty
        driver.get(BASE_URL.replace("index.html", "inventory.html"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals(0, driver.findElements(By.cssSelector(".shopping_cart_badge")).size(), "Cart badge should not be present after reset");
    }

    @Test
    @Order(9)
    void testFooterTwitterLink() {
        performLoginIfNecessary();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-twitter']")));
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
    }

    @Test
    @Order(10)
    void testFooterFacebookLink() {
        performLoginIfNecessary();
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-facebook']")));
        String originalWindow = driver.getWindowHandle();
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
    }

    @Test
    @Order(11)
    void testFooterLinkedInLink() {
        performLoginIfNecessary();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='social-linkedin']")));
        String originalWindow = driver.getWindowHandle();
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

    private void login(String username, String password) {
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        usernameInput.clear();
        usernameInput.sendKeys(username);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        loginButton.click();
    }

    private void performLoginIfNecessary() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            login(USERNAME, PASSWORD);
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout_sidebar_link")));
    }

    private String getSortOptionText(String value) {
        switch (value) {
            case "za": return "Name (Z to A)";
            case "az": return "Name (A to Z)";
            case "lohi": return "Price (low to high)";
            case "hilo": return "Price (high to low)";
            default: return "";
        }
    }

    private void validateSortOrder(String sortOption) {
        java.util.List<WebElement> itemNames = driver.findElements(By.cssSelector(".inventory_item_name"));
        java.util.List<String> names = new java.util.ArrayList<>();
        for (WebElement element : itemNames) {
            names.add(element.getText());
        }
        java.util.List<String> sortedNames = new java.util.ArrayList<>(names);
        switch (sortOption) {
            case "az":
                sortedNames.sort(String.CASE_INSENSITIVE_ORDER);
                break;
            case "za":
                sortedNames.sort(String.CASE_INSENSITIVE_ORDER.reversed());
                break;
            case "lohi":
                java.util.List<WebElement> itemPrices = driver.findElements(By.cssSelector(".inventory_item_price"));
                java.util.List<Double> prices = new java.util.ArrayList<>();
                for (WebElement element : itemPrices) {
                    prices.add(Double.parseDouble(element.getText().replace("$", "")));
                }
                java.util.List<Double> sortedPrices = new java.util.ArrayList<>(prices);
                sortedPrices.sort(Double::compareTo);
                assertEquals(prices, sortedPrices, "Prices should be sorted low to high");
                return;
            case "hilo":
                java.util.List<WebElement> itemPricesDesc = driver.findElements(By.cssSelector(".inventory_item_price"));
                java.util.List<Double> pricesDesc = new java.util.ArrayList<>();
                for (WebElement element : itemPricesDesc) {
                    pricesDesc.add(Double.parseDouble(element.getText().replace("$", "")));
                }
                java.util.List<Double> sortedPricesDesc = new java.util.ArrayList<>(pricesDesc);
                sortedPricesDesc.sort(Double::compareTo);
                sortedPricesDesc.sort(java.util.Collections.reverseOrder());
                assertEquals(pricesDesc, sortedPricesDesc, "Prices should be sorted high to low");
                return;
            default:
                break;
        }
        assertEquals(names, sortedNames, "Item names should be sorted correctly: " + sortOption);
    }
}