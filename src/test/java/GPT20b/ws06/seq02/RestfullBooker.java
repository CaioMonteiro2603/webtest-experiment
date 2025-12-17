package GPT20b.ws06.seq02;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assumptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_PASSWORD = "password";

    @BeforeAll
    public static void init() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void close() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* --------------------------------------------------------------------- */
    /* 1. Basic page load and title check                                       */
    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.navigate().to(BASE_URL);
        assertTrue(driver.getTitle().toLowerCase().contains("automation"),
                "Page title should contain 'automation'");
        // Ensure a visible header element
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        assertTrue(header.isDisplayed(), "Header should be displayed on home page");
    }

    /* --------------------------------------------------------------------- */
    /* 2. Positive login flow                                               */
    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.navigate().to(BASE_URL);
        List<WebElement> emailField = driver.findElements(By.cssSelector("input[type='email'], input[name='email']"));
        Assumptions.assumeTrue(!emailField.isEmpty(), "Email input not found - skipping test");

        emailField.get(0).clear();
        emailField.get(0).sendKeys(USER_EMAIL);

        List<WebElement> pwdField = driver.findElements(By.cssSelector("input[type='password'], input[name='password']"));
        Assumptions.assumeTrue(!pwdField.isEmpty(), "Password input not found - skipping test");
        pwdField.get(0).clear();
        pwdField.get(0).sendKeys(USER_PASSWORD);

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
        loginBtn.click();

        // Determine where the app lands after login
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("dashboard"),
                ExpectedConditions.urlContains("home"),
                ExpectedConditions.urlContains("account"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".profile"))
        ));

        assertTrue(driver.getCurrentUrl().contains("dashboard") || driver.getCurrentUrl().contains("home") || driver.getCurrentUrl().contains("account"),
                "After login, URL should contain 'dashboard', 'home', or 'account'");
        // Look for a generic user profile element that should be visible
        List<WebElement> profileElems = driver.findElements(By.cssSelector(".profile, .account-summary, .user-menu"));
        assertTrue(!profileElems.isEmpty() && profileElems.get(0).isDisplayed(),
                "User profile element should be visible after login");

        // Reset state by logging out
        logoutIfPresent();
    }

    /* --------------------------------------------------------------------- */
    /* 3. Negative login flow                                            */
    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.navigate().to(BASE_URL);
        List<WebElement> emailField = driver.findElements(By.cssSelector("input[type='email'], input[name='email']"));
        Assumptions.assumeTrue(!emailField.isEmpty(), "Email input not found - skipping test");

        emailField.get(0).clear();
        emailField.get(0).sendKeys("invalid@example.com");

        List<WebElement> pwdField = driver.findElements(By.cssSelector("input[type='password'], input[name='password']"));
        Assumptions.assumeTrue(!pwdField.isEmpty(), "Password input not found - skipping test");
        pwdField.get(0).clear();
        pwdField.get(0).sendKeys("wrong");

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
        loginBtn.click();

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-msg, .alert, .validation-error")));
        assertTrue(msg.isDisplayed(), "Error message should appear for invalid credentials");
        String errText = msg.getText();
        assertFalse(errText.isEmpty(), "Error message should contain text");
    }

    /* --------------------------------------------------------------------- */
    /* 4. Sorting dropdown test (if available)                          */
    @Test
    @Order(4)
    public void testSortingDropdown() {
        // First ensure we are on a page with products
        driver.navigate().to(BASE_URL);
        // Try to click a "Products" or similar link if found
        List<WebElement> prodLinks = driver.findElements(By.linkText("Products"));
        if (!prodLinks.isEmpty()) {
            prodLinks.get(0).click();
            wait.until(ExpectedConditions.urlContains("products"));
        }

        List<WebElement> sortSel = driver.findElements(By.cssSelector("select[name='sort'], select[id='sort']"));
        Assumptions.assumeTrue(!sortSel.isEmpty(), "Sorting dropdown not found - skipping test");

        Select select = new Select(sortSel.get(0));
        List<WebElement> options = select.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Less than two options in sorting dropdown - skipping test");

        // Capture the first item before sorting
        String firstItemBefore = getFirstProductName();
        for (WebElement opt : options) {
            String optionText = opt.getText();
            select.selectByVisibleText(optionText);
            // Wait for potential re-render
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-item")));
            String firstItemAfter = getFirstProductName();
            assertNotEquals(firstItemBefore, firstItemAfter,
                    "First product name should change after selecting sort option: " + optionText);
            firstItemBefore = firstItemAfter;
        }

        // Return to normal order for isolation
        select.selectByVisibleText(options.get(0).getText());
    }

    /* --------------------------------------------------------------------- */
    /* 5. External link check from main page                                */
    @Test
    @Order(5)
    public void testExternalLinksOnHome() {
        driver.navigate().to(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']"));
        Assumptions.assumeTrue(!externalLinks.isEmpty(), "No external links found - skipping test");

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (!href.contains("automationintesting.online")) {
                checkExternalLink(link, href);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* 6. Burger menu interactions, Including About, Logout, Reset             */
    @Test
    @Order(6)
    public void testBurgerMenuActions() {
        loginIfNeeded();

        // Open burger menu
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu'], .burger-menu, .nav-toggler")));
        burger.click();

        // File a list of common menu options
        List<WebElement> menuLinks = driver.findElements(By.tagName("a"));
        boolean allItemsVisited = false;
        boolean aboutClicked = false;
        boolean resetClicked = false;

        for (WebElement link : menuLinks) {
            String txt = link.getText().trim();
            switch (txt) {
                case "All Items":
                case "Products":
                    link.click();
                    wait.until(ExpectedConditions.urlContains("products"));
                    assertTrue(driver.getCurrentUrl().contains("products"),
                            "URL should contain 'products' after clicking All Items");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu'], .burger-menu, .nav-toggler")));
                    burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu'], .burger-menu, .nav-toggler")));
                    burger.click();
                    allItemsVisited = true;
                    break;
                case "About":
                    link.click();
                    checkExternalLink(link, link.getAttribute("href"));
                    driver.navigate().back();
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu'], .burger-menu, .nav-toggler")));
                    burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu'], .burger-menu, .nav-toggler")));
                    burger.click();
                    aboutClicked = true;
                    break;
                case "Reset App State":
                    link.click();
                    wait.until(ExpectedConditions.urlContains("dashboard"));
                    resetClicked = true;
                    break;
                case "Logout":
                    link.click();
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
                    logoutIfPresent();
                    break;
                default:
                    // ignore others
            }
        }

        assertTrue(allItemsVisited, "All Items link was not visited");
        assertTrue(aboutClicked, "About link was not visited");
        assertTrue(resetClicked, "Reset App State link was not clicked");
    }

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                        */
    private void loginIfNeeded() {
        if (driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']")).isEmpty()) {
            return;
        }
        driver.navigate().to(BASE_URL);
        List<WebElement> emailField = driver.findElements(By.cssSelector("input[type='email'], input[name='email']"));
        if (emailField.isEmpty()) {
            return;
        }
        emailField.get(0).clear();
        emailField.get(0).sendKeys(USER_EMAIL);
        List<WebElement> pwdField = driver.findElements(By.cssSelector("input[type='password'], input[name='password']"));
        if (pwdField.isEmpty()) {
            return;
        }
        pwdField.get(0).clear();
        pwdField.get(0).sendKeys(USER_PASSWORD);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
        loginBtn.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("dashboard"),
                ExpectedConditions.urlContains("home"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".profile"))
        ));
    }

    private void logoutIfPresent() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
        }
    }

    private String getFirstProductName() {
        List<WebElement> items = driver.findElements(By.cssSelector(".product-item .product-name, .item-title"));
        if (items.isEmpty()) {
            return "";
        }
        return items.get(0).getText();
    }

    private void checkExternalLink(WebElement link, String href) {
        String originalHandle = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains(href));
                assertTrue(driver.getCurrentUrl().contains(href),
                        "External link URL should contain: " + href);
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }
    }
}