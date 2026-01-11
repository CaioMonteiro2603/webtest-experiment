package deepseek.ws01.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLoginSuccess() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(), "Inventory page should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"), "Error message should be displayed");
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        loginIfNeeded();
        driver.get(BASE_URL + "inventory.html");
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select.product_sort_container")));
        Select sortSelect = new Select(sortDropdown);

        // Test Name (A to Z)
        sortSelect.selectByVisibleText("Name (A to Z)");
        List<WebElement> itemsAZ = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        Assertions.assertTrue(itemsAZ.get(0).getText().compareTo(itemsAZ.get(1).getText()) <= 0, "Items should be sorted A-Z");

        // Test Name (Z to A)
        sortSelect.selectByVisibleText("Name (Z to A)");
        List<WebElement> itemsZA = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        Assertions.assertTrue(itemsZA.get(0).getText().compareTo(itemsZA.get(1).getText()) >= 0, "Items should be sorted Z-A");

        // Test Price (low to high)
        sortSelect.selectByVisibleText("Price (low to high)");
        List<WebElement> pricesLowHigh = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        double firstPrice = Double.parseDouble(pricesLowHigh.get(0).getText().replace("$", ""));
        double secondPrice = Double.parseDouble(pricesLowHigh.get(1).getText().replace("$", ""));
        Assertions.assertTrue(firstPrice <= secondPrice, "Prices should be sorted low to high");

        // Test Price (high to low)
        sortSelect.selectByVisibleText("Price (high to low)");
        List<WebElement> pricesHighLow = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        firstPrice = Double.parseDouble(pricesHighLow.get(0).getText().replace("$", ""));
        secondPrice = Double.parseDouble(pricesHighLow.get(1).getText().replace("$", ""));
        Assertions.assertTrue(firstPrice >= secondPrice, "Prices should be sorted high to low");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        loginIfNeeded();
        driver.get(BASE_URL + "inventory.html");
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button button")));
        menuButton.click();

        // Test All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(), "Inventory page should be displayed");

        // Test About (external)
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button button")));
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "Should be on Sauce Labs site");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Logout
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button button")));
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Should be back on login page");

        // Log back in for subsequent tests
        driver.findElement(By.id("user-name")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        loginIfNeeded();
        driver.get(BASE_URL + "inventory.html");
        String originalWindow = driver.getWindowHandle();

        // Test Twitter
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter a")));
        twitterLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should be on Twitter");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook a")));
        facebookLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Should be on Facebook");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin a")));
        linkedinLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "Should be on LinkedIn");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testAddRemoveItems() {
        loginIfNeeded();
        driver.get(BASE_URL + "inventory.html");
        resetAppState();

        // Add first item to cart
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn_inventory")));
        addButton.click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");

        // Remove item
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn_inventory")));
        removeButton.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(0, badges.size(), "Cart badge should be removed");
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));
            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void resetAppState() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button button")));
        menuButton.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOf(resetLink));
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-cross-button button")));
        closeButton.click();
    }

    private void switchToNewWindow() {
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
}