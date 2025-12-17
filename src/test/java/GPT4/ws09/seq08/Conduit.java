package GPT4.ws09.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class Conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".banner h1")));
        Assertions.assertEquals("conduit", banner.getText().trim(), "Home page should show 'conduit' banner");
    }

    @Test
    @Order(2)
    public void testNavigateToLoginPage() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#login']")));
        signInLink.click();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.text-xs-center")));
        Assertions.assertEquals("Sign In", header.getText().trim(), "Login page should show 'Sign In' header");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#login"), "URL should contain #login");
    }

    @Test
    @Order(3)
    public void testNavigateToRegisterPage() {
        driver.get(BASE_URL);
        WebElement signUpLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#register']")));
        signUpLink.click();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.text-xs-center")));
        Assertions.assertEquals("Sign Up", header.getText().trim(), "Register page should show 'Sign Up' header");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#register"), "URL should contain #register");
    }

    @Test
    @Order(4)
    public void testInvalidLoginShowsError() {
        driver.get(BASE_URL + "#login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        email.sendKeys("invalid@example.com");
        password.sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement errorList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorList.getText().toLowerCase().contains("email or password"), "Error message should be shown for invalid login");
    }

    @Test
    @Order(5)
    public void testGlobalFeedLoads() {
        driver.get(BASE_URL);
        WebElement globalFeedTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='']")));
        globalFeedTab.click();
        WebElement articlePreview = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".article-preview")));
        Assertions.assertTrue(articlePreview.isDisplayed(), "At least one article preview should be visible in Global Feed");
    }

    @Test
    @Order(6)
    public void testExternalGitHubLink() {
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github.com/gothinkster/realworld']")));
        String originalWindow = driver.getWindowHandle();
        githubLink.click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "GitHub page should open in new tab");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testFooterTextPresence() {
        driver.get(BASE_URL);
        WebElement footer = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("footer")));
        Assertions.assertTrue(footer.getText().contains("RealWorld"), "Footer should contain 'RealWorld'");
    }

    @Test
    @Order(8)
    public void testClickOnFirstArticleOpensIt() {
        driver.get(BASE_URL);
        WebElement firstArticleLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".preview-link")));
        String previewTitle = firstArticleLink.findElement(By.cssSelector("h1")).getText();
        firstArticleLink.click();
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals(previewTitle, articleTitle.getText(), "Article title should match preview title");
    }
}
