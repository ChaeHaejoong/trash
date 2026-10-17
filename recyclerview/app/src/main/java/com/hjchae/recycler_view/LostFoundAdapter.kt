package com.hjchae.recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hjchae.recycler_view.databinding.ItemLostFoundBinding

class ItemViewHolder(
    private val binding: ItemLostFoundBinding,
    private val currentUser: String,
    private val onItemClick: (LostFoundItem) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: LostFoundItem) {
        with(binding) {
            ivItem.setImageResource(R.drawable.placeholder)
            tvItemName.text = item.name
            tvPlace.text = "장소: ${item.place}"
            tvDate.text = "descripton: ${item.description}"

            if (item.type == LostFoundItem.TYPE_LOST) {
                tvType.text = root.context.getString(R.string.type_lost)
                tvType.setBackgroundResource(R.drawable.bg_badge_lost)
            } else {
                tvType.text = root.context.getString(R.string.type_found)
                tvType.setBackgroundResource(R.drawable.bg_badge_found)
            }

            // status에 따른 설정
            if (item.status == LostFoundItem.STATUS_OPEN) {
                tvStatus.text = "보관중";
                tvStatus.setBackgroundResource(R.drawable.bg_badge_open)
            } else {
                tvStatus.text = root.context.getString(R.string.status_completed)
                tvStatus.setBackgroundResource(R.drawable.bg_badge_done)
            }

            tvOwner.text = if (item.ownerId == currentUser) {
                root.context.getString(R.string.written_by_me, item.ownerId)
            } else {
                root.context.getString(R.string.written_by, item.ownerId)
            }

            ivItem.setImageResource(item.imageUri)
            // 클릭 이벤트 처리기 등록
            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

class LostFoundAdapter(
    private val currentUser: String,
    private val onItemClick: (LostFoundItem) -> Unit
) : RecyclerView.Adapter<ItemViewHolder>() {

    private var items: MutableList<LostFoundItem> = mutableListOf()
    private var allitems: MutableList<LostFoundItem> = mutableListOf()

    // onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // binding, currentUser, onItemClick
        val binding =
            ItemLostFoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ItemViewHolder(binding, currentUser, onItemClick)

        return viewHolder
    }

    // onBindViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<LostFoundItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    fun addItem(item: LostFoundItem) {
        allitems.add(item)
        submitList(allitems)
    }

    fun applyFilters(typeFilter: Int, statusFilter: Int) {
        // 타입에 따른 필터링
        val type =
            when (typeFilter) {//binding.contentMain.spinnerTypeFilter.selectedItemPosition) {
                1 -> LostFoundItem.TYPE_LOST
                2 -> LostFoundItem.TYPE_FOUND
                else -> null
            }
        // 상태에 따른 필터링
        val status =
            when (statusFilter) {//binding.contentMain.spinnerStatusFilter.selectedItemPosition) {
                1 -> LostFoundItem.STATUS_OPEN
                2 -> LostFoundItem.STATUS_COMPLETED
                else -> null
            }

        // 모든 항목중에서 필터된 항목들만 출려서 저장
        val filtered = allitems.filter { item ->
            val matchesType = type == null || item.type == type
            val matchesStatus = status == null || item.status == status
            matchesType && matchesStatus
        }

        // adapter에 반영하여 보여줌
        submitList(filtered)
    }
}
