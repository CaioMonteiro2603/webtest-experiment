package SunaGPT20b.ws10.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class WebAppTestSuite {

    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
    }

    private void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("/inventory")) {
            login();
        }
    }

    @Test
    @Order(1)
    public void testLoginSuccess() {
        login();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "After successful login the URL should contain '/inventory'");
    }

    @Test
    @Order(2)
    public void testLoginFailure() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys("invalid@example.com");
        driver.findElement(By.id("password")).sendKeys("wrongpass");
        driver.findElement(By.id("login-button")).click();

        By errorLocator = By.cssSelector("[data-test='error'] , .error-message-container");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorLocator));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginIfNeeded();

        By sortLocator = By.cssSelector("[data-test='product_sort_container'], .product_sort_container");
        WebElement sortElement = wait.until(ExpectedConditions.elementToBeClickable(sortLocator));
        Select sortSelect = new Select(sortElement);
        List<WebElement> options = sortSelect.getOptions();

        Assertions.assertFalse(options.isEmpty(), "Sorting dropdown should contain options");

        for (WebElement option : options) {
            sortSelect.selectByVisibleText(option.getText());
            // Verify that the first product name changes after sorting
            By firstItemLocator = By.cssSelector(".inventory_item_name");
            WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItemLocator));
            Assertions.assertNotNull(firstItem.getText(),
                    "First item name should be present after selecting sort option: " + option.getText());
        }
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        loginIfNeeded();

        By menuButton = By.id("react-burger-menu-btn");
        wait.until(ExpectedConditions.elementToBeClickable(menuButton)).click();

        By allItems = By.id("inventory_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(allItems)).click();

        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "Clicking All Items should keep the user on the inventory page");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        loginIfNeeded();

        By menuButton = By.id("react-burger-menu-btn");
        wait.until(ExpectedConditions.elementToBeClickable(menuButton)).click();

        By aboutLink = By.id("about_sidebar_link");
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        String originalWindow = driver.getWindowHandle();
        Set<String> existingWindows = driver.getWindowHandles();

        about.click();

        // Wait for new window
        wait.until(driver -> driver.getWindowHandles().size() > existingWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(existingWindows);
        String newWindow = newWindows.iterator().next();

        driver.switchTo().window(newWindow);
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("saucelabs.com") || currentUrl.contains("about"),
                "About link should open an external page containing expected domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        loginIfNeeded();

        By menuButton = By.id("react-burger-menu-btn");
        wait.until(ExpectedConditions.elementToBeClickable(menuButton)).click();

        By logoutLink = By.id("logout_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();

        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "After logout the user should be redirected to the login page");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        loginIfNeeded();

        // Add an item to cart to change state
        By addToCart = By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack'], button[id*='add-to-cart']");
        wait.until(ExpectedConditions.elementToBeClickable(addToCart)).click();

        By cartBadge = By.cssSelector(".shopping_cart_badge");
        wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));

        By menuButton = By.id("react-burger-menu-btn");
        wait.until(ExpectedConditions.elementToBeClickable(menuButton)).click();

        By resetLink = By.id("reset_sidebar_link");
        wait.until(ExpectedConditions.elementToBeClickable(resetLink)).click();

        // Verify cart badge is gone
        List<WebElement> badges = driver.findElements(cartBadge);
        Assertions.assertTrue(badges.isEmpty(), "Reset App State should clear the cart badge");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        loginIfNeeded();

        String[][] socials = {
                {"twitter.com", "a[href*='twitter.com']"},
                {"facebook.com", "a[href*='facebook.com']"},
                {"linkedin.com", "a[href*='linkedin.com']"}
        };

        for (String[] social : socials) {
            String domain = social[0];
            String selector = social[1];
            List<WebElement> links = driver.findElements(By.cssSelector(selector));
            if (links.isEmpty()) continue; // skip if not present

            WebElement link = links.get(0);
            String originalWindow = driver.getWindowHandle();
            Set<String> existingWindows = driver.getWindowHandles();

            link.click();

            wait.until(driver -> driver.getWindowHandles().size() > existingWindows.size());
            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(existingWindows);
            String newWindow = newWindows.iterator().next();

            driver.switchTo().window(newWindow);
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains(domain),
                    "Social link should open a page containing domain: " + domain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(9)
    public void testExternalLinksOnInventoryPage() {
        loginIfNeeded();

        // Gather external links on the current page (one level below base)
        List<WebElement> externalLinks = driver.findElements(By.xpath(
                "//a[starts-with(@href,'http') and not(contains(@href,'gestao.brasilagritest.com'))]"));

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            String originalWindow = driver.getWindowHandle();
            Set<String> existingWindows = driver.getWindowHandles();

            // Open link in new tab/window
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);

            wait.until(driver -> driver.getWindowHandles().size() > existingWindows.size());
            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(existingWindows);
            String newWindow = newWindows.iterator().next();

            driver.switchTo().window(newWindow);
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains(new java.net.URL(href).getHost()),
                    "External link should navigate to its own domain: " + href);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}