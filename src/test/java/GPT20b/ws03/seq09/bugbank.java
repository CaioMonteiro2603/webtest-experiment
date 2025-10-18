package GPT20b.ws03.seq09;

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
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class BugbankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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
        By userInput = By.id");
        By passInput = By.id("password");
        By loginBtn = By.id("login-button");

        wait.until(ExpectedConditions.elementToBeClickable(userInput)).clear();
        driver.findElement(userInput).sendKeys(USERNAME);
        driver.findElement(passInput).clear();
        driver.findElement(passInput).sendKeys(PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Login did not redirect to inventory page");
        Assertions.assertFalse(driver.findElements(By.cssSelector(".inventory_item")).isEmpty(),
                "Inventory items not found after login");
    }

    private void performLogout() {
        By logoutLink = By.id("logout_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                "Logout did not redirect to login page");
    }

    private void resetAppStateAndConfirm() {
        By resetLink = By.id("reset_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(resetLink)).click();
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item")));
        Assertions.assertFalse(driver.findElements(By.cssSelector(".inventory_item")).isEmpty(),
                "Reset App State did not display inventory items");
    }

    private String getCurrentWindowHandle() {
        return driver.getWindowHandle();
    }

    private void closeOtherWindows(String originalHandle) {
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
               .switchTo().window(handle);
                driver.close();
            }
        }
        driver.switchTo().window(originalHandle);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        performLogin();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        By userInput = By.id("user-name");
        By passInput = By.id("password");
        By loginBtn = By.id("login-button");

        wait.until(ExpectedConditions.elementToBeClickable(userInput)).clear();
        driver.findElement(userInput).sendKeys("wrong_user");
        driver.findElement(passInput).clear();
        driver.findElement(passInput).sendKeys("wrong_pass");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        By errorMsg = By.cssSelector("p[data-test='error']");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
        Assertions.assertTrue(error.getText().("Epic sadface"),
                "Error message not displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        performLogin();
        By sortDropdown = By.id("product_sort_container");
        By firstItemSelector = By.cssSelector(".inventory_item_name");

        String[] values = {"lohi", "hilo", "za", "az"};
        String previousFirst = "";

        for (String value : values) {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
            dropdown.findElement(By.xpath(String.format(".//option[@value='%s']", value))).click();

            WebElement first = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItemSelector));
            String currentFirst = first.getText();

            if (!previousFirst.isEmpty()) {
                Assertions.assertNotEquals(previousFirst, currentFirst,
                        "Sorting option did not change order for value: " + value);
            }
            previousFirst = currentFirst;
        }
    }

    @Test
    @Order(4)
    public void testBurgerMenuAllItems() {
        performLogin();
        By menuBtn = By.id("react-burger-menu-btn");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By allItemsLink = By.id("inventory_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(allItemsLink)).click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Burger menu All Items did not navigate to inventory page");
    }

    @Test
    @Order(5)
    public void testBurgerMenuAboutExternalLink() {
        performLogin();
        String originalHandle = getCurrentWindowHandle();

        By menuBtn = By.id("react-burger-menu-btn");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By aboutLink = By.id("about_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();

        Set<String> handles = driver.getWindowHandles();
        String newHandle = "";
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                newHandle = handle;
                break;
            }
        }
        Assertions.assertFalse(newHandle.isEmpty(), "External About link did not open a new tab");
        driver.switchTo().window(newHandle);
        wait.until(ExpectedConditions.urlContains("about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                "About link URL does not contain expected domain");
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(6)
    public void testBurgerMenuLogout() {
        performLogin();
        By menuBtn = By.id("react-burger-menu-btn");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By logoutLink = By.id("logout_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();

        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                "Logout did not redirect to login page");
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
        String originalHandle = getCurrentWindowHandle();

        // Twitter
        By twitterLink = By.cssSelector("a[href*='twitter.com']");
        WebElement twLink = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        twLink.click();
        closeOtherWindows(originalHandle);
        driver.switchTo().window(originalHandle);
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"),
                "Twitter link did not navigate correctly");

        // Facebook
        By facebookLink = By.cssSelector("a[href*='facebook.com']");
        WebElement fbLink = wait.until(ExpectedConditions.elementToBeClickable(facebookLink));
        fbLink.click();
        closeOtherWindows(originalHandle);
        driver.switchTo().window(originalHandle);
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"),
                "Facebook link did not navigate correctly");

        // LinkedIn
        By linkedInLink = By.cssSelector("a[href*='linkedin.com']");
        WebElement lnLink = wait.until(ExpectedConditions.elementToBeClickable(linkedInLink));
        lnLink.click();
        closeOtherWindows(originalHandle);
        driver.switchTo().window(originalHandle);
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"),
                "LinkedIn link did not navigate correctly");
    }

    @Test
    @Order(9)
    public void testAddToCartAndCheckout() {
        performLogin();

        By addButtonSelector = By.cssSelector("button[id^='add-to-cart-']");
        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(addButtonSelector));
        Assertions.assertFalse(addButtons.isEmpty(), "No add-to-cart buttons found");
        addButtons.get(0).click();

        By cartBadge = By.id("shopping_cart_badge");
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));
        Assertions.assertEquals("1", badge.getText(), "Cart badge not updated to 1");

        By cartLink = By.id("shopping_cart_container");
        wait.until(ExpectedConditions.elementToBeClickable(cartLink)).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        By checkoutBtn = By.id("checkout");
        wait.until(ExpectedConditions.elementToBeClickable(checkoutBtn)).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));

        By firstName = By.id("first-name");
        By lastName = By.id("last-name");
        By postalCode = By.id("postal-code");
        By continueBtn = By.id("continue");

        wait.until(ExpectedConditions.visibilityOfElementLocated(firstName)).sendKeys("John");
        wait.until(ExpectedConditions.visibilityOfElementLocated(lastName)).sendKeys("Doe");
        wait.until(ExpectedConditions.visibilityOfElementLocated(postalCode)).sendKeys("12345");
        wait.until(ExpectedConditions.elementToBeClickable(continueBtn)).click();
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));

        By finishBtn = By.id("finish");
        wait.until(ExpectedConditions.elementToBeClickable(finishBtn)).click();
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));

        By thankYouHeader = By.cssSelector(".complete-header");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(thankYouHeader));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", header.getText().trim(),
                "Checkout completion message not found");
    }
}