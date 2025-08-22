package GPT4.ws08.seq01;

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
public class JPetStoreTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jpetstore.aspectran.com/";

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

    private void switchToNewTabAndVerify(String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected external domain not found: " + expectedDomain);
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#Logo img")));
        Assertions.assertTrue(logo.isDisplayed(), "Home page logo should be visible.");
    }

    @Test
    @Order(2)
    public void testEnterStoreLink() {
        driver.get(BASE_URL);
        WebElement enterStore = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Enter the Store")));
        enterStore.click();
        wait.until(ExpectedConditions.urlContains("/catalog/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/"), "URL should contain /catalog/ after entering store.");
    }

    @Test
    @Order(3)
    public void testSignInWithInvalidCredentials() {
        driver.get(BASE_URL + "catalog/");
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signIn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys("invaliduser");
        driver.findElement(By.name("password")).sendKeys("invalidpass");
        driver.findElement(By.name("signon")).click();
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.messages li")));
        Assertions.assertTrue(message.getText().toLowerCase().contains("invalid"),
                "Expected invalid login error message.");
    }

    @Test
    @Order(4)
    public void testValidLoginAndLogout() {
        driver.get(BASE_URL + "catalog/");
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signIn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        driver.findElement(By.name("username")).sendKeys("j2ee");
        driver.findElement(By.name("password")).sendKeys("j2ee");
        driver.findElement(By.name("signon")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign Out")));
        Assertions.assertTrue(driver.findElement(By.linkText("Sign Out")).isDisplayed(), "Sign Out link should be visible after login.");

        // Logout
        driver.findElement(By.linkText("Sign Out")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign In")));
        Assertions.assertTrue(driver.findElement(By.linkText("Sign In")).isDisplayed(), "Should be able to see Sign In after logout.");
    }

    @Test
    @Order(5)
    public void testFooterExternalLink() {
        driver.get(BASE_URL);
        WebElement aspectranLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='aspectran.com']")));
        String originalWindow = driver.getWindowHandle();
        aspectranLink.click();
        switchToNewTabAndVerify("aspectran.com");
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Should return to original window.");
    }

    @Test
    @Order(6)
    public void testProductCategoryNavigation() {
        driver.get(BASE_URL + "catalog/");
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("area[alt='Fish']")));
        fishLink.click();
        wait.until(ExpectedConditions.urlContains("FISH"));
        WebElement categoryTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Fish')]")));
        Assertions.assertTrue(categoryTitle.isDisplayed(), "Fish category page should load.");
    }

    @Test
    @Order(7)
    public void testProductAddToCartAndRemove() {
        driver.get(BASE_URL + "catalog/");
        WebElement dogsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("area[alt='Dogs']")));
        dogsLink.click();
        wait.until(ExpectedConditions.urlContains("DOGS"));
        WebElement bulldogLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("K9-BD-01")));
        bulldogLink.click();
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();
        WebElement cartTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.cart")));
        Assertions.assertTrue(cartTable.getText().contains("Bulldog"), "Cart should contain Bulldog item.");

        WebElement removeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Remove")));
        removeLink.click();
        wait.until(ExpectedConditions.invisibilityOf(cartTable));
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p")));
        Assertions.assertTrue(message.getText().toLowerCase().contains("empty"), "Cart should be empty after removing item.");
    }
}
