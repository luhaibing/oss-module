package com.mskj.mercer.oss.gilde

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import java.io.InputStream

class OssModelLoader : ModelLoader<String, InputStream> {

    override fun buildLoadData(
        model: String,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(GlideUrl(model),OssDataFetcher(model))
    }

    override fun handles(model: String): Boolean = !model.startsWith("http")

}