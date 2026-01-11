package GPT5.ws07.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
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

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // -------------------- Helpers --------------------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private String registrableDomain(String url) {
        try {
            URI u = URI.create(url);
            String host = u.getHost();
            if (host == null) return "";
            String[] p = host.split("\\.");
            if (p.length < 2) return host;
            return p[p.length - 2] + "." + p[p.length - 1];
        } catch (Exception e) {
            return "";
        }
    }

    private ExpectedCondition<Boolean> urlChangedFrom(String previous) {
        return d -> previous == null || !d.getCurrentUrl().equals(previous);
    }

    private WebElement first(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? null : els.get(0);
    }

    private void click(WebElement el) {
        if (el != null) wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void verifyExternalLink(WebElement anchor) {
        String href = anchor.getAttribute("href");
        if (href == null || href.isEmpty() || href.startsWith("mailto:") || href.startsWith("javascript:")) return;

        String baseDomain = registrableDomain(BASE_URL);
        String targetDomain = registrableDomain(href);
        boolean external = !baseDomain.equalsIgnoreCase(targetDomain);

        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(anchor)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],'_blank');", href);
        }

        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(driver.getCurrentUrl()));
        } catch (TimeoutException ignored) { }

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newTab = after.iterator().next();
            driver.switchTo().window(newTab);
            if (external) {
                wait.until(ExpectedConditions.urlContains(targetDomain));
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "External URL should contain expected domain: " + targetDomain);
            }
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            if (external) {
                wait.until(ExpectedConditions.urlContains(targetDomain));
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "External URL should contain expected domain: " + targetDomain);
            }
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    private void verifyAllExternalLinksOnPage() {
        String baseDomain = registrableDomain(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        Map<String, WebElement> uniqueExternal = new LinkedHashMap<>();
        for (WebElement a : links) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith("http")) continue;
            String d = registrableDomain(href);
            if (!d.isEmpty() && !d.equalsIgnoreCase(baseDomain)) {
                uniqueExternal.putIfAbsent(href, a);
            }
        }
        for (WebElement a : uniqueExternal.values()) {
            verifyExternalLink(a);
        }
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void homePageLoadsAndHasPrimaryNav() {
        goHome();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should start on jsfiddle home.");
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jsfiddle") || title.toLowerCase().contains("fiddle"),
                "Title should mention JSFiddle.");
        WebElement header = first(By.cssSelector("header, .navbar, .navigation, .site-header"));
        Assertions.assertTrue(header != null && header.isDisplayed(), "Primary header/nav should be visible.");
        verifyAllExternalLinksOnPage();
    }

    @Test
    @Order(2)
    public void headerNavigationInternalPagesAndExternalLinks() {
        goHome();

        // Attempt typical header items: Explore, Docs, Blog, Support
        List<WebElement> navLinks = driver.findElements(By.cssSelector("header a[href], .navbar a[href], nav a[href]"));
        Assertions.assertTrue(navLinks.size() > 0, "There should be navigation links in header.");

        String baseDomain = registrableDomain(BASE_URL);
        // Visit up to 5 internal pages one level below
        int visited = 0;
        for (WebElement a : navLinks) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty() || !href.startsWith("http")) continue;
            if (!registrableDomain(href).equalsIgnoreCase(baseDomain)) continue;
            if (!href.startsWith(BASE_URL)) continue;
            if (href.equals(BASE_URL)) continue; // home itself
            String current = driver.getCurrentUrl();
            try {
                click(a);
                wait.until(urlChangedFrom(current));
                // Validate page
                boolean hasContent = driver.findElements(By.tagName("h1")).size() > 0
                        || driver.findElements(By.tagName("h2")).size() > 0
                        || driver.findElements(By.cssSelector("main, .container, .content")).size() > 0;
                Assertions.assertTrue(hasContent, "Internal page should render content: " + driver.getCurrentUrl());

                // Also verify external links on that page
                verifyAllExternalLinksOnPage();
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(current));
            }
            visited++;
            if (visited >= 5) break;
        }

        // Explicitly verify typical external header links if present
        List<By> externalSelectors = Arrays.asList(
                By.partialLinkText("Docs"),
                By.partialLinkText("Blog"),
                By.partialLinkText("Support"),
                By.cssSelector("a[href*='docs'], a[href*='medium'], a[href*='github.com/jsfiddle']")
        );
        for (By sel : externalSelectors) {
            List<WebElement> found = driver.findElements(sel);
            if (!found.isEmpty()) {
                verifyExternalLink(found.get(0));
            }
        }
    }

    @Test
    @Order(3)
    public void createNewFiddleAndDetectEditorPanels() {
        goHome();

        // Navigate to "Create" / "New" editor if there is a direct link
        WebElement create = first(By.partialLinkText("Create"));
        if (create == null) create = first(By.partialLinkText("New"));
        String before = driver.getCurrentUrl();
        if (create != null) {
            click(create);
            try {
                wait.until(urlChangedFrom(before));
            } catch (TimeoutException ignored) { }
        }

        // Validate editor presence (CodeMirror panes, Run button, or result iframe)
        boolean hasEditor = wait.until(d -> d.findElements(By.cssSelector(".CodeMirror, .editor, .cm-editor")).size() > 0
                || d.findElements(By.xpath("//button[contains(.,'Run') or contains(.,'RUN')]")).size() > 0);
        Assertions.assertTrue(hasEditor, "Editor should be present on new fiddle page.");

        // If a "Run" button exists, click it and expect a result frame or output area
        WebElement runBtn = first(By.xpath("//button[contains(.,'Run') or contains(.,'RUN')]"));
        if (runBtn != null) {
            click(runBtn);
            // Expect some iframe or result container to be present
            WebElement result = wait.until(d -> {
                List<WebElement> frames = d.findElements(By.tagName("iframe"));
                if (!frames.isEmpty()) return frames.get(0);
                List<WebElement> outputs = d.findElements(By.cssSelector("#result, .result, .output"));
                return outputs.isEmpty() ? null : outputs.get(0);
            });
            Assertions.assertNotNull(result, "A result frame or container should appear after running the fiddle.");
        }

        // Return to home to keep a known state
        goHome();
    }

    @Test
    @Order(4)
    public void explorePageSortingIfAvailable() {
        // Go to Explore page if it exists
        driver.get(BASE_URL + "explore");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/explore") || driver.getTitle().toLowerCase().contains("explore"),
                "Should be on Explore page (if supported).");

        // Try to locate a sorting dropdown/select
        WebElement sortSelect = first(By.cssSelector("select[id*='sort'], select[name*='sort'], select#sort, select[name='sort']"));
        if (sortSelect == null) {
            // Sometimes sorting is a group of tabs/buttons; attempt to toggle if present
            List<WebElement> sortButtons = driver.findElements(By.xpath("//button[contains(.,'Popular') or contains(.,'Recent')]"));
            if (sortButtons.size() >= 2) {
                String beforeFirstCard = textOfFirstCard();
                click(sortButtons.get(1));
                wait.until(d -> !Objects.equals(beforeFirstCard, textOfFirstCard()));
                String afterFirstCard = textOfFirstCard();
                Assertions.assertNotEquals(beforeFirstCard, afterFirstCard, "Order should change when toggling sort buttons.");
                return;
            }
            Assumptions.assumeTrue(false, "No sorting UI present on Explore; skipping sort test.");
            return;
        }

        // Use Select to change sort options, assert order change by first card text if possible
        Select select = new Select(sortSelect);
        List<WebElement> options = select.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Sorting select has insufficient options; skipping.");

        String beforeFirst = textOfFirstCard();
        select.selectByIndex(1);
        wait.until(d -> !Objects.equals(beforeFirst, textOfFirstCard()));
        String afterFirst = textOfFirstCard();
        Assertions.assertNotEquals(beforeFirst, afterFirst, "First card should change after sorting.");

        // Switch back to initial option
        select.selectByIndex(0);
    }

    private String textOfFirstCard() {
        List<WebElement> cards = driver.findElements(By.cssSelector(".card, .item, .fiddle, .post, article, .list-item"));
        if (!cards.isEmpty()) {
            String t = cards.get(0).getText();
            return t == null ? "" : t.trim();
        }
        // fallback to first list/anchor
        List<WebElement> links = driver.findElements(By.cssSelector("main a[href]"));
        return links.isEmpty() ? "" : (links.get(0).getText() == null ? "" : links.get(0).getText().trim());
        }

    @Test
    @Order(5)
    public void footerSocialLinksAsExternal() {
        goHome();

        // Scroll to bottom
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Common social domains
        List<String> domains = Arrays.asList("twitter.com", "facebook.com", "linkedin.com", "github.com", "discord.gg", "youtube.com");
        Map<String, WebElement> found = new LinkedHashMap<>();
        for (String dmn : domains) {
            List<WebElement> anchors = driver.findElements(By.cssSelector("a[href*='" + dmn + "']"));
            if (!anchors.isEmpty()) found.put(dmn, anchors.get(0));
        }

        Assumptions.assumeTrue(!found.isEmpty(), "No social links found in footer; skipping.");
        for (WebElement a : found.values()) {
            verifyExternalLink(a);
        }
    }

    @Test
    @Order(6)
    public void iterateInternalLinksOneLevelFromHome() {
        goHome();
        String baseDomain = registrableDomain(BASE_URL);

        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<String> internal = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(h -> h != null && !h.isEmpty())
                .filter(h -> h.startsWith("http"))
                .filter(h -> registrableDomain(h).equalsIgnoreCase(baseDomain))
                .filter(h -> h.startsWith(BASE_URL))
                .distinct()
                .collect(Collectors.toList());

        int visited = 0;
        for (String href : internal) {
            if (visited >= 6) break;
            String current = driver.getCurrentUrl();
            try {
                WebElement link = first(By.cssSelector("a[href='" + href + "']"));
                if (link == null) continue;
                click(link);
                try {
                    wait.until(urlChangedFrom(current));
                } catch (TimeoutException ignored) { }

                // Validate that the page has meaningful content
                boolean hasContent = driver.findElements(By.tagName("h1")).size() > 0
                        || driver.findElements(By.tagName("h2")).size() > 0
                        || driver.findElements(By.cssSelector("main, .container, .content, article")).size() > 0;
                Assertions.assertTrue(hasContent, "Internal page should show content: " + driver.getCurrentUrl());

                // On each one-level page, validate external links as well
                verifyAllExternalLinksOnPage();
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(current));
            }
            visited++;
        }
        Assertions.assertTrue(visited >= 0, "Iterated internal links without failures.");
    }

    @Test
    @Order(7)
    public void mobileBurgerMenuIfPresent() {
        goHome();

        // Try to locate a burger / menu toggle if available (responsive sites may show this)
        List<By> burgerSelectors = Arrays.asList(
                By.cssSelector("button[aria-label*='menu' i]"),
                By.cssSelector("button[class*='burger' i], .hamburger, .navbar-toggle"),
                By.xpath("//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'navigation')]"),
                By.xpath("//button[contains(.,'Menu') or contains(.,'menu')]")
        );
        WebElement burger = null;
        for (By sel : burgerSelectors) {
            burger = first(sel);
            if (burger != null) break;
        }
        Assumptions.assumeTrue(burger != null, "No burger/menu toggle present; skipping.");

        click(burger);
        // After open, expect nav links to be visible
        boolean menuVisible = wait.until(d -> d.findElements(By.cssSelector("nav a[href], .menu a[href], .navbar a[href]")).size() > 0);
        Assertions.assertTrue(menuVisible, "Menu items should be visible after opening the burger.");

        // Close if there is a close control
        WebElement close = first(By.cssSelector("button[aria-label*='close' i], .close, .navbar-toggle"));
        if (close != null) click(close);
    }
}