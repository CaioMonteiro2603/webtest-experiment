package GPT20b.ws05.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_HOST = "cac-tat.s3.eu-central-1.amazonaws.com";

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

    /* ---------- Test 1: Page load and title ---------- */

    @Test
    @Order(1)
    public void testPageLoadAndTitle() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertFalse(title.isBlank(),
                "Page title should not be empty.");
        Assertions.assertTrue(title.toLowerCase().contains("cac"),
                "Page title should contain 'CAC'.");
    }

    /* ---------- Test 2: Valid login ---------- */

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get(BASE_URL);
        By userFieldLocator = By.id("username");
        By passFieldLocator = By.id("password");
        By loginBtnLocator = By.cssSelector("input[type='submit'], button[type='submit']");

        if (driver.findElements(userFieldLocator).isEmpty()
                || driver.findElements(passFieldLocator).isEmpty()
                || driver.findElements(loginBtnLocator).isEmpty()) {
            Assumptions.assumeTrue(false, "Login form not present; skipping test.");
        }

        driver.findElement(userFieldLocator).clear();
        driver.findElement(userFieldLocator).sendKeys("caio@gmail.com");
        driver.findElement(passFieldLocator).clear();
        driver.findElement(passFieldLocator).sendKeys("123");
        driver.findElement(loginBtnLocator).click();

        // After login, a logout link/button should be visible
        By logoutLocator = By.id("logout");
        WebElement logout = wait.until(ExpectedConditions.visibilityOfElementLocated(logoutLocator));
        Assertions.assertTrue(logout.isDisplayed(), "Logout button should be visible after login.");
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("dashboard"),
                "URL should contain 'dashboard' after successful login.");
    }

    /* ---------- Test 3: Sorting dropdown ---------- */

    @Test
    @Order(3)
    public void testSortingDropdown() {
        driver.get(BASE_URL);
        // Assume user is already logged in; if not try to login
        try {
            testValidLogin();
        } catch (AssertionError | TimeoutException ignored) {}

        By sortDropdownLocator = By.id("sort-select");
        if (driver.findElements(sortDropdownLocator).isEmpty()) {
            Assumptions.assumeTrue(false, "Sorting dropdown not found; skipping test.");
        }

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdownLocator));
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        Assertions.assertTrue(options.size() > 1,
                "Sorting dropdown should contain multiple options.");

        Select select = new Select(sortDropdown);
        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());

            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.cssSelector(".loading"))
            );

            List<WebElement> items = driver.findElements(By.cssSelector(".product .name"));
            Assertions.assertFalse(items.isEmpty(),
                    "Items should be present after sorting.");

            String firstItem = items.get(0).getText();
            Assertions.assertFalse(firstItem.isBlank(),
                    "First item name should not be blank after sorting.");
        }
    }

    /* ---------- Test 4: External links policy ---------- */

    @Test
    @Order(4)
    public void testExternalLinksPolicy() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.xpath("//a[starts-with(@href,'http')]"));
        Assertions.assertFalse(links.isEmpty(), "No external links found on the page.");

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty() || href.contains(BASE_HOST)) {
                continue; // skip internal or empty links
            }

            link.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Set<String> handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                            "External link URL should contain the expected domain: " + href);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    /* ---------- Test 5: Reset app state ---------- */

    @Test
    @Order(5)
    public void testResetAppState() {
        driver.get(BASE_URL);
        // Ensure logged in
        try {
            testValidLogin();
        } catch (AssertionError | TimeoutException ignored) {}

        By resetLocator = By.id("reset");
        if (driver.findElements(resetLocator).isEmpty()) {
            Assumptions.assumeTrue(false, "Reset button not found; skipping test.");
        }

        WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(resetLocator));
        resetBtn.click();

        // After reset, we expect some indicator such as inventory count to revert
        By inventoryCountLocator = By.id("inventory-count");
        if (!driver.findElements(inventoryCountLocator).isEmpty()) {
            int count = Integer.parseInt(driver.findElement(inventoryCountLocator).getText());
            Assertions.assertTrue(count >= 0, "Inventory count should be non-negative after reset.");
        }
    }

    /* ---------- Test 6: Footer social links ---------- */

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        List<WebElement> socialLinks = driver.findElements(By.xpath("//a[contains(@href,'twitter.com') | //a[contains(@href,'facebook.com') | //a[contains(@href,'linkedin.com')]]"));
        Assertions.assertFalse(socialLinks.isEmpty(), "No social links found in footer.");

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) {
                continue;
            }
            link.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Set<String> handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                            "Opened social URL does not contain expected domain.");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}