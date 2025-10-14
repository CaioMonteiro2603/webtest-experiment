package GPT20b.ws06.seq05;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class AutomationIntTestingWebTest {

    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String INVENTORY_URL = BASE_URL + "inventory.html";

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

    // ------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------

    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void login(String username, String password) {
        navigateToHome();
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='username'], input#username, input[name='user']")));
        WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password'], input#password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button#loginBtn, button.login")));
        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
        wait.until(ExpectedConditions.urlToBe(INVENTORY_URL));
    }

    private boolean isLoggedIn() {
        return driver.findElements(By.linkText("Logout")).stream()
                .anyMatch(el -> el.isDisplayed() && el.isEnabled());
    }

    private void logout() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty() && logoutLinks.get(0).isDisplayed()) {
            WebElement logout = logoutLinks.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(logout)).click();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    private String getFirstItemName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".inventory_item_name"))).getText();
    }

    private void resetAppState() {
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        if (!resetLinks.isEmpty() && resetLinks.get(0).isDisplayed()) {
            WebElement reset = resetLinks.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(reset)).click();
            wait.until(ExpectedConditions.urlToBe(INVENTORY_URL));
        }
    }

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToHome();
        String title = driver.getTitle();
        assertNotNull(title, "Page title should not be null");
        assertTrue(title.length() > 0, "Page title should not be empty");
    }

    @Test
    @Order(2)
    public void testLoginValid() {
        login("caio@gmail.com", "123");
        assertTrue(isLoggedIn(), "User should be logged in after valid credentials");
        assertTrue(driver.getCurrentUrl().contains("inventory"), "URL should contain inventory after login");
        assertFalse(driver.findElements(By.cssSelector(".inventory_item")).isEmpty(),
                "Inventory items should be displayed after login");
        logout();
    }

    @Test
    @Order(3)
    public void testLoginInvalidShowsError() {
        navigateToHome();
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='username'], input#username, input[name='user']")));
        WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password'], input#password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button#loginBtn, button.login")));
        userField.clear();
        userField.sendKeys("wrong");
        passField.clear();
        passField.sendKeys("wrong");
        loginBtn.click();
        List<WebElement> errors = driver.findElements(By.cssSelector(".error, .alert, .error-message"));
        assertFalse(errors.isEmpty(), "Error message should appear with invalid credentials");
        assertTrue(driver.getCurrentUrl().contains("index"), "URL should remain on home after failed login");
        logout();
    }

    @Test
    @Order(4)
    public void testSortingDropdownChangesOrder() {
        login("caio@gmail.com", "123");
        List<WebElement> sortSelectElems = driver.findElements(By.cssSelector("select#sort-select, select#sort"));
        assertFalse(sortSelectElems.isEmpty(), "Sorting dropdown should exist on inventory page");
        WebElement sortSelect = sortSelectElems.get(0);
        Select sort = new Select(sortSelect);
        int optionCount = sort.getOptions().size();
        assertTrue(optionCount > 1, "Sorting dropdown should have multiple options");

        String firstNameBefore = getFirstItemName();
        for (int i = 0; i < optionCount; i++) {
            sort.selectByIndex(i);
            // Wait for change to apply
            wait.until(ExpectedConditions.stalenessOf(sortSelect));
            sort = new Select(sortSelect);
            String firstNameAfter = getFirstItemName();
            if (i > 0) {
                assertNotEquals(firstNameBefore, firstNameAfter,
                        "Sorting option selection did not change item order");
                firstNameBefore = firstNameAfter;
            }
        }
        resetAppState();
        logout();
    }

    @Test
    @Order(5)
    public void testBurgerMenuLinks() {
        login("caio@gmail.com", "123");
        List<WebElement> burgerBtns = driver.findElements(By.cssSelector("button[aria-label='Open Menu'], button.menu-btn, button#burger"));
        assertFalse(burgerBtns.isEmpty(), "Burger menu button should be present");
        WebElement burger = burgerBtns.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();

        List<WebElement> menuLinks = driver.findElements(By.cssSelector(".menu a, .nav a, .menu-item a"));
        assertFalse(menuLinks.isEmpty(), "Menu links should appear after opening burger menu");

        boolean hasAllItems = false, hasAbout = false, hasLogout = false, hasReset = false;
        for (WebElement link : menuLinks) {
            String text = link.getText().trim().toLowerCase();
            if (text.contains("all items") || text.contains("inventory")) hasAllItems = true;
            if (text.contains("about")) hasAbout = true;
            if (text.contains("logout")) hasLogout = true;
            if (text.contains("reset")) hasReset = true;
        }
        assertTrue(hasAllItems, "All Items link missing in burger menu");
        assertTrue(hasAbout, "About link missing in burger menu");
        assertTrue(hasLogout, "Logout link missing in burger menu");
        assertTrue(hasReset, "Reset App State link missing in burger menu");

        // Test About external navigation
        for (WebElement link : menuLinks) {
            if (link.getText().trim().equalsIgnoreCase("About")) {
                String original = driver.getWindowHandle();
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
                Set<String> windows = driver.getWindowHandles();
                for (String win : windows) {
                    if (!win.equals(original)) {
                        driver.switchTo().window(win);
                        assertTrue(driver.getCurrentUrl().contains("github.com") || driver.getCurrentUrl().startsWith("https://"),
                                "About link should navigate to an external domain");
                        driver.close();
                        driver.switchTo().window(original);
                        break;
                    }
                }
                break;
            }
        }

        // Reset App State
        for (WebElement link : menuLinks) {
            if (link.getText().trim().equalsIgnoreCase("Reset App State")) {
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                wait.until(ExpectedConditions.urlToBe(INVENTORY_URL));
                break;
            }
        }

        // Logout
        for (WebElement link : menuLinks) {
            if (link.getText().trim().equalsIgnoreCase("Logout")) {
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                wait.until(ExpectedConditions.urlToBe(BASE_URL));
                break;
            }
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialExternalLinks() {
        login("caio@gmail.com", "123");
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertFalse(footerLinks.isEmpty(), "Footer should contain links");
        Set<String> expectedDomains = new HashSet<>();
        expectedDomains.add("twitter.com");
        expectedDomains.add("facebook.com");
        expectedDomains.add("linkedin.com");

        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            String host;
            try {
                host = new java.net.URI(href).getHost();
                if (host == null) continue;
            } catch (Exception e) {
                continue;
            }
            if (!expectedDomains.contains(host.toLowerCase())) continue;

            String original = driver.getWindowHandle();
            wait.until(ExpectedConditions.elementToBeClickable(link)).click();
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String win : windows) {
                if (!win.equals(original)) {
                    driver.switchTo().window(win);
                    assertTrue(driver.getCurrentUrl().contains(host),
                            "External link URL does not contain expected domain: " + host);
                    driver.close();
                    driver.switchTo().window(original);
                    break;
                }
            }
        }
        logout();
    }
}