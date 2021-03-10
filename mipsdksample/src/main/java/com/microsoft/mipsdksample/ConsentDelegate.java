package com.microsoft.mipsdksample;
import com.microsoft.informationprotection.Consent;
import com.microsoft.informationprotection.IConsentDelegate;

public class ConsentDelegate implements IConsentDelegate {

  @Override
  public Consent getUserConsent(String url) {
    return Consent.ACCEPT;
  }
}