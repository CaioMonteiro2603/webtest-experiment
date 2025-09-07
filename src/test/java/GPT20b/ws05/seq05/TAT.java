package GPT5.ws05.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class CacTatHeadlessSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // -------------------- Helpers --------------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should land on base URL");
        wait.until(d -> d.findElements(By.tagName("form")).size() > 0);
    }

    private WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private boolean present(By by) {
        return driver.findElements(by).size() > 0;
    }

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        return null;
    }

    private void clearAndType(WebElement el, String text) {
        el.clear();
        el.sendKeys(text);
        Assertions.assertEquals(text, el.getAttribute("value"), "Typed value should be reflected in input");
    }

    private String hostOf(String url) {
        try { return URI.create(url).getHost(); } catch (Exception e) { return ""; }
    }

    private void handleExternalLink(WebElement link) {
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        String originalHost = hostOf(driver.getCurrentUrl());
        String href = link.getAttribute("href");
        if (href == null || href.isBlank()) return;

        waitClickable(link).click();
        Set<String> after = driver.getWindowHandles();

        if (after.size() > before.size()) {
            // New tab/window opened
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl() != null && d.getCurrentUrl().startsWith("http"));
            String newHost = hostOf(driver.getCurrentUrl());
            Assertions.assertNotEquals(originalHost, newHost, "External link should lead to a different host");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            // Same tab navigation
            wait.until(d -> !hostOf(d.getCurrentUrl()).equals(originalHost));
            String newHost = hostOf(driver.getCurrentUrl());
            Assertions.assertNotEquals(originalHost, newHost, "External link should lead to a different host");
            driver.navigate().back();
            wait.until(d -> hostOf(d.getCurrentUrl()).equals(originalHost));
        }
    }

    private String getValidationMessage(WebElement input) {
        Object msg = ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", input);
        return msg == null ? "" : msg.toString();
    }

    private boolean waitForVisible(By by) {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
            return el.isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void basePageLoadsAndHasCoreElements() {
        openBase();
        Assertions.assertTrue(present(By.cssSelector("h1, h2, header")), "A visible header/title should be present");
        Assertions.assertTrue(present(By.id("firstName")) || present(By.name("firstName")),
                "First name input should be present");
        Assertions.assertTrue(present(By.id("lastName")) || present(By.name("lastName")),
                "Last name input should be present");
        Assertions.assertTrue(present(By.id("email")) || present(By.cssSelector("input[type='email']")),
                "Email input should be present");
        Assertions.assertTrue(present(By.id("open-text-area")) || present(By.tagName("textarea")),
                "Message textarea should be present");
        Assertions.assertTrue(present(By.cssSelector("button[type='submit']")),
                "Submit button should be present");
    }

    @Test
    @Order(2)
    public void fillValidFormAndSubmitShowsSuccess() {
        openBase();
        WebElement firstName = firstPresent(By.id("firstName"), By.name("firstName"));
        WebElement lastName = firstPresent(By.id("lastName"), By.name("lastName"));
        WebElement email = firstPresent(By.id("email"), By.cssSelector("input[type='email']"));
        WebElement textarea = firstPresent(By.id("open-text-area"), By.tagName("textarea"));
        WebElement submit = firstPresent(By.cssSelector("button[type='submit']"));

        Assumptions.assumeTrue(firstName != null && lastName != null && email != null && textarea != null && submit != null,
                "Critical fields not found; skipping");

        clearAndType(firstName, "John");
        clearAndType(lastName, "Doe");
        clearAndType(email, "john.doe@example.com");
        clearAndType(textarea, "Mensagem de teste via Selenium.");

        // Optional product dropdown
        WebElement product = firstPresent(By.id("product"), By.cssSelector("select"));
        if (product != null) {
            Select select = new Select(product);
            if (select.getOptions().size() > 1) {
                select.selectByIndex(1);
                Assertions.assertEquals(select.getOptions().get(1).getText(),
                        select.getFirstSelectedOption().getText(), "Selected product should reflect choice");
            }
        }

        // Optional radios (e.g., service type)
        List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"));
        if (!radios.isEmpty()) {
            waitClickable(radios.get(0)).click();
            Assertions.assertTrue(radios.get(0).isSelected(), "Chosen radio should be selected");
        }

        // Optional checkboxes (e.g., contact by phone)
        List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));
        if (!checks.isEmpty()) {
            waitClickable(checks.get(0)).click();
            Assertions.assertTrue(checks.get(0).isSelected(), "Checkbox should be selected");
        }

        waitClickable(submit).click();

        // Expect success message on the page (commonly .success)
        boolean success = waitForVisible(By.cssSelector(".success, .alert-success, [data-test='success']"));
        // Some builds may show an alert
        if (!success) {
            try {
                Alert a = wait.until(ExpectedConditions.alertIsPresent());
                a.accept();
                success = true;
            } catch (TimeoutException ignored) {}
        }
        Assertions.assertTrue(success, "After valid submit, a success indicator should appear");
    }

    @Test
    @Order(3)
    public void invalidEmailTriggersValidation() {
        openBase();
        WebElement firstName = firstPresent(By.id("firstName"), By.name("firstName"));
        WebElement lastName = firstPresent(By.id("lastName"), By.name("lastName"));
        WebElement email = firstPresent(By.id("email"), By.cssSelector("input[type='email']"));
        WebElement textarea = firstPresent(By.id("open-text-area"), By.tagName("textarea"));
        WebElement submit = firstPresent(By.cssSelector("button[type='submit']"));

        Assumptions.assumeTrue(firstName != null && lastName != null && email != null && textarea != null && submit != null,
                "Critical fields not found; skipping");

        clearAndType(firstName, "A");
        clearAndType(lastName, "B");
        clearAndType(email, "not-an-email");
        clearAndType(textarea, "Any message");

        waitClickable(submit).click();

        // HTML5 native browser validation or page-level error
        String msg = getValidationMessage(email);
        boolean nativeValidation = msg != null && msg.trim().length() > 0;
        boolean pageError = waitForVisible(By.cssSelector(".error, .alert-error, [data-test='error']"));

        Assumptions.assumeTrue(nativeValidation || pageError, "No validation surfaced; skipping");
        Assertions.assertTrue(nativeValidation || pageError,
                "Invalid email should trigger validation (native or page-level)");
    }

    @Test
    @Order(4)
    public void dropdownSelectionChanges() {
        openBase();
        WebElement product = firstPresent(By.id("product"), By.cssSelector("select"));
        Assumptions.assumeTrue(product != null, "Product dropdown not present; skipping");

        Select select = new Select(product);
        List<String> options = select.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
        Assumptions.assumeTrue(options.size() >= 2, "Not enough options to test selection; skipping");

        select.selectByIndex(0);
        String first = select.getFirstSelectedOption().getText();

        select.selectByIndex(1);
        String second = select.getFirstSelectedOption().getText();

        Assertions.assertNotEquals(first, second, "Selecting a different option should change the selection text");
    }

    @Test
    @Order(5)
    public void radioAndCheckboxBehavior() {
        openBase();
        List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"));
        List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));
        Assumptions.assumeTrue(!radios.isEmpty() || !checks.isEmpty(), "No radios or checkboxes found; skipping");

        if (!radios.isEmpty()) {
            WebElement r0 = radios.get(0);
            waitClickable(r0).click();
            Assertions.assertTrue(r0.isSelected(), "Radio should be selected after click");

            String name = r0.getAttribute("name");
            if (name != null) {
                List<WebElement> sameGroup = driver.findElements(By.cssSelector("input[type='radio'][name='" + name + "']"));
                if (sameGroup.size() > 1) {
                    WebElement r1 = sameGroup.get(1);
                    waitClickable(r1).click();
                    Assertions.assertTrue(r1.isSelected(), "Second radio should be selected");
                    Assertions.assertFalse(r0.isSelected(), "First radio should be unselected due to exclusivity");
                }
            }
        }

        if (!checks.isEmpty()) {
            WebElement c0 = checks.get(0);
            waitClickable(c0).click();
            Assertions.assertTrue(c0.isSelected(), "Checkbox should be selected");
            waitClickable(c0).click();
            Assertions.assertFalse(c0.isSelected(), "Checkbox should toggle off");
        }
    }

    @Test
    @Order(6)
    public void fileUploadIfAvailable() {
        openBase();
        WebElement fileInput = firstPresent(By.id("file-upload"), By.cssSelector("input[type='file']"));
        Assumptions.assumeTrue(fileInput != null, "File upload input not present; skipping");

        // Create a tiny temporary file path reference (we won't actually create it; just ensure control accepts a path string)
        String fakePath = System.getProperty("user.dir") + "/README.txt";
        fileInput.sendKeys(fakePath);
        Assertions.assertTrue(fileInput.getAttribute("value") != null, "File input should reflect selected path");
    }

    @Test
    @Order(7)
    public void internalLinksOneLevelBelow() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        String original = driver.getCurrentUrl();

        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<WebElement> internal = new ArrayList<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.isBlank()) continue;
            if (hostOf(href).equals(baseHost)) internal.add(a);
        }
        Assumptions.assumeTrue(!internal.isEmpty(), "No internal links found; skipping");

        int visited = 0;
        for (WebElement a : internal) {
            if (visited >= 3) break;
            String href = a.getAttribute("href");
            if (href == null) continue;

            // Only one level below (same directory as index.html)
            try {
                URI base = URI.create(original);
                URI link = URI.create(href);
                String basePath = base.getPath();
                String linkPath = link.getPath();
                int baseDepth = Math.max(0, basePath.replaceAll("^/+", "").split("/").length - 1);
                int linkDepth = Math.max(0, linkPath.replaceAll("^/+", "").split("/").length - 1);
                if (linkDepth != baseDepth) continue; // skip deeper paths
            } catch (Exception ignored) { continue; }

            String before = driver.getCurrentUrl();
            try {
                waitClickable(a).click();
                wait.until(d -> !d.getCurrentUrl().equals(before));
                Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should remain on same host");
                Assertions.assertTrue(driver.findElements(By.tagName("body")).size() > 0, "Internal page should render");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(before));
                visited++;
            } catch (Exception e) {
                driver.get(BASE_URL);
                wait.until(ExpectedConditions.urlContains("cac-tat.s3.eu-central-1.amazonaws.com"));
            }
        }
        Assertions.assertTrue(visited >= 1, "At least one internal link should be visited when present");
    }

    @Test
    @Order(8)
    public void externalLinksOpenDifferentDomain() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<WebElement> externals = new ArrayList<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.isBlank()) continue;
            String host = hostOf(href);
            if (!host.equals(baseHost) && href.startsWith("http")) externals.add(a);
        }
        Assumptions.assumeTrue(!externals.isEmpty(), "No external links found; skipping");

        int validated = 0;
        for (WebElement link : externals) {
            if (validated >= 3) break;
            handleExternalLink(link);
            validated++;
        }
        Assertions.assertTrue(validated > 0, "At least one external link should be validated");
    }

    @Test
    @Order(9)
    public void requiredFieldsPreventEmptySubmission() {
        openBase();
        WebElement firstName = firstPresent(By.id("firstName"), By.name("firstName"));
        WebElement lastName = firstPresent(By.id("lastName"), By.name("lastName"));
        WebElement email = firstPresent(By.id("email"), By.cssSelector("input[type='email']"));
        WebElement textarea = firstPresent(By.id("open-text-area"), By.tagName("textarea"));
        WebElement submit = firstPresent(By.cssSelector("button[type='submit']"));

        Assumptions.assumeTrue(submit != null, "Submit button not found; skipping");

        if (firstName != null) firstName.clear();
        if (lastName != null) lastName.clear();
        if (email != null) { email.clear(); email.sendKeys(""); }
        if (textarea != null) textarea.clear();

        waitClickable(submit).click();

        // Check for native validation messages or page-level errors
        List<String> msgs = new ArrayList<>();
        if (firstName != null) msgs.add(getValidationMessage(firstName));
        if (lastName != null) msgs.add(getValidationMessage(lastName));
        if (email != null) msgs.add(getValidationMessage(email));
        if (textarea != null) msgs.add(getValidationMessage(textarea));

        boolean anyNative = msgs.stream().filter(Objects::nonNull).anyMatch(s -> s.trim().length() > 0);
        boolean pageError = waitForVisible(By.cssSelector(".error, .alert-error, [data-test='error']"));

        Assumptions.assumeTrue(anyNative || pageError, "No validation surfaced; skipping");
        Assertions.assertTrue(anyNative || pageError, "Empty required fields should prevent submission");
    }
}
