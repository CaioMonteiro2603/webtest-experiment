package SunaGPT20b.ws09.seq01;

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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
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

    private void login(String email, String password) {
        driver.get(BASE_URL + "login");
        WebElement emailInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        WebElement passwordInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Password']")));
        WebElement loginButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        loginButton.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("testuser@example.com", "test123");
        // Verify successful login by checking presence of the home feed
        WebElement feed = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.feed-toggle")));
        Assertions.assertTrue(feed.isDisplayed(),
                "Home feed should be visible after successful login");
        // Ensure URL contains /feed
        Assertions.assertTrue(driver.getCurrentUrl().contains("/feed"),
                "URL should contain '/feed' after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        WebElement emailInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        WebElement passwordInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Password']")));
        WebElement loginButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        emailInput.clear();
        emailInput.sendKeys("invalid@example.com");
        passwordInput.clear();
        passwordInput.sendKeys("wrongpass");
        loginButton.click();

        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.error-messages li")));
        Assertions.assertTrue(error.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertEquals("email or password is invalid", error.getText().toLowerCase(),
                "Error message text should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuBurgerAndLogout() {
        // Ensure we are logged in first
        login("testuser@example.com", "test123");

        // Open the burger menu (navbar toggle)
        WebElement burger = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        burger.click();

        // Click Settings
        WebElement settings = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Settings")));
        settings.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/settings"),
                "Should navigate to Settings page");

        // Return to home via burger menu
        burger = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        burger.click();
        WebElement home = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        home.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/feed"),
                "Should return to Home feed");

        // Logout
        burger = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.navbar-toggler")));
        burger.click();
        WebElement logout = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logout.click();

        // Verify we are back on login page
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "After logout, URL should contain '/login'");
    }

    @Test
    @Order(4)
    public void testInternalLinksOneLevelDeep() {
        driver.get(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null) continue;
            if (href.startsWith(BASE_URL) && !href.equals(BASE_URL)) {
                // Navigate to internal link
                driver.navigate().to(href);
                // Wait for page load
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
                        "Internal link should stay within base domain: " + href);
                // Simple sanity check: page title not empty
                String title = driver.getTitle();
                Assertions.assertFalse(title.isEmpty(),
                        "Page title should not be empty for internal page: " + href);
                // Return to base page
                driver.navigate().back();
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
            }
        }
    }

    @Test
    @Order(5)
    public void testExternalLinksPolicy() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null) continue;
            if (href.startsWith("http") && !href.contains("demo.realworld.io")) {
                // Click the external link
                try {
                    link.click();
                } catch (Exception e) {
                    // If click fails (e.g., element not interactable), skip
                    continue;
                }

                // Wait for new window/tab
                wait.until(driver -> driver.getWindowHandles().size() > 1);
                Set<String> windows = driver.getWindowHandles();
                Iterator<String> it = windows.iterator();
                while (it.hasNext()) {
                    String handle = it.next();
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }

                // Verify URL contains expected domain
                String currentUrl = driver.getCurrentUrl();
                String expectedDomain = href.replaceAll("^(https?://[^/]+).*", "$1");
                Assertions.assertTrue(currentUrl.startsWith(expectedDomain),
                        "External link should open expected domain. Expected: " + expectedDomain + ", Got: " + currentUrl);

                // Close external tab and switch back
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        // Footer social links selectors (example: Twitter, Facebook, LinkedIn)
        String[] socialSelectors = {
                "a[href*='twitter.com']",
                "a[href*='facebook.com']",
                "a[href*='linkedin.com']"
        };
        String originalWindow = driver.getWindowHandle();

        for (String selector : socialSelectors) {
            List<WebElement> elems = driver.findElements(By.cssSelector(selector));
            if (elems.isEmpty()) continue;
            WebElement link = elems.get(0);
            String href = link.getAttribute("href");
            link.click();

            // Wait for new window/tab
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String handle : windows) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            // Verify domain matches expected social network
            String expectedDomain = href.replaceAll("^(https?://[^/]+).*", "$1");
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.startsWith(expectedDomain),
                    "Social link should open correct domain. Expected: " + expectedDomain + ", Got: " + currentUrl);

            // Close and return
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}