package Qwen3.ws01.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;

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
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("standard_user");
        passwordField.sendKeys("secret_sauce");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));
        assertTrue(driver.getCurrentUrl().contains("/inventory.html"));
        assertEquals("Swag Labs", driver.getTitle());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://www.saucedemo.com/v1/index.html");

        WebElement usernameField = driver.findElement(By.id("user-name"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test=\"error\"]")));
        assertTrue(errorElement.isDisplayed());
        assertTrue(errorElement.getText().contains("Epic sadface:"));
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("standard_user");
        passwordField.sendKeys("secret_sauce");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));

        WebElement sortDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product_sort_container")));
        Select select = new Select(sortDropdown);
        select.selectByValue("az");

        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        String[] itemNames = items.stream().map(WebElement::getText).toArray(String[]::new);

        assertArrayEquals(new String[]{"Test.allTheThings() T-Shirt (Red)", "Sauce Labs Backpack", "Sauce Labs Bike Light", "Sauce Labs Bolt T-Shirt", "Sauce Labs Fleece Jacket", "Sauce Labs Onesie"}, itemNames);

        select.selectByValue("za");
        items = driver.findElements(By.cssSelector(".inventory_item_name"));
        itemNames = items.stream().map(WebElement::getText).toArray(String[]::new);

        assertArrayEquals(new String[]{"Sauce Labs Onesie", "Sauce Labs Fleece Jacket", "Sauce Labs Bolt T-Shirt", "Sauce Labs Bike Light", "Sauce Labs Backpack", "Test.allTheThings() T-Shirt (Red)"}, itemNames);

        select.selectByValue("lohi");
        items = driver.findElements(By.cssSelector(".inventory_item_price"));
        String[] itemPrices = items.stream().map(WebElement::getText).toArray(String[]::new);

        assertArrayEquals(new String[]{"$7.99", "$9.99", "$15.99", "$15.99", "$29.99", "$49.99"}, itemPrices);

        select.selectByValue("hilo");
        items = driver.findElements(By.cssSelector(".inventory_item_price"));
        itemPrices = items.stream().map(WebElement::getText).toArray(String[]::new);

        assertArrayEquals(new String[]{"$49.99", "$29.99", "$15.99", "$15.99", "$9.99", "$7.99"}, itemPrices);
    }

    @Test
    @Order(4)
    public void testBurgerMenuActions() {
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("standard_user");
        passwordField.sendKeys("secret_sauce");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='bm-burger-button']")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-test='inventory-sidebar-link']")));
        allItemsLink.click();
        assertEquals("https://www.saucedemo.com/v1/inventory.html", driver.getCurrentUrl());

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='bm-burger-button']")));
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-test='about-sidebar-link']")));
        aboutLink.click();

        Set<String> windowHandles = driver.getWindowHandles();
        String originalWindow = driver.getWindowHandle();
        for (String handle : windowHandles) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalWindow);

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='bm-burger-button']")));
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-test='logout-sidebar-link']")));
        logoutLink.click();
        assertTrue(driver.getCurrentUrl().contains("/index.html"));

        driver.get("https://www.saucedemo.com/v1/index.html");
        usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        passwordField = driver.findElement(By.id("password"));
        loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("standard_user");
        passwordField.sendKeys("secret_sauce");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test='bm-burger-button']")));
        menuButton.click();
        WebElement resetAppLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-test='reset-sidebar-link']")));
        resetAppLink.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get("https://www.saucedemo.com/v1/index.html");
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("standard_user");
        passwordField.sendKeys("secret_sauce");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/inventory.html"));

        WebElement footer = driver.findElement(By.tagName("footer"));

        List<WebElement> links = footer.findElements(By.tagName("a"));
        assertEquals(3, links.size());

        String originalWindow = driver.getWindowHandle();

        for (int i = 0; i < links.size(); i++) {
            WebElement link = links.get(i);
            String href = link.getAttribute("href");
            assertNotNull(href);
            link.click();

            Set<String> windowHandles = driver.getWindowHandles();
            String newWindow = windowHandles.stream()
                    .filter(w -> !w.equals(originalWindow))
                    .findFirst()
                    .orElse(null);

            if (newWindow != null) {
                driver.switchTo().window(newWindow);
                String currentUrl = driver.getCurrentUrl();
                switch (i) {
                    case 0:
                        assertTrue(currentUrl.contains("twitter.com"));
                        break;
                    case 1:
                        assertTrue(currentUrl.contains("facebook.com"));
                        break;
                    case 2:
                        assertTrue(currentUrl.contains("linkedin.com"));
                        break;
                }
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }
}