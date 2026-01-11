package GPT4.ws01.seq10;

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
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";

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

    private void login() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.clear();
        username.sendKeys("standard_user");
        password.clear();
        password.sendKeys("secret_sauce");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
    }

    private void resetAppState() {
        try {
            WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            menuButton.click();
            WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
            resetLink.click();
            try {
                WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
                closeButton.click();
            } catch (Exception e) {
                menuButton.click();
            }
        } catch (Exception e) {
            // If menu is open, try to close it
            try {
                WebElement closeButton = driver.findElement(By.id("react-burger-cross-btn"));
                if (closeButton.isDisplayed()) {
                    closeButton.click();
                }
            } catch (Exception ex) {
                // Ignore if can't close
            }
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("inventory"), "Login failed: Inventory page not loaded.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.clear();
        username.sendKeys("invalid_user");
        password.clear();
        password.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message not shown for invalid credentials.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        
        // Find the sort dropdown and click to open it
        WebElement sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sortSelect);
        sortSelect.click();
        
        // Select A-Z option
        WebElement azOption = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("option[value='az']")));
        azOption.click();
        String firstItemAZ = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_item_name"))).getText();
        Thread.sleep(1000); // Small wait for DOM update
        
        // Click dropdown again and select Z-A
        sortSelect = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortSelect.click();
        WebElement zaOption = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("option[value='za']")));
        zaOption.click();
        String firstItemZA = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("inventory_item_name"))).getText();

        Assertions.assertNotEquals(firstItemAZ, firstItemZA, "Sorting did not affect item order.");
        resetAppState();
    }

    @Test
    @Order(4)
    public void testBurgerMenuAllItems() {
        login();
        try {
            WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuButton);
        } catch (Exception e) {
            // Menu might be open, try clicking button
            WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
            closeButton.click();
            WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuButton);
        }
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();

        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "All Items did not navigate to inventory.");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testBurgerMenuAboutExternalLink() {
        login();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuButton);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));

        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(driver -> {
            try {
                return driver.getWindowHandles().size() > 1;
            } catch (Exception e) {
                return false;
            }
        });
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        String externalUrl = driver.getCurrentUrl();
        Assertions.assertTrue(externalUrl.contains("saucelabs.com") || externalUrl.contains("sauce.io"), "About link did not go to expected domain.");

        driver.close();
        driver.switchTo().window(originalWindow);
        resetAppState();
    }

    @Test
    @Order(6)
    public void testBurgerMenuLogout() {
        login();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuButton);
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();

        wait.until(driver -> {
            String url = driver.getCurrentUrl();
            return url.contains("index.html") || url.equals("https://www.saucedemo.com/") || url.equals("https://www.saucedemo.com");
        });
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("index.html") || currentUrl.equals("https://www.saucedemo.com/") || currentUrl.equals("https://www.saucedemo.com"), "Logout did not redirect to login page.");
    }

    @Test
    @Order(7)
    public void testAddToCartAndReset() {
        login();
        List<WebElement> addButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".btn_inventory")));
        int itemsToAdd = Math.min(2, addButtons.size());

        for (int i = 0; i < itemsToAdd; i++) {
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addButtons.get(i)));
            addBtn.click();
        }

        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals(String.valueOf(itemsToAdd), cartBadge.getText(), "Cart count mismatch.");

        resetAppState();
        List<WebElement> badges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(), "Cart not cleared after reset.");
    }

    @Test
    @Order(8)
    public void testExternalSocialLinks() {
        login();
        String[] selectors = {
            "footer .social_twitter a",
            "footer .social_facebook a",
            "footer .social_linkedin a"
        };
        String[] expectedDomains = {
            "twitter.com",
            "facebook.com",
            "linkedin.com"
        };

        for (int i = 0; i < selectors.length; i++) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selectors[i])));
            String originalWindow = driver.getWindowHandle();
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);

            wait.until(driver -> {
                try {
                    return driver.getWindowHandles().size() > 1;
                } catch (Exception e) {
                    return false;
                }
            });
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);

            String newUrl = driver.getCurrentUrl();
            boolean containsDomain = newUrl.contains(expectedDomains[i]) || 
                (expectedDomains[i].equals("twitter.com") && newUrl.contains("x.com")) ||
                (expectedDomains[i].equals("facebook.com") && newUrl.contains("meta.com")) ||
                (expectedDomains[i].equals("linkedin.com") && newUrl.contains("linkedin"));
            Assertions.assertTrue(containsDomain, "External link did not open expected domain.");

            driver.close();
            driver.switchTo().window(originalWindow);
        }

        resetAppState();
    }
}