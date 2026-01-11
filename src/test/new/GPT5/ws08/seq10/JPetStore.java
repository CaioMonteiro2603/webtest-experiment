package GPT5.ws08.seq10;

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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

    private static final By BODY = By.tagName("body");
    private static final By HEADER = By.cssSelector("header, #Header, .header");
    private static final By FOOTER = By.cssSelector("footer, #Footer, .footer");
    private static final By ANY_LINK = By.cssSelector("a[href]");
    private static final By CATEGORY_LINKS = By.xpath("//a[contains(.,'Fish') or contains(.,'Dogs') or contains(.,'Cats') or contains(.,'Reptiles') or contains(.,'Birds')]");
    private static final By SEARCH_INPUT = By.cssSelector("input[type='text'][name*='keyword' i], input[type='search'], form input[type='text']");
    private static final By ADD_TO_CART_LINKS = By.xpath("//a[contains(.,'Add to Cart') or contains(.,'Add to cart')]");
    private static final By CART_TABLE = By.xpath("//table[contains(@class,'cart') or contains(@id,'Cart') or //th[contains(.,'Item')]]");
    private static final By CART_QTY_INPUTS = By.xpath("//input[contains(@name,'quantity') or @name='quantity' or @type='number']");
    private static final By CART_UPDATE_BUTTON = By.xpath("//input[@type='submit' and (contains(@value,'Update') or contains(@value,'update'))] | //button[contains(.,'Update')]");
    private static final By SIGN_IN_LINK = By.xpath("//a[contains(.,'Sign In') or contains(.,'Sign in')] | //a[contains(@href,'signon') or contains(@href,'signin')]");
    private static final By HELP_LINK = By.xpath("//a[contains(.,'Help')]");
    private static final By ANY_SELECT = By.tagName("select");

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ---------------- helpers ----------------

    private static String hostOf(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean present(By by) {
        return !driver.findElements(by).isEmpty();
    }

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL), "Should land on BASE_URL");
    }

    private WebElement firstDisplayed(By by) {
        for (WebElement e : driver.findElements(by)) {
            if (e.isDisplayed()) return e;
        }
        throw new NoSuchElementException("No displayed element for: " + by);
    }

    private void click(WebElement el) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el)).click();
        } catch (ElementClickInterceptedException ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click()", el);
        }
    }

    private void scrollIntoView(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
        } catch (Exception ignored) {}
    }

    private void assertExternalLinkOpensAndClose(WebElement link) {
        String href = link.getAttribute("href");
        Assumptions.assumeTrue(href != null && href.startsWith("http"), "Not an external http(s) link");
        String expectedHost = hostOf(href);
        String original = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        scrollIntoView(link);
        click(link);
        wait.until(d -> d.getWindowHandles().size() > before.size()
                || hostOf(d.getCurrentUrl()).equalsIgnoreCase(expectedHost));

        if (driver.getWindowHandles().size() > before.size()) {
            Set<String> after = new HashSet<>(driver.getWindowHandles());
            after.removeAll(before);
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External URL should contain expected host: " + expectedHost);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedHost));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedHost), "External URL should contain expected host (same tab): " + expectedHost);
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
    }

    // ---------------- tests ----------------

    @Test
    @Order(1)
    void base_Should_Load_With_Header_Footer_And_Categories() {
        openBase();
        Assertions.assertAll("Core structure",
                () -> Assertions.assertTrue(present(HEADER), "Header should be present"),
                () -> Assertions.assertTrue(present(FOOTER), "Footer should be present"),
                () -> Assertions.assertTrue(present(ANY_LINK), "There should be links on the page")
        );
        String title = driver.getTitle();
        Assertions.assertTrue(title.toLowerCase().contains("jpetstore") || title.toLowerCase().contains("pet"),
                "Title should reference JPetStore/Pet");
        // categories (one level below)
        List<WebElement> cats = driver.findElements(CATEGORY_LINKS);
        Assertions.assertTrue(cats.size() > 0, "At least one category link (Fish/Dogs/Cats/Reptiles/Birds) should be visible");
    }

    @Test
    @Order(2)
    void navigate_Into_A_Category_And_Back() {
        openBase();
        WebElement category = firstDisplayed(CATEGORY_LINKS);
        String catName = category.getText().trim();
        String before = driver.getCurrentUrl();
        scrollIntoView(category);
        click(category);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        String after = driver.getCurrentUrl();
        Assertions.assertTrue(!after.equals(before), "URL should change when entering a category");
        // Assert category page shows some products/links
        List<WebElement> productLinks = driver.findElements(By.xpath("//a[contains(@href,'/catalog/products/') or contains(@href,'/catalog/items/') or contains(@href,'/catalog/categories/') or contains(.,'Add to Cart')]"));
        Assertions.assertTrue(productLinks.size() > 0, "Category " + catName + " should list products/items");
        driver.navigate().to(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
    }

    @Test
    @Order(3)
    void go_To_Help_And_SignIn_From_Header_If_Present() {
        openBase();
        // Help (internal)
        if (present(HELP_LINK)) {
            WebElement help = firstDisplayed(HELP_LINK);
            String before = driver.getCurrentUrl();
            scrollIntoView(help);
            click(help);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            String after = driver.getCurrentUrl();
            Assertions.assertTrue(!after.equals(before) || after.contains("help") || after.contains("Help"),
                    "URL should change after clicking Help");
            driver.navigate().to(BASE_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        } else {
            Assumptions.assumeTrue(false, "Help link not present - skipping");
        }

        // Sign In (internal)
        if (present(SIGN_IN_LINK)) {
            WebElement signIn = firstDisplayed(SIGN_IN_LINK);
            String before = driver.getCurrentUrl();
            scrollIntoView(signIn);
            click(signIn);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            String after = driver.getCurrentUrl();
            Assertions.assertTrue(!after.equals(before) || after.toLowerCase().contains("sign") || after.toLowerCase().contains("account"),
                    "URL should change or contain sign/account after clicking Sign In");
            driver.navigate().to(BASE_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        } else {
            Assumptions.assumeTrue(false, "Sign In link not present - skipping");
        }
    }

    @Test
    @Order(4)
    void external_Links_In_Footer_Should_Open_Then_Close() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a[href], #Footer a[href], .footer a[href]"));
        if (footerLinks.isEmpty()) {
            // fall back: any external link visible
            footerLinks = driver.findElements(ANY_LINK);
        }
        List<WebElement> externals = footerLinks.stream()
                .filter(WebElement::isDisplayed)
                .filter(a -> {
                    String href = a.getAttribute("href");
                    if (href == null || !href.startsWith("http")) return false;
                    String host = hostOf(href);
                    return !host.equalsIgnoreCase(baseHost);
                })
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!externals.isEmpty(), "No external links found to test");
        int tested = 0;
        for (WebElement link : externals) {
            String href = link.getAttribute("href");
            if (href == null || !href.startsWith("http")) continue;
            String h = hostOf(href);
            if (h.isEmpty()) continue;
            Set<String> hosts = new HashSet<>();
            if (hosts.contains(h)) continue;
            hosts.add(h);
            assertExternalLinkOpensAndClose(link);
            tested++;
            if (tested >= 3) break;
        }
        Assertions.assertTrue(tested > 0, "At least one external link should be validated");
    }

    @Test
    @Order(5)
    void search_Box_If_Present_Should_Return_Results() {
        openBase();
        if (!present(SEARCH_INPUT)) {
            Assumptions.assumeTrue(false, "Search input not present - skipping");
        }
        WebElement search = firstDisplayed(SEARCH_INPUT);
        search.clear();
        search.sendKeys("fish");
        search.sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        List<WebElement> results = driver.findElements(By.xpath("//a[contains(@href,'/catalog/products/') or contains(@href,'/catalog/items/') or contains(@href,'/catalog/categories/')]"));
        Assertions.assertTrue(results.size() > 0, "Searching for 'fish' should return product/item links");
    }

    @Test
    @Order(6)
    void add_First_Product_To_Cart_Then_Update_Quantity() {
        // Navigate into a category first
        openBase();
        WebElement category = firstDisplayed(CATEGORY_LINKS);
        scrollIntoView(category);
        click(category);
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));

        // Go to first product page or directly add to cart if available
        List<WebElement> productLinks = driver.findElements(By.xpath("//a[contains(@href,'/catalog/products/')]"));
        if (!productLinks.isEmpty()) {
            WebElement product = productLinks.get(0);
            scrollIntoView(product);
            click(product);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }

        // Add to Cart
        List<WebElement> adds = driver.findElements(ADD_TO_CART_LINKS);
        Assumptions.assumeTrue(!adds.isEmpty(), "No 'Add to Cart' links found");
        WebElement add = adds.get(0);
        scrollIntoView(add);
        click(add);
        // Cart should be visible/URL changed
        wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains("cart") || present(CART_TABLE),
                "After adding to cart, cart page/table should be visible");

        // Update quantity if possible
        List<WebElement> qtyInputs = driver.findElements(CART_QTY_INPUTS);
        if (!qtyInputs.isEmpty()) {
            WebElement qty = qtyInputs.get(0);
            scrollIntoView(qty);
            qty.clear();
            qty.sendKeys("2");
            // Click update button if present, else press Enter
            if (present(CART_UPDATE_BUTTON)) {
                WebElement upd = firstDisplayed(CART_UPDATE_BUTTON);
                scrollIntoView(upd);
                click(upd);
            } else {
                qty.sendKeys(Keys.ENTER);
            }
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            // Assert quantity value is now 2
            String val = qty.getAttribute("value");
            Assertions.assertTrue("2".equals(val) || driver.getPageSource().contains(">2<"),
                    "Cart quantity should be updated to 2");
        }
    }

    @Test
    @Order(7)
    void remove_Items_From_Cart_If_Present() {
        // Directly open cart page if known; otherwise add then remove
        // Try to find a cart link
        openBase();
        Optional<WebElement> cartLink = driver.findElements(By.xpath("//a[contains(.,'Cart') or contains(@href,'/cart')]"))
                .stream().filter(WebElement::isDisplayed).findFirst();
        if (cartLink.isPresent()) {
            click(cartLink.get());
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }

        if (!present(CART_TABLE)) {
            // If cart is empty, add one item quickly
            if (present(CATEGORY_LINKS)) {
                WebElement cat = firstDisplayed(CATEGORY_LINKS);
                click(cat);
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
                List<WebElement> adds = driver.findElements(ADD_TO_CART_LINKS);
                if (!adds.isEmpty()) {
                    click(adds.get(0));
                    wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
                }
            }
        }

        // Remove links
        List<WebElement> removes = driver.findElements(By.xpath("//a[contains(.,'Remove') or contains(@href,'remove')]"));
        if (!removes.isEmpty()) {
            int toRemove = Math.min(2, removes.size());
            for (int i = 0; i < toRemove; i++) {
                WebElement rem = removes.get(i);
                scrollIntoView(rem);
                click(rem);
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            }
            // Assert either fewer remove links or empty message
            List<WebElement> afterRemoves = driver.findElements(By.xpath("//a[contains(.,'Remove') or contains(@href,'remove')]"));
            Assertions.assertTrue(afterRemoves.size() <= removes.size(), "Cart should have fewer or equal 'Remove' links after removal");
        } else {
            Assumptions.assumeTrue(false, "No remove links present - cart may already be empty");
        }
    }

    @Test
    @Order(8)
    void generic_Select_If_Present_Should_Change_Selection() {
        openBase();
        if (!present(ANY_SELECT)) {
            // Try a category page for potential sorting/filters
            if (present(CATEGORY_LINKS)) {
                WebElement cat = firstDisplayed(CATEGORY_LINKS);
                click(cat);
                wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            }
        }
        Assumptions.assumeTrue(present(ANY_SELECT), "No select element present to exercise");
        WebElement selectEl = firstDisplayed(ANY_SELECT);
        Select sel = new Select(selectEl);
        List<WebElement> options = sel.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Select does not offer multiple options");
        String before = sel.getFirstSelectedOption().getText();
        sel.selectByIndex(1);
        String after = sel.getFirstSelectedOption().getText();
        Assertions.assertNotEquals(before, after, "Selected option should change after selecting a different index");
    }

    @Test
    @Order(9)
    void internal_Links_One_Level_From_Home_Should_Remain_On_Domain() {
        openBase();
        String baseHost = hostOf(BASE_URL);
        List<String> internal = driver.findElements(ANY_LINK).stream()
                .map(a -> a.getAttribute("href"))
                .filter(Objects::nonNull)
                .filter(href -> {
                    String host = hostOf(href);
                    return href.startsWith("#") || href.startsWith("/")
                            || host.isEmpty() || host.equalsIgnoreCase(baseHost)
                            || href.startsWith(BASE_URL);
                })
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
        Assumptions.assumeTrue(!internal.isEmpty(), "No internal links discovered on the home page");
        int visited = 0;
        for (String href : internal) {
            String target = href;
            if (href.startsWith("/")) target = BASE_URL + href.substring(1);
            driver.navigate().to(target);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
            Assertions.assertTrue(hostOf(driver.getCurrentUrl()).equalsIgnoreCase(baseHost),
                    "Should remain within site host on one-level navigation");
            visited++;
            driver.navigate().to(BASE_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(BODY));
        }
        Assertions.assertTrue(visited > 0, "Visited at least one internal link");
    }
}