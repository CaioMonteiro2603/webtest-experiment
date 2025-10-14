package GPT20b.ws07.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsfiddleHeadlessTest {

    private static final String BASE_URL = "https://jsfiddle.net/";

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
    /* Utility functions                                                     */
    /* --------------------------------------------------------------------- */

    /** Finds the first element matching any of the provided CSS selectors. */
    private WebElement findElement(String... cssSelectors) {
        for (String sel : cssSelectors) {
            List<WebElement> els = driver.findElements(By.cssSelector(sel));
            if (!els.isEmpty()) {
                return els.get(0);
            }
        }
        throw new NoSuchElementException("No element matched selectors: " + String.join(", ", cssSelectors));
    }

    /** Switches to a new window opened by the provided action, verifies the URL contains the expected domain, then closes the new window. */
    private void openAndVerifyExternalLink(String linkFragment, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + linkFragment + "']"));
        if (links.isEmpty()) return; // nothing to test
        WebElement link = links.get(0);
        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        link.click();
        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            driver.navigate().back();
        }
    }

    /* --------------------------------------------------------------------- */
    /* Tests                                                                 */
    /* --------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.navigate().to(BASE_URL);
        // Verify that the main container and search bar are visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("main")));
        assertTrue(driver.findElements(By.cssSelector("input[data-automation='search'])).size() > 0,
                "Search field should be present on the home page");
    }

    @Test
    @Order(2)
    public void testLoginButtonPresence() {
        driver.navigate().to(BASE_URL);
        WebElement loginBtn = findElement("a[href*='login'], button[data-automation='login']",
                                          "a[data-automation='auth']",
                                          "button#login");
        assertTrue(loginBtn.isDisplayed(), "Login button should be visible");
    }

    @Test
    @Order(3)
    public void testInvalidLoginShowsError() {
        driver.navigate().to(BASE_URL + "login/");
        WebElement emailInput = findElement("input[type='email'], input#email");
        WebElement passwordInput = findElement("input[type='password'], input#password");
        WebElement submitBtn = findElement("button[type='submit'], button#login-button");

        emailInput.clear();
        emailInput.sendKeys("invalid@example.com");
        passwordInput.clear();
        passwordInput.sendKeys("wrongpassword");
        submitBtn.click();

        List<WebElement> errors = driver.findElements(By.cssSelector(".error-message, .auth-error"));
        assertFalse(errors.isEmpty(), "An error should be displayed for invalid credentials");
    }

    @Test
    @Order(4)
    public void testLibraryNavigationAndSort() {
        driver.navigate().to(BASE_URL);
        WebElement libraryLink = findElement("a[href*='library']");
        libraryLink.click();

        wait.until(ExpectedConditions.urlContains("/library/"));
        assertTrue(driver.getCurrentUrl().contains("/library/"),
                "Should be on the Library page after clicking the link");

        // If a sort dropdown is present, test changing its value
        List<WebElement> sortDropdowns = driver.findElements(By.cssSelector("select[data-automation='sort']"));
        if (!sortDropdowns.isEmpty()) {
            WebElement dropdown = sortDropdowns.get(0);
            List<WebElement> options = dropdown.findElements(By.tagName("option"));
            if (options.size() > 1) {
                String initial = options.get(0).getAttribute("value");
                options.get(1).click();
                String changed = dropdown.getAttribute("value");
                assertNotEquals(initial, changed, "Sorting option should change the selected value");
            }
        }
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.navigate().to(BASE_URL);
        openAndVerifyExternalLink("twitter.com", "twitter.com");
        openAndVerifyExternalLink("facebook.com", "facebook.com");
        openAndVerifyExternalLink("instagram.com", "instagram.com");
    }

    @Test
    @Order(6)
    public void testCreateNewFiddleAndRun() {
        driver.navigate().to(BASE_URL);
        WebElement newFiddleBtn = findElement("a[data-automation='create'], button[data-automation='create-fiddle']");
        newFiddleBtn.click();

        wait.until(ExpectedConditions.urlMatches(".*/new/?$"));
        assertTrue(driver.getCurrentUrl().contains("/new/"), "Should be on the new fiddle page");

        // Input some basic HTML and JS
        WebElement htmlPane = findElement("textarea[data-automation='html-pane']", "textarea.ace_text-input");
        WebElement jsPane = findElement("textarea[data-automation='js-pane']", "textarea.ace_text-input");
        if (htmlPane != null && jsPane != null) {
            htmlPane.clear();
            htmlPane.sendKeys("<h1>Hello World</h1>");
            jsPane.clear();
            jsPane.sendKeys("console.log('test');");
        }

        // Run the fiddle
        WebElement runBtn = findElement("button[data-automation='run'], button#run");
        runBtn.click();

        // Wait for the result pane to update
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("iframe")));
        // Verify the iframe contains the expected output
        WebElement iframe = driver.findElement(By.cssSelector("iframe"));
        driver.switchTo().frame(iframe);
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertTrue(bodyText.contains("Hello World"), "Result should contain the heading added to HTML");
        driver.switchTo().defaultContent();
    }
}