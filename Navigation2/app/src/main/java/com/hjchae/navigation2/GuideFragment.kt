package com.hjchae.navigation2
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hjchae.navigation2.databinding.FragmentGuideBinding


class GuideFragment : Fragment() {

    private var binding: FragmentGuideBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuideBinding.inflate(inflater, container, false)

        binding?.btnCallSchool?.setOnClickListener {
           
        }

        binding?.btnOpenWebsite?.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.ac.kr"))
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "실행할 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        return binding?.root!!
    }
}
