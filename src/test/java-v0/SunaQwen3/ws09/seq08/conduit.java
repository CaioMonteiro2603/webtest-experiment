package SunaQwen3.ws09.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";

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
    public void testPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().contains("Conduit"), "Page title should contain 'Conduit'");
        Assertions.assertTrue(driver.getCurrentUrl().contains("demo.realworld.io"), "URL should contain base domain");
    }

    @Test
    @Order(2)
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        WebElement signInElement = wait.until(elementToBeClickable(signInLink));
        signInElement.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By submitButton = By.cssSelector("button[type='submit']");

        wait.until(visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(submitButton).click();

        By homeLink = By.xpath("//a[contains(text(), 'Home')]");
        wait.until(elementToBeClickable(homeLink));

        Assertions.assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to home feed after login");
        Assertions.assertTrue(driver.getPageSource().contains("Your Feed"), "Page should contain 'Your Feed' section");
    }

    @Test
    @Order(3)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL + "#/login");
        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By submitButton = By.cssSelector("button[type='submit']");

        wait.until(visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        driver.findElement(passwordField).sendKeys("wrongpassword");
        driver.findElement(submitButton).click();

        By errorAlert = By.cssSelector(".error-messages li");
        WebElement errorElement = wait.until(visibilityOfElementLocated(errorAlert));

        Assertions.assertTrue(errorElement.isDisplayed(), "Error message should be displayed");
        Assertions.assertEquals("email or password is invalid", errorElement.getText().toLowerCase(), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    public void testNavigationToRegisterPageAndBack() {
        driver.get(BASE_URL);
        By signUpLink = By.xpath("//a[contains(text(), 'Sign up')]");
        WebElement signUpElement = wait.until(elementToBeClickable(signUpLink));
        signUpElement.click();

        Assertions.assertTrue(driver.getCurrentUrl().contains("#/register"), "Should navigate to register page");

        By haveAnAccountLink = By.xpath("//a[contains(text(), 'Have an account?')]");
        WebElement loginLink = wait.until(elementToBeClickable(haveAnAccountLink));
        loginLink.click();

        wait.until(urlContains("#/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/login"), "Should return to login page");
    }

    @Test
    @Order(5)
    public void testCreateNewArticleLinkRequiresAuthentication() {
        driver.get(BASE_URL);
        By newPostLink = By.xpath("//a[contains(text(), 'New Post')]");
        WebElement newPostElement = wait.until(elementToBeClickable(newPostLink));
        newPostElement.click();

        wait.until(urlContains("#/login"));
        Assertions.assertTrue(driver.getPageSource().contains("Need an account?"), "Should redirect to login page for unauthenticated users");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinksOpenInNewTab() {
        driver.get(BASE_URL);
        By twitterLink = By.cssSelector("a[href='https://twitter.com/gothinkster']");
        By facebookLink = By.cssSelector("a[href='https://www.facebook.com/thinkster.io']");
        By linkedinLink = By.cssSelector("a[href='https://www.linkedin.com/company/thinkster-io']");

        String originalWindow = driver.getWindowHandle();
        List<By> socialLinks = List.of(twitterLink, facebookLink, linkedinLink);
        List<String> expectedDomains = List.of("twitter.com", "facebook.com", "linkedin.com");

        for (int i = 0; i < socialLinks.size(); i++) {
            WebElement link = wait.until(elementToBeClickable(socialLinks.get(i)));
            ((JavascriptExecutor) driver).executeScript("arguments[0].target='_blank';", link);
            link.click();

            Set<String> windowHandles = driver.getWindowHandles();
            Assertions.assertEquals(2, windowHandles.size(), "A new tab should be opened");

            for (String windowHandle : windowHandles) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains(expectedDomains.get(i)), "New tab should navigate to expected domain: " + expectedDomains.get(i));

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(7)
    public void testMenuNavigationAndLogout() {
        // Ensure logged in
        loginIfNotLoggedIn();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menuElement = wait.until(elementToBeClickable(menuButton));
        menuElement.click();

        By allItemsLink = By.xpath("//a[contains(text(), 'Home')]");
        By settingsLink = By.xpath("//a[contains(text(), 'Settings')]");
        By logoutLink = By.xpath("//a[contains(text(), 'Log out')]");

        // Test All Items
        WebElement allItemsElement = wait.until(elementToBeClickable(allItemsLink));
        allItemsElement.click();
        wait.until(urlContains("#/"));
        Assertions.assertTrue(driver.getPageSource().contains("Your Feed"), "Should navigate to home feed");

        // Reopen menu
        menuElement = wait.until(elementToBeClickable(menuButton));
        menuElement.click();

        // Test Settings
        WebElement settingsElement = wait.until(elementToBeClickable(settingsLink));
        settingsElement.click();
        wait.until(urlContains("#/settings"));
        Assertions.assertTrue(driver.getPageSource().contains("Your Settings"), "Should navigate to settings page");

        // Reopen menu
        menuElement = wait.until(elementToBeClickable(menuButton));
        menuElement.click();

        // Test Logout
        WebElement logoutElement = wait.until(elementToBeClickable(logoutLink));
        logoutElement.click();

        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        wait.until(elementToBeClickable(signInLink));

        Assertions.assertTrue(driver.getCurrentUrl().contains("#/"), "Should return to home after logout");
        Assertions.assertTrue(driver.getPageSource().contains("Sign in"), "Login link should be visible after logout");
    }

    @Test
    @Order(8)
    public void testTagFilteringOnHomePage() {
        driver.get(BASE_URL);
        loginIfNotLoggedIn();

        By popularTagsSection = By.cssSelector(".sidebar");
        wait.until(visibilityOfElementLocated(popularTagsSection));

        By firstTag = By.cssSelector(".tag-pill");
        List<WebElement> tagElements = wait.until(presenceOfAllElementsLocatedBy(firstTag));

        Assertions.assertTrue(tagElements.size() > 0, "There should be at least one tag available");

        for (int i = 0; i < Math.min(2, tagElements.size()); i++) {
            String tagName = tagElements.get(i).getText();

            driver.get(BASE_URL); // Reset to home
            tagElements = wait.until(presenceOfAllElementsLocatedBy(firstTag));
            WebElement tagElement = tagElements.get(i);

            wait.until(elementToBeClickable(tagElement)).click();

            By tagTitle = By.cssSelector(".banner h1");
            wait.until(visibilityOfElementLocated(tagTitle));

            String pageHeader = driver.findElement(By.cssSelector(".banner h1")).getText();
            Assertions.assertEquals(tagName, pageHeader, "Page header should match the selected tag: " + tagName);
        }
    }

    @Test
    @Order(9)
    public void testArticleListLoadsAndDisplaysCorrectly() {
        driver.get(BASE_URL);
        loginIfNotLoggedIn();

        By feedToggle = By.xpath("//a[contains(text(), 'Your Feed')]");
        wait.until(elementToBeClickable(feedToggle));

        By articleList = By.cssSelector(".article-preview");
        List<WebElement> articles = wait.until(presenceOfAllElementsLocatedBy(articleList));

        Assertions.assertTrue(articles.size() > 0, "At least one article should be displayed");

        WebElement firstArticle = articles.get(0);
        By titleLocator = By.cssSelector("h1");
        By authorLocator = By.cssSelector(".author");
        By favoriteCount = By.cssSelector(".counter");

        String titleText = firstArticle.findElement(titleLocator).getText();
        String authorText = firstArticle.findElement(authorLocator).getText();

        Assertions.assertFalse(titleText.isEmpty(), "Article title should not be empty");
        Assertions.assertFalse(authorText.isEmpty(), "Article author should not be empty");

        if (firstArticle.findElements(favoriteCount).size() > 0) {
            String favorites = firstArticle.findElement(favoriteCount).getText();
            Assertions.assertNotNull(favorites, "Favorite count should be present");
        }
    }

    @Test
    @Order(10)
    public void testFavoriteAnArticle() {
        driver.get(BASE_URL);
        loginIfNotLoggedIn();

        By firstFavoriteButton = By.cssSelector(".article-preview .btn-outline-primary");
        WebElement favoriteBtn = wait.until(elementToBeClickable(firstFavoriteButton));
        String initialText = favoriteBtn.getText();

        favoriteBtn.click();
        wait.until(textToBePresentInElement(favoriteBtn, "Unfavorite"));

        String updatedText = favoriteBtn.getText();
        Assertions.assertTrue(updatedText.contains("Unfavorite"), "Button should change to 'Unfavorite'");
        Assertions.assertNotEquals(initialText, updatedText, "Favorite status should change after click");
    }

    @Test
    @Order(11)
    public void testUnfavoriteAnArticle() {
        driver.get(BASE_URL);
        loginIfNotLoggedIn();

        By firstFavoriteButton = By.cssSelector(".article-preview .btn-primary");
        List<WebElement> favoriteButtons = driver.findElements(firstFavoriteButton);

        if (favoriteButtons.isEmpty()) {
            // If no article is favorited, skip or favorite one first
            testFavoriteAnArticle();
            driver.navigate().refresh();
        }

        WebElement favoriteBtn = wait.until(elementToBeClickable(firstFavoriteButton));
        String initialText = favoriteBtn.getText();

        favoriteBtn.click();
        wait.until(textToBePresentInElement(favoriteBtn, "Favorite"));

        String updatedText = favoriteBtn.getText();
        Assertions.assertTrue(updatedText.contains("Favorite"), "Button should change to 'Favorite'");
        Assertions.assertNotEquals(initialText, updatedText, "Favorite status should change after click");
    }

    @Test
    @Order(12)
    public void testViewArticleDetails() {
        driver.get(BASE_URL);
        loginIfNotLoggedIn();

        By firstArticleTitle = By.cssSelector(".article-preview h1");
        WebElement articleTitleElement = wait.until(elementToBeClickable(firstArticleTitle));
        String expectedTitle = articleTitleElement.getText();

        articleTitleElement.click();

        By detailTitle = By.cssSelector(".banner h1");
        WebElement titleElement = wait.until(visibilityOfElementLocated(detailTitle));

        Assertions.assertEquals(expectedTitle, titleElement.getText(), "Article detail title should match clicked article");
        Assertions.assertTrue(driver.getPageSource().contains("Read more..."), "Article page should contain content");
    }

    @Test
    @Order(13)
    public void testUserProfilePage() {
        driver.get(BASE_URL);
        loginIfNotLoggedIn();

        By firstAuthorLink = By.cssSelector(".article-preview .author");
        WebElement authorLink = wait.until(elementToBeClickable(firstAuthorLink));
        String expectedAuthor = authorLink.getText();

        authorLink.click();

        By profileHeader = By.cssSelector(".user-info h4");
        WebElement profileElement = wait.until(visibilityOfElementLocated(profileHeader));

        Assertions.assertEquals(expectedAuthor, profileElement.getText(), "Profile header should match author name");
        Assertions.assertTrue(driver.getPageSource().contains("Posts"), "User profile should show posts section");
    }

    @Test
    @Order(14)
    public void testAboutLinkInFooterIsExternal() {
        driver.get(BASE_URL);
        By aboutLink = By.xpath("//a[contains(text(), 'About')]");

        String originalWindow = driver.getWindowHandle();
        WebElement aboutElement = wait.until(elementToBeClickable(aboutLink));
        ((JavascriptExecutor) driver).executeScript("arguments[0].target='_blank';", aboutElement);
        aboutElement.click();

        Set<String> windowHandles = driver.getWindowHandles();
        Assertions.assertEquals(2, windowHandles.size(), "A new tab should be opened");

        String newWindow = null;
        for (String handle : windowHandles) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }

        Assertions.assertNotNull(newWindow, "New window handle should not be null");
        driver.switchTo().window(newWindow);

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("thinkster.io"), "About link should redirect to thinkster.io domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginIfNotLoggedIn() {
        if (driver.getCurrentUrl().contains("#/login") || driver.findElements(By.xpath("//a[contains(text(), 'Sign in')]")).size() > 0) {
            driver.get(BASE_URL + "#/login");
            By emailField = By.cssSelector("input[placeholder='Email']");
            By passwordField = By.cssSelector("input[placeholder='Password']");
            By submitButton = By.cssSelector("button[type='submit']");

            wait.until(visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
            driver.findElement(passwordField).sendKeys(PASSWORD);
            driver.findElement(submitButton).click();

            wait.until(elementToBeClickable(By.xpath("//a[contains(text(), 'Home')]")));
        }
    }
}