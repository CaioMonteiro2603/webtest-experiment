package GPT5.ws04.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
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

    // ================== Helpers ==================

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(d -> d.findElements(By.tagName("form")).size() > 0
                || d.findElements(By.xpath("//*[contains(.,'Katalon') or contains(.,'Form')]")).size() > 0);
    }

    private WebElement findFirst(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? null : els.get(0);
    }

    private WebElement getInputLike(String idOrNameOrPlaceholder) {
        String xp = "//input[@id='" + idOrNameOrPlaceholder + "' or @name='" + idOrNameOrPlaceholder + "' or contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + idOrNameOrPlaceholder.toLowerCase() + "')]";
        return findFirst(By.xpath(xp));
    }

    private WebElement getTextareaLike(String idOrNameOrPlaceholder) {
        String xp = "//textarea[@id='" + idOrNameOrPlaceholder + "' or @name='" + idOrNameOrPlaceholder + "' or contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + idOrNameOrPlaceholder.toLowerCase() + "')]";
        return findFirst(By.xpath(xp));
    }

    private WebElement getSelectLike(String idOrNameOrLabelText) {
        // by id or name
        WebElement sel = findFirst(By.xpath("//select[@id='" + idOrNameOrLabelText + "' or @name='" + idOrNameOrLabelText + "']"));
        if (sel != null) return sel;
        // by label text
        WebElement label = findFirst(By.xpath("//label[contains(.,'" + idOrNameOrLabelText + "')]"));
        if (label != null) {
            String forAttr = label.getAttribute("for");
            if (forAttr != null && !forAttr.isEmpty()) {
                sel = findFirst(By.id(forAttr));
                if (sel != null) return sel;
            }
            // select near the label
            sel = findFirst(By.xpath("//label[contains(.,'" + idOrNameOrLabelText + "')]/following::select[1]"));
        }
        return sel;
    }

    private void setSelectByVisibleText(WebElement select, String containsText) {
        if (select == null) return;
        List<WebElement> options = select.findElements(By.tagName("option"));
        for (WebElement opt : options) {
            if (opt.getText() != null && opt.getText().toLowerCase().contains(containsText.toLowerCase())) {
                wait.until(ExpectedConditions.elementToBeClickable(opt)).click();
                return;
            }
        }
        if (!options.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(options.get(0))).click();
        }
    }

    private void verifyAndCloseExternalLink(WebElement link) {
        String href = link.getAttribute("href");
        if (href == null || href.trim().isEmpty()) return;
        String baseDomain = deriveDomain(BASE_URL);
        String targetDomain = deriveDomain(href);
        boolean isExternal = !targetDomain.equalsIgnoreCase(baseDomain);

        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        try {
            wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],'_blank');", href);
        }

        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().startsWith(BASE_URL));
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();

        if (after.size() > before.size()) {
            for (String h : after) {
                if (!before.contains(h)) {
                    driver.switchTo().window(h);
                    if (isExternal) {
                        wait.until(ExpectedConditions.urlContains(targetDomain));
                        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                                "URL should contain external domain: " + targetDomain);
                    }
                    driver.close();
                    driver.switchTo().window(original);
                    return;
                }
            }
        } else {
            if (isExternal && !driver.getCurrentUrl().startsWith(BASE_URL)) {
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "URL should contain external domain: " + targetDomain);
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains(deriveOrigin(BASE_URL)));
            }
        }
    }

    private String deriveDomain(String url) {
        try {
            URI u = URI.create(url);
            String host = u.getHost();
            if (host == null) return url;
            String[] parts = host.split("\\.");
            if (parts.length >= 2) return parts[parts.length - 2] + "." + parts[parts.length - 1];
            return host;
        } catch (Exception e) {
            return url;
        }
    }

    private String deriveOrigin(String url) {
        try {
            URI u = URI.create(url);
            String scheme = u.getScheme() == null ? "https" : u.getScheme();
            String host = u.getHost();
            int port = u.getPort();
            return scheme + "://" + host + (port == -1 ? "" : ":" + port);
        } catch (Exception e) {
            return url;
        }
    }

    private void assertDisplayed(WebElement el, String message) {
        Assertions.assertNotNull(el, message + " (element not found)");
        Assertions.assertTrue(el.isDisplayed(), message + " (element not displayed)");
    }

    // ================== Tests ==================

    @Test
    @Order(1)
    public void testHomeLoadsAndExternalLinks() {
        goHome();
        // Assert form exists
        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("form")));
        Assertions.assertTrue(form.isDisplayed(), "Main form should be visible.");
        Assertions.assertTrue(driver.getTitle() != null, "Page title should be present.");

        // Probe some typical inputs on this demo page for stability
        WebElement firstName = getInputLike("first-name");
        if (firstName == null) firstName = getInputLike("first name");
        WebElement email = getInputLike("email");
        assertDisplayed(firstName, "First name input should be present.");
        assertDisplayed(email, "Email input should be present.");

        // External links (common on demo pages)
        List<WebElement> externals = new ArrayList<>();
        externals.addAll(driver.findElements(By.cssSelector("a[href*='katalon.com']")));
        externals.addAll(driver.findElements(By.cssSelector("a[href*='github.com']")));
        externals.addAll(driver.findElements(By.cssSelector("a[href*='twitter.com']")));
        externals.addAll(driver.findElements(By.cssSelector("a[href*='facebook.com']")));
        externals = externals.stream().distinct().collect(Collectors.toList());
        for (WebElement a : externals) {
            verifyAndCloseExternalLink(a);
        }

        Assertions.assertTrue(driver.getCurrentUrl().startsWith(deriveOrigin(BASE_URL)), "Should remain on same origin after external link checks.");
    }

    @Test
    @Order(2)
    public void testEmptySubmitShowsValidationOrPreventsSubmit() {
        goHome();
        WebElement submit = findFirst(By.cssSelector("button[type='submit'], input[type='submit'], button#submit"));
        Assertions.assertNotNull(submit, "Submit button should exist.");

        // Try submit with empty form
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        // Expect either an error helper, HTML5 validation preventing navigation, or a visible message
        boolean errorOrBlocked = false;
        try {
            wait.until(d ->
                    d.findElements(By.cssSelector(".help-block, .error, [role='alert']")).size() > 0
                            || d.findElements(By.xpath("//*[contains(.,'required') or contains(.,'obrigat') or contains(.,'Please')]")).size() > 0
                            || Objects.equals(d.getCurrentUrl(), BASE_URL) // remained on same page (blocked)
            );
            errorOrBlocked = true;
        } catch (TimeoutException ignored) {}
        Assertions.assertTrue(errorOrBlocked, "Expected validation feedback or blocked submission for empty form.");
    }

    @Test
    @Order(3)
    public void testFillAndSubmitFormSuccessfully() {
        goHome();

        // Fill text inputs (robust selectors)
        WebElement firstName = Optional.ofNullable(getInputLike("first-name")).orElse(getInputLike("first name"));
        WebElement lastName = Optional.ofNullable(getInputLike("last-name")).orElse(getInputLike("last name"));
        WebElement email = getInputLike("email");
        WebElement password = Optional.ofNullable(getInputLike("password")).orElse(getInputLike("senha"));
        WebElement company = Optional.ofNullable(getInputLike("company")).orElse(getInputLike("empresa"));
        WebElement address = Optional.ofNullable(getInputLike("address")).orElse(getInputLike("endereco"));
        WebElement dob = Optional.ofNullable(getInputLike("dob")).orElse(getInputLike("date"));

        if (firstName != null) { firstName.clear(); firstName.sendKeys("John"); }
        if (lastName != null) { lastName.clear(); lastName.sendKeys("Tester"); }
        if (email != null) { email.clear(); email.sendKeys("john.tester@example.com"); }
        if (password != null) { password.clear(); password.sendKeys("Secret123!"); }
        if (company != null) { company.clear(); company.sendKeys("Katalon QA"); }
        if (address != null) { address.clear(); address.sendKeys("123 Testing Street"); }
        if (dob != null) { dob.clear(); dob.sendKeys("1990-01-01"); }

        // Gender radio (pick first if present) - fix for obscured element
        WebElement maleRadio = findFirst(By.cssSelector("input[type='radio'][name*='gender'], input[type='radio'][id*='male'], input[type='radio'][value*='male']"));
        if (maleRadio != null) {
            wait.until(ExpectedConditions.elementToBeClickable(maleRadio));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", maleRadio);
            try {
                maleRadio.click();
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", maleRadio);
            }
        }

        // Role dropdown (select)
        WebElement roleSelect = Optional.ofNullable(getSelectLike("role")).orElse(getSelectLike("Role"));
        if (roleSelect != null) setSelectByVisibleText(roleSelect, "QA");

        // Expectation multi-select or checkboxes
        WebElement expectationSelect = Optional.ofNullable(getSelectLike("expectation")).orElse(getSelectLike("Job"));
        if (expectationSelect != null) {
            List<WebElement> options = expectationSelect.findElements(By.tagName("option"));
            if (!options.isEmpty()) wait.until(ExpectedConditions.elementToBeClickable(options.get(0))).click();
            if (options.size() > 1) wait.until(ExpectedConditions.elementToBeClickable(options.get(1))).click();
        } else {
            // checkbox fallbacks
            List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));
            if (!checks.isEmpty()) wait.until(ExpectedConditions.elementToBeClickable(checks.get(0))).click();
        }

        // Development ways (checkboxes)
        List<WebElement> devWays = driver.findElements(By.cssSelector("input[type='checkbox'][name*='development'], input[type='checkbox'][id*='development']"));
        if (!devWays.isEmpty()) wait.until(ExpectedConditions.elementToBeClickable(devWays.get(0))).click();

        // Comment
        WebElement comment = Optional.ofNullable(getTextareaLike("comment")).orElse(getTextareaLike("Comments"));
        if (comment != null) { comment.clear(); comment.sendKeys("Submitting the demo form via Selenium."); }

        // Submit
        WebElement submit = findFirst(By.cssSelector("button[type='submit'], input[type='submit'], button#submit"));
        Assertions.assertNotNull(submit, "Submit button should exist.");
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submit);
        wait.until(ExpectedConditions.elementToBeClickable(submit));
        try {
            submit.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
        }

        // Expect success message/modal
        WebElement success = wait.until(d -> {
            List<By> locs = Arrays.asList(
                    By.xpath("//*[contains(translate(.,'SUCCESS','success'),'success')]"),
                    By.cssSelector(".alert-success, .modal, .swal2-container"),
                    By.id("submit-msg")
            );
            for (By by : locs) {
                List<WebElement> found = d.findElements(by);
                if (!found.isEmpty()) return found.get(0);
            }
            return null;
        });
        Assertions.assertNotNull(success, "Expected a success confirmation after submitting the form.");

        // Close modal if any
        List<WebElement> closes = driver.findElements(By.xpath("//button[contains(.,'OK') or contains(.,'Ok') or contains(.,'Close') or contains(.,'Fechar')]"));
        if (!closes.isEmpty()) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(closes.get(0))).click();
            } catch (Exception ignored) {}
        }
    }

    @Test
    @Order(4)
    public void testDropdownOrSortingBehaviorIfPresent() {
        goHome();

        // Collect all selects and try switching an option to assert state change
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No dropdowns found on the page; skipping.");

        WebElement select = selects.get(0);
        List<WebElement> options = select.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() > 1, "Not enough options in dropdown; skipping.");

        String initiallySelected = options.stream().filter(WebElement::isSelected).map(WebElement::getText).findFirst().orElse(options.get(0).getText());
        WebElement alternative = options.stream().filter(o -> !o.getText().equals(initiallySelected)).findFirst().orElse(options.get(1));

        wait.until(ExpectedConditions.elementToBeClickable(select)).click();
        wait.until(ExpectedConditions.elementToBeClickable(alternative)).click();

        String nowSelected = select.findElements(By.tagName("option")).stream().filter(WebElement::isSelected).map(WebElement::getText).findFirst().orElse("");
        Assertions.assertNotEquals(initiallySelected, nowSelected, "Selected option should change after user interaction.");
    }

    @Test
    @Order(5)
    public void testInternalLinksOneLevelIfAny() {
        goHome();
        String origin = deriveOrigin(BASE_URL);

        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<String> visited = new ArrayList<>();

        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith(origin)) continue; // external handled elsewhere

            // One level below the page (same origin, different path)
            if (href.equals(driver.getCurrentUrl()) || visited.contains(href)) continue;

            String current = driver.getCurrentUrl();
            try {
                wait.until(ExpectedConditions.elementToBeClickable(a)).click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("window.location.href=arguments[0];", href);
            }

            try {
                wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(current)));
                Assertions.assertTrue(driver.getCurrentUrl().startsWith(origin), "Internal navigation should keep same origin.");
            } finally {
                visited.add(href);
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(current));
            }
        }
    }

    @Test
    @Order(6)
    public void testMenuBurgerIfPresent() {
        goHome();
        WebElement burger = findFirst(By.xpath("//button[contains(@class,'menu') or contains(@aria-label,'menu') or contains(.,'☰')]"));
        Assumptions.assumeTrue(burger != null, "No burger/menu button on this page; skipping.");

        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();

        WebElement about = findFirst(By.xpath("//a[contains(.,'About')]"));
        if (about != null) {
            verifyAndCloseExternalLink(about);
        }

        WebElement reset = findFirst(By.xpath("//a[contains(.,'Reset')] | //button[contains(.,'Reset')]"));
        if (reset != null) {
            wait.until(ExpectedConditions.elementToBeClickable(reset)).click();
            Assertions.assertTrue(driver.findElements(By.tagName("form")).size() > 0, "Form should remain present after Reset.");
        }

        WebElement allItems = findFirst(By.xpath("//a[contains(.,'All Items') or contains(.,'Home')]"));
        if (allItems != null) {
            wait.until(ExpectedConditions.elementToBeClickable(allItems)).click();
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(originOfCurrent()), "Expected navigation to remain on same origin after 'All Items/Home'.");
        }
    }

    private String originOfCurrent() {
        return deriveOrigin(driver.getCurrentUrl());
    }
}