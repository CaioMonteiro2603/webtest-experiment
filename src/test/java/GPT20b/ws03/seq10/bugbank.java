package GPT20b.ws03.seq10;

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
public class BugbankTests {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------------- Utility Methods ---------------- */

    private static void login() {
        WebElement emailField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login"));

        emailField.clear();
        emailField.sendKeys(USERNAME);
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/dashboard.html"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("dashboard"),
                "Login did not navigate to dashboard");
    }

    private static void logout() {
        WebElement logoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("logout")));
        logoutBtn.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(
                BASE_URL,
                driver.getCurrentUrl(),
                "Logout did not return to home page");
    }

    private static boolean isLoggedIn() {
        return driver.findElements(By.id("dashboard")).size() > 0;
    }

    private static void ensureLoggedIn() {
        if (!isLoggedIn()) {
            login();
        }
    }

    private static void openExternalLink(By locator, String expectedDomain) {
        String originalHandle = driver.getWindowHandle();
        driver.findElement(locator).click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        String newHandle = handles.stream()
                .filter(h -> !h.equals(originalHandle))
                .findFirst()
                .orElseThrow();
        driver.switchTo().window(newHandle);

        Assertions.assertTrue(
                driver.getCurrentUrl().contains(expectedDomain),
                "External link URL does not contain expected domain: " + expectedDomain);

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    /* ---------------- Tests ---------------- */

    @Test
    @Order(1)
    public void testLoginValid() {
        driver.get(BASE_URL);
        login();
        Assertions.assertTrue(isLoggedIn(), "User should be logged in after valid credentials");
        logout();
    }

    @Test
    @Order(2)
    public void testLoginInvalid() {
        driver.get(BASE_URL);
        WebElement emailField = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("email")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login"));

        emailField.clear();
        emailField.sendKeys("wrong@user.com");
        passwordField.clear();
        passwordField.sendKeys("wrongpass");
        loginBtn.click();

        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(
                error.getText().toLowerCase().contains("incorrect"),
                "Login error message not displayed for invalid credentials");
        Assertions.assertEquals(
                BASE_URL,
                driver.getCurrentUrl(),
                "URL should remain on home page after failed login");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        ensureLoggedIn();

        By sortByLocator = By.id("sortBy");
        List<WebElement> sortElements = driver.findElements(sortByLocator);
        Assumptions.assumeTrue(!sortElements.isEmpty(), "Sorting dropdown missing, skipping test.");

        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(sortByLocator));
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() > 1, "Sorting dropdown has insufficient options.");

        // Capture initial order
        List<WebElement> rowsBefore = driver.findElements(By.cssSelector(".product-item"));
        Assertions.assertFalse(rowsBefore.isEmpty(), "No product items found before sorting");
        String firstPrior = rowsBefore.get(0).findElement(By.cssSelector(".product-title")).getText();

        for (int i = 0; i < options.size(); i++) {
            sortDropdown.click();
            options.get(i).click();

            wait.until(ExpectedConditions.stalenessOf(rowsBefore.get(0)));

            List<WebElement> rowsAfter = driver.findElements(By.cssSelector(".product-item"));
            String firstAfter = rowsAfter.get(0).findElement(By.cssSelector(".product-title")).getText();

            Assertions.assertNotEquals(
                    firstPrior,
                    firstAfter,
                    "Sorting option '" + options.get(i).getText() + "' did not change product order");
            firstPrior = firstAfter;
            rowsBefore = rowsAfter;
        }

        logout();
    }

    @Test
    @Order(4)
    public void testMenuOptions() {
        ensureLoggedIn();

        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("menuBtn")));
        menuBtn.click();

        // All Items
        WebElement allItems = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("menuAllItems")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("products.html"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("products"),
                "Did not navigate to products page");

        // About (external)
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("menuBtn")));
        menuBtn.click();
        openExternalLink(By.id("menuAbout"), "bugbank.netlify.app");

        // Reset App State
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("menuBtn")));
        menuBtn.click();
        WebElement reset = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("menuReset")));
        reset.click();
        wait.until(ExpectedConditions.urlContains("dashboard.html"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("dashboard"),
                "Reset App State should keep user on dashboard");

        // Logout
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("menuBtn")));
        menuBtn.click();
        WebElement logout = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("menuLogout")));
        logout.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(
                BASE_URL,
                driver.getCurrentUrl(),
                "Logout did not redirect to home page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        ensureLoggedIn();

        List<WebElement> links = driver.findElements(By.cssSelector("footer a[href]"));
        Assumptions.assumeTrue(!links.isEmpty(), "No footer links found, skipping test.");

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;
            if (href.contains("bugbank.netlify.app")) continue; // internal link
            openExternalLink(By.linkText(link.getText()), href);
        }

        logout();
    }
}