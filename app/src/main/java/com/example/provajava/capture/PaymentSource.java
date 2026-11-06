package com.example.provajava.capture;

import androidx.annotation.Nullable;

/**
 * Apps whose notifications may describe a card payment.
 * Matched by a fragment of the package name, so that small differences in the
 * package (e.g. business vs retail app) are still picked up.
 */
public enum PaymentSource {

    FINECO("Fineco", "fineco"),
    BPER("BPER", "bper"),
    PAYPAL("PayPal", "paypal"),
    GOOGLE_WALLET("Google Wallet", "com.google.android.apps.walletnfcrel");

    public final String label;
    private final String packageFragment;

    PaymentSource(String label, String packageFragment) {
        this.label = label;
        this.packageFragment = packageFragment;
    }

    @Nullable
    public static PaymentSource fromPackage(@Nullable String packageName) {
        if (packageName == null) return null;
        String pkg = packageName.toLowerCase();
        for (PaymentSource s : values()) {
            if (pkg.contains(s.packageFragment)) return s;
        }
        return null;
    }
}
