package GPT20b.ws09.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String BASE_HOST = "demo.realworld.io";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Helpers ---------- */

    private void navigateToBase() {
        driver.get(BASE_URL);
    }

    private void clickLink(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToBase();
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("conduit") || title.toLowerCase().contains("realworld"),
                "Page title should mention RealWorld.");
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("demo.realworld.io"),
                "Current URL should include demo.realworld.io.");
    }

    @Test
    @Order(2)
    public void testSignInLinkPresent() {
        navigateToBase();
        By signInLink = By.linkText("Sign in");
        List<WebElement> links = driver.findElements(signInLink);
        Assumptions.assumeTrue(!links.isEmpty(),
                "Sign in link not found on home page; skipping test.");
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        Assertions.assertTrue(link.isDisplayed(),
                "Sign in link should be visible.");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        navigateToBase();
        By signInLink = By.linkText("Sign in");
        List<WebElement> links = driver.findElements(signInLink);
        Assumptions.assumeTrue(!links.isEmpty(),
                "Sign in link not found; skipping test.");
        clickLink(signInLink);

        By emailField = By.cssSelector("input[type='email']");
        By passwordField = By.cssSelector("input[type='password']");
        By submitBtn = By.cssSelector("button[type='submit']");

        List<WebElement> elems = driver.findElements(emailField);
        Assumptions.assumeTrue(!elems.isEmpty(),
                "Login form not displayed; skipping test.");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).sendKeys("wrongpass");
        clickLink(submitBtn);

        By errorMsg = By.cssSelector(".error-messages");
        List<WebElement> errors = driver.findElements(errorMsg);
        Assertions.assertFalse(errors.isEmpty(),
                "Error message should appear for invalid credentials.");
        String msg = errors.get(0).getText().toLowerCase();
        Assertions.assertTrue(msg.contains("unauthorized") || msg.contains("invalid") || msg.contains("wrong") || msg.contains("email or password") || msg.contains("error"),
                "Error message text should indicate invalid credentials.");
    }

    @Test
    @Order(4)
    public void testFooterExternalLinks() {
        navigateToBase();
        By externalLinks = By.xpath("//footer//a[starts-with(@href,'http')]");

        List<WebElement> links = driver.findElements(externalLinks);
        Assertions.assertFalse(links.isEmpty(),
                "No external links found on the page.");

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : links) {
            String url = link.getAttribute("href");
            if (url == null || url.isEmpty() || url.contains(BASE_HOST)) {
                continue; // skip internal or empty links
            }

            // click link
            try {
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
            } catch (Exception e) {
                // If click fails, try JavaScript click
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
            }

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Set<String> handles = driver.getWindowHandles();
            for (String h : handles) {
                if (!h.equals(originalWindow)) {
                    driver.switchTo().window(h);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(url),
                            "Opened link URL does not contain expected domain: " + url);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(5)
    public void testBurgerMenuInteractions() {
        navigateToBase();

        // Locate hamburger icon if present
        By burgerBtn = By.cssSelector("button[aria-label='Open navigation menu']");
        List<WebElement> burgerElements = driver.findElements(burgerBtn);
        Assumptions.assumeTrue(!burgerElements.isEmpty(),
                "Burger menu button not present; skipping test.");

        clickLink(burgerBtn);

        // After opening, check presence of menu links
        By menuLinks = By.cssSelector("nav[role='navigation'] a");
        List<WebElement> links = driver.findElements(menuLinks);
        Assertions.assertFalse(links.isEmpty(),
                "Menu links should be visible after opening burger menu.");

        // Click 'Home'
        By homeLink = By.linkText("Home");
        if (!driver.findElements(homeLink).isEmpty()) {
            clickLink(homeLink);
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("demo.realworld.io"),
                    "URL should contain demo.realworld.io after clicking Home.");
        }

        // Click 'Create' link if present
        By createLink = By.linkText("New Article");
        if (!driver.findElements(createLink).isEmpty()) {
            clickLink(createLink);
            // Requires authentication; if not logged in, it redirects to sign in
            By signInLink = By.linkText("Sign in");
            Assertions.assertTrue(driver.findElements(signInLink).size() > 0,
                    "Unauthenticated user should be redirected to sign in after trying to create article.");
        }

        // Click 'Sign out' if user is already logged in
        By signOutLink = By.linkText("Sign out");
        if (!driver.findElements(signOutLink).isEmpty()) {
            clickLink(signOutLink);
            // After sign out, sign in link should reappear
            By signInAfter = By.linkText("Sign in");
            Assertions.assertTrue(driver.findElements(signInAfter).size() > 0,
                    "Sign in link should be visible after signing out.");
        }
    }

    @Test
    @Order(6)
    public void testSortingDropdownOnExplorePage() {
        navigateToBase();
        By exploreLink = By.linkText("Explore");
        if (!driver.findElements(exploreLink).isEmpty()) {
            clickLink(exploreLink);
        } else {
            Assumptions.assumeTrue(false, "Explore link not found; skipping test.");
        }

        // On Explore, a sorting select may appear
        By sortSelectLocator = By.cssSelector("select");
        List<WebElement> selects = driver.findElements(sortSelectLocator);
        Assumptions.assumeTrue(!selects.isEmpty(),
                "No select element found for sorting; skipping test.");

        WebElement sortSelect = selects.get(0);
        org.openqa.selenium.support.ui.Select sort = new org.openqa.selenium.support.ui.Select(sortSelect);
        List<WebElement> options = sort.getOptions();
        Assertions.assertTrue(options.size() > 1,
                "Sorting dropdown should have more than one option.");

        String firstTitle = null;
        for (WebElement option : options) {
            sort.selectByVisibleText(option.getText());
            // Wait for the articles list to update
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loader")));
            // Capture first article title
            By titleLocator = By.cssSelector(".card a");
            List<WebElement> titles = driver.findElements(titleLocator);
            Assumptions.assumeTrue(!titles.isEmpty(),
                    "No article titles found after sorting; skipping remaining verification.");
            String currentFirst = titles.get(0).getText();
            if (firstTitle != null) {
                Assertions.assertNotEquals(firstTitle, currentFirst,
                        "Sorting should change the order of articles.");
            }
            firstTitle = currentFirst;
        }
    }
}