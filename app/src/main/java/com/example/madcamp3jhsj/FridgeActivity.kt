package com.example.madcamp3jhsj
import android.animation.Animator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.madcamp3jhsj.SpringRetrofitClient.apiService
import com.example.madcamp3jhsj.data.AppDatabase
import com.example.madcamp3jhsj.data.User
import com.example.madcamp3jhsj.data.UserDao
import com.example.madcamp3jhsj.data.UserRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FridgeActivity : AppCompatActivity() {

    private lateinit var lottieView: LottieAnimationView
    private lateinit var user: User
    private lateinit var userDao: UserDao
    private lateinit var repository: UserRepository
    private lateinit var viewModel: UserViewModel
    private var isLogin: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge) // 위 XML 파일의 레이아웃
        userDao = AppDatabase.getDatabase(this).userDao()
        repository = UserRepository(userDao)
        viewModel = UserViewModel(repository)
        // LottieAnimationView 초기화
        lottieView = findViewById(R.id.lottieAnimationView)

        userinit()
        // 클릭 시 애니메이션 재생


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
    override fun onResume() {
        super.onResume()
        userinit()
    }
    private fun resetAnimation() {
        lottieView.progress = 0f    // 초기 상태로 되돌리기
    }
    fun userinit(){
        viewModel.fetchLastLoggedInUser()
        viewModel.lastLoggedInUser.observe(this) { user ->
            if (user != null) {
                println("Last logged in user: ${user.username}")
                this.user = user
                isLogin = true
                // 필요한 UI 업데이트 처리
            } else {
                println("No user found with last_login = -1")
            }
            lottieView.setOnClickListener {
                if (isLogin) lottieView.playAnimation()
                else loginLogic()
                // 애니메이션 다시 시작
            }
        }
    }
    fun loginLogic() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        setLoginPopup(dialogView) {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
    fun setLoginPopup(dialogView: View, dismissPopup: () -> Unit) {
        val loginButton = dialogView.findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            performLogin(dialogView)
            dismissPopup()
            lottieView.playAnimation()
        }
    }
    fun performLogin(dialogView: View) {
        val call: Call<Void> = apiService.login()
        val username = dialogView.findViewById<EditText>(R.id.editText).text.toString()
        val useremail = dialogView.findViewById<EditText>(R.id.editEmailText).text.toString()
        val token = "example_token"
        user = User(username = username, email = useremail, token = token, last_login = -1)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    println("Login successful! Response code: ${response.raw().request.url.toString()}")
                    val url = response.raw().request.url.toString()
                    println("URL: $url")

                    val intent = Intent(this@FridgeActivity, WebViewActivity::class.java)
                    intent.putExtra("URL", url)
                    startActivity(intent)
                } else {
                    println("Login failed. Response code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                println("Login request failed. Error: ${t.message}")
            }
        })
        loginUser(user)
    }

    fun loginUser(user:User){
        // Room에 유저 정보 저장
        viewModel.saveUserIfNotExists(user!!) { isNewUser ->
            if (isNewUser) {
                println("새 유저 저장 완료: $user")
            } else {
                println("유저가 이미 존재합니다: $user.username")
            }
        }
        viewModel.clearLastLoginExcept(user.username)
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
        Log.e("[setSelectPopup]","$user")
        val fridge1Button = dialogView.findViewById<Button>(R.id.fridge1Button)
        val fridge2Button = dialogView.findViewById<Button>(R.id.fridge2Button)
        val fridge3Button = dialogView.findViewById<Button>(R.id.fridge3Button)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_NAME", user.username)
        intent.putExtra("USER_EMAIL", user.email)
        fridge1Button.setOnClickListener {
            intent.putExtra("FRAGMENT_ID", R.id.navigation_home)
            this.startActivity(intent)
            dismissPopup()
            resetAnimation()
        }

        fridge2Button.setOnClickListener {
            intent.putExtra("FRAGMENT_ID", R.id.navigation_dashboard)
            this.startActivity(intent)
            dismissPopup()
            resetAnimation()
        }

        fridge3Button.setOnClickListener {
            intent.putExtra("FRAGMENT_ID", R.id.navigation_notifications)
            dismissPopup()
            resetAnimation()
            this.startActivity(intent)
        }

        cancelButton.setOnClickListener {
            dismissPopup()
            resetAnimation()
        }
    }
}
