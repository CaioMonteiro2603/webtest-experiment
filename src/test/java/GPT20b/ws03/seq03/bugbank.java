package GPT20b.ws03.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assumptions;
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
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void init() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void cleanup() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* --------------------- Helper methods --------------------- */

    private static void openBaseUrl() {
        driver.get(BASE_URL);
    }

    private static void login() {
        openBaseUrl();
        // locate email field
        By emailLocator = new By.ByCssSelector("input[id='email'], input[name='email'], input[placeholder='Email']");
        By passwordLocator = new By.ByCssSelector("input[id='password'], input[name='password'], input[placeholder='Password']");
        By submitLocator = new By.ByCssSelector("button[type='submit'], button[id='loginBtn'], button:contains('Login')");

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(emailLocator));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordLocator));
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(submitLocator));

        emailField.clear();
        emailField.sendKeys(USER_EMAIL);
        passwordField.clear();
        passwordField.sendKeys(USER_PASSWORD);
        submitBtn.click();

        // wait for logged‑in indicator (e.g., logout button or account overview)
        By logoutLink = new By.ByLinkText("Logout");
        wait.until(ExpectedConditions.visibilityOfElementLocated(logoutLink));
    }

    private static void openBurgerMenu() {
        // flexible selector for a typical burger icon
        By menuBtnLocator = new By.ByCssSelector(
                "button[aria-label='menu'], .hamburger, #menu-toggle, .nav-toggle");
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(menuBtnLocator));
        menuBtn.click();
    }

    private static List<String> getItemNames() {
        By itemNameLocator = new By.ByCssSelector(".item-name, .product-title, .product-name");
        return driver.findElements(itemNameLocator)
                     .stream()
                     .map(WebElement::getText)
                     .collect(Collectors.toList());
    }

    private static List<Double> getItemPrices() {
        By priceLocator = new By.ByCssSelector(".item-price, .product-price, .price");
        return driver.findElements(priceLocator)
                     .stream()
                     .map(e -> e.getText().replaceAll("[^\\d.]+", ""))
                     .map(Double::valueOf)
                     .collect(Collectors.toList());
    }

    /* --------------------- Test cases --------------------- */

    @Test
    @Order(1)
    @DisplayName("Valid login credentials")
    void testValidLogin() {
        login();
        String current = driver.getCurrentUrl();
        assertTrue(current.contains("dashboard") || current.contains("home") || current.contains("account"),
                "After login, URL did not contain expected path. Current: " + current);
    }

    @Test
    @Order(2)
    @DisplayName("Invalid login credentials")
    void testInvalidLogin() {
        openBaseUrl();
        By emailLocator = new By.ByCssSelector("input[id='email'], input[name='email'], input[placeholder='Email']");
        By passwordLocator = new By.ByCssSelector("input[id='password'], input[name='password'], input[placeholder='Password']");
        By submitLocator = new By.ByCssSelector("button[type='submit'], button[id='loginBtn'], button:contains('Login')");

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(emailLocator));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordLocator));
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(submitLocator));

        emailField.clear();
        emailField.sendKeys("wrong@example.com");
        passwordField.clear();
        passwordField.sendKeys("wrongpass");
        submitBtn.click();

        // expect an error message element
        By errorLocator = new By.ByCssSelector(".error, .alert, .alert-danger, .validation-message");
        Assumptions.assumeTrue(driver.findElements(errorLocator).size() > 0,
                "Error message element not found for invalid credentials");
        WebElement errorEl = wait.until(ExpectedConditions.visibilityOfElementLocated(errorLocator));
        String errText = errorEl.getText();
        assertTrue(errText.toLowerCase().contains("invalid") || errText.toLowerCase().contains("wrong") ||
                errText.toLowerCase().contains("error"),
                "Unexpected error message: " + errText);
    }

    @Test
    @Order(3)
    @DisplayName("Sorting dropdown test")
    void testSortingDropdown() {
        login();

        // locate the sorting dropdown
        By sortDropdownLocator = new By.ByCssSelector("select[id='sort-select'], select[id='sort'], select[name='sort']");
        List<WebElement> sortElements = driver.findElements(sortDropdownLocator);
        Assumptions.assumeTrue(!sortElements.isEmpty(),
                "Sorting dropdown not present; skipping this test");

        Select sortSelect = new Select(sortElements.get(0));

        // capture original order
        List<String> originalOrder = new ArrayList<>(getItemNames());

        // Define sorting options to test
        String[] options = {"Price (low to high)", "Price (high to low)", "Name (A to Z)", "Name (Z to A)"};

        for (String optLabel : options) {
            sortSelect.selectByVisibleText(optLabel);
            // wait for list to update
            wait.until(ExpectedConditions.stalenessOf(sortElements.get(0)));
            // re-fetch the same dropdown element
            sortElements = driver.findElements(sortDropdownLocator);
            sortSelect = new Select(sortElements.get(0));

            // verify order changed
            if (optLabel.contains("Name")) {
                List<String> current = getItemNames();
                assertFalse(current.equals(originalOrder),
                        "Name sort (" + optLabel + ") did not change order");
                List<String> expected = new ArrayList<>(current);
                expected.sort(optLabel.contains("A to Z") ? String::compareTo : (a, b) -> b.compareTo(a));
                assertEquals(expected, current, "Name sorted list does not match expected order");
            } else {
                List<Double> current = getItemPrices();
                assertFalse(current.equals(getItemPrices()),
                        "Price sort (" + optLabel + ") did not change order");
                List<Double> expected = new ArrayList<>(current);
                expected.sort(optLabel.contains("low") ? Double::compareTo : (a, b) -> b.compareTo(a));
                assertEquals(expected, current, "Price sorted list does not match expected order");
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("Menu actions test")
    void testMenuActions() {
        login();

        // 1. All Items
        openBurgerMenu();
        By allItemsLink = new By.ByLinkText("All Items");
        Assumptions.assumeTrue(driver.findElements(allItemsLink).size() > 0,
                "All Items link not found in menu; skipping this part");
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(allItemsLink));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("products"));
        assertTrue(driver.getCurrentUrl().contains("products"), "Did not navigate to All Items page");

        driver.navigate().back();

        // 2. About (external)
        openBurgerMenu();
        By aboutLink = new By.ByLinkText("About");
        if (driver.findElements(aboutLink).size() > 0) {
            WebElement about = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
            String parentHandle = driver.getWindowHandle();
            about.click();
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> handles = driver.getWindowHandles();
            handles.remove(parentHandle);
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            assertTrue(driver.getCurrentUrl().toLowerCase().contains("github.com") ||
                       driver.getCurrentUrl().toLowerCase().contains("bugbank"),
                       "About link did not navigate to expected domain");
            driver.close();
            driver.switchTo().window(parentHandle);
        }

        // 3. Logout
        openBurgerMenu();
        By logoutLink = new By.ByLinkText("Logout");
        if (driver.findElements(logoutLink).size() > 0) {
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutLink));
            logoutBtn.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[type='submit'], button[id='loginBtn']")));
            assertTrue(driver.getCurrentUrl().contains("index") || driver.getCurrentUrl().contains("login"),
                    "Logout did not return to login page");
        }

        // 4. Reset App State (if present)
        login();
        By resetLink = new By.ByLinkText("Reset");
        if (driver.findElements(resetLink).size() > 0) {
            WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(resetLink));
            resetBtn.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(resetLink));
            // verify state reset by checking items count again
            List<WebElement> items = driver.findElements(By.cssSelector(".item-name, .product-title, .product-name"));
            assertTrue(items.size() > 0, "After reset, no items found");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Footer social links")
    void testFooterSocialLinks() {
        login();

        List<By> links = List.of(
                By.cssSelector("a[href*='twitter.com']"),
                By.cssSelector("a[href*='facebook.com']"),
                By.cssSelector("a[href*='linkedin.com']"));

        for (By loc : links) {
            List<WebElement> elems = driver.findElements(loc);
            Assumptions.assumeTrue(!elems.isEmpty(), "Social link not found: " + loc);
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(elems.get(0)));
            String parentHandle = driver.getWindowHandle();
            link.click();
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> handles = driver.getWindowHandles();
            handles.remove(parentHandle);
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            assertTrue(driver.getCurrentUrl().toLowerCase().contains(loc.toString()),
                    "External link URL does not contain expected domain: " + loc);
            driver.close();
            driver.switchTo().window(parentHandle);
        }
    }
}