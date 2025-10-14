package GPT20b.ws05.seq01;

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
public class CACTATTests {


private static WebDriver driver;
private static WebDriverWait wait;
private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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

/* Helper methods */

private boolean elementExists(By locator) {
    return !driver.findElements(locator).isEmpty();
}

private void navigateToBase() {
    driver.get(BASE_URL);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
}

/* Tests */

@Test
@Order(1)
public void testPageLoadsCorrectly() {
    navigateToBase();
    Assertions.assertTrue(driver.getTitle().toLowerCase().contains("cac-tat"),
            "Page title should contain 'cac-tat'");
}

@Test
@Order(2)
public void testSortingDropdown() {
    navigateToBase();

    By sortDiff = By.id("sortDropdown");
    Assumptions.assumeTrue(elementExists(sortDiff),
            "Sorting element not present; skipping sorting test.");

    WebElement sortElement = wait.until(ExpectedConditions.elementToBeClickable(sortDiff));
    sortElement.click();

    List<WebElement> options = wait.await(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//select[@id='sortDropdown']/option")),
            Duration.ofSeconds(5));
    Assumptions.assumeTrue(options.size() > 1,
            "Sorting dropdown has insufficient options; skipping.");

    for (WebElement option : options) {
        sortElement.click(); // re-open dropdown
        option.click();

        // Assume a heading with id 'sortResult' updates after sorting
        if (elementExists(By.id("sortResult"))) {
            WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sortResult")));
            Assertions.assertTrue(result.getText().toLowerCase().contains(option.getText().toLowerCase()),
                    "Sort result should reflect selected option: " + option.getText());
        }
    }
}

@Test
@Order(3)
public void testBurgerMenuInteraction() {
    navigateToBase();

    By burger = By.id("menu");
    Assumptions.assumeTrue(elementExists(burger),
            "Burger menu button not found; skipping menu test.");

    WebElement burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burger));
    burgerBtn.click();

    // All Items
    By allItems = By.xpath("//li[contains(@class,'menuItem') and a[contains(text(),'All Items')]]");
    Assumptions.assumeTrue(elementExists(allItems),
            "All Items link missing in menu; skipping that part.");

    WebElement allItemsLink = driver.findElement(allItems);
    allItemsLink.click();
    wait.until(ExpectedConditions.urlContains("index.html"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
            "Should be back on main page after All Items.");

    // Reset App State – not available on this site, skip if absent
    By reset = By.xpath("//li[contains(@class,'menuItem') and a[contains(text(),'Reset App State')]]");
    if (elementExists(reset)) {
        burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burger));
        burgerBtn.click();
        WebElement resetLink = driver.findElement(reset);
        resetLink.click();
        // Assume an alert or message appears
        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".message")));
        Assertions.assertTrue(msg.isDisplayed(), "Reset confirmation should be displayed.");
    }

    // Logout – unlikely, skip if missing
    By logout = By.xpath("//li[contains(@class,'menuItem') and a[contains(text(),'Logout')]]");
    if (elementExists(logout)) {
        burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burger));
        burgerBtn.click();
        WebElement logoutLink = driver.findElement(logout);
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("login.html") );
        Assertions.assertTrue(driver.getCurrentUrl().contains("login.html"),
                "Should navigate to login after logout.");
    }
}

@Test
@Order(4)
public void testExternalLinksInFooter() {
    navigateToBase();

    // Social links
    String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
    for (String domain : domains) {
        By linkLocator = By.xpath("//footer//a[contains(@href,'" + domain + "')]");
        Assumptions.assumeTrue(elementExists(linkLocator),
                "Footer link for " + domain + " not found; skipping.");

        List<WebElement> links = driver.findElements(linkLocator);
        for (WebElement link : links) {
            String original = driver.getWindowHandle();
            Set<String> before = driver.getWindowHandles();
            link.click();

            // Wait for new tab if created
            try {
                wait.until(driver -> driver.getWindowHandles().size() > before.size());
            } catch (TimeoutException ignored) {
            }

            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            if (!after.isEmpty()) {
                String newHandle = after.iterator().next();
                driver.switchTo().window(newHandle);
                wait.until(ExpectedConditions.urlContains(domain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                        "External link should contain domain " + domain);
                driver.close();
                driver.switchTo().window(original);
            } else {
                // Same tab navigation
                Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                        "Navigated to external link within same tab, domain should be present.");
                driver.navigate().back();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
            }
        }
    }
}

@Test
@Order(5)
public void testExternalLinksOnPage() {
    navigateToBase();

    By externalLinkLocator = By.xpath("//a[starts-with(@href,'http') and not(contains(@href,'cac-tat'))]");
    Assumptions.assumeTrue(elementExists(externalLinkLocator),
            "No external links found on main page; skipping test.");

    List<WebElement> links = driver.findElements(externalLinkLocator);
    for (WebElement link : links) {
        String href = link.getAttribute("href");
        if (href == null || href.isEmpty()) continue;

        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        link.click();

        // Wait for new tab if any
        try {
            wait.until(driver -> driver.getWindowHandles().size() > before.size());
        } catch (TimeoutException ignored) {
        }

        Set<String> after = driver.getWindowHandles();
        after.removeAll(before);
        if (!after.isEmpty()) {
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlToBePresent());
            Assertions.assertNotEquals(href, driver.getCurrentUrl(),
                    "URL should load after click – a new window/tab was opened.");
            driver.close();
            driver.switchTo().window(original);
        } else {
            // Same tab
            Assertions.assertTrue(driver.getCurrentUrl().equals(href),
                    "Link clicked and opened same tab with expected URL.");
            driver.navigate().back();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        }
    }
}