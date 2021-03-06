package com.chancehee.unsplashapp_tutorial.activities

import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.chancehee.unsplashapp_tutorial.R
import com.chancehee.unsplashapp_tutorial.model.Photo
import com.chancehee.unsplashapp_tutorial.model.SearchData
import com.chancehee.unsplashapp_tutorial.recyclerview.ISearchHistoryRecyclerView
import com.chancehee.unsplashapp_tutorial.recyclerview.PhotoGridRecyclerViewAdapter
import com.chancehee.unsplashapp_tutorial.recyclerview.SearchHistoryRecyclerViewAdapter
import com.chancehee.unsplashapp_tutorial.retrofit.RetrofitManager
import com.chancehee.unsplashapp_tutorial.utils.Constant.TAG
import com.chancehee.unsplashapp_tutorial.utils.RESPONSE_STATUS
import com.chancehee.unsplashapp_tutorial.utils.SharedPrefManager
import com.chancehee.unsplashapp_tutorial.utils.toSimpleString
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_photo_collection.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class PhotoCollectionActivity: AppCompatActivity(),
                            SearchView.OnQueryTextListener,
                            CompoundButton.OnCheckedChangeListener,
                            View.OnClickListener,
                            ISearchHistoryRecyclerView
{
    // 데이터
    var photoList = ArrayList<Photo>()

    // 검색 기록 배열
    private var searchHistoryList = ArrayList<SearchData>()

    // 어답터
    private lateinit var photoGridRecyclerViewAdapter: PhotoGridRecyclerViewAdapter
    private lateinit var mySearchHistoryRecyclerViewAdapter: SearchHistoryRecyclerViewAdapter

    // 서치뷰
    private lateinit var mySearchView: SearchView

    // 서치뷰 에딧 텍스트
    private lateinit var mySearchViewEditText: EditText

    // 옵저버를 통합 제거를 위한 CompositeDisposable
    private var myCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_collection)

        Log.d(TAG, "PhotoCollectionActivity - onCreate() is called")

        val bundle = intent.getBundleExtra("array_bundle")

        val searchTerm = intent.getStringExtra("search_term")

        Log.d(TAG, "PhotoCollectionActivity - onCreate() is called / searchTerm : $searchTerm, photoList.count() : ${photoList.count()}")




        search_history_mode_switch.setOnCheckedChangeListener(this)
        clear_search_history_button.setOnClickListener(this)

        search_history_mode_switch.isChecked = SharedPrefManager.CheckSearchHistoryMode()

        top_app_bar.title = searchTerm

        // 액티비티에서 어떤 액션바를 사용할지 설정한다.
        setSupportActionBar(top_app_bar)

        photoList = bundle?.getSerializable("photo_array_list") as ArrayList<Photo>

        // 사진 리사이클러뷰 세팅
        this.photoCollectionRecyclerViewSetting(this.photoList)



        // 저장된 검색 기록 가져오기
        this.searchHistoryList = SharedPrefManager.getSearchHistoryList() as ArrayList<SearchData>

        this.searchHistoryList.forEach {
            Log.d(TAG, "저장된 검색 기록 - it.term : ${it.term}, it.timestamp : ${it.timestamp}")
        }

        handleSearchViewUi()

        // 검색 기록 리사이클러뷰 준비
        this.searchHistoryRecyclerViewSetting(this.searchHistoryList)

        if (searchTerm != null) {
            if(searchTerm.isNotEmpty()){
                val term = searchTerm?.let {
                    it
                }?: ""
                this.insertSearchTermHistory(term)
            }
        }

    } // onCreate

    override fun onDestroy() {
        // 모두 삭제
        this.myCompositeDisposable.clear()
        super.onDestroy()
    }

    // 검색 기록 리사이클러뷰 준비
    private fun searchHistoryRecyclerViewSetting(searchHistoryList: ArrayList<SearchData>){
        Log.d(TAG, "PhotoCollectionActivity - searchHistoryRecyclerViewSetting() is called")

        this.mySearchHistoryRecyclerViewAdapter = SearchHistoryRecyclerViewAdapter(this)
        this.mySearchHistoryRecyclerViewAdapter.submitList(searchHistoryList)

        // 최근 검색어가 위로 오게하기.
        val myLinearLayoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true)
        myLinearLayoutManager.stackFromEnd = true

        search_history_recycler_view.apply{
            layoutManager = myLinearLayoutManager
            this.scrollToPosition(mySearchHistoryRecyclerViewAdapter.itemCount - 1)
            adapter = mySearchHistoryRecyclerViewAdapter
        }
    }

    // 그리드 사진 리사이클러뷰 세팅
    private fun photoCollectionRecyclerViewSetting(photoList: ArrayList<Photo>){
        Log.d(TAG, "PhotoCollectionActivity - searchHistoryRecyclerViewSetting() is called")

        this.photoGridRecyclerViewAdapter = PhotoGridRecyclerViewAdapter()

        this.photoGridRecyclerViewAdapter.submitList(photoList)

        // 여러가지 방식으로 할 수 있따(일반적으로는 리니어레이아웃 매니저 사용)
        my_photo_recycler_view.layoutManager = GridLayoutManager(this,2,GridLayoutManager.VERTICAL, false)
        my_photo_recycler_view.adapter = this.photoGridRecyclerViewAdapter
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "PhotoCollectionActivity - onCreateOptionsMenu() is called")
        val inflater = menuInflater
        inflater.inflate(R.menu.top_app_bar_menu, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        this.mySearchView = menu?.findItem(R.id.search_menu_item)?.actionView as SearchView
        this.mySearchView.apply {
            this.queryHint = "검색어를 입력해주세요"

            this.setOnQueryTextListener(this@PhotoCollectionActivity)

            this.setOnQueryTextFocusChangeListener { _, hasExpaned ->
                when(hasExpaned){
                    true ->{
                        Log.d(TAG, "서치뷰 열림")
//                        linear_search_history_view.visibility = View.VISIBLE
                        handleSearchViewUi()
                    }
                    false ->{
                        Log.d(TAG, "서치뷰 닫힘")
                        linear_search_history_view.visibility = View.INVISIBLE
                    }
                }
            }

            // 서치뷰에서 에딧텍스를 가져온다.
            mySearchViewEditText = this.findViewById(androidx.appcompat.R.id.search_src_text)

            // 에딧텍스트 옵저버블
            val editTextChangeObservable = mySearchViewEditText.textChanges()

            val searchEditTextSubscription : Disposable =
                // 옵저버블에 오퍼레이터를 추가
                editTextChangeObservable
                    // 글자가 입력되고 나서 0.8 초 후에 onNext 이벤트로 데이터 흘려보내기
                    .debounce(800, TimeUnit.MILLISECONDS)
                    // IO 쓰레드에서 돌리겠다.
                    // Scheduler instance intended for IO-bound work.
                    // 네트워크 요청, 파일 읽기,쓰기, 디비처리 등
                    .subscribeOn(Schedulers.io())
                    // 구독을 통해 이벤트 응답 받기
                    .subscribeBy(
                        onNext = {
                            Log.d("RX", "onNext : $it")
                            if (it.isNotEmpty()){
                                searchPhotoApiCall(it.toString())
                            }
                        },
                        onComplete = {
                            Log.d("RX", "onComplete")
                        },
                        onError = {
                            Log.d("RX", "onError : $it")
                        }
                    )
            myCompositeDisposable.add(searchEditTextSubscription)
        }


        this.mySearchViewEditText.apply {
            this.filters = arrayOf(InputFilter.LengthFilter(12))
            this.setTextColor(Color.WHITE)
            this.setHintTextColor(Color.WHITE)
        }
        return true
    }


    // 서치뷰 검색어 입력 이벤트
    // 검색버튼이 클리되었을때
    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.d(TAG, "PhotoCollectionActivity - onQueryTextSubmit() is called / query : $query")
        if(!query.isNullOrEmpty()){
            this.top_app_bar.title = query

            //TODO:: api 호출
            //TODO:: 검색어 저장

            this.insertSearchTermHistory(query)
            this.searchPhotoApiCall(query)

        }
