package GPT20b.ws05.seq07;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacTatWebsiteTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Helper methods ---------- */

    private WebElement findElementWithFallback(List<By> locators) {
        for (By locator : locators) {
            try {
                return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            } catch (Exception ignored) {
            }
        }
        throw new NoSuchElementException("Element not found using locators: " + locators);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testPageLoads() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("cac"),
                "Page title should include 'cac'");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement userField = findElementWithFallback(
                List.of(By.id("username"), By.name("username"), By.cssSelector("input[name='username']")));
        WebElement passField = findElementWithFallback(
                List.of(By.id("password"), By.name("password"), By.cssSelector("input[name='password']")));
        WebElement loginBtn = findElementWithFallback(
                List.of(By.id("loginbutton"), By.cssSelector("button[type='submit']"), By.id("loginBtn")));

        userField.clear();
        userField.sendKeys("standard_user");
        passField.clear();
        passField.sendKeys("secret_sauce");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"),
                "Should navigate to home page after login");
        List<WebElement> items = driver.findElements(By.cssSelector(".product-item"));
        Assertions.assertFalse(items.isEmpty(), "Product list should not be empty after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement userField = findElementWithFallback(
                List.of(By.id("username"), By.name("username")));
        WebElement passField = findElementWithFallback(
                List.of(By.id("password"), By.name("password")));
        WebElement loginBtn = findElementWithFallback(
                List.of(By.id("loginbutton"), By.cssSelector("button[type='submit']")));

        userField.clear();
        userField.sendKeys("invalid_user");
        passField.clear();
        passField.sendKeys("wrong_password");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be visible for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error text should mention invalid login");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        driver.get(BASE_URL);
        login();

        List<WebElement> sortDropdowns = driver.findElements(By.cssSelector("select.sort-dropdown, select#sort"));
        Assumptions.assumeTrue(!sortDropdowns.isEmpty(), "Sorting dropdown not present on the page");

        WebElement sortDropdown = sortDropdowns.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
        sortDropdown.click();

        // Assume options: Name A-Z, Price low to high, etc.
        WebElement optionAZ = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//option[contains(text(),'A-Z')]")));
        optionAZ.click();
        List<String> namesAZ = getProductNames();
        List<String> expectedAZ = new ArrayList<>(namesAZ);
        Collections.sort(expectedAZ);
        Assertions.assertEquals(expectedAZ, namesAZ,
                "Sorting A-Z should order product names alphabetically");

        sortDropdown.click();
        WebElement optionLowHigh = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//option[contains(text(),'Price low to high')]")));
        optionLowHigh.click();
        List<Double> pricesLH = getProductPrices();
        List<Double> expectedLH = new ArrayList<>(pricesLH);
        Collections.sort(expectedLH);
        Assertions.assertEquals(expectedLH, pricesLH,
                "Sorting price low to high should order prices ascending");
    }

    @Test
    @Order(5)
    public void testBurgerMenuInteraction() {
        driver.get(BASE_URL);
        login();

        // Open burger menu
        List<WebElement> burgerBtns = driver.findElements(By.cssSelector(".burger-menu, #burgerMenuBtn"));
        Assumptions.assumeTrue(!burgerBtns.isEmpty(), "Burger menu not present");
        WebElement burger = burgerBtns.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(burger));
        burger.click();

        // All Items
        List<WebElement> menuLinks = driver.findElements(By.cssSelector(".menu-link, .menu-list a"));
        Assumptions.assumeTrue(!menuLinks.isEmpty(), "Menu links not present");
        for (WebElement link : menuLinks) {
            if (link.getText().equalsIgnoreCase("All Items")) {
                wait.until(ExpectedConditions.elementToBeClickable(link));
                link.click();
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"),
                "All Items should navigate to home page");

        // About (external)
        for (WebElement link : menuLinks) {
            if (link.getText().equalsIgnoreCase("About")) {
                String originalHandle = driver.getWindowHandle();
                link.click();
                wait.until(d -> d.getWindowHandles().size() > 1);
                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(originalHandle)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
                Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                        "About link should load an external page");
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }

        // Reset App State
        for (WebElement link : menuLinks) {
            if (link.getText().equalsIgnoreCase("Reset App State")) {
                wait.until(ExpectedConditions.elementToBeClickable(link));
                link.click();
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"),
                "Reset App State should bring back to home");

        // Logout
        for (WebElement link : menuLinks) {
            if (link.getText().equalsIgnoreCase("Logout")) {
                wait.until(ExpectedConditions.elementToBeClickable(link));
                link.click();
                break;
            }
        }
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to base URL");
    }

    @Test
    @Order(6)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        login();

        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']"));
        Assumptions.assumeTrue(!externalLinks.isEmpty(), "No external links found in footer");

        String originalHandle = driver.getWindowHandle();
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (href == null || !href.startsWith("http")) {
                continue;
            }
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                    "Opened link URL should match the href");
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(7)
    public void testCartAddRemove() {
        driver.get(BASE_URL);
        login();

        WebElement addBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".add-to-cart, button.add")));
        addBtn.click();

        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-count")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1");

        WebElement removeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".remove-from-cart, button.remove")));
        removeBtn.click();

        Assertions.assertTrue(driver.findElements(By.cssSelector(".cart-count")).isEmpty(),
                "Cart badge should be removed after last item");
    }

    @Test
    @Order(8)
    public void testCheckoutFlow() {
        driver.get(BASE_URL);
        login();

        // Add two items
        List<WebElement> addButtons = driver.findElements(By.cssSelector(".add-to-cart, button.add"));
        Assertions.assertTrue(addButtons.size() >= 2, "Need at least two items to add");
        addButtons.get(0).click();
        addButtons.get(1).click();

        WebElement cartLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/cart'], .cart-link")));
        cartLink.click();

        wait.until(ExpectedConditions.urlContains("/cart"));

        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("#checkout, button.checkout")));
        checkoutBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#checkout-form")));

        driver.findElement(By.id("firstName")).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("postalCode")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout/confirmation"));
        WebElement successMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".checkout-success")));
        Assertions.assertTrue(successMsg.isDisplayed(),
                "Checkout confirmation message should be visible");
    }

    @Test
    @Order(9)
    public void testResetAppState() {
        driver.get(BASE_URL);
        login();

        // Open burger menu
        List<WebElement> burgerBtns = driver.findElements(By.cssSelector(".burger-menu, #burgerMenuBtn"));
        Assumptions.assumeTrue(!burgerBtns.isEmpty(), "Burger menu not present");
        WebElement burger = burgerBtns.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(burger));
        burger.click();

        // Click Reset App State
        List<WebElement> menuLinks = driver.findElements(By.cssSelector(".menu-link, .menu-list a"));
        for (WebElement link : menuLinks) {
            if (link.getText().equalsIgnoreCase("Reset App State")) {
                wait.until(ExpectedConditions.elementToBeClickable(link));
                link.click();
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("/home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"),
                "After reset, should return to home page");
    }

    /* ---------- Utility methods for sorting tests ---------- */

    private List<String> getProductNames() {
        List<WebElement> nameEls = driver.findElements(By.cssSelector(".product-title"));
        List<String> names = new ArrayList<>();
        for (WebElement el : nameEls) {
            names.add(el.getText());
        }
        return names;
    }

    private List<Double> getProductPrices() {
        List<WebElement> priceEls = driver.findElements(By.cssSelector(".product-price"));
        List<Double> prices = new ArrayList<>();
        for (WebElement el : priceEls) {
            String text = el.getText().replace("$", "").trim();
            try {
                prices.add(Double.parseDouble(text));
            } catch (NumberFormatException ignored) {
            }
        }
        return prices;
    }
}