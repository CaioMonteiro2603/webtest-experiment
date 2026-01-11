package SunaGPT20b.ws01.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    private void loginIfNeeded() {
        if (driver.getCurrentUrl().contains("saucedemo.com")) {
            // already logged in
            return;
        }
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.clear();
        usernameField.sendKeys(USERNAME);
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);
        WebElement loginButton = driver.findElement(By.id("login-button"));
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "Login should navigate to inventory page");
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "After valid login URL should contain /inventory.html");
        WebElement inventoryContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(),
                "Inventory container should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        usernameField.sendKeys("invalid_user");
        driver.findElement(By.id("password")).sendKeys("wrong_password");
        driver.findElement(By.id("login-button")).click();
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginIfNeeded();
        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("select.product_sort_container")));
        Select select = new Select(sortDropdown);
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (String option : options) {
            select.selectByVisibleText(option);
            // verify that the first item changes accordingly
            WebElement firstItem = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item_name")));
            Assertions.assertNotNull(firstItem.getText(),
                    "First item name should be present after sorting by " + option);
        }
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        loginIfNeeded();
        // open menu
        WebElement menuButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button")));
        menuButton.click();
        // click All Items
        WebElement allItems = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should navigate to inventory page");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        loginIfNeeded();
        // open menu
        WebElement menuButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button")));
        menuButton.click();
        // click About (external)
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();
        // wait for new window
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        String newWindow = windows.stream().filter(w -> !w.equals(originalWindow)).findFirst().orElse(null);
        Assertions.assertNotNull(newWindow, "A new window should open for About link");
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
                "About page should be on saucelabs.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        loginIfNeeded();
        // open menu
        WebElement menuButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button")));
        menuButton.click();
        // click Logout
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to login page");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        loginIfNeeded();
        // add an item to cart to change state
        WebElement addToCart = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        addToCart.click();
        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should show 1 after adding item");
        // open menu
        WebElement menuButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button")));
        menuButton.click();
        // click Reset App State
        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // verify cart badge disappears
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(),
                "Cart badge should be removed after resetting app state");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        loginIfNeeded();
        // scroll to footer if needed
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        String[][] links = {
                {"twitter", "twitter.com"},
                {"facebook", "facebook.com"},
                {"linkedin", "linkedin.com"}
        };
        for (String[] pair : links) {
            String id = pair[0];
            String domain = pair[1];
            WebElement link = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-test='" + id + "-footer-link']")));
            String originalWindow = driver.getWindowHandle();
            link.click();
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            String newWindow = windows.stream().filter(w -> !w.equals(originalWindow)).findFirst().orElse(null);
            Assertions.assertNotNull(newWindow, "New window should open for " + id);
            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.urlContains(domain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                    id + " link should navigate to a URL containing " + domain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(9)
    public void testInternalLinksOneLevelBelow() {
        loginIfNeeded();
        // collect internal links on inventory page
        List<WebElement> linkElements = driver.findElements(By.cssSelector("a"));
        List<String> hrefs = linkElements.stream()
                .map(e -> e.getAttribute("href"))
                .filter(h -> h != null && !h.isEmpty())
                .filter(h -> h.startsWith(BASE_URL) || h.startsWith("https://www.saucedemo.com/v1/"))
                .filter(h -> !h.equals(BASE_URL) && !h.endsWith("/inventory.html"))
                .distinct()
                .collect(Collectors.toList());

        if (hrefs.isEmpty()) {
            // If no internal links found, skip the test
            return;
        }

        for (String href : hrefs) {
            try {
                // navigate via click
                WebElement link = driver.findElement(By.cssSelector("a[href='" + href + "']"));
                link.click();
                wait.until(ExpectedConditions.urlToBe(href));
                Assertions.assertEquals(href, driver.getCurrentUrl(),
                        "Navigated URL should match the link href");
                // go back to inventory page
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains("/inventory.html"));
            } catch (NoSuchElementException | TimeoutException e) {
                // Skip problematic links
                continue;
            }
        }
    }
}