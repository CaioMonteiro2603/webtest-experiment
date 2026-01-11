package SunaGPT20b.ws10.seq02;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String VALID_USERNAME = "superadmin@brasilagritest.com.br";
    private static final String VALID_PASSWORD = "10203040";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private static void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        userField.clear();
        userField.sendKeys(username);
        WebElement passField = driver.findElement(By.id("password"));
        passField.clear();
        passField.sendKeys(password);
        WebElement loginBtn = driver.findElement(By.xpath("//button[@type='submit']"));
        loginBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'inventory')]")));
    }

    private static void openMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class,'menu')]")));
        menuBtn.click();
    }

    private static void resetAppState() {
        openMenu();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Reset')]")));
        resetLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'inventory')]")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(VALID_USERNAME, VALID_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "After login, URL should contain /inventory");
        List<WebElement> items = driver.findElements(By.className("inventory_item"));
        Assertions.assertFalse(items.isEmpty(),
                "Inventory items should be displayed after successful login");
        resetAppState();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        userField.clear();
        userField.sendKeys("invalid_user");
        WebElement passField = driver.findElement(By.id("password"));
        passField.clear();
        passField.sendKeys("wrong_password");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'error')]")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(VALID_USERNAME, VALID_PASSWORD);
        WebElement sortDropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//select[contains(@class,'sort')]")));
        Select select = new Select(sortDropdown);
        List<WebElement> options = select.getOptions();
        Assertions.assertTrue(options.size() > 1, "Sorting dropdown should contain multiple options");

        String previousFirstItem = "";
        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());
            WebElement firstItem = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.className("inventory_item_name")));
            String firstItemName = firstItem.getText();
            Assertions.assertNotEquals(previousFirstItem, firstItemName,
                    "First item should change after selecting sort option: " + option.getText());
            previousFirstItem = firstItemName;
        }
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(VALID_USERNAME, VALID_PASSWORD);
        openMenu();
        WebElement allItems = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'All')]")));
        allItems.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class,'inventory')]")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                "Should navigate to inventory page after clicking All Items");
        resetAppState();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternal() {
        login(VALID_USERNAME, VALID_PASSWORD);
        openMenu();
        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'About')]")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        for (String win : windows) {
            if (!win.equals(originalWindow)) {
                driver.switchTo().window(win);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com") ||
                        driver.getCurrentUrl().contains("about"),
                "External About page URL should contain expected domain");
        driver.close();
        driver.switchTo().window(originalWindow);
        resetAppState();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(VALID_USERNAME, VALID_PASSWORD);
        openMenu();
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Logout')]")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@type='submit']")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "After logout, user should be redirected to the login page");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login(VALID_USERNAME, VALID_PASSWORD);
        WebElement addToCartBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class,'add-to-cart')]")));
        addToCartBtn.click();

        WebElement cartBadge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(),
                "Cart badge should display 1 after adding a product");

        resetAppState();

        List<WebElement> badges = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertTrue(badges.isEmpty(),
                "Cart badge should be cleared after resetting app state");
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login(VALID_USERNAME, VALID_PASSWORD);
        Map<String, String> socialLinks = new LinkedHashMap<>();
        socialLinks.put("social_twitter", "twitter.com");
        socialLinks.put("social_facebook", "facebook.com");
        socialLinks.put("social_linkedin", "linkedin.com");

        for (Map.Entry<String, String> entry : socialLinks.entrySet()) {
            String className = entry.getKey();
            String expectedDomain = entry.getValue();

            WebElement link = wait.until(
                    ExpectedConditions.elementToBeClickable(By.className(className)));
            String originalWindow = driver.getWindowHandle();
            link.click();

            wait.until(driver -> driver.getWindowHandles().size() > 1);
            for (String win : driver.getWindowHandles()) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    break;
                }
            }

            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "Social link should open a page containing " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
        resetAppState();
    }
}