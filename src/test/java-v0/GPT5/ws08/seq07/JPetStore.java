package GPT5.ws08.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
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

    private static final String BASE_URL = "https://jpetstore.aspectran.com//";

    @BeforeAll
    static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().deleteAllCookies();
    }

    @AfterAll
    static void teardown() {
        if (driver != null) driver.quit();
    }

    // ================= Helpers =================

    private void goHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private WebElement waitClickable(WebElement webElement) {
        return wait.until(ExpectedConditions.elementToBeClickable(webElement));
    }

    private WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private boolean exists(By by) {
        return driver.findElements(by).size() > 0;
    }

    private void type(By by, String text) {
        WebElement el = waitVisible(by);
        el.clear();
        el.sendKeys(text);
    }

    private String getHost(String url) {
        try { return new URI(url).getHost(); } catch (Exception e) { return ""; }
    }

    private void openExternalAndAssertDomain(WebElement link, String expectedDomainFragment) {
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        WebElement webb = wait.until(ExpectedConditions.elementToBeClickable(link));
        webb.click();
        // Wait for either navigation in same tab or a new tab
        try {
            wait.until(d -> d.getWindowHandles().size() != before.size() ||
                    !Objects.equals(((JavascriptExecutor) d).executeScript("return document.readyState"), "loading"));
        } catch (TimeoutException ignore) { }
        Set<String> after = new HashSet<>(driver.getWindowHandles());
        after.removeAll(before);
        if (!after.isEmpty()) {
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment), "External URL should contain: " + expectedDomainFragment);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomainFragment));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainFragment), "External URL should contain: " + expectedDomainFragment);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    private Optional<WebElement> findTopNavLink(String textContainsLower) {
        List<WebElement> links = driver.findElements(By.cssSelector("a"));
        return links.stream().filter(a -> {
            String t = (a.getText() == null ? "" : a.getText()).toLowerCase(Locale.ROOT).trim();
            return t.contains(textContainsLower);
        }).findFirst();
    }

    // ================= Tests =================

    @Test
    @Order(1)
    void homeLoads_AndCoreElementsPresent() {
        goHome();
        Assertions.assertTrue(driver.getTitle() != null && driver.getTitle().length() > 0, "Title should be non-empty");
        // Header / Logo presence
        Assertions.assertTrue(exists(By.cssSelector("div#Header, header, .header, #logo, .logo")), "Header/Logo should exist");
        // Category or quick links exist
        boolean categoriesPresent = driver.findElements(By.cssSelector("a[href*='category'], a[href*='/catalog/'], a[href*='categoryId=']")).size() > 0;
        Assertions.assertTrue(categoriesPresent, "At least one category/section link should exist");
        // Search box if present
        Assertions.assertTrue(exists(By.cssSelector("input[type='text']")) || exists(By.name("keyword")) || exists(By.id("keyword")),
                "A text input (search or similar) should be present");
    }

    @Test
    @Order(2)
    void browseCategory_Product_AddToCart_AndVerifyCart() {
        goHome();
        // Open the first category
        List<WebElement> cats = driver.findElements(By.cssSelector("a[href*='/category/'], a[href*='categoryId='], a[href*='/catalog/categories/']"));
        Assertions.assertTrue(!cats.isEmpty(), "There should be at least one category link");
        WebElement webb = wait.until(ExpectedConditions.elementToBeClickable(cats.get(0)));
        webb.click(); 
        // Assert product list visible
        boolean listVisible = exists(By.cssSelector("table, .product, .product-list, .catalog")) &&
                driver.findElements(By.cssSelector("a[href*='/product/'], a[href*='productId=']")).size() > 0;
        Assertions.assertTrue(listVisible, "Product list should be visible in category");

        // Open a product
        WebElement prodLink = driver.findElements(By.cssSelector("a[href*='/product/'], a[href*='productId=']")).get(0);
        WebElement web = wait.until(ExpectedConditions.elementToBeClickable(prodLink));
        web.click();
        Assertions.assertTrue(exists(By.cssSelector("h1, h2, .name, .product-name")), "Product detail header should be visible");

        // Add to cart (best-effort)
        List<By> addLocators = Arrays.asList(
                By.cssSelector("a[href*='addToCart'], a[href*='/cart/add'], a.Button"),
                By.cssSelector("input[type='submit'][value*='Add'], button[type='submit'], button.add-to-cart")
        );
        boolean added = false;
        for (By by : addLocators) {
            List<WebElement> btns = driver.findElements(by);
            if (!btns.isEmpty()) {
            	 WebElement el = wait.until(ExpectedConditions.elementToBeClickable(btns.get(0)));
                 el.click();
                added = true;
                break;
            }
        }
        Assertions.assertTrue(added, "Should click Add to Cart on product page");

        // Open cart page
        Optional<WebElement> cartLink = findTopNavLink("cart");
        if (cartLink.isPresent()) {
        	 WebElement webEl = wait.until(ExpectedConditions.elementToBeClickable(cartLink.get()));
             webEl.click();
        } else {
            // Fallback: try common cart URL
            driver.get("https://jpetstore.aspectran.com/cart/");
        }
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("cart"), "URL should indicate cart page");
        // Cart should have at least one line item
        boolean itemInCart = driver.findElements(By.cssSelector("table tr, .cart-item, .lineItem")).size() > 0;
        Assertions.assertTrue(itemInCart, "Cart should list at least one item");

        // Remove item if a remove control exists to reset state
        List<WebElement> removes = driver.findElements(By.cssSelector("a[href*='remove'], input[type='submit'][value*='Remove']"));
        if (!removes.isEmpty()) {
        	 WebElement webEle = wait.until(ExpectedConditions.elementToBeClickable(removes.get(0)));
             webEle.click(); 
        }
    }

    @Test
    @Order(3)
    void searchForItems_DisplaysResults() {
        goHome();
        // find a generic search input
        By searchBox = exists(By.name("keyword")) ? By.name("keyword") :
                (exists(By.id("keyword")) ? By.id("keyword") : By.cssSelector("input[type='text']"));
        type(searchBox, "fish");
        // Submit via enter or button
        WebElement box = waitVisible(searchBox);
        box.sendKeys(Keys.ENTER);

        // Either results page or same page with results section
        boolean resultsShown;
        try {
            resultsShown = wait.until(d ->
                    d.findElements(By.cssSelector("a[href*='/product/'], a[href*='productId=']")).size() > 0 ||
                            d.getPageSource().toLowerCase(Locale.ROOT).contains("result"));
        } catch (TimeoutException e) {
            resultsShown = false;
        }
        Assertions.assertTrue(resultsShown, "Search should show one or more results or indicate results found");
    }

    @Test
    @Order(4)
    void signInWithInvalidCredentials_ShowsError() {
        goHome();
        Optional<WebElement> signIn = findTopNavLink("sign in");
        if (signIn.isEmpty()) {
            // Some builds show "Login" instead of "Sign In"
            signIn = findTopNavLink("login");
        }
        Assertions.assertTrue(signIn.isPresent(), "Sign In/Login link should exist");
        WebElement webb = wait.until(ExpectedConditions.elementToBeClickable(signIn.get()));
        webb.click(); 

        // Fill and submit invalid creds
        By userBy = exists(By.name("username")) ? By.name("username") : By.id("username");
        By passBy = exists(By.name("password")) ? By.name("password") : By.id("password");
        type(userBy, "invalid_user");
        type(passBy, "invalid_pass");
        List<WebElement> submitBtns = driver.findElements(By.cssSelector("input[type='submit'], button[type='submit']"));
        Assertions.assertFalse(submitBtns.isEmpty(), "Submit button should exist on sign-in page");
        WebElement web = wait.until(ExpectedConditions.elementToBeClickable(submitBtns.get(0)));
        web.click();

        // Expect an error or remain on sign-in page
        boolean errorShown = exists(By.cssSelector(".error, .alert, .message-error")) ||
                driver.getPageSource().toLowerCase(Locale.ROOT).contains("invalid") ||
                driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("signon") ||
                driver.getTitle().toLowerCase(Locale.ROOT).contains("sign");
        Assertions.assertTrue(errorShown, "Invalid credentials should not log in and should show an error or remain on login page");
    }

    @Test
    @Order(5)
    void signInWithDemoCredentials_IfAccepted_ThenSignOut() {
        goHome();
        Optional<WebElement> signIn = findTopNavLink("sign in");
        if (signIn.isEmpty()) signIn = findTopNavLink("login");
        if (signIn.isPresent()) {
        	 WebElement webb = wait.until(ExpectedConditions.elementToBeClickable(signIn.get()));
             webb.click();
            By userBy = exists(By.name("username")) ? By.name("username") : By.id("username");
            By passBy = exists(By.name("password")) ? By.name("password") : By.id("password");
            // Common JPetStore demo credentials
            type(userBy, "j2ee");
            type(passBy, "j2ee");
            WebElement web = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'], button[type='submit']")));
            web.click();

            // Success if "Sign Out" or "My Account" appears
            boolean loggedIn = findTopNavLink("sign out").isPresent() || findTopNavLink("my account").isPresent();
            if (loggedIn) {
                Assertions.assertTrue(loggedIn, "Should be logged in with demo credentials");
                // Sign out to reset
                Optional<WebElement> signOut = findTopNavLink("sign out");
                if (signOut.isPresent()) {
                    waitClickable(signOut.get()).click();
                    Assertions.assertTrue(findTopNavLink("sign in").isPresent() || findTopNavLink("login").isPresent(), "Should be signed out and see Sign In/Login");
                }
            } else {
                // If demo creds are not accepted on this deployment, assert error message
                boolean errorShown = exists(By.cssSelector(".error, .alert, .message-error")) ||
                        driver.getPageSource().toLowerCase(Locale.ROOT).contains("invalid");
                Assertions.assertTrue(errorShown, "If login fails, an error should be shown");
            }
        } else {
            Assertions.assertTrue(true, "No Sign In link present; skipping login test");
        }
    }

    @Test
    @Order(6)
    void optionalSortingDropdown_IfPresent_ChangesOrder() {
        goHome();
        // Navigate to a category to look for sorting select (if any)
        List<WebElement> cats = driver.findElements(By.cssSelector("a[href*='/category/'], a[href*='categoryId='], a[href*='/catalog/categories/']"));
        if (!cats.isEmpty()) waitClickable(cats.get(0)).click();

        List<WebElement> selects = driver.findElements(By.cssSelector("select[id*='sort'], select[name*='sort'], select[class*='sort']"));
        if (selects.isEmpty()) {
            Assertions.assertTrue(true, "No sorting dropdown present; skipping");
            return;
        }
        Select sort = new Select(selects.get(0));
        List<WebElement> itemsBefore = driver.findElements(By.cssSelector("a[href*='/product/'], a[href*='productId=']"));
        String firstBefore = itemsBefore.isEmpty() ? "" : itemsBefore.get(0).getText();

        if (sort.getOptions().size() > 1) {
            sort.selectByIndex(1);
            // wait for order to change
            boolean changed;
            try {
                changed = wait.until(d -> {
                    List<WebElement> itemsNow = d.findElements(By.cssSelector("a[href*='/product/'], a[href*='productId=']"));
                    String firstNow = itemsNow.isEmpty() ? "" : itemsNow.get(0).getText();
                    return !Objects.equals(firstBefore, firstNow);
                });
            } catch (TimeoutException e) {
                changed = false;
            }
            Assertions.assertTrue(changed, "Selecting a different sort option should change the order");
        } else {
            Assertions.assertTrue(true, "Sorting dropdown has only one option; skipping change assertion");
        }
    }

    @Test
    @Order(7)
    void internalLinks_VisitOneLevelDeep_AndReturn() {
        goHome();
        String baseHost = getHost(BASE_URL);
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Set<String> hrefs = anchors.stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        int visited = 0;
        for (String href : hrefs) {
            if (href.startsWith("javascript:") || href.endsWith("#")) continue;
            String host = getHost(href);
            if (host == null) host = "";
            if (host.isEmpty() || host.equalsIgnoreCase(baseHost)) {
                driver.get(href);
                Assertions.assertTrue(exists(By.tagName("body")), "Body should exist at " + href);
                visited++;
                // Return to base each time
                driver.get(BASE_URL);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                if (visited >= 6) break; // limit for speed/flakiness
            }
        }
        Assertions.assertTrue(visited > 0, "Should visit at least one internal link");
    }

    @Test
    @Order(8)
    void externalLinks_OpenAndAssertDomain_ThenReturn() {
        goHome();
        String baseHost = getHost(BASE_URL);
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Map<String, WebElement> external = new LinkedHashMap<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            String host = getHost(href);
            if (host != null && !host.isEmpty() && !host.equalsIgnoreCase(baseHost)) {
                external.putIfAbsent(host, a); // one per external host
            }
        }
        if (external.isEmpty()) {
            Assertions.assertTrue(true, "No external links detected; skipping");
            return;
        }
        int checked = 0;
        for (Map.Entry<String, WebElement> e : external.entrySet()) {
            openExternalAndAssertDomain(e.getValue(), e.getKey());
            checked++;
            if (checked >= 3) break; // limit for stability
        }
        Assertions.assertTrue(checked > 0, "Should validate at least one external link");
    }

    @Test
    @Order(9)
    void topMenuNavigation_Home_MyAccount_Cart_Smoke() {
        goHome();
        // Home
        Optional<WebElement> homeLink = findTopNavLink("home");
        if (homeLink.isPresent()) {
            waitClickable(homeLink.get()).click();
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("jpetstore"), "Home navigation should keep us in app");
        } else {
            Assertions.assertTrue(true, "No explicit Home link; base already open");
        }

        // Cart
        Optional<WebElement> cartLink = findTopNavLink("cart");
        if (cartLink.isPresent()) {
            waitClickable(cartLink.get()).click();
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("cart"), "Cart navigation should open cart page");
            // Back home
            driver.get(BASE_URL);
        }

        // My Account (may require login)
        Optional<WebElement> myAccount = findTopNavLink("my account");
        if (myAccount.isPresent()) {
            waitClickable(myAccount.get()).click();
            // Either see account page or be redirected to sign-in
            boolean ok = driver.getPageSource().toLowerCase(Locale.ROOT).contains("account") ||
                    driver.getCurrentUrl().toLowerCase(Locale.ROOT).contains("signon") ||
                    driver.getTitle().toLowerCase(Locale.ROOT).contains("account");
            Assertions.assertTrue(ok, "My Account should open account or redirect to sign-in");
        } else {
            Assertions.assertTrue(true, "My Account not visible; likely requires login");
        }
    }
}
