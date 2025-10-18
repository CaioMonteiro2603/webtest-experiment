package GPT20b.ws10.seq07;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

/**
 * Test suite for the Gestão Brasilagri web application.
 * All tests use Firefox in headless mode and Selenium 4 APIs.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestaoWebsiteTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USER_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String USER_PASSWORD = "10203040";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().pageLoadTimeout(10, java.util.concurrent.TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Utility methods ---------- */

    private WebElement findElementWithFallback(List<By> locators) {
        for (By locator : locators) {
            try {
                return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            } catch (Exception ignored) {
            }
        }
        throw new NoSuchElementException("Element not found with any of: " + locators);
    }

    private void login() {
        driver.get(BASE_URL);

        WebElement emailField = findElementWithFallback(
                List.of(By.id("email"), By.name("email"), By.cssSelector("input[type='email']")));
        WebElement passwordField = findElementWithFallback(
                List.of(By.id("password"), By.name("password"), By.cssSelector("input[type='password']")));
        WebElement loginButton = findElementWithFallback(
                List.of(By.id("loginBtn"), By.cssSelector("button[type='submit']"),
                        By.xpath("//button[contains(text(),'Login')]")));

        emailField.clear();
        emailField.sendKeys(USER_EMAIL);
        passwordField.clear();
        passwordField.sendKeys(USER_PASSWORD);

        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();

        // After login URL typically contains 'dashboard' or 'home'
        wait.until(ExpectedConditions.urlMatches(".*(dashboard|home).*"));
        Assertions.assertTrue(driver.getCurrentUrl().matches(".*(dashboard|home).*"),
                "After login, URL should contain 'dashboard' or 'home'");
    }

    private void logout() {
        List<WebElement> menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("Logout")) {
                wait.until(ExpectedConditions.elementToBeClickable(item));
                item.click();
                break;
            }
        }
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "After logout, should be back at login page");
    }

    private List<WebElement> openBurgerMenuAndGetItems() {
        WebElement burgerBtn = findElementWithFallback(
                List.of(By.id("menu-toggle"), By.cssSelector(".burger-menu"),
                        By.cssSelector(".hamburger")));
        wait.until(ExpectedConditions.elementToBeClickable(burgerBtn));
        burgerBtn.click();

        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector(".menu-list a, .menu a, .nav-link")));
    }

    private void resetAppState() {
        List<WebElement> menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("Reset App State")) {
                wait.until(ExpectedConditions.elementToBeClickable(item));
                item.click();
                break;
            }
        }
        // Typical behavior: redirect to main dashboard
        wait.until(ExpectedConditions.urlMatches(".*(dashboard|home).*"));
    }

    private List<String> getItemNames() {
        List<WebElement> names = driver.findElements(By.cssSelector(".item-name, .list-item h4, .product-title"));
        return names.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getItemPrices() {
        List<WebElement> priceEls = driver.findElements(By.cssSelector(".item-price, .price, .product-price"));
        List<Double> prices = new ArrayList        for (WebElement el : priceEls) {
            String text = el.getText().replaceAll("[^0-9.,-]", "").replace(",", ".");
            try {
                prices.add(Double.parseDouble(text));
            } catch (NumberFormatException ignored) {
            }
        }
        return prices;
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        // Verify that a main content element exists
        List<WebElement> mainSections = driver.findElements(By.cssSelector(".dashboard, .content, .main"));
        Assertions.assertFalse(mainSections.isEmpty(), "Dashboard should contain at least one main section");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement emailField = findElementWithFallback(
                List.of(By.id("email"), By.name("email")));
        WebElement passwordField = findElementWithFallback(
                List.of(By.id("password"), By.name("password")));
        WebElement loginButton = findElementWithFallback(
                List.of(By.id("loginBtn"), By.cssSelector("button[type='submit']")));

        emailField.clear();
        emailField.sendKeys("invalid@user.com");
        passwordField.clear();
        passwordField.sendKeys("wrongpass");

        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error, .alert-danger, .alert")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed on invalid login");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid") ||
                errorMsg.getText().toLowerCase().contains("wrong"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        // Navigate to items list if needed
        driver.get(BASE_URL + "items"); // assumption
        List<WebElement> sortSelects = driver.findElements(By.cssSelector("select#sortOrder"));
        Assumptions.assumeTrue(!sortSelects.isEmpty(), "Sorting dropdown not present, skipping test");
        WebElement sortSelect = sortSelects.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(sortSelect));

        // Capture original order
        List<String> original = getItemNames();
        Assumptions.assumeTrue(!original.isEmpty(), "No items found to test sorting");

        // Option: Name ascending
        WebElement optionAsc = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//select[@id='sortOrder']/option[@value='name-asc']")));
        optionAsc.click();
        List<String> asc = getItemNames();
        List<String> sortedAsc = new ArrayList<>(original);
        Collections.sort(sortedAsc);
        Assertions.assertEquals(sortedAsc, asc, "Name ascending sort should order items alphabetically");

        // Option: Name descending
        WebElement optionDesc = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//select[@id='sortOrder']/option[@value='name-desc']")));
        optionDesc.click();
        List<String> desc = getItemNames();
        List<String> sortedDesc = new ArrayList<>(original);
        Collections.sort(sortedDesc, java.util.Comparator.reverseOrder());
        Assertions.assertEquals(sortedDesc, desc, "Name descending sort should order items reverse alphabetically");

        // Option: Price low to high
        WebElement optionLowHigh = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//select[@id='sortOrder']/option[@value='price optionLowHigh.click();
        List<Double> lowHigh = getItemPrices();
        List<Double> sortedLowHigh = new ArrayList<>(lowHigh);
        Collections.sort(sortedLowHigh);
        Assertions.assertEquals(sortedLowHigh, lowHigh,
                "Price low-to-high sort should order prices ascending");
    }

    @Test
    @Order(4)
    public void testBurgerMenuInteractions() {
        login();

        // All Items
        List<WebElement> menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("All Items")) {
                wait.until(ExpectedConditions.elementToBeClickable(item));
                item.click();
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("items"),
                "All Items should navigate to items page");

        // About (external)
        menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("About")) {
                String originalHandle = driver.getWindowHandle();
                item.click();
                wait.until(d -> d.getWindowHandles().size() > 1);
                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(originalHandle)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
                Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                        "About link should open an external page");
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }

        // Reset App State
        menuItems = openBurgerMenuAndGetItems();
        for (WebElement item : menuItems) {
            if (item.getText().equalsIgnoreCase("Reset App State")) {
                wait.until(ExpectedConditions.elementToBeClickable(item));
                item.click();
                break;
            }
        }
        wait.until(ExpectedConditions.urlMatches(".*(dashboard|home).*"));
        Assertions.assertTrue(driver.getCurrentUrl().matches(".*(dashboard|home).*"),
                "After reset, should land on dashboard/home");

        // Logout
        logout();
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        login();
        // Scroll to footer
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "window.scrollTo(0, document.body.scrollHeight);");
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("footer a[href^='http']"));
        Assumptions.assumeTrue(!externalLinks.isEmpty(), "No external links in footer; skipping");

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
                    "Opened link URL should contain the href: " + href);
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(6)
    public void testCartAddRemove() {
        login();

        // Go to items page
        driver.get(BASE_URL + "items");

        // Find first add to cart button
        List<WebElement> addButtons = driver.findElements(By.cssSelector(".add-to-cart, button[data-action='add']"));
        Assumptions.assumeTrue(!addButtons.isEmpty(), "No add-to-cart buttons found; skipping test");

        WebElement firstAdd = addButtons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(firstAdd));
        firstAdd.click();

        // Verify cart badge appears with count 1
        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-count, #cartBadge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should display 1");

        // Remove item
        List<WebElement> removeButtons = driver.findElements(By.cssSelector(".remove-from-cart, button[data-action='remove']"));
        if (!removeButtons.isEmpty()) {
            WebElement firstRemove = removeButtons.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(firstRemove));
            firstRemove.click();

            // Badge should disappear
            Assertions.assertTrue(driver.findElements(By.cssSelector(".cart-count, #cartBadge")).isEmpty(),
                    "Cart badge should be removed after last item");
        } else {
            Assumptions.assumeTrue(false, "No remove-from-cart buttons found; skipping removal part");
        }
    }

    @Test
    @Order(7)
    public void testCheckoutFlow() {
        login();

        // Add two items to cart
        driver.get(BASE_URL + "items");
        List<WebElement> addButtons = driver.findElements(By.cssSelector(".add-to-cart, button[data-action='add']"));
        Assumptions.assumeTrue(addButtons.size() >= 2, "Less than two items available for checkout");
        addButtons.get(0).click();
        addButtons.get(1).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".cart-count, #cartBadge"), "2"));

        // Go to cart
        driver.get(BASE_URL + "cart");
        wait.until(ExpectedConditions.urlContains("cart"));

        // Proceed to checkout
        WebElement checkoutBtn = findElementWithFallback(
                List.of(By.id("checkoutBtn"), By.cssSelector("button#checkout"), By.xpath("//button[contains(text(),'Checkout')]")));
        wait.until(ExpectedConditions.elementToBeClickable(checkoutBtn));
        checkoutBtn.click();

        // Fill shipping info
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ship-first-name"))).sendKeys("John");
        driver.findElement(By.id("ship-last-name")).sendKeys("Doe");
        driver.findElement(By.id("ship-address")).sendKeys("123 Test Ave");
        driver.findElement(By.id("ship-city")).sendKeys("Testville");
        driver.findElement(By.id("ship-zip")).sendKeys("12345");
        driver.findElement(By.id("ship-country")).sendKeys("BR");
        driver.findElement(By.id("ship-email")).sendKeys("john.doe@example.com");

        WebElement finishBtn = findElementWithFallback(
                List.of(By.id("finishBtn"), By.cssSelector("button#complete"), By.xpath("//button[contains(text(),'Finish')]")));
        finishBtn.click();

        wait.until(ExpectedConditions.urlMatches(".*(confirmation|order-success).*"));
        WebElement confirmationMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".confirmation, .order-success")));
        Assertions.assertTrue(confirmationMsg.getText().toLowerCase().contains("thank you"),
                "Checkout confirmation message should be displayed");
    }

    @Test
    @Order(8)
    public void testResetAppStateIndependence() {
        login();
        resetAppState();
        // Verify cart badge absent
        Assertions.assertTrue(driver.findElements(By.cssSelector(".cart-count, #cartBadge")).isEmpty(),
                "Cart badge should be absent after resetting application state");
    }

    @Test
    @Order(9)
    public void testLogoutFunctionality() {
        login();
        logout();
    }
}