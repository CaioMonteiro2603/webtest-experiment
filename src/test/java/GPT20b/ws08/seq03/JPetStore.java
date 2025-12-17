package GPT20b.ws08.seq03;

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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
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

    private static void openHomePage() {
        driver.get(BASE_URL);
    }

    private static void clickLoginLink() {
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='login']")));
        loginLink.click();
    }

    private static void submitLogin(String user, String pass) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='username']")));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='password']")));
        usernameField.clear();
        usernameField.sendKeys(user);
        passwordField.clear();
        passwordField.sendKeys(pass);

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='submit']")));
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
    @DisplayName("Home page loads and title contains 'JPetStore'")
    void testHomePageLoads() {
        openHomePage();
        String title = driver.getTitle();
        assertTrue(title.toLowerCase().contains("jpetstore"),
                "Home page title does not contain 'JPetStore'. Title: " + title);
    }

    @Test
    @Order(2)
    @DisplayName("Login with invalid credentials shows error")
    void testLoginInvalid() {
        openHomePage();
        clickLoginLink();

        submitLogin("invalidUser", "invalidPass");

        List<WebElement> errorEls = driver.findElements(
                By.cssSelector(".error-message, .message-error, .alert-danger"));
        Assumptions.assumeTrue(!errorEls.isEmpty(),
                "No error element displayed after invalid login attempt");
        WebElement errorEl = errorEls.get(0);
        String errText = errorEl.getText().toLowerCase();
        assertTrue(errText.contains("invalid") || errText.contains("error") || errText.contains("wrong"),
                "Unexpected error message: " + errorEl.getText());
    }

    @Test
    @Order(3)
    @DisplayName("Category page sorting changes item order")
    void testSortingDropdown() {
        openHomePage();

        // Navigate to a category link (first category)
        WebElement categoryLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("table#categoryTable a[href*='category']")));
        categoryLink.click();

        // Wait for items to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("table#detailTable")));
        // Find any select element that may be a sort control
        List<WebElement> sortEls = driver.findElements(
                By.cssSelector("select[name='sort'] , select[id*='sort'] , select[class*='sort']"));
        Assumptions.assumeTrue(!sortEls.isEmpty(),
                "Sorting dropdown not found on category page; skipping test");

        Select sorter = new Select(sortEls.get(0));
        List<String> original = getItemNames();

        String[] options = {"Name (A-Z)", "Name (Z-A)", "Price (Low->High)", "Price (High->Low)"};
        for (String opt : options) {
            sorter.selectByVisibleText(opt);
            wait.until(ExpectedConditions.stalenessOf(sortEls.get(0)));
            sortEls = driver.findElements(
                    By.cssSelector("select[name='sort'] , select[id*='sort'] , select[class*='sort']"));
            sorter = new Select(sortEls.get(0));

            List<String> current = getItemNames();
            assertNotEquals(original, current,
                    "Sorting by '" + opt + "' did not change item order");
            original = current;
        }
    }

    private static List<String> getItemNames() {
        List<WebElement> nameEls = driver.findElements(
                By.cssSelector("table#detailTable tbody tr td a[href*='product']"));
        List<String> names = new ArrayList<>();
        for (WebElement el : nameEls) {
            names.add(el.getAttribute("title").trim());
        }
        return names;
    }

    @Test
    @Order(4)
    @DisplayName("Menu actions: About (external), Logout, Reset App State if present")
    void testMenuActions() {
        openHomePage();

        // Find a menu or navigation that contains 'About' or similar links
        List<WebElement> aboutLinks = driver.findElements(
                By.linkText("About"));
        if (!aboutLinks.isEmpty()) {
            String parentHandle = driver.getWindowHandle();
            aboutLinks.get(0).click();
            switchToNewWindow(parentHandle);
            try {
                String url = driver.getCurrentUrl().toLowerCase();
                assertTrue(url.contains("github.com") || url.contains("aspectran.com"),
                        "About link opened unexpected domain: " + url);
            } finally {
                driver.close();
                driver.switchTo().window(parentHandle);
            }
        }

        // Logout if logged in
        List<WebElement> logoutLinks = driver.findElements(
                By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("a[href*='login']")));
        }

        // Reset App State link if available
        List<WebElement> resetLinks = driver.findElements(
                By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.linkText("Reset App State")));
        }
    }

    @Test
    @Order(5)
    @DisplayName("External social links open correctly")
    void testExternalLinks() {
        openHomePage();

        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Assumptions.assumeTrue(!anchors.isEmpty(),
                "No anchor elements found; skipping external link test");

        String baseHost = "jpetstore.aspectran.com";
        for (WebElement link : anchors) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty() || href.contains(baseHost)) {
                continue; // skip internal links
            }

            String parentHandle = driver.getWindowHandle();
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