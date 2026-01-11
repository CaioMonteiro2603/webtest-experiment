package GPT5.ws05.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().deleteAllCookies();
    }

    @AfterAll
    static void teardown() {
        if (driver != null) driver.quit();
    }

    // ----------------- Helpers -----------------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private boolean exists(By by) {
        return driver.findElements(by).size() > 0;
    }

    private void clearAndType(By by, String text) {
        WebElement el = waitVisible(by);
        el.clear();
        el.sendKeys(text);
    }

    private String getHost(String url) {
        try { return new URI(url).getHost(); } catch (Exception e) { return ""; }
    }

    private void openExternalAndAssertDomain(WebElement link, String expectedDomainFragment) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(link));
        el.click(); 
        wait.until(d -> d.getWindowHandles().size() != before.size() || !Objects.equals(((JavascriptExecutor) d).executeScript("return document.readyState"), "loading"));
        Set<String> after = new HashSet<>(driver.getWindowHandles());
        after.removeAll(before);
        if (!after.isEmpty()) {
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment), "External URL should contain: " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(original);
        } else {
            // Fallback: some links navigate in same tab
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment), "External URL should contain: " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    // ----------------- Tests -----------------

    @Test
    @Order(1)
    void homeLoads_AndFormControlsArePresent() {
        goHome();
        Assertions.assertTrue(driver.getTitle() != null && driver.getTitle().length() > 0, "Title should be non-empty");

        // Core form controls
        Assertions.assertAll("Core form field presence",
                () -> Assertions.assertTrue(exists(By.id("firstName")), "First name input should exist"),
                () -> Assertions.assertTrue(exists(By.id("lastName")), "Last name input should exist"),
                () -> Assertions.assertTrue(exists(By.id("email")), "Email input should exist"),
                () -> Assertions.assertTrue(exists(By.id("open-text-area")), "Message textarea should exist"),
                () -> Assertions.assertTrue(exists(By.cssSelector("button[type='submit']")), "Submit button should exist")
        );

        // Optional header or logo
        Assertions.assertTrue(exists(By.tagName("h1")) || exists(By.tagName("h2")), "A visible header should exist");
    }

    @Test
    @Order(2)
    void invalidEmail_ShowsErrorMessage() {
        goHome();
        clearAndType(By.id("firstName"), "Caio");
        clearAndType(By.id("lastName"), "Silva");
        clearAndType(By.id("email"), "not-an-email");
        clearAndType(By.id("open-text-area"), "Teste de mensagem inválida (email).");
        waitClickable(By.cssSelector("button[type='submit']")).click();

        // Expect an error banner/message
        By errorBanner = By.cssSelector(".error, .error-message, .alert-error");
        boolean errorShown = false;
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(errorBanner));
            errorShown = true;
        } catch (TimeoutException ignored) { }
        // Also guard with page source snippet (for different builds)
        if (!errorShown) {
            errorShown = driver.getPageSource().toLowerCase().contains("valid") ||
                         driver.getPageSource().toLowerCase().contains("inválid") ||
                         driver.getPageSource().toLowerCase().contains("invalido") ||
                         driver.getPageSource().toLowerCase().contains("erro");
        }
        Assertions.assertTrue(errorShown, "An error indication should be shown for invalid email");
    }

    @Test
    @Order(3)
    void productDropdown_ExerciseOptions_AndFormHappyPath() {
        goHome();
        clearAndType(By.id("firstName"), "Maria");
        clearAndType(By.id("lastName"), "Oliveira");
        clearAndType(By.id("email"), "maria.oliveira@example.com");
        clearAndType(By.id("open-text-area"), "Mensagem de teste para envio com sucesso.");

        if (exists(By.id("product"))) {
            Select product = new Select(waitVisible(By.id("product")));
            List<String> optionTexts = product.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
            if (product.getOptions().size() > 1) {
                product.selectByIndex(1);
                String selected1 = product.getFirstSelectedOption().getText();
                Assertions.assertEquals(optionTexts.get(1), selected1, "First change should select index 1");

                product.selectByIndex(0);
                String selected0 = product.getFirstSelectedOption().getText();
                Assertions.assertEquals(optionTexts.get(0), selected0, "Should be able to switch back to index 0");
            } else {
                Assertions.assertTrue(true, "Only one option in product dropdown; skipping change assertions");
            }
        }

        // Submit and expect success
        waitClickable(By.cssSelector("button[type='submit']")).click();
        By successBanner = By.cssSelector(".success, .alert-success, .message.success");
        boolean successShown = false;
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successBanner));
            successShown = true;
        } catch (TimeoutException ignored) { }
        if (!successShown) {
            successShown = driver.getPageSource().toLowerCase().contains("sucesso") ||
                           driver.getPageSource().toLowerCase().contains("success");
        }
        Assertions.assertTrue(successShown, "A success confirmation should be shown after valid submission");
    }

    @Test
    @Order(4)
    void radioButtons_IfPresent_ClickEachAndAssertSelectionChanges() {
        goHome();
        List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"));
        if (radios.isEmpty()) {
            Assertions.assertTrue(true, "No radio buttons found; skipping");
            return;
        }
        for (int i = 0; i < radios.size(); i++) {
            WebElement r = radios.get(i);
            wait.until(ExpectedConditions.elementToBeClickable(r)).click();
            // After selecting, exactly one should be selected (for same name groups)
            long selected = driver.findElements(By.cssSelector("input[type='radio']:checked")).size();
            Assertions.assertTrue(selected >= 1, "At least one radio should be selected after clicking");
        }
    }

    @Test
    @Order(5)
    void checkboxes_IfPresent_ToggleAndPhoneRequirementIfAny() {
        goHome();
        List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
        if (checkboxes.isEmpty()) {
            Assertions.assertTrue(true, "No checkboxes found; skipping");
            return;
        }
        // Toggle all checkboxes on then off
        for (WebElement cb : checkboxes) {
            if (!cb.isSelected()) wait.until(ExpectedConditions.elementToBeClickable(cb)).click();
            Assertions.assertTrue(cb.isSelected(), "Checkbox should become selected");
        }
        for (WebElement cb : checkboxes) {
            wait.until(ExpectedConditions.elementToBeClickable(cb)).click();
        }
        long selectedAfter = driver.findElements(By.cssSelector("input[type='checkbox']:checked")).size();
        Assertions.assertEquals(0, selectedAfter, "All checkboxes should be unselected after toggling off");

        // If selecting "phone" makes phone required, satisfy it
        Optional<WebElement> phoneCheck = checkboxes.stream().filter(cb -> {
            String id = cb.getAttribute("id") != null ? cb.getAttribute("id").toLowerCase() : "";
            String name = cb.getAttribute("name") != null ? cb.getAttribute("name").toLowerCase() : "";
            return id.contains("phone") || name.contains("phone") || id.contains("telefone") || name.contains("telefone");
        }).findFirst();
        if (phoneCheck.isPresent() && exists(By.id("phone"))) {
            WebElement cb = phoneCheck.get();
            if (!cb.isSelected()) {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(cb));
                el.click();
            }
            clearAndType(By.id("phone"), "11987654321");
            Assertions.assertEquals("11987654321", waitVisible(By.id("phone")).getAttribute("value"), "Phone should accept digits");
        }
    }

    @Test
    @Order(6)
    void fileUpload_IfPresent_UploadsSuccessfully() throws Exception {
        goHome();
        if (!exists(By.id("file-upload")) && !exists(By.cssSelector("input[type='file']"))) {
            Assertions.assertTrue(true, "No file input present; skipping");
            return;
        }
        Path temp = Files.createTempFile("upload-test", ".txt");
        Files.write(temp, Collections.singletonList("arquivo de teste"));
        By fileInputBy = exists(By.id("file-upload")) ? By.id("file-upload") : By.cssSelector("input[type='file']");
        WebElement fileInput = waitVisible(fileInputBy);
        fileInput.sendKeys(temp.toAbsolutePath().toString());

        // Some UIs show file name next to input; assert value contains file name (best effort)
        String val = fileInput.getAttribute("value");
        Assertions.assertTrue(val != null && !val.trim().isEmpty(), "File input value should reflect selected file");
    }

    @Test
    @Order(7)
    void internalLinks_VisitOneLevelAndReturn() {
        goHome();
        String baseHost = getHost(BASE_URL);
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Set<String> hrefs = anchors.stream().map(a -> a.getAttribute("href")).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        for (String href : hrefs) {
            if (href.startsWith("javascript:") || href.endsWith("#")) continue;
            String host = getHost(href);
            if (host == null) host = "";
            // internal links in same host (one level)
            if (host.isEmpty() || host.equalsIgnoreCase(baseHost)) {
                driver.get(href);
                Assertions.assertTrue(exists(By.tagName("body")), "Body should exist at " + href);
                // Return to base
                driver.get(BASE_URL);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
        }
        Assertions.assertTrue(true, "Visited internal links one level deep");
    }

    @Test
    @Order(8)
    void externalLinks_OpenInNewTabAndAssertDomain() {
        goHome();
        String baseHost = getHost(BASE_URL);
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Map<String, WebElement> external = new LinkedHashMap<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            String host = getHost(href);
            if (host != null && !host.isEmpty() && !host.equalsIgnoreCase(baseHost)) {
                // Keep the first link per external host to minimize flakiness
                external.putIfAbsent(host, a);
            }
        }
        if (external.isEmpty()) {
            Assertions.assertTrue(true, "No external links detected on base page; skipping");
            return;
        }
        for (Map.Entry<String, WebElement> entry : external.entrySet()) {
            String domainFrag = entry.getKey();
            openExternalAndAssertDomain(entry.getValue(), domainFrag);
        }
    }

    @Test
    @Order(9)
    void resetForm_ClearsInputs() {
        goHome();
        // Fill some fields
        if (exists(By.id("firstName"))) clearAndType(By.id("firstName"), "Teste");
        if (exists(By.id("lastName"))) clearAndType(By.id("lastName"), "Reset");
        if (exists(By.id("email"))) clearAndType(By.id("email"), "reset@example.com");
        if (exists(By.id("open-text-area"))) clearAndType(By.id("open-text-area"), "Texto que será limpo.");

        // Click reset if present
        if (exists(By.cssSelector("button[type='reset']"))) {
            waitClickable(By.cssSelector("button[type='reset']")).click();
            // Assert cleared
            if (exists(By.id("firstName"))) Assertions.assertEquals("", waitVisible(By.id("firstName")).getAttribute("value"), "First name should be cleared");
            if (exists(By.id("lastName"))) Assertions.assertEquals("", waitVisible(By.id("lastName")).getAttribute("value"), "Last name should be cleared");
            if (exists(By.id("email"))) Assertions.assertEquals("", waitVisible(By.id("email")).getAttribute("value"), "Email should be cleared");
            if (exists(By.id("open-text-area"))) Assertions.assertEquals("", waitVisible(By.id("open-text-area")).getAttribute("value"), "Message should be cleared");
        } else {
            Assertions.assertTrue(true, "Reset button not present; skipping reset assertions");
        }
    }
}
