package GPT4.ws07.seq06;

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
public class JSFIDDLE {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

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

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JSFiddle"));
        Assertions.assertTrue(driver.getTitle().contains("JSFiddle"), "Home page title should contain 'JSFiddle'");
    }

    @Test
    @Order(2)
    public void testNewFiddleButtonNavigatesToEditor() {
        driver.get(BASE_URL);
        WebElement newFiddleBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/']")));
        newFiddleBtn.click();
        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("jsfiddle.net"), "URL should remain on jsfiddle.net");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.panel_html")));
        Assertions.assertTrue(driver.findElement(By.cssSelector("div.panel_html")).isDisplayed(), "HTML editor should be visible");
    }

    @Test
    @Order(3)
    public void testExploreButtonNavigatesToExplorePage() {
        driver.get(BASE_URL);
        WebElement exploreLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("header a[href='/explore']")));
        exploreLink.click();
        wait.until(ExpectedConditions.urlContains("/explore"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/explore"), "Explore page should be loaded");
    }

    @Test
    @Order(4)
    public void testLoginLinkVisibility() {
        driver.get(BASE_URL);
        List<WebElement> loginLinks = driver.findElements(By.cssSelector("a[href='/user/login/']"));
        Assertions.assertFalse(loginLinks.isEmpty(), "Login link should be present");
        Assertions.assertTrue(loginLinks.get(0).isDisplayed(), "Login link should be visible");
    }

    @Test
    @Order(5)
    public void testInvalidLoginShowsError() {
        driver.get("https://jsfiddle.net/user/login/");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("id_login")));
        usernameField.sendKeys("invaliduser");
        driver.findElement(By.id("id_password")).sendKeys("invalidpass");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement errorBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorBox.isDisplayed(), "Error message should be shown for invalid login");
    }

    @Test
    @Order(6)
    public void testFooterLinkTwitter() {
        testExternalLink("footer a[href*='twitter.com']", "twitter.com");
    }

    @Test
    @Order(7)
    public void testFooterLinkFacebook() {
        testExternalLink("footer a[href*='facebook.com']", "facebook.com");
    }

    @Test
    @Order(8)
    public void testFooterLinkGitHub() {
        testExternalLink("footer a[href*='github.com']", "github.com");
    }

    private void testExternalLink(String selector, String expectedDomain) {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
        link.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains(expectedDomain));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains(expectedDomain), "URL should contain " + expectedDomain);

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testDocumentationLinkNavigatesToDocsPage() {
        driver.get(BASE_URL);
        WebElement docsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='docs']")));
        String originalWindow = driver.getWindowHandle();
        docsLink.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("docs"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("docs"), "Should open JSFiddle documentation");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testSearchBarPresence() {
        driver.get(BASE_URL + "explore");
        List<WebElement> searchInputs = driver.findElements(By.cssSelector("input[type='search']"));
        Assertions.assertTrue(searchInputs.isEmpty() || !searchInputs.get(0).isDisplayed(), "Search input should not be present on Explore page");
    }
}