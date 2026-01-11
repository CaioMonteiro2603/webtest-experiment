package GPT20b.ws08.seq02;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static WebDriver driver;
    private static WebDriverWait wait;
    final String BASE_URL = "https://jpetstore.aspectran.com/";
    // Sample credentials for the demo application
    private static final String USERNAME = "user01";
    private static final String PASSWORD = "test";

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
        assertTrue(driver.getTitle().toLowerCase().contains("petstore"),
                "Home page title should contain 'PetStore'");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    /* --------------------------------------------------------------------- */
    /* Valid login test                                                    */
    @Test
    @Order(2)
    public void testValidLogin() {
        driver.navigate().to(BASE_URL);
        // Locate login form
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='username']")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='password']")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Login']")));

        usernameField.clear();
        usernameField.sendKeys(USERNAME);
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        // Success indicator: URL contains 'account' or 'myAccount', or greeting displayed
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("account"),
                ExpectedConditions.urlContains("myAccount"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector(".welcome-msg"))
        ));

        assertTrue(driver.getCurrentUrl().contains("account") ||
                driver.getCurrentUrl().contains("myAccount"),
                "After login, URL should contain 'account' or 'myAccount'");
        List<WebElement> welcome = driver.findElements(By.cssSelector(".welcome-msg"));
        assertFalse(welcome.isEmpty() && !welcome.get(0).isDisplayed(),
                "Welcome message should be visible after login");

        logoutIfPresent();
    }

    /* --------------------------------------------------------------------- */
    /* Invalid login test                                                 */
    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.navigate().to(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='username']")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='password']")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Login']")));

        usernameField.clear();
        usernameField.sendKeys("nonexistent");
        passwordField.clear();
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error, .alert, .validation-error")));
        assertTrue(error.isDisplayed(), "Error message should be displayed for invalid credentials");
        assertFalse(error.getText().trim().isEmpty(), "Error message should contain text");
    }

    /* --------------------------------------------------------------------- */
    /* Sorting dropdown test on Items page                                 */
    @Test
    @Order(4)
    public void testSortingDropdown() {
        // Navigate to Items page (first category)
        driver.navigate().to(BASE_URL);
        List<WebElement> itemsLink = driver.findElements(By.linkText("Items"));
        if (!itemsLink.isEmpty()) {
            itemsLink.get(0).click();
            wait.until(ExpectedConditions.urlContains("catalogue"));
        }

        // Find sorting dropdown
        List<WebElement> sortSelect = findElementsOrAssume(By.cssSelector("select[name='orderBy'], select[id='sort']"));
        Select sorter = new Select(sortSelect.get(0));
        List<WebElement> options = sorter.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Sorting dropdown should have multiple options");

        String firstBefore = getFirstProductName();
        for (WebElement opt : options) {
            sorter.selectByVisibleText(opt.getText());
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".catalogue-items .catalogue-item, .product-item")));
            String firstAfter = getFirstProductName();
            assertNotEquals(firstBefore, firstAfter,
                    "Sorting by '" + opt.getText() + "' should change the first product");
            firstBefore = firstAfter;
        }

        // Reset to original order
        sorter.selectByVisibleText(options.get(0).getText());
    }

    /* --------------------------------------------------------------------- */
    /* External links on home and about pages                              */
    @Test
    @Order(5)
    public void testExternalLinksOnHome() {
        driver.navigate().to(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']"));
        Assumptions.assumeFalse(externalLinks.isEmpty(), "No external links found on home page");

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (!href.contains("jpetstore.aspectran.com")) {
                openAndVerifyExternalLink(link, href);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* Burger menu actions test                                            */
    @Test
    @Order(6)
    public void testBurgerMenuActions() {
        loginIfNeeded();

        // Open menu
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[aria-label='Menu'], .nav-toggle, .menu-icon, .burger-button")));
        burger.click();

        boolean allItemsClicked = false;
        boolean aboutClicked = false;
        boolean resetClicked = false;

        List<WebElement> menuLinks = driver.findElements(By.tagName("a"));
        for (WebElement link : menuLinks) {
            String text = link.getText().trim();
            switch (text) {
                case "All Items":
                case "Items":
                    link.click();
                    wait.until(ExpectedConditions.urlContains("catalogue"));
                    assertTrue(driver.getCurrentUrl().contains("catalogue"),
                            "URL should contain 'catalogue' after clicking All Items");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button[aria-label='Menu'], .nav-toggle, .menu-icon, .burger-button")));
                    burger = wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button[aria-label='Menu'], .nav-toggle, .menu-icon, .burger-button")));
                    burger.click();
                    allItemsClicked = true;
                    break;
                case "About":
                    openedLinkTest(link, "jpetstore.aspectran.com");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button[aria-label='Menu'], .nav-toggle, .menu-icon, .burger-button")));
                    burger = wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button[aria-label='Menu'], .nav-toggle, .menu-icon, .burger-button")));
                    burger.click();
                    aboutClicked = true;
                    break;
                case "Reset App State":
                    link.click();
                    wait.until(ExpectedConditions.urlContains("home"));
                    resetClicked = true;
                    break;
                case "Logout":
                    link.click();
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("input[type='submit'][value='Login']")));
                    logoutIfPresent();
                    break;
                default:
                    // ignore other menu items
            }
        }

        assertTrue(allItemsClicked, "All Items link was not visited");
        assertTrue(aboutClicked, "About link did not perform external navigation");
        assertTrue(resetClicked, "Reset App State link was not clicked");
    }

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                      */
    private void loginIfNeeded() {
        if (driver.findElements(By.cssSelector("input[type='submit'][value='Login']"))
                .isEmpty()) {
            return; // already logged in
        }
        driver.navigate().to(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='username']")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='password']")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit'][value='Login']")));

        usernameField.clear();
        usernameField.sendKeys(USERNAME);
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".welcome-msg, .account-summary")));
    }

    private void logoutIfPresent() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[type='submit'][value='Login']")));
        }
    }

    private String getFirstProductName() {
        List<WebElement> items = driver.findElements(By.cssSelector(
                ".catalogue-items .catalogue-item .title, .product-item .name, .item-title"));
        return items.isEmpty() ? "" : items.get(0).getText();
    }

    private List<WebElement> findElementsOrAssume(By locator) {
        List<WebElement> elems = driver.findElements(locator);
        Assumptions.assumeTrue(!elems.isEmpty(), "Required element not found: " + locator);
        return elems;
    }

    private void openAndVerifyExternalLink(WebElement link, String expectedDomain) {
        String originalHandle = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "External link URL should contain: " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }
    }

    private void openedLinkTest(WebElement link, String expectedDomain) {
        openAndVerifyExternalLink(link, expectedDomain);
    }
}