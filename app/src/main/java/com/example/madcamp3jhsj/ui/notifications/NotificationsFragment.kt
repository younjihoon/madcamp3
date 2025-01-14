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
import com.example.madcamp3jhsj.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

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
        logoutButton.setOnClickListener {
            requireActivity().finish()
        }

        // 탈퇴하기 버튼 클릭 리스너
        deleteAccountButton.setOnClickListener {
            requireActivity().finish()
        }
        // 유저 데이터 관찰
        sharedViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                userTextView.text = "${it.username}님의 식습관 점수는?"
                userNamer.text = it.username
                userEmail.text = it.email
                logoutButton.setOnClickListener {
                    sharedViewModel.logout(user.username)
                    Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }

                // 탈퇴하기 버튼 클릭 리스너
                deleteAccountButton.setOnClickListener {
                    sharedViewModel.deleteAccount(user)
                    Toast.makeText(requireContext(), "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }
            }
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}