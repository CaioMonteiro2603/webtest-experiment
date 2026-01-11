package GPT5.ws05.seq06;

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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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
        if (driver != null) {
            driver.quit();
        }
    }

    // ----------------------------
    // Utilities
    // ----------------------------

    private static void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains("cac-tat.s3.eu-central-1.amazonaws.com"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://cac-tat.s3.eu-central-1.amazonaws.com"),
                "Base URL did not load as expected.");
    }

    private static Optional<WebElement> first(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? Optional.empty() : Optional.of(els.get(0));
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

    private static boolean elementExists(By by) {
        return driver.findElements(by).size() > 0;
    }

    private static void clearAndType(WebElement input, String value) {
        wait.until(ExpectedConditions.visibilityOf(input));
        input.clear();
        input.sendKeys(value);
    }

    private static String hostOf(String url) {
        try {
            URI u = new URI(url);
            return u.getHost() == null ? "" : u.getHost().toLowerCase(Locale.ROOT);
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
        int idx = p.lastIndexOf('/');
        if (idx < 0) return "/";
        return p.substring(0, idx + 1); // keep trailing slash
    }

    private static int relativeDepthFromBaseDir(String candidatePath) {
        String baseDir = baseDirectoryPath();
        if (!candidatePath.startsWith(baseDir)) return Integer.MAX_VALUE;
        String rel = candidatePath.substring(baseDir.length());
        if (rel.isEmpty()) return 0;
        return rel.contains("/") ? rel.split("/").length - 1 : 0; // file directly under baseDir => 0
    }

    private static List<String> collectInternalLinksOneLevel() {
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
            if (depth <= 0) {
                urls.add(href);
            }
        }
        urls.add(BASE_URL);
        return new ArrayList<>(urls);
    }

    private static void assertExternalLinkInNewOrSameTab(WebElement link) {
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        String href = link.getAttribute("href");
        String expectedHost = hostOf(href);

        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

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
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(d -> !d.getCurrentUrl().equals(BASE_URL));
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedHost),
                    "External link did not navigate to expected domain in same tab.");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains(hostOf(BASE_URL)));
        }
    }

    private static Optional<WebElement> findForm() {
        return first(By.tagName("form"));
    }

    private static boolean html5FormValidity() {
        Optional<WebElement> form = findForm();
        if (form.isEmpty()) return true;
        Object res = ((JavascriptExecutor) driver).executeScript(
                "var f=arguments[0];return f && f.checkValidity ? f.checkValidity() : true;", form.get());
        return res instanceof Boolean && (Boolean) res;
    }

    private static WebElement findSubmitButton() {
        By[] candidates = new By[] {
                By.cssSelector("button[type='submit']"),
                By.cssSelector("input[type='submit']"),
                By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'enviar') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit')]"),
                By.xpath("//input[@type='button' or @type='submit'][contains(translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'enviar') or contains(translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submit')]")
        };
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        throw new NoSuchElementException("No submit button found.");
    }

    private static void setValue(By locator, String value) {
        Optional<WebElement> el = first(locator);
        el.ifPresent(e -> clearAndType(e, value));
    }

    private static void selectIfPresent(By locator, String visibleText) {
        Optional<WebElement> el = first(locator);
        el.ifPresent(e -> {
            Select sel = new Select(e);
            List<WebElement> options = sel.getOptions();
            Optional<WebElement> match = options.stream().filter(o -> o.getText().trim().equalsIgnoreCase(visibleText)).findFirst();
            if (match.isPresent()) {
                sel.selectByVisibleText(match.get().getText());
            } else if (!options.isEmpty()) {
                sel.selectByIndex(Math.min(1, options.size() - 1));
            }
        });
    }

    // ----------------------------
    // Tests
    // ----------------------------

    @Test
    @Order(1)
    @DisplayName("Base page loads; internal pages one level reachable")
    void baseAndInternalOneLevelReachable() {
        openBase();
        List<String> internal = collectInternalLinksOneLevel();
        Assertions.assertTrue(!internal.isEmpty(), "No internal links found at one level.");
        for (String url : internal) {
            driver.navigate().to(url);
            wait.until(d -> d.getCurrentUrl().startsWith("https://"));
            Assertions.assertEquals(hostOf(BASE_URL), hostOf(driver.getCurrentUrl()),
                    "Internal page not on expected host: " + driver.getCurrentUrl());
            Assertions.assertFalse(driver.getPageSource().isEmpty(), "Page appears empty: " + url);
        }
        openBase();
    }

    @Test
    @Order(2)
    @DisplayName("External links on base and one-level pages open correct domains")
    void externalLinksPolicy() {
        openBase();
        Set<String> pages = new LinkedHashSet<>(collectInternalLinksOneLevel());
        for (String p : pages) {
            driver.navigate().to(p);
            wait.until(ExpectedConditions.urlContains(hostOf(BASE_URL)));
            List<WebElement> externals = driver.findElements(By.cssSelector("a[href]"))
                    .stream()
                    .filter(a -> {
                        String href = a.getAttribute("href");
                        if (href == null || href.startsWith("#") || href.startsWith("javascript:")) return false;
                        return !hostOf(href).equals(hostOf(BASE_URL));
                    })
                    .collect(Collectors.toList());
            for (WebElement link : externals) {
                String href = link.getAttribute("href");
                Assumptions.assumeTrue(href != null && !href.isBlank(), "External link without href.");
                assertExternalLinkInNewOrSameTab(link);
            }
        }
        openBase();
    }

    @Test
    @Order(3)
    @DisplayName("Invalid form submission shows validation (HTML5 validity or error present)")
    void invalidFormSubmissionValidation() {
        openBase();
        Optional<WebElement> formOpt = findForm();
        Assumptions.assumeTrue(formOpt.isPresent(), "No form found; skipping validation test.");

        WebElement submit = findSubmitButton();
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        boolean validity = html5FormValidity();
        boolean errorShown = elementExists(By.cssSelector(".error, .alert, [role='alert'], .error-message")) ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("campo obrigatório") ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("required");
        boolean stayed = driver.getCurrentUrl().contains("/index.html") || driver.getCurrentUrl().contains("/eu-central-1/");
        Assertions.assertTrue(!validity || errorShown || stayed,
                "Submitting empty form did not show validation nor remained on the form.");
    }

    @Test
    @Order(4)
    @DisplayName("Fill and submit form successfully (success message or confirmation visible)")
    void fillAndSubmitFormSuccessfully() {
        openBase();

        // Typical CAC TAT fields
        setValue(By.id("firstName"), "John");
        setValue(By.id("lastName"), "Doe");
        setValue(By.id("email"), "john.doe@example.com");
        setValue(By.id("phone"), "11999999999");
        setValue(By.id("open-text-area"), "Mensagem de teste enviada via Selenium WebDriver.");

        // Optional dropdown (product)
        selectIfPresent(By.id("product"), "YouTube");

        // Optional checkboxes/radios
        clickIfPresent(By.id("email-checkbox"));
        clickIfPresent(By.id("phone-checkbox"));

        // Ensure form validity before submit if supported
        Assertions.assertTrue(html5FormValidity() || findForm().isEmpty(), "Form still invalid before submission.");

        WebElement submit = findSubmitButton();
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        // Success signals on CAC TAT: element with success text, or URL change
        boolean successByText = driver.findElements(By.cssSelector(".success, .success-message")).size() > 0
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("sucesso")
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("enviada")
                || driver.getPageSource().toLowerCase(Locale.ROOT).contains("success");
        boolean successByUrl = !driver.getCurrentUrl().equals(BASE_URL);
        Assertions.assertTrue(successByText || successByUrl, "No clear success indicator after form submission.");
    }

    @Test
    @Order(5)
    @DisplayName("Dropdown interactions (if any select exists)")
    void dropdownInteractionsIfPresent() {
        openBase();
        List<WebElement> selects = driver.findElements(By.cssSelector("select"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No select elements present; skipping dropdown test.");

        WebElement selectEl = selects.get(0);
        Select sel = new Select(selectEl);
        List<WebElement> options = sel.getOptions();
        Assumptions.assumeTrue(options.size() >= 2, "Not enough options to exercise dropdown.");

        String before = sel.getFirstSelectedOption().getText().trim();
        
        // Find enabled option that is not the currently selected one
        WebElement enabledOption = null;
        for (int i = options.size() - 1; i >= 0; i--) {
            if (options.get(i).isEnabled() && !options.get(i).getText().trim().equals(before)) {
                enabledOption = options.get(i);
                sel.selectByIndex(i);
                break;
            }
        }
        
        if (enabledOption != null) {
            String after1 = sel.getFirstSelectedOption().getText().trim();
            
            // Find another enabled option different from the current one
            for (int i = 0; i < options.size(); i++) {
                WebElement option = options.get(i);
                if (option.isEnabled() && !option.getText().trim().equals(after1)) {
                    sel.selectByIndex(i);
                    break;
                }
            }
            
            String after2 = sel.getFirstSelectedOption().getText().trim();
            Assertions.assertNotEquals(before, after1, "Selecting option did not change selection.");
            Assertions.assertNotEquals(after1, after2, "Selecting another option did not change selection.");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Menu/Burger actions if available (open/close; About/Reset/Logout)")
    void menuBurgerActionsIfAvailable() {
        openBase();
        By[] burgers = new By[] {
                By.cssSelector("button[aria-label*='menu' i], button[id*='menu' i], .bm-burger-button, .hamburger, .menu"),
                By.xpath("//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'menu')]")
        };
        Optional<WebElement> burger = Optional.empty();
        for (By by : burgers) {
            burger = first(by);
            if (burger.isPresent()) break;
        }
        Assumptions.assumeTrue(burger.isPresent(), "No burger/menu present; skipping.");

        wait.until(ExpectedConditions.elementToBeClickable(burger.get())).click();

        // All Items / Home
        By home = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'home') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'all items') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'início') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'inicio')]");
        if (elementExists(home)) {
            clickIfPresent(home);
            Assertions.assertEquals(hostOf(BASE_URL), hostOf(driver.getCurrentUrl()), "Home/All Items left base host unexpectedly.");
        }

        // About (external)
        By about = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'about') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sobre')]");
        if (elementExists(about)) {
            assertExternalLinkInNewOrSameTab(driver.findElement(about));
        }

        // Reset App State
        By reset = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset app state') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'limpar')]");
        if (elementExists(reset)) {
            Assertions.assertTrue(clickIfPresent(reset), "Reset App State click failed.");
        }

        // Logout
        By logout = By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sair') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log out')]");
        if (elementExists(logout)) {
            clickIfPresent(logout);
            Assertions.assertTrue(driver.getCurrentUrl().contains("/index.html") || driver.getCurrentUrl().contains("/eu-central-1/"),
                    "Logout did not return to a base/login-like page.");
        }

        // Close menu if still open
        clickIfPresent(burgers[0]);
        openBase();
    }

    @Test
    @Order(7)
    @DisplayName("Login behavior (if a login form exists)")
    void loginBehaviorIfPresent() {
        openBase();
        Optional<WebElement> user = first(By.cssSelector("input[type='email'], input[name*='user' i], input[id*='user' i], input[name*='email' i], input[id*='email' i]"));
        Optional<WebElement> pass = first(By.cssSelector("input[type='password'], input[name*='pass' i], input[id*='pass' i]"));
        Optional<WebElement> submit = Optional.empty();
        if (user.isPresent() || pass.isPresent()) {
            try {
                submit = Optional.of(findSubmitButton());
            } catch (NoSuchElementException ignored) {}
        }
        Assumptions.assumeTrue(user.isPresent() && pass.isPresent() && submit.isPresent(), "No login form detected; skipping login tests.");

        // Negative
        clearAndType(user.get(), "invalid@example.com");
        clearAndType(pass.get(), "wrong");
        wait.until(ExpectedConditions.elementToBeClickable(submit.get())).click();

        boolean error = elementExists(By.cssSelector(".error, .alert, [role='alert']")) ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("invalid") ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("erro");
        Assertions.assertTrue(error || driver.getCurrentUrl().contains("/index.html"),
                "Invalid login did not show an error or remain on login page.");

        // Positive (no credentials provided for this site)
        Assumptions.assumeTrue(false, "No valid credentials provided; positive login test skipped.");
    }
}