package com.hjchae.recycler_view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hjchae.recycler_view.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LostFoundAdapter
    private var currentUser = "student1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // 어답터 생성
        adapter = LostFoundAdapter(currentUser, { item: LostFoundItem ->
            Toast.makeText(baseContext, item.date, Toast.LENGTH_LONG).show()
        })
        // 리사이클러 뷰의 layoutManager, adapter 설정
        binding.contentMain.recyclerView.adapter = adapter
        binding.contentMain.recyclerView.layoutManager = LinearLayoutManager(this)

        val now = System.currentTimeMillis()
        // 항목 하나 생성 및 추가
        var item = LostFoundItem(
            now,
            LostFoundItem.TYPE_FOUND,
            "검은색 우산",
            "3호관 2층",
            "2026-09-30",
            LostFoundItem.STATUS_OPEN,
            R.drawable.sample_umbrella,
            "student1",
            now,
            "this is black umbrella"
        )
        adapter.addItem(item)

        item = LostFoundItem(
            place = "중앙도서관 3층 열람실",
            id = now - 20_000,
            type = LostFoundItem.TYPE_LOST,
            name = "흰색 무선 이어폰",
            date = "2026-07-25",
            status = LostFoundItem.STATUS_COMPLETED,
            imageUri = R.drawable.sample_earbuds,
            ownerId = "student2",
            createdAt = now - 20_000,
            description = "this is bean sprout"
        )
        adapter.addItem(item)

        item = LostFoundItem(
            place = "중앙도서관 3층 열람실",
            id = now - 20_000,
            type = LostFoundItem.TYPE_LOST,
            name = "학생증",
            date = "2026-07-25",
            status = LostFoundItem.STATUS_COMPLETED,
            imageUri = R.drawable.sample_student_card,
            ownerId = "student2",
            createdAt = now - 20_000,
            description = "this is student card"
        )
        adapter.addItem(item)

        // 스피너 설정
        val types = arrayOf("전체", "분실물", "습득물")
        val aadapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)

        // spinnerTypeFilter에 어답터 설정
        binding.contentMain.spinnerTypeFilter.adapter = aadapter

        // 스피너에 의해 선택된 filter 를 반영하여 보여줄 목록을 만들기 위함
        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                adapter.applyFilters(
                    binding.contentMain.spinnerTypeFilter.selectedItemPosition,
                    binding.contentMain.spinnerStatusFilter.selectedItemPosition
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.contentMain.spinnerTypeFilter.onItemSelectedListener = filterListener
        binding.contentMain.spinnerStatusFilter.onItemSelectedListener = filterListener

        binding.fab.setOnClickListener {
            Snackbar.make(it, "학교로 문의", Snackbar.LENGTH_INDEFINITE)
                .setAction("문의", object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0212345678"))
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(baseContext, "실행할 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }).setAnchorView(R.id.fab).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    } // onCreate

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> finish()
        }

        return super.onOptionsItemSelected(item)
    }
}