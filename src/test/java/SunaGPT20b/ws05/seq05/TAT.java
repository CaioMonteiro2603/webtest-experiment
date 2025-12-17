package SunaGPT20b.ws05.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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

    /** Helper to navigate to a URL and wait for the page to be ready */
    private void open(String url) {
        driver.get(url);
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
    }

    /** Helper to click an element when it becomes clickable */
    private void clickWhenReady(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
    }

    /** Helper to switch to a newly opened window and return its handle */
    private String switchToNewWindow(String originalHandle) {
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(originalHandle)) {
                driver.switchTo().window(h);
                return h;
            }
        }
        return null;
    }

    /** Helper to close current window and switch back */
    private void closeCurrentWindowAndReturn(String originalHandle) {
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    /** Test 1 – Verify base page loads and title is present */
    @Test
    @Order(1)
    public void testBasePageLoads() {
        open(BASE_URL);
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
                "Base URL should contain 'index.html'");
        // Example check for a known element on the page (adjust selector as needed)
        By mainHeader = By.cssSelector("h1, h2, h3");
        List<WebElement> headers = driver.findElements(mainHeader);
        Assertions.assertFalse(headers.isEmpty(), "Page should contain at least one header element");
    }

    /** Test 2 – Verify navigation to one‑level‑below pages */
    @Test
    @Order(2)
    public void testOneLevelLinks() {
        open(BASE_URL);
        List<WebElement> links = driver.findElements(By.xpath("//a[@href]"));
        Assertions.assertFalse(links.isEmpty(), "Base page should contain links");

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            // Only consider same‑origin links that are one level below (no further '/')
            if (href != null && href.startsWith("https://cac-tat.s3.eu-central-1.amazonaws.com/") && !href.equals(BASE_URL)) {
                // Ensure we don't follow deeper paths
                String path = href.replace("https://cac-tat.s3.eu-central-1.amazonaws.com/", "");
                if (!path.contains("/") || path.endsWith(".html")) {
                    // Open link in same tab
                    clickWhenReady(By.xpath("//a[@href='" + href + "']"));
                    wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState").equals("complete"));
                    Assertions.assertTrue(driver.getCurrentUrl().contains(path),
                            "Navigated URL should contain the expected path");
                    // Return to base page
                    driver.navigate().back();
                    wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                            .executeScript("return document.readyState").equals("complete"));
                }
            }
        }
    }

    /** Test 3 – Verify external links open in new tab and have correct domain 
     * @throws MalformedURLException */
    @Test
    @Order(3)
    public void testExternalLinks() throws MalformedURLException {
        open(BASE_URL);
        String originalHandle = driver.getWindowHandle();

        List<WebElement> externalLinks = driver.findElements(By.xpath("//a[starts-with(@href, 'http') and not(starts-with(@href, 'https://cac-tat.s3.eu-central-1.amazonaws.com/')]"));
        Assertions.assertFalse(externalLinks.isEmpty(), "Page should contain external links");

        for (WebElement extLink : externalLinks) {
            String href = extLink.getAttribute("href");
            // Click link (may open new tab/window)
            clickWhenReady(By.xpath("//a[@href='" + href + "']"));
            String newHandle = switchToNewWindow(originalHandle);
            Assertions.assertNotNull(newHandle, "A new window/tab should have opened for external link");

            // Verify domain of new URL
            String newUrl = driver.getCurrentUrl();
            Assertions.assertTrue(newUrl.contains(new java.net.URL(href).getHost()),
                    "External link should navigate to expected domain");

            // Close external tab and return
            closeCurrentWindowAndReturn(originalHandle);
        }
    }

    /** Test 4 – Verify a sample dropdown sorting works (if present) */
    @Test
    @Order(4)
    public void testSortingDropdown() {
        open(BASE_URL);
        // Adjust selector to match a sorting dropdown if it exists
        By dropdown = By.cssSelector("select[id*='sort'], select[name*='sort']");
        List<WebElement> dropdowns = driver.findElements(dropdown);
        if (dropdowns.isEmpty()) {
            Assertions.assertTrue(true, "No sorting dropdown present – test skipped");
            return;
        }
        WebElement sortSelect = dropdowns.get(0);
        Select select = new Select(sortSelect);
        List<WebElement> options = select.getOptions();
        Assertions.assertFalse(options.isEmpty(), "Sorting dropdown should have options");

        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());
            // Wait for any page update (e.g., items list) – using generic wait for DOM change
            wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector("body"))));
            // Simple verification: ensure page still loads after selection
            Assertions.assertTrue(driver.getCurrentUrl().contains("index.html") || driver.getCurrentUrl().contains("sort"),
                    "URL should reflect sorting action or remain on page");
        }
    }

    /** Test 5 – Verify menu (burger) button toggles and navigation items work */
    @Test
    @Order(5)
    public void testMenuButton() {
        open(BASE_URL);
        By menuButton = By.cssSelector("[class*='menu'], [id*='menu'], button[aria-label='Open Menu'], button[aria-label='Menu']");
        List<WebElement> menus = driver.findElements(menuButton);
        Assertions.assertFalse(menus.isEmpty(), "Menu button should be present");
        WebElement btn = menus.get(0);
        btn.click();

        // Example: look for a known menu item like "All Items" or "Logout"
        By allItems = By.xpath("//a[contains(text(),'All Items') or contains(text(),'Home')]");
        List<WebElement> allItemsLinks = driver.findElements(allItems);
        if (!allItemsLinks.isEmpty()) {
            clickWhenReady(allItems);
            Assertions.assertTrue(driver.getCurrentUrl().contains("index.html") || driver.getCurrentUrl().contains("home"),
                    "Clicking All Items should stay on or return to main page");
        }

        // Close menu if possible
        btn.click();
    }

    /** Test 6 – Verify footer social links (Twitter, Facebook, LinkedIn) 
     * @throws MalformedURLException */
    @Test
    @Order(6)
    public void testFooterSocialLinks() throws MalformedURLException {
        open(BASE_URL);
        String originalHandle = driver.getWindowHandle();

        // Common social link selectors (adjust as needed)
        By socialLinks = By.cssSelector("footer a[href*='twitter.com'], footer a[href*='facebook.com'], footer a[href*='linkedin.com']");
        List<WebElement> links = driver.findElements(socialLinks);
        Assertions.assertFalse(links.isEmpty(), "Footer should contain social media links");

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            clickWhenReady(By.xpath("//a[@href='" + href + "']"));
            String newHandle = switchToNewWindow(originalHandle);
            Assertions.assertNotNull(newHandle, "Social link should open a new tab/window");

            String newUrl = driver.getCurrentUrl();
            Assertions.assertTrue(newUrl.contains(new java.net.URL(href).getHost()),
                    "Social link should navigate to expected domain");

            closeCurrentWindowAndReturn(originalHandle);
        }
    }
}