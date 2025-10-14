package GPT20b.ws10.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

@TestMethodOrder(OrderAnnotation.class)
public class GestaoTestSuite {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USER_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    @BeforeAll
    public static void init() {
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

    /* --------------------------------------------------------------------- */
    /* 1. Base login page loads                                             */
    @Test
    @Order(1)
    public void testLoginPageLoads() {
        driver.navigate().to(BASE_URL);
        assertTrue(driver.getTitle().toLowerCase().contains("gestão")
                || driver.getTitle().toLowerCase().contains("login"),
                "Page title should contain 'Gestão' or 'Login'");

        WebElement emailField = findFirstElement(
                By.id("email"),
                By.name("email"),
                By.cssSelector("input[type='email']"));
        assertNotNull(emailField, "Email input field should be present");
        assertTrue(emailField.isDisplayed(), "Email input should be displayed");

        WebElement pwdField = findFirstElement(
                By.id("password"),
                By.name("password"),
                By.cssSelector("input[type='password']"));
        assertNotNull(pwdField, "Password input field should be present");
        assertTrue(pwdField.isDisplayed(), "Password input should be displayed");
    }

    /* --------------------------------------------------------------------- */
    /* 2. Valid login                                                        */
    @Test
    @Order(2)
    public void testValidLogin() {
        driver.navigate().to(BASE_URL);
        performLogin(USER_EMAIL, PASSWORD);

        wait.until(d -> d.getCurrentUrl().contains("/dashboard") ||
                d.getCurrentUrl().contains("/home") ||
                d.getCurrentUrl().contains("/profile"));

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("/dashboard") || url.contains("/home") || url.contains("/profile"),
                "After login, URL should contain '/dashboard', '/home', or '/profile'");

        List<WebElement> logoutLinks = driver.findElements(By.cssSelector("a[href*='logout']"));
        assertFalse(logoutLinks.isEmpty() && !logoutLinks.get(0).isDisplayed(),
                "Logout link should be visible after login");

        logoutIfPresent();
    }