//        this.mySearchView.setQuery("",false)
//        this.mySearchView.clearFocus()
        this.top_app_bar.collapseActionView()

        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Log.d(TAG, "PhotoCollectionActivity - onQueryTextChange() is called / newText: $newText")
//        val userInputText = newText ?: ""
        val userInputText = newText.let{
            it
        }?: ""

        if(userInputText.count() == 12){
            Toast.makeText(this, "검색어는 12자 까지만 입력 가능합니다.", Toast.LENGTH_SHORT).show()
        }

//        if(userInputText.length in 1..12){
//            searchPhotoApiCall(userInputText)
//        }

        return true
    }

    override fun onCheckedChanged(switch: CompoundButton?, isChecked: Boolean) {
        when(switch){
            search_history_mode_switch -> {
                if(isChecked == true){
                    Log.d(TAG, "검색어 저장기능 온")
                    SharedPrefManager.setSearchHistoryMode(isActivated = true)
                } else{
                    Log.d(TAG, "검색어 저장기능 오프")
                    SharedPrefManager.setSearchHistoryMode(isActivated = false)
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when(view){
            clear_search_history_button ->{
                Log.d(TAG, "검색 기록 삭제 버튼이 클릭 되었다.")
                SharedPrefManager.clearSearchHistoryList()
                this.searchHistoryList.clear()
                // ui 처리
                handleSearchViewUi()
            }
        }
    }


    // 검색 아이템 버튼 이벤트
    override fun onSearchItemClicked(position: Int) {
        Log.d(TAG, "PhotoCollectionActivity - onSearchItemClicked() is called")
        val queryString = this.searchHistoryList[position].term
        searchPhotoApiCall(queryString)
        top_app_bar.title = queryString

        this.insertSearchTermHistory(searchTerm=queryString)
        this.top_app_bar.collapseActionView()

    }

    // 검색 아이템삭제 버튼 이벤트
    override fun onSearchItemDeleteClicked(position: Int) {
        Log.d(TAG, "PhotoCollectionActivity - onSearchItemDeleteClicked() is called")
        // 해당 요소 삭제
        this.searchHistoryList.removeAt(position)
        // 데이터 덮어쓰기
        SharedPrefManager.storeSearchHistoryList(this.searchHistoryList)
        // 데이터 변경 됐다고 알려줌
        this.mySearchHistoryRecyclerViewAdapter.notifyDataSetChanged()

        handleSearchViewUi()
    }

    // 사진 검색 API 호출
    private fun searchPhotoApiCall(query: String){
        RetrofitManager.instance.searchPhotos(searchTerm = query,completion = {status,list ->
            when(status){
                RESPONSE_STATUS.OKAY -> {
                    Log.d(TAG, "PhotoCollectionActivity - searchPhotoApiCall() is called 응답 성공 / list.size: ${list?.size}")

                    if (list != null){
                        this.photoList.clear()
                        this.photoList = list
                        this.photoGridRecyclerViewAdapter.submitList(this.photoList)
                        this.photoGridRecyclerViewAdapter.notifyDataSetChanged()
                    }
                }
                RESPONSE_STATUS.NO_CONTENT ->{
                    Toast.makeText(this,"$query 에 대한 검색 결과가 없습니다.",Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun handleSearchViewUi(){
        Log.d(TAG, "PhotoCollectionActivity - handleSearchViewUi() is called / size : ${this.searchHistoryList.size}")

        if(this.searchHistoryList.size > 0){
            search_history_recycler_view.visibility = View.VISIBLE
            search_history_recycler_label.visibility = View.VISIBLE
            clear_search_history_button.visibility = View.VISIBLE

        }else{
            search_history_recycler_view.visibility = View.INVISIBLE
            search_history_recycler_label.visibility = View.INVISIBLE
            clear_search_history_button.visibility = View.INVISIBLE
        }
    }


    // 검색어 저장
    private fun insertSearchTermHistory(searchTerm: String){
        Log.d(TAG, "PhotoCollectionActivity - insertSearchTermHistory() is called")

        if(SharedPrefManager.CheckSearchHistoryMode() == true){
            // 중복 아이템 삭제
            var indexListToRemove = ArrayList<Int>()

            this.searchHistoryList.forEachIndexed{ index, searchDataItem ->
                if(searchDataItem.term == searchTerm){
                    Log.d(TAG, "index: $index")
                    indexListToRemove.add(index)
                }
            }

            indexListToRemove.forEach {
                this.searchHistoryList.removeAt(it)
            }

            // 새 아이템 넣기
            val newSearchData = SearchData(term = searchTerm, timestamp = Date().toSimpleString())
            this.searchHistoryList.add(newSearchData)

            // 기존 데이터에 덮어쓰기
            SharedPrefManager.storeSearchHistoryList(this.searchHistoryList)
            this.mySearchHistoryRecyclerViewAdapter.notifyDataSetChanged()

        }
    }
}