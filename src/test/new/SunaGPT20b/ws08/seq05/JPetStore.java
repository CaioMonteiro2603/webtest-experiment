package SunaGPT20b.ws08.seq05;

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
public class JPetStore {
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
            By.cssSelector("#SidebarContent a")));
        Assertions.assertTrue(categories.size() > 0);

        for (int i = 0; i < categories.size(); i++) {
            WebElement category = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("#SidebarContent a"))).get(i);
            String href = category.getAttribute("href");
            category.click();
            wait.until(ExpectedConditions.urlContains(href.substring(href.lastIndexOf("/") + 1)));
            Assertions.assertTrue(driver.getCurrentUrl().contains(href.substring(href.lastIndexOf("/") + 1)));
            driver.navigate().back();
        }
    }

    @Test
    @Order(3)
    public void testProductNavigation() {
        driver.get(BASE_URL + "catalog/categories/FISH");
        List<WebElement> products = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("#Catalog a")));
        Assertions.assertTrue(products.size() > 0);

        for (int i = 0; i < products.size(); i++) {
            WebElement product = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("#Catalog a"))).get(i);
            String href = product.getAttribute("href");
            product.click();
            wait.until(ExpectedConditions.urlContains(href.substring(href.lastIndexOf("/") + 1)));
            Assertions.assertTrue(driver.getCurrentUrl().contains(href.substring(href.lastIndexOf("/") + 1)));
            driver.navigate().back();
        }
    }

    @Test
    @Order(4)
    public void testLogin() {
        driver.get(BASE_URL);
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Sign In")));
        signIn.click();

        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input[type='submit']"));

        username.clear();
        username.sendKeys(USERNAME);
        password.clear();
        password.sendKeys(PASSWORD);
        loginBtn.click();

        WebElement welcomeMsg = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#WelcomeContent")));
        Assertions.assertTrue(welcomeMsg.getText().contains(USERNAME));
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "account/signonForm");
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input[type='submit']"));

        username.clear();
        username.sendKeys("invalid");
        password.clear();
        password.sendKeys("invalid");
        loginBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#Content ul li")));
        Assertions.assertTrue(errorMsg.getText().contains("Invalid username or password"));
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String mainWindow = driver.getWindowHandle();

        // Test footer links
        List<WebElement> footerLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("footer a")));
        Assertions.assertTrue(footerLinks.size() > 0);

        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href.contains("facebook.com") || href.contains("twitter.com") || href.contains("linkedin.com")) {
                link.click();
                
                // Switch to new window
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(mainWindow)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                
                Assertions.assertTrue(driver.getCurrentUrl().contains(href.substring(href.indexOf("//") + 2, href.indexOf(".com") + 4)));
                driver.close();
                driver.switchTo().window(mainWindow);
            }
        }
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("keyword")));
        WebElement searchBtn = driver.findElement(By.cssSelector("input[type='submit']"));

        searchInput.clear();
        searchInput.sendKeys("fish");
        searchBtn.click();

        List<WebElement> results = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector("#Catalog a")));
        Assertions.assertTrue(results.size() > 0);
    }

    @Test
    @Order(8)
    public void testShoppingCart() {
        // Login first
        driver.get(BASE_URL + "account/signonForm");
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input[type='submit']"));

        username.clear();
        username.sendKeys(USERNAME);
        password.clear();
        password.sendKeys(PASSWORD);
        loginBtn.click();

        // Add item to cart
        driver.get(BASE_URL + "catalog/products/FI-FW-01");
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.linkText("Add to Cart")));
        addToCart.click();

        WebElement cartItem = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#Cart tbody tr")));
        Assertions.assertTrue(cartItem.getText().contains("Angelfish"));

        // Remove item from cart
        WebElement removeBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.name("remove")));
        removeBtn.click();

        WebElement emptyCart = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("#Cart")));
        Assertions.assertTrue(emptyCart.getText().contains("Your cart is empty"));
    }
}