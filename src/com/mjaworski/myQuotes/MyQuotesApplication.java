package com.mjaworski.myQuotes;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.holoeverywhere.app.Application;


public class MyQuotesApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .discCacheSize(10 * 1024 * 1024)
                .discCacheFileCount(10)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .enableLogging()
                .build();
        ImageLoader.getInstance().init(config);
    }
}
