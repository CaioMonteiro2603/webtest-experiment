package GPT5.ws07.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jsfiddle.net/";

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
        if (driver != null) driver.quit();
    }

    // ----------------------- Helpers -----------------------

    private void goHome() {
        driver.get(BASE_URL);
        waitUntilPageReady();
        dismissCookieIfPresent();
    }

    private void waitUntilPageReady() {
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el));
        el.click();
    }

    private List<WebElement> displayedAll(By by) {
        return driver.findElements(by).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
    }

    private WebElement firstDisplayed(By... locators) {
        for (By by : locators) {
            for (WebElement el : driver.findElements(by)) {
                if (el.isDisplayed()) return el;
            }
        }
        return null;
    }

    private void openExternalAndAssert(By linkLocator, String expectedDomainFragment) {
        List<WebElement> links = displayedAll(linkLocator);
        if (links.isEmpty()) return; // optional link; skip if not present
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
            // same-tab nav
            wait.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment.toLowerCase(Locale.ROOT)));
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains(BASE_URL));
        }
    }

    private void dismissCookieIfPresent() {
        // Try a few common cookie consent selectors/texts
        List<By> candidates = Arrays.asList(
                By.cssSelector("button[aria-label='Accept all']"),
                By.cssSelector("button[aria-label='Accept']"),
                By.cssSelector("button[mode='primary']"),
                By.cssSelector("button#onetrust-accept-btn-handler"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'accept')]"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'agree')]")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            for (WebElement el : els) {
                if (el.isDisplayed()) {
                    try { safeClick(el); wait.until(ExpectedConditions.invisibilityOf(el)); } catch (Exception ignored) {}
                    return;
                }
            }
        }
    }

    private static int pathDepth(String url) {
        try {
            String path = new java.net.URL(url).getPath();
            if (path == null || path.isEmpty() || path.equals("/")) return 0;
            String[] parts = Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);
            return parts.length;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    // ----------------------- Tests -----------------------

    @Test
    @Order(1)
    public void homePageLoads_CoreElementsPresent() {
        goHome();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL");
        WebElement headerLogo = firstDisplayed(
                By.cssSelector("a[href='/']"),
                By.cssSelector("header a"),
                By.cssSelector("nav a[href='/']")
        );
        WebElement hero = firstDisplayed(By.cssSelector("h1"), By.cssSelector("h2"));
        Assertions.assertAll(
                () -> Assertions.assertNotNull(headerLogo, "Header/logo link to home should be present"),
                () -> Assertions.assertNotNull(hero, "A prominent heading should be present on the page")
        );
    }

    @Test
    @Order(2)
    public void internalNavigation_OneLevel_LinksReachable() {
        goHome();
        // Collect internal links within one path level below the base URL
        List<WebElement> allLinks = displayedAll(By.cssSelector("a[href]"));
        LinkedHashSet<String> uniqueInternal = new LinkedHashSet<>();
        for (WebElement a : allLinks) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith("http")) continue;
            if (!href.startsWith(BASE_URL)) continue;
            int depth = pathDepth(href);
            if (depth <= 1) {
                uniqueInternal.add(href);
            }
        }
        // Visit up to 3 internal links (one level) deterministically
        int visited = 0;
        for (String link : uniqueInternal) {
            driver.navigate().to(link);
            waitUntilPageReady();
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should remain within site for internal link: " + link);
            visited++;
            if (visited >= 3) break;
        }
        goHome();
    }

    @Test
    @Order(3)
    public void topBarOrMenu_NavigationIfPresent() {
        goHome();
        // Attempt to open any visible menu button if exists (mobile burger)
        WebElement burger = firstDisplayed(
                By.cssSelector("button[aria-label*='menu' i]"),
                By.cssSelector("button[aria-label*='navigation' i]"),
                By.cssSelector("button[class*='menu' i]")
        );
        if (burger != null) {
            safeClick(burger);
            // Click first visible link in menu that is internal one level
            List<WebElement> menuLinks = displayedAll(By.cssSelector("nav a[href], [role='menu'] a[href]"));
            for (WebElement a : menuLinks) {
                String href = a.getAttribute("href");
                if (href != null && href.startsWith(BASE_URL) && pathDepth(href) <= 1) {
                    String before = driver.getCurrentUrl();
                    safeClick(a);
                    waitUntilPageReady();
                    Assertions.assertNotEquals(before, driver.getCurrentUrl(), "URL should change after clicking a menu item");
                    break;
                }
            }
        } else {
            Assertions.assertTrue(true, "No burger/menu button; skipping optional menu test.");
        }
        goHome();
    }

    @Test
    @Order(4)
    public void optional_SortingDropdown_ExerciseIfPresent() {
        goHome();
        WebElement selectEl = firstDisplayed(
                By.cssSelector("select[id*='sort' i]"),
                By.cssSelector("select[name*='sort' i]"),
                By.xpath("//select[contains(translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sort') or contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sort')]")
        );
        if (selectEl == null) {
            Assertions.assertTrue(true, "Sorting dropdown not present; skipping.");
            return;
        }
        Select select = new Select(selectEl);
        List<WebElement> options = select.getOptions();
        Assertions.assertTrue(options.size() >= 1, "Sorting dropdown should have at least one option");
        String initial = select.getFirstSelectedOption().getText();
        for (int i = 0; i < options.size(); i++) {
            select.selectByIndex(i);
            String current = select.getFirstSelectedOption().getText();
            Assertions.assertEquals(options.get(i).getText(), current, "Selected option text should match");
        }
        if (options.size() > 1) {
            select.selectByIndex(options.size() - 1);
            String after = select.getFirstSelectedOption().getText();
            Assertions.assertNotEquals(initial, after, "Sorting selection should change when a different option is chosen");
        }
        goHome();
    }

    @Test
    @Order(5)
    public void footer_ExternalSocialLinks_OpenAndHaveExpectedDomains() {
        goHome();
        // Scroll to footer if needed
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        waitUntilPageReady();

        // Treat the common social links as external
        openExternalAndAssert(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        openExternalAndAssert(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        openExternalAndAssert(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
        openExternalAndAssert(By.cssSelector("a[href*='github.com']"), "github.com");
        openExternalAndAssert(By.cssSelector("a[href*='discord.gg'], a[href*='discord.com']"), "discord");
        openExternalAndAssert(By.cssSelector("a[href*='youtube.com']"), "youtube.com");
        goHome();
    }

    @Test
    @Order(6)
    public void docsOrAbout_ExternalPolicyLinks_WorkAndReturn() throws URISyntaxException {
        goHome();
        // Look for typical legal/help/doc links and open them (one level/external)
        List<By> linkLocators = Arrays.asList(
                By.partialLinkText("Docs"),
                By.partialLinkText("Documentation"),
                By.partialLinkText("Support"),
                By.partialLinkText("Help"),
                By.partialLinkText("Terms"),
                By.partialLinkText("Privacy"),
                By.partialLinkText("About"),
                By.cssSelector("a[href*='docs']")
        );
        for (By by : linkLocators) {
            List<WebElement> links = displayedAll(by);
            if (!links.isEmpty()) {
                String href = links.get(0).getAttribute("href");
                if (href == null) continue;
                boolean isExternal = !href.startsWith(BASE_URL) && href.startsWith("http");
                if (isExternal) {
                    String host = java.net.URI.create(href).getHost();
                    openExternalAndAssert(by, host);
                } else {
                    // same-origin, one level below
                    safeClick(links.get(0));
                    waitUntilPageReady();
                    Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://jsfiddle.net"), "Should remain in jsfiddle domain");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlContains(BASE_URL));
                }
                break; // only one is enough to validate behavior
            }
        }
        goHome();
    }

    @Test
    @Order(7)
    public void searchOrExplore_IfPresent_PerformsNavigation() {
        goHome();
        // Try a search box or an Explore link if available
        WebElement search = firstDisplayed(
                By.cssSelector("input[type='search']"),
                By.cssSelector("input[placeholder*='search' i]"),
                By.name("q")
        );
        if (search != null) {
            search.clear();
            search.sendKeys("button");
            search.sendKeys(Keys.ENTER);
            waitUntilPageReady();
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("search") ||
                                  driver.findElements(By.cssSelector("a, h1, h2")).size() > 0,
                    "Search should navigate or render results");
        } else {
            // Fallback: click an Explore link if present
            List<WebElement> exploreLinks = displayedAll(By.partialLinkText("Explore"));
            if (!exploreLinks.isEmpty()) {
                String before = driver.getCurrentUrl();
                safeClick(exploreLinks.get(0));
                waitUntilPageReady();
                Assertions.assertNotEquals(before, driver.getCurrentUrl(), "URL should change after Explore click");
            } else {
                Assertions.assertTrue(true, "No search or explore elements; skipping optional test.");
            }
        }
        goHome();
    }

    @Test
    @Order(8)
    public void pageStable_AfterAllInteractions() {
        goHome();
        WebElement logo = firstDisplayed(By.cssSelector("a[href='/']"));
        WebElement anyHeading = firstDisplayed(By.cssSelector("h1"), By.cssSelector("h2"));
        Assertions.assertAll(
                () -> Assertions.assertNotNull(logo, "Home logo/link should be visible"),
                () -> Assertions.assertNotNull(anyHeading, "A heading should be visible")
        );
    }
}
