package com.alxad.omsdk;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.alxad.entity.AlxOmidBean;
import com.iab.omid.library.algorixco.Omid;
import com.iab.omid.library.algorixco.adsession.AdSession;
import com.iab.omid.library.algorixco.adsession.AdSessionConfiguration;
import com.iab.omid.library.algorixco.adsession.AdSessionContext;
import com.iab.omid.library.algorixco.adsession.CreativeType;
import com.iab.omid.library.algorixco.adsession.ImpressionType;
import com.iab.omid.library.algorixco.adsession.Owner;
import com.iab.omid.library.algorixco.adsession.Partner;
import com.iab.omid.library.algorixco.adsession.VerificationScriptResource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * AdSessionUtil
 *
 * @author liuweile
 * @date 2022-4-27
 */
public final class AdSessionUtil {

    @NonNull
    public static AdSession getNativeAdSession(Context context, String customReferenceData, CreativeType creativeType, AlxOmidBean bean) throws Exception {
        ensureOmidActivated(context);

        String key = OmConfig.VENDOR_KEY;
        String parameters = OmConfig.VERIFICATION_PARAMETERS;
        String url = OmConfig.VERIFICATION_URL;

        if (bean != null) {
            if (!TextUtils.isEmpty(bean.key)) {
                key = bean.key;
            }
            if (!TextUtils.isEmpty(bean.params)) {
                parameters = bean.params;
            }
            if (!TextUtils.isEmpty(bean.url)) {
                url = bean.url;
            }
        }

        AdSessionConfiguration adSessionConfiguration =
                AdSessionConfiguration.createAdSessionConfiguration(creativeType,
                        (creativeType == CreativeType.AUDIO ? ImpressionType.AUDIBLE : ImpressionType.VIEWABLE),
                        Owner.NATIVE,
                        (creativeType == CreativeType.HTML_DISPLAY || creativeType == CreativeType.NATIVE_DISPLAY) ?
                                Owner.NONE : Owner.NATIVE, false);

        Partner partner = Partner.createPartner(OmConfig.PARTNER_NAME, OmConfig.VERSION_NAME);
        final String omidJs = OmidJsLoader.getOmidJs(context);
        List<VerificationScriptResource> verificationScripts = AdSessionUtil.getVerificationScriptResources(url, key, parameters);
        AdSessionContext adSessionContext = AdSessionContext.createNativeAdSessionContext(partner,
                omidJs,
                verificationScripts,
                null,
                customReferenceData);
        return AdSession.createAdSession(adSessionConfiguration, adSessionContext);
    }

    @NonNull
    public static AdSession getHtmlAdSession(Context context, WebView webView, String customReferenceData, CreativeType creativeType) throws Exception {
        ensureOmidActivated(context);

        AdSessionConfiguration adSessionConfiguration =
                AdSessionConfiguration.createAdSessionConfiguration(
                        creativeType,
                        ImpressionType.BEGIN_TO_RENDER, Owner.NATIVE,
                        creativeType == CreativeType.HTML_DISPLAY || creativeType == CreativeType.DEFINED_BY_JAVASCRIPT ? Owner.NONE : Owner.NATIVE, false);
        Partner partner = Partner.createPartner(OmConfig.PARTNER_NAME, OmConfig.VERSION_NAME);
        AdSessionContext adSessionContext = AdSessionContext.createHtmlAdSessionContext(partner, webView, null, customReferenceData);
        AdSession adSession = AdSession.createAdSession(adSessionConfiguration, adSessionContext);

        adSession.registerAdView(webView);
        return adSession;
    }

    @NonNull
    public static AdSession getJsAdSession(Context context, WebView webView, String customReferenceData, CreativeType creativeType) throws Exception {
        ensureOmidActivated(context);

        AdSessionConfiguration adSessionConfiguration =
                AdSessionConfiguration.createAdSessionConfiguration(
                        creativeType,
                        ImpressionType.VIEWABLE,
                        Owner.NATIVE,
                        (creativeType == CreativeType.NATIVE_DISPLAY) ? Owner.NONE : Owner.NATIVE,
                        false);
        Partner partner = Partner.createPartner(OmConfig.PARTNER_NAME, OmConfig.VERSION_NAME);
        AdSessionContext adSessionContext = AdSessionContext.createJavascriptAdSessionContext(partner, webView, null, customReferenceData);
        AdSession adSession = AdSession.createAdSession(adSessionConfiguration, adSessionContext);

        return adSession;
    }

    @NonNull
    private static List<VerificationScriptResource> getVerificationScriptResources(String url, String key, String paramters) throws Exception {
        VerificationScriptResource verificationScriptResource = paramters == null ?
                VerificationScriptResource.createVerificationScriptResourceWithoutParameters(getURL(url)) :
                VerificationScriptResource.createVerificationScriptResourceWithParameters(key, getURL(url), paramters);
        return Collections.singletonList(verificationScriptResource);
    }

    @NonNull
    private static URL getURL(String url) throws MalformedURLException {
        return new URL(url);
    }

    /**
     * Lazily activate the OMID API.
     *
     * @param context any context
     */
    private static void ensureOmidActivated(Context context) throws Exception {
        Omid.activate(context.getApplicationContext());
    }
}