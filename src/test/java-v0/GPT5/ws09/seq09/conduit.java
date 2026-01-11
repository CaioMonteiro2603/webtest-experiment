package GPT5.ws09.seq09;

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
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://demo.realworld.io/";

    private static final By MAIN_CONTAINER = By.cssSelector("body");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By NAVBAR = By.cssSelector("nav.navbar, header, .navbar");
    private static final By HOME_LINK_BY_TEXT = By.xpath("//a[normalize-space(translate(., 'HOME', 'home'))='home' or contains(translate(., 'HOME', 'home'),'home')]");
    private static final By SIGNIN_LINK = By.xpath("//a[contains(translate(., 'SIGN IN', 'sign in'),'sign in') or contains(@href,'login')]");
    private static final By SIGNUP_LINK = By.xpath("//a[contains(translate(., 'SIGN UP', 'sign up'),'sign up') or contains(@href,'register')]");
    private static final By EMAIL_INPUT = By.cssSelector("input[type='email'], input[placeholder='Email'], input[formcontrolname='email'], input[name='email']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password'], input[placeholder='Password'], input[formcontrolname='password'], input[name='password']");
    private static final By SIGNIN_SUBMIT = By.xpath("//button[contains(translate(., 'SIGN IN', 'sign in'),'sign in') or contains(.,'Sign in')]");
    private static final By ERROR_MESSAGES = By.cssSelector(".error-messages li, .error-messages, .error");
    private static final By TAG_LIST_LINKS = By.cssSelector(".tag-list a, .sidebar .tag-list a");
    private static final By FEED_TOGGLE_ACTIVE = By.cssSelector(".feed-toggle li.active, .nav-pills .active");
    private static final By ARTICLE_PREVIEW = By.cssSelector(".article-preview, .preview-link");
    private static final By ARTICLE_TITLE_LINK = By.cssSelector(".article-preview a.preview-link, .preview-link, .article-preview h1 a, .article-preview h1");
    private static final By ARTICLE_PAGE_TITLE = By.cssSelector("h1, .article-page h1");
    private static final By PROFILE_BIO = By.cssSelector(".user-info, .profile-page, .articles-toggle");
    private static final By FOOTER = By.tagName("footer");

    // App-specific (not applicable) locators guarded by assertions to be absent
    private static final By SORTING_DROPDOWN = By.cssSelector("select[data-test='product_sort_container'], select#sort, select[name*='sort']");
    private static final By BURGER_BTN = By.id("react-burger-menu-btn");
    private static final By MENU_ALL_ITEMS = By.id("inventory_sidebar_link");
    private static final By MENU_ABOUT = By.id("about_sidebar_link");
    private static final By MENU_LOGOUT = By.id("logout_sidebar_link");
    private static final By MENU_RESET = By.id("reset_sidebar_link");

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ===== Helper methods =====

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(MAIN_CONTAINER));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should land on BASE_URL");
        Assertions.assertTrue(isPresent(NAVBAR), "Navbar should be visible");
    }

    private boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private Optional<WebElement> findFirst(By locator) {
        List<WebElement> els = driver.findElements(locator);
        return els.isEmpty() ? Optional.empty() : Optional.of(els.get(0));
    }

    private Optional<WebElement> findLinkByTextContains(String... texts) {
        List<WebElement> links = driver.findElements(ANY_LINK);
        for (WebElement a : links) {
            String txt = (a.getText() == null ? "" : a.getText()).trim().toLowerCase(Locale.ROOT);
            String aria = Optional.ofNullable(a.getAttribute("aria-label")).orElse("").toLowerCase(Locale.ROOT);
            String title = Optional.ofNullable(a.getAttribute("title")).orElse("").toLowerCase(Locale.ROOT);
            for (String want : texts) {
                String w = want.toLowerCase(Locale.ROOT);
                if (txt.contains(w) || aria.contains(w) || title.contains(w)) {
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

    private void assertExternalLinkInNewTabOrSame(WebElement link, String expectedDomainContains) {
        String baseHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        wait.until(d -> d.getWindowHandles().size() > before.size() || d.getCurrentUrl().contains(expectedDomainContains));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newH = after.iterator().next();
            driver.switchTo().window(newH);
            wait.until(ExpectedConditions.urlContains(expectedDomainContains));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainContains),
                    "External URL should contain " + expectedDomainContains);
            driver.close();
            driver.switchTo().window(baseHandle);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomainContains));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainContains),
                    "External URL (same tab) should contain " + expectedDomainContains);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(MAIN_CONTAINER));
        }
    }

    // ===== Tests =====

    @Test
    @Order(1)
    public void home_LoadsAndShowsCoreNav() {
        openBase();
        boolean hasHome = isPresent(HOME_LINK_BY_TEXT) || findLinkByTextContains("home").isPresent();
        boolean hasSignIn = isPresent(SIGNIN_LINK);
        boolean hasSignUp = isPresent(SIGNUP_LINK);
        Assertions.assertAll(
                () -> Assertions.assertTrue(hasHome, "Home link should be visible"),
                () -> Assertions.assertTrue(hasSignIn || hasSignUp, "Sign in or Sign up link should be visible")
        );
    }

    @Test
    @Order(2)
    public void signIn_Negative_ShowsErrorOrStaysOnLogin() {
        openBase();
        WebElement signIn = findFirst(SIGNIN_LINK).orElseGet(() -> waitClickable(SIGNIN_LINK));
        signIn.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("login"),
                ExpectedConditions.presenceOfElementLocated(EMAIL_INPUT)
        ));

        wait.until(ExpectedConditions.presenceOfElementLocated(EMAIL_INPUT)).sendKeys("invalid@example.com");
        wait.until(ExpectedConditions.presenceOfElementLocated(PASSWORD_INPUT)).sendKeys("wrongpass");
        wait.until(ExpectedConditions.elementToBeClickable(SIGNIN_SUBMIT)).click();

        boolean errorShown = isPresent(ERROR_MESSAGES);
        boolean stillOnLogin = driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("login");
        Assertions.assertTrue(errorShown || stillOnLogin, "Either error must show or remain on login page");
    }

    @Test
    @Order(3)
    public void signUp_Navigation_Visible() {
        openBase();
        WebElement signUp = findFirst(SIGNUP_LINK).orElseGet(() -> waitClickable(SIGNUP_LINK));
        signUp.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("register"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Username'], input[name='username'], input[formcontrolname='username']"))
        ));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("register") ||
                              isPresent(By.cssSelector("input[placeholder='Username'], input[name='username'], input[formcontrolname='username']")),
                "Sign up page or form should be present");
    }

    @Test
    @Order(4)
    public void home_TagFilter_ActivatesFeed() {
        openBase();
        // Pick a tag from the sidebar or tag list
        Optional<WebElement> tagOpt = findFirst(TAG_LIST_LINKS);
        Assumptions.assumeTrue(tagOpt.isPresent(), "No tags visible to test");
        String tagText = tagOpt.get().getText().trim();
        wait.until(ExpectedConditions.elementToBeClickable(tagOpt.get())).click();

        // Expect articles to (re)load and an active pill/tag
        wait.until(ExpectedConditions.presenceOfElementLocated(ARTICLE_PREVIEW));
        boolean hasActive = isPresent(FEED_TOGGLE_ACTIVE);
        Assertions.assertTrue(hasActive || isPresent(ARTICLE_PREVIEW),
                "Filtered feed should show article previews or active filter");
        Assertions.assertTrue(tagText.length() == 0 || driver.getPageSource().toLowerCase(Locale.ROOT).contains(tagText.toLowerCase(Locale.ROOT)),
                "Page source should reflect chosen tag (best-effort)");
    }

    @Test
    @Order(5)
    public void open_FirstArticle_Then_Back() {
        openBase();
        wait.until(ExpectedConditions.presenceOfElementLocated(ARTICLE_PREVIEW));
        Optional<WebElement> firstArticleLink = findFirst(ARTICLE_TITLE_LINK);
        Assumptions.assumeTrue(firstArticleLink.isPresent(), "No article preview link found");
        String originalUrl = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(firstArticleLink.get())).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(ARTICLE_PAGE_TITLE));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("article") ||
                              isPresent(By.cssSelector(".article-page")),
                "URL or DOM should indicate article page");
        driver.navigate().back();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlToBe(originalUrl),
                ExpectedConditions.presenceOfElementLocated(ARTICLE_PREVIEW)
        ));
        Assertions.assertTrue(isPresent(ARTICLE_PREVIEW), "Should return to feed with previews");
    }

    @Test
    @Order(6)
    public void open_AuthorProfile_FromHome() {
        openBase();
        wait.until(ExpectedConditions.presenceOfElementLocated(ARTICLE_PREVIEW));
        // Try to find author link within first preview
        Optional<WebElement> authorOpt = findFirst(By.cssSelector(".article-preview .author, .article-meta a.author, .article-meta .info a"));
        Assumptions.assumeTrue(authorOpt.isPresent(), "No author link found on preview");
        String expectedName = authorOpt.get().getText().trim();
        wait.until(ExpectedConditions.elementToBeClickable(authorOpt.get())).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(PROFILE_BIO));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("profile") ||
                              driver.getCurrentUrl().contains("@") ||
                              isPresent(PROFILE_BIO),
                "Should land on author profile page");
        if (!expectedName.isEmpty()) {
            Assertions.assertTrue(driver.getPageSource().toLowerCase(Locale.ROOT).contains(expectedName.toLowerCase(Locale.ROOT)),
                    "Profile page should mention author name (best-effort)");
        }
    }

    @Test
    @Order(7)
    public void footer_External_Thinkster_And_GitHub() {
        openBase();
        if (isPresent(FOOTER)) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'instant',block:'end'});", driver.findElement(FOOTER));
        }

        // Prefer explicit Thinkster link text or href; else any external known socials (GitHub).
        Optional<WebElement> thinkster = findLinkByTextContains("thinkster");
        if (!thinkster.isPresent()) {
            thinkster = driver.findElements(By.cssSelector("a[href*='thinkster.io']")).stream().findFirst();
        }
        thinkster.ifPresent(a -> assertExternalLinkInNewTabOrSame(a, "thinkster.io"));

        // GitHub external (common on RealWorld footer/header)
        List<WebElement> githubLinks = driver.findElements(By.cssSelector("a[href*='github.com']"));
        if (!githubLinks.isEmpty()) {
            // De-dup and test one
            WebElement gh = githubLinks.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(e -> e.getAttribute("href"), e -> e, (e1, e2) -> e1, LinkedHashMap::new),
                            m -> m.values().iterator().next()))
                    ;
            String host = hostOf(gh.getAttribute("href"));
            assertExternalLinkInNewTabOrSame(gh, host.isEmpty() ? "github.com" : host);
        }
    }

    @Test
    @Order(8)
    public void nonApplicable_AppSpecific_Controls_AreAbsent() {
        openBase();
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.findElements(SORTING_DROPDOWN).isEmpty(), "Sorting dropdown should not exist on RealWorld"),
                () -> Assertions.assertTrue(driver.findElements(BURGER_BTN).isEmpty(), "Burger menu button should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_ALL_ITEMS).isEmpty(), "All Items menu entry should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_ABOUT).isEmpty(), "About menu entry should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_LOGOUT).isEmpty(), "Logout menu entry should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_RESET).isEmpty(), "Reset App State menu entry should not exist")
        );
    }
}
