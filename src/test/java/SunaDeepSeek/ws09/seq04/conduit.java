package SunaDeepSeek.ws09.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser123";
    private static final String PASSWORD = "test123";

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
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        Assertions.assertTrue(driver.getTitle().contains("Conduit"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.navbar-brand")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testLoginFunctionality() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#/login']")));
        signInLink.click();

        wait.until(ExpectedConditions.urlContains("#/login"));
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        signInButton.click();

        wait.until(ExpectedConditions.urlContains("#/"));
        WebElement userProfile = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#@" + USERNAME + "']")));
        Assertions.assertTrue(userProfile.isDisplayed());
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        signInButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        List<WebElement> articles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("article-preview")));
        if (articles.size() > 0) {
            String firstArticleTitle = articles.get(0).findElement(By.tagName("h1")).getText();
            articles.get(0).click();
            
            wait.until(ExpectedConditions.urlContains("#/articles/"));
            WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            Assertions.assertEquals(firstArticleTitle, articleTitle.getText());
        }
    }

    @Test
    @Order(5)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String mainWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://twitter.com/gothinkster']")));
        twitterLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(mainWindow)) {
                driver.switchTo().window(windowHandle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
                driver.close();
                break;
            }
        }
        driver.switchTo().window(mainWindow);
    }

    @Test
    @Order(6)
    public void testUserProfileNavigation() {
        driver.get(BASE_URL);
        WebElement userProfile = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#@" + USERNAME + "']")));
        userProfile.click();
        
        wait.until(ExpectedConditions.urlContains("#@" + USERNAME));
        WebElement profileName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h4")));
        Assertions.assertTrue(profileName.getText().contains(USERNAME));
    }

    @Test
    @Order(7)
    public void testNewArticleCreation() {
        driver.get(BASE_URL);
        WebElement newArticleLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#/editor']")));
        newArticleLink.click();
        
        wait.until(ExpectedConditions.urlContains("#/editor"));
        WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Article Title']")));
        WebElement descriptionField = driver.findElement(By.cssSelector("input[placeholder=\"What's this article about?\"]"));
        WebElement bodyField = driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        WebElement publishButton = driver.findElement(By.cssSelector("button[type='button']"));
        
        titleField.sendKeys("Test Article Title");
        descriptionField.sendKeys("Test Article Description");
        bodyField.sendKeys("Test Article Body");
        publishButton.click();
        
        wait.until(ExpectedConditions.urlContains("#/articles/"));
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertEquals("Test Article Title", articleTitle.getText());
    }

    @AfterEach
    public void resetState() {
        // Return to home page after each test
        if (driver.getCurrentUrl() != null && !driver.getCurrentUrl().equals(BASE_URL)) {
            driver.get(BASE_URL);
        }
    }
}