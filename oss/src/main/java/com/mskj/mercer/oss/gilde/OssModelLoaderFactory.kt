package com.mskj.mercer.oss.gilde

import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.io.InputStream

class OssModelLoaderFactory: ModelLoaderFactory<String, InputStream> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
        return OssModelLoader()
    }

    override fun teardown() {
    }

}