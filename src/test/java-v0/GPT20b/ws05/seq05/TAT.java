package GPT20b.ws05.seq05;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.net.URISyntaxException;

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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String HOST = "cac-tat.s3.eu-central-1.amazonaws.com";

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
    /* Utilities                                                        */
    /* ------------------------------------------------------------------ */

    private void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private Set<String> externalLinkHosts() {
        Set<String> hosts = new HashSet<>();
        hosts.add("twitter.com");
        hosts.add("facebook.com");
        hosts.add("linkedin.com");
        hosts.add("github.com");
        return hosts;
    }

    /* ------------------------------------------------------------------ */
    /* Tests                                                            */
    /* ------------------------------------------------------------------ */

    @Test
    @Order(1)
    public void testPageLoadsSuccessfully() {
        navigateToBase();
        String title = driver.getTitle();
        assertNotNull(title, "Page title must be present");
        assertTrue(title.length() > 0, "Page title should not be empty");
        WebElement body = driver.findElement(By.tagName("body"));
        assertTrue(body.isDisplayed(), "Body element should be displayed");
    }

    @Test
    @Order(2)
    public void testFormPresenceAndInputs() {
        navigateToBase();
        List<WebElement> forms = driver.findElements(By.tagName("form"));
        assertFalse(forms.isEmpty(), "No form elements found on the page");
        // Inspect first form for input fields
        WebElement form = forms.get(0);
        List<WebElement> inputs = form.findElements(By.tagName("input"));
        assertFalse(inputs.isEmpty(), "Form should contain input elements");
        boolean foundInput = false;
        for (WebElement input : inputs) {
            if (input.isDisplayed() && input.isEnabled()) {
                foundInput = true;
                break;
            }
        }
        assertTrue(foundInput, "Form should have a visible, enabled input element");
    }

    @Test
    @Order(3)
    public void testSortingDropdownFunctionality() {
        navigateToBase();
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        assertFalse(selects.isEmpty(), "No dropdowns found on the page");
        WebElement sortSelect = selects.get(0); // use first select as potential sorting control
        Select select = new Select(sortSelect);
        int optionCount = select.getOptions().size();
        assertTrue(optionCount > 1, "Sorting dropdown should have multiple options");
        String initialValue = select.getFirstSelectedOption().getAttribute("value");
        for (int i = 0; i < optionCount; i++) {
            Select sort = new Select(sortSelect);
            sort.selectByIndex(i);
            wait.until(ExpectedConditions.stalenessOf(sortSelect)); // wait for potential page update
            String currentValue = new Select(sortSelect).getFirstSelectedOption().getAttribute("value");
            assertNotEquals(initialValue, currentValue,
                    "Sorting option changed but value remains the same");
            initialValue = currentValue;
        }
    }

    @Test
    @Order(4)
    public void testBurgerMenuInteractions() {
        navigateToBase();
        // Search for a common burger menu button by aria-label or class
        List<WebElement> burgerButtons = driver.findElements(By.cssSelector("button[aria-label*='menu'], .burger-menu, .hamburger"));
        if (burgerButtons.isEmpty()) {
            // If none found, the application might not have such a menu
            return;
        }
        WebElement burgerBtn = burgerButtons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(burgerBtn)).click();
        // After opening, expect some menu items to be visible
        List<WebElement> menuItems = driver.findElements(By.cssSelector("ul[role='menu'] li, .menu a"));
        assertFalse(menuItems.isEmpty(), "No menu items displayed after opening burger menu");
        // Close menu
        wait.until(ExpectedConditions.elementToBeClickable(burgerBtn)).click();
        // Verify menu is hidden
        boolean menuHidden = true;
        for (WebElement item : menuItems) {
            if (item.isDisplayed()) {
                menuHidden = false;
                break;
            }
        }
        assertTrue(menuHidden, "Menu items still displayed after closing burger menu");
    }

    @Test
    @Order(5)
    public void testExternalLinksHandling() throws URISyntaxException {
        navigateToBase();
        List<WebElement> anchorElements = driver.findElements(By.tagName("a"));
        assertFalse(anchorElements.isEmpty(), "No link elements found on the page");
        Set<String> handled = new HashSet<>();
        Set<String> expectedExternalHosts = externalLinkHosts();

        for (WebElement link : anchorElements) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty() || href.contains(HOST)) {
                continue; // skip internal or missing links
            }
            String host = new java.net.URI(href).getHost();
            if (!expectedExternalHosts.contains(host) || handled.contains(href)) {
                continue; // filter only known external domains and avoid duplicates
            }
            handled.add(href);
            String originalWindow = driver.getWindowHandle();
            wait.until(ExpectedConditions.elementToBeClickable(link)).click();
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            for (String win : driver.getWindowHandles()) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains(host),
                            "Expected host " + host + " not found in URL: " + currentUrl);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
        }

        assertFalse(handled.isEmpty(), "No expected external links were found and validated");
    }
}