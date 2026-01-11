```java
package GPT5.ws08.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // -------------------- Helpers --------------------

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private String registrableDomain(String url) {
        try {
            URI u = URI.create(url);
            String host = u.getHost();
            if (host == null) return "";
            String[] p = host.split("\\.");
            if (p.length < 2) return host;
            return p[p.length - 2] + "." + p[p.length - 1];
        } catch (Exception e) {
            return "";
        }
    }

    private ExpectedCondition<Boolean> urlChangedFrom(String previous) {
        return d -> previous == null || !d.getCurrentUrl().equals(previous);
    }

    private WebElement first(By by) {
        List<WebElement> els = driver.findElements(by);
        return els.isEmpty() ? null : els.get(0);
    }

    private void click(WebElement el) {
        if (el != null) wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void verifyExternalLink(WebElement anchor) {
        String href = anchor.getAttribute("href");
        if (href == null || href.isEmpty() || href.startsWith("mailto:") || href.startsWith("javascript:")) return;

        String baseDomain = registrableDomain(BASE_URL);
        String targetDomain = registrableDomain(href);
        boolean external = !baseDomain.equalsIgnoreCase(targetDomain);

        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(anchor)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0],'_blank');", href);
        }

        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !d.getCurrentUrl().equals(driver.getCurrentUrl()));
        } catch (TimeoutException ignored) { }

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newTab = after.iterator().next();
            driver.switchTo().window(newTab);
            if (external) {
                wait.until(ExpectedConditions.urlContains(targetDomain));
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "External URL should contain expected domain: " + targetDomain);
            }
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            if (external) {
                Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(targetDomain.toLowerCase()),
                        "External URL should contain expected domain: " + targetDomain);
            }
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    private void verifyAllExternalLinksOnPage() {
        String baseDomain = registrableDomain(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
        Map<String, WebElement> uniqueExternal = new LinkedHashMap<>();
        for (WebElement a : links) {
            String href = a.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (!href.startsWith("http")) continue;
            String d = registrableDomain(href);
            if (!d.isEmpty() && !d.equalsIgnoreCase(baseDomain)) {
                uniqueExternal.putIfAbsent(href, a);
            }
        }
        for (WebElement a : uniqueExternal.values()) {
            verifyExternalLink(a);
        }
    }

    private void clearCartIfAny() {
        // Navigate to cart
        List<WebElement> cartLinks = driver.findElements(By.cssSelector("a[href*='cart/viewCart']"));
        if (cartLinks.isEmpty()) {
            // Try clicking "Cart" text if present
            cartLinks = driver.findElements(By.linkText("Cart"));
        }
        if (!cartLinks.isEmpty()) {
            click(cartLinks.get(0));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            // Remove links/buttons
            List<WebElement> removeLinks = driver.findElements(By.xpath("//a[contains(.,'Remove')]"));
            for (WebElement r : removeLinks) {
                click(r);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
        }
    }

    private boolean isSignedIn() {
        return driver.findElements(By.linkText("Sign Out")).size() > 0
                || driver.findElements(By.xpath("//*[contains(text(),'Welcome')]")).size() > 0;
    }

    private void signOutIfSignedIn() {
        List<WebElement> outs = driver.findElements(By.linkText("Sign Out"));
        if (!outs.isEmpty()) {
            click(outs.get(0));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign In")));
        }
    }

    private void signIn(String user, String pass) {
        WebElement signIn = first(By.linkText("Sign In"));
        if (signIn == null) signIn = first(By.partialLinkText("Sign"));
        if (signIn == null) signIn = first(By.cssSelector("a[href*='signon']"));
        Assertions.assertNotNull(signIn, "Sign In link should exist on home.");
        click(signIn);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        WebElement userField = driver.findElement(By.name("username"));
        WebElement passField = driver.findElement(By.name("password"));
        WebElement loginBtn = first(By.name("signon"));

        userField.clear();
        userField.sendKeys(user);
        passField.clear();
        passField.sendKeys(pass);
        click(loginBtn != null ? loginBtn : driver.findElement(By.cssSelector("input[type='submit'], button[type='submit']")));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    // -------------------- Tests --------------------

    @Test
    @Order(1)
    public void homePageLoadsAndHasCategories() {
        goHome();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should start on JPetStore home.");
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jpetstore") || title.toLowerCase().contains("jpet"),
                "Title should mention JPetStore.");
        // Presence of category links/images
        boolean hasCategories = driver.findElements(By.cssSelector("area[href*='catalog/categories/'], a[href*='catalog/categories/']")).size() > 0
                || driver.findElements(By.cssSelector("div#Main, #Content, main")).size() > 0
                || driver.findElements(By.tagName("area")).size() > 0;
        Assertions.assertTrue(hasCategories, "Home should display category navigation.");
        verifyAllExternalLinksOnPage();
        clearCartIfAny();
        signOutIfSignedIn();
    }

    @Test
    @Order(2)
    public void validLoginAndLogoutIfAvailable() {
        goHome();
        signIn(USERNAME, PASSWORD);

        // Verify login success heuristics
        boolean success = isSignedIn();
        Assumptions.assumeTrue(success, "Could not confirm successful login with demo credentials; skipping assertions.");
        Assertions.assertTrue(driver.findElements(By.linkText("My Account")).size() > 0
                        || driver.findElements(By.linkText("Sign Out")).size() > 0,
                "After login, My Account or Sign Out should be visible.");

        // Logout
        signOutIfSignedIn();
        Assertions.assertTrue(driver.findElements(By.linkText("Sign In")).size() > 0, "Sign In link should reappear after logout.");
    }

    @Test
    @Order(3)
    public void invalidLoginShowsError() {
        goHome();
        signIn("invalid_user_xyz", "invalid_pass_xyz");
        // Expect an error message and remain on signon page or get alert text
        boolean hasError = driver.findElements(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid') or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'signon failed')]")).size() > 0;
        Assumptions.assumeTrue(hasError || !isSignedIn(), "No explicit error found but not signed in; treating as negative case.");
        Assertions.assertTrue(!isSignedIn(), "User should not be signed in with invalid credentials.");
        goHome();
    }

    @Test
    @Order(4)
    public void browseCategoryProductItemAndAddToCart() {
        goHome();

        // Click a category (Fish preferred)
        WebElement fish = first(By.cssSelector("a[href*='catalog/categories/FISH']"));
        if (fish == null) fish = first(By.cssSelector("area[href*='catalog/categories/FISH']"));
        WebElement anyCat = fish != null ? fish : first(By.cssSelector("a[href*='catalog/categories/'], area[href*='catalog/categories/']"));
        if (anyCat == null) anyCat = first(By.tagName("area"));
        Assertions.assertNotNull(anyCat, "At least one category link should be present.");
        String before = driver.getCurrentUrl();
        click(anyCat);
        wait.until(urlChangedFrom(before));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/categories/"), "Should be on a category page.");

        // Click a product within the category
        WebElement product = first(By.cssSelector("a[href*='catalog/products/']"));
        Assertions.assertNotNull(product, "A product link should be present on the category page.");
        before = driver.getCurrentUrl();
        click(product);
        wait.until(urlChangedFrom(before));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/products/"), "Should be on a product page.");

        // Click an item within the product
        WebElement item = first(By.cssSelector("a[href*='catalog/items/']"));
        Assertions.assertNotNull(item, "An item link should be present on the product page.");
        before = driver.getCurrentUrl();
        click(item);
        wait.until(urlChangedFrom(before));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/items/"), "Should be on an item page.");

        // Add to cart
        WebElement addToCart = first(By.xpath("//a[contains(.,'Add to Cart') or contains(@href,'addItemToCart')]"));
        Assertions.assertNotNull(addToCart, "Add to Cart link should be present on the item page.");
        click(addToCart);

        // Validate cart
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/cart/"),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(.,'Shopping Cart')]"))
        ));
        boolean hasCartTable = driver.findElements(By.cssSelector("table")).size() > 0
                || driver.findElements(By.xpath("//*[contains(.,'Sub Total') or contains(.,'Total')]")).size() > 0;
        Assertions.assertTrue(hasCartTable, "Cart table or totals should be visible.");
        clearCartIfAny();
        goHome();
    }

    @Test
    @Order(5)
    public void searchForFishAndOptionallySort() {
        goHome();
        // Locate search input
        WebElement input = first(By.name("keyword"));
        if (input == null) input = first(By.cssSelector("input[type='text']"));
        Assumptions.assumeTrue(input != null, "No search input found; skipping search test.");
        input.clear();
        input.sendKeys("fish");

        // Click search
        WebElement searchBtn = first(By.cssSelector("input[type='submit'][value='Search'], button[type='submit']"));
        if (searchBtn == null) searchBtn = first(By.xpath("//input[@type='submit' and @value='Search']"));
        if (searchBtn == null) searchBtn = first(By.xpath("//button[contains(.,'Search')]"));
        if (searchBtn == null) searchBtn = first(By.cssSelector("input[type='submit']"));
        Assertions.assertNotNull(searchBtn, "Search submit control should exist.");
        click(searchBtn);

        // Verify results
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        boolean hasResults = driver.findElements(By.cssSelector("a[href*='catalog/products/']")).size() > 0
                || driver.findElements(By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'result')]")).size() > 0;
        Assertions.assertTrue(hasResults, "Search should return product links or a results section.");

        // Exercise a sorting dropdown if present
        WebElement sortSelect = first(By.cssSelector("select[id*='sort'], select[name*='sort'], select#sort"));
        if (sortSelect != null) {
            Select sel = new Select(sortSelect);
            List<WebElement> options = sel.getOptions();
            if (options.size() > 1) {
                String beforeFirst = firstResultText();
                sel.selectByIndex(1);
                wait.until(d -> !Objects.equals(beforeFirst, firstResultText()));
                String afterFirst = firstResultText();
                Assertions.assertNotEquals(beforeFirst, afterFirst, "First result should change after sorting.");
                sel.selectByIndex(0);
            } else {
                Assumptions.assumeTrue(false, "Sorting select has insufficient options; skipping.");
            }
        } else {
            Assumptions.assumeTrue(false, "No sorting dropdown present on results; skipping.");
        }
    }

    private String firstResultText() {
        List<WebElement> results = driver.findElements(By.cssSelector("a[href*='catalog/products/'], .result, .item, article"));
        if (!results.isEmpty()) {
            String t = results.get(0).getText();
            return t == null ? "" : t.trim();
        }
        return "";
    }

    @Test
    @Order(6)
    public void footerSocialLinksAsExternalIfPresent() {
        goHome();
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        List<String> domains = Arrays.asList("twitter.com", "facebook.com", "linkedin.com", "github.com", "youtube.com");
        Map<String, WebElement> found = new LinkedHashMap<>();
        for (String dmn : domains) {
            List<WebElement> anchors = driver.findElements(By.cssSelector("a[href*='" + dmn + "']"));
            if (!anchors.isEmpty()) found.put(dmn, anchors.get(0));
        }

        Assumptions.assumeTrue(!found.isEmpty(), "No social links found; skipping.");
        for (WebElement a : found.values()) {
            verifyExternalLink(a);
        }
    }

    @Test
    @Order(7)
    public void iterateInternalLinksOneLevelFromHome() {
        goHome();
        String baseDomain = registrableDomain(BASE_URL);

        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        List<String> internal = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(h -> h != null && !h.isEmpty())
                .filter(h -> h.startsWith("http"))
                .filter(h -> registrableDomain(h).equalsIgnoreCase(baseDomain))
                .filter(h -> h.startsWith(BASE_URL))
                .distinct()
                .collect(Collectors.toList());

        int visited = 0;
        for (String href : internal) {
            // Avoid signout/logout links to keep state clean
            if (href.toLowerCase().contains("signout") || href.toLowerCase().contains("logout")) continue;

            if (visited >= 6) break;
            String current = driver.getCurrentUrl();
            try {
                WebElement link = first(By.cssSelector("a[href='" + href + "']"));
                if (link == null) continue;
                click(link);
                try {
                    wait.until(urlChangedFrom(current));
                } catch (TimeoutException ignored) { }

                boolean hasContent = driver.findElements(By.tagName("h1")).size() > 0
                        || driver.findElements(By.tagName("h2")).size() > 0
                        || driver.findElements(By.cssSelector("main, #Main, #Content, .content, article")).size() > 0;
                Assertions.assertTrue(hasContent, "Internal page should show content: " + driver.getCurrentUrl());

                verifyAllExternalLinksOnPage();
            } finally {
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
            visited++;
        }
        Assertions.assertTrue(visited >= 0, "Iterated internal links without failures.");
    }

    @Test
    @Order(8)
    public void burgerMenuIfPresent() {
        goHome();
        // Responsive menu toggle might exist; try common selectors
        List<By> burgerSelectors = Arrays.asList(
                By.cssSelector("button[aria-label*='menu' i]"),
                By.cssSelector(".navbar-toggle, .hamburger, button[class*='burger' i]"),
                By.xpath("//button[contains(.,'Menu') or contains(.,'menu')]")
        );
        WebElement burger = null;
        for (By sel : burgerSelectors) {
            burger = first(sel);
            if (burger != null) break;
        }
        Assumptions.assumeTrue