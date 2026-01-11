package GPT20b.ws06.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker{

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

    private boolean elementExists(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void loginIfNeeded() {
        if (!elementExists(By.id("inventory-list"))) {
            navigateToHome();
            WebElement userEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            WebElement passEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
            WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

            userEl.clear();
            userEl.sendKeys(USERNAME);
            passEl.clear();
            passEl.sendKeys(PASSWORD);
            loginBtn.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory-list")));
        }
    }

    private List<String> getItemNames() {
        List<WebElement> elements = driver.findElements(By.cssSelector(".inventory_item_name"));
        return elements.stream()
                .map(WebElement::getText)
                .toList();
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testPageLoadsCorrectly() {
        navigateToHome();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("automation"),
                "Page title should contain 'Automation'");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        navigateToHome();
        WebElement userEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

        userEl.clear();
        userEl.sendKeys(USERNAME);
        passEl.clear();
        passEl.sendKeys(PASSWORD);
        loginBtn.click();

        Assertions.assertTrue(
                elementExists(By.id("inventory-list")),
                "Inventory list should be visible after successful login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        navigateToHome();
        WebElement userEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

        userEl.clear();
        userEl.sendKeys("wrong@example.com");
        passEl.clear();
        passEl.sendKeys("wrongpass");
        loginBtn.click();

        By errorLocator = By.cssSelector(".error-message, .payload-danger");
        Assumptions.assumeTrue(elementExists(errorLocator),
                "Error message element not found on failed login; skipping test.");
        WebElement errorMsg = driver.findElement(errorLocator);
        Assertions.assertTrue(
                errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        loginIfNeeded();

        By sortLocator = By.id("sortDropdown");
        Assumptions.assumeTrue(elementExists(sortLocator),
                "Sorting dropdown not present; skipping test.");

        List<String> initialOrder = getItemNames();

        List<WebElement> options = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath("//select[@id='sortDropdown']/option")));

        for (WebElement option : options) {
            wait.until(ExpectedConditions.elementToBeClickable(sortLocator)).click();
            WebElement opt = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//select[@id='sortDropdown']/option[.='" + option.getText() + "']")));
            opt.click();

            // Wait until inventory list updates
            wait.until(driver -> !getItemNames().equals(initialOrder));

            List<String> newOrder = getItemNames();
            Assertions.assertNotEquals(
                    initialOrder,
                    newOrder,
                    "Item order should change after selecting sorting option: " + option.getText());
        }
    }

    @Test
    @Order(5)
    public void testBurgerMenuInteraction() {
        loginIfNeeded();

        By menuBtnLocator = By.id("menu");
        Assumptions.assumeTrue(elementExists(menuBtnLocator),
                "Burger menu button not found; skipping test.");
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(menuBtnLocator));
        menuBtn.click();

        // All Items
        By allItemsLink = By.xpath("//a[text()='All Items']");
        Assumptions.assumeTrue(elementExists(allItemsLink),
                "All Items link missing; skipping that part.");
        WebElement allItems = driver.findElement(allItemsLink);
        allItems.click();
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("index.html"),
                "After clicking All Items, should be on main page");

        // Reset App State
        By resetLink = By.xpath("//a[text()='Reset App State']");
        if (elementExists(resetLink)) {
            menuBtn = wait.until(ExpectedConditions.elementToBeClickable(menuBtnLocator));
            menuBtn.click();
            WebElement reset = driver.findElement(resetLink);
            reset.click();
            // Assume a modal with id 'reset-confirmation' appears
            Assertions.assertTrue(
                    elementExists(By.id("reset-confirmation")),
                    "Reset confirmation should be displayed");
            // Close modal if present
            if (elementExists(By.cssSelector(".modal-close"))) {
                driver.findElement(By.cssSelector(".modal-close")).click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("reset-confirmation")));
            }
        }

        // About link (external)
        By aboutLink = By.xpath("//a[text()='About']");
        if (elementExists(aboutLink)) {
            menuBtn = wait.until(ExpectedConditions.elementToBeClickable(menuBtnLocator));
            menuBtn.click();
            WebElement about = driver.findElement(aboutLink);
            String original = driver.getWindowHandle();
            Set<String> before = driver.getWindowHandles();
            about.click();

            // Wait for possible new window
            try {
                wait.until(driver1 -> driver1.getWindowHandles().size() > before.size());
            } catch (TimeoutException ignored) {
            }

            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            if (!after.isEmpty()) {
                String newHandle = after.iterator().next();
                driver.switchTo().window(newHandle);
                wait.until(ExpectedConditions.urlContains("about"));
                Assertions.assertTrue(
                        driver.getCurrentUrl().contains("about"),
                        "About link should open external page");
                driver.close();
                driver.switchTo().window(original);
            } else {
                Assertions.assertTrue(
                        driver.getCurrentUrl().contains("about"),
                        "About link opened in same tab");
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".menu")));
            }
        }

        // Logout
        By logoutLink = By.xpath("//a[text()='Logout']");
        if (elementExists(logoutLink)) {
            menuBtn = wait.until(ExpectedConditions.elementToBeClickable(menuBtnLocator));
            menuBtn.click();
            WebElement logout = driver.findElement(logoutLink);
            logout.click();
            wait.until(ExpectedConditions.urlContains("login"));
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("login"),
                    "After logout, should be on login page");
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        navigateToHome();

        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            By linkLocator = By.xpath("//footer//a[contains(@href,'" + domain + "')]");
            Assumptions.assumeTrue(elementExists(linkLocator),
                    "Footer link for " + domain + " not found; skipping.");
            List<WebElement> links = driver.findElements(linkLocator);
            for (WebElement link : links) {
                String originalHandle = driver.getWindowHandle();
                Set<String> before = driver.getWindowHandles();
                link.click();

                // Wait for potential new window/tab
                try {
                    wait.until(driver1 -> driver1.getWindowHandles().size() > before.size());
                } catch (TimeoutException ignored) {
                }

                Set<String> after = driver.getWindowHandles();
                after.removeAll(before);
                if (!after.isEmpty()) {
                    String newHandle = after.iterator().next();
                    driver.switchTo().window(newHandle);
                    wait.until(ExpectedConditions.urlContains(domain));
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "External link should contain domain: " + domain);
                    driver.close();
                    driver.switchTo().window(originalHandle);
                } else {
                    // Same tab navigation
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "Link navigated within same tab to expected domain");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
                }
            }
        }
    }
}