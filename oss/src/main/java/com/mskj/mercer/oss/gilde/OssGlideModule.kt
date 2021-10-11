package com.mskj.mercer.oss.gilde

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.module.LibraryGlideModule
import java.io.InputStream

@com.bumptech.glide.annotation.GlideModule
class OssGlideModule: LibraryGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        registry.prepend(String::class.java, InputStream::class.java, OssModelLoaderFactory())
    }

}