package com.schoolday.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RecoveryCodePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators
    private final By heading = By.cssSelector("ngc-recovery-code-login div.title");
    private final By descriptionText = By.cssSelector("ngc-recovery-code-login h3");
    private final By recoveryCodeInput = By.cssSelector("[data-testid='recovery-code-login-recovery-code-input']");
    private final By confirmButton = By.cssSelector("[data-testid='recovery-code-login-submit-btn']");
    private final By backToMfaLink = By.cssSelector("[data-testid='recovery-code-login-back-to-mfa-btn']");
    private final By contactSupportLink = By.cssSelector("[data-testid='recovery-code-login-contact-support-link']");
    private final By validationError = By.cssSelector("ngc-recovery-code-login .mat-form-field-invalid");
    private final By snackbarContainer = By.cssSelector(
            "simple-snack-bar, .mat-mdc-snack-bar-container, .mat-snack-bar-container");

    public RecoveryCodePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public RecoveryCodePage waitForPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(heading));
        return this;
    }

    public boolean isDisplayed() {
        try {
            return driver.findElement(heading).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getHeadingText() {
        return driver.findElement(heading).getText();
    }

    public String getDescriptionText() {
        return driver.findElement(descriptionText).getText();
    }

    public boolean isRecoveryCodeInputDisplayed() {
        return driver.findElement(recoveryCodeInput).isDisplayed();
    }

    public boolean isConfirmButtonDisplayed() {
        return driver.findElement(confirmButton).isDisplayed();
    }

    public boolean isBackToMfaLinkDisplayed() {
        return driver.findElement(backToMfaLink).isDisplayed();
    }

    public boolean isContactSupportLinkDisplayed() {
        return driver.findElement(contactSupportLink).isDisplayed();
    }

    public RecoveryCodePage enterRecoveryCode(String code) {
        WebElement input = driver.findElement(recoveryCodeInput);
        input.clear();
        input.sendKeys(code);
        return this;
    }

    public void clickConfirm() {
        driver.findElement(confirmButton).click();
    }

    public void clickBackToMfa() {
        driver.findElement(backToMfaLink).click();
    }

    public void clickContactSupport() {
        driver.findElement(contactSupportLink).click();
    }

    public String getValidationError() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(validationError));
        List<WebElement> errors = driver.findElements(validationError);
        if (errors.isEmpty()) {
            return "";
        }
        return errors.stream()
                .map(WebElement::getText)
                .filter(text -> !text.isBlank())
                .findFirst()
                .orElse("");
    }

    public boolean hasValidationError() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(validationError));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasSnackbarError() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(snackbarContainer));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSnackbarErrorMessage() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(snackbarContainer));
        return driver.findElement(snackbarContainer).getText();
    }

    public String getContactSupportHref() {
        return driver.findElement(contactSupportLink).getAttribute("href");
    }
}
