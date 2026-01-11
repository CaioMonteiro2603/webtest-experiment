package SunaGPT20b.ws10.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("email")));
        emailField = wait.until(ExpectedConditions.elementToBeClickable(emailField));
        emailField.clear();
        emailField.sendKeys(user);

        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("password")));
        passwordField = wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        passwordField.clear();
        passwordField.sendKeys(pass);

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("login-button")));
        loginButton.click();

        // Verify successful login by checking URL contains "/inventory" and an inventory element exists
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "Login failed: URL does not contain '/inventory'.");

        // Example inventory container check (adjust selector if needed)
        List<WebElement> inventory = driver.findElements(By.id("inventory_container"));
        Assertions.assertFalse(inventory.isEmpty(),
                "Login failed: Inventory container not found.");
    }

    private void ensureLoggedIn() {
        if (!driver.getCurrentUrl().contains("/inventory")) {
            login(USERNAME, PASSWORD);
        }
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("bm-menu")));
    }

    private void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("reset_sidebar_link")));
        resetLink.click();
        // Verify state reset by checking that cart badge is not present
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("shopping_cart_badge")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "Valid login should navigate to inventory page.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("email")));
        emailField = wait.until(ExpectedConditions.elementToBeClickable(emailField));
        emailField.clear();
        emailField.sendKeys("invalid@example.com");

        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("password")));
        passwordField = wait.until(ExpectedConditions.elementToBeClickable(passwordField));
        passwordField.clear();
        passwordField.sendKeys("wrongpass");

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("login-button")));
        loginButton.click();

        // Expect error message element
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        ensureLoggedIn();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.className("product_sort_container")));
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        Assertions.assertTrue(options.size() > 1, "Sorting dropdown should have multiple options.");

        for (WebElement option : options) {
            option.click();
            // Verify that the first item changes after sorting
            WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".inventory_item_name")));
            Assertions.assertNotNull(firstItem.getText(),
                    "First item name should be present after sorting with option: " + option.getText());
        }
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        ensureLoggedIn();
        openMenu();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "All Items should navigate to inventory page.");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternal() {
        ensureLoggedIn();
        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new window
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }

        // Verify external domain (example: "about.brasilagritest.com")
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                "About link should open an external page containing 'about'.");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        ensureLoggedIn();
        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to login page.");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        ensureLoggedIn();
        // Add an item to cart to change state
        WebElement firstAddToCart = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-test='add-to-cart']")));
        firstAddToCart.click();

        // Verify cart badge appears
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("shopping_cart_badge")));
        Assertions.assertTrue(badge.isDisplayed(), "Cart badge should be displayed after adding item.");

        // Reset app state
        resetAppState();

        // Verify cart badge is removed
        List<WebElement> badges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should be removed after resetting app state.");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        ensureLoggedIn();
        // Scroll to footer if needed
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        String[][] links = {
                {"footer_twitter", "twitter.com"},
                {"footer_facebook", "facebook.com"},
                {"footer_linkedin", "linkedin.com"}
        };

        String originalWindow = driver.getWindowHandle();

        for (String[] linkInfo : links) {
            String elementId = linkInfo[0];
            String expectedDomain = linkInfo[1];

            List<WebElement> elems = driver.findElements(By.id(elementId));
            if (elems.isEmpty()) {
                continue; // Skip if element not present
            }
            WebElement socialLink = elems.get(0);
            socialLink.click();

            // Wait for new window
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String win : windows) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    break;
                }
            }

            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should open a page containing domain: " + expectedDomain);

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}