package SunaQwen3.ws08.seq07;

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
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("JPetStore Demo", driver.getTitle(), "Page title should be 'JPetStore Demo'");

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Enter the Store")));
        signInLink.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));

        WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInButton.click();

        wait.until(ExpectedConditions.urlContains("/actions/Account.action?view=signon"));

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should be redirected to catalog after login");
        assertTrue(driver.getPageSource().contains("Fish"), "Catalog should contain Fish category");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL + "actions/Account.action?view=signon");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.name("signon"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("messages")));
        String errorMessage = driver.findElement(By.className("messages")).getText();
        assertTrue(errorMessage.contains("Invalid username or password"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testBrowseCategoriesAndProducts() {
        driver.get(BASE_URL + "actions/Catalog.action");

        List<WebElement> categoryLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a[href*='categoryId']")));
        assertFalse(categoryLinks.isEmpty(), "At least one category should be present");

        for (WebElement link : categoryLinks) {
            link.click();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h3")));
            assertTrue(driver.getPageSource().contains("Product ID"), "Product list should be displayed");
            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a[href*='categoryId']")));
        }
    }

    @Test
    @Order(4)
    void testProductDetailAndAddToCart() {
        driver.get(BASE_URL + "actions/Catalog.action");
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        assertTrue(driver.getPageSource().contains("Angelfish"), "Product detail should show Angelfish");

        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCartButton.click();

        wait.until(ExpectedConditions.urlContains("viewItem"));
        WebElement updateCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.name("updateCartQuantities")));
        assertTrue(updateCartButton.isDisplayed(), "Should be on cart page with update button");

        WebElement cartBadge = driver.findElement(By.cssSelector("a[href='viewCart.do'] > img + span"));
        assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");
    }

    @Test
    @Order(5)
    void testRemoveFromCart() {
        driver.get(BASE_URL + "actions/Cart.action");

        List<WebElement> removeCheckboxes = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.name("removeItems")));
        if (!removeCheckboxes.isEmpty()) {
            removeCheckboxes.get(0).click();
            driver.findElement(By.name("updateCartQuantities")).click();

            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Your cart is empty."));
            assertTrue(driver.getPageSource().contains("Your cart is empty."), "Cart should be empty after removal");
        }
    }

    @Test
    @Order(6)
    void testMenuNavigationAndResetAppState() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src='/images/menu.gif']")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src='/images/menu.gif']")));
        menuButton.click();

        WebElement resetAppStateLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetAppStateLink.click();

        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        assertTrue(driver.getCurrentUrl().contains("/actions/Catalog.action"), "Should remain in catalog after reset");
    }

    @Test
    @Order(7)
    void testExternalLinksInFooter() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.footer a")));

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("div.footer a"));
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            String target = link.getAttribute("target");

            if (target != null && target.equals("_blank")) {
                String originalWindow = driver.getWindowHandle();
                link.click();

                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
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
    void testLogout() {
        driver.get(BASE_URL + "actions/Catalog.action");

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("img[src='/images/menu.gif']")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("/actions/Catalog.action"));
        assertTrue(driver.getPageSource().contains("You have logged out."), "Logout confirmation message should appear");
    }
}
