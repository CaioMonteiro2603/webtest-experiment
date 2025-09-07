package GPT20b.ws07.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Set;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class JsfiddleTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    /* --------------------------------------------------------------------- */
    /*  Test Setup & Teardown                                               */
    /* --------------------------------------------------------------------- */

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

    /* --------------------------------------------------------------------- */
    /*  Helper Methods                                                      */
    /* --------------------------------------------------------------------- */

    private void goTo(String url) {
        driver.get(url);
    }

    private void waitForVisibility(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void waitForClickability(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /* --------------------------------------------------------------------- */
    /*  Tests                                                              */
    /* --------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        goTo(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Fiddle"));
        Assertions.assertTrue(
                driver.getTitle().toLowerCase().contains("fiddle"),
                "Home page title should contain 'fiddle'.");
    }

    @Test
    @Order(2)
    public void testExternalLinksInFooter() {
        goTo(BASE_URL);
        List<String> domains = List.of("twitter.com", "github.com", "patreon.com");
        String originalHandle = driver.getWindowHandle();

        for (String domain : domains) {
            List<WebElement> links = driver.findElements(
                    By.xpath("//a[contains(@href,'" + domain + "')]"));
            if (!links.isEmpty()) {
                links.get(0).click();

                // Wait for either a new tab or URL change
                wait.until(driver1 -> {
                    Set<String> handles = driver1.getWindowHandles();
                    return handles.size() > 1 || !driver1.getCurrentUrl().equals(BASE_URL);
                });

                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > 1) {
                    handles.remove(originalHandle);
                    String newHandle = handles.iterator().next();
                    driver.switchTo().window(newHandle);
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "External link should load domain " + domain);
                    driver.close();
                    driver.switchTo().window(originalHandle);
                } else {
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "External link should load domain " + domain);
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlToBe(BASE_URL));
                }
            }
        }
    }

    @Test
    @Order(3)
    public void testNavigationMenuResources() {
        goTo(BASE_URL);
        By resourcesLink = By.xpath("//a[normalize-space()='Resources' or contains(@href,'resources')]");
        waitForClickability(resourcesLink);
        driver.findElement(resourcesLink).click();

        wait.until(ExpectedConditions.urlContains("/resources"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/resources"),
                "Clicking 'Resources' should navigate to a page containing '/resources' in the URL.");

        driver.navigate().back();
        wait.until(ExpectedConditions.titleContains("Fiddle"));
    }

    @Test
    @Order(4)
    public void testNavigationMenuHelp() {
        goTo(BASE_URL);
        By helpLink = By.xpath("//a[normalize-space()='Help' or contains(@href,'help')]");

        waitForClickability(helpLink);
        driver.findElement(helpLink).click();

        wait.until(ExpectedConditions.urlContains("/help"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/help"),
                "Clicking 'Help' should navigate to a page containing '/help' in the URL.");

        driver.navigate().back();
        wait.until(ExpectedConditions.titleContains("Fiddle"));
    }

    @Test
    @Order(5)
    public void testNewFiddleButtonCreatesNewExample() {
        goTo(BASE_URL);
        By newFiddleBtn = By.xpath("//a[normalize-space()='New fiddle' or @href='/new/']");
        waitForClickability(newFiddleBtn);
        driver.findElement(newFiddleBtn).click();

        wait.until(ExpectedConditions.urlContains("/new/"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/new/"),
                "Clicking 'New fiddle' should navigate to a URL containing '/new/'.");
    }

    @Test
    @Order(6)
    public void testThemeDropdownSelect() {
        goTo(BASE_URL);
        // The theme dropdown is present in the editor area; use its id or a stable CSS selector
        By themeSelect = By.cssSelector("select#theme"); // placeholder selector
        List<WebElement> selects = driver.findElements(themeSelect);
        if (!selects.isEmpty()) {
            waitForClickability(themeSelect);
            WebElement selectEl = driver.findElement(themeSelect);
            // Grab all options to test selection
            List<WebElement> options = selectEl.findElements(By.tagName("option"));
            Assertions.assertFalse(options.isEmpty(), "Theme dropdown should have options.");
            String originalValue = selectEl.getAttribute("value");

            for (int i = 0; i < options.size(); i++) {
                if (!options.get(i).getAttribute("value").equals(originalValue)) {
                    options.get(i).click();
                    wait.until(ExpectedConditions.attributeToBe(
                            themeSelect, "value", options.get(i).getAttribute("value")));
                    Assertions.assertEquals(
                            options.get(i).getAttribute("value"),
                            selectEl.getAttribute("value"),
                            "Theme selection should update the dropdown value.");
                }
            }
        }
    }
}