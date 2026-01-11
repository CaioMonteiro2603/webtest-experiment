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
public class TAT {

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

        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(link));
        el.click();
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
        try {
            return ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", input).toString();
        } catch (Exception e) {
            return "";
        }
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
                "Critical form elements missing; skipping test.");

        clearAndType(firstName, "John");
        clearAndType(lastName, "Doe");
        clearAndType(email, "john.doe@example.com");
        clearAndType(textarea, "Test message submitted via Selenium.");

        // Dropdown handling
        WebElement productSelect = firstPresent(By.id("product"), By.cssSelector("select"));
        if (productSelect != null) {
            try {
                Select select = new Select(productSelect);
                List<WebElement> options = select.getOptions();
                for (WebElement opt : options) {
                    if (!opt.getAttribute("disabled") && !opt.getAttribute("value").isEmpty()) {
                        select.selectByVisibleText(opt.getText());
                        break;
                    }
                }
                Assertions.assertNotNull(select.getFirstSelectedOption(), "A product should be selected.");
            } catch (UnsupportedOperationException e) {
                // Skip silently if select is read-only
            }
        }

        // Radio buttons
        List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"));
        for (WebElement r : radios) {
            if (r.isDisplayed() && r.isEnabled() && r.getAttribute("disabled") == null) {
                wait.until(ExpectedConditions.elementToBeClickable(r));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", r);
                r.click();
                break;
            }
        }

        // Checkboxes
        List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));
        for (WebElement c : checks) {
            if (c.isDisplayed() && c.isEnabled() && c.getAttribute("disabled") == null) {
                wait.until(ExpectedConditions.elementToBeClickable(c));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", c);
                if (!c.isSelected()) c.click();
                break;
            }
        }

        submit.click();

        boolean success = waitForVisible(By.cssSelector(".success, .alert-success, [data-testid='success']"));
        if (!success) {
            try {
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
                success = true;
            } catch (TimeoutException ignored) {}
        }
        Assertions.assertTrue(success, "Success indicator should appear after valid submission.");
    }

    @Test
    @Order(3)
    public void invalidEmailTriggersValidation() {
        openBase();
        WebElement email = firstPresent(By.id("email"), By.cssSelector("input[type='email']"));
        WebElement submit = firstPresent(By.cssSelector("button[type='submit']"));

        Assumptions.assumeTrue(email != null && submit != null, "Email field or submit button missing; skipping test.");

        email.clear();
        email.sendKeys("invalid-email");
        submit.click();

        String validationMessage = getValidationMessage(email);
        boolean hasPageError = waitForVisible(By.cssSelector(".error, .alert-error, [data-testid='error']"));
        Assertions.assertTrue(!validationMessage.isEmpty() || hasPageError,
                "Invalid email should trigger validation error.");
    }

    @Test
    @Order(4)
    public void dropdownSelectionChanges() {
        openBase();
        WebElement selectEl = firstPresent(By.id("product"), By.cssSelector("select"));
        Assumptions.assumeTrue(selectEl != null, "No <select> element found; skipping test.");

        Select select = new Select(selectEl);
        List<WebElement> opts = select.getOptions().stream()
                .filter(o -> o.isDisplayed() && o.isEnabled() && o.getAttribute("disabled") == null && !o.getText().isEmpty())
                .collect(Collectors.toList());
        Assumptions.assumeTrue(opts.size() >= 2, "Not enough selectable options; skipping test.");

        WebElement firstOpt = opts.get(0);
        String firstText = firstOpt.getText();
        select.selectByVisibleText(firstText);

        WebElement secondOpt = opts.get(1);
        String secondText = secondOpt.getText();
        select.selectByVisibleText(secondText);

        String selected = select.getFirstSelectedOption().getText();
        Assertions.assertEquals(secondText, selected, "Second option should now be selected.");
    }

    @Test
    @Order(5)
    public void radioAndCheckboxBehavior() {
        openBase();

        // Radios
        List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"))
                .stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
        Map<String, List<WebElement>> grouped = radios.stream()
                .collect(Collectors.groupingBy(r -> r.getAttribute("index.html:0<|tool_call_argument_begin|>"));
        for (Map.Entry<String, List<WebElement>> entry : grouped.entrySet()) {
            List<WebElement"> group = entry.getValue();
            if (group.size() < 2) continue;
            WebElement first = group.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", first);
            wait.until(ExpectedConditions.elementToBeClickable(first)).click();
            Assertions.assertTrue(first.isSelected(), "First radio should be selected.");

            WebElement second = group.get(1);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", second);
            wait.until(ExpectedConditions.elementToBeClickable(second)).click();
            Assertions.assertTrue(second.isSelected(), "Second radio should be selected.");
            Assertions.assertFalse(first.isSelected(), "First radio should be deselected.");
            break; // test one group only
        }

        // Checkboxes
        List<WebElement"> checks = driver.findElements(By.cssSelector("input[type='checkbox']"))
                .stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
        Assumptions.assumeTrue(!checks.isEmpty(), "No visible checkboxes found; skipping test.");
        WebElement c = checks.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", c);
        wait.until(ExpectedConditions.elementToBeClickable(c));
        c.click();
        Assertions.assertTrue(c.isSelected(), "Checkbox should be checked.");
        c.click();
        Assertions.assertFalse(c.isSelected(), "Checkbox should be unchecked after second click.");
    }

    @Test
    @Order(6)
    public void fileUploadIfAvailable() {
        openBase();
        WebElement fileInput = firstPresent(By.id("file-upload"), By.cssSelector("input[type='file']"));
        Assumptions.assumeTrue(fileInput != null, "File upload control not present; skipping test.");

        String fakePath = System.getProperty("user.dir") + "/src/test/resources/dummy.txt";
        fileInput.sendKeys(fakePath);
        String val = fileInput.getAttribute("value");
        Assertions.assertNotNull(val, "File input should hold selected path.");
        Assertions.assertTrue(val.contains("dummy.txt") || val.contains("fake-file"), "File name should appear in input.");
    }

    @Test
    @Order(7)
    public void internalLinksOneLevelBelow() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        String originalUrl = driver.getCurrentUrl();

        // gather internal links
        List<WebElement> links = driver.findElements(By.cssSelector("a[href]")).stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    if (href == null || href.isEmpty()) return false;
                    return hostOf(href).equals(baseHost) && href.startsWith("http");
                })
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!links.isEmpty(), "No internal links detected; skipping test.");

        int visited = 0;
        for (WebElement a : links) {
            if (visited >= 3) break;
            String href = a.getAttribute("href");
            if (href == null) continue;

            // ensure link is one level below index.html
            if (!href.contains("/index.html") && href.split("/").length > originalUrl.split("/").length) continue;

            String before = driver.getCurrentUrl();
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", a);
                wait.until(ExpectedConditions.elementToBeClickable(a)).click();
                wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(before)));
                Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should remain on same host.");
                Assertions.assertTrue(present(By.tagName("body")), "Page body should render.");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(before));
                visited++;
            } catch (Exception e) {
                driver.get(BASE_URL);
                wait.until(ExpectedConditions.urlContains("cac-tat.s3.eu-central-1.amazonaws.com"));
            }
        }
        Assertions.assertTrue(visited >= 1, "At least one internal link should be visited when present.");
    }

    @Test
    @Order(8)
    public void externalLinksOpenDifferentDomain() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        List<WebElement"> externals = driver.findElements(By.cssSelector("a[href]"))
                .stream().filter(a -> {
                    String href = a.getAttribute("href");
                    if (href == null || href.isEmpty()) return false;
                    String host = hostOf(href);
                    return !host.equals(baseHost) && href.startsWith("http");
                })
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!externals.isEmpty(), "No external links found; skipping test.");

        int validated = 0;
        for (WebElement link : externals) {
            if (validated >= 3) break;
            handleExternalLink(link);
            validated++;
        }
        Assertions.assertTrue(validated > 0, "At least one external link should be validated.");
    }

    @Test
    @Order(9)
    public void requiredFieldsPreventEmptySubmission() {
        openBase();
        WebElement firstName = firstPresent(By.id("firstName"), By.name("firstName"));
        WebElement lastName  = firstPresent(By.id("lastName"), By.name("lastName"));
        WebElement email       = firstPresent(By.id("email"), By.cssSelector("input[type='email']"));
        WebElement textarea    = firstPresent(By.id("open-text-area"), By.tagName("textarea"));
        WebElement submit      = firstPresent(By.cssSelector("button[type='submit']"));

        Assumptions.assumeTrue(submit != null, "Submit button not found; skipping test.");

        if (firstName != null) firstName.clear();
        if (lastName  != null) lastName.clear();
        if (email     != null) { email.clear(); }
        if (textarea  != null) textarea.clear();

        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        boolean nativeError = false;
        if (firstName != null && !getValidationMessage(firstName).isEmpty()) nativeError = true;
        if (lastName  != null && !getValidationMessage(lastName).isEmpty())  nativeError = true;
        if (email     != null && !getValidationMessage(email).isEmpty())    nativeError = true;
        if (textarea  != null && !getValidationMessage(textarea).isEmpty()) nativeError = true;

        boolean pageError = waitForVisible(By.cssSelector(".error, .alert-error, [data-testid='error']"));

        Assertions.assertTrue(nativeError || pageError,
                "Empty required fields should trigger validation error.");
    }
}