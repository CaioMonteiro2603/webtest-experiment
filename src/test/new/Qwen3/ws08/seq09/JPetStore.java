package Qwen3.ws08.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
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
    void testHomePageLoadsSuccessfully() {
        driver.get(BASE_URL);

        String title = driver.getTitle();
        assertTrue(title.contains("JPetStore"), "Page title should contain 'JPetStore'");

        WebElement mainTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        assertEquals("JPetStore Demo", mainTitle.getText(), "Main title should match");

        WebElement blurb = driver.findElement(By.cssSelector("div.jumbotron>p"));
        assertTrue(blurb.getText().contains("pet"), "Blurb should mention pets");
    }

    @Test
    @Order(2)
    void testNavigationToSignInPage() {
        driver.get(BASE_URL);

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("signon")));
        assertTrue(loginForm.isDisplayed(), "Login form should be visible");
    }

    @Test
    @Order(3)
    void testValidLogin() {
        driver.get(BASE_URL);

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys(USERNAME);

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(PASSWORD);

        WebElement loginButton = driver.findElement(By.name("signon"));
        loginButton.click();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.smallText")));
        assertTrue(welcomeMessage.getText().contains(USERNAME), "Welcome message should contain username");
    }

    @Test
    @Order(4)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL);

        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys("invalid");

        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("wrong");

        WebElement loginButton = driver.findElement(By.name("signon"));
        loginButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.error>li")));
        assertTrue(error.isDisplayed(), "Error message should appear");
        assertTrue(error.getText().contains("Invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(5)
    void testBrowseCatsCategory() {
        loginIfNecessary();

        WebElement catsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("area[alt='Cats']")));
        catsLink.click();

        WebElement categoryTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));
        assertEquals("Cats", categoryTitle.getText(), "Should navigate to Cats category");
    }

    @Test
    @Order(6)
    void testBrowseDogsCategory() {
        loginIfNecessary();

        WebElement dogsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("area[alt='Dogs']")));
        dogsLink.click();

        WebElement categoryTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));
        assertEquals("Dogs", categoryTitle.getText(), "Should navigate to Dogs category");
    }

    @Test
    @Order(7)
    void testProductDetailNavigation() {
        loginIfNecessary();

        // Go to birds category
        WebElement birdsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("area[alt='Birds']")));
        birdsLink.click();

        // Click on specific bird product
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("AV-SB-02")));
        productLink.click();

        WebElement productTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("th")));
        assertTrue(productTitle.getText().contains("Amazon Parrot"), "Should display Amazon Parrot details");
    }

    @Test
    @Order(8)
    void testAddItemToCart() {
        loginIfNecessary();

        // Go to fish category
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("area[alt='Fish']")));
        fishLink.click();

        // Click on product
        WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("FI-SW-01")));
        productLink.click();

        // Click add to cart
        WebElement addToCartLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add to Cart")));
        addToCartLink.click();

        // Verify cart has item
        WebElement itemInCart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("EST-6")));
        assertTrue(itemInCart.isDisplayed(), "Item should appear in cart");
    }

    @Test
    @Order(9)
    void testUpdateCartQuantity() {
        loginIfNecessary();

        // Add item to cart first
        driver.get(BASE_URL + "shop/addItemToCart?workingItemId=EST-6");

        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("?")));
        cartLink.click();

        WebElement quantitySelect = wait.until(ExpectedConditions.elementToBeClickable(By.name("quantity")));
        Select select = new Select(quantitySelect);
        select.selectByValue("3");

        WebElement updateButton = driver.findElement(By.cssSelector("form[name='updateCartQuantities'] input.btn"));
        updateButton.click();

        WebElement quantityDisplay = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("td.quantity input")));
        assertEquals("3", quantityDisplay.getAttribute("value"), "Cart quantity should update to 3");
    }

    @Test
    @Order(10)
    void testProceedToCheckout() {
        loginIfNecessary();

        // Add item to cart
        driver.get(BASE_URL + "shop/addItemToCart?workingItemId=EST-6");

        WebElement cartLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("?")));
        cartLink.click();

        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.button[href*='newOrderForm']")));
        checkoutButton.click();

        WebElement shippingTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));
        assertEquals("Shipping Address", shippingTitle.getText(), "Should navigate to shipping information");
    }

    @Test
    @Order(11)
    void testLogoutFunctionality() {
        loginIfNecessary();

        WebElement signOutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign Out")));
        signOutLink.click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.smallText")));
        assertTrue(confirmation.getText().contains("You have logged out"), "Logout confirmation should appear");
    }

    @Test
    @Order(12)
    void testHelpPageLink() {
        driver.get(BASE_URL);

        WebElement helpLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("HELP")));
        helpLink.click();

        WebElement helpTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));
        assertEquals("Help/FAQ", helpTitle.getText(), "Should navigate to Help page");
    }

    @Test
    @Order(13)
    void testAboutThisWebsiteLink() {
        driver.get(BASE_URL);

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("ABOUT")));
        aboutLink.click();

        WebElement aboutTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));
        assertEquals("About The Site", aboutTitle.getText(), "Should navigate to About page");
    }

    @Test
    @Order(14)
    void testFooterFacebookLink() {
        driver.get(BASE_URL);

        String originalWindow = driver.getWindowHandle();
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook']")));
        facebookLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("facebook.com"), "Facebook link should redirect to Facebook");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(15)
    void testFooterTwitterLink() {
        driver.get(BASE_URL);

        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("twitter.com"), "Twitter link should redirect to Twitter");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(16)
    void testFooterFlickrLink() {
        driver.get(BASE_URL);

        String originalWindow = driver.getWindowHandle();
        WebElement flickrLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='flickr']")));
        flickrLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("flickr.com"), "Flickr link should redirect to Flickr");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(17)
    void testSearchFunctionality() {
        loginIfNecessary();

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("keyword")));
        searchBox.sendKeys("Great White Shark");
        searchBox.submit();

        WebElement searchTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));
        assertEquals("Search Results", searchTitle.getText(), "Should show search results");

        WebElement resultItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Great White Shark")));
        assertTrue(resultItem.isDisplayed(), "Search should return matching product");
    }

    private void loginIfNecessary() {
        driver.get(BASE_URL);
        try {
            WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
            if (signInLink.isDisplayed()) {
                signInLink.click();

                WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
                usernameField.sendKeys(USERNAME);

                WebElement passwordField = driver.findElement(By.name("password"));
                passwordField.sendKeys(PASSWORD);

                WebElement loginButton = driver.findElement(By.name("signon"));
                loginButton.click();

                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.smallText")));
            }
        } catch (TimeoutException e) {
            // Already logged in
        }
    }
}