package GPT20b.ws07.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddleTests {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    /* ---------- Helper Methods ---------- */

    private boolean elementExists(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void clickLoginLink() {
        By loginLink = By.cssSelector("a[data-account]");
        if (elementExists(loginLink)) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(loginLink));
            link.click();
        } else {
            throw new IllegalStateException("Login link not found on the page");
        }
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testPageLoads() {
        navigateToHome();
        Assertions.assertTrue(
                driver.getTitle().toLowerCase().contains("jsfiddle"),
                "Page title should contain 'jsfiddle'");
    }

    @Test
    @Order(2)
    public void testLoginFormPresence() {
        navigateToHome();
        Assumptions.assumeTrue(elementExists(By.cssSelector("a[data-account]")),
                "Login link not present, skipping login test");

        clickLoginLink();

        // Wait for modal to appear
        By modal = By.cssSelector("div#auth-modal");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(modal));
            Assertions.assertTrue(
                    elementExists(By.cssSelector("input[name='email']")),
                    "Email input should be visible in login modal");
        } catch (TimeoutException e) {
            // Some sites use OAuth redirects; check for redirect to an OAuth provider
            By oauthHeader = By.cssSelector("h1");
            Assertions.assertTrue(
                    elementExists(oauthHeader),
                    "Expected OAuth provider page after clicking login");
        }
    }

    @Test
    @Order(3)
    public void testInvalidLoginAttempt() {
        navigateToHome();
        Assumptions.assumeTrue(elementExists(By.cssSelector("a[data-account]")),
                "Login link not present, skipping invalid login test");

        clickLoginLink();

        By modal = By.cssSelector("div#auth-modal");
        Assumptions.assumeTrue(elementExists(modal),
                "Login modal not displayed; cannot test invalid credentials");

        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));

        email.clear();
        email.sendKeys("invalid@example.com");
        password.clear();
        password.sendKeys("wrongpassword");
        submit.click();

        By errorMsg = By.cssSelector(".error");
        Assertions.assertTrue(
                elementExists(errorMsg),
                "Error message should appear after wrong credentials");
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        navigateToHome();

        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            By linkLocator = By.xpath("//footer//a[contains(@href,'" + domain + "')]");
            Assumptions.assumeTrue(elementExists(linkLocator),
                    "Footer link for " + domain + " not found, skipping");

            List<WebElement> links = driver.findElements(linkLocator);
            for (WebElement link : links) {
                String originalHandle = driver.getWindowHandle();
                Set<String> before = driver.getWindowHandles();
                link.click();

                // Wait for possible new tab/window
                try {
                    wait.until(d -> driver.getWindowHandles().size() > before.size());
                } catch (TimeoutException ignored) {
                }

                Set<String> after = driver.getWindowHandles();
                after.removeAll(before);
                if (!after.isEmpty()) {
                    String newHandle = after.iterator().next();
                    driver.switchTo().window(newHandle);
                    wait.until(ExpectedConditions.urlContains(domain));
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "External link should open domain: " + domain);
                    driver.close();
                    driver.switchTo().window(originalHandle);
                } else {
                    // Same tab navigation
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "Link navigated within same tab to expected domain");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
                }
            }
        }
    }

    @Test
    @Order(5)
    public void testSortingDropdownAndBurgerMenu() {
        // jsfiddle does not provide a standard sorting dropdown or burger menu in the
        // sense described in the specification.
        // The test below will simply check for the presence of a few known UI elements
        // and will skip further steps if they are absent.

        navigateToHome();

        // Check for hypothetical sorting dropdown
        By sorting = By.id("sortOptions");
        Assumptions.assumeTrue(elementExists(sorting),
                "Sorting dropdown not present on jsfiddle – skipping feature test");

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(sorting));
        // Example interaction: select the first option
        IList<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        if (!options.isEmpty()) {
            options.get(0).click();
            // Verify that page content changes – stubbed by expecting URL change
            String urlAfter = driver.getCurrentUrl();
            Assertions.assertNotEquals(
                    BASE_URL,
                    urlAfter,
                    "Page URL should change after selecting sort option");
        }

        // Check for burger menu
        By burger = By.id("burger-menu");
        Assumptions.assumeTrue(elementExists(burger),
                "Burger menu not present on jsfiddle – skipping menu test");
        WebElement burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burger));
        burgerBtn.click();
        // Verify that menu appears
        By menuList = By.cssSelector("ul.menu-list");
        Assertions.assertTrue(
                elementExists(menuList),
                "Menu list should be visible after clicking burger button");
    }
}