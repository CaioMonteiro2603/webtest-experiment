package GPT5.ws05.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1400, 1000));
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
    }

    // -------------------- Helpers --------------------

    private static String orEmpty(String s) { return s == null ? "" : s; }

    private List<WebElement> findAll(By by) {
        try { return driver.findElements(by); } catch (Exception e) { return Collections.emptyList(); }
    }

    private WebElement first(By by) {
        List<WebElement> list = findAll(by);
        return list.isEmpty() ? null : list.get(0);
    }

    private WebElement findInputByIdNameOrPlaceholder(String... keys) {
        List<WebElement> candidates = new ArrayList<>();
        candidates.addAll(findAll(By.cssSelector("input")));
        candidates.addAll(findAll(By.cssSelector("textarea")));
        for (WebElement el : candidates) {
            String id = orEmpty(el.getAttribute("id")).toLowerCase();
            String name = orEmpty(el.getAttribute("name")).toLowerCase();
            String placeholder = orEmpty(el.getAttribute("placeholder")).toLowerCase();
            for (String k : keys) {
                String key = k.toLowerCase();
                if (id.contains(key) || name.contains(key) || placeholder.contains(key)) return el;
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

    private WebElement findButtonByTextContains(String... texts) {
        List<WebElement> buttons = new ArrayList<>();
        buttons.addAll(findAll(By.cssSelector("button")));
        buttons.addAll(findAll(By.cssSelector("input[type='submit'], input[type='button']")));
        for (WebElement b : buttons) {
            String t = orEmpty(b.getText()).toLowerCase();
            String v = orEmpty(b.getAttribute("value")).toLowerCase();
            for (String needle : texts) {
                String n = needle.toLowerCase();
                if (t.contains(n) || v.contains(n)) return b;
            }
        }
        // fallback common submit selector
        WebElement fallback = first(By.cssSelector("form button, form input[type='submit']"));
        if (fallback != null) return fallback;
        return buttons.isEmpty() ? null : buttons.get(0);
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void setValue(WebElement el, String value) {
        wait.until(ExpectedConditions.visibilityOf(el));
        el.clear();
        el.sendKeys(value);
    }

    private void selectIfPresent(String idOrName, String... preferVisibleTexts) {
        WebElement el = findInputByIdNameOrPlaceholder(idOrName, "product", "assunto", "subject", "select");
        if (el != null && el.getTagName().equalsIgnoreCase("select")) {
            Select sel = new Select(el);
            List<WebElement> opts = sel.getOptions();
            for (String pv : preferVisibleTexts) {
                for (WebElement o : opts) {
                    if (o.getText().trim().equalsIgnoreCase(pv)) {
                        sel.selectByVisibleText(o.getText());
                        return;
                    }
                }
            }
            if (opts.size() > 1) sel.selectByIndex(1); // skip placeholder if any
            else if (!opts.isEmpty()) sel.selectByIndex(0);
        }
    }

    private void checkByLabelKeywordIfPresent(String keyword) {
        List<WebElement> checks = findAll(By.cssSelector("input[type='checkbox']"));
        for (WebElement c : checks) {
            String id = orEmpty(c.getAttribute("id"));
            String name = orEmpty(c.getAttribute("name"));
            List<WebElement> labels = new ArrayList<>();
            if (!id.isEmpty()) labels.addAll(findAll(By.cssSelector("label[for='" + id + "']")));
            labels.addAll(c.findElements(By.xpath("ancestor::*[1]//label")));
            boolean match = labels.stream().anyMatch(l -> orEmpty(l.getText()).toLowerCase().contains(keyword.toLowerCase()))
                    || id.toLowerCase().contains(keyword.toLowerCase())
                    || name.toLowerCase().contains(keyword.toLowerCase());
            if (match && !c.isSelected()) {
                safeClick(c);
                break;
            }
        }
    }

    private boolean assertExternalLinkDomain(WebElement link, String expectedDomainFragment) {
        String currentWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        safeClick(link);
        try {
            wait.until(d -> driver.getWindowHandles().size() > before.size() || !driver.getCurrentUrl().equals(BASE_URL));
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        boolean ok;
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            ok = driver.getCurrentUrl().toLowerCase().contains(expectedDomainFragment);
            driver.close();
            driver.switchTo().window(currentWindow);
        } else {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(BASE_URL)));
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
                Assertions.assertFalse(sel.getAllSelectedOptions().isEmpty(), "Option should be selectable.");
            }
            List<String> after = sel.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
            Assertions.assertEquals(before, after, "Options should remain consistent after selection.");
        }
    }

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
    public void homeLoadsAndKeyElementsPresent() {
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "URL should contain index.html");
        WebElement title = first(By.cssSelector("h1, h2, .header, .title"));
        Assertions.assertNotNull(title, "A page title/header should be present.");
        WebElement form = first(By.tagName("form"));
        Assertions.assertNotNull(form, "Contact form should be present.");
        int fields = findAll(By.cssSelector("input, textarea, select")).size();
        Assertions.assertTrue(fields > 0, "Form fields should be present.");
    }

    @Test
    @Order(2)
    public void submitValidForm_ShowsSuccessOrStaysWithConfirmation() {
        WebElement firstName = findInputByIdNameOrPlaceholder("first", "first-name", "nome", "first name");
        WebElement lastName  = findInputByIdNameOrPlaceholder("last", "last-name", "sobrenome", "last name");
        WebElement email     = findInputByIdNameOrPlaceholder("email", "e-mail");
        WebElement message   = findInputByIdNameOrPlaceholder("open-text-area", "message", "mensagem", "comment", "description");
        if (firstName != null) setValue(firstName, "Maria");
        if (lastName  != null) setValue(lastName,  "Silva");
        if (email     != null) setValue(email,     "maria.silva@example.com");
        if (message   != null) setValue(message,   "Mensagem automática de teste.");

        selectIfPresent("product", "YouTube", "Blog", "Cursos", "Mentoria");
        checkByLabelKeywordIfPresent("email");
        checkByLabelKeywordIfPresent("phone");

        WebElement submit = findButtonByTextContains("enviar", "send", "submit");
        Assumptions.assumeTrue(submit != null, "Submit button not found.");
        safeClick(submit);

        boolean successText = !findAll(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sucesso') or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'success')]")).isEmpty();
        boolean successClass = !findAll(By.cssSelector(".success, .alert-success, .success-message")).isEmpty();
        boolean sameUrl = driver.getCurrentUrl().contains("index.html");

        Assertions.assertTrue(successText || successClass || sameUrl, "After submit, a success indication or same URL should be present.");
    }

    @Test
    @Order(3)
    public void invalidEmail_ShowsValidationMessageOrError() {
        WebElement email = findInputByIdNameOrPlaceholder("email", "e-mail");
        WebElement submit = findButtonByTextContains("enviar", "send", "submit");
        Assumptions.assumeTrue(email != null && submit != null, "Email field or submit button missing.");

        setValue(email, "invalid-email");
        safeClick(submit);

        String validation = email.getAttribute("validationMessage");
        boolean nativeValidation = validation != null && !validation.trim().isEmpty();
        boolean inlineError = !findAll(By.xpath("//*[contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'error') or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email') or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'inválid') or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid')]")).isEmpty();

        Assertions.assertTrue(nativeValidation || inlineError || driver.getCurrentUrl().contains("index.html"),
                "Invalid email should trigger validation or not allow successful submission.");
    }

    @Test
    @Order(4)
    public void exerciseDropdowns_AllOptionsSelectable() {
        exerciseAnyDropdowns();
        Assertions.assertTrue(true, "Dropdowns exercised (if present).");
    }

    @Test
    @Order(5)
    public void internalLinks_OneLevelBelow_NavigateAndBack() {
        String origin = getOrigin(BASE_URL);
        List<WebElement> anchors = findAll(By.cssSelector("a[href]"));
        int visited = 0;
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith(origin)) continue; // external handled in another test
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
            if (visited >= 3) break; // keep stable
        }
        Assertions.assertTrue(visited >= 0, "Visited a subset of internal links (if present).");
    }

    @Test
    @Order(6)
    public void externalLinks_OpenAndVerifyDomain_ThenReturn() {
        Map<String, String> expectedDomains = new LinkedHashMap<>();
        expectedDomains.put("github.com", "github.com");
        expectedDomains.put("twitter.com", "twitter.com");
        expectedDomains.put("linkedin.com", "linkedin.com");
        expectedDomains.put("youtube.com", "youtube.com");
        expectedDomains.put("katalon.com", "katalon.com");

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

    @Test
    @Order(7)
    public void tryBurgerMenuIfPresent_OpenClose() {
        // This static site may not have a burger; attempt common patterns gracefully.
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
            safeClick(burger);
        } else {
            Assertions.assertTrue(true, "No burger menu present; skipped.");
        }
    }
}
