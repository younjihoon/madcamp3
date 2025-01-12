package com.example.madcamp3jhsj
import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class FridgeActivity : AppCompatActivity() {

    private lateinit var lottieView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge) // 위 XML 파일의 레이아웃

        // LottieAnimationView 초기화
        lottieView = findViewById(R.id.lottieAnimationView)

        // 클릭 시 애니메이션 재생
        lottieView.setOnClickListener {
            lottieView.playAnimation() // 애니메이션 다시 시작
        }

        // 애니메이션 상태 리스너 추가
        lottieView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                showSelectPopup()
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })
    }

    fun showSelectPopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_fridge_select, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        setSelectPopup(dialogView) {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    fun setSelectPopup(dialogView: View, dismissPopup: () -> Unit) {
        val fridge1Button = dialogView.findViewById<Button>(R.id.fridge1Button)
        val fridge2Button = dialogView.findViewById<Button>(R.id.fridge2Button)
        val fridge3Button = dialogView.findViewById<Button>(R.id.fridge3Button)
        fridge1Button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
            dismissPopup()
        }

        fridge2Button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
            dismissPopup()
        }

        fridge3Button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
            dismissPopup()
        }
    }
}
