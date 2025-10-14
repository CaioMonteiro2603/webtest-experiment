package GPT20b.ws03.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugbankTests {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

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

    /* ---------- Helper Methods ---------- */

    private void navigateToLoginPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
    }

    private void performLogin(String user, String pass) {
        WebElement userEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passEl = driver.findElement(By.id("password"));
        WebElement loginEl = driver.findElement(By.id("login-button"));

        userEl.clear();
        userEl.sendKeys(user);
        passEl.clear();
        passEl.sendKeys(pass);
        loginEl.click();
    }

    private void ensureLoggedIn() {
        if (!isLoggedIn()) {
            navigateToLoginPage();
            performLogin(USERNAME, PASSWORD);
            Assertions.assertTrue(isLoggedIn(), "Failed to log in with valid credentials.");
        }
    }

    private boolean isLoggedIn() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_item")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void logout() {
        try {
            WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            burger.click();
            WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
            logout.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
        } catch (NoSuchElementException | TimeoutException ignored) {
        }
    }

    private void resetAppState() {
        try {
            WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            burger.click();
            WebElement reset = wait.until(ExpectedConditions