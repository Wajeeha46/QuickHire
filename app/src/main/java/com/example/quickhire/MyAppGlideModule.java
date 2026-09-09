package com.example.quickhire;

import android.content.Context;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class MyAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Set memory cache size (20MB)
        int memoryCacheSizeBytes = 1024 * 1024 * 20;
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));

        // Set disk cache size (100MB)
        int diskCacheSizeBytes = 1024 * 1024 * 100;
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));

        // You can add other configurations here:
        // builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false; // Disable manifest parsing for better performance
    }
}