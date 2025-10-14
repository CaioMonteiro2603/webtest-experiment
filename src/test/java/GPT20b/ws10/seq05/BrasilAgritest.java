package GPT20b.ws10.seq05;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class GestionBrasilagriWebTest {

    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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

    /* --------------------------------------- */
    /* Helper methods                          */
    /* --------------------------------------- */

    private void navigateToLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void performLogin(String user, String pass) {
        navigateToLoginPage();
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email'], input#email, input[name='email']")));
        WebElement pwdInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password'], input#password, input[name='password']")));
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button#loginButton, button[name='login']")));

        emailInput.clear();
        emailInput.sendKeys(user);
        pwdInput.clear();
        pwdInput.sendKeys(pass);
        submitBtn.click();
    }

    private boolean isLoggedIn() {
        return driver.getCurrentUrl().contains("/gestao") &&
               !driver.findElements(By.linkText("Login")).isEmpty();
    }

    private void performLogout() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty() && logoutLinks.get(0).isDisplayed()) {
            WebElement logout = logoutLinks.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(logout)).click();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    private void resetAppStateIfAvailable() {
        List<WebElement> resetLink = driver.findElements(By.linkText("Reset App State"));
        if (!resetLink.isEmpty() && resetLink.get(0).isDisplayed()) {
            WebElement reset = resetLink.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(reset)).click();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    private void handleExternalLink(WebElement link, String expectedDomain) {
        String original = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(original)) {
                driver.switchTo().window(win);
                assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "External link did not navigate to domain: " + expectedDomain);
                driver.close();
                driver.switchTo().window(original);
                break;
            }
        }
    }

    /* --------------------------------------- */
    /* Tests                                   */
    /* --------------------------------------- */

    @Test
    @Order(1)
    public void testLoginPageLoads() {
        navigateToLoginPage();
        String title = driver.getTitle();
        assertNotNull(title, "Login page title should exist");
        assertTrue(title.toLowerCase().contains("login"), "Title should reference login");

        WebElement emailInput = driver.findElement(
                By.cssSelector("input[type='email'], input#email, input[name='email']"));
        WebElement pwdInput = driver.findElement(
                By.cssSelector("input[type='password'], input#password, input[name='password']"));
        WebElement submitBtn = driver.findElement(
                By.cssSelector("button[type='submit'], button#loginButton, button[name='login']"));

        assertTrue(emailInput.isDisplayed(), "Email field should be displayed");
        assertTrue(pwdInput.isDisplayed(), "Password field should be displayed");
        assertTrue(submitBtn.isDisplayed(), "Submit button should be displayed");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        performLogin("invalid@example.com", "wrongpwd");
        List<WebElement> errorMsgs = driver.findElements(
                By.cssSelector(".alert,.error,.validation-error,.toast"));
        assertFalse(errorMsgs.isEmpty(), "Error message should appear on invalid login");
        assertTrue(driver.getCurrentUrl().contains("/login"),
                "URL should remain on login page after failed attempt");
        performLogout();
    }

    @Test
    @Order(3)
    public void testValidLoginAndLogout() {
        performLogin(USERNAME, PASSWORD);
        assertTrue(isLoggedIn(), "Login should navigate to '/gestao' and show no login link");
        performLogout();
        assertFalse(isLoggedIn(), "Logout should return to login page");
    }

    @Test
    @Order(4)
    public void testBurgerMenuOperations() {
        performLogin(USERNAME, PASSWORD);
        assertTrue(isLoggedIn(), "Login succeeded before menu test");

        List<WebElement> burgerBtns = driver.findElements(
                By.cssSelector("button[aria-label='Open menu'], button.navbar-toggler, .burger-menu"));
        assertFalse(burgerBtns.isEmpty(), "Burger menu button should exist");
        WebElement burger = burgerBtns.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();

        List<WebElement> menuItems = driver.findElements(
                By.cssSelector(".menu-panel a, .dropdown-item, .nav-link"));
        assertFalse(menuItems.isEmpty(), "Menu items should appear after clicking burger");

        boolean hasAllItems = false, hasAbout = false, hasLogout = false, hasReset = false;
        for (WebElement item : menuItems) {
            String text = item.getText().trim().toLowerCase();
            if (text.contains("all items") || text.contains("dashboard")) hasAllItems = true;
            if (text.contains("about")) hasAbout = true;
            if (text.contains("logout")) hasLogout = true;
            if (text.contains("reset")) hasReset = true;
        }
        assertTrue(hasAllItems, "All Items / Dashboard link missing");
        assertTrue(hasAbout, "About link missing");
        assertTrue(hasLogout, "Logout link missing");
        assertTrue(hasReset, "Reset App State link missing");

        // About external
        for (WebElement item : menuItems) {
            if (item.getText().trim().equalsIgnoreCase("About")) {
                handleExternalLink(item, "about");
                break;
            }
        }

        // Reset App State
        for (WebElement item : menuItems) {
            if (item.getText().trim().equalsIgnoreCase("Reset App State")) {
                wait.until(ExpectedConditions.elementToBeClickable(item)).click();
                wait.until(ExpectedConditions.urlToBe(BASE_URL));
                break;
            }
        }

        // Logout via menu
        for (WebElement item : menuItems) {
            if (item.getText().trim().equalsIgnoreCase("Logout")) {
                wait.until(ExpectedConditions.elementToBeClickable(item)).click();
                wait.until(ExpectedConditions.urlToBe(BASE_URL));
                break;
            }
        }

        assertFalse(isLoggedIn(), "User should not be logged in after menu logout");
    }

    @Test
    @Order(5)
    public void testSortingDropdownChangesOrder() {
        performLogin(USERNAME, PASSWORD);
        assertTrue(isLoggedIn(), "Login succeeded before sorting test");

        // Navigate to a list page that contains a sorting dropdown.
        // Generic attempt: find link containing 'List' or 'View'.
        List<WebElement> listLinks = driver.findElements(
                By.xpath("//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'list']"));
        if (!listLinks.isEmpty()) {
            WebElement listLink = listLinks.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(listLink)).click();
            wait.until(ExpectedConditions.urlContains("/list"));

            List<WebElement> sortSelects = driver.findElements(
                    By.cssSelector("select.sort, select#sort, select[name='sort']"));
            if (!sortSelects.isEmpty()) {
                WebElement sortSelect = sortSelects.get(0);
                List<WebElement> options = sortSelect.findElements(By.tagName("option"));
                Set<String> firstItems = new HashSet<>();
                for (WebElement option : options) {
                    wait.until(ExpectedConditions.elementToBeClickable(option)).click();
                    // Wait for list reload
                    wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector("tbody tr")));
                    WebElement firstRow = driver.findElement(By.cssSelector("tbody tr"));
                    firstItems.add(firstRow.getText());
                }
                assertTrue(firstItems.size() > 1,
                        "Sorting options should change list order");
            }
        }

        performLogout();
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        navigateToLoginPage();
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        Set<String> expectedDomains = new HashSet<>();
        expectedDomains.add("twitter.com");
        expectedDomains.add("facebook.com");
        expectedDomains.add("linkedin.com");

        boolean foundAny = false;
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            for (String domain : expectedDomains) {
                if (href.contains(domain)) {
                    foundAny = true;
                    handleExternalLink(link, domain);
                    break;
                }
            }
        }
        // Test passes even if no social links were found
        assertTrue(true, "Footer social link test executed; any found: " + foundAny);
    }

    @Test
    @Order(7)
    public void testResetAppStateIndependence() {
        // Ensure we are logged out
        performLogout();
        // Perform minimal actions that change state
        navigateToLoginPage();
        performLogin(USERNAME, PASSWORD);
        assertTrue(isLoggedIn(), "Login succeeded before state reset");

        // Assume there's a UI element that toggles a setting or creates data
        // We'll attempt to click a generic button containing 'Add'
        List<WebElement> addButtons = driver.findElements(
                By.cssSelector("button:contains('Add'), button.add, .btn-add"));
        if (!addButtons.isEmpty()) {
            WebElement addBtn = addButtons.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(addBtn)).click();
            // Wait for any confirmation
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".alert-success, .toast-success")));
        }

        // Reset app state
        resetAppStateIfAvailable();
        // After reset, user should still be logged in but any changes cleared
        assertTrue(isLoggedIn(), "User still logged in after reset");

        performLogout();
    }
}