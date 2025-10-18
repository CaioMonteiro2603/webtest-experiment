package GPT20b.ws01.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class SauceDemoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setUpDriver() {
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

    /* ---------- Helper Methods ---------- */

    private void performLogin() {
        driver.get(BASE_URL);
        WebElement userInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passInput = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userInput.clear();
        userInput.sendKeys(USERNAME);
        passInput.clear();
        passInput.sendKeys(PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Login did not redirect to inventory page");
    }

    private void performLogout() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Logout did not redirect to login page");
    }

    private void resetAppStateAndConfirm() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Wait for inventory items to be visible again
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item")));
        Assertions.assertTrue(driver.findElements(By.cssSelector(".inventory_item")).size() > 0,
                "Reset App State did not show inventory items");
    }

    private String getCurrentWindowHandle() {
        return driver.getWindowHandle();
    }

    private void closeOtherWindows(String originalHandle) {
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }
        driver.switchTo().window(originalHandle);
    }

    private List<Double> getAllPrices() {
        List<WebElement> priceElements = driver.findElements(By.cssSelector(".inventory_item_price"));
        List<Double> prices = new ArrayList<>();
        for (WebElement el : priceElements) {
            String text = el.getText().replace("$", "").trim();
            try {
                prices.add(Double.parseDouble(text));
            } catch (NumberFormatException {
            }
        }
        return prices;
    }

    /* ---------- Test Cases ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        performLogin();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        performLogout(); // Ensure we are on login page
        driver.get(BASE_URL);
        WebElement userInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passInput = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userInput.clear();
        userInput.sendKeys("invalid_user");
        passInput.clear();
        passInput.sendKeys("wrong_pass");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error")));
        Assertions.assertTrue(errorMsg.getText().contains("Username and password do not match"),
                "Error message not displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        performLogin();
        By sortDropdown = By.id("product_sort_container");
        By firstItemName = By.cssSelector(".inventory_item_name");

        String[] options = {"lohi", "hilo", "za", "az"};
        String[] values = {"lohi", "hilo", "za", "az"};
        String previousName = "";

        for (int i = 0; i < options.length; i++) {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
            dropdown.findElement(By.xpath(String.format(".//option[@value='%s']", values[i]))).click();

            WebElement firstNameEl = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItemName));
            String currentName = firstNameEl.getText();

            if (!previousName.isEmpty()) {
                Assertions.assertNotEquals(previousName, currentName,
                        "Sorting option did not change the first item");
            }
            previousName = currentName;
        }
    }

    @Test
    @Order(4)
    public void testBurgerMenuAllItems() {
        performLogin();
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Burger menu All Items did not navigate to inventory page");
    }

    @Test
    @Order(5)
    public void testBurgerMenuAboutExternalLink() {
        performLogin();
        String originalHandle = getCurrentWindowHandle();

        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        // Handle new tab
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains("about"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                        "About link did not open expected external URL");
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(6)
    public void testBurgerMenuLogout() {
        performLogin();
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Logout did not navigate back to login page");
    }

    @Test
    @Order(7)
    public void testBurgerMenuResetAppState() {
        performLogin();
        resetAppStateAndConfirm();
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        performLogin();
        // Tweets
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='twitter.com']")));
        String originalHandle = getCurrentWindowHandle();
        twitterLink.click();
        closeOtherWindows(originalHandle);
        driver.switchTo().window(originalHandle);
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"),
                "Twitter link did not navigate correctly");

        // Facebook
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        closeOtherWindows(originalHandle);
        driver.switchTo().window(originalHandle);
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"),
                "Facebook link did not navigate correctly");

        // LinkedIn
        WebElement linkedInLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='linkedin.com']")));
        linkedInLink.click();
        closeOtherWindows(originalHandle);
        driver.switchTo().window(originalHandle);
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"),
                "LinkedIn link did not navigate correctly");
    }

    @Test
    @Order(9)
    public void testAddToCartAndCheckout() {
        performLogin();
        // Add first item to cart
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector("button[id^='add-to-cart']")));
        Assertions.assertFalse(addToCartButtons.isEmpty(), "No add-to-cart buttons found");
        addToCartButtons.get(0).click();

        // Verify cart badge
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge not updated to 1");

        // Go to cart
        WebElement cartLink = driver.findElement(By.id("shopping_cart_container"));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));

        // Fill form
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));

        // Finish
        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));

        WebElement thankYouHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", thankYouHeader.getText().trim(),
                "Checkout completion message not found");

        // Return to inventory and logout to reset state
        driver.navigate().to("https://www.saucedemo.com/v1/index.html");
        performLogout();
    }
}