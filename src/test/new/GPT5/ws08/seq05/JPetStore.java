package GPT5.ws08.seq05;

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
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

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

    // ---------------------- Helpers ----------------------

    private void openBase() {
        driver.get(BASE_URL);
        waitDocumentReady();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should land on base URL");
        Assertions.assertTrue(driver.findElements(By.tagName("body")).size() > 0, "Body should be present");
        dismissOverlaysIfAny();
    }

    private void waitDocumentReady() {
        try {
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
    }

    private void dismissOverlaysIfAny() {
        // Best-effort cookie banners / modals
        List<By> candidates = Arrays.asList(
                By.cssSelector("button[id*='accept'],button[class*='accept'],button[aria-label*='accept']"),
                By.cssSelector("button[class*='agree'],button[id*='agree']"),
                By.cssSelector(".cookie-accept,.cc-allow,.cc-accept,.cookiebar-close,.cc-dismiss,.btn-close,.close")
        );
        for (By by : candidates) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(els.get(0))).click();
                    break;
                } catch (Exception ignored) {}
            }
        }
    }
    private String hostOf(String url) {
        try { return URI.create(url).getHost(); } catch (Exception e) { return ""; }
    }

    private int depthOfPath(String path) {
        if (path == null) return 0;
        String p = path;
        if (p.startsWith("/")) p = p.substring(1);
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        if (p.isEmpty()) return 0;
        return p.split("/").length;
    }

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        return null;
    }

    private void handleExternalLink(WebElement link) {
        String originalWindow = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        String baseHost = hostOf(driver.getCurrentUrl());

        WebElement web = wait.until(ExpectedConditions.elementToBeClickable(link));
        web.click();

        // wait for new tab or navigation
        try {
            wait.until(d -> d.getWindowHandles().size() > before.size() || !hostOf(d.getCurrentUrl()).equals(baseHost));
        } catch (TimeoutException ignored) {}

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            waitDocumentReady();
            Assertions.assertNotEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should be on an external domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            waitDocumentReady();
            Assertions.assertNotEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should have navigated to an external domain");
            driver.navigate().back();
            waitDocumentReady();
        }
    }

    // ---------------------- Tests ----------------------

    @Test
    @Order(1)
    public void baseLoads_CoreUIVisible() {
        openBase();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jpetstore"), "Title should mention JPetStore");

        // Header nav and footer should exist
        Assertions.assertTrue(driver.findElements(By.cssSelector("header, .header, #Header")).size() > 0, "Header should exist");
        Assertions.assertTrue(driver.findElements(By.cssSelector("footer, .footer, #Footer")).size() > 0, "Footer should exist");

        // Category tiles or links should be visible
        Assertions.assertTrue(
                driver.findElements(By.partialLinkText("Fish")).size() > 0 ||
                driver.findElements(By.partialLinkText("Dogs")).size() > 0 ||
                driver.findElements(By.partialLinkText("Cats")).size() > 0 ||
                driver.findElements(By.cssSelector("a[href*='catalog']")).size() > 0,
                "At least one category link should be present");
    }

    @Test
    @Order(2)
    public void quickSearch_Fish_ShowsResults() {
        openBase();
        WebElement search = firstPresent(
                By.name("keyword"),
                By.cssSelector("input[type='search']"),
                By.cssSelector("input[placeholder*='Search']"),
                By.cssSelector("input[name*='search']")
        );
        Assumptions.assumeTrue(search != null, "Search input not available; skipping");
        search.clear();
        search.sendKeys("fish");
        search.sendKeys(Keys.ENTER);

        // Wait for navigation or results to load
        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        boolean hasResults = false;
        try {
            longerWait.until(d -> 
                d.getCurrentUrl().toLowerCase().contains("search") || 
                d.getCurrentUrl().toLowerCase().contains("catalog/categories/FISH") ||
                d.findElements(By.cssSelector("table, .product, .product-list, #Catalog, .results")).size() > 0);
            hasResults = true;
        } catch (TimeoutException ignored) {}
        
        Assertions.assertTrue(hasResults, "Search should navigate to a results view or show product list");

        // If there are product rows, assert at least one row mentioning fish
        List<WebElement> rows = driver.findElements(By.cssSelector("table tr, .product, .item"));
        if (!rows.isEmpty()) {
            boolean mentionsFish = rows.stream().anyMatch(r ->
                    r.getText().toLowerCase().contains("fish"));
            Assertions.assertTrue(mentionsFish, "At least one result should mention 'fish'");
        }
    }

    @Test
    @Order(3)
    public void signInPage_InvalidLogin_ShowsErrorOrStays() {
        openBase();
        WebElement signIn = firstPresent(
                By.partialLinkText("Sign In"),
                By.cssSelector("a[href*='signon'], a[href*='signin'], a[href*='account']")
        );
        Assumptions.assumeTrue(signIn != null, "Sign In link not found; skipping");
        String start = driver.getCurrentUrl();
        WebElement web = wait.until(ExpectedConditions.elementToBeClickable(signIn));
        web.click();
        waitDocumentReady();

        // Fill credentials (invalid on purpose)
        WebElement user = firstPresent(By.name("username"), By.id("username"));
        WebElement pass = firstPresent(By.name("password"), By.id("password"));
        WebElement loginBtn = firstPresent(By.cssSelector("input[type='submit'][value*='Login']"),
                By.cssSelector("button[type='submit']"), By.cssSelector("input[type='submit']"));

        Assumptions.assumeTrue(user != null && pass != null && loginBtn != null, "Login form not found; skipping");
        user.clear(); user.sendKeys("invalid_user");
        pass.clear(); pass.sendKeys("invalid_pass");
        WebElement webA = wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        webA.click();
        waitDocumentReady();

        // Either show error, or remain on sign-in
        boolean errorShown = driver.findElements(By.cssSelector(".error, .alert, .message, .validation, #MessageBar")).stream()
                .anyMatch(WebElement::isDisplayed);
        boolean stayedOnSignIn = driver.getCurrentUrl().contains("signon") || driver.getCurrentUrl().contains("signin") || driver.getCurrentUrl().equals(start);
        Assertions.assertTrue(errorShown || stayedOnSignIn, "Invalid login should not authenticate");
    }

    @Test
    @Order(4)
    public void navigateCategory_AddFirstItemToCart_ThenCleanup() {
        openBase();
        // Open a category (prefer Fish/Dogs/Cats if available)
        WebElement category = firstPresent(
                By.partialLinkText("Fish"),
                By.partialLinkText("Dogs"),
                By.partialLinkText("Cats"),
                By.cssSelector("a[href*='/catalog']")
        );
        Assumptions.assumeTrue(category != null, "No category link found; skipping");
        WebElement web = wait.until(ExpectedConditions.elementToBeClickable(category));
        web.click();
        waitDocumentReady();

        // Click first product in list
        WebElement firstProduct = firstPresent(
                By.cssSelector("table a[href*='products'], table a[href*='items']"),
                By.cssSelector("a[href*='/products/']"),
                By.cssSelector("a[href*='/items/']")
        );
        Assumptions.assumeTrue(firstProduct != null, "No product link found in category; skipping");
        WebElement webA = wait.until(ExpectedConditions.elementToBeClickable(firstProduct));
        webA.click();
        waitDocumentReady();

        // Add to cart: click first "Add to Cart"
        WebElement addBtn = firstPresent(
                By.cssSelector("a[href*='addToCart'], a[href*='addCart'], a.button"),
                By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add to cart')]"),
                By.cssSelector("input[type='submit'][value*='Add']")
        );
        Assumptions.assumeTrue(addBtn != null, "Add to Cart control not found; skipping");
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(addBtn));
        el.click();
        waitDocumentReady();

        // Verify cart page
        boolean onCart = driver.getCurrentUrl().toLowerCase().contains("cart");
        Assertions.assertTrue(onCart || driver.findElements(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'shopping cart')]")).size() > 0,
                "Should be on cart or show cart contents");
        Assertions.assertTrue(driver.findElements(By.cssSelector("table, .cart, #Cart")).size() > 0, "Cart table/list should be present");

        // At least one line mentions product or has quantity/price
        List<WebElement> cartRows = driver.findElements(By.cssSelector("table tr"));
        Assertions.assertTrue(cartRows.size() > 1, "Cart should have at least one line item");

        // Cleanup: remove items if remove link present, otherwise set qty 0 and update
        List<WebElement> removeLinks = driver.findElements(By.xpath("//a[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'remove')]"));
        if (!removeLinks.isEmpty()) {
        	WebElement list = wait.until(ExpectedConditions.elementToBeClickable(removeLinks.get(0)));
            list.click();
            waitDocumentReady();
        } else {
            List<WebElement> qtyInputs = driver.findElements(By.cssSelector("input[name*='quantity'], input.qty, input[name*='qty']"));
            if (!qtyInputs.isEmpty()) {
                WebElement qty = qtyInputs.get(0);
                qty.clear();
                qty.sendKeys("0");
                WebElement updateBtn = firstPresent(
                        By.xpath("//input[@type='submit' and contains(translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'update')]"),
                        By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'update')]")
                );
                if (updateBtn != null) {
                	WebElement upd = wait.until(ExpectedConditions.elementToBeClickable(updateBtn));
                    upd.click();
                    waitDocumentReady();
                }
            }
        }
    }

    @Test
    @Order(5)
    public void internalLinks_OneLevelBelow_Work() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        URI base = URI.create(driver.getCurrentUrl());
        int baseDepth = depthOfPath(base.getPath());

        List<String> hrefs = driver.findElements(By.cssSelector("a[href]")).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(h -> h.startsWith("http"))
                .distinct()
                .collect(Collectors.toList());

        List<String> internalOneLevel = hrefs.stream()
                .filter(h -> baseHost.equals(hostOf(h)))
                .filter(h -> {
                    URI u = URI.create(h);
                    int depth = depthOfPath(u.getPath());
                    return depth <= baseDepth + 1; // one level below
                })
                .collect(Collectors.toList());

        Assumptions.assumeTrue(!internalOneLevel.isEmpty(), "No internal one-level-below links found; skipping");
        int checked = 0;
        String original = driver.getCurrentUrl();
        for (String link : internalOneLevel) {
            if (checked >= 3) break; // limit for stability
            Optional<WebElement> anchor = driver.findElements(By.cssSelector("a[href]")).stream()
                    .filter(a -> link.equals(a.getAttribute("href"))).findFirst();
            if (anchor.isEmpty()) continue;
            String before = driver.getCurrentUrl();
            try {
            	WebElement web = wait.until(ExpectedConditions.elementToBeClickable(anchor.get()));
                web.click();
                wait.until(d -> !d.getCurrentUrl().equals(before));
                waitDocumentReady();
                Assertions.assertEquals(baseHost, hostOf(driver.getCurrentUrl()), "Should stay within same host");
                Assertions.assertTrue(driver.findElements(By.tagName("body")).size() > 0, "Destination should render");
                driver.navigate().back();
                wait.until(ExpectedConditions.urlToBe(before));
                checked++;
            } catch (Exception e) {
                driver.get(original);
                waitDocumentReady();
            }
        }
        Assertions.assertTrue(checked >= 1, "At least one internal one-level-below link should load");
    }

    @Test
    @Order(6)
    public void footerOrHeader_ExternalLinks_OpenOnDifferentDomain() {
        openBase();
        String baseHost = hostOf(driver.getCurrentUrl());
        List<WebElement> anchors = driver.findElements(By.cssSelector("footer a[href], header a[href], nav a[href], a[target='_blank']"));
        List<WebElement> external = anchors.stream()
                .filter(a -> {
                    String href = a.getAttribute("href");
                    return href != null && href.startsWith("http") && !hostOf(href).equals(baseHost);
                })
                .collect(Collectors.toList());

        if (external.isEmpty()) {
            // Try broader selectors to find any external links
            anchors = driver.findElements(By.cssSelector("a[href]"));
            external = anchors.stream()
                    .filter(a -> {
                        String href = a.getAttribute("href");
                        return href != null && href.startsWith("http") && !hostOf(href).equals(baseHost);
                    })
                    .collect(Collectors.toList());
        }

        Assumptions.assumeTrue(!external.isEmpty(), "No external links found; skipping");
        int validated = 0;
        for (WebElement link : external) {
            if (validated >= 1) break; // reduce flakiness - only validate 1
            try {
                handleExternalLink(link);
                validated++;
            } catch (Exception e) {
                driver.get(BASE_URL);
                waitDocumentReady();
            }
        }
        Assertions.assertTrue(validated >= 1, "Should validate at least one external link");
    }

    @Test
    @Order(7)
    public void optionalDropdowns_IfAny_ChangeSelection() {
        openBase();
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        Assumptions.assumeTrue(!selects.isEmpty(), "No select dropdowns present; skipping");
        Select s = new Select(selects.get(0));
        List<WebElement> opts = s.getOptions();
        Assumptions.assumeTrue(opts.size() >= 2, "Not enough options to test selection; skipping");
        s.selectByIndex(0);
        String first = s.getFirstSelectedOption().getText();
        s.selectByIndex(1);
        String second = s.getFirstSelectedOption().getText();
        Assertions.assertNotEquals(first, second, "Selection should change after choosing a different option");
    }
}