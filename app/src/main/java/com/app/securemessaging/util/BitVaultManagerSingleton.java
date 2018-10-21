package com.app.securemessaging.util;

import bitmanagers.BitVaultWalletManager;

/**
 * Singleton class to get single instance of BitVaultWalletManager
 */

public class BitVaultManagerSingleton {

    public static BitVaultWalletManager mBitVaultmanager = null;

    public static BitVaultWalletManager getInstance() {
        if (mBitVaultmanager == null) {
            mBitVaultmanager = BitVaultWalletManager.getWalletInstance();
        }
        return mBitVaultmanager;
    }

}
