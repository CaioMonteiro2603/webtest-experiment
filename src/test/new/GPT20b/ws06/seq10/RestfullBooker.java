package GPT20b.ws06.seq10;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.net.URI;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

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

    private static boolean elementPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    private static WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private static void openExternalLink(By locator, String expectedDomain) {
        String originalHandle = driver.getWindowHandle();
        driver.findElement(locator).click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(originalHandle))
                .findFirst()
                .orElseThrow();
        driver.switchTo().window(newHandle);
        Assertions.assertTrue(
                driver.getCurrentUrl().contains(expectedDomain),
                "External link URL does not contain expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    private static void loginIfNeeded() {
        if (!driver.getCurrentUrl().contains("/dashboard")) {
            driver.get(BASE_URL);
        }
        if (!elementPresent(By.id("logout")) && !elementPresent(By.linkText("Logout"))) {
            login();
        }
    }

    private static void login() {
        boolean usernamePresent =
                !driver.findElements(By.xpath("//input[@placeholder='Username']")).isEmpty()
                || !driver.findElements(By.name("username")).isEmpty()
                || !driver.findElements(By.id("username")).isEmpty()
                || !driver.findElements(By.xpath("//input[@name='username']")).isEmpty();

        if (!usernamePresent) {
            driver.get("https://automationintesting.online/#/admin");
        }

        usernamePresent =
                !driver.findElements(By.xpath("//input[@placeholder='Username']")).isEmpty()
                || !driver.findElements(By.name("username")).isEmpty()
                || !driver.findElements(By.id("username")).isEmpty()
                || !driver.findElements(By.xpath("//input[@name='username']")).isEmpty();

        Assertions.assertTrue(
                usernamePresent,
                "Username field not found"
        );

        By usernameLocator = locateFirst(By.xpath("//input[@placeholder='Username']"),
                By.name("username"), By.id("username"), By.xpath("//input[@name='username']"));
        By passwordLocator = locateFirst(By.xpath("//input[@placeholder='Password']"),
                By.name("password"), By.id("password"), By.xpath("//input[@name='password']"));
        By submitLocator = locateFirst(By.xpath("//button[@type='submit']"),
                By.id("login"), By.cssSelector("button[type='submit']"), By.xpath("//button[contains(text(),'Login')]"));

        WebElement userField = waitClickable(usernameLocator);
        WebElement passField = waitClickable(passwordLocator);
        WebElement loginBtn = waitClickable(submitLocator);

        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        loginBtn.click();

        Assertions.assertTrue(
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(By.id("logout")),
                        ExpectedConditions.visibilityOfElementLocated(By.linkText("Logout")))),
                "Login did not redirect to dashboard");
    }

    private static By locateFirst(By... locators) {
        for (By locator : locators) {
            if (driver.findElements(locator).size() > 0) {
                return locator;
            }
        }
        throw new RuntimeException("No matching locator found");
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testLoginValid() {
        driver.get(BASE_URL);
        login();
        Assertions.assertTrue(
                driver.findElements(By.id("logout")).size() > 0 ||
                        driver.findElements(By.linkText("Logout")).size() > 0x
                "Logout button not present after login");
        logout();
    }

    @Test
    @Order(2)
    public void testLoginInvalid() {
        driver.get("https://automationintesting.online/#/admin");
        
        By usernameLocator = locateFirst(By.xpath("//input[@placeholder='Username']"),
                By.name("username"), By.id("username"), By.xpath("//input[@name='username']"));
        By passwordLocator = locateFirst(By.xpath("//input[@placeholder='Password']"),
                By.name("password"), By.id("password"), By.xpath("//input[@name='password']"));
        By submitLocator = locateFirst(By.xpath("//button[@type='submit']"),
                By.id("login"), By.cssSelector("button[type='submit']"), By.xpath("//button[contains(text(),'Login')]"));

        WebElement userField = waitClickable(usernameLocator);
        WebElement passField = waitClickable(passwordLocator);
        WebElement loginBtn = waitClickable(submitLocator);

        userField.clear();
        userField.sendKeys("wrong_user");
        passField.clear();
        passField.sendKeys("wrong_pass");
        loginBtn.click();

        Assertions.assertTrue(
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error"))).isDisplayed(),
                "Error message not displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        loginIfNeeded();

        // Find the first select that could be a sorting dropdown
        By sortLocator = locateFirst(By.id("sortBy"),
                By.xpath("//select[contains(@class,'sort') or contains(@id,'sort')]"),
                By.cssSelector("select"));

        Assumptions.assumeTrue(elementPresent(sortLocator), "Sorting dropdown not found");

        WebElement sortDropdown = waitClickable(sortLocator);
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() > 1, "Sorting dropdown has insufficient options");

        // Capture initial order
        List<WebElement> itemsBefore = findProductElements();
        Assertions.assertFalse(itemsBefore.isEmpty(), "No product items found before sorting");
        String firstBefore = itemsBefore.get(0).getText();

        for (int i = 0; i < options.size(); i++) {
            String value = options.get(i).getAttribute("value");
            if (value == null || value.isEmpty()) continue;
            options.get(i).click();

            wait.until(ExpectedConditions.stalenessOf(itemsBefore.get(0)));

            List<WebElement> itemsAfter = findProductElements();
            String firstAfter = itemsAfter.get(0).getText();
            Assertions.assertNotEquals(
                    firstBefore,
                    firstAfter,
                    "Sorting option '" + options.get(i).getText() + "' did not change order");
            firstBefore = firstAfter;
            itemsBefore = itemsAfter;
        }
        logout();
    }

    @Test
    @Order(4)
    public void testBurgerMenuOptions() {
        loginIfNeeded();

        By menuButtonLocator = locateFirst(By.id("burger-menu"),
                By.id("openMenu"),
                By.className("navbar-toggler"),
                By.cssSelector("button[aria-label='Toggle navigation']"));

        Assumptions.assumeTrue(elementPresent(menuButtonLocator), "Burger menu button not found");
        WebElement menuBtn = waitClickable(menuButtonLocator);
        menuBtn.click();

        // All Items
        By allItemsLocator = locateFirst(By.linkText("All Items"),
                By.id("menu-all-items"), By.xpath("//a[text()='All Items']"));
        if (elementPresent(allItemsLocator)) {
            waitClickable(allItemsLocator).click();
            Assertions.assertTrue(
                    wait.until(ExpectedConditions.urlContains("products")),
                    "Did not navigate to All Items page");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("/dashboard"));
        }

        // About (external)
        By aboutLocator = locateFirst(By.linkText("About"),
                By.id("menu-about"), By.xpath("//a[text()='About']"));
        if (elementPresent(aboutLocator)) {
            openExternalLink(aboutLocator, "about");
        }

        // Reset App State
        By resetLocator = locateFirst(By.linkText("Reset App State"),
                By.id("menu-reset"), By.xpath("//a[text()='Reset App State']"));
        if (elementPresent(resetLocator)) {
            waitClickable(resetLocator).click();
            Assertions.assertTrue(
                    wait.until(ExpectedConditions.urlContains("/dashboard")),
                    "Reset App State did not keep user on dashboard");
        }

        // Logout
        By logoutLocator = locateFirst(By.linkText("Logout"),
                By.id("menu-logout"), By.xpath("//a[text()='Logout']"));
        if (elementPresent(logoutLocator)) {
            waitClickable(logoutLocator).click();
            Assertions.assertTrue(
                    wait.until(ExpectedConditions.urlToBe(BASE_URL)),
                    "Logout did not redirect to home page");
        }
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        By footerLinksLocator = By.cssSelector("footer a[href]");
        Assumptions.assumeTrue(elementPresent(footerLinksLocator), "No footer links found");

        List<WebElement> links = driver.findElements(footerLinksLocator);
        String internalHost = extractHost(BASE_URL);

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            String host = extractHost(href);
            if (host == null || host.isEmpty()) continue;
            if (internalHost.equals(host)) continue; // skip internal links
            openExternalLink(By.cssSelector("footer a[href='" + href + "']"),
                    host);
        }
    }

    /* ---------- Utility Methods ---------- */

    private static List<WebElement> findProductElements() {
        By selector = locateFirst(
                By.cssSelector(".product-item"),
                By.cssSelector(".inventory_item"),
                By.cssSelector(".card"),
                By.cssSelector("li.product"),
                By.cssSelector(".item"));
        return driver.findElements(selector);
    }

    private static String extractHost(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private static void logout() {
        By logoutLocator = locateFirst(By.id("logout"),
                By.linkText("Logout"));
        if (elementPresent(logoutLocator)) {
            waitClickable(logoutLocator).click();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }
}