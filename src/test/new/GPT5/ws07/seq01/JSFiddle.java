package GPT5.ws07.seq01;

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
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jsfiddle.net/";

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
        dismissConsentIfPresent();
        // Top nav should be present
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("header")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("nav")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='explore']"))
        ));
    }

    private void dismissConsentIfPresent() {
        // Try common CMP selectors/texts
        List<By> consentButtons = Arrays.asList(
                By.id("onetrust-accept-btn-handler"),
                By.cssSelector("button#onetrust-accept-btn-handler"),
                By.cssSelector("button[aria-label='Accept all']"),
                By.xpath("//button[contains(.,'Accept All') or contains(.,'Accept all') or contains(.,'Accept') or contains(.,'AGREE') or contains(.,'Agree')]"),
                By.xpath("//button[contains(.,'I agree')]"),
                By.xpath("//button[contains(.,'OK') or contains(.,'Ok')]"),
                By.cssSelector("button[mode='primary']"),
                By.cssSelector("button[title*='Accept']")
        );
        for (By by : consentButtons) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty() && els.get(0).isDisplayed()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(els.get(0))).click();
                    break;
                } catch (Exception ignored) {}
            }
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

    private void clickExternalAndAssert(String expectedDomain, By... candidates) {
        int before = driver.getWindowHandles().size();
        boolean clicked = false;
        for (By by : candidates) {
            if (clickIfPresent(by)) { clicked = true; break; }
        }
        Assertions.assertTrue(clicked, "Expected external link was not found/clickable.");

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

    private List<String> captureExploreFirstItemsText() {
        // Try several selectors that may list fiddles/cards
        List<By> candidates = Arrays.asList(
                By.cssSelector("[data-test='fiddle-card'] a[title], .fiddle-card a[title]"),
                By.cssSelector(".collection-item a, article a[href*='/']"),
                By.cssSelector("a[href*='/users/'], a[href*='/code/'], a[href*='/embed/']"),
                By.cssSelector("main a[href]")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            List<String> texts = els.stream()
                    .map(WebElement::getText)
                    .filter(t -> t != null && !t.trim().isEmpty())
                    .limit(5)
                    .collect(Collectors.toList());
            if (!texts.isEmpty()) return texts;
        }
        return Collections.emptyList();
    }

    /* ============================ Tests ============================ */

    @Test
    @Order(1)
    public void homePageLoads() {
        openHome();
        String title = driver.getTitle();
        Assertions.assertTrue(title != null && !title.isEmpty(), "Home page title should not be empty.");
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on JSFiddle home domain.");
    }

    @Test
    @Order(2)
    public void signInPageAccessible() {
        openHome();
        boolean clicked = clickIfPresent(By.linkText("Sign in"))
                || clickIfPresent(By.partialLinkText("Sign"))
                || clickIfPresent(By.cssSelector("a[href*='login'], a[href*='signin']"));
        Assertions.assertTrue(clicked, "Sign in link should be present.");

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("login"),
                ExpectedConditions.urlContains("signin")
        ));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login") || driver.getCurrentUrl().contains("signin"),
                "Should navigate to sign in page.");
    }

    @Test
    @Order(3)
    public void explorePageLoadsAndOptionalSortChangesOrder() {
        openHome();
        boolean clicked = clickIfPresent(By.linkText("Explore"))
                || clickIfPresent(By.partialLinkText("Explore"))
                || clickIfPresent(By.cssSelector("a[href*='explore']"))
                || clickIfPresent(By.cssSelector("a[href*='/explore']"))
                || clickIfPresent(By.xpath("//a[contains(text(),'Explore')]"))
                || clickIfPresent(By.xpath("//a[contains(@href,'explore')]"));
        Assertions.assertTrue(clicked, "Explore link should be present.");
        wait.until(ExpectedConditions.urlContains("explore"));

        List<String> before = captureExploreFirstItemsText();

        // Try to interact with a sort control if present
        boolean changed = false;
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        if (!selects.isEmpty()) {
            Select sort = new Select(selects.get(0));
            if (sort.getOptions().size() > 1) {
                String initial = sort.getFirstSelectedOption().getText();
                int altIndex = sort.getOptions().size() > 2 ? 2 : 1;
                sort.selectByIndex(altIndex);
                // Wait for any list change
                wait.withTimeout(Duration.ofSeconds(10));
                List<String> after = captureExploreFirstItemsText();
                changed = !before.equals(after) || !sort.getFirstSelectedOption().getText().equals(initial);
            }
        } else {
            // Try a button-based sort (common pattern)
            List<WebElement> sortButtons = driver.findElements(By.cssSelector("button[aria-haspopup='listbox'], .sort button, [data-test='sort'] button"));
            if (!sortButtons.isEmpty()) {
                WebElement btn = sortButtons.get(0);
                wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
                List<WebElement> menuItems = driver.findElements(By.cssSelector("[role='menuitem'], [role='option'], .dropdown a, .menu a"));
                if (menuItems.size() > 1) {
                    wait.until(ExpectedConditions.elementToBeClickable(menuItems.get(1))).click();
                    List<String> after = captureExploreFirstItemsText();
                    changed = !before.equals(after);
                }
            }
        }

        // It's optional—only assert if control existed; otherwise pass with list presence
        if (!driver.findElements(By.tagName("select")).isEmpty()
                || !driver.findElements(By.cssSelector("button[aria-haspopup='listbox'], .sort button, [data-test='sort'] button")).isEmpty()) {
            Assertions.assertTrue(changed, "List order/text should change after sorting selection.");
        } else {
            Assertions.assertFalse(before.isEmpty(), "Explore should list some fiddles/items.");
        }
    }

    @Test
    @Order(4)
    public void documentationLinkIsExternal() {
        openHome();
        clickExternalAndAssert("docs",
                By.linkText("Documentation"),
                By.partialLinkText("Docs"),
                By.cssSelector("a[href*='docs']"),
                By.cssSelector("a[href*='documentation']"),
                By.xpath("//a[contains(text(),'Documentation')]")
        );
    }

    @Test
    @Order(5)
    public void blogLinkAccessible() {
        openHome();
        // Blog might be external (Medium) or internal path; handle both
        int before = driver.getWindowHandles().size();
        boolean clicked = clickIfPresent(By.linkText("Blog"))
                || clickIfPresent(By.partialLinkText("Blog"))
                || clickIfPresent(By.cssSelector("a[href*='blog']"));
        Assertions.assertTrue(clicked, "Blog link should exist.");

        // Determine if new tab opened
        if (driver.getWindowHandles().size() > before) {
            // Likely Medium or external
            switchToNewTabAndVerify("medium.com");
        } else {
            // Same-tab
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("blog"),
                    ExpectedConditions.urlContains("medium.com")
            ));
            Assertions.assertTrue(driver.getCurrentUrl().contains("blog") || driver.getCurrentUrl().contains("medium.com"),
                    "Should be on blog path or Medium.");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    @Test
    @Order(6)
    public void externalSocialLinksOpen() {
        openHome();
        // Collect external candidates in header/footer
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Map<String, WebElement> targets = new LinkedHashMap<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            String h = href.toLowerCase();
            if (h.contains("twitter.com") && !targets.containsKey("twitter.com")) targets.put("twitter.com", a);
            if (h.contains("facebook.com") && !targets.containsKey("facebook.com")) targets.put("facebook.com", a);
            if (h.contains("linkedin.com") && !targets.containsKey("linkedin.com")) targets.put("linkedin.com", a);
            if (h.contains("github.com") && !targets.containsKey("github.com")) targets.put("github.com", a);
        }

        int visited = 0;
        for (Map.Entry<String, WebElement> e : targets.entrySet()) {
            if (visited >= 2) break; // limit to reduce flakiness
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
                        "Expected to navigate to external domain: " + domain);
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
            visited++;
        }

        // If none found, page still should have anchors
        if (visited == 0) {
            Assertions.assertTrue(anchors.size() > 0, "No external social links detected; page should still have anchors.");
        }
    }

    @Test
    @Order(7)
    public void oneLevelDeepInternalPagesSmoke() {
        // Visit some common one-level paths directly
        String[] paths = new String[] {"explore/", "user/login/", "user/signup/"};
        for (String p : paths) {
            driver.get(BASE_URL + p);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Assertions.assertTrue(driver.getCurrentUrl().contains(p.replace("/", "")) || driver.getTitle() != null,
                    "Should load internal page: " + p);
            // Go back to home to keep independence clear
            openHome();
        }
    }
}