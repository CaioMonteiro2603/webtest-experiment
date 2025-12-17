package GPT5.ws06.seq08;

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
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @Order(1)
    public void homePageLoadsAndHeroVisible() {
        driver.get(BASE_URL);
        WebElement heroTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(heroTitle.getText().toLowerCase().contains("welcome") || heroTitle.getText().toLowerCase().contains("shady"),
                "Hero title should contain a welcome message or site name");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("automation") || driver.getTitle().toLowerCase().contains("testing"),
                "Document title should be set");
    }

    @Test
    @Order(2)
    public void navigateToContactSection() {
        driver.get(BASE_URL);
        WebElement contactNav = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactNav.click();
        wait.until(ExpectedConditions.urlContains("#contact"));
        WebElement formHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#contact h2")));
        Assertions.assertTrue(formHeader.getText().toLowerCase().contains("contact"),
                "Contact section header should be visible");
    }

    @Test
    @Order(3)
    public void navigateToRoomsSectionAndValidateCards() {
        driver.get(BASE_URL);
        WebElement roomsNav = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Rooms")));
        roomsNav.click();
        wait.until(ExpectedConditions.urlContains("#rooms"));
        List<WebElement> cards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#rooms .room-info, #rooms .row .col-sm-4")));
        Assertions.assertFalse(cards.isEmpty(), "At least one room card should be present");
        WebElement firstCard = cards.get(0);
        WebElement title = firstCard.findElement(By.cssSelector("h3, h2, .room-name"));
        Assertions.assertTrue(title.isDisplayed(), "Room title should be visible");
        List<WebElement> bookButtons = firstCard.findElements(By.cssSelector(".btn-primary, button, a"));
        Assertions.assertTrue(bookButtons.stream().anyMatch(b -> b.getText().toLowerCase().contains("book")),
                "A 'Book' button/link should exist on a room card");
    }

    @Test
    @Order(4)
    public void contactFormShowsErrorsWhenEmpty() {
        driver.get(BASE_URL + "#contact");
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#contact button[type='submit'], #contact button")));
        submitBtn.click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger, .alert.alert-danger, #contact .alert")));
        Assertions.assertTrue(error.isDisplayed(), "An error alert should be shown for missing required fields");
    }

    @Test
    @Order(5)
    public void contactFormValidSubmissionShowsSuccess() {
        driver.get(BASE_URL + "#contact");
        wait.until(ExpectedConditions.elementToBeClickable(By.name("name"))).sendKeys("John Doe");
        driver.findElement(By.name("email")).sendKeys("john@example.com");
        driver.findElement(By.name("phone")).sendKeys("1234567890");
        driver.findElement(By.name("subject")).sendKeys("Booking enquiry");
        driver.findElement(By.name("description")).sendKeys("I'd like to know about availability next weekend.");
        driver.findElement(By.cssSelector("#contact button[type='submit'], #contact button")).click();
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success, .alert.alert-success, #contact .alert-success")));
        Assertions.assertTrue(success.getText().toLowerCase().contains("thanks") || success.getText().toLowerCase().contains("thank"),
                "Success alert should confirm submission");
    }

    @Test
    @Order(6)
    public void openFirstRoomDetailsOrBooking() {
        driver.get(BASE_URL + "#rooms");
        List<WebElement> bookButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#rooms .room-info .btn-primary, #rooms .room-info a, #rooms .room-info button")));
        WebElement firstBook = null;
        for (WebElement btn : bookButtons) {
            if (btn.getText().toLowerCase().contains("book")) { firstBook = btn; break; }
        }
        if (firstBook == null && !bookButtons.isEmpty()) firstBook = bookButtons.get(0);
        Assertions.assertNotNull(firstBook, "A book/details button should be available");
        wait.until(ExpectedConditions.elementToBeClickable(firstBook)).click();
        // After click, either a modal appears or a booking panel becomes visible
        boolean modalOrPanel =
                !driver.findElements(By.cssSelector(".modal, .modal-dialog")).isEmpty() ||
                !driver.findElements(By.cssSelector("#booking, .room-booking")).isEmpty();
        Assertions.assertTrue(modalOrPanel, "Booking modal/panel should appear after clicking a 'Book' button");
    }

    @Test
    @Order(7)
    public void footerTwitterLinkOpensExternal() {
        driver.get(BASE_URL);
        handleExternalLink("a[href*='twitter.com']", "twitter.com");
    }

    @Test
    @Order(8)
    public void footerFacebookLinkOpensExternal() {
        driver.get(BASE_URL);
        handleExternalLink("a[href*='facebook.com']", "facebook.com");
    }

    @Test
    @Order(9)
    public void footerLinkedInLinkOpensExternal() {
        driver.get(BASE_URL);
        handleExternalLink("a[href*='linkedin.com']", "linkedin.com");
    }

    private void handleExternalLink(String cssSelector, String expectedDomain) {
        List<WebElement> links = driver.findElements(By.cssSelector(cssSelector));
        Assumptions.assumeTrue(!links.isEmpty(), "External link not present: " + cssSelector);
        String originalWindow = driver.getWindowHandle();
        String beforeUrl = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(links.get(0))).click();
        try {
            wait.until(d -> d.getWindowHandles().size() > 1 || !d.getCurrentUrl().equals(beforeUrl));
        } catch (TimeoutException ignored) { }
        Set<String> handles = driver.getWindowHandles();
        if (handles.size() > 1) {
            for (String h : handles) {
                if (!h.equals(originalWindow)) {
                    driver.switchTo().window(h);
                    break;
                }
            }
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "External link should navigate to " + expectedDomain);
            driver.navigate().back();
        }
    }
}
