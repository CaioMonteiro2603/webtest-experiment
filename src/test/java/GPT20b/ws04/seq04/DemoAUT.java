package GPT20b.ws04.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.util.Set;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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

    private WebElement findElement(String... cssSelectors) {
        for (String sel : cssSelectors) {
            List<WebElement> elements = driver.findElements(By.cssSelector(sel));
            if (!elements.isEmpty()) {
                return elements.get(0);
            }
        }
        throw new NoSuchElementException("Unable to find element: " + String.join(", ", cssSelectors));
    }

    private void openAndVerifyExternalLink(String partialHref, String domain) {
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + partialHref + "']"));
        if (links.isEmpty()) return; // nothing to test
        WebElement link = links.get(0);
        String originalWindow = driver.getWindowHandle();
        Set<String> handlesBefore = driver.getWindowHandles();
        link.click();
        Set<String> handlesAfter = driver.getWindowHandles();
        if (handlesAfter.size() > handlesBefore.size()) {
            handlesAfter.removeAll(handlesBefore);
            String newWindow = handlesAfter.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(d -> d.getCurrentUrl().contains(domain));
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(d -> d.getCurrentUrl().contains(domain));
            driver.navigate().back();
        }
    }

    /* --------------------------------------------------------------------- */
    /* Test cases                                                             */
    /* --------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testEmptyFormSubmissionShowsErrors() {
        driver.navigate().to(BASE_URL);
        // Ensure form is present
        WebElement form = findElement("form#contactForm, form");
        assertNotNull(form, "Form should be present on page");

        // Click submit without filling
        WebElement submitBtn = findElement("button[type='submit'], input[type='submit'], button.btn-primary");
        submitBtn.click();

        // Expect at least one error message displayed
        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".error, .form-error, .invalid-feedback"));
        assertFalse(errorMessages.isEmpty(), "Expected validation error messages after empty submission");
    }

    @Test
    @Order(2)
    public void testValidFormSubmissionShowsSuccess() {
        driver.navigate().to(BASE_URL);

        // Fill required fields if they exist
        try {
            WebElement firstName = findElement("input[name='firstName'], input#firstname, input#first-name");
            firstName.clear();
            firstName.sendKeys("John");
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement lastName = findElement("input[name='lastName'], input#lastname, input#last-name");
            lastName.clear();
            lastName.sendKeys("Doe");
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement email = findElement("input[name='email'], input#email, input#email-address");
            email.clear();
            email.sendKeys("john@example.com");
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement message = findElement("textarea[name='message'], textarea#message, textarea#comments");
            message.clear();
            message.sendKeys("This is a test message.");
        } catch (NoSuchElementException ignored) {}

        // Submit form
        WebElement submitBtn = findElement("button[type='submit'], input[type='submit'], button.btn-primary");
        submitBtn.click();

        // Verify success indicator
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert-success, .success, .form-success, .alert[role='alert']")));
        assertNotNull(successMsg, "Success message should be displayed after form submission");
    }

    @Test
    @Order(3)
    public void testCheckboxInteraction() {
        driver.navigate().to(BASE_URL);

        // Locate a checkbox if present
        List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
        if (checkboxes.isEmpty()) return; // No checkbox to test
        WebElement cb = checkboxes.get(0);
        boolean isSelectedBefore = cb.isSelected();

        // Toggle checkbox
        wait.until(ExpectedConditions.elementToBeClickable(cb));
        cb.click();

        // Verify state changed
        assertNotEquals(isSelectedBefore, cb.isSelected(),
                "Checkbox state should toggle after click");

        // Toggle back
        cb.click();
        assertEquals(isSelectedBefore, cb.isSelected(),
                "Checkbox should revert to original state after second click");
    }

    @Test
    @Order(4)
    public void testDropdownSelectionChangesValue() {
        driver.navigate().to(BASE_URL);

        List<WebElement> selects = driver.findElements(By.cssSelector("select"));
        if (selects.isEmpty()) return; // No dropdown present

        WebElement select = selects.get(0);
        List<WebElement> options = select.findElements(By.tagName("option"));
        if (options.size() < 2) return; // Not enough options to test

        // Select first option
        options.get(0).click();
        String firstVal = select.getAttribute("value");

        // Select second option
        options.get(1).click();
        String secondVal = select.getAttribute("value");

        assertNotEquals(firstVal, secondVal, "Dropdown selection should change the value");
    }

    @Test
    @Order(5)
    public void testExternalSocialLinks() {
        driver.navigate().to(BASE_URL);

        // Twitter
        openAndVerifyExternalLink("twitter.com", "twitter.com");

        // Facebook
        openAndVerifyExternalLink("facebook.com", "facebook.com");

        // LinkedIn
        openAndVerifyExternalLink("linkedin.com", "linkedin.com");
    }
}