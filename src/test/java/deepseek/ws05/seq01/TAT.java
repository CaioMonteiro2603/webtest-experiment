package deepseek.ws05.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class WebAppTest {
    private static WebDriver driver;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";
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

    @Test
    @Order(1)
    public void testLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Login failed - not redirected to inventory page");
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0, "Inventory items not displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginAndGoToInventory();

        Select sortDropdown = new Select(driver.findElement(By.className("product_sort_container")));

        // Test Name (A to Z)
        sortDropdown.selectByValue("az");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().compareTo(items.get(1).getText()) < 0, "Items not sorted A-Z");

        // Test Name (Z to A)
        sortDropdown.selectByValue("za");
        items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().compareTo(items.get(1).getText()) > 0, "Items not sorted Z-A");

        // Test Price (low to high)
        sortDropdown.selectByValue("lohi");
        List<WebElement> prices = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(Double.parseDouble(prices.get(0).getText().substring(1)) <= 
                            Double.parseDouble(prices.get(1).getText().substring(1)), "Prices not sorted low to high");

        // Test Price (high to low)
        sortDropdown.selectByValue("hilo");
        prices = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(Double.parseDouble(prices.get(0).getText().substring(1)) >= 
                            Double.parseDouble(prices.get(1).getText().substring(1)), "Prices not sorted high to low");
    }

    @Test
    @Order(4)
    public void testMenuButtonFunctions() {
        loginAndGoToInventory();

        // Open menu
        WebElement menuButton = driver.findElement(By.id("react-burger-menu-btn"));
        menuButton.click();

        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "All Items link not working");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Test About link (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link not opening external page");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        // Test Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        WebElement closeButton = driver.findElement(By.id("react-burger-cross-btn"));
        closeButton.click();

        Assertions.assertEquals(0, driver.findElements(By.className("shopping_cart_badge")).size(), "Cart not reset");

        // Open menu and logout
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Logout not successful");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));

        // Test Twitter link
        testExternalLink("social_twitter", "twitter.com");

        // Test Facebook link
        testExternalLink("social_facebook", "facebook.com");

        // Test LinkedIn link
        testExternalLink("social_linkedin", "linkedin.com");
    }

    private void testExternalLink(String linkId, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = driver.findElement(By.id(linkId));
        link.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
                           "Link " + linkId + " not pointing to " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginAndGoToInventory() {
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