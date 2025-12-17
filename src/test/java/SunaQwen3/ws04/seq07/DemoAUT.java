package SunaQwen3.ws04.seq07;

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
public class DemoAUT {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String LOGIN_PAGE_URL = BASE_URL;
    private static final String INVENTORY_PAGE_URL = "inventory.html"; // Assuming relative path
    private static final String USERNAME = "demo";
    private static final String PASSWORD = "demo123";

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

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        // Assert successful navigation to inventory page
        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));
        assertTrue(driver.getCurrentUrl().contains(INVENTORY_PAGE_URL), "Should be redirected to inventory page after login");

        // Assert presence of inventory list (assuming table with id 'inventory')
        WebElement inventoryTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory")));
        assertTrue(inventoryTable.isDisplayed(), "Inventory table should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLoginCredentials() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        // Assert error message is displayed
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid credentials");
        assertTrue(errorMessage.getText().contains("Invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuAllItemsNavigation() {
        // Ensure we're on inventory page
        driver.get(LOGIN_PAGE_URL);
        performLogin();
        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-toggle")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();

        // Assert navigation to inventory page
        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));
        assertTrue(driver.getCurrentUrl().contains(INVENTORY_PAGE_URL), "Should navigate to inventory page when 'All Items' is clicked");
    }

    @Test
    @Order(4)
    public void testMenuAboutExternalLink() {
        // Ensure we're on inventory page
        driver.get(LOGIN_PAGE_URL);
        performLogin();
        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-toggle")));
        menuButton.click();

        // Click About (assumed to open in new tab)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain (e.g., saucelabs.com)
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open saucelabs.com domain");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testMenuLogout() {
        // Ensure we're on inventory page
        driver.get(LOGIN_PAGE_URL);
        performLogin();
        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-toggle")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();

        // Assert navigation back to login page
        wait.until(ExpectedConditions.urlToBe(LOGIN_PAGE_URL));
        assertEquals(LOGIN_PAGE_URL, driver.getCurrentUrl(), "Should return to login page after logout");
    }

    @Test
    @Order(6)
    public void testMenuResetAppState() {
        // Ensure we're on inventory page
        driver.get(LOGIN_PAGE_URL);
        performLogin();
        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));

        // Add an item to cart first to have state to reset
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("btn-add-to-cart")));
        if (!addToCartButtons.isEmpty()) {
            addToCartButtons.get(0).click();
        }

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-toggle")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetLink.click();

        // Close menu if needed
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-toggle")));
        menuButton.click(); // Close menu

        // Assert cart badge is gone (state reset)
        List<WebElement> cartBadges = driver.findElements(By.className("cart-badge"));
        assertEquals(0, cartBadges.size(), "Cart badge should be removed after resetting app state");
    }

    @Test
    @Order(7)
    public void testFooterTwitterLink() {
        // Ensure we're on inventory page
        driver.get(LOGIN_PAGE_URL);
        performLogin();
        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));

        // Find Twitter link in footer
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains twitter.com
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open twitter.com domain");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testFooterFacebookLink() {
        // Ensure we're on inventory page
        driver.get(LOGIN_PAGE_URL);
        performLogin();
        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));

        // Find Facebook link in footer
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='facebook.com']")));
        String originalWindow = driver.getWindowHandle();
        facebookLink.click();

        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains facebook.com
        wait.until(ExpectedConditions.urlContains("facebook.com"));
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open facebook.com domain");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testFooterLinkedInLink() {
        // Ensure we're on inventory page
        driver.get(LOGIN_PAGE_URL);
        performLogin();
        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));

        // Find LinkedIn link in footer
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='linkedin.com']")));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();

        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains linkedin.com
        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open linkedin.com domain");

        // Close new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testSortingDropdownOptions() {
        // Ensure we're on inventory page
        driver.get(LOGIN_PAGE_URL);
        performLogin();
        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));

        // Locate sorting dropdown
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));

        // Test each option
        String[] sortOptions = {"name", "price"}; // Assuming values like 'za', 'lohi', 'hilo'
        String[] sortValues = {"name", "price"}; // Actual values in dropdown

        for (int i = 0; i < sortOptions.length; i++) {
            // Re-locate dropdown to avoid stale element reference
            sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
            sortDropdown.click();

            // Select option by value
            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("option[value='" + sortValues[i] + "']")));
            option.click();

            // Assert the selected option is correct
            assertEquals(sortValues[i], sortDropdown.getAttribute("value"), "Sorting should be set to " + sortOptions[i]);
        }
    }

    /**
     * Helper method to perform login
     */
    private void performLogin() {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        usernameField.clear();
        passwordField.clear();

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains(INVENTORY_PAGE_URL));
    }
}