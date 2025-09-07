package GPT20b.ws10.seq01;

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
public class BrasilagriTest {

    private static final String BASE_URL = "https://beta.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    private static WebDriver driver;
    private static WebDriverWait wait;

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

    /* ------------------------------------------------------------------- */
    /*  Helper methods                                                    */
    /* ------------------------------------------------------------------- */

    private void goTo(String url) {
        driver.get(url);
    }

    private void login() {
        goTo(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))
                .orElse(By.id("username"))
                .orElse(By.cssSelector("input[type='email']")));
        WebElement userField = driver.findElement(
                By.name("username").or(() -> By.id("username"))
                        .or(() -> By.cssSelector("input[type='email']")));
        userField.clear();
        userField.sendKeys(USERNAME);

        WebElement passField = driver.findElement(
                By.name("password").or(() -> By.id("password"))
                        .or(() -> By.cssSelector("input[type='password']")));
        passField.clear();
        passField.sendKeys(PASSWORD);

        WebElement loginBtn = driver.findElement(
                By.cssSelector("button[type='submit']").or(() -> By.id("login"))
                        .or(() -> By.xpath("//button[normalize-space()='Entrar']")));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();
    }

    private void waitForVisibility(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void waitForClickability(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /* ------------------------------------------------------------------- */
    /*  Tests                                                              */
    /* ------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testLoginPageLoads() {
        goTo(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(
                driver.getTitle().toLowerCase().contains("login"),
                "Login page title should contain 'login'.");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        login();
        wait.until(ExpectedConditions.urlMatches(".*/$"));
        Assertions.assertTrue(
                driver.getCurrentUrl().matches(".*/$"),
                "After login, URL should end with '/'.");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        goTo(BASE_URL);
        WebElement userField = driver.findElement(
                By.name("username").or(() -> By.id("username"))
                        .or(() -> By.cssSelector("input[type='email']")));
        userField.clear();
        userField.sendKeys("invalid@example.com");

        WebElement passField = driver.findElement(
                By.name("password").or(() -> By.id("password"))
                        .or(() -> By.cssSelector("input[type='password']")));
        passField.clear();
        passField.sendKeys("wrongpassword");

        WebElement loginBtn = driver.findElement(
                By.cssSelector("button[type='submit']").or(() -> By.id("login"))
                        .or(() -> By.xpath("//button[normalize-space()='Entrar']")));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        By errorMsg = By.xpath("//*[contains(text(),'incorrect') or contains(text(),'erro')]");
        waitForVisibility(errorMsg);
        Assertions.assertTrue(
                driver.findElement(errorMsg).isDisplayed(),
                "Error message should be displayed for invalid credentials.");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        login();
        // Assume sorting dropdown has id 'sort' or name 'order'
        List<WebElement> sortElems = driver.findElements(
                By.id("sort").or(() -> By.name("order"))
                        .or(() -> By.cssSelector("select[data-test='sort']")));
        if (!sortElems.isEmpty()) {
            WebElement sortDropdown = sortElems.get(0);
            waitForClickability(By.id(sortDropdown.getAttribute("id")));
            WebElement firstItem = driver.findElement(
                    By.cssSelector(".products .product:first-child .name"));
            String originalFirst = firstItem.getText();

            List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
            Assertions.assertFalse(options.isEmpty(), "Sorting dropdown should have options.");

            for (WebElement opt : options) {
                String val = opt.getAttribute("value");
                if (!val.isEmpty()) {
                    opt.click();
                    // Simple wait for list re-render
                    wait.until(ExpectedConditions.textToBePresentInElementLocated(
                            By.cssSelector(".products .product:first-child .name"),
                            originalFirst.equals(firstItem.getText()) ? opt.getText() : originalFirst));
                    WebElement newFirst = driver.findElement(
                            By.cssSelector(".products .product:first-child .name"));
                    Assertions.assertNotEquals(
                            originalFirst,
                            newFirst.getText(),
                            "Order should change after selecting option '" + opt.getText() + "'.");
                    originalFirst = newFirst.getText();
                }
            }
        }
    }

    @Test
    @Order(5)
    public void testBurgerMenuActions() {
        login();
        // Assuming hamburger menu button id 'menu-toggle'
        By burger = By.id("menu-toggle").or(() -> By.className("hamburger"))
                .or(() -> By.cssSelector("[data-bs-toggle='offcanvas']"));
        waitForClickable(burger);
        driver.findElement(burger).click();

        // All Items
        By allItems = By.xpath("//a[normalize-space()='All Items' or contains(@href,'Items')]");
        if (!driver.findElements(allItems).isEmpty()) {
            waitForClickable(allItems);
            driver.findElement(allItems).click();
            wait.until(ExpectedConditions.urlMatches(".*/items$"));
            Assertions.assertTrue(
                    driver.getCurrentUrl().matches(".*/items$"),
                    "'All Items' should navigate to '/items' page.");
        }

        // Reopen menu
        driver.findElement(burger).click();

        // About (external)
        By aboutLink = By.xpath("//a[normalize-space()='About'] | //a[contains(@href,'about')] ");
        if (!driver.findElements(aboutLink).isEmpty()) {
            String originalUrl = driver.getCurrentUrl();
            driver.findElement(aboutLink).click();

            wait.until(driver1 -> {
                String url = driver1.getCurrentUrl();
                return !url.equals(originalUrl);
            });

            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("about"),
                    "About link should navigate to an external page containing 'about'.");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(originalUrl));
        }

        // Reopen menu
        driver.findElement(burger).click();

        // Reset App State
        By resetLink = By.xpath("//a[normalize-space()='Reset App State'] | //a[contains(@href,'reset')] ");
        if (!driver.findElements(resetLink).isEmpty()) {
            driver.findElement(resetLink).click();
            wait.until(ExpectedConditions.urlMatches(".*/$"));
            Assertions.assertTrue(
                    driver.getCurrentUrl().matches(".*/$"),
                    "'Reset App State' should return to home page.");
        }

        // Reopen menu
        driver.findElement(burger).click();

        // Logout
        By logoutLink = By.xpath("//a[normalize-space()='Logout' or contains(@href,'logout')]");
        if (!driver.findElements(logoutLink).isEmpty()) {
            driver.findElement(logoutLink).click();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
            Assertions.assertTrue(
                    driver.getCurrentUrl().equals(BASE_URL),
                    "'Logout' should redirect back to login page.");
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        goTo(BASE_URL);
        String originalHandle = driver.getWindowHandle();
        List<String> domains = List.of("twitter.com", "facebook.com", "linkedin.com");
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
}