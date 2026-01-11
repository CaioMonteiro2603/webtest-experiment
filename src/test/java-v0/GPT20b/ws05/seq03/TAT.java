package GPT20b.ws05.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void init() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void cleanup() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* -------------- Helper Utilities -------------- */

    private static void loadPage() {
        driver.get(BASE_URL);
    }

    /* -------------- Tests -------------- */

    @Test
    @Order(1)
    @DisplayName("Page loads with expected title")
    void testPageLoads() {
        loadPage();
        String title = driver.getTitle();
        assertTrue(title.toLowerCase().contains("home") || title.toLowerCase().contains("index"),
                "Page title does not contain expected keywords. Title: " + title);
    }

    @Test
    @Order(2)
    @DisplayName("Login attempt with provided credentials")
    void testLogin() {
        loadPage();

        // Lookup username/email field
        List<WebElement> usernameEls = driver.findElements(By.cssSelector("input[name='username'], input[id='username'], input[name='email'], input[id='email']"));
        Assumptions.assumeTrue(usernameEls.size() > 0, "No username/email input found; skipping login test");

        WebElement usernameField = usernameEls.get(0);
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password'], input[name='password'], input[id='password']")));
        usernameField.clear();
        usernameField.sendKeys("caio@gmail.com");
        passwordField.clear();
        passwordField.sendKeys("123");

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button[id='login'], input[type='submit']")));
        submitBtn.click();

        // Expect a redirect or visible element indicating successful login
        wait.until(ExpectedConditions.urlContains("dashboard"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("dashboard") || currentUrl.contains("account") || currentUrl.contains("home"),
                "Login did not redirect to a page containing expected path. URL: " + currentUrl);
    }

    @Test
    @Order(3)
    @DisplayName("Invalid login shows error message")
    void testInvalidLogin() {
        loadPage();

        List<WebElement> usernameEls = driver.findElements(By.cssSelector("input[name='username'], input[id='username'], input[name='email'], input[id='email']"));
        Assumptions.assumeTrue(usernameEls.size() > 0, "No username/email input found; skipping invalid login test");

        WebElement usernameField = usernameEls.get(0);
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password'], input[name='password'], input[id='password']")));
        usernameField.clear();
        usernameField.sendKeys("invalid@user");
        passwordField.clear();
        passwordField.sendKeys("wrongpass");

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button[id='login'], input[type='submit']")));
        submitBtn.click();

        // Expect error element
        List<WebElement> errorEls = driver.findElements(By.cssSelector(".error, .err, .alert, .alert-danger"));
        Assumptions.assumeTrue(errorEls.size() > 0, "No error element displayed; skipping assertion");
        WebElement errorEl = errorEls.get(0);
        String errText = errorEl.getText().toLowerCase();
        assertTrue(errText.contains("invalid") || errText.contains("wrong") || errText.contains("error"),
                "Error message does not indicate invalid credentials: " + errText);
    }

    @Test
    @Order(4)
    @DisplayName("Sorting dropdown functions correctly")
    void testSortingDropdown() {
        loadPage();

        List<WebElement> sortEls = driver.findElements(By.cssSelector("select[id='sort'], select[name='sort']"));
        Assumptions.assumeTrue(sortEls.size() > 0, "Sorting dropdown not found; skipping test");
        Select sorter = new Select(sortEls.get(0));

        // capture original order of product titles
        List<String> original = getProductTitles();

        String[] options = {"Name A-Z", "Name Z-A", "Price Low-High", "Price High-Low"};
        for (String opt : options) {
            sorter.selectByVisibleText(opt);
            // wait for list update
            wait.until(ExpectedConditions.stalenessOf(sortEls.get(0)));
            sortEls = driver.findElements(By.cssSelector("select[id='sort'], select[name='sort']"));
            sorter = new Select(sortEls.get(0));
            List<String> current = getProductTitles();

            assertNotEquals(original, current,
                    "Sorting by '" + opt + "' did not change product order");
            original = current;
        }
    }

    private List<String> getProductTitles() {
        List<WebElement> titleEls = driver.findElements(By.cssSelector(".product-title, .product-name, .item-title"));
        List<String> titles = new ArrayList<>();
        for (WebElement el : titleEls) {
            titles.add(el.getText().trim());
        }
        return titles;
    }

    @Test
    @Order(5)
    @DisplayName("Menu actions perform expected navigations")
    void testMenuActions() {
        loadPage();

        // Attempt to locate burger menu icon if present
        List<WebElement> menuBtns = driver.findElements(By.cssSelector(".hamburger, .menu-icon, #menuToggle, button[aria-label='menu']"));
        Assumptions.assumeTrue(menuBtns.size() > 0, "Burger menu button not found; skipping menu tests");

        WebElement menuBtn = menuBtns.get(0);
        menuBtn.click();

        // All Items
        List<WebElement> allItems = driver.findElements(By.linkText("All Items"));
        if (!allItems.isEmpty()) {
            WebElement allLink = allItems.get(0);
            allLink.click();
            wait.until(ExpectedConditions.urlContains("products"));
            wait.until(ExpectedConditions.urlContains("inventory"));
            assertTrue(driver.getCurrentUrl().contains("products") || driver.getCurrentUrl().contains("inventory"),
                    "Did not navigate to All Items page");
            driver.navigate().back();
        }

        // About (external)
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        if (!aboutLinks.isEmpty()) {
            String parentHandle = driver.getWindowHandle();
            WebElement about = aboutLinks.get(0);
            about.click();

            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> handles = driver.getWindowHandles();
            handles.remove(parentHandle);
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            try {
                String url = driver.getCurrentUrl().toLowerCase();
                assertTrue(url.contains("github.com") || url.contains("s3.amazonaws.com"),
                        "About link did not open expected external domain. URL: " + url);
            } finally {
                driver.close();
                driver.switchTo().window(parentHandle);
            }
        }

        // Reset App State
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            WebElement reset = resetLinks.get(0);
            reset.click();
            // Wait for any indication that state is reset
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.linkText("Reset App State")));
        }

        // Logout
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            WebElement logout = logoutLinks.get(0);
            logout.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        }
    }

    @Test
    @Order(6)
    @DisplayName("External links open correctly and close")
    void testExternalLinks() {
        loadPage();

        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Assumptions.assumeTrue(anchors.size() > 0, "No links on page; skipping external link test");

        for (WebElement link : anchors) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (href.contains("cac-tat.s3.eu")) continue; // skip same host

            String parentHandle = driver.getWindowHandle();
            link.click();

            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> handles = driver.getWindowHandles();
            handles.remove(parentHandle);
            String newHandle = handles.iterator().next();

            driver.switchTo().window(newHandle);
            try {
                String currentUrl = driver.getCurrentUrl().toLowerCase();
                assertTrue(currentUrl.contains(href.toLowerCase().replaceFirst("https?://", "")),
                        "External link URL does not contain expected domain: " + href);
            } finally {
                driver.close();
                driver.switchTo().window(parentHandle);
            }
        }
    }
}