package SunaDeepSeek.ws08.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStoreTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

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
    public void testHomePageNavigation() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
        Assertions.assertTrue(driver.getTitle().contains("JPetStore"));
    }

    @Test
    @Order(2)
    public void testMainCategories() {
        driver.get(BASE_URL);
        List<WebElement> categories = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("#MainImageContent area")));
        Assertions.assertTrue(categories.size() > 0);

        for (WebElement category : categories) {
            String href = category.getAttribute("href");
            category.click();
            wait.until(ExpectedConditions.urlContains(href.substring(href.lastIndexOf("/") + 1)));
            Assertions.assertTrue(driver.getCurrentUrl().contains(href.substring(href.lastIndexOf("/") + 1)));
            driver.navigate().back();
        }
    }

    @Test
    @Order(3)
    public void testSignInAndOut() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign In")));
        signInLink.click();

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("password")));
        WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("signon")));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        signInButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#WelcomeContent")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome"));

        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign Out")));
        signOutLink.click();

        WebElement signInLinkAgain = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign In")));
        Assertions.assertTrue(signInLinkAgain.isDisplayed());
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("username")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("password")));
        WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("signon")));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        signInButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid username or password"));
    }

    @Test
    @Order(5)
    public void testProductNavigation() {
        driver.get(BASE_URL);
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#SidebarContent a[href*='FISH']")));
        fishCategory.click();

        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#Catalog table a")));
        productLink.click();

        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#Catalog table tr:nth-child(3) a")));
        itemLink.click();

        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#Catalog input[value='Add to Cart']")));
        Assertions.assertTrue(addToCartButton.isDisplayed());
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        githubLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Aspectran link
        WebElement aspectranLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='aspectran.com']")));
        aspectranLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("aspectran.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("keyword")));
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("searchProducts")));

        searchInput.sendKeys("fish");
        searchButton.click();

        List<WebElement> products = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("#Catalog table tr")));
        Assertions.assertTrue(products.size() > 1);
    }
}