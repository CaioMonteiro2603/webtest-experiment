package SunaQwen3.ws01.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String LOGIN = "standard_user";
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
        if (driver != null) driver.quit();
    }

    /* -------------------- Helpers -------------------- */

    private void login(String username, String password) {
        WebElement usernameField =
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
    }

    private void navigateToInventory() {
        driver.get(BASE_URL);
        login(LOGIN, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    private void openMenu() {
        WebElement menuButton =
                wait.until(ExpectedConditions.elementToBeClickable(By.className("bm-burger-button")));
        menuButton.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replace("$", "").trim());
    }

    /* -------------------- Tests -------------------- */

    @Test
    @Order(1)
    public void testValidLoginSuccess() {
        driver.get(BASE_URL);
        login(LOGIN, PASSWORD);

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        WebElement inventory =
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        assertTrue(inventory.isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get(BASE_URL);
        login("invalid_user", PASSWORD);

        WebElement error =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".error-message-container")));
        assertTrue(error.getText().contains("Epic sadface"));
    }

    @Test
    @Order(3)
    public void testSortByNameAtoZ() {
        navigateToInventory();

        Select select = new Select(
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.className("product_sort_container"))));
        select.selectByValue("az");

        List<WebElement> items =
                driver.findElements(By.className("inventory_item_name"));
        assertTrue(items.size() > 0);
        assertTrue(items.get(0).getText()
                .compareTo(items.get(items.size() - 1).getText()) <= 0);
    }

    @Test
    @Order(4)
    public void testSortByPriceLowToHigh() {
        navigateToInventory();

        Select select = new Select(
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.className("product_sort_container"))));
        select.selectByValue("lohi");

        List<WebElement> prices =
                driver.findElements(By.className("inventory_item_price"));

        double previous = 0;
        for (WebElement p : prices) {
            double current = parsePrice(p.getText());
            assertTrue(current >= previous);
            previous = current;
        }
    }

    @Test
    @Order(5)
    public void testAddRemoveItemFromCart() {
        navigateToInventory();

        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("add-to-cart-sauce-labs-backpack"))).click();

        WebElement badge =
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.className("shopping_cart_badge")));
        assertEquals("1", badge.getText());

        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("remove-sauce-labs-backpack"))).click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.className("shopping_cart_badge")));
        assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty());
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        navigateToInventory();

        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("logout_sidebar_link"))).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl());
    }

    @Test
    @Order(7)
    public void testFooterTwitterLink() {
        navigateToInventory();

        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.className("social_twitter")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", twitterLink);

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String w : driver.getWindowHandles()) {
            if (!w.equals(originalWindow)) {
                driver.switchTo().window(w);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("twitter.com"));
        assertTrue(driver.getCurrentUrl().contains("twitter.com"));

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}