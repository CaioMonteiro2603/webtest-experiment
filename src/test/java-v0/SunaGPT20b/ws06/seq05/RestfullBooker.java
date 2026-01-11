package SunaGPT20b.ws06.seq05;

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
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static final String BASE_URL = "https://automationintesting.online/";
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
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    private void resetAppState() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Ensure cart badge is removed
        wait.until(driver -> driver.findElements(By.className("shopping_cart_badge")).isEmpty());
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
                .map(e -> Double.parseDouble(e.getText().replace("$", "")))
                .collect(Collectors.toList());
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("standard_user", "secret_sauce");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "After valid login, URL should contain /inventory.html");
        List<WebElement> items = driver.findElements(By.className("inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory items should be displayed after login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));
        userField.sendKeys("invalid_user");
        passField.sendKeys("wrong_password");
        loginBtn.click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username"),
                "Error message should mention username or password");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login("standard_user", "secret_sauce");
        WebElement sortSelectElem = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
        Select sortSelect = new Select(sortSelectElem);

        // Name (A to Z)
        sortSelect.selectByVisibleText("Name (A to Z)");
        List<String> namesAsc = getItemNames();
        List<String> sortedNamesAsc = namesAsc.stream().sorted().collect(Collectors.toList());
        Assertions.assertEquals(sortedNamesAsc, namesAsc, "Items should be sorted alphabetically A-Z");

        // Name (Z to A)
        sortSelect.selectByVisibleText("Name (Z to A)");
        List<String> namesDesc = getItemNames();
        List<String> sortedNamesDesc = namesDesc.stream().sorted((a, b) -> b.compareTo(a)).collect(Collectors.toList());
        Assertions.assertEquals(sortedNamesDesc, namesDesc, "Items should be sorted alphabetically Z-A");

        // Price (low to high)
        sortSelect.selectByVisibleText("Price (low to high)");
        List<Double> pricesLowHigh = getItemPrices();
        List<Double> sortedPricesLowHigh = pricesLowHigh.stream().sorted().collect(Collectors.toList());
        Assertions.assertEquals(sortedPricesLowHigh, pricesLowHigh, "Items should be sorted by price low to high");

        // Price (high to low)
        sortSelect.selectByVisibleText("Price (high to low)");
        List<Double> pricesHighLow = getItemPrices();
        List<Double> sortedPricesHighLow = pricesHighLow.stream().sorted((a, b) -> b.compareTo(a)).collect(Collectors.toList());
        Assertions.assertEquals(sortedPricesHighLow, pricesHighLow, "Items should be sorted by price high to low");

        resetAppState();
    }

    @Test
    @Order(4)
    public void testBurgerMenuAllItems() {
        login("standard_user", "secret_sauce");
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory.html"),
                "All Items should navigate back to inventory page");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testBurgerMenuAboutExternal() {
        login("standard_user", "secret_sauce");
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        Set<String> windowsBefore = driver.getWindowHandles();
        aboutLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > windowsBefore.size());
        Set<String> windowsAfter = driver.getWindowHandles();
        windowsAfter.removeAll(windowsBefore);
        String newWindow = windowsAfter.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("automationintesting.online"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("automationintesting.online"),
                "About link should open automationintesting.online page");
        driver.close();
        driver.switchTo().window(originalWindow);
        resetAppState();
    }

    @Test
    @Order(6)
    public void testBurgerMenuLogout() {
        login("standard_user", "secret_sauce");
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(),
                "Logout should return to the login page");
    }

    @Test
    @Order(7)
    public void testAddToCartAndCheckout() {
        login("standard_user", "secret_sauce");
        // Add first item to cart
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@id,'add-to-cart')]")));
        addToCartBtn.click();
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 after adding an item");

        // Go to cart
        WebElement cartIcon = driver.findElement(By.id("shopping_cart_container"));
        cartIcon.click();
        wait.until(ExpectedConditions.urlContains("/cart.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/cart.html"), "Should navigate to cart page");

        // Checkout
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one.html"));
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-two.html"));
        driver.findElement(By.id("finish")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout_complete_container")));
        WebElement completeMsg = driver.findElement(By.cssSelector(".complete-header"));
        Assertions.assertTrue(completeMsg.getText().toUpperCase().contains("THANK YOU"),
                "Checkout completion message should contain THANK YOU");
        resetAppState();
    }

    @Test
    @Order(8)
    public void testFooterSocialLinksExternal() {
        login("standard_user", "secret_sauce");
        // Scroll to footer
        WebElement footer = driver.findElement(By.cssSelector("footer"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", footer);

        // Define expected domains for each social link
        String[][] links = {
                {"twitter", "twitter.com"},
                {"facebook", "facebook.com"},
                {"linkedin", "linkedin.com"}
        };

        for (String[] linkInfo : links) {
            String linkId = linkInfo[0];
            String expectedDomain = linkInfo[1];
            WebElement socialLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.social_" + linkId)));
            String originalWindow = driver.getWindowHandle();
            Set<String> windowsBefore = driver.getWindowHandles();
            socialLink.click();
            wait.until(driver -> driver.getWindowHandles().size() > windowsBefore.size());
            Set<String> windowsAfter = driver.getWindowHandles();
            windowsAfter.removeAll(windowsBefore);
            String newWindow = windowsAfter.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "Social link for " + linkId + " should open a page containing " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
    }
}