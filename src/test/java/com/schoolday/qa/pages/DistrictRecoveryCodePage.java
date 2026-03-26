package com.schoolday.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class DistrictRecoveryCodePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators — district recovery code page uses recovery-code-* data-testid pattern
    private final By heading = By.cssSelector("div.title");
    private final By descriptionText = By.xpath("//h3[contains(text(),'recovery codes')]");
    private final By recoveryCodeInput = By.cssSelector("[data-testid='recovery-code-code-input']");
    private final By confirmButton = By.cssSelector("[data-testid='recovery-code-verify-btn']");
    private final By backButton = By.cssSelector("[data-testid='recovery-code-back-btn']");
    private final By validationError = By.cssSelector(".mat-form-field-invalid");
    private final By snackbarContainer = By.cssSelector(
            "simple-snack-bar, .mat-mdc-snack-bar-container, .mat-snack-bar-container");
    private final By snackbarActionButton = By.cssSelector(
            ".mat-simple-snackbar-action button, .mat-mdc-snack-bar-action button");

    public DistrictRecoveryCodePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public DistrictRecoveryCodePage waitForPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(recoveryCodeInput));
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
        return wait.until(ExpectedConditions.visibilityOfElementLocated(heading)).getText();
    }

    public String getDescriptionText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(descriptionText)).getText();
    }

    public boolean isRecoveryCodeInputDisplayed() {
        return driver.findElement(recoveryCodeInput).isDisplayed();
    }

    public boolean isConfirmButtonDisplayed() {
        return driver.findElement(confirmButton).isDisplayed();
    }

    public DistrictRecoveryCodePage enterRecoveryCode(String code) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(recoveryCodeInput));
        input.clear();
        input.sendKeys(code);
        return this;
    }

    public void clickConfirm() {
        wait.until(ExpectedConditions.elementToBeClickable(confirmButton)).click();
    }

    public void clickBackToMfa() {
        wait.until(ExpectedConditions.elementToBeClickable(backButton)).click();
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

    public String waitForAnyError() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(validationError),
                            ExpectedConditions.visibilityOfElementLocated(snackbarContainer)
                    ));
        } catch (Exception e) {
            return "none";
        }
        if (!driver.findElements(validationError).isEmpty()) return "validation";
        if (!driver.findElements(snackbarContainer).isEmpty()) return "snackbar";
        return "none";
    }

    public String getSnackbarErrorMessage() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(snackbarContainer));
        return driver.findElement(snackbarContainer).getText();
    }

    public void dismissSnackbar() {
        try {
            WebElement actionButton = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(snackbarActionButton));
            actionButton.click();
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.invisibilityOfElementLocated(snackbarContainer));
        } catch (Exception ignored) {
            // snackbar already gone
        }
    }
}
