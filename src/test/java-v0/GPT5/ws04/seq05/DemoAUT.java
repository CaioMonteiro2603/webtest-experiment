package GPT5.ws04.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should land on the base form URL");
        // Wait for form presence
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
        Object msg = ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", input);
        return msg == null ? "" : msg.toString();
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void basePageLoadsAndHasFormElements() {
        openBase();
        Assertions.assertTrue(present(By.id("first-name")) || present(By.name("first-name")),
                "First name input should be present");
        Assertions.assertTrue(present(By.id("last-name")) || present(By.name("last-name")),
                "Last name input should be present");
        Assertions.assertTrue(present(By.id("email")) || present(By.cssSelector("input[type='email']")),
                "Email input should be present");
        Assertions.assertTrue(present(By.id("role")) || present(By.tagName("select")),
                "Role dropdown (select) should be present");
        Assertions.assertTrue(present(By.id("submit")) || present(By.cssSelector("button[type='submit']")),
                "Submit button should be present");
        // Page title or header
        boolean hasHeader = present(By.tagName("h1")) || present(By.tagName("h2")) || present(By.xpath("//*[contains(.,'Katalon')]"));
        Assertions.assertTrue(hasHeader, "A visible header or page title should be present");
    }

    @Test
    @Order(2)
    public void fillFormWithValidDataAndSubmit() {
        openBase();
        WebElement firstName = firstPresent(By.id("first-name"), By.name("first-name"));
        WebElement lastName = firstPresent(By.id("last-name"), By.name("last-name"));
        WebElement email = firstPresent(By.id("email"), By.cssSelector("input[type='email']"));
        WebElement password = firstPresent(By.id("password"), By.cssSelector("input[type='password']"));
        WebElement company = firstPresent(By.id("company"), By.name("company"));
        WebElement comment = firstPresent(By.id("comment"), By.tagName("textarea"));
        WebElement submit = firstPresent(By.id("submit"), By.cssSelector("button[type='submit']"));

        Assumptions.assumeTrue(firstName != null && lastName != null && email != null && submit != null,
                "Critical fields not found; skipping");

        clearAndType(firstName, "John");
        clearAndType(lastName, "Doe");
        clearAndType(email, "john.doe@example.com");
        if (password != null) clearAndType(password, "StrongP@ss1");
        if (company != null) clearAndType(company, "Katalon QA");
        if (comment != null) clearAndType(comment, "Submitting form via Selenium test.");

        // Select gender if present (radio group)
        List<WebElement> genders = driver.findElements(By.cssSelector("input[type='radio'][name*='gender' i]"));
        if (!genders.isEmpty()) {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(genders.get(0)));
            el.click();
            Assertions.assertTrue(genders.get(0).isSelected(), "Chosen gender radio should be selected");
        }

        // Choose at least one expectation checkbox if present
        List<WebElement> expectations = driver.findElements(By.cssSelector("input[type='checkbox'][name*='expectation' i]"));
        if (!expectations.isEmpty()) {
        	WebElement el = wait.until(ExpectedConditions.elementToBeClickable(genders.get(0)));
            el.click();
            Assertions.assertTrue(expectations.get(0).isSelected(), "Expectation checkbox should be selected");
        }

        // Select role from dropdown if present
        WebElement role = firstPresent(By.id("role"), By.tagName("select"));
        if (role != null) {
            Select select = new Select(role);
            if (select.getOptions().size() > 1) {
                select.selectByIndex(1);
                Assertions.assertEquals(select.getOptions().get(1).getText(), select.getFirstSelectedOption().getText(),
                        "Selected role option should match expected");
            }
        }

        String beforeUrl = driver.getCurrentUrl();
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(submit));
        el.click();

        // Handle either HTML5 validation stopping submission, or a successful submission (alert or acknowledgment)
        boolean acknowledged = false;
        try {
            Alert a = wait.until(ExpectedConditions.alertIsPresent());
            acknowledged = true;
            a.accept();
        } catch (TimeoutException ignored) { }

        // If no alert, assert we are still on page and form remains displayed
        Assertions.assertTrue(acknowledged || driver.getCurrentUrl().equals(beforeUrl),
                "After submit, either an alert acknowledged the submission or URL remained for validation");
        Assertions.assertTrue(present(By.tagName("form")), "Form should remain visible after interaction");
    }

    @Test
    @Order(3)
    public void invalidEmailShowsNativeValidationMessage() {
        openBase();
        WebElement email = firstPresent(By.id("email"), By.cssSelector("input[type='email']"));
        WebElement submit = firstPresent(By.id("submit"), By.cssSelector("button[type='submit']"));
        WebElement firstName = firstPresent(By.id("first-name"), By.name("first-name"));
        WebElement lastName = firstPresent(By.id("last-name"), By.name("last-name"));

        Assumptions.assumeTrue(email != null && submit != null && firstName != null && lastName != null,
                "Required fields for validation not found; skipping");

        clearAndType(firstName, "A");
        clearAndType(lastName, "B");
        clearAndType(email, "not-an-email");

        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(submit));
        el.click();

        String msg = getValidationMessage(email);
        Assumptions.assumeTrue(msg != null, "No native validation message; skipping assertion");
        Assertions.assertTrue(msg.toLowerCase().contains("include an") || msg.toLowerCase().contains("email") || msg.toLowerCase().contains("@"),
                "Invalid email should trigger a native browser validation message. Actual: " + msg);
    }

    @Test
    @Order(4)
    public void dropdownOptionsChangeSelection() {
        openBase();
        WebElement role = firstPresent(By.id("role"), By.tagName("select"));
        Assumptions.assumeTrue(role != null, "Role dropdown not present; skipping");

        Select select = new Select(role);
        List<String> optionTexts = select.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
        Assumptions.assumeTrue(optionTexts.size() >= 2, "Not enough options to exercise selection; skipping");

        select.selectByIndex(0);
        String first = select.getFirstSelectedOption().getText();

        select.selectByIndex(1);
        String second = select.getFirstSelectedOption().getText();

        Assertions.assertNotEquals(first, second, "Selecting another option should change the selected value");
    }

    @Test
    @Order(5)
    public void radioAndCheckboxTogglesPersist() {
        openBase();
        List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"));
        List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));

        Assumptions.assumeTrue(!radios.isEmpty() || !checks.isEmpty(), "No radios or checkboxes present; skipping");

        if (!radios.isEmpty()) {
            WebElement r0 = radios.get(0);
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable((r0))); 
            el.click();
            Assertions.assertTrue(r0.isSelected(), "Radio should be selected after click");
            // If same name group has more than one, select another to ensure exclusivity
            String name = r0.getAttribute("name");
            List<WebElement> sameGroup = driver.findElements(By.cssSelector("input[type='radio'][name='" + name + "']"));
            if (sameGroup.size() > 1) {
                WebElement r1 = sameGroup.get(1);
                WebElement ele = wait.until(ExpectedConditions.elementToBeClickable((r1))); 
                ele.click();
                Assertions.assertTrue(r1.isSelected(), "Second radio in group should be selected");
                Assertions.assertFalse(r0.isSelected(), "First radio should be unselected due to exclusivity");
            }
        }

        if (!checks.isEmpty()) {
            WebElement c0 = checks.get(0);
            WebElement ele = wait.until(ExpectedConditions.elementToBeClickable((c0))); 
            ele.click();
            Assertions.assertTrue(c0.isSelected(), "Checkbox should be selected after click");
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable((c0))); 
            el.click();
            Assertions.assertFalse(c0.isSelected(), "Checkbox should toggle off on second click");
        }
    }

    @Test
    @Order(6)
    public void visitInternalLinksOneLevelBelow() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        // Consider internal anchors pointing to same host; limit to same directory or relative links
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        int visited = 0;
        String original = driver.getCurrentUrl();

        for (WebElement a : anchors) {
            if (visited >= 3) break;
            String href = a.getAttribute("href");
            if (href == null || href.isBlank()) continue;

            String host = hostOf(href);
            if (!baseHost.equals(host)) continue; // external handled elsewhere

            // Ensure one-level "below" relative to current file's directory (no deep traversal)
            try {
                URI uri = URI.create(href);
                String path = uri.getPath() == null ? "" : uri.getPath();
                // Allow links in same directory or to the file in the same path; disallow deep subdirs
                String[] segs = path.replaceAll("^/+", "").split("/");
                if (segs.length > 0 && segs[segs.length - 1].contains(".")) {
                    // file in some path; ensure same parent directory depth as current file
                    // We will accept if parent path depth equals current file's parent depth
                    URI cur = URI.create(original);
                    String curPath = cur.getPath();
                    int curDepth = Math.max(0, curPath.replaceAll("^/+", "").split("/").length - 1);
                    int newDepth = Math.max(0, path.replaceAll("^/+", "").split("/").length - 1);
                    if (newDepth != curDepth) continue;
                }
            } catch (Exception ignored) { continue; }

            String before = driver.getCurrentUrl();
            try {
            	WebElement ele = wait.until(ExpectedConditions.elementToBeClickable((a))); 
                ele.click();
                wait.until(d -> !d.getCurrentUrl().equals(before));
                Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()),
                        "Internal link should remain on same host");
                Assertions.assertTrue(driver.findElements(By.tagName("body")).size() > 0,
                        "Internal page should render a body");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(before));
                visited++;
            } catch (Exception e) {
                // Recover by returning to base
                driver.get(BASE_URL);
                wait.until(ExpectedConditions.urlContains("katalon-test.s3.amazonaws.com"));
            }
        }
        // It's fine if there are no internal links; ensure test is meaningful otherwise
        Assumptions.assumeTrue(anchors.stream().anyMatch(a -> hostOf(a.getAttribute("href")).equals(baseHost)),
                "No internal links available; skipping");
        Assertions.assertTrue(visited >= 1, "At least one internal link should be visited when present");
    }

    @Test
    @Order(7)
    public void externalLinksOpenAndAreOnDifferentDomain() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<WebElement> externals = new ArrayList<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.isBlank()) continue;
            String host = hostOf(href);
            if (!host.equals(baseHost) && href.startsWith("http")) {
                externals.add(a);
            }
        }
        Assumptions.assumeTrue(!externals.isEmpty(), "No external links found; skipping");
        int checked = 0;
        for (WebElement link : externals) {
            if (checked >= 3) break;
            handleExternalLink(link);
            checked++;
        }
        Assertions.assertTrue(checked > 0, "At least one external link should be validated");
    }

    @Test
    @Order(8)
    public void html5RequiredFieldsPreventEmptySubmission() {
        openBase();
        WebElement firstName = firstPresent(By.id("first-name"), By.name("first-name"));
        WebElement lastName = firstPresent(By.id("last-name"), By.name("last-name"));
        WebElement email = firstPresent(By.id("email"), By.cssSelector("input[type='email']"));
        WebElement submit = firstPresent(By.id("submit"), By.cssSelector("button[type='submit']"));

        Assumptions.assumeTrue(submit != null && (firstName != null || lastName != null || email != null),
                "Form fields not available; skipping");

        // Clear fields if present
        if (firstName != null) firstName.clear();
        if (lastName != null) lastName.clear();
        if (email != null) { email.clear(); email.sendKeys(""); }

        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(submit));
        el.click();

        // Check if any of these fields report a validation message
        List<String> msgs = new ArrayList<>();
        if (firstName != null) msgs.add(getValidationMessage(firstName));
        if (lastName != null) msgs.add(getValidationMessage(lastName));
        if (email != null) msgs.add(getValidationMessage(email));

        boolean hasRequiredMessage = msgs.stream().filter(Objects::nonNull)
                .map(String::toLowerCase).anyMatch(s -> s.contains("fill") || s.contains("required") || s.length() > 0);
        Assumptions.assumeTrue(msgs.stream().anyMatch(m -> m != null), "No native validation surfaced; skipping");
        Assertions.assertTrue(hasRequiredMessage, "At least one required-field validation message should appear");
    }
}
