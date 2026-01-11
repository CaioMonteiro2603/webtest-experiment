package GPT4.ws01.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameInput.clear();
        usernameInput.sendKeys(username);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        loginButton.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "Login failed: Not redirected to inventory page");
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0, "No inventory items found after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "invalid_pass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Expected error message for invalid login not displayed");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        login(USERNAME, PASSWORD);
        
        Select sortSelect = new Select(wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container"))));
        sortSelect.selectByValue("az");
        List<String> azNames = getItemNames();

        sortSelect.selectByValue("za");
        List<String> zaNames = getItemNames();

        List<String> reversed = new ArrayList<>(azNames);
        Collections.reverse(reversed);

        Assertions.assertEquals(reversed, zaNames, "Z-A sorting did not reverse A-Z order");

        sortSelect.selectByValue("lohi");
        List<Double> lohiPrices = getItemPrices();

        sortSelect.selectByValue("hilo");
        List<Double> hiloPrices = getItemPrices();

        List<Double> sortedLowHigh = new ArrayList<>(lohiPrices);
        List<Double> sortedHighLow = new ArrayList<>(lohiPrices);
        Collections.sort(sortedLowHigh);
        sortedHighLow.sort(Collections.reverseOrder());

        Assertions.assertEquals(sortedLowHigh, lohiPrices, "Low to High price sorting incorrect");
        Assertions.assertEquals(sortedHighLow, hiloPrices, "High to Low price sorting incorrect");
    }

    private List<String> getItemNames() {
        List<WebElement> items = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("inventory_item_name")));
        List<String> names = new ArrayList<>();
        for (WebElement item : items) {
            names.add(item.getText());
        }
        return names;
    }

    private List<Double> getItemPrices() {
        List<WebElement> prices = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("inventory_item_price")));
        List<Double> values = new ArrayList<>();
        for (WebElement price : prices) {
            values.add(Double.parseDouble(price.getText().replace("$", "")));
        }
        return values;
    }

    @Test
    @Order(4)
    public void testBurgerMenuActions() {
        login(USERNAME, PASSWORD);
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        
        // Check if menu is already open
        try {
            if (driver.findElement(By.className("bm-menu")).isDisplayed()) {
                // Menu is open, close it first
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].click();", menuButton);
                Thread.sleep(500);
            }
        } catch (Exception e) {
            // Menu not found, continue
        }
        
        menuButton.click();

        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        Assertions.assertTrue(allItems.isDisplayed(), "All Items link not visible");
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "All Items link did not redirect to inventory");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String originalWindow = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About link did not open SauceLabs page");
                driver.close();
                break;
            }
        }
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testLogout() {
        login(USERNAME, PASSWORD);
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe("https://www.saucedemo.com/v1/index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), "Logout did not return to login page");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        login(USERNAME, PASSWORD);
        List<String> expectedDomains = List.of("x.com", "facebook.com", "linkedin.com");

        List<WebElement> socialLinks = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".social a")));
        String originalWindow = driver.getWindowHandle();

        for (int i = 0; i < socialLinks.size(); i++) {
            WebElement link = socialLinks.get(i);
            String expectedDomain = expectedDomains.get(i);
            link.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Expected domain not found: " + expectedDomain);
                    driver.close();
                    break;
                }
            }
            driver.switchTo().window(originalWindow);
        }
    }
}