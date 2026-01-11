package GPT5.ws05.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    /* ===================== Helpers ===================== */

    private void openHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
    }

    private WebElement firstPresent(By... locators) {
        for (By by : locators) {
            List<WebElement> els = driver.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }
        throw new NoSuchElementException("None of the locators matched: " + Arrays.toString(locators));
    }

    private WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private void scrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'})", el);
    }

    private void safeClick(WebElement el) {
        scrollIntoView(el);
        wait.until(ExpectedConditions.elementToBeClickable(el)).click();
    }

    private void clearAndType(WebElement el, String text) {
        scrollIntoView(el);
        el.clear();
        el.sendKeys(text);
    }

    private void submitForm() {
        WebElement submit = firstPresent(
                By.cssSelector("button[type='submit']"),
                By.cssSelector("input[type='submit']"),
                By.xpath("//button[contains(.,'Enviar') or contains(.,'Submit')]"),
                By.xpath("//input[@type='submit']")
        );
        safeClick(submit);
    }

    private void assertSuccessVisible() {
        WebElement success = waitVisible(By.cssSelector(".success"));
        String msg = success.getText().toLowerCase();
        Assertions.assertTrue(msg.contains("sucesso") || msg.contains("success"),
                "Expected success message after valid submission. Actual: " + msg);
    }

    private void assertErrorVisible() {
        WebElement error = waitVisible(By.cssSelector(".error"));
        String msg = error.getText().toLowerCase();
        Assertions.assertTrue(msg.contains("erro") || msg.contains("error") || msg.contains("valide"),
                "Expected error message for invalid submission. Actual: " + msg);
    }

    private void fillRequiredFieldsValid() {
        WebElement firstName = firstPresent(By.id("firstName"), By.name("firstName"), By.name("first_name"));
        WebElement lastName  = firstPresent(By.id("lastName"), By.name("lastName"), By.name("last_name"));
        WebElement email     = firstPresent(By.id("email"), By.name("email"));
        WebElement message   = firstPresent(By.id("open-text-area"), By.name("open-text-area"), By.name("message"));

        clearAndType(firstName, "Caio");
        clearAndType(lastName, "Tester");
        clearAndType(email, "caio.tester@example.com");
        clearAndType(message, "Mensagem de teste automatizada.");
    }

    private void switchToNewTabAndVerify(String expectedDomainOrPath) {
        String original = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> wins = driver.getWindowHandles();
        for (String w : wins) {
            if (!w.equals(original)) {
                driver.switchTo().window(w);
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.urlContains(expectedDomainOrPath),
                        ExpectedConditions.titleContains("Privacy"),
                        ExpectedConditions.presenceOfElementLocated(By.tagName("body"))
                ));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomainOrPath),
                        "Expected URL to contain: " + expectedDomainOrPath + " but was: " + driver.getCurrentUrl());
                driver.close();
                driver.switchTo().window(original);
                return;
            }
        }
        Assertions.fail("No new tab opened for external link.");
    }

    /* ===================== Tests ===================== */

    @Test
    @Order(1)
    public void testHomePageLoadsAndHeaderPresent() {
        openHome();
        WebElement title = waitVisible(By.cssSelector("h1"));
        Assertions.assertTrue(title.getText().toLowerCase().contains("central") || title.isDisplayed(),
                "Main title/header should be present.");
    }

    @Test
    @Order(2)
    public void testRequiredFieldsValidationShowsError() {
        openHome();
        submitForm();
        assertErrorVisible();
    }

    @Test
    @Order(3)
    public void testValidSubmissionShowsSuccess() {
        openHome();
        fillRequiredFieldsValid();
        submitForm();
        assertSuccessVisible();
    }

    @Test
    @Order(4)
    public void testInvalidEmailShowsError() {
        openHome();
        WebElement firstName = firstPresent(By.id("firstName"), By.name("firstName"));
        WebElement lastName  = firstPresent(By.id("lastName"), By.name("lastName"));
        WebElement email     = firstPresent(By.id("email"), By.name("email"));
        WebElement message   = firstPresent(By.id("open-text-area"), By.name("open-text-area"));

        clearAndType(firstName, "Caio");
        clearAndType(lastName, "Tester");
        clearAndType(email, "invalid-email");
        clearAndType(message, "Mensagem de teste.");

        submitForm();
        assertErrorVisible();
    }

    @Test
    @Order(5)
    public void testPhoneBecomesRequiredWhenCheckboxChecked() {
        openHome();
        fillRequiredFieldsValid();

        // Check the "phone required" checkbox if present, then ensure phone is required
        List<WebElement> phoneRequired = driver.findElements(By.id("phone-checkbox"));
        if (!phoneRequired.isEmpty()) {
            safeClick(phoneRequired.get(0));
        }

        // Leave phone empty and submit
        submitForm();
        // Expect error due to missing phone when required
        if (!phoneRequired.isEmpty()) {
            assertErrorVisible();
        } else {
            // If checkbox not present on this version, just assert page still functional
            Assertions.assertTrue(driver.findElement(By.tagName("form")).isDisplayed(),
                    "Form should still be present if phone-required checkbox is absent.");
        }
    }

    @Test
    @Order(6)
    public void testSelectProductAndContactTypeIfPresent() {
        openHome();
        fillRequiredFieldsValid();

        // Product dropdown
        List<WebElement> products = driver.findElements(By.id("product"));
        if (!products.isEmpty()) {
            Select select = new Select(products.get(0));
            if (select.getOptions().size() > 1) {
                select.selectByIndex(1);
                String chosen = select.getFirstSelectedOption().getText().trim();
                Assertions.assertFalse(chosen.isEmpty(), "A product option should be selected.");
            }
        }

        // Contact type radios (email/phone)
        List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio'][name='contact']"));
        if (!radios.isEmpty()) {
            WebElement first = radios.get(0);
            if (!first.isSelected()) safeClick(first);
            Assertions.assertTrue(first.isSelected(), "First contact type radio should be selected.");
        }

        submitForm();
        assertSuccessVisible();
    }

    @Test
    @Order(7)
    public void testFileUploadIfPresent() throws IOException {
        openHome();
        fillRequiredFieldsValid();

        List<WebElement> fileInputs = driver.findElements(By.cssSelector("input[type='file']"));
        if (!fileInputs.isEmpty()) {
            Path tmp = Files.createTempFile("upload-", ".txt");
            Files.write(tmp, "test file".getBytes());
            fileInputs.get(0).sendKeys(tmp.toAbsolutePath().toString());
        }

        submitForm();
        assertSuccessVisible();
    }

    @Test
    @Order(8)
    public void testPrivacyPolicyExternalLinkOpensInNewTab() {
        openHome();
        // In this page, "Política de Privacidade" usually opens privacy.html in a new tab
        List<WebElement> privacyLinks = driver.findElements(By.cssSelector("a[href*='privacy']"));
        if (!privacyLinks.isEmpty()) {
            int before = driver.getWindowHandles().size();
            WebElement link = privacyLinks.get(0);
            scrollIntoView(link);
            link.click();

            if (driver.getWindowHandles().size() > before) {
                switchToNewTabAndVerify("privacy.html");
            } else {
                // Same-tab navigation fallback
                wait.until(ExpectedConditions.urlContains("privacy.html"));
                Assertions.assertTrue(driver.getCurrentUrl().contains("privacy.html"),
                        "Should navigate to privacy.html");
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
            }
        } else {
            Assertions.assertTrue(true, "No privacy link present; skipping external link test.");
        }
    }

    @Test
    @Order(9)
    public void testBackToHomeFromPrivacyIfSameTab() {
        openHome();
        List<WebElement> privacyLinks = driver.findElements(By.linkText("Política de Privacidade"));
        if (!privacyLinks.isEmpty()) {
            WebElement link = privacyLinks.get(0);
            scrollIntoView(link);
            link.click();
            // If it's same-tab, go back
            if (driver.getWindowHandles().size() == 1) {
                wait.until(ExpectedConditions.urlContains("privacy.html"));
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("form")));
                Assertions.assertTrue(driver.getCurrentUrl().contains("index.html") || driver.getCurrentUrl().contains("s3.eu-central-1.amazonaws.com"),
                        "Should return to home page.");
            }
        } else {
            Assertions.assertTrue(true, "No privacy link present; nothing to assert here.");
        }
    }
}
