package GPT5.ws04.seq10;

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
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    private static final By BODY = By.tagName("body");
    private static final By FORM = By.tagName("form");
    private static final By SUBMIT = By.xpath("//button[@type='submit' or contains(.,'Submit') or contains(.,'Enviar')]");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By ANY_SELECT = By.tagName("select");
    private static final By ANY_CHECKBOX = By.cssSelector("input[type='checkbox']");
    private static final By ANY_RADIO = By.cssSelector("input[type='radio']");
    private static final By SUCCESS_TEXT = By.xpath("//*[contains(translate(., 'SUCCESS', 'success'),'success') or contains(translate(., 'THANK', 'thank'),'thank')]");

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
        Assertions.assertTrue(driver.getCurrentUrl().contains("/form.html"),
                "URL should include /form.html");
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

    private void typeIntoLabeledField(String labelText, String value) {
        String xpath = String.format("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]/following::*[self::input or self::textarea][1]", labelText.toLowerCase());
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'})", input);
        input.clear();
        input.sendKeys(value);
    }

    private void chooseRadioByLabelContains(String labelPart) {
        String xpath = String.format("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]/preceding::*[self::input and @type='radio'][1] | //label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]/following::*[self::input and @type='radio'][1]", labelPart.toLowerCase(), labelPart.toLowerCase());
        WebElement radio = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'})", radio);
        if (!radio.isSelected()) radio.click();
        Assertions.assertTrue(radio.isSelected(), "Radio should be selected for label containing: " + labelPart);
    }

    private void toggleFirstNCheckboxes(int n) {
        List<WebElement> boxes = driver.findElements(ANY_CHECKBOX).stream()
                .filter(WebElement::isDisplayed).collect(Collectors.toList());
        Assumptions.assumeTrue(!boxes.isEmpty(), "No checkboxes found");
        int count = 0;
        for (WebElement c : boxes) {
            ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'})", c);
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

        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(link));
        el.click();

        // Wait for either a new window or navigation to external host
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
    public void basePage_ShouldLoad_And_ShowForm() {
        openBase();
        Assertions.assertTrue(present(FORM), "Form should be present on the page");
        // Verify some visible fields by label
        String[] labels = {"first name", "last name", "email", "password"};
        for (String l : labels) {
            String xpath = String.format("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]", l);
            Assertions.assertTrue(present(By.xpath(xpath)), "Expected label containing: " + l);
        }
    }

    @Test
    @Order(2)
    public void form_Submit_WithValidData_ShouldShowSuccessOrConfirmation() {
        openBase();
        // Fill common fields using labels (robust)
        typeIntoLabeledField("first name", "John");
        typeIntoLabeledField("last name", "Doe");
        typeIntoLabeledField("address", "123 Test Street");
        typeIntoLabeledField("email", "john.doe@example.com");
        typeIntoLabeledField("password", "SuperSecret123!");
        typeIntoLabeledField("company", "Katalon QA");
        // Role (select) if present
        if (present(ANY_SELECT)) {
            Select sel = new Select(firstDisplayed(ANY_SELECT));
            if (!sel.getOptions().isEmpty()) sel.selectByIndex(Math.min(1, sel.getOptions().size()-1));
        }
        // Gender radio if present
        if (present(ANY_RADIO)) {
            chooseRadioByLabelContains("male"); // falls back if not found by label text presence
        }
        // Some checkboxes
        toggleFirstNCheckboxes(2);

        // Submit
        WebElement submitBtn = firstDisplayed(SUBMIT);
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'})", submitBtn);
        waitClickable(SUBMIT).click();

        // Assert success page/message or any confirmation keywords
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(SUCCESS_TEXT),
                    ExpectedConditions.urlContains("submit"),
                    ExpectedConditions.urlContains("success"),
                    ExpectedConditions.urlContains("thank")
            ));
        } catch (TimeoutException ignored) {}

        boolean successDetected = present(SUCCESS_TEXT)
                || driver.getCurrentUrl().toLowerCase().contains("submit")
                || driver.getCurrentUrl().toLowerCase().contains("success")
                || driver.getCurrentUrl().toLowerCase().contains("thank");

        Assertions.assertTrue(successDetected, "Form submission should show success/confirmation");
    }

    @Test
    @Order(3)
    public void form_EmailValidation_ShouldPreventSubmission_OnInvalidEmail() {
        openBase();
        typeIntoLabeledField("email", "invalid-email"); // HTML5 validation should fail on submit
        WebElement submitBtn = firstDisplayed(SUBMIT);
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'})", submitBtn);
        submitBtn.click();

        // Use HTML5 validity check via JS
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email')]/following::*[self::input][1]")));
        Boolean valid = (Boolean) ((JavascriptExecutor)driver).executeScript("return arguments[0].checkValidity()", emailInput);
        String message = (String) ((JavascriptExecutor)driver).executeScript("return arguments[0].validationMessage", emailInput);

        Assertions.assertFalse(valid, "Email field should be invalid for bad format");
        Assertions.assertTrue(message != null && !message.trim().isEmpty(), "Validation message should be present");
    }

    @Test
    @Order(4)
    public void dropdown_SortingOrSelection_ChangesSelectedOption() {
        openBase();
        Assumptions.assumeTrue(present(ANY_SELECT), "No select elements found on the form.");
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

        if (!r1.isSelected()) r1.click();
        Assertions.assertTrue(r1.isSelected(), "First radio should be selected");
        if (!r2.equals(r1)) {
            r2.click();
            Assertions.assertTrue(r2.isSelected(), "Second radio should be selected");
            Assertions.assertFalse(r1.isSelected(), "First radio should be unselected after selecting second");
        }
    }

    @Test
    @Order(6)
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
    @Order(7)
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
    @Order(8)
    public void optional_ResetOrClear_IfSuchControlExists() {
        openBase();
        // Some demo forms offer a reset button
        List<By> possibleResets = Arrays.asList(
                By.cssSelector("button[type='reset']"),
                By.xpath("//button[contains(.,'Reset') or contains(.,'Limpar') or contains(.,'Clear')]"),
                By.xpath("//a[contains(.,'Reset') or contains(.,'Limpar') or contains(.,'Clear')]")
        );
        boolean clicked = false;
        for (By locator : possibleResets) {
            if (present(locator)) {
                waitClickable(locator).click();
                clicked = true;
                break;
            }
        }
        Assumptions.assumeTrue(clicked, "No reset/clear control found; skipping.");
        // If we clicked reset, ensure fields are empty (best-effort on common fields)
        try {
            String value = driver.findElement(By.xpath("//label[contains(translate(. , 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'first name')]/following::input[1]")).getAttribute("value");
            Assertions.assertTrue(value == null || value.isEmpty(), "First name should be cleared after reset");
        } catch (NoSuchElementException ignored) {}
    }
}
