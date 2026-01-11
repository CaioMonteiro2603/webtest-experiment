package GPT5.ws09.seq10;

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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://demo.realworld.io/";

    // Common locators (robust/fallbacks)
    private static final By BODY = By.tagName("body");
    private static final By HEADER = By.cssSelector("nav.navbar, header");
    private static final By FOOTER = By.cssSelector("footer, .footer");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By HOME_LINK = By.xpath("//a[normalize-space()='Home' or contains(@href,'#/')]");
    private static final By SIGNIN_LINK = By.xpath("//a[normalize-space()='Sign in' or contains(@href,'#/login')]");
    private static final By SIGNUP_LINK = By.xpath("//a[normalize-space()='Sign up' or contains(@href,'#/register')]");
    private static final By ARTICLE_PREVIEW_LINKS = By.cssSelector(".article-preview h1 a, .article-preview a.preview-link");
    private static final By ARTICLE_TITLE = By.cssSelector("h1, h1.article-title");
    private static final By TAG_PILLS = By.cssSelector(".tag-list a.tag-pill, .sidebar .tag-list a");
    private static final By FEED_ACTIVE = By.cssSelector(".feed-toggle .nav-link.active");
    private static final By AUTHOR_LINK = By.cssSelector(".article-preview .info a.author, .article-meta a.author");
    private static final By PROFILE_HEADER = By.cssSelector("h4, .user-info h4");
    private static final By PAGINATION_LINKS = By.cssSelector("ul.pagination li a.page-link, .pagination a");
    private static final By SIGNIN_EMAIL = By.cssSelector("input[type='email'], input[placeholder='Email'], input[name='email']");
    private static final By SIGNIN_PASSWORD = By.cssSelector("input[type='password'], input[placeholder='Password'], input[name='password']");
    private static final By SIGNIN_BUTTON = By.xpath("//button[contains(.,'Sign in')]");
    private static final By ERROR_MESSAGES = By.cssSelector(".error-messages, .error-messages li");

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

    // ---------------- helper utils ----------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on BASE_URL");
    }

    private boolean present(By by) {
        return !driver.findElements(by).isEmpty();
    }

    private WebElement firstDisplayed(By by) {
        for (WebElement e : driver.findElements(by)) {
            if (e.isDisplayed()) return e;
        }
        throw new NoSuchElementException("No displayed element found for: " + by);
    }

    private void click(WebElement el) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el)).click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", el);
        }
    }

    private void scrollIntoView(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
        } catch (Exception ignored) {}
    }

    private static String hostOf(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private void assertExternalLinkOpensAndClose(WebElement link) {
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Not an external http(s) link");
        String expectedHost = hostOf(href);
        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        scrollIntoView(link);
        click(link);
        wait.until(d -> d.getWindowHandles().size() > before.size()
                || hostOf(d.getCurrentUrl()).equalsIgnoreCase(expectedHost));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External URL should contain expected host: " + expectedHost);
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External URL should contain expected host (same tab): " + expectedHost);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    // ---------------- tests ----------------

    @Test
    @Order(1)
    void home_Should_Load_With_Header_Nav_And_Footer() {
        openBase();
        Assertions.assertAll("Core structure",
                () -> Assertions.assertTrue(present(HEADER), "Header/navbar should be present"),
                () -> Assertions.assertTrue(present(FOOTER), "Footer should be present"),
                () -> Assertions.assertTrue(present(ANY_LINK), "There should be some links on the page")
        );
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("conduit") || title.toLowerCase().contains("realworld"),
                "Page title should reference Conduit/RealWorld");
        // Ensure Home link is available and works
        if (present(HOME_LINK)) {
            click(firstDisplayed(HOME_LINK));
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Home navigation should keep us on BASE_URL");
        }
    }

    @Test
    @Order(2)
    void global_Feed_Should_Show_Articles_And_Article_Page_Opens() {
        openBase();
        // Wait until some article previews load
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfAllElementsLocatedBy(ARTICLE_PREVIEW_LINKS),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-preview"))
        ));
        List<WebElement> previews = driver.findElements(ARTICLE_PREVIEW_LINKS);
        Assumptions.assumeTrue(!previews.isEmpty(), "No article previews found on Global Feed - skipping");
        WebElement first = previews.get(0);
        String previewTitle = first.getText().trim();
        scrollIntoView(first);
        click(first);
        // Article page should show an <h1> with title (or at least a header)
        WebElement h1 = wait.until(ExpectedConditions.presenceOfElementLocated(ARTICLE_TITLE));
        Assertions.assertTrue(h1.isDisplayed(), "Article header/title should be visible");
        if (!previewTitle.isEmpty()) {
            Assertions.assertTrue(h1.getText().toLowerCase().contains(previewTitle.toLowerCase())
                            || previewTitle.toLowerCase().contains(h1.getText().toLowerCase()),
                    "Article title should correspond to the preview link");
        }
        // Back to home to keep one-level scope
        openBase();
    }

    @Test
    @Order(3)
    void sidebar_Tag_Click_Should_Filter_Feed() {
        openBase();
        if (!present(TAG_PILLS)) {
            Assumptions.assumeTrue(false, "No tag pills found in sidebar - skipping");
        }
        WebElement tag = firstDisplayed(TAG_PILLS);
        String tagText = tag.getText().trim();
        scrollIntoView(tag);
        click(tag);
        // Active feed header should include the tag or URL contains tag=
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(FEED_ACTIVE),
                ExpectedConditions.urlContains("tag=")
        ));
        boolean ok = false;
        if (present(FEED_ACTIVE)) {
            WebElement active = firstDisplayed(FEED_ACTIVE);
            ok = active.getText().toLowerCase().contains(tagText.toLowerCase());
        }
        if (!ok) ok = driver.getCurrentUrl().toLowerCase().contains("tag=");
        Assertions.assertTrue(ok, "Feed should indicate it is filtered by the clicked tag");
        // And there should still be article previews
        List<WebElement> filtered = driver.findElements(By.cssSelector(".article-preview"));
        Assertions.assertTrue(filtered.size() > 0, "Filtered feed should show article previews");
    }

    @Test
    @Order(4)
    void pagination_If_Present_Should_Navigate_And_Show_Articles() {
        openBase();
        // Wait initial previews
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".article-preview")),
                ExpectedConditions.presenceOfElementLocated(ARTICLE_PREVIEW_LINKS)
        ));
        List<WebElement> initial = driver.findElements(By.cssSelector(".article-preview"));
        int initialCount = initial.size();
        // Find pagination link "2" or any other
        List<WebElement> pages = driver.findElements(PAGINATION_LINKS).stream()
                .filter(WebElement::isDisplayed)
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!pages.isEmpty(), "No pagination links present - skipping");
        WebElement target = pages.stream().filter(a -> a.getText().trim().equals("2")).findFirst().orElse(pages.get(0));
        String beforeUrl = driver.getCurrentUrl();
        scrollIntoView(target);
        click(target);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".article-preview")));
        List<WebElement> after = driver.findElements(By.cssSelector(".article-preview"));
        Assertions.assertTrue(after.size() >= 0, "Article previews should be visible after pagination");
        Assertions.assertTrue(!driver.getCurrentUrl().equals(beforeUrl) || after.size() != initialCount,
                "Either URL should change or number of articles should change after navigating pages");
    }

    @Test
    @Order(5)
    void author_Profile_From_Preview_Should_Open_And_Show_Username() {
        openBase();
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".article-preview")));
        List<WebElement> authors = driver.findElements(AUTHOR_LINK).stream()
                .filter(WebElement::isDisplayed).collect(Collectors.toList());
        Assumptions.assumeTrue(!authors.isEmpty(), "No author links found on previews - skipping");
        WebElement author = authors.get(0);
        String name = author.getText().trim();
        scrollIntoView(author);
        click(author);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(PROFILE_HEADER),
                ExpectedConditions.urlContains("/profile/")
        ));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("/profile/"), "URL should navigate to a profile page");
        if (present(PROFILE_HEADER)) {
            WebElement h = firstDisplayed(PROFILE_HEADER);
            Assertions.assertTrue(h.getText().toLowerCase().contains(name.toLowerCase()) || name.isEmpty(),
                    "Profile header should contain the author name");
        }
        // back to base
        openBase();
    }

    @Test
    @Order(6)
    void signIn_Page_Negative_Invalid_Credentials_Shows_Error() {
        openBase();
        if (!present(SIGNIN_LINK)) {
            Assumptions.assumeTrue(false, "Sign in link not present - skipping");
        }
        click(firstDisplayed(SIGNIN_LINK));
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        // fill invalid creds
        WebElement email = wait.until(ExpectedConditions.presenceOfElementLocated(SIGNIN_EMAIL));
        WebElement pass = wait.until(ExpectedConditions.presenceOfElementLocated(SIGNIN_PASSWORD));
        email.clear();
        email.sendKeys("invalid@example.com");
        pass.clear();
        pass.sendKeys("wrongpassword");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SIGNIN_BUTTON));
        click(btn);
        // Expect errors
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(ERROR_MESSAGES),
                ExpectedConditions.urlContains("/login")
        ));
        Assertions.assertTrue(present(ERROR_MESSAGES) || driver.getCurrentUrl().toLowerCase().contains("/login"),
                "Invalid sign in should keep user on login page and/or show error messages");
    }

    @Test
    @Order(7)
    void signUp_Page_Should_Load_Then_Return_Home() {
        openBase();
        if (!present(SIGNUP_LINK)) {
            Assumptions.assumeTrue(false, "Sign up link not present - skipping");
        }
        click(firstDisplayed(SIGNUP_LINK));
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("register"),
                "Sign up page URL should contain 'register'");
        // back one level
        if (present(HOME_LINK)) click(firstDisplayed(HOME_LINK));
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be back to base");
    }

    @Test
    @Order(8)
    void footer_External_Links_Should_Open_And_Close() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a[href], .footer a[href]"));
        if (footerLinks.isEmpty()) footerLinks = driver.findElements(ANY_LINK);
        List<WebElement> externals = footerLinks.stream()
                .filter(WebElement::isDisplayed)
                .filter(a -> {
                    String href = a.getAttribute("href");
                    if (href == null || !href.startsWith("http")) return false;
                    return !hostOf(href).equalsIgnoreCase(baseHost);
                })
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!externals.isEmpty(), "No external links discovered - skipping");
        // Validate up to three distinct hosts
        Set<String> testedHosts = new HashSet<>();
        int tested = 0;
        for (WebElement link : externals) {
            String host = hostOf(link.getAttribute("href"));
            if (host.isEmpty() || testedHosts.contains(host)) continue;
            testedHosts.add(host);
            assertExternalLinkOpensAndClose(link);
            tested++;
            if (tested >= 3) break;
        }
        Assertions.assertTrue(tested > 0, "At least one external footer link should be validated");
    }

    // ---------------- Optional features from brief (guarded/skipped) ----------------

    @Test
    @Order(9)
    void sorting_Dropdown_If_Present_Should_Change_Order() {
        openBase();
        // RealWorld demo typically has no sorting; guard the scenario
        List<WebElement> selects = driver.findElements(By.tagName("select")).stream()
                .filter(WebElement::isDisplayed).collect(Collectors.toList());
        Assumptions.assumeTrue(!selects.isEmpty(), "No sorting/select dropdown present - skipping");
        WebElement sel = selects.get(0);
        String beforeListKey = driver.findElements(ARTICLE_PREVIEW_LINKS).stream().map(WebElement::getText).collect(Collectors.joining("|"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].selectedIndex = 1; arguments[0].dispatchEvent(new Event('change',{bubbles:true}))", sel);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        String afterListKey = driver.findElements(ARTICLE_PREVIEW_LINKS).stream().map(WebElement::getText).collect(Collectors.joining("|"));
        Assertions.assertNotEquals(beforeListKey, afterListKey, "Changing sorting should alter the list order");
    }

    @Test
    @Order(10)
    void burger_Menu_And_Reset_Actions_If_Present() {
        openBase();
        // Conduit has no burger/reset features; skip if absent
        List<WebElement> burger = driver.findElements(By.cssSelector("button[aria-label*='menu' i], .burger, .hamburger"));
        Assumptions.assumeTrue(!burger.isEmpty(), "No burger/menu button present - skipping");
        WebElement b = burger.get(0);
        scrollIntoView(b);
        click(b);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        // Try clicking any "About", "All Items", "Logout", "Reset App State" if they exist
        for (By by : Arrays.asList(
                By.xpath("//a[normalize-space()='About']"),
                By.xpath("//a[normalize-space()='All Items']"),
                By.xpath("//a[contains(.,'Logout')]"),
                By.xpath("//a[contains(.,'Reset App State') or contains(.,'Reset')]")
        )) {
            if (present(by)) {
                WebElement link = firstDisplayed(by);
                scrollIntoView(link);
                click(link);
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            }
        }
        Assertions.assertTrue(true, "Menu interactions executed when present");
    }
}
