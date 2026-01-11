package SunaDeepSeek.ws01.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@TestMethodOrder(OrderAnnotation.class)
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
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), 
            "Login should redirect to inventory page");
        Assertions.assertTrue(driver.findElements(By.className("inventory_list")).size() > 0,
            "Inventory list should be visible after login");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));
        
        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();
        
        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"),
            "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        login();
        
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='az']")).click();
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        List<String> itemNames = new ArrayList<>();
        for (WebElement item : items) {
            itemNames.add(item.getText());
        }
        List<String> sortedNames = new ArrayList<>(itemNames);
        Collections.sort(sortedNames);
        Assertions.assertEquals(sortedNames, itemNames, "Items should be sorted A-Z");
        
        // Test Name (Z to A)
        sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='za']")).click();
        items = driver.findElements(By.cssSelector(".inventory_item_name"));
        itemNames.clear();
        for (WebElement item : items) {
            itemNames.add(item.getText());
        }
        sortedNames = new ArrayList<>(itemNames);
        sortedNames.sort(Comparator.reverseOrder());
        Assertions.assertEquals(sortedNames, itemNames, "Items should be sorted Z-A");
        
        // Test Price (low to high)
        sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='lohi']")).click();
        List<WebElement> priceElements = driver.findElements(By.cssSelector(".inventory_item_price"));
        List<Double> prices = new ArrayList<>();
        for (WebElement priceElement : priceElements) {
            prices.add(Double.parseDouble(priceElement.getText().replace("$", "")));
        }
        List<Double> sortedPrices = new ArrayList<>(prices);
        Collections.sort(sortedPrices);
        Assertions.assertEquals(sortedPrices, prices, "Items should be sorted by price low to high");
        
        // Test Price (high to low)
        sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".product_sort_container")));
        sortDropdown.click();
        driver.findElement(By.cssSelector("option[value='hilo']")).click();
        priceElements = driver.findElements(By.cssSelector(".inventory_item_price"));
        prices.clear();
        for (WebElement priceElement : priceElements) {
            prices.add(Double.parseDouble(priceElement.getText().replace("$", "")));
        }
        sortedPrices = new ArrayList<>(prices);
        sortedPrices.sort(Comparator.reverseOrder());
        Assertions.assertEquals(sortedPrices, prices, "Items should be sorted by price high to low");
    }

    @Test
    @Order(4)
    public void testMenuOptions() {
        login();
        
        // Close menu if open
        try {
            WebElement closeButton = driver.findElement(By.cssSelector(".bm-cross-button button"));
            if (closeButton.isDisplayed()) {
                closeButton.click();
                Thread.sleep(500);
            }
        } catch (Exception e) {
            // Menu not open, continue
        }
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
            "Clicking All Items should show inventory page");
        
        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test About (external link)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("about_sidebar_link")));
        aboutLink.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
            "About link should open saucelabs.com");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        resetLink.click();
        
        // Verify reset by checking cart is empty
        Assertions.assertEquals(0, driver.findElements(By.cssSelector(".shopping_cart_badge")).size(),
            "Cart should be empty after reset");
        
        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logoutLink.click();
        
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
            "Should be logged out and on login page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        
        // Test Twitter link
        testExternalLink("social_twitter", "twitter.com");
        
        // Test Facebook link
        testExternalLink("social_facebook", "facebook.com");
        
        // Test LinkedIn link
        testExternalLink("social_linkedin", "linkedin.com");
    }

    private void testExternalLink(String linkId, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[id*='" + linkId + "']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
        Thread.sleep(1000);
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "Link should open " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void login() {
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