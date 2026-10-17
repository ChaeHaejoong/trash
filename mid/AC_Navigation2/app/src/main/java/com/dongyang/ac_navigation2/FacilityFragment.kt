package com.dongyang.ac_navigation2
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.dongyang.ac_navigation2.databinding.FragmentFacilityBinding
import kotlin.jvm.java

class FacilityFragment : Fragment() {

    lateinit private var binding: FragmentFacilityBinding
    //private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFacilityBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val facilityId = "library"
        val bundle = bundleOf(
            "facilityId" to facilityId
        )

        binding.ivDetail1.setOnClickListener {
            view.findNavController().navigate(R.id.action_facilityFragment_to_facilityDetailFragment, bundle)
        }
    }
}
