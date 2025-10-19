package Qwen3.ws06.seq10;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class HotelAppTest {
    private static WebDriver driver;
    private static WebDriverWait wait;

    private final String BASE_URL = "https://automationintesting.online/";

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

    @TestMethodOrder(OrderAnnotation.class)
    @Test
    @Order(1)
    void testPageTitleAndHeader_DisplayedCorrectly() {
        driver.get(BASE_URL);

        assertEquals("Let's automate a hotel!", driver.getTitle(), "Page title should be 'Let's automate a hotel!'");
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Let's automate a hotel!", header.getText(), "Main header should match page title");
    }

    @Test
    @Order(2)
    void testRoomAvailabilitySearch_ValidDates_DisplaysResults() {
        driver.get(BASE_URL);

        WebElement checkin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkin")));
        WebElement checkout = driver.findElement(By.id("checkout"));
        WebElement submitButton = driver.findElement(By.id("submit"));

        // Set check-in to tomorrow
        String tomorrow = java.time.LocalDate.now().plusDays(1).toString();
        String dayAfter = java.time.LocalDate.now().plusDays(2).toString();

        checkin.sendKeys(tomorrow);
        checkout.sendKeys(dayAfter);
        submitButton.click();

        WebElement results = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".room-list")));
        assertTrue(results.isDisplayed(), "Room results should be displayed after search");
        assertTrue(driver.findElements(By.cssSelector(".room")).size() > 0, "At least one room should be listed");
    }

    @Test
    @Order(3)
    void testRoomBookingForm_OpenedWhenBookNowClicked() {
        testRoomAvailabilitySearch_ValidDates_DisplaysResults(); // Ensure rooms are loaded

        WebElement firstBookButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".room .btn")));
        firstBookButton.click();

        WebElement bookingForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bookingForm")));
        assertTrue(bookingForm.isDisplayed(), "Booking form should appear when 'Book Now' is clicked");
        assertTrue(isElementPresent(By.name("firstname")), "Booking form should contain firstname field");
        assertTrue(isElementPresent(By.name("lastname")), "Booking form should contain lastname field");
        assertTrue(isElementPresent(By.name("totalprice")), "Booking form should contain totalprice field");
    }

    @Test
    @Order(4)
    void testSubmitBooking_WithValidData_SuccessMessageDisplayed() {
        testRoomBookingForm_OpenedWhenBookNowClicked();

        fillBookingFormWithValidData();

        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#bookingForm .btn")));
        saveButton.click();

        // Success alert
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(alert.isDisplayed(), "Success alert should be shown after booking");
        assertTrue(alert.getText().contains("created"), "Success message should confirm booking creation");
    }

    @Test
    @Order(5)
    void testSubmitBooking_MissingFirstName_ErrorShown() {
        testRoomBookingForm_OpenedWhenBookNowClicked();

        // Fill all except first name
        WebElement lastname = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("lastname")));
        WebElement totalPrice = driver.findElement(By.name("totalprice"));
        WebElement deposit = driver.findElement(By.name("deposit"));
        WebElement checkin = driver.findElement(By.name("checkin"));
        WebElement checkout = driver.findElement(By.name("checkout"));

        lastname.sendKeys("Silva");
        totalPrice.sendKeys("200");
        deposit.sendKeys("true");
        // Use JavaScript to set dates since input may be read-only
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2023-11-10'", checkin);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2023-11-12'", checkout);

        WebElement saveButton = driver.findElement(By.cssSelector("#bookingForm .btn"));
        saveButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".field-error")));
        assertTrue(errorMsg.isDisplayed(), "Error should be shown for missing firstname");
        assertEquals("Name required", errorMsg.getText(), "Error message should indicate name is required");
    }

    @Test
    @Order(6)
    void testContactForm_Submit_ValidData_Success() {
        driver.get(BASE_URL);

        WebElement contactTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#contact']")));
        contactTab.click();

        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("name")));
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement phoneField = driver.findElement(By.name("phone"));
        WebElement subjectField = driver.findElement(By.name("subject"));
        WebElement descriptionField = driver.findElement(By.name("description"));
        WebElement sendMessageButton = driver.findElement(By.id("sendmessage"));

        nameField.sendKeys("João Oliveira");
        emailField.sendKeys("joao.oliveira@example.com");
        phoneField.sendKeys("11987654321");
        subjectField.sendKeys("General Inquiry");
        descriptionField.sendKeys("I would like to know more about your services.");
        sendMessageButton.click();

        WebElement successModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("success"));
        assertTrue(successModal.isDisplayed(), "Success modal should appear after contact form submission");
        assertTrue(successModal.getText().contains("Thanks"), "Success message should contain 'Thanks'");
        
        WebElement okButton = successModal.findElement(By.cssSelector(".btn"));
        okButton.click();

        wait.until(ExpectedConditions.invisibilityOf(successModal));
    }

    @Test
    @Order(7)
    void testAdminLogin_ValidCredentials_Succeeds() {
        driver.get(BASE_URL);

        WebElement adminTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#admin']")));
        adminTab.click();

        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        username.sendKeys("admin");
        password.sendKeys("password");
        loginButton.click();

        WebElement loggedInHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#loggedIn h3")));
        assertEquals("Admin Panel", loggedInHeader.getText(), "Admin header should appear after login");
        assertTrue(isElementPresent(By.cssSelector("#loggedIn a[ng-click='logout()']")), "Logout link should be present");
    }

    @Test
    @Order(8)
    void testAdminLogin_InvalidCredentials_Fails() {
        driver.get(BASE_URL + "#admin");

        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        username.sendKeys("invalid");
        password.sendKeys("wrongpass");
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        WebElement error = driver.findElement(By.cssSelector(".alert-danger"));
        assertTrue(error.isDisplayed(), "Error alert should be displayed");
        assertTrue(error.getText().contains("Incorrect"), "Error message should indicate login failure");
    }

    @Test
    @Order(9)
    void testAdminLogout_ReturnsToLoginForm() {
        testAdminLogin_ValidCredentials_Succeeds(); // Already logged in

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#loggedIn a[ng-click='logout()']")));
        logoutLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        assertTrue(isElementPresent(By.name("username")), "Login form should reappear after logout");
        assertTrue(isElementPresent(By.name("password")), "Password field should be present after logout");
    }

    @Test
    @Order(10)
    void testFooterGitHubLink_OpenInNewTab() {
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='github']")));
        String originalWindow = driver.getWindowHandle();

        githubLink.sendKeys(Keys.CONTROL, Keys.RETURN); // Open in new tab

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("github"));
                assertTrue(driver.getCurrentUrl().contains("github"), "GitHub link should open github domain");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback: same tab
        driver.navigate().refresh();
        githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='github']")));
        githubLink.click();

        wait.until(ExpectedConditions.urlContains("github"));
        assertTrue(driver.getCurrentUrl().contains("github"), "GitHub link should redirect to github");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(11)
    void testFooterTwitterLink_OpenInNewTab() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='twitter']")));
        String originalWindow = driver.getWindowHandle();

        twitterLink.sendKeys(Keys.CONTROL, Keys.RETURN);

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("twitter"));
                assertTrue(driver.getCurrentUrl().contains("twitter"), "Twitter link should open twitter domain");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback
        driver.switchTo().window(originalWindow);
        driver.navigate().refresh();
        twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='twitter']")));
        twitterLink.click();

        wait.until(ExpectedConditions.urlContains("twitter"));
        assertTrue(driver.getCurrentUrl().contains("twitter"), "Twitter link should redirect");
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    @Test
    @Order(12)
    void testAboutSection_NavigatesAndDisplaysContent() {
        driver.get(BASE_URL);

        WebElement aboutTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#about']")));
        aboutTab.click();

        WebElement aboutHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#about h2")));
        assertEquals("About Us", aboutHeader.getText(), "About section should display correct header");
        assertTrue(isElementPresent(By.cssSelector("#about p")), "About section should contain description text");
    }

    private void fillBookingFormWithValidData() {
        WebElement firstname = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("firstname")));
        WebElement lastname = driver.findElement(By.name("lastname"));
        WebElement totalPrice = driver.findElement(By.name("totalprice"));
        WebElement deposit = driver.findElement(By.name("deposit"));
        WebElement checkin = driver.findElement(By.name("checkin"));
        WebElement checkout = driver.findElement(By.name("checkout"));

        firstname.sendKeys("Maria");
        lastname.sendKeys("Costa");
        totalPrice.sendKeys("150");
        deposit.sendKeys("true");
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2023-10-10'", checkin);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '2023-10-15'", checkout);
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