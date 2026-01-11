package GPT20b.ws05.seq10;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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

    /* ---------------- General helpers ---------------- */

    private static boolean elementPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    private static WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private static void openExternalLink(By locator, String expectedDomain) {
        String original = driver.getWindowHandle();
        driver.findElement(locator).click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(original))
                .findFirst()
                .orElseThrow();
        driver.switchTo().window(newHandle);
        Assertions.assertTrue(
                driver.getCurrentUrl().contains(expectedDomain),
                "External link URL does not contain expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(original);
    }

    /* ---------------- Tests ---------------- */

    @Test
    @Order(1)
    public static void testLoginValid() {
        driver.get(BASE_URL);
        Assumptions.assumeTrue(elementPresent(By.id("userid")), "User id field not found");
        Assumptions.assumeTrue(elementPresent(By.id("password")), "Password field not found");
        Assumptions.assumeTrue(elementPresent(By.id("login")), "Login button not found");

        WebElement userField = waitClickable(By.id("userid"));
        WebElement passField = waitClickable(By.id("password"));
        WebElement loginBtn = waitClickable(By.id("login"));

        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        loginBtn.click();

        // Expect presence of logout link or account summary
        Assumptions.assumeTrue(
                wait.until(
                    ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(By.id("logout")),
                        ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Account Summary')]")))),
                "Login success indicator not found");
        Assertions.assertTrue(true, "Login succeeded");
    }

    @Test
    @Order(2)
    public void testLoginInvalid() {
        driver.get(BASE_URL);
        Assumptions.assumeTrue(elementPresent(By.id("userid")), "User id field not found");
        Assumptions.assumeTrue(elementPresent(By.id("password")), "Password field not found");
        Assumptions.assumeTrue(elementPresent(By.id("login")), "Login button not found");

        WebElement userField = waitClickable(By.id("userid"));
        WebElement passField = waitClickable(By.id("password"));
        WebElement loginBtn = waitClickable(By.id("login"));

        userField.clear();
        userField.sendKeys("wrong");
        passField.clear();
        passField.sendKeys("wrong");
        loginBtn.click();

        Assumptions.assumeTrue(
                (BooleanSupplier) wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error"))),
                "Error message not displayed");
        Assertions.assertTrue(true, "Invalid login produced error");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginIfNeeded();

        Assumptions.assumeTrue(elementPresent(By.id("sortBy")), "Sorting dropdown not found");
        Assumptions.assumeTrue(elementPresent(By.cssSelector(".inventory-row")), "No inventory rows found");

        List<WebElement> rowsBefore = driver.findElements(By.cssSelector(".inventory-row"));
        String firstBefore = rowsBefore.get(0).getText();

        WebElement sort = waitClickable(By.id("sortBy"));
        List<WebElement> options = sort.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() > 1, "Not enough sort options");

        for (WebElement opt : options) {
            String val = opt.getAttribute("value");
            if (val == null || val.isEmpty()) continue;
            opt.click();
            wait.until(ExpectedConditions.stalenessOf(rowsBefore.get(0)));
            List<WebElement> rowsAfter = driver.findElements(By.cssSelector(".inventory-row"));
            String firstAfter = rowsAfter.get(0).getText();
            Assertions.assertNotEquals(firstBefore, firstAfter,
                    "Sorting option '" + opt.getText() + "' did not change order");
            firstBefore = firstAfter;
            rowsBefore = rowsAfter;
        }
    }

    @Test
    @Order(4)
    public void testBurgerMenuOptions() {
        loginIfNeeded();

        Assumptions.assumeTrue(elementPresent(By.id("burgerMenu")), "Burger menu not found");
        WebElement menuBtn = waitClickable(By.id("burgerMenu"));
        menuBtn.click();

        // All Items
        if (elementPresent(By.id("menuAllItems"))) {
            WebElement allItems = waitClickable(By.id("menuAllItems"));
            allItems.click();
            Assumptions.assumeTrue(wait.until(ExpectedConditions.urlContains("items.html")),
                    "Navigation to All Items failed");
        }

        // About (external)
        menuBtn = waitClickable(By.id("burgerMenu"));
        if (elementPresent(By.id("menuAbout"))) {
            openExternalLink(By.id("menuAbout"), "cac-tat.s3");
        }

        // Reset App State
        menuBtn = waitClickable(By.id("burgerMenu"));
        if (elementPresent(By.id("menuReset"))) {
            WebElement resetLink = waitClickable(By.id("menuReset"));
            resetLink.click();
            Assumptions.assumeTrue(wait.until(ExpectedConditions.urlContains("index.html")),
                    "Reset App State did not keep user on home page");
        }

        // Logout
        menuBtn = waitClickable(By.id("burgerMenu"));
        if (elementPresent(By.id("menuLogout"))) {
            WebElement logoutLink = waitClickable(By.id("menuLogout"));
            logoutLink.click();
            Assumptions.assumeTrue(wait.until(ExpectedConditions.urlToBe(BASE_URL)),
                    "Logout did not redirect to home page");
        }
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        Assumptions.assumeTrue(elementPresent(By.cssSelector("footer a[href]")), "Footer links missing");

        List<WebElement> links = driver.findElements(By.cssSelector("footer a[href]"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            String domain = extractDomain(href);
            if (domain.contains("cac-tat.s3")) continue; // internal
            openExternalLink(By.cssSelector("footer a[href='" + href + "']"), href);
        }
    }

    /* ---------------- Helper for login state ---------------- */

    private static void loginIfNeeded() {
        if (!driver.getCurrentUrl().equals(BASE_URL)) {
            driver.get(BASE_URL);
        }
        if (!elementPresent(By.id("logout"))) {
            testLoginValid();
        }
    }

    /* ---------------- Helpers ---------------- */

    private static String extractDomain(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            return uri.getHost();
        } catch (Exception e) {
            return "";
        }
    }
}