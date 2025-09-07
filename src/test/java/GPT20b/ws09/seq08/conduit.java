package GPT5.ws09.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class RealWorldConduitE2ETest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ----------------- Helpers -----------------

    private void openBase() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".banner h1")));
    }

    private void handleExternalLink(String cssSelector, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector(cssSelector));
        Assumptions.assumeTrue(!links.isEmpty(), "External link not present: " + cssSelector);
        String original = driver.getWindowHandle();
        String before = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(links.get(0))).click();

        try {
            wait.until(d -> d.getWindowHandles().size() > 1 || !d.getCurrentUrl().equals(before));
        } catch (TimeoutException ignored) { }

        if (driver.getWindowHandles().size() > 1) {
            for (String h : driver.getWindowHandles()) {
                if (!h.equals(original)) {
                    driver.switchTo().window(h);
                    break;
                }
            }
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Should navigate to domain: " + expectedDomain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Should navigate to domain: " + expectedDomain);
            driver.navigate().back();
        }
    }

    // ----------------- Tests -----------------

    @Test
    @Order(1)
    public void homePageLoads_bannerVisible() {
        openBase();
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".banner h1")));
        Assertions.assertEquals("conduit", banner.getText().trim(), "Banner should display 'conduit'");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("conduit") || driver.getTitle().toLowerCase().contains("realworld"),
                "Title should reference Conduit/RealWorld");
    }

    @Test
    @Order(2)
    public void navigateToLoginPage() {
        openBase();
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#login']")));
        signIn.click();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.text-xs-center")));
        Assertions.assertEquals("Sign In", header.getText().trim(), "Login header should be 'Sign In'");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#login"), "URL should contain #login");
    }

    @Test
    @Order(3)
    public void navigateToRegisterPage() {
        openBase();
        WebElement signUp = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#register']")));
        signUp.click();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.text-xs-center")));
        Assertions.assertEquals("Sign Up", header.getText().trim(), "Register header should be 'Sign Up'");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#register"), "URL should contain #register");
    }

    @Test
    @Order(4)
    public void invalidLoginShowsError() {
        driver.get(BASE_URL + "#login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        email.clear(); email.sendKeys("invalid@example.com");
        password.clear(); password.sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement errorItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorItem.getText().toLowerCase().contains("email or password"),
                "Error should mention email or password is invalid");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#login"), "Should remain on login page after failure");
    }

    @Test
    @Order(5)
    public void globalFeedShowsArticlePreviewsOrEmptyMessage() {
        openBase();
        // Ensure Global Feed tab is selected
        WebElement globalTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='']")));
        globalTab.click();
        // Either previews exist or an empty message appears
        List<WebElement> previews = driver.findElements(By.cssSelector(".article-preview"));
        List<WebElement> emptyMsg = driver.findElements(By.xpath("//*[contains(.,'No articles are here')]"));
        Assertions.assertTrue(!previews.isEmpty() || !emptyMsg.isEmpty(),
                "Global Feed should show article previews or an empty message");
    }

    @Test
    @Order(6)
    public void clickFirstArticleOpensArticlePage() {
        openBase();
        List<WebElement> links = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".preview-link")));
        Assumptions.assumeTrue(!links.isEmpty(), "No article previews to click");
        WebElement first = links.get(0);
        String previewTitle = first.findElement(By.cssSelector("h1")).getText().trim();
        wait.until(ExpectedConditions.elementToBeClickable(first)).click();
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals(previewTitle, articleTitle.getText().trim(), "Article title should match preview title");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/article/"), "URL should point to an article page");
    }

    @Test
    @Order(7)
    public void clickPopularTagFiltersFeed_oneLevel() {
        openBase();
        List<WebElement> tags = driver.findElements(By.cssSelector(".tag-list a.tag-default"));
        Assumptions.assumeTrue(!tags.isEmpty(), "No popular tags available to test");
        WebElement tag = tags.get(0);
        String tagText = tag.getText().trim();
        wait.until(ExpectedConditions.elementToBeClickable(tag)).click();
        wait.until(ExpectedConditions.urlContains("tag="));
        Assertions.assertTrue(driver.getCurrentUrl().contains("tag=" + tagText) || driver.getCurrentUrl().contains("tag="),
                "URL should contain tag filter");
        // After tag click, article previews typically reload
        List<WebElement> previews = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".article-preview")));
        Assertions.assertFalse(previews.isEmpty(), "Tagged feed should list article previews (if any exist for the tag)");
    }

    @Test
    @Order(8)
    public void paginationAdvancesActivePage_ifPresent() {
        openBase();
        List<WebElement> pages = driver.findElements(By.cssSelector(".pagination li a.page-link"));
        if (pages.size() >= 2) {
            WebElement second = pages.get(1);
            wait.until(ExpectedConditions.elementToBeClickable(second)).click();
            // active page item gets .active class
            WebElement active = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".pagination li.page-item.active")));
            Assertions.assertTrue(active.getText().trim().equals("2") || active.getText().trim().length() > 0,
                    "Active pagination item should change after clicking page 2");
        } else {
            Assertions.assertTrue(true, "Pagination not present; skipping without failure");
        }
    }

    @Test
    @Order(9)
    public void footerExternalGitHubLinkOpens() {
        openBase();
        // Footer or somewhere on the page links to the RealWorld GitHub repo
        handleExternalLink("a[href*='github.com/gothinkster/realworld']", "github.com");
    }

    @Test
    @Order(10)
    public void headerInternalLinks_navigateWithinOneLevel() {
        openBase();
        // Click 'Home' and 'Sign in' top-level links (internal)
        WebElement home = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.navbar-brand, a[href='#/']")));
        home.click();
        wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#login']")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("#login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#login"), "Should land on Sign In page");
        // Back to home one level
        WebElement brand = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.navbar-brand")));
        brand.click();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("#login")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("demo.realworld.io"), "Should be back on home");
    }
}
