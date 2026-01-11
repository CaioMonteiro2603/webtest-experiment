package SunaGPT20b.ws06.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import java.util.ArrayList;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static final String BASE_URL = "https://www.saucedemo.com/";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    private static WebDriver driver;
    private static WebDriverWait wait;

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
        WebElement loginBtn = driver.findElement(By.id("login-button"));
        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    private void resetAppState() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Wait for the menu to close
        wait.until(ExpectedConditions.invisibilityOf(resetLink));
    }

    private void switchToNewTabAndBack(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        Set<String> windowsBefore = driver.getWindowHandles();
        // Assume click already performed
        wait.until(driver -> driver.getWindowHandles().size() > windowsBefore.size());
        Set<String> windowsAfter = driver.getWindowHandles();
        windowsAfter.removeAll(windowsBefore);
        String newWindow = windowsAfter.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link did not navigate to expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testLoginValid() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "Login did not navigate to inventory page.");
        WebElement inventoryContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(),
                "Inventory container is not displayed after login.");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testLoginInvalid() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));
        userField.sendKeys("invalid_user");
        passField.sendKeys("wrong_pass");
        loginBtn.click();
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message not displayed for invalid login.");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username"),
                "Error message does not mention username.");
    }

    @Test
    @Order(3)
    public void testInventorySorting() {
        login(USERNAME, PASSWORD);
        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='product_sort_container']")));
        String[] options = {"Name (A to Z)", "Name (Z to A)", "Price (low to high)", "Price (high to low)"};
        for (String option : options) {
            sortDropdown.click();
            WebElement opt = driver.findElement(By.xpath("//option[text()='" + option + "']"));
            opt.click();
            // Verify sorting by checking first two items
            List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
            Assertions.assertTrue(items.size() >= 2, "Not enough items to verify sorting.");
            String first = items.get(0).getText();
            String second = items.get(1).getText();
            if (option.equals("Name (A to Z)")) {
                Assertions.assertTrue(first.compareTo(second) <= 0,
                        "Items not sorted A to Z.");
            } else if (option.equals("Name (Z to A)")) {
                Assertions.assertTrue(first.compareTo(second) >= 0,
                        "Items not sorted Z to A.");
            } else if (option.equals("Price (low to high)")) {
                List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
                double p1 = Double.parseDouble(prices.get(0).getText().replace("$", ""));
                double p2 = Double.parseDouble(prices.get(1).getText().replace("$", ""));
                Assertions.assertTrue(p1 <= p2, "Prices not sorted low to high.");
            } else if (option.equals("Price (high to low)")) {
                List<WebElement> prices = driver.findElements(By.cssSelector(".inventory_item_price"));
                double p1 = Double.parseDouble(prices.get(0).getText().replace("$", ""));
                double p2 = Double.parseDouble(prices.get(1).getText().replace("$", ""));
                Assertions.assertTrue(p1 >= p2, "Prices not sorted high to low.");
            }
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(USERNAME, PASSWORD);
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
                "All Items did not navigate to inventory page.");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USERNAME, PASSWORD);
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        switchToNewTabAndBack("saucelabs.com");
        resetAppState();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USERNAME, PASSWORD);
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Logout did not return to login page.");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login(USERNAME, PASSWORD);
        // Add an item to cart to change state
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addBtn.click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge not updated.");
        // Reset state
        resetAppState();
        // Verify cart is empty
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge still present after reset.");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        // Twitter
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_twitter")));
        twitter.click();
        switchToNewTabAndBack("twitter.com");
        // Facebook
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_facebook")));
        facebook.click();
        switchToNewTabAndBack("facebook.com");
        // LinkedIn
        WebElement linkedIn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_linkedin")));
        linkedIn.click();
        switchToNewTabAndBack("linkedin.com");
        resetAppState();
    }

    @Test
    @Order(9)
    public void testCheckoutProcess() {
        login(USERNAME, PASSWORD);
        // Add first item to cart
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='add-to-cart-sauce-labs-backpack']")));
        addBtn.click();
        // Go to cart
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        cartIcon.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("cart.html"), "Did not navigate to cart page.");
        // Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        // Fill info
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("first-name"))).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        // Finish
        WebElement finishBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishBtn.click();
        WebElement thankYou = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertEquals("THANK YOU FOR YOUR ORDER", thankYou.getText(),
                "Checkout completion message not as expected.");
        resetAppState();
    }

    @Test
    @Order(10)
    public void testInternalLinksOneLevelDeep() {
        login(USERNAME, PASSWORD);
        // Collect internal links on inventory page
        List<WebElement> links = driver.findElements(By.cssSelector("a"));
        List<String> internalHrefs = new ArrayList<>();
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith(BASE_URL) && !href.equals(BASE_URL)) {
                internalHrefs.add(href);
            }
        }
        // Visit each internal link once
        for (String href : internalHrefs) {
            driver.navigate().to(href);
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
                    "Navigated to unexpected URL: " + driver.getCurrentUrl());
            // Simple verification: page has a header
            List<WebElement> headers = driver.findElements(By.tagName("h1"));
            Assertions.assertFalse(headers.isEmpty(), "Page at " + href + " has no <h1> header.");
        }
        // Return to inventory and reset
        driver.navigate().to(BASE_URL + "inventory.html");
        resetAppState();
    }
}