package GPT4.ws01.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    private void login(String username, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name"))).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();
    }

    private void logout() {
        openBurgerMenu();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void openBurgerMenu() {
        try {
            WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            menuButton.click();
        } catch (ElementClickInterceptedException e) {
            WebElement menuButton = driver.findElement(By.id("react-burger-menu-btn"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", menuButton);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventory_sidebar_link")));
    }

    private void closeBurgerMenu() {
        try {
            WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
            closeButton.click();
        } catch (ElementClickInterceptedException e) {
            WebElement closeButton = driver.findElement(By.id("react-burger-cross-btn"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", closeButton);
        }
    }

    private void resetAppState() {
        openBurgerMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        closeBurgerMenu();
    }

    private void switchToNewTabAndAssert(String expectedDomain) {
        String originalHandle = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "URL should contain expected domain: " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(1)
    public void loginPageElementsVisible() {
        driver.get(BASE_URL);
        Assertions.assertAll(
                () -> Assertions.assertTrue(driver.findElement(By.id("user-name")).isDisplayed(), "Username field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.id("password")).isDisplayed(), "Password field should be visible"),
                () -> Assertions.assertTrue(driver.findElement(By.id("login-button")).isDisplayed(), "Login button should be visible")
        );
    }

    @Test
    @Order(2)
    public void invalidLoginShowsError() {
        login("invalid_user", "wrong_password");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid login");
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Should remain on login page");
    }

    @Test
    @Order(3)
    public void validLoginNavigatesToInventory() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should navigate to inventory page");
    }

    @Test
    @Order(4)
    public void sortDropdownAffectsItemOrder() {
        login(USERNAME, PASSWORD);
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        Select select = new Select(dropdown);

        List<String> originalOrder = driver.findElements(By.className("inventory_item_name"))
                .stream().map(WebElement::getText).collect(Collectors.toList());

        select.selectByVisibleText("Name (Z to A)");
        wait.until(d -> {
            List<String> newOrder = driver.findElements(By.className("inventory_item_name"))
                    .stream().map(WebElement::getText).collect(Collectors.toList());
            return !newOrder.equals(originalOrder);
        });

        List<String> reversedOrder = driver.findElements(By.className("inventory_item_name"))
                .stream().map(WebElement::getText).collect(Collectors.toList());

        List<String> expectedOrder = new ArrayList<>(originalOrder);
        expectedOrder.sort(Comparator.reverseOrder());

        Assertions.assertEquals(expectedOrder, reversedOrder, "Items should be sorted in descending order");
    }

    @Test
    @Order(5)
    public void burgerMenuAllItemsNavigation() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            login(USERNAME, PASSWORD);
        }
        openBurgerMenu();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Should navigate to inventory page");
    }

    @Test
    @Order(6)
    public void burgerMenuAboutOpensExternal() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            login(USERNAME, PASSWORD);
        }
        openBurgerMenu();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        switchToNewTabAndAssert("saucelabs.com");
    }

    @Test
    @Order(7)
    public void resetAppStateClearsCart() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            login(USERNAME, PASSWORD);
        }
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn_inventory")));
        addToCart.click();
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Cart badge should show 1 item");
        resetAppState();
        List<WebElement> badges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart badge should be cleared after resetting app state");
    }

    @Test
    @Order(8)
    public void footerTwitterLinkOpensExternal() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            login(USERNAME, PASSWORD);
        }
        closeBurgerMenuIfOpen();
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter a")));
        twitter.click();
        switchToNewTabAndAssert("twitter.com");
    }

    @Test
    @Order(9)
    public void footerFacebookLinkOpensExternal() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            login(USERNAME, PASSWORD);
        }
        closeBurgerMenuIfOpen();
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook a")));
        facebook.click();
        switchToNewTabAndAssert("facebook.com");
    }

    @Test
    @Order(10)
    public void footerLinkedInLinkOpensExternal() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            login(USERNAME, PASSWORD);
        }
        closeBurgerMenuIfOpen();
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin a")));
        linkedin.click();
        switchToNewTabAndAssert("linkedin.com");
    }

    @Test
    @Order(11)
    public void logoutReturnsToLoginPage() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            login(USERNAME, PASSWORD);
        }
        logout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Should return to login page after logout");
    }

    private void closeBurgerMenuIfOpen() {
        List<WebElement> menuWrappers = driver.findElements(By.className("bm-menu-wrap"));
        if (!menuWrappers.isEmpty() && menuWrappers.get(0).getAttribute("aria-hidden").equals("false")) {
            closeBurgerMenu();
        }
    }
}