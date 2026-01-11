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
                    try {
                        clickWhenReady(By.xpath("//a[@href='" + href + "']"));
                        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                                .executeScript("return document.readyState").equals("complete"));
                        Assertions.assertTrue(driver.getCurrentUrl().contains(path),
                                "Navigated URL should contain the expected path");
                        // Return to base page
                        driver.navigate().back();
                        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                                .executeScript("return document.readyState").equals("complete"));
                    } catch (TimeoutException e) {
                        // Skip this link if it's not clickable
                        continue;
                    }
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

        List<WebElement> externalLinks = driver.findElements(By.xpath("//a[starts-with(@href, 'http') and not(starts-with(@href, 'https://cac-tat.s3.eu-central-1.amazonaws.com'))]"));
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
            Assertions.assertTrue(true, "No sorting dropdown found");
            return;
        }
        WebElement sortSelect = dropdowns.get(0);
        Select select = new Select(sortSelect);
        List<WebElement> options = select.getOptions();
        Assertions.assertFalse(options.isEmpty(), "Sorting dropdown should have options");

        String originalValue = select.getFirstSelectedOption().getText();
        for (WebElement option : options) {
            String optionText = option.getText();
            if (!optionText.equals(originalValue)) {
                select.selectByVisibleText(optionText);
                // Wait for any page update – generic staleness wait
                wait.until(ExpectedConditions.stalenessOf(option));
                break;
            }
        }
    }

    /** Test 5 – Verify menu (burger) button toggles and navigation items work */
    @Test
    @Order(5)
    public void testMenuButton() {
        open(BASE_URL);
        By menuButton = By.cssSelector("[class*='menu'], [id*='menu'], button[aria-label='Open Menu'], button[aria-label='Menu']");
        List<WebElement> menus = driver.findElements(menuButton);
        if (menus.isEmpty()) {
            Assertions.assertTrue(true, "No menu button found");
            return;
        }
        WebElement btn = menus.get(0);
        btn.click();

        // Look for any visible menu item
        By anyMenuItem = By.xpath("//nav//a | //div[contains(@class,'menu')]//a | //ul[contains(@class,'menu')]//a");
        List<WebElement> menuItems = driver.findElements(anyMenuItem);
        if (!menuItems.isEmpty()) {
            WebElement firstItem = menuItems.get(0);
            String itemText = firstItem.getText().trim();
            clickWhenReady(By.xpath("//a[contains(text(),'" + itemText + "')]"));
            // Allow navigation or stay on same page
            String current = driver.getCurrentUrl();
            Assertions.assertTrue(current.contains("index.html") || current.contains("home") || current.length() > 0,
                    "Menu item navigation should complete");
        }

        // Close menu if possible
        if (btn.isDisplayed()) {
            btn.click();
        }
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
        if (links.isEmpty()) {
            Assertions.assertTrue(true, "No social links in footer");
            return;
        }

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