    /* --------------------------------------------------------------------- */
    /* 3. Invalid login                                                      */
    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.navigate().to(BASE_URL);
        performLogin("wrong@user.com", "wrongpass");

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert.alert-danger, .alert-danger, .error-message")));
        assertTrue(errorMsg.isDisplayed(), "Error message should appear after failed login");
        assertFalse(errorMsg.getText().trim().isEmpty(), "Error message should contain text");
    }

    /* --------------------------------------------------------------------- */
    /* 4. Sorting dropdown on inventory page                                */
    @Test
    @Order(4)
    public void testSortingDropdown() {
        ensureLoggedIn();

        navigateToInventory();

        List<WebElement> sortSelectEls = driver.findElements(By.cssSelector(
                "select[name='sort'], select[id='sort'], select[aria-label='Sort']"));
        Assumptions.assumeTrue(!sortSelectEls.isEmpty(), "Sorting dropdown not found; skipping test");
        Select sorter = new Select(sortSelectEls.get(0));
        List<WebElement> options = sorter.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Sorting dropdown has fewer than two options; skipping test");

        String previousFirst = getFirstItemName();
        for (WebElement opt : options) {
            sorter.selectByVisibleText(opt.getText());
            wait.until(d -> d.getCurrentUrl().contains("/inventory") || d.getCurrentUrl().contains("/items"));
            String currentFirst = getFirstItemName();
            assertNotEquals(previousFirst, currentFirst,
                    "Sorting by '" + opt.getText() + "' should change the first item");
            previousFirst = currentFirst;
        }

        // Reset to original order
        sorter.selectByVisibleText(options.get(0).getText());

        logoutIfPresent();
    }

    /* --------------------------------------------------------------------- */
    /* 5. Burger menu interactions                                         */
    @Test
    @Order(5)
    public void testBurgerMenuActions() {
        ensureLoggedIn();

        openBurgerMenu();

        boolean allItemsVisited = false;
        boolean aboutVisited = false;
        boolean resetVisited = false;

        List<WebElement> menuLinks = driver.findElements(By.tagName("a"));
        for (WebElement link : menuLinks) {
            String txt = link.getText().trim();
            switch (txt) {
                case "All Items":
                case "Items":
                    link.click();
                    wait.until(d -> d.getCurrentUrl().contains("/items") || d.getCurrentUrl().contains("/inventory"));
                    assertTrue(driver.getCurrentUrl().contains("/items") || driver.getCurrentUrl().contains("/inventory"),
                            "URL should contain '/items' or '/inventory' after clicking All Items");
                    driver.navigate().back();
                    openBurgerMenu();
                    allItemsVisited = true;
                    break;
                case "About":
                    openAndVerifyExternalLink(link, "sobre");
                    driver.navigate().back();
                    openBurgerMenu();
                    aboutVisited = true;
                    break;
                case "Reset App State":
                    link.click();
                    wait.until(d -> d.getCurrentUrl().contains("/dashboard") || d.getCurrentUrl().contains("/home"));
                    resetVisited = true;
                    break;
                case "Logout":
                    link.click();
                    wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Login")));
                    logoutIfPresent();
                    break;
                default:
                    // ignore
            }
        }

        assertTrue(allItemsVisited, "All Items link was not visited from burger menu");
        assertTrue(aboutVisited, "About link was not visited from burger menu");
        assertTrue(resetVisited, "Reset App State link was not visited from burger menu");

        logoutIfPresent();
    }

    /* --------------------------------------------------------------------- */
    /* 6. External links policy                                            */
    @Test
    @Order(6)
    public void testExternalLinksOnHome() {
        ensureLoggedIn();

        List<WebElement> links = driver.findElements(By.cssSelector("a[href^='http']"));
        Assumptions.assumeFalse(links.isEmpty(), "No external links found on home page");

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (!href.contains("gestao.brasilagritest.com")) {
                openAndVerifyExternalLink(link, href);
            }
        }

        logoutIfPresent();
    }

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                      */
    private WebElement findFirstElement(By... locators) {
        for (By loc : locators) {
            List<WebElement> elems = driver.findElements(loc);
            if (!elems.isEmpty()) {
                return elems.get(0);
            }
        }
        return null;
    }

    private void performLogin(String email, String pass) {
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("email"), By.name("email"), By.cssSelector("input[type='email']")));
        WebElement pwdField = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("password"), By.name("password"), By.cssSelector("input[type='password']")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button#login, input[type='submit']")));

        emailField.clear();
        emailField.sendKeys(email);
        pwdField.clear();
        pwdField.sendKeys(pass);
        loginBtn.click();
    }

    private void ensureLoggedIn() {
        if (driver.findElements(By.cssSelector("a[href*='logout']")).isEmpty()) {
            performLogin(USER_EMAIL, PASSWORD);
            wait.until(d -> d.getCurrentUrl().contains("/dashboard") ||
                    d.getCurrentUrl().contains("/home") ||
                    d.getCurrentUrl().contains("/profile"));
        }
    }

    private void logoutIfPresent() {
        List<WebElement> logoutLinks = driver.findElements(By.cssSelector("a[href*='logout']"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Login")));
        }
    }

    private void navigateToInventory() {
        List<WebElement> itemsLinks = driver.findElements(By.linkText("All Items"));
        if (!itemsLinks.isEmpty()) {
            itemsLinks.get(0).click();
        } else {
            List<WebElement> inventoryLinks = driver.findElements(By.partialLinkText("inventory"));
            if (!inventoryLinks.isEmpty()) {
                inventoryLinks.get(0).click();
            } else {
                Assumptions.fail("No link to inventory page found");
            }
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".item-card, .product-card, .grid-item")));
    }

    private void openBurgerMenu() {
        List<WebElement> burgerButtons = driver.findElements(By.cssSelector(
                "button[aria-label='Menu'], .burger-menu, .hamburger, .nav-toggler"));
        Assumptions.assumeTrue(!burgerButtons.isEmpty(), "Burger menu button not found");
        wait.until(ExpectedConditions.elementToBeClickable(burgerButtons.get(0))).click();
    }

    private String getFirstItemName() {
        List<WebElement> items = driver.findElements(By.cssSelector(
                ".item-card .item-title, .product-card .product-title, .grid-item .title"));
        return items.isEmpty() ? "" : items.get(0).getText();
    }

    private void openAndVerifyExternalLink(WebElement link, String href) {
        String originalHandle = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(originalHandle)) {
                driver.switchTo().window(h);
                wait.until(ExpectedConditions.urlContains(href));
                assertTrue(driver.getCurrentUrl().contains(href),
                        "External link URL should contain: " + href);
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }
    }
}