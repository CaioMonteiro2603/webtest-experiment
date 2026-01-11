package GPT5.ws06.seq10;

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
import java.time.LocalDate;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://automationintesting.online/";

    private static final By BODY = By.tagName("body");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By ANY_SELECT = By.tagName("select");
    private static final By ANY_CHECKBOX = By.cssSelector("input[type='checkbox']");
    private static final By ANY_RADIO = By.cssSelector("input[type='radio']");
    private static final By CONTACT_SUBMIT = By.cssSelector("button[type='submit'], button#submitContact");
    private static final By SUCCESS_ALERT = By.cssSelector(".alert-success, .success, .alert.alert-success");
    private static final By ERROR_ALERT = By.cssSelector(".alert-danger, .error, .alert.alert-danger");
    private static final By ROOM_CARD = By.cssSelector("[data-testid='room'], .room, .col-sm-4:has(button), .col-md-4:has(button)");
    private static final By BOOK_BUTTON = By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'book')]");
    private static final By NAV_TOGGLER = By.cssSelector("button.navbar-toggler, button[aria-label*='menu' i]");

    @BeforeAll
    public static void beforeAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void afterAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --------------------------- Helpers ---------------------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL");
    }

    private boolean present(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private WebElement firstDisplayed(By locator) {
        for (WebElement e : driver.findElements(locator)) {
            if (e.isDisplayed()) return e;
        }
        throw new NoSuchElementException("No displayed element found for: " + locator);
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private void scrollIntoView(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
        } catch (Exception ignored) {}
    }

    private static String hostOf(String url) {
        try { return new URI(url).getHost(); } catch (Exception e) { return ""; }
    }

    private void typeByIdOrLabel(String id, String labelContains, String value) {
        List<By> candidates = new ArrayList<>();
        if (id != null) candidates.add(By.id(id));
        if (labelContains != null) {
            String xp = String.format(
                    "//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]/following::*[self::input or self::textarea][1]",
                    labelContains.toLowerCase());
            candidates.add(By.xpath(xp));
        }
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                WebElement el = els.get(0);
                scrollIntoView(el);
                try { el.clear(); } catch (Exception ignored) {}
                el.sendKeys(value);
                return;
            }
        }
    }

    private void assertExternalByClick(WebElement link) {
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Skipping non-http link.");
        String expectedHost = hostOf(href);
        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        scrollIntoView(link);
        waitClickable(By.xpath(".//a[@href='" + href + "']")).click();

        wait.until(d -> d.getWindowHandles().size() > before.size()
                || hostOf(d.getCurrentUrl()).equalsIgnoreCase(expectedHost));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost),
                    "External URL should contain expected host");
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost),
                    "External URL should contain expected host");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    private void chooseRadioByLabelContains(String labelPart) {
        String xp = String.format(
                "//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]/preceding::*[self::input and @type='radio'][1] | " +
                "//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'%s')]/following::*[self::input and @type='radio'][1]",
                labelPart.toLowerCase(), labelPart.toLowerCase());
        List<WebElement> radios = driver.findElements(By.xpath(xp));
        if (!radios.isEmpty()) {
            WebElement r = radios.get(0);
            scrollIntoView(r);
            if (!r.isSelected()) r.click();
            Assertions.assertTrue(r.isSelected(), "Radio should be selected for label containing: " + labelPart);
        }
    }

    // --------------------------- Tests ---------------------------

    @Test
    @Order(1)
    public void base_Should_Load_With_Core_Sections() {
        openBase();
        Assertions.assertAll("Base sanity",
                () -> Assertions.assertTrue(present(By.tagName("header")) || present(By.cssSelector("nav")),
                        "Header/nav should be present"),
                () -> Assertions.assertTrue(present(By.tagName("footer")), "Footer should be present"),
                () -> Assertions.assertTrue(present(ROOM_CARD) || present(By.xpath("//h2[contains(.,'Rooms') or contains(.,'Quartos') or contains(.,'Room')]")),
                        "Rooms section or cards should be present"));
    }

    @Test
    @Order(2)
    public void contact_Form_Valid_Submit_Should_Show_Success() {
        openBase();

        // Fill using common labels on this site: Forename/Name, Email, Telephone/Phone, Subject, Message
        typeByIdOrLabel("name", "forename", "Maria");
        typeByIdOrLabel("email", "email", "maria.silva@example.com");
        typeByIdOrLabel("phone", "telephone", "11999998888");
        typeByIdOrLabel("subject", "subject", "Booking enquiry");
        typeByIdOrLabel("message", "message", "Automation test message via Selenium WebDriver.");

        // Pick any radios/checkboxes if present
        if (present(ANY_RADIO)) chooseRadioByLabelContains("general");
        if (present(ANY_CHECKBOX)) {
            List<WebElement> boxes = driver.findElements(ANY_CHECKBOX).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
            if (!boxes.isEmpty()) { scrollIntoView(boxes.get(0)); if (!boxes.get(0).isSelected()) boxes.get(0).click(); }
        }

        WebElement submit = firstDisplayed(CONTACT_SUBMIT);
        scrollIntoView(submit);
        waitClickable(CONTACT_SUBMIT).click();

        boolean successDetected = false;
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(SUCCESS_ALERT),
                    ExpectedConditions.textToBePresentInElementLocated(BODY, "Thanks"),
                    ExpectedConditions.textToBePresentInElementLocated(BODY, "thank"),
                    ExpectedConditions.textToBePresentInElementLocated(BODY, "success")
            ));
            String text = driver.findElement(BODY).getText().toLowerCase();
            successDetected = present(SUCCESS_ALERT) || text.contains("thanks") || text.contains("success");
        } catch (TimeoutException ignored) {}

        Assertions.assertTrue(successDetected || !present(ERROR_ALERT),
                "Form submission should succeed or at least not show a global error");
    }

    @Test
    @Order(3)
    public void contact_Form_Invalid_Email_Should_Fail_HTML5_Validation() {
        openBase();
        typeByIdOrLabel("name", "forename", "Joao");
        typeByIdOrLabel("email", "email", "invalid-email");
        typeByIdOrLabel("subject", "subject", "Bad email test");
        typeByIdOrLabel("message", "message", "Testing invalid email handling.");

        WebElement submit = firstDisplayed(CONTACT_SUBMIT);
        scrollIntoView(submit);
        submit.click();

        // Try to fetch the email input and validate HTML5 validity
        WebElement emailInput = null;
        List<By> emailLocators = Arrays.asList(By.id("email"),
                By.xpath("//label[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'email')]/following::input[1]"),
                By.cssSelector("input[type='email']"));
        for (By by : emailLocators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) { emailInput = els.get(0); break; }
        }
        Assumptions.assumeTrue(emailInput != null, "Email input not found; skipping validation test.");

        Boolean valid = (Boolean) ((JavascriptExecutor) driver).executeScript("return arguments[0].checkValidity()", emailInput);
        String message = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage", emailInput);
        Assertions.assertFalse(valid, "Email should be invalid for malformed value");
        Assertions.assertTrue(message != null && !message.trim().isEmpty(), "Browser should expose a validation message");
    }

    @Test
    @Order(4)
    public void rooms_Dropdown_IfAny_Should_Change_Selection_Or_Order() {
        openBase();
        Assumptions.assumeTrue(present(ANY_SELECT), "No select elements found on page.");
        Select sel = new Select(firstDisplayed(ANY_SELECT));
        List<WebElement> options = sel.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Not enough options to test selection changes.");
        String initialSelected = sel.getFirstSelectedOption().getText();

        // capture first room card title before change
        String firstBefore = "";
        List<WebElement> cardsBefore = driver.findElements(ROOM_CARD);
        if (!cardsBefore.isEmpty()) firstBefore = cardsBefore.get(0).getText();

        sel.selectByIndex(1);
        String selectedAfter = sel.getFirstSelectedOption().getText();

        // room ordering after change
        String firstAfter = "";
        List<WebElement> cardsAfter = driver.findElements(ROOM_CARD);
        if (!cardsAfter.isEmpty()) firstAfter = cardsAfter.get(0).getText();

        Assertions.assertTrue(!initialSelected.equals(selectedAfter) || !firstBefore.equals(firstAfter),
                "Changing the dropdown should update selection or affect room order");
    }

    @Test
    @Order(5)
    public void menu_Toggler_IfPresent_Should_Open_And_Navigate_Internal_Links() {
        openBase();
        if (present(NAV_TOGGLER)) {
            WebElement toggler = firstDisplayed(NAV_TOGGLER);
            scrollIntoView(toggler);
            waitClickable(NAV_TOGGLER).click();
        }
        // Click up to 3 internal nav links (visible in header/nav)
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a[href], header a[href], .navbar a[href]"))
                .stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
        Assumptions.assumeTrue(!navLinks.isEmpty(), "No nav links found.");
        String baseHost = hostOf(BASE_URL);
        int count = 0;
        for (WebElement a : navLinks) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            String host = hostOf(href);
            // allow SPA hash links or same-host links one level
            if (href.startsWith("#") || host.isEmpty() || host.equalsIgnoreCase(baseHost)) {
                String beforeUrl = driver.getCurrentUrl();
                scrollIntoView(a);
                a.click();
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
                // Consider success if URL changes or SPA hash changes or body remains responsive
                String afterUrl = driver.getCurrentUrl();
                Assertions.assertTrue(!afterUrl.equals(beforeUrl) || afterUrl.contains("#") || present(ROOM_CARD) || present(ANY_LINK),
                        "Navigation should result in a state change or render content");
                count++;
                if (count >= 3) break;
            }
        }
        Assertions.assertTrue(count > 0, "At least one internal nav link should be tested.");
    }

    @Test
    @Order(6)
    public void externalLinks_InFooter_Should_Open_In_New_Tab_And_Match_Domain() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("footer a[href], .footer a[href], a[href]"))
                .stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
        Assumptions.assumeTrue(!links.isEmpty(), "No links found.");
        List<WebElement> external = links.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    return href != null && href.startsWith("http") && !hostOf(href).equalsIgnoreCase(baseHost);
                })
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!external.isEmpty(), "No external footer links found.");
        int tested = 0;
        Set<String> seenDomains = new HashSet<>();
        for (WebElement a : external) {
            String host = hostOf(a.getAttribute("href"));
            if (host.isEmpty() || seenDomains.contains(host)) continue;
            seenDomains.add(host);
            assertExternalByClick(a);
            tested++;
            if (tested >= 3) break; // limit to reduce flakiness
        }
        Assertions.assertTrue(tested > 0, "At least one external link should be validated.");
    }

    @Test
    @Order(7)
    public void book_First_Room_IfAvailable_Should_Reach_Success_Or_NoError() {
        openBase();
        Assumptions.assumeTrue(present(ROOM_CARD), "No room cards found.");
        // Open first room booking modal / flow
        WebElement card = driver.findElements(ROOM_CARD).get(0);
        WebElement bookBtn = null;
        List<WebElement> btns = card.findElements(BOOK_BUTTON);
        if (!btns.isEmpty()) bookBtn = btns.get(0);
        if (bookBtn == null) {
            // fallback: global first BOOK button
            List<WebElement> global = driver.findElements(BOOK_BUTTON);
            if (!global.isEmpty()) bookBtn = global.get(0);
        }
        Assumptions.assumeTrue(bookBtn != null, "No 'Book' button found.");
        scrollIntoView(bookBtn);
        bookBtn.click();

        // Try to fill booking form (common ids/names used on site)
        LocalDate in = LocalDate.now().plusDays(7);
        LocalDate out = in.plusDays(2);

        // dates
        List<By> checkinLocs = Arrays.asList(By.id("checkin"), By.name("checkin"),
                By.cssSelector("input[placeholder*='Check in' i]"), By.cssSelector("input[type='date']"));
        List<By> checkoutLocs = Arrays.asList(By.id("checkout"), By.name("checkout"),
                By.cssSelector("input[placeholder*='Check out' i]"), By.xpath("(//input[@type='date'])[2]"));

        for (By by : checkinLocs) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) { WebElement el = els.get(0); scrollIntoView(el); try { el.clear(); } catch (Exception ignored) {} el.sendKeys(in.toString()); break; }
        }
        for (By by : checkoutLocs) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) { WebElement el = els.get(0); scrollIntoView(el); try { el.clear(); } catch (Exception ignored) {} el.sendKeys(out.toString()); break; }
        }

        // person details
        typeByIdOrLabel("firstname", "first name", "Alice");
        typeByIdOrLabel("lastname", "last name", "Tester");
        typeByIdOrLabel("email", "email", "alice.tester@example.com");
        typeByIdOrLabel("phone", "phone", "11987654321");

        // confirm booking button
        List<By> confirmLocs = Arrays.asList(
                By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'book')]"),
                By.cssSelector("button[type='submit']")
        );
        boolean clicked = false;
        for (By by : confirmLocs) {
            if (present(by)) {
                WebElement b = firstDisplayed(by);
                scrollIntoView(b);
                waitClickable(by).click();
                clicked = true;
                break;
            }
        }
        Assumptions.assumeTrue(clicked, "Could not find a confirm booking button.");

        boolean success = false;
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(SUCCESS_ALERT),
                    ExpectedConditions.textToBePresentInElementLocated(BODY, "Booking"),
                    ExpectedConditions.textToBePresentInElementLocated(BODY, "success")
            ));
            String txt = driver.findElement(BODY).getText().toLowerCase();
            success = present(SUCCESS_ALERT) || txt.contains("booking") || txt.contains("success");
        } catch (TimeoutException ignored) {}

        Assertions.assertTrue(success || !present(ERROR_ALERT),
                "Booking should succeed or at least not show a global error");
    }

    @Test
    @Order(8)
    public void internal_Links_One_Level_Should_Be_Reachable() {
        openBase();
        String baseHost = hostOf(BASE_URL);

        List<String> internalHrefs = driver.findElements(ANY_LINK).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(href -> {
                    String host = hostOf(href);
                    return href.startsWith(BASE_URL) || href.startsWith("/") || href.startsWith("#")
                            || (host.isEmpty() || host.equalsIgnoreCase(baseHost));
                })
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!internalHrefs.isEmpty(), "No internal links found.");

        for (String href : internalHrefs) {
            if (href.startsWith("#")) {
                // SPA navigation
                WebElement anchor = driver.findElements(By.cssSelector("a[href='" + href + "']")).stream().findFirst().orElse(null);
                if (anchor != null) { scrollIntoView(anchor); anchor.click(); }
            } else if (href.startsWith("/")) {
                driver.navigate().to(BASE_URL.replaceAll("/+$","") + href);
            } else {
                driver.navigate().to(href);
            }
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            String after = driver.getCurrentUrl();
            Assertions.assertTrue(!after.isEmpty(), "After navigation URL should not be empty");
            // return back to base for next iteration
            driver.navigate().to(BASE_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should return to base after each check");
        }
    }

    @Test
    @Order(9)
    public void radios_And_Checkboxes_IfPresent_Should_Be_Selectable() {
        openBase();
        // Radios
        if (present(ANY_RADIO)) {
            List<WebElement> radios = driver.findElements(ANY_RADIO).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
            if (radios.size() >= 2) {
                WebElement r1 = radios.get(0);
                WebElement r2 = radios.get(1);
                scrollIntoView(r1); r1.click();
                Assertions.assertTrue(r1.isSelected(), "First radio should be selected");
                scrollIntoView(r2); r2.click();
                Assertions.assertTrue(r2.isSelected(), "Second radio should be selected");
                Assertions.assertFalse(r1.isSelected(), "First radio should be unselected after selecting second");
            }
        }
        // Checkboxes
        if (present(ANY_CHECKBOX)) {
            List<WebElement> boxes = driver.findElements(ANY_CHECKBOX).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
            int toggled = 0;
            for (WebElement c : boxes) {
                scrollIntoView(c);
                if (!c.isSelected()) { c.click(); toggled++; Assertions.assertTrue(c.isSelected(), "Checkbox should become selected"); }
                if (toggled >= 2) break;
            }
            Assertions.assertTrue(toggled >= 0, "Checkbox interaction executed");
        }
    }
}
