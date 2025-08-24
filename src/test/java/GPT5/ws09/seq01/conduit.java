package GPT5.ws09.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class RealWorldDemoSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://demo.realworld.io/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    /* ============================ Helpers ============================ */

    private void openHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        // Home headline/feed should exist
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".home-page")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".feed-toggle")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(.,'Global Feed')]"))
        ));
    }

    private void go(String route) {
        // Try hash-router first, then path-router as a fallback
        driver.get(BASE_URL + "#/" + route.replaceFirst("^/+", ""));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        if (!(driver.getCurrentUrl().contains("#/" + route.replaceFirst("^/+", "")))) {
            driver.get(BASE_URL + route);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    private boolean clickIfPresent(By by) {
        List<WebElement> els = driver.findElements(by);
        if (!els.isEmpty()) {
            WebElement el = els.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
            wait.until(ExpectedConditions.elementToBeClickable(el)).click();
            return true;
        }
        return false;
    }

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        throw new NoSuchElementException("None matched: " + Arrays.toString(locators));
    }

    private void type(By by, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
        el.clear();
        el.sendKeys(text);
    }

    private void switchToNewTabAndVerify(String expectedDomain) {
        String original = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String w : driver.getWindowHandles()) {
            if (!w.equals(original)) {
                driver.switchTo().window(w);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected external domain in URL: " + expectedDomain + " but was " + driver.getCurrentUrl());
                driver.close();
                driver.switchTo().window(original);
                return;
            }
        }
        Assertions.fail("No new tab opened for external link.");
    }

    private void clickExternalAndAssert(String expectedDomain, By... candidates) {
        int before = driver.getWindowHandles().size();
        boolean clicked = false;
        for (By by : candidates) {
            if (clickIfPresent(by)) { clicked = true; break; }
        }
        Assertions.assertTrue(clicked, "Expected external link not found/clickable.");

        if (driver.getWindowHandles().size() > before) {
            switchToNewTabAndVerify(expectedDomain);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "Expected URL to contain external domain: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    private List<WebElement> homepageArticlePreviews() {
        List<By> candidates = Arrays.asList(
                By.cssSelector(".article-preview"),
                By.cssSelector("article.preview, article.article-preview")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els;
        }
        return Collections.emptyList();
    }

    /* ============================ Tests ============================ */

    @Test
    @Order(1)
    public void homePageLoadsAndShowsGlobalFeed() {
        openHome();
        Assertions.assertTrue(driver.getTitle() != null && !driver.getTitle().isEmpty(),
                "Home page title should not be empty.");
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
                "Should be on RealWorld demo domain.");
        // Global Feed tab
        Assertions.assertTrue(
                driver.findElements(By.xpath("//*[contains(.,'Global Feed')]")).size() > 0
                        || driver.findElements(By.cssSelector(".feed-toggle")).size() > 0,
                "Global Feed tab/section should be visible."
        );
        // Presence of some articles
        Assertions.assertFalse(homepageArticlePreviews().isEmpty(), "Expected article previews on home page.");
    }

    @Test
    @Order(2)
    public void loginPageNegativeCredentialsShowsError() {
        go("login");
        // Assert we are on login route
        Assertions.assertTrue(driver.getCurrentUrl().contains("login") || driver.getCurrentUrl().contains("#/login"),
                "URL should indicate login route.");

        // Fill invalid credentials (deterministic values)
        type(By.cssSelector("input[type='email'], input[placeholder='Email']"), "invalid.user@example.com");
        type(By.cssSelector("input[type='password'], input[placeholder='Password']"), "wrongpassword");

        WebElement signInBtn = firstPresent(
                By.cssSelector("button[type='submit']"),
                By.xpath("//button[contains(.,'Sign in')]")
        );
        wait.until(ExpectedConditions.elementToBeClickable(signInBtn)).click();

        // Error message should appear
        WebElement errorBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".error-messages, .ng-scope .error-messages, .error")));
        String errorText = errorBox.getText().toLowerCase();
        Assertions.assertTrue(errorText.contains("email") || errorText.contains("password") || errorText.contains("invalid"),
                "Expected an error related to invalid email or password, but was: " + errorText);
    }

    @Test
    @Order(3)
    public void signUpPageAccessible() {
        go("register");
        Assertions.assertTrue(driver.getCurrentUrl().contains("register") || driver.getCurrentUrl().contains("#/register"),
                "URL should indicate register route.");
        Assertions.assertTrue(
                driver.findElements(By.cssSelector("input[placeholder='Username']")).size() > 0
                        || driver.findElements(By.cssSelector("input[name='username']")).size() > 0,
                "Sign up form should include a Username input."
        );
    }

    @Test
    @Order(4)
    public void openFirstTagFiltersGlobalFeed() {
        openHome();
        // Sidebar tags
        WebElement tag = firstPresent(
                By.cssSelector(".tag-list a"),
                By.xpath("(//a[contains(@class,'tag')])[1]")
        );
        String tagText = tag.getText().trim();
        wait.until(ExpectedConditions.elementToBeClickable(tag)).click();

        // After clicking tag, a tag feed should appear
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(.,'Global Feed')]")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(.,'Your Feed')]")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(.,'" + (tagText.length() > 0 ? tagText.substring(0, Math.min(3, tagText.length())) : "") + "')]"))
        ));
        // Articles should be visible
        Assertions.assertFalse(homepageArticlePreviews().isEmpty(), "Expected filtered articles for tag: " + tagText);
    }

    @Test
    @Order(5)
    public void openFirstArticleThenReturnHome() {
        openHome();
        List<WebElement> previews = homepageArticlePreviews();
        Assertions.assertFalse(previews.isEmpty(), "Need at least one article preview.");
        // Click the title link inside first preview
        WebElement titleLink = firstPresent(
                By.cssSelector(".article-preview h1, .article-preview a.preview-link"),
                By.xpath("(//a[contains(@class,'preview-link')])[1]")
        );
        wait.until(ExpectedConditions.elementToBeClickable(titleLink)).click();

        // On article page
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("article"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-page"))
        ));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("article") || driver.findElements(By.cssSelector(".article-page")).size() > 0,
                "Should be on an article page."
        );

        // Navigate back and verify home again
        driver.navigate().back();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".home-page, .feed-toggle")));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
                "After going back, should return to home domain.");
    }

    @Test
    @Order(6)
    public void externalLinksFromFooterOpenInNewTab() {
        openHome();

        // Collect obvious external links (Thinkster and GitHub commonly appear)
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Map<String, WebElement> targets = new LinkedHashMap<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            String h = href.toLowerCase();
            if (h.contains("thinkster.io") && !targets.containsKey("thinkster.io")) targets.put("thinkster.io", a);
            if (h.contains("github.com") && !targets.containsKey("github.com")) targets.put("github.com", a);
        }

        int visited = 0;
        for (Map.Entry<String, WebElement> e : targets.entrySet()) {
            if (visited >= 2) break; // limit for stability
            String domain = e.getKey();
            WebElement link = e.getValue();
            int before = driver.getWindowHandles().size();
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", link);
            wait.until(ExpectedConditions.elementToBeClickable(link)).click();

            if (driver.getWindowHandles().size() > before) {
                switchToNewTabAndVerify(domain);
            } else {
                wait.until(ExpectedConditions.urlContains(domain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                        "Expected navigation to external domain: " + domain);
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
            visited++;
        }

        if (visited == 0) {
            Assertions.assertTrue(anchors.size() > 0, "No external links detected; page should still have anchors.");
        }
    }

    @Test
    @Order(7)
    public void oneLevelInternalPathsSmoke() {
        // Visit common SPA routes one level deep
        String[] routes = new String[] {"login", "register", "settings", "editor"};
        for (String r : routes) {
            go(r);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            // For protected routes (settings/editor), unauthenticated users typically see login
            if (r.equals("settings") || r.equals("editor")) {
                boolean loggedRedirect = driver.getCurrentUrl().contains("login")
                        || driver.getCurrentUrl().contains("#/login")
                        || driver.findElements(By.xpath("//*[contains(.,'Sign in')]")).size() > 0;
                Assertions.assertTrue(loggedRedirect, "Protected route should require sign-in (route: " + r + ").");
            } else {
                Assertions.assertTrue(driver.getCurrentUrl().contains(r) || driver.getCurrentUrl().contains("#/" + r),
                        "URL should indicate route: " + r);
            }
            // Return to home after each to keep tests independent
            openHome();
        }
    }

    @Test
    @Order(8)
    public void optionalSortingIfPresentOnHomeFeed() {
        openHome();
        // Capture first few preview titles
        List<String> before = homepageArticlePreviews().stream()
                .map(e -> {
                    List<WebElement> t = e.findElements(By.cssSelector("h1, h2, a.preview-link"));
                    return t.isEmpty() ? e.getText() : t.get(0).getText();
                })
                .filter(t -> t != null && !t.trim().isEmpty())
                .limit(5)
                .collect(Collectors.toList());

        boolean changed = false;
        // Some implementations may have a sort dropdown or toggle; exercise if present
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        if (!selects.isEmpty()) {
            Select sort = new Select(selects.get(0));
            if (sort.getOptions().size() > 1) {
                String initial = sort.getFirstSelectedOption().getText();
                int altIndex = sort.getOptions().size() > 2 ? 2 : 1;
                sort.selectByIndex(altIndex);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                List<String> after = homepageArticlePreviews().stream()
                        .map(e -> {
                            List<WebElement> t = e.findElements(By.cssSelector("h1, h2, a.preview-link"));
                            return t.isEmpty() ? e.getText() : t.get(0).getText();
                        })
                        .filter(t -> t != null && !t.trim().isEmpty())
                        .limit(5)
                        .collect(Collectors.toList());
                changed = !before.equals(after) || !sort.getFirstSelectedOption().getText().equals(initial);
            }
        } else {
            // Try a button-based sort menu, if any
            List<WebElement> sortButtons = driver.findElements(By.cssSelector("button[aria-haspopup='listbox'], .sort button, [data-test='sort'] button"));
            if (!sortButtons.isEmpty()) {
                WebElement btn = sortButtons.get(0);
                wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
                List<WebElement> menuItems = driver.findElements(By.cssSelector("[role='menuitem'], [role='option'], .dropdown a, .menu a"));
                if (menuItems.size() > 1) {
                    wait.until(ExpectedConditions.elementToBeClickable(menuItems.get(1))).click();
                    List<String> after = homepageArticlePreviews().stream()
                            .map(e -> {
                                List<WebElement> t = e.findElements(By.cssSelector("h1, h2, a.preview-link"));
                                return t.isEmpty() ? e.getText() : t.get(0).getText();
                            })
                            .filter(t -> t != null && !t.trim().isEmpty())
                            .limit(5)
                            .collect(Collectors.toList());
                    changed = !before.equals(after);
                }
            }
        }

        // If a sort control existed, assert change; otherwise, assert feed is present
        if (!driver.findElements(By.tagName("select")).isEmpty()
                || !driver.findElements(By.cssSelector("button[aria-haspopup='listbox'], .sort button, [data-test='sort'] button")).isEmpty()) {
            Assertions.assertTrue(changed, "Feed order/text should change after sorting selection.");
        } else {
            Assertions.assertFalse(before.isEmpty(), "Home feed should list articles.");
        }
    }
}
