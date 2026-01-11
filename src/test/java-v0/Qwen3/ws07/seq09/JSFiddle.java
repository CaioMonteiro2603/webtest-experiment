package Qwen3.ws07.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

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
    void testHomePageLoadsSuccessfully() {
        driver.get(BASE_URL);

        String title = driver.getTitle();
        assertTrue(title.contains("JSFiddle"), "Page title should contain 'JSFiddle'");

        WebElement headerLogo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("header .logo")));
        assertTrue(headerLogo.isDisplayed(), "Logo should be visible in header");
    }

    @Test
    @Order(2)
    void testEditorPanelsArePresent() {
        driver.get(BASE_URL);

        // Accept cookies if prompt appears
        acceptCookiesIfPresent();

        java.util.List<WebElement> panels = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".CodeMirror")));
        assertEquals(3, panels.size(), "There should be 3 editor panels (HTML, CSS, JS)");

        WebElement htmlPanel = driver.findElement(By.cssSelector("label[for='id_code_html']"));
        assertEquals("HTML", htmlPanel.getText().trim(), "First panel should be HTML");

        WebElement cssPanel = driver.findElement(By.cssSelector("label[for='id_code_css']"));
        assertEquals("CSS", cssPanel.getText().trim(), "Second panel should be CSS");

        WebElement jsPanel = driver.findElement(By.cssSelector("label[for='id_code_js']"));
        assertEquals("JavaScript", jsPanel.getText().trim(), "Third panel should be JavaScript");
    }

    @Test
    @Order(3)
    void testRunButtonExecutesCode() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        // Switch to HTML frame and write simple content
        WebElement htmlEditor = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#panel_html .CodeMirror")));
        htmlEditor.click();
        
        // Using JavaScript to insert text because CodeMirror requires special handling
        injectTextInCodeMirror("id_code_html", "<h1>Test Heading</h1>");

        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[title='Run (Ctrl + Enter)']")));
        runButton.click();

        // Switch to result frame
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("result"));
        
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Test Heading", heading.getText(), "Result should display rendered heading");
        
        // Switch back to main
        driver.switchTo().defaultContent();
    }

    @Test
    @Order(4)
    void testSaveFiddleWithoutLoginShowsLoginPrompt() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[title='Save (Ctrl + S)']")));
        saveButton.click();

        // Wait for login modal
        WebElement loginModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".login-modal")));
        assertTrue(loginModal.isDisplayed(), "Login modal should appear when saving without login");
    }

    @Test
    @Order(5)
    void testSearchBoxIsFunctional() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='search']")));
        searchBox.sendKeys("jquery");
        searchBox.sendKeys(Keys.RETURN);

        // Wait for search results
        wait.until(ExpectedConditions.urlContains("/search/"));

        assertTrue(driver.getCurrentUrl().contains("/search/"), "Should navigate to search results page");
        assertTrue(driver.getPageSource().contains("jquery"), "Search results should be relevant to query");
    }

    @Test
    @Order(6)
    void testNavigationToAboutPage() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        wait.until(ExpectedConditions.urlContains("/about/"));
        assertTrue(driver.getCurrentUrl().contains("/about/"), "Should navigate to about page");

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(heading.getText().contains("About"), "About page should have correct heading");
    }

    @Test
    @Order(7)
    void testFooterResourceLinks() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement resourcesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resources")));
        resourcesLink.click();

        wait.until(ExpectedConditions.urlContains("/resources/"));
        assertTrue(driver.getCurrentUrl().contains("/resources/"), "Should navigate to resources page");

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(heading.getText().contains("Resources"), "Resources page should have correct heading");
    }

    @Test
    @Order(8)
    void testDocumentationLinkExternal() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        String originalWindow = driver.getWindowHandle();
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Documentation")));
        docsLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("github") || url.contains("jsfiddle"), "Documentation link should redirect to documentation domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    void testFooterGitHubLink() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        String originalWindow = driver.getWindowHandle();
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='github']")));
        githubLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("github.com"), "GitHub link should redirect to GitHub");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    void testFooterTwitterLink() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='twitter']")));
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("twitter.com") || url.contains("x.com"), "Twitter link should redirect to X/Twitter");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(11)
    void testLoginLinkNavigatesToLoginPage() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Login")));
        loginLink.click();

        wait.until(ExpectedConditions.urlContains("/login/"));
        assertTrue(driver.getCurrentUrl().contains("/login/"), "Should navigate to login page");

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(heading.getText().contains("Login"), "Login page should have correct heading");
    }

    @Test
    @Order(12)
    void testSignupLinkNavigatesToSignupPage() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement signupLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign up")));
        signupLink.click();

        wait.until(ExpectedConditions.urlContains("/signup/"));
        assertTrue(driver.getCurrentUrl().contains("/signup/"), "Should navigate to signup page");

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(heading.getText().contains("Sign up"), "Signup page should have correct heading");
    }

    @Test
    @Order(13)
    void testFrameworkSelectionDropdown() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement frameworkDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[name='editor_choice']")));
        Select select = new Select(frameworkDropdown);

        // Test selecting jQuery
        select.selectByValue("jquery");
        String selectedValue = select.getFirstSelectedOption().getAttribute("value");
        assertEquals("jquery", selectedValue, "Should be able to select jQuery framework");

        // Test selecting Angular
        select.selectByValue("ng");
        selectedValue = select.getFirstSelectedOption().getAttribute("value");
        assertEquals("ng", selectedValue, "Should be able to select Angular framework");
    }

    @Test
    @Order(14)
    void testEnvironmentSelection() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement envLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/dark/']")));
        envLink.click();

        wait.until(ExpectedConditions.urlContains("/dark/"));
        assertTrue(driver.getCurrentUrl().contains("/dark/"), "Should navigate to dark theme");

        WebElement body = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        String backgroundColor = body.getCssValue("background-color");
        assertTrue(backgroundColor.equals("rgba(34, 34, 34, 1)") || 
                   backgroundColor.equals("rgb(34, 34, 34)"), "Dark theme should have dark background");
    }

    private void acceptCookiesIfPresent() {
        try {
            WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.cc-accept-button")));
            acceptButton.click();
        } catch (Exception e) {
            // Cookie banner not present, continue
        }
    }

    private void injectTextInCodeMirror(String textareaId, String text) {
        // JSFiddle uses CodeMirror which doesn't respond to normal sendKeys
        // Need to use JavaScript to set the value through CodeMirror API
        String script = String.format(
            "var textarea = document.getElementById('%s');" +
            "if (textarea && window.CodeMirror) {" +
            "  var editor = textarea.parentNode.CodeMirror;" +
            "  if (editor) {" +
            "    editor.getDoc().setValue(`%s`);" +
            "  }" +
            "}", 
            textareaId, text.replace("`", "\\`"));
        ((JavascriptExecutor) driver).executeScript(script);
    }
}