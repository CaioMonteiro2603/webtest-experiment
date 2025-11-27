package SunaQwen3.ws07.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class JsFiddleTestSuite {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final String LOGIN_PAGE_URL = BASE_URL;
    private static final String HOME_PAGE_URL = BASE_URL;

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.equals(BASE_URL) || currentUrl.equals(HOME_PAGE_URL), 
                   "Expected URL to be " + BASE_URL + " or " + HOME_PAGE_URL + ", but was: " + currentUrl);

        String title = driver.getTitle();
        assertTrue(title.contains("JSFiddle") || title.contains("JS Fiddle"), 
                   "Page title should contain 'JSFiddle' or 'JS Fiddle', but was: " + title);

        // Verify main editor area is present
        By editorSelector = By.cssSelector("div#result");
        WebElement editor = wait.until(ExpectedConditions.presenceOfElementLocated(editorSelector));
        assertTrue(editor.isDisplayed(), "Main result frame should be displayed");
    }

    @Test
    @Order(2)
    void testRunButtonExecutesCode() {
        driver.get(BASE_URL);

        // Wait for Run button to be clickable
        By runButtonSelector = By.id("run");
        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(runButtonSelector));
        runButton.click();

        // Switch to result iframe
        By resultFrameSelector = By.cssSelector("iframe#result");
        WebElement resultFrame = wait.until(ExpectedConditions.presenceOfElementLocated(resultFrameSelector));
        driver.switchTo().frame(resultFrame);

        // Check if default output appears (Hello world)
        By helloWorldSelector = By.tagName("body");
        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(helloWorldSelector));
        String bodyText = body.getText();
        assertTrue(bodyText.contains("Hello") || bodyText.trim().length() > 0, 
                   "Expected some output in result frame after running default code");

        // Switch back to main content
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(3)
    void testChangeLanguageToJavaScript() {
        driver.get(BASE_URL);

        // Click on JavaScript tab
        By jsTabSelector = By.cssSelector("li#tab_js a");
        WebElement jsTab = wait.until(ExpectedConditions.elementToBeClickable(jsTabSelector));
        jsTab.click();

        // Verify active tab is JavaScript
        By activeTabSelector = By.cssSelector("li#tab_js.active");
        wait.until(ExpectedConditions.presenceOfElementLocated(activeTabSelector));
        assertTrue(driver.findElement(activeTabSelector).isDisplayed(), "JavaScript tab should be active");
    }

    @Test
    @Order(4)
    void testChangeLanguageToHTML() {
        driver.get(BASE_URL);

        // Click on HTML tab
        By htmlTabSelector = By.cssSelector("li#tab_html a");
        WebElement htmlTab = wait.until(ExpectedConditions.elementToBeClickable(htmlTabSelector));
        htmlTab.click();

        // Verify active tab is HTML
        By activeTabSelector = By.cssSelector("li#tab_html.active");
        wait.until(ExpectedConditions.presenceOfElementLocated(activeTabSelector));
        assertTrue(driver.findElement(activeTabSelector).isDisplayed(), "HTML tab should be active");
    }

    @Test
    @Order(5)
    void testChangeLanguageToCSS() {
        driver.get(BASE_URL);

        // Click on CSS tab
        By cssTabSelector = By.cssSelector("li#tab_css a");
        WebElement cssTab = wait.until(ExpectedConditions.elementToBeClickable(cssTabSelector));
        cssTab.click();

        // Verify active tab is CSS
        By activeTabSelector = By.cssSelector("li#tab_css.active");
        wait.until(ExpectedConditions.presenceOfElementLocated(activeTabSelector));
        assertTrue(driver.findElement(activeTabSelector).isDisplayed(), "CSS tab should be active");
    }

    @Test
    @Order(6)
    void testExternalResourcesLinkOpens() {
        driver.get(BASE_URL);

        // Click on Resources button
        By resourcesButtonSelector = By.cssSelector("button.resources");
        WebElement resourcesButton = wait.until(ExpectedConditions.elementToBeClickable(resourcesButtonSelector));
        resourcesButton.click();

        // Verify resources panel appears
        By resourcesPanelSelector = By.cssSelector("div#resources");
        WebElement resourcesPanel = wait.until(ExpectedConditions.presenceOfElementLocated(resourcesPanelSelector));
        assertTrue(resourcesPanel.isDisplayed(), "Resources panel should be displayed");
    }

    @Test
    @Order(7)
    void testSaveFiddleButtonIsPresent() {
        driver.get(BASE_URL);

        // Verify Save button is present
        By saveButtonSelector = By.cssSelector("button.save");
        wait.until(ExpectedConditions.presenceOfElementLocated(saveButtonSelector));
        WebElement saveButton = driver.findElement(saveButtonSelector);
        assertTrue(saveButton.isDisplayed(), "Save button should be displayed");
        assertTrue(saveButton.isEnabled(), "Save button should be enabled");
    }

    @Test
    @Order(8)
    void testLoginLinkNavigatesToLoginPage() {
        driver.get(BASE_URL);

        // Find and click login link
        By loginLinkSelector = By.linkText("Login");
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(loginLinkSelector));
        loginLink.click();

        // Wait for URL to change to login page
        wait.until(ExpectedConditions.urlContains("/login"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login"), "Expected URL to contain '/login', but was: " + currentUrl);
    }

    @Test
    @Order(9)
    void testFooterLinksArePresentAndExternal() {
        driver.get(BASE_URL);

        // List of expected footer links and their domains
        String[][] footerLinks = {
            {"GitHub", "github.com"},
            {"Twitter", "twitter.com"},
            {"Facebook", "facebook.com"},
            {"LinkedIn", "linkedin.com"},
            {"YouTube", "youtube.com"}
        };

        for (String[] linkInfo : footerLinks) {
            String linkText = linkInfo[0];
            String expectedDomain = linkInfo[1];

            By linkSelector = By.linkText(linkText);
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(linkSelector));
            String originalWindow = driver.getWindowHandle();

            // Click link that opens in new tab
            link.click();

            // Wait for new window and switch to it
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            // Assert URL contains expected domain
            String newUrl = driver.getCurrentUrl();
            assertTrue(newUrl.contains(expectedDomain), 
                       "Expected new window URL to contain '" + expectedDomain + "' but was: " + newUrl);

            // Close new tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(10)
    void testSearchFunctionality() {
        driver.get(BASE_URL);

        // Click on search input
        By searchInputSelector = By.cssSelector("input.search");
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(searchInputSelector));
        searchInput.click();
        searchInput.clear();
        searchInput.sendKeys("javascript" + Keys.RETURN);

        // Wait for results
        By resultsSelector = By.cssSelector("div.fiddles");
        wait.until(ExpectedConditions.presenceOfElementLocated(resultsSelector));

        // Verify results are shown
        List<WebElement> fiddles = driver.findElements(By.cssSelector("div.fiddle"));
        assertTrue(fiddles.size() > 0, "At least one fiddle should be displayed in search results");
    }

    @Test
    @Order(11)
    void testCreateFiddleButtonWorks() {
        driver.get(BASE_URL);

        // Click Create button
        By createButtonSelector = By.cssSelector("a.create");
        WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(createButtonSelector));
        createButton.click();

        // Wait for URL to change
        wait.until(ExpectedConditions.urlContains("/create"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/create"), "Expected URL to contain '/create', but was: " + currentUrl);
    }

    @Test
    @Order(12)
    void testTutorialsLinkIsExternal() {
        driver.get(BASE_URL);

        // Find Tutorials link
        By tutorialsLinkSelector = By.linkText("Tutorials");
        WebElement tutorialsLink = wait.until(ExpectedConditions.elementToBeClickable(tutorialsLinkSelector));
        String originalWindow = driver.getWindowHandle();

        // Click link
        tutorialsLink.click();

        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Verify it's external (jsfiddle.net tutorials subdomain or different domain)
        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains("jsfiddle") || newUrl.contains("tutorial"), 
                   "Tutorials link should navigate to a tutorial page, but was: " + newUrl);

        // Close tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(13)
    void testAboutLinkIsExternal() {
        driver.get(BASE_URL);

        // Find About link
        By aboutLinkSelector = By.linkText("About");
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(aboutLinkSelector));
        String originalWindow = driver.getWindowHandle();

        // Click link
        aboutLink.click();

        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Verify URL contains expected domain
        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains("jsfiddle") || newUrl.contains("about"), 
                   "About link should navigate to an about page, but was: " + newUrl);

        // Close tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(14)
    void testFooterLicenseLinkWorks() {
        driver.get(BASE_URL);

        // Find License link in footer
        By licenseLinkSelector = By.xpath("//footer//a[contains(text(), 'License')]");
        WebElement licenseLink = wait.until(ExpectedConditions.elementToBeClickable(licenseLinkSelector));
        licenseLink.click();

        // Wait for URL to change
        wait.until(ExpectedConditions.urlContains("/license"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/license"), "Expected URL to contain '/license', but was: " + currentUrl);
    }

    @Test
    @Order(15)
    void testAPIReferenceLinkIsExternal() {
        driver.get(BASE_URL);

        // Find API link
        By apiLinkSelector = By.linkText("API");
        WebElement apiLink = wait.until(ExpectedConditions.elementToBeClickable(apiLinkSelector));
        String originalWindow = driver.getWindowHandle();

        // Click link
        apiLink.click();

        // Switch to new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Verify URL contains expected domain
        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains("jsfiddle") || newUrl.contains("api"), 
                   "API link should navigate to an API page, but was: " + newUrl);

        // Close tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}