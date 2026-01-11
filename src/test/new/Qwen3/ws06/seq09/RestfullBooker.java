package Qwen3.ws06.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "password";

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
        assertTrue(title.contains("Shady Meadows"), "Page title should contain 'Shady Meadows'");

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Welcome to Shady Meadows B&B", heading.getText(), "Main heading should match");

        WebElement description = driver.findElement(By.tagName("p"));
        assertTrue(description.getText().toLowerCase().contains("hotel") || description.getText().toLowerCase().contains("shady meadows"), "Description should describe hotel");
    }

    @Test
    @Order(2)
    void testRoomAvailabilityDisplayed() {
        driver.get(BASE_URL);

        java.util.List<WebElement> rooms = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".row.room")));
        assertTrue(rooms.size() > 0, "At least one room should be displayed");
    }

    @Test
    @Order(3)
    void testRoomDetailsModalOpens() {
        driver.get(BASE_URL);

        WebElement firstRoom = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".col-sm-6.room")));
        firstRoom.click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-content")));
        assertTrue(modal.isDisplayed(), "Room details modal should appear");
    }

    @Test
    @Order(4)
    void testContactFormSubmitSuccess() {
        driver.get(BASE_URL);

        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();

        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        nameField.sendKeys("Caio Silva");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio@example.com");

        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("555-1234");

        WebElement subjectField = driver.findElement(By.id("subject"));
        subjectField.sendKeys("Test Inquiry");

        WebElement descriptionField = driver.findElement(By.id("description"));
        descriptionField.sendKeys("This is a test message from automated test.");

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Submit']")));
        submitButton.click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-success")));
        assertTrue(successMessage.isDisplayed(), "Success message should appear");
        assertTrue(successMessage.getText().trim().contains("Thanks for getting in touch") || successMessage.getText().trim().contains("Thank you for contacting us!"), "Success message should match");
    }

    @Test
    @Order(5)
    void testContactFormValidationNameRequired() {
        driver.get(BASE_URL);

        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        emailField.sendKeys("caio@example.com");

        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("555-1234");

        WebElement subjectField = driver.findElement(By.id("subject"));
        subjectField.sendKeys("Test");

        WebElement descriptionField = driver.findElement(By.id("description"));
        descriptionField.sendKeys("Test");

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Submit']")));
        submitButton.click();

        WebElement nameField = driver.findElement(By.id("name"));
        String nameClass = nameField.getAttribute("class");
        assertTrue(nameClass.contains("error"), "Name field should have error class when empty");
    }

    @Test
    @Order(6)
    void testContactFormValidationEmailRequired() {
        driver.get(BASE_URL);

        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();

        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        nameField.sendKeys("Caio");

        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("555-1234");

        WebElement subjectField = driver.findElement(By.id("subject"));
        subjectField.sendKeys("Test");

        WebElement descriptionField = driver.findElement(By.id("description"));
        descriptionField.sendKeys("Test");

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Submit']")));
        submitButton.click();

        WebElement emailField = driver.findElement(By.id("email"));
        String emailClass = emailField.getAttribute("class");
        assertTrue(emailClass.contains("error"), "Email field should have error class when empty");
    }

    @Test
   Order(7)
    void testContactFormValidationDescriptionRequired() {
        driver.get(BASE_URL);

        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();

        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        nameField.sendKeys("Caio");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio@example.com");

        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("555-1234");

        WebElement subjectField = driver.findElement(By.id("subject"));
        subjectField.sendKeys("Test");

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Submit']")));
        submitButton.click();

        WebElement descriptionField = driver.findElement(By.id("description"));
        String descriptionClass = descriptionField.getAttribute("class");
        assertTrue(descriptionClass.contains("error"), "Description field should have error class when empty");
    }

    @Test
    @Order(8)
    void testAdminLoginValidCredentials() {
        driver.get(BASE_URL + "admin");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys(ADMIN_USER);

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(ADMIN_PASS);

        WebElement loginButton = driver.findElement(By.id("doLogin"));
        loginButton.click();

        WebElement dashboardHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h3")));
        assertEquals("B&B Booking Management", dashboardHeader.getText(), "Should navigate to admin dashboard");
    }

    @Test
    @Order(9)
    void testAdminLoginInvalidCredentials() {
        driver.get(BASE_URL + "admin");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys("invalid");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("wrong");

        WebElement loginButton = driver.findElement(By.id("doLogin"));
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear");
        assertTrue(errorMessage.getText().toLowerCase().contains("login") || errorMessage.getText().toLowerCase().contains("fail"), "Error message should indicate wrong credentials");
    }

    @Test
    @Order(10)
    void testAdminDashboardDisplaysBookings() {
        adminLogin();

        java.util.List<WebElement> bookingRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".table-responsive tbody tr")));
        assertTrue(bookingRows.size() > 0, "At least one booking should be displayed in dashboard");
    }

    @Test
    @Order(11)
    void testAdminCanLogout() {
        adminLogin();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By cssSelector("button[data-testid='logout'],
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("/login") || ExpectedConditions.urlContains("/admin"));
    }

    @Test
    @Order(12)
    void testFooterGithubLink() {
        driver.get(BASE_URL);

        String originalWindow = driver.getWindowHandle();
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github']")));
        githubLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("github.com"), "GitHub link should redirect to GitHub domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(13)
    void testFooterLinkedinLink() {
        driver.get(BASE_URL);

        String originalWindow = driver.getWindowHandle();
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin']")));
        linkedinLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("linkedin.com"), "LinkedIn link should redirect to LinkedIn domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(14)
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
        assertTrue(url.contains("twitter.com") || url.contains("x.com"), "Twitter link should redirect to X/Twitter domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(15)
    void testBookingRoomFromModal() {
        driver.get(BASE_URL);

        WebElement firstRoom = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".col-sm-6.room")));
        firstRoom.click();

        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstname")));
        firstNameField.sendKeys("Caio");

        WebElement lastNameField = driver.findElement(By.id("lastname"));
        lastNameField.sendKeys("Silva");

        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("caio@example.com");

        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("555-1234");

        WebElement checkIn = driver.findElement(By.id("checkin"));
        checkIn.clear();
        checkIn.sendKeys("2023-10-01");

        WebElement checkOut = driver.findElement(By.id("checkout"));
        checkOut.clear();
        checkOut.sendKeys("2023-10-05");

        WebElement bookNowButton = driver.findElement(By.xpath("//button[text()='Book']"));
        bookNowButton.click();

        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-success")));
        assertTrue(successAlert.isDisplayed(), "Booking success message should appear");
    }

    private void adminLogin() {
        if (!driver.getCurrentUrl().contains("/admin")) {
            driver.get(BASE_URL + "admin");
        }

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            WebElement usernameField = driver.findElement(By.id("username"));
            usernameField.sendKeys(ADMIN_USER);

            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys(ADMIN_PASS);

            WebElement loginButton = driver.findElement(By.id("doLogin"));
            loginButton.click();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".row")));
        } catch (TimeoutException e) {
            // Already logged in
        }
    }
}