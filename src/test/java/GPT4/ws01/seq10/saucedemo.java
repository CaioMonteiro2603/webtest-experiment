package GPT4.ws01.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
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

    private void login() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.clear();
        username.sendKeys("standard_user");
        password.clear();
        password.sendKeys("secret_sauce");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
    }

    private void resetAppState() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeButton.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("inventory"), "Login failed: Inventory page not loaded.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.clear();
        username.sendKeys("invalid_user");
        password.clear();
        password.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message not shown for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));

        sortSelect.click();
        sortSelect.findElement(By.cssSelector("option[value='az']")).click();
        String firstItemAZ = driver.findElement(By.className("inventory_item_name")).getText();

        sortSelect.click();
        sortSelect.findElement(By.cssSelector("option[value='za']")).click();
        String firstItemZA = driver.findElement(By.className("inventory_item_name")).getText();

        Assertions.assertNotEquals(firstItemAZ, firstItemZA, "Sorting did not affect item order.");
        resetAppState();
    }

    @Test
    @Order(4)
    public void testBurgerMenuAllItems() {
        login();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "All Items did not navigate to inventory.");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testBurgerMenuAboutExternalLink() {
        login();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));

        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        String externalUrl = driver.getCurrentUrl();
        Assertions.assertTrue(externalUrl.contains("saucelabs.com"), "About link did not go to expected domain.");

        driver.close();
        driver.switchTo().window(originalWindow);
        resetAppState();
    }

    @Test
    @Order(6)
    public void testBurgerMenuLogout() {
        login();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Logout did not redirect to login page.");
    }

    @Test
    @Order(7)
    public void testAddToCartAndReset() {
        login();
        List<WebElement> addButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".btn_inventory")));
        int itemsToAdd = Math.min(2, addButtons.size());

        for (int i = 0; i < itemsToAdd; i++) {
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(i)));
            addBtn.click();
        }

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals(String.valueOf(itemsToAdd), cartBadge.getText(), "Cart count mismatch.");

        resetAppState();
        List<WebElement> badges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart not cleared after reset.");
    }

    @Test
    @Order(8)
    public void testExternalSocialLinks() {
        login();
        String[] selectors = {
            "footer .social_twitter a",
            "footer .social_facebook a",
            "footer .social_linkedin a"
        };
        String[] expectedDomains = {
            "twitter.com",
            "facebook.com",
            "linkedin.com"
        };

        for (int i = 0; i < selectors.length; i++) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selectors[i])));
            String originalWindow = driver.getWindowHandle();
            link.click();

            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            String newUrl = driver.getCurrentUrl();
            Assertions.assertTrue(newUrl.contains(expectedDomains[i]), "External link did not open expected domain.");

            driver.close();
            driver.switchTo().window(originalWindow);
        }

        resetAppState();
    }
}
