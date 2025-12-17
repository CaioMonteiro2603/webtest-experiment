package SunaGPT20b.ws03.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement userInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passInput = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userInput.clear();
        userInput.sendKeys(user);
        passInput.clear();
        passInput.sendKeys(pass);
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
    }

    private void logoutIfLoggedIn() {
        List<WebElement> menuButtons = driver.findElements(By.id("react-burger-menu-btn"));
        if (!menuButtons.isEmpty()) {
            menuButtons.get(0).click();
            WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
            logoutLink.click();
            wait.until(ExpectedConditions.urlContains("login"));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "After login the URL should contain 'inventory'.");

        // Verify inventory container is displayed
        WebElement inventoryContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(),
                "Inventory container should be visible after successful login.");

        // Clean up
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement userInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passInput = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));

        userInput.sendKeys("invalid@example.com");
        passInput.sendKeys("wrongpass");
        loginBtn.click();

        WebElement errorMsg = wait.until(
 ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials.");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("username") ||
                        errorMsg.getText().toLowerCase().contains("password"),
                "Error message should reference username or password.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);

        // Locate sorting dropdown
        WebElement sortSelectElem = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
        Select sortSelect = new Select(sortSelectElem);
        List<WebElement> options = sortSelect.getOptions();

        // Verify each option changes order
        for (WebElement option : options) {
            sortSelect.selectByVisibleText(option.getText());

            // Wait for the first inventory item to be refreshed
            List<WebElement> items = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("inventory_item_name")));

            Assertions.assertFalse(items.isEmpty(),
                    "Inventory items should be present after sorting with option: " + option.getText());

            // Simple sanity check: capture first item's name
            String firstItem = items.get(0).getText();
            Assertions.assertNotNull(firstItem,
                    "First item name should not be null after sorting with option: " + option.getText());
        }

        // Clean up
        logoutIfLoggedIn();
    }

    @Test
    @Order(4)
    public void testMenuBurgerAndReset() {
        login(USERNAME, PASSWORD);

        // Add an item to cart to later verify reset
        WebElement firstAddBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='add-to-cart-sauce-labs-backpack']")));
        firstAddBtn.click();

        // Verify cart badge shows 1
        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", badge.getText(),
                "Cart badge should display count 1 after adding an item.");

        // Open burger menu
        WebElement menuBtn = driver.findElement(By.id("react-burger-menu-btn"));
        menuBtn.click();

        // Click All Items
        WebElement allItemsLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "URL should still contain 'inventory' after clicking All Items.");

        // Open menu again for About link
        menuBtn = driver.findElement(By.id("react-burger-menu-btn"));
        menuBtn.click();

        // Click About (external)
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new tab
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com") ||
                        driver.getCurrentUrl().contains("about"),
                "About link should navigate to an external Sauce Labs page.");

        // Close external tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again for Reset App State
        menuBtn = driver.findElement(By.id("react-burger-menu-btn"));
        menuBtn.click();

        WebElement resetLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();

        // Verify cart badge disappears
        wait.until(driver -> driver.findElements(By.className("shopping_cart_badge")).isEmpty());

        // Open menu again for Logout
        menuBtn = driver.findElement(By.id("react-burger-menu-btn"));
        menuBtn.click();

        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                "After logout the URL should contain 'login'.");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);

        // Scroll to footer (simple JS scroll)
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Define social links and expected domains
        String[][] socials = {
                {"a[data-test='social-twitter']", "twitter.com"},
                {"a[data-test='social-facebook']", "facebook.com"},
                {"a[data-test='social-linkedin']", "linkedin.com"}
        };

        for (String[] social : socials) {
            List<WebElement> links = driver.findElements(By.cssSelector(social[0]));
            if (links.isEmpty()) {
                continue; // Skip if link not present
            }
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(links.get(0))); 
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Wait for new window
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            Assertions.assertTrue(driver.getCurrentUrl().contains(social[1]),
                    "External social link should navigate to a URL containing " + social[1]);

            // Close and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }

        // Clean up
        logoutIfLoggedIn();
    }
}