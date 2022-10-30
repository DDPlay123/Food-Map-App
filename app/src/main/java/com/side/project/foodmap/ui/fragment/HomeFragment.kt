package com.side.project.foodmap.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.side.project.foodmap.data.remote.tdx.RestaurantList
import com.side.project.foodmap.databinding.FragmentHomeBinding
import com.side.project.foodmap.helper.displayShortToast
import com.side.project.foodmap.network.ApiClient
import com.side.project.foodmap.util.logE
import retrofit2.Call
import retrofit2.Response

class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJER2lKNFE5bFg4WldFajlNNEE2amFVNm9JOGJVQ3RYWGV6OFdZVzh3ZkhrIn0.eyJleHAiOjE2NjcxOTgxNzYsImlhdCI6MTY2NzExMTc3NiwianRpIjoiMTVkMjI4ODMtN2ZkOC00MzA5LWI0ZjUtYjQ0MTgxMzVlOThiIiwiaXNzIjoiaHR0cHM6Ly90ZHgudHJhbnNwb3J0ZGF0YS50dy9hdXRoL3JlYWxtcy9URFhDb25uZWN0Iiwic3ViIjoiNDVlYzJlMzktZTdjYS00NGExLWIyZWMtMjI5ZDM4N2M4OGM4IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiQjEwNzEzMDQ4LTYzNmY1NGZmLTNlNWMtNDE5OCIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsic3RhdGlzdGljIiwicHJlbWl1bSIsIm1hYXMiLCJhZHZhbmNlZCIsImhpc3RvcmljYWwiLCJiYXNpYyJdfSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwidXNlciI6Ijc3ZDYzMjEwIn0.OnO0xXzT5rqsBkH_FEoVRUhs1BZspl6mDXvtXlPsp4wJrC3A5tYjadRm8mXTyUsJ2vhkp3YSDqjJ16p8P96IXLot1VWNLZagaJ_Pah5iS6A8llDOCaxcghludNE4bqBNdj0vMlxdEvwZGdX46fwZnaucg6A-6hRD0fOD-bArfAD4aH-bjBz5rWjgTmjJBXiCqVYhJTgv6Nc9REJe9z-gHIHUZ7zXSn_YeA6tqgDTGv2cQ-XaZ40kR-YfFtX4gvEy44WhjRKbccki4Z_SmBSInht0pGOJtPUUZZTR2jK-ZbeGk4WsQqGH1W-gpQLq8ngTQO2CR5anEucOXayK86C3VQ"
        val city = "NewTaipei"

        ApiClient.getTdxRestaurant.getCityRestaurantByTDX(token, city).enqueue(object : retrofit2.Callback<RestaurantList> {
            override fun onResponse(call: Call<RestaurantList>, response: Response<RestaurantList>) {
                requireActivity().displayShortToast("成功")
            }

            override fun onFailure(call: Call<RestaurantList>, t: Throwable) {
                requireActivity().displayShortToast("失敗")
            }
        })
    }
}