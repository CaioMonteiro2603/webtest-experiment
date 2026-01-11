package SunaQwen3.ws08.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals("JPetStore Demo", driver.getTitle());

        WebElement enterStoreLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Enter the Store')]")));
        enterStoreLink.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));

        WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Sign In')]")));
        signInButton.click();

        wait.until(ExpectedConditions.urlContains("/actions/Account.action?event=signonForm"));

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='username']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@name='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@name='signon']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should be redirected to catalog after login");
        assertTrue(driver.getPageSource().contains("Fish"), "Catalog should display categories like Fish");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?event=signonForm");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='username']")));
        WebElement passwordField = driver.findElement(By.xpath("//input[@name='password']"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@name='signon']"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();

        String alertText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'alert') and contains(@class,'danger')]"))).getText();
        assertTrue(alertText.contains("Invalid username or password"), "Error message should appear for invalid login");
    }

    @Test
    @Order(3)
    public void testCategoryNavigation() {
        driver.get(BASE_URL + "actions/Catalog.action");

        List<WebElement> categoryLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//a[contains(@href,'viewCategory')]")));
        assertFalse(categoryLinks.isEmpty(), "At least one category should be present");

        for (WebElement link : categoryLinks) {
            String categoryName = link.getText();
            String href = link.getAttribute("href");

            link.click();
            wait.until(ExpectedConditions.urlContains(href));

            assertTrue(driver.getPageSource().contains(categoryName), "Page should contain category name: " + categoryName);

            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        }
    }

    @Test
    @Order(4)
    public void testProductDetailAndAddToCart() {
        driver.get(BASE_URL + "actions/Catalog.action");

        List<WebElement> productLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//a[contains(@href,'viewProduct')]")));
        if (!productLinks.isEmpty()) {
            productLinks.get(0).click();
            wait.until(ExpectedConditions.urlContains("/actions/Catalog.action?viewProduct"));

            WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Add to Cart')]")));
            addToCartButton.click();

            wait.until(ExpectedConditions.urlContains("/actions/Cart.action"));
            assertTrue(driver.getCurrentUrl().contains("/actions/Cart.action"), "Should be redirected to cart");

            WebElement cartItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(@class,'product')]")));
            assertTrue(cartItem.getText().length() > 0, "Cart should contain the added product");
        }
    }

    @Test
    @Order(5)
    public void testCartUpdateAndCheckout() {
        driver.get(BASE_URL + "actions/Cart.action");

        if (driver.findElements(By.xpath("//td[contains(@class,'product')]")).size() == 0) {
            // Ensure cart has an item
            List<WebElement> productLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//a[contains(@href,'viewProduct')]")));
            if (!productLinks.isEmpty()) {
                productLinks.get(0).click();
                wait.until(ExpectedConditions.urlContains("/actions/Catalog.action?viewProduct"));

                WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Add to Cart')]")));
                addToCartButton.click();
                wait.until(ExpectedConditions.urlContains("/actions/Cart.action"));
            }
        }

        if (driver.findElements(By.xpath("//input[contains(@name,'quantity')]")).size() > 0) {
            WebElement quantityInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[contains(@name,'quantity')]")));
            quantityInput.clear();
            quantityInput.sendKeys("2");

            WebElement updateButton = driver.findElement(By.xpath("//input[contains(@value,'Update')]"));
            updateButton.click();

            WebElement proceedToCheckout = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Proceed to Checkout')]")));
            proceedToCheckout.click();

            wait.until(ExpectedConditions.urlContains("/actions/Order.action"));
            assertTrue(driver.getCurrentUrl().contains("/actions/Order.action"), "Should be on checkout page");
        }
    }

    @Test
    @Order(6)
    public void testMenuNavigationAndResetAppState() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'All Items')]")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'About')]")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("github.com"), "About link should open GitHub page");
        driver.close();
        driver.switchTo().window(originalWindow);

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Reset App State')]")));
        resetLink.click();

        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getPageSource().contains("Fish"), "Catalog should reload after reset");
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);

        List<WebElement> socialLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("footer a")));
        assertFalse(socialLinks.isEmpty(), "Footer should contain social links");

        String originalWindow = driver.getWindowHandle();

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");

            if (href != null && (href.contains("twitter.com") || href.contains("facebook.com") || href.contains("linkedin.com"))) {
                link.click();

                wait.until(d -> d.getWindowHandles().size() > 1);
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(originalWindow)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }

                if (href.contains("twitter.com")) {
                    assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open correct domain");
                } else if (href.contains("facebook.com")) {
                    assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open correct domain");
                } else if (href.contains("linkedin.com")) {
                    assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open correct domain");
                }

                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(8)
    public void testLogout() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Sign Out')]")));
        signOutLink.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should return to catalog after logout");
        List<WebElement> signInElements = driver.findElements(By.xpath("//a[contains(text(),'Sign In')]"));
        assertEquals(1, signInElements.size(), "Sign In link should be visible after logout");
    }
}