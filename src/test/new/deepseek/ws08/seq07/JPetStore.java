package deepseek.ws08.seq07;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore{
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
    public void testStoreNavigation() {
        driver.get(BASE_URL);
        WebElement fishCategory = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Fish")));
        fishCategory.click();

        wait.until(ExpectedConditions.urlContains("FISH"));
        WebElement productTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//table[contains(@id, 'table') or contains(@class, 'products')]")));
        Assertions.assertTrue(productTable.isDisplayed(), "Fish category page should be displayed");
    }

    @Test
    @Order(2)
    public void testProductSelection() {
        driver.get(BASE_URL + "actions/Catalog.action?viewCategory=&categoryId=FISH");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'FI-SW-01') or contains(@href,'FI-SW-01')]")));
        productLink.click();

        wait.until(ExpectedConditions.urlContains("FI-SW-01"));
        WebElement addToCartButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@value='Add to Cart' or @type='submit']")));
        Assertions.assertTrue(addToCartButton.isDisplayed(), "Product details page should be displayed");
    }

    @Test
    @Order(3)
    public void testCartOperations() {
        driver.get(BASE_URL + "actions/Catalog.action?viewProduct=&productId=FI-SW-01");
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@value='Add to Cart' or @type='submit']")));
        addToCartButton.click();

        wait.until(ExpectedConditions.urlContains("viewCart"));
        WebElement cartTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h2[contains(text(),'Shopping Cart')]")));
        Assertions.assertTrue(cartTable.getText().contains("Shopping Cart"), "Cart page should be displayed");

        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Remove') or @href*='/remove']")));
        removeButton.click();
        
        WebElement emptyCartMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'messages')]//li[contains(text(),'empty')]")));
        Assertions.assertTrue(emptyCartMessage.getText().contains("Your cart is empty"));
    }

    @Test
    @Order(4)
    public void testUserLogin() {
        driver.get(BASE_URL);
        WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign In")));
        signInButton.click();

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@name='username' or @id='username']")));
        WebElement passwordField = driver.findElement(
            By.xpath("//input[@name='password' or @id='password']"));
        WebElement loginButton = driver.findElement(
            By.xpath("//input[@type='submit' or @value='Login' or @name='signon']"));

        usernameField.clear();
        usernameField.sendKeys(USERNAME);
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("main"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[@id='WelcomeContent' or contains(text(),'Welcome')]")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome"), "Login should be successful");
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@name='username' or @id='username']")));
        WebElement passwordField = driver.findElement(
            By.xpath("//input[@name='password' or @id='password']"));
        WebElement loginButton = driver.findElement(
            By.xpath("//input[@type='submit' or @value='Login' or @name='signon']"));

        usernameField.sendKeys("invaliduser");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'messages')]//li[contains(text(),'Invalid')]")));
        Assertions.assertTrue(errorMessage.getText().contains("Invalid"), "Should show invalid login message");
    }

    @Test
    @Order(6)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@name='keyword' or @placeholder='Keyword']")));
        WebElement searchButton = driver.findElement(
            By.xpath("//input[@type='submit' or @value='Search' or contains(@onclick,'search')]"));

        searchInput.sendKeys("fish");
        searchButton.click();

        wait.until(ExpectedConditions.urlContains("keyword=fish"));
        List<WebElement> products = driver.findElements(
            By.xpath("//table//td//a[contains(@href,'product')]"));
        Assertions.assertTrue(products.size() > 0, "Search should return results");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'twitter') or img[contains(@src,'twitter')]]")));
        twitterLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}