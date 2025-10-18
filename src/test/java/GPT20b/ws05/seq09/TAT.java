package GPT20b.ws05.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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

@TestMethodOrder(OrderAnnotation.class)
public class CacTatTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123";

    @BeforeAll
    public static void setUpDriver() {
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

    private void reloadPage() {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void closeOtherWindows(String originalHandle) {
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }
        driver.switchTo().window(originalHandle);
    }

    private void performLogin(String user, String pass) {
        reloadPage();
        By userField = By.id("username");
        By passField = By.id("password");
        By loginBtn = By.id("login-btn");

        wait.until(ExpectedConditions.elementToBeClickable(userField)).clear();
        driver.findElement(userField).sendKeys(user);
        driver.findElement(passField).clear();
        driver.findElement(passField).sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("cac"),
                "Page title does not contain 'CAC'");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        performLogin(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"),
                "Login did not redirect to home page");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        performLogin("wrong", "wrong");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message not displayed for invalid credentials");
    }

    @Test
    @Order(4)
    public void testSortingOptions() {
        performLogin(USERNAME, PASSWORD);
        By sortDropdown = By.id("sort-options");
        By firstItem = By.cssSelector(".product-name");

        String[] values = {"low-high", "high-low", "name-asc", "name-desc"};
        String previousFirst = "";

        for (String val : values) {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
            dropdown.findElement(By.xpath(String.format(".//option[@value='%s']", val))).click();

            WebElement firstProduct = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItem));
            String currentFirst = firstProduct.getText();

            if (!previousFirst.isEmpty()) {
                Assertions.assertNotEquals(previousFirst, currentFirst,
                        "Sorting option " + val + " did not change first item");
            }
            previousFirst = currentFirst;
        }
    }

    @Test
    @Order(5)
    public void testBurgerMenuAllItems() {
        performLogin(USERNAME, PASSWORD);
        By menuBtn = By.id("menu-button");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By allItems = By.id("menu-all-items");
        wait.until(ExpectedConditions.elementToBeClickable(allItems)).click();

        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"),
                "Menu All Items did not navigate to home page");
    }

    @Test
    @Order(6)
    public void testBurgerMenuAboutExternalLink() {
        performLogin(USERNAME, PASSWORD);
        String originalHandle = driver.getWindowHandle();

        By menuBtn = By.id("menu-button");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By aboutLink = By.id("menu-about");
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();

        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(originalHandle))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("New window did not open"));
        driver.switchTo().window(newHandle);
        wait.until(ExpectedConditions.urlContains("about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                "About link URL does not contain expected domain");
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(7)
    public void testBurgerMenuLogout() {
        performLogin(USERNAME, PASSWORD);
        By menuBtn = By.id("menu-button");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By logoutLink = By.id("menu-logout");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();

        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Logout did not redirect to login page");
    }

    @Test
    @Order(8)
    public void testBurgerMenuResetAppState() {
        performLogin(USERNAME, PASSWORD);
        By menuBtn = By.id("menu-button");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By resetLink = By.id("menu-reset");
        wait.until(ExpectedConditions.elementToBeClickable(resetLink)).click();

        // Verify inventory is refreshed
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".product-item")));
        Assertions.assertFalse(driver.findElements(By.cssSelector(".product-item")).isEmpty(),
                "Reset App State did not show product items");
    }

    @Test
    @Order(9)
    public void testExternalLinks() {
        performLogin(USERNAME, PASSWORD);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[target='_blank']"));
        Assertions.assertFalse(externalLinks.isEmpty(),
                "No external links found on the page");

        String originalHandle = driver.getWindowHandle();
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            link.click();

            Set<String> handles = driver.getWindowHandles();
            String newHandle = handles.stream()
                    .filter(h -> !h.equals(originalHandle))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("New window did not open"));
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(href));
            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                    "External link URL does not contain expected domain");
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(10)
    public void testCartAddRemove() {
        performLogin(USERNAME, PASSWORD);
        By addButton = By.cssSelector("button[id^='add-to-cart-']");
        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(addButton));
        Assertions.assertFalse(addButtons.isEmpty(), "No add-to-cart buttons found");
        addButtons.get(0).click();

        By cartBadge = By.id("cart-count");
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));
        Assertions.assertEquals("1", badge.getText(), "Cart badge not updated to 1");

        By removeButton = By.cssSelector("button[id^='remove-']");
        WebElement removeBtn = wait.until(ExpectedConditions.elementToBeClickable(removeButton));
        removeBtn.click();

        WebElement badgeAfter = wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));
        Assertions.assertEquals("0", badgeAfter.getText(), "Cart badge not updated to 0 after removal");
    }

    @Test
    @Order(11)
    public void testCheckoutProcess() {
        performLogin(USERNAME, PASSWORD);

        // Add item
        By addButton = By.cssSelector("button[id^='add-to-cart-']");
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(addButton)).get(0).click();

        // Go to cart
        By cartLink = By.id("cart");
        wait.until(ExpectedConditions.elementToBeClickable(cartLink)).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        // Proceed to checkout
        By checkoutBtn = By.id("checkout");
        wait.until(ExpectedConditions.elementToBeClickable(checkoutBtn)).click();
        wait.until(ExpectedConditions.urlContains("checkout.html"));

        // Fill checkout form
        By firstName = By.id("firstName");
        By lastName = By.id("lastName");
        By address = By.id("address");
        By continueBtn = By.id("continue");

        wait.until(ExpectedConditions.visibilityOfElementLocated(firstName)).sendKeys("John");
        wait.until(ExpectedConditions.visibilityOfElementLocated(lastName)).sendKeys("Doe");
        wait.until(ExpectedConditions.visibilityOfElementLocated(address)).sendKeys("123 Test Ave");
        wait.until(ExpectedConditions.elementToBeClickable(continueBtn)).click();

        // Finish
        By finishBtn = By.id("finish");
        wait.until(ExpectedConditions.elementToBeClickable(finishBtn)).click();
        wait.until(ExpectedConditions.urlContains("confirmation.html"));

        // Success message
        By successMsg = By.cssSelector(".order-confirmation");
        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(successMsg));
        Assertions.assertTrue(msg.getText().toLowerCase().contains("thank you"),
                "Checkout confirmation message not displayed");
    }
}