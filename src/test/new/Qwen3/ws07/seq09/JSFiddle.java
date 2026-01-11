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
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
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

        WebElement headerLogo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[title='JSFiddle']")));
        assertTrue(headerLogo.isDisplayed(), "Logo should be visible in header");
    }

    @Test
    @Order(2)
    void testEditorPanelsArePresent() {
        driver.get(BASE_URL);

        // Accept cookies if prompt appears
        acceptCookiesIfPresent();

        java.util.List<WebElement> panels = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".CodeMirror-code")));
        assertEquals(3, panels.size(), "There should be 3 editor panels (HTML, CSS, JS)");

        WebElement htmlPanel = driver.findElement(By.cssSelector("label[for='panel_html']"));
        assertEquals("HTML", htmlPanel.getText().trim(), "First panel should be HTML");

        WebElement cssPanel = driver.findElement(By.cssSelector("label[for='panel_css']"));
        assertEquals("CSS", cssPanel.getText().trim(), "Second panel should be CSS");

        WebElement jsPanel = driver.findElement(By.cssSelector("label[for='panel_js']"));
        assertEquals("JavaScript", jsPanel.getText().trim(), "Third panel should be JavaScript");
    }

    @Test
    @Order(3)
    void testRunButtonExecutesCode() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        // Switch to HTML frame and write simple content
        WebElement htmlEditor = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".CodeMirror.cm-s-default:nth-child(1)")));
        htmlEditor.click();
        
        // Using JavaScript to insert text because CodeMirror requires special handling
        injectTextInCodeMirror("id_code_html", "<h1>Test Heading</h1>");

        WebElement runButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.run-button")));
        runButton.click();

        // Switch to result frame
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe[name='result']")));
        
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

        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.save")));
        saveButton.click();

        // Wait for login modal
        WebElement loginModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".LoginFormPopup")));
        assertTrue(loginModal.isDisplayed(), "Login modal should appear when saving without login");
    }

    @Test
    @Order(5)
    void testSearchBoxIsFunctional() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input.search")));
        searchBox.sendKeys("jquery");
        searchBox.sendKeys(Keys.RETURN);

        // Wait for search results
        wait.until(ExpectedConditions.urlContains("/"));

        assertTrue(driver.getCurrentUrl().contains("jquery"), "Search results should be relevant to query");
    }

    @Test
    @Order(6)
    void testNavigationToAboutPage() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/about']")));
        aboutLink.click();

        wait.until(ExpectedConditions.urlContains("/about"));
        assertTrue(driver.getCurrentUrl().contains("/about"), "Should navigate to about page");

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(heading.getText().contains("About"), "About page should have correct heading");
    }

    @Test
    @Order(7)
    void testFooterResourceLinks() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement resourcesLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/resources']")));
        resourcesLink.click();

        wait.until(ExpectedConditions.urlContains("/resources"));
        assertTrue(driver.getCurrentUrl().contains("/resources"), "Should navigate to resources page");

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(heading.getText().contains("Resources"), "Resources page should have correct heading");
    }

    @Test
    @Order(8)
    void testDocumentationLinkExternal() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        String originalWindow = driver.getWindowHandle();
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='http://doc.jsfiddle.net/']")));
        docsLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("doc.jsfiddle.net"), "Documentation link should redirect to documentation domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    void testFooterGitHubLink() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        String originalWindow = driver.getWindowHandle();
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github.com']")));
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
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("twitter.com"), "Twitter link should redirect to Twitter");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(11)
    void testLoginLinkNavigatesToLoginPage() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/login/']")));
        loginLink.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"), "Should navigate to login page");

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".LoginFormPopup h1")));
        assertTrue(heading.getText().contains("Login"), "Login page should have correct heading");
    }

    @Test
    @Order(12)
    void testSignupLinkNavigatesToSignupPage() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement signupLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/register/']")));
        signupLink.click();

        wait.until(ExpectedConditions.urlContains("/register"));
        assertTrue(driver.getCurrentUrl().contains("/register"), "Should navigate to signup page");

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        assertTrue(heading.getText().contains("Sign up"), "Signup page should have correct heading");
    }

    @Test
    @Order(13)
    void testFrameworkSelectionDropdown() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement frameworkDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[name='library']")));
        Select select = new Select(frameworkDropdown);

        // Test selecting jQuery
        select.selectByValue("jquery/3.6.0");
        String selectedValue = select.getFirstSelectedOption().getAttribute("value");
        assertEquals("jquery/3.6.0", selectedValue, "Should be able to select jQuery framework");

        // Test selecting Angular
        select.selectByValue("angularjs/1.8.2");
        selectedValue = select.getFirstSelectedOption().getAttribute("value");
        assertEquals("angularjs/1.8.2", selectedValue, "Should be able to select Angular framework");
    }

    @Test
    @Order(14)
    void testEnvironmentSelection() {
        driver.get(BASE_URL);
        acceptCookiesIfPresent();

        WebElement settingsButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.configure")));
        settingsButton.click();

        WebElement darkModeCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='dark']")));
        if (!darkModeCheckbox.isSelected()) {
            darkModeCheckbox.click();
        }

        // Apply settings
        WebElement applyButton = driver.findElement(By.cssSelector("button.apply"));
        applyButton.click();

        WebElement body = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        String classAttribute = body.getAttribute("class");
        assertTrue(classAttribute.contains("dark"), "Dark theme should be applied");
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
            "if (textarea && textarea.nextElementSibling && textarea.nextElementSibling.CodeMirror) {" +
            "  textarea.nextElementSibling.CodeMirror.setValue(`%s`);" +
            "}", 
            textareaId, text.replace("`", "\\`"));
        ((JavascriptExecutor) driver).executeScript(script);
    }
}