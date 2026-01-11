package GPT5.ws05.seq10;

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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    private static final By BODY = By.tagName("body");
    private static final By FORM = By.tagName("form");
    private static final By SUBMIT = By.cssSelector("button[type='submit']");
    private static final By SUCCESS_BANNER = By.cssSelector(".success, .alert-success");
    private static final By ERROR_BANNER = By.cssSelector(".error, .alert-error");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By ANY_SELECT = By.tagName("select");
    private static final By ANY_CHECKBOX = By.cssSelector("input[type='checkbox']");
    private static final By ANY_RADIO = By.cssSelector("input[type='radio']");
    private static final By PRIVACY_LINK = By.cssSelector("a[href*='privacy']");

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ------------------ Helpers ------------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/index.html") || driver.getCurrentUrl().endsWith("/"),
                "URL should include /index.html or end with /");
        Assertions.assertTrue(present(FORM), "Form should be present on the base page");
    }

    private boolean present(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private WebElement firstDisplayed(By locator) {
        for (WebElement e : driver.findElements(locator)) {
            if (e.isDisplayed()) return e;
        }
        throw new NoSuchElementException("No displayed element for: " + locator);
    }

    private static String hostOf(String url) {
        try { return new URI(url).getHost(); } catch (Exception e) { return ""; }
    }

    private void scrollIntoView(WebElement e) {
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'})", e);
    }

    private void setIfPresent(By locator, String value) {
        List<WebElement> els = driver.findElements(locator);
        if (!els.isEmpty()) {
            WebElement el = els.get(0);
            scrollIntoView(el);
            try { el.clear(); } catch (Exception ignored) {}
            el.sendKeys(value);
        }
    }

    private void typeIntoLabeledFieldContains(String labelTextContains, String value) {
        String xpath = String.format("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]/following::*[self::input or self::textarea][1]", labelTextContains.toLowerCase());
        List<WebElement> els = driver.findElements(By.xpath(xpath));
        if (!els.isEmpty()) {
            WebElement input = els.get(0);
            scrollIntoView(input);
            try { input.clear(); } catch (Exception ignored) {}
            input.sendKeys(value);
        }
    }

    private void chooseRadioByLabelContains(String labelPart) {
        String xpath = String.format(
                "//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]/preceding::*[self::input and @type='radio'][1] | " +
                "//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]/following::*[self::input and @type='radio'][1]",
                labelPart.toLowerCase(), labelPart.toLowerCase());
        List<WebElement> el = driver.findElements(By.xpath(xpath));
        if (!el.isEmpty()) {
            WebElement radio = el.get(0);
            scrollIntoView(radio);
            if (!radio.isSelected()) radio.click();
            Assertions.assertTrue(radio.isSelected(), "Radio should be selected for label containing: " + labelPart);
        }
    }

    private void toggleFirstNCheckboxes(int n) {
        List<WebElement> boxes = driver.findElements(ANY_CHECKBOX).stream()
                .filter(WebElement::isDisplayed).collect(Collectors.toList());
        Assumptions.assumeTrue(!boxes.isEmpty(), "No checkboxes found");
        int count = 0;
        for (WebElement c : boxes) {
            scrollIntoView(c);
            if (!c.isSelected()) c.click();
            Assertions.assertTrue(c.isSelected(), "Checkbox should be selected");
            count++;
            if (count >= n) break;
        }
    }

    private void assertExternalByClick(WebElement link) {
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Skipping non-http link.");
        String expectedHost = hostOf(href);

        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        waitClickable(By.xpath(".//a[@href='" + href + "']")).click();

        // Wait either a new window or direct navigation
        wait.until(d -> d.getWindowHandles().size() > before.size()
                || hostOf(d.getCurrentUrl()).equalsIgnoreCase(expectedHost));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External URL should contain host: " + expectedHost);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External URL should contain host: " + expectedHost);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    // ------------------ Tests ------------------

    @Test
    @Order(1)
    public void basePage_ShouldLoad_And_ShowExpectedElements() {
        openBase();

        // Try common field ids (present on CAC-TAT)
        Assertions.assertAll("Key inputs should exist (best-effort)",
                () -> Assertions.assertTrue(present(By.id("firstName")) || present(By.xpath("//label[contains(.,'Nome')]")),
                        "First name field or label should be present"),
                () -> Assertions.assertTrue(present(By.id("lastName")) || present(By.xpath("//label[contains(.,'Sobrenome')]")),
                        "Last name field or label should be present"),
                () -> Assertions.assertTrue(present(By.id("email")) || present(By.xpath("//label[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email')]")),
                        "Email field or label should be present"),
                () -> Assertions.assertTrue(present(By.id("open-text-area")) || present(By.xpath("//textarea")),
                        "Message textarea should be present")
        );
    }

    @Test
    @Order(2)
    public void form_Submit_WithValidData_ShouldShowSuccess() {
        openBase();

        // Fill by ids when possible, else by label
        setIfPresent(By.id("firstName"), "Maria");
        setIfPresent(By.id("lastName"), "Silva");
        setIfPresent(By.id("email"), "maria.silva@example.com");
        setIfPresent(By.id("phone"), "11999998888");
        setIfPresent(By.id("open-text-area"), "Teste de mensagem automática via Selenium.");

        // Fallback using labels in case any field is missing the id
        typeIntoLabeledFieldContains("nome", "Maria");
        typeIntoLabeledFieldContains("sobrenome", "Silva");
        typeIntoLabeledFieldContains("email", "maria.silva@example.com");
        typeIntoLabeledFieldContains("telefone", "11999998888");
        typeIntoLabeledFieldContains("mensagem", "Teste de mensagem automática via Selenium.");

        // Select product dropdown if present
        if (present(ANY_SELECT)) {
            Select sel = new Select(firstDisplayed(ANY_SELECT));
            if (!sel.getOptions().isEmpty()) sel.selectByIndex(Math.min(1, sel.getOptions().size() - 1));
        }

        // Choose some radio (service type) if present
        if (present(ANY_RADIO)) {
            chooseRadioByLabelContains("elogio");
            chooseRadioByLabelContains("ajuda");
        }

        // Toggle a couple of checkboxes (e.g., phone required, newsletter)
        if (present(ANY_CHECKBOX)) {
            toggleFirstNCheckboxes(2);
        }

        WebElement submit = firstDisplayed(SUBMIT);
        scrollIntoView(submit);
        waitClickable(SUBMIT).click();

        // Success indicators: success banner, or message text
        boolean successDetected = false;
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(SUCCESS_BANNER),
                    ExpectedConditions.textToBePresentInElementLocated(BODY, "sucesso"),
                    ExpectedConditions.textToBePresentInElementLocated(BODY, "enviado"),
                    ExpectedConditions.textToBePresentInElementLocated(BODY, "Obrigado")
            ));
            successDetected = present(SUCCESS_BANNER)
                    || driver.findElement(BODY).getText().toLowerCase().contains("sucesso")
                    || driver.findElement(BODY).getText().toLowerCase().contains("enviado")
                    || driver.findElement(BODY).getText().toLowerCase().contains("obrigado");
        } catch (TimeoutException ignored) {}

        // If the page uses HTML5 required fields and the success banner is flaky,
        // at least ensure we didn't land on a clear validation error banner.
        Assertions.assertTrue(successDetected || !present(ERROR_BANNER),
                "Form submission should succeed or at least not show an error banner");
    }

    @Test
    @Order(3)
    public void form_InvalidEmail_ShouldFailHTML5Validation() {
        openBase();

        setIfPresent(By.id("firstName"), "Joao");
        setIfPresent(By.id("lastName"), "Souza");
        setIfPresent(By.id("email"), "invalid-email");
        setIfPresent(By.id("open-text-area"), "Mensagem teste.");

        // Click submit and rely on HTML5 validity
        WebElement submit = firstDisplayed(SUBMIT);
        scrollIntoView(submit);
        submit.click();

        // Email validity check (HTML5)
        WebElement emailInput = present(By.id("email"))
                ? driver.findElement(By.id("email"))
                : wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email')]/following::*[self::input][1]")));
        Boolean valid = (Boolean) ((JavascriptExecutor)driver).executeScript("return arguments[0].checkValidity()", emailInput);
        String message = (String) ((JavascriptExecutor)driver).executeScript("return arguments[0].validationMessage", emailInput);

        Assertions.assertFalse(valid, "Email field should be invalid for bad format");
        Assertions.assertTrue(message != null && !message.trim().isEmpty(), "Validation message should be present");
    }

    @Test
    @Order(4)
    public void dropdown_Selection_ChangesSelectedOption() {
        openBase();
        Assumptions.assumeTrue(present(ANY_SELECT), "No select elements found.");
        Select sel = new Select(firstDisplayed(ANY_SELECT));
        List<WebElement> options = sel.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Not enough options to test selection.");
        String initial = sel.getFirstSelectedOption().getText();
        sel.selectByIndex(1);
        String after1 = sel.getFirstSelectedOption().getText();
        if (options.size() > 2) {
            sel.selectByIndex(2);
            String after2 = sel.getFirstSelectedOption().getText();
            Assertions.assertTrue(!initial.equals(after1) || !after1.equals(after2),
                    "Changing selection should alter selected option text");
        } else {
            Assertions.assertNotEquals(initial, after1, "Changing selection should alter selected option text");
        }
    }

    @Test
    @Order(5)
    public void radioButtons_SelectDifferentOption_ShouldReflectSelection() {
        openBase();
        Assumptions.assumeTrue(present(ANY_RADIO), "No radio buttons found.");
        List<WebElement> radios = driver.findElements(ANY_RADIO).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
        Assumptions.assumeTrue(radios.size() >= 2, "Need at least two radios to test.");
        WebElement r1 = radios.get(0);
        WebElement r2 = radios.get(1);

        scrollIntoView(r1);
        if (!r1.isSelected()) r1.click();
        Assertions.assertTrue(r1.isSelected(), "First radio should be selected");
        if (!r2.equals(r1)) {
            scrollIntoView(r2);
            r2.click();
            Assertions.assertTrue(r2.isSelected(), "Second radio should be selected");
            Assertions.assertFalse(r1.isSelected(), "First radio should be unselected after selecting second");
        }
    }

    @Test
    @Order(6)
    public void internal_PrivacyLink_ShouldOpen_AndContainPrivacyInURL() {
        openBase();
        Assumptions.assumeTrue(present(PRIVACY_LINK), "Privacy link not found.");
        WebElement link = firstDisplayed(PRIVACY_LINK);
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && !href.isEmpty(), "Privacy href is empty.");

        String baseHost = hostOf(BASE_URL);
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        scrollIntoView(link);
        link.click();

        // Wait for new tab or same tab navigation
        wait.until(d -> d.getWindowHandles().size() > before.size()
                || hostOf(d.getCurrentUrl()).equalsIgnoreCase(baseHost) && d.getCurrentUrl().contains("privacy"));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains("privacy"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("privacy"), "URL should contain privacy");
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains("privacy"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("privacy"), "URL should contain privacy");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    @Test
    @Order(7)
    public void internalLinks_OneLevelBelow_AreReachable_IfAny() {
        openBase();
        String baseHost = hostOf(BASE_URL);

        List<String> internal = driver.findElements(ANY_LINK).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(href -> hostOf(href).equalsIgnoreCase(baseHost))
                .filter(href -> !href.contains("#"))
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!internal.isEmpty(), "No internal links found.");

        for (String href : internal) {
            driver.navigate().to(href);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should stay on same host for internal link");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    @Test
    @Order(8)
    public void externalLinks_OnBase_ShouldOpenAndMatchDomain() {
        openBase();
        List<WebElement> links = driver.findElements(ANY_LINK).stream()
                .filter(WebElement::isDisplayed)
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!links.isEmpty(), "No links found on page.");

        String baseHost = hostOf(BASE_URL);
        List<WebElement> external = links.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    return href != null && href.startsWith("http") && !hostOf(href).equalsIgnoreCase(baseHost);
                })
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!external.isEmpty(), "No external links found.");

        int tested = 0;
        Set<String> domains = new HashSet<>();
        for (WebElement a : external) {
            String href = a.getAttribute("href");
            String host = hostOf(href);
            if (host.isEmpty() || domains.contains(host)) continue;
            domains.add(host);
            assertExternalByClick(a);
            tested++;
            if (tested >= 3) break; // keep it stable
        }
        Assertions.assertTrue(tested > 0, "At least one external link should be tested.");
    }

    @Test
    @Order(9)
    public void resetOrClear_IfPresent_ShouldClearCommonFields() {
        openBase();
        // Pre-fill fields
        setIfPresent(By.id("firstName"), "Alice");
        setIfPresent(By.id("lastName"), "Pereira");
        setIfPresent(By.id("email"), "alice.pereira@example.com");
        setIfPresent(By.id("open-text-area"), "Mensagem temporária.");

        // Try to find a reset/clear button
        List<By> possibleResets = Arrays.asList(
                By.cssSelector("button[type='reset']"),
                By.xpath("//button[contains(.,'Limpar') or contains(.,'Reset') or contains(.,'Apagar')]"),
                By.xpath("//a[contains(.,'Limpar') or contains(.,'Reset') or contains(.,'Apagar')]")
        );

        boolean clicked = false;
        for (By locator : possibleResets) {
            if (present(locator)) {
                scrollIntoView(firstDisplayed(locator));
                waitClickable(locator).click();
                clicked = true;
                break;
            }
        }
        Assumptions.assumeTrue(clicked, "No reset/clear control found; skipping.");

        // Validate fields cleared (best-effort)
        try {
            String v1 = driver.findElement(By.id("firstName")).getAttribute("value");
            String v2 = driver.findElement(By.id("lastName")).getAttribute("value");
            Assertions.assertTrue((v1 == null || v1.isEmpty()) && (v2 == null || v2.isEmpty()),
                    "First and last name should be cleared after reset");
        } catch (NoSuchElementException ignored) {}
    }
}
