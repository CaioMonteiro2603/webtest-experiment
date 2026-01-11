package GPT20b.ws10.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void initDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* --------------------------------------------------------------------- */
    /* Helper methods                                                         */
    /* --------------------------------------------------------------------- */

    /** Find the first element matching any of the provided CSS selectors. */
    private WebElement findElement(String... cssSelectors) {
        for (String sel : cssSelectors) {
            List<WebElement> lista = driver.findElements(By.cssSelector(sel));
            if (!lista.isEmpty()) {
                return lista.get(0);
            }
        }
        throw new NoSuchElementException("No element found for selectors: " + String.join(", ", cssSelectors));
    }
    

    /** Open a link that contains the specified fragment, verify the URL contains the expected domain, then close and return. */
    private void openAndVerifyExternalLink(String hrefFragment, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + hrefFragment + "']"));
        if (links.isEmpty()) return;
        WebElement link = links.get(0);
        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        link.click();

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newWindow = after.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            wait.until(d -> d.getCurrentUrl().contains(expectedDomain));
            driver.navigate().back();
        }
    }

   
    private void performLogin() {
        driver.navigate().to(BASE_URL);
        WebElement emailField = findElement("input#email", "input[name='email']", "input[type='email']");
        WebElement passwordField = findElement("input#password", "input[name='password']", "input[type='password']");
        emailField.clear();
        emailField.sendKeys(USERNAME);
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = findElement("button#login", "button[type='submit']",
                                             "button[name='login']", "button.login");
        loginButton.click();

        wait.until(driver1 -> driver1.getCurrentUrl().contains("/dashboard") ||
                                 driver1.getCurrentUrl().contains("/home") ||
                                 driver1.getCurrentUrl().contains("/"));
    }

    /** Attempt to click 'Reset App State' if present. */
    private void resetAppStateIfPresent() {
        List<WebElement> resetLinks = driver.findElements(By.cssSelector("a[href*='resetdatastore'], a[href*='reset']"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            // Wait until page returns to a stable state after reset
            wait.until(ExpectedConditions.urlMatches(".*(/dashboard|/home|$)"));
        }
    }

    /* --------------------------------------------------------------------- */
    /* Test cases                                                           */
    /* --------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testLoginPageElementsPresent() {
        driver.navigate().to(BASE_URL);
        findElement("input#email", "input[name='email']", "input[type='email']");
        findElement("input#password", "input[name='password']", "input[type='password']");
        findElement("button#", "button[type='submit']");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        driver.navigate().to(BASE_URL);
        WebElement emailField = findElement("input#email", "input[name='email']", "input[type='email']");
        WebElement passwordField = findElement("input#password", "input[name='password']", "input[type='password']");
        emailField.clear();
        passwordField.clear();
        passwordField.sendKeys("wrongpass");

        WebElement loginBtn = findElement("button#login", "button[type='submit']");
        loginBtn.click();

        List<WebElement> errors = driver.findElements(By.cssSelector(".error, .alert-danger, .text-danger"));
        assertFalse(errors.isEmpty(), "An error message should appear for invalid credentials");
    }

    @Test
    @Order(3)
    public void testValidLoginAndLogout() {
        performLogin();
        // Verify an element visible only after login, e.g., a dashboard placeholder
        List<WebElement> dashboardElems = driver.findElements(By.cssSelector(".dashboard, .home-content, .welcome"));
        assertFalse(dashboardElems.isEmpty(), "Dashboard element should be visible after login");

        // Logout
        WebElement logoutLink = findElement("a[href*='logout'], button#logout", "button.logout");
        logoutLink.click();
        wait.until(driver1 -> driver1.getCurrentUrl().contains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"), "Should return to login page after logout");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        performLogin();

        List<WebElement> sortDrop = driver.findElements(By.cssSelector("select#sort"));
        if (sortDrop.isEmpty()) {
            // No sorting available; treat as passed
            return;
        }
        WebElement sortDropdown = sortDrop.get(0);
        List<WebElement> options = sortDropdown.findElements(By.tagName(""));
        assertTrue(options.size() > 1, "Sorting dropdown should contain multiple options");

       
        options.get(1).click();
        String changedValue = sortDropdown.getAttribute("value");
        assertNotEquals(changedValue, "Selecting a different sorting option should change the value");

        // Verify list order changed
        List<WebElement> itemsBefore = driver.findElements(By.cssSelector(".item-card, .product-item"));
        if (!itemsBefore.isEmpty()) {
            String firstTitleBefore = itemsBefore.get(0).getText();
            options.get(2).click();
            wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.cssSelector(".item-card, .product-item"), firstTitleBefore)));
            List<WebElement> itemsAfter = driver.findElements(By.cssSelector(".item-card, .product-item"));
            String firstTitleAfter = itemsAfter.get(0).getText();
            assertNotEquals(firstTitleBefore, firstTitleAfter, "First item title should change after applying another sort");
        }

        // Reset state after test
        resetAppStateIfPresent();
        performLogin(); // return to logged state
    }

    @Test
    @Order(5)
    public void testBurgerMenuOptions() {
        performLogin();

        // Open burger menu
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-toggler, button#menu-toggle, button.burger-menu")));
        burger.click();

        // All Items (usually Home)
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/']")));
        allItems.click();
        wait.until(driver1 -> driver1.getCurrentUrl().contains("/home") || driver1.getCurrentUrl().contains("/"));
        assertTrue(driver.getCurrentUrl().contains("/home") || driver.getCurrentUrl().contains("/"),
                "All Items should navigate to main page");

        // Reopen burger for About (external)
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-toggler, button#menu-toggle, button.burger-menu")));
        burger.click();
        openAndVerifyExternalLink("about", "about");

        // Reopen burger for Reset App State
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-toggler, button#menu-toggle, button.burger-menu")));
        burger.click();
        List<WebElement> resetLinks = driver.findElements(By.cssSelector("a[href*='reset']"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            wait.until(driver1 -> driver1.getCurrentUrl().contains("/dashboard") || driver1.getCurrentUrl().contains("/home"));
        }

        // Reopen burger for Logout
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-toggler, button#menu-toggle, button.burger-menu")));
        burger.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='logout'], button#logout")));
        logout.click();
        wait.until(driver1 -> driver1.getCurrentUrl().contains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"), "Logout should redirect to login page");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.navigate().to(BASE_URL);
        openAndVerifyExternalLink("twitter.com", "twitter.com");
        openAndVerifyExternalLink("facebook.com", "facebook.com");
        openAndVerifyExternalLink("linkedin.com", "linkedin.com");
    }
}