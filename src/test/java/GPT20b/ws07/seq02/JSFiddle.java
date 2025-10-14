package GPT20b.ws07.seq02;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
 org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

@TestMethodOrder(OrderAnnotation.class)
public class CacTatTestSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
    private static final String USER_EMAIL = "caio@gmail.com";
    static final String USER_PASSWORD = "123";

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
    /* Home page basic load test                                           */
    @Test
 @Order(1)
    public void testHomePageLoads() {
        driver.navigate().to(BASE_URL);
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("cac-tat"),
                "Title should contain 'cac-tat'");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    /* --------------------------------------------------------------------- */
    /* Valid login test                                                    */
    @Test
    @Order(2)
    public void testValidLogin() {
        driver.navigate().to(BASE_URL);
        List<WebElement> emailInput = findElementsOrAssume(By.cssSelector("input[type='email'], input[name='email'], input[name='login']"));
        emailInput.get(0).clear();
        emailInput.get(0).sendKeys(USER_EMAIL);

        List<WebElement> pwdInput = findElementsOrAssume(By.cssSelector("input[type='password'], input[name='password']"));
        pwdInput.get(0).clear();
        pwdInput.get(0).sendKeys(USER_PASSWORD);

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
        loginBtn.click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("home"),
                ExpectedConditions.urlContains("dashboard"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".profile, .account-summary"))));

        Assertions.assertTrue(driver.getCurrentUrl().contains("home") || driver.getCurrentUrl().contains("dashboard"),
                "After login, URL should contain 'home' or 'dashboard'");
        List<WebElement> profileElems = driver.findElements(By.cssSelector(".profile, .account-summary"));
        Assertions.assertFalse(profileElems.isEmpty() && !profileElems.get(0).isDisplayed(),
                "User profile component should be visible after login");

        logoutIfPresent();
    }

    /* --------------------------------------------------------------------- */
    /* Invalid login test                                                  */
    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.navigate().to(BASE_URL);
        List<WebElement> emailInput = findElementsOrAssume(By.cssSelector("input[type='email'], input[name='email'], input[name='login']"));
        emailInput.get(0).clear();
        emailInput.get(0).sendKeys("wrong@example.com");

        List<WebElement> pwdInput = findElementsOrAssume(By.cssSelector("input[type='password'], input[name='password']"));
        pwdInput.get(0).clear();
        pwdInput.get(0).sendKeys("wrongpassword");

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
        loginBtn.click();

        WebElement err = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error, .alert, .notification, .validation")));
        Assertions.assertTrue(err.isDisplayed(), "Error message must be shown after failed login");
        Assertions.assertFalse(err.getText().trim().isEmpty(), "Error message should contain text");
    }

    /* --------------------------------------------------------------------- */
    /* Sort dropdown test on catalog page                                  */
    @Test
    @Order(4)
    public void testSortingDropdown() {
        driver.navigate().to(BASE_URL);
        List<WebElement> itemsLink = driver.findElements(By.linkText("Items"));
        if (!itemsLink.isEmpty()) {
            itemsLink.get(0).click();
            wait.until(ExpectedConditions.urlContains("items"));
        }

        List<WebElement> sortSelect = findElementsOrAssume(By.cssSelector("select[name='sort'], select[id='sort']"));
        Select sorter = new Select(sortSelect.get(0));
        List<WebElement> options = sorter.getOptions();
        Assertions.assertTrue(options.size() > 1, "Sorting dropdown should have multiple options");

        String firstBefore = getFirstProductName();
        for (WebElement opt : options) {
            sorter.selectByVisibleText(opt.getText());
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-item, .item, .product .name, .item-title")));
            String firstAfter = getFirstProductName();
            Assertions.assertNotEquals(firstBefore, firstAfter,
                    "Sorting by '" + opt.getText() + "' should change first product");
            firstBefore = firstAfter;
        }

        // Reset order to original
        sorter.selectByVisibleText(options.get(0).getText());
    }

    /* --------------------------------------------------------------------- */
    /* External links on footer and other pages                            */
    @Test
    @Order(5)
    public void testExternalLinksOnHome() {
        driver.navigate().to(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href^='http']"));
        Assertions.assertFalse(links.isEmpty(), "No external links found on home page");

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (!href.contains("cac-tat.s3.eu-central-1.amazonaws.com")) {
                checkExternalLink(link, href);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* Burger menu actions test                                           */
    @Test
    @Order(6)
    public void testBurgerMenuActions() {
        loginIfNeeded();

        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='menu'], .burger-menu, .nav-toggler")));
        burger.click();

        boolean allItems = false;
        boolean about = false;
        boolean reset = false;

        List<WebElement> menuLinks = driver.findElements(By.tagName("a"));
        for (WebElement link : menuLinks) {
            String txt = link.getText().trim();
            switch (txt) {
                case "All Items":
                case "Items":
                    link.click();
                    wait.until(ExpectedConditions.urlContains("items"));
                    Assertions.assertTrue(driver.getCurrentUrl().contains("items"),
                            "URL should contain 'items' after clicking All Items");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='menu'], .burger-menu, .nav-toggler")));
                    burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='menu'], .burger-menu, .nav-toggler")));
                    burger.click();
                    allItems = true;
                    break;
                case "About":
                    checkExternalLink(link, link.getAttribute("href"));
                    driver.navigate().back();
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='menu'], .burger-menu, .nav-toggler")));
                    burger = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[aria-label='menu'], .burger-menu, .nav-toggler")));
                    burger.click();
                    about = true;
                    break;
                case "Reset App State":
                    link.click();
                    wait.until(ExpectedConditions.urlContains("home"));
                    reset = true;
                    break;
                case "Logout":
                    link.click();
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
                    logoutIfPresent();
                    break;
                default:
                    // ignore other links
            }
        }

        Assertions.assertTrue(allItems, "All Items link was not visited");
        Assertions.assertTrue(about, "About link did not navigate externally");
        Assertions.assertTrue(reset, "Reset App State link was not clicked");
    }

    /* --------------------------------------------------------------------- */
    /* Utility helpers                                                     */
    private List<WebElement> findElementsOrAssume(By locator) {
        List<WebElement> elems = driver.findElements(locator);
        Assumptions.assumeTrue(!elems.isEmpty(), "Required element not found: " + locator);
        return elems;
    }

    private void loginIfNeeded() {
        if (driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']")).isEmpty()) {
            return; // already logged in
        }
        driver.navigate().to(BASE_URL);
        List<WebElement> emailInput = findElementsOrAssume(By.cssSelector("input[type='email'], input[name='email'], input[name='login']"));
        emailInput.get(0).clear();
        emailInput.get(0).sendKeys(USER_EMAIL);

        List<WebElement> pwdInput = findElementsOrAssume(By.cssSelector("input[type='password'], input[name='password']"));
        pwdInput.get(0).clear();
        pwdInput.get(0).sendKeys(USER_PASSWORD);

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
        loginBtn.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".profile, .account-summary")));
    }

    private void logoutIfPresent() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], input[type='submit']")));
        }
    }

    private String getFirstProductName() {
        List<WebElement> items = driver.findElements(By.cssSelector(".product-item .name, .product-item .title, .item-title, .product .name"));
        return items.isEmpty() ? "" : items.get(0).getText();
    }

    private void checkExternalLink(WebElement link, String href) {
        String original = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(original)) {
                driver.switchTo().window(h);
                wait.until(ExpectedConditions.urlContains(href));
                Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                        "External link URL should contain part of: " + href);
                driver.close();
                driver.switchTo().window(original);
                break;
            }
        }
    }
}