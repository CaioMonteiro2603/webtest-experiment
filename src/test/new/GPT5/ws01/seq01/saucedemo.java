package GPT5.ws01.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String INVENTORY_URL_FRACTION = "/inventory.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ----------------------- Helper Methods ----------------------- */

    private void openLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void login(String user, String pass) {
        openLoginPage();
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));
        userField.clear();
        userField.sendKeys(user);
        passField.clear();
        passField.sendKeys(pass);
        loginBtn.click();
    }

    private void loginStandardUserToInventory() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains(INVENTORY_URL_FRACTION));
        Assertions.assertTrue(driver.getCurrentUrl().contains(INVENTORY_URL_FRACTION), "Should land on inventory after login.");
        // Ensure menu is closed if present to avoid overlay issues
        if (driver.findElements(By.id("react-burger-cross-btn")).size() > 0) {
            driver.findElement(By.id("react-burger-cross-btn")).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("react-burger-cross-btn")));
        }
    }

    private void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-cross-btn")));
    }

    private void closeMenuIfOpen() {
        List<WebElement> close = driver.findElements(By.id("react-burger-cross-btn"));
        if (!close.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(close.get(0))).click();
            wait.until(ExpectedConditions.invisibilityOf(close.get(0)));
        }
    }

    private void resetAppStateViaMenuIfAvailable() {
        if (driver.getCurrentUrl().contains("inventory")) {
            openMenu();
            List<WebElement> reset = driver.findElements(By.id("reset_sidebar_link"));
            if (!reset.isEmpty()) {
                wait.until(ExpectedConditions.elementToBeClickable(reset.get(0))).click();
            }
            closeMenuIfOpen();
        }
    }

    private void logoutIfPossible() {
        if (driver.getCurrentUrl().contains("inventory")) {
            openMenu();
            List<WebElement> logout = driver.findElements(By.id("logout_sidebar_link"));
            if (!logout.isEmpty()) {
                wait.until(ExpectedConditions.elementToBeClickable(logout.get(0))).click();
                wait.until(ExpectedConditions.urlContains("index.html"));
            } else {
                closeMenuIfOpen();
            }
        }
    }

    private String getFirstItemName() {
        List<WebElement> names = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_name")));
        return names.get(0).getText().trim();
    }

    private double getFirstItemPrice() {
        List<WebElement> prices = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".inventory_item_price")));
        String priceText = prices.get(0).getText().replace("$", "").trim();
        return Double.parseDouble(priceText);
    }

    private void selectSortOptionByVisibleText(String visibleText) {
        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product_sort_container")));
        new Select(dropdown).selectByVisibleText(visibleText);
    }

    private void clickExternalLinkAndVerify(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        int preWindows = driver.getWindowHandles().size();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
        link.click();

        // Handle either same-tab navigation or new tab
        if (driver.getWindowHandles().size() > preWindows) {
            switchToNewTabAndAssertDomain(expectedDomain, originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Expected to navigate to domain: " + expectedDomain);
            driver.navigate().back(); // return to app
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("inventory"),
                    ExpectedConditions.urlContains("index.html"),
                    ExpectedConditions.presenceOfElementLocated(By.id("login-button"))
            ));
        }
    }

    private void switchToNewTabAndAssertDomain(String expectedDomain, String originalWindow) {
        wait.until(drv -> drv.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String w : windows) {
            if (!w.equals(originalWindow)) {
                driver.switchTo().window(w);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "URL should contain external domain: " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException nse) {
            return false;
        }
    }

    /* ----------------------- Tests ----------------------- */

    @Test
    @Order(1)
    public void testValidLoginNavigatesToInventory() {
        loginStandardUserToInventory();
        // Assert inventory list visible
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_list .inventory_item"));
        Assertions.assertFalse(items.isEmpty(), "Inventory items should be visible after login.");
        logoutIfPossible();
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        openLoginPage();
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userField.clear();
        userField.sendKeys("invalid_user");
        passField.clear();
        passField.sendKeys("wrong_password");
        loginBtn.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("epic sadface") || error.isDisplayed(),
                "An error message should appear for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testSortingDropdownChangesOrder() {
        loginStandardUserToInventory();

        // Ensure starting with Name (A to Z)
        selectSortOptionByVisibleText("Name (A to Z)");
        String firstAZ = getFirstItemName();

        // Switch to Name (Z to A) and assert first item changed
        selectSortOptionByVisibleText("Name (Z to A)");
        wait.until(drv -> !getFirstItemName().equals(firstAZ));
        String firstZA = getFirstItemName();
        Assertions.assertNotEquals(firstAZ, firstZA, "First item should differ after switching to Z->A.");

        // Price sorting: Low to High vs High to Low
        selectSortOptionByVisibleText("Price (low to high)");
        double firstLow = getFirstItemPrice();

        selectSortOptionByVisibleText("Price (high to low)");
        double firstHigh = getFirstItemPrice();
        Assertions.assertTrue(firstHigh >= firstLow, "First price under High->Low should be >= first price under Low->High.");

        resetAppStateViaMenuIfAvailable();
        logoutIfPossible();
    }

    @Test
    @Order(4)
    public void testMenuAllItemsFromItemDetail() {
        loginStandardUserToInventory();

        // Navigate to an item detail
        WebElement firstItem = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_item_name")));
        String nameBefore = firstItem.getText();
        firstItem.click();
        wait.until(ExpectedConditions.urlContains("inventory-item.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory-item.html"), "Should be on item detail page.");

        // Use menu -> All Items
        openMenu();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "All Items should return to inventory list.");

        // Ensure list present again
        Assertions.assertTrue(isElementPresent(By.xpath("//div[@class='inventory_item_name' and text()='" + nameBefore + "']")) ||
                        !driver.findElements(By.cssSelector(".inventory_item_name")).isEmpty(),
                "Inventory items should be visible after returning from item details.");

        resetAppStateViaMenuIfAvailable();
        logoutIfPossible();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        loginStandardUserToInventory();
        openMenu();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        int preWindows = driver.getWindowHandles().size();
        about.click();

        // It may open same tab or new tab
        if (driver.getWindowHandles().size() > preWindows) {
            switchToNewTabAndAssertDomain("saucelabs.com", originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains("saucelabs.com"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About should navigate to Sauce Labs site.");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
        // Back on app: ensure we can see inventory or login
        if (!driver.getCurrentUrl().contains("inventory") && isElementPresent(By.id("login-button"))) {
            loginStandardUserToInventory();
        }
        resetAppStateViaMenuIfAvailable();
        logoutIfPossible();
    }

    @Test
    @Order(6)
    public void testMenuResetAppStateClearsCart() {
        loginStandardUserToInventory();

        // Add first item to cart
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".inventory_list .inventory_item:first-of-type button.btn_primary.btn_inventory")));
        addBtn.click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 after adding an item.");

        // Reset App State
        openMenu();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        closeMenuIfOpen();

        // After reset, the cart badge should disappear
        wait.until(ExpectedConditions.or(
                ExpectedConditions.invisibilityOfElementLocated(By.className("shopping_cart_badge")),
                ExpectedConditions.numberOfElementsToBe(By.className("shopping_cart_badge"), 0)
        ));
        Assertions.assertTrue(driver.findElements(By.className("shopping_cart_badge")).isEmpty(),
                "Cart badge should be cleared after Reset App State.");

        logoutIfPossible();
    }

    @Test
    @Order(7)
    public void testMenuLogout() {
        loginStandardUserToInventory();
        openMenu();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Logout should redirect to login page.");
    }

    @Test
    @Order(8)
    public void testFooterTwitterLink() {
        loginStandardUserToInventory();
        clickExternalLinkAndVerify(By.cssSelector("a.social_twitter"), "twitter.com");
        resetAppStateViaMenuIfAvailable();
        logoutIfPossible();
    }

    @Test
    @Order(9)
    public void testFooterFacebookLink() {
        loginStandardUserToInventory();
        clickExternalLinkAndVerify(By.cssSelector("a.social_facebook"), "facebook.com");
        resetAppStateViaMenuIfAvailable();
        logoutIfPossible();
    }

    @Test
    @Order(10)
    public void testFooterLinkedInLink() {
        loginStandardUserToInventory();
        clickExternalLinkAndVerify(By.cssSelector("a.social_linkedin"), "linkedin.com");
        resetAppStateViaMenuIfAvailable();
        logoutIfPossible();
    }
}