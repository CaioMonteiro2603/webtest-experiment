package SunaQwen3.ws07.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";
    private static final String HOME_PAGE_URL = BASE_URL;

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
    public void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.equals(BASE_URL) || currentUrl.equals(HOME_PAGE_URL),
                "Page should load successfully and URL should match base or home URL. Current URL: " + currentUrl);
        Assertions.assertTrue(driver.getTitle().contains("JSFiddle"),
                "Page title should contain 'JSFiddle'. Actual title: " + driver.getTitle());
    }

    @Test
    @Order(2)
    public void testLoginPageValidCredentials() {
        driver.get(BASE_URL);
        // JSFiddle does not have a traditional login on the main page; login is via GitHub or Google
        // Look for login button and assert it's present
        By loginButtonSelector = By.cssSelector("a[href='/login']");
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(loginButtonSelector));
        Assertions.assertTrue(loginButton.isDisplayed(), "Login button should be visible on the homepage");

        loginButton.click();

        // Wait for login page to load
        wait.until(ExpectedConditions.urlContains("/login"));

        // Check for GitHub login option
        By githubLoginSelector = By.cssSelector("a[href*='github']");
        WebElement githubLogin = wait.until(ExpectedConditions.elementToBeClickable(githubLoginSelector));
        Assertions.assertTrue(githubLogin.isDisplayed(), "GitHub login option should be available");
    }

    @Test
    @Order(3)
    public void testLoginPageInvalidCredentials() {
        driver.get(BASE_URL + "login");
        wait.until(ExpectedConditions.urlContains("/login"));

        // Look for form fields
        By emailField = By.name("email");
        By passwordField = By.name("password");
        By submitButton = By.cssSelector("button[type='submit']");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        WebElement passwordInput = driver.findElement(passwordField);
        WebElement submit = driver.findElement(submitButton);

        // Input invalid credentials
        emailInput.sendKeys("invalid_user@example.com");
        passwordInput.sendKeys("invalid_password");

        submit.click();

        // Wait for error message
        By errorMessage = By.cssSelector(".error, .alert-danger, .notification");
        boolean errorAppears = wait.until(ExpectedConditions.textMatches(errorMessage, java.util.regex.Pattern.compile(".*(error|invalid|failed).*", java.util.regex.Pattern.CASE_INSENSITIVE)));

        Assertions.assertTrue(errorAppears, "Error message should appear for invalid login attempt");
    }

    @Test
    @Order(4)
    public void testNavigationToCreateFiddle() {
        driver.get(BASE_URL);
        By createFiddleLink = By.cssSelector("a[href='/create']");
        WebElement createLink = wait.until(ExpectedConditions.elementToBeClickable(createFiddleLink));
        createLink.click();

        wait.until(ExpectedConditions.urlContains("/create"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/create"),
                "Should navigate to /create after clicking 'Create Fiddle'");
    }

    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get(BASE_URL);
        // Test Twitter link
        testExternalLink(By.cssSelector("footer a[href*='twitter.com']"), "twitter.com");
        // Test Facebook link
        testExternalLink(By.cssSelector("footer a[href*='facebook.com']"), "facebook.com");
        // Test GitHub link
        testExternalLink(By.cssSelector("footer a[href*='github.com']"), "github.com");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();

        // Wait for new window
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        Set<String> windows = driver.getWindowHandles();
        for (String window : windows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }

        String newUrl = driver.getCurrentUrl();
        Assertions.assertTrue(newUrl.contains(expectedDomain),
                "External link should open a page containing " + expectedDomain + ". Current URL: " + newUrl);

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        By searchBox = By.name("q");
        By searchButton = By.cssSelector("input[type='submit'][value='Search']");

        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox));
        WebElement submit = driver.findElement(searchButton);

        searchInput.sendKeys("javascript");
        submit.click();

        wait.until(ExpectedConditions.urlContains("/search"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/search"),
                "Search should redirect to /search page");
    }

    @Test
    @Order(7)
    public void testFeaturedFiddlesSection() {
        driver.get(BASE_URL);
        By featuredSection = By.cssSelector("section#featured");
        WebElement section = wait.until(ExpectedConditions.visibilityOfElementLocated(featuredSection));
        Assertions.assertTrue(section.isDisplayed(), "Featured fiddles section should be visible");

        By fiddleItems = By.cssSelector("section#featured .fiddle-item");
        List<WebElement> items = driver.findElements(fiddleItems);
        Assertions.assertTrue(items.size() > 0, "There should be at least one featured fiddle item");
    }

    @Test
    @Order(8)
    public void testDocumentationLink() {
        driver.get(BASE_URL);
        By docsLink = By.cssSelector("a[href='/docs']");
        WebElement docs = wait.until(ExpectedConditions.elementToBeClickable(docsLink));
        docs.click();

        wait.until(ExpectedConditions.urlContains("/docs"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/docs"),
                "Should navigate to documentation page");
        Assertions.assertTrue(driver.getTitle().contains("Documentation"),
                "Documentation page should have 'Documentation' in title");
    }

    @Test
    @Order(9)
    public void testUserMenuNavigation() {
        // Since JSFiddle requires login for user menu, we'll test the presence of login/signup
        driver.get(BASE_URL);
        By loginLink = By.cssSelector("a[href='/login']");
        By signupLink = By.cssSelector("a[href='/signup']");

        WebElement login = wait.until(ExpectedConditions.elementToBeClickable(loginLink));
        WebElement signup = driver.findElement(signupLink);

        Assertions.assertTrue(login.isDisplayed(), "Login link should be visible");
        Assertions.assertTrue(signup.isDisplayed(), "Signup link should be visible");
    }

    @Test
    @Order(10)
    public void testFiddleEditorPageLoads() {
        // Navigate to a popular fiddle
        driver.get(BASE_URL + "example");
        By editorContainer = By.cssSelector("#result");
        wait.until(ExpectedConditions.presenceOfElementLocated(editorContainer));
        Assertions.assertTrue(driver.getCurrentUrl().contains("example") || driver.getCurrentUrl().contains("hello"),
                "Should load a fiddle example page");
    }
}