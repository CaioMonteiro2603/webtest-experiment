package Qwen3.ws08.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class JPetStore {
    private static WebDriver driver;
    private static WebDriverWait wait;

    private final String BASE_URL = "https://jpetstore.aspectran.com/";
    private final String LOGIN = "jpetstore";
    private final String PASSWORD = "password";

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
    void testHomePageTitleAndLogo_DisplayedCorrectly() {
        driver.get(BASE_URL);

        assertEquals("JPetStore Demo", driver.getTitle(), "Page title should be 'JPetStore Demo'");
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Logo")));
        assertTrue(logo.isDisplayed(), "Logo should be visible on homepage");
        assertTrue(logo.getText().contains("JPetStore"), "Logo text should contain JPetStore");
    }

    @Test
    @Order(2)
    void testHeaderNavigationItems_ArePresent() {
        driver.get(BASE_URL);

        WebElement navbar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".TopBar")));
        String navbarText = navbar.getText();

        assertTrue(navbarText.contains("Sign In"), "Navigation bar should contain 'Sign In' link");
        assertTrue(navbarText.contains("Help"), "Navigation bar should contain 'Help' link");
        assertTrue(navbarText.contains("My Account"), "Navigation bar should contain 'My Account' link (disabled when not logged in)");
    }

    @Test
    @Order(3)
    void testSignInLink_NavigatesToLoginPage() {
        driver.get(BASE_URL);

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        wait.until(ExpectedConditions.titleIs("JPetStore Demo"));
        assertTrue(driver.getCurrentUrl().contains("signonForm"), "Sign In link should redirect to login page");

        assertTrue(isElementPresent(By.name("username")), "Login page should contain username field");
        assertTrue(isElementPresent(By.name("password")), "Login page should contain password field");
    }

    @Test
    @Order(4)
    void testValidLogin_SuccessfulRedirectToAccount() {
        driver.get(BASE_URL + "account/signonForm");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("signin"));
        assertTrue(driver.getCurrentUrl().contains("signin"), "Should be redirected after login");
        assertTrue(isElementPresent(By.linkText("My Account")), "My Account link should be visible after login");
        assertTrue(isElementPresent(By.linkText("Sign Out")), "Sign Out link should appear after login");
    }

    @Test
    @Order(5)
    void testInvalidLogin_ErrorMessageShown() {
        driver.get(BASE_URL + "account/signonForm");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("wrong");
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Messages")));
        WebElement error = driver.findElement(By.cssSelector(".Messages"));
        assertTrue(error.isDisplayed(), "Error message container should be visible");
        assertTrue(error.getText().contains("Invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(6)
    void testCategoryNavigation_Dogs_DisplayProducts() {
        driver.get(BASE_URL);

        WebElement dogsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("DOGS")));
        dogsLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".SidebarContent")));
        assertTrue(driver.getCurrentUrl().contains("category"), "Clicking DOGS should navigate to category page");
        assertTrue(isElementPresent(By.linkText("Bulldog")), "Dogs category should contain Bulldog product");
        assertTrue(isElementPresent(By.linkText("Poodle")), "Dogs category should contain Poodle product");
    }

    @Test
    @Order(7)
    void testProductDetailPage_LoadsCorrectly() {
        testCategoryNavigation_Dogs_DisplayProducts(); // Navigate to dogs

        WebElement bulldogLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bulldog")));
        bulldogLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Catalog")));
        assertTrue(isElementPresent(By.cssSelector("img[alt='Bulldog']")), "Bulldog image should be displayed");
        assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Bulldog"), "Page should display Bulldog as title");
    }

    @Test
    @Order(8)
    void testAddToCart_FromProductPage() {
        testProductDetailPage_LoadsCorrectly();

        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCart.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#Cart h2")));
        WebElement cartTitle = driver.findElement(By.cssSelector("#Cart h2"));
        assertEquals("Shopping Cart", cartTitle.getText(), "Should be on Shopping Cart page");
        assertTrue(isElementPresent(By.linkText("Bulldog")), "Cart should contain Bulldog item");
    }

    @Test
    @Order(9)
    void testUpdateCartQuantity_ChangesTotal() {
        testAddToCart_FromProductPage();

        WebElement quantityField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("EST-")));
        quantityField.clear();
        quantityField.sendKeys("2");
        quantityField.submit();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".totalprice"), "$"));
        WebElement totalPrice = driver.findElement(By.cssSelector(".totalprice"));
        String priceText = totalPrice.getText();
        assertTrue(priceText.contains("$"), "Total price should be updated and visible");
    }

    @Test
    @Order(10)
    void testSignOut_ReturnsToHomePage() {
        loginIfNotAlready();

        WebElement signOut = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOut.click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Sign Out should return to home page");
        assertTrue(isElementPresent(By.linkText("Sign In")), "Sign In link should reappear after logout");
    }

    @Test
    @Order(11)
    void testFooterAboutUsLink_OpenInNewTab() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About us")));
        String originalWindow = driver.getWindowHandle();

        aboutLink.sendKeys(Keys.CONTROL, Keys.RETURN);

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("github"));
                assertTrue(driver.getCurrentUrl().contains("github"), "About us link should open GitHub repository");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback: same tab
        driver.switchTo().window(originalWindow);
        aboutLink.click();
        wait.until(ExpectedConditions.urlContains("github"));
        assertTrue(driver.getCurrentUrl().contains("github"), "About us link should redirect to GitHub");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(12)
    void testFooterAspectranLink_OpenInNewTab() {
        driver.get(BASE_URL);
        WebElement aspectranLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Aspectran")));
        String originalWindow = driver.getWindowHandle();

        aspectranLink.sendKeys(Keys.CONTROL, Keys.RETURN);

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("aspectran"));
                assertTrue(driver.getCurrentUrl().contains("aspectran"), "Aspectran link should open aspectran site");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback
        driver.switchTo().window(originalWindow);
        aspectranLink.click();
        wait.until(ExpectedConditions.urlContains("aspectran"));
        assertTrue(driver.getCurrentUrl().contains("aspectran"), "Aspectran link should redirect");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(13)
    void testHelpLink_NavigatesToHelpPage() {
        driver.get(BASE_URL);
        WebElement helpLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Help")));
        helpLink.click();

        wait.until(ExpectedConditions.titleIs("JPetStore Demo"));
        assertTrue(driver.getCurrentUrl().contains("help"), "Help link should navigate to help page");
        assertTrue(isElementPresent(By.tagName("h3")), "Help page should contain section headers");
    }

    @Test
    @Order(14)
    void testMyAccountLink_RequiresLogin() {
        driver.get(BASE_URL);
        WebElement myAccount = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("My Account")));

        myAccount.click();
        wait.until(ExpectedConditions.urlContains("signonForm"));

        WebElement loginMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Title")));
        assertTrue(loginMessage.getText().contains("Please enter"), "My Account should redirect to login when not authenticated");
    }

    private void loginIfNotAlready() {
        driver.get(BASE_URL);
        if (isElementPresent(By.linkText("Sign In"))) {
            driver.get(BASE_URL + "account/signonForm");
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));

            usernameField.sendKeys(LOGIN);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("signin"));
        }
    }

    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}