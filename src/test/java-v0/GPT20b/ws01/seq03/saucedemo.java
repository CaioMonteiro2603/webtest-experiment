package GPT20b.ws01.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
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

    /* ---------- Helper Methods ---------- */

    private void login() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")))
                .sendKeys(USERNAME);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")))
                .sendKeys(PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")))
                .click();
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item")));
    }

    private void resetAppState() {
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")))
                .click();
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".inventory_item")));
    }

    private void openMenu() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")))
                .click();
    }

    /* ---------- Test Cases ---------- */

    @Test
    @Order(1)
    @DisplayName("Valid Login Test")
    public void testLoginSuccess() {
        login();
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/inventory.html"),
                "Expected to navigate to inventory page after login, but URL was: " + currentUrl);

        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item"));
        assertTrue(items.size() > 0,
                "Inventory list is empty after login. Found " + items.size() + " items.");
    }

    @Test
    @Order(2)
    @DisplayName("Invalid Login Test")
    public void testLoginInvalidCredentials() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")))
                .sendKeys("invalid_user");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")))
                .sendKeys("invalid_pass");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")))
                .click();

        WebElement errorEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error")));
        String errorText = errorEl.getText();
        assertTrue(errorText.contains("Epic sadface"),
                "Unexpected error message for invalid credentials: " + errorText);
    }

    @Test
    @Order(3)
    @DisplayName("Sorting Dropdown Functionality Test")
    public void testSortingDropdown() {
        login();

        Select sortSelect = new Select(wait.until(ExpectedConditions.elementToBeClickable(By.id("product_sort_container"))));

        // 1. Name (A to Z)
        sortSelect.selectByVisibleText("Name (A to Z)");
        waitForSorting(() -> getItemNames(), String::compareTo);

        // 2. Name (Z to A)
        sortSelect.selectByVisibleText("Name (Z to A)");
        waitForSorting(() -> getItemNames(), (a, b) -> b.compareTo(a));

        // 3. Price (Low to High)
        sortSelect.selectByVisibleText("Price (Low to High)");
        waitForSorting(() -> getItemPrices(), Double::compareTo);

        // 4. Price (High to Low)
        sortSelect.selectByVisibleText("Price (High to Low)");
        waitForSorting(() -> getItemPrices(), (a, b) -> b.compareTo(a));
    }

    private List<String> getItemNames() {
        return driver.findElements(By.cssSelector(".inventory_item_name"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    private List<Double> getItemPrices() {
        return driver.findElements(By.cssSelector(".inventory_item_price"))
                .stream()
                .map(p -> Double.parseDouble(p.getText().replace("$", "")))
                .collect(Collectors.toList());
    }

    private <T> void waitForSorting(java.util.function.Supplier<List<T>> getter, java.util.Comparator<T> comparator) {
        List<T> expected = new ArrayList<>(getter.get());
        expected.sort(comparator);
        List<T> actual = getter.get();
        assertEquals(expected, actual, "Sorting order did not match expected order.");
    }

    @Test
    @Order(4)
    @DisplayName("Menu Actions Test")
    public void testMenuActions() {
        login();

        // All Items
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")))
                .click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));

        // About (external)
        openMenu();
        String originalHandle = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")))
                .click();
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(originalHandle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No new window detected"));
        driver.switchTo().window(newHandle);
        assertTrue(driver.getCurrentUrl().toLowerCase().contains("saucelabs.com"),
                "About link did not navigate to expected domain. URL: " + driver.getCurrentUrl());
        driver.close();
        driver.switchTo().window(originalHandle);

        // Logout
        openMenu();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")))
                .click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));

        // Reset App State
        login(); // login again to access menu
        resetAppState();
    }

    @Test
    @Order(5)
    @DisplayName("Footer Social Links Test")
    public void testFooterSocialLinks() {
        login();
        String originalHandle = driver.getWindowHandle();

        // Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        verifyAndCloseExternalLink(originalHandle, "twitter.com");

        // Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        verifyAndCloseExternalLink(originalHandle, "facebook.com");

        // LinkedIn link
        WebElement linkedInLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='linkedin.com']")));
        linkedInLink.click();
        verifyAndCloseExternalLink(originalHandle, "linkedin.com");
    }

    private void verifyAndCloseExternalLink(String originalHandle, String expectedDomain) {
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(originalHandle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No new window detected"));
        driver.switchTo().window(newHandle);
        assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomain),
                "External link did not contain expected domain '" + expectedDomain + "'. URL: " + driver.getCurrentUrl());
        driver.close();
        driver.switchTo().window(originalHandle);
    }
}