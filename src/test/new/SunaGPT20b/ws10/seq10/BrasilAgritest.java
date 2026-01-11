package SunaGPT20b.ws10.seq10;

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
import java.util.ArrayList;
import java.util.Collections;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

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

    private void navigateToLogin() {
        driver.get(BASE_URL);
    }

    private void performLogin(String user, String pass) {
        navigateToLogin();
        WebElement userField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("login")));
        WebElement passField = driver.findElement(By.id("senha"));
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));

        userField.clear();
        userField.sendKeys(user);
        passField.clear();
        passField.sendKeys(pass);
        loginBtn.click();

        // Wait for inventory page (common identifier)
        wait.until(ExpectedConditions.urlContains("/inventory"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
    }

    private void ensureLoggedIn() {
        if (!driver.getCurrentUrl().contains("/inventory")) {
            performLogin(USERNAME, PASSWORD);
        }
    }

    private void openBurgerMenu() {
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-menu")));
    }

    private void switchToNewTabAndClose(String expectedDomain) {
        String originalHandle = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External URL does not contain expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        performLogin(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "Login did not navigate to inventory page.");
        Assertions.assertTrue(driver.findElements(By.id("inventory_container")).size() > 0,
                "Inventory container not found after login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigateToLogin();
        WebElement userField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("login")));
        WebElement passField = driver.findElement(By.id("senha"));
        WebElement loginBtn = driver.findElement(By.xpath("//button[contains(text(),'Entrar')]"));

        userField.clear();
        userField.sendKeys("invalid_user");
        passField.clear();
        passField.sendKeys("wrong_pass");
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials.");
    }
    
    private List<Double> getPrices() {
        List<WebElement> priceElems = driver.findElements(By.cssSelector(".inventory_item_price"));
        List<Double> prices = new ArrayList<>();
        for (WebElement el : priceElems) {
            String txt = el.getText().replace("$", "").trim();
            prices.add(Double.parseDouble(txt));
        }
        return prices;
    }

    private List<String> getNames() {
        List<WebElement> nameElems = driver.findElements(By.cssSelector(".inventory_item_name"));
        List<String> names = new ArrayList<>();
        for (WebElement el : nameElems) {
            names.add(el.getText().trim());
        }
        return names;
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        ensureLoggedIn();

        WebElement sortSelectElem = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("select[data-test='product_sort_container']"))
        );
        Select sortSelect = new Select(sortSelectElem);

        // Price low to high
        sortSelect.selectByVisibleText("Price (low to high)");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_price")));
        List<Double> lowToHigh = getPrices();
        List<Double> sortedLowToHigh = new ArrayList<>(lowToHigh);
        Collections.sort(sortedLowToHigh);
        Assertions.assertEquals(sortedLowToHigh, lowToHigh,
                "Items are not sorted by price low to high.");

        // Price high to low
        sortSelect.selectByVisibleText("Price (high to low)");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_price")));
        List<Double> highToLow = getPrices();
        List<Double> sortedHighToLow = new ArrayList<>(highToLow);
        sortedHighToLow.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedHighToLow, highToLow,
                "Items are not sorted by price high to low.");

        // Name A to Z
        sortSelect.selectByVisibleText("Name (A to Z)");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_name")));
        List<String> aToZ = getNames();
        List<String> sortedAToZ = new ArrayList<>(aToZ);
        Collections.sort(sortedAToZ);
        Assertions.assertEquals(sortedAToZ, aToZ,
                "Items are not sorted alphabetically A to Z.");

        // Name Z to A
        sortSelect.selectByVisibleText("Name (Z to A)");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".inventory_item_name")));
        List<String> zToA = getNames();
        List<String> sortedZToA = new ArrayList<>(zToA);
        sortedZToA.sort(Collections.reverseOrder());
        Assertions.assertEquals(sortedZToA, zToA,
                "Items are not sorted alphabetically Z to A.");
    }


    @Test
    @Order(4)
    public void testMenuAllItems() {
        ensureLoggedIn();
        openBurgerMenu();

        WebElement allItems = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.id("react-burger-menu-item-all-items"))
        );
        allItems.click();

        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "All Items menu did not navigate to inventory page.");
    }

    @Test
    @Order(5)
    public void testMenuAboutExternal() {
        ensureLoggedIn();
        openBurgerMenu();
        WebElement about = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-item-about")));
        about.click();
        switchToNewTabAndClose("saucelabs.com");
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        ensureLoggedIn();
        openBurgerMenu();
        WebElement logout = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-item-logout")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Logout did not return to login page.");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        ensureLoggedIn();
        // Add an item to cart
        WebElement addBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[id^='add-to-cart']")));
        addBtn.click();
        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(), "Cart badge should show 1 item.");

        // Reset app state
        openBurgerMenu();
        WebElement reset = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-item-reset")));
        reset.click();

        // Verify cart is empty
        List<WebElement> badges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should be cleared after reset.");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        ensureLoggedIn();
        // Scroll to bottom (simple JS)
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Twitter
        WebElement twitter = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitter.click();
        switchToNewTabAndClose("twitter.com");

        // Facebook
        WebElement facebook = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        facebook.click();
        switchToNewTabAndClose("facebook.com");

        // LinkedIn
        WebElement linkedIn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedIn.click();
        switchToNewTabAndClose("linkedin.com");
    }

    @Test
    @Order(9)
    public void testCheckoutFlow() {
        ensureLoggedIn();
        // Add two items
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button[id^='add-to-cart']"));
        Assertions.assertTrue(addButtons.size() >= 2, "At least two add-to-cart buttons required.");
        addButtons.get(0).click();
        addButtons.get(1).click();

        // Verify badge count
        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("2", badge.getText(), "Cart badge should show 2 items.");

        // Go to cart
        WebElement cartLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("shopping_cart_container")));
        cartLink.click();
        wait.until(ExpectedConditions.urlContains("/cart"));

        // Checkout
        WebElement checkoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("checkout")));
        checkoutBtn.click();
        wait.until(ExpectedConditions.urlContains("/checkout-step-one"));

        // Fill info
        driver.findElement(By.id("first-name")).sendKeys("John");
        driver.findElement(By.id("last-name")).sendKeys("Doe");
        driver.findElement(By.id("postal-code")).sendKeys("12345");
        driver.findElement(By.id("continue")).click();

        wait.until(ExpectedConditions.urlContains("/checkout-step-two"));
        // Finish
        WebElement finishBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("finish")));
        finishBtn.click();

        wait.until(ExpectedConditions.urlContains("/checkout-complete"));
        WebElement completeMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-header")));
        Assertions.assertTrue(completeMsg.getText().toLowerCase().contains("thank"),
                "Checkout completion message not found.");
    }
}