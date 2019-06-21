package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.demoapplication.R
import com.example.demoapplication.presenter.MatchPresenter
import com.example.demoapplication.presenter.view.MatchView
import com.kotlin.base.ui.fragment.BaseMvpFragment

/**
 * A simple [Fragment] subclass.
 *
 */
class MatchFragment : BaseMvpFragment<MatchPresenter>(), MatchView {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_match, container, false)
    }


}
