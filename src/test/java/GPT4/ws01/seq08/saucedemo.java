package GPT4.ws01.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";

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

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginButton.click();
    }

    private void logout() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
    }

    private void resetAppState() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        WebElement closeMenu = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeMenu.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("standard_user", "secret_sauce");
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Expected to be on inventory page after login.");
        List<WebElement> inventoryItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("inventory_item")));
        Assertions.assertFalse(inventoryItems.isEmpty(), "Expected inventory items to be present after login.");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_password");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("username and password do not match"),
                "Expected error message for invalid login.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login("standard_user", "secret_sauce");
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortSelect.click();
        WebElement optionLoHi = sortSelect.findElement(By.cssSelector("option[value='lohi']"));
        optionLoHi.click();
        List<WebElement> prices = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("inventory_item_price")));
        double firstPrice = Double.parseDouble(prices.get(0).getText().replace("$", ""));
        double lastPrice = Double.parseDouble(prices.get(prices.size() - 1).getText().replace("$", ""));
        Assertions.assertTrue(firstPrice <= lastPrice, "Expected prices to be sorted from low to high.");
        resetAppState();
        logout();
    }

    @Test
    @Order(4)
    public void testBurgerMenuOptions() {
        login("standard_user", "secret_sauce");

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "Expected to be on inventory page.");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }
        wait.until(d -> !d.getCurrentUrl().equals(BASE_URL));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("saucelabs.com"), "Expected external link to point to Sauce Labs.");
        driver.close();
        driver.switchTo().window(originalWindow);

        resetAppState();
        logout();
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login("standard_user", "secret_sauce");
        String originalWindow = driver.getWindowHandle();

        List<WebElement> socialLinks = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("social_twitter")));
        for (WebElement link : socialLinks) {
            link.click();
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String win : windows) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    break;
                }
            }
            String newUrl = driver.getCurrentUrl();
            Assertions.assertTrue(newUrl.contains("twitter.com") || newUrl.contains("facebook.com") || newUrl.contains("linkedin.com"),
                    "Expected social link to open correct external site.");
            driver.close();
            driver.switchTo().window(originalWindow);
        }

        resetAppState();
        logout();
    }

    @Test
    @Order(6)
    public void testAddToCartAndCheckout() {
        login("standard_user", "secret_sauce");

        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".btn_inventory")));
        addButtons.get(0).click();

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item.");

        WebElement cartLink = driver.findElement(By.className("shopping_cart_link"));
        cartLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("cart.html"), "Expected to be on cart page.");

        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".checkout_button")));
        checkoutButton.click();

        WebElement firstName = wait.until(ExpectedConditions.elementToBeClickable(By.id("first-name")));
        WebElement lastName = driver.findElement(By.id("last-name"));
        WebElement postalCode = driver.findElement(By.id("postal-code"));
        WebElement continueButton = driver.findElement(By.cssSelector(".cart_button"));

        firstName.sendKeys("John");
        lastName.sendKeys("Doe");
        postalCode.sendKeys("12345");
        continueButton.click();

        WebElement finishButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".cart_button")));
        finishButton.click();

        WebElement completeHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", completeHeader.getText(), "Expected order completion message.");

        resetAppState();
        logout();
    }
}
