package com.teamspirt.puzzlequest

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.rememberTextMeasurer
import java.io.IOException
import java.util.Collections
import kotlin.random.Random

class PuzzleActivity : AppCompatActivity() {

    var pieces :ArrayList<PuzzlePiece>? = null
    var mCurrentPhotoPath :String? = null
    var mCurrentPhotoUri :String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)
        val layout = findViewById<RelativeLayout>(R.id.layout)

        val imageView = findViewById<ImageView>(R.id.imageView)

        val intent = intent
        val assetName = intent.getStringExtra("assetName")
        mCurrentPhotoPath = intent.getStringExtra("mCurrentPhotoPath")
        mCurrentPhotoUri = intent.getStringExtra("mCurrentPhotoUri")

        // run image related code after the view was laid out, to have all dimensions calculated

        imageView.post {
            if (assetName != null){
                setPicFromAsset(assetName,imageView)
            }
            else if (mCurrentPhotoPath != null){
                setPicFromPhotoPath(mCurrentPhotoPath!!,imageView)
            }
            else if (mCurrentPhotoUri != null){
                imageView.setImageURI(Uri.parse(mCurrentPhotoUri))
            }
            pieces = splitImage()
            val touchListener = TouchListener(this@PuzzleActivity)
            // shuffle pieces order
            Collections.shuffle(pieces)
            for (piece in pieces!!){
                piece.setOnTouchListener(touchListener)
                layout.addView(piece)

            // randomize position on the bottom of the screen
            val lParams = piece.layoutParams as RelativeLayout.LayoutParams
            lParams.leftMargin = Random.nextInt(
                layout.width - piece.pieceWidth
            )
            lParams.topMargin = layout.height - piece.pieceHeight

            piece.layoutParams = lParams


            }

        }
    }


    private fun setPicFromAsset(assetName: String, imageView: ImageView?) {

        val targetW = imageView!!.width
        val targetH = imageView.height
        val am = assets

        try {

            val `is` = am.open("img/$assetName")
            // Get the dimensions of the bitmap
            val bmOPtion = Options()
            BitmapFactory.decodeStream(
                `is`, Rect(-1,-1,-1,-1),bmOPtion
            )
            val photoW = bmOPtion.outWidth
            val photoH = bmOPtion.outHeight

            // Determine how much to scale down the image
            val scaleFactor = Math.min(
                photoW/targetW,photoH/targetH
            )

            // Decode the image file into Bitmap sized to fill the view
            bmOPtion.inJustDecodeBounds = false
            bmOPtion.inSampleSize = scaleFactor
            bmOPtion.inPurgeable = true
            val bitmap = BitmapFactory.decodeStream(
                `is`, Rect(-1,-1,-1,-1),bmOPtion
            )
            imageView.setImageBitmap(bitmap)


        }catch (e:IOException){
            e.printStackTrace()
            Toast.makeText(this@PuzzleActivity,e.localizedMessage,Toast.LENGTH_SHORT).show()
        }

    }


    private fun splitImage(): ArrayList<PuzzlePiece> {

        val piecesNumber = 12
        val rows = 4
        val cols = 3
        val imageView = findViewById<ImageView>(R.id.imageView)
        val pieces = ArrayList<PuzzlePiece>(piecesNumber)

        // Get the scaled bitmap of the source image
        val drawable = imageView.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val dimensions = getBitmapPositionInsideImageView(imageView)

        val scaledBitmapLeft = dimensions[0]
        val scaledBitmapTop = dimensions[1]
        val scaledBitmapWidth = dimensions[2]
        val scaledBitmapHeight = dimensions[3]

        val croppedImageWidth = scaledBitmapWidth - 2 * Math.abs(scaledBitmapLeft)
        val croppedImageHeight = scaledBitmapHeight - 2 * Math.abs(scaledBitmapTop)

        // Check if bitmap is not null before creating scaledBitmap
        val scaledBitmap: Bitmap = if (bitmap != null) {
            Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth, scaledBitmapHeight, true)
        } else {
            // Handle the situation when the bitmap is null
            // For example, show an error message and return an empty list
            Toast.makeText(this@PuzzleActivity, "Image is null", Toast.LENGTH_SHORT).show()
            return ArrayList()
        }
        val croppedBitmap = Bitmap.createBitmap(
            scaledBitmap,
                Math.abs(scaledBitmapLeft),
                Math.abs(scaledBitmapTop),croppedImageWidth,croppedImageHeight)


        // Calculate the width and height of the pieces (продовжуйте тут далі)sadsadsadsadas
        val pieceWidth = croppedImageWidth/cols
        val pieceHeight = croppedImageHeight/rows

        //create each bitmap piece and it to the resulting array
        var yCoord = 0
        for (row in 0 until rows)
        {
            var xCoord = 0
            for (col in 0 until cols)
            {
                //calculate offset for each piece
                var offsetX = 0
                var offsetY = 0
                if (col > 0){
                    offsetX = pieceWidth / 3
                }
                 if (row > 0){
                     offsetY = pieceHeight / 3
                 }
                val pieceBitmap = Bitmap.createBitmap(
                    croppedBitmap,xCoord - offsetX,yCoord - offsetY,
                    pieceWidth + offsetX, pieceHeight + offsetX
                )
                val piece = PuzzlePiece(applicationContext)
                piece.setImageBitmap(pieceBitmap)
                piece.xCoord = xCoord - offsetX + imageView.left
                piece.xCoord = xCoord - offsetX + imageView.top

                piece.pieceWidth = pieceWidth + offsetX
                piece.pieceHeight = pieceHeight + offsetY

                // this bitmap will hold our final puzzle piece image
                val puzzlePiece = Bitmap.createBitmap(
                    pieceWidth + offsetX, pieceHeight + offsetY,
                    Bitmap.Config.ARGB_8888
                )

                //draw path
                val bampSize = pieceHeight / 4
                val canvas = Canvas(puzzlePiece)
                val path = android.graphics.Path() //можливо ще треба дужки - (треба)
                path.moveTo(offsetX.toFloat(),offsetY.toFloat())

                if(row == 0){
                //top side piece
                path.lineTo(
                    pieceBitmap.width.toFloat(),
                    offsetY.toFloat()
                )
                }
                else{
                    //top bump
                    path.lineTo(
                        (offsetX + (pieceBitmap.width - offsetX)/3).toFloat(),
                        offsetY.toFloat()
                    )

                    path.cubicTo(
                        (offsetX + (pieceBitmap.width - offsetX).toFloat()),
                        (offsetY - bampSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 6 *5).toFloat(),
                        (offsetY - bampSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX)/ 3 * 2).toFloat(),
                        offsetY.toFloat()
                    )

                    path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
                }

                if(col == cols - 1){
                    // right side piece
                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                }
                else {
                    // right bump
                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3).toFloat()
                    )
                    path.cubicTo(
                        (pieceBitmap.width - bampSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6).toFloat(),
                        (pieceBitmap.width - bampSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 6 * 5).toFloat(),
                        (pieceBitmap.width - bampSize).toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3 * 2).toFloat(),
                    )

                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                }
                if (row == -1){
                    //bottom side piece
                    path.lineTo(
                        offsetX.toFloat(), pieceBitmap.height.toFloat()
                    )
                }
                else{
                    //bottom bump
                    path.lineTo(
                        (offsetX + (pieceBitmap.width)/3 * 2).toFloat(),
                        pieceBitmap.height.toFloat()
                    )

                    path.cubicTo(
                        (offsetX + (pieceBitmap.width - offsetX)/ 6 * 5).toFloat(),
                        (pieceBitmap.height - bampSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX)/ 6).toFloat(),
                        (pieceBitmap.height - bampSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX)/ 3).toFloat(),
                        (pieceBitmap.height - bampSize).toFloat()
                    )

                    path.lineTo(
                        offsetX.toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                }
                if (col == 0){
                    //left side piece
                    path.close()
                }
                else{

                    //left bump
                    path.lineTo(
                        offsetX.toFloat(),
                        (offsetY + ( pieceBitmap.height - offsetY)/ 3 * 2).toFloat(),
                    )
                    path.cubicTo(
                        (offsetX - bampSize).toFloat(),
                        (offsetY + ( pieceBitmap.height)/6 * 5).toFloat(),
                        (offsetX - bampSize).toFloat(),
                        (offsetY + ( pieceBitmap.height)/6 ).toFloat(),
                        (offsetX - bampSize).toFloat(),
                        (offsetY + ( pieceBitmap.height)/3).toFloat()
                )
                    path.close()
                }

                // mask the piece
                val paint = Paint()
                paint.color = 0x10000000
                paint.style = Paint.Style.FILL
                canvas.drawPath(path, paint)
                paint .xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(pieceBitmap,0f , 0f , paint)

                // draw a white border

                var border = Paint()
                border.color = -0x7f000001
                border.style = Paint.Style.STROKE
                border.strokeWidth = 8.0f
                canvas.drawPath(path,border)

                // draw a black border
                border = Paint()
                border.color = -0x80000000
                border.style = Paint.Style.STROKE
                border.strokeWidth = 3.0f
                canvas.drawPath(path,border)

                // set the resulting bit map to the piece
                piece.setImageBitmap(puzzlePiece)
                pieces.add(piece)
                xCoord += pieceWidth
            }
            yCoord += pieceHeight
        }
        return  pieces
    }

    fun checkGameOver(){
        if(isGameOver) {
            androidx.appcompat.app.AlertDialog.Builder(this@PuzzleActivity)
                .setTitle("You win!")
                //.setIcon(R.drawable.ic_celebration)
                .setMessage("You win!\ndo you want a new game?")
                .setPositiveButton("Yes"){
                    dialog,_->
                    finish()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){
                        dialog,_->
                    finish()
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private val isGameOver :Boolean
        private get (){
            for (piece in pieces!!){
                if(piece.canMove){
                    return false
                }
            }
            return true
        }


    private fun getBitmapPositionInsideImageView(imageView: ImageView?): IntArray {

        val ret = IntArray(4)
        if (imageView == null || imageView.drawable == null){
            return ret
        }

        //Get image dimensions
        //Get image matrix value and place them in an array
        val  f = FloatArray(9)

        imageView.imageMatrix.getValues(f)

        //Extract the scale value  using the  constants (if aspect ratio maintained scaleX == scaleY)

        val scaleX = f[Matrix.MSCALE_X]
        val scaleY = f[Matrix.MSCALE_Y]

        //Get the drawable (could also set the bitmap the drawable and getWidth / get Height)

        val d = imageView.drawable

        val origW = d.intrinsicWidth
        val origH = d.intrinsicHeight

        //Calculate the actual dimensions (далі продовжувати тут..)

        val actW = Math.round(origW * scaleX)
        val actH = Math.round(origH * scaleY)

        ret[2]= actW
        ret[3]= actH

        val imageViewW=imageView.width
        val imageViewH=imageView.height

        val top = (imageViewH -actH) / 2
        val left = (imageViewW -actW) / 2

        ret[0]= top
        ret[1]= left

        return ret

    }
    private fun setPicFromPhotoPath(mCurrentPhotoPath: String, imageView: ImageView?) {
        val targetW=imageView!!.width
        val targetH =imageView!!.height

        val bmOptions =Options()

        bmOptions.inJustDecodeBounds=true
        BitmapFactory.decodeFile(mCurrentPhotoPath,bmOptions)

        val photoW=bmOptions.outWidth
        val photoH=bmOptions.outHeight

        val scaleFactor = Math.min(
            photoW/targetW,photoH/targetH
        )

        bmOptions.inJustDecodeBounds =false
        bmOptions.inSampleSize=scaleFactor
        bmOptions.inPurgeable=true
        val bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath,bmOptions)
        var rotatedBitmap = bitmap

        try{
            val ei=ExifInterface(mCurrentPhotoPath)
            val orientation=ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            when (orientation){
                ExifInterface.ORIENTATION_ROTATE_90 ->{
                    rotatedBitmap = rotateImage(bitmap,90f)
                }
                ExifInterface.ORIENTATION_ROTATE_180 ->{
                    rotatedBitmap = rotateImage(bitmap,180f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 ->{
                    rotatedBitmap = rotateImage(bitmap,270f)
                }
            }


        } catch (e: IOException){
            e.printStackTrace()
            Toast.makeText(this@PuzzleActivity,e.localizedMessage,Toast.LENGTH_SHORT).show()
        }
        imageView.setImageBitmap(rotatedBitmap)


    }
    companion object {
        fun rotateImage(source: Bitmap, angle: Float): Bitmap {

            val matrix = Matrix()
            matrix.postRotate(angle)

            return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height, matrix, true
            )
        }
    }
}





