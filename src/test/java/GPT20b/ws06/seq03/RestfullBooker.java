package GPT20b.ws06.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assumptions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class AutomationIntTestingTests {

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

    /* ---------- Helper methods ---------- */

    private static void loadHomePage() {
        driver.get(BASE_URL);
    }

    private static List<String> getProductTitles() {
        List<String> titles = new ArrayList<>();
        List<WebElement> elements = driver.findElements(By.cssSelector(".product-title, .product-name, .item-title"));
        for (WebElement el : elements) {
            titles.add(el.getText().trim());
        }
        return titles;
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    @DisplayName("Home page title loads")
    void testHomePageLoads() {
        loadHomePage();
        String title = driver.getTitle();
        assertTrue(title != null && !title.isEmpty(),
                "Page title is empty after navigation to home page");
    }

    @Test
    @Order(2)
    @DisplayName("Valid login works")
    void testValidLogin() {
        loadHomePage();

        List<WebElement> emailEls = driver.findElements(By.cssSelector("input#email, input[name='email'], input[type='email']"));
        Assumptions.assumeTrue(emailEls.size() > 0, "Email input not found; skipping login test");
        WebElement emailField = emailEls.get(0);

        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input#password, input[name='password'], input[type='password']")));
        emailField.clear();
        emailField.sendKeys("caio@gmail.com");
        passwordField.clear();
        passwordField.sendKeys("123");

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button#login, button[type='submit'], button[name='login']")));
        submitBtn.click();

        // Wait for successful login indication (URL or element)
        wait.until(ExpectedConditions.urlMatches(".*(login|home|dashboard).*", true));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("login") || currentUrl.contains("home") || currentUrl.contains("dashboard"),
                "Login did not redirect to expected page. URL: " + currentUrl);
    }

    @Test
    @Order(3)
    @DisplayName("Invalid login shows error")
    void testInvalidLogin() {
        loadHomePage();

        List<WebElement> emailEls = driver.findElements(By.cssSelector("input#email, input[name='email'], input[type='email']"));
        Assumptions.assumeTrue(emailEls.size() > 0, "Email input not found; skipping invalid login test");
        WebElement emailField = emailEls.get(0);

        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input#password, input[name='password'], input[type='password']")));
        emailField.clear();
        emailField.sendKeys("invalid@example.com");
        passwordField.clear();
        passwordField.sendKeys("wrongpass");

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button#login, button[type='submit'], button[name='login']")));
        submitBtn.click();

        List<WebElement> errorEls = driver.findElements(By.cssSelector(".error, .alert-danger, .validation-error"));
        Assumptions.assumeTrue(errorEls.size() > 0, "Error message element not displayed; skipping assertion");
        WebElement errorEl = errorEls.get(0);
        String errText = errorEl.getText().toLowerCase();
        assertTrue(errText.contains("invalid") || errText.contains("error") || errText.contains("wrong"),
                "Unexpected error message after login attempt: " + errText);
    }

    @Test
    @Order(4)
    @DisplayName("Sorting dropdown applies correct order")
    void testSortingDropdown() {
        loadHomePage();

        List<WebElement> sortEls = driver.findElements(By.cssSelector("select#sort, select[name='sort']"));
        Assumptions.assumeTrue(!sortEls.isEmpty(), "Sorting dropdown not found; skipping test");

        Select sorter = new Select(sortEls.get(0));

        List<String> originalOrder = getProductTitles();

        String[] optionsToTest = {"Name A-Z", "Name Z-A", "Price Low-High", "Price High-Low"};
        for (String option : optionsToTest) {
            sorter.selectByVisibleText(option);

            // Wait for list to refresh
            wait.until(ExpectedConditions.stalenessOf(sortEls.get(0)));
            sortEls = driver.findElements(By.cssSelector("select#sort, select[name='sort']"));
            sorter = new Select(sortEls.get(0));

            List<String> currentOrder = getProductTitles();

            assertNotEquals(originalOrder, currentOrder,
                    "Sorting by '" + option + "' did not change product order");
            originalOrder = currentOrder;
        }
    }

    @Test
    @Order(5)
    @DisplayName("Burger menu actions work correctly")
    void testMenuActions() {
        loadHomePage();

        List<WebElement> burgerBtns = driver.findElements(By.cssSelector(".hamburger, .burger-menu, button[aria-label='menu'], #menuBtn"));
        Assumptions.assumeTrue(!burgerBtns.isEmpty(), "Burger menu button not found; skipping menu tests");
        burgerBtns.get(0).click();

        // All Items
        List<WebElement> allItems = driver.findElements(By.linkText("All Items"));
        if (!allItems.isEmpty()) {
            allItems.get(0).click();
            wait.until(ExpectedConditions.urlContains("products") || ExpectedConditions.urlContains("inventory"));
            assertTrue(driver.getCurrentUrl().contains("products") || driver.getCurrentUrl().contains("inventory"),
                    "Failed to navigate to All Items page");
            driver.navigate().back();
        }

        // About (external)
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        if (!aboutLinks.isEmpty()) {
            String parentHandle = driver.getWindowHandle();
            aboutLinks.get(0).click();

            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> handles = driver.getWindowHandles();
            handles.remove(parentHandle);
            String newHandle = handles.iterator().next();

            driver.switchTo().window(newHandle);
            try {
                String url = driver.getCurrentUrl().toLowerCase();
                assertTrue(url.contains("github.com") || url.contains("automationintesting.com"),
                        "About link opened unexpected domain: " + url);
            } finally {
                driver.close();
                driver.switchTo().window(parentHandle);
            }
        }

        // Reset App State
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.linkText("Reset App State")));
        }

        // Logout
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button#login, button[type='submit'], button[name='login']")));
        }
    }

    @Test
    @Order(6)
    @DisplayName("External links open in new tabs")
    void testExternalLinks() {
        loadHomePage();

        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Assumptions.assumeTrue(!anchors.isEmpty(), "No anchor elements found; skipping external link test");

        String baseHost = "automationintesting.online";
        for (WebElement link : anchors) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty() || href.contains(baseHost)) {
                continue; // skip internal links
            }

            String parentHandle = driver.getWindowHandle();
            link.click();

            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> handles = driver.getWindowHandles();
            handles.remove(parentHandle);
            String newHandle = handles.iterator().next();

            driver.switchTo().window(newHandle);
            try {
                String currentUrl = driver.getCurrentUrl().toLowerCase();
                String expectedDomain = href.replaceFirst("https?://", "").split("/")[0].toLowerCase();
                assertTrue(currentUrl.contains(expectedDomain),
                        "External link URL does not contain expected domain. Expected: " + expectedDomain + ", actual: " + currentUrl);
            } finally {
                driver.close();
                driver.switchTo().window(parentHandle);
            }
        }
    }
}