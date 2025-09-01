package GPT4.ws08.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
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

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#Logo img")));
        Assertions.assertTrue(logo.isDisplayed(), "Logo should be visible on home page");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jpetstore"), "Title should contain 'JPetStore'");
    }

    @Test
    @Order(2)
    public void testSignInWithValidCredentials() {
        driver.get(BASE_URL);
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("signonForm"));

        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        username.sendKeys("j2ee");
        password.sendKeys("j2ee");
        loginButton.click();

        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='WelcomeContent']")));
        Assertions.assertTrue(welcome.getText().contains("Welcome"), "Login should display welcome message");

        WebElement signOut = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOut.click();
    }

    @Test
    @Order(3)
    public void testSignInWithInvalidCredentials() {
        driver.get(BASE_URL + "signonForm");
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        username.sendKeys("invalidUser");
        password.sendKeys("wrongPass");
        loginButton.click();

        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@class='messages']/li")));
        Assertions.assertTrue(message.getText().toLowerCase().contains("invalid username or password"),
                "Should show invalid credentials message");
    }

    @Test
    @Order(4)
    public void testCategoryLinksNavigate() {
        driver.get(BASE_URL);
        List<WebElement> categories = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div#Content a[href*='catalog']")));
        Assertions.assertTrue(categories.size() > 0, "Category links should be present");

        WebElement firstCategory = categories.get(0);
        String href = firstCategory.getAttribute("href");
        firstCategory.click();
        wait.until(ExpectedConditions.urlContains(href.split("/")[href.split("/").length - 1]));
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2")));
        Assertions.assertTrue(heading.isDisplayed(), "Category heading should be visible after click");
    }

    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        List<WebElement> externalLinks = driver.findElements(By.cssSelector("div#PoweredBy a[target='_blank']"));
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;

            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
            wait.until(driver -> driver.getWindowHandles().size() > 1);

            Set<String> windowHandles = driver.getWindowHandles();
            for (String handle : windowHandles) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    wait.until(d -> d.getCurrentUrl().startsWith("http"));
                    String newUrl = driver.getCurrentUrl();
                    Assertions.assertTrue(newUrl.contains("aspectran.com") || newUrl.contains("github.com"),
                            "External link should lead to known domain");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("keyword")));
        searchInput.sendKeys("dog");
        searchInput.submit();

        wait.until(ExpectedConditions.urlContains("searchProducts"));
        WebElement productList = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#Catalog")));
        Assertions.assertTrue(productList.getText().toLowerCase().contains("dog"), "Search results should contain 'dog'");
    }

    @Test
    @Order(7)
    public void testCartAddAndRemoveItem() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01")));
        productLink.click();

        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("EST-1")));
        itemLink.click();

        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();

        WebElement cart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#Cart")));
        Assertions.assertTrue(cart.getText().contains("EST-1"), "Cart should contain the added item");

        WebElement removeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Remove")));
        removeLink.click();

        WebElement emptyMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='Cart']/p")));
        Assertions.assertTrue(emptyMessage.getText().toLowerCase().contains("empty"), "Cart should be empty after removing item");
    }

    @Test
    @Order(8)
    public void testHelpLink() {
        driver.get(BASE_URL);
        WebElement helpLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Help")));
        helpLink.click();
        wait.until(ExpectedConditions.urlContains("help"));
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#Content h2")));
        Assertions.assertTrue(heading.getText().toLowerCase().contains("help"), "Help page should load");
    }

}
