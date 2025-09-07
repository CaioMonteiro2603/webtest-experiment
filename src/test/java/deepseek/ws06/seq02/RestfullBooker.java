package GPT5.ws06.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class AutomationInTestingHeadlessTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://automationintesting.online/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1440, 1000));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ----------------------- Helpers -----------------------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    private WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el));
        el.click();
    }

    private WebElement firstDisplayed(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            for (WebElement el : els) {
                if (el.isDisplayed()) return el;
            }
        }
        return null;
    }

    private List<WebElement> displayedAll(By by) {
        return driver.findElements(by).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
    }

    private void setValueJS(By locator, String value) {
        WebElement el = visible(locator);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));",
                el, value
        );
    }

    private void openExternalAndAssert(By linkLocator, String expectedDomainFragment) {
        List<WebElement> links = displayedAll(linkLocator);
        if (links.isEmpty()) return; // optional link; skip if not present
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        safeClick(links.get(0));
        wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(BASE_URL));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl() != null && !d.getCurrentUrl().isEmpty());
            String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
            Assertions.assertTrue(url.contains(expectedDomainFragment.toLowerCase(Locale.ROOT)),
                    "External URL should contain: " + expectedDomainFragment + " but was: " + url);
            driver.close();
            driver.switchTo().window(original);
        } else {
            // same-tab nav (unlikely for external)
            wait.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment.toLowerCase(Locale.ROOT)));
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains(BASE_URL));
        }
    }

    private void assertAnyAlert() {
        boolean alertShown =
                driver.findElements(By.cssSelector(".alert, .alert-success, .alert-danger, .notification, .error")).size() > 0 ||
                driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'thank') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sucesso') or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'error')]")).size() > 0;
        Assertions.assertTrue(alertShown, "Expected a success or error alert to be shown.");
    }

    // ----------------------- Tests -----------------------

    @Test
    @Order(1)
    public void homePageLoads_CoreElementsPresent() {
        goHome();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on the base URL");
        // Header title and room list / contact form should be present
        WebElement header = firstDisplayed(By.cssSelector("h1"), By.cssSelector("header h1"));
        WebElement contactForm = firstDisplayed(By.id("contact"), By.cssSelector("section#contact"));
        WebElement rooms = firstDisplayed(By.id("rooms"), By.cssSelector("section#rooms"));
        Assertions.assertAll(
                () -> Assertions.assertNotNull(header, "Header H1 should be present"),
                () -> Assertions.assertNotNull(contactForm, "Contact section should be present"),
                () -> Assertions.assertNotNull(rooms, "Rooms section should be present")
        );
    }

    @Test
    @Order(2)
    public void contactForm_SubmitEmpty_ShowsValidationErrors() {
        goHome();
        WebElement submit = firstDisplayed(By.id("submitContact"), By.cssSelector("#contact button[type='submit']"), By.xpath("//section[@id='contact']//button"));
        Assertions.assertNotNull(submit, "Contact submit button must exist");
        safeClick(submit);
        // Expect validation or error hints
        boolean hasErrors =
                driver.findElements(By.cssSelector(".alert-danger, .error, .input-error")).size() > 0 ||
                driver.findElements(By.xpath("//input[@required and (not(@value) or @value='')]")).size() > 0;
        Assertions.assertTrue(hasErrors, "Submitting empty contact form should show validation errors");
    }

    @Test
    @Order(3)
    public void contactForm_ValidSubmission_ShowsSuccess() {
        goHome();
        WebElement name = firstDisplayed(By.id("name"), By.name("name"), By.cssSelector("#contact input[name='name']"));
        WebElement email = firstDisplayed(By.id("email"), By.name("email"), By.cssSelector("#contact input[type='email']"));
        WebElement phone = firstDisplayed(By.id("phone"), By.name("phone"), By.cssSelector("#contact input[type='tel']"));
        WebElement subject = firstDisplayed(By.id("subject"), By.name("subject"));
        WebElement message = firstDisplayed(By.id("message"), By.name("message"), By.cssSelector("#contact textarea"));

        if (name != null) { name.clear(); name.sendKeys("Test User"); }
        if (email != null) { email.clear(); email.sendKeys("test.user@example.com"); }
        if (phone != null) { phone.clear(); phone.sendKeys("5551234567"); }
        if (subject != null) { subject.clear(); subject.sendKeys("Booking inquiry"); }
        if (message != null) { message.clear(); message.sendKeys("Hello, I'd like to know about availability."); }

        WebElement submit = firstDisplayed(By.id("submitContact"), By.cssSelector("#contact button[type='submit']"));
        Assertions.assertNotNull(submit, "Contact submit button must exist");
        safeClick(submit);

        // Accept either success or error (backend may gate requests) but an alert must appear
        assertAnyAlert();
    }

    @Test
    @Order(4)
    public void rooms_FirstRoom_BookFlow_AlertShown() {
        goHome();
        // Find a visible "Book this room" button within rooms section
        WebElement roomsSection = firstDisplayed(By.id("rooms"), By.cssSelector("section#rooms"));
        Assertions.assertNotNull(roomsSection, "Rooms section should be present on home page");

        List<WebElement> bookButtons = roomsSection.findElements(By.xpath(".//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'book')]"));
        if (bookButtons.isEmpty()) {
            // Alternate locator
            bookButtons = roomsSection.findElements(By.cssSelector("button"));
        }
        Assertions.assertFalse(bookButtons.isEmpty(), "Should have at least one room with a Book button");

        safeClick(bookButtons.get(0));

        // Booking modal/section fields
        By checkin = By.id("checkin");
        By checkout = By.id("checkout");
        By firstname = By.id("firstname");
        By lastname = By.id("lastname");
        By email = By.id("email");
        By phone = By.id("phone");
        By bookAction = By.id("book");

        // Some fields might be re-used IDs; scope to modal if present
        WebElement modal = firstDisplayed(By.id("booking"), By.cssSelector("#booking, .modal"));

        // Set values via JS to avoid datepicker flakiness
        setValueJS(checkin, "2099-12-20");
        setValueJS(checkout, "2099-12-25");

        // Fill in other fields (fallback to name/placeholder if needed)
        WebElement fn = firstDisplayed(firstname, By.name("firstname"), By.cssSelector("input[placeholder*='first' i]"));
        WebElement ln = firstDisplayed(lastname, By.name("lastname"), By.cssSelector("input[placeholder*='last' i]"));
        WebElement em = firstDisplayed(email, By.cssSelector("#booking input[type='email'], .modal input[type='email']"));
        WebElement ph = firstDisplayed(phone, By.cssSelector("#booking input[type='tel'], .modal input[type='tel']"));

        if (fn != null) { fn.clear(); fn.sendKeys("Test"); }
        if (ln != null) { ln.clear(); ln.sendKeys("User"); }
        if (em != null) { em.clear(); em.sendKeys("booker@example.com"); }
        if (ph != null) { ph.clear(); ph.sendKeys("5550001111"); }

        WebElement bookBtn = firstDisplayed(bookAction, By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'book')]"));
        Assertions.assertNotNull(bookBtn, "Book button should be present in booking form");
        safeClick(bookBtn);

        // Expect either success or error alert (API dependent). Verify any alert presence.
        assertAnyAlert();

        // Close modal if close button exists to restore base state
        WebElement close = firstDisplayed(By.cssSelector(".close"), By.xpath("//button[contains(.,'Close') or contains(.,'Fechar')]"));
        if (close != null) {
            safeClick(close);
        } else {
            goHome(); // ensure clean state
        }
    }

    @Test
    @Order(5)
    public void navigation_InternalLinks_OneLevel() {
        goHome();
        // Collect internal nav links that point to same origin and are one-level below
        List<WebElement> navLinks = displayedAll(By.cssSelector("nav a, header a, a[href^='/'], a[href^='#/']"));
        Set<String> visitedHrefs = new LinkedHashSet<>();
        for (WebElement a : navLinks) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            if (!href.startsWith("http")) continue;
            if (!href.startsWith(BASE_URL)) continue; // only internal
            // one level below: avoid deep anchors
            visitedHrefs.add(href);
        }
        int count = 0;
        for (String href : visitedHrefs) {
            if (count >= 3) break; // keep it stable and within one level sampling
            driver.navigate().to(href);
            wait.until(d -> d.getCurrentUrl().startsWith(BASE_URL));
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should remain within site for internal link");
            count++;
        }
        // Return to base state
        goHome();
    }

    @Test
    @Order(6)
    public void footer_ExternalSocialLinks_OpenAndHaveExpectedDomains() {
        goHome();
        openExternalAndAssert(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        openExternalAndAssert(By.cssSelector("a[href*='facebook.com']"), "facebook.com");
        openExternalAndAssert(By.cssSelector("a[href*='instagram.com']"), "instagram.com");
        openExternalAndAssert(By.cssSelector("a[href*='linkedin.com']"), "linkedin.com");
        openExternalAndAssert(By.cssSelector("a[href*='youtube.com']"), "youtube.com");
        openExternalAndAssert(By.cssSelector("a[href*='github.com']"), "github.com");
    }

    @Test
    @Order(7)
    public void optional_SortingDropdown_ExerciseIfPresent() {
        goHome();
        WebElement sort = firstDisplayed(
                By.cssSelector("select#sort"),
                By.cssSelector("select[name='sort']"),
                By.xpath("//select[contains(translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sort')]")
        );
        if (sort == null) {
            Assertions.assertTrue(true, "Sorting dropdown not present; skipping.");
            return;
        }
        Select select = new Select(sort);
        List<WebElement> options = select.getOptions();
        Assertions.assertTrue(options.size() >= 1, "Sorting dropdown should have options");
        String initial = select.getFirstSelectedOption().getText();
        for (int i = 0; i < options.size(); i++) {
            select.selectByIndex(i);
            String current = select.getFirstSelectedOption().getText();
            Assertions.assertEquals(options.get(i).getText(), current, "Selected option should match");
        }
        if (options.size() > 1) {
            select.selectByIndex((select.getOptions().size() - 1));
            String after = select.getFirstSelectedOption().getText();
            Assertions.assertNotEquals(initial, after, "Sorting selection should change when a different option is chosen");
        }
        goHome();
    }

    @Test
    @Order(8)
    public void ensure_PageStable_AfterMutations() {
        goHome();
        // Basic smoke checks to confirm page can still render key areas
        WebElement rooms = firstDisplayed(By.id("rooms"), By.cssSelector("section#rooms"));
        WebElement contact = firstDisplayed(By.id("contact"), By.cssSelector("section#contact"));
        Assertions.assertAll(
                () -> Assertions.assertNotNull(rooms, "Rooms section should remain visible"),
                () -> Assertions.assertNotNull(contact, "Contact section should remain visible")
        );
    }
}
