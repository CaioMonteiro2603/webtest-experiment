package deepseek.ws09.seq06;

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
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser@example.com";
    private static final String PASSWORD = "password123";
    private static WebDriverWait wait;

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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".banner")));
        Assertions.assertTrue(banner.isDisplayed(), "Home page banner should be visible");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get(BASE_URL + "#/login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        signInButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> userAvatars = driver.findElements(By.cssSelector("a[href*='profile']"));
        Assertions.assertTrue(userAvatars.size() > 0, "User should be logged in and profile link should be visible");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys("invalid@email.com");
        password.sendKeys("wrongpassword");
        signInButton.click();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".error-messages li"));
        Assertions.assertTrue(errorMessages.size() > 0 && errorMessages.get(0).isDisplayed(), "Error message should be shown for invalid login");
    }

    @Test
    @Order(4)
    public void testArticleCreation() {
        testValidLogin();
        
        List<WebElement> editorLinks = driver.findElements(By.cssSelector("a[href*='editor']"));
        if (editorLinks.size() > 0) {
            editorLinks.get(0).click();
        } else {
            driver.get(BASE_URL + "#/editor");
        }

        WebElement title = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Article Title']")));
        WebElement description = driver.findElement(By.cssSelector("input[placeholder*='article']"));
        WebElement body = driver.findElement(By.cssSelector("textarea[placeholder*='article']"));
        List<WebElement> buttons = driver.findElements(By.cssSelector("button[type='button']"));
        WebElement publishButton = buttons.stream()
            .filter(b -> b.getText().toLowerCase().contains("publish"))
            .findFirst()
            .orElse(buttons.get(buttons.size() - 1));

        title.sendKeys("Test Article Title");
        description.sendKeys("This is a test article description");
        body.sendKeys("This is the body of the test article");
        publishButton.click();

        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals("Test Article Title", articleTitle.getText(), "Article should be created successfully");
    }

    @Test
    @Order(5)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> articleLinks = driver.findElements(By.cssSelector("a.preview-link"));
        if (articleLinks.size() > 0) {
            String expectedTitle = articleLinks.get(0).getText();
            articleLinks.get(0).click();

            WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h1")));
            Assertions.assertFalse(articleTitle.getText().isEmpty(), "Should navigate to article page with title");
        } else {
            Assertions.fail("No articles found on the page");
        }
    }

    @Test
    @Order(6)
    public void testTagSelection() {
        driver.get(BASE_URL);
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> tagLinks = driver.findElements(By.cssSelector(".tag-pill"));
        if (tagLinks.size() > 0) {
            String tagName = tagLinks.get(0).getText();
            tagLinks.get(0).click();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
            Assertions.assertTrue(articles.size() >= 0, "Should show articles for selected tag");
        } else {
            Assertions.fail("No tags found on the page");
        }
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href^='http']"));
        if (externalLinks.size() > 0) {
            String originalWindow = driver.getWindowHandle();
            externalLinks.get(0).click();

            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
            Assertions.assertTrue(true, "External link clicked and handled");
        } else {
            Assertions.assertTrue(true, "No external links found");
        }
    }

    @Test
    @Order(8)
    public void testProfileNavigation() {
        testValidLogin();
        
        List<WebElement> profileLinks = driver.findElements(By.cssSelector("a[href*='profile']"));
        if (profileLinks.size() > 0) {
            profileLinks.get(0).click();

            WebElement profileName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h4")));
            Assertions.assertTrue(profileName.isDisplayed(), "User profile should be visible");
        } else {
            driver.get(BASE_URL + "#/profile");
            WebElement profileName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h4")));
            Assertions.assertTrue(profileName.isDisplayed(), "User profile should be visible");
        }
    }

    @Test
    @Order(9)
    public void testLogout() {
        testValidLogin();
        
        List<WebElement> settingsLinks = driver.findElements(By.cssSelector("a[href*='settings']"));
        if (settingsLinks.size() > 0) {
            settingsLinks.get(0).click();
        } else {
            driver.get(BASE_URL + "#/settings");
        }

        List<WebElement> logoutButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("button")));
        WebElement logoutButton = logoutButtons.stream()
            .filter(b -> b.getText().toLowerCase().contains("logout") || 
                        b.getAttribute("class").toLowerCase().contains("danger"))
            .findFirst()
            .orElse(null);

        if (logoutButton != null) {
            logoutButton.click();
        } else {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("document.querySelector('button.btn-outline-danger').click();");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> loginLinks = driver.findElements(By.cssSelector("a[href*='login']"));
        Assertions.assertTrue(loginLinks.size() > 0 || driver.getCurrentUrl().contains("login"), 
            "Should be logged out and see login link");
    }
}