package com.mskj.mercer.app

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.*
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.mskj.mercer.app.databinding.ActivityMainBinding
import com.mskj.mercer.oss.OssManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    val pic3 = "android_20211008_72_1633680153880.jpg"
    val pic4 =
        "http://mswm-private-bucket.oss-cn-shenzhen.aliyuncs.com/android_20210524_1343_1621823709218.jpg?Expires=1624969616&OSSAccessKeyId=STS.NUq3BkYuK5Wwa22rqTGbxKEgT&Signature=00pxO1YtNBHATHzkj4ndw2gDfHg%3D&security-token=CAIS%2BgF1q6Ft5B2yfSjIr5bEePjftKpqgpWcYxSDlnEBS%2B1UpIDMtjz2IH9JfHhuBOkct/81nW1Y7f4clr1pTJtIclfJdtBx6ZJg%2BAWteY3At5RUISO5Rdr3d1KIAjvXgeXwAYygPv6/F96pb1fb7FwRpZLxaTSlWXG8LJSNkuQJR98LXw6%2BH1EkbZUsUWkEksIBMmbLPvuAKwPjhnGqbHBloQ1hk2hym8/dq4%2B%2BkkOD0AygkrJF/NSofsT%2BMphWUc0hA4vv7otfbbHc1SNc0R9O%2BZptgbZMkTW95Y7AWwgPvkzcarGOq4A/d1cnfMchB7Veq/zxhRbWeDlSYAydGoABpBb5P1BHfq%2B4xEyvqkFrhxMVC5y68J3TWrBKqjqSTdqc99GCSqPUvl3t2vFk891sW0Kgce7rLj0BwMaFYiA7ttccjvPHP2YqMvOJcGnzWIVfN9bFk4%2Bhup2N%2BkBWz3YbCBcy9rt68kT4q9IxmLdYaNVEepyzB/FfCayX%2BI2yMC4%3D"
    val pic5 = "android_20210629_1343_1624934248869.jpg"

    val pic6 = "android_20210701_72_1625089241733.jpg"

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)

        val root = "http://mswm-private-bucket.oss-cn-shenzhen.aliyuncs.com/"

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.iv1.setOnClickListener {
            Glide.with(this)
                .load(pic3)
                .into(binding.iv1)
        }

        binding.iv2.setOnClickListener {

            /*Glide.with(this)
                .load(pic5)
                .into(binding.iv2)

            lifecycleScope.launch {
                OssManager()
                    .downLoad(pic5)
                    .catch { throwable ->
                        Log.e("TAG", "throwable : $throwable")
                        ToastUtils.showLong("下载失败")
                    }
                    .collect {
                        ToastUtils.showLong("下载成功")
                        Glide.with(this@MainActivity)
                            .load(it.readBytes())
                            .into(binding.iv2)
                    }
            }*/

            lifecycleScope.launch {
                downLoadApk()
            }
        }

        binding.iv3.setOnClickListener {
            Glide.with(this)
                .load(pic4)
                .into(binding.iv3)

//            dataStore.data.map {
//                val s = it[KEY_NAME]
//                val i = it[KEY_AGE]
//                println("111")
//                s to i
//            }.asLiveData(lifecycleScope.coroutineContext)
//                .observe(this@MainActivity) {
//                    println("111")
//                    Toast.makeText(this, "name : ${it.first}", Toast.LENGTH_SHORT).show()
//                }

            //判断是否授权这里以一个权限为例
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                //没有授权进行权限申请
//                // ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
//
//                requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//
//            }
            requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        }

    }

    private suspend fun downLoadApk() {
        // 下载
        /*
        OssManager().downLoad(
            "admin_1614053139918_userid_ihkbusiness_1.0.5V6.apk",
            file = File(
                PathUtils.getExternalAppFilesPath(),
                "admin_1614053139918_userid_ihkbusiness_1.0.5V6.apk"
            ), onProgressListener = object : OnProgressListener {
                override fun onProgress(currentSize: Long, totalSize: Long) {
                    runOnUiThread {
                        binding.tvProgress.text =
                            "当前进度 : ${(currentSize.toDouble() / totalSize * 100).toInt()}%"
                    }
                }
            }).catch {
            ToastUtils.showLong("下载失败")
        }.collect {
            ToastUtils.showLong("下载成功")
            AppUtils.installApp(it)
        }
        */

        /*
        OssManager().downLoadByStream(
            "admin_1614053139918_userid_ihkbusiness_1.0.5V6.apk",
            file = File(
                PathUtils.getExternalAppFilesPath(),
                "${System.currentTimeMillis()}.apk"
            )
        ) { currentSize: Long, totalSize: Long ->
            runOnUiThread {
                binding.tvProgress.text =
                    "当前进度 : ${(currentSize.toDouble() / totalSize * 100).toInt()}%"
            }
        }.catch {
            it.printStackTrace()
            ToastUtils.showLong("下载失败")
        }.collect {
            ToastUtils.showLong("下载成功")
            AppUtils.installApp(it)
        }
        */

    }

    val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                albumLauncher.launch("image/*")
            } else {
                ToastUtils.showLong("权限获取失败")
            }
        }

    val albumLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

        uri?:return@registerForActivityResult

        Glide.with(this)
            .load(uri)
            .into(binding.iv3)
//        /* val fileAbsolutePath = PhoneUtil.getFileAbsolutePath(this, uri)
//         updateImageFile(File(fileAbsolutePath))*/
//

        lifecycleScope.launch {
            /*OssManager().upLoad(uri)
                .catch { throwable ->
                    println("11111111111")
                    ToastUtils.showLong("上传失败")
                    Log.e("TAG", "catch --> throwable          : ${throwable}")

                }
                .collect { pair ->
                    println("11111111111")
                    ToastUtils.showLong("上传成功")
                }*/
            val push = OssManager().push(uri){ currentSize: Long, totalSize: Long ->
                runOnUiThread {
                    binding.tvProgress.text =
                        "当前进度 : ${(currentSize.toDouble() / totalSize * 100).toInt()}%"
                }
            }
            println("111111111111")
        }

    }


}
