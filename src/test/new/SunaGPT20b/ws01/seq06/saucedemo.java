package SunaGPT20b.ws01.seq06;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    private static WebDriver driver;
    private static WebDriverWait wait;

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
        WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.clear();
        usernameField.sendKeys(user);

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(pass);

        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();
    }

    private void resetAppStateIfNeeded() {
        // Open menu
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click Reset App State
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Close menu
        WebElement closeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();
    }

    private void switchToNewWindowAndBack() {
        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }
        // After verification, close and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "URL should contain /inventory.html after successful login");

        WebElement inventoryContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(),
                "Inventory container should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.clear();
        usernameField.sendKeys("invalid_user");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys("wrong_password");

        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));

        // Price (low to high)
        WebElement sortDropdown1 = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("select.product_sort_container")));
        Select select1 = new Select(sortDropdown1);
        select1.selectByValue("lohi");
        List<WebElement> pricesLowToHigh = driver.findElements(By.cssSelector(".inventory_item_price"));
        double firstPrice = Double.parseDouble(pricesLowToHigh.get(0).getText().replace("$", ""));
        double secondPrice = Double.parseDouble(pricesLowToHigh.get(1).getText().replace("$", ""));
        Assertions.assertTrue(firstPrice <= secondPrice,
                "First item price should be less or equal to second after low-to-high sort");

        // Price (high to low)
        WebElement sortDropdown2 = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("select.product_sort_container")));
        Select select2 = new Select(sortDropdown2);
        select2.selectByValue("hilo");
        List<WebElement> pricesHighToLow = driver.findElements(By.cssSelector(".inventory_item_price"));
        double firstHigh = Double.parseDouble(pricesHighToLow.get(0).getText().replace("$", ""));
        double secondHigh = Double.parseDouble(pricesHighToLow.get(1).getText().replace("$", ""));
        Assertions.assertTrue(firstHigh >= secondHigh,
                "First item price should be greater or equal to second after high-to-low sort");

        // Name (A to Z)
        WebElement sortDropdown3 = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("select.product_sort_container")));
        Select select3 = new Select(sortDropdown3);
        select3.selectByValue("az");
        List<WebElement> namesAz = driver.findElements(By.cssSelector(".inventory_item_name"));
        String firstAz = namesAz.get(0).getText();
        String secondAz = namesAz.get(1).getText();
        Assertions.assertTrue(firstAz.compareTo(secondAz) <= 0,
                "First item name should be alphabetically before second after A-Z sort");

        // Name (Z to A)
        WebElement sortDropdown4 = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("select.product_sort_container")));
        Select select4 = new Select(sortDropdown4);
        select4.selectByValue("za");
        List<WebElement> namesZa = driver.findElements(By.cssSelector(".inventory_item_name"));
        String firstZa = namesZa.get(0).getText();
        String secondZa = namesZa.get(1).getText();
        Assertions.assertTrue(firstZa.compareTo(secondZa) >= 0,
                "First item name should be alphabetically after second after Z-A sort");

        resetAppStateIfNeeded();
    }

    @Test
    @Order(4)
    public void testMenuNavigationAndLogout() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));

        // Open menu
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click All Items
        WebElement allItemsLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Should stay on inventory page after clicking All Items");

        // Close menu first
        WebElement closeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();

        // Open menu again for About
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click About (external)
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Switch to new tab and verify domain
        switchToNewWindowAndBack();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Should return to inventory page after closing About tab");

        // Close menu first
        closeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();

        // Open menu again for Logout
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/index.html"),
                "Should be back on login page after logout");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));

        // Twitter
        WebElement twitterLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("li.social_twitter a")));
        twitterLink.click();
        switchToNewWindowAndBack();
        // Verify we are back on the original page
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Should return to inventory after Twitter link");

        // Facebook
        WebElement facebookLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("li.social_facebook a")));
        facebookLink.click();
        switchToNewWindowAndBack();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Should return to inventory after Facebook link");

        // LinkedIn
        WebElement linkedInLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("li.social_linkedin a")));
        linkedInLink.click();
        switchToNewWindowAndBack();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Should return to inventory after LinkedIn link");

        // Clean up any added items
        resetAppStateIfNeeded();
    }
}