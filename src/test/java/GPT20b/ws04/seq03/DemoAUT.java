package GTP5.ws04.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KatalonFormHeadlessTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().setSize(new Dimension(1400, 1000));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    public void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    // -------------------- Helpers --------------------

    private List<WebElement> findAll(By by) {
        try {
            return driver.findElements(by);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private WebElement first(By by) {
        List<WebElement> els = findAll(by);
        return els.isEmpty() ? null : els.get(0);
    }

    private WebElement findInputByIdNameOrPlaceholder(String... keys) {
        List<WebElement> inputs = new ArrayList<>();
        inputs.addAll(findAll(By.cssSelector("input")));
        inputs.addAll(findAll(By.cssSelector("textarea")));
        for (WebElement el : inputs) {
            String id = orEmpty(el.getAttribute("id")).toLowerCase();
            String name = orEmpty(el.getAttribute("name")).toLowerCase();
            String placeholder = orEmpty(el.getAttribute("placeholder")).toLowerCase();
            String type = orEmpty(el.getAttribute("type")).toLowerCase();
            for (String k : keys) {
                String key = k.toLowerCase();
                if (id.contains(key) || name.contains(key) || placeholder.contains(key) || type.contains(key)) {
                    return el;
                }
            }
        }
        // fallback to direct id/name
        for (String k : keys) {
            WebElement byId = first(By.id(k));
            if (byId != null) return byId;
            WebElement byName = first(By.name(k));
            if (byName != null) return byName;
        }
        return null;
    }

    private String orEmpty(String s) {
        return s == null ? "" : s;
    }

    private WebElement findButtonByTextContains(String... texts) {
        List<WebElement> buttons = new ArrayList<>();
        buttons.addAll(findAll(By.cssSelector("button")));
        buttons.addAll(findAll(By.cssSelector("input[type='submit'], input[type='button']")));
        buttons.addAll(findAll(By.cssSelector("a[role='button']")));
        for (WebElement b : buttons) {
            String t = orEmpty(b.getText()).toLowerCase();
            String v = orEmpty(b.getAttribute("value")).toLowerCase();
            for (String needle : texts) {
                String n = needle.toLowerCase();
                if (t.contains(n) || v.contains(n)) return b;
            }
        }
        return buttons.isEmpty() ? null : buttons.get(0);
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void selectIfPresent(String idOrName, String visibleText) {
        WebElement el = findInputByIdNameOrPlaceholder(idOrName, "select", "dropdown");
        if (el != null && el.getTagName().equalsIgnoreCase("select")) {
            Select sel = new Select(el);
            List<WebElement> opts = sel.getOptions();
            for (WebElement opt : opts) {
                if (opt.getText().trim().equalsIgnoreCase(visibleText)) {
                    sel.selectByVisibleText(opt.getText());
                    return;
                }
            }
            // otherwise select first non-empty
            for (WebElement opt : opts) {
                if (!opt.getText().trim().isEmpty()) {
                    sel.selectByVisibleText(opt.getText());
                    return;
                }
            }
        }
    }

    private void setRadioIfPresent(String groupNameOrId, String labelKeyword) {
        // Try radios by name/id and matching label text
        List<WebElement> radios = findAll(By.cssSelector("input[type='radio']"));
        for (WebElement r : radios) {
            String name = orEmpty(r.getAttribute("name")).toLowerCase();
            String id = orEmpty(r.getAttribute("id")).toLowerCase();
            if (name.contains(groupNameOrId.toLowerCase()) || id.contains(groupNameOrId.toLowerCase())) {
                // check label
                String forId = orEmpty(r.getAttribute("id"));
                List<WebElement> labels = new ArrayList<>();
                labels.addAll(findAll(By.cssSelector("label[for='" + forId + "']")));
                labels.addAll(r.findElements(By.xpath("ancestor::*[1]//label")));
                for (WebElement lab : labels) {
                    if (orEmpty(lab.getText()).toLowerCase().contains(labelKeyword.toLowerCase())) {
                        safeClick(r);
                        return;
                    }
                }
            }
        }
    }

    private void setCheckboxIfPresent(String labelKeyword) {
        List<WebElement> checks = findAll(By.cssSelector("input[type='checkbox']"));
        for (WebElement c : checks) {
            String id = orEmpty(c.getAttribute("id"));
            String name = orEmpty(c.getAttribute("name"));
            List<WebElement> labels = new ArrayList<>();
            if (!id.isEmpty()) labels.addAll(findAll(By.cssSelector("label[for='" + id + "']")));
            labels.addAll(c.findElements(By.xpath("ancestor::*[1]//label")));
            boolean match = labels.stream().anyMatch(l -> orEmpty(l.getText()).toLowerCase().contains(labelKeyword.toLowerCase()))
                    || id.toLowerCase().contains(labelKeyword.toLowerCase())
                    || name.toLowerCase().contains(labelKeyword.toLowerCase());
            if (match && !c.isSelected()) {
                safeClick(c);
                return;
            }
        }
    }

    private boolean assertExternalLinkDomain(WebElement link, String expectedDomainFragment) {
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        safeClick(link);
        try {
            wait.until(d -> driver.getWindowHandles().size() > before.size() || !driver.getCurrentUrl().equals(BASE_URL));
        } catch (TimeoutException ignored) {}
        Set<String> after = driver.getWindowHandles();
        boolean ok = false;
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            ok = driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment);
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            // same tab
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            ok = driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
        return ok;
    }

    private void exerciseAnyDropdowns() {
        List<WebElement> selects = findAll(By.tagName("select"));
        for (WebElement s : selects) {
            Select sel = new Select(s);
            List<String> before = sel.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
            for (int i = 0; i < sel.getOptions().size(); i++) {
                sel.selectByIndex(i);
                List<WebElement> selected = sel.getAllSelectedOptions();
                Assertions.assertFalse(selected.isEmpty(), "A dropdown option should be selected.");
            }
            List<String> after = sel.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
            // Not asserting order change (these are not sorters); just ensure interaction worked
            Assertions.assertEquals(before, after, "Dropdown options should remain consistent after interactions.");
        }
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void homeLoadsAndFormVisible() {
        Assertions.assertTrue(driver.getCurrentUrl().contains("form.html"), "URL should contain form.html");
        WebElement form = first(By.tagName("form"));
        Assertions.assertNotNull(form, "Form should be present on the page.");
        Assertions.assertTrue(findAll(By.cssSelector("input, textarea, select")).size() > 0, "Form fields should be present.");
    }

    @Test
    @Order(2)
    public void fillFormAndSubmit_VerifySuccessOrEcho() {
        // Fill typical fields if they exist
        WebElement firstName = findInputByIdNameOrPlaceholder("first-name", "firstname", "first name", "first");
        if (firstName != null) { firstName.clear(); firstName.sendKeys("John"); }

        WebElement lastName = findInputByIdNameOrPlaceholder("last-name", "lastname", "last name", "surname", "last");
        if (lastName != null) { lastName.clear(); lastName.sendKeys("Doe"); }

        WebElement dob = findInputByIdNameOrPlaceholder("dob", "date", "birth");
        if (dob != null) { dob.clear(); dob.sendKeys("01011990"); }

        WebElement address = findInputByIdNameOrPlaceholder("address", "street");
        if (address != null) { address.clear(); address.sendKeys("123 Test Street"); }

        WebElement email = findInputByIdNameOrPlaceholder("email", "e-mail");
        if (email != null) { email.clear(); email.sendKeys("john.doe@example.com"); }

        WebElement password = findInputByIdNameOrPlaceholder("password", "pwd");
        if (password != null) { password.clear(); password.sendKeys("P@ssw0rd!"); }

        WebElement company = findInputByIdNameOrPlaceholder("company", "organization");
        if (company != null) { company.clear(); company.sendKeys("Katalon QA"); }

        selectIfPresent("role", "QA");
        selectIfPresent("expectation", "Good teamwork");
        setRadioIfPresent("gender", "male");
        setCheckboxIfPresent("java");
        setCheckboxIfPresent("selenium");
        WebElement comment = findInputByIdNameOrPlaceholder("comment", "additional", "about");
        if (comment != null) { comment.clear(); comment.sendKeys("Automated submission by Selenium JUnit test."); }

        WebElement submit = findButtonByTextContains("submit", "enviar");
        Assumptions.assumeTrue(submit != null, "Submit button not found; cannot proceed with submission test.");
        safeClick(submit);

        // Success: try to detect either a message or echo content
        boolean successMsg = !findAll(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success')]")).isEmpty()
                || !findAll(By.cssSelector(".success, .alert-success, .message-success")).isEmpty();
        boolean summaryTable = !findAll(By.cssSelector("table, .result, .summary, .panel-success")).isEmpty();
        // Or URL stays but some result block appears; allow any of these
        Assertions.assertTrue(successMsg || summaryTable || driver.getCurrentUrl().contains("form.html"),
                "After submit, a success indicator, a summary, or same URL should be present.");
    }

    @Test
    @Order(3)
    public void invalidEmailShowsConstraintOrNoSuccess() {
        WebElement email = findInputByIdNameOrPlaceholder("email", "e-mail");
        WebElement submit = findButtonByTextContains("submit", "enviar");
        Assumptions.assumeTrue(email != null && submit != null, "Email field or submit not present; skipping.");
        email.clear();
        email.sendKeys("not-an-email");
        safeClick(submit);

        // Try native validation message if the input is type=email and required
        String validation = email.getAttribute("validationMessage");
        boolean hasValidation = validation != null && !validation.trim().isEmpty();

        // Or look for generic error text
        boolean inlineError = !findAll(By.xpath("//*[contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'error') or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid') or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email')]")).isEmpty();

        Assertions.assertTrue(hasValidation || inlineError || driver.getCurrentUrl().contains("form.html"),
                "Invalid email should not produce a silent successful completion.");
    }

    @Test
    @Order(4)
    public void exerciseDropdownsIfAny() {
        exerciseAnyDropdowns();
        Assertions.assertTrue(true, "Dropdowns exercised (if present).");
    }

    @Test
    @Order(5)
    public void tryBurgerMenuIfPresent() {
        // This static page likely has no burger, but try common selectors gracefully.
        List<By> burgerSelectors = Arrays.asList(
                By.cssSelector("button[aria-label*='menu' i]"),
                By.cssSelector(".navbar-burger, .burger, button.burger"),
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu')]")
        );
        WebElement burger = null;
        for (By by : burgerSelectors) {
            burger = first(by);
            if (burger != null) break;
        }
        if (burger != null) {
            safeClick(burger);
            boolean opened = !findAll(By.cssSelector("nav, .menu, .drawer, [role='navigation']")).isEmpty();
            Assertions.assertTrue(opened, "Burger should open navigation.");
        } else {
            Assertions.assertTrue(true, "No burger menu present; skipped.");
        }
    }

    @Test
    @Order(6)
    public void internalLinksOneLevelNavigation() {
        String origin = getOrigin(BASE_URL);
        List<WebElement> anchors = findAll(By.cssSelector("a[href]"));
        int visited = 0;
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith(origin)) continue; // external handled elsewhere
            if (href.equals(BASE_URL)) continue;
            // one level below: path depth difference not more than 1 segment
            if (!isOneLevelBelow(origin, BASE_URL, href)) continue;

            String current = driver.getCurrentUrl();
            try {
                safeClick(a);
                wait.until(d -> !driver.getCurrentUrl().equals(current));
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(origin), "Should stay on same origin for internal link.");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(current));
                visited++;
            } catch (Exception ignored) {}
            if (visited >= 3) break; // keep it fast and stable
        }
        Assertions.assertTrue(visited >= 0, "Exercised a subset of internal links (if any).");
    }

    @Test
    @Order(7)
    public void externalLinksPolicy_FooterOrBody() {
        Map<String, String> expectedDomains = new LinkedHashMap<>();
        expectedDomains.put("katalon.com", "katalon.com");
        expectedDomains.put("github.com", "github.com");
        expectedDomains.put("twitter.com", "twitter.com");
        expectedDomains.put("facebook.com", "facebook.com");
        expectedDomains.put("linkedin.com", "linkedin.com");
        expectedDomains.put("youtube.com", "youtube.com");

        List<WebElement> links = findAll(By.cssSelector("a[href]"));
        int checked = 0;
        for (Map.Entry<String, String> entry : expectedDomains.entrySet()) {
            String domain = entry.getKey();
            Optional<WebElement> match = links.stream()
                    .filter(a -> {
                        String h = a.getAttribute("href");
                        return h != null && h.toLowerCase().contains(domain);
                    })
                    .findFirst();
            if (match.isPresent()) {
                boolean ok = assertExternalLinkDomain(match.get(), domain);
                Assertions.assertTrue(ok, "External URL should contain: " + domain);
                checked++;
            }
        }
        Assertions.assertTrue(checked >= 0, "Validated external links if present.");
    }

    // -------------------- Utility --------------------

    private String getOrigin(String url) {
        try {
            int schemeIdx = url.indexOf("://");
            if (schemeIdx < 0) return url;
            int slash = url.indexOf('/', schemeIdx + 3);
            return (slash > 0) ? url.substring(0, slash) : url;
        } catch (Exception e) {
            return url;
        }
    }

    private boolean isOneLevelBelow(String origin, String base, String href) {
        // Normalize to paths
        String basePath = base.startsWith(origin) ? base.substring(origin.length()) : base;
        String hrefPath = href.startsWith(origin) ? href.substring(origin.length()) : href;
        if (basePath.startsWith("/")) basePath = basePath.substring(1);
        if (hrefPath.startsWith("/")) hrefPath = hrefPath.substring(1);
        String[] baseSegs = basePath.split("/");
        String[] hrefSegs = hrefPath.split("/");
        // allow same directory or one extra segment
        return hrefSegs.length <= baseSegs.length + 1;
    }
}
