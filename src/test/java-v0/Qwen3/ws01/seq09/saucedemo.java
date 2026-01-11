package Qwen3.ws01.seq09;

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
public class saucedemo {

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
        assertTrue(driver.getTitle().contains("Swag Labs"), "Page title should contain 'Swag Labs'");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain inventory.html after login");

        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        assertTrue(inventoryList.isDisplayed(), "Inventory list should be displayed after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be visible");
        assertTrue(errorMessage.getText().contains("Epic sadface"), "Error message should indicate login failure");
    }

    @Test
    @Order(3)
    void testSortingLowToHigh() {
        loginIfNecessary();

        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortSelect.click();
        sortSelect.findElement(By.cssSelector("option[value='lohi']")).click();

        wait.until(ExpectedConditions.textToBe(By.className("product_sort_container"), "Price (low to high)"));

        java.util.List<WebElement> priceElements = driver.findElements(By.className("inventory_item_price"));
        double previousPrice = 0;
        for (WebElement priceElement : priceElements) {
            double currentPrice = Double.parseDouble(priceElement.getText().replace("$", ""));
            assertTrue(currentPrice >= previousPrice, "Prices should be sorted from low to high");
            previousPrice = currentPrice;
        }
    }

    @Test
    @Order(4)
    void testSortingHighToLow() {
        loginIfNecessary();

        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortSelect.click();
        sortSelect.findElement(By.cssSelector("option[value='hilo']")).click();

        wait.until(ExpectedConditions.textToBe(By.className("product_sort_container"), "Price (high to low)"));

        java.util.List<WebElement> priceElements = driver.findElements(By.className("inventory_item_price"));
        Double previousPrice = null;
        for (WebElement priceElement : priceElements) {
            double currentPrice = Double.parseDouble(priceElement.getText().replace("$", ""));
            if (previousPrice != null) {
                assertTrue(currentPrice <= previousPrice, "Prices should be sorted from high to low");
            }
            previousPrice = currentPrice;
        }
    }

    @Test
    @Order(5)
    void testSortingAtoZ() {
        loginIfNecessary();

        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortSelect.click();
        sortSelect.findElement(By.cssSelector("option[value='az']")).click();

        wait.until(ExpectedConditions.textToBe(By.className("product_sort_container"), "Name (A to Z)"));

        java.util.List<WebElement> nameElements = driver.findElements(By.className("inventory_item_name"));
        String previousName = "";
        for (WebElement nameElement : nameElements) {
            String currentName = nameElement.getText();
            assertTrue(currentName.compareToIgnoreCase(previousName) >= 0, "Names should be sorted A to Z");
            previousName = currentName;
        }
    }

    @Test
    @Order(6)
    void testSortingZtoA() {
        loginIfNecessary();

        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortSelect.click();
        sortSelect.findElement(By.cssSelector("option[value='za']")).click();

        wait.until(ExpectedConditions.textToBe(By.className("product_sort_container"), "Name (Z to A)"));

        java.util.List<WebElement> nameElements = driver.findElements(By.className("inventory_item_name"));
        String previousName = "ZZZ"; // Start high for descending
        for (WebElement nameElement : nameElements) {
            String currentName = nameElement.getText();
            assertTrue(currentName.compareToIgnoreCase(previousName) <= 0, "Names should be sorted Z to A");
            previousName = currentName;
        }
    }

    @Test
    @Order(7)
    void testMenuAllItems() {
        loginIfNecessary();

        openMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should return to inventory page");
    }

    @Test
    @Order(8)
    void testMenuAboutExternalLink() {
        loginIfNecessary();

        openMenu();
        String originalWindow = driver.getWindowHandle();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));

        aboutLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String aboutUrl = driver.getCurrentUrl();
        assertTrue(aboutUrl.contains("saucelabs.com"), "About link should redirect to Sauce Labs domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    void testMenuLogout() {
        loginIfNecessary();

        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        assertTrue(driver.getCurrentUrl().contains("index.html"), "Should return to login page after logout");
        assertTrue(driver.findElement(By.id("login-button")).isDisplayed(), "Login button should be visible");
    }

    @Test
    @Order(10)
    void testMenuResetAppState() {
        loginIfNecessary();

        // Add an item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[name='add-to-cart-sauce-labs-backpack']")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart should have 1 item before reset");

        openMenu();
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetButton.click();

        // Wait for UI to reset
        wait.until(ExpectedConditions.stalenessOf(cartBadge));
        java.util.List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, cartBadges.size(), "Cart badge should disappear after reset");
    }

    @Test
    @Order(11)
    void testFooterTwitterLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String twitterUrl = driver.getCurrentUrl();
        assertTrue(twitterUrl.contains("twitter.com") || twitterUrl.contains("x.com"), "Twitter link should redirect to X/Twitter domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(12)
    void testFooterFacebookLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook']")));
        facebookLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String facebookUrl = driver.getCurrentUrl();
        assertTrue(facebookUrl.contains("facebook.com"), "Facebook link should redirect to Facebook domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(13)
    void testFooterLinkedInLink() {
        loginIfNecessary();

        String originalWindow = driver.getWindowHandle();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin']")));
        linkedinLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String linkedinUrl = driver.getCurrentUrl();
        assertTrue(linkedinUrl.contains("linkedin.com"), "LinkedIn link should redirect to LinkedIn domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginIfNecessary() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            usernameField.clear();
            passwordField.clear();
            usernameField.sendKeys(LOGIN);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu_button")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout_sidebar_link")));
    }
}