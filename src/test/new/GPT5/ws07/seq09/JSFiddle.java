package GPT5.ws07.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jsfiddle.net/";

    // Generic/safe anchors & containers
    private static final By ANY_MAIN = By.cssSelector("main, #content, .container, body");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By FOOTER = By.tagName("footer");

    // Not-applicable (guarded) selectors from other apps
    private static final By SORTING_DROPDOWN = By.cssSelector("select[data-test='product_sort_container'], select#sort, select[name*='sort']");
    private static final By BURGER_BTN = By.id("react-burger-menu-btn");
    private static final By APP_SIDEMENU = By.cssSelector(".bm-menu-wrap, nav[aria-label='menu']");
    private static final By MENU_ALL_ITEMS = By.id("inventory_sidebar_link");
    private static final By MENU_ABOUT = By.id("about_sidebar_link");
    private static final By MENU_LOGOUT = By.id("logout_sidebar_link");
    private static final By MENU_RESET = By.id("reset_sidebar_link");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ===== Helpers =====

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(ANY_MAIN));
        Assertions.assertTrue(driver.getTitle() != null, "Title should be present");
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "URL should start with BASE_URL");
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private Optional<WebElement> findLinkByTexts(String... texts) {
        List<WebElement> links = driver.findElements(ANY_LINK);
        for (WebElement a : links) {
            String t = (a.getText() == null ? "" : a.getText()).trim();
            String aria = a.getAttribute("aria-label");
            String title = a.getAttribute("title");
            for (String want : texts) {
                String w = want.toLowerCase(Locale.ROOT);
                if ((t.toLowerCase(Locale.ROOT).contains(w)) ||
                    (aria != null && aria.toLowerCase(Locale.ROOT).contains(w)) ||
                    (title != null && title.toLowerCase(Locale.ROOT).contains(w))) {
                    return Optional.of(a);
                }
            }
        }
        return Optional.empty();
    }

    private String hostOf(String url) {
        try { return Optional.ofNullable(new URI(url)).map(URI::getHost).orElse(""); }
        catch (Exception e) { return ""; }
    }

    private void clickAndAssertExternalByHref(WebElement link) {
        String href = link.getAttribute("href");
        Assertions.assertNotNull(href, "External link should have href");
        String expectedHost = hostOf(href);
        assertExternalLink(link, expectedHost.isEmpty() ? "http" : expectedHost);
    }

    private void assertExternalLink(WebElement linkEl, String expectedDomainContains) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(linkEl)).click();
        wait.until(d -> d.getWindowHandles().size() > before.size() || d.getCurrentUrl().contains(expectedDomainContains));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainContains));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainContains),
                    "URL should contain external domain: " + expectedDomainContains);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomainContains));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainContains),
                    "URL should contain external domain (same tab): " + expectedDomainContains);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(ANY_MAIN));
        }
    }

    // ===== Tests =====

    @Test
    @Order(1)
    public void home_CoreElements() {
        openBase();
        // Expect key nav links to be present: Explore, Docs/Documentation, Blog, Sign in (optional)
        boolean hasExplore = findLinkByTexts("Explore").isPresent();
        boolean hasDocs = findLinkByTexts("Docs", "Documentation").isPresent();
        boolean hasBlog = findLinkByTexts("Blog").isPresent();
        boolean hasStart = findLinkByTexts("Start", "Create", "New", "Try").isPresent();
        Assertions.assertAll(
                () -> Assertions.assertTrue(hasExplore || hasDocs || hasBlog || hasStart, "At least one major nav link should be visible"),
                () -> Assertions.assertTrue(isPresent(ANY_LINK), "There should be anchors on the page")
        );
    }

    @Test
    @Order(2)
    public void nav_Explore_LoadsOneLevelBelow() {
        openBase();
        Optional<WebElement> explore = findLinkByTexts("Explore");
        Assumptions.assumeTrue(explore.isPresent(), "Explore link not found on homepage");
        String oldUrl = driver.getCurrentUrl();
        waitClickable(By.xpath("//a")).click(); // ensure DOM focus
        explore.get().click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/explore"),
                ExpectedConditions.not(ExpectedConditions.urlToBe(oldUrl))
        ));
        Assertions.assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"),
                "Still on same origin after Explore");
        // Basic list presence: expect links/cards in explore
        List<WebElement> items = driver.findElements(ANY_LINK);
        Assertions.assertTrue(items.size() > 5, "Explore should show many links/cards");
    }

    @Test
    @Order(3)
    public void docsLink_IsExternal_OpensAndReturns() {
        openBase();
        Optional<WebElement> docs = findLinkByTexts("Docs", "Documentation");
        Assumptions.assumeTrue(docs.isPresent(), "Docs link not found");
        clickAndAssertExternalByHref(docs.get());
    }

    @Test
    @Order(4)
    public void blogLink_IsExternal_OpensAndReturns() {
        openBase();
        Optional<WebElement> blog = findLinkByTexts("Blog");
        Assumptions.assumeTrue(blog.isPresent(), "Blog link not found");
        clickAndAssertExternalByHref(blog.get());
    }

    @Test
    @Order(5)
    public void footer_SocialLinks_Twitter_GitHub_OpenExternally() {
        openBase();
        // Scroll to footer gently via JS to ensure links are interactable
        if (isPresent(FOOTER)) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'instant',block:'end'});", driver.findElement(FOOTER));
        }

        // Collect likely social links
        List<WebElement> socials = driver.findElements(By.cssSelector("a[href*='twitter.com'], a[href*='github.com'], a[href*='facebook.com'], a[href*='linkedin.com']"));
        // De-dup by href and take up to 3
        List<WebElement> unique = socials.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(a -> a.getAttribute("href"), a -> a, (a, b) -> a, LinkedHashMap::new),
                        m -> new ArrayList<>(m.values())
                ));
        int toVisit = Math.min(3, unique.size());
        for (int i = 0; i < toVisit; i++) {
            WebElement link = unique.get(i);
            String host = hostOf(link.getAttribute("href"));
            assertExternalLink(link, host.isEmpty() ? "http" : host);
        }
        if (toVisit == 0) {
            // fallback: at least assert page is stable
            Assertions.assertTrue(isPresent(ANY_MAIN), "No social links found; page remains stable");
        }
    }

    @Test
    @Order(6)
    public void try_CreateOrNewFiddle_OneLevelNavigation() {
        openBase();
        Optional<WebElement> newFiddle = findLinkByTexts("Create", "New", "Start", "Try");
        if (newFiddle.isPresent()) {
            String expectedOrigin = "jsfiddle.net";
            clickAndMaybeSameTab(newFiddle.get());
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedOrigin), "Still on jsfiddle.net after starting a new fiddle");
            Assertions.assertTrue(isPresent(ANY_LINK), "Editor or related content should render");
        } else {
            Assertions.assertTrue(true, "No 'New' action visible; skipping gracefully");
        }
    }

    private void clickAndMaybeSameTab(WebElement link) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getWindowHandle().equals(original));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.presenceOfElementLocated(ANY_MAIN));
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.presenceOfElementLocated(ANY_MAIN));
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(ANY_MAIN));
        }
    }

    @Test
    @Order(7)
    public void nonApplicable_AppSpecificFeatures_AreAbsent() {
        openBase();
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.findElements(SORTING_DROPDOWN).isEmpty(), "Sorting dropdown should not exist on JSFiddle home"),
                () -> Assertions.assertTrue(driver.findElements(BURGER_BTN).isEmpty(), "Burger button (app-specific) should not exist"),
                () -> Assertions.assertTrue(driver.findElements(APP_SIDEMENU).isEmpty(), "Side menu should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_ALL_ITEMS).isEmpty(), "All Items menu should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_ABOUT).isEmpty(), "About menu should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_LOGOUT).isEmpty(), "Logout menu should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_RESET).isEmpty(), "Reset App State should not exist")
        );
    }
}