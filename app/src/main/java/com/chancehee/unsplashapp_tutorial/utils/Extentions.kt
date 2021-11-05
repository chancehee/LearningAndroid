package com.chancehee.unsplashapp_tutorial.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

// 사용할 기능만을 사용하기 위한 설정
fun EditText.onMyTextChanged(completion: (Editable?) -> Unit){
    this.addTextChangedListener(object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(editable : Editable?) {
            completion(editable)
        }
    })
}