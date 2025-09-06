package GPT4.ws09.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class RealWorldUITest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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

    private void openBasePage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.navbar-brand")));
    }

    private void switchToNewWindowAndAssertUrlContains(String expected) {
        String originalWindow = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        for (String handle : oldWindows) {
            driver.switchTo().window(handle);
        }

        wait.until(driver -> driver.getWindowHandles().size() > oldWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(oldWindows);
        String newWindow = newWindows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains(expected));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expected), "Expected URL to contain: " + expected);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        openBasePage();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("conduit"), "Page title should contain 'conduit'");
        WebElement brand = driver.findElement(By.cssSelector("a.navbar-brand"));
        Assertions.assertTrue(brand.isDisplayed(), "Navbar brand should be displayed");
    }

    @Test
    @Order(2)
    public void testNavigateToSignInPage() {
        openBasePage();
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#login']")));
        signInLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1.text-xs-center")));
        Assertions.assertEquals("Sign In", driver.findElement(By.cssSelector("h1.text-xs-center")).getText(), "Header should be 'Sign In'");
    }

    @Test
    @Order(3)
    public void testLoginWithInvalidCredentials() {
        openBasePage();
        driver.findElement(By.cssSelector("a.nav-link[href='#login']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        driver.findElement(By.cssSelector("input[type='email']")).sendKeys("invalid@example.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("invalidpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("email or password"), "Expected error message for invalid login");
    }

    @Test
    @Order(4)
    public void testNavigateToSignUpPage() {
        openBasePage();
        WebElement signUpLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#register']")));
        signUpLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1.text-xs-center")));
        Assertions.assertEquals("Sign Up", driver.findElement(By.cssSelector("h1.text-xs-center")).getText(), "Header should be 'Sign Up'");
    }

    @Test
    @Order(5)
    public void testGlobalFeedIsVisible() {
        openBasePage();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.nav-link.active")));
        List<WebElement> articles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.article-preview")));
        Assertions.assertFalse(articles.isEmpty(), "Expected articles in global feed");
    }

    @Test
    @Order(6)
    public void testExternalLinksInFooter() {
        openBasePage();
        List<WebElement> links = driver.findElements(By.cssSelector("footer a"));
        int tested = 0;
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("github") || href.contains("thinkster.io"))) {
                String originalWindow = driver.getWindowHandle();
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
                switchToNewWindowAndAssertUrlContains(href.contains("github") ? "github.com" : "thinkster.io");
                driver.switchTo().window(originalWindow);
                tested++;
                if (tested >= 2) break;
            }
        }
        Assertions.assertTrue(tested > 0, "Should test at least one external link");
    }

    @Test
    @Order(7)
    public void testTagNavigation() {
        openBasePage();
        WebElement tag = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tag-list a")));
        String tagName = tag.getText();
        tag.click();
        wait.until(ExpectedConditions.urlContains("tag="));
        Assertions.assertTrue(driver.getCurrentUrl().contains("tag="), "URL should contain selected tag");
        WebElement activeFeed = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".feed-toggle .nav-link.active")));
        Assertions.assertTrue(activeFeed.getText().contains(tagName), "Active feed tab should reflect selected tag");
    }

}
