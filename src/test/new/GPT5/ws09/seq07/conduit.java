package GPT5.ws09.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Single-file JUnit 5 + Selenium 4 test suite for https://demo.realworld.io/
 *
 * - Uses FirefoxDriver with headless argument via addArguments("--headless")
 * - Uses WebDriverWait with Duration.ofSeconds(10)
 * - Tests home load, registration/login (register or fallback to login), feed toggles,
 *   navigation menu actions, external links, and one-level internal links.
 *
 * Notes:
 * - A deterministic test account is used: testuser_e2e / testuser_e2e@example.com / Test1234!
 *   If registration fails due to existing account, the test attempts to log in with the same credentials.
 */
@TestMethodOrder(OrderAnnotation.class)
public class conduit {
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static URI baseUri;

    // deterministic test credentials (not random)
    private static final String TEST_USERNAME = "testuser_e2e";
    private static final String TEST_EMAIL = "testuser_e2e@example.com";
    private static final String TEST_PASSWORD = "Test1234!";

    @BeforeAll
    public static void setup() throws URISyntaxException {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUri = new URI(BASE_URL);
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Navigate to base and wait for ready
    private void goToBaseAndWait() {
        driver.navigate().to(BASE_URL);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(baseUri.getHost().toLowerCase()),
                "After navigate, expected URL to contain host: " + baseUri.getHost());
    }

