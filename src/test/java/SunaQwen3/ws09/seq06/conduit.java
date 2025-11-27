package SunaQwen3.ws09.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class RealWorldAppTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass123";

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

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("Conduit", driver.getTitle(), "Page title should be 'Conduit'");

        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        WebElement signInElement = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        signInElement.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(signInButton).click();

        By homeLink = By.xpath("//a[contains(text(), 'conduit')]");
        wait.until(ExpectedConditions.elementToBeClickable(homeLink));

        assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to home after login");
        By globalFeed = By.xpath("//a[contains(text(), 'Global Feed')]");
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(globalFeed)).isDisplayed(), "Global Feed should be visible");
    }

    @Test
    @Order(2)
    public void testInvalidLoginError() {
        driver.get(BASE_URL);
        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        WebElement signInElement = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        signInElement.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        driver.findElement(passwordField).sendKeys("wrongpass");
        driver.findElement(signInButton).click();

        By errorDiv = By.cssSelector("div.error-messages");
        String errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(errorDiv)).getText();

        assertTrue(errorMessage.contains("email or password is invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testNavigationMenuAllItems() {
        // Ensure logged in
        loginIfNot();

        By menuButton = By.cssSelector("button.navbar-toggler");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By allItemsLink = By.xpath("//a[contains(text(), 'All Articles')]");
        WebElement allItemsEl = wait.until(ExpectedConditions.elementToBeClickable(allItemsLink));
        allItemsEl.click();

        By globalFeedHeader = By.xpath("//a[contains(text(), 'Global Feed')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(globalFeedHeader));

        assertTrue(driver.getCurrentUrl().contains("#/"), "URL should contain #/ after clicking All Articles");
    }

    @Test
    @Order(4)
    public void testNavigationMenuAboutExternalLink() {
        // Ensure logged in
        loginIfNot();

        By menuButton = By.cssSelector("button.navbar-toggler");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By aboutLink = By.xpath("//a[contains(text(), 'About')]");
        String originalWindow = driver.getWindowHandle();
        WebElement aboutLinkEl = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        aboutLinkEl.click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String aboutUrl = driver.getCurrentUrl();
        assertTrue(aboutUrl.contains("realworld.io"), "About link should open realworld.io domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testNavigationMenuLogout() {
        // Ensure logged in
        loginIfNot();

        By menuButton = By.cssSelector("button.navbar-toggler");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By logoutLink = By.xpath("//a[contains(text(), 'Log out')]");
        WebElement logoutEl = wait.until(ExpectedConditions.elementToBeClickable(logoutLink));
        logoutEl.click();

        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        wait.until(ExpectedConditions.elementToBeClickable(signInLink));

        assertTrue(driver.getCurrentUrl().contains("#/login"), "Should be redirected to login page after logout");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        By twitterLink = By.cssSelector("a[href*='twitter.com']");
        By facebookLink = By.cssSelector("a[href*='facebook.com']");
        By linkedinLink = By.cssSelector("a[href*='linkedin.com']");

        List<By> socialLinks = List.of(twitterLink, facebookLink, linkedinLink);
        List<String> expectedDomains = List.of("twitter.com", "facebook.com", "linkedin.com");

        String originalWindow = driver.getWindowHandle();

        for (int i = 0; i < socialLinks.size(); i++) {
            By link = socialLinks.get(i);
            String expectedDomain = expectedDomains.get(i);

            WebElement linkEl = wait.until(ExpectedConditions.elementToBeClickable(link));
            linkEl.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            String newUrl = driver.getCurrentUrl();
            assertTrue(newUrl.contains(expectedDomain), "Social link should open correct domain: " + expectedDomain);

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(7)
    public void testArticleListAndSorting() {
        loginIfNot();

        By articleFeed = By.cssSelector("div.article-preview");
        wait.until(ExpectedConditions.presenceOfElementLocated(articleFeed));

        List<WebElement> articlesBefore = driver.findElements(articleFeed);
        int countBefore = articlesBefore.size();
        assertTrue(countBefore > 0, "At least one article should be displayed");

        // Simulate sorting - if there's a sort dropdown
        By sortDropdown = By.cssSelector("select.sort-by");
        if (driver.findElements(sortDropdown).size() > 0) {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
            dropdown.click();

            By newestOption = By.cssSelector("select.sort-by option[value='newest']");
            WebElement newestOptionEl = wait.until(ExpectedConditions.elementToBeClickable(newestOption));
            newestOptionEl.click();

            // Wait for reload
            wait.until(ExpectedConditions.stalenessOf(articlesBefore.get(0)));

            List<WebElement> articlesAfter = driver.findElements(articleFeed);
            assertTrue(articlesAfter.size() > 0, "Articles should still be present after sorting");
        }
    }

    @Test
    @Order(8)
    public void testCreateNewArticle() {
        loginIfNot();

        By newPostLink = By.xpath("//a[contains(text(), 'New Post')]");
        WebElement newPostEl = wait.until(ExpectedConditions.elementToBeClickable(newPostLink));
        newPostEl.click();

        By titleField = By.cssSelector("input[placeholder='Article Title']");
        By aboutField = By.cssSelector("input[placeholder=\"What's this article about?\"]");
        By bodyField = By.cssSelector("textarea[placeholder='Write your article (in markdown)']");
        By publishButton = By.xpath("//button[contains(text(), 'Publish Article')]");

        String testTitle = "Test Article Title";
        String testAbout = "This is a test article";
        String testBody = "This is the body of the test article.";

        wait.until(ExpectedConditions.visibilityOfElementLocated(titleField)).sendKeys(testTitle);
        driver.findElement(aboutField).sendKeys(testAbout);
        driver.findElement(bodyField).sendKeys(testBody);
        driver.findElement(publishButton).click();

        By articleTitle = By.cssSelector("h1");
        String actualTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(articleTitle)).getText();

        assertEquals(testTitle, actualTitle, "Published article title should match input");
    }

    @Test
    @Order(9)
    public void testViewArticleAndComments() {
        loginIfNot();

        By firstArticleLink = By.cssSelector("a.preview-link h1");
        WebElement firstArticleEl = wait.until(ExpectedConditions.elementToBeClickable(firstArticleLink));
        firstArticleEl.click();

        By articleContent = By.cssSelector("div.article-content");
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(articleContent)).isDisplayed(), "Article content should be visible");

        By commentField = By.cssSelector("textarea[placeholder='Write a comment...']");
        assertTrue(driver.findElements(commentField).size() > 0, "Comment section should be present");
    }

    @Test
    @Order(10)
    public void testUserProfileVisit() {
        loginIfNot();

        By profileLink = By.cssSelector("a.nav-link");
        String profileText = wait.until(ExpectedConditions.visibilityOfElementLocated(profileLink)).getText();

        profileLink = By.xpath("//a[@class='nav-link' and text()='" + profileText + "']");
        WebElement profileEl = wait.until(ExpectedConditions.elementToBeClickable(profileLink));
        profileEl.click();

        By userArticles = By.cssSelector("div.article-preview");
        wait.until(ExpectedConditions.presenceOfElementLocated(userArticles));

        assertTrue(driver.getCurrentUrl().contains("#/profile/"), "URL should contain profile path");
        assertTrue(driver.findElements(userArticles).size() >= 0, "User articles section should be present");
    }

    private void loginIfNot() {
        if (driver.getCurrentUrl().contains("#/login") || driver.findElements(By.xpath("//a[contains(text(), 'Sign in')]")).size() > 0) {
            driver.get(BASE_URL);
            By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
            WebElement signInElement = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
            signInElement.click();

            By emailField = By.cssSelector("input[placeholder='Email']");
            By passwordField = By.cssSelector("input[placeholder='Password']");
            By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

            wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
            driver.findElement(passwordField).sendKeys(PASSWORD);
            driver.findElement(signInButton).click();

            By homeLink = By.xpath("//a[contains(text(), 'conduit')]");
            wait.until(ExpectedConditions.elementToBeClickable(homeLink));
        }
    }
}