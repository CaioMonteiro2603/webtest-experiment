package SunaQwen3.ws08.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

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
    public void testValidLogin() {
        driver.get(BASE_URL);
        Assertions.assertEquals("JPetStore Demo", driver.getTitle(), "Page title should be 'JPetStore Demo'");

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='Welcome ABC!']")));
        Assertions.assertTrue(welcomeText.isDisplayed(), "Welcome message should be displayed after login");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/main.shtml"), "URL should contain '/catalog/main.shtml' after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?signonForm=");
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_pass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("font[color='red']")));
        Assertions.assertEquals("Invalid username or password. Signon failed.", errorMessage.getText(),
                "Error message should appear for invalid credentials");
    }

    @Test
    @Order(3)
    public void testBrowseCatsCategory() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.name("signon")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='Welcome ABC!']")));

        WebElement catsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Cats")));
        catsLink.click();

        WebElement categoryTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[text()='Cats']")));
        Assertions.assertTrue(categoryTitle.isDisplayed(), "Cats category page should be displayed");
        Assertions.assertTrue(driver.getCurrentUrl().contains("categoryId=CATS"), "URL should contain categoryId=CATS");
    }

    @Test
    @Order(4)
    public void testViewProductDetail() {
        driver.get(BASE_URL + "actions/Catalog.action?viewCategory=&categoryId=CATS");
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FL-DSH-01")));
        productLink.click();

        WebElement productTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[text()='Name']")));
        WebElement productName = driver.findElement(By.xpath("//td[text()='Manx']"));
        Assertions.assertTrue(productTitle.isDisplayed(), "Product detail table should be visible");
        Assertions.assertTrue(productName.isDisplayed(), "Product name 'Manx' should be displayed");
    }

    @Test
    @Order(5)
    public void testAddToCartAndCheckout() {
        driver.get(BASE_URL + "actions/Catalog.action?viewProduct=&productId=FI-SW-01");
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();

        WebElement updateCart = wait.until(ExpectedConditions.elementToBeClickable(By.name("updateCartQuantities")));
        Assertions.assertTrue(updateCart.isDisplayed(), "Cart page should be displayed with update button");

        WebElement checkoutBtn = driver.findElement(By.xpath("//a[text()='Proceed to Checkout']"));
        checkoutBtn.click();

        WebElement billingHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='Billing Address Information']")));
        Assertions.assertTrue(billingHeader.isDisplayed(), "Billing address form should be displayed");
    }

    @Test
    @Order(6)
    public void testMenuNavigationAndResetAppState() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.name("signon")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[text()='Welcome ABC!']")));

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src='/images/menu.gif']")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/main.shtml"), "Should navigate to All Items page");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src='/images/menu.gif']")));
        menuButton.click();

        WebElement resetAppState = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetAppState.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog/main.shtml"), "URL should remain on main catalog after reset");
    }

    @Test
    @Order(7)
    public void testExternalFooterLinks() {
        driver.get(BASE_URL);
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("div.footer a"));

        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            String linkText = link.getText();

            if (linkText.contains("Facebook")) {
                assertExternalLinkInNewTab(href, "facebook.com");
            } else if (linkText.contains("Twitter")) {
                assertExternalLinkInNewTab(href, "twitter.com");
            } else if (linkText.contains("LinkedIn")) {
                assertExternalLinkInNewTab(href, "linkedin.com");
            }
        }
    }

    private void assertExternalLinkInNewTab(String url, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open('" + url + "','_blank');");
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains(expectedDomain),
                "External link should navigate to domain containing: " + expectedDomain);

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}