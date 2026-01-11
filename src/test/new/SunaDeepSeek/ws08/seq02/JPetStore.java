package SunaDeepSeek.ws08.seq02;

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JPetStore Demo"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("jpetstore.aspectran.com"));
    }

    @Test
    @Order(2)
    public void testNavigationToCategories() {
        driver.get(BASE_URL);
        List<WebElement> categories = wait.until(ExpectedConditions
                .presenceOfAllElementsLocatedBy(By.cssSelector("#SidebarContent a")));
        
        for (WebElement category : categories) {
            String categoryName = category.getText();
            category.click();
            wait.until(ExpectedConditions.urlContains("categories/"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("categories/"));
            Assertions.assertTrue(driver.getTitle().contains(categoryName));
            driver.navigate().back();
        }
    }

    @Test
    @Order(3)
    public void testProductPages() {
        driver.get(BASE_URL + "categories/FISH");
        List<WebElement> products = wait.until(ExpectedConditions
                .presenceOfAllElementsLocatedBy(By.cssSelector("table.table-splash a")));
        
        for (int i = 0; i < Math.min(3, products.size()); i++) {
            String productName = products.get(i).getText();
            products.get(i).click();
            wait.until(ExpectedConditions.urlContains("products/"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("products/"));
            Assertions.assertTrue(driver.getPageSource().contains(productName));
            driver.navigate().back();
            products = wait.until(ExpectedConditions
                    .presenceOfAllElementsLocatedBy(By.cssSelector("table.table-splash a")));
        }
    }

    @Test
    @Order(4)
    public void testLogin() {
        driver.get(BASE_URL + "login");
        WebElement username = wait.until(ExpectedConditions
                .elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.xpath("//button[@type='submit']"));

        username.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("accounts/edit"));
        Assertions.assertTrue(driver.getPageSource().contains("My Account"));
    }

    @Test
    @Order(5)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        WebElement username = wait.until(ExpectedConditions
                .elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.xpath("//button[@type='submit']"));

        username.sendKeys("invalid");
        password.sendKeys("invalid");
        loginBtn.click();

        WebElement error = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.cssSelector(".alert-danger")));
        Assertions.assertTrue(error.getText().contains("Invalid"));
    }

    @Test
    @Order(6)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> socialLinks = wait.until(ExpectedConditions
                .presenceOfAllElementsLocatedBy(By.cssSelector("footer a")));
        
        for (WebElement link : socialLinks) {
            String originalWindow = driver.getWindowHandle();
            link.click();
            
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    Assertions.assertNotEquals(BASE_URL, driver.getCurrentUrl());
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    break;
                }
            }
        }
    }

    @Test
    @Order(7)
    public void testSearchFunctionality() {
        driver.get(BASE_URL);
        WebElement searchInput = wait.until(ExpectedConditions
                .elementToBeClickable(By.name("keyword")));
        WebElement searchBtn = driver.findElement(By.xpath("//button[@type='submit']"));

        searchInput.sendKeys("fish");
        searchBtn.click();

        wait.until(ExpectedConditions.urlContains("search"));
        List<WebElement> results = driver.findElements(By.cssSelector("table.table-splash tr"));
        Assertions.assertTrue(results.size() > 0);
    }

    @Test
    @Order(8)
    public void testAddToCart() {
        driver.get(BASE_URL + "login");
        WebElement username = wait.until(ExpectedConditions
                .elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.xpath("//button[@type='submit']"));

        username.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        loginBtn.click();

        driver.get(BASE_URL + "products/FI-SW-01");
        WebElement addToCartBtn = wait.until(ExpectedConditions
                .elementToBeClickable(By.linkText("Add to Cart")));
        addToCartBtn.click();

        wait.until(ExpectedConditions.urlContains("cart"));
        WebElement cartItem = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.cssSelector("td[headers='desc']")));
        Assertions.assertTrue(cartItem.getText().contains("Angelfish"));
    }
}