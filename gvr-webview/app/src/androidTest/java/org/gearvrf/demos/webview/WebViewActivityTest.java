package org.gearvrf.demos.webview;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class org.gearvrf.demos.webview.WebViewActivityTest \
 * org.gearvrf.demos.webview.tests/android.test.InstrumentationTestRunner
 */
public class WebViewActivityTest extends ActivityInstrumentationTestCase2<WebViewActivity> {

    public WebViewActivityTest() {
        super("org.gearvrf.demos.webview", WebViewActivity.class);
    }

}
