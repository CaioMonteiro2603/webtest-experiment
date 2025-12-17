package SunaQwen3.ws05.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;

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
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain 'inventory.html'");
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
        loginIfNecessary();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown.click();

        // Test Name (A to Z)
        sortDropdown.sendKeys("Name (A to Z)");
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Name (A to Z)"));
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        assertTrue(items.size() > 0, "At least one item should be present");
        String firstItemText = items.get(0).getText();
        sortDropdown.sendKeys("Name (Z to A)");
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(By.className("product_sort_container"), "Name (A to Z)")));
        String lastItemText = driver.findElements(By.className("inventory_item_name")).get(0).getText();
        assertNotEquals(firstItemText, lastItemText, "Sorting should change item order");

        // Test Price (low to high)
        sortDropdown.sendKeys("Price (low to high)");
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Price (low to high)"));
        List<WebElement> prices = driver.findElements(By.className("inventory_item_price"));
        double firstPrice = Double.parseDouble(prices.get(0).getText().replace("$", ""));
        double secondPrice = Double.parseDouble(prices.get(1).getText().replace("$", ""));
        assertTrue(firstPrice <= secondPrice, "Prices should be sorted low to high");

        // Test Price (high to low)
        sortDropdown.sendKeys("Price (high to low)");
        wait.until(ExpectedConditions.textToBePresentInElement(sortDropdown, "Price (high to low)"));
        prices = driver.findElements(By.className("inventory_item_price"));
        firstPrice = Double.parseDouble(prices.get(0).getText().replace("$", ""));
        secondPrice = Double.parseDouble(prices.get(1).getText().replace("$", ""));
        assertTrue(firstPrice >= secondPrice, "Prices should be sorted high to low");
    }

    @Test
    @Order(4)
    void testMenuNavigation() {
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should navigate to inventory page");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

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

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement resetAppState = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetAppState.click();

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"), "Should return to login page after logout");
    }

    @Test
    @Order(5)
    void testFooterSocialLinks() {
        driver.get(BASE_URL);
        loginIfNecessary();

        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".footer_social a"));
        assertEquals(3, socialLinks.size(), "Footer should have 3 social links");

        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = socialLinks.get(0);
        twitterLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        socialLinks = driver.findElements(By.cssSelector(".footer_social a"));
        WebElement facebookLink = socialLinks.get(1);
        facebookLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        socialLinks = driver.findElements(By.cssSelector(".footer_social a"));
        WebElement linkedinLink = socialLinks.get(2);
        linkedinLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    void testAddRemoveItemsFromCart() {
        loginIfNecessary();

        List<WebElement> addToCartButtons = driver.findElements(By.cssSelector(".btn_inventory"));
        assertTrue(addToCartButtons.size() > 0, "At least one item should be available to add to cart");

        WebElement firstAddToCart = addToCartButtons.get(0);
        firstAddToCart.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        List<WebElement> removeFromCartButtons = driver.findElements(By.cssSelector(".btn_inventory"));
        WebElement firstRemoveFromCart = removeFromCartButtons.get(0);
        firstRemoveFromCart.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, cartBadges.size(), "Cart badge should disappear after removing item");
    }

    private void loginIfNecessary() {
        if (driver.getCurrentUrl().contains("index.html") || !driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void switchToNewWindow(String originalWindow) {
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                return;
            }
        }
        fail("No new window found");
    }
}