package com.example.madcamp3jhsj.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.madcamp3jhsj.R
import com.example.madcamp3jhsj.SharedViewModel
import com.example.madcamp3jhsj.data.User
import com.example.madcamp3jhsj.databinding.FragmentNotificationsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: User
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val userTextView = root.findViewById<TextView>(R.id.title)
        val userNamer = root.findViewById<TextView>(R.id.user_name)
        val userEmail = root.findViewById<TextView>(R.id.user_email)
        val logoutButton = root.findViewById<Button>(R.id.logout_button)
        val deleteAccountButton = root.findViewById<Button>(R.id.delete_account_button)
        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        firebaseAuth.currentUser
        logoutButton.setOnClickListener {
            sharedViewModel.logout(firebaseAuth.currentUser!!.displayName!!)
            firebaseAuth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                requireActivity().finish() // 액티비티 종료
            }
        }
        userTextView.text = "${firebaseAuth.currentUser!!.displayName!!}님의 식습관 점수는?"
        userNamer.text = firebaseAuth.currentUser!!.displayName!!
        userEmail.text = firebaseAuth.currentUser!!.email!!
        deleteAccountButton.setOnClickListener {
            sharedViewModel.deleteAccountByUserName(firebaseAuth.currentUser!!.displayName!!)
            firebaseAuth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                Toast.makeText(requireContext(), "계정이 삭제 되었습니다.", Toast.LENGTH_SHORT).show()
                requireActivity().finish() // 액티비티 종료
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}