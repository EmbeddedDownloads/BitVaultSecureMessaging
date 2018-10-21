package com.app.securemessaging.bean;

/**
 * Wallet Bean Class to Set walletName,walletAmount,walletAddress.
 */

public class WalletBeanClass {
    String walletName;
    String walletAmount;
    String walletAddress = "158GApV7U3SX455vd1c3tCFH1A2828eK8D";

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public String getWalletAmount() {
        return walletAmount;
    }

    public void setWalletAmount(String walletAmount) {
        this.walletAmount = walletAmount;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
}
