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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.madcamp3jhsj.SpringRetrofitClient.apiService
import com.example.madcamp3jhsj.data.AppDatabase
import com.example.madcamp3jhsj.data.User
import com.example.madcamp3jhsj.data.UserDao
import com.example.madcamp3jhsj.data.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class FridgeActivity : AppCompatActivity() {

    private lateinit var lottieView: LottieAnimationView
    private lateinit var user: User
    private lateinit var userDao: UserDao
    private lateinit var repository: UserRepository
    private lateinit var viewModel: UserViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private var isLogin: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge) // 위 XML 파일의 레이아웃
        userDao = AppDatabase.getDatabase(this).userDao()
        repository = UserRepository(userDao)
        viewModel = UserViewModel(repository)
        firebaseAuth = FirebaseAuth.getInstance()
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
                isLogin = false
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
            firebaselogin()
            dismissPopup()

        }
    }

    fun firebaselogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id)) // Web client ID
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        Log.d("GoogleSignIn", "signInWithGoogle: ${signInIntent.toString()}")
        startActivityForResult(signInIntent, 1)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign-In 성공
                Log.e("Google Sign-In", "Google Sign...")
                val account = task.getResult(ApiException::class.java)
                Log.e("Google Sign-In", "Google Signing...")
                firebaseAuthWithGoogle(account)
            } catch (e: Exception) {
                Log.e("Google Sign-In", "Google Sign-In failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    val fuser = firebaseAuth.currentUser
                    if (fuser!= null){
                    user = User(username=fuser.displayName!!,email=fuser.email!!, token = "", last_login = -1)

                    }
                    Log.d("FirebaseAuth", "signInWithCredential:success - User: ${fuser?.displayName}")
                    Toast.makeText(this, "Welcome ${fuser?.displayName}", Toast.LENGTH_SHORT).show()
                    loginUser(user)
                    lottieView.playAnimation()
                } else {
                    // 로그인 실패
                    Log.e("FirebaseAuth", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Firebase Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }

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
