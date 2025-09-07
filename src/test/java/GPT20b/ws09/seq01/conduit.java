package GPT20b.ws09.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class RealWorldDemoTest {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "demo";
    private static final String PASSWORD = "demo";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
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

    /* ------------------------------------------------------------------ */
    /*  Generic helpers                                                   */
    /* ------------------------------------------------------------------ */

    private void goTo(String url) {
        driver.get(url);
    }

    private void waitForVisibility(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void waitForClickability(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /* ------------------------------------------------------------------ */
    /*  Tests                                                              */
    /* ------------------------------------------------------------------ */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        goTo(BASE_URL);
        wait.until(ExpectedConditions.titleContains("RealWorld Demo"));
        Assertions.assertTrue(
                driver.getTitle().toLowerCase().contains("realworld"),
                "Home page title should contain 'RealWorld'.");
    }

    @Test
    @Order(2)
    public void testLoginWithValidCredentials() {
        goTo(BASE_URL);
        By loginLink = By.linkText("Login");
        waitForClickability(loginLink);
        driver.findElement(loginLink).click();

        waitForVisibility(By.id("email-address"));
        driver.findElement(By.id("email-address")).sendKeys(USERNAME + "@example.com");
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-submit-button")).click();

        wait.until(ExpectedConditions.urlMatches(".*/\\?login=true$"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/?login=true"),
                "After login URL should include '?login=true'.");
    }

    @Test
    @Order(3)
    public void testLoginWithInvalidCredentials() {
        goTo(BASE_URL);
        By loginLink = By.linkText("Login");
        waitForClickability(loginLink);
        driver.findElement(loginLink).click();

        waitForVisibility(By.id("email-address"));
        driver.findElement(By.id("email-address")).sendKeys("invalid@example.com");
        driver.findElement(By.id("password")).sendKeys("wrong");
        driver.findElement(By.id("login-submit-button")).click();

        By errorMsg = By.xpath("//*[contains(text(),'E‑mail or password is incorrect')]");
        waitForVisibility(errorMsg);
        Assertions.assertTrue(
                driver.findElement(errorMsg).isDisplayed(),
                "Error message should appear for invalid credentials.");
    }

    @Test
    @Order(4)
    public void testNavigationToArticleAndBack() {
        goTo(BASE_URL + "#/article/");
        // Article list header
        By listHeader = By.xpath("//h1[normalize-space()='Home']");
        waitForVisibility(listHeader);
        Assertions.assertTrue(
                driver.findElement(listHeader).isDisplayed(),
                "Home header should be visible.");

        // Click first article
        By articleLink = By.cssSelector("article.card-item a");
        waitForClickability(articleLink);
        driver.findElement(articleLink).click();

        // Article view header
        By articleHeader = By.cssSelector("h1.article-title");
        waitForVisibility(articleHeader);
        Assertions.assertTrue(
                driver.findElement(articleHeader).isDisplayed(),
                "Article title should be visible.");

        // Go back to list
        driver.navigate().back();
        waitForVisibility(listHeader);
        Assertions.assertTrue(
                driver.findElement(listHeader).isDisplayed(),
                "Returned to article list.");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        goTo(BASE_URL);
        String originalHandle = driver.getWindowHandle();

        List<String> domains = List.of("twitter.com", "github.com", "linkedin.com");
        for (String domain : domains) {
            List<WebElement> links = driver.findElements(
                    By.xpath("//a[contains(@href,'" + domain + "')]"));
            if (!links.isEmpty()) {
                links.get(0).click();

                wait.until(driver1 -> {
                    Set<String> handles = driver1.getWindowHandles();
                    return handles.size() > 1 || !driver1.getCurrentUrl().equals(BASE_URL);
                });

                Set<String> handles = driver.getWindowHandles();
                if (handles.size() > 1) {
                    handles.remove(originalHandle);
                    String newHandle = handles.iterator().next();
                    driver.switchTo().window(newHandle);
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "External link should load domain " + domain);
                    driver.close();
                    driver.switchTo().window(originalHandle);
                } else {
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "External link should load domain " + domain);
                    driver.navigate().back();
                    wait.until(ExpectedConditions.urlToBe(BASE_URL));
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testMenuAboutPage() {
        goTo(BASE_URL);
        By aboutLink = By.xpath("//a[normalize-space()='About']");
        waitForClickability(aboutLink);
        driver.findElement(aboutLink).click();

        waitForVisibility(By.cssSelector("h2"));
        Assertions.assertTrue(
                driver.findElement(By.cssSelector("h2")).isDisplayed(),
                "About page header should be visible.");

        driver.navigate().back();
        waitForVisibility(By.xpath("//h1[normalize-space()='Home']"));
    }

    @Test
    @Order(7)
    public void testLogout() {
        // Ensure we are logged in
        goTo(BASE_URL);
        By loginLink = By.linkText("Login");
        waitForClickability(loginLink);
        driver.findElement(loginLink).click();

        waitForVisibility(By.id("email-address"));
        driver.findElement(By.id("email-address")).sendKeys(USERNAME + "@example.com");
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-submit-button")).click();

        wait.until(ExpectedConditions.urlMatches(".*/\\?login=true$"));

        // Client menu appears
        By userMenu = By.cssSelector("button[aria-label='menu']");
        waitForClickability(userMenu);
        driver.findElement(userMenu).click();

        By logoutLink = By.xpath("//a[normalize-space()='Logout']");
        waitForClickability(logoutLink);
        driver.findElement(logoutLink).click();

        wait.until(ExpectedConditions.urlMatches(".*/$"));
        Assertions.assertTrue(
                driver.getCurrentUrl().equals(BASE_URL),
                "After logout, should return to home page.");
    }
}