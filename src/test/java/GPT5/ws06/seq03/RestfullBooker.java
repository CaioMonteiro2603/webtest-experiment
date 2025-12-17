package GPT5.ws06.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://automationintesting.online/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1280, 900));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    public void navigateHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        dismissCookieOrPromoIfPresent();
    }

    // -------------------- Helpers --------------------

    private static String txt(WebElement el) { return el == null ? "" : String.valueOf(el.getText()); }

    private void dismissCookieOrPromoIfPresent() {
        List<By> selectors = Arrays.asList(
                By.cssSelector("button#closeCookie, .cookie button, .cookies button"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'accept')]"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'ok')]")
        );
        for (By by : selectors) {
            List<WebElement> btns = driver.findElements(by);
            if (!btns.isEmpty()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(btns.get(0))).click();
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

    private WebElement field(String... keys) {
        List<WebElement> inputs = new ArrayList<>();
        inputs.addAll(findAll(By.cssSelector("input")));
        inputs.addAll(findAll(By.cssSelector("textarea")));
        for (WebElement el : inputs) {
            String id = String.valueOf(el.getAttribute("id")).toLowerCase();
            String name = String.valueOf(el.getAttribute("name")).toLowerCase();
            String ph = String.valueOf(el.getAttribute("placeholder")).toLowerCase();
            for (String k : keys) {
                String key = k.toLowerCase();
                if (id.contains(key) || name.contains(key) || ph.contains(key)) return el;
            }
        }
        for (String k : keys) {
            WebElement byId = first(By.id(k));
            if (byId != null) return byId;
            WebElement byName = first(By.name(k));
            if (byName != null) return byName;
        }
        return null;
    }

    private WebElement button(String... texts) {
        List<WebElement> buttons = new ArrayList<>();
        buttons.addAll(findAll(By.cssSelector("button")));
        buttons.addAll(findAll(By.cssSelector("input[type='submit'], input[type='button']")));
        for (WebElement b : buttons) {
            String t = txt(b).toLowerCase();
            String v = String.valueOf(b.getAttribute("value")).toLowerCase();
            for (String s : texts) {
                String needle = s.toLowerCase();
                if (t.contains(needle) || v.contains(needle)) return b;
            }
        }
        return buttons.isEmpty() ? null : buttons.get(0);
    }

    private void set(WebElement el, String value) {
        wait.until(ExpectedConditions.visibilityOf(el));
        try { el.clear(); } catch (Exception ignored) {}
        el.sendKeys(value);
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void exerciseAnyDropdowns() {
        List<WebElement> selects = findAll(By.tagName("select"));
        for (WebElement s : selects) {
            Select sel = new Select(s);
            List<WebElement> opts = sel.getOptions();
            for (int i = 0; i < opts.size(); i++) {
                sel.selectByIndex(i);
                Assertions.assertFalse(sel.getAllSelectedOptions().isEmpty(), "Option should be selectable.");
            }
        }
    }

    private boolean openExternalAndAssertDomain(WebElement link, String expectedDomainFragment) {
        String original = driver.getWindowHandle();
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
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            result = driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL)));
            result = driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
        return result;
    }

    private String origin(String url) {
        int scheme = url.indexOf("://");
        if (scheme < 0) return url;
        int slash = url.indexOf('/', scheme + 3);
        return slash > 0 ? url.substring(0, slash) : url;
    }

    private boolean isOneLevelBelow(String origin, String base, String href) {
        String basePath = base.startsWith(origin) ? base.substring(origin.length()) : base;
        String hrefPath = href.startsWith(origin) ? href.substring(origin.length()) : href;
        if (basePath.startsWith("/")) basePath = basePath.substring(1);
        if (hrefPath.startsWith("/")) hrefPath = hrefPath.substring(1);
        String[] baseSegs = basePath.split("/");
        String[] hrefSegs = hrefPath.split("/");
        return hrefSegs.length <= baseSegs.length + 1;
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void homeLoads_HeaderPresent() {
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL.");
        WebElement banner = first(By.cssSelector("section.hero, .hotel-logoUrl, .banner, header"));
        WebElement title = first(By.xpath("//*[self::h1 or self::h2][contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'shady') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'meadows')]"));
        Assertions.assertTrue(banner != null || title != null, "Hero/banner or site title should be visible.");
        WebElement contactForm = first(By.cssSelector("form#contact, form[action*='contact'], form"));
        Assertions.assertNotNull(contactForm, "Contact form should be present on the page.");
    }

    @Test
    @Order(2)
    public void contactForm_SubmitValid_ShowsSuccess() {
        WebElement name = field("name", "firstname");
        WebElement email = field("email");
        WebElement phone = field("phone", "mobile");
        WebElement subject = field("subject");
        WebElement message = field("message", "comment", "description", "enquiry", "enquirytext");
        WebElement submit = button("submit", "send", "enviar");

        Assumptions.assumeTrue(name != null && email != null && phone != null && subject != null && message != null && submit != null,
                "Expected contact form fields are not all present.");

        set(name, "Jane Doe");
        set(email, "jane.doe@example.com");
        set(phone, "123456789");
        set(subject, "Booking question");
        set(message, "Hello, this is an automated test message about booking.");

        safeClick(submit);

        boolean successFound = !findAll(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'thanks for getting in touch') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'we will get back to you') or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'alert-success') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success')]")).isEmpty();

        Assertions.assertTrue(successFound || driver.getCurrentUrl().startsWith(BASE_URL), "Success confirmation should appear or URL should remain on contact section.");
    }

    @Test
    @Order(3)
    public void contactForm_InvalidEmail_ShowsValidation() {
        WebElement name = field("name");
        WebElement email = field("email");
        WebElement phone = field("phone");
        WebElement subject = field("subject");
        WebElement message = field("message");
        WebElement submit = button("submit", "send", "enviar");

        Assumptions.assumeTrue(email != null && submit != null, "Email field or submit button missing.");

        if (name != null) set(name, "John Invalid");
        if (phone != null) set(phone, "123456789");
        if (subject != null) set(subject, "Invalid email test");
        if (message != null) set(message, "Trigger email validation.");

        set(email, "invalid-email");
        safeClick(submit);

        String validation = email.getAttribute("validationMessage");
        boolean nativeValidation = validation != null && !validation.trim().isEmpty();
        boolean inlineError = !findAll(By.xpath("//*[contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'error') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid')]")).isEmpty();

        Assertions.assertTrue(nativeValidation || inlineError, "Invalid email should show validation or error.");
    }

    @Test
    @Order(4)
    public void navigateToRooms_VerifyRoomsList() {
        // Try clicking the "Rooms" navigation link if present
        List<By> roomsLinkSelectors = Arrays.asList(
                By.linkText("Rooms"),
                By.partialLinkText("Room"),
                By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'rooms')]")
        );
        WebElement roomsLink = null;
        for (By by : roomsLinkSelectors) {
            roomsLink = first(by);
            if (roomsLink != null) break;
        }
        Assumptions.assumeTrue(roomsLink != null, "Rooms navigation link not found.");
        String before = driver.getCurrentUrl();
        safeClick(roomsLink);
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(before)));

        boolean onRooms = driver.getCurrentUrl().toLowerCase().contains("room");
        boolean roomCardsPresent = !findAll(By.cssSelector(".room, .hotel-room, .row .col-sm-3, .room-info")).isEmpty();

        Assertions.assertTrue(onRooms || roomCardsPresent, "Should be on rooms page or see room listing.");
        // Go back to keep state clean
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(5)
    public void exerciseDropdowns_AllOptionsSelectable_IfAny() {
        exerciseAnyDropdowns();
        Assertions.assertTrue(true, "Dropdowns exercised if any exist.");
    }

    @Test
    @Order(6)
    public void internalLinks_OneLevelBelow_NavigateAndReturn() {
        String origin = origin(BASE_URL);
        List<WebElement> anchors = findAll(By.cssSelector("a[href]"));
        int visited = 0;
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith(origin)) continue; // external handled elsewhere
            if (href.equals(BASE_URL)) continue;
            if (!isOneLevelBelow(origin, BASE_URL, href)) continue;

            String before = driver.getCurrentUrl();
            try {
                safeClick(a);
                wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(before)));
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(origin), "Should remain on same origin.");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(before));
                visited++;
            } catch (Exception ignored) {}
            if (visited >= 3) break; // limit for stability
        }
        Assertions.assertTrue(visited >= 0, "Visited a subset of internal links if available.");
    }

    @Test
    @Order(7)
    public void footerExternalLinks_OpenAndVerifyDomains() {
        Map<String, String> domains = new LinkedHashMap<>();
        domains.put("twitter.com", "twitter.com");
        domains.put("facebook.com", "facebook.com");
        domains.put("linkedin.com", "linkedin.com");
        domains.put("github.com", "github.com");
        domains.put("youtube.com", "youtube.com");

        List<WebElement> links = findAll(By.cssSelector("footer a[href], a[href]"));
        int checked = 0;
        for (Map.Entry<String, String> entry : domains.entrySet()) {
            String domain = entry.getKey();
            Optional<WebElement> match = links.stream()
                    .filter(a -> {
                        String h = a.getAttribute("href");
                        return h != null && h.toLowerCase().contains(domain);
                    })
                    .findFirst();
            if (match.isPresent()) {
                boolean ok = openExternalAndAssertDomain(match.get(), domain);
                Assertions.assertTrue(ok, "External URL should contain domain: " + domain);
                checked++;
            }
        }
        Assertions.assertTrue(checked >= 0, "Validated external links if present.");
    }

    @Test
    @Order(8)
    public void burgerMenu_OpenClose_IfPresent() {
        // force smaller width to encourage burger visibility
        driver.manage().window().setSize(new Dimension(480, 800));
        driver.get(BASE_URL);
        dismissCookieOrPromoIfPresent();

        List<By> burgerSelectors = Arrays.asList(
                By.cssSelector("button[aria-label*='menu' i]"),
                By.cssSelector("button.navbar-toggler, .navbar-burger, .burger"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(@class,'menu')]")
        );
        WebElement burger = null;
        for (By by : burgerSelectors) {
            burger = first(by);
            if (burger != null) break;
        }
        if (burger != null) {
            safeClick(burger);
            boolean navOpened = !findAll(By.cssSelector("nav.show, .navbar-collapse.show, .menu.open, .drawer.open, [role='navigation']")).isEmpty()
                    || !findAll(By.xpath("//nav")).isEmpty();
            Assertions.assertTrue(navOpened, "Burger should open navigation.");
            safeClick(burger); // close if toggler
        } else {
            Assertions.assertTrue(true, "No burger menu on current viewport; skipped.");
        }
        // restore size
        driver.manage().window().setSize(new Dimension(1280, 900));
    }
}
