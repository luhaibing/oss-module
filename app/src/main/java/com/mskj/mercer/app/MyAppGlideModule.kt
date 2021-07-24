package com.mskj.mercer.app

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey

@com.bumptech.glide.annotation.GlideModule
class MyAppGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        var defaultMemoryCacheSize = 8 * 1024 * 1024L
        val runtimeMemoryCacheSize = Runtime.getRuntime().maxMemory() / 8
        val defaultDiskCacheSize = 250 * 1024 * 1024L
        if (runtimeMemoryCacheSize > defaultDiskCacheSize) {
            defaultMemoryCacheSize = runtimeMemoryCacheSize
        }
        builder.setMemoryCache(LruResourceCache(defaultMemoryCacheSize))
            .setDiskCache(InternalCacheDiskCacheFactory(context, defaultDiskCacheSize.toLong()))
            .setDefaultRequestOptions(requestOptions())
    }

    private fun requestOptions(): RequestOptions {
        return RequestOptions()
            .signature(
                ObjectKey(
                    System.currentTimeMillis() / (24 * 60 * 60 * 1000)
                )
            )
            // .override(100, 200)
            // .centerCrop()
            .encodeFormat(Bitmap.CompressFormat.PNG)
            .encodeQuality(100)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .skipMemoryCache(false)
    }

}