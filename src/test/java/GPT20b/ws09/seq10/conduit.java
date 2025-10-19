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
public class RealworldTests {

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
                title.toLowerCase().contains("realworld"),
                "Page title does not contain 'realworld': " + title);
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
            if (host.isEmpty() || host.equals(baseHost)) continue; // skip internal
            openExternalLink(By.cssSelector("footer a[href='" + href + "']"),
                    host);
        }
    }

    @Test
    @Order(3)
    public void testNavigationToAllStories() {
        driver.get(BASE_URL);
        By allStoriesLink = By.linkText("Home");
        Assumptions.assumeTrue(elementPresent(allStoriesLink), "'Home' link not found");
        waitClickable(allStoriesLink).click();

        wait.until(ExpectedConditions.urlContains("/stories"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/stories"),
                "Did not navigate to All Stories page");

        By articleCards = By.cssSelector(".story-card");
        Assertions.assertTrue(
                elementPresent(articleCards),
                "No stories found on All Stories page");
    }

    @Test
    @Order(4)
    public void testLoginValid() {
        driver.get(BASE_URL);
        By loginLink = By.linkText("Login");
        Assumptions.assumeTrue(elementPresent(loginLink), "'Login' link not found");
        waitClickable(loginLink).click();

        login("demo@demo.com", "demo");

        By createStoryBtn = By.cssSelector("a[href='/stories/new']");
        Assertions.assertTrue(
                elementPresent(createStoryBtn),
                "Create Story button not present after login");
        logout();
    }

    @Test
    @Order(5)
    public void testLoginInvalid() {
        driver.get(BASE_URL);
        By loginLink = By.linkText("Login");
        Assumptions.assumeTrue(elementPresent(loginLink), "'Login' link not found");
        waitClickable(loginLink).click();

        login("wrong@demo.com", "wrong");
        By errorMsg = By.cssSelector(".error");
        Assertions.assertTrue(
                elementPresent(errorMsg),
                "Error message not displayed on invalid login");
    }

    @Test
    @Order(6)
    public void testSortingDropdown() {
        driver.get(BASE_URL + "stories");
        By sortSelector = locateFirst(By.id("sort"),
                By.cssSelector("select[name='sort']"));
        Assumptions.assumeTrue(elementPresent(sortSelector), "Sorting dropdown not found");

        WebElement sortDropdown = waitClickable(sortSelector);
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() > 1, "Only one sorting option available");

        // Capture initial first story title
        By storyTitle = By.cssSelector(".story-card .title");
        List<WebElement> titlesBefore = driver.findElements(storyTitle);
        Assertions.assertFalse(titlesBefore.isEmpty(), "No story titles before sorting");
        String firstBefore = titlesBefore.get(0).getText();

        for (int i = 0; i < options.size(); i++) {
            options.get(i).click();
            wait.until(ExpectedConditions.stalenessOf(titlesBefore.get(0)));

            List<WebElement> titlesAfter = driver.findElements(storyTitle);
            String firstAfter = titlesAfter.get(0).getText();

            Assertions.assertNotEquals(firstBefore, firstAfter,
                    "Sorting option '" + options.get(i).getText() + "' did not change order");
            firstBefore = firstAfter;
            titlesBefore = titlesAfter;
        }
    }

    @Test
    @Order(7)
    public void testBurgerMenuOptions() {
        driver.get(BASE_URL);
        // In this demo the navigation bar is not a burger, but links are available directly.
        // We will use the same link texts.

        // About (external)
        By aboutLink = By.linkText("About");
        if (elementPresent(aboutLink)) {
            String href = driver.findElement(aboutLink).getAttribute("href");
            if (href != null && !extractHost(href).equals(extractHost(BASE_URL))) {
                openExternalLink(aboutLink, extractHost(href));
            }
        }

        // Profile / My Stories
        By myStoriesLink = By.linkText("My Stories");
        if (elementPresent(myStoriesLink)) {
            waitClickable(myStoriesLink).click();
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("/profile"),
                    "Did not navigate to My Stories page");
            driver.navigate().back();
        }

        // Create Story
        By createLink = By.linkText("Create Story");
        if (elementPresent(createLink)) {
            waitClickable(createLink).click();
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("/stories/new"),
                    "Did not navigate to Create Story page");
            driver.navigate().back();
        }

        // Logout (requires login)
        login("demo@demo.com", "demo");
        logout();
    }
}