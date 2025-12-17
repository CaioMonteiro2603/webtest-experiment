package SunaQwen3.ws06.seq03;

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
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

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

    private static void goToBase() {
        driver.get(BASE_URL);
    }

    private static void login(String username, String password) {
        goToBase();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        driver.findElement(By.id("user-name")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    private static void loginValid() {
        login("standard_user", "secret_sauce");
    }

    private static void resetAppState() {
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            menuBtn.click();
            WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
            resetLink.click();
            WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
            closeBtn.click();
        } catch (TimeoutException ignored) {
        }
    }

    private static double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replace("$", "").trim());
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        loginValid();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Should navigate to inventory page after login");
        WebElement inventory = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventory.isDisplayed(), "Inventory container should be displayed");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        goToBase();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        driver.findElement(By.id("user-name")).sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_pass");
        driver.findElement(By.id("login-button")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(error.getText().toLowerCase().contains("username") ||
                        error.getText().toLowerCase().contains("password"),
                "Error message should mention invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginValid();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("select[data-test='product_sort_container']")));
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (String opt : options) {
            sortDropdown.click();
            WebElement optionEl = sortDropdown.findElement(By.xpath(".//option[. = '" + opt + "']"));
            optionEl.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
            List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
            Assertions.assertFalse(items.isEmpty(), "Item list should not be empty after sorting");
            if (opt.contains("Price (low to high)")) {
                List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
                double first = parsePrice(prices.get(0).getText());
                double last = parsePrice(prices.get(prices.size() - 1).getText());
                Assertions.assertTrue(first <= last,
                        "First price should be <= last for low-to-high sort");
            } else if (opt.contains("Price (high to low)")) {
                List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
                double first = parsePrice(prices.get(0).getText());
                double last = parsePrice(prices.get(prices.size() - 1).getText());
                Assertions.assertTrue(first >= last,
                        "First price should be >= last for high-to-low sort");
            }
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuBurger() {
        loginValid();
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement allItems = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));
        Assertions.assertTrue(allItems.isDisplayed(), "All Items link should be visible");
        WebElement aboutLink = driver.findElement(By.id("about_sidebar_link"));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();
        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "External About page should contain expected domain");
        driver.close();
        driver.switchTo().window(originalWindow);
        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("/"),
                "Should return to login page after logout");
        loginValid();
        resetAppState();
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        loginValid();
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a"));
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href == null) continue;
            if (href.contains("twitter.com") || href.contains("facebook.com") || href.contains("linkedin.com")) {
                String originalWindow = driver.getWindowHandle();
                link.click();
                wait.until(d -> d.getWindowHandles().size() > 1);
                Set<String> windows = driver.getWindowHandles();
                windows.remove(originalWindow);
                String newWindow = windows.iterator().next();
                driver.switchTo().window(newWindow);
                String domain = href.split("/")[2];
                Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                        "External social link should open correct domain: " + domain);
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
        resetAppState();
    }
}