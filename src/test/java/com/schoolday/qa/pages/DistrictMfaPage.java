package com.schoolday.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DistrictMfaPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators — district MFA uses sms-mfa-* data-testid pattern
    private final By heading = By.cssSelector("div.title");
    private final By phoneDisplay = By.cssSelector("[data-testid='sms-mfa-phone-display']");
    private final By codeInput = By.cssSelector("[data-testid='sms-mfa-code-input']");
    private final By confirmButton = By.cssSelector("[data-testid='sms-mfa-verify-btn']");
    private final By resendCodeButton = By.cssSelector("[data-testid='sms-mfa-resend-btn']");
    private final By backToSignInButton = By.cssSelector("[data-testid='sms-mfa-back-btn']");
    private final By recoveryCodeButton = By.cssSelector("[data-testid='sms-mfa-recovery-btn']");
    private final By havingProblemsText = By.xpath("//h4[contains(text(),'Having problems')]");

    public DistrictMfaPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public DistrictMfaPage waitForPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(codeInput));
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

    public boolean isCodeInputDisplayed() {
        return driver.findElement(codeInput).isDisplayed();
    }

    public boolean isConfirmButtonDisplayed() {
        return driver.findElement(confirmButton).isDisplayed();
    }

    public boolean isResendCodeButtonDisplayed() {
        return driver.findElement(resendCodeButton).isDisplayed();
    }

    public String getResendCodeButtonText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(resendCodeButton)).getText();
    }

    public boolean isHavingProblemsTextDisplayed() {
        return driver.findElement(havingProblemsText).isDisplayed();
    }

    public boolean isRecoveryCodeButtonDisplayed() {
        return driver.findElement(recoveryCodeButton).isDisplayed();
    }

    public void clickRecoveryCodeButton() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(recoveryCodeButton));
        btn.click();
    }

    public void clickBackToSignIn() {
        wait.until(ExpectedConditions.elementToBeClickable(backToSignInButton)).click();
    }
}
