package SunaQwen3.ws06.seq09;

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
public class RestfullBooker {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/";
    private static final String LOGIN_URL = BASE_URL;
    private static final String INVENTORY_URL = BASE_URL + "inventory.html";
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
        driver.get(LOGIN_URL);
        assertEquals(LOGIN_URL, driver.getCurrentUrl(), "Should be on login page");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        assertTrue(driver.getCurrentUrl().contains("inventory"), "Should navigate to inventory page after login");
        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_list")));
        assertTrue(inventoryList.isDisplayed(), "Inventory list should be displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(LOGIN_URL);
        assertEquals(LOGIN_URL, driver.getCurrentUrl(), "Should be on login page");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Username and password do not match"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginIfNecessary();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown.click();

        // Test Name (A to Z)
        sortDropdown.findElement(By.cssSelector("option[value='az']")).click();
        wait.until(ExpectedConditions.stalenessOf(sortDropdown));
        sortDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product_sort_container")));
        assertEquals("az", sortDropdown.getAttribute("value"), "Sort should be set to A to Z");

        // Test Name (Z to A)
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='za']")).click();
        wait.until(ExpectedConditions.stalenessOf(sortDropdown));
        sortDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product_sort_container")));
        assertEquals("za", sortDropdown.getAttribute("value"), "Sort should be set to Z to A");

        // Test Price (low to high)
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='lohi']")).click();
        wait.until(ExpectedConditions.stalenessOf(sortDropdown));
        sortDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product_sort_container")));
        assertEquals("lohi", sortDropdown.getAttribute("value"), "Sort should be set to low to high");

        // Test Price (high to low)
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='hilo']")).click();
        wait.until(ExpectedConditions.stalenessOf(sortDropdown));
        sortDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product_sort_container")));
        assertEquals("hilo", sortDropdown.getAttribute("value"), "Sort should be set to high to low");
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        loginIfNecessary();

        openMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        assertTrue(driver.getCurrentUrl().contains("inventory"), "Should navigate to inventory page");
    }

    @Test
    @Order(5)
    public void testMenuAbout() {
        loginIfNecessary();

        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open Sauce Labs website");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals(INVENTORY_URL, driver.getCurrentUrl(), "Should return to inventory page");
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        loginIfNecessary();

        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("login"));
        assertTrue(driver.getCurrentUrl().contains("login"), "Should navigate to login page after logout");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        loginIfNecessary();

        // Add item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item:first-child .btn_inventory")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart should have 1 item");

        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Wait for cart to be cleared
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, cartBadges.size(), "Cart should be empty after reset");
    }

    @Test
    @Order(8)
    public void testFooterTwitterLink() {
        loginIfNecessary();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open Twitter website");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals(INVENTORY_URL, driver.getCurrentUrl(), "Should return to inventory page");
    }

    @Test
    @Order(9)
    public void testFooterFacebookLink() {
        loginIfNecessary();

        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook")));
        String originalWindow = driver.getWindowHandle();
        facebookLink.click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open Facebook website");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals(INVENTORY_URL, driver.getCurrentUrl(), "Should return to inventory page");
    }

    @Test
    @Order(10)
    public void testFooterLinkedInLink() {
        loginIfNecessary();

        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin")));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open LinkedIn website");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
        assertEquals(INVENTORY_URL, driver.getCurrentUrl(), "Should return to inventory page");
    }

    private void loginIfNecessary() {
        if (driver.getCurrentUrl().contains("login") || driver.getCurrentUrl().equals(BASE_URL)) {
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory"));
        }
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu")));
    }
}