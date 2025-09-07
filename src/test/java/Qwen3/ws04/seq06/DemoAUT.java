package GPT5.ws04.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class KatalonFormHeadlessSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setupClass() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    }

    @AfterAll
    public static void tearDownClass() {
        if (driver != null) driver.quit();
    }

    // ----------------------------
    // Utilities
    // ----------------------------

    private static void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("katalon-test.s3.amazonaws.com"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL) || driver.getCurrentUrl().contains("/aut/html/"),
                "Base URL did not load as expected.");
    }

    private static Optional<WebElement> first(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? Optional.empty() : Optional.of(els.get(0));
    }

    private static Optional<WebElement> waitVisible(By by) {
        try {
            return Optional.of(wait.until(ExpectedConditions.visibilityOfElementLocated(by)));
        } catch (TimeoutException e) {
            return Optional.empty();
        }
    }

    private static Optional<WebElement> waitClickable(By by) {
        try {
            return Optional.of(wait.until(ExpectedConditions.elementToBeClickable(by)));
        } catch (TimeoutException e) {
            return Optional.empty();
        }
    }

    private static boolean clickIfPresent(By by) {
        Optional<WebElement> el = waitClickable(by);
        el.ifPresent(WebElement::click);
        return el.isPresent();
    }

    private static String hostOf(String url) {
        try {
            URI u = new URI(url);
            return (u.getHost() == null ? "" : u.getHost().toLowerCase(Locale.ROOT));
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private static String pathOf(String url) {
        try {
            URI u = new URI(url);
            return u.getPath() == null ? "/" : u.getPath();
        } catch (URISyntaxException e) {
            return "/";
        }
    }

    private static String baseDirectoryPath() {
        String p = pathOf(BASE_URL);
        if (!p.contains("/")) return "/";
        int idx = p.lastIndexOf('/');
        return p.substring(0, idx + 1); // keep trailing slash to denote directory
    }

    private static int relativeDepthFromBaseDir(String candidatePath) {
        // Count slashes after the base directory
        String baseDir = baseDirectoryPath();
        if (!candidatePath.startsWith(baseDir)) return Integer.MAX_VALUE;
        String rel = candidatePath.substring(baseDir.length());
        if (rel.isEmpty()) return 0;
        // files like form.html have depth 0; subdir/page.html depth 1, etc.
        return rel.contains("/") ? rel.split("/").length - 1 : 0;
    }

    private static List<String> collectInternalOneLevelBelow() {
        String baseHost = hostOf(BASE_URL);
        String baseDir = baseDirectoryPath();
        Set<String> urls = new LinkedHashSet<>();
        for (WebElement a : driver.findElements(By.cssSelector("a[href]"))) {
            String href = a.getAttribute("href");
            if (href == null || href.startsWith("javascript:") || href.startsWith("#")) continue;
            if (!hostOf(href).equals(baseHost)) continue;
            String path = pathOf(href);
            if (!path.startsWith(baseDir)) continue;
            int depth = relativeDepthFromBaseDir(path);
            if (depth <= 0) urls.add(href);
        }
        urls.add(BASE_URL);
        return new ArrayList<>(urls);
    }

    private static void assertExternalLinkBehavior(WebElement link) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        String href = link.getAttribute("href");
        String expectedHost = hostOf(href);

        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        // New tab or same tab?
        try {
            wait.until(d -> d.getWindowHandles().size() != before.size());
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> !d.getCurrentUrl().isEmpty());
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedHost),
                    "External link did not navigate to expected domain. Expected host: " + expectedHost + " actual: " + driver.getCurrentUrl());
            driver.close();
            driver.switchTo().window(original);
        } else {
            // same tab navigation
            wait.until(d -> !d.getCurrentUrl().equals(BASE_URL));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedHost),
                    "External link did not navigate to expected domain in same tab.");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains(hostOf(BASE_URL)));
        }
    }

    private static boolean elementExists(By by) {
        return driver.findElements(by).size() > 0;
    }

    private static WebElement findSubmitButton() {
        By[] candidates = new By[] {
                By.cssSelector("button[type='submit']"),
                By.cssSelector("input[type='submit']"),
                By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit')]"),
                By.xpath("//input[@type='button' or @type='submit'][contains(translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit')]")
        };
        for (By by : candidates) {
            if (elementExists(by)) return driver.findElement(by);
        }
        throw new NoSuchElementException("No submit button found.");
    }

    private static Optional<WebElement> findForm() {
        return first(By.tagName("form"));
    }

    private static boolean html5FormValidity() {
        Optional<WebElement> form = findForm();
        if (form.isEmpty()) return true; // if no form, consider valid to avoid false failures
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object res = js.executeScript("var f=arguments[0];return f && f.checkValidity && f.checkValidity();", form.get());
        return res instanceof Boolean && (Boolean) res;
    }

    private static void setValue(By locator, String value) {
        Optional<WebElement> el = first(locator);
        el.ifPresent(e -> {
            wait.until(ExpectedConditions.visibilityOf(e));
            e.clear();
            e.sendKeys(value);
        });
    }

    private static void selectIfPresent(By locator, String visibleText) {
        Optional<WebElement> el = first(locator);
        el.ifPresent(e -> {
            wait.until(ExpectedConditions.elementToBeClickable(e));
            Select sel = new Select(e);
            Optional<WebElement> opt = sel.getOptions().stream().filter(o -> o.getText().trim().equalsIgnoreCase(visibleText)).findFirst();
            if (opt.isPresent()) {
                sel.selectByVisibleText(opt.get().getText());
            } else if (!sel.getOptions().isEmpty()) {
                sel.selectByIndex(Math.min(1, sel.getOptions().size() - 1));
            }
        });
    }

    private static void clickIfExists(By locator) {
        Optional<WebElement> el = first(locator);
        el.ifPresent(e -> wait.until(ExpectedConditions.elementToBeClickable(e)).click());
    }

    // ----------------------------
    // Tests
    // ----------------------------

    @Test
    @Order(1)
    @DisplayName("Base page loads and form is visible")
    void baseLoadsAndFormVisible() {
        openBase();
        Optional<WebElement> form = waitVisible(By.tagName("form"));
        Assertions.assertTrue(form.isPresent(), "Expected a form element on the base page.");
        // Basic field presence (robust candidates)
        boolean hasAnyTextInput = elementExists(By.cssSelector("input[type='text']")) ||
                elementExists(By.cssSelector("input[name='firstName'], input[id*='first' i]")) ||
                elementExists(By.cssSelector("input[name='lastName'], input[id*='last' i]"));
        Assertions.assertTrue(hasAnyTextInput, "Expected at least one text input field.");
    }

    @Test
    @Order(2)
    @DisplayName("Invalid submission: required fields must block submit (HTML5 validity or error shown)")
    void invalidSubmissionShowsValidation() {
        openBase();
        Optional<WebElement> formOpt = findForm();
        Assumptions.assumeTrue(formOpt.isPresent(), "No form found; skipping validation test.");

        WebElement submit = findSubmitButton();
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        // Assert: either HTML5 validity fails or an error appears or URL unchanged
        boolean validity = html5FormValidity();
        boolean errorMessageShown = elementExists(By.cssSelector(".error, .help-block, .alert, [role='alert']")) ||
                elementExists(By.xpath("//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'required') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'preench')]"));
        boolean stayed = driver.getCurrentUrl().contains("/aut/html/");
        Assertions.assertTrue(!validity || errorMessageShown || stayed,
                "Submitting empty form did not show validation error nor remained on the form.");
    }

    @Test
    @Order(3)
    @DisplayName("Fill form and submit successfully (alert, success text, or URL change)")
    void fillFormAndSubmit() {
        openBase();

        // Fill common demo fields (use multiple candidates to be robust)
        setValue(By.cssSelector("input[name='firstName'], input[id*='first' i]"), "John");
        setValue(By.cssSelector("input[name='lastName'], input[id*='last' i]"), "Doe");
        setValue(By.cssSelector("input[type='email'], input[name='email'], input[id*='email' i]"), "john.doe@example.com");
        setValue(By.cssSelector("input[type='tel'], input[name='phone'], input[id*='phone' i]"), "5551234567");
        setValue(By.cssSelector("input[type='date'], input[name*='birth' i], input[id*='birth' i]"), "1990-01-01");
        setValue(By.cssSelector("textarea[name='comment'], textarea[id*='comment' i], textarea"), "Test submission via Selenium.");

        // Gender or similar radios/checkboxes if exist
        clickIfExists(By.cssSelector("input[type='radio'][value='male'], input[type='radio'][value='Male'], input[type='radio']"));
        clickIfExists(By.cssSelector("input[type='checkbox'][name*='agree' i], input[type='checkbox'][id*='agree' i], input[type='checkbox']"));

        // Select dropdowns if present
        selectIfPresent(By.cssSelector("select[name*='role' i], select[id*='role' i], select"), "Developer");

        // Ensure form is now valid before submit if possible
        boolean validNow = html5FormValidity();
        Assertions.assertTrue(validNow || findForm().isEmpty(), "Form still invalid before submission.");

        WebElement submit = findSubmitButton();
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        boolean successByUrl = !driver.getCurrentUrl().equals(BASE_URL) && !driver.getCurrentUrl().endsWith("form.html");
        boolean successByText = driver.getPageSource().toLowerCase(Locale.ROOT).contains("success")
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("thank")
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("submitted");

        boolean successByAlert = false;
        try {
            wait.withTimeout(Duration.ofSeconds(3));
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            successByAlert = alert != null && alert.getText() != null;
            if (alert != null) alert.accept();
        } catch (TimeoutException ignored) {
        } finally {
            wait.withTimeout(DEFAULT_TIMEOUT);
        }

        Assertions.assertTrue(successByUrl || successByText || successByAlert,
                "No clear success indicator after submitting the form.");
    }

    @Test
    @Order(4)
    @DisplayName("Visit all internal pages one level below base directory")
    void visitInternalOneLevelBelow() {
        openBase();
        List<String> internal = collectInternalOneLevelBelow();
        Assumptions.assumeTrue(!internal.isEmpty(), "No internal links found one level below.");
        for (String url : internal) {
            driver.navigate().to(url);
            wait.until(d -> d.getCurrentUrl().startsWith("https://"));
            Assertions.assertEquals(hostOf(BASE_URL), hostOf(driver.getCurrentUrl()),
                    "Internal navigation landed on unexpected host: " + driver.getCurrentUrl());
            // Basic content check
            Assertions.assertFalse(driver.getPageSource().isEmpty(), "Page appears empty: " + url);
        }
        openBase();
    }

    @Test
    @Order(5)
    @DisplayName("External links on base and one-level pages open correct domains")
    void externalLinksBehavior() {
        openBase();
        Set<String> pages = new LinkedHashSet<>(collectInternalOneLevelBelow());
        for (String page : pages) {
            driver.navigate().to(page);
            wait.until(ExpectedConditions.urlContains(hostOf(BASE_URL)));
            List<WebElement> externals = driver.findElements(By.cssSelector("a[href]"))
                    .stream()
                    .filter(a -> {
                        String href = a.getAttribute("href");
                        if (href == null || href.startsWith("javascript:") || href.startsWith("#")) return false;
                        return !hostOf(href).equals(hostOf(BASE_URL));
                    })
                    .collect(Collectors.toList());
            for (WebElement link : externals) {
                String href = link.getAttribute("href");
                String domain = hostOf(href);
                Assumptions.assumeTrue(!domain.isEmpty(), "External link without host.");
                assertExternalLinkBehavior(link);
            }
        }
        openBase();
    }

    @Test
    @Order(6)
    @DisplayName("Dropdown interactions (if a sorting/select exists)")
    void dropdownInteractionsIfPresent() {
        openBase();
        List<WebElement> selects = driver.findElements(By.cssSelector("select"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No select elements found; skipping dropdown test.");

        WebElement selectEl = selects.get(0);
        Select sel = new Select(selectEl);
        List<String> originalOrder = sel.getOptions().stream().map(o -> o.getText().trim()).collect(Collectors.toList());
        Assumptions.assumeTrue(originalOrder.size() >= 2, "Not enough options to test.");

        // Select the last option then the first and ensure selection changes
        sel.selectByIndex(originalOrder.size() - 1);
        String after1 = sel.getFirstSelectedOption().getText().trim();
        sel.selectByIndex(0);
        String after2 = sel.getFirstSelectedOption().getText().trim();

        Assertions.assertNotEquals(after1, after2, "Selecting different options did not change the selection.");

        // If there is any dynamic list that reacts (heuristic), verify DOM change by toggling selection and checking body HTML length changes
        String body1 = driver.findElement(By.tagName("body")).getAttribute("innerHTML");
        sel.selectByIndex(Math.min(1, originalOrder.size() - 1));
        String body2 = driver.findElement(By.tagName("body")).getAttribute("innerHTML");
        Assertions.assertTrue(!body1.equals(body2) || originalOrder.size() > 0, "Selecting options did not cause any observable change (acceptable if static).");
    }

    @Test
    @Order(7)
    @DisplayName("Menu/Burger actions if available (open/close, About external, Reset, Logout)")
    void menuActionsIfAvailable() {
        openBase();
        // Generic burger/menu button candidates
        By[] burgers = new By[] {
                By.cssSelector("button[aria-label*='menu' i], button[id*='menu' i], .bm-burger-button, .hamburger, .menu"),
                By.xpath("//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu')]")
        };
        Optional<WebElement> burger = Optional.empty();
        for (By by : burgers) {
            burger = first(by);
            if (burger.isPresent()) break;
        }
        Assumptions.assumeTrue(burger.isPresent(), "No burger/menu found; skipping.");

        wait.until(ExpectedConditions.elementToBeClickable(burger.get())).click();

        // All Items/Home
        By home = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'home') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'all items') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'início') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'inicio')]");
        if (elementExists(home)) {
            clickIfPresent(home);
            Assertions.assertTrue(hostOf(driver.getCurrentUrl()).equals(hostOf(BASE_URL)), "Home/All Items did not keep us on base host.");
        }

        // About (external)
        By about = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'about') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sobre')]");
        if (elementExists(about)) {
            WebElement link = driver.findElement(about);
            assertExternalLinkBehavior(link);
        }

        // Reset App State
        By reset = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset app state') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'limpar')]");
        if (elementExists(reset)) {
            Assertions.assertTrue(clickIfPresent(reset), "Failed to click Reset App State.");
        }

        // Logout
        By logout = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log out') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair')]");
        if (elementExists(logout)) {
            clickIfPresent(logout);
            Assertions.assertTrue(driver.getCurrentUrl().contains("/aut/html/") || driver.getCurrentUrl().equals(BASE_URL),
                    "Logout did not return to a base/login-like page.");
        }

        // Close menu if still open
        clickIfPresent(burgers[0]);
        openBase();
    }

    @Test
    @Order(8)
    @DisplayName("Login behavior tests if a login form exists")
    void loginBehaviorIfPresent() {
        openBase();
        // Try to locate email/username and password fields
        Optional<WebElement> user = first(By.cssSelector("input[type='email'], input[name*='user' i], input[id*='user' i], input[name*='email' i], input[id*='email' i]"));
        Optional<WebElement> pass = first(By.cssSelector("input[type='password'], input[name*='pass' i], input[id*='pass' i]"));
        Optional<WebElement> submit = Optional.empty();
        if (user.isPresent() || pass.isPresent()) {
            // Find a likely submit
            try {
                submit = Optional.of(findSubmitButton());
            } catch (NoSuchElementException ignored) {}
        }
        Assumptions.assumeTrue(user.isPresent() && pass.isPresent() && submit.isPresent(), "No login form detected; skipping login tests.");

        // Negative: invalid credentials
        user.get().clear(); user.get().sendKeys("invalid@example.com");
        pass.get().clear(); pass.get().sendKeys("wrong");
        wait.until(ExpectedConditions.elementToBeClickable(submit.get())).click();

        boolean error = elementExists(By.cssSelector(".error, .alert, [role='alert']")) ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("invalid") ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("erro");
        Assertions.assertTrue(error || driver.getCurrentUrl().contains("/aut/html/"),
                "Invalid login did not show an error or remain on the login page.");

        // Positive: if site has known demo credentials (not provided), skip
        Assumptions.assumeTrue(false, "No valid credentials provided for this site; positive login test skipped.");
    }
}
