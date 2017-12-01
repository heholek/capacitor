package com.avocadojs;

import android.content.Context;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class Avocado {
  private Context context;
  private WebView webView;
  private MessageHandler msgHandler;
  private Map<String, Plugin> plugins = new HashMap<String, Plugin>();


  public Avocado(Context context, WebView webView) {
    Log.d("AVOCADO", "constructor");
    this.context = context;
    this.webView = webView;
    this.msgHandler = new MessageHandler(this, webView);


    final WebViewLocalServer localServer = new WebViewLocalServer(context);
    WebViewLocalServer.AssetHostingDetails ahd = localServer.hostAssets("www");

    webView.setWebViewClient(new WebViewClient() {
      @Override
      public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return localServer.shouldInterceptRequest(request);
      }
    });

    String url = ahd.getHttpsPrefix().buildUpon().appendPath("index.html").build().toString();
    webView.loadUrl(url);
  }

  public void addPlugin(Plugin plugin) {
    try {
      this.plugins.put(plugin.getId(), plugin);
      this.indexMethods(plugin);
    } catch (Exception ex) {
      Log.e("addPlugin", "Exception : " + ex);
      ex.printStackTrace();
    }
  }

  private void indexMethods(Plugin plugin) {
    Class<? extends Plugin> pluginClass = plugin.getClass();
    Method[] methods = pluginClass.getDeclaredMethods();

    for(Method pluginMethod: methods) {
      PluginMethod method = pluginMethod.getAnnotation(PluginMethod.class);

      if(method != null) {
        Log.d("Found method on plugin", method.toString());
        PluginMethodMetadata methodMeta = new PluginMethodMetadata(pluginMethod);
      }
    }
  }

  public Plugin getPlugin(String pluginId) {
    return this.plugins.get(pluginId);
  }

  public Context getContext() {
    return this.context;
  }

  public WebView getWebView() {
    return this.webView;
  }

}