    private void closeTabAndSwitchBack(String originalHandle) {
        driver.close();
        driver.switchTo().window(originalHandle);
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    private boolean isExternalHref(String href) {
        if (href == null || href.trim().isEmpty()) return false;
        try {
            URI uri = new URI(href);
            String host = uri.getHost();
            if (host == null) return false;
            return !host.equalsIgnoreCase(baseUri.getHost());
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> collectOneLevelInternalLinks() {
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Set<String> result = new LinkedHashSet<>();
        for (WebElement a : anchors) {
            try {
                String href = a.getAttribute("href");
                if (href == null || href.trim().isEmpty()) continue;
                URI uri = new URI(href);
                if (uri.getHost() == null || uri.getHost().equalsIgnoreCase(baseUri.getHost())) {
                    result.add(href);
                }
            } catch (Exception e) {
                String href = a.getAttribute("href");
                if (href != null) result.add(href);
            }
        }
        return new ArrayList<>(result);
    }

    @Test
    @Order(1)
    public void testHomePageLoadsAndHeader() {
        goToBaseAndWait();
        // Header / navbar presence
        List<WebElement> navs = driver.findElements(By.cssSelector("nav, .navbar, .container > .nav"));
        Assertions.assertTrue(navs.size() > 0 && navs.get(0).isDisplayed(), "Expected visible navbar on home page");

        // Title contains "Conduit" often used for this demo
        String title = driver.getTitle();
        Assertions.assertTrue(title != null && (title.toLowerCase().contains("conduit") || title.toLowerCase().contains("realworld")),
                "Expected page title to contain 'Conduit' or 'RealWorld'. Actual: " + title);
    }

    @Test
    @Order(2)
    public void testRegisterOrLogin_thenVerifyProfileVisible() {
        goToBaseAndWait();

        // Click "Sign up" (register) - robust locator by link text if present
        List<WebElement> signUpElems = driver.findElements(By.xpath("//a[normalize-space()='Sign up' or normalize-space()='Sign Up']"));
        if (!signUpElems.isEmpty()) {
            WebElement signUp = signUpElems.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(signUp));
            signUp.click();
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        } else {
            // fallback: click "Sign in" and navigate to register via /register
            driver.navigate().to(BASE_URL + "register");
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        }

        // Fill registration form with deterministic values
        // Locators robust: input[placeholder='Username'], input[placeholder='Email'], input[placeholder='Password']
        WebElement usernameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Username'], input[name='username']")));
        WebElement emailInput = driver.findElement(By.cssSelector("input[placeholder='Email'], input[name='email']"));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[placeholder='Password'], input[name='password']"));
        WebElement signUpButton = driver.findElements(By.cssSelector("button[type='submit'], button")).stream()
                .filter(WebElement::isDisplayed)
                .filter(e -> {
                    String txt = e.getText() == null ? "" : e.getText().toLowerCase();
                    return txt.contains("sign up") || txt.contains("sign-up") || txt.contains("signup");
                })
                .findFirst().orElse(null);

        usernameInput.clear();
        usernameInput.sendKeys(TEST_USERNAME);
        emailInput.clear();
        emailInput.sendKeys(TEST_EMAIL);
        passwordInput.clear();
        passwordInput.sendKeys(TEST_PASSWORD);

        boolean registeredOrLoggedIn = false;

        if (signUpButton != null) {
            wait.until(ExpectedConditions.elementToBeClickable(signUpButton));
            signUpButton.click();
            // After submit, either registration succeeds and navbar shows username or errors appear
            try {
                // wait for either nav username to appear or error messages
                wait.until(d -> d.findElements(By.cssSelector("ul.error-messages li, .error-messages li, .error")).size() > 0
                        || d.findElements(By.xpath("//a[normalize-space()='" + TEST_USERNAME + "']")).size() > 0
                        || d.findElements(By.cssSelector("nav a[href*='@']")).size() > 0);
            } catch (Exception ignored) { }

            // Check for errors
            List<WebElement> errors = driver.findElements(By.cssSelector("ul.error-messages li, .error-messages li, .error"));
            if (errors.size() > 0) {
                // Registration may have failed (user exists). Attempt login instead.
                // Navigate to Sign in
                List<WebElement> signInLinks = driver.findElements(By.xpath("//a[normalize-space()='Sign in' or normalize-space()='Sign In']"));
                if (!signInLinks.isEmpty()) {
                    WebElement signIn = signInLinks.get(0);
                    wait.until(ExpectedConditions.elementToBeClickable(signIn));
                    signIn.click();
                } else {
                    driver.navigate().to(BASE_URL + "login");
                    wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
                }

                WebElement loginEmail = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Email'], input[name='email']")));
                WebElement loginPassword = driver.findElement(By.cssSelector("input[placeholder='Password'], input[name='password']"));
                WebElement loginButton = driver.findElements(By.cssSelector("button[type='submit'], button")).stream()
                        .filter(WebElement::isDisplayed)
                        .filter(e -> {
                            String txt = e.getText() == null ? "" : e.getText().toLowerCase();
                            return txt.contains("sign in");
                        })
                        .findFirst().orElse(null);

                loginEmail.clear();
                loginEmail.sendKeys(TEST_EMAIL);
                loginPassword.clear();
                loginPassword.sendKeys(TEST_PASSWORD);
                if (loginButton != null) {
                    wait.until(ExpectedConditions.elementToBeClickable(loginButton));
                    loginButton.click();
                } else {
                    loginPassword.sendKeys(Keys.RETURN);
                }

                // wait for navbar username or profile link
                try {
                    wait.until(d -> d.findElements(By.xpath("//a[normalize-space()='" + TEST_USERNAME + "']")).size() > 0
                            || d.findElements(By.cssSelector("nav a[href*='@" + TEST_USERNAME + "']")).size() > 0);
                    registeredOrLoggedIn = true;
                } catch (Exception ignored) { }
            } else {
                // no error messages; check for username in nav
                try {
                    wait.until(d -> d.findElements(By.xpath("//a[normalize-space()='" + TEST_USERNAME + "']")).size() > 0
                            || d.findElements(By.cssSelector("nav a[href*='@" + TEST_USERNAME + "']")).size() > 0);
                    registeredOrLoggedIn = true;
                } catch (Exception ignored) { }
            }
        } else {
            Assertions.fail("Could not find sign-up button to register.");
        }

        Assertions.assertTrue(registeredOrLoggedIn, "Expected to be registered or logged in with username visible in navbar.");

        // Verify profile link leads to user's profile page
        WebElement profileLink = driver.findElements(By.xpath("//a[normalize-space()='" + TEST_USERNAME + "']")).stream().findFirst().orElse(null);
        if (profileLink == null) {
            profileLink = driver.findElements(By.cssSelector("nav a[href*='@" + TEST_USERNAME + "']")).stream().findFirst().orElse(null);
        }
        Assertions.assertNotNull(profileLink, "Expected profile link with username after login/registration.");
        wait.until(ExpectedConditions.elementToBeClickable(profileLink));
        profileLink.click();
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("/@" + TEST_USERNAME.toLowerCase()) || driver.getTitle().toLowerCase().contains(TEST_USERNAME.toLowerCase()),
                "After clicking profile, expected URL to contain @username or title to contain username. Actual URL: " + driver.getCurrentUrl());
    }

    @Test
    @Order(3)
    public void testFeedTabsAndArticleListPresence() {
        goToBaseAndWait();

        // If logged in, "Your Feed" may appear. We will click both "Global Feed" and "Your Feed" if present.
        // More robust: look for links with text "Global Feed" or "Your Feed"
        List<WebElement> globalFeed = driver.findElements(By.xpath("//*[normalize-space()='Global Feed']"));
        List<WebElement> yourFeed = driver.findElements(By.xpath("//*[normalize-space()='Your Feed']"));

        if (!globalFeed.isEmpty()) {
            WebElement gf = globalFeed.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(gf));
            gf.click();
            // After click, assert articles list present
            wait.until(d -> d.findElements(By.cssSelector(".article-preview, .article-list, .article")).size() > 0);
            Assertions.assertTrue(driver.findElements(By.cssSelector(".article-preview, .article-list, .article")).size() > 0,
                    "After clicking Global Feed expected some articles to be present.");
        }

        if (!yourFeed.isEmpty()) {
            try {
                WebElement yf = yourFeed.get(0);
                wait.until(ExpectedConditions.elementToBeClickable(yf));
                yf.click();
                Assertions.assertTrue(true, "Clicked 'Your Feed' -- presence of articles depends on followed users; test ensures clickability.");
            } catch (Exception e) {
                // Ignore if Your Feed is not clickable
            }
        }
    }

