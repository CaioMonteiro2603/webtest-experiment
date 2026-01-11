package SunaQwen3.ws05.seq03;

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
    private static final String LOGIN_PAGE_URL = BASE_URL;;
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
        driver.get(LOGIN_PAGE_URL);
        driver.findElement(By.id("user-name")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be redirected to inventory page after login");
        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertTrue(inventoryContainer.isDisplayed(), "Inventory container should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(LOGIN_PAGE_URL);
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("invalid_password");
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginButton.click();
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message-container")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
        assertTrue(errorMessage.getText().contains("Username and password do not match"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testSortingDropdownOptions() {
        performLoginIfNecessary();
        WebElement sortDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".product_sort_container")));
        String[] expectedOptions = {"az", "za", "lohi", "hilo"};
        String[] optionValues = {"", "za", "lohi", "hilo"};

        for (int i = 0; i < expectedOptions.length; i++) {
            sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".product_sort_container")));
            sortDropdown.click();
            sortDropdown.sendKeys(expectedOptions[i]);
            sortDropdown.sendKeys(Keys.RETURN);
            wait.until(ExpectedConditions.attributeContains(By.cssSelector(".product_sort_container"), "value", optionValues[i]));
            assertTrue(driver.findElement(By.cssSelector(".product_sort_container")).getAttribute("value").contains(optionValues[i]), 
                "Sort dropdown should reflect selected option: " + optionValues[i]);
        }
    }

    @Test
    @Order(4)
    void testMenuAllItems() {
        performLoginIfNecessary();
        openMenuAndClick("Open Menu");
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Clicking 'All Items' should return to inventory page");
    }

    @Test
    @Order(5)
    void testMenuAboutExternalLink() {
        performLoginIfNecessary();
        openMenuAndClick("Open Menu");
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
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open Sauce Labs website");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    void testMenuLogout() {
        performLoginIfNecessary();
        openMenuAndClick("Open Menu");
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(LOGIN_PAGE_URL));
        assertTrue(driver.getCurrentUrl().contains("index.html"), "Logout should redirect to login page");
        assertTrue(driver.findElement(By.id("login-button")).isDisplayed(), "Login button should be visible after logout");
    }

    @Test
    @Order(7)
    void testMenuResetAppState() {
        performLoginIfNecessary();
        // Add an item to cart first
        List<WebElement> addToCartButtons = driver.findElements(By.cssSelector(".btn_inventory"));
        if (addToCartButtons.size() > 0) {
            addToCartButtons.get(0).click();
            WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
            assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after adding first item");
        }
        openMenuAndClick("Open Menu");
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector(".shopping_cart_badge"))));
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertEquals(0, cartBadges.size(), "Cart badge should disappear after reset app state");
    }

    @Test
    @Order(8)
    void testFooterSocialLinks() {
        performLoginIfNecessary();
        String originalWindow = driver.getWindowHandle();
        String[] linkIds = {"twitter", "facebook", "linkedin"};
        String[] expectedDomains = {"twitter.com", "facebook.com", "linkedin.com"};

        for (int i = 0; i < linkIds.length; i++) {
            WebElement socialLink = driver.findElement(By.cssSelector("#" + linkIds[i] + " > svg"));
            socialLink.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            wait.until(ExpectedConditions.urlContains(expectedDomains[i]));
            assertTrue(driver.getCurrentUrl().contains(expectedDomains[i]), 
                "Social link " + linkIds[i] + " should open corresponding domain: " + expectedDomains[i]);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    private void performLoginIfNecessary() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(LOGIN_PAGE_URL);
            driver.findElement(By.id("user-name")).sendKeys(USERNAME);
            driver.findElement(By.id("password")).sendKeys(PASSWORD);
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void openMenuAndClick(String menuButtonText) {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout_sidebar_link")));
    }
}