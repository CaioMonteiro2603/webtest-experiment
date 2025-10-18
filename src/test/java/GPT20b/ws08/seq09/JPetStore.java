package GPT20b.ws08.seq09;

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
public class JPetStoreTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    private void performLogin(String user, String pass) {
        driver.get(BASE_URL);
        By userField = By.id("username");
        By passField = By.id("password");
        By loginBtn = By.cssSelector("button[type='submit'], button[id='login']");

        wait.until(ExpectedConditions.elementToBeClickable(userField)).clear();
        driver.findElement(userField).sendKeys(user);
        driver.findElement(passField).clear();
        driver.findElement(passField).sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    private void performLogout() {
        By logoutLink = By.linkText("Logout");
        wait.until(ExpectedConditions.elementToBeClickableLink)).click();
        wait.until(ExpectedConditions.urlContains("index.jsp"));
    }

    private void resetAppState() {
        By resetBtn = By.linkText("Reset the app state");
        if (!driver.findElements(resetBtn).isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetBtn)).click();
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".product")));
        }
    }

    private String currentWindowHandle() {
        return driver.getWindowHandle();
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

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("pet store"),
                "Home page title does not contain 'Pet Store'");
    }

    @Test
    @Order(2)
    public void testLoginPagePresence() {
        driver.get(BASE_URL);
        Assertions.assertFalse(driver.findElements(By.id("username")).isEmpty(),
                "Username field not found");
        Assertions.assertFalse(driver.findElements(By.id("password")).isEmpty(),
                "Password field not found");
        Assertions.assertFalse(driver.findElements(By.cssSelector("button[type='submit'], button[id='login']")).isEmpty(),
                "Login button not found");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        performLogin(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("home"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("home"),
                "Login did not redirect to home page");
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        By userField = By.id("username");
        By passField = By.id("password");
        By loginBtn = By.cssSelector("button[type='submit'], button[id='login']");

        wait.until(ExpectedConditions.elementToBeClickable(userField)).clear();
        driver.findElement(userField).sendKeys("invalid");
        driver.findElement(passField).clear();
        driver.findElement(passField).sendKeys("wrong");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        By errorMsg = By.id("error");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
        Assertions.assertTrue(error.getText().toLowerCase().contains("invalid"),
                "Expected error message for invalid credentials not displayed");
    }

    @Test
    @Order(5)
    public void testSortingOptions() {
        performLogin(USERNAME, PASSWORD);
        By sortDropdown = By.id("sort");
        By firstItem = By.css(".product .productName");

        String[] optionValues = {"Name", "Category", "Price"};
        String previousFirst = "";

        for (String val : optionValues) {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
            dropdown.findElement(By.xpath(String.format(".//option[text()='%s']", val))).click();

            WebElement first = wait.until(ExpectedConditions.visibilityOfElementLocated(firstItem));
            String currentFirst = first.getText();

            if (!previousFirst.isEmpty()) {
                Assertions.assertNotEquals(previousFirst, currentFirst,
                        "Sorting option '" + val + "' did not change first item");
            }
            previousFirst = currentFirst;
        }
    }

    @Test
    @Order(6)
    public void testAllItemsNavigation() {
        performLogin(USERNAME, PASSWORD);
        By allItemsLink = By.linkText("All Products");
        wait.until(ExpectedConditions.elementToBeClickable(allItemsLink)).click();

        wait.until(ExpectedConditions.urlContains("products"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("products"),
                "All Items link did not navigate to products page");
    }

    @Test
    @Order(7)
    public void testAboutExternalLink() {
        performLogin(USERNAME, PASSWORD);
        String originalHandle = currentWindowHandle();

        By aboutLink = By.linkText("About");
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
    @Order(8)
    public void testLogout() {
        performLogin(USERNAME, PASSWORD);
        performLogout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.jsp"),
                "Logout did not redirect to login page");
    }

    @Test
    @Order(9)
    public void testResetAppState() {
        performLogin(USERNAME, PASSWORD);
        resetAppState();
        Assertions.assertFalse(driver.findElements(By.cssSelector(".product")).isEmpty(),
                "Reset App State did not restore product list");
    }

    @Test
    @Order(10)
    public void testFooterSocialLinks() {
        performLogin(USERNAME,        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[target='_blank']"));
        Assertions.assertFalse(externalLinks.isEmpty(),
                "No external links found in footer");

        String originalHandle = currentWindowHandle();
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
                    "External link URL does not contain expected domain: " + href);

            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(11)
    public void testAddToCartAndCheckout() {
        performLogin(USERNAME, PASSWORD);

        By addButton = By.cssSelector("button[id^='add-to-cart-']");
        List<WebElement> addButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(addButton));
        Assertions.assertFalse(addButtons.isEmpty(), "No add-to-cart buttons found");
        addButtons.get(0).click();

        By cartBadge = By.id("shopping_cart_badge");
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));
        Assertions.assertEquals("1", badge.getText(), "Cart badge not updated to 1");

        By cartLink = By.id("cart");
        wait.until(ExpectedConditions.elementToBeClickable(cartLink)).click();
        wait.until(ExpectedConditions.urlContains("cart.jsp"));

        By checkoutBtn = By.id("checkout");
        wait.until(ExpectedConditions.elementToBeClickable(checkoutBtn)).click();
        wait.until(ExpectedConditions.urlContains("checkout.jsp"));

        By firstName = By.id("first-name");
        By lastName = By.id("last-name");
        By address = By.id("address");
        By continueBtn = By.id("continue");

        wait.until(ExpectedConditions.visibilityOfElementLocated(firstName)).sendKeys("John");
        wait.until(ExpectedConditions.visibilityOfElementLocated(lastName)).sendKeys("Doe");
        wait.until(ExpectedConditions.visibilityOfElementLocated(address)).sendKeys("123 Test Ave");
        wait.until(ExpectedConditions.elementToBeClickable(continueBtn)).click();

        By finishBtn = By.id("finish");
        wait.until(ExpectedConditions.elementToBeClickable(finishBtn)).click();
        wait.until(ExpectedConditions.urlContains("confirmation.jsp"));

        By confirmationMsg = By.cssSelector(".confirmationMessage");
        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(confirmationMsg));
        Assertions.assertTrue(msg.getText().toLowerCase().contains("thank you"),
                "Checkout confirmation message not displayed");
    }
}