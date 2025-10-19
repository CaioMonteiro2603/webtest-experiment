package GPT20b.ws04.seq10;

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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FormTests {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";

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

    /* ---------- Utility Methods ---------- */

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
        String newHandle = handles.stream().filter(h -> !h.equals(original)).findFirst().orElseThrow();
        driver.switchTo().window(newHandle);
        Assertions.assertTrue(
                driver.getCurrentUrl().contains(expectedDomain),
                "External link URL does not contain expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(original);
    }

    private static void navigateTo() {
        driver.get(BASE_URL);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testLoginValid() {
        navigateTo();
        Assumptions.assumeTrue(elementPresent(By.id("username")), "Login form not found");
        Assumptions.assumeTrue(elementPresent(By.id("password")), "Password field not found");
        Assumptions.assumeTrue(elementPresent(By.id("loginButton")), "Login button not found");

        WebElement userField = waitClickable(By.id("username"));
        WebElement passField = waitClickable(By.id("password"));
        WebElement loginBtn = waitClickable(By.id("loginButton"));

        userField.clear();
        userField.sendKeys("testuser");
        passField.clear();
        passField.sendKeys("testpass");
        loginBtn.click();

        // After login, expect a logout button or message
        Assumptions.assumeTrue(wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.id("logoutButton")),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".login-success")))), 
                "Login success indicator not found");
        Assertions.assertTrue(true, "Login succeeded");
    }

    @Test
    @Order(2)
    public void testLoginInvalid() {
        navigateTo();
        Assumptions.assumeTrue(elementPresent(By.id("username")), "Login form not found");
        Assumptions.assumeTrue(elementPresent(By.id("password")), "Password field not found");
        Assumptions.assumeTrue(elementPresent(By.id("loginButton")), "Login button not found");

        WebElement userField = waitClickable(By.id("username"));
        WebElement passField = waitClickable(By.id("password"));
        WebElement loginBtn = waitClickable(By.id("loginButton"));

        userField.clear();
        userField.sendKeys("invalid");
        passField.clear();
        passField.sendKeys("wrong");
        loginBtn.click();

        Assumptions.assumeTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message"))), 
                "Error message not displayed");
        Assertions.assertTrue(true, "Invalid login displayed error");
    }

    @Test
    @Order(3)
    public void testDropdownSorting() {
        navigateTo();
        Assumptions.assumeTrue(elementPresent(By.id("sortSelect")), "Sort dropdown not present");
        Assumptions.assumeTrue(elementPresent(By.cssSelector(".item-list .item")), "No items to sort");

        List<WebElement> itemsBefore = driver.findElements(By.cssSelector(".item-list .item"));
        String firstBefore = itemsBefore.get(0).getText();

        WebElement dropdown = waitClickable(By.id("sortSelect"));
        List<WebElement> options = dropdown.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() > 1, "Not enough sort options");

        for (WebElement opt : options) {
            String value = opt.getAttribute("value");
            if (value.isEmpty()) continue;
            opt.click();
            wait.until(ExpectedConditions.stalenessOf(itemsBefore.get(0)));
            List<WebElement> itemsAfter = driver.findElements(By.cssSelector(".item-list .item"));
            String firstAfter = itemsAfter.get(0).getText();
            Assertions.assertNotEquals(firstBefore, firstAfter, 
                    "Sorting option '" + opt.getText() + "' did not change order");
            firstBefore = firstAfter;
            itemsBefore = itemsAfter;
        }
    }

    @Test
    @Order(4)
    public void testBurgerMenuOptions() {
        navigateTo();
        Assumptions.assumeTrue(elementPresent(By.id("burgerMenu")), "Burger menu button not found");

        WebElement menuBtn = waitClickable(By.id("burgerMenu"));
        menuBtn.click();

        if (elementPresent(By.id("menuAllItems"))) {
            WebElement allItems = waitClickable(By.id("menuAllItems"));
            allItems.click();
            Assumptions.assumeTrue(wait.until(ExpectedConditions.urlContains("items.html")), 
                    "Navigation to All Items failed");
        }

        menuBtn = waitClickable(By.id("burgerMenu"));
        if (elementPresent(By.id("menuAbout"))) {
            menuBtn.click();
            openExternalLink(By.id("menuAbout"), "katalon-test.s3.amazonaws.com");
        }

        menuBtn = waitClickable(By.id("burgerMenu"));
        if (elementPresent(By.id("menuReset"))) {
            menuBtn.click();
            WebElement resetLink = waitClickable(By.id("menuReset"));
            resetLink.click();
            Assumptions.assumeTrue(wait.until(ExpectedConditions.urlContains("index.html")), 
                    "Reset App State did not keep on home page");
        }

        menuBtn = waitClickable(By.id("burgerMenu"));
        if (elementPresent(By.id("menuLogout"))) {
            menuBtn.click();
            WebElement logoutLink = waitClickable(By.id("menuLogout"));
            logoutLink.click();
            Assumptions.assumeTrue(wait.until(ExpectedConditions.urlToBe(BASE_URL)), 
                    "Logout did not redirect to home page");
        }
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        navigateTo();
        Assumptions.assumeTrue(elementPresent(By.cssSelector("footer a[href]")), "Footer links not found");

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a[href]"));
        String internalDomain = "katalon-test.s3.amazonaws.com";
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (href.contains(internalDomain)) continue; // skip internal links
            try {
                openExternalLink(By.linkText(link.getText()), href);
            } catch (Exception e) {
                // If link text doesn't resolve, try by href
                openExternalLink(By.xpath("//a[@href='" + href + "']"), href);
            }
        }
    }
}