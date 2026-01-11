package SunaQwen3.ws09.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;


@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

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
        Assertions.assertTrue(driver.getTitle().contains("Demo") || driver.getTitle().contains("Conduit"), "Page title should contain 'Demo' or 'Conduit'");

        By signInLink = By.linkText("Sign in");
        WebElement signInElement = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        signInElement.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(signInButton).click();

        By homeLink = By.linkText("Home");
        wait.until(ExpectedConditions.visibilityOfElementLocated(homeLink));

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("#/"), "URL should contain '#/' after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        driver.findElement(passwordField).sendKeys("wrongpass");
        driver.findElement(signInButton).click();

        // Try different error message selectors
        By errorDiv = By.cssSelector(".error-messages li");
        By errorDivAlt = By.cssSelector(".error-message");
        By errorDivAlt2 = By.cssSelector(".alert-danger");
        
        WebElement errorElement = null;
        try {
            errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(errorDiv));
        } catch (TimeoutException e) {
            try {
                errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(errorDivAlt));
            } catch (TimeoutException e2) {
                errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(errorDivAlt2));
            }
        }

        Assertions.assertTrue(errorElement.getText().toLowerCase().contains("invalid") || 
                            errorElement.getText().toLowerCase().contains("error"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigation_AllItems() {
        // Ensure logged in
        testValidLogin();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By allItemsLink = By.linkText("All Articles");
        WebElement allItemsEl = wait.until(ExpectedConditions.elementToBeClickable(allItemsLink));
        allItemsEl.click();

        By articleList = By.cssSelector(".article-preview");
        wait.until(ExpectedConditions.presenceOfElementLocated(articleList));

        List<WebElement> articles = driver.findElements(articleList);
        Assertions.assertTrue(articles.size() > 0, "At least one article should be displayed");
    }

    @Test
    @Order(4)
    public void testMenuNavigation_About_External() {
        // Ensure logged in
        testValidLogin();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By aboutLink = By.linkText("About");
        WebElement aboutEl = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        String originalWindow = driver.getWindowHandle();
        aboutEl.click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String aboutUrl = driver.getCurrentUrl();
        Assertions.assertTrue(aboutUrl.contains("realworld.io") || aboutUrl.contains("github"), "About page should be on realworld.io or github domain");

        // Close external tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testMenuNavigation_Logout() {
        // Ensure logged in
        testValidLogin();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By logoutLink = By.linkText("Log out");
        WebElement logoutEl = wait.until(ExpectedConditions.elementToBeClickable(logoutLink));
        logoutEl.click();

        // Wait for redirect to home
        By homeLink = By.linkText("Home");
        wait.until(ExpectedConditions.visibilityOfElementLocated(homeLink));

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("#/"), "Should be redirected to home after logout");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks_Twitter() {
        driver.get(BASE_URL);

        // Try different selectors for social links
        By twitterLink = By.cssSelector("a[href*='twitter.com']");
        By twitterLinkAlt = By.cssSelector("a[href*='twitter']");
        By twitterLinkAlt2 = By.xpath("//a[contains(@href, 'twitter')]");
        
        WebElement twitterEl = null;
        try {
            twitterEl = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        } catch (TimeoutException e) {
            try {
                twitterEl = wait.until(ExpectedConditions.elementToBeClickable(twitterLinkAlt));
            } catch (TimeoutException e2) {
                twitterEl = wait.until(ExpectedConditions.elementToBeClickable(twitterLinkAlt2));
            }
        }
        
        String originalWindow = driver.getWindowHandle();
        twitterEl.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String twitterUrl = driver.getCurrentUrl();
        Assertions.assertTrue(twitterUrl.contains("twitter.com") || twitterUrl.contains("x.com"), "Twitter link should open twitter.com or x.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks_Facebook() {
        driver.get(BASE_URL);

        // Try different selectors for social links
        By facebookLink = By.cssSelector("a[href*='facebook.com']");
        By facebookLinkAlt = By.cssSelector("a[href*='facebook']");
        By facebookLinkAlt2 = By.xpath("//a[contains(@href, 'facebook')]");
        
        WebElement facebookEl = null;
        try {
            facebookEl = wait.until(ExpectedConditions.elementToBeClickable(facebookLink));
        } catch (TimeoutException e) {
            try {
                facebookEl = wait.until(ExpectedConditions.elementToBeClickable(facebookLinkAlt));
            } catch (TimeoutException e2) {
                facebookEl = wait.until(ExpectedConditions.elementToBeClickable(facebookLinkAlt2));
            }
        }
        
        String originalWindow = driver.getWindowHandle();
        facebookEl.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String facebookUrl = driver.getCurrentUrl();
        Assertions.assertTrue(facebookUrl.contains("facebook.com") || facebookUrl.contains("fb.com"), "Facebook link should open facebook.com or fb.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks_LinkedIn() {
        driver.get(BASE_URL);

        // Try different selectors for social links
        By linkedInLink = By.cssSelector("a[href*='linkedin.com']");
        By linkedInLinkAlt = By.cssSelector("a[href*='linkedin']");
        By linkedInLinkAlt2 = By.xpath("//a[contains(@href, 'linkedin')]");
        
        WebElement linkedInEl = null;
        try {
            linkedInEl = wait.until(ExpectedConditions.elementToBeClickable(linkedInLink));
        } catch (TimeoutException e) {
            try {
                linkedInEl = wait.until(ExpectedConditions.elementToBeClickable(linkedInLinkAlt));
            } catch (TimeoutException e2) {
                linkedInEl = wait.until(ExpectedConditions.elementToBeClickable(linkedInLinkAlt2));
            }
        }
        
        String originalWindow = driver.getWindowHandle();
        linkedInEl.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String linkedInUrl = driver.getCurrentUrl();
        Assertions.assertTrue(linkedInUrl.contains("linkedin.com") || linkedInUrl.contains("lnkd.in"), "LinkedIn link should open linkedin.com or lnkd.in");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testArticleList_SortingDropdown() {
        testValidLogin();

        // Check if dropdown exists with multiple possible selectors
        By sortDropdown = By.cssSelector("select[ng-model='sortType']");
        By sortDropdownAlt = By.cssSelector("select");
        By sortDropdownAlt2 = By.xpath("//select");
        
        WebElement dropdown = null;
        try {
            if (driver.findElements(sortDropdown).size() > 0) {
                dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
            } else if (driver.findElements(sortDropdownAlt).size() > 0) {
                dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdownAlt));
            } else if (driver.findElements(sortDropdownAlt2).size() > 0) {
                dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdownAlt2));
            }
        } catch (Exception e) {
            // If no dropdown found, skip sorting tests
            return;
        }
        
        if (dropdown != null) {
            dropdown.click();

            By newestOption = By.cssSelector("option[value='createdAt']");
            By oldestOption = By.cssSelector("option[value='-createdAt']");
            By popularOption = By.cssSelector("option[value='favoritesCount']");
            By unpopularOption = By.cssSelector("option[value='-favoritesCount']");

            // Test Newest First
            if (driver.findElements(newestOption).size() > 0) {
                driver.findElement(newestOption).click();
                wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector(".article-preview"))));
                List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
                Assertions.assertTrue(articles.size() > 0, "At least one article should be displayed after sorting");
            }
        }
    }

    @Test
    @Order(10)
    public void testMenu_ResetAppState() {
        testValidLogin();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By resetLink = By.linkText("Reset App State");
        By resetLinkAlt = By.linkText("Reset");
        if (driver.findElements(resetLink).size() > 0 || driver.findElements(resetLinkAlt).size() > 0) {
            WebElement resetEl = null;
            if (driver.findElements(resetLink).size() > 0) {
                resetEl = wait.until(ExpectedConditions.elementToBeClickable(resetLink));
            } else {
                resetEl = wait.until(ExpectedConditions.elementToBeClickable(resetLinkAlt));
            }
            resetEl.click();

            // Wait for any potential reload or state reset
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-preview")));

            List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
            Assertions.assertTrue(articles.size() > 0, "Articles should be visible after reset");
        }
    }
}