package deepseek.ws09.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//a[contains(@class,'navbar-brand')]")));
        Assertions.assertTrue(logo.isDisplayed());
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL + "#/login");
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='Email']")));
        emailField.sendKeys("testuser@realworld.io");
        
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Password']"));
        passwordField.sendKeys(PASSWORD);
        
        WebElement signInButton = driver.findElement(By.xpath("//button[contains(text(),'Sign in')]"));
        signInButton.click();

        WebElement userProfile = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//a[contains(@class,'nav-link') and contains(text(),'testuser')]")));
        Assertions.assertTrue(userProfile.isDisplayed());
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='Email']")));
        emailField.sendKeys("invalid@email.com");
        
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Password']"));
        passwordField.sendKeys("wrongpassword");
        
        WebElement signInButton = driver.findElement(By.xpath("//button[contains(text(),'Sign in')]"));
        signInButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[@class='error-messages']//li")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(4)
    public void testArticleCreation() {
        login();
        
        WebElement newArticleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[@href='#/editor']")));
        newArticleLink.click();
        
        WebElement titleField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='Article Title']")));
        titleField.sendKeys("Test Article");
        
        WebElement aboutField = driver.findElement(By.xpath("//input[contains(@placeholder,'about')]"));
        aboutField.sendKeys("This is a test article");
        
        WebElement bodyField = driver.findElement(By.xpath("//textarea[@placeholder='Write your article']"));
        bodyField.sendKeys("This is the body of my test article");
        
        WebElement publishButton = driver.findElement(By.xpath("//button[contains(text(),'Publish')]"));
        publishButton.click();

        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Test Article')]")));
        Assertions.assertTrue(articleTitle.isDisplayed());
    }

    @Test
    @Order(5)
    public void testArticleNavigation() {
        login();
        
        WebElement globalFeedLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Global Feed')]")));
        globalFeedLink.click();
        
        List<WebElement> articles = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.xpath("//div[@class='article-preview']"), 0));
        WebElement firstArticle = articles.get(0).findElement(By.xpath(".//h1"));
        firstArticle.click();
        
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[@class='container']//h1")));
        Assertions.assertTrue(articleTitle.isDisplayed());
    }

    @Test
    @Order(6)
    public void testFollowUser() {
        login();
        
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'#/@testuser')]")));
        profileLink.click();
        
        WebElement followButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(@class,'btn-outline-secondary')]")));
        followButton.click();
        
        WebElement unfollowButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(@class,'btn-secondary')]")));
        Assertions.assertTrue(unfollowButton.getText().contains("Unfollow"));
        
        unfollowButton.click();
    }

    @Test
    @Order(7)
    public void testProfilePage() {
        login();
        
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'#/@testuser')]")));
        profileLink.click();
        
        WebElement profileHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h4[contains(text(),'testuser')]")));
        Assertions.assertTrue(profileHeader.isDisplayed());
    }

    @Test
    @Order(8)
    public void testSettingsPage() {
        login();
        
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[@href='#/settings']")));
        settingsLink.click();
        
        WebElement settingsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Settings')]")));
        Assertions.assertTrue(settingsHeader.isDisplayed());
    }

    @Test
    @Order(9)
    public void testLogout() {
        login();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Sign out')]")));
        logoutButton.click();
        
        WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Sign in')]")));
        Assertions.assertTrue(signInButton.isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL);
        if (driver.findElements(By.xpath("//a[contains(@class,'nav-link') and contains(text(),'testuser')]")).size() == 0) {
            WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'#/login')]")));
            signInLink.click();
            
            loginWithCredentials();
        }
    }

    private void loginWithCredentials() {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@placeholder='Email']")));
        emailField.sendKeys("testuser@realworld.io");
        
        WebElement passwordField = driver.findElement(By.xpath("//input[@placeholder='Password']"));
        passwordField.sendKeys(PASSWORD);
        
        WebElement signInButton = driver.findElement(By.xpath("//button[contains(text(),'Sign in')]"));
        signInButton.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//a[contains(@class,'nav-link') and contains(text(),'testuser')]")));
    }
}