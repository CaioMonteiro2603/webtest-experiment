package Qwen3.ws09.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;

    private final String BASE_URL = "https://demo.realworld.io/";

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
    void testHomePageTitleAndGlobalFeed_Displayed() {
        driver.get(BASE_URL);

        assertEquals("Conduit", driver.getTitle(), "Page title should be 'Conduit'");
        WebElement mainTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".navbar-brand")));
        assertEquals("conduit", mainTitle.getText().toLowerCase(), "Header brand text should be 'conduit'");
        assertTrue(isElementPresent(By.cssSelector(".feed-toggle")), "Feed toggle (Global/Mine) should be present");
        assertTrue(isElementPresent(By.cssSelector(".article-preview")), "At least one article should be visible in global feed");
    }

    @Test
    @Order(2)
    void testHeaderNavigation_SignInAndSignUpLinksPresent() {
        driver.get(BASE_URL);

        WebElement navbar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".nav")));
        String navbarText = navbar.getText();
        assertTrue(navbarText.contains("Sign in"), "Header should contain 'Sign in' link");
        assertTrue(navbarText.contains("Sign up"), "Header should contain 'Sign up' link");
    }

    @Test
    @Order(3)
    void testSignInLink_NavigatesToSignInPage() {
        driver.get(BASE_URL);

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        signInLink.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"), "Sign in link should redirect to login page");
        assertTrue(isElementPresent(By.name("email")), "Login form should contain email field");
        assertTrue(isElementPresent(By.name("password")), "Login form should contain password field");
    }

    @Test
    @Order(4)
    void testInvalidLogin_ShowErrorMessages() {
        driver.get(BASE_URL + "/#/login");

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpass");
        signInButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        assertTrue(error.isDisplayed(), "Error messages block should be displayed");
        assertTrue(error.getText().contains("invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(5)
    void testSignUpLink_NavigatesToRegisterPage() {
        driver.get(BASE_URL);

        WebElement signUpLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign up")));
        signUpLink.click();

        wait.until(ExpectedConditions.urlContains("/register"));
        assertTrue(driver.getCurrentUrl().contains("/register"), "Sign up link should navigate to registration page");
        assertTrue(isElementPresent(By.name("username")), "Registration form should contain username field");
        assertTrue(isElementPresent(By.name("email")), "Registration form should contain email field");
        assertTrue(isElementPresent(By.name("password")), "Registration form should contain password field");
    }

    @Test
    @Order(6)
    void testRegisterNewUser_WithValidData_ShowsErrorForNow() {
        driver.get(BASE_URL + "/#/register");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement registerButton = driver.findElement(By.cssSelector("button[type='submit']"));

        String username = "Tester" + System.currentTimeMillis();
        String email = "tester_" + System.currentTimeMillis() + "@test.com";
        String password = "password123";

        usernameField.sendKeys(username);
        emailField.sendKeys(email);
        passwordField.sendKeys(password);
        registerButton.click();

        // Note: RealWorld demo doesn't allow registration by default (API returns 403)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        WebElement error = driver.findElement(By.cssSelector(".error-messages"));
        assertTrue(error.isDisplayed(), "Error should be shown due to registration being disabled");
        assertTrue(error.getText().contains("username") || error.getText().contains("email"),
                   "Error message should indicate registration issue");
    }

    @Test
    @Order(7)
    void testTagNavigation_FilterArticlesByTag() {
        driver.get(BASE_URL);

        // Wait for tags to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".sidebar")));

        java.util.List<WebElement> tags = driver.findElements(By.cssSelector(".sidebar .tag-list a"));
        assertFalse(tags.isEmpty(), "At least one tag should be available");

        WebElement firstTag = tags.get(0);
        String tagName = firstTag.getText();
        firstTag.click();

        // Wait for filtered feed
        wait.until(ExpectedConditions.urlContains("/tag/" + tagName.toLowerCase()));

        java.util.List<WebElement> articleTags = driver.findElements(By.cssSelector(".tag-list a"));
        boolean anyMatch = false;
        for (WebElement tag : articleTags) {
            if (tag.getText().equalsIgnoreCase(tagName)) {
                anyMatch = true;
                break;
            }
        }
        assertTrue(anyMatch, "At least one article should have the selected tag: " + tagName);
    }

    @Test
    @Order(8)
    void testArticlePreview_ClickOpensArticleDetail() {
        driver.get(BASE_URL);

        WebElement firstArticleLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".article-preview a.preview-link h1")));
        String expectedTitle = firstArticleLink.getText();
        firstArticleLink.click();

        // Wait for detail page
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".article-page")));

        WebElement actualTitle = driver.findElement(By.cssSelector("h1"));
        assertEquals(expectedTitle, actualTitle.getText(), "Article detail should show correct title");
        assertTrue(isElementPresent(By.cssSelector(".article-meta")), "Article meta (author, date) should be visible");
    }

    @Test
    @Order(9)
    void testUserProfile_ClickNavigatesToProfile() {
        driver.get(BASE_URL);

        // Get first article author
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".article-meta a")));
        WebElement authorLink = driver.findElement(By.cssSelector(".article-meta a"));
        String authorName = authorLink.getText();

        authorLink.click();

        // Wait for profile
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".profile-page")));
        WebElement profileName = driver.findElement(By.cssSelector("h4"));
        assertEquals(authorName, profileName.getText(), "Profile should display correct username");
    }

    @Test
    @Order(10)
    void testFooterGitHubLink_OpenInNewTab() {
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github']")));
        String originalWindow = driver.getWindowHandle();

        githubLink.sendKeys(Keys.CONTROL, Keys.RETURN);

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("github"));
                assertTrue(driver.getCurrentUrl().contains("github"), "GitHub link should open github domain");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback: same tab
        driver.navigate().refresh();
        githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github']")));
        githubLink.click();
        wait.until(ExpectedConditions.urlContains("github"));
        assertTrue(driver.getCurrentUrl().contains("github"), "GitHub link should redirect to github");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(11)
    void testFooterMediumLink_OpenInNewTab() {
        driver.get(BASE_URL);
        WebElement mediumLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='medium']")));
        String originalWindow = driver.getWindowHandle();

        mediumLink.sendKeys(Keys.CONTROL, Keys.RETURN);

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("medium"));
                assertTrue(driver.getCurrentUrl().contains("medium"), "Medium link should open medium domain");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback
        driver.navigate().refresh();
        mediumLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='medium']")));
        mediumLink.click();
        wait.until(ExpectedConditions.urlContains("medium"));
        assertTrue(driver.getCurrentUrl().contains("medium"), "Medium link should redirect to medium");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(12)
    void testHomeLink_NavigatesToHomePage() {
        // Go to an article
        testArticlePreview_ClickOpensArticleDetail();

        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-brand")));
        homeLink.click();

        wait.until(ExpectedConditions.urlToBe("https://demo.realworld.io/#/"));
        assertTrue(isElementPresent(By.cssSelector(".article-preview")), "Should be back on home feed with articles");
    }

    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}