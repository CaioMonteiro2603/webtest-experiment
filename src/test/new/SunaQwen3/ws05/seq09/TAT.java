package SunaQwen3.ws05.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
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
        driver.get(BASE_URL);
        login(USERNAME, PASSWORD);

        // Assert successful login by checking URL and presence of inventory container
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL should contain inventory.html after login");

        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertNotNull(inventoryContainer, "Inventory container should be visible after login");
    }

    @Test
    @Order(2)
    public void testInvalidCredentialsError() {
        driver.get(BASE_URL);

        login("invalid_user", "invalid_password");

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        assertTrue(errorMessage.isDisplayed(), "Error message container should be displayed");
        assertTrue(errorMessage.getText().contains("Username and password do not match"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testLockedUserLogin() {
        driver.get(BASE_URL);

        login("locked_out_user", "secret_sauce");

        // Wait for error message to appear
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        assertTrue(errorMessage.isDisplayed(), "Error message container should be displayed");
        assertTrue(errorMessage.getText().contains("Sorry, this user has been locked out"),
                "Error message should indicate user is locked out");
    }

    @Test
    @Order(4)
    public void testSortingDropdownOptions() {
        performLoginIfNecessary();

        // Get the sorting dropdown
        WebElement sortSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".product_sort_container")));
        String[] expectedOptions = {"za", "az", "lohi", "hilo"};

        for (String optionValue : expectedOptions) {
            // Re-locate the element to avoid stale reference
            sortSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".product_sort_container")));
            sortSelect.click();

            // Select the option
            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("option[value='" + optionValue + "']")));
            option.click();

            // Wait for sorting to take effect
            wait.until(ExpectedConditions.attributeToBe(sortSelect, "value", optionValue));

            // Verify the sorting by checking first and last product names or prices
            List<WebElement> products = driver.findElements(By.cssSelector(".inventory_item"));
            assertTrue(products.size() > 0, "There should be products displayed after sorting");

            if (optionValue.equals("az") || optionValue.equals("za")) {
                // Name-based sorting
                String firstProductName = driver.findElement(By.cssSelector(".inventory_item_name")).getText();
                String lastProductName = driver.findElements(By.cssSelector(".inventory_item_name")).get(products.size() - 1).getText();
                if (optionValue.equals("az")) {
                    assertTrue(firstProductName.compareTo(lastProductName) <= 0,
                            "Products should be sorted from A to Z");
                } else {
                    assertTrue(firstProductName.compareTo(lastProductName) >= 0,
                            "Products should be sorted from Z to A");
                }
            } else if (optionValue.equals("lohi") || optionValue.equals("hilo")) {
                // Price-based sorting
                List<WebElement> priceElements = driver.findElements(By.cssSelector(".inventory_item_price"));
                double firstPrice = parsePrice(priceElements.get(0).getText());
                double lastPrice = parsePrice(priceElements.get(priceElements.size() - 1).getText());
                if (optionValue.equals("lohi")) {
                    assertTrue(firstPrice <= lastPrice,
                            "Products should be sorted from low to high price");
                } else {
                    assertTrue(firstPrice >= lastPrice,
                            "Products should be sorted from high to low price");
                }
            }
        }
    }

    @Test
    @Order(5)
    public void testMenuAllItems() {
        performLoginIfNecessary();

        openMenu();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();

        // Wait for navigation
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should navigate to inventory page");
    }

    @Test
    @Order(6)
    public void testMenuAboutExternalLink() {
        performLoginIfNecessary();

        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Wait for new tab to open
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        // Get window handles
        Set<String> windowHandles = driver.getWindowHandles();
        String originalHandle = driver.getWindowHandle();
        String newHandle = windowHandles.stream().filter(handle -> !handle.equals(originalHandle)).findFirst().orElse(null);
        assertNotNull(newHandle, "New tab should have been opened");

        // Switch to new tab
        driver.switchTo().window(newHandle);

        // Assert URL contains expected domain
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("saucelabs.com"), "About link should redirect to saucelabs.com");

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalHandle);

        // Verify back on original page
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should be back on inventory page");
    }

    @Test
    @Order(7)
    public void testMenuLogout() {
        performLoginIfNecessary();

        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        // Wait for login page
        wait.until(ExpectedConditions.urlContains("index.html"));
        assertTrue(driver.getCurrentUrl().contains("index.html"), "Should navigate to login page after logout");

        // Verify login elements are present
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        assertNotNull(loginButton, "Login button should be visible on login page");
    }

    @Test
    @Order(8)
    public void testMenuResetAppState() {
        performLoginIfNecessary();

        // Add an item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn_inventory")));
        addToCartButton.click();

        // Verify cart badge appears with count
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Open menu and reset app state
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Close menu (wait for it to disappear)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("menu_button")));

        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        assertTrue(cartBadges.isEmpty() || !cartBadges.get(0).isDisplayed(),
                "Cart badge should not be displayed after reset");
    }

    @Test
    @Order(9)
    public void testFooterSocialLinks() {
        performLoginIfNecessary();

        // Find all footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".social a"));
        assertEquals(3, socialLinks.size(), "There should be 3 social links in the footer");

        String[] expectedDomains = {"twitter.com", "facebook.com", "linkedin.com"};
        String originalHandle = driver.getWindowHandle();

        for (int i = 0; i < socialLinks.size(); i++) {
            // Re-locate the element to avoid stale reference
            socialLinks = driver.findElements(By.cssSelector(".social a"));
            WebElement link = socialLinks.get(i);

            link.click();

            // Wait for new tab
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));

            // Switch to new tab
            Set<String> windowHandles = driver.getWindowHandles();
            String newHandle = windowHandles.stream().filter(handle -> !handle.equals(originalHandle)).findFirst().orElse(null);
            assertNotNull(newHandle, "New tab should have been opened");

            driver.switchTo().window(newHandle);

            // Assert URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains(expectedDomains[i]),
                    "Social link should redirect to " + expectedDomains[i]);

            // Close new tab and switch back
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    private void login(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.clear();
        usernameField.sendKeys(username);

        passwordField.clear();
        passwordField.sendKeys(password);

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

        // Wait for menu to open
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        assertTrue(closeButton.isDisplayed(), "Menu should be open");
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replaceAll("[^\\d.]", ""));
    }
}