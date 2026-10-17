package com.hjchae.sqlite

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hjchae.sqlite.databinding.ItemLostFoundBinding
import kotlin.collections.filter
import kotlin.collections.toMutableList
import kotlin.concurrent.thread

// 뷰홀더 클래스
class ItemViewHolder(
    private val binding: ItemLostFoundBinding,
    private val currentUser: String,
    private val onItemClick: (LostFoundItem) -> Unit,
    private val onItemLongClick: (LostFoundItem) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    // 각항목의 데이터를 뷰홀더의 각 뷰에 매핑(bind)
    fun bind(item: LostFoundItem) {
        with(binding) {
            tvItemName.text = item.name
            tvPlace.text = "장소: ${item.place}"
            tvDate.text = "날짜: ${item.date}"

            if (item.type == LostFoundItem.TYPE_LOST) {
                tvType.text = root.context.getString(R.string.type_lost)
                tvType.setBackgroundResource(R.drawable.bg_badge_lost)
            } else {
                tvType.text = root.context.getString(R.string.type_found)
                tvType.setBackgroundResource(R.drawable.bg_badge_found)
            }

            // status에 따른 설정
            if (item.status == LostFoundItem.STATUS_COMPLETED) {
                tvStatus.text = root.context.getString(R.string.status_completed)
                tvStatus.setBackgroundResource(R.drawable.bg_badge_done)
            } else {
                tvStatus.text = root.context.getString(R.string.status_open)
                tvStatus.setBackgroundResource(R.drawable.bg_badge_open)
            }

            tvOwner.text = if (item.ownerId == currentUser) {
                root.context.getString(R.string.written_by_me, item.ownerId)
            } else {
                root.context.getString(R.string.written_by, item.ownerId)
            }

            ivItem.setImageResource(item.imageUri)
            // 클릭 이벤트 처리기 등록 : 항목 상세 보기
            root.setOnClickListener { onItemClick(item) }
            // 롱클릭 이벤트 처리기 등록 : 항목 삭제
            root.setOnLongClickListener {
                onItemLongClick(item)
                return@setOnLongClickListener true
            }
        } // with
    } // bind()
} // class ItemViewHolder

// 어답터 클래스
class LostFoundAdapter(
    private val currentUser: String,
    private val onItemClick: (LostFoundItem) -> Unit,
    private val onItemLongClick: (LostFoundItem) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {

    private var items: MutableList<LostFoundItem> =
        mutableListOf() // 타입과 status에 의해 필터링되어 리사이클러뷰에 보여줄 항목들의 리스트
    private var allitems: MutableList<LostFoundItem> = mutableListOf() // 모든 항목들의 리스트

    private var typeFilter = 0 // 타입 스피너에서 선택된 필터 인덱스
    private var statusFilter = 0 // status 스피너에서 선택된 필터 인덱스

    // onCreateViewHolder : 각 항목들의 뷰홀더를 생성할 때마다 호출되는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // 바인딩 객체 생성
        val binding =
            ItemLostFoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // 뷰홀더 객체를 생성해서 반환
        return ItemViewHolder(binding, currentUser, onItemClick, onItemLongClick)
    }

    // onBindViewHolder : 각 항목들을 리사이클러뷰에 보여줄 때마다 호출되는 함수
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        // 각 항목의 뷰 홀더에 어답터가 가지고 있는 데이터를 바인딩
        holder.bind(items[position])
    }

    // 리사이클러뷰에서 보여줄 항목의 갯수를 반환
    override fun getItemCount(): Int {
        return items.size
    }

    // 리사이클러뷰에서 보여줄 항목들의 리스트를 새로 설정
    fun submitList(newItems: List<LostFoundItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged() //  변경된 데이터를 리사이클러뷰가 다시 그릴 수 있도록 호출해야 함
    }

    fun loadItems(dao: LostFoundDao) {
        // 항목들을 DB로부터 읽어오기
        thread {
            val list = dao.getAll()
            allitems = list.toMutableList()
            Handler(Looper.getMainLooper()).post {
                submitList(allitems)
            }
        }
    }

    fun addItem(item: LostFoundItem, dao: LostFoundDao) {
        // database에 추가하고 현재 필터링에 맞춰 리사이클러 뷰에 반영
        thread {
            dao.insert(item)
            Handler(Looper.getMainLooper()).post {
                allitems.add(item)
                applyFilters()
            }
        }
    }

    fun updateItem(item: LostFoundItem, dao: LostFoundDao): Boolean {
        // database에 특정항목에 대한 변경을 반영하고 리사이클러 뷰에 반영
        if (item.id == -1L) return false

        // database에 update
        thread {
            dao.update(item)
            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        }

        return true
    }

    fun deleteItem(item: LostFoundItem, dao: LostFoundDao): Boolean {
        // database에서 특정항목을 삭제하고 리사이클러뷰에 반영
        if (item.id == -1L) return false

        // database에서 삭제하고 현재 필터링에 맞춰 리사이클러 뷰에 반영
        thread {
            dao.delete(item)
            Handler(Looper.getMainLooper()).post {
                allitems.remove(item)
                applyFilters()
            }
        }

        return true
    }

    // 변경된 타입과 status 필터정보를 저장하고 필터링을 반영
    fun setFilters(typeFilter: Int, statusFilter: Int) {
        this.typeFilter = typeFilter
        this.statusFilter = statusFilter
        applyFilters()
    }

    // 필터링 수행
    fun applyFilters() {
        // 타입에 따른 필터링을 위해 typeFilter를 스트링값으로 변경 : LOST / FOUND
        val type = when (typeFilter) {
            1 -> LostFoundItem.TYPE_LOST
            2 -> LostFoundItem.TYPE_FOUND
            else -> null
        }
        // 상태에 따른 필터링을 위해 statusFilter를 스트링값으로 변경 : OPEN / COMPLETED
        val status = when (statusFilter) {
            1 -> LostFoundItem.STATUS_OPEN
            2 -> LostFoundItem.STATUS_COMPLETED
            else -> null
        }

        // 모든 항목중에서 필터된 항목들만 추려서 저장
        // filter() 함수 : 대상이 되는 리스트에서 각 항목을 읽어 주어진 조건에 맞는 항목들만 추려서 새 리스트를 만들어서 반환해줌
        val filtered = allitems.filter { item ->
            val matchesType = type == null || item.type == type
            val matchesStatus = status == null || item.status == status
            matchesType && matchesStatus
        }

        // adapter의 items에 반영하고 리사이클러에 반영하여 보여줌
        submitList(filtered)
    } // applyFilters
} // class LostFoundAdapter
