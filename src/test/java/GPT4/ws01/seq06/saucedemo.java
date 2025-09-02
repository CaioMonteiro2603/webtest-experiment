package GPT4.ws01.seq06;

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

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
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
        WebElement inventoryContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(), "Inventory page should be displayed after login.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "URL should contain 'inventory' after login.");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "invalid_pass");
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testSortDropdown() {
        login("standard_user", "secret_sauce");
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        dropdown.click();

        List<WebElement> options = driver.findElements(By.cssSelector(".product_sort_container option"));
        String initialFirstItem = driver.findElement(By.className("inventory_item_name")).getText();

        for (WebElement option : options) {
            option.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_item")));
            String newFirstItem = driver.findElement(By.className("inventory_item_name")).getText();
            Assertions.assertNotEquals("", newFirstItem, "First item should not be empty after sorting.");
        }

        resetAppState();
        logout();
    }

    @Test
    @Order(4)
    public void testBurgerMenuLinks() {
        login("standard_user", "secret_sauce");
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "Should be on inventory page after clicking All Items.");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));

        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> allWindows = driver.getWindowHandles();
        allWindows.remove(originalWindow);
        String newWindow = allWindows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs"), "About link should navigate to Sauce Labs.");
        driver.close();
        driver.switchTo().window(originalWindow);

        resetAppState();
        logout();
    }

    @Test
    @Order(5)
    public void testFooterLinks() {
        login("standard_user", "secret_sauce");

        String originalWindow = driver.getWindowHandle();

        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".social_twitter a, .social_facebook a, .social_linkedin a"));
        for (WebElement link : socialLinks) {
            String expectedDomain = "";
            if (link.getAttribute("href").contains("twitter.com")) expectedDomain = "twitter.com";
            if (link.getAttribute("href").contains("facebook.com")) expectedDomain = "facebook.com";
            if (link.getAttribute("href").contains("linkedin.com")) expectedDomain = "linkedin.com";

            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", link.getAttribute("href"));

            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newTab = windows.iterator().next();
            driver.switchTo().window(newTab);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should go to " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }

        resetAppState();
        logout();
    }
}
