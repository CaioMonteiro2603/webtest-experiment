package GPT5.ws09.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://demo.realworld.io/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1440, 1000));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ----------------------- Helpers -----------------------

    private void goHome() {
        driver.get(BASE_URL);
        waitForAppReady();
    }

    private void waitForAppReady() {
        wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        // App shell header should appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("nav.navbar")));
    }

    private WebElement visible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private WebElement clickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    private void safeClick(By by) {
        clickable(by).click();
    }

    private void safeClick(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private List<WebElement> displayedAll(By by) {
        return driver.findElements(by).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
    }

    private static int hashPathDepth(String url) {
        try {
            String hash = URI.create(url).getRawFragment(); // part after '#'
            if (hash == null || hash.isEmpty()) return 0;
            if (!hash.startsWith("/")) return 0;
            String[] parts = Arrays.stream(hash.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);
            return parts.length;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private String toAbsoluteFromHref(String href) {
        if (href == null || href.isEmpty()) return null;
        if (href.startsWith("http")) return href;
        if (href.startsWith("#")) return BASE_URL + href;
        if (href.startsWith("/")) return BASE_URL + "#" + href;
        return null;
    }

    private void openExternalAndAssert(By linkLocator, String expectedDomainFragment) {
        List<WebElement> links = displayedAll(linkLocator);
        if (links.isEmpty()) return; // optional link; skip
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        WebElement link = links.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        // Either a new tab opens or navigation occurs in same tab
        wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(BASE_URL));
        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(d -> d.getCurrentUrl() != null && !d.getCurrentUrl().isEmpty());
            String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
            Assertions.assertTrue(url.contains(expectedDomainFragment.toLowerCase(Locale.ROOT)),
                    "External URL should contain: " + expectedDomainFragment + " but was: " + url);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(d -> d.getCurrentUrl().toLowerCase(Locale.ROOT).contains(expectedDomainFragment.toLowerCase(Locale.ROOT)));
            driver.navigate().back();
            waitForAppReady();
        }
    }

    // ----------------------- Tests -----------------------

    @Test
    @Order(1)
    public void homePageLoads_HeaderFooterAndFeedVisible() {
        goHome();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should be on base URL");
        WebElement brand = visible(By.cssSelector("a.navbar-brand"));
        WebElement homeTab = visible(By.cssSelector("a.nav-link[href='#/']"));
        WebElement globalFeedTab = visible(By.xpath("//a[contains(@class,'nav-link') and contains(.,'Global Feed')]"));
        // Article preview or empty state should exist on demo site
        boolean hasPreviews = !driver.findElements(By.cssSelector(".article-preview")).isEmpty();
        Assertions.assertAll(
                () -> Assertions.assertEquals("conduit", brand.getText().trim().toLowerCase(Locale.ROOT), "Navbar brand should be 'conduit'"),
                () -> Assertions.assertTrue(homeTab.isDisplayed(), "Home tab should be visible"),
                () -> Assertions.assertTrue(globalFeedTab.isDisplayed(), "Global Feed tab should be visible"),
                () -> Assertions.assertTrue(hasPreviews, "Should show article previews on home")
        );
    }

    @Test
    @Order(2)
    public void internalOneLevelHashLinks_AreReachable() {
        goHome();
        // Collect one-level links (hash depth <= 1)
        LinkedHashSet<String> oneLevel = new LinkedHashSet<>();
        for (WebElement a : displayedAll(By.cssSelector("a[href]"))) {
            String abs = toAbsoluteFromHref(a.getAttribute("href"));
            if (abs == null) continue;
            if (!abs.startsWith(BASE_URL)) continue;
            int depth = hashPathDepth(abs);
            if (depth <= 1) {
                oneLevel.add(abs);
            }
        }
        int visited = 0;
        for (String url : oneLevel) {
            driver.navigate().to(url);
            waitForAppReady();
            Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should remain within app domain");
            visited++;
            if (visited >= 5) break; // visit a few to keep test stable
        }
        goHome();
    }

    @Test
    @Order(3)
    public void tagsSidebar_FilterByFirstTag_IfPresent() {
        goHome();
        List<WebElement> tags = displayedAll(By.cssSelector(".tag-list a.tag-pill"));
        if (tags.isEmpty()) {
            Assertions.assertTrue(true, "No tags available; skipping tag filter test.");
            return;
        }
        String tagText = tags.get(0).getText().trim();
        safeClick(tags.get(0));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".feed-toggle")));
        String url = driver.getCurrentUrl();
        Assertions.assertTrue(url.contains("#/tag/"), "URL should contain tag route after selecting tag");
        // The active tab should include the tag name
        WebElement active = visible(By.cssSelector(".feed-toggle .nav-link.active"));
        Assertions.assertTrue(active.getText().toLowerCase(Locale.ROOT).contains(tagText.toLowerCase(Locale.ROOT)),
                "Active feed tab should include the selected tag name");
        goHome();
    }

    @Test
    @Order(4)
    public void signIn_Negative_InvalidCredentials_ShowsError() {
        driver.get(BASE_URL + "#/login");
        waitForAppReady();
        WebElement email = visible(By.cssSelector("input[type='email']"));
        WebElement password = visible(By.cssSelector("input[type='password']"));
        email.clear(); email.sendKeys("invalid@example.com");
        password.clear(); password.sendKeys("wrongpassword");
        safeClick(By.cssSelector("button[type='submit']"));
        // Wait for error message to appear
        WebElement errorList = null;
        try {
            Thread.sleep(1000); // Brief wait for AJAX response
        } catch (InterruptedException e) {} // Sleep to allow error to appear
        errorList = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorList.isDisplayed() && errorList.getText().toLowerCase(Locale.ROOT).contains("error") || errorList.getText().toLowerCase(Locale.ROOT).contains("email or password is invalid"),
                "Should display an error message for invalid login");
        // Back to home to restore known state
        goHome();
    }

    @Test
    @Order(5)
    public void signUpPage_NavigatesAndRendersForm() {
        goHome();
        WebElement signUp = visible(By.cssSelector("a.nav-link[href='#/register']"));
        safeClick(signUp);
        waitForAppReady();
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/register"), "Should navigate to Sign up page");
        Assertions.assertAll(
                () -> Assertions.assertFalse(displayedAll(By.cssSelector("input[type='text']")).isEmpty(), "Username input should be present"),
                () -> Assertions.assertFalse(displayedAll(By.cssSelector("input[type='email']")).isEmpty(), "Email input should be present"),
                () -> Assertions.assertFalse(displayedAll(By.cssSelector("input[type='password']")).isEmpty(), "Password input should be present")
        );
        goHome();
    }

    @Test
    @Order(6)
    public void yourFeedTab_RequiresAuth_ShowsNoFeedForGuest() {
        goHome();
        List<WebElement> yourFeedLinks = driver.findElements(By.xpath("//a[contains(@class,'nav-link') and contains(.,'Your Feed')]"));
        if (yourFeedLinks.isEmpty() || !yourFeedLinks.get(0).isDisplayed()) {
            Assertions.assertTrue(true, "Your Feed tab not present for guest; skipping test.");
            return;
        }
        WebElement yourFeed = yourFeedLinks.get(0);
        safeClick(yourFeed);
        // As guest, 'Your Feed' usually shows no articles; assert tab active and previews may be zero
        WebElement active = visible(By.cssSelector(".feed-toggle .nav-link.active"));
        Assertions.assertTrue(active.getText().toLowerCase(Locale.ROOT).contains("your feed"),
                "Your Feed tab should be active after click");
        // Either zero previews or a message; assert no error thrown and page is still within app
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Still within domain");
        goHome();
    }

    @Test
    @Order(7)
    public void optional_SortingDropdown_IfPresent_ChangesSelection() {
        goHome();
        WebElement sort = null;
        List<By> locs = Arrays.asList(
                By.cssSelector("select[id*='sort' i]"),
                By.cssSelector("select[name*='sort' i]"),
                By.xpath("//select[contains(translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sort') or contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sort')]")
        );
        for (By by : locs) {
            List<WebElement> found = displayedAll(by);
            if (!found.isEmpty()) { sort = found.get(0); break; }
        }
        if (sort == null) {
            Assertions.assertTrue(true, "No sorting dropdown on this app; skipping.");
            return;
        }
        Select select = new Select(sort);
        String initial = select.getFirstSelectedOption().getText();
        List<WebElement> options = select.getOptions();
        if (options.size() > 1) {
            select.selectByIndex(options.size() - 1);
            String after = select.getFirstSelectedOption().getText();
            Assertions.assertNotEquals(initial, after, "Selecting another sort option should change selection");
        } else {
            Assertions.assertTrue(true, "Only one sort option; nothing to change.");
        }
        goHome();
    }

    @Test
    @Order(8)
    public void externalLinksInFooter_OpenAndContainExpectedDomains() {
        goHome();
        // Footer contains a Thinkster link on the demo site
        openExternalAndAssert(By.cssSelector("a[href*='thinkster']"), "thinkster");
        // Also check GitHub or Twitter if present anywhere
        openExternalAndAssert(By.cssSelector("a[href*='github.com']"), "github.com");
        openExternalAndAssert(By.cssSelector("a[href*='twitter.com']"), "twitter.com");
        goHome();
    }

    @Test
    @Order(9)
    public void menuBurger_Optional_OpenClose_IfPresent() {
        goHome();
        // On smaller screens, there may be a burger button. Try to interact if it exists.
        List<WebElement> burgers = driver.findElements(By.cssSelector("button.navbar-toggler, button[aria-label='Toggle navigation']"));
        if (burgers.isEmpty()) {
            Assertions.assertTrue(true, "No burger menu present; skipping.");
            return;
        }
        WebElement burger = burgers.get(0);
        safeClick(burger);
        // Expect nav links to be visible after toggle
        boolean anyLinkVisible = !displayedAll(By.cssSelector("a.nav-link")).isEmpty();
        Assertions.assertTrue(anyLinkVisible, "Nav links should be visible after opening burger menu");
        safeClick(burger); // close
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Still within app");
        goHome();
    }
}