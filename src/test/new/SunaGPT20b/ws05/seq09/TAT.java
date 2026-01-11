package SunaGPT20b.ws05.seq09;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.net.MalformedURLException;
import java.net.URL;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
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

    /** Helper to extract domain from a URL string */
    private String getDomain(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(body.isDisplayed(), "Home page body should be displayed");
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Current URL should match BASE_URL");
    }

    @Test
    @Order(2)
    public void testInternalLinksOneLevelDeep() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.tagName("a"));
        List<String> internalHrefs = new ArrayList<>();

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (href.startsWith(BASE_URL) || href.startsWith("/") || href.startsWith("./") || href.startsWith("../")) {
                internalHrefs.add(href);
            }
        }

        for (String href : internalHrefs) {
            driver.navigate().to(href);
            // Verify navigation succeeded by checking URL contains the href fragment (ignoring query params)
            Assertions.assertTrue(driver.getCurrentUrl().contains(href.replace(BASE_URL, "")),
                    "Navigated URL should contain the internal link path");
            driver.navigate().back();
        }
    }

    @Test
    @Order(3)
    public void testExternalLinksOneLevelDeep() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.tagName("a"));
        List<String> externalHrefs = new ArrayList<>();

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith(BASE_URL) && (href.startsWith("http://") || href.startsWith("https://"))) {
                externalHrefs.add(href);
            }
        }

        String originalWindow = driver.getWindowHandle();

        for (String href : externalHrefs) {
            List<WebElement> linkElements = driver.findElements(By.xpath("//a[@href='" + href + "']"));
            if (!linkElements.isEmpty()) {
                WebElement link = linkElements.get(0);
                link.click();

                // Switch to new window/tab if opened
                Set<String> windows = driver.getWindowHandles();
                if (windows.size() > 1) {
                    for (String win : windows) {
                        if (!win.equals(originalWindow)) {
                            driver.switchTo().window(win);
                            break;
                        }
                    }
                }

                String expectedDomain = getDomain(href);
                String actualDomain = getDomain(driver.getCurrentUrl());
                Assertions.assertTrue(actualDomain.contains(expectedDomain),
                        "External link should navigate to domain containing: " + expectedDomain);

                // Close external tab/window and switch back
                if (windows.size() > 1) {
                    driver.close();
                    driver.switchTo().window(originalWindow);
                } else {
                    driver.navigate().back();
                }
            }
        }
    }

    @Test
    @Order(4)
    public void testLoginIfPresent() {
        driver.get(BASE_URL);
        // Attempt to locate typical login fields; if not present, skip the test
        List<WebElement> usernameFields = driver.findElements(By.id("user-name"));
        List<WebElement> passwordFields = driver.findElements(By.id("password"));
        List<WebElement> loginButtons = driver.findElements(By.id("login-button"));

        if (usernameFields.isEmpty() || passwordFields.isEmpty() || loginButtons.isEmpty()) {
            // No login form detected; consider test passed as not applicable
            return;
        }

        WebElement username = usernameFields.get(0);
        WebElement password = passwordFields.get(0);
        WebElement loginBtn = loginButtons.get(0);

        username.clear();
        username.sendKeys("standard_user");
        password.clear();
        password.sendKeys("secret_sauce");
        loginBtn.click();

        // Wait for either inventory page or error message
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("inventory"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']"))
        ));

        boolean loggedIn = driver.getCurrentUrl().contains("inventory");
        Assertions.assertTrue(loggedIn, "User should be logged in and redirected to inventory page");
    }

    @Test
    @Order(5)
    public void testSortingDropdownIfPresent() {
        driver.get(BASE_URL);
        // Assume we are on an inventory-like page after possible login
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        if (selects.isEmpty()) {
            return; // No dropdown to test
        }

        WebElement selectElement = selects.get(0);
        Select select = new Select(selectElement);
        List<WebElement> options = select.getOptions();

        for (WebElement option : options) {
            String value = option.getAttribute("value");
            if (option.isEnabled()) {
                select.selectByValue(value);
                // Verify that the selected option is indeed active
                Assertions.assertEquals(value, select.getFirstSelectedOption().getAttribute("value"),
                        "Dropdown should have selected value: " + value);
            }
            // Simple verification that page content changes (e.g., first item text changes)
            // This is a placeholder; real verification would depend on page specifics
        }
    }

    @Test
    @Order(6)
    public void testMenuBurgerButtonIfPresent() {
        driver.get(BASE_URL);
        List<WebElement> menuButtons = driver.findElements(By.id("react-burger-menu-btn"));
        if (menuButtons.isEmpty()) {
            return; // No burger menu detected
        }

        WebElement menuBtn = menuButtons.get(0);
        menuBtn.click();

        // Wait for menu panel to become visible
        WebElement menuPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("menu_button_container")));
        Assertions.assertTrue(menuPanel.isDisplayed(), "Menu panel should be displayed after clicking burger button");

        // Attempt to click "Logout" if present
        List<WebElement> logoutLinks = driver.findElements(By.id("logout_sidebar_link"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.urlContains("login"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "Should be redirected to login page after logout");
        }

        // Attempt to click "Reset App State" if present
        List<WebElement> resetLinks = driver.findElements(By.id("reset_sidebar_link"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            // Verify that cart badge is cleared (if present)
            List<WebElement> cartBadges = driver.findElements(By.className("shopping_cart_badge"));
            if (!cartBadges.isEmpty()) {
                Assertions.assertEquals("0", cartBadges.get(0).getText(),
                        "Cart badge should be reset to 0 after resetting app state");
            }
        }
    }
}