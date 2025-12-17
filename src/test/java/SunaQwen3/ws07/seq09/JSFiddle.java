package SunaQwen3.ws07.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final String EXTERNAL_LINKEDIN = "linkedin.com";
    private static final String EXTERNAL_TWITTER = "twitter.com";
    private static final String EXTERNAL_FACEBOOK = "facebook.com";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        assertTrue(driver.getTitle().contains("JSFiddle"), "Page title should contain 'JSFiddle'");
        assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"), "Current URL should contain jsfiddle.net");
    }

    @Test
    @Order(2)
    public void testEditorElementsArePresent() {
        driver.get(BASE_URL);

        // Wait for main editor container
        By editorContainer = By.id("result");
        wait.until(ExpectedConditions.presenceOfElementLocated(editorContainer));
        WebElement resultFrame = driver.findElement(editorContainer);
        assertNotNull(resultFrame, "Result frame should be present");

        // Check for HTML, CSS, JS panels
        By htmlPanel = By.cssSelector("div.panel.html");
        By cssPanel = By.cssSelector("div.panel.css");
        By jsPanel = By.cssSelector("div.panel.javascript");

        wait.until(ExpectedConditions.presenceOfElementLocated(htmlPanel));
        wait.until(ExpectedConditions.presenceOfElementLocated(cssPanel));
        wait.until(ExpectedConditions.presenceOfElementLocated(jsPanel));

        assertTrue(driver.findElements(htmlPanel).size() > 0, "HTML panel should be present");
        assertTrue(driver.findElements(cssPanel).size() > 0, "CSS panel should be present");
        assertTrue(driver.findElements(jsPanel).size() > 0, "JavaScript panel should be present");
    }

    @Test
    @Order(3)
    public void testRunButtonFunctionality() {
        driver.get(BASE_URL);

        By runButton = By.id("run");
        wait.until(ExpectedConditions.elementToBeClickable(runButton));
        WebElement runBtn = driver.findElement(runButton);
        runBtn.click();

        // Wait for result iframe to reload
        By resultIframe = By.cssSelector("iframe.result");
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(resultIframe));
        driver.switchTo().defaultContent(); // Switch back
    }

    @Test
    @Order(4)
    public void testSaveFiddleButtonRequiresLogin() {
        driver.get(BASE_URL);

        By saveButton = By.cssSelector("button.save");
        wait.until(ExpectedConditions.elementToBeClickable(saveButton));
        WebElement saveBtn = driver.findElement(saveButton);
        saveBtn.click();

        // After clicking save, login modal should appear
        By loginModal = By.cssSelector("div.modal.login");
        wait.until(ExpectedConditions.presenceOfElementLocated(loginModal));

        WebElement modal = driver.findElement(loginModal);
        assertTrue(modal.isDisplayed(), "Login modal should be displayed when trying to save");
    }

    @Test
    @Order(5)
    public void testNavigationToAboutPage() {
        driver.get(BASE_URL);

        By aboutLink = By.linkText("About");
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        WebElement about = driver.findElement(aboutLink);

        String originalWindow = driver.getWindowHandle();
        about.click();

        // Wait for new window to appear
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains about.jsfiddle.net
        assertTrue(driver.getCurrentUrl().contains("about.jsfiddle.net"), "About page URL should contain about.jsfiddle.net");

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks_OpenInNewTab() {
        driver.get(BASE_URL);

        // Find all footer social links
        By footerLinks = By.cssSelector("footer a[href*='twitter.com'], footer a[href*='facebook.com'], footer a[href*='linkedin.com']");
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(footerLinks));
        List<WebElement> links = driver.findElements(footerLinks);

        String originalWindow = driver.getWindowHandle();

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            String expectedDomain;

            if (href.contains("twitter.com")) {
                expectedDomain = EXTERNAL_TWITTER;
            } else if (href.contains("facebook.com")) {
                expectedDomain = EXTERNAL_FACEBOOK;
            } else if (href.contains("linkedin.com")) {
                expectedDomain = EXTERNAL_LINKEDIN;
            } else {
                continue;
            }

            link.click();

            // Wait for new window
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));

            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            // Assert URL contains expected domain
            assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should navigate to " + expectedDomain);

            // Close tab and return
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(7)
    public void testSearchBoxFunctionality() {
        driver.get(BASE_URL);

        By searchBox = By.name("q");
        wait.until(ExpectedConditions.presenceOfElementLocated(searchBox));
        WebElement search = driver.findElement(searchBox);
        search.clear();
        search.sendKeys("jquery");
        search.sendKeys(Keys.RETURN);

        // Wait for results
        By searchResults = By.cssSelector("div.fiddles-list");
        wait.until(ExpectedConditions.presenceOfElementLocated(searchResults));

        List<WebElement> fiddles = driver.findElements(By.cssSelector("article.fiddle-item"));
        assertTrue(fiddles.size() > 0, "Search should return at least one fiddle");
    }

    @Test
    @Order(8)
    public void testFrameworkSelectionDropdown() {
        driver.get(BASE_URL);

        By frameworkDropdown = By.name("framework");
        wait.until(ExpectedConditions.elementToBeClickable(frameworkDropdown));
        WebElement dropdown = driver.findElement(frameworkDropdown);

        // Store original value
        String originalValue = dropdown.getAttribute("value");

        // Try selecting different frameworks
        String[] frameworks = {"jquery", "mootools", "prototype", "yui", "dojo", "ext", "angular", "backbone", "vue"};
        for (String framework : frameworks) {
            dropdown.click();
            By option = By.cssSelector("select[name='framework'] option[value='" + framework + "']");
            wait.until(ExpectedConditions.elementToBeClickable(option));
            driver.findElement(option).click();

            // Re-locate dropdown to avoid stale reference
            dropdown = driver.findElement(frameworkDropdown);
            assertEquals(framework, dropdown.getAttribute("value"), "Framework should be set to " + framework);
        }

        // Reset to original
        dropdown.click();
        By originalOption = By.cssSelector("select[name='framework'] option[value='" + originalValue + "']");
        wait.until(ExpectedConditions.elementToBeClickable(originalOption));
        driver.findElement(originalOption).click();
    }

    @Test
    @Order(9)
    public void testLoginModalAppearsOnPrivateFiddle() {
        driver.get(BASE_URL);

        By privateFiddleButton = By.cssSelector("button.private");
        wait.until(ExpectedConditions.elementToBeClickable(privateFiddleButton));
        WebElement privateBtn = driver.findElement(privateFiddleButton);
        privateBtn.click();

        // Login modal should appear
        By loginModal = By.cssSelector("div.modal.login");
        wait.until(ExpectedConditions.presenceOfElementLocated(loginModal));

        WebElement modal = driver.findElement(loginModal);
        assertTrue(modal.isDisplayed(), "Login modal should be displayed when creating private fiddle");
    }

    @Test
    @Order(10)
    public void testNavigationToResourcesPage() {
        driver.get(BASE_URL);

        By resourcesLink = By.linkText("Resources");
        wait.until(ExpectedConditions.elementToBeClickable(resourcesLink));
        WebElement resources = driver.findElement(resourcesLink);

        String originalWindow = driver.getWindowHandle();
        resources.click();

        // Wait for new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains resources.jsfiddle.net
        assertTrue(driver.getCurrentUrl().contains("resources.jsfiddle.net"), "Resources page URL should contain resources.jsfiddle.net");

        // Close tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}