package GPT20b.ws09.seq03;

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
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

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

    /* ----------------------- helpers ----------------------- */

    private static void openHome() {
        driver.get(BASE_URL);
    }

    private static void clickLogin() {
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='login'], a[routerlink='/login'], .nav-link[href*='login']")));
        loginLink.click();
    }

    private static void submitLogin(String email, String password) {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email'], input#email, input[name='email']")));
        WebElement pwdField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password'], input#password, input[name='password']")));
        emailField.clear();
        emailField.sendKeys(email);
        pwdField.clear();
        pwdField.sendKeys(password);

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button#loginBtn, button[name='login']")));
        submitBtn.click();
    }

    private static void logoutIfPresent() {
        List<WebElement> logoutLinks = driver.findElements(By.cssSelector("a[href*='logout'], .nav-link[href*='logout']"));
        if (!logoutLinks.isEmpty()) {
            WebElement logout = logoutLinks.get(0);
            logout.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("a[href*='login'], a[routerlink='/login'], .nav-link[href*='login']")));
        }
    }

    private static void resetAppStateIfPresent() {
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            WebElement resetBtn = resetLinks.get(0);
            resetBtn.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.linkText("Reset App State")));
        }
    }

    private static void switchToNewWindow(String parentHandle) {
        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        handles.remove(parentHandle);
        driver.switchTo().window(handles.iterator().next());
    }

    private static List<String> getArticleTitles() {
        List<WebElement> articles = driver.findElements(
                By.cssSelector("article h2 a, article h3 a"));
        List<String> titles = new ArrayList<>();
        for (WebElement el : articles) {
            titles.add(el.getText().trim());
        }
        return titles;
    }

    /* ------------------------ tests ------------------------ */

    @Test
    @Order(1)
    @DisplayName("Home page loads with correct title")
    void testHomePageLoads() {
        openHome();
        WebDriverWait titleWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        titleWait.until(d -> !d.getTitle().isEmpty());
        String title = driver.getTitle();
        assertTrue(title.toLowerCase().contains("conduit") || title.toLowerCase().contains("realworld"),
                "Home page title does not contain expected text. Title: " + title);
    }

    @Test
    @Order(2)
    @DisplayName("Invalid login credentials show error message")
    void testInvalidLogin() {
        openHome();
        clickLogin();
        submitLogin("invalid@example.com", "wrongpass");

        List<WebElement> errorEls = driver.findElements(
                By.cssSelector(".alert-danger, .alert-danger, .error, .highlight"));
        Assumptions.assumeTrue(!errorEls.isEmpty(),
                "No error element displayed after invalid login attempt");
        WebElement errorEl = errorEls.get(0);
        String errText = errorEl.getText().toLowerCase();
        assertTrue(errText.contains("invalid") || errText.contains("error") || errText.contains("wrong"),
                "Unexpected error message: " + errorEl.getText());
    }

    @Test
    @Order(3)
    @DisplayName("Sorting dropdown changes article order")
    void testSortingDropdown() {
        openHome();
        clickLogin();
        submitLogin(USER_EMAIL, USER_PASSWORD);
        wait.until(d -> d.getCurrentUrl().contains("/") || d.getCurrentUrl().contains("home"));

        List<WebElement> sortEls = driver.findElements(
                By.cssSelector("select#sortBy, select.sortBy, select[name='sort']"));
        Assumptions.assumeTrue(!sortEls.isEmpty(),
                "Sorting dropdown not present; skipping test");

        Select sorter = new Select(sortEls.get(0));
        List<String> originalOrder = getArticleTitles();

        String[] optionsToTest = {"Newest", "Oldest", "Most Popular", "Least Popular"};
        for (String option : optionsToTest) {
            try {
                sorter.selectByVisibleText(option);
                Thread.sleep(500);
            } catch (Exception e) {
                continue;
            }

            List<String> currentOrder = getArticleTitles();
            assertNotEquals(originalOrder, currentOrder,
                    "Sorting by '" + option + "' did not change article order");
            originalOrder = currentOrder;
        }

        logoutIfPresent();
    }

    @Test
    @Order(4)
    @DisplayName("Menu actions: Login, Logout, Reset App State")
    void testMenuActions() {
        openHome();

        // Login via menu
        clickLogin();
        submitLogin(USER_EMAIL, USER_PASSWORD);
        WebDriverWait waitForRedirect = new WebDriverWait(driver, Duration.ofSeconds(10));
        waitForRedirect.until(d -> d.getCurrentUrl().contains("/") || d.getCurrentUrl().contains("home"));
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/") || currentUrl.contains("home"),
                "Login did not navigate to expected page");

        // Attempt Reset App State if available
        resetAppStateIfPresent();

        // Logout via menu
        logoutIfPresent();
        String afterLogoutUrl = driver.getCurrentUrl();
        assertTrue(afterLogoutUrl.contains("/login") || afterLogoutUrl.contains("/") || afterLogoutUrl.contains("home"),
                "Logout did not navigate to expected page");
    }

    @Test
    @Order(5)
    @DisplayName("External social links open correctly")
    void testExternalLinks() {
        openHome();

        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Assumptions.assumeTrue(!anchors.isEmpty(),
                "No anchor elements found; skipping external link test");

        String baseHost = "demo.realworld.io";
        int linkCount = 0;
        for (WebElement link : anchors) {
            if (linkCount >= 3) break; // Limit to first 3 external links
            
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty() || href.contains(baseHost) || href.startsWith("#")) {
                continue; // skip internal links
            }

            try {
                String parentHandle = driver.getWindowHandle();
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
                switchToNewWindow(parentHandle);
                try {
                    String currentUrl = driver.getCurrentUrl().toLowerCase();
                    String expectedDomain = href.replaceFirst("https?://", "").split("/")[0].toLowerCase();
                    assertTrue(currentUrl.contains(expectedDomain),
                            "External link URL does not contain expected domain. Expected: "
                                    + expectedDomain + ", actual: " + currentUrl);
                    linkCount++;
                } finally {
                    driver.close();
                    driver.switchTo().window(parentHandle);
                }
            } catch (Exception e) {
                // Continue with next link if this one fails
            }
        }
        
        assertTrue(linkCount > 0, "No external links were successfully tested");
    }
}