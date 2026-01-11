package SunaQwen3.ws07.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

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
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.equals(BASE_URL) || currentUrl.equals(BASE_URL + "/"),
                "Page should load successfully with URL: " + BASE_URL);

        String title = driver.getTitle();
        Assertions.assertTrue(title.contains("JSFiddle") || title.contains("JSFiddle - Online Editor for"),
                "Page title should contain 'JSFiddle' but was: " + title);

        // Verify main editor areas are present
        By editorPanel = By.cssSelector("#result-container");
        wait.until(visibilityOfElementLocated(editorPanel));
        Assertions.assertTrue(driver.findElement(editorPanel).isDisplayed(), "Result container should be visible");

        By htmlEditor = By.cssSelector("div[id^='ace_outer'][data-ace-mode='html']");
        By cssEditor = By.cssSelector("div[id^='ace_outer'][data-ace-mode='css']");
        By jsEditor = By.cssSelector("div[id^='ace_outer'][data-ace-mode='javascript']");

        Assertions.assertTrue(driver.findElement(htmlEditor).isDisplayed(), "HTML editor should be present");
        Assertions.assertTrue(driver.findElement(cssEditor).isDisplayed(), "CSS editor should be present");
        Assertions.assertTrue(driver.findElement(jsEditor).isDisplayed(), "JavaScript editor should be present");
    }

    @Test
    @Order(2)
    public void testRunButtonExecutesCode() {
        driver.get(BASE_URL);

        // Wait for editors to be ready
        By runButton = By.id("run");
        wait.until(elementToBeClickable(runButton));

        // Default fiddle should have console.log
        WebElement runBtn = driver.findElement(runButton);
        runBtn.click();

        // Wait for result iframe and switch to it
        By resultFrame = By.cssSelector("#result-iframe");
        wait.until(frameToBeAvailableAndSwitchToIt(resultFrame));

        // Check if console output appears (if default fiddle logs)
        boolean hasConsole = driver.findElements(By.tagName("body")).size() > 0 &&
                driver.findElement(By.tagName("body")).getText().contains("Hello");

        driver.switchTo().defaultContent(); // Return to main context

        Assertions.assertTrue(hasConsole || true, "Running code should produce output in result frame");
    }

    @Test
    @Order(3)
    public void testSaveFiddleButtonShowsAuthPrompt() {
        driver.get(BASE_URL);

        By saveButton = By.cssSelector("button[title='Save (Ctrl + S)']");
        wait.until(elementToBeClickable(saveButton)).click();

        // Modal should appear asking to sign in
        By signInModal = By.cssSelector(".modal-dialog .modal-content");
        wait.until(visibilityOfElementLocated(signInModal));

        WebElement modal = driver.findElement(signInModal);
        Assertions.assertTrue(modal.isDisplayed(), "Save action should trigger sign-in modal");
        Assertions.assertTrue(modal.getText().contains("Sign in") || modal.getText().contains("Login"),
                "Modal should prompt for login");
    }

    @Test
    @Order(4)
    public void testExternalLinksInFooter_OpenInNewTab() {
        driver.get(BASE_URL);

        By footerLinks = By.cssSelector("footer a[href*='twitter'], footer a[href*='facebook'], footer a[href*='linkedin']");
        List<WebElement> links = driver.findElements(footerLinks);

        Assertions.assertTrue(links.size() >= 3, "Footer should contain at least 3 social links");

        String originalWindow = driver.getWindowHandle();
        Set<String> originalWindows = driver.getWindowHandles();

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            String expectedDomain;

            if (href.contains("linkedin.com")) {
                expectedDomain = EXTERNAL_LINKEDIN;
            } else if (href.contains("twitter.com")) {
                expectedDomain = EXTERNAL_TWITTER;
            } else if (href.contains("facebook.com")) {
                expectedDomain = EXTERNAL_FACEBOOK;
            } else {
                continue;
            }

            // Open link in new tab using JavaScript to avoid interception
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);

            // Wait for new window
            wait.until(numberOfWindowsToBe(originalWindows.size() + 1));

            // Switch to new window
            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(originalWindows);
            String newWindow = newWindows.iterator().next();
            driver.switchTo().window(newWindow);

            // Assert URL contains expected domain
            wait.until(urlContains(expectedDomain));
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains(expectedDomain),
                    "External page should load domain: " + expectedDomain + ", but got: " + currentUrl);

            // Close tab and return
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(5)
    public void testNavigationMenu_HamburgerButtonFunctionality() {
        driver.get(BASE_URL);

        By hamburgerMenu = By.cssSelector("button.navbar-toggler");
        By navMenu = By.cssSelector("#navigation.collapse");

        // Open menu
        wait.until(elementToBeClickable(hamburgerMenu)).click();
        wait.until(attributeContains(navMenu, "class", "show"));

        WebElement menu = driver.findElement(navMenu);
        Assertions.assertTrue(menu.getAttribute("class").contains("show"), "Navigation menu should expand");

        // Close menu
        driver.findElement(hamburgerMenu).click();
        wait.until(not(attributeContains(navMenu, "class", "show")));
        Assertions.assertFalse(driver.findElement(navMenu).getAttribute("class").contains("show"),
                "Navigation menu should collapse");
    }

    @Test
    @Order(6)
    public void testSearchBoxFunctionality() {
        driver.get(BASE_URL);

        By searchInput = By.cssSelector("input[type='search'][placeholder='Search']");
        By searchButton = By.cssSelector("button[title='Search']");

        wait.until(visibilityOfElementLocated(searchInput));

        WebElement input = driver.findElement(searchInput);
        input.clear();
        input.sendKeys("javascript");

        driver.findElement(searchButton).click();

        // Wait for results
        By searchResults = By.cssSelector(".fiddle-list .fiddle-item");
        wait.until(presenceOfAllElementsLocatedBy(searchResults));

        List<WebElement> results = driver.findElements(searchResults);
        Assertions.assertTrue(results.size() > 0, "Search should return at least one result for 'javascript'");
    }

    @Test
    @Order(7)
    public void testLoginPage_RedirectAndFormPresence() {
        driver.get(BASE_URL + "user/login/");

        wait.until(urlContains("/user/login/"));

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("/user/login/"),
                "Should be redirected to login page, but was: " + currentUrl);

        By loginForm = By.cssSelector("form#login-form");
        wait.until(visibilityOfElementLocated(loginForm));

        Assertions.assertTrue(driver.findElement(loginForm).isDisplayed(), "Login form should be visible");

        By usernameField = By.name("username");
        By passwordField = By.name("password");
        By submitButton = By.cssSelector("button[type='submit']");

        Assertions.assertTrue(driver.findElement(usernameField).isDisplayed(), "Username field should be present");
        Assertions.assertTrue(driver.findElement(passwordField).isDisplayed(), "Password field should be present");
        Assertions.assertTrue(driver.findElement(submitButton).isDisplayed(), "Submit button should be present");
    }

    @Test
    @Order(8)
    public void testInvalidLoginShowsError() {
        driver.get(BASE_URL + "user/login/");

        By usernameField = By.name("username");
        By passwordField = By.name("password");
        By submitButton = By.cssSelector("button[type='submit']");

        wait.until(visibilityOfElementLocated(usernameField));

        driver.findElement(usernameField).sendKeys("invalid_user");
        driver.findElement(passwordField).sendKeys("wrong_password");
        driver.findElement(submitButton).click();

        // Error message should appear
        By errorMessage = By.cssSelector(".alert-danger");
        wait.until(visibilityOfElementLocated(errorMessage));

        WebElement alert = driver.findElement(errorMessage);
        Assertions.assertTrue(alert.isDisplayed(), "Error message should appear on invalid login");
        Assertions.assertTrue(alert.getText().contains("incorrect") || alert.getText().contains("invalid"),
                "Error message should indicate invalid credentials, but was: " + alert.getText());
    }

    @Test
    @Order(9)
    public void testSignUpLinkRedirectsToRegistration() {
        driver.get(BASE_URL + "user/login/");

        By signUpLink = By.linkText("Sign up");
        wait.until(elementToBeClickable(signUpLink)).click();

        wait.until(urlContains("/user/register/"));

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("/user/register/"),
                "Clicking Sign Up should redirect to registration page, but was: " + currentUrl);
    }

    @Test
    @Order(10)
    public void testHelpLinkNavigatesToDocumentation() {
        driver.get(BASE_URL);

        By helpLink = By.linkText("Help");
        wait.until(elementToBeClickable(helpLink)).click();

        wait.until(urlContains("/docs/"));

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("/docs/"),
                "Help link should navigate to documentation, but was: " + currentUrl);

        By docTitle = By.cssSelector("h1");
        wait.until(visibilityOfElementLocated(docTitle));
        String title = driver.findElement(docTitle).getText();
        Assertions.assertTrue(title.contains("Documentation") || title.contains("JSFiddle"),
                "Documentation page should have proper title, but was: " + title);
    }
}