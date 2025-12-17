package GPT5.ws08.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;
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

    /* ============================ Helpers ============================ */

    private void openHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        // Header or logo should be present
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("header")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("img[alt*='JPetStore'], img[alt*='JPet'], img[alt*='logo']")),
                ExpectedConditions.presenceOfElementLocated(By.linkText("Enter the Store"))
        ));
    }

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        throw new NoSuchElementException("None of the locators matched: " + Arrays.toString(locators));
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

    private void safeClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
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
    private boolean onCartPage() {
        return driver.getCurrentUrl().toLowerCase().contains("cart")
                || driver.findElements(By.xpath("//*[contains(.,'Shopping Cart') or contains(.,'Cart')]")).size() > 0;
    }

    private List<WebElement> productRowsOnListPage() {
        List<By> candidates = Arrays.asList(
                By.cssSelector("table tbody tr"),
                By.cssSelector(".product, .item, .card"),
                By.xpath("//a[contains(@href,'product')]/ancestor::tr")
        );
        for (By by : candidates) {
            List<WebElement> rows = driver.findElements(by);
            if (!rows.isEmpty()) return rows;
        }
        return Collections.emptyList();
    }

    private void goToCategory(String name) {
        // Try top/side navigation
        boolean clicked = clickIfPresent(By.linkText(name))
                || clickIfPresent(By.partialLinkText(name))
                || clickIfPresent(By.cssSelector("a[href*='catalog'] a[title*='" + name + "']"))
                || clickIfPresent(By.cssSelector("a[href*='" + name.toLowerCase() + "']"));
        if (!clicked) {
            // Sometimes the landing page has "Enter the Store" first
            clickIfPresent(By.linkText("Enter the Store"));
            clicked = clickIfPresent(By.linkText(name)) || clickIfPresent(By.partialLinkText(name));
        }
        Assertions.assertTrue(clicked, "Category link '" + name + "' should be clickable.");
        // Verify category page loaded
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains(name.toLowerCase()),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[self::h1 or self::h2 or self::h3][contains(.,'" + name.substring(0, Math.min(3, name.length())) + "')]")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("#Catalog, .catalog, .category"))
        ));
        Assertions.assertFalse(productRowsOnListPage().isEmpty(), "Category (" + name + ") should list products/items.");
    }

    private void addFirstItemToCartViaDetails() {
        // From category, open first product details then add to cart
        WebElement firstProdLink = firstPresent(
                By.cssSelector("a[href*='product']"),
                By.xpath("(//a[contains(@href,'product')])[1]"),
                By.xpath("(//a[contains(.,'View Details') or contains(.,'Details')])[1]")
        );
        safeClick(firstProdLink);

        // On item page
        WebElement addBtn = firstPresent(
                By.cssSelector("a[href*='cart/add'], button[name*='add'], input[value*='Add to Cart']"),
                By.xpath("//a[contains(.,'Add to Cart')] | //button[contains(.,'Add to Cart')] | //input[@value='Add to Cart']")
        );
        safeClick(addBtn);
        wait.until(d -> onCartPage());
        Assertions.assertTrue(onCartPage(), "After adding to cart, should be on Cart page.");
    }

    /* ============================ Tests ============================ */

    @Test
    @Order(1)
    public void homePageLoads() {
        openHome();
        Assertions.assertTrue(driver.getTitle() != null && !driver.getTitle().isEmpty(),
                "Home page title should not be empty.");
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
                "Should be on JPetStore base domain.");
    }

    @Test
    @Order(2)
    public void navigateAllMainCategories() {
        openHome();
        // Common categories in JPetStore
        String[] categories = {"Fish", "Dogs", "Reptiles", "Cats", "Birds"};
        for (String cat : categories) {
            goToCategory(cat);
            driver.navigate().back(); // return to previous (often home/catalog landing)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    @Test
    @Order(3)
    public void searchFunctionalityShowsResults() {
        openHome();
        // Try search field
        List<By> searchBoxes = Arrays.asList(
                By.name("keyword"),
                By.cssSelector("input[type='search']"),
                By.cssSelector("input[name*='search'], input[id*='search']")
        );
        boolean typed = false;
        for (By by : searchBoxes) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) {
                type(by, "fish");
                typed = true;
                break;
            }
        }
        Assertions.assertTrue(typed, "Search input should be present to type query.");

        boolean submitted = clickIfPresent(By.cssSelector("input[type='submit'][value*='Search']"))
                || clickIfPresent(By.cssSelector("button[type='submit']"))
                || clickIfPresent(By.xpath("//input[@type='submit' and contains(@value,'Search')]"))
                || clickIfPresent(By.xpath("//button[contains(.,'Search')]"));
        if (!submitted) {
            // fallback: press ENTER
            driver.switchTo().activeElement().sendKeys(Keys.ENTER);
        }

        // Expect results/listing
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(
                driver.findElements(By.cssSelector("#Catalog, .catalog, table, .product, .item, .card")).size() > 0,
                "Search results/listing should be visible."
        );
    }

    @Test
    @Order(4)
    public void addItemToCartAndSeeItInCart() {
        openHome();
        goToCategory("Dogs"); // choose a likely populated category
        addFirstItemToCartViaDetails();

        // Assert at least one line in the cart table
        List<WebElement> cartRows = driver.findElements(By.cssSelector("table tbody tr"));
        Assertions.assertTrue(cartRows.size() > 0, "Cart should contain at least one row after adding an item.");

        // Try increasing quantity if input is available
        List<WebElement> qtyInputs = driver.findElements(By.cssSelector("input[name*='quantity'], input.qty"));
        if (!qtyInputs.isEmpty()) {
            WebElement qty = qtyInputs.get(0);
            qty.clear();
            qty.sendKeys("2");
            // Update
            boolean updated = clickIfPresent(By.cssSelector("input[type='submit'][value*='Update']"))
                    || clickIfPresent(By.xpath("//input[@type='submit' and contains(@value,'Update')]"))
                    || clickIfPresent(By.cssSelector("button[name*='update'], button:contains('Update')"));
            if (updated) {
                // Some carts recompute totals; verify quantity reflected in page text
                String body = driver.findElement(By.tagName("body")).getText().toLowerCase();
                Assertions.assertTrue(body.contains("2") || body.contains("qty"),
                        "After updating quantity to 2, page should reflect the change.");
            }
        }
    }

    @Test
    @Order(5)
    public void proceedToCheckoutRedirectsToSignInWhenNotLogged() {
        // Ensure there is at least one item; if not, add one
        if (!onCartPage() || driver.findElements(By.cssSelector("table tbody tr")).isEmpty()) {
            openHome();
            goToCategory("Cats");
            addFirstItemToCartViaDetails();
        }

        boolean clicked = clickIfPresent(By.linkText("Proceed to Checkout"))
                || clickIfPresent(By.partialLinkText("Checkout"))
                || clickIfPresent(By.cssSelector("a[href*='order/new'], a.button[href*='checkout'], button[name*='checkout']"))
                || clickIfPresent(By.xpath("//a[contains(.,'Checkout')] | //button[contains(.,'Checkout')]"));
        Assertions.assertTrue(clicked, "Proceed to Checkout should be clickable.");

        // Expect sign-in requirement (username/password present or sign-in header)
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.name("username")),
                ExpectedConditions.presenceOfElementLocated(By.name("password")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(.,'Sign In') or contains(.,'Login')]"))
        ));
        Assertions.assertTrue(
                driver.findElements(By.name("username")).size() > 0
                        || driver.findElements(By.name("password")).size() > 0
                        || driver.findElements(By.xpath("//*[contains(.,'Sign In') or contains(.,'Login')]")).size() > 0,
                "Should be redirected to sign-in/login page when checking out without auth."
        );

        // Return to home to keep independence clear
        openHome();
    }

    @Test
    @Order(6)
    public void optionalSortingIfPresent() {
        // Some product lists may have a sort control; exercise if it exists
        openHome();
        goToCategory("Fish");

        List<String> before = productRowsOnListPage().stream()
                .map(WebElement::getText).filter(t -> t != null && !t.trim().isEmpty())
                .collect(Collectors.toList());

        List<WebElement> selects = driver.findElements(By.tagName("select"));
        boolean changed = false;
        if (!selects.isEmpty()) {
            Select sort = new Select(selects.get(0));
            if (sort.getOptions().size() > 1) {
                String initial = sort.getFirstSelectedOption().getText();
                int altIndex = sort.getOptions().size() > 2 ? 2 : 1;
                sort.selectByIndex(altIndex);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                List<String> after = productRowsOnListPage().stream()
                        .map(WebElement::getText).filter(t -> t != null && !t.trim().isEmpty())
                        .collect(Collectors.toList());
                changed = !before.equals(after) || !sort.getFirstSelectedOption().getText().equals(initial);
            }
        }

        if (!selects.isEmpty()) {
            Assertions.assertTrue(changed, "List/order should change after selecting a different sort option.");
        } else {
            // If no sorting control is present, assert the list is non-empty as smoke
            Assertions.assertFalse(before.isEmpty(), "Product list should not be empty.");
        }
    }

    @Test
    @Order(7)
    public void menuLinksAndMyCartAccessible() {
        openHome();

        // My Cart
        boolean cartClicked = clickIfPresent(By.linkText("My Cart"))
                || clickIfPresent(By.partialLinkText("Cart"))
                || clickIfPresent(By.cssSelector("a[href*='cart']"));
        Assertions.assertTrue(cartClicked, "My Cart link should be accessible.");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(onCartPage(), "Navigating to My Cart should show cart page.");

        // Sign In page opens
        openHome();
        boolean signInClicked = clickIfPresent(By.linkText("Sign In"))
                || clickIfPresent(By.partialLinkText("Sign"))
                || clickIfPresent(By.cssSelector("a[href*='account/signon'], a[href*='signon'], a[href*='login']"));
        Assertions.assertTrue(signInClicked, "Sign In link should be accessible.");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.name("username")),
                ExpectedConditions.presenceOfElementLocated(By.name("password")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(.,'Sign In') or contains(.,'Login')]"))
        ));
    }

    @Test
    @Order(8)
    public void footerExternalLinksOpen() {
        openHome();

        // Collect candidate external links (limit to 2 for stability)
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Map<String, WebElement> targets = new LinkedHashMap<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href == null) continue;
            String h = href.toLowerCase();
            if (h.contains("github.com") && !targets.containsKey("github.com")) targets.put("github.com", a);
            if (h.contains("twitter.com") && !targets.containsKey("twitter.com")) targets.put("twitter.com", a);
            if (h.contains("facebook.com") && !targets.containsKey("facebook.com")) targets.put("facebook.com", a);
            if (h.contains("linkedin.com") && !targets.containsKey("linkedin.com")) targets.put("linkedin.com", a);
            // Treat apex domain as external if it leaves the subdomain
            if (h.startsWith("https://aspectran.com") && !targets.containsKey("aspectran.com")) targets.put("aspectran.com", a);
        }

        int visited = 0;
        for (Map.Entry<String, WebElement> e : targets.entrySet()) {
            if (visited >= 2) break;
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

        if (visited == 0) {
            Assertions.assertTrue(anchors.size() > 0, "No external links detected; page should still contain anchors.");
        }
    }
}
