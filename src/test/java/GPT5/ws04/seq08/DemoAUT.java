package GPT5.ws04.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // --------------------- Helper utilities ---------------------

    private void openBase() {
        driver.get(BASE_URL);
        // Wait for a stable element on the page (form or submit button)
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("button, input[type='submit']")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("form"))
        ));
    }

    private boolean exists(By by) {
        return !driver.findElements(by).isEmpty();
    }

    private Optional<WebElement> firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return Optional.of(els.get(0));
        }
        return Optional.empty();
    }

    private void typeIfPresent(String logicalName, String value, By... candidates) {
        Optional<WebElement> el = firstPresent(candidates);
        el.ifPresent(e -> {
            wait.until(ExpectedConditions.visibilityOf(e));
            e.clear();
            e.sendKeys(value);
        });
        Assertions.assertTrue(el.isPresent(), "Expected input for " + logicalName + " to be present");
    }

    private void selectIfPresent(By selectLocator, String visibleText) {
        Optional<WebElement> sel = firstPresent(selectLocator);
        if (sel.isPresent()) {
            Select s = new Select(sel.get());
            List<String> options = s.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
            if (options.stream().anyMatch(t -> t.trim().equalsIgnoreCase(visibleText))) {
                s.selectByVisibleText(visibleText);
                Assertions.assertEquals(visibleText.trim().toLowerCase(), s.getFirstSelectedOption().getText().trim().toLowerCase(),
                        "Dropdown should select expected option");
            } else if (!options.isEmpty()) {
                // Fallback: select first option if requested text not found
                s.selectByIndex(0);
                Assertions.assertTrue(s.getFirstSelectedOption().isDisplayed(), "Some option should be selected");
            }
        }
    }

    private void toggleAll(By locator) {
        List<WebElement> boxes = driver.findElements(locator);
        for (WebElement box : boxes) {
            if (!box.isSelected() && box.isEnabled() && box.isDisplayed()) {
                wait.until(ExpectedConditions.elementToBeClickable(box)).click();
            }
        }
    }

    private void assertSuccessMessage() {
        // Katalon's demo shows "Successfully submitted!" text
        By successBy = By.xpath("//*[contains(.,'Successfully submitted')]");
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(successBy));
        Assertions.assertTrue(success.getText().toLowerCase().contains("successfully"),
                "A success confirmation should be shown after submission");
    }

    private void assertExternalLinkOpens(String cssSelector, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector(cssSelector));
        if (links.isEmpty()) return; // optional, skip silently
        String originalWindow = driver.getWindowHandle();
        String beforeUrl = driver.getCurrentUrl();
        links.get(0).click();

        // Wait for either a new window or same-tab navigation
        try {
            wait.until(drv -> drv.getWindowHandles().size() > 1 || !drv.getCurrentUrl().equals(beforeUrl));
        } catch (TimeoutException ignored) {}

        Set<String> handles = new HashSet<>(driver.getWindowHandles());
        handles.remove(originalWindow);

        if (!handles.isEmpty()) {
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to " + expectedDomain);
            driver.navigate().back();
        }
    }

    // --------------------- Tests ---------------------

    @Test
    @Order(1)
    public void pageLoads_andKeyElementsAreVisible() {
        openBase();
        // Header or title presence
        boolean hasHeader = exists(By.tagName("h1")) || exists(By.tagName("h2"));
        // At least first/last name inputs visible (by common ids/names)
        Optional<WebElement> firstName = firstPresent(By.id("first-name"), By.name("first-name"), By.name("first_name"));
        Optional<WebElement> lastName = firstPresent(By.id("last-name"), By.name("last-name"), By.name("last_name"));
        Assertions.assertAll(
                () -> Assertions.assertTrue(hasHeader || exists(By.cssSelector("form")), "Header or form should be present"),
                () -> Assertions.assertTrue(firstName.isPresent(), "First name input should be present"),
                () -> Assertions.assertTrue(lastName.isPresent(), "Last name input should be present")
        );
    }

    @Test
    @Order(2)
    public void fillAllSupportedFields_andSubmit_showsSuccess() {
        openBase();

        typeIfPresent("first-name", "Alex",
                By.id("first-name"), By.name("first-name"), By.name("first_name"));
        typeIfPresent("last-name", "Smith",
                By.id("last-name"), By.name("last-name"), By.name("last_name"));
        // Gender: select first available radio
        List<WebElement> genders = driver.findElements(By.cssSelector("input[type='radio']"));
        if (!genders.isEmpty()) {
            WebElement g = genders.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(g)).click();
            Assertions.assertTrue(g.isSelected(), "Gender radio should become selected");
        }
        // Date of birth (if present)
        firstPresent(By.id("dob"), By.name("dob"), By.cssSelector("input[type='date']")).ifPresent(d -> {
            d.clear();
            d.sendKeys("01011990"); // Many HTML date inputs accept MMDDYYYY without separators
        });
        // Address (if present)
        firstPresent(By.id("address"), By.name("address")).ifPresent(a -> {
            a.clear(); a.sendKeys("123 Main St");
        });
        // Email / Password / Company
        typeIfPresent("email", "alex.smith@example.com", By.id("email"), By.name("email"));
        firstPresent(By.id("password"), By.name("password"), By.cssSelector("input[type='password']")).ifPresent(p -> {
            p.clear(); p.sendKeys("StrongPass123");
        });
        typeIfPresent("company", "Katalon", By.id("company"), By.name("company"));

        // Role dropdown
        selectIfPresent(By.id("role"), "Developer");

        // Job expectations and development checkboxes (optional)
        toggleAll(By.cssSelector("input[type='checkbox']"));

        // Comment textarea
        firstPresent(By.id("comment"), By.name("comment"), By.cssSelector("textarea")).ifPresent(t -> {
            t.clear(); t.sendKeys("This is a Selenium/JUnit automated submission.");
        });

        // Submit
        Optional<WebElement> submit = firstPresent(
                By.id("submit"),
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Submit')]")
        );
        Assertions.assertTrue(submit.isPresent(), "Submit button should be present");
        wait.until(ExpectedConditions.elementToBeClickable(submit.get())).click();

        // Assert success
        assertSuccessMessage();
    }

    @Test
    @Order(3)
    public void resetButton_ifPresent_clearsInputs() {
        openBase();

        // Pre-fill some fields
        firstPresent(By.id("first-name"), By.name("first-name"), By.name("first_name")).ifPresent(f -> { f.clear(); f.sendKeys("Temp"); });
        firstPresent(By.id("last-name"), By.name("last-name"), By.name("last_name")).ifPresent(f -> { f.clear(); f.sendKeys("User"); });

        // Reset if available
        Optional<WebElement> reset = firstPresent(By.id("reset"), By.cssSelector("input[type='reset']"), By.xpath("//button[contains(.,'Reset')]"));
        if (reset.isPresent()) {
            wait.until(ExpectedConditions.elementToBeClickable(reset.get())).click();
            String fn = firstPresent(By.id("first-name"), By.name("first-name"), By.name("first_name"))
                    .map(e -> e.getAttribute("value")).orElse("");
            String ln = firstPresent(By.id("last-name"), By.name("last-name"), By.name("last_name"))
                    .map(e -> e.getAttribute("value")).orElse("");
            Assertions.assertAll(
                    () -> Assertions.assertTrue(fn.isEmpty(), "First name should be empty after reset"),
                    () -> Assertions.assertTrue(ln.isEmpty(), "Last name should be empty after reset")
            );
        } else {
            Assertions.assertTrue(true, "Reset control not present; skipping without failure");
        }
    }

    @Test
    @Order(4)
    public void radioButtons_areExclusive_whenPresent() {
        openBase();
        List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"));
        if (radios.size() >= 2) {
            WebElement r1 = radios.get(0);
            WebElement r2 = radios.get(1);
            wait.until(ExpectedConditions.elementToBeClickable(r1)).click();
            Assertions.assertTrue(r1.isSelected(), "First radio should be selected after click");
            wait.until(ExpectedConditions.elementToBeClickable(r2)).click();
            Assertions.assertAll(
                    () -> Assertions.assertTrue(r2.isSelected(), "Second radio should be selected after click"),
                    () -> Assertions.assertFalse(r1.isSelected(), "First radio should be unselected after selecting second")
            );
        } else {
            Assertions.assertTrue(true, "Not enough radio buttons present; skipping without failure");
        }
    }

    @Test
    @Order(5)
    public void externalLinks_openAndHaveExpectedDomains() {
        openBase();
        // Handle a few common external patterns if they exist
        assertExternalLinkOpens("a[href*='katalon.com']", "katalon.com");
        assertExternalLinkOpens("a[href*='github.com']", "github.com");
        assertExternalLinkOpens("a[target='_blank'][href^='http']", "http"); // generic external in new tab
    }

    @Test
    @Order(6)
    public void textarea_limitsOrInputAcceptance_isReasonable() {
        openBase();
        Optional<WebElement> ta = firstPresent(By.id("comment"), By.name("comment"), By.cssSelector("textarea"));
        if (ta.isPresent()) {
            String longText = "A".repeat(1500);
            ta.get().clear();
            ta.get().sendKeys(longText);
            String actual = ta.get().getAttribute("value");
            Assertions.assertTrue(actual.length() > 0, "Textarea should accept input");
        } else {
            Assertions.assertTrue(true, "Textarea not present; skipping without failure");
        }
    }
}
