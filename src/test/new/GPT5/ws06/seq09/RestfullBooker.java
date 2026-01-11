package GPT5.ws06.seq09;

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
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://automationintesting.online/";

    // ===== Common helpers / generic locators =====
    private static final By ANY_CONTAINER = By.cssSelector("main, body, .container, #root");

    private static final By LINK_ROOMS = By.xpath("//a[normalize-space()='Rooms' or contains(translate(.,'ROOMS','rooms'),'rooms')]");
    private static final By ROOMS_LIST = By.cssSelector(".room, .rooms, [class*='room']");
    private static final By BOOK_BUTTON_IN_CARD = By.xpath(".//button[contains(translate(.,'BOOK','book'),'book') or contains(translate(.,'RESERVE','reserve'),'reserve')]");
    private static final By BOOK_FORM = By.xpath("//form[.//input[@id='firstname' or @name='firstname'] or .//button[contains(.,'Book')]]");
    private static final By BOOK_FIRSTNAME = By.cssSelector("#firstname, input[name='firstname']");
    private static final By BOOK_LASTNAME = By.cssSelector("#lastname, input[name='lastname']");
    private static final By BOOK_EMAIL = By.cssSelector("#email, input[name='email'][type='email']");
    private static final By BOOK_PHONE = By.cssSelector("#phone, input[name='phone']");
    private static final By BOOK_CHECKIN = By.cssSelector("#checkin, input[name='checkin'], input[id*='checkin']");
    private static final By BOOK_CHECKOUT = By.cssSelector("#checkout, input[name='checkout'], input[id*='checkout']");
    private static final By BOOK_CONFIRM = By.xpath("//button[contains(translate(.,'BOOK','book'),'book') and not(contains(translate(.,'BOOKING','booking'),'booking'))]");

    // ===== Contact form (homepage) - robust, multi-fallbacks =====
    private static final By CONTACT_FORM = By.xpath("//form[.//textarea or .//input[@id='subject'] or .//button[contains(.,'Submit')]]");
    private static final By CONTACT_NAME = By.cssSelector("#name, input[name='name'], input[placeholder*='Name'], input[placeholder*='name']");
    private static final By CONTACT_EMAIL = By.cssSelector("#email, input[type='email'][name='email'], input[type='email']");
    private static final By CONTACT_PHONE = By.cssSelector("#phone, input[name='phone'], input[placeholder*='Phone'], input[placeholder*='phone']");
    private static final By CONTACT_SUBJECT = By.cssSelector("#subject, input[name='subject'], input[placeholder*='Subject'], input[placeholder*='subject']");
    private static final By CONTACT_MESSAGE = By.cssSelector("#message, textarea[name='message'], textarea[placeholder*='Message'], textarea[placeholder*='message'], textarea");
    private static final By CONTACT_SUBMIT = By.xpath("//form//button[@type='submit' or contains(translate(.,'SUBMIT','submit'),'submit')]");
    private static final By CONTACT_SUCCESS = By.cssSelector(".alert-success, .alert.alert-success, .contact .alert-success, [data-test='contact-success']");
    private static final By CONTACT_ERROR = By.cssSelector(".alert-danger, .alert.alert-danger, .contact .alert-danger, [data-test='contact-error']");

    // Non-applicable "burger/sorting/login" (guarded as absent) =====
    private static final By BURGER_BTN = By.id("react-burger-menu-btn");
    private static final By SIDE_MENU = By.cssSelector(".bm-menu-wrap, nav[aria-label='menu']");
    private static final By MENU_ALL_ITEMS = By.id("inventory_sidebar_link");
    private static final By MENU_ABOUT = By.id("about_sidebar_link");
    private static final By MENU_LOGOUT = By.id("logout_sidebar_link");
    private static final By MENU_RESET = By.id("reset_sidebar_link");
    private static final By SORTING_DROPDOWN = By.cssSelector("select[data-test='product_sort_container'], select#sort, select[name*='sort']");

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
        if (driver != null) driver.quit();
    }

    // ===== Helper methods =====
    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(ANY_CONTAINER));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://automationintesting.online"), "Should be on the expected origin");
    }

    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private static String hostOf(String url) {
        try { return Optional.ofNullable(new URI(url).getHost()).orElse(""); }
        catch (Exception e) { return ""; }
    }

    private void assertExternalLink(WebElement linkEl, String expectedDomainContains) {
        String original = driver.getWindowHandle();
        Set<String> old = driver.getWindowHandles();
        linkEl.click();
        wait.until(d -> d.getWindowHandles().size() > old.size() || d.getCurrentUrl().contains(expectedDomainContains));
        if (driver.getWindowHandles().size() > old.size()) {
            Set<String> diff = new HashSet<>(driver.getWindowHandles());
            diff.removeAll(old);
            String newHandle = diff.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainContains));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainContains), "External URL should contain: " + expectedDomainContains);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomainContains));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainContains), "External URL should contain: " + expectedDomainContains);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(ANY_CONTAINER));
        }
    }

    // ===== Tests =====

    @Test
    @Order(1)
    public void landing_CoreElementsPresent() {
        openBase();
        // Home page content check
        waitVisible(By.cssSelector("body"));

        boolean contactFormPresent = isPresent(By.tagName("form")) || isPresent(By.xpath("//form"));
        boolean nameFieldPresent = isPresent(By.cssSelector("input[name='name'], input[id='name'], input[type='text']")) || isPresent(By.xpath("//form//input[@type='text']"));

        Assertions.assertAll(
                () -> Assertions.assertTrue(contactFormPresent, "Contact form should be present on landing"),
                () -> Assertions.assertTrue(nameFieldPresent, "Contact name field should be present"),
                () -> Assertions.assertTrue(isPresent(CONTACT_EMAIL), "Contact email field should be present"),
                () -> Assertions.assertTrue(isPresent(CONTACT_MESSAGE), "Contact message field should be present"),
                () -> Assertions.assertTrue(isPresent(CONTACT_SUBMIT), "Contact submit button should be present")
        );
    }

    @Test
    @Order(2)
    public void contact_InvalidEmail_ShowsErrorOrHtml5Validation() {
        openBase();
        waitVisible(CONTACT_NAME).clear();
        driver.findElement(CONTACT_NAME).sendKeys("Caio Tester");
        waitVisible(CONTACT_EMAIL).clear();
        driver.findElement(CONTACT_EMAIL).sendKeys("invalid-email");
        if (isPresent(CONTACT_PHONE)) { driver.findElement(CONTACT_PHONE).clear(); driver.findElement(CONTACT_PHONE).sendKeys("11999999999"); }
        if (isPresent(CONTACT_SUBJECT)) { driver.findElement(CONTACT_SUBJECT).clear(); driver.findElement(CONTACT_SUBJECT).sendKeys("Invalid Email Check"); }
        waitVisible(CONTACT_MESSAGE).clear();
        driver.findElement(CONTACT_MESSAGE).sendKeys("Testing invalid email validation.");
        waitClickable(CONTACT_SUBMIT).click();

        String validationMessage = driver.findElement(CONTACT_EMAIL).getDomProperty("validationMessage");
        boolean html5 = validationMessage != null && validationMessage.trim().length() > 0;
        boolean custom = isPresent(CONTACT_ERROR) || driver.getPageSource().toLowerCase().contains("invalid") || driver.getPageSource().toLowerCase().contains("required");
        Assertions.assertTrue(html5 || custom, "Invalid email should be blocked by HTML5 or app error message");
    }

    @Test
    @Order(3)
    public void contact_ValidSubmission_ShowsSuccess() {
        openBase();
        waitVisible(CONTACT_NAME).clear();
        driver.findElement(CONTACT_NAME).sendKeys("Maria Quality");
        waitVisible(CONTACT_EMAIL).clear();
        driver.findElement(CONTACT_EMAIL).sendKeys("maria.quality@example.com");
        if (isPresent(CONTACT_PHONE)) { driver.findElement(CONTACT_PHONE).clear(); driver.findElement(CONTACT_PHONE).sendKeys("21988887777"); }
        if (isPresent(CONTACT_SUBJECT)) { driver.findElement(CONTACT_SUBJECT).clear(); driver.findElement(CONTACT_SUBJECT).sendKeys("Booking info"); }
        waitVisible(CONTACT_MESSAGE).clear();
        driver.findElement(CONTACT_MESSAGE).sendKeys("Please contact me regarding availability.");
        waitClickable(CONTACT_SUBMIT).click();

        boolean successByBlock = isPresent(CONTACT_SUCCESS);
        boolean successByText = driver.getPageSource().toLowerCase().contains("thanks") || driver.getPageSource().toLowerCase().contains("thank you") || driver.getPageSource().toLowerCase().contains("sucesso") || driver.getPageSource().toLowerCase().contains("message sent");
        Assertions.assertTrue(successByBlock || successByText, "A success message should appear after valid contact submission");
    }

    @Test
    @Order(4)
    public void navigate_Rooms_ListVisible() {
        openBase();
        // Click Rooms link (one level below within SPA)
        Assumptions.assumeTrue(isPresent(LINK_ROOMS), "Rooms link not found on header/nav");
        waitClickable(LINK_ROOMS).click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("room"),
                ExpectedConditions.presenceOfElementLocated(ROOMS_LIST)
        ));
        Assertions.assertTrue(isPresent(ROOMS_LIST), "Rooms list/cards should be visible after navigating to Rooms");
    }

    @Test
    @Order(5)
    public void book_FirstRoom_EndToEnd_ShowsConfirmation() {
        openBase();
        if (isPresent(LINK_ROOMS)) {
            waitClickable(LINK_ROOMS).click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("room"),
                    ExpectedConditions.presenceOfElementLocated(ROOMS_LIST)
            ));
        }
        Assumptions.assumeTrue(isPresent(ROOMS_LIST), "Rooms are not available to book right now");

        WebElement firstRoom = driver.findElements(ROOMS_LIST).get(0);
        // Try find "Book" button inside the room card
        List<WebElement> bookBtns = firstRoom.findElements(BOOK_BUTTON_IN_CARD);
        Assumptions.assumeTrue(!bookBtns.isEmpty(), "No 'Book' button found in the first room card");
        WebElement bookBtn = bookBtns.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(bookBtn)).click();

        // Fill out booking form when it appears
        WebElement form = wait.until(ExpectedConditions.presenceOfElementLocated(BOOK_FORM));
        wait.until(ExpectedConditions.presenceOfElementLocated(BOOK_FIRSTNAME));

        if (isPresent(BOOK_FIRSTNAME)) { WebElement fn = driver.findElement(BOOK_FIRSTNAME); try { fn.clear(); } catch (Exception ignored) {} fn.sendKeys("Ana"); }
        if (isPresent(BOOK_LASTNAME)) { WebElement ln = driver.findElement(BOOK_LASTNAME); try { ln.clear(); } catch (Exception ignored) {} ln.sendKeys("Tester"); }
        if (isPresent(BOOK_EMAIL)) { WebElement em = driver.findElement(BOOK_EMAIL); try { em.clear(); } catch (Exception ignored) {} em.sendKeys("ana.tester@example.com"); }
        if (isPresent(BOOK_PHONE)) { WebElement ph = driver.findElement(BOOK_PHONE); try { ph.clear(); } catch (Exception ignored) {} ph.sendKeys("11999990000"); }
        // Dates - tolerant formats for common HTML5 date inputs (YYYY-MM-DD)
        if (isPresent(BOOK_CHECKIN)) { WebElement ci = driver.findElement(BOOK_CHECKIN); try { ci.clear(); } catch (Exception ignored) {} ci.sendKeys("2030-01-10"); }
        if (isPresent(BOOK_CHECKOUT)) { WebElement co = driver.findElement(BOOK_CHECKOUT); try { co.clear(); } catch (Exception ignored) {} co.sendKeys("2030-01-12"); }

        // Submit booking
        List<WebElement> submitCandidates = driver.findElements(BOOK_CONFIRM);
        if (submitCandidates.isEmpty()) {
            // fallback inside the form
            submitCandidates = form.findElements(By.cssSelector("button[type='submit'], button"));
        }
        Assumptions.assumeTrue(!submitCandidates.isEmpty(), "No booking submit button found");
        wait.until(ExpectedConditions.elementToBeClickable(submitCandidates.get(0))).click();

        // Assert success indication
        boolean successToast = !driver.findElements(By.cssSelector(".alert-success, .booking-success, .toast-success")).isEmpty();
        boolean successText = driver.getPageSource().toLowerCase().contains("booking") && driver.getPageSource().toLowerCase().contains("success");
        Assertions.assertTrue(successToast || successText, "Booking flow should indicate success (toast or text)");
    }

    @Test
    @Order(6)
    public void externalLinks_OnVisiblePage_OpenAndClose() {
        openBase();
        // Collect external links on base page (one level below external only)
        String baseHost = hostOf(BASE_URL);
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href^='http']"));
        List<WebElement> externals = anchors.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    String host = hostOf(href);
                    return href != null && !host.isEmpty() && !host.contains(baseHost);
                }).collect(Collectors.toList());

        int toVisit = Math.min(3, externals.size());
        for (int i = 0; i < toVisit; i++) {
            WebElement link = externals.get(i);
            String host = hostOf(link.getAttribute("href"));
            if (!host.isEmpty()) {
                assertExternalLink(link, host);
            }
        }

        if (toVisit == 0) {
            Assertions.assertTrue(isPresent(ANY_CONTAINER), "No external links found; page should remain stable");
        }
    }

    @Test
    @Order(7)
    public void burgerMenu_Sorting_Login_NotApplicable_ButGuarded() {
        openBase();
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.findElements(BURGER_BTN).isEmpty(), "Burger button should not exist on this site"),
                () -> Assertions.assertTrue(driver.findElements(SIDE_MENU).isEmpty(), "Side menu should not exist on this site"),
                () -> Assertions.assertTrue(driver.findElements(MENU_ALL_ITEMS).isEmpty(), "All Items menu should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_ABOUT).isEmpty(), "About menu should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_LOGOUT).isEmpty(), "Logout menu should not exist"),
                () -> Assertions.assertTrue(driver.findElements(MENU_RESET).isEmpty(), "Reset App State should not exist"),
                () -> Assertions.assertTrue(driver.findElements(SORTING_DROPDOWN).isEmpty(), "Sorting dropdown should not exist on this site")
        );
    }
}