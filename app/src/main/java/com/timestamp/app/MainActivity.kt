package com.timestamp.app

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var editTextWatermark: EditText
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonGenerateWatermark: Button
    private lateinit var buttonSaveImage: Button
    
    private var selectedImageBitmap: Bitmap? = null
    private var watermarkedBitmap: Bitmap? = null

    // 图片选择器
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                selectedImageBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                selectedImageBitmap?.let { bitmap ->
                    imageView.setImageBitmap(bitmap)
                    buttonGenerateWatermark.isEnabled = true
                    
                    // 设置默认水印文本为当前时间
                    val currentTime = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date())
                    editTextWatermark.setText(currentTime)
                    
                    Toast.makeText(this, "图片选择成功", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "加载图片失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 权限请求回调
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            saveImageToGallery()
        } else {
            Toast.makeText(this, "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
        
        // 初始状态：生成和保存按钮禁用
        buttonGenerateWatermark.isEnabled = false
        buttonSaveImage.isEnabled = false
    }

    private fun initViews() {
        imageView = findViewById(R.id.imageViewPreview)
        editTextWatermark = findViewById(R.id.editTextWatermark)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonGenerateWatermark = findViewById(R.id.buttonGenerateWatermark)
        buttonSaveImage = findViewById(R.id.buttonSaveImage)
    }

    private fun setupClickListeners() {
        // 选择图片按钮
        buttonSelectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        // 生成水印按钮
        buttonGenerateWatermark.setOnClickListener {
            generateWatermark()
        }

        // 保存图片按钮
        buttonSaveImage.setOnClickListener {
            checkPermissionAndSave()
        }
    }

    /**
     * 生成水印图片
     */
    private fun generateWatermark() {
        val originalBitmap = selectedImageBitmap ?: run {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show()
            return
        }

        val watermarkText = editTextWatermark.text.toString().trim()
        if (watermarkText.isEmpty()) {
            Toast.makeText(this, "请输入水印文字", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 创建可变的副本用于绘制水印
            watermarkedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            
            val canvas = Canvas(watermarkedBitmap!!)
            
            // 字体大小：图片宽度的 4.5%
            val fontSize = (originalBitmap.width * 0.045).toFloat()
            
            // 创建画笔
            val paint = android.graphics.Paint().apply {
                color = Color.WHITE          // 纯白色
                textSize = fontSize
                isAntiAlias = true           // 抗锯齿
                style = android.graphics.Paint.Style.FILL
                typeface = android.graphics.Typeface.DEFAULT  // 系统默认无衬线字体（Roboto）
            }
            
            // 计算文字尺寸
            val textBounds = android.graphics.Rect()
            paint.getTextBounds(watermarkText, 0, watermarkText.length, textBounds)
            val textWidth = textBounds.width().toFloat()
            val textHeight = textBounds.height().toFloat()
            
            // 位置：右下角，距边缘约 5%
            val marginX = (originalBitmap.width * 0.05).toFloat()
            val marginY = (originalBitmap.height * 0.05).toFloat()
            
            val x = originalBitmap.width - marginX - textWidth
            val y = originalBitmap.height - marginY
            
            // 绘制水印文字
            canvas.drawText(watermarkText, x, y, paint)
            
            // 显示预览
            imageView.setImageBitmap(watermarkedBitmap)
            buttonSaveImage.isEnabled = true
            
            Toast.makeText(this, "水印生成成功", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "生成水印失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 检查权限并保存图片
     */
    private fun checkPermissionAndSave() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要媒体权限
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-12 需要存储权限
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            saveImageToGallery()
        }
    }

    /**
     * 保存图片到相册
     */
    private fun saveImageToGallery() {
        val bitmap = watermarkedBitmap ?: run {
            Toast.makeText(this, "请先生成水印图片", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val timestamp = System.currentTimeMillis()
            val fileName = "Timestamp_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
            
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, timestamp / 1000)
                put(MediaStore.Images.Media.DATE_MODIFIED, timestamp / 1000)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val uri = contentResolver.insert(collection, contentValues)
            
            uri?.let {
                val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(it, contentValues, null, null)
                }
                
                Toast.makeText(this, "图片已保存到相册: $fileName", Toast.LENGTH_LONG).show()
                
                // 可选：通知媒体扫描器刷新相册
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            } ?: run {
                Toast.makeText(this, "保存失败：无法创建文件", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 回收 Bitmap 防止内存泄漏
        selectedImageBitmap?.recycle()
        watermarkedBitmap?.recycle()
    }
}
