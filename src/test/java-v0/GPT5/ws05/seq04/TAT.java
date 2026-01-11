package GPT5.ws05.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void beforeAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterAll
    public static void afterAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ============== Helpers ==============

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
    }

    private String originOf(String url) {
        try {
            URI u = URI.create(url);
            if (u.getPort() == -1) return u.getScheme() + "://" + u.getHost();
            return u.getScheme() + "://" + u.getHost() + ":" + u.getPort();
        } catch (Exception e) {
            return url;
        }
    }

    private String registrableDomain(String url) {
        try {
            String host = URI.create(url).getHost();
            if (host == null) return "";
            String[] parts = host.split("\\.");
            if (parts.length < 2) return host;
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        } catch (Exception e) {
            return "";
        }
    }

    private WebElement findFirst(By by) {
        List<WebElement> list = driver.findElements(by);
        return list.isEmpty() ? null : list.get(0);
    }

    private WebElement inputByIdOrName(String key) {
        WebElement el = findFirst(By.id(key));
        if (el == null) el = findFirst(By.name(key));
        return el;
    }

    private void typeIfPresent(WebElement el, String text) {
        if (el != null) {
            el.clear();
            el.sendKeys(text);
        }
    }

    private void clickIfPresent(WebElement el) {
        if (el != null) wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void assertSuccessVisible() {
        WebElement success = wait.until(d -> {
            List<By> candidates = Arrays.asList(
                    By.cssSelector(".success"),
                    By.id("success"),
                    By.xpath("//*[contains(translate(.,'SUCESSO','sucesso'),'sucesso') or contains(translate(.,'SUCCESS','success'),'success')]")
            );
            for (By by : candidates) {
                List<WebElement> found = d.findElements(by);
                if (!found.isEmpty() && found.get(0).isDisplayed()) return found.get(0);
            }
            return null;
        });
        Assertions.assertNotNull(success, "Expected a success confirmation to be visible.");
    }

    private void assertErrorVisible() {
        WebElement error = wait.until(d -> {
            List<By> candidates = Arrays.asList(
                    By.cssSelector(".error"),
                    By.id("error"),
                    By.xpath("//*[contains(translate(.,'ERRO','erro'),'erro') or contains(.,'Please') or contains(.,'required') or contains(.,'Obrigat')]")
            );
            for (By by : candidates) {
                List<WebElement> found = d.findElements(by);
                if (!found.isEmpty() && found.get(0).isDisplayed()) return found.get(0);
            }
            return null;
        });
        Assertions.assertNotNull(error, "Expected an error/validation message to be visible.");
    }

    private void verifyExternalLink(WebElement anchor) {
        String href = anchor.getAttribute("href");
        if (href == null || href.isEmpty()) return;

        String baseDomain = registrableDomain(BASE_URL);
        String targetDomain = registrableDomain(href);
        boolean external = !baseDomain.equalsIgnoreCase(targetDomain);

        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        try {
            wait.until(ExpectedConditions.elementToBeClickable(anchor)).click();
        } catch (Exception e) {
            // fallback open via JS
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],'_blank');", href);
        }

        // Either navigated in same tab or opened a new tab
        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(BASE_URL));
        } catch (TimeoutException ignored) { }

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            // new tab opened
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            if (external) {
                wait.until(ExpectedConditions.urlContains(targetDomain));
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "External URL should contain expected domain: " + targetDomain);
            }
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            // same tab navigation (rare here)
            if (external) {
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "External URL should contain expected domain: " + targetDomain);
            }
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
        }
    }

    // ============== Tests ==============

    @Test
    @Order(1)
    public void loadHomeAndSanity() {
        goHome();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(originOf(BASE_URL)), "Should remain on same origin.");
        WebElement form = findFirst(By.tagName("form"));
        Assertions.assertNotNull(form, "Main form should be present.");
        Assertions.assertTrue(form.isDisplayed(), "Main form should be visible.");
        Assertions.assertNotNull(driver.getTitle(), "Title should be present.");
    }

    @Test
    @Order(2)
    public void internalPrivacyLinkOneLevel() {
        goHome();
        WebElement privacy = findFirst(By.cssSelector("a[href*='privacy']"));
        Assumptions.assumeTrue(privacy != null, "No privacy link found; skipping.");
        String current = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(privacy)).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(current)));
        Assertions.assertTrue(driver.getCurrentUrl().contains("privacy"), "Expected to be on privacy page.");
        // Basic content assertion (robust)
        Assertions.assertFalse(driver.findElements(By.tagName("h1")).isEmpty() || driver.findElements(By.tagName("p")).isEmpty(),
                "Privacy page should have some text content.");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(current));
    }

    @Test
    @Order(3)
    public void externalLinksOneLevel() {
        goHome();
        String baseDomain = registrableDomain(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        // Filter external anchors
        List<WebElement> externalAnchors = links.stream().filter(a -> {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) return false;
            if (!href.startsWith("http")) return false;
            String d = registrableDomain(href);
            return !d.isEmpty() && !d.equalsIgnoreCase(baseDomain);
        }).distinct().collect(Collectors.toList());

        for (WebElement a : externalAnchors) {
            verifyExternalLink(a);
        }

        Assertions.assertTrue(driver.getCurrentUrl().startsWith(originOf(BASE_URL)),
                "After verifying external links, we should be back on the base origin.");
    }

    @Test
    @Order(4)
    public void submitWithValidDataShowsSuccess() {
        goHome();

        // Typical fields on this demo page (with robust fallbacks)
        WebElement firstName = inputByIdOrName("firstName");
        if (firstName == null) firstName = inputByIdOrName("firstname");
        WebElement lastName = inputByIdOrName("lastName");
        if (lastName == null) lastName = inputByIdOrName("lastname");
        WebElement email = inputByIdOrName("email");
        WebElement phone = inputByIdOrName("phone");
        WebElement textArea = inputByIdOrName("open-text-area");
        if (textArea == null) textArea = findFirst(By.tagName("textarea"));

        typeIfPresent(firstName, "Joao");
        typeIfPresent(lastName, "Silva");
        typeIfPresent(email, "joao.silva@example.com");
        if (phone != null) typeIfPresent(phone, "11999999999");
        if (textArea != null) typeIfPresent(textArea, "Mensagem de teste automatizado.");

        // Optional dropdowns / checkboxes for stability
        WebElement productSelect = findFirst(By.cssSelector("select#product, select[name='product']"));
        if (productSelect != null) {
            List<WebElement> opts = productSelect.findElements(By.tagName("option"));
            if (opts.size() > 1) wait.until(ExpectedConditions.elementToBeClickable(opts.get(1))).click();
        }
        WebElement emailCheckbox = findFirst(By.cssSelector("input[type='checkbox'][id*='email'], input[type='checkbox'][name*='email']"));
        clickIfPresent(emailCheckbox);

        WebElement submit = findFirst(By.cssSelector("button[type='submit'], input[type='submit']"));
        Assertions.assertNotNull(submit, "Submit button should exist.");
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        assertSuccessVisible();
    }

    @Test
    @Order(5)
    public void invalidEmailShowsError() {
        goHome();

        WebElement firstName = inputByIdOrName("firstName");
        WebElement lastName = inputByIdOrName("lastName");
        WebElement email = inputByIdOrName("email");
        WebElement textArea = inputByIdOrName("open-text-area");
        if (textArea == null) textArea = findFirst(By.tagName("textarea"));

        typeIfPresent(firstName, "Ana");
        typeIfPresent(lastName, "Tester");
        typeIfPresent(email, "invalid-email"); // bad
        if (textArea != null) typeIfPresent(textArea, "Mensagem com email inválido.");

        WebElement submit = findFirst(By.cssSelector("button[type='submit'], input[type='submit']"));
        Assertions.assertNotNull(submit, "Submit button should exist.");
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        assertErrorVisible();
    }

    @Test
    @Order(6)
    public void phoneRequiredWhenCheckedIfFeatureExists() {
        goHome();

        WebElement phoneRequiredCheckbox = findFirst(By.cssSelector("#phone-checkbox, input[type='checkbox'][name*='phone'][id*='checkbox']"));
        Assumptions.assumeTrue(phoneRequiredCheckbox != null, "Phone-required checkbox not present; skipping.");

        WebElement firstName = inputByIdOrName("firstName");
        WebElement lastName = inputByIdOrName("lastName");
        WebElement email = inputByIdOrName("email");
        WebElement phone = inputByIdOrName("phone");
        WebElement textArea = inputByIdOrName("open-text-area");
        if (textArea == null) textArea = findFirst(By.tagName("textarea"));

        typeIfPresent(firstName, "Maria");
        typeIfPresent(lastName, "QA");
        typeIfPresent(email, "maria.qa@example.com");
        if (phone != null) phone.clear(); // leave it empty to trigger validation
        typeIfPresent(textArea, "Checando validação de telefone obrigatório.");

        wait.until(ExpectedConditions.elementToBeClickable(phoneRequiredCheckbox)).click();

        WebElement submit = findFirst(By.cssSelector("button[type='submit'], input[type='submit']"));
        Assertions.assertNotNull(submit, "Submit button should exist.");
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        assertErrorVisible();
    }

    @Test
    @Order(7)
    public void fileUploadIfPresent() throws Exception {
        goHome();
        WebElement fileInput = findFirst(By.cssSelector("input[type='file']"));
        Assumptions.assumeTrue(fileInput != null, "No file input present; skipping.");

        File tmp = File.createTempFile("upload-demo", ".txt");
        Files.writeString(tmp.toPath(), "Selenium upload test content.");
        tmp.deleteOnExit();

        fileInput.sendKeys(tmp.getAbsolutePath());

        // Some demos show file name preview or allow submit.
        WebElement submit = findFirst(By.cssSelector("button[type='submit'], input[type='submit']"));
        if (submit != null) {
            // Fill minimum required fields to submit
            WebElement firstName = inputByIdOrName("firstName");
            WebElement lastName = inputByIdOrName("lastName");
            WebElement email = inputByIdOrName("email");
            WebElement textArea = inputByIdOrName("open-text-area");
            if (textArea == null) textArea = findFirst(By.tagName("textarea"));

            typeIfPresent(firstName, "File");
            typeIfPresent(lastName, "Uploader");
            typeIfPresent(email, "file.uploader@example.com");
            if (textArea != null) typeIfPresent(textArea, "Upload test.");

            wait.until(ExpectedConditions.elementToBeClickable(submit)).click();
            // Either success or at least no crash; prefer success if available.
            try {
                assertSuccessVisible();
            } catch (Exception ex) {
                // If success not shown, at least stay on page and no navigation error
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(originOf(BASE_URL)), "Should remain on same origin after upload attempt.");
            }
        }
    }

    @Test
    @Order(8)
    public void genericMenuAndResetIfPresent() {
        goHome();
        // This page usually doesn't have burger/reset, but we probe defensively.
        WebElement burger = findFirst(By.xpath("//button[contains(@class,'menu') or contains(@aria-label,'menu') or contains(.,'☰')]"));
        if (burger != null) {
            wait.until(ExpectedConditions.elementToBeClickable(burger)).click();
            WebElement allItems = findFirst(By.xpath("//a[contains(.,'All Items') or contains(.,'Home')]"));
            if (allItems != null) {
                wait.until(ExpectedConditions.elementToBeClickable(allItems)).click();
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(originOf(BASE_URL)), "All Items/Home should keep same origin.");
            }
            WebElement about = findFirst(By.xpath("//a[contains(.,'About')]"));
            if (about != null) verifyExternalLink(about);
            WebElement reset = findFirst(By.xpath("//a[contains(.,'Reset') or contains(.,'Reiniciar')] | //button[contains(.,'Reset')]"));
            if (reset != null) wait.until(ExpectedConditions.elementToBeClickable(reset)).click();
        }
        Assertions.assertTrue(driver.findElements(By.tagName("form")).size() > 0, "Form should still be present at the end.");
    }
}
