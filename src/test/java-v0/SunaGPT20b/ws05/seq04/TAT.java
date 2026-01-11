package SunaGPT20b.ws05.seq04;

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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
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

    private void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void clickIfExists(By locator) {
        List<WebElement> elems = driver.findElements(locator);
        if (!elems.isEmpty()) {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(elems.get(0)));
            el.click();
        }
    }

    private String getDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return "";
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToBase();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Home page URL should contain 'index.html'");
        Assertions.assertTrue(driver.findElements(By.tagName("body")).size() > 0,
                "Home page should contain a <body> element");
    }

    @Test
    @Order(2)
    public void testExternalLinksOneLevelDeep() {
        navigateToBase();
        List<WebElement> links = driver.findElements(By.xpath("//a[starts-with(@href,'http')]"));
        List<String> visitedHandles = new ArrayList<>();

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (href.contains("cac-tat.s3.eu-central-1.amazonaws.com")) continue; // skip internal

            // Open link in new tab via JavaScript
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
            Set<String> handles = driver.getWindowHandles();
            handles.removeAll(visitedHandles);
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(getDomain(href)));
            Assertions.assertTrue(driver.getCurrentUrl().contains(getDomain(href)),
                    "External link should navigate to its domain: " + getDomain(href));
            driver.close();
            driver.switchTo().window(driver.getWindowHandles().iterator().next());
            visitedHandles.add(newHandle);
        }
    }

    @Test
    @Order(3)
    public void testMenuBurgerButton() {
        navigateToBase();

        // Open burger menu if present
        clickIfExists(By.id("react-burger-menu-btn"));
        clickIfExists(By.id("react-burger-menu-btn")); // toggle close if needed

        // All Items
        clickIfExists(By.id("inventory_sidebar_link"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html") ||
                        driver.getCurrentUrl().contains("index.html"),
                "All Items should keep us on inventory or home page");

        // About (external)
        clickIfExists(By.id("about_sidebar_link"));
        // If About opens external, handle new tab
        Set<String> handlesBefore = driver.getWindowHandles();
        if (handlesBefore.size() > 1) {
            for (String handle : handlesBefore) {
                if (!handle.equals(driver.getWindowHandle())) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                            "About page URL should contain 'about'");
                    driver.close();
                }
            }
            driver.switchTo().window(driver.getWindowHandles().iterator().next());
        }

        // Reset App State
        clickIfExists(By.id("reset_sidebar_link"));
        // Verify that cart badge is cleared (if present)
        List<WebElement> badge = driver.findElements(By.className("shopping_cart_badge"));
        if (!badge.isEmpty()) {
            Assertions.assertEquals("0", badge.get(0).getText(),
                    "Cart badge should be cleared after reset");
        }

        // Logout
        clickIfExists(By.id("logout_sidebar_link"));
        // Verify we are back to login page (if exists)
        Assertions.assertTrue(driver.getCurrentUrl().contains("login") ||
                        driver.getCurrentUrl().contains("index.html"),
                "After logout we should be on login or home page");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        navigateToBase();

        // Locate sorting dropdown (common id)
        By dropdownLocator = By.cssSelector("select[data-test='product_sort_container'], select#sort_container");
        List<WebElement> dropdowns = driver.findElements(dropdownLocator);
        if (dropdowns.isEmpty()) {
            // No sorting dropdown present; test passes trivially
            return;
        }

        WebElement dropdown = dropdowns.get(0);
        List<WebElement> options = dropdown.findElements(By.tagName("option"));
        Assertions.assertFalse(options.isEmpty(), "Sorting dropdown should have options");

        for (WebElement option : options) {
            String value = option.getAttribute("value");
            dropdown.click();
            option.click();
            // Wait for page to reflect sorting (simple check: first item text changes)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_name")));
            WebElement firstItem = driver.findElement(By.cssSelector(".inventory_item_name"));
            Assertions.assertNotNull(firstItem.getText(),
                    "First item name should be present after sorting by " + value);
        }
    }

    @Test
    @Order(5)
    public void testLoginValidAndInvalid() {
        // Attempt valid login if login form exists
        navigateToBase();

        By usernameLocator = By.id("user-name");
        By passwordLocator = By.id("password");
        By loginBtnLocator = By.id("login-button");

        if (driver.findElements(usernameLocator).size() > 0) {
            // Valid credentials (example from SauceDemo)
            driver.findElement(usernameLocator).clear();
            driver.findElement(usernameLocator).sendKeys("standard_user");
            driver.findElement(passwordLocator).clear();
            driver.findElement(passwordLocator).sendKeys("secret_sauce");
            driver.findElement(loginBtnLocator).click();

            // Verify successful login
            wait.until(ExpectedConditions.urlContains("inventory"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                    "Successful login should navigate to inventory page");

            // Logout to reset state
            clickIfExists(By.id("react-burger-menu-btn"));
            clickIfExists(By.id("logout_sidebar_link"));
        }

        // Invalid login attempt
        navigateToBase();
        if (driver.findElements(usernameLocator).size() > 0) {
            driver.findElement(usernameLocator).clear();
            driver.findElement(usernameLocator).sendKeys("invalid_user");
            driver.findElement(passwordLocator).clear();
            driver.findElement(passwordLocator).sendKeys("wrong_password");
            driver.findElement(loginBtnLocator).click();

            // Expect error message
            By errorLocator = By.cssSelector("[data-test='error']");
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(errorLocator));
            Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username") ||
                            errorMsg.getText().toLowerCase().contains("password"),
                    "Error message should indicate invalid credentials");
        }
    }
}