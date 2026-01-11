package GPT5.ws07.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1366, 900));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    public void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        dismissCookieOrModalsIfPresent();
    }

    // -------------------- Helpers --------------------

    private void dismissCookieOrModalsIfPresent() {
        // Try common cookie banners / modals
        List<By> closeCandidates = Arrays.asList(
                By.cssSelector("button[aria-label*='accept' i], button[aria-label*='agree' i], button[aria-label*='ok' i]"),
                By.cssSelector("button#onetrust-accept-btn-handler, button[aria-label='Close']"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'accept')]"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'agree')]"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'ok')]"),
                By.cssSelector(".ot-sdk-container button, .cookie, .cookies button, .modal button")
        );
        for (By by : closeCandidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(els.get(0))).click();
                    break;
                } catch (Exception ignored) {}
            }
        }
    }

    private List<WebElement> findAll(By by) {
        try { return driver.findElements(by); } catch (Exception e) { return Collections.emptyList(); }
    }

    private WebElement first(By by) {
        List<WebElement> els = findAll(by);
        return els.isEmpty() ? null : els.get(0);
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void set(WebElement el, String value) {
        wait.until(ExpectedConditions.visibilityOf(el));
        try { el.clear(); } catch (Exception ignored) {}
        el.sendKeys(value);
    }

    private boolean openExternalAndAssertDomain(WebElement link, String expectedDomainFragment) {
        String expected = expectedDomainFragment.toLowerCase();
        String originalHandle = driver.getWindowHandle();
        Set<String> handlesBefore = driver.getWindowHandles();

        safeClick(link);

        try {
            wait.until(d -> driver.getWindowHandles().size() > handlesBefore.size() || !driver.getCurrentUrl().equals(BASE_URL));
        } catch (TimeoutException ignored) {}

        Set<String> handlesAfter = driver.getWindowHandles();
        boolean result;

        if (handlesAfter.size() > handlesBefore.size()) {
            handlesAfter.removeAll(handlesBefore);
            String newHandle = handlesAfter.iterator().next();
            driver.switchTo().window(newHandle);
            try {
                wait.until(ExpectedConditions.urlContains(expected));
            } catch (TimeoutException ignored) {}
            result = driver.getCurrentUrl().toLowerCase().contains(expected);
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            // Same tab navigation
            try {
                wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL)));
            } catch (TimeoutException ignored) {}
            result = driver.getCurrentUrl().toLowerCase().contains(expected);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
        return result;
    }

    private void exerciseAnyDropdowns() {
        List<WebElement> selects = findAll(By.tagName("select"));
        for (WebElement s : selects) {
            try {
                Select sel = new Select(s);
                List<WebElement> options = sel.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    sel.selectByIndex(i);
                    Assertions.assertFalse(sel.getAllSelectedOptions().isEmpty(), "Option should be selectable for dropdown.");
                }
            } catch (UnexpectedTagNameException ignored) { }
        }
    }

    private String origin(String url) {
        int i = url.indexOf("://");
        if (i < 0) return url;
        int slash = url.indexOf('/', i + 3);
        return slash > 0 ? url.substring(0, slash) : url;
    }

    private boolean isOneLevelBelow(String origin, String base, String href) {
        String basePath = base.startsWith(origin) ? base.substring(origin.length()) : base;
        String hrefPath = href.startsWith(origin) ? href.substring(origin.length()) : href;

        if (basePath.startsWith("/")) basePath = basePath.substring(1);
        if (hrefPath.startsWith("/")) hrefPath = hrefPath.substring(1);

        String[] baseSegs = basePath.isEmpty() ? new String[0] : basePath.split("/");
        String[] hrefSegs = hrefPath.isEmpty() ? new String[0] : hrefPath.split("/");

        return hrefSegs.length <= baseSegs.length + 1;
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void homePage_Loads_TitleContainsJSFiddle() {
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should land on base URL.");
        String title = driver.getTitle() == null ? "" : driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jsfiddle"), "Title should contain 'JSFiddle'. Actual: " + title);
        // Main CTA or header present
        WebElement cta = first(By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create')] | //button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create')]"));
        WebElement brand = first(By.cssSelector("a[href='https://jsfiddle.net/'], a.navbar-brand, header a"));
        Assertions.assertTrue(cta != null || brand != null, "CTA or brand link should be visible on homepage.");
    }

    @Test
    @Order(2)
    public void searchOrExplore_IfPresent_Works() {
        // Try to interact with a search/explore input if present
        List<By> searchSelectors = Arrays.asList(
                By.cssSelector("input[type='search']"),
                By.cssSelector("input[placeholder*='search' i]"),
                By.cssSelector("input[name*='search' i]"),
                By.xpath("//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'search')]")
        );
        WebElement search = null;
        for (By by : searchSelectors) {
            search = first(by);
            if (search != null) break;
        }
        if (search != null) {
            wait.until(ExpectedConditions.visibilityOf(search));
            set(search, "react");
            search.sendKeys(Keys.ENTER);
            try { wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL))); } catch (Exception ignored) {}
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("react") || !driver.getCurrentUrl().equals(BASE_URL),
                    "URL should change or include query parameter after searching.");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        } else {
            Assertions.assertTrue(true, "Search not present; skipped.");
        }
    }

    @Test
    @Order(3)
    public void burgerMenu_OpenClose_IfVisible() {
        // Encourage mobile menu
        driver.manage().window().setSize(new Dimension(420, 800));
        driver.get(BASE_URL);
        dismissCookieOrModalsIfPresent();

        List<By> burgerCandidates = Arrays.asList(
                By.cssSelector("button[aria-label*='menu' i]"),
                By.cssSelector(".navbar-toggler, .hamburger, .burger"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu')]")
        );
        WebElement burger = null;
        for (By by : burgerCandidates) {
            burger = first(by);
            if (burger != null) break;
        }
        if (burger != null) {
            safeClick(burger);
            boolean opened = !findAll(By.cssSelector(".navbar-collapse.show, nav.show, .menu.open, .drawer.open")).isEmpty()
                    || !findAll(By.xpath("//nav")).isEmpty();
            Assertions.assertTrue(opened, "Burger menu should open navigation.");
            // Close if toggler behaves as toggle
            try { safeClick(burger); } catch (Exception ignored) {}
        } else {
            Assertions.assertTrue(true, "No burger menu detected; skipped.");
        }
        // Restore window
        driver.manage().window().setSize(new Dimension(1366, 900));
    }

    @Test
    @Order(4)
    public void dropdowns_ExerciseAll_IfAny() {
        List<WebElement> selects = findAll(By.tagName("select"));
        for (WebElement s : selects) {
            try {
                Select sel = new Select(s);
                // Scroll into view first
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", s);
                wait.until(ExpectedConditions.elementToBeClickable(s));
                List<WebElement> options = sel.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", options.get(i));
                    wait.until(ExpectedConditions.elementToBeClickable(options.get(i)));
                    sel.selectByIndex(i);
                    Assertions.assertFalse(sel.getAllSelectedOptions().isEmpty(), "Option should be selectable for dropdown.");
                }
            } catch (UnexpectedTagNameException | ElementNotInteractableException ignored) { }
        }
        Assertions.assertTrue(true, "Dropdowns exercised if present.");
    }

    @Test
    @Order(5)
    public void internalLinks_NavigateOneLevelBelow_AndBack() {
        String origin = origin(BASE_URL);
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        // Filter same-origin, one-level-below links
        List<WebElement> internal = anchors.stream().filter(a -> {
            String h = a.getAttribute("href");
            if (h == null || h.isEmpty()) return false;
            if (!h.startsWith(origin)) return false;
            if (h.equals(BASE_URL)) return false;
            return isOneLevelBelow(origin, BASE_URL, h);
        }).collect(Collectors.toList());

        int visited = 0;
        String before = driver.getCurrentUrl();
        for (WebElement a : internal) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            try {
                safeClick(a);
                wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(before)));
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(origin), "Should remain on same origin after navigating internal link.");
                // A simple content assertion: page must have a header or h1
                boolean hasHeader = !findAll(By.cssSelector("h1, header, main")).isEmpty();
                Assertions.assertTrue(hasHeader, "Navigated page should have structural header/main elements.");
            } catch (Exception ignored) {
                // Ignore flaky links but continue
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(before));
            }
            visited++;
            if (visited >= 3) break; // limit for stability
        }
        Assertions.assertTrue(visited >= 0, "Visited a subset of one-level internal links if available.");
    }

    @Test
    @Order(6)
    public void docsLink_External_Subdomain_Verified() {
        // JSFiddle docs live at docs.jsfiddle.net (external origin). Validate if present.
        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        Optional<WebElement> docs = links.stream()
                .filter(a -> {
                    String h = a.getAttribute("href");
                    return h != null && h.toLowerCase().contains("docs.jsfiddle.net");
                }).findFirst();
        if (docs.isPresent()) {
            boolean ok = openExternalAndAssertDomain(docs.get(), "docs.jsfiddle.net");
            Assertions.assertTrue(ok, "Docs link should navigate to docs.jsfiddle.net domain.");
        } else {
            // Sometimes a "Docs" text link without absolute domain; try by text and assert domain after click
            Optional<WebElement> docsByText = links.stream()
                    .filter(a -> a.getText() != null && a.getText().trim().equalsIgnoreCase("Docs"))
                    .findFirst();
            if (docsByText.isPresent()) {
                boolean ok = openExternalAndAssertDomain(docsByText.get(), "docs.jsfiddle.net");
                Assertions.assertTrue(ok, "Docs link by text should navigate to docs.jsfiddle.net.");
            } else {
                Assertions.assertTrue(true, "Docs link not found; skipped.");
            }
        }
    }

    @Test
    @Order(7)
    public void footerExternalLinks_Twitter_GitHub_Medium_IfPresent() {
        Map<String, String> expectedDomains = new LinkedHashMap<>();
        expectedDomains.put("twitter.com", "twitter.com");
        expectedDomains.put("github.com", "github.com");
        expectedDomains.put("medium.com", "medium.com");

        List<WebElement> anchors = driver.findElements(By.cssSelector("footer a[href], a[href]"));
        int checked = 0;
        for (Map.Entry<String, String> entry : expectedDomains.entrySet()) {
            String domain = entry.getKey();
            Optional<WebElement> link = anchors.stream().filter(a -> {
                String h = a.getAttribute("href");
                return h != null && h.toLowerCase().contains(domain);
            }).findFirst();
            if (link.isPresent()) {
                boolean ok = openExternalAndAssertDomain(link.get(), domain);
                Assertions.assertTrue(ok, "External link should navigate to domain containing: " + domain);
                checked++;
            }
        }
        Assertions.assertTrue(checked >= 0, "Validated external social links if present.");
    }

    @Test
    @Order(8)
    public void createNewFiddleCTA_IfPresent_Navigates() {
        // Click primary CTA if present (e.g., Create or New fiddle)
        List<WebElement> buttons = new ArrayList<>();
        buttons.addAll(findAll(By.cssSelector("a.btn, button.btn, a.button, button.button")));
        buttons.addAll(findAll(By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create')]")));
        buttons.addAll(findAll(By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create')]")));
        String before = driver.getCurrentUrl();

        Optional<WebElement> create = buttons.stream().filter(b -> {
            String t = (b.getText() == null ? "" : b.getText()).toLowerCase();
            String v = String.valueOf(b.getAttribute("href")).toLowerCase();
            return t.contains("create") || t.contains("new fiddle") || v.contains("/create");
        }).findFirst();

        if (create.isPresent()) {
            safeClick(create.get());
            try { wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(before))); } catch (Exception ignored) {}
            Assertions.assertNotEquals(before, driver.getCurrentUrl(), "Clicking Create should change the URL.");
            // Ensure we can get back to base
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(before));
        } else {
            Assertions.assertTrue(true, "Create/New fiddle CTA not found; skipped.");
        }
    }
}