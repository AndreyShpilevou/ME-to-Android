package app.appsList

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.dao.AppItem
import app.utils.onClick
import app.utils.onLongClick

class AppsListAdapter : RecyclerView.Adapter<AppsListAdapter.ViewHolder>() {

    var list: List<AppItem> = listOf()

    inner class ViewHolder(itemView: AppItemView) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(appItem: AppItem){
            (itemView as AppItemView).setItem(appItem)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(list: List<AppItem>){
        this.list = list

        notifyDataSetChanged()
    }

    fun getItem(position: Int): AppItem = list[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        AppItemView(parent.context)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])

        holder.itemView.onClick {
            control?.onClickItem(position)
        }

        holder.itemView.onLongClick {
            control?.onLongClickItem(position, it)
            false
        }
    }

    override fun getItemCount() = list.size

    var control: Control? = null

    interface Control {

        fun onClickItem(position: Int)

        fun onLongClickItem(position: Int, view: View)

    }

}