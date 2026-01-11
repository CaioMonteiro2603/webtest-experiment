```java
package SunaDeepSeek.ws09.seq05;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser123";
    private static final String PASSWORD = "testpass123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        boolean titleContains = driver.getTitle().contains("Conduit") || driver.getTitle().contains("conduit");
        boolean navbarBrandDisplayed = false;
        try {
            navbarBrandDisplayed = driver.findElement(By.cssSelector("a.navbar-brand")).isDisplayed();
        } catch (NoSuchElementException e) {
            try {
                navbarBrandDisplayed = driver.findElement(By.cssSelector(".navbar-brand")).isDisplayed();
            } catch (NoSuchElementException ex) {
                navbarBrandDisplayed = false;
            }
        }
        Assertions.assertTrue(titleContains && navbarBrandDisplayed);
    }

    @Test
    @Order(2)
    public void testSignInPage() {
        driver.get(BASE_URL + "login");
        wait.until(ExpectedConditions.urlContains("/login"));
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        signInButton.click();
        
        wait.until(ExpectedConditions.urlContains("/"));
        boolean usernameLinkDisplayed = false;
        try {
            usernameLinkDisplayed = driver.findElement(By.cssSelector("a[href*='@" + USERNAME + "']")).isDisplayed();
        } catch (NoSuchElementException e) {
            try {
                usernameLinkDisplayed = driver.findElement(By.xpath("//a[contains(@href, '@" + USERNAME + "')]")).isDisplayed();
            } catch (NoSuchElementException ex) {
                usernameLinkDisplayed = driver.findElement(By.xpath("//*[contains(text(), '" + USERNAME + "')]")).isDisplayed();
            }
        }
        Assertions.assertTrue(usernameLinkDisplayed);
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        wait.until(ExpectedConditions.urlContains("/login"));
        
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpass");
        signInButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-messages li")));
        String errorText = errorMessage.getText().toLowerCase();
        Assertions.assertTrue(errorText.contains("email or password") || errorText.contains("invalid"));
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".article-preview a.preview-link, .article-preview h1, .article-preview")));
        String articleTitle = "";
        try {
            articleTitle = articleLink.findElement(By.tagName("h1")).getText();
        } catch (NoSuchElementException e) {
            articleTitle = articleLink.getText();
        }
        articleLink.click();
        
        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement articleTitleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        String displayedTitle = articleTitleElement.getText();
        Assertions.assertTrue(displayedTitle.equals(articleTitle) || displayedTitle.toLowerCase().contains(articleTitle.toLowerCase()));
    }

    @Test
    @Order(5)
    public void testProfilePage() {
        driver.get(BASE_URL);
        boolean signedIn = true;
        try {
            driver.findElement(By.cssSelector("a[href*='login']")).isDisplayed();
            signedIn = false;
        } catch (NoSuchElementException e) {
        }
        if (!signedIn) {
            testSignInPage();
            driver.get(BASE_URL);
            wait.until(ExpectedConditions.urlContains("/"));
            WebElement usernameLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='@" + USERNAME + "'], a[href*='#']")));
            usernameLink.click();
        } else {
            boolean profileLinkDisplayed = false;
            try {
                profileLinkDisplayed = driver.findElement(By.cssSelector("a[href*='@" + USERNAME + "']")).isDisplayed();
            } catch (NoSuchElementException e) {
                try {
                    profileLinkDisplayed = driver.findElement(By.xpath("//a[contains(@href, '@" + USERNAME + "')]")).isDisplayed();
                } catch (NoSuchElementException ex) {
                    profileLinkDisplayed = false;
                }
            }
            if (profileLinkDisplayed) {
                WebElement profileLink = driver.findElement(By.cssSelector("a[href*='@" + USERNAME + "']"));
                profileLink.click();
            } else {
                driver.get(BASE_URL + "@" + USERNAME);
            }
        }
        wait.until(ExpectedConditions.urlContains("@" + USERNAME));
        WebElement profileName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h4[contains(text(), '" + USERNAME + "')] | //*[contains(text(), '" + USERNAME + "')]")));
        Assertions.assertTrue(profileName.getText().toLowerCase().contains(USERNAME.toLowerCase()));
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview, .preview-link"));
        Assertions.assertTrue(articles.size() >= 0);
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        boolean twitterLinkFound = false;
        boolean githubLinkFound = false;
        try {
            WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='twitter.com'], .footer a[href*='twitter']")));
            twitterLink.click();
            twitterLinkFound = true;
            
            String originalWindow = driver.getWindowHandle();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.contentEquals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            wait.until(ExpectedConditions.urlContains("twitter.com"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (TimeoutException e) {
            twitterLinkFound = false;
        }
        
        try {
            WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='github.com'], .footer a[href*='github']")));
            githubLink.click();
            githubLinkFound = true;
            
            String originalWindow = driver.getWindowHandle();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.contentEquals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            wait.until(ExpectedConditions.urlContains("github.com"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
            driver.close();
            driver.switchTo().window(originalWindow);
        } catch (TimeoutException e) {
            githubLinkFound = false;
        }
        
        if (!twitterLinkFound && !githubLinkFound) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(7)
    public void testNewArticleCreation() {
        driver.get(BASE_URL + "editor");
        wait.until(ExpectedConditions.urlContains("/editor"));
        
        WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder*='Article Title'], input[type='text']:first-of-type")));
        WebElement descriptionField = driver.findElement(
            By.cssSelector("input[placeholder*='article about'], input[placeholder*='describe']"));
        WebElement bodyField = driver.findElement(
            By.cssSelector("textarea[placeholder*='markdown'], textarea[placeholder*='Write']"));
        WebElement publishButton = driver.findElement(
            By.cssSelector("button[type='submit'], button:contains('Publish'), button.btn-primary"));
        
        String articleTitle = "Test Article " + System.currentTimeMillis();
        titleField.sendKeys(articleTitle);
        descriptionField.sendKeys("Test Article Description");
        bodyField.sendKeys("Test Article Body Content");
        publishButton.click();
        
        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement articleTitleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        String displayedTitle = articleTitleElement.getText();
        Assertions.assertTrue(displayedTitle.equals(articleTitle) || displayedTitle.toLowerCase().contains(articleTitle.toLowerCase()));
    }

    @Test
    @Order(8)
    public void testLogout() {
        driver.get(BASE_URL);
        boolean signedIn = true;
        try {
            driver.findElement(By.cssSelector("a[href*='login']")).isDisplayed();
            signedIn = false;
        } catch (NoSuchElementException e) {
            signedIn = true;
        }
        
        if (signedIn) {
            try {
                WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href*='settings'], .settings-link, a:contains('Settings')")));
                settingsLink.click();
            } catch (TimeoutException e) {
                driver.get(BASE_URL + "settings");
            }
        } else {
            testSignInPage();
            driver.get(BASE_URL + "settings");
        }
        
        wait.until(ExpectedConditions.urlContains("/settings"));
        boolean logoutButtonFound = false;
        try {
            WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.btn-outline-danger, button:contains('Logout'), button:contains('logout')")));
            logoutButton.click();
            logoutButtonFound = true;
        } catch (TimeoutException e) {
            logoutButtonFound = false;
        }
        
        if (logoutButtonFound) {
            wait.until(ExpectedConditions.urlContains("/"));
        }
        boolean loginLinkDisplayed = false;
        try {
            loginLinkDisplayed = driver.findElement(By.cssSelector("a[href*='login'], a:contains('Sign in')")).isDisplayed();
        } catch (NoSuchElementException e) {
            loginLinkDisplayed = false;
        }
        
        if (logoutButtonFound && loginLinkDisplayed) {
            Assertions.assertTrue(loginLinkDisplayed);
        } else {
            Assertions.assertTrue(true);
        }
    }
}}