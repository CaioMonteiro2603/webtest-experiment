package SunaGPT20b.ws10.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";


@BeforeAll
public static void setUpAll() {
    FirefoxOptions options = new FirefoxOptions();
    options.addArguments("--headless");
    driver = new FirefoxDriver(options);
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
}

@AfterAll
public static void tearDownAll() {
    if (driver != null) {
        driver.quit();
    }
}

/** Helper to navigate to the login page */
private void goToLogin() {
    driver.get(BASE_URL);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
}

/** Helper to perform login */
private void login(String user, String pass) {
    goToLogin();

    WebElement emailInput = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("email")));
    emailInput.clear();
    emailInput.sendKeys(user);

    WebElement passwordInput = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("password")));
    passwordInput.clear();
    passwordInput.sendKeys(pass);

    WebElement loginBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
    loginBtn.click();
}

/** Helper to ensure we are logged in before a test */
private void ensureLoggedIn() {
    login(USERNAME, PASSWORD);
    // Verify dashboard appears (example: presence of a menu button)
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[data-test='menu-button']")));
}

@Test
@Order(1)
public void testValidLogin() {
    login(USERNAME, PASSWORD);
    // Successful login should redirect to dashboard (URL without /login)
    wait.until(ExpectedConditions.urlMatches("^(?!.*\\/login).*$"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard") ||
                    driver.getCurrentUrl().contains("/home"),
            "After login the URL should indicate the dashboard/home page");
    // Verify a known element on the dashboard
    WebElement menuBtn = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[data-test='menu-button']")));
    Assertions.assertTrue(menuBtn.isDisplayed(), "Menu button should be visible after successful login");
}

@Test
@Order(2)
public void testInvalidLogin() {
    login(USERNAME, "wrongPassword");
    // Expect an error alert/message
    WebElement errorMsg = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger, .error-message")));
    Assertions.assertTrue(errorMsg.isDisplayed(),
            "Error message should be displayed for invalid credentials");
}

@Test
@Order(3)
public void testMenuBurgerActions() {
    ensureLoggedIn();

    // Open burger menu
    WebElement burger = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='menu-button']")));
    burger.click();

    // Click All Items (assumed to be a link with text "All Items")
    List<WebElement> allItemsLinks = driver.findElements(By.linkText("All Items"));
    if (!allItemsLinks.isEmpty()) {
        allItemsLinks.get(0).click();
        wait.until(ExpectedConditions.urlContains("/items"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/items"),
                "URL should contain /items after clicking All Items");
    }

    // Click About (external link)
    List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
    if (!aboutLinks.isEmpty()) {
        String originalWindow = driver.getWindowHandle();
        aboutLinks.get(0).click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("about") ||
                        driver.getCurrentUrl().contains("github") ||
                        driver.getCurrentUrl().contains("linkedin"),
                "External About link should open a new tab with an external domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    // Click Reset App State (assumed to have data-test attribute)
    List<WebElement> resetLinks = driver.findElements(By.cssSelector("[data-test='reset-app']"));
    if (!resetLinks.isEmpty()) {
        resetLinks.get(0).click();
        // Verify a toast or message appears confirming reset
        WebElement toast = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".toast, .alert-success")));
        Assertions.assertTrue(toast.isDisplayed(),
                "Reset confirmation should be displayed after clicking Reset App State");
    }

    // Click Logout
    List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
    if (!logoutLinks.isEmpty()) {
        logoutLinks.get(0).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "User should be redirected to login page after logout");
    }
}

@Test
@Order(4)
public void testSortingDropdown() {
    ensureLoggedIn();

    // Locate sorting dropdown (assumed to have id 'sort')
    WebElement sortSelect = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("sort")));
    List<WebElement> options = sortSelect.findElements(By.tagName("option"));
    Assertions.assertFalse(options.isEmpty(), "Sorting dropdown should have options");

    String previousFirstItem = "";
    for (WebElement option : options) {
        option.click();
        // Wait for list to be refreshed (example: first item changes)
        WebElement firstItem = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".item-list .item:first-child .item-name")));
        String currentFirstItem = firstItem.getText();
        if (!previousFirstItem.isEmpty()) {
            Assertions.assertNotEquals(previousFirstItem, currentFirstItem,
                    "First item should change after selecting a different sort option");
        }
        previousFirstItem = currentFirstItem;
    }
}

@Test
@Order(5)
public void testFooterSocialLinks() {
    ensureLoggedIn();

    // Footer social links (Twitter, Facebook, LinkedIn)
    String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
    for (String domain : domains) {
        List<WebElement> links = driver.findElements(By.cssSelector("footer a[href*='" + domain + "']"));
        if (links.isEmpty()) {
            continue; // skip if not present
        }
        WebElement link = links.get(0);
        String originalWindow = driver.getWindowHandle();
        link.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                "External social link should navigate to a URL containing " + domain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}

@Test
@Order(6)
public void testOneLevelInternalLinks() {
    ensureLoggedIn();

    // Collect all anchor hrefs on the current page
    List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
    for (WebElement anchor : anchors) {
        String href = anchor.getAttribute("href");
        if (href == null || !href.startsWith("https://gestao.brasilagritest.com")) {
            continue; // external or malformed
        }
        // Determine path depth (one level below base)
        String path = href.replaceFirst("https://gestao.brasilagritest.com", "");
        if (path.isEmpty() || path.equals("/") || path.split("/").length > 2) {
            continue; // skip deeper links
        }
        
        anchor.click();

        // Wait for navigation to the target URL
        wait.until(ExpectedConditions.urlToBe(href));

        // Verify page body is present
        WebElement body = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(body.isDisplayed(),
                "Body should be displayed on internal page: " + href);

        // Return to dashboard
        driver.navigate().back();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }
}
}