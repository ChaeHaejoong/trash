package com.dongyang.ac_navigation2
import android.os.Bundle
import android.view.Menu
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.dongyang.ac_navigation2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        //nav_host_fragment_content_main : NavHostFragment의 id
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.guideFragment, R.id.facilityFragment
            ), drawerLayout
        )
        // 과제 1:
        // 1. NavigationView의 메뉴파일에 항목을 추가 : 학교 소식, 아이콘은 아무거나, id는 schoolFragment
        // 2. SchoolFragment 생성(blank fragment로 생성) : layout file까지
        // 3. Navigation Graph 파일 수정 : 스쿨프레그먼트를 추가
        // 4. MainActivity 수정 : school fragment로 이동했을 때 툴바에 햄버거 버튼(줄 세개 버튼)이 나타나도록
        // 5. 제출 : MainActivity.kt, 네비게이션 메뉴파일, 네비게이션 그래프파일, 실행 후 네비게이션 메뉴에
        // 학교 소식이 추가된 화면 하나, 학교소식을 클릭해서 이동한 결과 이미지 파일 하나
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}