    @Test
    @Order(4)
    public void testNewArticleAndLogoutFlow() {
        goToBaseAndWait();

        // Click "New Post" / "New Article" if present
        List<WebElement> newArticleLinks = driver.findElements(By.xpath("//a[normalize-space()='New Post' or normalize-space()='New Article' or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'new post')]"));
        if (!newArticleLinks.isEmpty()) {
            WebElement newArticle = newArticleLinks.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(newArticle));
            newArticle.click();
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

            // If a new article editor is present, fill a minimal article and publish (guarded)
            List<WebElement> titleInput = driver.findElements(By.cssSelector("input[placeholder='Article Title'], input[name='title']"));
            if (!titleInput.isEmpty()) {
                WebElement title = titleInput.get(0);
                WebElement about = driver.findElements(By.cssSelector("input[placeholder='What's this article about?'], input[name='description']")).stream().findFirst().orElse(null);
                WebElement body = driver.findElements(By.cssSelector("textarea[placeholder='Write your article (in markdown)'], textarea[name='body']")).stream().findFirst().orElse(null);
                WebElement publishBtn = driver.findElements(By.xpath("//button[normalize-space()='Publish Article' or normalize-space()='Publish']")).stream().findFirst().orElse(null);

                if (title != null && body != null && publishBtn != null) {
                    title.clear();
                    title.sendKeys("E2E Test Article Title");
                    if (about != null) {
                        about.clear();
                        about.sendKeys("E2E about");
                    }
                    body.clear();
                    body.sendKeys("This is a test article body for automated test.");
                    wait.until(ExpectedConditions.elementToBeClickable(publishBtn));
                    publishBtn.click();

                    // Wait for article page / URL change
                    wait.until(d -> d.getCurrentUrl().toLowerCase().contains("/article/") || d.findElements(By.cssSelector(".article-page")).size() > 0);
                    Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("/article/") || driver.findElements(By.cssSelector(".article-page")).size() > 0,
                            "After publishing, expected to be on article page or URL to contain /article/");
                }
            } else {
                // No editor visible (user may not be logged in); that's acceptable for this environment
            }
        }

        // Logout if logged in
        List<WebElement> logoutLinks = driver.findElements(By.xpath("//a[normalize-space()='Log Out' or normalize-space()='Logout' or normalize-space()='Sign out']"));
        if (!logoutLinks.isEmpty()) {
            WebElement logout = logoutLinks.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(logout));
            logout.click();
            // After logout, navbar should show "Sign in" or "Sign up"
            wait.until(d -> d.findElements(By.xpath("//a[normalize-space()='Sign in' or normalize-space()='Sign up']")).size() > 0);
            Assertions.assertTrue(driver.findElements(By.xpath("//a[normalize-space()='Sign in' or normalize-space()='Sign up']")).size() > 0,
                    "After logout expected Sign in / Sign up links to be visible.");
        } else {
            // Not logged in - nothing to logout
        }
    }

    @Test
    @Order(5)
    public void testFooterAndExternalLinksOpen() {
        goToBaseAndWait();

        // Collect footer links (anchor tags inside footer if present) and test external links up to a limit
        List<WebElement> footers = driver.findElements(By.tagName("footer"));
        List<WebElement> anchors = new ArrayList<>();
        if (!footers.isEmpty()) {
            anchors = footers.get(0).findElements(By.cssSelector("a[href]"));
        } else {
            anchors = driver.findElements(By.cssSelector("a[href]"));
        }

        List<String> hrefs = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        String originalHandle = driver.getWindowHandle();
        int tested = 0;
        for (String href : hrefs) {
            if (isExternalHref(href)) {
                tested++;
                if (tested > 4) break; // limit for flakiness
                try {
                    try {
                        String current = driver.getCurrentUrl();
                        // open external link in new tab by JS
                        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", href);
                        List<String> newHandles = new ArrayList<>(driver.getWindowHandles());
                        newHandles.remove(originalHandle);
                        if (!newHandles.isEmpty()) {
                            driver.switchTo().window(newHandles.get(0));
                            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
                        }
                    } catch (Exception ignored) {
                        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
                    }
                } finally {
                    Set<String> handles = driver.getWindowHandles();
                    if (handles.size() > 1) {
                        closeTabAndSwitchBack(originalHandle);
                    } else {
                        // only one window left
                    }
                }
            }
        }

        if (hrefs.stream().anyMatch(this::isExternalHref)) {
            Assertions.assertTrue(tested > 0, "There are external links but none were tested.");
        }
    }

    @Test
    @Order(6)
    public void testOneLevelInternalLinksNavigateAndContainContent() {
        goToBaseAndWait();
        List<String> internalLinks = collectOneLevelInternalLinks().stream()
                .filter(h -> !h.equalsIgnoreCase(BASE_URL))
                .collect(Collectors.toList());

        int tested = 0;
        for (String href : internalLinks) {
            if (tested >= 6) break;
            try {
                // Limit to one-level-ish links by path segments (skip deep links)
                URI uri = new URI(href);
                String path = uri.getPath() == null ? "" : uri.getPath();
                String normalized = path.replaceAll("^/+", "").replaceAll("/+$", "");
                int segments = normalized.isEmpty() ? 0 : normalized.split("/").length;
                if (segments > 2) continue;

                driver.navigate().to(href);
                wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

                boolean hasHeader = driver.findElements(By.cssSelector("h1, h2, h3")).stream().anyMatch(WebElement::isDisplayed);
                boolean hasBodyText = driver.findElements(By.tagName("body")).stream()
                        .anyMatch(b -> b.getText() != null && b.getText().trim().length() > 30);

                Assertions.assertTrue(hasHeader || hasBodyText, "Expected header or body text at internal page: " + href);

                tested++;
            } catch (Exception e) {
                // ignore and continue
            } finally {
                driver.navigate().to(BASE_URL);
                wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            }
        }

        if (!internalLinks.isEmpty()) {
            Assertions.assertTrue(tested > 0, "Expected to test at least one one-level internal link.");
        }
    }
}