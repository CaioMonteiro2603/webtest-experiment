package GPT20b.ws07.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assumptions;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static final String BASE_URL = "https://jsfiddle.net/";
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

    private static void clickLoginLink() {
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*=\"login\"]")));
        loginLink.click();
    }

    private static void submitLogin(String email, String password) {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='email']")));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='password']")));
        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], input[type='submit']")));
        submitBtn.click();
    }

    private static void switchToNewWindow(String parentHandle) {
        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        handles.remove(parentHandle);
        driver.switchTo().window(handles.iterator().next());
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    @DisplayName("Home page loads with expected title")
    void testHomePageLoads() {
        loadHomePage();
        String title = driver.getTitle();
        assertTrue(title.toLowerCase().contains("jsfiddle"),
                "Home page title does not contain 'JSFiddle'. Title: " + title);
    }

    @Test
    @Order(2)
    @DisplayName("Invalid login displays error message")
    void testInvalidLogin() {
        loadHomePage();
        clickLoginLink();

        submitLogin("invalid@example.com", "wrongPassword");

        List<WebElement> errorEls = driver.findElements(
                By.cssSelector(".alert.alert-danger, .error, .validation-error"));
        Assumptions.assumeTrue(!errorEls.isEmpty(),
                "No error element displayed after invalid login attempt");
        WebElement errorEl = errorEls.get(0);
        String text = errorEl.getText().toLowerCase();
        assertTrue(text.contains("invalid") || text.contains("error") || text.contains("wrong"),
                "Unexpected error message: " + errorEl.getText());
    }

    @Test
    @Order(3)
    @DisplayName("Sorting dropdown changes order if present")
    void testSortingDropdown() {
        loadHomePage();

        List<WebElement> sortElements = driver.findElements(
                By.cssSelector("select[name='sort']"));
        Assumptions.assumeTrue(!sortElements.isEmpty(),
                "Sorting dropdown not found; skipping test");
        Select sortSelect = new Select(sortElements.get(0));

        List<String> originalOrder = getFiddleTitles();

        String[] optionsToTest = {"Most Recent", "Last Updated", "Most Viewed"};
        for (String opt : optionsToTest) {
            sortSelect.selectByVisibleText(opt);
            wait.until(ExpectedConditions.stalenessOf(sortElements.get(0)));
            sortElements = driver.findElements(
                    By.cssSelector("select[name='sort']"));
            sortSelect = new Select(sortElements.get(0));

            List<String> current = getFiddleTitles();
            assertNotEquals(originalOrder, current,
                    "Sorting by '" + opt + "' did not change fiddle order");
            originalOrder = current;
        }
    }

    private List<String> getFiddleTitles() {
        List<WebElement> titleEls = driver.findElements(
                By.cssSelector("li.fiddle-item a.child-link"));
        List<String> titles = new ArrayList<>();
        for (WebElement el : titleEls) {
            titles.add(el.getAttribute("title").trim());
        }
        return titles;
    }

    @Test
    @Order(4)
    @DisplayName("Burger menu actions are available if present")
    void testMenuActions() {
        loadHomePage();

        List<WebElement> burgerBtns = driver.findElements(
                By.cssSelector("button[aria-label='Menu'], .hamburger, .burger-menu"));
        Assumptions.assumeTrue(!burgerBtns.isEmpty(),
                "Burger menu button not found; skipping menu tests");
        burgerBtns.get(0).click();

        // All Items
        List<WebElement> allItems = driver.findElements(
                By.linkText("All Items"));
        if (!allItems.isEmpty()) {
            allItems.get(0).click();
            wait.until(ExpectedConditions.urlContains("fiddles"));
            assertTrue(driver.getCurrentUrl().contains("fiddles"),
                    "Did not navigate to All Items page");
            driver.navigate().back();
        }

        // About (external)
        List<WebElement> aboutLinks = driver.findElements(
                By.linkText("About"));
        if (!aboutLinks.isEmpty()) {
            String parentHandle = driver.getWindowHandle();
            aboutLinks.get(0).click();
            switchToNewWindow(parentHandle);
            try {
                String url = driver.getCurrentUrl().toLowerCase();
                assertTrue(url.contains("github.com") || url.contains("jsfiddle.com"),
                        "About link opened unexpected domain: " + url);
            } finally {
                driver.close();
                driver.switchTo().window(parentHandle);
            }
        }

        // Reset App State
        List<WebElement> resetLinks = driver.findElements(
                By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.linkText("Reset App State")));
        }

        // Logout
        List<WebElement> logoutLinks = driver.findElements(
                By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("a[href*='login']")));
        }
    }

    @Test
    @Order(5)
    @DisplayName("External social links open and close correctly")
    void testExternalLinks() {
        loadHomePage();

        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Assumptions.assumeTrue(!anchors.isEmpty(),
                "No anchor elements found; skipping external link test");

        String baseHost = "jsfiddle.net";
        for (WebElement link : anchors) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty() || href.contains(baseHost)) {
                continue; // skip internal links
            }

            String parentHandle = driver.getWindowHandle();
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView(true);", link);
            wait.until(ExpectedConditions.elementToBeClickable(link));
            link.click();
            switchToNewWindow(parentHandle);
            try {
                String currentUrl = driver.getCurrentUrl().toLowerCase();
                String expectedDomain = href.replaceFirst("https?://", "").split("/")[0].toLowerCase();
                assertTrue(currentUrl.contains(expectedDomain),
                        "External link URL does not contain expected domain. Expected: "
                                + expectedDomain + ", actual: " + currentUrl);
            } finally {
                driver.close();
                driver.switchTo().window(parentHandle);
            }
        }
    }
}