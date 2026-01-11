package GPT5.ws07.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
@TestMethodOrder(OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // -------------------- Helpers --------------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='auth']")),
                ExpectedConditions.titleContains("JSFiddle")
        ));
    }

    private void handleExternalLink(String cssSelector, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector(cssSelector));
        Assumptions.assumeTrue(!links.isEmpty(), "External link not present: " + cssSelector);
        String original = driver.getWindowHandle();
        String beforeUrl = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(links.get(0))).click();

        try {
            wait.until(d -> d.getWindowHandles().size() > 1 || !d.getCurrentUrl().equals(beforeUrl));
        } catch (TimeoutException ignored) { }

        if (driver.getWindowHandles().size() > 1) {
            for (String h : driver.getWindowHandles()) {
                if (!h.equals(original)) {
                    driver.switchTo().window(h);
                    break;
                }
            }
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to " + expectedDomain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to " + expectedDomain);
            driver.navigate().back();
        }
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void homePageLoads_keyElementsVisible() {
        openBase();
        boolean hasLogo = !driver.findElements(By.cssSelector("a.header-logo, a[href='/'")).isEmpty();
        boolean hasNav = !driver.findElements(By.cssSelector("nav, .header")).isEmpty();
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jsfiddle"), "Title should contain 'JSFiddle'"),
                () -> Assertions.assertTrue(hasLogo || hasNav, "Header or navigation should be present")
        );
    }

    @Test
    @Order(2)
    public void navigateToLogin() {
        openBase();
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='/auth/login'] , a[href*='login']")));
        loginLink.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/auth/login"),
                ExpectedConditions.urlContains("/user/login")
        ));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("/auth/login") || currentUrl.contains("/user/login"),
                "URL should contain /auth/login or /user/login after clicking Log in");
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("log in") || header.getText().toLowerCase().contains("login"),
                "Login page should have a proper header");
    }

    @Test
    @Order(3)
    public void navigateToSignUp() {
        openBase();
        WebElement signUp = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='/auth/register'], a[href*='/auth/signup'], a[href*='register'], a[href*='/user/signup']")));
        signUp.click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/auth/register"),
                ExpectedConditions.urlContains("/auth/signup"),
                ExpectedConditions.urlContains("/user/signup")
        ));
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("/auth/register") || url.contains("/auth/signup") || url.contains("/user/signup"),
                "URL should be the register/signup page");
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(header.getText().toLowerCase().contains("sign up") || header.getText().toLowerCase().contains("register"),
                "Register page should have a proper header");
    }

    @Test
    @Order(4)
    public void termsOfServiceLoads_oneLevel() {
        openBase();
        List<WebElement> tosLinks = driver.findElements(By.linkText("Terms of Service"));
        if (tosLinks.isEmpty()) tosLinks = driver.findElements(By.cssSelector("a[href*='/terms']"));
        Assumptions.assumeTrue(!tosLinks.isEmpty(), "Terms link not present");
        wait.until(ExpectedConditions.elementToBeClickable(tosLinks.get(0))).click();
        wait.until(ExpectedConditions.urlContains("/terms"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/terms"),
                "Terms of Service URL should contain /terms");
        WebElement h1 = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("terms"),
                "Terms page should have a title or header mentioning 'Terms'");
    }

    @Test
    @Order(5)
    public void privacyPolicyLoads_oneLevel() {
        openBase();
        List<WebElement> privacyLinks = driver.findElements(By.linkText("Privacy Policy"));
        if (privacyLinks.isEmpty()) privacyLinks = driver.findElements(By.cssSelector("a[href*='/privacy']"));
        Assumptions.assumeTrue(!privacyLinks.isEmpty(), "Privacy link not present");
        wait.until(ExpectedConditions.elementToBeClickable(privacyLinks.get(0))).click();
        wait.until(ExpectedConditions.urlContains("/privacy"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/privacy"),
                "Privacy Policy URL should contain /privacy");
        WebElement h1 = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(h1.getText().toLowerCase().contains("privacy"),
                "Privacy page should have a title or header mentioning 'Privacy'");
    }

    @Test
    @Order(6)
    public void externalGithubLinkOpens() {
        openBase();
        handleExternalLink("a[href*='github.com']", "github.com");
    }

    @Test
    @Order(7)
    public void externalTwitterLinkOpens() {
        openBase();
        handleExternalLink("a[href*='twitter.com']", "twitter.com");
    }

    @Test
    @Order(8)
    public void docsLinkIfPresent_isExternalAndOpens() {
        openBase();
        // Some layouts link to docs.jsfiddle.net
        List<WebElement> docs = driver.findElements(By.cssSelector("a[href*='docs.jsfiddle.net']"));
        if (!docs.isEmpty()) {
            handleExternalLink("a[href*='docs.jsfiddle.net']", "docs.jsfiddle.net");
        } else {
            Assertions.assertTrue(true, "Docs link not present; skipping");
        }
    }

    @Test
    @Order(9)
    public void loginNegativeIfFormPresent_elseValidateLoginPageContent() {
        driver.get(BASE_URL + "auth/login");
        // JSFiddle often uses 3rd-party auth; only proceed if basic email/password fields are present
        List<WebElement> emailFields = driver.findElements(By.cssSelector("input[type='email'], input[name*='email']"));
        List<WebElement> passFields = driver.findElements(By.cssSelector("input[type='password'], input[name*='password']"));
        if (!emailFields.isEmpty() && !passFields.isEmpty()) {
            WebElement email = wait.until(ExpectedConditions.elementToBeClickable(emailFields.get(0)));
            WebElement pass = passFields.get(0);
            email.clear(); email.sendKeys("invalid@example.com");
            pass.clear(); pass.sendKeys("wrongpassword");
            // Try a generic submit button
            List<WebElement> submits = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']"));
            Assumptions.assumeTrue(!submits.isEmpty(), "Submit button not present on login form");
            wait.until(ExpectedConditions.elementToBeClickable(submits.get(0))).click();
            // Expect some form of error or still on login page
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/auth/login"),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error, .alert, [role='alert']"))
            ));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/auth/login") ||
                                  !driver.findElements(By.cssSelector(".error, .alert, [role='alert']")).isEmpty(),
                    "Invalid login should not navigate away without error");
        } else {
            // Validate that the login page still has expected content (header/buttons)
            WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h1, h2, .login-header, .auth-header")));
            String headerText = header.getText().toLowerCase();
            Assertions.assertTrue(headerText.contains("log in") || headerText.contains("login") || headerText.contains("sign in"),
                    "Login page header should be present");
        }
    }

    @Test
    @Order(10)
    public void headerLinks_oneLevelNavigationWorks() {
        openBase();
        // Try clicking a couple of visible top-level links that stay within jsfiddle.net
        List<WebElement> topLinks = driver.findElements(By.cssSelector("a[href^='/']"));
        int navigations = 0;
        String original = driver.getCurrentUrl();
        for (int i = 0; i < topLinks.size(); i++) {
            WebElement l = topLinks.get(i);
            try {
                if (!l.isDisplayed()) continue;
                String href = l.getAttribute("href");
                if (href == null) continue;
                if (href.startsWith(BASE_URL) || href.startsWith("/")) {
                    wait.until(ExpectedConditions.elementToBeClickable(l)).click();
                    // must remain one level and same domain
                    wait.until(ExpectedConditions.urlContains("jsfiddle.net"));
                    Assertions.assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"),
                            "Internal navigation should remain on jsfiddle.net");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.or(
                            ExpectedConditions.urlToBe(original),
                            ExpectedConditions.titleContains("JSFiddle")
                    ));
                    navigations++;
                    if (navigations >= 2) break; // test a couple to reduce flakiness
                }
            } catch (StaleElementReferenceException e) {
                // Re-find elements after navigation
                topLinks = driver.findElements(By.cssSelector("a[href^='/']"));
                continue;
            }
        }
        Assertions.assertTrue(navigations >= 1, "At least one internal navigation link should be verified");
    }
}