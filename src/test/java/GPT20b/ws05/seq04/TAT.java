package GPT20b.ws05.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacTatHeadlessTest {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void initDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                         */
    /* --------------------------------------------------------------------- */

    /**
     * Returns the first element that matches any of the provided CSS selectors.
     * Throws NoSuchElementException if none are found.
     */
    private WebElement findElement(String... cssSelectors) {
        for (String sel : cssSelectors) {
            List<WebElement> els = driver.findElements(By.cssSelector(sel));
            if (!els.isEmpty()) {
                return els.get(0);
            }
        }
        throw new NoSuchElementException("No element found for selectors: " + String.join(", ", cssSelectors));
    }

    /**
     * Opens a link that contains the provided text fragment,
     * verifies that a new window/tab contains the expected domain,
     * then closes it and returns to the original window.
     */
    private void openAndVerifyExternalLink(String fragment, String domain) {
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + fragment + "']"));
        if (links.isEmpty()) return; // nothing to test
        WebElement link = links.get(0);
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        link.click();

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newWindow = after.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(d -> d.getCurrentUrl().contains(domain));
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(d -> d.getCurrentUrl().contains(domain));
            driver.navigate().back();
        }
    }

    /* --------------------------------------------------------------------- */
    /* Tests                                                                   */
    /* --------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testEmptyFormSubmissionShowsErrors() {
        driver.navigate().to(BASE_URL);
        WebElement form = findElement("form#login, form");
        assertNotNull(form, "Form should be present on the page");

        WebElement submitBtn = findElement("button[type='submit'], input[type='submit'], button.btn-primary");
        submitBtn.click();

        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".error, .form-error, .invalid-feedback, .alert-danger"));
        assertFalse(errorMessages.isEmpty(), "Expected validation error messages after submitting an empty form");
    }

    @Test
    @Order(2)
    public void testValidFormSubmissionShowsSuccess() {
        driver.navigate().to(BASE_URL);

        // Fill fields if they exist
        try {
            WebElement username = findElement("input[name='username'], input#username, input#user");
            username.clear();
            username.sendKeys("caio@gmail.com");
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement password = findElement("input[name='password'], input#password, input#pass");
            password.clear();
            password.sendKeys("123");
        } catch (NoSuchElementException ignored) {}

        // Submit
        WebElement submitBtn = findElement("button[type='submit'], input[type='submit'], button.btn-primary");
        submitBtn.click();

        // Look for a success notification
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-success, .success, .form-success, .alert[role='alert']")));
        assertNotNull(successMsg, "Success message should appear after a valid submission");
    }

    @Test
    @Order(3)
    public void testCheckboxInteraction() {
        driver.navigate().to(BASE_URL);
        List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
        if (checkboxes.isEmpty()) return; // No checkboxes to test

        WebElement cb = checkboxes.get(0);
        boolean before = cb.isSelected();

        wait.until(ExpectedConditions.elementToBeClickable(cb));
        cb.click();

        assertNotEquals(before, cb.isSelected(), "Checkbox state should toggle after click");

        cb.click();
        assertEquals(before, cb.isSelected(), "Checkbox should revert to original state after second click");
    }

    @Test
    @Order(4)
    public void testDropdownSelectionChangesValue() {
        driver.navigate().to(BASE_URL);
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        if (selects.isEmpty()) return; // No dropdowns

        WebElement select = selects.get(0);
        List<WebElement> options = select.findElements(By.tagName("option"));
        if (options.size() < 2) return; // Not enough options

        // Record first value
        String firstVal = options.get(0).getAttribute("value");

        // Select second option
        options.get(1).click();
        String secondVal = select.getAttribute("value");

        assertNotEquals(firstVal, secondVal, "Dropdown value should change when a different option is selected");
    }

    @Test
    @Order(5)
    public void testExternalSocialLinks() {
        driver.navigate().to(BASE_URL);

        openAndVerifyExternalLink("twitter.com", "twitter.com");
        openAndVerifyExternalLink("facebook.com", "facebook.com");
        openAndVerifyExternalLink("linkedin.com", "linkedin.com");
    }
}