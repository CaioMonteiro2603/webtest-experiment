package GPT5.ws06.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class AutomationInTestingSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://automationintesting.online/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    /* =========================== Helpers =========================== */

    private void openHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        // Ensure the hero/header is present
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("header")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".hero")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(.,\"Shady Meadows\")]"))
        ));
    }

    private WebElement findFirst(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        throw new NoSuchElementException("None of the locators matched: " + Arrays.toString(locators));
    }

    private void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
    }

    private void safeClick(By by) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(by));
        scrollIntoView(el);
        el.click();
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el));
        scrollIntoView(el);
        el.click();
    }

    private void type(By by, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        scrollIntoView(el);
        el.clear();
        el.sendKeys(text);
    }

    private void typeOptional(By by, String text) {
        List<WebElement> els = driver.findElements(by);
        if (!els.isEmpty()) {
            WebElement el = els.get(0);
            scrollIntoView(el);
            el.clear();
            el.sendKeys(text);
        }
    }

    private ExpectedCondition<Boolean> anyVisible(By... locators) {
        return drv -> {
            for (By by : locators) {
                List<WebElement> els = drv.findElements(by);
                for (WebElement e : els) {
                    if (e.isDisplayed()) return true;
                }
            }
            return false;
        };
    }

    private void switchToNewTabAndVerifyDomain(String expectedDomain) {
        String original = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String w : driver.getWindowHandles()) {
            if (!w.equals(original)) {
                driver.switchTo().window(w);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected external domain in URL: " + expectedDomain + " but was " + driver.getCurrentUrl());
                driver.close();
                driver.switchTo().window(original);
                return;
            }
        }
        Assertions.fail("No new tab was opened for the external link.");
    }

    private void clickExternalLinkAndAssert(By locator, String expectedDomain) {
        int before = driver.getWindowHandles().size();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollIntoView(link);
        link.click();
        // If same-tab navigation, validate and navigate back; else switch to new tab
        if (driver.getWindowHandles().size() == before) {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "Expected navigation to external domain: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        } else {
            switchToNewTabAndVerifyDomain(expectedDomain);
        }
    }

    /* =========================== Tests =========================== */

    @Test
    @Order(1)
    public void homePageLoads() {
        openHome();
        Assertions.assertTrue(
                driver.getTitle() != null && !driver.getTitle().isEmpty(),
                "Page title should be present."
        );
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Shady Meadows') or contains(.,'Welcome')]")).size() > 0
                        || driver.findElements(By.cssSelector(".hero, header")).size() > 0,
                "Hero/header text or element should be visible."
        );
    }

    @Test
    @Order(2)
    public void navigateMenuRoomsAndContact() {
        openHome();
        // Rooms navigation - prefer a link with Rooms
        List<By> roomsLocators = Arrays.asList(
                By.linkText("Rooms"),
                By.partialLinkText("Room"),
                By.cssSelector("a[href*='rooms'], a[href='#rooms']")
        );
        boolean roomsVisited = false;
        for (By by : roomsLocators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                safeClick(els.get(0));
                roomsVisited = true;
                break;
            }
        }
        Assertions.assertTrue(roomsVisited, "Should be able to navigate to Rooms section/link.");
        Assertions.assertTrue(
                wait.until(anyVisible(
                        By.cssSelector(".room, .hotel-room, .row .room"),
                        By.xpath("//*[contains(.,'Rooms') and (self::h2 or self::h1 or self::h3)]")
                )),
                "Rooms section/cards should be visible."
        );

        // Contact navigation
        List<By> contactLocators = Arrays.asList(
                By.linkText("Contact"),
                By.partialLinkText("Contact"),
                By.cssSelector("a[href*='contact'], a[href='#contact']")
        );
        boolean contactVisited = false;
        for (By by : contactLocators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                safeClick(els.get(0));
                contactVisited = true;
                break;
            }
        }
        Assertions.assertTrue(contactVisited, "Should be able to navigate to Contact section/link.");
        Assertions.assertTrue(
                wait.until(anyVisible(
                        By.id("contact"),
                        By.xpath("//*[contains(.,'Contact') and (self::h2 or self::h1 or self::h3)]"),
                        By.cssSelector("form[action*='message'], form#contact, form[class*='contact']")
                )),
                "Contact form/section should be visible."
        );
    }

    @Test
    @Order(3)
    public void contactFormValidationShowsErrors() {
        openHome();
        // Ensure contact section in view
        List<WebElement> contactLinks = driver.findElements(By.linkText("Contact"));
        if (!contactLinks.isEmpty()) safeClick(contactLinks.get(0));

        // Attempt to submit without filling
        List<By> submitLocators = Arrays.asList(
                By.id("submitContact"),
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Submit') or contains(.,'Send') or contains(.,'submit')]")
        );
        boolean clicked = false;
        for (By by : submitLocators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                safeClick(els.get(0));
                clicked = true;
                break;
            }
        }
        Assertions.assertTrue(clicked, "Contact submit button should exist.");

        // Expect some validation highlighting or alerts
        boolean hasAlerts = driver.findElements(By.cssSelector(".alert, .error, .help-block, .text-danger")).size() > 0;
        boolean hasInvalids = driver.findElements(By.cssSelector("input:invalid, textarea:invalid")).size() > 0;
        Assertions.assertTrue(hasAlerts || hasInvalids, "Validation feedback should appear for empty contact submission.");
    }

    @Test
    @Order(4)
    public void contactFormSuccessfulSubmission() {
        openHome();

        // Navigate to contact form if necessary
        List<WebElement> contactLinks = driver.findElements(By.linkText("Contact"));
        if (!contactLinks.isEmpty()) safeClick(contactLinks.get(0));

        // Fill contact form (typical ids/names used by the site)
        type(By.id("name"), "Caio Tester");
        type(By.id("email"), "caio.tester@example.com");
        type(By.id("phone"), "1234567890");
        type(By.id("subject"), "Booking enquiry");
        type(By.id("description"), "Hello, this is an automated test message.");

        // Submit
        List<By> submitLocators = Arrays.asList(
                By.id("submitContact"),
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Submit') or contains(.,'Send') or contains(.,'submit')]")
        );
        boolean submitted = false;
        for (By by : submitLocators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                safeClick(els.get(0));
                submitted = true;
                break;
            }
        }
        Assertions.assertTrue(submitted, "Contact submit button should be clickable.");

        // Expect success message
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(translate(., 'THANKS', 'thanks'),'thanks for getting in touch') or contains(@class,'alert-success') or contains(.,'We will get back to you')]")
        ));
        Assertions.assertTrue(success.isDisplayed(), "Success message should be visible after submission.");
    }

    @Test
    @Order(5)
    public void roomsListAndOpenCloseBookingModal() {
        openHome();

        // Go to Rooms section
        List<By> roomsLocators = Arrays.asList(
                By.linkText("Rooms"),
                By.partialLinkText("Room"),
                By.cssSelector("a[href*='rooms'], a[href='#rooms']")
        );
        for (By by : roomsLocators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                safeClick(els.get(0));
                break;
            }
        }

        // Ensure at least one room card exists
        List<WebElement> rooms = driver.findElements(By.cssSelector(".room, .hotel-room, .row .room, [data-testid='room']"));
        if (rooms.isEmpty()) {
            // Some builds render as cards with a "Book this room" button without a .room class
            rooms = driver.findElements(By.xpath("//button[contains(.,'Book this room')]/ancestor::*[self::div or self::section][1]"));
        }
        Assertions.assertFalse(rooms.isEmpty(), "At least one room should be listed.");

        // Open booking modal from first room
        WebElement bookBtn = findFirst(
                By.xpath("//button[contains(.,'Book this room')]"),
                By.cssSelector("button.book-room, button[data-testid='book-room']")
        );
        safeClick(bookBtn);

        // Modal should appear
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [role='dialog'], .ReactModal__Content, .booking-modal")
        ));
        Assertions.assertTrue(modal.isDisplayed(), "Booking modal should be visible.");

        // Close the modal
        List<By> closeLocators = Arrays.asList(
                By.cssSelector(".close, button.close, [data-dismiss='modal']"),
                By.xpath("//button[contains(.,'Close') or contains(.,'Cancel')]")
        );
        boolean closed = false;
        for (By by : closeLocators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                safeClick(els.get(0));
                closed = true;
                break;
            }
        }
        if (!closed) {
            // Fallback: press ESC to close
            modal.sendKeys(Keys.ESCAPE);
        }
        wait.until(ExpectedConditions.invisibilityOf(modal));
        Assertions.assertTrue(driver.findElements(By.cssSelector(".modal, [role='dialog'], .ReactModal__Content, .booking-modal"))
                .stream().noneMatch(WebElement::isDisplayed), "Booking modal should close.");
    }

    @Test
    @Order(6)
    public void adminInvalidLoginShowsError() {
        // Navigate directly to admin route (SPA hash or path)
        driver.get(BASE_URL + "#/admin");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

        // Locate username/password fields (use common ids/names)
        List<By> userLocators = Arrays.asList(By.id("username"), By.name("username"), By.cssSelector("input[name='username']"));
        List<By> passLocators = Arrays.asList(By.id("password"), By.name("password"), By.cssSelector("input[name='password']"));
        List<By> submitLocators = Arrays.asList(By.cssSelector("button[type='submit']"), By.xpath("//button[contains(.,'Login') or contains(.,'Sign in') or contains(.,'Log in')]"));

        // If the admin page takes a moment to initialize inputs, wait until at least one is present
        wait.until(d -> userLocators.stream().anyMatch(l -> !driver.findElements(l).isEmpty())
                && passLocators.stream().anyMatch(l -> !driver.findElements(l).isEmpty()));

        // Type invalid credentials
        for (By by : userLocators) {
            if (!driver.findElements(by).isEmpty()) { type(by, "wronguser"); break; }
        }
        for (By by : passLocators) {
            if (!driver.findElements(by).isEmpty()) { type(by, "wrongpass"); break; }
        }

        boolean clicked = false;
        for (By by : submitLocators) {
            if (!driver.findElements(by).isEmpty()) {
                safeClick(by);
                clicked = true;
                break;
            }
        }
        Assertions.assertTrue(clicked, "Admin login submit button should be clickable.");

        // Expect an error feedback
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert, .error, .message-error, .notification-error")),
                ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "invalid"),
                ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Wrong username or password")
        ));
        boolean hasError =
                driver.findElements(By.cssSelector(".alert, .error, .message-error, .notification-error")).size() > 0
                        || driver.findElement(By.tagName("body")).getText().toLowerCase().contains("invalid")
                        || driver.findElement(By.tagName("body")).getText().toLowerCase().contains("wrong username")
                        || driver.findElement(By.tagName("body")).getText().toLowerCase().contains("wrong password");
        Assertions.assertTrue(hasError, "Invalid admin login should show an error message/indicator.");
    }

    @Test
    @Order(7)
    public void footerExternalLinksOpen() {
        openHome();

        // Collect candidate external links in footer/header
        List<String> domains = Arrays.asList("twitter.com", "facebook.com", "linkedin.com", "github.com", "restful-booker");
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Map<String, List<WebElement>> byDomain = new LinkedHashMap<>();
        for (String dom : domains) {
            List<WebElement> matched = anchors.stream()
                    .filter(a -> a.getAttribute("href") != null && a.getAttribute("href").toLowerCase().contains(dom))
                    .collect(Collectors.toList());
            if (!matched.isEmpty()) byDomain.put(dom, matched);
        }

        // Visit up to two external links one-level deep to minimize flakiness
        int visited = 0;
        for (Map.Entry<String, List<WebElement>> entry : byDomain.entrySet()) {
            if (visited >= 2) break;
            String expectedDomain = entry.getKey();
            WebElement link = entry.getValue().get(0);
            int before = driver.getWindowHandles().size();
            scrollIntoView(link);
            link.click();
            if (driver.getWindowHandles().size() > before) {
                switchToNewTabAndVerifyDomain(expectedDomain);
            } else {
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected to navigate to external domain: " + expectedDomain);
                driver.navigate().back();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
            }
            visited++;
        }

        // If no external links found, assert page still fine
        if (visited == 0) {
            Assertions.assertTrue(driver.findElements(By.tagName("a")).size() > 0,
                    "No external links detected; page should still contain anchors.");
        }
    }
}
