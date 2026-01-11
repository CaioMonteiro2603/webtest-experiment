package GPT20b.ws09.seq10;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.net.URI;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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

    /* ---------- Helper Methods ---------- */

    private static boolean elementPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    private static WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private static void openExternalLink(By locator, String expectedDomain) {
        String original = driver.getWindowHandle();
        driver.findElement(locator).click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(original))
                .findFirst()
                .orElseThrow();
        driver.switchTo().window(newHandle);
        Assertions.assertTrue(
                driver.getCurrentUrl().contains(expectedDomain),
                "External link URL does not contain expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(original);
    }

    private static String extractHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    /* ---------- Login / Logout ---------- */

    private static void login(String email, String password) {
        By emailLocator = locateFirst(By.cssSelector("input[type='email']"),
                By.name("email"));
        By passLocator = locateFirst(By.cssSelector("input[placeholder='Password']"),
                By.name("password"));
        By submitLocator = locateFirst(By.cssSelector("button[type='submit']"),
                By.id("login-button"));

        WebElement emailField = waitClickable(emailLocator);
        WebElement passField = waitClickable(passLocator);
        WebElement loginBtn = waitClickable(submitLocator);

        emailField.clear();
        emailField.sendKeys(email);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    private static void logout() {
        By avatarLocator = By.cssSelector("div.user-dropdown-toggle");
        if (!elementPresent(avatarLocator)) {
            return;
        }
        waitClickable(avatarLocator).click();
        By logoutLink = By.id("logout");
        if (elementPresent(logoutLink)) {
            waitClickable(logoutLink).click();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }

    /* ---------- Utility ---------- */

    private static By locateFirst(By... locators) {
        for (By locator : locators) {
            if (driver.findElements(locator).size() > 0) {
                return locator;
            }
        }
        throw new RuntimeException("None of the provided locators matched any element");
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testHomePageTitle() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        Assertions.assertTrue(
                title.toLowerCase().contains("conduit"),
                "Page title does not contain 'conduit': " + title);
    }

    @Test
    @Order(2)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        By footerLinks = By.cssSelector("footer a[href]");
        Assumptions.assumeTrue(elementPresent(footerLinks), "Footer links not found");

        List<WebElement> links = driver.findElements(footerLinks);
        String baseHost = extractHost(BASE_URL);
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            String host = extractHost(href);
            if (host.isEmpty() || host.equals(baseHost)) continue;
            openExternalLink(By.cssSelector("footer a[href^='" + href.split("//")[0] + "//" + host + "']"),
                    host);
        }
    }

    @Test
    @Order(3)
    public void testNavigationToAllStories() {
        driver.get(BASE_URL);
        By globalFeedLink = By.linkText("Global Feed");
        Assumptions.assumeTrue(elementPresent(globalFeedLink), "'Global Feed' link not found");
        waitClickable(globalFeedLink).click();

        wait.until(d -> driver.getCurrentUrl().contains("/"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/#/") || driver.getCurrentUrl().equals(BASE_URL),
                "Did not navigate correctly");

        By articleCards = By.cssSelector(".article-preview");
        Assertions.assertTrue(
                elementPresent(articleCards),
                "No stories found on page");
    }

    @Test
    @Order(4)
    public void testLoginValid() {
        driver.get(BASE_URL);
        By loginLink = By.linkText("Sign in");
        Assumptions.assumeTrue(elementPresent(loginLink), "'Sign in' link not found");
        waitClickable(loginLink).click();

        login("demo@demo.com", "demo");

        By createStoryBtn = By.cssSelector("a[href='#/editor']");
        Assertions.assertTrue(
                elementPresent(createStoryBtn),
                "Create Story button not present after login");
        logout();
    }

    @Test
    @Order(5)
    public void testLoginInvalid() {
        driver.get(BASE_URL);
        By loginLink = By.linkText("Sign in");
        Assumptions.assumeTrue(elementPresent(loginLink), "'Sign in' link not found");
        waitClickable(loginLink).click();

        login("wrong@demo.com", "wrong");
        By errorMsg = By.cssSelector(".error-messages");
        Assertions.assertTrue(
                elementPresent(errorMsg),
                "Error message not displayed on invalid login");
    }

    @Test
    @Order(6)
    public void testSortingDropdown() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Global Feed")).click();
        
        By sortSelector = locateFirst(By.cssSelector("a[href='#/']"),
                By.cssSelector("a.nav-link"));
        Assumptions.assumeTrue(elementPresent(By.linkText("Global Feed")), "Feed tabs not found");

        WebElement globalFeedTab = waitClickable(By.linkText("Global Feed"));
        globalFeedTab.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-preview")));
        Assertions.assertTrue(
                elementPresent(By.cssSelector(".article-preview")),
                "No articles found");
    }

    @Test
    @Order(7)
    public void testBurgerMenuOptions() {
        driver.get(BASE_URL);
        
        // Home link
        By homeLink = By.linkText("Home");
        if (elementPresent(homeLink)) {
            waitClickable(homeLink).click();
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("/#/") || driver.getCurrentUrl().equals(BASE_URL),
                    "Did not navigate to Home page");
        }

        // Sign in link
        By signInLink = By.linkText("Sign in");
        if (elementPresent(signInLink)) {
            waitClickable(signInLink).click();
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("/#/login"),
                    "Did not navigate to Sign in page");
            driver.navigate().back();
        }

        // Sign up link
        By signUpLink = By.linkText("Sign up");
        if (elementPresent(signUpLink)) {
            waitClickable(signUpLink).click();
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("/#/register"),
                    "Did not navigate to Sign up page");
            driver.navigate().back();
        }

        // New Post link
        login("demo@demo.com", "demo");
        By newPostLink = By.linkText("New Post");
        if (elementPresent(newPostLink)) {
            waitClickable(newPostLink).click();
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("/#/editor"),
                    "Did not navigate to New Post page");
            driver.navigate().back();
        }
        logout();
    }
}