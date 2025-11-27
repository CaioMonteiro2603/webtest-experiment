package SunaQwen3.ws04.seq06;

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
public class WebUITestSuite {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String LOGIN_PAGE_URL = BASE_URL;
    private static final String INVENTORY_PAGE_URL = "https://katalon-test.s3.amazonaws.com/aut/inventory.html";
    private static final String ABOUT_PAGE_URL = "https://saucelabs.com/";
    private static final String USERNAME = "katalon";
    private static final String PASSWORD = "katalon123";

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
    public void testValidLoginSuccess() {
        driver.get(LOGIN_PAGE_URL);
        assertTrue(driver.getCurrentUrl().contains("form.html"), "Should be on login page");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should navigate to inventory page after login");
        assertTrue(driver.getTitle().contains("Inventory"), "Page title should contain 'Inventory'");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get(LOGIN_PAGE_URL);
        assertTrue(driver.getCurrentUrl().contains("form.html"), "Should be on login page");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed");
        assertTrue(errorMessage.getText().contains("Please check your username and password"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        // Ensure we're logged in
        loginIfNecessary();

        // Verify sorting dropdown exists
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        String initialOption = sortDropdown.getAttribute("value");
        assertEquals("az", initialOption, "Default sort should be A to Z");

        // Test each sorting option
        selectAndVerifySort("za", "Z to A");
        selectAndVerifySort("lohi", "Low to High");
        selectAndVerifySort("hilo", "High to Low");
    }

    private void selectAndVerifySort(String value, String description) {
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown.click();
        
        // Wait for dropdown to be interactive and select option
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("option[value='" + value + "']"))).click();

        // Verify selection took effect
        WebElement selectedOption = driver.findElement(By.cssSelector("option[value='" + value + "']:checked"));
        assertEquals(value, selectedOption.getAttribute("value"), "Sort should change to " + description);
    }

    @Test
    @Order(4)
    public void testMenuNavigationAndResetAppState() {
        loginIfNecessary();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should return to inventory page");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();

        // Click About (external link)
        String originalWindow = driver.getWindowHandle();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should open Sauce Labs website");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Reopen menu
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetLink.click();

        // Wait for potential refresh or state reset
        wait.until(ExpectedConditions.stalenessOf(menuButton));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));

        // Verify inventory items are still present
        List<WebElement> inventoryItems = driver.findElements(By.className("inventory_item"));
        assertTrue(inventoryItems.size() > 0, "Inventory items should still be visible after reset");

        // Close menu
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        loginIfNecessary();

        // Store original window
        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        testExternalLink(By.linkText("Twitter"), "twitter.com");

        // Test Facebook link
        testExternalLink(By.linkText("Facebook"), "facebook.com");

        // Test LinkedIn link
        testExternalLink(By.linkText("LinkedIn"), "linkedin.com");

        // Ensure we're back to main window
        driver.switchTo().window(originalWindow);
        assertEquals(INVENTORY_PAGE_URL, driver.getCurrentUrl(), "Should return to inventory page after testing external links");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();

        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert domain
        assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                   "External link should navigate to domain containing: " + expectedDomain);

        // Close external tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testLogoutFunctionality() {
        loginIfNecessary();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();

        // Wait for navigation to login page
        wait.until(ExpectedConditions.urlContains("form.html"));
        assertTrue(driver.getCurrentUrl().contains("form.html"), "Should return to login page after logout");
        assertTrue(driver.getTitle().contains("Login"), "Should be on login page after logout");
    }

    private void loginIfNecessary() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(LOGIN_PAGE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

            usernameField.clear();
            passwordField.clear();
            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }
}