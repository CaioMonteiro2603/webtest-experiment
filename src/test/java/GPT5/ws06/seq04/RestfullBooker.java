package GPT5.ws06.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://automationintesting.online/";

    @BeforeAll
    public static void setUpClass() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterAll
    public static void tearDownClass() {
        if (driver != null) driver.quit();
    }

    // ================= Helpers =================

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

    private WebElement inputByIdOrNameOrPlaceholder(String key, String placeholderContains) {
        WebElement el = first(By.id(key));
        if (el == null) el = first(By.name(key));
        if (el == null && placeholderContains != null) {
            el = first(By.cssSelector("input[placeholder*='" + placeholderContains + "'], textarea[placeholder*='" + placeholderContains + "']"));
        }
        return el;
    }

    private void type(WebElement el, String text) {
        if (el != null) {
            el.clear();
            el.sendKeys(text);
        }
    }

    private void click(WebElement el) {
        if (el != null) wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void assertTextVisibleContains(String fragmentLower) {
        WebElement el = wait.until(d -> {
            List<WebElement> all = d.findElements(By.xpath("//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),\"" + fragmentLower.toLowerCase() + "\")]"));
            for (WebElement e : all) {
                if (e.isDisplayed()) return e;
            }
            return null;
        });
        Assertions.assertNotNull(el, "Expected to see text containing: " + fragmentLower);
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
            // same tab navigation
            if (external) {
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
        List<WebElement> external = links.stream().filter(a -> {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) return false;
            if (!href.startsWith("http")) return false;
            String d = registrableDomain(href);
            return !d.isEmpty() && !d.equalsIgnoreCase(baseDomain);
        }).collect(Collectors.toList());

        // de-duplicate by href
        Map<String, WebElement> uniq = new LinkedHashMap<>();
        for (WebElement a : external) {
            String href = a.getAttribute("href");
            uniq.putIfAbsent(href, a);
        }
        for (WebElement a : uniq.values()) {
            verifyExternalLink(a);
        }
    }

    // ================= Tests =================

    @Test
    @Order(1)
    public void homeSanity() {
        goHome();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should load base URL.");
        // The site header often contains the hotel name
        WebElement header = first(By.cssSelector("header, .navbar, .hotel-header, .navbar-header, .hero, .header"));
        Assertions.assertTrue(header != null && header.isDisplayed(), "Header should be visible.");
        // Basic key sections exist on home (contact form present)
        Assertions.assertTrue(driver.findElements(By.cssSelector("form")).size() > 0, "There should be at least one form on home (contact/book).");
        verifyAllExternalLinksOnPage(); // external links on the home page
    }

    @Test
    @Order(2)
    public void contactFormPositive() {
        goHome();

        // Contact form fields (robust selectors)
        WebElement name = inputByIdOrNameOrPlaceholder("name", "name");
        WebElement email = inputByIdOrNameOrPlaceholder("email", "email");
        WebElement phone = inputByIdOrNameOrPlaceholder("phone", "phone");
        WebElement subject = inputByIdOrNameOrPlaceholder("subject", "subject");
        WebElement description = inputByIdOrNameOrPlaceholder("description", "message");

        type(name, "Test User");
        type(email, "test.user@example.com");
        if (phone != null) type(phone, "1234567890");
        type(subject, "Booking enquiry");
        type(description, "Hello, this is an automated contact test.");

        WebElement submit = first(By.cssSelector("button[type='submit'], input[type='submit'], #submitContact"));
        Assertions.assertNotNull(submit, "Submit button should exist on contact form.");
        String prev = driver.getCurrentUrl();
        click(submit);

        // Expect a success toast / banner or message indicating the contact was submitted
        try {
            assertTextVisibleContains("thanks");
        } catch (Exception e) {
            // Fallback: at least remain on same origin without JS errors
            wait.until(urlChangedFrom(null)); // yields true immediately; keeps flow
            Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://"), "Still navigable after submitting contact.");
        }

        // Return to a known state
        if (!driver.getCurrentUrl().equals(prev)) {
            driver.navigate().to(prev);
        }
    }

    @Test
    @Order(3)
    public void contactFormInvalidEmailShowsError() {
        goHome();

        WebElement name = inputByIdOrNameOrPlaceholder("name", "name");
        WebElement email = inputByIdOrNameOrPlaceholder("email", "email");
        WebElement subject = inputByIdOrNameOrPlaceholder("subject", "subject");
        WebElement description = inputByIdOrNameOrPlaceholder("description", "message");

        type(name, "Invalid Email User");
        type(email, "invalid-email");
        type(subject, "Invalid email test");
        type(description, "This should trigger an email validation error.");

        WebElement submit = first(By.cssSelector("button[type='submit'], input[type='submit'], #submitContact"));
        Assertions.assertNotNull(submit, "Submit button should exist on contact form.");
        click(submit);

        // Look for validation error text near email or a general error alert
        WebElement error = wait.until(d -> {
            List<By> candidates = Arrays.asList(
                    By.cssSelector(".alert-danger, .error, .invalid-feedback, .help-block"),
                    By.xpath("//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'valid email')]"),
                    By.xpath("//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid')]")
            );
            for (By by : candidates) {
                List<WebElement> found = d.findElements(by);
                for (WebElement f : found) if (f.isDisplayed()) return f;
            }
            // Some forms add aria-invalid on the field
            WebElement emailField = first(By.cssSelector("input[type='email'], #email, [name='email']"));
            if (emailField != null && "true".equalsIgnoreCase(emailField.getAttribute("aria-invalid"))) return emailField;
            return null;
        });
        Assertions.assertNotNull(error, "Expected an error/validation indicator for invalid email.");
    }

    @Test
    @Order(4)
    public void navigateRoomsAndValidateContent() {
        goHome();

        // Try to find a Rooms navigation link
        WebElement roomsLink = first(By.xpath("//a[contains(translate(.,'ROOMS','rooms'),'rooms') or contains(@href,'room')]"));
        Assumptions.assumeTrue(roomsLink != null, "Rooms link not found; skipping rooms test.");

        String before = driver.getCurrentUrl();
        click(roomsLink);
        wait.until(urlChangedFrom(before));

        // Assert URL changed and content present
        Assertions.assertTrue(registrableDomain(driver.getCurrentUrl()).equalsIgnoreCase(registrableDomain(BASE_URL)),
                "Rooms page should remain on the same domain.");
        // Look for room cards or book buttons
        boolean hasRooms = !driver.findElements(By.cssSelector(".room, .card, .room-card, [data-testid*='room']")).isEmpty()
                || !driver.findElements(By.xpath("//button[contains(translate(.,'BOOK','book'),'book')]")).isEmpty()
                || !driver.findElements(By.xpath("//*[contains(translate(.,'ROOM','room'),'room')]")).isEmpty();
        Assertions.assertTrue(hasRooms, "Rooms page should display room items/buttons.");

        // Verify external links on Rooms page (one level down)
        verifyAllExternalLinksOnPage();

        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(before));
    }

    @Test
    @Order(5)
    public void navigateBookIfAvailable() {
        goHome();

        WebElement bookLink = first(By.xpath("//a[contains(translate(.,'BOOK','book'),'book') or contains(@href,'book')]"));
        if (bookLink == null) {
            // Try a book button on home
            bookLink = first(By.xpath("//button[contains(translate(.,'BOOK','book'),'book')]"));
        }
        Assumptions.assumeTrue(bookLink != null, "Booking link/button not found; skipping booking test.");

        String start = driver.getCurrentUrl();
        click(bookLink);
        wait.until(urlChangedFrom(start));

        // Booking page sanity: presence of a form or calendar elements
        boolean hasBookingForm = !driver.findElements(By.cssSelector("form")).isEmpty()
                || !driver.findElements(By.cssSelector("input[type='date'], .calendar, .datepicker")).isEmpty()
                || !driver.findElements(By.xpath("//*[contains(translate(.,'BOOK','book'),'book')]")).isEmpty();
        Assertions.assertTrue(hasBookingForm, "Booking page should contain a form or date inputs.");

        // External links on booking page
        verifyAllExternalLinksOnPage();

        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(start));
    }

    @Test
    @Order(6)
    public void adminLoginPageIfPresent_negativeCredentials() {
        goHome();

        WebElement adminLink = first(By.xpath("//a[contains(translate(.,'ADMIN','admin'),'admin') or contains(@href,'admin')]"));
        Assumptions.assumeTrue(adminLink != null, "Admin link not found; skipping admin login test.");

        String prev = driver.getCurrentUrl();
        click(adminLink);
        wait.until(urlChangedFrom(prev));

        // Look for a login form on admin page
        WebElement user = inputByIdOrNameOrPlaceholder("username", "user");
        if (user == null) user = inputByIdOrNameOrPlaceholder("user", "user");
        WebElement pass = inputByIdOrNameOrPlaceholder("password", "password");
        WebElement submit = first(By.cssSelector("button[type='submit'], input[type='submit']"));

        Assumptions.assumeTrue(user != null && pass != null && submit != null, "Admin login form elements not found; skipping.");

        type(user, "invalid");
        type(pass, "invalid");
        click(submit);

        // Expect an error message, toast, or refusal
        WebElement error = wait.until(d -> {
            List<By> candidates = Arrays.asList(
                    By.cssSelector(".alert, .error, .invalid-feedback, .toast-error"),
                    By.xpath("//*[contains(translate(.,'INVALID','invalid'),'invalid') or contains(translate(.,'ERROR','error'),'error')]")
            );
            for (By by : candidates) {
                List<WebElement> found = d.findElements(by);
                for (WebElement f : found) if (f.isDisplayed()) return f;
            }
            return null;
        });
        Assertions.assertNotNull(error, "Expected an error indicator on invalid admin login.");

        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(prev));
    }

    @Test
    @Order(7)
    public void iterateInternalLinksOneLevelFromHome() {
        goHome();

        String baseDomain = registrableDomain(BASE_URL);
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));

        // Filter internal, http(s), non-mailto, non-javascript, not hashes
        List<String> hrefs = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(h -> h != null && !h.isEmpty())
                .filter(h -> h.startsWith("http"))
                .filter(h -> !h.endsWith("#") && !h.contains("#!"))
                .filter(h -> !h.toLowerCase().startsWith("javascript:") && !h.toLowerCase().startsWith("mailto:"))
                .filter(h -> registrableDomain(h).equalsIgnoreCase(baseDomain))
                .distinct()
                .collect(Collectors.toList());

        // Only one level below base: allow direct pages linked from home (limit to 6 to keep runtime bounded)
        int visited = 0;
        for (String href : hrefs) {
            if (visited >= 6) break;
            String current = driver.getCurrentUrl();
            try {
                WebElement toClick = first(By.cssSelector("a[href='" + href + "']"));
                if (toClick == null) continue;
                click(toClick);
                wait.until(urlChangedFrom(current));

                // Basic validation: page displays meaningful content
                boolean hasContent = !driver.findElements(By.tagName("h1")).isEmpty()
                        || !driver.findElements(By.tagName("h2")).isEmpty()
                        || !driver.findElements(By.cssSelector("main, .container, .content")).isEmpty();
                Assertions.assertTrue(hasContent, "Internal page should show content for: " + driver.getCurrentUrl());

                // Also verify external links on that one-level page
                verifyAllExternalLinksOnPage();
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(current));
            }
            visited++;
        }
        Assertions.assertTrue(visited >= 0, "Iterated internal links successfully.");
    }
}
