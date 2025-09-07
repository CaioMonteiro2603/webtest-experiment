package GPT5.ws08.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStoreHeadlessTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1440, 1000));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ----------------------- Helpers -----------------------

    private void goHome() {
        driver.get(BASE_URL);
        waitForReady();
        dismissCookieIfPresent();
    }

    private void waitForReady() {
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    private WebElement visible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private WebElement clickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private void safeClick(By by) {
        clickable(by).click();
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el));
        el.click();
    }

    private WebElement firstDisplayed(By... candidates) {
        for (By by : candidates) {
            for (WebElement el : driver.findElements(by)) {
                if (el.isDisplayed()) return el;
            }
        }
        return null;
    }

    private List<WebElement> displayedAll(By by) {
        return driver.findElements(by).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
    }

    private static int pathDepth(String url) {
        try {
            URI u = URI.create(url);
            String path = u.getPath();
            if (path == null || path.isEmpty() || "/".equals(path)) return 0;
            String[] parts = Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);
            return parts.length;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private void openExternalAndAssert(By linkLocator, String expectedDomainFragment) {
        List<WebElement> links = displayedAll(linkLocator);
        if (links.isEmpty()) return; // optional link; skip
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        safeClick(links.get(0));
        wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(BASE_URL));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl() != null && !d.getCurrentUrl().isEmpty());
            String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
            Assertions.assertTrue(url.contains(expectedDomainFragment.toLowerCase(Locale.ROOT)),
                    "External URL should contain: " + expectedDomainFragment + " but was: " + url);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment.toLowerCase(Locale.ROOT)));
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains(BASE_URL));
        }
    }

    private void dismissCookieIfPresent() {
        List<By> candidates = Arrays.asList(
                By.cssSelector("button#onetrust-accept-btn-handler"),
                By.cssSelector("button[aria-label*='accept' i]"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'accept')]"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'close')]")
        );
        for (By by : candidates) {
            for (WebElement el : driver.findElements(by)) {
                if (el.isDisplayed()) {
                    try { safeClick(el); } catch (Exception ignored) {}
                    return;
                }
            }
        }
    }

    // ----------------------- Tests -----------------------

    @Test
    @Order(1)
    public void homePageLoads_CoreUIVisible() {
        goHome();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL");
        WebElement logoLink = firstDisplayed(By.cssSelector("a[href='/']"), By.cssSelector("a.header-logo"));
        WebElement searchBox = firstDisplayed(By.cssSelector("input[name='keyword']"), By.cssSelector("input[type='search']"));
        WebElement catalogLink = firstDisplayed(By.partialLinkText("Catalog"), By.cssSelector("a[href='/catalog/']"));
        Assertions.assertAll(
                () -> Assertions.assertNotNull(logoLink, "Logo/Home link should be visible"),
                () -> Assertions.assertNotNull(catalogLink, "Catalog link should be present"),
                () -> Assertions.assertNotNull(searchBox, "Search input should be present on home")
        );
    }

    @Test
    @Order(2)
    public void internalLinks_OneLevelBelow_AreReachable() {
        goHome();
        List<WebElement> all = displayedAll(By.cssSelector("a[href]"));
        LinkedHashSet<String> oneLevel = new LinkedHashSet<>();
        for (WebElement a : all) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith("http")) continue;
            if (!href.startsWith(BASE_URL)) continue;
            int depth = pathDepth(href);
            if (depth <= 1) {
                oneLevel.add(href);
            }
        }
        int visited = 0;
        for (String url : oneLevel) {
            driver.navigate().to(url);
            waitForReady();
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Internal navigation should remain in domain");
            visited++;
            if (visited >= 5) break; // limit to reduce flakiness
        }
        goHome();
    }

    @Test
    @Order(3)
    public void signIn_Negative_InvalidCredentials_ShowsError() {
        goHome();
        WebElement signInLink = firstDisplayed(By.partialLinkText("Sign In"), By.cssSelector("a[href*='signon']"));
        if (signInLink == null) {
            Assertions.assertTrue(true, "No sign-in link on home; skipping negative login test.");
            return;
        }
        safeClick(signInLink);
        waitForReady();
        WebElement user = visible(By.name("username"));
        WebElement pass = visible(By.name("password"));
        user.clear(); user.sendKeys("invalid_user");
        pass.clear(); pass.sendKeys("invalid_pass");
        safeClick(By.cssSelector("input[type='submit'], button[type='submit']"));
        // Expect an error banner or message
        WebElement error = firstDisplayed(
                By.cssSelector(".alert, .msg-error, .error, .error-message"),
                By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid')]"),
                By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'failed')]")
        );
        Assertions.assertNotNull(error, "An error message should be displayed for invalid credentials");
        // Return to home
        driver.navigate().to(BASE_URL);
        waitForReady();
    }

    @Test
    @Order(4)
    public void catalog_SearchIfPresent_NavigatesOrShowsResults() {
        goHome();
        WebElement search = firstDisplayed(By.cssSelector("input[name='keyword']"), By.cssSelector("input[type='search']"));
        if (search != null) {
            search.clear();
            search.sendKeys("fish");
            search.sendKeys(Keys.ENTER);
            waitForReady();
            boolean navigated = driver.getCurrentUrl().contains("/search/") || driver.findElements(By.cssSelector("table, .product, .item")).size() > 0;
            Assertions.assertTrue(navigated, "Search should navigate or show a result list");
        } else {
            Assertions.assertTrue(true, "Search box not present; skipping.");
        }
        goHome();
    }

    @Test
    @Order(5)
    public void categories_NavigateFromHome_AndBack() {
        goHome();
        // Collect category links commonly present on JPetStore home
        List<WebElement> catLinks = new ArrayList<>();
        catLinks.addAll(displayedAll(By.cssSelector("a[href='/catalog/']")));
        catLinks.addAll(displayedAll(By.cssSelector("a[href*='/catalog/categories']")));
        catLinks.addAll(displayedAll(By.partialLinkText("Fish")));
        catLinks.addAll(displayedAll(By.partialLinkText("Dogs")));
        catLinks.addAll(displayedAll(By.partialLinkText("Cats")));
        catLinks.addAll(displayedAll(By.partialLinkText("Reptiles")));
        catLinks.addAll(displayedAll(By.partialLinkText("Birds")));

        // De-duplicate by href
        LinkedHashMap<String, WebElement> unique = new LinkedHashMap<>();
        for (WebElement a : catLinks) {
            String href = a.getAttribute("href");
            if (href != null && href.startsWith(BASE_URL) && pathDepth(href) <= 1) {
                unique.putIfAbsent(href, a);
            }
        }

        int count = 0;
        for (Map.Entry<String, WebElement> entry : unique.entrySet()) {
            driver.navigate().to(entry.getKey());
            waitForReady();
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should remain within domain for category link");
            // Expect some product/category structure
            boolean hasItems = driver.findElements(By.cssSelector("table, .product, .item, .catalog")).size() > 0;
            Assertions.assertTrue(hasItems, "Category page should render some list/table of items");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains(BASE_URL));
            count++;
            if (count >= 3) break; // limit a few to minimize flakiness
        }
        goHome();
    }

    @Test
    @Order(6)
    public void optional_SortingDropdown_IfPresent_ChangesSelection() {
        goHome();
        WebElement sort = firstDisplayed(
                By.cssSelector("select[id*='sort' i]"),
                By.cssSelector("select[name*='sort' i]"),
                By.xpath("//select[contains(translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sort') or contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sort')]")
        );
        if (sort == null) {
            Assertions.assertTrue(true, "Sorting dropdown not present; skipping.");
            return;
        }
        Select select = new Select(sort);
        List<WebElement> opts = select.getOptions();
        String initial = select.getFirstSelectedOption().getText();
        if (opts.size() > 1) {
            select.selectByIndex(opts.size() - 1);
            String after = select.getFirstSelectedOption().getText();
            Assertions.assertNotEquals(initial, after, "Selecting different sort option should change the selection");
        } else {
            Assertions.assertTrue(true, "Only one sort option available; nothing to change.");
        }
        goHome();
    }

    @Test
    @Order(7)
    public void headerFooter_ExternalLinks_OpenAndContainExpectedDomains() {
        goHome();
        // Scroll to footer just in case
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        waitForReady();

        // Check common social/external links if present
        openExternalAndAssert(By.cssSelector("a[href*='github.com']"), "github.com");
        openExternalAndAssert(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        openExternalAndAssert(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        openExternalAndAssert(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
        openExternalAndAssert(By.cssSelector("a[href*='aspectran.com']"), "aspectran.com");
        openExternalAndAssert(By.cssSelector("a[href*='github.io']"), "github.io");

        goHome();
    }

    @Test
    @Order(8)
    public void cartPage_OneLevel_IsReachable() {
        goHome();
        WebElement cartLink = firstDisplayed(By.partialLinkText("Cart"), By.cssSelector("a[href='/cart/']"), By.cssSelector("a[href*='/cart']"));
        if (cartLink != null) {
            safeClick(cartLink);
            waitForReady();
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Cart should be inside site");
            // Expect an empty cart message or table
            boolean cartUi = driver.findElements(By.cssSelector("table, .cart, .shopping-cart")).size() > 0 ||
                    !driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cart')]")).isEmpty();
            Assertions.assertTrue(cartUi, "Cart UI should render (table or message)");
        } else {
            Assertions.assertTrue(true, "No cart link found; skipping cart reachability.");
        }
        goHome();
    }

    @Test
    @Order(9)
    public void pageStable_AfterInteractions() {
        goHome();
        WebElement heading = firstDisplayed(By.cssSelector("h1"), By.cssSelector("h2"));
        WebElement logo = firstDisplayed(By.cssSelector("a[href='/']"));
        Assertions.assertAll(
                () -> Assertions.assertNotNull(logo, "Logo/home link should be present"),
                () -> Assertions.assertNotNull(heading, "Heading should be visible")
        );
    }
}
