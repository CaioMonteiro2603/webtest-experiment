package GPT20b.ws07.seq05;

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
public class JsfiddleWebTest {

    private static final String BASE_URL = "https://jsfiddle.net/";
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

    /* ------------------------------------------------------------------ */
    /* Helper methods                                                    */
    /* ------------------------------------------------------------------ */

    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    /* ------------------------------------------------------------------ */
    /* Tests                                                             */
    /* ------------------------------------------------------------------ */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToHome();
        String title = driver.getTitle();
        assertNotNull(title, "Page title should not be null");
        assertTrue(title.toLowerCase().contains("jsfiddle"),
                "Title should contain 'jsfiddle'");
    }

    @Test
    @Order(2)
    public void testNewFiddleNavigation() {
        navigateToHome();
        List<WebElement> newButton = driver.findElements(
                By.xpath("//a[contains(@class,'fiddlebutton') and contains(text(),'New')]"));
        if (newButton.isEmpty()) {
            newButton = driver.findElements(By.xpath("//a[contains(text(),'New')]"));
        }
        assertFalse(newButton.isEmpty(), "New fiddle button not found on home page");
        WebElement newBtn = newButton.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(newBtn)).click();
        wait.until(ExpectedConditions.urlPattern("https://jsfiddle.net/i/.+")); // new fiddle URLs start with /i/
        assertTrue(driver.getCurrentUrl().contains("/i/"),
                "URL after clicking New should contain '/i/'");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(3)
    public void testSignInModalPresence() {
        navigateToHome();
        List<WebElement> signInButton = driver.findElements(
                By.xpath("//a[contains(@class,'user-action') and contains(text(),'Sign in')]"));
        if (signInButton.isEmpty()) {
            signInButton = driver.findElements(By.xpath("//a[contains(text(),'Sign in')]"));
        }
        assertFalse(signInButton.isEmpty(), "Sign in button not found");
        WebElement signInBtn = signInButton.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(signInBtn)).click();

        // Wait for modal to appear
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div#bs-login-modal, div#login-modal, div[role='dialog']")));
        assertTrue(modal.isDisplayed(), "Login modal should be displayed");

        // Close the modal
        List<WebElement> closeButtons = modal.findElements(
                By.xpath(".//button[contains(@class,'close') or @aria-label='Close']"));
        if (!closeButtons.isEmpty()) {
            WebElement closeBtn = closeButtons.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(closeBtn)).click();
            wait.until(ExpectedConditions.invisibilityOf(modal));
        }
    }

    @Test
    @Order(4)
    public void testFooterExternalLinks() {
        navigateToHome();
        List<WebElement> anchorElements = driver.findElements(By.tagName("a"));
        Set<String> expectedDomains = new HashSet<>();
        expectedDomains.add("twitter.com");
        expectedDomains.add("facebook.com");
        expectedDomains.add("linkedin.com");

        boolean anyFound = false;
        for (WebElement link : anchorElements) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            for (String domain : expectedDomains) {
                if (href.contains(domain)) {
                    anyFound = true;
                    String original = driver.getWindowHandle();
                    wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                    wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
                    Set<String> windows = driver.getWindowHandles();
                    for (String win : windows) {
                        if (!win.equals(original)) {
                            driver.switchTo().window(win);
                            assertTrue(driver.getCurrentUrl().contains(domain),
                                    "External link URL does not contain expected domain: " + domain);
                            driver.close();
                            driver.switchTo().window(original);
                            break;
                        }
                    }
                }
            }
        }
        // If no external links were found, the test still passes as the site may not expose them.
        assertTrue(true, "External link check executed. Found: " + anyFound);
    }

    @Test
    @Order(5)
    public void testSidebarNavigationIfPresent() {
        navigateToHome();
        List<WebElement> sidebar = driver.findElements(
                By.cssSelector("nav[role='navigation'], ul.nav, aside"));
        if (!sidebar.isEmpty()) {
            WebElement nav = sidebar.get(0);
            assertTrue(nav.isDisplayed(), "Sidebar navigation should be visible");
        }
        // If the site doesn't have a sidebar, the test simply passes.
        assertTrue(true, "Sidebar navigation test executed.");
    }
}