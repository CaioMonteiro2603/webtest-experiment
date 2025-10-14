package GPT20b.ws04.seq05;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class FrmTest {

    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
    private static final String HOST = "katalon-test.s3.amazonaws.com";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* --------------------------------------------------------------- */
    /* Utility methods                                                  */
    /* --------------------------------------------------------------- */

    private void navigateToBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
    }

    private List<WebElement> findFormElements() {
        return driver.findElements(By.cssSelector("form *"));
    }

    /* --------------------------------------------------------------- */
    /* Test cases                                                      */
    /* --------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testPageLoads() {
        navigateToBase();
        String title = driver.getTitle();
        assertNotNull(title, "Page title should be present");
        assertTrue(title.length() > 0, "Page title should not be empty");

        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(header.isDisplayed(), "Header should be visible on page load");
    }

    @Test
    @Order(2)
    public void testFormElementPresence() {
        navigateToBase();
        List<WebElement> elements = findFormElements();
        assertFalse(elements.isEmpty(), "Form should contain input, select or textarea elements");
    }

    @Test
    @Order(3)
    public void testFormSubmissionSuccess() {
        navigateToBase();

        // Fill text inputs
        List<WebElement> textInputs = driver.findElements(By.cssSelector("input[type='text'], input[type='email'], input[type='tel']"));
        for (int i = 0; i < Math.min(textInputs.size(), 3); i++) {
            WebElement input = textInputs.get(i);
            if (input.isDisplayed() && input.isEnabled()) {
                input.clear();
                input.sendKeys("TestValue");
            }
        }

        // Fill numeric inputs
        List<WebElement> numberInputs = driver.findElements(By.cssSelector("input[type='number']"));
        for (WebElement input : numberInputs) {
            if (input.isDisplayed() && input.isEnabled()) {
                input.clear();
                input.sendKeys("10");
            }
        }

        // Fill selects
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        for (WebElement select : selects) {
            if (select.isDisplayed() && select.isEnabled()) {
                List<WebElement> options = select.findElements(By.tagName("option"));
                if (options.size() > 1) { // choose first non-default
                    options.get(1).click();
                }
            }
        }

        // Handle checkboxes
        List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
        for (WebElement checkbox : checkboxes) {
            if (checkbox.isDisplayed() && checkbox.isEnabled() && !checkbox.isSelected()) {
                checkbox.click();
            }
        }

        // Click Submit button
        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit'], input[type='submit']"));
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();

        // Verify success either by alert or modal
        boolean successDetected = false;

        // Check for JavaScript alert
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            String alertText = driver.switchTo().alert().getText();
            assertTrue(alertText.toLowerCase().contains("thank") || alertText.toLowerCase().contains("success"),
                    "Alert message does not indicate success");
            driver.switchTo().alert().accept();
            successDetected = true;
        } catch (Exception e) {
            // No alert
        }

        // Check for modal or success message block
        if (!successDetected) {
            List<WebElement> successMsgEls = driver.findElements(By.cssSelector(".success,.alert,.feedback,.modal-body,.message"));
            for (WebElement el : successMsgEls) {
                if (el.isDisplayed() && el.getText().toLowerCase().contains("thank") || el.getText().toLowerCase().contains("success")) {
                    successDetected = true;
                    break;
                }
            }
        }

        assertTrue(successDetected, "Form submission did not trigger a success message or alert");
    }

    @Test
    @Order(4)
    public void testExternalLinksHandling() {
        navigateToBase();

        List<WebElement> anchors = driver.findElements(By.tagName("a"));
        assertFalse(anchors.isEmpty(), "Page should contain link elements");

        Set<String> tested = new HashSet<>();
        for (WebElement link : anchors) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            try {
                URI uri = new URI(href);
                String host = uri.getHost();
                if (host == null || host.equals(HOST)) continue; // internal link
                if (tested.contains(href)) continue; // avoid duplicate

                // Open link via click
                String original = driver.getWindowHandle();
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();

                // Wait for new window
                wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
                Set<String> windows = driver.getWindowHandles();
                for (String win : windows) {
                    if (!win.equals(original)) {
                        driver.switchTo().window(win);
                        break;
                    }
                }

                // Verify the new page's URL contains the external host
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains(host),
                        "External link navigation failed. Expected host: " + host + ", got: " + currentUrl);

                // Close external tab and return
                driver.close();
                driver.switchTo().window(original);
                // Confirm we are back at base page
                assertTrue(driver.getCurrentUrl().contains(BASE_URL),
                        "Did not return to base after closing external link");

                tested.add(href);
            } catch (URISyntaxException | InterruptedException e) {
                // Skip malformed URLs or interrupted waits
            }
        }

        assertFalse(tested.isEmpty(), "No external links were found and tested on the page");
    }
}