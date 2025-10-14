package GPT20b.ws05.seq02;

import java.time.Duration;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assumptions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class CacTatTestSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

    @BeforeAll
    public static void initDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void closeDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* --------------------------------------------------------------------- */
    /* basic page load test                                                */
    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.navigate().to(BASE_URL);
        assertTrue(driver.getTitle().toLowerCase().contains("cac-tat"),
                "Title should contain 'cac-tat'");
        byId("main", "Main section should be present");
    }

    /* --------------------------------------------------------------------- */
    /* positive login test                                                 */
    @Test
    @Order(2)
    public void testValidLogin() {
        driver.navigate().to(BASE_URL);
        List<WebElement> emailInput = driver.findElements(By.name("email"));
        Assumptions.assumeTrue(!emailInput.isEmpty(), "Email input not found");

        emailInput.get(0).clear();
        emailInput.get(0).sendKeys(USER_EMAIL);

        List<WebElement> pwdInput = driver.findElements(By.name("password"));
        Assumptions.assumeTrue(!pwdInput.isEmpty(), "Password input not found");

        pwdInput.get(0).clear();
        pwdInput.get(0).sendKeys(USER_PASSWORD);

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        loginBtn.click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("dashboard"),
                ExpectedConditions.urlContains("home"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".profile"), By.cssSelector(".account-summary"))
        ));

        assertTrue(driver.getCurrentUrl().contains("dashboard") || driver.getCurrentUrl().contains("home") || driver.getCurrentUrl().contains("account"),
                "After login, URL should point to dashboard/home/account");
        List<WebElement> profileElems = driver.findElements(By.cssSelector(".profile, .account-summary"));
        assertTrue(!profileElems.isEmpty() && profileElems.get(0).isDisplayed(),
                "User profile component should be visible after login");

        logoutIfPresent(); // return to known state
    }

    /* --------------------------------------------------------------------- */
    /* negative login test                                                  */
    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.navigate().to(BASE_URL);
        List<WebElement> emailInput = driver.findElements(By.name("email"));
        Assumptions.assumeTrue(!emailInput.isEmpty(), "Email input not found");

        emailInput.get(0).clear();
        emailInput.get(0).sendKeys("wrong@example.com");

        List<WebElement> pwdInput = driver.findElements(By.name("password"));
        Assumptions.assumeTrue(!pwdInput.isEmpty(), "Password input not found");

        pwdInput.get(0).clear();
        pwdInput.get(0).sendKeys("wrongpassword");

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        loginBtn.click();

        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error, .alert, .validation-error")));
        assertTrue(err.isDisplayed(), "An error message must be shown after failed login");
        assertFalse(err.getText().trim().isEmpty(), "Error message should contain text");
    }

    /* --------------------------------------------------------------------- */
    /* sorting dropdown test (if present)                                  */
    @Test
    @Order(4)
    public void testSortingDropdown() {
        // Ensure we are on a product listing page
        driver.navigate().to(BASE_URL);
        List<WebElement> prodLink = driver.findElements(By.linkText("Products"));
        if (!prodLink.isEmpty()) {
            prodLink.get(0).click();
            wait.until(ExpectedConditions.urlContains("products"));
        }

        List<WebElement> sortElements = driver.findElements(By.cssSelector("select[name='sort'], select[id='sort']"));
        Assumptions.assumeTrue(!sortElements.isEmpty(), "Sorting dropdown not found");

        Select sorter = new Select(sortElements.get(0));
        List<WebElement> options = sorter.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Less than two sorting options");

        String firstBefore = getFirstProductName();
        for (WebElement opt : options) {
            sorter.selectByVisibleText(opt.getText());
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-item")));
            String firstAfter = getFirstProductName();
            assertNotEquals(firstBefore, firstAfter, "Sorting by " + opt.getText() + " should change first product");
            firstBefore = firstAfter;
        }

        // Reset to original order
        sorter.selectByVisibleText(options.get(0).getText());
    }

    /* --------------------------------------------------------------------- */
    /* external link validation on home page                                   */
    @Test
    @Order(5)
    public void testExternalLinksOnHome() {
        driver.navigate().to(BASE_URL);
        List<WebElement> allLinks = driver.findElements(By.cssSelector("a[href^='http']"));
        Assumptions.assumeTrue(!allLinks.isEmpty(), "No external links found");

        for (WebElement link : allLinks) {
            String href = link.getAttribute("href");
            if (!href.contains("cac-tat.s3.eu-central-1.amazonaws.com")) {
                checkExternalLink(href, link);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* burger menu interactions                                              */
    @Test
    @Order(6)
    public void testBurgerMenuActions() {
        loginIfNeeded();

        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu'], .burger-menu, .nav-toggler")));
        burger.click();

        boolean allItemsVisited = false;
        boolean aboutUponExternal = false;
        boolean resetClicked = false;

        List<WebElement> menuLinks = driver.findElements(By.tagName("a"));
        for (WebElement link : menuLinks) {
            String txt = link.getText().trim();
            switch (txt) {
                case "All Items":
                case "Products":
                    link.click();
                    wait.until(ExpectedConditions.urlContains("products"));
                    assertTrue(driver.getCurrentUrl().contains("products"),
                            "URL should contain 'products' after All Items");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu'], .burger-menu, .nav-toggler")));
                    burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu'], .burger-menu, .nav-toggler")));
                    burger.click();
                    allItemsVisited = true;
                    break;
                case "About":
                    checkExternalLink(link.getAttribute("href"), link);
                    driver.navigate().back();
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu'], .burger-menu, .nav-toggler")));
                    burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='Menu'], .burger-menu, .nav-toggler")));
                    burger.click();
                    aboutUponExternal = true;
                    break;
                case "Reset App State":
                    link.click();
                    wait.until(ExpectedConditions.urlContains("dashboard"));
                    resetClicked = true;
                    break;
                case "Logout":
                    link.click();
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
                    logoutIfPresent();
                    break;
                default:
                    // ignore
            }
        }

        assertTrue(allItemsVisited, "All Items link was not visited");
        assertTrue(aboutUponExternal, "About link did not trigger external navigation");
        assertTrue(resetClicked, "Reset App State link was not clicked");
    }

    /* --------------------------------------------------------------------- */
    /* helper methods                                                      */
    private WebElement byId(String id, String message) {
        List<WebElement> elems = driver.findElements(By.id(id));
        assertFalse(elems.isEmpty(), message);
        return elems.get(0);
    }

    private void loginIfNeeded() {
        if (driver.findElements(By.cssSelector("button[type='submit']"))
                .isEmpty()) return; // already logged in
        driver.navigate().to(BASE_URL);
        List<WebElement> emailInput = driver.findElements(By.name("email"));
        if (emailInput.isEmpty()) return;
        emailInput.get(0).clear();
        emailInput.get(0).sendKeys(USER_EMAIL);
        List<WebElement> pwdInput = driver.findElements(By.name("password"));
        if (pwdInput.isEmpty()) return;
        pwdInput.get(0).clear();
        pwdInput.get(0).sendKeys(USER_PASSWORD);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        loginBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".profile, .account-summary")));
    }

    private void logoutIfPresent() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        }
    }

    private String getFirstProductName() {
        List<WebElement> items = driver.findElements(By.cssSelector(".product-item .product-name, .item-title"));
        return items.isEmpty() ? "" : items.get(0).getText();
    }

    private void checkExternalLink(String href, WebElement link) {
        String originalHandle = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(originalHandle)) {
                driver.switchTo().window(h);
                wait.until(ExpectedConditions.urlContains(href));
                assertTrue(driver.getCurrentUrl().contains(href),
                        "External link URL should contain part of: " + href);
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }
    }
}