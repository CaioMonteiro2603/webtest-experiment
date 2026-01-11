package GPT20b.ws09.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

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

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                          */
    /* --------------------------------------------------------------------- */
    private void navigateToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void login(String email, String password) {
        navigateToHome();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='login'], a[ui-sref*='login'], .nav-link[href*='login']")));
        driver.findElement(By.cssSelector("a[href*='login'], a[ui-sref*='login'], .nav-link[href*='login']")).click();

        // username/ email field
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email' or @name='email' or @placeholder='Email']")));
        WebElement passwordField = driver.findElement(
                By.xpath("//input[@type='password' or @name='password' or @placeholder='Password']"));
        WebElement loginBtn = driver.findElement(
                By.xpath("//button[contains(text(),'Login') or @type='submit']"));

        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        wait.until(ExpectedConditions.urlContains("#/"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[ui-sref*='profile'], .profile, [href*='profile']")));
    }

    private void logout() {
        List<WebElement> logoutLinks = driver.findElements(By.cssSelector("[ui-sref*='logout'], .logout, a[href*='logout']"));
        if (!logoutLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLinks.get(0))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='login'], a[ui-sref*='login']")));
        }
    }

    private void openLinkAndVerifyExternal(By locator, String expectedDomain) {
        WebElement link = driver.findElement(locator);
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(expectedDomain.toLowerCase()),
                "URL should contain " + expectedDomain + " after opening external link");
        driver.close();
        driver.switchTo().window(driver.getWindowHandles().iterator().next());
    }

    private void switchToNewWindow() {
        String original = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(original)) {
                driver.switchTo().window(h);
                break;
            }
        }
    }

    private boolean isInternalLink(String href) {
        if (href == null) return false;
        href = href.trim();
        if (href.isEmpty() || href.startsWith("javascript:") || href.startsWith("mailto:")) return false;
        try {
            URI uri = new URI(href);
            if (uri.isAbsolute()) {
                URI baseUri = new URI(driver.getCurrentUrl());
                return baseUri.getHost().equalsIgnoreCase(uri.getHost());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getFirstArticleTitle() {
        List<WebElement> items = driver.findElements(By.cssSelector(".article-preview h1, .article-preview h2, .article-preview h3"));
        return items.isEmpty() ? null : items.get(0).getText().trim();
    }

    /* --------------------------------------------------------------------- */
    /* Tests                                                                  */
    /* --------------------------------------------------------------------- */
    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateToHome();
        Assertions.assertFalse(driver.getTitle().isBlank(), "Home page title should not be blank");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/"),
                "After login, URL should contain #/");
        Assertions.assertTrue(driver.findElements(By.cssSelector("[ui-sref*='profile'], .profile, [href*='profile']")).size() > 0,
                "Profile section should be visible after login");
        logout();
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        navigateToHome();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='login'], a[ui-sref*='login'], .nav-link[href*='login']")));
        driver.findElement(By.cssSelector("a[href*='login'], a[ui-sref*='login'], .nav-link[href*='login']")).click();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email' or @name='email' or @placeholder='Email']")));
        WebElement passwordField = driver.findElement(
                By.xpath("//input[@type='password' or @name='password' or @placeholder='Password']"));
        WebElement loginBtn = driver.findElement(
                By.xpath("//button[contains(text(),'Login') or @type='submit']"));

        emailField.clear();
        emailField.sendKeys("wrong@test.com");
        passwordField.clear();
        passwordField.sendKeys("wrongpass");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".error, .toast, .alert, .error-messages")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("email") || errorMsg.getText().toLowerCase().contains("password") || errorMsg.getText().toLowerCase().contains("login"),
                "Error message should mention login failure");
    }

    @Test
    @Order(4)
    public void testNavigationLinksOneLevelBelow() {
        navigateToHome();
        List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
        Assumptions.assumeTrue(!navLinks.isEmpty(), "No navigation links found; skipping test");

        String currentUrl = driver.getCurrentUrl();
        for (WebElement link : navLinks) {
            String href = link.getAttribute("href");
            if (!isInternalLink(href) || href.equals(currentUrl)) continue;
            String linkText = link.getText();
            if (linkText == null || linkText.trim().isEmpty()) continue;
            try {
                wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                wait.until(ExpectedConditions.urlToBe(href));
                Assertions.assertEquals(href, driver.getCurrentUrl(),
                        "Navigated URL should match link href: " + href);
                Assertions.assertFalse(driver.getTitle().isBlank(),
                        "Page title should not be blank after navigation");
            } catch (Exception e) {
                Assertions.fail("Navigation through link failed: " + e.getMessage());
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(currentUrl));
            }
        }
    }

    @Test
    @Order(5)
    public void testSortingDropdown() {
        login(USER_EMAIL, USER_PASSWORD);
        List<WebElement> selectElements = driver.findElements(By.tagName("select"));
        WebElement sortSelect = selectElements.stream()
                .filter(sel -> sel.findElements(By.tagName("option")).size() > 1)
                .findFirst()
                .orElse(null);
        Assumptions.assumeTrue(sortSelect != null, "No sorting dropdown found; skipping test");

        List<WebElement> options = sortSelect.findElements(By.tagName("option"));
        String firstBefore = getFirstArticleTitle();
        Assertions.assertNotNull(firstBefore, "No articles found before sorting");

        for (WebElement opt : options) {
            String optText = opt.getText();
            if (optText == null || optText.trim().isEmpty()) continue;
            wait.until(ExpectedConditions.elementToBeClickable(opt)).click();
            wait.until(d -> {
                String after = getFirstArticleTitle();
                return after != null && !after.isBlank();
            });
            String firstAfter = getFirstArticleTitle();
            Assertions.assertNotEquals(firstBefore, firstAfter,
                    "Sorting option '" + optText + "' should change order of articles");
            firstBefore = firstAfter;
        }
        logout();
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        navigateToHome();
    }

    @Test
    @Order(7)
    public void testExternalAboutLink() {
        navigateToHome();
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        Assumptions.assumeTrue(!aboutLinks.isEmpty(), "\"About\" link not found; skipping test");
        openLinkAndVerifyExternal(By.linkText("About"), "realworld.io");
    }

    @Test
    @Order(8)
    public void testLogout() {
        login(USER_EMAIL, USER_PASSWORD);
        logout();
        Assertions.assertFalse(driver.findElements(By.cssSelector("[ui-sref*='logout'], .logout, a[href*='logout']")).size() > 0,
                "Logout link should not be visible after logging out");
    }
}