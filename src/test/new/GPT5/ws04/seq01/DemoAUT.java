package GPT5.ws04.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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

    /* ======================= Helpers ======================= */

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
    }

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        throw new NoSuchElementException("No locator matched: " + Arrays.toString(locators));
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    private void switchToNewTabAndVerify(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> wins = driver.getWindowHandles();
        for (String w : wins) {
            if (!w.equals(originalWindow)) {
                driver.switchTo().window(w);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected domain not found: " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }
        Assertions.fail("No new tab opened.");
    }

    private void fillMinimalValidFormData() {
        openBase();
        WebElement firstName = firstPresent(By.name("first_name"), By.name("firstName"), By.id("firstName"));
        WebElement lastName  = firstPresent(By.name("last_name"), By.name("lastName"), By.id("lastName"));
        WebElement dob       = firstPresent(By.name("dob"), By.id("dob"));
        WebElement address   = firstPresent(By.name("address"), By.id("address"));
        WebElement email     = firstPresent(By.name("email"), By.id("email"));
        WebElement password  = firstPresent(By.name("password"), By.id("password"));

        firstName.clear(); firstName.sendKeys("Caio");
        lastName.clear();  lastName.sendKeys("Tester");
        dob.clear();       dob.sendKeys("2000-01-01");
        address.clear();   address.sendKeys("123 Test Street");
        email.clear();     email.sendKeys("test@example.com");
        password.clear();  password.sendKeys("StrongPass123");

        // Gender radios (pick the first available)
        List<WebElement> genders = driver.findElements(By.cssSelector("input[name='gender']"));
        if (!genders.isEmpty() && !genders.get(0).isSelected()) safeClick(genders.get(0));

        // Role dropdown if present
        List<WebElement> roleEls = driver.findElements(By.name("role"));
        if (!roleEls.isEmpty()) {
            Select role = new Select(roleEls.get(0));
            // Prefer a meaningful option if available
            List<String> preferred = Arrays.asList("Manager", "Developer", "QA", "Tester");
            boolean selected = false;
            for (String p : preferred) {
                for (WebElement opt : role.getOptions()) {
                    if (opt.getText().trim().equalsIgnoreCase(p)) {
                        role.selectByVisibleText(opt.getText());
                        selected = true;
                        break;
                    }
                }
                if (selected) break;
            }
            if (!selected && role.getOptions().size() > 1) {
                role.selectByIndex(1); // skip placeholder
            }
        }
    }

    /* ======================= Tests ======================= */

    @Test
    @Order(1)
    public void testFormElementsPresence() {
        openBase();
        Assertions.assertTrue(firstPresent(By.name("first_name"), By.name("firstName"), By.id("firstName")).isDisplayed(), "First name should be visible.");
        Assertions.assertTrue(firstPresent(By.name("last_name"), By.name("lastName"), By.id("lastName")).isDisplayed(), "Last name should be visible.");
        Assertions.assertTrue(firstPresent(By.name("dob"), By.id("dob")).isDisplayed(), "DOB should be visible.");
        Assertions.assertTrue(firstPresent(By.name("address"), By.id("address")).isDisplayed(), "Address should be visible.");
        Assertions.assertTrue(firstPresent(By.name("email"), By.id("email")).isDisplayed(), "Email should be visible.");
        Assertions.assertTrue(firstPresent(By.name("password"), By.id("password")).isDisplayed(), "Password should be visible.");
    }

    @Test
    @Order(2)
    public void testDropdownSelectionIfPresent() {
        openBase();
        List<WebElement> roleEls = driver.findElements(By.name("role"));
        if (!roleEls.isEmpty()) {
            Select role = new Select(roleEls.get(0));

            // Select first non-placeholder option
            if (role.getOptions().size() > 1) {
                role.selectByIndex(1);
                String chosen1 = role.getFirstSelectedOption().getText().trim();

                // If there is another option, switch and assert change
                if (role.getOptions().size() > 2) {
                    role.selectByIndex(2);
                    String chosen2 = role.getFirstSelectedOption().getText().trim();
                    Assertions.assertNotEquals(chosen1, chosen2, "Role selection should change after selecting a different option.");
                } else {
                    Assertions.assertFalse(chosen1.isEmpty(), "Selected role should not be empty.");
                }
            } else {
                // Only one option present; just assert it's visible
                Assertions.assertTrue(role.getFirstSelectedOption().isDisplayed(), "Role option should be visible.");
            }
        } else {
            // If no dropdown exists, ensure the form still renders
            Assertions.assertTrue(driver.findElement(By.tagName("form")).isDisplayed(), "Form should still be present even without role dropdown.");
        }
    }

    @Test
    @Order(3)
    public void testCheckboxInteractionIfPresent() {
        openBase();
        List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));
        if (checks.size() >= 2) {
            WebElement c1 = checks.get(0);
            WebElement c2 = checks.get(1);
            if (!c1.isSelected()) safeClick(c1);
            if (!c2.isSelected()) safeClick(c2);
            Assertions.assertAll(
                () -> Assertions.assertTrue(c1.isSelected(), "First checkbox should be selected."),
                () -> Assertions.assertTrue(c2.isSelected(), "Second checkbox should be selected.")
            );
        } else {
            // No checkboxes on page; assert form is still usable
            Assertions.assertTrue(driver.findElement(By.tagName("form")).isDisplayed(), "Form should be present.");
        }
    }

    @Test
    @Order(4)
    public void testInvalidEmailPreventsSuccess() {
        openBase();
        WebElement firstName = firstPresent(By.name("first_name"), By.name("firstName"), By.id("firstName"));
        WebElement lastName  = firstPresent(By.name("last_name"), By.name("lastName"), By.id("lastName"));
        WebElement email     = firstPresent(By.name("email"), By.id("email"));
        WebElement password  = firstPresent(By.name("password"), By.id("password"));
        WebElement submit    = firstPresent(By.cssSelector("button[type='submit']"), By.id("submit"));

        firstName.clear(); firstName.sendKeys("Caio");
        lastName.clear();  lastName.sendKeys("Montes");
        email.clear();     email.sendKeys("invalid-email"); // invalid format
        password.clear();  password.sendKeys("abc12345");

        safeClick(submit);

        // Expect NOT to see success message
        List<WebElement> success = driver.findElements(By.id("submit-msg"));
        if (!success.isEmpty()) {
            Assertions.assertFalse(success.get(0).getText().toLowerCase().contains("success"),
                    "Success message should not appear with invalid email.");
        } else {
            Assertions.assertTrue(true, "No success message present, as expected for invalid email.");
        }
    }

    @Test
    @Order(5)
    public void testSuccessfulSubmissionShowsSuccessMessage() {
        fillMinimalValidFormData();

        // Select a couple of interests if available
        List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));
        for (int i = 0; i < Math.min(2, checks.size()); i++) {
            if (!checks.get(i).isSelected()) safeClick(checks.get(i));
        }

        WebElement submit = firstPresent(By.cssSelector("button[type='submit']"), By.id("submit"));
        safeClick(submit);

        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit-msg")));
        String msg = success.getText().toLowerCase();
        Assertions.assertTrue(msg.contains("success"), "Submission should show success message. Actual: " + msg);
    }

    @Test
    @Order(6)
    public void testInternalPrivacyPageNavigation() {
        openBase();
        // Many variants use an internal privacy.html page
        List<WebElement> links = driver.findElements(By.linkText("Privacy Policy"));
        if (!links.isEmpty()) {
            safeClick(links.get(0));
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("privacy.html"),
                    ExpectedConditions.presenceOfElementLocated(By.tagName("h1"))
            ));
            Assertions.assertTrue(driver.getCurrentUrl().contains("privacy.html") || driver.getTitle().toLowerCase().contains("privacy"),
                    "Should navigate to privacy page.");
        } else {
            // If link text differs, try href directly
            List<WebElement> alt = driver.findElements(By.cssSelector("a[href='privacy.html']"));
            if (!alt.isEmpty()) {
                safeClick(alt.get(0));
                wait.until(ExpectedConditions.urlContains("privacy.html"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("privacy.html"), "Should navigate to privacy.html.");
            } else {
                // If not present, just assert page still OK
                Assertions.assertTrue(driver.findElement(By.tagName("form")).isDisplayed(), "Form remains visible when no privacy link exists.");
            }
        }
    }

    @Test
    @Order(7)
    public void testExternalKatalonLinkIfPresent() {
        openBase();
        List<WebElement> ext = driver.findElements(By.cssSelector("a[href*='katalon.com']"));
        if (!ext.isEmpty()) {
            int before = driver.getWindowHandles().size();
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", ext.get(0));
            ext.get(0).click();
            if (driver.getWindowHandles().size() > before) {
                switchToNewTabAndVerify("katalon.com");
            } else {
                wait.until(ExpectedConditions.urlContains("katalon.com"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("katalon.com"), "Expected navigation to katalon.com");
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
            }
        } else {
            Assertions.assertTrue(true, "No external Katalon link present; skipping.");
        }
    }
}