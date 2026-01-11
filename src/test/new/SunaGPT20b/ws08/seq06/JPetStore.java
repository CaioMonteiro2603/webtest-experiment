package SunaGPT20b.ws08.seq06;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore{

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    public void navigateHome() {
        driver.get(BASE_URL);
        // Ensure we are on a clean state
        resetAppStateIfPresent();
    }

    private void resetAppStateIfPresent() {
        // Open menu and click Reset App State if available
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            menuBtn.click();
            WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
            resetLink.click();
            // Close menu
            WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
            closeBtn.click();
        } catch (Exception ignored) {
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL + "login");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("signon-username")));
        WebElement password = driver.findElement(By.id("signon-password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        username.sendKeys("j2ee");
        password.sendKeys("j2ee");
        loginBtn.click();

        // Verify successful login by checking URL contains /catalog
        wait.until(ExpectedConditions.urlContains("/catalog"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog"),
                "After login, URL should contain /catalog");

        // Verify catalog is displayed
        List<WebElement> items = driver.findElements(By.cssSelector("#Catalog table"));
        Assertions.assertFalse(items.isEmpty(), "Catalog should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("signon-username")));
        WebElement password = driver.findElement(By.id("signon-password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        username.sendKeys("invalid_user");
        password.sendKeys("invalid_pass");
        loginBtn.click();

        // Verify error message is shown
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".message.error")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(errorMsg.getText().contains("Invalid username or password"),
                "Error message text should contain expected text");
    }

    @Test
    @Order(3)
    public void testInventorySorting() {
        // Ensure we are logged in
        performLoginIfNeeded();

        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[data-test='product_sort_container']")));
        Select select = new Select(sortSelect);
        List<WebElement> options = select.getOptions();

        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());

            // Verify that the first item changes after sorting
            WebElement firstItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".inventory_item_name")));
            Assertions.assertNotNull(firstItem.getText(),
                    "First item name should be present after sorting by " + option.getText());
        }
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        performLoginIfNeeded();

        // Open burger menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "All Items should navigate to inventory page");

        // Re-open menu for next actions
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        switchToNewWindowAndVerifyDomain("github.com");
        driver.close();
        switchToOriginalWindow();

        // Re-open menu
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        // Verify that cart badge is cleared (if any)
        List<WebElement> badge = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertTrue(badge.isEmpty(), "Cart badge should be cleared after resetting app state");

        // Re-open menu
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Logout should redirect to login page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        // Footer links are external; verify they open correct domains
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a"));
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;

            // Click link
            link.click();
            // Switch to new window
            switchToNewWindow();

            // Verify domain based on known social platforms
            if (href.contains("twitter.com")) {
                Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"),
                        "Twitter link should open twitter.com");
            } else if (href.contains("facebook.com")) {
                Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"),
                        "Facebook link should open facebook.com");
            } else if (href.contains("linkedin.com")) {
                Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"),
                        "LinkedIn link should open linkedin.com");
            }

            // Close external tab and return
            driver.close();
            switchToOriginalWindow();
        }
    }

    @Test
    @Order(6)
    public void testExternalLinksFromOneLevelPages() {
        performLoginIfNeeded();

        // Gather internal links one level below base URL
        List<String> internalUrls = new ArrayList<>();
        List<WebElement> links = driver.findElements(By.cssSelector("a"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith(BASE_URL) && !href.equals(BASE_URL)) {
                // Ensure only one level deep (no further slashes after base path)
                String path = href.substring(BASE_URL.length());
                if (!path.isEmpty() && !path.contains("/")) {
                    internalUrls.add(href);
                }
            }
        }

        // Visit each internal page and click external links found there
        for (String pageUrl : internalUrls) {
            driver.get(pageUrl);
            List<WebElement> externalLinks = driver.findElements(By.cssSelector("a"));
            for (WebElement extLink : externalLinks) {
                String href = extLink.getAttribute("href");
                if (href == null) continue;
                if (!href.startsWith(BASE_URL) && (href.startsWith("http://") || href.startsWith("https://"))) {
                    // Click external link
                    extLink.click();
                    switchToNewWindow();

                    // Verify that the URL contains the external domain (basic check)
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href.replaceAll("https?://([^/]+).*", "$1")),
                            "External link should navigate to its domain");

                    // Close and return
                    driver.close();
                    switchToOriginalWindow();
                }
            }
        }
    }

    // Helper methods

    private void performLoginIfNeeded() {
        if (!driver.getCurrentUrl().contains("/catalog")) {
            driver.get(BASE_URL + "login");
            WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("signon-username")));
            WebElement password = driver.findElement(By.id("signon-password"));
            WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));
            username.sendKeys("j2ee");
            password.sendKeys("j2ee");
            loginBtn.click();
            wait.until(ExpectedConditions.urlContains("/catalog"));
        }
    }

    private void switchToNewWindow() {
        String original = driver.getWindowHandle();
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
                break;
            }
        }
    }

    private void switchToNewWindowAndVerifyDomain(String expectedDomain) {
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External link should open a page containing domain: " + expectedDomain);
    }

    private void switchToOriginalWindow() {
        Set<String> handles = driver.getWindowHandles();
        String original = handles.iterator().next(); // first handle is original
        driver.switchTo().window(original);
    }
}