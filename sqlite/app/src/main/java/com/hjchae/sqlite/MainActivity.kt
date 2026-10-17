package com.hjchae.sqlite

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.hjchae.sqlite.databinding.ActivityMainBinding
import com.hjchae.sqlite.databinding.DialogItemFormBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.toString

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding // binding 객체
    private lateinit var adapter: LostFoundAdapter // 리사이클러뷰와 연동될 어답터객체
    private var currentUser = "student1" // 현재 사용자

    private lateinit var database: LostFoundDatabase // 데이터베이스 객체
    private lateinit var dao: LostFoundDao // 데이터 액세스 객체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 레이아웃 인플레이션
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액티비티와 툴바를 서로 연동시킴
        setSupportActionBar(binding.toolbar)

        // database 객체와 dao 객체 생성
        database =
            Room.databaseBuilder(applicationContext, LostFoundDatabase::class.java, "LOSTFOUND_T")
                .build()
        dao = database.lostFoundDao()

        adapter = LostFoundAdapter(currentUser, { item: LostFoundItem ->
            // 상세 정보 보여주고 수정하기
            showItemFormDialog(item)
        }, { item: LostFoundItem ->
            // 항목 삭제
            adapter.deleteItem(item, dao)
        })

        // 리사이클러뷰에 레이아웃매니저와 어답터를 연동 설정 시킴
        binding.contentMain.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.contentMain.recyclerView.adapter = adapter

        // DB에서 데이터 로드
        adapter.loadItems(dao)

        // 버튼 클릭하면 항목 추가
        binding.contentMain.btnAdd.setOnClickListener {
            // 새로운 항목을 등록하기
            showItemFormDialog(null)
        }

        // 스피너에 보여줄 내용을 위한 배열 설정
        val types = arrayOf("전체", "분실물", "습득물")
        // 스피너를 위한 어답터 생성
        // 첫번째 매개변수 : context 객체, 두번째 매개변수 : 스피너항목을 위한 레이아웃, 세번째 매개변수 : 스피너 내용을 위한 배열
        val aadapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)

        // 타입 필터링을 위한 스피너에 어답터 설정
        binding.contentMain.spinnerTypeFilter.adapter = aadapter

        // 스피너에서 항목을 선택할 때 발생하는 이벤트를 처리하기 위한 이벤트 처리기 생성
        val filterListener = object : AdapterView.OnItemSelectedListener {
            // 스피너에서 항목선택하면 아래의 메서드 호출이 됨
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                // 리사이클러뷰에서 보여줄 항목들을 필터링하기 위해 어답터의 메소드를 호출
                adapter.setFilters(
                    binding.contentMain.spinnerTypeFilter.selectedItemPosition,
                    binding.contentMain.spinnerStatusFilter.selectedItemPosition
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        // 각 스피너에서 항목 선택시 이벤트를 처리하기 위해 이벤트 처리기 등록
        // 스피너에서 항목을 선택할 때마다 필터링이라는 동일한 작업을 수행해야 하므로
        // 하나의 이벤트 처리기를 두개의 스피너가 공유해서 사용
        binding.contentMain.spinnerTypeFilter.onItemSelectedListener = filterListener
        binding.contentMain.spinnerStatusFilter.onItemSelectedListener = filterListener

        // 플로팅액션버튼을 클릭하면 스낵바를 보여주고
        // 스낵바의 액션버튼을 클릭하면 전화거는 화면으로 이동하도록 함
        binding.fab.setOnClickListener {
            Snackbar.make(it, "학교로 문의", Snackbar.LENGTH_LONG)
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
                }).setAnchorView(R.id.fab).show() // 스낵바가 플로팅액션버튼 위에서 나타나도록 앵커뷰를 설정
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    } // onCreate

    // 새로운 항목을 등록하거나 수정해야 할 때 입력 다이얼로그를 띄워주는 함수
    private fun showItemFormDialog(existingItem: LostFoundItem?) {
        // existingItem 이 null이면 새로 등록. null이 아니면 기존의 항목을 수정하는 경우
        // 수정하는 경우에는 현재 로긴한 사용자(currentUser)와 작성자가 동일한 경우에만 수정이 가능
        if (existingItem != null && existingItem.ownerId != currentUser) {
            Toast.makeText(baseContext, "작성자만 수정가능", Toast.LENGTH_LONG).show()
            return
        }

        // 커스텀 다이얼로그를 띄우기 위해 다이얼로그(대화창)을 위한 레이아웃 인플레이션을 수행
        val dialogBinding = DialogItemFormBinding.inflate(layoutInflater)

        // 대화창의 타이틀 설정 : 수정이냐 새로 등록이냐에 따라
        dialogBinding.tvFormTitle.text = if (existingItem == null) "등록" else "수정"


        if (existingItem == null) { // 새로 등록하는 경우에는 오늘 날짜로 날짜정보를 셋팅
            dialogBinding.etDate.setText(
                SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Calendar.getInstance().time)
            )
        } else { // 수정인 경우에는 기존 항목의 값을 읽어 다이얼로그 창의 각 항목에 값을 보여줌
            // 수정인 경우에는 기존에 입력했던 값을 보여주고 수정해야 하기 때문에
            dialogBinding.etItemName.setText(existingItem.name) // 항목의 이름
            dialogBinding.etPlace.setText(existingItem.place) // 장소
            dialogBinding.etDate.setText(existingItem.date) // 날짜
            dialogBinding.etDescription.setText(existingItem.description) // 상세정보
            // status 설정 : 스피너의 선택된 항목을 설정
            dialogBinding.spinnerStatus.setSelection(if (existingItem.status == LostFoundItem.STATUS_OPEN) 0 else 1)

            if (existingItem.type == LostFoundItem.TYPE_FOUND) { // 타입
                dialogBinding.rbFound.isChecked = true
            } else {
                dialogBinding.rbLost.isChecked = true
            }
        }

        // custom dialog 생성
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        // 대화창의 버튼들에 대한 클릭이벤트 처리
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() } //  대화창을 닫음
        dialogBinding.btnSave.setOnClickListener { // 입력된 사항을 저장(새 항목을 등록하거나 수정사항 반영)
            saveFromDialog(dialogBinding, existingItem, dialog)
        }

        // dialog 보여주기
        dialog.show()
    }

    // 입력된 사항을 저장(새 항목을 등록하거나 수정사항 반영)해 주는 함수
    private fun saveFromDialog(
        dialogBinding: DialogItemFormBinding, existingItem: LostFoundItem?, dialog: AlertDialog
    ) {
        // 입력된 내용을 추출
        val name = dialogBinding.etItemName.text.toString().trim() // 이름
        val place = dialogBinding.etPlace.text.toString().trim() // 장소
        val date = dialogBinding.etDate.text.toString().trim() // 날짜
        val description = dialogBinding.etDescription.text.toString().trim() // 상세정보

        // 새로 등록하는 경우
        if (existingItem == null) {
            val now = System.currentTimeMillis() // 현재 시간 계산하여 저장
            // 등록하기 위해 새 항목을 생성
            val item = LostFoundItem(
                id = now - 30000,
                type = if (dialogBinding.rbLost.isChecked) {
                    LostFoundItem.TYPE_LOST
                } else {
                    LostFoundItem.TYPE_FOUND
                },
                name = name,
                place = place,
                date = date,
                status = if (dialogBinding.spinnerStatus.selectedItemPosition == 1) {
                    LostFoundItem.STATUS_COMPLETED
                } else {
                    LostFoundItem.STATUS_OPEN
                },
                description = description,
                ownerId = currentUser,
                createdAt = now
            )
            adapter.addItem(item, dao)
            // DB에 저장하고 리사이클러뷰에서 보여주기 위해 어답터 메소드를 호출
        } else { // 기존의 항목을 수정하는 경우
            existingItem.type = if (dialogBinding.rbLost.isChecked) {
                LostFoundItem.TYPE_LOST
            } else {
                LostFoundItem.TYPE_FOUND
            }
            existingItem.name = name
            existingItem.place = place
            existingItem.date = date
            existingItem.status = if (dialogBinding.spinnerStatus.selectedItemPosition == 1) {
                LostFoundItem.STATUS_COMPLETED
            } else {
                LostFoundItem.STATUS_OPEN
            }
            existingItem.description = description
            adapter.updateItem(existingItem, dao)
        }
        dialog.dismiss()
    }

    // 메뉴를 생성할 때 호출하는 메소드
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 메뉴 리소스 파일을 읽어 메뉴판을 채움
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // 각 메뉴의 항목을 클릭했을 때 호출되는 메소드
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) { // 선택된 메뉴항목의 id에 따라 적당히 이벤트를 처리할 수 있게함.
            R.id.menu_logout -> finish() // 이 액티비티를 완전히 종료시킴
        }

        return super.onOptionsItemSelected(item)
    }
}