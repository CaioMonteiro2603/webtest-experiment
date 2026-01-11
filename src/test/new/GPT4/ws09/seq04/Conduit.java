package GPT4.ws09.seq04;

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
public class Conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.logo-font")));
        Assertions.assertEquals("conduit", title.getText().trim(), "Homepage logo text mismatch");
        Assertions.assertTrue(driver.getCurrentUrl().contains("realworld.io"), "Homepage URL is incorrect");
    }

    @Test
    @Order(2)
    public void testSignInWithValidCredentials() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#/login']")));
        signInLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));

        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        email.clear();
        password.clear();
        email.sendKeys("demo@demo.com");
        password.sendKeys("demo");
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();

        WebElement userLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.nav-link[href='#/@demo']")));
        Assertions.assertTrue(userLink.getText().contains("demo"), "Login failed - username not visible");
    }

    @Test
    @Order(3)
    public void testSignInWithInvalidCredentials() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#/login']")));
        signInLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));

        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        email.clear();
        password.clear();
        email.sendKeys("wrong@user.com");
        password.sendKeys("invalid");
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("email or password is invalid"), "Expected error message for invalid login");
    }

    @Test
    @Order(4)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> footerLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("footer a")));
        String originalWindow = driver.getWindowHandle();

        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("github.com") || href.contains("thinkster.io"))) {
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
                wait.until(d -> driver.getWindowHandles().size() > 1);
                Set<String> handles = driver.getWindowHandles();
                for (String handle : handles) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        wait.until(ExpectedConditions.urlContains(href.contains("github.com") ? "github.com" : "thinkster.io"));
                        Assertions.assertTrue(driver.getCurrentUrl().contains(href.contains("github.com") ? "github.com" : "thinkster.io"), "External link URL mismatch");
                        driver.close();
                        driver.switchTo().window(originalWindow);
                    }
                }
            }
        }
    }

    @Test
    @Order(5)
    public void testNavigateToSettingsAndLogout() {
        // Ensure user is logged in first
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#/login']")));
        signInLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));
        driver.findElement(By.cssSelector("input[type='email']")).sendKeys("demo@demo.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("demo");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='#/settings']"))).click();

        WebElement settingsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.text-xs-center")));
        Assertions.assertEquals("Your Settings", settingsTitle.getText().trim(), "Settings page title mismatch");

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline-danger")));
        logoutButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='#/login']")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/"), "Should return to home after logout");
    }

    @Test
    @Order(6)
    public void testTagFilterNavigation() {
        driver.get(BASE_URL);
        WebElement tag = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".sidebar .tag-list a")));
        String tagName = tag.getText().trim();
        tag.click();

        WebElement feedTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.feed-toggle li.nav-item a.nav-link.active")));
        Assertions.assertTrue(feedTitle.getText().contains(tagName), "Tag filter not applied correctly");
    }

    @Test
    @Order(7)
    public void testArticlePreviewNavigation() {
        driver.get(BASE_URL);
        WebElement preview = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("article-list article a.preview-link")));
        preview.click();

        wait.until(ExpectedConditions.urlContains("#/article/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/article/"), "Did not navigate to article page");
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertFalse(articleTitle.getText().isEmpty(), "Article title should be present");
    }
}