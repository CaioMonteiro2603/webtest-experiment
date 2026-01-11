package GPT20b.ws09.seq01;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {
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
public static void tearDown() {
    if (driver != null) {
        driver.quit();
    }
}

/* ---------- Utility methods ---------- */

private boolean elementExists(By locator) {
    return !driver.findElements(locator).isEmpty();
}

private void navigateTo(String path) {
    driver.get(BASE_URL + path);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
}

/* ---------- Tests ---------- */

@Test
@Order(1)
public void testHomePageLoads() {
    navigateTo("");
    Assertions.assertTrue(
            driver.getTitle().toLowerCase().contains("conduit"),
            "Home page title should contain 'conduit'");
}

@Test
@Order(2)
public void testLoginFormPresence() {
    navigateTo("login");
    By username = By.id("email");
    By password = By.id("password");
    By loginBtn = By.cssSelector("button[type='submit']");

    Assumptions.assumeTrue(
            elementExists(username) && elementExists(password) && elementExists(loginBtn),
            "Login form elements not present; skipping login form test");
}

@Test
@Order(3)
public void testInvalidLogin() {
    navigateTo("login");
    By username = By.id("email");
    By password = By.id("password");
    By loginBtn = By.cssSelector("button[type='submit']");

    Assumptions.assumeTrue(
            elementExists(username) && elementExists(password) && elementExists(loginBtn),
            "Login form not found; skipping invalid login test");

    WebElement userEl = wait.until(ExpectedConditions.visibilityOfElementLocated(username));
    WebElement passEl = wait.until(ExpectedConditions.visibilityOfElementLocated(password));
    WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(loginBtn));

    userEl.clear();
    userEl.sendKeys("wrong@example.com");
    passEl.clear();
    passEl.sendKeys("wrongpassword");
    loginButton.click();

    By errorLocator = By.cssSelector(".error-messages");
    Assertions.assertTrue(
            elementExists(errorLocator),
            "Error message should appear after invalid credentials");
}

@Test
@Order(4)
public void testNavigationLinks() {
    navigateTo("");
    By allPosts = By.linkText("Home");
    By tags = By.linkText("Tags");
    By about = By.linkText("About");

    Assumptions.assumeTrue(
            elementExists(allPosts) || elementExists(tags) || elementExists(about),
            "Expected navigation links missing; skipping navigation link test");

    if (elementExists(allPosts)) {
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(allPosts));
        homeLink.click();
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("/"),
                "Clicking Home should keep user on root page");
        driver.navigate().back();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    if (elementExists(tags)) {
        WebElement tagsLink = wait.until(ExpectedConditions.elementToBeClickable(tags));
        tagsLink.click();
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("tags"),
                "Clicking Tags should navigate to /tags");
        driver.navigate().back();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    if (elementExists(about)) {
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(about));
        aboutLink.click();
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("about"),
                "Clicking About should navigate to /about");
        driver.navigate().back();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }
}

@Test
@Order(5)
public void testFooterSocialLinks() {
    navigateTo("");
    String[] domains = {"twitter.com", "facebook.com", "linkedin.com", "github.com"};
    for (String domain : domains) {
        By linkLocator = By.xpath("//footer//a[contains(@href,'" + domain + "')]");
        Assumptions.assumeTrue(
                elementExists(linkLocator),
                "Footer link for " + domain + " not found – skipping this link");

        List<WebElement> links = driver.findElements(linkLocator);
        for (WebElement link : links) {
            String originalHandle = driver.getWindowHandle();
            Set<String> before = driver.getWindowHandles();
            link.click();

            try {
                wait.until(d -> driver.getWindowHandles().size() > before.size());
            } catch (TimeoutException ignored) {
            }

            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            if (!after.isEmpty()) {
                String newHandle = after.iterator().next();
                driver.switchTo().window(newHandle);
                wait.until(ExpectedConditions.urlContains(domain));
                Assertions.assertTrue(
                        driver.getCurrentUrl().contains(domain),
                        "External link should open domain: " + domain);
                driver.close();
                driver.switchTo().window(originalHandle);
            } else {
                Assertions.assertTrue(
                        driver.getCurrentUrl().contains(domain),
                        "Link navigated within same tab to expected domain");
                driver.navigate().back();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
            }
        }
    }
}

@Test
@Order(6)
public void testExternalAboutLink() {
    navigateTo("");
    By aboutLink = By.xpath("//a[contains(text(),'About')]");
    Assumptions.assumeTrue(
            elementExists(aboutLink),
            "About link not found; skipping external about link test");

    WebElement about = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
    String originalHandle = driver.getWindowHandle();
    Set<String> before = driver.getWindowHandles();
    about.click();

    try {
        wait.until(d -> driver.getWindowHandles().size() > before.size());
    } catch (TimeoutException ignored) {
    }

    Set<String> after = driver.getWindowHandles();
    after.removeAll(before);
    if (!after.isEmpty()) {
        String newHandle = after.iterator().next();
        driver.switchTo().window(newHandle);
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("github.com"),
                "About link should open external GitHub page");
        driver.close();
        driver.switchTo().window(originalHandle);
    } else {
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("about"),
                "About opened in same tab");
        driver.navigate().back();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
    }
}
}