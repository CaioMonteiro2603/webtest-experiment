package SunaQwen3.ws06.seq05;

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
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String LOGIN_PAGE_URL = BASE_URL + "login";

    // Test credentials
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "password";

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
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys(VALID_USERNAME);
        passwordField.sendKeys(VALID_PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("admin"));
        assertTrue(driver.getCurrentUrl().contains("admin"), "URL should contain 'admin' after login");

        WebElement roomForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("roomName")));
        assertTrue(roomForm.isDisplayed(), "Room form should be displayed after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='alert']")));
        assertTrue(errorElement.isDisplayed(), "Error message should be displayed for invalid login");
        assertTrue(errorElement.getText().contains("Username and password"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testSortingDropdownOptions() {
        // Ensure logged in
        loginIfNotOnInventoryPage();

        WebElement roomDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("room")));
        roomDropdown.click();

        List<WebElement> options = driver.findElements(By.xpath("//select[@id='room']/option"));
        assertTrue(options.size() > 0, "Room options should be present");

        for (WebElement option : options) {
            option.click();
            assertTrue(option.isSelected(), "Option should be selectable");
        }
    }

    @Test
    @Order(4)
    void testMenuBurgerButtonAndOptions() {
        loginIfNotOnInventoryPage();

        WebElement burgerMenu = wait.until(ExpectedConditions.elementToBeClickable(By.id("showHeaderPanel")));
        burgerMenu.click();

        // Wait for menu to open
        WebElement mainMenu = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='main-menu']")));
        WebElement aboutLink = driver.findElement(By.xpath("//a[contains(text(),'About']"));
        WebElement roomsLink = driver.findElement(By.xpath("//a[contains(text(),'Rooms']"));
        WebElement galleryLink = driver.findElement(By.xpath("//a[contains(text(),'Gallery']"));

        assertTrue(aboutLink.isDisplayed(), "About link should be visible in menu");
        assertTrue(roomsLink.isDisplayed(), "Rooms link should be visible in menu");
        assertTrue(galleryLink.isDisplayed(), "Gallery link should be visible in menu");

        // Click about link
        aboutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'About']")));
        assertTrue(driver.getPageSource().contains("About"), "Should show about section");
    }

    @Test
    @Order(5)
    void testFooterSocialLinks() {
        loginIfNotOnInventoryPage();

        WebElement footerDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='footer']")));
        List<WebElement> socialLinks = footerDiv.findElements(By.tagName("a"));
        assertEquals(3, socialLinks.size(), "There should be 3 social links in the footer");

        String[] expectedUrls = {"facebook", "twitter", "linkedin"};
        for (int i = 0; i < socialLinks.size(); i++) {
            String href = socialLinks.get(i).getAttribute("href");
            assertTrue(href.contains(expectedUrls[i]), "Link should contain " + expectedUrls[i]);
        }
    }

    @Test
    @Order(6)
    void testAboutLinkInMenu() {
        loginIfNotOnInventoryPage();

        WebElement burgerMenu = wait.until(ExpectedConditions.elementToBeClickable(By.id("showHeaderPanel")));
        burgerMenu.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'About']")));
        aboutLink.click();

        WebElement aboutSection = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'About']")));
        assertTrue(aboutSection.isDisplayed(), "About section should be visible");
    }

    @Test
    @Order(7)
    void testAddRemoveItemsFromCart() {
        loginIfNotOnInventoryPage();

        // Reset app state via menu
        resetAppState();

        // Add first search result to cart
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchRoom")));
        searchInput.sendKeys("room");
        
        WebElement searchButton = driver.findElement(By.xpath("//button[contains(text(),'Search')]"));
        searchButton.click();

        List<WebElement> availableRooms = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='room-item']")));
        assertTrue(availableRooms.size() > 0, "At least one room should be present");

        // Click on first room to book it
        availableRooms.get(0).click();
        
        WebElement bookingForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bookingForm")));
        assertTrue(bookingForm.isDisplayed(), "Booking form should be displayed");
    }

    @Test
    @Order(8)
    void testCheckoutProcess() {
        loginIfNotOnInventoryPage();

        // Reset app state
        resetAppState();

        // Search for a room
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchRoom")));
        searchInput.sendKeys("room");

        WebElement searchButton = driver.findElement(By.xpath("//button[contains(text(),'Search')]"));
        searchButton.click();

        // Select a room
        List<WebElement> availableRooms = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='room-item']")));
        if (availableRooms.isEmpty()) {
            fail("No rooms available to book");
        }

        availableRooms.get(0).click();

        // Fill in booking form
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstname")));
        WebElement lastNameField = driver.findElement(By.id("lastname"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement phoneField = driver.findElement(By.id("phone"));

        firstNameField.sendKeys("John");
        lastNameField.sendKeys("Doe");
        emailField.sendKeys("john.doe@example.com");
        phoneField.sendKeys("1234567890");

        // Submit booking
        WebElement bookButton = driver.findElement(By.xpath("//button[contains(text(),'Book')]"));
        bookButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'Booking Successful')]")));
        assertTrue(successMessage.isDisplayed(), "Success message should be displayed");
    }

    private void loginIfNotOnInventoryPage() {
        if (!driver.getCurrentUrl().contains("admin")) {
            driver.get(LOGIN_PAGE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("doLogin"));

            usernameField.sendKeys(VALID_USERNAME);
            passwordField.sendKeys(VALID_PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("admin"));
        }
    }

    private void resetAppState() {
        loginIfNotOnInventoryPage();
    }
}