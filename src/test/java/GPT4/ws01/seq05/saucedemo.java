package GPT4.ws01.seq05;

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
    private static final String USERNAME = "standard_user";
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
        if (driver != null) {
            driver.quit();
        }
    }

    private void login() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("inventory"));
    }

    private void resetAppState() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reset_sidebar_link"))).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("reset_sidebar_link")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "URL should contain 'inventory' after login.");
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0, "Inventory items should be visible after login.");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name"))).sendKeys("wrong_user");
        driver.findElement(By.id("password")).sendKeys("wrong_pass");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("username and password do not match"), "Error message should mention invalid credentials.");
    }

    @Test
    @Order(3)
    public void testSortingOptions() {
        login();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='az']")).click();
        String firstAZ = driver.findElements(By.className("inventory_item_name")).get(0).getText();

        sortDropdown = driver.findElement(By.className("product_sort_container"));
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='za']")).click();
        String firstZA = driver.findElements(By.className("inventory_item_name")).get(0).getText();

        Assertions.assertNotEquals(firstAZ, firstZA, "First item name should differ between A-Z and Z-A sorting.");
        resetAppState();
    }

    @Test
    @Order(4)
    public void testAddAndRemoveFromCart() {
        login();
        WebElement firstAddBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'ADD TO CART')]")));
        firstAddBtn.click();
        WebElement cartBadge = driver.findElement(By.className("shopping_cart_badge"));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after adding one item.");
        WebElement removeBtn = driver.findElement(By.xpath("//button[contains(text(),'REMOVE')]"));
        removeBtn.click();
        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty(), "Cart badge should disappear after removing item.");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testMenuActions() {
        login();
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "Should navigate to inventory page on All Items.");

        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();
        for (String handle : allWindows) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link should go to saucelabs.com");
        driver.close();
        driver.switchTo().window(originalWindow);

        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Should redirect to login page on logout.");
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        login();
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".social_link a"));
        String originalWindow = driver.getWindowHandle();
        for (WebElement link : socialLinks) {
            String expectedDomain = link.getAttribute("href");
            link.click();
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> allWindows = driver.getWindowHandles();
            for (String handle : allWindows) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            String newUrl = driver.getCurrentUrl();
            Assertions.assertTrue(newUrl.contains("twitter.com") || newUrl.contains("facebook.com") || newUrl.contains("linkedin.com"),
                    "External link should contain expected social domain.");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
    }
}