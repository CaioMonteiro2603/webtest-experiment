package SunaQwen3.ws06.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;


@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String LOGIN_PAGE_URL = BASE_URL + "#/login";
    private static final String INVENTORY_PAGE_URL = BASE_URL + "#/inventory";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

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

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("book"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("book"), "Should be redirected to book page after login");

        WebElement roomContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-type='room']")));
        Assertions.assertTrue(roomContainer.isDisplayed(), "Room container should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(errorMessage.getText().contains("must"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        navigateToInventory();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select.form-control")));
        
        // Option: Name (A to Z)
        sortDropdown.click();
        WebElement optionAZ = driver.findElement(By.cssSelector("option[value='1']"));
        optionAZ.click();
        verifyFirstItemName("Apple");

        // Option: Name (Z to A)
        sortDropdown.click();
        WebElement optionZA = driver.findElement(By.cssSelector("option[value='2']"));
        optionZA.click();
        verifyFirstItemName("Zebra");

        // Option: Price (low to high)
        sortDropdown.click();
        WebElement optionLoHi = driver.findElement(By.cssSelector("option[value='3']"));
        optionLoHi.click();
        verifyFirstItemPrice("10");

        // Option: Price (high to low)
        sortDropdown.click();
        WebElement optionHiLo = driver.findElement(By.cssSelector("option[value='4']"));
        optionHiLo.click();
        verifyFirstItemPrice("99");
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        navigateToInventory();

        openMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-menu-item='rooms']")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("book"));
        WebElement roomContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-type='room']")));
        Assertions.assertTrue(roomContainer.isDisplayed(), "Room container should be displayed after clicking All Items");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        navigateToInventory();

        openMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-menu-item='about']")));
        aboutLink.click();

        // Switch to new tab
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open saucelabs.com");

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);

        // Verify we're back on the inventory page
        wait.until(ExpectedConditions.urlContains("book"));
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        navigateToInventory();

        openMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-menu-item='logout']")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("login"));
        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        Assertions.assertTrue(loginForm.isDisplayed(), "Login form should be displayed after logout");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        navigateToInventory();

        // Add an item to cart first
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-type='room'] button")));
        addToCartButton.click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart should have 1 item before reset");

        // Open menu and reset app state
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-menu-item='reset']")));
        resetLink.click();

        // Close menu
        WebElement closeMenuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".close-menu")));
        closeMenuButton.click();

        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".badge"));
        Assertions.assertTrue(cartBadges.isEmpty() || !cartBadges.get(0).isDisplayed(), "Cart should be empty after reset app state");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        navigateToInventory();

        // Twitter link
        testExternalLink(By.cssSelector("[data-testid='twitter-link']"), "twitter.com");

        // Facebook link
        testExternalLink(By.cssSelector("[data-testid='facebook-link']"), "facebook.com");

        // LinkedIn link
        testExternalLink(By.cssSelector("[data-testid='linkedin-link']"), "linkedin.com");
    }

    private void navigateToInventory() {
        if (!driver.getCurrentUrl().contains("book")) {
            driver.get(INVENTORY_PAGE_URL);
            wait.until(ExpectedConditions.urlContains("book"));
        }
        // Ensure app state is clean
        resetAppState();
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".menu-toggle")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".menu-open")));
    }

    private void resetAppState() {
        if (driver.getCurrentUrl().contains("book")) {
            try {
                openMenu();
                WebElement resetLink = driver.findElement(By.cssSelector("[data-menu-item='reset']"));
                resetLink.click();
                WebElement closeMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".close-menu")));
                closeMenu.click();
            } catch (Exception e) {
                // Menu might not be open or reset option not available
            }
        }
    }

    private void verifyFirstItemName(String expectedName) {
        List<WebElement> itemNames = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".room-title")));
        Assertions.assertFalse(itemNames.isEmpty(), "There should be at least one item in the inventory");
        Assertions.assertEquals(expectedName, itemNames.get(0).getText(), "First item name should match the expected name after sorting");
    }

    private void verifyFirstItemPrice(String expectedPrice) {
        List<WebElement> itemPrices = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".room-price")));
        Assertions.assertFalse(itemPrices.isEmpty(), "There should be at least one item with a price");
        Assertions.assertEquals(expectedPrice, itemPrices.get(0).getText(), "First item price should match the expected price after sorting");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        String originalWindow = driver.getWindowHandle();

        link.click();

        // Wait for new window and switch
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert domain
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should open the expected domain");

        // Close and return
        driver.close();
        driver.switchTo().window(originalWindow);
        wait.until(ExpectedConditions.urlContains("book"));
    }
}