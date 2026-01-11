package GPT20b.ws09.seq04;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
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

    /* -------------------- Utility methods -------------------- */

    private WebElement findElement(String... cssSelectors) {
        for (String sel : cssSelectors) {
            List<WebElement> els = driver.findElements(By.cssSelector(sel));
            if (!els.isEmpty()) {
                return els.get(0);
            }
        }
        throw new NoSuchElementException("Could not find element matching selectors: "
                + String.join(", ", cssSelectors));
    }

    private void openAndVerifyExternalLink(String hrefFragment, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector("a[href*='" + hrefFragment + "']"));
        if (links.isEmpty()) {
            return; // nothing to test
        }
        WebElement link = links.get(0);
        String originalHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();

        link.click();

        Set<String> after = driver.getWindowHandles();
        if (after.size() > before.size()) {
            after.removeAll(before);
            String newWindow = after.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(d1 -> d1.getCurrentUrl().contains(expectedDomain));
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            wait.until(d1 -> d1.getCurrentUrl().contains(expectedDomain));
            driver.navigate().back();
        }
    }

    /* -------------------- Tests -------------------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.navigate().to(BASE_URL);
        // Verify title and a header are present
        wait.until(ExpectedConditions.titleContains("Realworld"));
        WebElement header = findElement("h1");
        assertTrue(header.isDisplayed(), "Home page should display H1 header");
    }

    @Test
    @Order(2)
    public void testSortingDropdownIfPresent() {
        driver.navigate().to(BASE_URL);
        List<WebElement> sortElements = driver.findElements(By.cssSelector("select#sort"));
        if (sortElements.isEmpty()) {
            // Dropdown not present; test passes
            return;
        }
        WebElement sortDropdown = sortElements.get(0);
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        assertTrue(options.size() > 1, "Sorting dropdown should have multiple options");

        String firstOption = options.get(0).getAttribute("value");
        options.get(1).click();
        String secondOption = sortDropdown.getAttribute("value");
        assertNotEquals(firstOption, secondOption, "Sorting selection should change the value");

        // Verify that the list of posts changed
        List<WebElement> posts = driver.findElements(By.cssSelector(".post-preview"));
        if (!posts.isEmpty()) {
            String firstPostTitle = posts.get(0).findElement(By.cssSelector("h2")).getText();
            options.get(2).click(); // another option
            wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.cssSelector(".post-preview h2"), firstPostTitle)));
            String newFirstPostTitle = posts.get(0).findElement(By.cssSelector("h2")).getText();
            assertNotEquals(firstPostTitle, newFirstPostTitle, "First post title should change after sorting");
        }
    }

    @Test
    @Order(3)
    public void testBurgerMenuNavigation() {
        driver.navigate().to(BASE_URL);
        // Reveal the navigation menu (for mobile view)
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-toggler, button.navbar-toggler")));
        burger.click();

        // All Items (Home)
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a.nav-link[href='/' or href='home']")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().contains("/"),
                "Should navigate back to home page via 'All Items' link");

        // Reopen menu
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-toggler, button.navbar-toggler")));
        burger.click();

        // About – external link
        openAndVerifyExternalLink("about", "about");

        // Reopen menu
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-toggler, button.navbar-toggler")));
        burger.click();

        // Reset App State – placeholder link, may not exist
        List<WebElement> resetLinks = driver.findElements(By.cssSelector("a[href*='reset']"));
        if (!resetLinks.isEmpty()) {
            resetLinks.get(0).click();
            wait.until(ExpectedConditions.urlContains("/"));
        }

        // Reopen menu
        burger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-toggler, button.navbar-toggler")));
        burger.click();

        // Logout – if logged in, otherwise placeholder
        List<WebElement> logoutLinks = driver.findElements(By.cssSelector("a[href*='logout']"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.urlContains("/login"));
        }
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        driver.navigate().to(BASE_URL);
        openAndVerifyExternalLink("twitter.com", "twitter.com");
        openAndVerifyExternalLink("facebook.com", "facebook.com");
        openAndVerifyExternalLink("linkedin.com", "linkedin.com");
    }

    @Test
    @Order(5)
    public void testCreateNewArticleIfLoggedIn() {
        driver.navigate().to(BASE_URL + "login");
        // Attempt login – this may succeed if a test account exists
        List<WebElement> emailField = driver.findElements(By.cssSelector("input[type='email']"));
        List<WebElement> passwordField = driver.findElements(By.cssSelector("input[type='password']"));
        if (!emailField.isEmpty() && !passwordField.isEmpty()) {
            emailField.get(0).clear();
            emailField.get(0).sendKeys("john");
            passwordField.get(0).clear();
            passwordField.get(0).sendKeys("password");
            WebElement loginBtn = findElement("button.btn-primary");
            loginBtn.click();
            wait.until(ExpectedConditions.urlContains("/profile"));
        } else {
            // No login form – skip
            return;
        }

        // Create new article
        WebElement newArticleBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/editor']")));
        newArticleBtn.click();
        wait.until(ExpectedConditions.urlContains("/editor"));

        WebElement titleInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[placeholder='Article Title']")));
        titleInput.sendKeys("Test Article");
        WebElement bodyTextarea = findElement("textarea[placeholder='Write your article (in markdown)']");
        bodyTextarea.sendKeys("This is a test article created by automated test.");
        WebElement publishBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.btn-primary")));
        publishBtn.click();
        wait.until(ExpectedConditions.urlContains("/article/"));

        // Verify article appears in feed
        driver.navigate().to(BASE_URL);
        List<WebElement> feedArticles = driver.findElements(By.cssSelector(".post-preview"));
        boolean found = feedArticles.stream()
                .anyMatch(p -> p.findElement(By.cssSelector("h2")).getText().contains("Test Article"));
        assertTrue(found, "Newly created article should appear in the feed");

        // Optional: delete article via API or UI if supported
    }
}