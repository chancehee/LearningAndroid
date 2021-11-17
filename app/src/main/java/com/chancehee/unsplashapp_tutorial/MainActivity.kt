package com.chancehee.unsplashapp_tutorial

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import com.chancehee.unsplashapp_tutorial.databinding.ActivityMainBinding
import com.chancehee.unsplashapp_tutorial.retrofit.RetrofitManager
import com.chancehee.unsplashapp_tutorial.utils.Constant.TAG
import com.chancehee.unsplashapp_tutorial.utils.RESPONSE_STATUS
import com.chancehee.unsplashapp_tutorial.utils.SEARCH_TYPE
import com.chancehee.unsplashapp_tutorial.utils.onMyTextChanged
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_button_search.*

class MainActivity : AppCompatActivity() {

    // 선택된 UI가 어떤 것인지 알기 위한 변수 설정
    private var currentSearchType : SEARCH_TYPE = SEARCH_TYPE.PHOTO

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 뷰 바인딩
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Log.d(TAG, "MainActivity - onCreate() is called")

        // 라디오 그룹 가져오기
        // _: 사용안함 / checkedId: 선택된 아이디
        binding.searchTernRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            // 선택된 버튼에 맞게 UI 변경
            when(checkedId){
                // 사진 검색 버튼이라면
                R.id.photo_search_radio_btn ->{
                    Log.d(TAG, "사진검색 버튼 클릭!!")
                    binding.searchTermTextLayout.hint = "사진 검색"
                    binding.searchTermTextLayout.startIconDrawable = resources.getDrawable(R.drawable.ic_baseline_photo_library_24, resources.newTheme())
                    this.currentSearchType = SEARCH_TYPE.PHOTO
                }
                // 사용자 검색 버튼이라면
                R.id.user_search_radio_btn ->{
                    Log.d(TAG, "사용자 검색 버튼 클릭!!")
                    binding.searchTermTextLayout.hint = "사용자 검색"
                    binding.searchTermTextLayout.startIconDrawable = resources.getDrawable(R.drawable.ic_baseline_person_24, resources.newTheme())
                    this.currentSearchType = SEARCH_TYPE.USER
                }
            }
            Log.d(TAG, "MainActivity - OnCheckedChanged() called / currentSearchType: $currentSearchType")
        }


        // 텍스트가 변겅이 되었을때 (글자가 입력된 경우 / 글자가 아무것도 없는 경우) UI 변경
        // Extension(completion)을 활용하여 필요한 기능만 사용한다.
        binding.searchTermEditText.onMyTextChanged {
            if(it.toString().count() > 0){
                frame_search_btn.visibility = View.VISIBLE
                search_term_text_layout.helperText=" "
                main_scrollview.scrollTo(0,200)
            }else{
                frame_search_btn.visibility = View.INVISIBLE
                search_term_text_layout.helperText = "검색어를 입력해주세요"
            }

            // 글자 수가 정해논 범위를 초과할 경우 토스트 메시지 띄우기
            if (it.toString().count() == 12){
                Log.d(TAG, "MainActivity - 에러 띄우기")
                Toast.makeText(this,"검색어는 12자 까지만 입력 가능합니다.", Toast.LENGTH_SHORT).show()
            }


        }

        // 검색 버튼 클릭시
        btn_search.setOnClickListener {
            Log.d(TAG, "MainActivity - 검색 버튼이 클릭되었다. / currentSearchType : $currentSearchType")

            this.handleSearchButtonUi()

            val userSearchInput = search_term_edit_text.text.toString()

            // 검색 api 호출
            RetrofitManager.instance.searchPhotos(searchTerm = binding.searchTermEditText.text.toString(), completion = {
                responseState, responseDataArrayList ->

                when(responseState){
                    RESPONSE_STATUS.OKAY -> {
                        Log.d(TAG, "api 호출 성공: ${responseDataArrayList?.size}")

                        val intent = Intent(this,PhotoCollectionActivity::class.java)

                        val bundle = Bundle()

                        bundle.putSerializable("photo_array_list",responseDataArrayList)

                        intent.putExtra("array_bundle",bundle)

                        intent.putExtra("search_term", userSearchInput)

                        startActivity(intent)

                    }
                    RESPONSE_STATUS.FAIL -> {
                        Toast.makeText(this,"api 호출 에러입니다.", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "api 호출 실패: ${responseDataArrayList}")
                    }
                    RESPONSE_STATUS.NO_CONTENT ->{
                        Toast.makeText(this,"검색결과가 없습니다.",Toast.LENGTH_SHORT).show()
                    }
                }
                btn_progress.visibility = View.INVISIBLE
                btn_search.text = "검색"
                search_term_edit_text.setText("")
            })


        }
    } // oncreate


    // 1.progressbar= visible, text = ' '  ->  2.딜레이 시간 주기  ->  3.progressbar= invisible, text = '검색'
    private fun handleSearchButtonUi(){
        btn_progress.visibility = View.VISIBLE
        btn_search.text = ""
        // Handler() 그냥 사용하는 것은 곧 사라질 예정이라 Looper 추가
//        Handler(Looper.getMainLooper()).postDelayed({
//            btn_progress.visibility = View.INVISIBLE
//            btn_search.text = "검색"
//        },1500)
    }
}