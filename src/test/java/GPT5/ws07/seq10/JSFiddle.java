package GPT5.ws07.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jsfiddle.net/";

    private static final By BODY = By.tagName("body");
    private static final By HEADER = By.tagName("header");
    private static final By FOOTER = By.tagName("footer");
    private static final By NAV_LINKS = By.cssSelector("nav a[href], header a[href]");
    private static final By FOOTER_LINKS = By.cssSelector("footer a[href]");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By ANY_SELECT = By.tagName("select");
    private static final By MENU_TOGGLER = By.cssSelector("button[aria-label*='menu' i], button.navbar-burger, button[aria-controls]");

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

    // ---------------- helpers ----------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Must land on BASE_URL");
    }

    private static String hostOf(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean present(By by) {
        return !driver.findElements(by).isEmpty();
    }

    private WebElement firstDisplayed(By by) {
        for (WebElement e : driver.findElements(by)) {
            if (e.isDisplayed()) return e;
        }
        throw new NoSuchElementException("No displayed element for: " + by);
    }

    private void scrollIntoView(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
        } catch (Exception ignored) {}
    }

    private void assertExternalLink(WebElement link) {
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Not an http(s) link");
        String expectedHost = hostOf(href);
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        scrollIntoView(link);
        try { link.click(); } catch (ElementClickInterceptedException e) { ((JavascriptExecutor)driver).executeScript("arguments[0].click()", link); }

        wait.until(d -> d.getWindowHandles().size() > before.size()
                || hostOf(d.getCurrentUrl()).equalsIgnoreCase(expectedHost));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External URL should contain expected host");
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External URL should contain expected host");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    // --------------- tests -------------------

    @Test
    @Order(1)
    void base_Should_Load_And_Render_Header_And_Footer() {
        openBase();
        Assertions.assertAll("Core sections",
                () -> Assertions.assertTrue(present(HEADER) || present(By.cssSelector("nav")), "Header/nav should exist"),
                () -> Assertions.assertTrue(present(FOOTER), "Footer should exist"),
                () -> Assertions.assertTrue(present(ANY_LINK), "Page should contain links"));
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jsfiddle") || title.toLowerCase().contains("fiddle"),
                "Title should mention JSFiddle/fiddle");
    }

    @Test
    @Order(2)
    void header_Navigation_Internal_Links_One_Level() {
        openBase();
        List<WebElement> links = driver.findElements(NAV_LINKS).stream()
                .filter(WebElement::isDisplayed).collect(Collectors.toList());
        Assumptions.assumeTrue(!links.isEmpty(), "No header links found");
        String baseHost = hostOf(BASE_URL);
        int tested = 0;
        for (WebElement a : links) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            String host = hostOf(href);
            if (href.startsWith("#")) {
                scrollIntoView(a);
                a.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
                Assertions.assertTrue(driver.getCurrentUrl().contains("#"), "Hash navigation should update URL");
                tested++;
            } else if (host.isEmpty() || host.equalsIgnoreCase(baseHost) || href.startsWith("/")) {
                String before = driver.getCurrentUrl();
                scrollIntoView(a);
                try { a.click(); } catch (Exception e) { driver.navigate().to(href.startsWith("/") ? BASE_URL + href.substring(1) : href); }
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
                String after = driver.getCurrentUrl();
                Assertions.assertTrue(!after.equals(before), "URL should change after internal navigation");
                // return to base for isolation
                driver.navigate().to(BASE_URL);
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
                tested++;
            }
            if (tested >= 3) break;
        }
        Assertions.assertTrue(tested > 0, "At least one internal header link should be validated");
    }

    @Test
    @Order(3)
    void external_Links_In_Footer_Should_Open_And_Have_Expected_Domain() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        List<WebElement> links = driver.findElements(FOOTER_LINKS).stream()
                .filter(WebElement::isDisplayed).collect(Collectors.toList());
        Assumptions.assumeTrue(!links.isEmpty(), "No footer links found");
        List<WebElement> external = links.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    return href != null && href.startsWith("http") && !hostOf(href).equalsIgnoreCase(baseHost);
                }).collect(Collectors.toList());
        Assumptions.assumeTrue(!external.isEmpty(), "No external footer links");
        int tested = 0;
        Set<String> domains = new HashSet<>();
        for (WebElement a : external) {
            String host = hostOf(a.getAttribute("href"));
            if (host.isEmpty() || domains.contains(host)) continue;
            domains.add(host);
            assertExternalLink(a);
            tested++;
            if (tested >= 3) break; // keep it tight
        }
        Assertions.assertTrue(tested > 0, "At least one external link should be tested");
    }

    @Test
    @Order(4)
    void explore_Page_If_Select_Present_Change_Selection() {
        // Navigate to an "explore" or similar page where dropdowns are more likely
        driver.navigate().to(BASE_URL + "explore");
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        if (!present(ANY_SELECT)) {
            // try another likely page
            driver.navigate().to(BASE_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
        Assumptions.assumeTrue(present(ANY_SELECT), "No <select> found to test sorting/filtering");
        WebElement selectEl = firstDisplayed(ANY_SELECT);
        Select sel = new Select(selectEl);
        List<WebElement> opts = sel.getOptions();
        Assumptions.assumeTrue(opts.size() > 1, "Not enough options in select to change");
        String before = sel.getFirstSelectedOption().getText();
        sel.selectByIndex(1);
        String after = sel.getFirstSelectedOption().getText();
        Assertions.assertNotEquals(before, after, "Selection should change after choosing another option");
        // Optional: verify list changed, if any list present
        List<WebElement> items = driver.findElements(By.cssSelector("article, .list-item, .item, .search-result"));
        if (items.size() >= 2) {
            String headBefore = items.get(0).getText();
            sel.selectByIndex(0);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            List<WebElement> itemsAfter = driver.findElements(By.cssSelector("article, .list-item, .item, .search-result"));
            if (!itemsAfter.isEmpty()) {
                String headAfter = itemsAfter.get(0).getText();
                Assertions.assertNotEquals(headBefore, headAfter, "Top item should change after different selection");
            }
        }
        driver.navigate().to(BASE_URL); // restore
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
    }

    @Test
    @Order(5)
    void menu_Burger_Toggler_If_Present_Should_Open_Close() {
        openBase();
        if (!present(MENU_TOGGLER)) {
            Assumptions.assumeTrue(false, "Menu toggler not present on current viewport");
        }
        WebElement toggler = firstDisplayed(MENU_TOGGLER);
        scrollIntoView(toggler);
        String navStateBefore = driver.findElement(BODY).getAttribute("class");
        toggler.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        String navStateAfter = driver.findElement(BODY).getAttribute("class");
        Assertions.assertNotEquals(navStateBefore, navStateAfter, "Body class (or DOM) should change when menu opens");

        // try clicking a visible menu link if any
        List<WebElement> menuLinks = driver.findElements(By.cssSelector("nav a[href]")).stream()
                .filter(WebElement::isDisplayed).collect(Collectors.toList());
        if (!menuLinks.isEmpty()) {
            WebElement a = menuLinks.get(0);
            String beforeUrl = driver.getCurrentUrl();
            scrollIntoView(a);
            a.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            String afterUrl = driver.getCurrentUrl();
            Assertions.assertTrue(!beforeUrl.equals(afterUrl) || afterUrl.contains("#"),
                    "URL should change or hash should be appended after clicking a menu link");
        }
        // close if toggler still visible
        if (present(MENU_TOGGLER)) {
            toggler = firstDisplayed(MENU_TOGGLER);
            scrollIntoView(toggler);
            toggler.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    @Test
    @Order(6)
    void create_New_Fiddle_Entry_Point_If_Available() {
        openBase();
        // Try to find a "Create" or "New" entry point
        List<By> candidates = Arrays.asList(
                By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create')]"),
                By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'new')]"),
                By.cssSelector("a[href='/'], a[href='/#'], a[href*='new']")
        );
        WebElement entry = null;
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
            if (!els.isEmpty()) { entry = els.get(0); break; }
        }
        Assumptions.assumeTrue(entry != null, "No obvious 'Create/New' entry point found");
        String before = driver.getCurrentUrl();
        scrollIntoView(entry);
        try { entry.click(); } catch (Exception e) { ((JavascriptExecutor)driver).executeScript("arguments[0].click()", entry); }
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        String after = driver.getCurrentUrl();
        Assertions.assertTrue(!after.equals(before) || after.contains("#"),
                "Navigating to create/new should change URL or hash");
        // Return to base
        driver.navigate().to(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
    }

    @Test
    @Order(7)
    void internal_Links_One_Level_From_Homepage() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        List<String> internal = driver.findElements(ANY_LINK).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(href -> {
                    String host = hostOf(href);
                    return href.startsWith("#") || href.startsWith("/")
                            || host.isEmpty() || host.equalsIgnoreCase(baseHost)
                            || href.startsWith(BASE_URL);
                })
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!internal.isEmpty(), "No internal links collected");
        int visited = 0;
        for (String href : internal) {
            String target = href;
            if (href.startsWith("/")) target = BASE_URL + href.substring(1);
            driver.navigate().to(target);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            String after = driver.getCurrentUrl();
            Assertions.assertTrue(!after.isEmpty(), "After navigation URL should not be empty");
            Assertions.assertTrue(after.contains("jsfiddle.net"), "Must remain within jsfiddle.net");
            visited++;
            // back to base
            driver.navigate().to(BASE_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
        Assertions.assertTrue(visited > 0, "Should visit at least one internal link");
    }

    @Test
    @Order(8)
    void generic_Select_If_Present_On_Home_Should_Change() {
        openBase();
        if (!present(ANY_SELECT)) {
            Assumptions.assumeTrue(false, "No select on home page; skipping");
        }
        WebElement selectEl = firstDisplayed(ANY_SELECT);
        Select sel = new Select(selectEl);
        List<WebElement> options = sel.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Select does not have multiple options");
        String before = sel.getFirstSelectedOption().getText();
        sel.selectByIndex(1);
        String after = sel.getFirstSelectedOption().getText();
        Assertions.assertNotEquals(before, after, "Select option should change");
    }
}
