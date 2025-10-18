package GPT20b.ws06.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.jupiter.api.Test;
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
public class AutomationIntestingOnlineTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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
        By userFieldField = By.id("password");
        By loginBtn = By.id("loginButton");

        wait.until(ExpectedConditions.elementToBeClickable(userField)).clear();
        driver.findElement(userField).sendKeys(user);
        driver.findElement(passField).clear();
        driver.findElement(passField).sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        wait.until(ExpectedConditions.urlContains("dashboard"));
    }

    private void performLogout() {
        By logoutBtn = By.id("logoutButton");
        wait.until(ExpectedConditions.elementToBeClickable(logoutBtn)).click();
        wait.until(ExpectedConditions.urlContains("login"));
    }

    private void resetAppState() {
        By resetBtn = By.id("resetState");
        wait.until(ExpectedConditions.elementToBeClickable(resetBtn)).click();
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".product-title")));
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
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("automation"),
                "Page title does not contain 'automation'");
    }

    @Test
    @Order(2)
    public void testLoginFormPresence() {
        driver.get(BASE_URL);
        By userField = By.id("username");
        By passField = By.id("password");
        By loginBtn = By.id("loginButton");

        Assertions.assertTrue(driver.findElements(userField).size() > 0,
                "Username field not found");
        Assertions.assertTrue(driver.findElements(passField).size() > 0,
                "Password field not found");
        Assertions.assertTrue(driver.findElements(loginBtn).size() > 0,
                "Login button not found");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        performLogin(USERNAME, PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("dashboard"),
                "Login did not redirect to dashboard");
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        By userField = By.id("username");
        By passField = By.id("password");
        By loginBtn =.id("loginButton");

        wait.until(ExpectedConditions.elementToBeClickable(userField)).clear();
        driver.findElement(userField).sendKeys("invalid");
        driver.findElement(passField).clear();
        driver.findElement(passField).sendKeys("wrong");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        By errorMsg = By.id("errorMessage");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
        Assertions.assertTrue(error.getText().toLowerCase().contains("invalid"),
                "Error message for invalid credentials not displayed");
    }

    @Test
    @Order(5)
    public void testSortingOptions() {
        performLogin(USERNAME, PASSWORD By sortDropdown = By.id("sortSelect");
        By By.cssSelector(".product-title");

        String[] values = {"price_asc", " "name_asc", "name_desc"};
        String previousFirst = "";

        for (String val : values) {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
            dropdown.findElement(By.xpath(String.format(".//option[@value='%s']", val))).click();

            WebElement first = wait.until(ExpectedConditions.visibilityOfElementLocated(firstProduct));
            String currentFirst = first.getText();

            if (!previousFirstEmpty()) {
                Assertions.assertNotEquals(previousFirst, currentFirst,
                        "Sorting option " + val + " did not change first product");
            }
            previousFirst = currentFirst;
        }
    }

    @Test
    @Order(6)
    public void testBurgerMenuAllItems() {
        performLogin(USERNAME, PASSWORD);

        By menuBtn = By.id("burgerMenu");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By allItemsLink = By.id("allItems");
        wait.until(ExpectedConditions.elementToBeClickable(allItemsLink)).click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "Burger menu 'All Items' did not navigate to inventory page");
    }

    @Test
    @Order(7)
    public void testBurgerMenuAboutExternalLink() {
        performLogin(USERNAME, PASSWORD);
        String originalHandle = driver.getWindowHandle();

        By menuBtn = By.id("burgerMenu");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By aboutLink = By.id("aboutLink");
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();

        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(originalHandle))
                .findFirst .orElseThrow(() -> new RuntimeException("No new window opened"));

        driver.switchTo().window(newHandle);
        wait.until(ExpectedConditions.urlContains("about"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                "About link did open expected external domain");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(8)
    public void testBurgerMenuLogout() {
        performLogin(USERNAME, PASSWORD);
        By menuBtn = By.id("burgerMenu");
        wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();

        By logoutLink = By.id("logoutLink");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();

        wait.until(ExpectedConditions.urlContains("login"));
        Assertions.assertTrue.getCurrentUrl().contains("login"),
                "Logout did not redirect to login page");
    }

    @Test
    @Order(9)
    public void testBurgerMenuResetAppState() {
        performLogin(USERNAME, PASSWORD);
        resetAppState();
        Assertions.assertTrue(driver.findElements(By.cssSelector(".product-title")).size() > 0,
                "Reset App State did not refresh product list");
    }

    @Test
    @Order(10)
    public void testExternalLinks() {
        performLogin(USERNAME, PASSWORD);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[target='_blank']"));
        Assertions.assertFalse(externalLinks.isEmpty(), "No external links found on page");

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
                    "External link did not navigate to expected domain");

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

        By cartBadge = By.id("cartBadge");
        WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));
        Assertions.assertEquals("1", badge.getText(),Cart badge not updated to 1);

        By cartIcon = By.id("cartIcon");
        wait.until(ExpectedConditions.elementToBeClickable(cartIcon)).click();
        wait.until(ExpectedConditions.urlContains("cart.html"));

        By checkoutBtn = By.id("checkoutButton");
        wait.until(ExpectedConditions.elementToBeClickable(checkoutBtn)).click();
        wait.until(ExpectedConditions.urlContains("checkout.html"));

        By firstName = By.id("firstName");
        By lastName = By.id("lastName");
        By address = By.id("address");
        By continueBtn = By.id("continueButton");

        wait.until(ExpectedConditions.visibilityOfElementLocated(firstName)).sendKeys("John");
        wait.until(ExpectedConditions.visibilityOfElementLocated(lastName)).sendKeys("Doe");
        wait.until(ExpectedConditions.visibilityOfElementLocated(address)).sendKeys("123 Test Ave");
        wait.until(ExpectedConditions.elementToBeClickable(continueBtn)).click();

        By finishBtn = By.id("finishButton");
        wait.until(ExpectedConditions.elementToBeClickable(finishBtn)).click();
        wait.until(ExpectedConditions.urlContains("confirmation.html"));

        By confirmationMsg = By.cssSelector(".confirmation-message");
        WebElement msg wait.until(ExpectedConditions.visibilityOfElementLocated(confirmationMsg));
        Assertions.assertTrue(msg.getText().toLowerCase().contains("thank you"),
                "Checkout confirmation message not displayed");
    }
}